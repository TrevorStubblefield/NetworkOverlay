package cs455.overlay.wireformats;

import java.io.DataOutputStream;
import java.nio.ByteBuffer;

import static cs455.overlay.wireformats.WireFormatConstants.CONNECTION_REQUEST;
import static cs455.overlay.wireformats.WireFormatConstants.REGISTER_REQUEST;

public class ConnectionRequest implements Protocol {

    int MESSAGE_TYPE = CONNECTION_REQUEST;
    String IP_ADDRESS;
    int PORT;
    int SIZE;

    public ConnectionRequest(String ipAddress, int port){
        this.IP_ADDRESS = ipAddress;
        this.PORT = port;
    }

    @Override
    public void send(DataOutputStream out){

        try {
            SIZE = 4 + 4 + 4 + IP_ADDRESS.getBytes().length;
            ByteBuffer buffer = ByteBuffer.allocate(SIZE);

            buffer.putInt(SIZE);
            buffer.putInt(MESSAGE_TYPE);
            buffer.putInt(PORT);
            buffer.put(IP_ADDRESS.getBytes());

            byte[] message = buffer.array();
            out.write(message);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

}
