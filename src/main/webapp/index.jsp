<html>
<body>
    <h2>MDQEngine RESTful Web Application</h2>
    <p><a href="webapi/checks">Checks</a>
    <p><a href="webapi/suites">Suites</a>
    <p><a href="webapi/runs">Runs</a>
    
    <h3>Upload Metadata document for QC</h3>
    <form id="singleForm" method="POST" enctype="multipart/form-data" action="webapi/suites/test-lter-suite.1.1/plot">
   		<input type="file" name="document" id="document"/>
   		<input type="submit" value="Run"/>
    </form>
    
    <h3>Run QC batch</h3>
    Using <a href='webapi/suites/test-lter-suite.1.1/plot/q=formatId:"eml:%2f%2fecoinformatics.org%2feml-2.1.1"&rows=10'>latest 10 EML 2.1.1 docs</a> in DataONE
    
</body>
</html>
