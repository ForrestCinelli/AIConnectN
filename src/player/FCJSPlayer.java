package player;

/* Questions for ta:
 * why does running referee cause an error?
 * how can our player know how much time it has to make a move? Similarly, how can our player know waht the size of the board is?
 * can we modify board.java?
 * is there a size limit on the .zip file we submit? is there a limit on how much RAM our program uses?
 * */

public class FCJSPlayer {

	public Board board; //current state of board
	
	public FCJSPlayer()
	{
		
	}
	
	/* Does alpha beta pruning */
	public void prune()
	{
	
	}
	
	/* After player hears about opponent move, prune each part of the decision tree that is no longer reachable since the opponent chose a different move
	 * 
	 * */
	public void pruneAfterMove()
	{
		
	}

	/* do we need a separate function to determine if a board state is a 'game over' state?
	 * rank spaces, then try to control the best spaces. Spaces with more ways to win are ranked higher. Spaces whose ways to win are easier spaces to get to are ranked higher.
	 * make the heuristic like to force opponent moves as a way of controlling the game
	 * */
	public int eval(Board b)
	{
		return 0;
	}
	
	private class boardStateNode
	{
		public Board b;
		public boardStateNode[] children;
		public boardStateNode(Board b)
		{
			this.b = b;
			this.children = new boardStateNode[4];//board.width
		}
	} 
}
