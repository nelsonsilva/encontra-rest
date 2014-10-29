package pt.inevo.encontra.rest.map;

/**
 * Created by jpvguerreiro on 9/24/2014.
 */

import pt.inevo.encontra.lucene.index.*;
import pt.inevo.encontra.nbtree.index.*;

public enum IndexMap {
    LUCENE(LuceneIndex.class),
    BTREE(BTreeIndex.class),
    ;

    private Class<?> featureClass;

    IndexMap(Class<?> featureClass) {
        this.featureClass = featureClass;
    }

    public Class<?> getFeatureClass() {
        return featureClass;
    }

    public static IndexMap getByName(String name) {
        return valueOf(name.toUpperCase());
    }
}
