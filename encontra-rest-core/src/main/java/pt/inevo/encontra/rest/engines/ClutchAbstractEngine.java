package pt.inevo.encontra.rest.engines;

import pt.inevo.encontra.common.DefaultResultProvider;
import pt.inevo.encontra.common.Result;
import pt.inevo.encontra.descriptors.Descriptor;
import pt.inevo.encontra.descriptors.DescriptorExtractor;
import pt.inevo.encontra.engine.SimpleIndexedObjectFactory;
import pt.inevo.encontra.image.descriptors.ColorLayoutDescriptor;
import pt.inevo.encontra.index.IndexedObject;
import pt.inevo.encontra.index.search.AbstractSearcher;
import pt.inevo.encontra.index.search.SimpleSearcher;
import pt.inevo.encontra.lucene.index.LuceneIndex;
import pt.inevo.encontra.query.QueryProcessorDefaultImpl;
import pt.inevo.encontra.rest.utils.ImageModel;
import pt.inevo.encontra.rest.utils.ImageModelLoader;
import pt.inevo.encontra.storage.IEntity;
import pt.inevo.encontra.storage.IEntry;
import pt.inevo.encontra.storage.ModelLoader;
import pt.inevo.encontra.storage.SimpleFSObjectStorage;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by jpvguerreiro on 10/30/2014.
 */
public abstract class ClutchAbstractEngine<O extends IEntity, D extends DescriptorExtractor> extends AbstractSearcher<O> {

    protected ModelLoader loader;
    protected String type;
    protected Class modelClass;
    protected String desc;

    protected LuceneIndex index;


    public ClutchAbstractEngine() {
        setQueryProcessor(new QueryProcessorDefaultImpl());
        setIndexedObjectFactory(new SimpleIndexedObjectFactory());
        setResultProvider(new DefaultResultProvider());
    }

    public abstract void setEngine() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException;

    @Override
    protected Result<O> getResultObject(Result<IEntry> entryResult) {
        return new Result<O>((O) storage.get( Long.parseLong(entryResult.getResultObject().getId().toString())));
    }

    public ModelLoader getLoader() {
        return loader;
    }

    public void setLoader(ModelLoader loader) {
        this.loader = loader;
    }

    public Class getModelClass() {
        return modelClass;
    }

    public void setModelClass(Class model) {
        this.modelClass = model;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void closeIndex() throws IOException {this.index.close();}

}