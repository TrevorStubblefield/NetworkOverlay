package cs455.overlay.node;

import cs455.overlay.wireformats.MessagingNodesList;
import cs455.overlay.wireformats.RegisterResponse;
import cs455.overlay.wireformats.SetupOverlay;

import static cs455.overlay.wireformats.WireFormatConstants.DEREGISTER_REQUEST;
import static cs455.overlay.wireformats.WireFormatConstants.REGISTER_REQUEST;
import static cs455.overlay.wireformats.WireFormatConstants.SETUP_OVERLAY;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

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

        int currentNode = 0;
        MessagingNode[] messagingNodes = queue.toArray(new MessagingNode[queue.size()]);

        for (MessagingNode messagingNode : messagingNodes) {
            messagingNode.connectedWeights = new int[numberOfRequiredConnections];
            ArrayList<MessagingNode> connectedMessagingNodes = new ArrayList<>();
            while(messagingNode.numberOfConnections() < numberOfRequiredConnections){
                Random random = new Random();
                int randomNum = random.nextInt(10);
                if(randomNum != currentNode) {
                    if (messagingNode.numberOfConnections() < numberOfRequiredConnections && messagingNodes[randomNum].numberOfConnections() < numberOfRequiredConnections) {
                        if (!messagingNode.connectedNodes.contains(messagingNodes[randomNum]) && !(messagingNodes[randomNum].connectedNodes.contains(messagingNode))) {
                            messagingNode.connectedNodes.add(messagingNodes[randomNum]);
                            messagingNodes[randomNum].connectedNodes.add(messagingNode);
                            connectedMessagingNodes.add(messagingNodes[randomNum]);
                        }
                    }
                }
            }

            try {
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                MessagingNodesList messagingNodesList = new MessagingNodesList(numberOfRequiredConnections,connectedMessagingNodes);
                messagingNodesList.send(out);
            }
            catch(Exception e){
                e.printStackTrace();
            }

            currentNode++;
        }
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
