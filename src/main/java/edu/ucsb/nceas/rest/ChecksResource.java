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
import edu.ucsb.nceas.mdqengine.model.Check;
import edu.ucsb.nceas.mdqengine.serialize.JsonMarshaller;
import edu.ucsb.nceas.mdqengine.serialize.XmlMarshaller;

/**
 * Root resource (exposed at "checks" path)
 */
@Path("checks")
public class ChecksResource {
	
	private Log log = LogFactory.getLog(this.getClass());
	
	private MDQStore store = null;
	
	public ChecksResource() {
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
    public String listChecks() {
    	Collection<String> checks = store.listChecks();
        return JsonMarshaller.toJson(checks);
    }
    
//    @GET
//    @Path("/{id}")
//    @Produces(MediaType.APPLICATION_JSON)
//    public String getCheckJSON(@PathParam("id") String id) {
//    	Check check = store.getCheck(id);
//        return JsonMarshaller.toJson(check);
//    }
    
    @GET
    @Path("/{id}")
    @Produces(MediaType.TEXT_XML)
    public String getCheck(@PathParam("id") String id) throws UnsupportedEncodingException, JAXBException {
    	Check check = store.getCheck(id);
        return XmlMarshaller.toXml(check);
    }
    
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public boolean createCheck(@FormDataParam("check") InputStream xml) {
    	Check check = null;
		try {
			check = (Check) XmlMarshaller.fromXml(IOUtils.toString(xml, "UTF-8"), Check.class);
	    	store.createCheck(check);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return false;
		} 
        return true;
    }
    
    @PUT
    @Path("/{id}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public boolean updateCheck(@PathParam("id") String id, @FormDataParam("check") InputStream xml) throws JAXBException, IOException {
    	Check check = null;
		try {
			check = (Check) XmlMarshaller.fromXml(IOUtils.toString(xml, "UTF-8"), Check.class);
	    	store.updateCheck(check);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return false;
		} 
        return true;
    }
    
    @DELETE
    @Path("/{id}")
    @Produces(MediaType.TEXT_PLAIN)
    public boolean updateCheck(@PathParam("id") String id) {
    	Check check = store.getCheck(id);
    	store.deleteCheck(check);
        return true;
    }
}
