package edu.ucsb.nceas.mdq.rest;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.xml.bind.JAXBException;

import edu.ucsb.nceas.mdqengine.exception.MetadigException;
import edu.ucsb.nceas.mdqengine.exception.MetadigStoreException;
import edu.ucsb.nceas.mdqengine.store.MNStore;
import edu.ucsb.nceas.mdqengine.store.StoreFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.ucsb.nceas.mdqengine.store.MDQStore;
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
	
	public RunsResource() throws InternalServerErrorException {
        boolean persist = true;
        try {
            store = StoreFactory.getStore(persist);
        } catch (MetadigException mse) {
            mse.printStackTrace();
            InternalServerErrorException ise = new InternalServerErrorException(mse.getMessage());
            throw(ise);
        }
    }
	
    /**
     * Method handling HTTP GET requests. The returned object will be sent
     * to the client as "text/plain" media type.
     *
     * @return String that will be returned as a text/plain response.
     */
//    @GET
//    @Produces(MediaType.APPLICATION_JSON)
    public String listRuns() {
        // persist = true causes a database based store to be created by the factory.
        if(!this.store.isAvailable()) {
            try {
                store.renew();
            } catch (MetadigException e) {
                e.printStackTrace();
                InternalServerErrorException ise = new InternalServerErrorException(e.getMessage());
                throw(ise);
            }
        }

    	Collection<String> runs = store.listRuns();
        return JsonMarshaller.toJson(runs);
    }
    
    @GET
    @Path("/{suite}/{id : .+}") // Allow for '/' in the metadataId
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response getRun(@PathParam("suite") String suiteId, @PathParam("id") String metadataId, @Context Request r) throws UnsupportedEncodingException, JAXBException {

        if(!this.store.isAvailable()) {
            try {
                store.renew();
            } catch (MetadigStoreException e) {
                e.printStackTrace();
                InternalServerErrorException ise = new InternalServerErrorException(e.getMessage());
                throw(ise);
            }
        }
        // Decode just the pid portion of the URL
        try {
            metadataId = java.net.URLDecoder.decode(metadataId, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            // not going to happen - value came from JDK's own StandardCharsets
        }

        log.debug("Getting run for suiteId: " + suiteId + ", metadataId: " + metadataId);
    	Run run = store.getRun(metadataId, suiteId);

    	if(run != null) {
            log.debug("Retrieved run with pid: " + run.getId());
        } else {
            log.info("Run not retrieved for suiteId: " + suiteId + ", metadataId: " + metadataId);
    	    if(run == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        }

        // Get the HTML request 'Accept' header specified media type and return that type
        String resultString = null;
        List<Variant> vs =
                Variant.mediaTypes(MediaType.APPLICATION_JSON_TYPE, MediaType.APPLICATION_XML_TYPE).build();
        Variant v = r.selectVariant(vs);
        if (v == null) {
            return Response.notAcceptable(vs).build();
        } else {
            MediaType mt = v.getMediaType();
            if (mt.equals(MediaType.APPLICATION_XML_TYPE)) {
                resultString = XmlMarshaller.toXml(run, true);
                log.debug("Returning quality report as text/xml");
            } else {
                log.debug("Returning quality report as application/json");
                resultString = JsonMarshaller.toJson(run);
            }
        }

        return Response.ok(resultString).build();

    }
    
//    @DELETE
//    @Path("/{id}")
//    @Produces(MediaType.TEXT_PLAIN)
    public boolean updateRun(@PathParam("suite") String suiteId, @PathParam("id") String metadataId) {

        // Decode just the pid portion of the URL
        try {
            metadataId = java.net.URLDecoder.decode(metadataId, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            // not going to happen - value came from JDK's own StandardCharsets
        }

    	Run run = store.getRun(metadataId, suiteId);
    	store.deleteRun(run);
        return true;
    }
}
