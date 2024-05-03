package edu.ucsb.nceas.mdq.rest;

import java.io.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;
import javax.xml.bind.JAXBException;

import edu.ucsb.nceas.mdqengine.exception.MetadigException;
import edu.ucsb.nceas.mdqengine.store.StoreFactory;
import edu.ucsb.nceas.mdqengine.store.DatabaseStore;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dataone.exceptions.MarshallingException;
import org.dataone.service.types.v1.NodeReference;
import org.dataone.service.types.v2.SystemMetadata;
import org.dataone.service.util.TypeMarshaller;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.joda.time.DateTime;

import edu.ucsb.nceas.mdqengine.MDQEngine;
import edu.ucsb.nceas.mdqengine.Controller;
import edu.ucsb.nceas.mdqengine.store.MDQStore;
import edu.ucsb.nceas.mdqengine.model.Run;
import edu.ucsb.nceas.mdqengine.model.Suite;
import edu.ucsb.nceas.mdqengine.serialize.JsonMarshaller;
import edu.ucsb.nceas.mdqengine.serialize.XmlMarshaller;
import edu.ucsb.nceas.mdqengine.dispatch.Dispatcher;

/**
 * Root resource (exposed at "suites" path)
 */
@Path("suites")
public class SuitesResource {

    private Log log = LogFactory.getLog(this.getClass());
    private static Controller metadigCtrl = null;

    public SuitesResource() {}

    /**
     * Method handling HTTP GET requests. The returned object will be sent to the client as
     * "text/plain" media type.
     *
     * @return String that will be returned as a text/plain response.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String listSuites() {
        boolean persist = false;
        MDQStore store = null;
        try {
            store = StoreFactory.getStore(persist);
        } catch (MetadigException e) {
            InternalServerErrorException ise = new InternalServerErrorException(e.getMessage());
            throw (ise);
        }

        Collection<String> suites = store.listSuites();
        return JsonMarshaller.toJson(suites);
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.TEXT_XML)
    public String getSuite(@PathParam("id") String id)
            throws UnsupportedEncodingException, JAXBException {
        boolean persist = false;
        MDQStore store = null;
        try {
            store = StoreFactory.getStore(persist);
        } catch (MetadigException e) {
            InternalServerErrorException ise = new InternalServerErrorException(e.getMessage());
            throw (ise);
        }
        Suite suite = store.getSuite(id);
        return XmlMarshaller.toXml(suite, true);
    }

    // @POST
    // @Consumes(MediaType.MULTIPART_FORM_DATA)
    // not enabled for security reasons, see: https://github.com/NCEAS/metadig-webapp/issues/21
    public boolean createSuite(@FormDataParam("suite") InputStream xml) {

        try (DatabaseStore store = new DatabaseStore()) {
            Suite suite = null;
            try {
                suite = (Suite) XmlMarshaller.fromXml(IOUtils.toString(xml, "UTF-8"), Suite.class);
                store.createSuite(suite);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                return false;
            }
        } catch (MetadigException e) {
            InternalServerErrorException ise = new InternalServerErrorException(e.getMessage());
            throw (ise);
        }
        return true;
    }

    // @PUT
    // @Path("/{id}")
    // @Consumes(MediaType.MULTIPART_FORM_DATA)
    // not enabled for security reasons, see: https://github.com/NCEAS/metadig-webapp/issues/21
    public boolean updateSuite(@PathParam("id") String id, @FormDataParam("suite") InputStream xml)
            throws JAXBException, IOException {

        try (DatabaseStore store = new DatabaseStore()) {
            Suite suite = null;
            try {
                suite = (Suite) XmlMarshaller.fromXml(IOUtils.toString(xml, "UTF-8"), Suite.class);
                store.updateSuite(suite);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                return false;
            }
        } catch (MetadigException e) {
            InternalServerErrorException ise = new InternalServerErrorException(e.getMessage());
            throw (ise);
        }
        return true;
    }

    // @DELETE
    // @Path("/{id}")
    // @Produces(MediaType.TEXT_PLAIN)
    // not enabled for security reasons, see: https://github.com/NCEAS/metadig-webapp/issues/21
    public boolean deleteSuite(@PathParam("id") String id) {

        try (DatabaseStore store = new DatabaseStore()) {
            Suite suite = store.getSuite(id);
            store.deleteSuite(suite);
        } catch (MetadigException e) {
            InternalServerErrorException ise = new InternalServerErrorException(e.getMessage());
            throw (ise);
        }
        return true;
    }

    @POST
    @Path("/{id}/run")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response run(@PathParam("id") String id, // id is the metadig suite id
            @FormDataParam("document") InputStream input, // the input metadata document
            @FormDataParam("systemMetadata") InputStream sysMetaStream, // the system metadata for
                                                                        // the input metadata
                                                                        // document
            @FormDataParam("priority") String priority, // the priority to enqueue the metadig
                                                        // engine request with
                                                        // ("high", "medium", "low")
            @Context Request r) throws UnsupportedEncodingException, JAXBException {

        MDQEngine engine = null;

        if (priority == null)
            priority = "low";
        Run run = null;
        String resultString = null;
        // Copy the sysmeta input stream because we need to read it twice
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        byte[] streamData = null;

        try {
            int read = 0;
            // This size should be several times larger than the average system metadata
            byte[] buff = new byte[101024];
            while ((read = sysMetaStream.read(buff)) != -1) {
                bos.write(buff, 0, read);
            }
            streamData = bos.toByteArray();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Response.serverError().entity(e).build();
        }

        ByteArrayInputStream sysmetaStream = new ByteArrayInputStream(streamData);

        SystemMetadata sysMeta = null;
        // Read sysmeta input stream to get values to log and pass to the controller
        if (sysMetaStream != null) {
            try {
                sysMeta =
                        TypeMarshaller.unmarshalTypeFromStream(SystemMetadata.class, sysmetaStream);
            } catch (InstantiationException | IllegalAccessException | IOException
                    | MarshallingException e) {
                log.warn("Could not unmarshall SystemMetadata from stream", e);
            }
        }

        // If the request is identifying itself as 'high', then process it now,
        // otherwise send it
        // to the processing queue.
        if (priority.equals("high")) {

            try (DatabaseStore store = new DatabaseStore()) {
                engine = new MDQEngine();

                try {
                    log.info("Running suite " + id + " for pid "
                            + sysMeta.getIdentifier().getValue());
                    Map<String, Object> params = new HashMap<String, Object>();
                    Suite suite = store.getSuite(id);
                    run = engine.runSuite(suite, input, params, sysMeta);
                    store.createRun(run);
                    Dispatcher.getDispatcher("python").close();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    return Response.serverError().entity(e).build();
                }
            } catch (MetadigException | IOException | ConfigurationException e) {
                InternalServerErrorException ise = new InternalServerErrorException(e.getMessage());
                throw (ise);
            }

            // determine the format of plot to return
            List<Variant> vs = Variant
                    .mediaTypes(MediaType.APPLICATION_JSON_TYPE, MediaType.APPLICATION_XML_TYPE)
                    .build();
            Variant v = r.selectVariant(vs);
            if (v == null) {
                return Response.notAcceptable(vs).build();
            } else {
                MediaType mt = v.getMediaType();
                if (mt.equals(MediaType.APPLICATION_XML_TYPE)) {
                    resultString = XmlMarshaller.toXml(run, true);
                } else {
                    resultString = JsonMarshaller.toJson(run);
                }
            }
        } else {
            try {
                if (metadigCtrl == null) {
                    metadigCtrl = Controller.getInstance();
                    // Start the controller if it has not already been started.
                    if (!metadigCtrl.getIsStarted()) {
                        metadigCtrl.start();
                    }
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                return Response.serverError().entity(e).build();
            }

            // Check if the metadig-engine controller has been started. If not, return a
            // message.
            // TODO: return a properly formatted XML error message
            if (!metadigCtrl.getIsStarted()) {
                return Response.serverError().build();
            }

            // Create another input stream to pass to the controller
            ByteArrayInputStream sysmetaStream2 = new ByteArrayInputStream(streamData);
            try {
                DateTime requestDateTime = new DateTime();
                NodeReference dataSource = sysMeta.getOriginMemberNode();
                String metadataPid = sysMeta.getIdentifier().getValue();
                log.info("Queue generation request of quality document for: "
                        + dataSource.getValue() + ", PID: " + metadataPid + ", " + id + ", "
                        + requestDateTime.toString());
                metadigCtrl.processQualityRequest(dataSource.getValue(), metadataPid, input, id, "",
                        requestDateTime, sysmetaStream2);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                return Response.serverError().entity(e).build();
            }
        }

        return Response.ok(resultString).build();
    }

}
