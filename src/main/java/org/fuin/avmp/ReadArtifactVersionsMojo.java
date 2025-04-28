package org.fuin.avmp;

import com.vdurmont.semver4j.Semver;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.impl.RemoteRepositoryManager;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.VersionRangeRequest;
import org.eclipse.aether.resolution.VersionRangeResolutionException;
import org.eclipse.aether.version.Version;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Mojo(name = "read",
        defaultPhase = LifecyclePhase.INITIALIZE,
        requiresProject = true,
        threadSafe = true)
public class ReadArtifactVersionsMojo extends AbstractMojo {

    private static final String BASE_KEY = "org.fuin.avmp";

    private static final String KEY_LATEST = BASE_KEY + ".latest";

    private static final String KEY_STABLE = BASE_KEY + ".stable";

    private static final String KEY_ALL = BASE_KEY + ".all";

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    @Inject
    private RepositorySystem repositorySystem;

    @Inject
    RemoteRepositoryManager remoteRepositoryManager;

    @Parameter(defaultValue = "${project.remoteProjectRepositories}", readonly = true)
    private List<RemoteRepository> projectRepos;

    @Parameter(defaultValue = "${repositorySystemSession}", readonly = true)
    private RepositorySystemSession repoSession;

    @Parameter(property = "artifacts", required = true, readonly = true)
    List<String> artifacts;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        for (String artifact : artifacts) {
            final Arti arti = Arti.parse(artifact);
            final List<Semver> versions = getVersions(arti);
            project.getProperties().put(KEY_ALL + "-" + arti.toValue(), all(versions));
            latest(versions).ifPresent(version -> project.getProperties().put(KEY_LATEST + "-" + arti.toValue(), version));
            stable(versions).ifPresent(version -> project.getProperties().put(KEY_STABLE + "-" + arti.toValue(), version));
        }
    }

    private Optional<String> stable(List<Semver> versions) {
        return versions.stream().filter(Semver::isStable).max(Semver::compareTo).map(Semver::toString);
    }

    private Optional<String> latest(List<Semver> versions) {
        return versions.stream().max(Semver::compareTo).map(Semver::toString);
    }

    private String all(List<Semver> versions) {
        return versions.stream().map(Semver::toString).collect(Collectors.joining(", "));
    }

    private List<Semver> getVersions(Arti arti) throws MojoExecutionException {
        final VersionRangeRequest rangeRequest = new VersionRangeRequest();
        final Artifact artifact = arti.toAllVersionsArtifact();
        rangeRequest.setArtifact(artifact);
        rangeRequest.setRepositories(projectRepos);
        try {
            final List<Version> versions = repositorySystem.resolveVersionRange(repoSession, rangeRequest).getVersions();
            final List<Semver> semvers = new ArrayList<>(versions.stream().map(version -> new Semver(version.toString(), Semver.SemverType.LOOSE)).toList());
            Collections.sort(semvers);
            return semvers;
        } catch (VersionRangeResolutionException ex) {
            throw new MojoExecutionException("Failed to resolve available versions for: " + arti, ex);
        }
    }

    private record Arti(String groupId, String artifactId) {

        public Artifact toAllVersionsArtifact() {
            return new DefaultArtifact(groupId + ":" + artifactId + ":(0,]");
        }

        public String toValue() {
            return groupId + "_" + artifactId;
        }

        public static Arti parse(String str) {
            final int p = str.indexOf(':');
            if (p == -1) {
                throw new IllegalArgumentException("Invalid artifact format: " + str);
            }
            final String groupId = str.substring(0, p);
            final String artifactId = str.substring(p + 1);
            return new Arti(groupId, artifactId);
        }

    }

}
