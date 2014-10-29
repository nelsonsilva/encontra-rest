package pt.inevo.encontra.rest.map;

/**
 * Created by jpvguerreiro on 9/18/2014.
 */

import pt.inevo.encontra.image.descriptors.*;

public enum ImageDescriptorMap {
    CEDD(CEDDDescriptor.class),
    COLORLAYOUT(ColorLayoutDescriptor.class),
    DOMINANTCOLOR(DominantColorDescriptor.class),
    EDGEHISTOGRAM(EdgeHistogramDescriptor.class),
    FCTH(FCTHDescriptor.class);
    ;

    private Class<?> featureClass;

    ImageDescriptorMap(Class<?> featureClass) {
        this.featureClass = featureClass;
    }

    public Class<?> getFeatureClass() {
        return featureClass;
    }

    public static ImageDescriptorMap getByName(String name) {
        return valueOf(name.toUpperCase());
    }
}
