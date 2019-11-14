package edu.ucsb.nceas.mdq.rest;

import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import edu.ucsb.nceas.mdq.util.ResourceDocumenter;

/**
 * Root resource
 */
@Path("")
public class RootResource {
		
	public RootResource() {
		
	}
	
    /**
     * Method handling HTTP GET requests. The returned object will be sent
     * to the client as "text/plain" media type.
     *
     * @return String that will be returned as a text/plain response.
     */
    @GET
    @Produces(MediaType.TEXT_HTML)
    public String showHelp(@Context UriInfo r) {
    	URI baseUri = r.getBaseUri();
    	String context = baseUri.getPath();
    	
    	StringBuffer sb = new StringBuffer();
    	sb.append("<h3>Available services</h3>");
    	sb.append("<p><a href='" + context + "checks'>Checks</a>");
    	sb.append("<pre>"
    			+ ResourceDocumenter.inspectClass(ChecksResource.class)
    			+ "</pre>");
    	sb.append("<p><a href='" + context + "suites'>Suites</a>");
    	sb.append("<pre>"
    			+ ResourceDocumenter.inspectClass(SuitesResource.class)
    			+ "</pre>");
    	sb.append("<p><a href='" + context + "runs'>Runs</a>");
    	sb.append("<pre>"
    			+ ResourceDocumenter.inspectClass(RunsResource.class)
    			+ "</pre>");
        sb.append("<p><a href='" + context + "scores'>Graph</a>");
        sb.append("<pre>"
            	+ ResourceDocumenter.inspectClass(ScoresResource.class)
            	+ "</pre>");
    	return sb.toString();
    }
}
