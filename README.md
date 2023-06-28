# MetaDIG webapp
Web service for interacting with the [MetaDIG Engine](https://github.com/NCEAS/metadig-engine) API.

This project builds a 'metadig-webapp.war' file that can be deployed in the Tomcat webapps directory. Depending on the Tomcat configuration, the war may be unpacked automatically each time a new version is copied in place.

The Docker container is also delivered to DockerHub that can be obtained with

```
docker pull ghcr.io/nceas/metadig-controller
```

## The REST API
Typically, the API is used to:
- list available/enabled checks and suites
- run checks or suites on a submitted metadata document

In the future, we will expose more administrative functions like modifying and adding checks and suites, but for now those actions are reserved for the MDQ admins.

### metadig-webapp

The [`metadig-webapp` git repository](https://github.com/NCEAS/metadig-webapp) provides a REST API fronend to metadig-engine that can be deployed as a Tomcat servlet or Docker container.

This repo builds the `metadig-webapp.war` file that can be run as a servlet inside a Tomcat instance. 

Also built is the `metadig-controller` Docker container that includes a Tomcat instance that the `metadig-webapp` servlet runs in. This servlet implements the metadig REST API.

The metadig-webapp is dependant on the metadig-engine jar file, which is provided by the metadig-engine build.

In order to build metadig-webapp:
- mvn clean
- mvn package

The metadig-webapp Docker images are build with the command:
```
mvn docker:build docker:push
```

These images are pushed to the `metadig` Docker Hub, for example: https://hub.docker.com/repository/docker/metadig/metadig-controller.

Authorization for Docker Hub can be setup in several ways. One method is to create or append to the local file ~/.m2/settings, which allows the Maven build to set the necessary credentials to DockerHub during the push. An example of this file is:

```
<settings xmlns="http://maven.apache.org/SETTINGS/1.1.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.1.0 http://maven.apache.org/xsd/settings-1.1.0.xsd">
  <localRepository/>
  <interactiveMode/>
  <offline/>
  <pluginGroups/>
  <servers>
    <server>
      <id>docker.io</id>
      <username>[place username here]</username>
      <password>[place password here]</password>
    </server>
  </servers>
  <mirrors/>
  <proxies/>
  <profiles/>
  <activeProfiles/>
</settings>
```

The appropriate usename and password is available from the NCEAS secure repo.
