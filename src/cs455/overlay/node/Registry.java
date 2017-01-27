package cs455.overlay.node;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Registry {

    public static void main(String[] args){
        BlockingQueue<String> queue = new ArrayBlockingQueue<>(10);
        new RegistryServerCommands(queue).start();
        new RegistryServer(Integer.parseInt(args[0]), queue).start();
    }
}

class RegistryServer extends Thread{

    ServerSocket serverSocket;
    Socket socket;
    private int port;
    private BlockingQueue<String> queue;

    RegistryServer(int port, BlockingQueue<String> queue){
        this.port = port;
        this.queue = queue;
    }

    //TODO: Create data structure for nodes.
    //TODO: Actually register the nodes and do the checking.
    public void registerNode(String message){
        System.out.println(message);
        try {
            queue.put(message);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println("response");
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    //TODO: All of this method.
    public void deregisterNode(){}


    @Override
    public void run(){
        try{
            serverSocket = new ServerSocket(port);

            while(true){

                socket = serverSocket.accept();
                BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String requestLine = input.readLine();

                if(requestLine.contains("Message Type (int): REGISTER_REQUEST")){
                    String request = requestLine + "\n" + input.readLine() + "\n" + input.readLine();
                    registerNode(request);
                }

                else if(requestLine.contains("Message Type (int): DEREGISTER_REQUEST")){
                    deregisterNode();
                }
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
}

//TODO: GET ACTUAL COMMANDS WORKING.
class RegistryServerCommands extends Thread{

    private BlockingQueue<String> queue;

    RegistryServerCommands(BlockingQueue<String> queue) {
        this.queue = queue;
    }


    @Override
    public void run(){

        String command;

        do {
            Scanner cmdScanner = new Scanner(System.in);
            command = cmdScanner.nextLine();
            try {

                if (command.equals("list-messaging nodes")) {

                    while(!queue.isEmpty())
                        System.out.println("I GOT IT " + queue.take());

                } else if (command.contains("list-weights")) {

                } else if (command.contains("setup-overlay")) {

                } else if (command.contains("send-overlay-link-weights")) {

                } else if (command.contains("start")) {
                    int numberOfRounds = Integer.parseInt(command.substring(command.indexOf(" ")));
                }
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }while(!command.equals("exit"));
    }
}
