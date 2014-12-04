package pt.inevo.encontra.rest;

import javax.ws.rs.core.Application;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import pt.inevo.encontra.rest.utils.ModelFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;

public class SearchTest extends JerseyTest {

    @Override
    protected Application configure() {
        return new ResourceConfig(Search.class);
    }

    @Test
    public void testStorage() {
        final String responseMsg = target().path("search/3d/storeIndex").queryParam("path", "C:\\Users\\João\\Dropbox\\Vahid\\codebox\\model-samples").request().get(String.class);
        //final String responseMsg = target().path("search/3d/a3/storeIndex").queryParam("path", "C:\\Users\\João\\Dropbox\\Vahid\\codebox\\model-samples").request().get(String.class);

       // final String responseMsg = target().path("search/image/storeIndex").queryParam("path", "C:\\Users\\João\\Desktop\\imagens").request().get(String.class);
    }

    //Nao funciona quando sao seguidos, devido ao lock do Lucene
    //TODO Tem que se fechar o writer algures

    @Test
    public void testSimilar() {
    //    final String responseMsg = target().path("search/image/similar").queryParam("path", "C:\\Users\\João\\Desktop\\img.png").request().get(String.class);
        final String responseMsg = target().path("search/3d/similar").queryParam("path", "C:\\Users\\João\\Dropbox\\Vahid\\codebox\\model-samples\\m0.off").request().get(String.class);
    //    final String responseMsg = target().path("search/3d/a3/similar").queryParam("path", "C:\\Users\\João\\Dropbox\\Vahid\\codebox\\model-samples\\m0.off").request().get(String.class);

    }
/*
    @Test
    public void testSimilarFile() throws IOException {

        Path path = Paths.get("C:\\Users\\João\\Dropbox\\Vahid\\codebox\\model-samples\\m0.off");
        byte[] data = Files.readAllBytes(path);
        ModelFile model = new ModelFile("m0.off", data);

        //    final String responseMsg = target().path("search/image/similar").queryParam("path", "C:\\Users\\João\\Desktop\\img.png").request().get(String.class);
        final String responseMsg = target().path("search/3d/similar").queryParam("modeljson", model).request().get(String.class);
        //    final String responseMsg = target().path("search/3d/a3/similar").queryParam("path", "C:\\Users\\João\\Dropbox\\Vahid\\codebox\\model-samples\\m0.off").request().get(String.class);

    }
*/
}
