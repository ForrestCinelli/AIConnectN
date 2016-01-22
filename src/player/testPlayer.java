package player;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;


public class testPlayer {

	String[] playerNames={"aa", "bb", "cc", "dd", "ee", "ff", "gg", "hh", "ii"};
	String playerName = playerNames[new Random().nextInt(playerNames.length)];
	BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
	boolean first_move=false;
	
	public void processInput() throws IOException{	
	
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
			System.out.println("0 1");  //first move
		}
		else if(ls.size()==4){		//player1: aa player2: bb
			//TODO combine this information with game information to decide who is the first player
		}
		else
			System.out.println("not what I want");
	}
	
	public static void main(String[] args) throws IOException {
		testPlayer rp=new testPlayer();
		System.out.println(rp.playerName);
//		while (true){
//			rp.processInput();
//		}

	}

}
