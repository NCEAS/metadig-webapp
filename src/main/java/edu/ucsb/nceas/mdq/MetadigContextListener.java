package edu.ucsb.nceas.mdq;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import javax.servlet.annotation.WebListener;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import edu.ucsb.nceas.mdqengine.dispatch.Dispatcher;
import edu.ucsb.nceas.mdqengine.Controller;
import edu.ucsb.nceas.mdqengine.exception.MetadigException;

@WebListener
public class MetadigContextListener implements ServletContextListener {

    public static Log log = LogFactory.getLog(MetadigContextListener.class);

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        try {
            Dispatcher.setupJep();
        }  catch (MetadigException e) {
            throw new RuntimeException("Error setting up Jep. Aborting startup.", e);
        }

            log.info("Metadig 'contextInitialized' called.");
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        log.debug("Metadig 'contextDestroyed' called.");
        Controller controller = Controller.getInstance();
        if(controller.getIsStarted()) {
            try {
                log.debug("Shutting down controller...");
                controller.shutdown();
                log.info("Controller shutdown successfully.");
            } catch (IOException | TimeoutException e) {
                log.error("Error shutting down metadig controller.");
                e.printStackTrace();
            }
        }
    }
}
