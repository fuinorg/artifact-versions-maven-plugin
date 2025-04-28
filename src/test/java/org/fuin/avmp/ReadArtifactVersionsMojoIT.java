package org.fuin.avmp;

import com.soebes.itf.jupiter.extension.MavenGoal;
import com.soebes.itf.jupiter.extension.MavenJupiterExtension;
import com.soebes.itf.jupiter.extension.MavenTest;
import com.soebes.itf.jupiter.maven.MavenExecutionResult;

import static com.soebes.itf.extension.assertj.MavenITAssertions.assertThat;

/**
 * Test for the {@link ReadArtifactVersionsMojo} class.
 */
@MavenJupiterExtension
public class ReadArtifactVersionsMojoIT {

    @MavenTest
    @MavenGoal("${project.groupId}:${project.artifactId}:${project.version}:read")
    @MavenGoal("com.github.ekryd.echo-maven-plugin:echo-maven-plugin:1.3.2:echo")
    void testRead(MavenExecutionResult result) {
        assertThat(result).isSuccessful();
        assertThat(result)
                .out()
                .info()
                .anyMatch(line -> {
                    System.err.println(line);
                    return line.contains("Latest version of utils4j:");
                });
    }

}

