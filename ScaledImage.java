import java.awt.image.*;
/**
Sometimes, a solid will be drawn larger than its width/height,
eg when attacking. To handle this, when solids' draw methods
are called, they return a ScaledImage object, which contains
both the image to be drawn and the dimensions by which to
scale it.

Get ScaledImages with the static get method.
**/
public class ScaledImage{
  //if something does not want to be drawn, it returns nullImage
  final public static ScaledImage nullImage = new ScaledImage();
  //the following is the only ScaledImage drawn in this game.
  //It just gets reused for each Solid
  public BufferedImage image;
  public double widthCoefficient;
  public double heightCoefficient;
  private static ScaledImage scaledImage = new ScaledImage();
  
  public static ScaledImage get(BufferedImage image, double w, double h){
    scaledImage.image = image;
    scaledImage.widthCoefficient = w;
    scaledImage.heightCoefficient = h;
    return scaledImage;
  }
  public static ScaledImage get(BufferedImage image){
    return get(image, 1, 1);
  }
}