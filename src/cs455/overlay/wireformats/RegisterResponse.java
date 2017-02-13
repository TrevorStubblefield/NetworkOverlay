package cs455.overlay.wireformats;

import java.io.DataOutputStream;
import java.nio.ByteBuffer;

import static cs455.overlay.wireformats.WireFormatConstants.REGISTER_RESPONSE;

public class RegisterResponse implements Protocol {

    int MESSAGE_TYPE = REGISTER_RESPONSE;
    byte STATUS_CODE;
    String ADDITIONAL_INFO;

    public RegisterResponse(byte statusCode, String additionalInfo){
        STATUS_CODE = statusCode;
        ADDITIONAL_INFO = additionalInfo;
    }

    @Override
    public void send(DataOutputStream out){

        byte[] messageTypeInBytes = ByteBuffer.allocate(4).putInt(MESSAGE_TYPE).array();
        byte[] additionalInfoInBytes = ADDITIONAL_INFO.getBytes();

        try {
            out.write(messageTypeInBytes);
            out.write(STATUS_CODE);
            out.write(additionalInfoInBytes);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

}
