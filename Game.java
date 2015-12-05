
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Game extends Thread{
	
	class Gamer{
		public String name;
		public SocketThread st;
		public int numberOfPoinst = 2;
		int number;
		int[] firstCards = {0, 0}; 
		int numberOfVisible = 2;
		
 		public Gamer(int number){
			this.number = number;
		}
 		
 		public List<Integer> myCards(){
 			//System.out.println("W myCards,cards.size() = " + cards.size());
 			return cards.subList(number*10, (number+1)*10);
 		}
 		
 		public int randomFirstCards(){
 			//System.out.println("Rozpoczynam proces szukania kart do usuniecia");
 			Random r = new Random();
 			List<Integer> card = new LinkedList<Integer>(myCards());
 			
 			Collections.sort(card);
 			
 			//System.out.println(card);
 			
 			if(firstCards[0] != 0){
 				int it = card.indexOf(firstCards[0]);
 				card.set(it+1, -1);
 				card.set(it, -1);
 				card.set(it-1, -1);
 			}
 			
 			//System.out.println(card);
 			
 			card.set(0, -1);
 			card.set(card.size()-1, -1);
 			
 			//System.out.println(card);
 			
 			for(int x = 0; x < 4; x++){
 				if(colorFirstCards[x] < 2) continue;
 				Iterator it = card.iterator();
 				for(int y = 0; y < card.size(); y++){
 					if(card.get(y) == -1) continue;
 					if(card.get(y)%4 == x) card.set(y, -1);
 				}
 			}
 			
 			//System.out.println(card);
 			
 			Iterator it = card.iterator();
 			while(it.hasNext()){
 				if((int)it.next() == -1) it.remove();
 			}
 			
 			//System.out.println(card);
 			
 			int findCard = r.nextInt(card.size());
 			if(firstCards[0] == 0) firstCards[0] = card.get(findCard);
 			else firstCards[1] = card.get(findCard);
 			colorFirstCards[card.get(findCard)%4]++;
 			return card.get(findCard);
 		}
		
	}
	
    private String name;
    private String who;
    
    private int[] colorFirstCards = {0, 0, 0, 0};
    private List<Integer> firstCards = new LinkedList<Integer>();
    private int ready = 0;
    private int countGamers = 0;
    
    private int whoseTourn = 0;
    private boolean win = false;
    
    private List<Gamer> gamers = new LinkedList<Gamer>();
    
    private List<Integer> cards = new LinkedList<Integer>();
    
    private void shuffling(){
    	List<Integer> cardsSort = new LinkedList<Integer>();
    	for(int x=0;x<32;x++){
    		cardsSort.add(x+1);
    	}
    	for(int x=0;x<32;x++){
    		Random r = new Random(); 
    		int ir = r.nextInt(32-x);
    		cards.add(cardsSort.get(ir));
    		cardsSort.remove(ir);
    	}
    	System.out.println(cards.toString());
    }
    
    private String jsonWaitingForGamer(int number){
    	JSONObject json = new JSONObject();
    	
    	json.put("type", "preparingGame");
    	if(number == 2) json.put("message", "Oczekiwanie_na_gracza_drugiego");
    	if(number == 3) json.put("message", "Oczekiwanie_na_gracza_trzeciego");
    	
    	return json.toJSONString();
    }
    
    private String jsonRedy(){
    	JSONObject json = new JSONObject();
    	
    	json.put("type", "gameReady");
    	
    	return json.toJSONString();
    }
    
    private JSONArray cardsGamer(int g){
    	JSONArray arr = new JSONArray();
    	List<Integer> li = gamers.get(g).myCards();
    	Collections.sort(li);
    	for(int x=0;x<10;x++){
    		arr.add(li.get(x).toString());
    	}
    	return arr;
    }
    
    private JSONArray extraCards(){
    	JSONArray arr = new JSONArray();
    	arr.add(cards.get(30).toString());
    	arr.add(cards.get(31).toString());
    	return arr;
    }
    
    private JSONArray getFirstCards(){
    	JSONArray arr = new JSONArray();
    	for(int x =0;x<6;x++){
    		arr.add(firstCards.get(x).toString());
    	}
    	return arr;
    }
    
    private String jsonGetGameState(Gamer g){
    	JSONObject json = new JSONObject();
    	
    	json.put("type", "createGame");
    	json.put("player1Name", gamers.get(0).name);
    	json.put("player2Name", gamers.get(1).name);
    	json.put("player3Name", gamers.get(2).name);
    	json.put("yourName", g.name);
    	json.put("player1Cards", cardsGamer(0));
    	json.put("player2Cards", cardsGamer(1));
    	json.put("player3Cards", cardsGamer(2));
    	json.put("extraCards", extraCards());
    	json.put("visibleCards", getFirstCards());
    	
    	return json.toJSONString();
    }
    
    private String jsonWhoseTurn(int who){
    	JSONObject json = new JSONObject();
    	
    	json.put("type", "nextRound");
    	json.put("name", gamers.get(who).name);
    	
    	return json.toJSONString();
    }
    
    
    
    public void addNewGamer(SocketThread st, String name){
    	System.out.println("Do gry " + this.name + " doï¿½ï¿½czyï¿½ siï¿½ " + name );
        synchronized(gamers){
        	Gamer g = gamers.get(countGamers);
        	g.st = st;
        	g.name = name;
            //gamers.add(countGamers, g);
        	countGamers++;
        }
        if(countGamers == 3){
        	broadcast(jsonRedy());
        	return;
        }
        broadcast(jsonWaitingForGamer(countGamers+1));
    }
    
    private String unPack(String json, String key){
    	try{
    		JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = (JSONObject) jsonParser.parse(json);
            String command = (String) jsonObject.get(key);
    		return command;
    	} catch (ParseException e) {
            System.err.println("Blad PARSOWANIA!" + e.getMessage());
        }
    	return "error";
    }
    
    private void initialInfo(){
        System.out.println("Gracz " + who + " stworzyï¿½ï¿½ gre o nazwie " + name);
    }
    
    public Game(String name, String who){
        this.name = name;
        this.who = who;
        initialInfo();
        shuffling();
        for(int x= 0;x<3;x++){
        	gamers.add(new Gamer(x));
        }
        for(int x= 0;x<6;x++){
        	firstCards.add(gamers.get(x%3).randomFirstCards());
        }
    }
    
    int whichThisCardIs(String who, int card){
    	int whoNum  = 0;
    	for(;whoNum<3;whoNum++){
    		String n= gamers.get(whoNum).name;
    		if(n.equals(who)) break;
    	}
    	List<Integer> hisCards = gamers.get(whoNum).myCards();
    	Collections.sort(hisCards);
    	int odp = 0;
    	for(;odp<10;odp++){
    		if(hisCards.get(odp) == card) break;
    	}
    	return odp+1;
    }
    
    void feedback(String s){
    	JSONObject json = new JSONObject();
    	
    	json.put("type", "serverMessage");
    	json.put("message", s);
    	broadcast(json.toJSONString());
    }
    
    void getHimPoints(String who, int p ){
    	int whoNum  = 0;
    	for(;whoNum<3;whoNum++){
    		String n= gamers.get(whoNum).name;
    		if(n.equals(who)) break;
    	}
    	gamers.get(whoNum).numberOfPoinst += p;
    }
    
    int whoIs(String name){
    	int whoNum  = 0;
    	for(;whoNum<3;whoNum++){
    		String n= gamers.get(whoNum).name;
    		if(n.equals(who)) break;
    	}
    	return whoNum;
    }
    
    void guess(String m){
    	try{
			 String ownerGuessedCard = unPack(m, "ownerGuessedCard");
			 int[] playerTriedGuess = {-1,-1};		
			 JSONParser jsonParser = new JSONParser();
			 JSONObject jsonObject = (JSONObject) jsonParser.parse(m);
			 JSONArray arr = (JSONArray) jsonObject.get("playerTriedGuess");
			 playerTriedGuess[0] = Integer.parseInt(arr.get(0).toString());
			 if(arr.get(1) != null)
				 playerTriedGuess[1] = Integer.parseInt(arr.get(1).toString());
			 //System.out.println(playerTriedGuess[0] + "  --  " + playerTriedGuess[1]);
			 Integer cardName = Integer.parseInt(unPack(m, "cardName"));
			 String myName = unPack(m, "myName");
			 String state = unPack(m, "state");
			 int cardOlder = whichThisCardIs(ownerGuessedCard, cardName);
			 int numberOfVisible = gamers.get(whoIs(ownerGuessedCard)).numberOfVisible;
			 System.out.println("numberOfVisible " + numberOfVisible);
			 int pointsForYou = 3;
			 if(numberOfVisible == 9) pointsForYou = 2;
			 if(numberOfVisible == 10) pointsForYou = 1;
			 Boolean isVisible = false;
			 if(state.equals("ONE_TRY_FAIL_ANS")){
				 pointsForYou = 0;
				 isVisible = false;
				 feedback("Gracz_" + myName + "_Ÿle_typowa³_karte_" + cardOlder + "_gracza_" +  ownerGuessedCard + "_jako_karte_" + playerTriedGuess[0]);
			}
			if(state.equals("TWO_TRY_FAIL_ANS")){
				if(playerTriedGuess[1] == -1){
					whoseTourn += 4;
		    		whoseTourn %= 3;
		    		return;
				}
				pointsForYou = -1;
				isVisible = false;
				feedback("Gracz_" + myName + "_Ÿle_typowa³_karte_" + cardOlder + "_gracza_" +  ownerGuessedCard + "_jako_karty_" + playerTriedGuess[0] + "_i_" +playerTriedGuess[1]);
			}
			if(state.equals("ONE_TRY_GOOD_ANS")){
				gamers.get(whoIs(ownerGuessedCard)).numberOfVisible++;
				isVisible = true;
				feedback("Gracz_" + myName + "_poprawnie_zgad³_karte_" + cardOlder + "_gracza_" +  ownerGuessedCard + "_która_mia³a_wartoœæ_" + playerTriedGuess[0]);
			}
			if(state.equals("TWO_TRY_GOOD_ANS")){
				gamers.get(whoIs(ownerGuessedCard)).numberOfVisible++;
				pointsForYou -= 1;
				isVisible = true;
				feedback("Gracz_" + myName + "_Ÿle_typowa³_karte_" + cardOlder + "_gracza_" +  ownerGuessedCard + "_jako_karte_" + playerTriedGuess[0] + "_ale_poprawi³_odpowiedz_na_" + playerTriedGuess[1]);
			}
			getHimPoints(myName, pointsForYou);
			JSONObject json = new JSONObject();
			json.put("type", "cardGuess");
			json.put("WhoTryGuess", myName);
			json.put("isVisible", isVisible);
			Integer i = pointsForYou;
            json.put("addPointForPlayer", i.toString());
            json.put("cardName", cardName.toString());
            broadcast(json.toJSONString());
            
    	}catch(ParseException pe){
    		System.err.println("Blad PARSOWANIA!" + pe.getMessage());
        }
    	
    }
    
    String whoWin(int who){
    	JSONObject json = new JSONObject();
    	
    	json.put("type", "gameOver");
    	json.put("whoWin", gamers.get(who).name);
    	return json.toJSONString();
    }
    
    void ifEnd(){
    	int max = gamers.get(0).numberOfPoinst;
    	int who = 0;
    	for(int x= 1;x<3;x++){
			if(gamers.get(x).numberOfPoinst > max){
				max = gamers.get(x).numberOfPoinst;
				who = x;
			}
		}
    	if(whoseTourn == 0){
    		System.out.println("ifEnd " + max);
    		if(max >= 15){
    			broadcast(whoWin(who));
    			win = true;
    		}
    	}
    }
    
    void listen(Gamer g){
    	//System.out.println("GET() w listen");
    	String m = g.st.bm.get();
    	String command = unPack(m, "command");
    	//System.out.println("command = " + command);
    	if(command.equals("getPlayersAndCards")){
    		ready++;
    		System.out.println("Wysyï¿½am jednemu "+ jsonGetGameState(g));
    		g.st.send(jsonGetGameState(g));
    		if(ready == 3){
    			broadcast(jsonWhoseTurn(whoseTourn));
    		}
    	}
    	if(command.equals("guess")){
    		guess(m);
    		whoseTourn++;
    		whoseTourn %= 3;
    		broadcast(jsonWhoseTurn(whoseTourn));
    	}
    	ifEnd();
    }
    
    private String jsonFirstCard(int who, int card){
    	JSONObject json = new JSONObject();
    	
    	Integer i = card;
    	
    	json.put("type", "cardGuess");
    	json.put("WhotTryGuess", gamers.get(who).name);
    	json.put("isVisible", true);
    	json.put("addPointForPlayer", "0");
    	json.put("cardName", i.toString());
    	
    	return json.toJSONString();
    }
    
    void broadcast(String s){
    	System.out.println("Wysyï¿½am wszystkim " + s);
    	synchronized(gamers){


            
            for(int x=0;x<countGamers;x++){
            	gamers.get(x).st.send(s);
            }
        }
    }
    
    @Override
    public void run(){
        while(!win){
            synchronized(gamers){
	
	  
	            for(int x=0;x<countGamers;x++){
	            	Gamer element = gamers.get(x);
	            	if(element.st.bm.isNew()){
                        listen(element);
	            	}
	            }
	            
            }
            Thread.yield();
        }
    }
}