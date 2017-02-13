package cs455.overlay.wireformats;

import java.io.DataOutputStream;
import java.nio.ByteBuffer;

import static cs455.overlay.wireformats.WireFormatConstants.SETUP_OVERLAY;

public class SetupOverlay implements Protocol {

    int MESSAGE_TYPE = SETUP_OVERLAY;
    int NUMBER_OF_CONNECTIONS;

    public SetupOverlay(int numberOfConnections){
        this.NUMBER_OF_CONNECTIONS = numberOfConnections;
    }

    @Override
    public void send(DataOutputStream out){

        byte[] messageTypeInBytes = ByteBuffer.allocate(4).putInt(MESSAGE_TYPE).array();
        byte[] numberOfConnectionsInBytes = ByteBuffer.allocate(4).putInt(NUMBER_OF_CONNECTIONS).array();

        try {
            out.write(messageTypeInBytes);
            out.write(numberOfConnectionsInBytes);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
}
