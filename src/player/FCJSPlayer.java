package player;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

/* Questions for ta:
 * why does running referee cause an error?
 * how can our player know how much time it has to make a move? Similarly, how can our player know waht the size of the board is?
 * can we modify board.java?
 * is there a size limit on the .zip file we submit? is there a limit on how much RAM our program uses?
 * */

public class FCJSPlayer {
	public String playerName = "FCJS";
	public int playerNumber;
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
	
	BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
	boolean first_move=false;
	
	public void processInput() throws IOException
	{	
	
    	String s=input.readLine();	
		List<String> ls=Arrays.asList(s.split(" "));
		if(ls.size()==2){
			System.out.println(ls.get(0)+" "+ls.get(1));
		}
		else if(ls.size()==1){
			System.out.println("game over!!!");
			System.exit(0);
		}
		else if(ls.size()==5){          //ls contains game info
			board = new Board(Integer.parseInt(ls.get(0)), //height 
							  Integer.parseInt(ls.get(1)), //width
							  Integer.parseInt(ls.get(2))  //N
							  );
			System.out.println("0 1");  //first move
		}
		else if(ls.size()==4){		//player1: aa player2: bb
			if (ls.get(1).equals(playerName))
			{
				playerNumber = 1;
			}
			else 
			{
				playerNumber = 2;
			}
		}
		else
			System.out.println("not an input I understand");
	}
	
	public static void main(String[] args) throws IOException 
	{
		testPlayer rp=new testPlayer();
		System.out.println(rp.playerName);
		while (true)
		{
			rp.processInput();
		}
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
