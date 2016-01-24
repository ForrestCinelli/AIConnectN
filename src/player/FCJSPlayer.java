/** @author Forrest Cinelli
 *  @author Jingwei Shen
 *  due Jan 24, 2015
 * */

package player;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

/* Questions for ta:
 * can we modify board.java?
 * is there a size limit on the .zip file we submit? is there a limit on how much RAM our program uses?
 * */

public class FCJSPlayer 
{
	public String playerName = "FCJS";
	public int playerNumber;
	public int opponentNumber;
	public Board board; //current state of board
	BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
	boolean first_move=false;
	int timeLimit; //in seconds
	List<String> currentBestMove = Arrays.asList("0 1".split(" ")); //move formatting: first number is column to move in. (the first column is numbered 0, and the last is width - 1). Second number is move type: 0 means pop out and 1 means drop in. 
	
	public FCJSPlayer()
	{
		
	}
	
	//given a move, apply it to our board. Should happen after opponent declares move but before we begin searching the game tree and again after a move is returned.
	public void applyMove(List<String> move, Board b, int player)
	{
		if (Integer.parseInt(move.get(1)) == 0) //if move is a drop in
		{
			board.dropADiscFromTop(Integer.parseInt(move.get(0)), opponentNumber);
		}
		else //move is a pop out
		{
			board.removeADiscFromBottom(Integer.parseInt(move.get(0)));
		}
	}
	
	/* should be ready to return a move at any moment.*/
	//this needs to be implemented differently; to allow for time limiting, and also to not waste time updating our own board before the move is sent out.
	public String getNextMove()
	{
		applyMove(currentBestMove, board, playerNumber);
		return String.join(" ", currentBestMove);
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
	
	//this whole thing reads the input and updates info in our player appropriately. 
	public void processInput() throws IOException
	{	
	
    	String s=input.readLine();	
		List<String> ls=Arrays.asList(s.split(" "));
		if(ls.size()==2) //indicates that opponent made a move
		{
			applyMove(ls, board, opponentNumber);
		}
		else if(ls.size()==1) //indicates that game ended
		{
			System.out.println("ggwp");
			System.exit(0);
		}
		else if(ls.size()==5)
		{          //ls contains game info
			board = new Board(Integer.parseInt(ls.get(0)), //height 
							  Integer.parseInt(ls.get(1)), //width
							  Integer.parseInt(ls.get(2))  //N
							  );
			if (Integer.parseInt(ls.get(3)) == playerNumber) //whose move is it first
			{
				first_move = true;
			}
			else
			{
				first_move = false;
			}
			timeLimit = Integer.parseInt(ls.get(4));
			
			//right now this is not correct. We need to return before time is up. 
			System.out.println(this.getNextMove());  //first move
		}
		else if(ls.size()==4)
		{		//player1: aa player2: bb
			if ((ls.get(1)).equals(playerName))
			{
				playerNumber = 1;
				opponentNumber = 2;
			}
			else 
			{
				playerNumber = 2;
				opponentNumber = 1;
			}
		}
		else
		{
			System.out.println("not an input I understand");
		}
	}
	
	public static void main(String[] args) throws IOException 
	{
		FCJSPlayer us = new FCJSPlayer();
		System.out.println(us.playerName);
		while (true)
		{
			us.processInput();
		}
	}
	
	/* The tree we'll build / search over as we look for good moves.
	 * */
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
