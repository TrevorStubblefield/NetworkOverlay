package cs455.overlay.node;

import cs455.overlay.wireformats.MessagingNodesList;
import cs455.overlay.wireformats.RegisterResponse;
import cs455.overlay.wireformats.SetupOverlay;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static cs455.overlay.wireformats.WireFormatConstants.*;

public class Registry {

    public static void main(String[] args){
        ServerSocket serverSocket;
        Socket socket;
        int port = Integer.parseInt(args[0]);
        BlockingQueue<MessagingNode> queue = new ArrayBlockingQueue<>(100);
        new RegistryServerCommands(port, queue).start();

        try{
            System.out.println("Registry Server Running...");
            serverSocket = new ServerSocket(port);

            while(true){
                socket = serverSocket.accept();
                new RegistryHandler(socket, queue).start();
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
}

class RegistryHandler extends Thread{

    Socket socket;
    private BlockingQueue<MessagingNode> queue;

    RegistryHandler(Socket socket, BlockingQueue<MessagingNode> queue){
        this.socket = socket;
        this.queue = queue;
    }
    
    //TODO: Actually register the nodes and do the checking.
    public void registerNode(String message){
        try {
            String[] messageSplit = message.split("\n");

            MessagingNode messagingNode = new MessagingNode(messageSplit[0],messageSplit[1],Integer.parseInt(messageSplit[2]));

            queue.put(messagingNode);

            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            RegisterResponse registerResponse = new RegisterResponse((byte)1 , "Registration request successful. The number of messaging nodes currently constituting the overlay is (" + queue.size() + ")");
            registerResponse.send(out);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    //TODO: All of this method.
    public void deregisterNode(){}

    
    public void setupOverlay(int numberOfRequiredConnections){

        MessagingNode[] messagingNodes = queue.toArray(new MessagingNode[queue.size()]);
        MessagingNode messagingNode;
        for (int i = 0; i < messagingNodes.length; i++) {
            messagingNode = messagingNodes[i];
            messagingNode.connectedWeights = new int[numberOfRequiredConnections];
            ArrayList<MessagingNode> connectedMessagingNodes = new ArrayList<>();

            if(messagingNode.numberOfConnections() < numberOfRequiredConnections){
                if(i == 0){
                    messagingNode.connectedNodes.add(messagingNodes[messagingNodes.length-1]);
                    messagingNodes[messagingNodes.length-1].connectedNodes.add(messagingNode);

                    messagingNode.connectedNodes.add(messagingNodes[messagingNodes.length-1]);
                    messagingNodes[messagingNodes.length-2].connectedNodes.add(messagingNode);

                    messagingNode.connectedNodes.add(messagingNodes[i+1]);
                    messagingNodes[i+1].connectedNodes.add(messagingNode);

                    messagingNode.connectedNodes.add(messagingNodes[i+2]);
                    messagingNodes[i+2].connectedNodes.add(messagingNode);
                }
                else if (i == 1){
                    messagingNode.connectedNodes.add(messagingNodes[messagingNodes.length-1]);
                    messagingNodes[messagingNodes.length-1].connectedNodes.add(messagingNode);

                    messagingNode.connectedNodes.add(messagingNodes[i+1]);
                    messagingNodes[i+1].connectedNodes.add(messagingNode);

                    messagingNode.connectedNodes.add(messagingNodes[i+2]);
                    messagingNodes[i+2].connectedNodes.add(messagingNode);
                }
                else{
                    if(i+1 < messagingNodes.length) {
                        messagingNode.connectedNodes.add(messagingNodes[i + 1]);
                        messagingNodes[i + 1].connectedNodes.add(messagingNode);
                    }

                    if(i+2 < messagingNodes.length) {
                        messagingNode.connectedNodes.add(messagingNodes[i + 2]);
                        messagingNodes[i + 2].connectedNodes.add(messagingNode);
                    }
                }
            }



            try {
                for (MessagingNode connectedNode : messagingNode.connectedNodes){
                    connectedMessagingNodes.add(connectedNode);
                }
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                MessagingNodesList messagingNodesList = new MessagingNodesList(numberOfRequiredConnections,connectedMessagingNodes);
                messagingNodesList.send(out);
            }
            catch(Exception e){
                e.printStackTrace();
            }

        }
    }

    public void sendOverlayLinkWeights(){
        MessagingNode[] messagingNodes = queue.toArray(new MessagingNode[queue.size()]);

        
    }


    @Override
    public void run(){
        try{
            while(!socket.isClosed()){

                DataInputStream input = new DataInputStream(socket.getInputStream());

                int requestFormat = input.readInt();

                if( requestFormat == REGISTER_REQUEST ){

                    String hostName = socket.getInetAddress().getCanonicalHostName();

                    int port = input.readInt();
                    byte[] ipAddressInBytes = new byte[input.available()];
                    int i = 0;
                    while(input.available() > 0){
                        ipAddressInBytes[i] = input.readByte();
                        i++;
                    }
                    String ipAddress = new String(ipAddressInBytes, "UTF-8");
                    String request = ipAddress + "\n" + hostName + "\n" + port;

                    registerNode(request);
                }

                else if( requestFormat == DEREGISTER_REQUEST ){
                    deregisterNode();
                    socket.close();
                }

                else if( requestFormat == SETUP_OVERLAY ){
                    int numberOfRequiredConnections = input.readInt();
                    setupOverlay(numberOfRequiredConnections);
                    socket.close();
                }

                else if( requestFormat == SEND_OVERLAY_LINK_WEIGHTS ){
                    sendOverlayLinkWeights();
                    socket.close();
                }

            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
}

//TODO: send-overlay-link-weights and start.
class RegistryServerCommands extends Thread{

    private BlockingQueue<MessagingNode> queue;
    private int port;

    RegistryServerCommands(int port, BlockingQueue<MessagingNode> queue) {
        this.port = port;
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
                    if(!queue.isEmpty()){
                        MessagingNode[] nodes = queue.toArray(new MessagingNode[queue.size()]);
                        for (MessagingNode node : nodes) {
                            for (MessagingNode connectedNode : node.connectedNodes) {
                                int connectedNodeIndex = node.connectedNodes.indexOf(connectedNode);
                                int connectedNodeWeight = node.connectedWeights[connectedNodeIndex];
                                System.out.println(node.hostName + ":" + node.port + " " + connectedNode.hostName + ":" + connectedNode.port + " " + connectedNodeWeight);
                                //System.out.println(node.hostName + ":" + node.port + " " + connectedNode.hostName + ":" + connectedNode.port);
                            }
                            System.out.println();
                        }
                    }

                } else if (command.contains("setup-overlay")) {

                    String[] commandSplit = command.split(" ");
                    int numberOfRequiredConnections = Integer.parseInt(commandSplit[1]);
                    Socket socket = new Socket("localhost",port);
                    DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                    SetupOverlay setupOverlay = new SetupOverlay(numberOfRequiredConnections);
                    setupOverlay.send(out);

                } else if (command.contains("send-overlay-link-weights")) {

                    Socket socket = new Socket("localhost",port);
                    DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                    byte[] message = ByteBuffer.allocate(4).putInt(SEND_OVERLAY_LINK_WEIGHTS).array();
                    out.write(message);

                } else if (command.contains("start")) {

                    String[] commandSplit = command.split(" ");
                    int numberOfRounds = Integer.parseInt(commandSplit[1]);
                }
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }while(!command.equals("exit"));
    }
}
