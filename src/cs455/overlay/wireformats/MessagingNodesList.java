package cs455.overlay.wireformats;

import cs455.overlay.node.MessagingNode;

import java.io.DataOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static cs455.overlay.wireformats.WireFormatConstants.MESSAGING_NODES_LIST;

public class MessagingNodesList implements Protocol {

    int MESSAGE_TYPE = MESSAGING_NODES_LIST;
    int NUMBER_OF_MESSAGING_NODES;
    List<MessagingNode> MESSAGING_NODE_LIST;

    public MessagingNodesList(int numberOfMessagingNodes, ArrayList<MessagingNode> messagingNodeArrayList){
        NUMBER_OF_MESSAGING_NODES = numberOfMessagingNodes;
        MESSAGING_NODE_LIST = messagingNodeArrayList;
    }

    @Override
    public void send(DataOutputStream out){

        byte[] messageTypeInBytes = ByteBuffer.allocate(4).putInt(MESSAGE_TYPE).array();
        byte[] numberNodesInBytes = ByteBuffer.allocate(4).putInt(NUMBER_OF_MESSAGING_NODES).array();

        try {
            out.write(messageTypeInBytes);
            out.write(numberNodesInBytes);

            for (MessagingNode messagingNode : MESSAGING_NODE_LIST) {
                String connectionInfo = messagingNode.hostName + ":" + messagingNode.port;
                out.write(connectionInfo.getBytes());
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }

    }
}
