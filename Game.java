
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
		private int numberOfPoinst = 2;
		int number;
		
 		public Gamer(SocketThread st, String name, int number){
			this.st = st;
			this.name = name;
			this.number = number;
		}
 		
 		public List<Integer> myCards(){
 			return cards.subList(number*10, (number+1)*10);
 		}
		
	}
	
    private String name;
    private String who;
    
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
    		arr.add(li.get(x));
    	}
    	return arr;
    }
    
    private JSONArray extraCards(){
    	JSONArray arr = new JSONArray();
    	arr.add(cards.get(30));
    	arr.add(cards.get(31));
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
    	
    	return json.toJSONString();
    }
    
    public void addNewGamer(SocketThread st, String name){
    	System.out.println("Do gry " + this.name + " do³¹czy³ siê " + name );
        synchronized(gamers){
        	Gamer g = new Gamer(st, name, gamers.size());
            gamers.add(g);
        }
        if(gamers.size() == 3){
        	broadcast(jsonRedy());
        	return;
        }
        broadcast(jsonWaitingForGamer(gamers.size()+1));
    }
    
    private String unPack(String json, String key){
    	try{
    		JSONParser jsonParser = new JSONParser();
    		//System.out.println("Próba prasowania "+json+" z kruczem "+key);
            JSONObject jsonObject = (JSONObject) jsonParser.parse(json);
            String command = (String) jsonObject.get(key);
    		return command;
    	} catch (ParseException e) {
            System.err.println("Blad PARSOWANIA!" + e.getMessage());
        }
    	return "error";
    }
    
    private void initialInfo(){
        System.out.println("Gracz " + who + " stworzy³‚ gre o nazwie " + name);
    }
    
    public Game(String name, String who){
        this.name = name;
        this.who = who;
        initialInfo();
        shuffling();
    }
    
    void listen(Gamer g){
    	//System.out.println("GET() w listen");
    	String m = g.st.bm.get();
    	String command = unPack(m, "command");
    	//System.out.println("command = " + command);
    	if(command.equals("getPlayersAndCards")){
    		System.out.println("Wysy³am jednemu "+ jsonGetGameState(g));
    		g.st.send(jsonGetGameState(g));
    	}
    }
    
    void broadcast(String s){
    	System.out.println("Wysy³am wszystkim " + s);
    	synchronized(gamers){
            Iterator it = gamers.iterator();

            while(it.hasNext()){
                Gamer element = (Gamer) it.next();
                element.st.send(s);
            	//System.out.println(element.st.toString());
            }
        }
    }
    
    @Override
    public void run(){
        while(true){
            synchronized(gamers){
	            Iterator it = gamers.iterator();
	
	            while(it.hasNext()){
	                Gamer element = (Gamer) it.next();
	                //System.out.println(element.st.bm.isNew());
	                if(element.st.bm.isNew()){
	                        listen(element);
	                }
	            }
            }
            Thread.yield();
        }
    }
}
