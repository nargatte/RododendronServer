import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

public class Main {
    static NewThreadCointerner newThreads;
    
    static private void makeNewThreadCointerner(){
        newThreads = new NewThreadCointerner();
        newThreads.start();
    }
    
    public static void main(String[] args){
        makeNewThreadCointerner();
        
        int port = 6969;
        if(args.length == 2) port = Integer.parseInt(args[1]);
        
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Start serwera!");
            
            while (true) {
                try {
                    Socket comming = serverSocket.accept();
                    Thread t = new SocketThread(comming);
                    t.start();
                    newThreads.addNewThread(t);
                } catch (IOException e) {
                    System.err.println("Blad wejscai/wyjscia");
                }

            }
        } catch (IOException e) {
            System.err.println("Blad przy tworzeniu ServerSocket!");
        }
    }
}
