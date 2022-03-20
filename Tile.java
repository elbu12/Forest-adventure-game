import java.util.*;
import java.awt.*;
import java.awt.image.*;
import javax.imageio.*;
import java.io.*;
import javax.swing.*;

//Note that tile locations indicate their upper-left corner, not their center
//This is different from solids!

public class Tile extends Obstacle{
  private static BufferedImage[] forestFloorImages = ImageMaker.getAndSplit("forestFloor.png");
  private static BufferedImage[] waterImages = ImageMaker.getAndSplit("water.png");
  private static BufferedImage[] swampImages = ImageMaker.getAndSplit("swamp.png");
  final private static BufferedImage blankImage = ImageMaker.getBlank();
  final private static BufferedImage bridgeImage = ImageMaker.getBridge();
  private static BufferedImage[][] stoneImages = getStone();
  private static BufferedImage[][] streetImages = getStreet();
  private static BufferedImage[] sandImages = ImageMaker.getAndSplit("sand.png");
  private static BufferedImage[][] riverImages = ImageMaker.getRiver("horizontalRiver.png");
  
  public static Actor riverAnimator = new Actor(){
    public void act(){
      riverIndex++;
      if (Game.timeSpeed == 1){
        riverIndex++; //run faster at normal speed
      }
      riverIndex %= riverImages.length;
    }
  };
  
  private static int riverIndex = 0;
 
  //stone tiles should "fit" together, so they're all chopped from a single, larger picture
  private static BufferedImage[][] getStone(){
    int w = 10;
    int h = 10;
    BufferedImage bigImage = new BufferedImage(48*w, 48*h, BufferedImage.TYPE_INT_RGB);
    Graphics g = bigImage.createGraphics();
    //the image is broken into 12x12 blocks. These can be combined to form 24x12,
    //12x24, or 24x24 blocks
    //"assigned" is whether a given block has been assigned its appearance
    boolean[][] assigned = new boolean[4*w][4*h];
    for (int i=0;i<4*w;i++){
      for (int j=0;j<4*h;j++){
        assigned[i][j] = false;
      }
    }
    
    Color grout = Color.LIGHT_GRAY;
    
    for (int i=0;i<4*w;i++){
      for (int j=0;j<4*h;j++){
        if (assigned[i][j]){
          //already drawn; skip
          continue;
        }
        if (j+1 < 4*h && !assigned[i][j+1]){
          //the block below this can be combined with this one
          if (i+1<4*w && Math.random() < 0.5){
            //the block right of this can be combined with this one
            //combine this with the one right of it, and possibly those below it
            if (Math.random() < 0.5){
              //make a 2x2 block
              g.setColor(grout);
              g.drawRect(i*12, j*12, 24, 24);
              g.setColor(randomStoneColor());
              g.fillRect((i*12)+1, (j*12)+1, 23, 23);
              assigned[i+1][j] = true;
              assigned[i][j+1] = true;
              assigned[i+1][j+1] = true;
            }
            else {
              //make a 2x1 block
              g.setColor(grout);
              g.drawRect(i*12, j*12, 24, 12);
              g.setColor(randomStoneColor());
              g.fillRect((i*12)+1, (j*12)+1, 23, 11);
              assigned[i+1][j] = true;
            }
          }
          else {
            //this cannot be combined with the block right of it
            //it may still be combined with block below it
            if (Math.random() < 0.5){
              //combine it with the block below it
              g.setColor(grout);
              g.drawRect(i*12, j*12, 12, 24);
              g.setColor(randomStoneColor());
              g.fillRect((i*12)+1, (j*12)+1, 11, 23);
              assigned[i][j+1] = true;
            }
            else {
              //just make this a 1x1 block
              g.setColor(grout);
              g.drawRect(i*12, j*12, 12, 12);
              g.setColor(randomStoneColor());
              g.fillRect((i*12)+1, (j*12)+1, 11, 11);
            }
          }
        }
        else {
          //the block below this cannot be combined with this one
          if (i+1 < 4*w && Math.random() < 0.5){
            //the block right of this can be combined with this one
            g.setColor(grout);
            g.drawRect(i*12, j*12, 24, 12);
            g.setColor(randomStoneColor());
            g.fillRect((i*12)+1, (j*12)+1, 23, 11);
            assigned[i+1][j] = true;
          }
          else {
            //draw this block on its own
            g.setColor(grout);
            g.drawRect(i*12, j*12, 12, 12);
            g.setColor(randomStoneColor());
            g.fillRect((i*12)+1, (j*12)+1, 11, 11);
          }
        }
        assigned[i][j] = true;
      }
    }
    
    BufferedImage[][] images = new BufferedImage[w][h];
    for (int i=0;i<w;i++){
      for (int j=0;j<h;j++){
        images[i][j] = new BufferedImage(48,48, BufferedImage.TYPE_INT_RGB);
        images[i][j].createGraphics().drawImage(bigImage, -48*(i), -48*(j), null);
      }
    }
    return images;
  }
  
  private static BufferedImage[][] getStreet(){
    int w = 100; //width of big image
    int h = 100; //height of big image
    
    int ww = 10; //width of individual cobblestone
    BufferedImage bigImage = new BufferedImage(48*w, 48*h, BufferedImage.TYPE_INT_RGB);
    Graphics g = bigImage.createGraphics();
    
    //set the grout color
    g.setColor(new Color(10,100,10));
    g.fillRect(0,0,w*48,h*48);
    
    for (int i=0;i<48*w;i+=ww){
      int j = -(ww*2);
      while (j < h*48){
        int hh = ww + (int)(Math.random()*(ww+1)); //height of each cobblestone is random
        g.setColor(randomStreetColor());
        g.fillRoundRect(i, j, ww-1, hh-1, (int)(Math.random()*4), (int)(Math.random()*4));
        j += hh;
      }
    }
    
    BufferedImage[][] images = new BufferedImage[w][h];
    for (int i=0;i<w;i++){
      for (int j=0;j<h;j++){
        images[i][j] = new BufferedImage(48,48, BufferedImage.TYPE_INT_RGB);
        images[i][j].createGraphics().drawImage(bigImage, -48*(i), -48*(j), null);
      }
    }
    return images;
  }
  
  static Color randomStoneColor(){
    int shade = 120+(int)(Math.random()*60);
    return new Color(shade, shade, shade);
  }
  static Color randomStreetColor(){
    int shade = 100+(int)(Math.random()*60);
    return new Color(shade+(int)(Math.random()*21), shade, shade);
  }
  
  //solids contained within this tile
  public ArrayList <Solid> solids = new ArrayList <Solid>(){
    //removing an element automatically moves the last element to this new "hole"
    //This avoids having to renumber the array
    public boolean remove(Object o){
      int index = indexOf(o);
      if (index == -1){
        return false;  //Object not in array!
      }
      if (index < size() - 1){
        set(index, remove(size() - 1)); //replace the old element with the one at the list's end
      }
      else {
        remove(size() - 1);
      }
      return true;
    }
  };
  //can solids walk across this?
  boolean walkable;
  
  public int x;
  public int y;
  
  public BufferedImage image;
  
  public int type;
  //types:
  final public static int FOREST_FLOOR = 0;
  final public static int WATER = 1;
  final public static int BRIDGE = 2;
  final public static int BLANK = 3;
  final public static int STONE_FLOOR = 4;
  final public static int UNDESIGNATED = 5;
  final public static int STONE_STREET = 6;
  final public static int SAND = 7;
  final public static int SWAMP = 8;
  final public static int DIRT = 9;
  final public static int FLOWER = 10;
  final public static int RIVER = 11;

  //drawBlack/Brown is just used for testing purposes
  public boolean drawBlack = false;
  public boolean drawBrown = false;
  
  public Tile (int x, int y){
    this.x = x;
    this.y = y;
    type = UNDESIGNATED;
    int animationIndex = 0;
  }
  
  public boolean isTile(){
    return true;
  }
  
  final public double getx(){
    return x;
  }
  final public double gety(){
    return y;
  }
  final public double getWidth(){
    return 1;
  }
  final public double getHeight(){
    return 1;
  }
  final public int getShape(){
    return Game.RECTANGLE;
  }
  public boolean canHaveTree;
  
  /**
  a Tile's constructor creates an "undesignated" tile
  Use setType to actually give it its properties
  **/
  public void setType(int type){
    this.type = type;
    switch (type){
      case FOREST_FLOOR:
        image = forestFloorImages[(int)(Math.random()*forestFloorImages.length)];
        canHaveTree = true;
        walkable = true;
        break;
      case WATER:
        image = waterImages[(int)(Math.random()*waterImages.length)];
        canHaveTree = false;
        walkable = false;
        break;
      case SWAMP:
        image = swampImages[(int)(Math.random()*waterImages.length)];
        canHaveTree = false;
        walkable = false;
        break;
      case BLANK:
        image = blankImage;
        canHaveTree = true;
        walkable = false;
        break;
      case BRIDGE:
        image = bridgeImage;
        canHaveTree = false;
        walkable = true;
        break;
      case STONE_FLOOR:
        image = stoneImages[x % stoneImages.length][y % stoneImages[0].length];
        canHaveTree = false;
        walkable = true;
        break;
      case STONE_STREET:
        image = streetImages[x % stoneImages.length][y % stoneImages[0].length];
        canHaveTree = false;
        walkable = true;
        break;
      case SAND:
        image = sandImages[(int)(Math.random()*sandImages.length)];
        canHaveTree = false;
        walkable = true;
        break;
      case RIVER:
        canHaveTree = false;
        walkable = false;
        break;
    }
  }
  
  public BufferedImage draw(){
    if (drawBrown){
      drawBrown = false;
      return bridgeImage;
    }
    if (drawBlack){
      drawBlack = false;
      return blankImage;
    }
    if (type == RIVER){
      /**
      Rivers are special because they are animated.
      The game tracks a special "riverIndex" variable
      Each river tile draws an image from an array. Its image is image number
      (riverIndex + cx), where x is its x-value and c is a constant.
      
      Note that rivers are assumed to run horizontally
      **/
      return riverImages[( (x*50) + riverIndex ) % riverImages.length][y%2];
    }
    return image;
  }
  
  //this part here is just for testing! ignore it!
  public static void main(String[] args){
    double x = 0.1;
    double y = 0.2;
    long t0;
    long t1;
     t0 = System.currentTimeMillis();
    for (int i=0; i<123450; i++){
      for (int j=0; j<67890; j++){
        int rrr = 1;
        int rrrr = 2;
        int rrrrr = Math.max(rrr, rrrr);
      }
    }
    t1 = System.currentTimeMillis();
    System.out.println(t1-t0);
    t0 = System.currentTimeMillis();
    for (int i=0; i<123450; i++){
      for (int j=0; j<67890; j++){
        int rrr = 1;
        int rrrr = 2;
        int rrrrr = (rrr > rrrr ? rrr : rrrr);
      }
    }
    t1 = System.currentTimeMillis();
    System.out.println(t1-t0);
  }
}