package src;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client implements Runnable {
    private Socket client;
    private BufferedReader in;
    private PrintWriter out;
    private boolean done;


    @Override
    public void run() {
        try {
            client = new Socket("127.0.0.1", 5555);
            out = new PrintWriter(client.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            InputHandler handler = new InputHandler();
            Thread t = new Thread(handler);
            t.start();
            String inMessage;
            while((inMessage = in.readLine()) != null){
                System.out.println(inMessage);
            }
        } catch (IOException e) {
            shutdown();
//            throw new RuntimeException(e);
        }
    }

    public void shutdown() {
        done = true;
        try{
            if(out != null){
                out.close();
            }
            if(in != null){
                in.close();
            }
            if(!client.isClosed()){
                client.close();
            }
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }
    class InputHandler implements Runnable{
        @Override
        public void run() {
            try {
                BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));
                String msg;
                while(!done) {
                    msg = inputReader.readLine();
                    if (msg.startsWith("/quit")) {
                        inputReader.close();
                        shutdown();
                    }
                    else{
                        out.println(msg);
                    }
                }
            } catch (IOException e) {
                shutdown();
            }
        }
    }
    public static void main(String args[]){
        Client client = new Client();
        client.run();
    }
}
