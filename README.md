# artifact-versions-maven-plugin
Maven plugin that determines the versions of one or more artifacts in the 
Maven repository and provides them as properties for further processing.

[![Java Maven Build](https://github.com/fuinorg/artifact-versions-maven-plugin/actions/workflows/maven.yml/badge.svg)](https://github.com/fuinorg/artifact-versions-maven-plugin/actions/workflows/maven.yml)
[![Coverage Status](https://sonarcloud.io/api/project_badges/measure?project=org.fuin%3Aartifact-versions-maven-plugin&metric=coverage)](https://sonarcloud.io/dashboard?id=org.fuin%3Aartifact-versions-maven-plugin)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.fuin/artifact-versions-maven-plugin/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.fuin/artifact-versions-maven-plugin/)
[![LGPLv3 License](http://img.shields.io/badge/license-LGPLv3-blue.svg)](https://www.gnu.org/licenses/lgpl.html)
[![Java Development Kit 17](https://img.shields.io/badge/JDK-17-green.svg)](https://openjdk.java.net/projects/jdk/17/)


## Usage
Add the plugin to your project:

```xml
<plugin>
    <groupId>org.fuin</groupId>
    <artifactId>artifact-versions-maven-plugin</artifactId>
    <version>0.1.0</version>
    <configuration>
        <!-- Add the artifacts you want to retrieve versions for with "groupId:artifactId" -->
        <artifacts>
            <artifact>org.fuin:utils4j</artifact>
        </artifacts>
    </configuration>
</plugin>
```

After the plugin is executed, there is are three new properties for each artifact:

| Version(s)    | Property                                    | Example                               |
|---------------|---------------------------------------------|---------------------------------------|
| Latest        | org.fuin.avmp.**latest**-GROUPID_ARTIFACTID | org.fuin.avmp.latest-org.fuin_utils4j |
| Latest Stable | org.fuin.avmp.**stable**-GROUPID_ARTIFACTID | org.fuin.avmp.stable-org.fuin_utils4j |
| All           | org.fuin.avmp.**all**-GROUPID_ARTIFACTID    | org.fuin.avmp.all-org.fuin_utils4j    |

A "stable" version is any version that is greater than 0.x.y and no "-SNAPSHOT".
In case no version is found, the property is **not** set.

You can use them in any other plugin:
```xml
<plugin>
    <groupId>com.github.ekryd.echo-maven-plugin</groupId>
    <artifactId>echo-maven-plugin</artifactId>
    <version>2.0.0</version>
    <inherited>false</inherited>
    <configuration>
        <message>
            Latest version of utils4j: ${org.fuin.avmp.latest-org.fuin_utils4j}
            Stable version of utils4j: ${org.fuin.avmp.stable-org.fuin_utils4j}
            All versions of utils4j: ${org.fuin.avmp.all-org.fuin_utils4j}
        </message>
    </configuration>
    <executions>
        <execution>
            <id>end</id>
            <goals>
                <goal>echo</goal>
            </goals>
            <phase>validate</phase>
        </execution>
    </executions>
</plugin>

```