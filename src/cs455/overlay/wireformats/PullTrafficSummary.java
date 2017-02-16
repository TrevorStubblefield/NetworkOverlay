package cs455.overlay.wireformats;

import java.io.DataOutputStream;
import java.nio.ByteBuffer;

import static cs455.overlay.wireformats.WireFormatConstants.PULL_TRAFFIC_SUMMARY;

public class PullTrafficSummary implements Protocol{
    int MESSAGE_TYPE = PULL_TRAFFIC_SUMMARY;

    public PullTrafficSummary(){}

    @Override
    public void send(DataOutputStream out){
        byte[] messageTypeInBytes = ByteBuffer.allocate(4).putInt(MESSAGE_TYPE).array();
        try{
            out.write(messageTypeInBytes);
        }
        catch(Exception e){}
    }
}
