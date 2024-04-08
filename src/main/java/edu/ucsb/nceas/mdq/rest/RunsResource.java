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
import edu.ucsb.nceas.mdqengine.store.StoreFactory;
import edu.ucsb.nceas.mdqengine.store.DatabaseStore;
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

    public RunsResource() {}

    /**
     * Method handling HTTP GET requests. The returned object will be sent to the client as
     * "text/plain" media type.
     *
     * @return String that will be returned as a text/plain response.
     */
    // @GET
    // @Produces(MediaType.APPLICATION_JSON)
    public String listRuns() {
        // persist = true causes a database based store to be created by the factory.
        Collection<String> runs = null;
        try (DatabaseStore store = new DatabaseStore()) {
            runs = store.listRuns();
        } catch (MetadigException e) {
            InternalServerErrorException ise = new InternalServerErrorException(e.getMessage());
            throw (ise);
        }
        return JsonMarshaller.toJson(runs);
    }

    @GET
    @Path("/{suite}/{id : .+}") // Allow for '/' in the metadataId
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getRun(@PathParam("suite") String suiteId, @PathParam("id") String metadataId,
            @Context Request r) throws UnsupportedEncodingException, JAXBException {

        Run run = null;
        try (DatabaseStore store = new DatabaseStore()) {

            // Decode just the pid portion of the URL
            try {
                metadataId = java.net.URLDecoder.decode(metadataId, StandardCharsets.UTF_8.name());
            } catch (UnsupportedEncodingException e) {
                // not going to happen - value came from JDK's own StandardCharsets
            }

            log.debug("Getting run for suiteId: " + suiteId + ", metadataId: " + metadataId);
            try {
                run = store.getRun(metadataId, suiteId);
            } catch (MetadigStoreException e) {
                InternalServerErrorException ise = new InternalServerErrorException(e.getMessage());
                throw (ise);
            }
        } catch (MetadigException e) {
            InternalServerErrorException ise = new InternalServerErrorException(e.getMessage());
            throw (ise);
        }

        if (run != null) {
            log.debug("Retrieved run with pid: " + run.getId());
        } else {
            log.info("Run not retrieved for suiteId: " + suiteId + ", metadataId: " + metadataId);
            if (run == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        }

        // Get the HTML request 'Accept' header specified media type and return that type
        String resultString = null;
        List<Variant> vs =
                Variant.mediaTypes(MediaType.APPLICATION_JSON_TYPE, MediaType.APPLICATION_XML_TYPE)
                        .build();
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
}
