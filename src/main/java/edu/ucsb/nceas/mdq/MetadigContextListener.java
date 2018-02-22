package edu.ucsb.nceas.mdq;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import edu.ucsb.nceas.mdqengine.Controller;

public class MetadigContextListener implements ServletContextListener {

    public static Log log = LogFactory.getLog(MetadigContextListener.class);

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        log.info("Metadig 'contextInitialized' called.");
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        log.info("Metadig 'contextDestroyed' called.");
        Controller controller = Controller.getInstance();
        if(controller.getIsStarted()) {
            try {
                controller.shutdown();
                log.info("Shutting down controller...");
            } catch (IOException | TimeoutException e) {
                e.printStackTrace();
            }
        }
    }
}
