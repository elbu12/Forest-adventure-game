/**
 * This class is like an ArrayList for ints, but easily editable.
 * If an int is removed from the middle of this array, the int
 * from the end of the array is moved to its place. Thus, element
 * removal happens in constant time, but ordering is not preserved.
**/
public class EditableIntArray{
  private int[] a;
  private int size = 0;
  public EditableIntArray(int length){
    a = new int[length];
  }
  public EditableIntArray(){
    this(100);
  }
  public void add(int x){
    //Does this exceed the limits?
    if (size == a.length){
      //Expand!
      int[] newa = new int[a.length*2];
      System.arraycopy(a, 0, newa, 0, a.length);
      a = newa;
    }
    a[size] = x;
    size++;
  }
  public void remove(int i){
    //Note that this method removes the int with index number i.
    //It does NOT remove the int with VALUE of i.
    size--;
    if (i < size){
      //move the last element to fill the hole of the one being removed
      a[i] = a[size];
    }
  }
  public int get(int i){
    if (i < size && i >= 0){
      return a[i];
    }
    //error! Trying to get something null!
    return -1;
  }
  public void set(int i, int value){
    a[i] = value;
  }
  public int size(){
    return size;
  }
  public void clear(){
    size = 0;
  }
  public boolean contains(int d){
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