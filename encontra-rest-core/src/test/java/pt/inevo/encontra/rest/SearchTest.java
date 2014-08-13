package pt.inevo.encontra.rest;

import javax.ws.rs.core.Application;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class SearchTest extends JerseyTest {

    @Override
    protected Application configure() {
        return new ResourceConfig(Search.class);
    }

    @Test
    public void testSearch() {
        final String responseMsg = target().path("search").request().get(String.class);

        assertEquals("Hello from EnContRA!", responseMsg);
    }
}
