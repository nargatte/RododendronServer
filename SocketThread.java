import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SocketThread extends Thread {
    private Socket socket;
    private Scanner in;
    private PrintWriter out;
    
    class BufforMessage{
    	private Boolean newMessage = false;
        private String message;
        
        public synchronized void put(String m){
        	while(newMessage){
        		try{
        			wait();
        		}catch (InterruptedException e) { } 
        	}
        	message = m;
        	//System.out.println("newMessage w " + this.toString() + " ma wartosc true");
    		newMessage = true;
        	notifyAll(); 
        }
        
        public synchronized Boolean isNew(){
        	return newMessage;
        }
        
        public synchronized String get(){
        	while(!newMessage){
        		try{
        			wait();
        		}catch (InterruptedException e) { } 
        	}
        	//System.out.println("NM " + this.toString() + " false");
    		newMessage = false;
        	notifyAll(); 
        	return message;
        }
    }
    
    public BufforMessage bm = new BufforMessage();
    
    private void InitialInfo(){
        System.out.println("Nawi¹zano nowe Po³¹czenie");
    } 

    public SocketThread(Socket socket) {
        this.socket = socket;
        try {
            this.in = new Scanner(socket.getInputStream());
        } catch (IOException e) {
            System.err.println("Nie mozna pobrac strumienia wejsciowego!");
            throw new AssertionError();
        }
        try {
            this.out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            System.err.println("Nie mozna pobrac strumienia wyjsciowego!");
            throw new AssertionError();
        }
        InitialInfo();
    }
    
    @Override
    public void run(){
    	//System.out.println("Przed hasNext");
        while(in.hasNext()){
        	String m = in.next();
        	System.out.println("po hasNext " + bm.isNew().toString() + m);
        	bm.put(m);
        }
    }
    
    public void send(String str){
        out.println(str);
    }
    
}
