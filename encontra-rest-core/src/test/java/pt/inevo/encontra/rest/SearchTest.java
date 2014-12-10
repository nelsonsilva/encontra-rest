package pt.inevo.encontra.rest;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;

public class SearchTest extends JerseyTest {

    @Override
    protected URI getBaseUri() {
        return UriBuilder.fromUri(super.getBaseUri()).path("multipart-webapp").build();
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(Search.class)
                .packages("org.glassfish.jersey.examples.multipart")
                .register(MultiPartFeature.class);
    }

    @Override
    protected void configureClient(ClientConfig config) {
        config.register(MultiPartFeature.class);
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

    @Test
    public void testSimilarFile() throws IOException {

        // MediaType of the body part will be derived from the file.
        final FileDataBodyPart filePart = new FileDataBodyPart("file", new File("C:\\Users\\João\\Dropbox\\Vahid\\codebox\\model-samples\\m0.off"));

        final FormDataMultiPart multipart = new FormDataMultiPart()
                .field("filename", "m0.off");
        multipart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);

                multipart.bodyPart(filePart);

        final Response responseMsg = target().path("search/3d/similar").request().post(Entity.entity(multipart, multipart.getMediaType()));

    }

}
