package pt.inevo.encontra.rest.mapping;

/**
 * Created by jpvguerreiro on 9/18/2014.
 */

import pt.inevo.encontra.threed.descriptors.shapeDistribution.*;

public enum ThreedDescriptorMap {
    D1(D1.class),
    D2(D2.class),
    D3(D3.class),
    D4(D4.class),
    A3(A3.class);
    ;

    private Class<?> featureClass;

    ThreedDescriptorMap(Class<?> featureClass) {
        this.featureClass = featureClass;
    }

    public Class<?> getFeatureClass() {
        return featureClass;
    }

    public static ThreedDescriptorMap getByName(String name) {
        return valueOf(name.toUpperCase());
    }
}
