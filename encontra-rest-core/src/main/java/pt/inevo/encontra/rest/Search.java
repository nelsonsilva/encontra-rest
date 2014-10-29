package pt.inevo.encontra.rest;

import javax.imageio.ImageIO;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import pt.inevo.encontra.descriptors.Descriptor;
import pt.inevo.encontra.index.search.AbstractSearcher;
import pt.inevo.encontra.common.DefaultResultProvider;
import pt.inevo.encontra.common.Result;
import pt.inevo.encontra.common.ResultSet;
import pt.inevo.encontra.descriptors.DescriptorExtractor;
import pt.inevo.encontra.engine.SimpleEngine;
import pt.inevo.encontra.engine.SimpleIndexedObjectFactory;

import pt.inevo.encontra.nbtree.index.BTreeIndex;
import pt.inevo.encontra.nbtree.index.NBTreeSearcher;
import pt.inevo.encontra.query.CriteriaQuery;
import pt.inevo.encontra.query.QueryProcessorDefaultImpl;
import pt.inevo.encontra.query.criteria.CriteriaBuilderImpl;
import pt.inevo.encontra.storage.*;
import pt.inevo.encontra.rest.map.*;
import pt.inevo.encontra.index.AbstractIndex;

@Path("search")
public class Search<S extends AbstractSearcher, D extends DescriptorExtractor & Descriptor, I extends AbstractIndex> {

    /**
     * This method stores the indexes of all available descriptors and the objects of each ImageModel in the FS
     * Currently working for images and btree index
     *
     * @param type multimedia type - image, 3dObject,etc
     * @param index indexing type - btree, lucene
     * @param path Images folder
     * @return
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws InstantiationException
     */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/{type}/{index}/storeIndexes")
    public String storeIndexes (@PathParam("type") String type, @PathParam("index") String index, @QueryParam("path") String path) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        ImageDescriptorMap[] descriptors = ImageDescriptorMap.values(); //Get all descriptors
        EntityStorage storage = new SimpleFSObjectStorage(ImageModel.class);

        System.out.println("Loading some objects");
        ImageModelLoader loader = new ImageModelLoader(path);
        loader.load();
        Iterator<File> it = loader.iterator();
        ArrayList<ImageModel> imgModels = new ArrayList<ImageModel>();

        // Storing the ImageModels in an ArrayList since we will need to use them for every descriptor
        // Find better way to do this?
        for (int i = 0; it.hasNext(); i++) {
            File f = it.next();
            ImageModel im = loader.loadImage(f);
            imgModels.add(im);
        }


        for (ImageDescriptorMap descriptor : descriptors) {
            SimpleEngine<ImageModel> e = new SimpleEngine<ImageModel>();
            e.setObjectStorage(storage);
            e.setQueryProcessor(new QueryProcessorDefaultImpl());
            e.setIndexedObjectFactory(new SimpleIndexedObjectFactory());

            NBTreeSearcher imageSearcher = new NBTreeSearcher();

            Class<?> descriptorClass = descriptor.getFeatureClass();

            D myInstance = (D) descriptorClass.getConstructor().newInstance();

            System.out.println(myInstance.toString());
            //using a single descriptor
            imageSearcher.setDescriptorExtractor(myInstance);
            BTreeIndex btree = new BTreeIndex("data/indexes/" + descriptor.toString(), descriptorClass);

            imageSearcher.setIndex(btree);

            e.setSearcher("image", imageSearcher);


            System.out.println("Loading some objects to the test indexes");
            //Inserting the models in the index. The objects will be stored only in the first time
            for (ImageModel img : imgModels){
                e.insert(img);
            }
        }
        return "see_what_to_return";
    }

    /**
     * This method stores the indexes of a specific descriptor and the objects of each ImageModel in the FS
     * Currently working for images and btree index
     * Need to be careful with the IDs of the ImageModels, that may override the existing ones??
     *
     * @param type multimedia type - image, 3dObject,etc
     * @param index indexing type - btree, lucene
     * @param descriptor The name of the descriptor to index
     * @param path Images folder
     * @return
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws InstantiationException
     */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/{type}/{index}/storeIndex")
    public String storeIndex (@PathParam("type") String type, @PathParam("index") String index, @QueryParam("descriptor") String descriptor, @QueryParam("path") String path) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {


        EntityStorage storage = new SimpleFSObjectStorage(ImageModel.class);



        SimpleEngine<ImageModel> e = new SimpleEngine<ImageModel>();
        e.setObjectStorage(storage);
        e.setQueryProcessor(new QueryProcessorDefaultImpl());
        e.setIndexedObjectFactory(new SimpleIndexedObjectFactory());

        NBTreeSearcher imageSearcher = new NBTreeSearcher();

        ImageDescriptorMap descMap = ImageDescriptorMap.valueOf(descriptor.toUpperCase());
        Class<?> descriptorClass = descMap.getFeatureClass();

        D myInstance = (D) descriptorClass.getConstructor().newInstance();

        System.out.println(myInstance.toString());
        //using a single descriptor
        imageSearcher.setDescriptorExtractor(myInstance);
        BTreeIndex btree = new BTreeIndex("data/indexes/" + descMap.toString(), descriptorClass);

        imageSearcher.setIndex(btree);

        e.setSearcher("image", imageSearcher);

        System.out.println("Loading some objects to the test indexes...");

        ImageModelLoader loader = new ImageModelLoader(path);
        loader.load();
        Iterator<File> it = loader.iterator();
        ArrayList<ImageModel> imgModels = new ArrayList<ImageModel>();

        for (int i = 0; it.hasNext(); i++) {
            File f = it.next();
            ImageModel im = loader.loadImage(f);
            e.insert(im);
        }

        return "see_what_to_return";
    }


    /**
     *
     * @param type multimedia type - image, 3dObject,etc
     * @param descriptor The name of the descriptor to index
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
    public String similar(@PathParam("type") String type, @QueryParam("descriptor") String descriptor, @QueryParam("path") String path) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        //Creating the engine
        System.out.println("Creating the Retrieval Engine...");
        SimpleEngine<ImageModel> e = new SimpleEngine();
        EntityStorage storage = new SimpleFSObjectStorage(ImageModel.class);

        e.setQueryProcessor(new QueryProcessorDefaultImpl());
        e.setObjectStorage(storage);
        e.setIndexedObjectFactory(new SimpleIndexedObjectFactory());

        NBTreeSearcher imageSearcher = new NBTreeSearcher();
        ImageDescriptorMap desc = ImageDescriptorMap.valueOf(descriptor.toUpperCase());
        Class<?> descriptorClass = desc.getFeatureClass();
        D myInstance = (D) descriptorClass.getConstructor().newInstance();

        //using a single descriptor
        imageSearcher.setDescriptorExtractor(myInstance);
        //using a BTreeIndex

        BTreeIndex btree = new BTreeIndex("data/indexes/" + descriptor.toUpperCase(), descriptorClass);
        imageSearcher.setIndex(btree);

        imageSearcher.setQueryProcessor(new QueryProcessorDefaultImpl());
        imageSearcher.setResultProvider(new DefaultResultProvider());

        e.setSearcher("image", imageSearcher);

        try {
            System.out.println("Creating a knn query...");
            BufferedImage image = ImageIO.read(new File(path));

            CriteriaBuilderImpl cb = new CriteriaBuilderImpl();
            CriteriaQuery<ImageModel> query = cb.createQuery(ImageModel.class);
            pt.inevo.encontra.query.Path imagePath = query.from(ImageModel.class).get("image");
            query = query.where(cb.similar(imagePath, image)).distinct(true).limit(20);

            ResultSet<ImageModel> results = e.search(query);

            System.out.println("Number of retrieved elements: " + results.getSize());
            for (Result<ImageModel> r : results) {
                System.out.print("Retrieved element: " + r.getResultObject().toString() + "\t");
                System.out.println("Similarity: " + r.getScore());
            }
        } catch (IOException ex) {
            System.out.println("[Error] Couldn't load the query image. Possible reason: " + ex.getMessage());
        }
        return "see_what_to_return";
    }

    /*
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/{type}/{index}/storeIndex")
    public String storeIndex (@PathParam("type") String type, @PathParam("index") String index, @QueryParam("path") String path, @QueryParam("descriptor") String descriptor) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        ImageDescriptorMap descMap = ImageDescriptorMap.valueOf(descriptor.toUpperCase());
        Class<?> descriptorClass = descMap.getFeatureClass();

        // Lucene e BTree estao implementados de forma diferente. Condicao ate ser resolvido.
        if(index == "Lucene")
        {
            LuceneIndex indexInstance = new LuceneIndex(descriptor, descriptorClass);
        }
        else {
            IndexMap indMap = IndexMap.valueOf(index.toUpperCase());
            Class<?> indexClass = indMap.getFeatureClass();
            I indexInstance = (I) indexClass.getConstructor().newInstance("indexes/" + index + "/", descriptor, descriptorClass);
        }
        return type;
    }
    */

    /*
    @GET
    @Path("/{type}/getDescriptors")
    //@Produces("application/json")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public String getDescriptors(@PathParam("type") String type) throws ClassNotFoundException, IOException {

        Reflections reflections = new Reflections("pt.inevo.encontra." + type + ".descriptors", new SubTypesScanner(false));
        Set<String> classes = reflections.getStore().getSubTypesOf(Object.class.getName());

        System.out.println(classes.toString());
        System.out.println(classes.size());
        return classes.toString();
    }


    public Set<String> getClassNames()
    {
        Reflections reflections = new Reflections("pt.inevo.encontra", new SubTypesScanner(false));
        Set<String> classes = reflections.getStore().getSubTypesOf(Object.class.getName());
        return classes;
    }
*/

}
