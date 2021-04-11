/**
This represents a "connection" between two MazeNodes, ie
an edge on a graph
**/
public class MazeEdge{
  private MazeNode m;
  private MazeNode n;
  public MazeEdge(MazeNode m, MazeNode n){
    this.m = m;
    this.n = n;
  }
  public boolean equals(MazeEdge e){
    return (m == e.get(true) && n == e.get(false)) || (m == e.get(false) && n == e.get(true));
  }
  public MazeNode get(boolean mn){
    return (mn ? m : n);
  }
}