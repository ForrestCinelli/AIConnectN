/** @author Forrest Cinelli
 *  @author Jingwei Shen
 *  due Jan 24, 2015
 * */

package player;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

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
	long turnStart; //in nanoseconds
	List<String> currentBestMove = Arrays.asList("0 1".split(" ")); //move formatting: first number is column to move in. (the first column is numbered 0, and the last is width - 1). Second number is move type: 0 means pop out and 1 means drop in. 
	double currentBestMoveValue = Double.NEGATIVE_INFINITY;
	BoardStateNode bs;
	boolean weHaveUsedPopOut = false;
	boolean theyHaveUsedPopOut = false;
	PrintWriter writer;
	final int POPOUT = 0;
	final int DROP = 1;
	
	//params - these are things we can change to tweak the behavior of our player
	public final double timeBuffer = 0.1; //in seconds
	public final double tieVal = 0.0;
	
	public FCJSPlayer()
	{
		Random random = new Random();
		int num = random.nextInt(10000);
		try
		{
			this.writer = new PrintWriter("FCJSPlayer" + Integer.toString(num) + "output.txt", "UTF-8");
		} catch (Exception e)
		{
			System.out.println("Couldn't make output file for some reason. Will crash soon.");
			e.printStackTrace();
		}
		playerName += Integer.toString(num);
	}
	
	//given a move, apply it to our board. Should happen after opponent declares move but before we begin searching the game tree and again after a move is returned.
	public void applyMove(List<String> move, Board b, int player)
	{
		if (Integer.parseInt(move.get(1)) == this.DROP) //if move is a drop in
		{
			b.dropADiscFromTop(Integer.parseInt(move.get(0)), opponentNumber);
		}
		else //move is a pop out
		{
			b.removeADiscFromBottom(Integer.parseInt(move.get(0)));
		}
		this.writer.println(this.board.toString());
	}
	
	public double timeRemaining()
	{
		return (this.timeLimit * 1000000000) - (System.nanoTime() - this.turnStart);
	}
	
	/*builds and expands the search tree*/
	public void search()
	{
		int depth = 0;
		while (this.timeRemaining() > timeBuffer) //while we still have time
		{
			for (ArrayList<String> move : this.getValidMoves(board, this.playerNumber))
			{
				double val = minimax(this.bs, depth, true);
				if (val > this.currentBestMoveValue)
				{
					this.currentBestMove = move; //might cause currentBestMove to end up being null; better watch out for that
					this.currentBestMoveValue = val;
				}
			}
			depth++;
		}
	}
	
	public boolean canPopOut(int playerNum)
	{
		if (playerNum == this.playerNumber)
			return this.weHaveUsedPopOut;
		else
			return this.theyHaveUsedPopOut;
	}
	
	public ArrayList<ArrayList<String>> getValidMoves(Board b, int playerNum)
	{
		ArrayList<ArrayList<String>> output = new ArrayList<ArrayList<String>>();
		for (int j = 0; j < b.width; j++)
		{
			//drop in
			if (b.canDropADiscFromTop(j, playerNum))
			{
				ArrayList<String> move = new ArrayList<String>(2);
				move.add(0, Integer.toString(this.DROP));
				move.add(1, Integer.toString(j));
				output.add(move);
			}
			//pop out
			if (canPopOut(playerNum) && b.canRemoveADiscFromBottom(j, playerNum))
			{
				ArrayList<String> move = new ArrayList<String>(2);
				move.add(0, Integer.toString(this.POPOUT));
				move.add(1, Integer.toString(j));
				output.add(move);
			}
		}
		return output;
	}
	
	public double minimax(BoardStateNode node, int depth, boolean isMax)
	{
		int result = node.b.isConnectN();
		if (result != -1)
		{
			if (result == this.playerNumber) //winning board state
			{
				return Double.POSITIVE_INFINITY;
			}
			if (result == 0) //tie
			{
				return this.tieVal;
			}
			if (result == this.opponentNumber) //losing board state
			{
				return Double.NEGATIVE_INFINITY;
			}
		}
		if (depth == 0)
		{
			return eval(node.b);
		}
		if (isMax)
		{
			double bestValue = Double.NEGATIVE_INFINITY;
			for (List<String> move: this.getValidMoves(node.b, isMax ? this.playerNumber : this.opponentNumber))
			{
				//make child board, 
				Board newB = new Board(this.board.height, this.board.width, this.board.N);
				for (int i = 0; i < node.b.board.length; i++) //for each row
				{
					System.arraycopy(node.b.board[i], 0, newB.board[i], 0, node.b.board[i].length); //copy the col
				}
				applyMove(move, newB, this.playerNumber);
				
				BoardStateNode newChild = new BoardStateNode(newB, move, this.playerNumber);
				node.children.add(newChild); //TODO: currently this ignores existing stuff. We should fix that.
				 
				double val = minimax(newChild, depth - 1, !isMax);
				if (bestValue < val)
				{
					bestValue = val;
				}
			}
			return bestValue;
		}
		else //is min
		{
			double bestValue = Double.POSITIVE_INFINITY;
			for (List<String> move: this.getValidMoves(node.b, isMax ? this.playerNumber : this.opponentNumber))
			{
				//make child board, 
				Board newB = new Board(this.board.height, this.board.width, this.board.N);
				for (int i = 0; i < node.b.board.length; i++) //for each row
				{
					System.arraycopy(node.b.board[i], 0, newB.board[i], 0, node.b.board[i].length); //copy the col
				}
				applyMove(move, newB, this.opponentNumber);
				
				BoardStateNode newChild = new BoardStateNode(newB, move, this.opponentNumber);
				node.children.add(newChild); //TODO: currently this ignores existing stuff. We should fix that.
				 
				double val = minimax(newChild, depth - 1, !isMax);
				if (bestValue > val)
				{
					bestValue = val;
				}
			}
			return bestValue;
		}
	}
	
	/* should be ready to return a move at any moment.*/
	//this needs to be implemented differently; to allow for time limiting, and also to not waste time updating our own board before the move is sent out.
	public void makeNextMove()
	{
		this.writer.println("Getting ready to make a move!!");
		System.out.println(String.join(" ", this.currentBestMove));  //first move
		applyMove(currentBestMove, this.board, this.playerNumber);
		if (!this.weHaveUsedPopOut && this.currentBestMove.get(0).equals(Integer.toString(this.POPOUT))) this.weHaveUsedPopOut = true;
		this.currentBestMoveValue = Double.NEGATIVE_INFINITY;
	}
	
	/* Does alpha beta pruning */
	public void prune()
	{
	
	}
	
	/* After player hears about opponent move, prune each part of the decision tree that is no longer reachable since the opponent chose a different move
	 * Assumes that local board has already been updated
	 * */
	public void pruneAfterMove(List<String> move)
	{
		for (int i = 0; i < this.bs.children.size(); i++)
		{
			if ((this.bs.children.get(i).move).equals(move))
			{
				this.bs = this.bs.children.get(i); //TODO: this line may not work as intended. Be suspicious
				return; //end execution immediately
			}
		}
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
			this.turnStart = System.nanoTime();
			if (!this.theyHaveUsedPopOut && ls.get(0).equals(Integer.toString(this.POPOUT))) this.theyHaveUsedPopOut = true;
			applyMove(ls, board, this.opponentNumber);
			pruneAfterMove(ls);
			search(); //should terminate before time runs out
			this.makeNextMove();
		}
		else if(ls.size()==1) //indicates that game ended
		{
			System.out.println("ggwp");
			System.exit(0);
		}
		else if(ls.size()==5)
		{          //ls contains game info
			this.board = new Board(Integer.parseInt(ls.get(0)), //height 
							  Integer.parseInt(ls.get(1)), //width
							  Integer.parseInt(ls.get(2))  //N
							  );
			if (Integer.parseInt(ls.get(3)) == playerNumber) //whose move is it first
			{
				this.first_move = true;
				//just move in the middle.
				this.currentBestMove = Arrays.asList((Integer.toString((this.board.width/2)) + " 1").split(" "));
				this.makeNextMove(); //first move
			}
			else
			{
				this.first_move = false;
			}
			this.bs = new BoardStateNode(this.board, null, this.first_move ? this.playerNumber : this.opponentNumber);
			this.timeLimit = Integer.parseInt(ls.get(4));
			
			
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
	private class BoardStateNode
	{
		public Board b;
		public List<String> move;
		public ArrayList<BoardStateNode> children;
		public int currPlayer;
		public BoardStateNode(Board b, List<String> move, int currPlayer)
		{
			this.b = b;
			this.children = new ArrayList<BoardStateNode>(1);
			this.move = move;
			this.currPlayer = currPlayer;
		}
		
		
	} 
}
