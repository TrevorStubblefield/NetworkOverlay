package cs455.overlay.wireformats;

import java.io.DataOutputStream;
import java.nio.ByteBuffer;

import static cs455.overlay.wireformats.WireFormatConstants.DEREGISTER_REQUEST;

public class DeregisterRequest implements Protocol {

    int MESSAGE_TYPE = DEREGISTER_REQUEST;
    String IP_ADDRESS;
    int PORT;

    public DeregisterRequest(String ipAddress, int port){
        IP_ADDRESS = ipAddress;
        PORT = port;
    }

    @Override
    public void send(DataOutputStream out){

        byte[] messageTypeInBytes = ByteBuffer.allocate(4).putInt(MESSAGE_TYPE).array();
        byte[] portInBytes = ByteBuffer.allocate(4).putInt(PORT).array();
        byte[] ipAddressInBytes = IP_ADDRESS.getBytes();

        try {
            out.write(messageTypeInBytes);
            out.write(portInBytes);
            out.write(ipAddressInBytes);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

}
