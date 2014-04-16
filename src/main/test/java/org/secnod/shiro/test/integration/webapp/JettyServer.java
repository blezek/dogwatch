package org.secnod.shiro.test.integration.webapp;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.GzipHandler;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * An example standalone Jetty server.
 */
public class JettyServer {

    public static void main(String[] args) throws Exception {
        jarJetty();
    }

    static void jarJetty() throws Exception {
        Server server = new Server(8080);

        WebAppContext webapp = new WebAppContext();
        webapp.setContextPath("/");
        String resourcePath = JettyServer.class.getPackage().getName().replace('.', '/');
        webapp.setBaseResource(Resource.newClassPathResource(resourcePath));
        webapp.setParentLoaderPriority(true);

        GzipHandler gzip = new GzipHandler();
        gzip.setHandler(webapp);
        gzip.setMinGzipSize(1);

        server.setHandler(gzip);
        server.setGracefulShutdown(5000);
        server.start();
        server.join();
    }
}
