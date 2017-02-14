package cs455.overlay.wireformats;

import java.io.DataOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static cs455.overlay.wireformats.WireFormatConstants.LINK_WEIGHTS;

public class LinkWeights implements Protocol {

    int MESSAGE_TYPE = LINK_WEIGHTS;
    int NUMBER_OF_LINKS;
    List<String> LINK_INFO_LIST;

    public LinkWeights(int numberOfLinks, ArrayList<String> linkInfoArrayList){
        NUMBER_OF_LINKS = numberOfLinks;
        LINK_INFO_LIST = linkInfoArrayList;
    }

    @Override
    public void send(DataOutputStream out){

        byte[] messageTypeInBytes = ByteBuffer.allocate(4).putInt(MESSAGE_TYPE).array();
        byte[] numberLinksInBytes = ByteBuffer.allocate(4).putInt(NUMBER_OF_LINKS).array();

        try {
            out.write(messageTypeInBytes);
            out.write(numberLinksInBytes);

            for (String linkInfo : LINK_INFO_LIST) {
                out.write(linkInfo.getBytes());
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }

    }
}
