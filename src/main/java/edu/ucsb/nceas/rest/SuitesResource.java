package edu.ucsb.nceas.rest;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.glassfish.jersey.media.multipart.FormDataParam;

import edu.ucsb.nceas.mdqengine.Aggregator;
import edu.ucsb.nceas.mdqengine.MDQEngine;
import edu.ucsb.nceas.mdqengine.MDQStore;
import edu.ucsb.nceas.mdqengine.model.Run;
import edu.ucsb.nceas.mdqengine.model.Suite;
import edu.ucsb.nceas.mdqengine.serialize.JsonMarshaller;
import edu.ucsb.nceas.mdqengine.serialize.XmlMarshaller;

/**
 * Root resource (exposed at "suites" path)
 */
@Path("suites")
public class SuitesResource {
	
	private Log log = LogFactory.getLog(this.getClass());
	
	private MDQStore store = null;
	
	private MDQEngine engine = null;
		
	public SuitesResource() {
		this.store = StoreFactory.getStore();
		this.engine = new MDQEngine();
	}
	
    /**
     * Method handling HTTP GET requests. The returned object will be sent
     * to the client as "text/plain" media type.
     *
     * @return String that will be returned as a text/plain response.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String listSuites() {
    	Collection<String> suites = store.listSuites();
        return JsonMarshaller.toJson(suites);
    }
    
//    @GET
//    @Path("/{id}")
//    @Produces(MediaType.APPLICATION_JSON)
//    public String getSuiteJSON(@PathParam("id") String id) {
//    	Suite suite = store.getSuite(id);
//        return JsonMarshaller.toJson(suite);
//    }
    
    @GET
    @Path("/{id}")
    @Produces(MediaType.TEXT_XML)
    public String getSuite(@PathParam("id") String id) throws UnsupportedEncodingException, JAXBException {
    	Suite suite = store.getSuite(id);
        return XmlMarshaller.toXml(suite);
    }
    
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public boolean createSuite(@FormDataParam("suite") InputStream xml) {
    	Suite suite = null;
		try {
			suite = (Suite) XmlMarshaller.fromXml(IOUtils.toString(xml, "UTF-8"), Suite.class);
	    	store.createSuite(suite);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return false;
		} 
        return true;
    }
    
    @PUT
    @Path("/{id}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public boolean updateSuite(@PathParam("id") String id, @FormDataParam("suite") InputStream xml) throws JAXBException, IOException {
    	Suite suite = null;
		try {
			suite = (Suite) XmlMarshaller.fromXml(IOUtils.toString(xml, "UTF-8"), Suite.class);
	    	store.updateSuite(suite);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return false;
		} 
        return true;
    }
    
    @DELETE
    @Path("/{id}")
    @Produces(MediaType.TEXT_PLAIN)
    public boolean updateSuite(@PathParam("id") String id) {
    	Suite suite = store.getSuite(id);
    	store.deleteSuite(suite);
        return true;
    }
    
    @POST
    @Path("/{id}/run")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public String run(
    		@PathParam("id") String id,
    		@FormDataParam("document") InputStream input) throws UnsupportedEncodingException, JAXBException {
    	Run run = null;
		try {
			Suite suite = store.getSuite(id);
			run = engine.runSuite(suite, input);
	    	store.createRun(run);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return null;
		} 
        return XmlMarshaller.toXml(run);
    }
    
    @GET
    @Path("/{id}/aggregate/{query}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response aggregate(
    		@PathParam("id") String id,
    		@PathParam("query") String query) {
		File batchResult = null;
		try {
			Suite suite = store.getSuite(id);
			Aggregator a = new Aggregator();
			List<NameValuePair> params = URLEncodedUtils.parse(query, Charset.forName("UTF-8"));
			batchResult = a.runBatch(params , suite);
			
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return null;
		} 
		return Response.ok(batchResult).header("Content-Disposition", "attachment; filename=\"" + batchResult.getName() + "\"").build();

    }
}
