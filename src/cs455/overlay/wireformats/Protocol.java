package cs455.overlay.wireformats;

import java.io.DataOutputStream;

public interface Protocol {

    public void send(DataOutputStream out);
}
