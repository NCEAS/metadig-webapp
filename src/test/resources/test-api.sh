#!/bin/bash

# API endpoint URL with placeholder for {id}
url="http://localhost:8080/metadig-webapp-3.0.0-SNAPSHOT/checks/resource.keywords.controlled-2.0.0/run/"
# url="http://localhost:8080/metadig-webapp-3.0.0-SNAPSHOT/suites/FAIR-suite-0.4.0/run/"
# Headers
headers=(
  "Content-Type: multipart/mixed"
)

# Make the API request using curl
curl -X POST "$url" \
   --header 'Content-Type: multipart/form-data; boundary=---------BOUNDARY' \
   --data-binary @testfile.txt

