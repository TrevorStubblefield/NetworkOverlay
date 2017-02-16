package cs455.overlay.wireformats;

import java.io.DataOutputStream;
import java.nio.ByteBuffer;

import static cs455.overlay.wireformats.WireFormatConstants.TASK_INITIATE;

public class TaskInitiate implements Protocol {

    int MESSAGE_TYPE = TASK_INITIATE;
    int ROUNDS;

    public TaskInitiate(int rounds){
        this.ROUNDS = rounds;
    }

    @Override
    public void send(DataOutputStream out){
        byte[] messageTypeInBytes = ByteBuffer.allocate(4).putInt(MESSAGE_TYPE).array();
        byte[] roundsInBytes = ByteBuffer.allocate(4).putInt(ROUNDS).array();

        try{
            out.write(messageTypeInBytes);
            out.write(roundsInBytes);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

}
