package pt.inevo.encontra.rest;

import com.wordnik.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.inevo.encontra.common.Result;
import pt.inevo.encontra.common.ResultSet;
import pt.inevo.encontra.descriptors.DescriptorExtractor;
import pt.inevo.encontra.index.search.AbstractSearcher;
import pt.inevo.encontra.index.search.Searcher;
import pt.inevo.encontra.query.CriteriaQuery;
import pt.inevo.encontra.query.criteria.CriteriaBuilderImpl;
import pt.inevo.encontra.rest.engines.ClutchAbstractEngine;
import pt.inevo.encontra.rest.engines.ClutchImageEngine;
import pt.inevo.encontra.rest.engines.ClutchThreedEngine;
import pt.inevo.encontra.rest.utils.ImageModel;
import pt.inevo.encontra.rest.utils.StreamUtil;
import pt.inevo.encontra.storage.IEntity;
import pt.inevo.encontra.storage.ModelLoader;
import pt.inevo.encontra.threed.model.ThreedModel;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.glassfish.jersey.media.multipart.FormDataParam;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;


@Path("search")
@Api(value = "/search", description = "Query operations")
public class Search {

    public static final Logger log = LoggerFactory.getLogger(Search.class);

    public static Map<String, ClutchAbstractEngine> ENGINES = new HashMap<>();
    static {
        ENGINES.put("image", new ClutchImageEngine());
        ENGINES.put("3d", new ClutchThreedEngine());
    }

    /**
     * This method stores the indexes of a specific descriptor and the objects of each ImageModel in the FS
     * Currently working for images and btree index
     * Need to be careful with the IDs of the ImageModels, that may override the existing ones??
     *
     * @param type multimedia type - image, 3dObject,etc
     * @param path Images folder
     * @return
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws InstantiationException
     */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/{type}/storeIndex")
    @ApiOperation(value = "Index from filesystem")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "Path not found")})
    public Response storeIndex(
            @ApiParam(value = "Type", required = true) @PathParam("type") String type,
            @ApiParam(value = "Path", required = true) @QueryParam("path") String path) {

        ClutchAbstractEngine engine = ENGINES.get(type);

        log.info("Loading some objects to the test indexes...");

        try {
            ModelLoader loader = engine.getLoader();
            loader.setModelsPath(path);
            loader.load();
            Iterator<File> it = loader.iterator();

            for (int i = 0; it.hasNext(); i++) {
                File f = it.next();
                IEntity ml = loader.loadModel(f);
                engine.insert(ml);
            }
        } catch (IOException e) {
            throw new NotFoundException(e);
        }
        return Response.ok().build();
    }


    /**
     *
     * @param type multimedia type - image, 3dObject,etc
     * @param path The path of the image
     * @return
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws InstantiationException
     */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/{type}/similar")
    public String similar(@PathParam("type") String type, @QueryParam("path") String path) {

        ClutchAbstractEngine engine = ENGINES.get(type);

        log.info("Creating a knn query...");

        ModelLoader loader = engine.getLoader();

        Object model = null;
        try {
            model = loader.loadBuffered(new File(path));
        } catch (IOException e) {
            throw new NotFoundException(e);
        }

        CriteriaBuilderImpl cb = new CriteriaBuilderImpl();
        CriteriaQuery query = cb.createQuery(engine.getModelClass());
        pt.inevo.encontra.query.Path modelPath = query.from(engine.getModelClass()).get(engine.getType());
        query = query.where(cb.similar(modelPath, model)).distinct(true).limit(20);

        ResultSet<Result> results = engine.search(query);

        log.debug("Number of retrieved elements: " + results.getSize());
        for (Result r : results) {
            log.debug("Retrieved element: " + r.getResultObject().toString() + "\t");
            log.debug("Similarity: " + r.getScore());
        }
        return "see_what_to_return";
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/{type}/{descriptor}/storeIndex")
    public Response storeIndex (@PathParam("type") String type, @PathParam("descriptor") String descriptor, @QueryParam("path") String path) {

        ClutchAbstractEngine engine = ENGINES.get(type);

        log.info("Loading some objects to the test indexes...");

        try {
            ModelLoader loader = engine.getLoader();
            loader.setModelsPath(path);
            loader.load();
            Iterator<File> it = loader.iterator();

            for (int i = 0; it.hasNext(); i++) {
                File f = it.next();
                IEntity ml = loader.loadModel(f);
                engine.insert(ml);
            }
        } catch (IOException e) {
            throw new NotFoundException(e);
        }
        return Response.ok().build();
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/{type}/{descriptor}/similar")
    public Response similar(@PathParam("type") String type, @PathParam("descriptor") String descriptor, @QueryParam("path") String path) {


        ClutchAbstractEngine engine = ENGINES.get(type);

        log.info("Creating a knn query...");

        ModelLoader loader = engine.getLoader();

        Object model = null;
        try {
            model = loader.loadBuffered(new File(path));
        } catch (IOException e) {
            throw new NotFoundException(e);
        }

        CriteriaBuilderImpl cb = new CriteriaBuilderImpl();
        CriteriaQuery query = cb.createQuery(engine.getModelClass());
        pt.inevo.encontra.query.Path modelPath = query.from(engine.getModelClass()).get(engine.getType());
        query = query.where(cb.similar(modelPath, model)).distinct(true).limit(20);

        ResultSet<Result> results = engine.search(query);

        log.debug("Number of retrieved elements: " + results.getSize());
        for (Result r : results) {
            log.debug("Retrieved element: " + r.getResultObject().toString() + "\t");
            log.debug("Similarity: " + r.getScore());
        }
        return Response.ok().build();
    }

/*
    @POST
    //@Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Path("/{type}/similar")
    public Response similar(@PathParam("type") String type, @FormDataParam("file") InputStream uploadedInputStream,
                          @FormDataParam("file") FormDataContentDisposition fileDetail) {

        ClutchAbstractEngine engine = null;

        if(type.equals("image")){
            engine = new ClutchImageEngine();
        }
        else if (type.equals("3d")){
            engine = new ClutchThreedEngine();
        }

        log.info("Creating a knn query...");

        ModelLoader loader = engine.getLoader();

        String filename = fileDetail.getFileName();
        String extension = filename.substring(filename.lastIndexOf("."));

        //Queremos inserir também na nossa DB?
        O model = null;
        try {
            model = (O) loader.loadBuffered(StreamUtil.stream2file(uploadedInputStream, extension));
        } catch (IOException e) {
            throw new NotFoundException(e);
        }

        CriteriaBuilderImpl cb = new CriteriaBuilderImpl();
        CriteriaQuery<E> query = cb.createQuery(engine.getModelClass());
        pt.inevo.encontra.query.Path modelPath = query.from(engine.getModelClass()).get(engine.getType());
        query = query.where(cb.similar(modelPath, model)).distinct(true).limit(20);

        ResultSet<E> results = engine.search(query);

        log.debug("Number of retrieved elements: " + results.getSize());
        for (Result<E> r : results) {
            log.debug("Retrieved element: " + r.getResultObject().toString() + "\t");
            log.debug("Similarity: " + r.getScore());
        }
        return Response.ok().build();
    }

    @POST
    //@Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Path("/{type}/{descriptor}/similar")
    public Response similar(@PathParam("type") String type, @PathParam("descriptor") String descriptor, @FormDataParam("file") InputStream uploadedInputStream,
                          @FormDataParam("file") FormDataContentDisposition fileDetail) {

        ClutchAbstractEngine engine = null;

        if(type.equals("image")){
            engine = new ClutchImageEngine(descriptor);
        }
        else if (type.equals("3d")){
            engine = new ClutchThreedEngine(descriptor);
        }

        log.info("Creating a knn query...");

        ModelLoader loader = engine.getLoader();

        String filename = fileDetail.getFileName();
        String extension = filename.substring(filename.lastIndexOf("."));

        O model = null;
        try {
            model = (O) loader.loadBuffered(StreamUtil.stream2file(uploadedInputStream, extension));
        } catch (IOException e) {
            throw new NotFoundException(e);
        }

        CriteriaBuilderImpl cb = new CriteriaBuilderImpl();
        CriteriaQuery<E> query = cb.createQuery(engine.getModelClass());
        pt.inevo.encontra.query.Path modelPath = query.from(engine.getModelClass()).get(engine.getType());
        query = query.where(cb.similar(modelPath, model)).distinct(true).limit(20);

        ResultSet<E> results = engine.search(query);

        log.debug("Number of retrieved elements: " + results.getSize());
        for (Result<E> r : results) {
            log.debug("Retrieved element: " + r.getResultObject().toString() + "\t");
            log.debug("Similarity: " + r.getScore());
        }
        return Response.ok().build();
    }


     @POST
     @Consumes(MediaType.MULTIPART_FORM_DATA)
     @Path("/{type}/storeIndex")
     public Response storeIndex (@PathParam("type") String type, @FormDataParam("file") InputStream uploadedInputStream,
                               @FormDataParam("file") FormDataContentDisposition fileDetail) {

        ClutchAbstractEngine engine = null;

        if(type.equals("image")){
            engine = new ClutchImageEngine();
        }
        else if (type.equals("3d")){
            engine = new ClutchThreedEngine();
        }

         log.info("Loading some objects to the test indexes...");
        ModelLoader loader = engine.getLoader();

        String filename = fileDetail.getFileName();
        String extension = filename.substring(filename.lastIndexOf("."));

         E model = null;
         try {
             model = (E) loader.loadModel(StreamUtil.stream2file(uploadedInputStream, extension));
         } catch (IOException e) {
             throw new NotFoundException(e);
         }
         engine.insert(model);

        return Response.ok().build();
    }


    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Path("/{type}/{descriptor}/storeIndex")
    public Response storeIndex(@PathParam("type") String type, @PathParam("descriptor") String descriptor, @FormDataParam("file") InputStream uploadedInputStream,
                              @FormDataParam("file") FormDataContentDisposition fileDetail) {

        ClutchAbstractEngine engine = null;

        if(type.equals("image")){
            engine = new ClutchImageEngine(descriptor);
        }
        else if (type.equals("3d")){
            engine = new ClutchThreedEngine(descriptor);
        }

        System.out.println("Loading some objects to the test indexes...");
        ModelLoader loader = engine.getLoader();

        String filename = fileDetail.getFileName();
        String extension = filename.substring(filename.lastIndexOf("."));

        E model = null;
        try {
            model = (E) loader.loadModel(StreamUtil.stream2file(uploadedInputStream, extension));
        } catch (IOException e) {
            throw new NotFoundException(e);
        }
        engine.insert(model);

        return Response.ok().build();
    }

*/
}
