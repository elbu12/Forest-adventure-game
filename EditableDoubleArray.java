/**
 * This class is like an ArrayList for doubles, but easily editable.
 * If a double is removed from the middle of this array, the double
 * from the end of the array is moved to its place. Thus, element
 * removal happens in constant time, but ordering is not preserved.
**/
public class EditableDoubleArray{
  private double[] a;
  private int size = 0;
  public EditableDoubleArray(int length){
    a = new double[length];
  }
  public EditableDoubleArray(){
    this(100);
  }
  public void add(double x){
    //Does this exceed the limits?
    if (size == a.length){
      //Expand!
      double[] newa = new double[a.length*2];
      for (int i=0; i<size; i++){
        newa[i] = a[i];
      }
      a = newa;
    }
    a[size] = x;
    size++;
  }
  public void remove(int i){
    //Note that this method removes the double with index number i.
    //It does NOT remove the double with VALUE of i.
    size--;
    if (i < size){
      //move the last element to fill the hole of the one being removed
      a[i] = a[size];
    }
  }
  public double get(int i){
    if (i < size){
      return a[i];
    }
    //error! Trying to get something null!
    return -1;
  }
  public void set(int i, double value){
    a[i] = value;
  }
  public int size(){
    return size;
  }
  public void clear(){
    size = 0;
  }
  public boolean contains(double d){
    for (int i=0; i<size; i++){
      if (a[i] == d){
        return true;
      }
    }
    return false;
  }
  public String toString(){
    String s = "";
    for (int i=0; i<size; i++){
      s += a[i];
      if (i < size - 1){
        s+= ", ";
      }
    }
    return s;
  }
}