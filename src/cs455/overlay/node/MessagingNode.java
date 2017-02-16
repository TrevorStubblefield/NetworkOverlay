package cs455.overlay.node;

import cs455.overlay.wireformats.*;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static cs455.overlay.wireformats.WireFormatConstants.*;

public class MessagingNode {

    public static HashMap<String, ArrayList<Link>> links = new HashMap<>();
    public static HashMap<String, Socket> socketMap = new HashMap<>();

    //Messaging Node Specific Data.
    public String hostName,ipAddress;
    public int port;
    public List<MessagingNode> connectedNodes;
    public List<MessagingNode> oneWayConnection;
    public int[] connectedWeights;
    DataOutputStream outputStream;
    boolean registered;

    //Thread safe data.
    public static volatile AtomicInteger sendTracker = new AtomicInteger(0);
    public static volatile AtomicInteger receiveTracker = new AtomicInteger(0);
    public static volatile AtomicInteger relayTracker = new AtomicInteger(0);

    public static volatile AtomicLong sendSummation = new AtomicLong(0);
    public static volatile AtomicLong receiveSummation = new AtomicLong(0);
    public static volatile boolean wait = true;

    public MessagingNode(String ipAddress, String hostName, int port, DataOutputStream outputStream){
        this.ipAddress = ipAddress;
        this.hostName = hostName;
        this.port = port;
        this.connectedNodes = new ArrayList<>();
        this.oneWayConnection = new ArrayList<>();
        this.outputStream = outputStream;
    }

    public int numberOfConnections(){
        return connectedNodes.size();
    }

    public static void sendToRandomNode(HashMap<String, ArrayList<Link>> links, ArrayList<Socket> nodes, int data, String host, int port){

        try{
            ArrayList<String> keys = new ArrayList<>(links.keySet());
            keys.remove(host+":"+port);
            int randomNum = ThreadLocalRandom.current().nextInt(0, keys.size());
            String sink = keys.get(randomNum);

            String path = calculateShortestPath(host+":"+port, sink, links);
            String[] parts = path.split(" ");
            String sendTo = parts[0];

            path = "";
            for(int j = 1; j < parts.length; j++){
                path += parts[j] + " ";
            }

            System.out.println(path);
            DataOutputStream out = new DataOutputStream(socketMap.get(sendTo).getOutputStream());
            TrafficMessage trafficMessage = new TrafficMessage(path, data);
            trafficMessage.send(out);
            sendSummation.getAndAdd(data);
            sendTracker.getAndIncrement();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public static String calculateShortestPath(String source, String destination, HashMap<String, ArrayList<Link>> links){

        ArrayList<String> unvisitedNodes = new ArrayList<>();
        HashMap<String, Integer> distances = new HashMap<>();
        HashMap<String, String> previousNodes = new HashMap<>();

        for(String link: links.keySet()){
            distances.put(link, Integer.MAX_VALUE);
            previousNodes.put(link, null);
            unvisitedNodes.add(link);
        }

        distances.put(source, 0);

        while(unvisitedNodes.size() > 0){
            String currentNode = "";
            int lowestWeight = Integer.MAX_VALUE;

            for(String unvisitedNode : unvisitedNodes){
                if(distances.get(unvisitedNode) < lowestWeight){
                    lowestWeight = distances.get(unvisitedNode);
                    currentNode = unvisitedNode;
                }
            }

            if(currentNode.equals(destination)){
                break;
            }
            unvisitedNodes.remove(currentNode);

            ArrayList<Link> neighbors = links.get(currentNode);
            for(Link neighbor: neighbors){
                int newDistance = distances.get(currentNode) + neighbor.weight;

                if(newDistance < distances.get(neighbor.node)){
                    distances.put(neighbor.node, newDistance);
                    previousNodes.put(neighbor.node, currentNode);
                }
            }
        }

        String current = destination;
        String path = current;

        while(!previousNodes.get(current).equals(source)){
            path = previousNodes.get(current) + " " + path;
            current = previousNodes.get(current);
        }
        return path;
    }

    public static String printShortestPath(String source, String sink, HashMap<String, ArrayList<Link>> links){

        ArrayList<String> unvisitedNodes = new ArrayList<>();
        HashMap<String, Integer> distance = new HashMap<>();
        HashMap<String, String> previousNodes = new HashMap<>();

        for(String key: links.keySet()){
            distance.put(key, Integer.MAX_VALUE);
            previousNodes.put(key, null);

            unvisitedNodes.add(key);
        }

        distance.put(source, 0);

        while(unvisitedNodes.size() > 0){
            String currentNode = "";
            int lowestWeight = Integer.MAX_VALUE;

            for(String key: unvisitedNodes){
                if(distance.get(key) < lowestWeight){
                    lowestWeight = distance.get(key);
                    currentNode = key;
                }
            }

            if(currentNode.equals(sink)){
                break;
            }
            unvisitedNodes.remove(currentNode);

            ArrayList<Link> neighbors = links.get(currentNode);
            for(Link neighbor : neighbors){
                int newDistance = distance.get(currentNode) + neighbor.weight;

                if(newDistance < distance.get(neighbor.node)){
                    distance.put(neighbor.node, newDistance);
                    previousNodes.put(neighbor.node, currentNode);
                }
            }
        }

        String previousNode;
        String currentNode = sink;
        String fullPath = currentNode;

        while(!previousNodes.get(currentNode).equals(source)){
            previousNode = currentNode;
            currentNode = previousNodes.get(currentNode);

            for(Link link : links.get(currentNode)){
                if(link.node.equals(previousNode)){
                    fullPath = previousNodes.get(previousNode) + "--" + link.weight + "--" + fullPath;
                }
            }
        }
        for(Link link : links.get(source)) {
            if(link.node.equals(fullPath.split("--")[0]))
                fullPath = source + "--" + link.weight + "--" + fullPath;
        }
        return fullPath;
    }

    public static void main (String[] args){


        ServerSocket serverSocket;
        Socket registrySocket;
        ArrayList<Socket> nodeSockets = new ArrayList<>();
        String ipAddress;
        int port;

        DataOutputStream out;

        BlockingQueue<MessagingNode> queue = new ArrayBlockingQueue<>(100);
        BlockingQueue<Socket> socketQueue = new ArrayBlockingQueue<>(100);


        try {
            System.out.println("Messaging Node Running...");
            InetAddress inetAddress = InetAddress.getLocalHost();
            ipAddress = inetAddress.getHostAddress();
            serverSocket = new ServerSocket(0);
            port = serverSocket.getLocalPort();

            //Tells registry ready to register.
            registrySocket = new Socket(args[0], Integer.parseInt(args[1]));

            new MessagingNodeCommands(queue,serverSocket, registrySocket).start();
            new MessagingNodeListener(socketQueue,queue, serverSocket).start();

            out = new DataOutputStream(registrySocket.getOutputStream());
            RegisterRequest registerRequest = new RegisterRequest(ipAddress, port);
            registerRequest.send(out);

            //Receives messages from the registry.
            DataInputStream input = new DataInputStream(registrySocket.getInputStream());
            int messageType;
            do {
                messageType = input.readInt();

                if (messageType == REGISTER_RESPONSE) {

                    if (input.readByte() == 0) {
                        messageType = -1;
                    }
                    byte[] infoInBytes = new byte[input.available()];
                    int i = 0;
                    while(input.available() > 0){
                        Thread.sleep(10);
                        infoInBytes[i] = input.readByte();
                        i++;
                    }
                    String additionalInfo = new String(infoInBytes, "UTF-8");
                    System.out.println(additionalInfo);
                }

                else if (messageType == DEREGISTER_RESPONSE) {
                    if (input.readByte() == 0) {
                        messageType = -1;
                    }
                    byte[] infoInBytes = new byte[input.available()];
                    int i = 0;
                    while(input.available() > 0){
                        Thread.sleep(10);
                        infoInBytes[i] = input.readByte();
                        i++;
                    }
                    String additionalInfo = new String(infoInBytes, "UTF-8");
                    System.out.println(additionalInfo);
                    registrySocket.close();
                    System.exit(0);
                }

                else if (messageType == MESSAGING_NODES_LIST){
                    int numberOfConnections = input.readInt();
                    Thread.sleep(10);

                    byte[] received = new byte[input.available()];
                    Thread.sleep(10);
                    int j = 0;

                    while(input.available() > 0){
                        received[j] = input.readByte();
                        j++;
                    }
                    String connectedNodeString = new String(received, "UTF-8");
                    String[] messageSplit = connectedNodeString.split(" ");
                    for (String connection : messageSplit){
                        if(!connection.equals(" ") && !connection.isEmpty()) {
                            String[] connectionSplit = connection.split(":");
                            String connectionHostName = connectionSplit[0];
                            String connectionPort = connectionSplit[1];

                            Socket connectionSocket = new Socket(connectionHostName, Integer.parseInt(connectionPort));
                            socketMap.put(connectionHostName+":"+connectionPort, connectionSocket);
                            ConnectionRequest ConnectionRequest = new ConnectionRequest(ipAddress,port);
                            ConnectionRequest.send(new DataOutputStream(connectionSocket.getOutputStream()));
                            new MessagingNodeConnection(socketQueue, queue, connectionSocket).start();
                        }
                    }

                }

                else if (messageType == LINK_WEIGHTS){
                    Thread.sleep(50);
                    int numberOfLinks = input.readInt();
                    byte[] received = new byte[input.available()];
                    Thread.sleep(100);
                    int j = 0;

                    while(input.available() > 0){
                        received[j] = input.readByte();
                        j++;
                    }

                    String linkWeights = new String(received, "UTF-8");
                    String[] linkSplit = linkWeights.split(" ");

                    for(int i = 0; i < (numberOfLinks*3); i+=3) {
                        String node1 = linkSplit[i];
                        String node2 = linkSplit[i+1];
                        int weight = Integer.parseInt(linkSplit[i+2]);

                        Link link1 = new Link(node2, weight);
                        Link link2 = new Link(node1, weight);

                        ArrayList<Link> list1, list2;
                        if(links.containsKey(node1)){
                            list1 = links.get(node1);
                        } else {
                            list1 = new ArrayList<>();
                        }

                        if(links.containsKey(node2)){
                            list2 = links.get(node2);
                        } else {
                            list2= new ArrayList<>();
                        }

                        list1.add(link1);
                        list2.add(link2);

                        links.put(node1, list1);
                        links.put(node2, list2);

                    }

                }

                else if(messageType == TASK_INITIATE) {
                    Thread.sleep(50);

                    MessagingNode.receiveTracker.getAndSet(0);
                    MessagingNode.sendTracker.getAndSet(0);
                    MessagingNode.relayTracker.getAndSet(0);
                    MessagingNode.sendSummation.getAndSet(0);
                    MessagingNode.receiveSummation.getAndSet(0);

                    Thread.sleep(50);
                    wait = true;
                    int rounds = input.readInt();
                    for (int i = 0; i < rounds; i++){
                        int randomNum = ThreadLocalRandom.current().nextInt(Integer.MIN_VALUE, Integer.MAX_VALUE);
                        ArrayList<Socket> list = new ArrayList<>(Arrays.asList(socketQueue.toArray(new Socket[socketQueue.size()])));
                        sendToRandomNode(links, list,randomNum, ipAddress, port);
                    }
                    while(wait) {
                        Thread.sleep(10);
                    }
                    TaskComplete taskComplete = new TaskComplete(ipAddress,port);
                    taskComplete.send(out);
                }

                else if(messageType == PULL_TRAFFIC_SUMMARY){
                    System.out.println("Finished rounds. Sending TRAFFIC_SUMMARY...");
                    TrafficSummary trafficSummary = new TrafficSummary(ipAddress,port,sendTracker.get(),receiveTracker.get(),sendSummation.get(),receiveSummation.get(),relayTracker.get());
                    trafficSummary.send(out);
                }


            }while(messageType != EXIT_MESSAGE);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
}

class MessagingNodeListener extends Thread {

    ServerSocket serverSocket;
    Socket socket;
    BlockingQueue<MessagingNode> queue;
    BlockingQueue<Socket> socketQueue;

    MessagingNodeListener(BlockingQueue<Socket> socketQueue, BlockingQueue<MessagingNode> queue, ServerSocket serverSocket){

        this.queue = queue;
        this.serverSocket = serverSocket;
        this.socketQueue = socketQueue;
    }

    @Override
    public void run() {
        try {
            while (true) {
                socket = serverSocket.accept();
                new MessagingNodeConnection(socketQueue, queue, socket).start();
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

}

class MessagingNodeConnection extends Thread{

    Socket socket;
    private BlockingQueue<MessagingNode> queue;
    BlockingQueue<Socket> socketQueue;

    MessagingNodeConnection(BlockingQueue<Socket> socketQueue, BlockingQueue<MessagingNode> queue, Socket socket){

        this.queue = queue;
        this.socket = socket;
        this.socketQueue = socketQueue;
    }

    @Override
    public void run() {
        try {
            DataInputStream input = new DataInputStream(socket.getInputStream());

            boolean flag = true;
            while(flag){
                if(input.available() > 0){
                    byte[] message = new byte[input.available()];
                    input.readFully(message);

                    handleMessage(message);
                }

                Thread.sleep(10);
            }
        } catch (Exception e) {
            System.out.println("Failure getting available bytes. " + e);
        }

    }

    public void handleMessage(byte[] bytes) throws IOException{
        while(bytes.length > 0){
            ByteBuffer buffer = ByteBuffer.wrap(bytes);

            int length = buffer.getInt();
            byte[] message = Arrays.copyOfRange(bytes, 4, length);

            handleMessageType(message);

            bytes = Arrays.copyOfRange(bytes, length, bytes.length);
        }
    }

    public void handleMessageType(byte[] message) throws IOException{
        ByteBuffer buffer = ByteBuffer.wrap(message);
        int type = buffer.getInt();

        if(type == CONNECTION_REQUEST) {
            int port = buffer.getInt();
            String ipAddress = new String(Arrays.copyOfRange(message, 8, message.length));
            MessagingNode.socketMap.put(ipAddress + ":" + port, socket);
        }
        if(type == TRAFFIC_MESSAGE){
            handleTrafficMessage(message);
        }
    }

    public void handleTrafficMessage(byte[] message) throws IOException{
        ByteBuffer buffer = ByteBuffer.wrap(message);

        int data = buffer.getInt(4);
        String path = new String(Arrays.copyOfRange(message, 8, message.length));

        if(path.length() > 0){
            String[] parts = path.split(" ");
            String target = parts[0];

            path = "";
            for(int i = 1; i < parts.length; i++){
                path += parts[i] + " ";
            }

            //System.out.println(path);
            MessagingNode.relayTracker.getAndIncrement();
            TrafficMessage request = new TrafficMessage(path, data);
            if(MessagingNode.socketMap.containsKey(target)) {
                request.send(new DataOutputStream(MessagingNode.socketMap.get(target).getOutputStream()));
            }
        } else {
            MessagingNode.receiveTracker.getAndIncrement();
            MessagingNode.receiveSummation.getAndAdd(data);
            MessagingNode.wait = false;
        }
    }
}

class MessagingNodeCommands extends Thread{
    private BlockingQueue<MessagingNode> queue;
    ServerSocket serverSocket;
    Socket registrySocket;

    MessagingNodeCommands(BlockingQueue<MessagingNode> queue, ServerSocket serverSocket, Socket registrySocket) {
        this.queue = queue;
        this.serverSocket = serverSocket;
        this.registrySocket = registrySocket;
    }


    @Override
    public void run(){
        try{
            InetAddress inetAddress = InetAddress.getLocalHost();
            String ipAddress = inetAddress.getHostAddress();
            int port = serverSocket.getLocalPort();
            String command;
            do {
                Scanner cmdScanner = new Scanner(System.in);
                command = cmdScanner.nextLine();

                if (command.equals("print-shortest-path")) {
                    try {
                        ArrayList<String> keys = new ArrayList<>(MessagingNode.links.keySet());
                        keys.remove(ipAddress + ":" + port);

                        for(String key : keys){
                            System.out.println(MessagingNode.printShortestPath(ipAddress+":"+port,key,MessagingNode.links));
                        }

                    }
                    catch(Exception e){
                        e.printStackTrace();
                    }


                } else if (command.contains("exit-overlay")) {
                    DataOutputStream out = new DataOutputStream(registrySocket.getOutputStream());
                    DeregisterRequest deregisterRequest = new DeregisterRequest(ipAddress, port);
                    deregisterRequest.send(out);
                }

            } while (!command.equals("exit"));
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
}

class Link {
    String node;
    int weight;

    Link(String n, int w){
        this.node = n;
        this.weight = w;
    }
}



