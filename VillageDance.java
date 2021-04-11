/**
This determines the dance pattern of the village dancers.
At every game cycle iteration, it updates.
Dancers can then refer to this to get their required position
and direction.
**/
public class VillageDance implements Actor{
  public int i = 0; //i is where this is in the process
  
  //"beat" is how long certain patterns take
  //related to the "beat" of the music
  final static int beat = 100;
  //"step" is how far the dancer goes in a beat
  final static double step = 2.0/beat;
  
  public VillageDance(){
    //nothing for the constructor to really do
  }
  
  public void act(){
    i++;
    if (Game.timeSpeed == 1){
      i++;
    }
    if (i >= beat*6){
      i = 0;
    }
  }
  public double getx(boolean role){
    //this returns the expected x-value of a dancer
    //"role" refers to whether they take the male or female role
    if (i < beat){
      //move up
      return 0;
    }
    else if (i < beat*2){
      //move right
      return (i-beat)*step;
    }
    else if (i < beat*3){
      //move down
      return 2;
    }
    else if (i < beat*4){
      //move left
      return ((beat*4)-i)*step;
    }

    else if (i < beat*6){
      //circle
      //how far into the circle are we?
      double a = (i - (beat*4.0))/(beat*2.0);
      if (role){
        
        return -0.55 * Math.sin(Math.PI * 2.0 * a);
      }
      else {
        return 0.55 * Math.sin(Math.PI * 2.0 * a);
      }
    }
    else {
      return 0;
    }
  }
  public double gety(boolean role){
    //this returns the expected y-value of a dancer
    //"role" refers to whether they take the male or female role
    if (i < beat){
      //move up
      return -(i*step);
    }
    else if (i < beat*2){
      //move right
      return -2;
    }
    else if (i < beat*3){
      //move down
      return -2 + ((i-(beat*2))*step);
    }
    else if (i < beat*4){
      //move left
      return 0;
    }
    else if (i < beat*6){
      //circle
      //how far into the circle are we?
      double a = (i - (beat*4.0))/(beat*2.0);
      if (role){
        return 0.55 + (0.55 * Math.cos(Math.PI + (Math.PI * 2.0 * a)));
      }
      else {
        return (0.55 * Math.cos(Math.PI * 2.0 * a)) - 0.55;
      }
    }
    else {
      return 0;
    }
  }
  public double getDirection(boolean role){
    if (i < beat*9/2){
      return (role ? Math.PI : 0);
    }
    else if (i < beat*11/2){
      return ( (i - (beat*9/2))*Math.PI/beat) + (role ? Math.PI : 0);
    }
    else if (i < beat*6){
      return ( (i - (beat*11/2))*Math.PI*2/beat) + (role ? 0 : Math.PI);
    }
    else {
      return 0;
    }
  }
}