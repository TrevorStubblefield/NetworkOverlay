package cs455.overlay.wireformats;

import java.io.DataOutputStream;
import java.nio.ByteBuffer;

import static cs455.overlay.wireformats.WireFormatConstants.TRAFFIC_MESSAGE;

public class TrafficMessage implements Protocol {

    int MESSAGE_TYPE = TRAFFIC_MESSAGE;
    String PATH;
    int DATA;
    int SIZE;

    public TrafficMessage(String path, int data){
        this.PATH = path;
        this.DATA = data;
    }

    @Override
    public void send(DataOutputStream out){
        try {
            SIZE = 4 + 4 + 4 + PATH.getBytes().length;
            ByteBuffer buffer = ByteBuffer.allocate(SIZE);

            buffer.putInt(SIZE);
            buffer.putInt(MESSAGE_TYPE);
            buffer.putInt(DATA);
            buffer.put(PATH.getBytes());

            byte[] message = buffer.array();
            out.write(message);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

}
