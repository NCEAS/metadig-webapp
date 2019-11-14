package edu.ucsb.nceas.mdq.rest;

import java.io.*;
import java.util.List;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.xml.bind.JAXBException;

import edu.ucsb.nceas.mdqengine.Controller;
import edu.ucsb.nceas.mdqengine.exception.MetadigEntryNotFound;
import edu.ucsb.nceas.mdqengine.exception.MetadigException;
import edu.ucsb.nceas.mdqengine.exception.MetadigFilestoreException;
import edu.ucsb.nceas.mdqengine.filestore.MetadigFile;
import edu.ucsb.nceas.mdqengine.filestore.MetadigFileStore;
import edu.ucsb.nceas.mdqengine.filestore.StorageType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.joda.time.DateTime;

@Path("scores")
public class ScoresResource {

    private Log log = LogFactory.getLog(this.getClass());

    private static Controller metadigCtrl = null;

    public ScoresResource() throws InternalServerErrorException {
    }

    /**
     * Retrieve aggregated quality scores.
     * <p>
     *     The scores are returned as either an image file or CSV file
     *     containing the aggregated quality scores.
     * </p>
     *
     * @return An image file or CSV file
     */

    @GET
    @Produces({ MediaType.APPLICATION_OCTET_STREAM, MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON})
    public Response getScores(@QueryParam("collection") String collectionId,
                              @QueryParam("suite") String suiteId,
                              @QueryParam("node") String nodeId,
                              @Context Request r) {

        log.info("Scores 'get' request. collection: " + collectionId + ", suite: " + suiteId + ", node: " + nodeId);
        MetadigFileStore filestore = null;

        try {
            filestore = new MetadigFileStore();
        } catch (Exception e) {
            return Response.serverError().entity(e).build();
        }

        MetadigFile mdFile = null;
        try {
            mdFile = new MetadigFile();
        } catch (MetadigException me) {
            InternalServerErrorException ise = new InternalServerErrorException(me.getMessage());
            throw(ise);
        }

        if(collectionId != null) {
            mdFile.setCollectionId(collectionId);
        }

        if(nodeId != null) {
            mdFile.setNodeId(nodeId);
        }

        if(suiteId != null) {
            mdFile.setSuiteId(suiteId);
        }

        File statsFile = null;
        String mediaType = null;

        // Return the statistics in the format (media type) requested from the HTML 'Accept' header
        String resultString = null;
        List<Variant> vs = Variant.mediaTypes(MediaType.APPLICATION_OCTET_STREAM_TYPE, MediaType.TEXT_PLAIN_TYPE).build();
        Variant v = r.selectVariant(vs);
        if (v == null) {
            return Response.notAcceptable(vs).build();
        } else {
            MediaType mt = v.getMediaType();
            // Return a graphics file
            if (mt.equals(MediaType.APPLICATION_OCTET_STREAM_TYPE)) {
                log.debug("Will return mediaType 'image/jpeg'");
                mediaType = "image/jpeg";
                mdFile.setStorageType(StorageType.GRAPH.toString());
                mdFile.setMediaType(mediaType);
            } else {
                log.debug("Will return mediaType 'text/csv'");
                // Return a CVS file with the statistics
                mediaType = "text/csv";
                mdFile.setStorageType(StorageType.DATA.toString());
                mdFile.setMediaType(mediaType);
                mediaType = MediaType.TEXT_PLAIN;
            }
        }

        try {
            statsFile = filestore.getFile(mdFile);
        } catch (MetadigFilestoreException mse) {
                log.error("Unable to get file: " + mse.getMessage());
                if(mse.getCause() instanceof MetadigEntryNotFound) {
                    log.debug("file not found");
                    return Response.status(404).type(MediaType.APPLICATION_JSON).build();
                } else {
                    log.debug("something else");
                    return Response.serverError().entity(mse).type(MediaType.APPLICATION_JSON).build();
                }
        } catch (Exception e) {
            log.error("Error");
            log.error(e.getMessage(), e);
            return Response.serverError().entity(e).build();
        }

        return Response.ok(statsFile, mediaType)
                .header("Content-Disposition", "attachment; filename=" + statsFile.getName())
                .build();
    }

    /**
     * Create an aggregated quality score graph.
     *
     * @throws UnsupportedEncodingException
     * @throws JAXBException
     */
    @POST
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response run(
            @QueryParam("collection") String collectionId, // id is the metadig suite id
            @QueryParam("suite") String suiteId,
            @QueryParam("node") String nodeId,
            @Context Request r) throws UnsupportedEncodingException, JAXBException {


        log.info("Graph 'post' request. collection: " + collectionId + ", suite: " + suiteId + ",node: " + nodeId);
        String resultString = null;
        // Copy the sysmeta input stream because we need to read it twice
        //ByteArrayOutputStream bos = new ByteArrayOutputStream();

        byte[] streamData = null;

        // If the request is identifying itself as 'high', then process it now, otherwise send it
        // to the processing queue.

            try {
                if(metadigCtrl == null) {
                    metadigCtrl = Controller.getInstance();
                    // Start the controller if it has not already been started.
                    if (!metadigCtrl.getIsStarted()) {
                        metadigCtrl.start();
                        log.info("started controller");
                    }
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                return Response.serverError().entity(e).build();
            }

            // Check if the metadig-engine controller has been started. If not, return a message.
            // TODO: return a properly formatted XML error message
            if (!metadigCtrl.getIsStarted()) {
                log.error("Controller not started");
                return Response.serverError().build();
            }

            // Create another input stream to pass to the controller
            // ByteArrayInputStream sysmetaStream2 = new ByteArrayInputStream(streamData);
            try {
                DateTime requestDateTime = new DateTime();
                String projectName = null;
                String authTokenName = null;
                String serviceUrl = null;
                String formatFamily = null;

                metadigCtrl.processScorerRequest(collectionId, projectName, authTokenName, nodeId, serviceUrl,
                        formatFamily, suiteId, requestDateTime);

                log.info("Queued generation request of score file for collection id: " + collectionId + ", suiteId: " + suiteId
                        + ", nodeid: " + nodeId);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                return Response.serverError().entity(e).build();
            }

        return Response.ok().build();
    }
}
