import java.awt.image.*;
import java.awt.*;
import javax.imageio.*;
import java.io.*;
import javax.swing.*;
//This class is for getting images (obviously)
public class ImageMaker{
  
  //getAndSplit take a larger image and splits it into an array of smaller images,
  //each 50x50
  public static BufferedImage[][] getAndSplit2(String name){
    BufferedImage bigImage = null;
    BufferedImage[][] images;
    try {
      bigImage = ImageIO.read(new File(name));
      images = new BufferedImage[bigImage.getWidth()/50][bigImage.getHeight()/50];
      for (int i=0; i<images.length; i++){
        for (int j=0; j<images[i].length; j++){
          images[i][j] = new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB);
          Graphics g = images[i][j].createGraphics();
          g.drawImage(bigImage, -50*i, -50*j, null);
        }
      }
    }
    catch (Exception e){
      JOptionPane.showMessageDialog(null, "Error! Could not find " + name);
      System.exit(0);
      images = new BufferedImage[0][0];
    }
    return images;
  }
  
  //Now the 1-dimensional version, for when ordering is irrelevant
  public static BufferedImage[] getAndSplit(String name){
    BufferedImage[][] images2 = getAndSplit2(name);
    BufferedImage[] images1 = new BufferedImage[images2.length*images2[0].length];
    for (int i=0; i<images2.length; i++){
      for (int j=0; j<images2[i].length; j++){
        images1[(i*images2.length) + j] = images2[i][j];
      }
    }
    return images1;
  }
  
  public static BufferedImage get(String name){
    BufferedImage image = null;
    try {
      image = ImageIO.read(new File(name));
    }
    catch (Exception e){
      JOptionPane.showMessageDialog(null, "Error! Could not find " + name);
      System.exit(0);
    }
    return image;
  }
  
  public static BufferedImage getBlank(){
    BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
    Graphics g = image.createGraphics();
    g.setColor(Color.BLACK);
    g.fillRect(0,0,1,1);
    return image;
  }
  
  public static BufferedImage getBridge(){
    BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
    Graphics g = image.createGraphics();
    g.setColor(new Color(80, 60, 0));
    g.fillRect(0,0,1,1);
    return image;
  }
  
  public static BufferedImage getStone(){
    BufferedImage image = new BufferedImage(5, 5, BufferedImage.TYPE_INT_RGB);
    Graphics g = image.createGraphics();
    g.setColor(Color.LIGHT_GRAY);
    g.fillRect(0,0,5,5);
    return image;
  }
  
  public static BufferedImage[][] getRiver(String name){
    //getRiver takes a long image and makes an array of images from it, such that
    //each image is a 100x100 cropping of it.
    //"Playing" these images in order gives the appearance of panning across the
    //larger image.
    BufferedImage bigRiver = null; //The big image
    BufferedImage doubleRiver;     //The big image, repeated
    BufferedImage[][] images;        //Each individual image
    try {
      bigRiver = ImageIO.read(new File(name));
      doubleRiver = new BufferedImage(bigRiver.getWidth() * 2, bigRiver.getHeight(), BufferedImage.TYPE_INT_RGB);
      Graphics drg = doubleRiver.createGraphics();
      drg.drawImage(bigRiver, 0, 0, null);
      drg.drawImage(bigRiver, bigRiver.getWidth(), 0, null);
      images = new BufferedImage[bigRiver.getWidth()][2];
      for (int i=0; i<images.length; i++){
        for (int j=0; j<2; j++){
          images[i][j] = new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB);
          Graphics g = images[i][j].createGraphics();
          g.drawImage(doubleRiver, -i, -(j*50), null);
        }
      }
    }
    catch (Exception e){
      JOptionPane.showMessageDialog(null, "Error! Could not find " + name);
      System.exit(0);
      images = new BufferedImage[0][0];
    }
    return images;
  }
}