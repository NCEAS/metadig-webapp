# MetaDIG webapp
Web service for interacting with the [MetaDIG Engine](https://github.com/NCEAS/metadig-engine) API.

This project builds a 'metadig-webapp.war' file that can be deployed in the Tomcat webapps directory. Depending on the Tomcat configuration, the war may be unpacked automatically each time a new version is copied in place.

The Docker container is also delivered to GHCR that can be obtained with 

```
docker pull ghcr.io/nceas/metadig-controller
```

## The REST API
Typically, the API is used to:
- list available/enabled checks and suites
- run checks or suites on a submitted metadata document

In the future, we will expose more administrative functions like modifying and adding checks and suites, but for now those actions are reserved for the MDQ admins.

### metadig-webapp

The [`metadig-webapp` git repository](https://github.com/NCEAS/metadig-webapp) provides a REST API front end to metadig-engine that can be deployed as a Tomcat servlet or Docker container.

This repo builds the `metadig-webapp.war` file that can be run as a servlet inside a Tomcat instance. 

Also built is the `metadig-controller` Docker container that includes a Tomcat instance that the `metadig-webapp` servlet runs in. This servlet implements the metadig REST API.

The metadig-webapp is dependent on the metadig-engine jar file, which is provided by the metadig-engine build.

In order to build metadig-webapp:
- mvn clean
- mvn package

The metadig-webapp Docker images are built automatically with GitHub actions.

These images are pushed to the `metadig` Docker Hub, for example: https://hub.docker.com/repository/docker/metadig/metadig-controller.
