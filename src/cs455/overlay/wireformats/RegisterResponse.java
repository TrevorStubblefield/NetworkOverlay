package cs455.overlay.wireformats;

import java.io.PrintWriter;

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
    public void send(PrintWriter out){
        out.println(MESSAGE_TYPE);
        out.println(STATUS_CODE);
        out.println(ADDITIONAL_INFO);
    }

}
