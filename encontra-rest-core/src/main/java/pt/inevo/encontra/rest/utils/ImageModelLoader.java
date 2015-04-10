package pt.inevo.encontra.rest.utils;

import pt.inevo.encontra.storage.IEntity;
import pt.inevo.encontra.storage.ModelLoader;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Loader for Objects of the type: ImageModel.
 * @author Ricardo
 */
public class ImageModelLoader<I extends IEntity> extends ModelLoader {

    public ImageModelLoader() {
    }

    public ImageModelLoader(String imagesPath) {
        this.modelsPath = imagesPath;
    }

    @Override
     public ImageModel loadModel(File image) throws IOException {

        //for now only sets the filename
        ImageModel im = new ImageModel(image.getAbsolutePath(), "", null);

        //get the description
        //TO DO - load the description from here

        //get the bufferedimage
        BufferedImage bufImg = ImageIO.read(image);
        im.setImage(bufImg);

        return im;
    }

    @Override
    public BufferedImage loadBuffered(File image) throws IOException {
        return ImageIO.read(image);
    }

    public List<ImageModel> getModels(String path) throws IOException {
        File root = new File(path);
        String[] extensions = {"jpg", "png"};

        List<File> imageFiles = FileUtil.findFilesRecursively(root, extensions);
        List<ImageModel> images = new ArrayList<ImageModel>();

        for (File f : imageFiles) {
            images.add(loadModel(f));
        }

        return images;
    }

    public void load(String path) {
        File root = new File(path);
        String[] extensions = {"jpg", "png"};

        this.modelsFiles = FileUtil.findFilesRecursively(root, extensions);
    }

    public void load() {
        load(this.modelsPath);
    }

    public List<ImageModel> getImages() throws IOException {
        return getModels(this.modelsPath);
    }

    @Override
    public Iterator<File> iterator() {
        return modelsFiles.iterator();
    }
}
