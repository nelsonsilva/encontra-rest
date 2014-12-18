package pt.inevo.encontra.rest;

import org.json.JSONException;
import org.json.JSONObject;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jpvguerreiro on 12/17/2014.
 */

@XmlRootElement
public class JsonReturnObject {

    @XmlElement(name="id")
    Long id;

    @XmlElement(name="path")
    String path;

    @XmlElement(name="similarity")
    Double similarity;

    public JsonReturnObject(){

    }

    public JsonReturnObject(Long id, String path, Double similarity){
        this.id = id;
        this.path = path;
        this.similarity = similarity;
    }

    @Override
    public String toString(){
        try {
            // takes advantage of toString() implementation to format {"a":"b"}
            return new JSONObject().put("id", id).put("path", path).put("similarity", similarity).toString();
        } catch (JSONException e) {
            return null;
        }
    }

}
