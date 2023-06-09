#FROM tomcat:8.5
FROM tomcat:8-alpine

LABEL org.opencontainers.image.source="https://github.com/nceas/metadig-webapp"
LABEL org.opencontainers.image.title="metadig-webapp"
LABEL org.opencontainers.image.version="2.4.0"
LABEL maintainer="Jeanette Clark jclark@nceas.ucsb.edu"

ARG TAG=2.0.0
ENV TAG=${TAG}

# metadig-webapp can process a quality report generation request directly, or queue it for
# a metadig-worker (container) to process. If it has been requested that a quality report
# be created immediately, some minimal R and DataOne indexing needs to be configured.
# DataONE indexer prints copious error msgs if these files don't exist
RUN mkdir -p /etc/dataone/index && touch /etc/dataone/index/d1client.properties && touch /etc/dataone/node.properties && touch /etc/dataone/index/cn-index-processor.properties

# Add R runtime and install packages required by the quality suites
#COPY metadig_0.2.0.tar.gz ./metadig.tar.gz
#COPY ./r-cmds.txt r-cmds.txt
RUN apk update
#RUN apk add g++ R R-dev R-doc libc-dev openssl-dev libxml2 libxml2-dev
#RUN echo 'options(repos = c(CRAN = "https://cran.cnr.berkeley.edu/"))' >> /usr/lib/R/etc/Rprofile.site
#RUN Rscript --vanilla r-cmds.txt

# add context to /usr/local/tomcat/webapps
COPY ./server.xml /usr/local/tomcat/conf/server.xml
# The most recently built war file is copied from the maven build directory to this dir, so that
# it can be copyied to the image. The final war file is named 'quality.war' so that the tomcat
# context will be 'quality', i.e. 'https://docker-ucsb-1.test.dataone.org:30080/quality'.
COPY ./metadig-webapp.war /usr/local/tomcat/webapps/quality.war

#EXPOSE 8009
# metadig-controller will send requests to tomcat port 8080
EXPOSE 8080

CMD ["catalina.sh", "run"]

