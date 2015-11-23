
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class NewThreadCointerner extends Thread{
    private List threads = new LinkedList();
    private Map<String, Game> games = new TreeMap();
    
    public void addNewThread(Thread t){
        synchronized(threads){   
            threads.add(t);
        }
        //System.out.println("Dodano nowy watek do NTC");
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
    
    private void setThread(Iterator it, SocketThread st) {
    	//System.out.println("bm.get() w NTC ");
    	String m = st.bm.get();
        switch (unPack(m, "command")) {
            case "createNewGame":
                Game newGame = new Game(unPack(m, "gameName"),unPack(m,"whoDoIt"));
                games.put(unPack(m, "gameName"), newGame);
                newGame.start();
                break;
            case "joinToGame":
                if(games.containsKey(unPack(m,"gameName"))){
                    Game g = games.get(unPack(m,"gameName"));
                    g.addNewGamer(st, unPack(m,"myNick"));
                    it.remove();
                }
                break;
        }
    }
    
    @Override
    public void run(){
        while(true){
        	//System.out.print(".");
            synchronized(threads){
	            Iterator it = threads.iterator();
	
	            while(it.hasNext()){
	                SocketThread element = (SocketThread) it.next();
	                if(element.bm.isNew()){
	                        setThread(it, element);
	                }
	            }
            }
            Thread.yield();
        }
    }
}
