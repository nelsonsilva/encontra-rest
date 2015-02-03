package pt.inevo.encontra.rest.app;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.glassfish.jersey.jetty.JettyHttpContainerFactory;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import pt.inevo.encontra.rest.Search;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;

/**
 * This class launches the web application in an embedded Jetty container. This is the entry point to your application. The Java
 * command that is used for launching should fire this main method.
 */
public class Main {

    public static void main(String[] args) throws Exception{
        // The port that we should run on can be set into an environment variable
        // Look for that variable and default to 8080 if it isn't there.
        String webPort = System.getenv("PORT");
        if (webPort == null || webPort.isEmpty()) {
            webPort = "8080";
        }

        URI baseUri = UriBuilder.fromUri("http://localhost/").port(Integer.valueOf(webPort)).build();
        ResourceConfig config = new ResourceConfig(Search.class);
        config.register(MultiPartFeature.class);
        Server server = JettyHttpContainerFactory.createServer(baseUri, config, false);
       // server.stop();

        WebAppContext root = new WebAppContext();
        final String webappDirLocation = "encontra-rest/encontra-rest-app/src/main/webapp/";
        root.setDescriptor(webappDirLocation + "/WEB-INF/web.xml");
        System.out.println();
        System.out.println(root.getDescriptor());
        root.setResourceBase(webappDirLocation);
        System.out.println(root.getResourceBase());
        root.setContextPath("/");
        System.out.println(root.getContextPath());
        root.setParentLoaderPriority(true);
        server.setHandler(root);

        server.start();
        server.join();
    }
}
