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

import pt.inevo.encontra.index.search.SimpleSearcher;
import pt.inevo.encontra.lucene.index.LuceneEngine;
import pt.inevo.encontra.lucene.index.LuceneIndex;
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
     * @param path Images folder
     * @return
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws InstantiationException
     */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/{type}/storeIndexes")
    public String storeIndexes (@PathParam("type") String type, @QueryParam("path") String path) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

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

        IndexMap[] indexes = IndexMap.values();
        for(IndexMap indexMap: indexes) {

            Class<?> indexClass = indexMap.getFeatureClass();
            String index=indexMap.toString().toUpperCase();

            for (ImageDescriptorMap descMap : descriptors) {

                SimpleEngine<ImageModel> e = new SimpleEngine();
                e.setObjectStorage(storage);

                //Fixer for now. Should have a mapper also?
                AbstractSearcher imageSearcher;
                if (index.equals("BTREE")) {
                    imageSearcher = new NBTreeSearcher();
                } else {
                    imageSearcher = new SimpleSearcher();
                }


                Class<?> descriptorClass = descMap.getFeatureClass();

                D myInstance = (D) descriptorClass.getConstructor().newInstance();

                I indexInstance = (I) indexClass.getConstructor(String.class, Class.class).newInstance("data/indexes/" + index.toLowerCase() + "/" + descMap.toString(), descriptorClass);
                imageSearcher.setIndex(indexInstance);

                System.out.println(myInstance.toString());
                //using a single descriptor
                imageSearcher.setDescriptorExtractor(myInstance);

                e.setSearcher("image", imageSearcher);

                System.out.println("Loading some objects to the test indexes");
                //Inserting the models in the index. The objects will be stored only in the first time
                for (ImageModel img : imgModels) {
                    e.insert(img);
                }
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

        IndexMap indexMap = IndexMap.valueOf(index.toUpperCase());
        Class<?> indexClass = indexMap.getFeatureClass();

        SimpleEngine<ImageModel> e = new SimpleEngine<ImageModel>();
        e.setObjectStorage(storage);

        //Fixer for now. Should have a mapper also?
        AbstractSearcher imageSearcher;
        if(index.toUpperCase().equals("BTREE")){
            imageSearcher = new NBTreeSearcher();
        }
        else {
            imageSearcher = new SimpleSearcher();
        }

        ImageDescriptorMap descMap = ImageDescriptorMap.valueOf(descriptor.toUpperCase());
        Class<?> descriptorClass = descMap.getFeatureClass();

        D myInstance = (D) descriptorClass.getConstructor().newInstance();

        System.out.println(myInstance.toString());
        //using a single descriptor
        imageSearcher.setDescriptorExtractor(myInstance);


        I indexInstance = (I) indexClass.getConstructor(String.class, Class.class).newInstance("data/indexes/"+index+"/"+descMap.toString(), descriptorClass);

        imageSearcher.setIndex(indexInstance);

        e.setSearcher("image", imageSearcher);

        System.out.println("Loading some objects to the test indexes...");

        ImageModelLoader loader = new ImageModelLoader(path);
        loader.load();
        Iterator<File> it = loader.iterator();

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
    @Path("/{type}/{index}/similar")
    public String similar(@PathParam("type") String type, @PathParam("index") String index, @QueryParam("descriptor") String descriptor, @QueryParam("path") String path) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        //Creating the engine
        System.out.println("Creating the Retrieval Engine...");

        //Lucene Index does not work with SimpleEngine
        //Current fix, but need to do something better. Also a map?
        AbstractSearcher e;
        if(index.toUpperCase()=="LUCENE"){
            e = new LuceneEngine();
        }
        else {
            e = new SimpleEngine();
        }

        EntityStorage storage = new SimpleFSObjectStorage(ImageModel.class);

        e.setObjectStorage(storage);

        ImageDescriptorMap descMap = ImageDescriptorMap.valueOf(descriptor.toUpperCase());
        Class<?> descriptorClass = descMap.getFeatureClass();
        D myInstance = (D) descriptorClass.getConstructor().newInstance();

        IndexMap indexMap = IndexMap.valueOf(index.toUpperCase());
        Class<?> indexClass = indexMap.getFeatureClass();
        I indexInstance = (I) indexClass.getConstructor(String.class, Class.class).newInstance("data/indexes/"+index+"/"+descMap.toString(), descriptorClass);

        //Fixer for now. Should have a mapper also?
        AbstractSearcher imageSearcher;
        if(index.toUpperCase().equals("BTREE")){
            imageSearcher = new NBTreeSearcher();
        }
        else {
            imageSearcher = new SimpleSearcher();
        }

        imageSearcher.setIndex(indexInstance);

        //using a single descriptor
        imageSearcher.setDescriptorExtractor(myInstance);

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

}
