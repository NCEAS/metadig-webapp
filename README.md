# mdq-webapp
Web service for interacting with the [MetaDIG Engine](https://github.com/NCEAS/metadig-engine) API.

This project builds a 'quality.war' file that can be deployed in the Tomcat webapps directory. Depending on the Tomcat configuration, the war may be unpacked automatically each time a new version is copied in place.

We have been experimenting with Circle CI to provide continuous builds as well as tagged releases of this product.
See Circle CI in case a production-ready release is available: https://circleci.com/gh/NCEAS/mdq-webapp

## The REST API
Typically, the API is used to:
- list available/enabled checks and suites
- run checks or suites on a submitted metadata document

In the future, we will expose more administrative functions like modifying and adding checks and suites, but for now those actions are reserved for the MDQ admins.

## Aggregations
We are relying on Metacat's SOLR indexing capabilities to summarize 'Run' documents. These objects capture the complete results of running a suite against a given metadata document and metacat-index has a processor that knows how to extract pertinent fields from the run document for the index. Then faceted queries can be performed over the SOLR index and many different summary reports can be generated from the raw index information. See the MDQ index configuration in metacat-index for more details on the fields that are extracted: https://code.ecoinformatics.org/code/metacat/trunk/metacat-index/src/main/resources/application-context-mdq.xml
