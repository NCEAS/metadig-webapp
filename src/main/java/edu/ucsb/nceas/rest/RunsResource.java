package edu.ucsb.nceas.rest;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Collection;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glassfish.jersey.media.multipart.FormDataParam;

import edu.ucsb.nceas.mdqengine.MDQStore;
import edu.ucsb.nceas.mdqengine.model.Run;
import edu.ucsb.nceas.mdqengine.serialize.JsonMarshaller;
import edu.ucsb.nceas.mdqengine.serialize.XmlMarshaller;

/**
 * Root resource (exposed at "runs" path)
 */
@Path("runs")
public class RunsResource {
	
	private Log log = LogFactory.getLog(this.getClass());
	
	private MDQStore store = null;
	
	public RunsResource() {
		this.store = StoreFactory.getStore();
	}
	
    /**
     * Method handling HTTP GET requests. The returned object will be sent
     * to the client as "text/plain" media type.
     *
     * @return String that will be returned as a text/plain response.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String listRuns() {
    	Collection<String> runs = store.listRuns();
        return JsonMarshaller.toJson(runs);
    }
    
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getRunJSON(@PathParam("id") String id) {
    	Run run = store.getRun(id);
        return JsonMarshaller.toJson(run);
    }
    
    @GET
    @Path("/{id}")
    @Produces(MediaType.TEXT_XML)
    public String getRun(@PathParam("id") String id) throws UnsupportedEncodingException, JAXBException {
    	Run run = store.getRun(id);
        return XmlMarshaller.toXml(run);
    }
    
    @DELETE
    @Path("/{id}")
    @Produces(MediaType.TEXT_PLAIN)
    public boolean updateRun(@PathParam("id") String id) {
    	Run run = store.getRun(id);
    	store.deleteRun(run);
        return true;
    }
}
