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
    public void testStorage() {
        final String responseMsg = target().path("search/image/storeIndexes").queryParam("path", "C:\\Users\\João\\Desktop\\imagens").request().get(String.class);
    }

    //Nao funciona quando sao seguidos, devido ao lock do Lucene
    //TODO Tem que se fechar o writer algures
/*
    @Test
    public void testSimilar() {
        final String responseMsg = target().path("search/image/lucene/similar").queryParam("descriptor", "ColorLayout").queryParam("path", "C:\\Users\\João\\Desktop\\img.png").request().get(String.class);
    }
*/
}
