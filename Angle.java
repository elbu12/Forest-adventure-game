//the purpose of this class is to receive a vector and return the angle
//of that vector, rounded to one of 128 angles

//This is its own separate class just to get its ugliness out of the way

//It uses a 2-D array. One dimension is the relevant value. Other
//dimension is whether you want a slope or an angle

//eg, data[a][0] is the ath slope value
//    data[a][1] is the corresponding ath angle

//To look up the angle for a vector, first distinguish x's sign
//Then do binary search to find the closest y/x slope value.
//Then the angle with the same index number is the answer.

public class Angle{
  
  static double[][] data;
  
  public static double get(double x, double y){
    // is x negative?
    if (x == 0){
      //x is zero. This is vertical!
      if (y < 0){
        //straight up
        return 0;
      }
      //straight down
      return Math.PI;
    }
    else if (x  > 0){
      return get(-x, -y) + Math.PI;
    }
    int beg = 0;
    int end = 128;
    double slope = y / x;
    while (end-beg > 1){
      //get the halfway point
      int halfway = (beg + end) / 2;
      //what is the slope of the halfway point?
      double halfwaySlope = data[halfway][0];
      if (slope == halfwaySlope){
        return data[halfway][1];
      }
      else if (slope > halfwaySlope){
        //halfwaySlope is too low; index number is too high
        end = halfway;
      }
      else {
        //halfwaySlope is too high; index number is too low
        beg = halfway;
      }
      //are we within one index number of the answer?
      //if so, the loop terminates
    }
    return data[beg][1];
  }
  
  //setUp sets up the array
  public static void setUp(){
    data = new double[65][2];
    //now iterate through and fill with angles and y/x slopes
    for (int i=1; i<64; i++){
      //go through and set the angles
      double angle = Math.PI * i / 64.0;
      data[i][1] = angle;
      //now set the corresponding slopes
      data[i][0] = Math.cos(angle)/Math.sin(angle);
    }
    //set the extremes with infinite slope
    data[0][1] = 0;
    data[64][1] = Math.PI;
    data[0][0] = Double.POSITIVE_INFINITY;
    data[64][0] = Double.NEGATIVE_INFINITY;
  }
}