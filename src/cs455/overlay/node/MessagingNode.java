package cs455.overlay.node;

import cs455.overlay.wireformats.DeregisterRequest;
import cs455.overlay.wireformats.RegisterRequest;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static cs455.overlay.wireformats.WireFormatConstants.REGISTER_RESPONSE;

public class MessagingNode {

    String hostName,ipAddress;
    int port;
    List<MessagingNode> connectedNodes;

    public MessagingNode(String ipAddress, int port){
        this.ipAddress = ipAddress;
        this.port = port;
    }

    public MessagingNode(String hostName, String ipAddress, int port, ArrayList<MessagingNode> connectedNodes){
        this.hostName = hostName;
        this.ipAddress = ipAddress;
        this.port = port;
        this.connectedNodes = connectedNodes;
    }


    public static void main (String[] args){

        ServerSocket serverSocket;
        Socket socket;

        BlockingQueue<String> queue = new ArrayBlockingQueue<>(10);
        new MessagingNodeCommands(queue).start();
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            String ipAddress = inetAddress.getHostAddress();
            String hostName = inetAddress.getCanonicalHostName();
            serverSocket = new ServerSocket(0);
            int port = serverSocket.getLocalPort();

            //Tells registry ready to register.
            socket = new Socket(args[0], Integer.parseInt(args[1]));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            RegisterRequest registerRequest = new RegisterRequest(ipAddress, hostName, port);
            registerRequest.send(out);

            //Receives response message from the registry.
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            if( Integer.parseInt(input.readLine()) == REGISTER_RESPONSE ) {
                if( Integer.parseInt(input.readLine()) == 1 ) {
                    String responseText = input.readLine();
                    System.out.println(responseText);

                    Socket socket2 = serverSocket.accept();

                }
                else {
                    System.out.println("failed registration");
                }
            }








            //At the end, deregister.
            DeregisterRequest deregisterRequest = new DeregisterRequest(ipAddress, hostName, port);
            deregisterRequest.send(out);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
}

class MessagingNodeConnection extends Thread{

    ServerSocket serverSocket;
    Socket socket;
    private BlockingQueue<String> queue;

    MessagingNodeConnection(String registryAddress, int registryPort, BlockingQueue<String> queue){

        this.queue = queue;

    }
}

class MessagingNodeCommands extends Thread{
    private BlockingQueue<String> queue;

    MessagingNodeCommands(BlockingQueue<String> queue) {
        this.queue = queue;
    }


    @Override
    public void run(){

        String command;

        do {
            Scanner cmdScanner = new Scanner(System.in);
            command = cmdScanner.next();
            System.out.println(command);

        }while(!command.equals("exit"));
    }
}


