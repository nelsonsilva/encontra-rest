package pt.inevo.encontra.rest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import pt.inevo.encontra.common.Result;
import pt.inevo.encontra.common.ResultSet;
import pt.inevo.encontra.descriptors.DescriptorExtractor;
import pt.inevo.encontra.index.search.AbstractSearcher;
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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.glassfish.jersey.media.multipart.FormDataParam;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;

@Path("search")
public class Search<S extends AbstractSearcher, D extends DescriptorExtractor, E extends IEntity<Long>, O extends Object> {

    ClutchAbstractEngine engine = null;

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
    @Path("/{type}/index")
    public String storeIndex (@PathParam("type") String type, @DefaultValue("data/models/") @QueryParam("path") String path) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, IOException {

        if(type.equals("image")){
            engine = new ClutchImageEngine();
        }
        else if (type.equals("3d")){
            engine = new ClutchThreedEngine();
        }

        ModelLoader loader = engine.getLoader();

        loader.setModelsPath(path);
        loader.load();
        Iterator<File> it = loader.iterator();
        int i;
        for (i = 0; it.hasNext(); i++) {
            File f = it.next();
            E ml = (E) loader.loadModel(f);
            engine.insert(ml);
        }

        engine.closeIndex();
        return "Number of models loaded: " + i;
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
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{type}/similar")
    public String similar(@PathParam("type") String type, @QueryParam("path") String path) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, IOException, JSONException {

        if(type.equals("image")){
            engine = new ClutchImageEngine<ImageModel, D>();
        }
        else if (type.equals("3d"))
        {
            engine = new ClutchThreedEngine<ThreedModel,D>();
        }

        ModelLoader loader = engine.getLoader();

        O model = (O) loader.loadBuffered(new File(path));

        CriteriaBuilderImpl cb = new CriteriaBuilderImpl();
        CriteriaQuery<E> query = cb.createQuery(engine.getModelClass());
        pt.inevo.encontra.query.Path modelPath = query.from(engine.getModelClass()).get(engine.getType());
        query = query.where(cb.similar(modelPath, model)).distinct(true).limit(20);

        ResultSet<E> results = engine.search(query);

        System.out.println("Number of retrieved elements: " + results.getSize());

        List<JsonReturnObject> fullJson = processTopResults(results);

        engine.closeIndex();
        return fullJson.toString();
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/{type}/{descriptor}/index")
    public String storeIndex (@PathParam("type") String type, @DefaultValue("data/models/") @PathParam("descriptor") String descriptor, @QueryParam("path") String path) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, IOException {

        if(type.equals("image")){
            engine = new ClutchImageEngine(descriptor);
        }
        else if (type.equals("3d")){
            engine = new ClutchThreedEngine(descriptor);
        }

        ModelLoader loader = engine.getLoader();
        loader.setModelsPath(path);
        loader.load();
        Iterator<File> it = loader.iterator();

        int i;
        for (i = 0; it.hasNext(); i++) {
            File f = it.next();
            E ml = (E) loader.loadModel(f);
            engine.insert(ml);
        }
        engine.closeIndex();
        return "Number of models loaded: " + i;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{type}/{descriptor}/similar")
    public String similar(@PathParam("type") String type, @PathParam("descriptor") String descriptor, @QueryParam("path") String path) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, IOException, JSONException {

        if(type.equals("image")){
            engine = new ClutchImageEngine(descriptor);
        }
        else if (type.equals("3d")){
            engine = new ClutchThreedEngine(descriptor);
        }

        ModelLoader loader = engine.getLoader();

        O model = (O) loader.loadBuffered(new File(path));

        CriteriaBuilderImpl cb = new CriteriaBuilderImpl();
        CriteriaQuery<E> query = cb.createQuery(engine.getModelClass());
        pt.inevo.encontra.query.Path modelPath = query.from(engine.getModelClass()).get(engine.getType());
        query = query.where(cb.similar(modelPath, model)).distinct(true).limit(20);

        ResultSet<E> results = engine.search(query);

        System.out.println("Number of retrieved elements: " + results.getSize());

        List<JsonReturnObject> fullJson = processTopResults(results);

        engine.closeIndex();
        return fullJson.toString();
    }


    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Path("/{type}/similar")
    public String similar(@PathParam("type") String type, @FormDataParam("file") InputStream uploadedInputStream,
                          @FormDataParam("file") FormDataContentDisposition fileDetail)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, IOException, JSONException {

        if(type.equals("image")){
            engine = new ClutchImageEngine();
        }
        else if (type.equals("3d")){
            engine = new ClutchThreedEngine();
        }

        ModelLoader loader = engine.getLoader();

        String filename = fileDetail.getFileName();

        O model = (O) loader.loadBuffered(StreamUtil.stream2file(uploadedInputStream, filename, false));

        CriteriaBuilderImpl cb = new CriteriaBuilderImpl();
        CriteriaQuery<E> query = cb.createQuery(engine.getModelClass());
        pt.inevo.encontra.query.Path modelPath = query.from(engine.getModelClass()).get(engine.getType());
        query = query.where(cb.similar(modelPath, model)).distinct(true).limit(20);

        ResultSet<E> results = engine.search(query);

        System.out.println("Number of retrieved elements: " + results.getSize());

        List<JsonReturnObject> fullJson = processTopResults(results);

        engine.closeIndex();
        return fullJson.toString();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Path("/{type}/{descriptor}/similar")
    public String similar(@PathParam("type") String type, @PathParam("descriptor") String descriptor, @FormDataParam("file") InputStream uploadedInputStream,
                          @FormDataParam("file") FormDataContentDisposition fileDetail)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, IOException, JSONException {

        if(type.equals("image")){
            engine = new ClutchImageEngine(descriptor);
        }
        else if (type.equals("3d")){
            engine = new ClutchThreedEngine(descriptor);
        }

        ModelLoader loader = engine.getLoader();

        String filename = fileDetail.getFileName();

        O model = (O) loader.loadBuffered(StreamUtil.stream2file(uploadedInputStream, filename, false));

        CriteriaBuilderImpl cb = new CriteriaBuilderImpl();
        CriteriaQuery<E> query = cb.createQuery(engine.getModelClass());
        pt.inevo.encontra.query.Path modelPath = query.from(engine.getModelClass()).get(engine.getType());
        query = query.where(cb.similar(modelPath, model)).distinct(true).limit(20);

        ResultSet<E> results = engine.search(query);

        System.out.println("Number of retrieved elements: " + results.getSize());

        List<JsonReturnObject> fullJson = processTopResults(results);

        engine.closeIndex();
        return fullJson.toString();
    }


     @POST
     @Consumes(MediaType.MULTIPART_FORM_DATA)
     @Path("/{type}/index")
     public String storeIndex (@PathParam("type") String type, @FormDataParam("file") InputStream uploadedInputStream,
                               @FormDataParam("file") FormDataContentDisposition fileDetail) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, IOException {

        if(type.equals("image")){
            engine = new ClutchImageEngine();
        }
        else if (type.equals("3d")){
            engine = new ClutchThreedEngine();
        }

        ModelLoader loader = engine.getLoader();
        String filename = fileDetail.getFileName();


        E model = (E) loader.loadModel(StreamUtil.stream2file(uploadedInputStream, filename, true));
        engine.insert(model);
        engine.closeIndex();
        return "Model Indexed";
    }


    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Path("/{type}/{descriptor}/index")
    public String storeIndex(@PathParam("type") String type, @PathParam("descriptor") String descriptor, @FormDataParam("file") InputStream uploadedInputStream,
                              @FormDataParam("file") FormDataContentDisposition fileDetail) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, IOException {

        if(type.equals("image")){
            engine = new ClutchImageEngine(descriptor);
        }
        else if (type.equals("3d")){
            engine = new ClutchThreedEngine(descriptor);
        }

        ModelLoader loader = engine.getLoader();

        String filename = fileDetail.getFileName();

        E model = (E) loader.loadModel(StreamUtil.stream2file(uploadedInputStream, filename, true));
        engine.insert(model);
        engine.closeIndex();
        return "Model Indexed";
    }


    public List<JsonReturnObject> processTopResults(ResultSet<E> results) throws JSONException {
        List<JsonReturnObject> fullJson = new ArrayList();
        for (Result<E> r : results) {
            String jsonString = (r.getResultObject().toString()).replace("\\", "\\\\");
            JSONObject jsonObj = new JSONObject (jsonString);

            JsonReturnObject jsonResult = new JsonReturnObject((Long) jsonObj.get("id"), (String) jsonObj.get("title"), r.getScore());
            System.out.print("Retrieved element: " + r.getResultObject().toString() + "\t");
            System.out.println("Similarity: " + r.getScore());
            fullJson.add(jsonResult);
        }
        return fullJson;
    }
}