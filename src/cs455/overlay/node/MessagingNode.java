package cs455.overlay.node;

import cs455.overlay.wireformats.DeregisterRequest;
import cs455.overlay.wireformats.RegisterRequest;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static cs455.overlay.wireformats.WireFormatConstants.MESSAGING_NODES_LIST;
import static cs455.overlay.wireformats.WireFormatConstants.REGISTER_RESPONSE;

public class MessagingNode {

    //Messaging Node Specific Data
    public String hostName,ipAddress;
    public int port;
    public List<MessagingNode> connectedNodes;
    public int[] connectedWeights;
    DataOutputStream outputStream;

    public MessagingNode(String ipAddress, String hostName, int port, DataOutputStream outputStream){
        this.ipAddress = ipAddress;
        this.hostName = hostName;
        this.port = port;
        this.connectedNodes = new ArrayList<>();
        this.outputStream = outputStream;
    }

    public int numberOfConnections(){
        return connectedNodes.size();
    }

    public int getWeightOfConnection(MessagingNode connectedNode){
        return connectedWeights[connectedNodes.indexOf(connectedNode)];
    }

    public static void main (String[] args){

        ServerSocket serverSocket;
        Socket socket;
        String ipAddress;
        int port;

        DataOutputStream out;

        BlockingQueue<String> queue = new ArrayBlockingQueue<>(10);
        new MessagingNodeCommands(queue).start();


        try {
            System.out.println("Messaging Node Running...");
            InetAddress inetAddress = InetAddress.getLocalHost();
            ipAddress = inetAddress.getHostAddress();
            serverSocket = new ServerSocket(0);
            port = serverSocket.getLocalPort();

            new MessagingNodeListener(queue, serverSocket).start();

            //Tells registry ready to register.
            socket = new Socket(args[0], Integer.parseInt(args[1]));
            out = new DataOutputStream(socket.getOutputStream());
            RegisterRequest registerRequest = new RegisterRequest(ipAddress, port);
            registerRequest.send(out);

            //Receives messages from the registry.
            DataInputStream input = new DataInputStream(socket.getInputStream());
            int messageType;
            do {
                messageType = input.readInt();

                if (messageType == REGISTER_RESPONSE) {
                    if (input.readByte() == 1) {
                        byte[] infoInBytes = new byte[input.available()];
                        int i = 0;
                        while(input.available() > 0){
                            infoInBytes[i] = input.readByte();
                            i++;
                        }
                        String additionalInfo = new String(infoInBytes, "UTF-8");
                        System.out.println(additionalInfo);
                    } else {
                        System.out.println("failed registration");
                        messageType = -1;
                    }
                }

                else if (messageType == MESSAGING_NODES_LIST){
                    int numberOfConnections = input.readInt();
                    Thread.sleep(10);

                    byte[] received = new byte[input.available()];
                    Thread.sleep(10);
                    int j = 0;
                    System.out.println(received.length);

                    while(input.available() > 0){
                        received[j] = input.readByte();
                        j++;
                    }
                    String connectedNodeString = new String(received, "UTF-8");
                    System.out.println(connectedNodeString);
                    String[] messageSplit = connectedNodeString.split(" ");
                    for (String connection : messageSplit){
                        if(!connection.equals(" ") && !connection.isEmpty()) {
                            String[] connectionSplit = connection.split(":");
                            String connectionHostName = connectionSplit[0];
                            String connectionPort = connectionSplit[1];
                        }

                        //if doesnt contain a connection (look at queue)
                        //Socket connectionSocket = new Socket(connectionHostName, Integer.parseInt(connectionPort));
                        //new thread based on connectionSocket.
                    }

                }

            }while(messageType != -1);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
}


class MessagingNodeListener extends Thread {

    ServerSocket serverSocket;
    Socket socket;
    BlockingQueue<String> queue;

    MessagingNodeListener(BlockingQueue<String> queue, ServerSocket serverSocket){

        this.queue = queue;
        this.serverSocket = serverSocket;

    }

    @Override
    public void run() {
        try {
            while (true) {
                socket = serverSocket.accept();
                new MessagingNodeConnection(queue, socket).start();
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

}

class MessagingNodeConnection extends Thread{


    Socket socket;
    private BlockingQueue<String> queue;

    MessagingNodeConnection(BlockingQueue<String> queue, Socket socket){

        this.queue = queue;
        this.socket = socket;

    }

    @Override
    public void run(){

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
            command = cmdScanner.nextLine();
            try {

                if (command.equals("print-shortest-path")) {

                } else if (command.contains("exit-overlay")) {

                }
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }while(!command.equals("exit"));
    }
}


