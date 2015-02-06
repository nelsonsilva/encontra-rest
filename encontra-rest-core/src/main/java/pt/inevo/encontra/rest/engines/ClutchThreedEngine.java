package pt.inevo.encontra.rest.engines;

import pt.inevo.encontra.common.DefaultResultProvider;
import pt.inevo.encontra.common.Result;
import pt.inevo.encontra.descriptors.DescriptorExtractor;
import pt.inevo.encontra.engine.SimpleIndexedObjectFactory;
import pt.inevo.encontra.image.descriptors.ColorLayoutDescriptor;
import pt.inevo.encontra.index.IndexedObject;
import pt.inevo.encontra.index.search.AbstractSearcher;
import pt.inevo.encontra.index.search.SimpleSearcher;
import pt.inevo.encontra.lucene.index.LuceneIndex;
import pt.inevo.encontra.query.QueryProcessorDefaultImpl;
import pt.inevo.encontra.rest.mapping.ThreedDescriptorMap;
import pt.inevo.encontra.rest.utils.ImageModel;
import pt.inevo.encontra.rest.utils.ImageModelLoader;
import pt.inevo.encontra.storage.IEntity;
import pt.inevo.encontra.storage.IEntry;
import pt.inevo.encontra.storage.SimpleFSObjectStorage;
import pt.inevo.encontra.threed.descriptors.Histogram;
import pt.inevo.encontra.threed.descriptors.shapeDistribution.*;
import pt.inevo.encontra.threed.model.ThreedModel;
import pt.inevo.encontra.threed.model.utils.ThreedModelLoader;
import pt.inevo.encontra.threed.utils.Normalize;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by jpvguerreiro on 10/30/2014.
 */
public class ClutchThreedEngine<O extends IEntity, D extends DescriptorExtractor> extends ClutchAbstractEngine<O,D> {


    public ClutchThreedEngine(String descriptor) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        super();
        desc = descriptor.toUpperCase();
        setEngine();
    }

    public ClutchThreedEngine() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        super();
        setEngine();
    }

    public  void setEngine() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        loader = new ThreedModelLoader();
        type = "threedmodel";
        modelClass = ThreedModel.class;
        D descriptor;
        if (desc == null){
            desc = "";
            descriptor = (D) new D2();
        }
        else {
            ThreedDescriptorMap descMap = ThreedDescriptorMap.valueOf(desc);
            Class<?> descriptorClass = descMap.getFeatureClass();
            descriptor = (D) descriptorClass.getConstructor().newInstance();
        }

        String indexPath = "data/"+type+"/indexes/"+desc+"/";
        storage = new SimpleFSObjectStorage(modelClass, "data/"+type+"/objects/");
        SimpleSearcher<IndexedObject> searcher = new SimpleSearcher();
        index = new LuceneIndex(indexPath, Histogram.class);
        searcher.setDescriptorExtractor(descriptor);
        searcher.setIndex(index);
        searcher.setQueryProcessor(new QueryProcessorDefaultImpl());
        searcher.setResultProvider(new DefaultResultProvider());
        setSearcher(type, searcher);
    }

    @Override
    protected Result<O> getResultObject(Result<IEntry> entryResult) {
        return new Result<O>((O) storage.get( Long.parseLong(entryResult.getResultObject().getId().toString())));
    }

    public ThreedModelLoader getLoader() {
        return (ThreedModelLoader) loader;
    }

    public void setLoader(ThreedModelLoader loader) {
        this.loader = loader;
    }

    public Class getModelClass() {
        return modelClass;
    }

    public void setModelClass(Class model) {
        this.modelClass = model;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public void setType(String type) {
        this.type = type;
    }

}