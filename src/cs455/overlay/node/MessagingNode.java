package cs455.overlay.node;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by stubblet on 1/25/2017.
 */
public class MessagingNode {
    public static void main (String[] args){
        try {
            Socket socket = new Socket("16.78.182.242", 1234);

            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println("random text");

            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String text = input.readLine();
            System.out.println(text);
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }
    }
}
