package cs455.overlay.wireformats;

import java.io.DataOutputStream;
import java.nio.ByteBuffer;

import static cs455.overlay.wireformats.WireFormatConstants.TASK_COMPLETE;

public class TaskComplete implements Protocol {

    int MESSAGE_TYPE = TASK_COMPLETE;
    String ip;
    int port;

    public TaskComplete(String ip, int port){
        this.ip = ip;
        this.port = port;
    }

    @Override
    public void send(DataOutputStream out){
        byte[] messageTypeInBytes = ByteBuffer.allocate(4).putInt(MESSAGE_TYPE).array();
        byte[] portInBytes = ByteBuffer.allocate(4).putInt(port).array();
        byte[] ipAddressInBytes = ip.getBytes();

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
