/**Resetters are objects that can "reset"
   Over the course of a game iteration, objects can put
   their selves on the list of "resetters."
   When the game runs its cycle, after drawing the screen,
   all resetters on the list will be called to reset, and
   the list will be emptied.
   
   Making something a "resetter" is a way to give it the
   ability to act after being drawn, before the next game
   cycle begins.
**/

public interface Resetter{
  public void reset();
}