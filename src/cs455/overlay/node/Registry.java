package cs455.overlay.node;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by stubblet on 1/25/2017.
 */
public class Registry {
    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(1234);

            while(true){
                String text = "No Message";
                Socket socket = serverSocket.accept();
                BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                text = input.readLine();
                System.out.println(text);

                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                out.println("response");
            }


        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
