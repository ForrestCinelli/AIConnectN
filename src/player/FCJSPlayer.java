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
	public static String playerName = "FCJS";
	public static int playerNumber;
	public static int opponentNumber;
	public static Board board; //current state of board
	public static BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
	public static boolean first_move=false;
	public static int timeLimit; //in seconds
	public static long turnStart; //in nanoseconds
	public static List<String> currentBestMove = Arrays.asList("0 1".split(" ")); //move formatting: first number is column to move in. (the first column is numbered 0, and the last is width - 1). Second number is move type: 0 means pop out and 1 means drop in. 
	public static double currentBestMoveValue = Double.NEGATIVE_INFINITY;
	private static BoardStateNode bs;
	public static boolean weHaveUsedPopOut = false;
	public static boolean theyHaveUsedPopOut = false;
	private static PrintWriter writer;
	public static final int POPOUT = 0;
	public static final int DROP = 1;
	
	//params - these are things we can change to tweak the behavior of our player
	public final double timeBuffer = 1; //in seconds
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
	public static void applyMove(List<String> move, Board b, int player)
	{
		if (Integer.parseInt(move.get(1)) == DROP) //if move is a drop in
		{
			b.dropADiscFromTop(Integer.parseInt(move.get(0)), opponentNumber);
		}
		else //move is a pop out
		{
			b.removeADiscFromBottom(Integer.parseInt(move.get(0)));
		}
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
			for (ArrayList<String> move : this.getValidMoves(board, this.playerNumber)) //for each valid move we can take
			{
				double val = minimax(this.bs, depth, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 1);
				if (val > this.currentBestMoveValue)
				{
					this.currentBestMove = (List<String>) move.clone(); //clone is used to make the item persist after the for loop terminates. Idk if this is necessary or not.
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
				move.add(0, Integer.toString(j));
				move.add(1, Integer.toString(this.DROP));
				output.add(move);
			}
			//pop out
			if (canPopOut(playerNum) && b.canRemoveADiscFromBottom(j, playerNum))
			{
				ArrayList<String> move = new ArrayList<String>(2);
				move.add(0, Integer.toString(j));
				move.add(1, Integer.toString(this.POPOUT));
				output.add(move);
			}
		}
		return output;
	}
	
	/*minimax with alpha beta pruning*/
	public double minimax(BoardStateNode node, int depth, double beta, double alpha, int isMax)
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
			return isMax * eval(node.b);
		}
		double bestValue = Double.NEGATIVE_INFINITY;
		for (List<String> move: this.getValidMoves(node.b, isMax == 1 ? this.playerNumber : this.opponentNumber))
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
			 
			double val = -minimax(newChild, depth - 1, -1 * beta, -1 * alpha, -1 * isMax);
			if (bestValue < val)
			{
				bestValue = val;
			}
			if (alpha < val)
			{
				alpha = val;
			}
			if (alpha >= beta)
			{
				break;
			}
		}
		return bestValue;
	}
	
	/* should be ready to return a move at any moment.*/
	//this needs to be implemented differently; to allow for time limiting, and also to not waste time updating our own board before the move is sent out.
	public static void makeNextMove()
	{
		writer.println("Getting ready to make a move!!");
		writer.println(String.join(" ", currentBestMove));
		System.out.println(String.join(" ", currentBestMove));
		System.out.flush();
		applyMove(currentBestMove, board, playerNumber);
		if (!weHaveUsedPopOut && currentBestMove.get(0).equals(Integer.toString(POPOUT))) weHaveUsedPopOut = true;
		currentBestMoveValue = Double.NEGATIVE_INFINITY;
		writer.println(board.toString());
		writer.flush();
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
		int eva = 0;
		for(int i=0;i<b.numOfDiscsInColumn[i];i++)
		{
			for(int j=0;j<b.width;j++)
			{
				eva += evalpos(j,i,b);//collect utility of all discs on the board
			}
		}
		
		return eva;
	}
	
	public int evalpos(int x, int y, Board b)
	{
		int eva = 0;
		eva = evalverti(x,y,playerNumber,b,true)+evalhori(x,y,playerNumber,b,true)+evaldiag(x,y,playerNumber,b,true)+evaldiag2(x,y,playerNumber,b,true);
		return eva;
	}
	int evalverti(int x, int y,int player, Board b,Boolean opp)
	{
		int eva = 0;
		int samedisc = 0;
		int up = 1;
		while(y+up < b.height && (b.board[y+up][x]==player || b.board[y+up][x]==b.emptyCell) && up < b.N){//prob mistake here
			if(b.board[y+up][x]==player)
			{
				samedisc++;
			}/*else if(b.board[y+up][x]!=player && b.board[y+up][x]==b.emptyCell && opp){//reward block opponent
				eva += evalverti(x,y+up,opponentNumber,b,false);
			}*/
			up++;
		}
		up--;//up(empty cell) is actually one less
		int down = 1;
		while(y-down >= 0 && (b.board[y-down][x]==player && b.board[y-down][x]==b.emptyCell) && up < b.N){//prob mistake here
			if(b.board[y-down][x]==player)
			{
				samedisc++;
			}/*else if(b.board[y+up][x]!=player && b.board[y+up][x]==b.emptyCell && opp){//reward block opponent
				eva += evalverti(x,y-down,opponentNumber,b,false);
			}*/
			down++;
		}
		down--;
		if((up + down) < (b.N - 1)){
			return eva;
		}
		eva = up + down - 3 + samedisc;
		return eva;
	}
	
	int evalhori(int x, int y,int player,Board b,Boolean opp)
	{
		int eva = 0;
		int samedisc = 0;
		int left = 1;
		while(x-left >= 0 && (b.board[y][x-left]==player || b.board[y][x-left]==b.emptyCell) && left < b.N)
		{//prob mistake here
			if(b.board[y][x-left]==player)
			{
				samedisc++;
				
			}/*else if(b.board[y][x-left]!=player && b.board[y][x-left]==b.emptyCell && opp){//reward block opponent
				eva += evalhori(x-left,y,opponentNumber,b,false);
			}*/
			left++;
		}
		left--;
		int right = 1;
		while(x+right < b.width && (b.board[y][x+right]==player || b.board[y][x+right]==b.emptyCell) && right < b.N)
		{//prob mistake here
			if(b.board[y][x+right]==player)
			{
				samedisc++;
			}/*else if(b.board[y][x-left]!=player && b.board[y][x-left]==b.emptyCell && opp){//reward block opponent
				eva += evalhori(x+right,y,opponentNumber,b,false);
			}*/
			right++;
		}
		right--;
		if((left + right) < (b.N - 1))
		{
			return eva;
		}
		eva = left + right - 3 + samedisc;
		return eva;
		
	}
	int evaldiag(int x, int y,int player,Board b,Boolean opp)
	{
		int eva = 0;
		int samedisc = 0;
		int diagl = 1;
		while((x-diagl >= 0 && y+diagl<b.height) && (b.board[y+diagl][x-diagl]==player || b.board[y+diagl][x-diagl]==b.emptyCell) && diagl < b.N)
		{//prob mistake here
			if(b.board[y+diagl][x-diagl]==player)
			{
				samedisc++;
			}/*else if(b.board[y+diagl][x-diagl]!=player && b.board[y+diagl][x-diagl]==b.emptyCell && opp){//reward block opponent
				eva += evalhori(x-diagl,y+diagl,opponentNumber,b,false);
			}*/
			
			diagl++;
		}
		diagl--;
		int diagr = 1;
		while((x+diagr < b.width  && y-diagr>=0) && (b.board[y-diagr][x+diagr]==player || b.board[y-diagr][x+diagr]==b.emptyCell) && diagr < b.N)
		{//prob mistake here
			if(b.board[y-diagr][x+diagr]==player)
			{
				samedisc++;
			}/*else if(b.board[y-diagr][x+diagr]!=player && b.board[y-diagr][x+diagr]==b.emptyCell && opp){//reward block opponent
				eva += evalhori(x+diagr,y-diagr,opponentNumber,b,false);
			}*/
			diagr++;
		}
		diagr--;
		if((diagl + diagr) < b.N-1)
		{
			return eva;
		}
		eva = diagl + diagr - 3 + samedisc;	
		return eva;		
		
	}
	int evaldiag2(int x, int y,int player,Board b,Boolean opp)
	{
		int eva = 0;
		int samedisc = 0;
		int diagl = 1;
		while((x-diagl >= 0 && y-diagl>=0) && (b.board[y-diagl][x-diagl]==player || b.board[y-diagl][x-diagl]==b.emptyCell) && diagl < b.N){//prob mistake here
			if(b.board[y-diagl][x-diagl]==player)
			{
				samedisc++;
			}/*else if(b.board[y-diagl][x-diagl]!=player && b.board[y-diagl][x-diagl]==b.emptyCell && opp){//reward block opponent
				eva += evalhori(x-diagl,y-diagl,opponentNumber,b,false);
			}*/
			diagl++;
		}
		diagl--;
		int diagr = 1;
		while((x+diagr < b.width  && y+diagl<b.height) && (b.board[y+diagr][x+diagr]==player || b.board[y+diagr][x+diagr]==b.emptyCell) && diagr < b.N){//prob mistake here
			if(b.board[y+diagr][x+diagr]==player)
			{
				samedisc++;
			}/*else if(b.board[y+diagl][x+diagl]!=player && b.board[y-diagl][x-diagl]==b.emptyCell && opp)
			{//reward block opponent
				eva += evalhori(x+diagl,y+diagl,opponentNumber,b,false);
			}*/
			diagr++;
		}
		diagr--;
		if((diagl + diagr) < b.N-1){
			return eva;
		}
		eva = diagl + diagr - 3 + samedisc;	
		return eva;	
	}
	
	//this whole thing reads the input and updates info in our player appropriately. 
	public void processInput() throws IOException
	{	
	
    	String s=input.readLine();	
		List<String> ls=Arrays.asList(s.split(" "));
		if(ls.size()==2) //indicates that opponent made a move
		{
			new java.util.Timer().schedule( 
			        new java.util.TimerTask() {
			            @Override
			            public void run() {
			            	FCJSPlayer.makeNextMove();
			            }
			        }, 
			        (int)(this.timeLimit * 1000 - (this.timeBuffer * 1000))
			);
			this.turnStart = System.nanoTime();
			if (!this.theyHaveUsedPopOut && ls.get(0).equals(Integer.toString(this.POPOUT))) this.theyHaveUsedPopOut = true;
			applyMove(ls, board, this.opponentNumber);
			pruneAfterMove(ls);
			search(); //should terminate before time runs out, but it's ok if it goes a little over.
			
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
