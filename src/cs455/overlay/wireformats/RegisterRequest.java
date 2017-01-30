package cs455.overlay.wireformats;

import java.io.PrintWriter;

public class RegisterRequest implements Protocol {

    int MESSAGE_TYPE = 1;
    String IP_ADDRESS;
    int PORT;

    public RegisterRequest(String ipAddress, int port){
        IP_ADDRESS = ipAddress;
        PORT = port;
    }

    @Override
    public void send(PrintWriter out){
        out.print(MESSAGE_TYPE);

    }

}
