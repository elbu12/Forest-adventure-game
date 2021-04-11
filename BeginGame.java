/**
The game is called from here.

Do not create a static main method in the Game class that instantiates itself. For whatever reason, doing this causes errors with static variables. Perhaps future Java updates will fix that, but until then, this is the solution.

The game must be called from an outside file.
**/
public class BeginGame{
  public static void main(String[] args){
    new Game();
  }
}