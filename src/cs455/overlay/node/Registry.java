package cs455.overlay.node;

import cs455.overlay.wireformats.*;

import javax.xml.crypto.Data;
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static cs455.overlay.wireformats.WireFormatConstants.*;

public class Registry {

//    public static volatile AtomicInteger totalSent = new AtomicInteger(0);
//    public static volatile AtomicInteger totalReceived = new AtomicInteger(0);
//
//    public static volatile AtomicLong totalSumSent = new AtomicLong(0);
//    public static volatile AtomicLong totalSumReceived = new AtomicLong(0);

    public static volatile int totalSent = 0;
    public static volatile int totalReceived = 0;

    public static volatile long totalSumSent = 0;
    public static volatile long totalSumReceived = 0;

    public static volatile int counter = 0;

    static BlockingQueue<MessagingNode> queue = new ArrayBlockingQueue<>(100);

    public static synchronized void incrementThreadCounter(int a, int b, long c, long d){

        Registry.totalSent += a;
        Registry.totalReceived += b;
        Registry.totalSumSent += c;
        Registry.totalSumReceived += d;

        counter++;
        if(counter == queue.size())
            System.out.println("totals:             " + Registry.totalSent + "   " + Registry.totalReceived + "   " + Registry.totalSumSent + "    " + Registry.totalSumReceived);
    }

    public static void main(String[] args){
        ServerSocket serverSocket;
        Socket socket;
        int port = Integer.parseInt(args[0]);

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
    MessagingNode node;

    RegistryHandler(Socket socket, BlockingQueue<MessagingNode> queue){
        this.socket = socket;
        this.queue = queue;
    }

    public int numRegistered(){
        int counter = 0;
        MessagingNode[] nodes = queue.toArray(new MessagingNode[queue.size()]);
        for (MessagingNode node : nodes){
            if(node.registered == true){
                counter++;
            }
        }
        return counter;
    }

    public MessagingNode registerNode(String message){
        try {
            Thread.sleep(10);
            String[] messageSplit = message.split("\n");
            String ipAddress = messageSplit[0];
            String hostName = messageSplit[1];
            int port = Integer.parseInt(messageSplit[2]);
            byte passed = 1;
            boolean register = true;
            String responseMessage = "";

            MessagingNode messagingNode = new MessagingNode(ipAddress, hostName, port,new DataOutputStream(socket.getOutputStream()));
            MessagingNode[] nodes = queue.toArray(new MessagingNode[queue.size()]);

            for(MessagingNode node : nodes) {
                if (node.ipAddress.equals(ipAddress) && node.port == port) {
                    if (node.registered == true){
                        passed = 0;
                        responseMessage = "Error: Already registered on that IP and port.";
                    }
                    else{
                        node.registered = true;
                        register = false;
                    }
                }
            }

            if (!hostName.equals(ipAddress)){
                passed = 0;
                responseMessage = "Error: Mismatch in sockets IP and IP specified in registration request.";
            }

            if(passed == 1 && register) {
                messagingNode.registered = true;
                queue.put(messagingNode);
                responseMessage = "Registration request successful. The number of messaging nodes currently constituting the overlay is (" + numRegistered() + ")";
            }

            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            RegisterResponse registerResponse = new RegisterResponse(passed , responseMessage);
            registerResponse.send(out);
            return messagingNode;
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public void deregisterNode(String message){
        try {
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            String[] messageSplit = message.split("\n");
            String ipAddress = messageSplit[0];
            String hostName = messageSplit[1];
            int port = Integer.parseInt(messageSplit[2]);
            byte passed = 0;
            String responseMessage = "Failed to deregister from the overlay.";

            MessagingNode[] nodes = queue.toArray(new MessagingNode[queue.size()]);

            if(ipAddress.equals(hostName)){
                passed = 1;
                for (MessagingNode node : nodes){
                    if (node.ipAddress.equals(ipAddress) && node.port == port){
                        node.registered = false;
                        responseMessage = "Successfully deregistered from the overlay.";
                        queue.remove(node);
                    }
                }
            }

            DeregisterResponse deregisterResponse = new DeregisterResponse(passed, responseMessage);
            deregisterResponse.send(out);

        }
        catch(Exception e){
            e.printStackTrace();
        }

    }

    public void setupOverlay(int numberOfRequiredConnections){

        //Creates a randomized array of the messaging nodes, therefore connections are randomized.
        MessagingNode[] messagingNodes = queue.toArray(new MessagingNode[queue.size()]);
        Random random = new Random();
        for (int i = messagingNodes.length - 1; i > 0; i--)
        {
            int index = random.nextInt(i + 1);
            MessagingNode swap = messagingNodes[index];
            messagingNodes[index] = messagingNodes[i];
            messagingNodes[i] = swap;
        }

        MessagingNode messagingNode;
        for (int i = 0; i < messagingNodes.length; i++) {
            messagingNode = messagingNodes[i];
            messagingNode.connectedWeights = new int[numberOfRequiredConnections];
            ArrayList<MessagingNode> connectedMessagingNodes = new ArrayList<>();

            if(messagingNode.registered && messagingNode.numberOfConnections() < numberOfRequiredConnections){
                if(i == 0 && messagingNodes[messagingNodes.length-2].registered){
                    messagingNode.connectedNodes.add(messagingNodes[messagingNodes.length-2]);
                    messagingNodes[messagingNodes.length-2].connectedNodes.add(messagingNode);
                    messagingNode.oneWayConnection.add(messagingNodes[messagingNodes.length-2]);

                }
                if (i == 0 || i == 1 && messagingNodes[messagingNodes.length-1].registered) {
                    messagingNode.connectedNodes.add(messagingNodes[messagingNodes.length - 1]);
                    messagingNodes[messagingNodes.length - 1].connectedNodes.add(messagingNode);
                    messagingNode.oneWayConnection.add(messagingNodes[messagingNodes.length - 1]);

                }
                if(i+1 < messagingNodes.length && messagingNodes[i+1].registered) {
                    messagingNode.connectedNodes.add(messagingNodes[i + 1]);
                    messagingNodes[i + 1].connectedNodes.add(messagingNode);
                    messagingNode.oneWayConnection.add(messagingNodes[i + 1]);
                }
                if(i+2 < messagingNodes.length && messagingNodes[i+2].registered) {
                    messagingNode.connectedNodes.add(messagingNodes[i + 2]);
                    messagingNodes[i + 2].connectedNodes.add(messagingNode);
                    messagingNode.oneWayConnection.add(messagingNodes[i + 2]);
                }
            }

            //Sends MESSAGING_NODES_LIST message to all nodes.
            try {
                for (MessagingNode connectedNode : messagingNode.oneWayConnection){
                    connectedMessagingNodes.add(connectedNode);
                }
                MessagingNodesList messagingNodesList = new MessagingNodesList(numberOfRequiredConnections,connectedMessagingNodes);
                messagingNodesList.send(messagingNodes[i].outputStream);
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
        System.out.println("Overlay has been setup.");
    }

    public void sendOverlayLinkWeights(){

        //Create message, send to each node.
        int numLinks = 0;
        ArrayList<String> links = new ArrayList<>();

        MessagingNode[] messagingNodes = queue.toArray(new MessagingNode[queue.size()]);
        for (MessagingNode node : messagingNodes){
            for (MessagingNode connectedNode : node.oneWayConnection){
                Random random = new Random();
                int randomNum = random.nextInt(10) + 1;
                node.connectedWeights[node.connectedNodes.indexOf(connectedNode)] = randomNum;
                connectedNode.connectedWeights[connectedNode.connectedNodes.indexOf(node)] = randomNum;
                String link = node.hostName + ":" + node.port + " " + connectedNode.hostName + ":" + connectedNode.port + " " + randomNum + " ";
                links.add(link);
                numLinks++;
            }
        }

        LinkWeights linkWeights = new LinkWeights(numLinks, links);

        for(MessagingNode node : messagingNodes){
            linkWeights.send(node.outputStream);
        }
        System.out.println("Link weights have been sent.");
    }

    @Override
    public void run(){
        try{
            while(!socket.isClosed()){

                DataInputStream input = new DataInputStream(socket.getInputStream());

                int requestFormat = input.readInt();

                if( requestFormat == REGISTER_REQUEST ){

                    String hostName = socket.getInetAddress().getHostAddress();

                    int port = input.readInt();
                    byte[] ipAddressInBytes = new byte[input.available()];

                    int i = 0;
                    while(input.available() > 0){
                        ipAddressInBytes[i] = input.readByte();
                        i++;
                    }
                    String ipAddress = new String(ipAddressInBytes, "UTF-8");
                    String request = ipAddress + "\n" + hostName + "\n" + port;

                    node = registerNode(request);
                }

                else if( requestFormat == DEREGISTER_REQUEST ){
                    String hostName = socket.getInetAddress().getHostAddress();

                    int port = input.readInt();
                    byte[] ipAddressInBytes = new byte[input.available()];

                    int i = 0;
                    while(input.available() > 0){
                        ipAddressInBytes[i] = input.readByte();
                        i++;
                    }
                    String ipAddress = new String(ipAddressInBytes, "UTF-8");
                    String request = ipAddress + "\n" + hostName + "\n" + port;

                    deregisterNode(request);
                    //socket.close();
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

                else if( requestFormat == TASK_COMPLETE ){

                    int port = input.readInt();
                    byte[] ipAddressInBytes = new byte[input.available()];

                    int i = 0;
                    while(input.available() > 0){
                        ipAddressInBytes[i] = input.readByte();
                        i++;
                    }
                    String ipAddress = new String(ipAddressInBytes, "UTF-8");
                    PullTrafficSummary pullTrafficSummary = new PullTrafficSummary();
                    DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                    pullTrafficSummary.send(out);
                }

                else if ( requestFormat == TRAFFIC_SUMMARY ){

                    int port = input.readInt();
                    int messages_sent = input.readInt();
                    int messages_received = input.readInt();
                    long sum_sent = input.readLong();
                    long sum_received = input.readLong();
                    int relayed = input.readInt();
                    byte[] ipAddressInBytes = new byte[input.available()];

                    int i = 0;
                    while(input.available() > 0){
                        ipAddressInBytes[i] = input.readByte();
                        i++;
                    }
                    String ipAddress = new String(ipAddressInBytes, "UTF-8");

//                    Registry.totalSent.getAndAdd(messages_sent);
//                    Registry.totalReceived.getAndAdd(messages_received);
//                    Registry.totalSumSent.getAndAdd(sum_sent);
//                    Registry.totalSumReceived.getAndAdd(sum_received);

//                    Registry.totalSent += messages_sent;
//                    Registry.totalReceived += messages_received;
//                    Registry.totalSumSent += sum_sent;
//                    Registry.totalSumReceived += sum_received;

                    System.out.println(ipAddress+":"+port + "   " + messages_sent + "   " + messages_received + "   " + sum_sent + "    " + sum_received + "    " + relayed);
                    Registry.incrementThreadCounter(messages_sent, messages_received, sum_sent, sum_received);

                }

            }
        }
        catch(Exception e){
            queue.remove(node);
        }
    }
}

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
                            if(node.registered)
                                System.out.println(node.hostName + ":" + node.port);
                        }
                    }

                } else if (command.contains("list-weights")) {
                    if(!queue.isEmpty()){
                        MessagingNode[] nodes = queue.toArray(new MessagingNode[queue.size()]);
                        for (MessagingNode node : nodes) {
                            for (MessagingNode connectedNode : node.connectedNodes) {
                                int connectedNodeIndex = node.connectedNodes.indexOf(connectedNode);
                                int connectedNodeWeight = node.connectedWeights[connectedNodeIndex];
                                System.out.println(node.ipAddress + ":" + node.port + " " + connectedNode.ipAddress + ":" + connectedNode.port + " " + connectedNodeWeight);
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

//                    Registry.totalSent.getAndSet(0);
//                    Registry.totalReceived.getAndSet(0);
//                    Registry.totalSumSent.getAndSet(0);
//                    Registry.totalSumReceived.getAndSet(0);
                    Registry.totalReceived = 0;
                    Registry.totalSent = 0;
                    Registry.totalSumReceived = 0;
                    Registry.totalSumSent = 0;
                    Registry.counter = 0;

                    System.out.println("Starting rounds...");
                    String[] commandSplit = command.split(" ");
                    int numberOfRounds = Integer.parseInt(commandSplit[1]);
                    MessagingNode[] nodes = queue.toArray(new MessagingNode[queue.size()]);

                    for(MessagingNode node : nodes){
                        if(node.registered){
                            TaskInitiate taskInitiate = new TaskInitiate(numberOfRounds);
                            taskInitiate.send(node.outputStream);
                        }
                    }

                }
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }while(!command.equals("exit"));
    }
}
