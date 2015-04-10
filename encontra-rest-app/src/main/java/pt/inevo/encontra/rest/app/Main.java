package pt.inevo.encontra.rest.app;

import com.wordnik.swagger.jersey.config.JerseyJaxrsConfig;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import pt.inevo.encontra.rest.Search;

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

        final Server server = new Server(Integer.valueOf(webPort));

        ResourceConfig resourceConfig = new ResourceConfig();
        resourceConfig.packages("com.wordnik.swagger.jaxrs.json", "pt.inevo.encontra.rest");
        resourceConfig.register(com.wordnik.swagger.jersey.listing.ApiListingResourceJSON.class);
        resourceConfig.register(com.wordnik.swagger.jersey.listing.JerseyApiDeclarationProvider.class);
        resourceConfig.register(com.wordnik.swagger.jersey.listing.JerseyResourceListingProvider.class);
        resourceConfig.register(org.glassfish.jersey.media.multipart.MultiPartFeature.class);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        // Add jersey servlet
        context.addServlet(new ServletHolder(new ServletContainer(resourceConfig)), "/*");
        // Add swagger servlet
        ServletHolder swagger = new ServletHolder(JerseyJaxrsConfig.class);
        swagger.setInitParameter("api.version", "1.0.0");
        swagger.setInitParameter("swagger.api.basepath", "http://localhost:8080/");
        context.getServletHandler().addServlet(swagger);

        server.setHandler(context);

        server.start();
        server.join();
    }
}
