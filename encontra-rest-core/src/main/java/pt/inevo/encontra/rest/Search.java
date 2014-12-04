package pt.inevo.encontra.rest;

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
import pt.inevo.encontra.storage.IEntity;
import pt.inevo.encontra.storage.ModelLoader;
import pt.inevo.encontra.threed.model.ThreedModel;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;

@Path("search")
public class Search<S extends AbstractSearcher, D extends DescriptorExtractor, E extends IEntity<Long>, O extends Object> {

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
    public String storeIndex (@PathParam("type") String type, @QueryParam("path") String path) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, IOException {

        ClutchAbstractEngine engine = null;

        if(type.equals("image")){
            engine = new ClutchImageEngine();
        }
        else if (type.equals("3d")){
            engine = new ClutchThreedEngine();
        }


        System.out.println("Loading some objects to the test indexes...");
        ModelLoader loader = engine.getLoader();
        loader.setModelsPath(path);
        loader.load();
        Iterator<File> it = loader.iterator();

        for (int i = 0; it.hasNext(); i++) {
            File f = it.next();
            E ml = (E) loader.loadModel(f);
            engine.insert(ml);
        }
        return "see_what_to_return";
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
    public String similar(@PathParam("type") String type, @QueryParam("path") String path) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, IOException {

        ClutchAbstractEngine engine = null;
        if(type.equals("image")){
            engine = new ClutchImageEngine<ImageModel, D>();
        }
        else if (type.equals("3d"))
        {
            engine = new ClutchThreedEngine<ThreedModel,D>();
        }

        System.out.println("Creating a knn query...");

        ModelLoader loader = engine.getLoader();

        O model = (O) loader.loadBuffered(new File(path));

        CriteriaBuilderImpl cb = new CriteriaBuilderImpl();
        CriteriaQuery<E> query = cb.createQuery(engine.getModelClass());
        pt.inevo.encontra.query.Path modelPath = query.from(engine.getModelClass()).get(engine.getType());
        query = query.where(cb.similar(modelPath, model)).distinct(true).limit(20);

        ResultSet<E> results = engine.search(query);

        System.out.println("Number of retrieved elements: " + results.getSize());
        for (Result<E> r : results) {
            System.out.print("Retrieved element: " + r.getResultObject().toString() + "\t");
            System.out.println("Similarity: " + r.getScore());
        }
        return "see_what_to_return";
    }

    //Only for 3dModels now
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/{type}/{descriptor}/storeIndex")
    public String storeIndex (@PathParam("type") String type, @PathParam("descriptor") String descriptor, @QueryParam("path") String path) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        ClutchAbstractEngine engine = null;

        if(type.equals("image")){
            engine = new ClutchImageEngine(descriptor);
        }
        else if (type.equals("3d")){
            engine = new ClutchThreedEngine(descriptor);
        }

        System.out.println("Loading some objects to the test indexes...");
        ModelLoader loader = engine.getLoader();
        loader.setModelsPath(path);
        loader.load();
        Iterator<File> it = loader.iterator();

        for (int i = 0; it.hasNext(); i++) {
            File f = it.next();
            E ml = (E) loader.loadModel(f);
            engine.insert(ml);
        }
        return "see_what_to_return";
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/{type}/{descriptor}/similar")
    public String similar(@PathParam("type") String type, @PathParam("descriptor") String descriptor, @QueryParam("path") String path) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, IOException {


        ClutchAbstractEngine engine = null;

        if(type.equals("image")){
            engine = new ClutchImageEngine(descriptor);
        }
        else if (type.equals("3d")){
            engine = new ClutchThreedEngine(descriptor);
        }

        System.out.println("Creating a knn query...");

        ModelLoader loader = engine.getLoader();

        O model = (O) loader.loadBuffered(new File(path));

        CriteriaBuilderImpl cb = new CriteriaBuilderImpl();
        CriteriaQuery<E> query = cb.createQuery(engine.getModelClass());
        pt.inevo.encontra.query.Path modelPath = query.from(engine.getModelClass()).get(engine.getType());
        query = query.where(cb.similar(modelPath, model)).distinct(true).limit(20);

        ResultSet<E> results = engine.search(query);

        System.out.println("Number of retrieved elements: " + results.getSize());
        for (Result<E> r : results) {
            System.out.print("Retrieved element: " + r.getResultObject().toString() + "\t");
            System.out.println("Similarity: " + r.getScore());
        }
        return "see_what_to_return";
    }
}
