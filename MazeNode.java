import java.util.*;
/**
Unlike PathNodes, MazeNodes know their neighbors (nodes to which they connect).
MazeNodes are used to build "mazes." Unless the map is expected to change
during the game, MazeNodes should only be used when the game begins, to
build a maze into the map.

Each MazeNode represents a single contiguous area (not necessarily a single
tile). A collection of MazeNodes can be given to the MazeNode class' static
"explore" method, which will then "explore" the maze, beginning at the
first node in the collection. The boolean returned represents whether
every node in the collection can be reached, starting from the first.

The "explore" method's purpose is to verify that each part of the maze
can be reached from each other part. If the return value is false, then
some part(s) has been isolated.

This information is stored internally in an adjacency matrix.
**/

public class MazeNode {
  //the maximum number of nodes
  final public static int maxNodes = 64;
  //num represents this node's index number in the adjacency matrix
  final public int num;
  //nextNum represents the next num for the next MazeNode to be created
  private static int nextNum = 0;
  //This is the actual adjacency matrix
  //Note that it assumes ≤maxNodes nodes
  private static boolean[][] adjacency = new boolean[maxNodes][maxNodes];
  //The map from node index numbers back to nodes
  private static MazeNode[] nodes = new MazeNode[maxNodes];
  //a list of nodes, to be returned by the createMaze method
  //Note that it does not necessarily contain all nodes!
  private static ArrayList <MazeNode> list = new ArrayList <MazeNode> (maxNodes);
  //Used for exploring
  private static boolean[] reached = new boolean[maxNodes];
  //returned when neighbors are requested
  private static ArrayList <MazeNode> neighbors = new ArrayList <MazeNode> (maxNodes-1);
  //used for making mazes
  private static ArrayList <MazeEdge> edges = new ArrayList <MazeEdge> (2*maxNodes);
  
  public MazeNode(){
    num = nextNum;
    nextNum++;
    nodes[num] = this;
  }
  
  public void connect(MazeNode m){
    adjacency[num][m.num] = true;
    adjacency[m.num][num] = true;
  }
  
  public void disconnect(MazeNode m){
    adjacency[num][m.num] = false;
    adjacency[m.num][num] = false;
  }
  
  public AbstractList <MazeNode> getNeighbors(){
    neighbors.clear();
    for (int i=0; i<maxNodes; i++){
      if (i == num){
        continue;
      }
      if (adjacency[num][i]){
        neighbors.add(nodes[i]);
      }
    }
    return neighbors;
  }
  
  public boolean connectsTo(MazeNode n){
    return (adjacency[num][n.num]);
  }
  
  //can every node in this array be reached from the array's first node?
  public static boolean explore(MazeNode[][] a){
    //clear the array of "reached" nodes
    for (int i=0; i<reached.length; i++){
      reached[i] = false;
    }
    //branch out recursively from the first
    MazeNode n0 = a[0][0];
    reached[n0.num] = true;
    explore(n0);
    //now: was every node in l reached?
    for (MazeNode[] l : a){
      for (MazeNode n : l){
        if (!reached[n.num]){
          //n was not hit
          return false;
        }
      }
    }
    return true;
  }
  
  //this explores the direct neighbors of a single node
  private static void explore(MazeNode n){
    //go through n's neighbors
    for (int i=0; i<maxNodes; i++){
      //don't worry about this node's connection to itself
      if (i == n.num){
        continue;
      }
      if (adjacency[n.num][i]){
        //node i is adjacent
        //have we already reached it?
        if (reached[i]){
          //yes; skip it
        }
        else {
          //no; "reach" it
          reached[i] = true;
          //now explore it
          explore(nodes[i]);
        }
      }
    }
  }
  
  /**
  This returns a matrix of width x height nodes, such that every
  node is connected to the nodes directly next to it
  **/
  private static MazeNode[][] createConnectedMaze(int width, int height){
    MazeNode m[][] = new MazeNode[width][height];
    for (int i=0; i<width; i++){
      for (int j=0; j<height; j++){
        m[i][j] = new MazeNode();
      }
    }
    //go through each node
    for (int i=0; i<width; i++){
      for (int j=0; j<height; j++){
        //connect it to the nodes right and below it
        if (i < width - 1){
          m[i][j].connect(m[i+1][j]);
        }
        if (j < height - 1){
          m[i][j].connect(m[i][j+1]);
        }
      }
    }
    return m;
  }
  
  /**
  This creates a maze with "cycles" cycles. If cycles==0, then the maze
  will have no cycles and qualify as a tree.
  **/
  public static MazeNode[][] createMaze(int width, int height, int cycles){
    //make a fully connected maze
    MazeNode[][] m = createConnectedMaze(width, height);

    //collect its edges
    edges.clear();
    for (int i=0; i<width; i++){
      for (int j=0; j<height; j++){
        if (i<width - 1){
          edges.add(new MazeEdge(m[i][j], m[i+1][j]));
        }
        if (j<height - 1){
          edges.add(new MazeEdge(m[i][j], m[i][j+1]));
        }
      }
    }
    //shuffle edges
    Collections.shuffle(edges);
    //now begin removing edges if possible
    for (int i=cycles; i<edges.size(); i++){
      //get an edge
      MazeEdge e = edges.get(i);
      //tentatively remove e
      e.get(true).disconnect(e.get(false));
      if (!explore(m)){
        //oops! That connection/edge was necessary to prevent isolation
        e.get(true).connect(e.get(false));
      }
    }
    return m;
  }
  
  //if cycles is not given, it is assumed to be zero
  public static MazeNode[][] createMaze(int width, int height){
    return createMaze(width, height, 0);
  }
}