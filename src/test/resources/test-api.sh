#!/bin/bash

# API endpoint URL for testing a check or suite
check_url="http://localhost:8080/metadig-webapp-3.0.0/checks/resource.keywords.controlled-2.0.0/run/"
suite_url="http://localhost:8080/metadig-webapp-3.0.0/suites/FAIR-suite-0.4.0/run/"
# Headers
headers=(
  "Content-Type: multipart/mixed"
)

# Make the API request using curl
curl -X POST "$suite_url" \
   --header 'Content-Type: multipart/form-data; boundary=---------BOUNDARY' \
   --data-binary @testfile.txt

