package cs455.overlay.wireformats;

import java.io.DataOutputStream;
import java.nio.ByteBuffer;

import static cs455.overlay.wireformats.WireFormatConstants.TRAFFIC_SUMMARY;

public class TrafficSummary implements Protocol {

    int MESSAGE_TYPE = TRAFFIC_SUMMARY;
    String IP;
    int PORT;
    int MESSAGES_SENT;
    int MESSAGES_RECEIVED;
    long SUM_SENT;
    long SUM_RECEIVED;
    int NUM_RELAYED;

    public TrafficSummary(String ip, int port, int sent, int received, long sumSent, long sumReceived, int numRelayed){
        this.IP = ip;
        this.PORT = port;
        this.MESSAGES_SENT = sent;
        this.MESSAGES_RECEIVED = received;
        this.SUM_SENT = sumSent;
        this.SUM_RECEIVED = sumReceived;
        this.NUM_RELAYED = numRelayed;
    }

    @Override
    public void send(DataOutputStream out){
        byte[] messageTypeInBytes = ByteBuffer.allocate(4).putInt(MESSAGE_TYPE).array();
        byte[] portInBytes = ByteBuffer.allocate(4).putInt(PORT).array();
        byte[] ipAddressInBytes = IP.getBytes();
        byte[] sent = ByteBuffer.allocate(4).putInt(MESSAGES_SENT).array();
        byte[] rec = ByteBuffer.allocate(4).putInt(MESSAGES_RECEIVED).array();
        byte[] sumS = ByteBuffer.allocate(8).putLong(SUM_SENT).array();
        byte[] sumR = ByteBuffer.allocate(8).putLong(SUM_RECEIVED).array();
        byte[] numRelayed = ByteBuffer.allocate(4).putInt(NUM_RELAYED).array();


        try {
            out.write(messageTypeInBytes);
            out.write(portInBytes);
            out.write(sent);
            out.write(rec);
            out.write(sumS);
            out.write(sumR);
            out.write(numRelayed);
            out.write(ipAddressInBytes);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

}
