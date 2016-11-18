<%@page import="edu.ucsb.nceas.mdq.rest.RunsResource"%>
<%@page import="edu.ucsb.nceas.mdq.rest.SuitesResource"%>
<%@page import="edu.ucsb.nceas.mdq.rest.ChecksResource"%>
<%@page import="edu.ucsb.nceas.mdq.util.ResourceDocumenter"%>
<html>
<body>

    <h2>MDQEngine RESTful Web Application</h2>
    
    <h3>Upload Metadata document for QC</h3>
    <form id="singleRun" method="POST" enctype="multipart/form-data" action="suites/arctic.data.center.suite.1/run">
   		<input type="file" name="document" id="document"/>
   		<input type="submit" value="Run"/>
    </form>
    
    <h3>Upload Metadata document for QC Plot</h3>
    <form id="singlePlot" method="POST" enctype="multipart/form-data" action="suites/arctic.data.center.suite.1/plot">
   		<input type="file" name="document" id="document"/>
   		<input type="submit" value="Run"/>
    </form>
    
    <h3>Run QC batch plot</h3>
    Using <a href='suites/arctic.data.center.suite.1/plot/q=formatId:"eml:%2f%2fecoinformatics.org%2feml-2.1.1"&rows=10'>latest 10 EML 2.1.1 docs</a> in DataONE
    
    <h3>Available services</h3>
    <p><a href="checks">Checks</a>
    <pre><%=ResourceDocumenter.inspectClass(ChecksResource.class) %></pre>
    <p><a href="suites">Suites</a>
    <pre><%=ResourceDocumenter.inspectClass(SuitesResource.class) %></pre>
    <p><a href="runs">Runs</a>
    <pre><%=ResourceDocumenter.inspectClass(RunsResource.class) %></pre>
    
    
</body>
</html>
