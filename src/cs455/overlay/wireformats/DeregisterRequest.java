package cs455.overlay.wireformats;

import java.io.PrintWriter;

import static cs455.overlay.wireformats.WireFormatConstants.DEREGISTER_REQUEST;

public class DeregisterRequest implements Protocol {

    int MESSAGE_TYPE = DEREGISTER_REQUEST;
    String IP_ADDRESS, HOST_NAME;
    int PORT;

    public DeregisterRequest(String ipAddress, String hostName, int port){
        IP_ADDRESS = ipAddress;
        HOST_NAME = hostName;
        PORT = port;
    }

    @Override
    public void send(PrintWriter out){
        out.println(MESSAGE_TYPE);
        out.println(IP_ADDRESS);
        out.println(PORT);
    }

}
