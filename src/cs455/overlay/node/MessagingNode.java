package cs455.overlay.node;

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

public class MessagingNode {

    String hostName,ipAddress;
    int port;
    List<MessagingNode> connectedNodes;

    public MessagingNode(String hostName, String ipAddress, int port, ArrayList<MessagingNode> connectedNodes){
        this.hostName = hostName;
        this.ipAddress = ipAddress;
        this.port = port;
        this.connectedNodes = connectedNodes;
    }



    public static void main (String[] args){

        BlockingQueue<String> queue = new ArrayBlockingQueue<>(10);
        new MessagingNodeServer(args[0],Integer.parseInt(args[1]),queue).start();
        new MessagingNodeCommands(queue).start();
    }
}

class MessagingNodeServer extends Thread{

    ServerSocket serverSocket;
    Socket socket;
    private BlockingQueue<String> queue;

    MessagingNodeServer(String registryAddress, int registryPort, BlockingQueue<String> queue){

        this.queue = queue;
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            String ipAddress = inetAddress.getHostAddress();
            String hostName = inetAddress.getCanonicalHostName();
            this.serverSocket = new ServerSocket(0);
            int port = serverSocket.getLocalPort();

            //Tells registry ready to register.
            this.socket = new Socket(registryAddress, registryPort);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            RegisterRequest.send(out);

            //receives message back
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String text = input.readLine();
            System.out.println(text);
        }
        catch(Exception e){
            e.printStackTrace();
        }
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


