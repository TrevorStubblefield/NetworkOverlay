package cs455.overlay.node;

import cs455.overlay.wireformats.RegisterResponse;

import static cs455.overlay.wireformats.WireFormatConstants.DEREGISTER_REQUEST;
import static cs455.overlay.wireformats.WireFormatConstants.REGISTER_REQUEST;
import static cs455.overlay.wireformats.WireFormatConstants.SETUP_OVERLAY;

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
        BlockingQueue<MessagingNode> queue = new ArrayBlockingQueue<>(10);
        new RegistryServerCommands(queue).start();
        new RegistryServer(Integer.parseInt(args[0]), queue).start();
    }
}

class RegistryServer extends Thread{

    ServerSocket serverSocket;
    Socket socket;
    private int port;
    private BlockingQueue<MessagingNode> queue;

    RegistryServer(int port, BlockingQueue<MessagingNode> queue){
        this.port = port;
        this.queue = queue;
    }

    //TODO: Create data structure for nodes.
    //TODO: Actually register the nodes and do the checking.
    public void registerNode(String message){
        try {
            String[] messageSplit = message.split("\n");

            MessagingNode messagingNode = new MessagingNode(messageSplit[0],Integer.parseInt(messageSplit[1]));

            queue.put(messagingNode);

            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            RegisterResponse registerResponse = new RegisterResponse((byte)1,"Registration request successful. The number of messaging nodes currently constituting the overlay is (1)");
            registerResponse.send(out);
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

                if( Integer.parseInt(requestLine) == REGISTER_REQUEST ){
                    String request = input.readLine() + "\n" + input.readLine();
                    registerNode(request);
                }

                else if( Integer.parseInt(requestLine) == DEREGISTER_REQUEST ){
                    deregisterNode();
                }

                else if( Integer.parseInt(requestLine) == SETUP_OVERLAY ){

                }


                socket.close();
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
}

//TODO: GET ACTUAL COMMANDS WORKING.
class RegistryServerCommands extends Thread{

    private BlockingQueue<MessagingNode> queue;

    RegistryServerCommands(BlockingQueue<MessagingNode> queue) {
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
                    if(!queue.isEmpty()) {
                        MessagingNode[] nodes = queue.toArray(new MessagingNode[queue.size()]);
                        for (MessagingNode node : nodes) {
                            System.out.println(node.ipAddress + ":" + node.port);
                        }
                    }

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
