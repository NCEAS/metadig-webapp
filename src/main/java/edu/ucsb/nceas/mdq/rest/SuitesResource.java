package edu.ucsb.nceas.mdq.rest;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;
import javax.xml.bind.JAXBException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dataone.exceptions.MarshallingException;
import org.dataone.service.types.v2.SystemMetadata;
import org.dataone.service.util.TypeMarshaller;
import org.glassfish.jersey.media.multipart.FormDataParam;

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
    
    @GET
    @Path("/{id}")
    @Produces(MediaType.TEXT_XML)
    public String getSuite(@PathParam("id") String id) throws UnsupportedEncodingException, JAXBException {
    	Suite suite = store.getSuite(id);
        return XmlMarshaller.toXml(suite);
    }
    
//    @POST
//    @Consumes(MediaType.MULTIPART_FORM_DATA)
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
    
//    @PUT
//    @Path("/{id}")
//    @Consumes(MediaType.MULTIPART_FORM_DATA)
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
    
//    @DELETE
//    @Path("/{id}")
//    @Produces(MediaType.TEXT_PLAIN)
    public boolean deleteSuite(@PathParam("id") String id) {
    	Suite suite = store.getSuite(id);
    	store.deleteSuite(suite);
        return true;
    }
    
    @POST
    @Path("/{id}/run")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response run(
    		@PathParam("id") String id,
    		@FormDataParam("document") InputStream input,
    		@FormDataParam("systemMetadata") InputStream sysMetaStream,
    		@Context Request r) throws UnsupportedEncodingException, JAXBException {
    	
    	Run run = null;
    	// include SM if it was provided
    	SystemMetadata sysMeta = null;
    	if (sysMetaStream != null) {
    		try {
				sysMeta = TypeMarshaller.unmarshalTypeFromStream(SystemMetadata.class, sysMetaStream);
			} catch (InstantiationException | IllegalAccessException
					| IOException | MarshallingException e) {
				log.warn("Could not unmarshall SystemMetadata from stream", e);
			}
    	}
		try {
			Map<String, Object> params = new HashMap<String, Object>();
			Suite suite = store.getSuite(id);
			run = engine.runSuite(suite, input, params, sysMeta);
	    	store.createRun(run);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return Response.serverError().entity(e).build();
		} 
		
		// determine the format of plot to return
        String resultString = null;
		List<Variant> vs = 
			    Variant.mediaTypes(MediaType.APPLICATION_JSON_TYPE, MediaType.APPLICATION_XML_TYPE).build();
		Variant v = r.selectVariant(vs);
		if (v == null) {
		    return Response.notAcceptable(vs).build();
		} else {
		    MediaType mt = v.getMediaType();
		    if (mt.equals(MediaType.APPLICATION_XML_TYPE)) {
		    	resultString = XmlMarshaller.toXml(run);
		    } else {
		    	resultString = JsonMarshaller.toJson(run);
		    }
		}
		
		return Response.ok(resultString).build();
    }
    
}
