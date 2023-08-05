package src;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatAppServer implements Runnable {
    private ArrayList<ConnectionHandler> connections;
    private ServerSocket server;
    private boolean done;
    private ExecutorService pool;

    ChatAppServer(){
        connections = new ArrayList<>();
        done = false;
    }

    @Override
    public void run() {
        try {
            server = new ServerSocket(5555);
            pool = Executors.newCachedThreadPool();
            while(!done){
                Socket client = server.accept();
                ConnectionHandler handler = new ConnectionHandler(client);
                connections.add(handler);
                pool.execute(handler);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void broadcast(String message){
        for(ConnectionHandler ch: connections){
            if(ch != null){
                ch.sendMessage(message);
            }
        }
    }

    public void shutDown() throws IOException {
        if(!server.isClosed()){
            try {
                server.close();
                done=true;
                for(ConnectionHandler cn: connections){
                    cn.shutDown();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    class ConnectionHandler implements Runnable{
        private Socket user;
        private BufferedReader in;
        private PrintWriter out;
        private String userName;

        ConnectionHandler(Socket user){
            this.user = user;
        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(user.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(user.getInputStream()));
                out.println("Enter your name: ");
                userName = in.readLine();
                System.out.println(userName + " made a connection!");
                broadcast(userName + " joined the chat!");
                String message;
                while((message = in.readLine()) != null){
                    if(message.startsWith("/user ")){
                        String[] messageSplit = message.split(" ",2);
                        if(messageSplit.length == 2){
                            broadcast("The username " + userName + " is changed to " + messageSplit[1]);
                            userName = messageSplit[1];
                        }
                        else{
                            out.println("Wrong input");
                        }
                    }else if(message.startsWith("/quit ")){
                        broadcast(userName + " left the chat!");
                        shutDown();
                    }else{
                        broadcast(userName + ": " + message);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public void sendMessage(String message){
            out.println(message);
        }

        public void shutDown() throws IOException {
            try {
                out.close();
                in.close();
                if(!user.isClosed()) user.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }
    public static void main(String args[]){
        ChatAppServer chatServer = new ChatAppServer();
        chatServer.run();
    }
}
