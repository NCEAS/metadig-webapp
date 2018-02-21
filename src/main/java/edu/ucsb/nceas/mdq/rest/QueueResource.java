package edu.ucsb.nceas.mdq.rest;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.concurrent.TimeoutException;
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

import edu.ucsb.nceas.mdqengine.Controller;
import edu.ucsb.nceas.mdqengine.MDQEngine;
import edu.ucsb.nceas.mdqengine.MDQStore;
import edu.ucsb.nceas.mdqengine.model.Run;
import edu.ucsb.nceas.mdqengine.serialize.JsonMarshaller;
import edu.ucsb.nceas.mdqengine.serialize.XmlMarshaller;

/**
 * Root resource (exposed at "process" path)
 */
@Path("queue")
public class QueueResource {
	
	private Log log = LogFactory.getLog(this.getClass());
	
	private Controller controller = null;
	
	public QueueResource() {
        this.controller = Controller.getInstance();
	}
    
    @GET
    @Path("status")
    @Produces(MediaType.TEXT_PLAIN)
    public String queueStatus() {
        log.debug("handling request to get queue status...");
        Boolean isStarted = false;
        isStarted = controller.getIsStarted();
        if(isStarted) {
            log.debug("The controller is running.");
            return "The controller is running.";
        } else {
            log.debug("The controller is not running.");
            return "The controller is not running.";
        }
    }

    @GET
    @Path("start")
    @Produces(MediaType.TEXT_PLAIN)
    public String startQueue() {
        Boolean started = false;
        log.debug("handling request to start queue...");
        try {
            controller = Controller.getInstance();
            /* First check if the controller has already been started */
            started = controller.getIsStarted();
            if(started) return "controller already started";
            /* Controller hasn't been started yet, start it and check if success */
            controller.start();
            started = controller.getIsStarted();
            if(started) {
                return "controller started";
            } else {
                return "controller not started";
            }
        } catch (Exception e) {
            e.printStackTrace();
            //TODO: return properly formated XML 
            return "controller not started";
        }
    }
    
    @GET
    @Path("stop")
    @Produces(MediaType.TEXT_PLAIN)
    public String stopQueue() {
        Boolean started = false;
        log.debug("handling request to stop queue...");
        try {
            controller = Controller.getInstance();
            controller.shutdown();
            started = controller.getIsStarted();
            if(!started) {
                return "controller stopped";
            } else {
                return "controller not stopped";
            }
        } catch (Exception e) {
            e.printStackTrace();
            //TODO: return properly formated XML 
            return "error: controller not stopped";
        }
    }

    
    @GET
    @Path("test")
    @Produces(MediaType.TEXT_PLAIN)
    public String testQueue() {
        log.debug("handling request to test queue...");
        controller = Controller.getInstance();
        if(!controller.getIsStarted()) controller.start();
        controller.test();
        log.debug("running queue test...");
        return "test sent to queue.";
    }
}
