package cs455.overlay.wireformats;

public class WireFormatConstants {

    private WireFormatConstants(){}

    public static final int REGISTER_REQUEST          = 1;
    public static final int DEREGISTER_REQUEST        = 2;
    public static final int REGISTER_RESPONSE         = 3;
    public static final int DEREGISTER_RESPONSE       = 4;
    public static final int SETUP_OVERLAY             = 5;
    public static final int MESSAGING_NODES_LIST      = 6;
    public static final int SEND_OVERLAY_LINK_WEIGHTS = 7;
    public static final int LINK_WEIGHTS              = 8;
    public static final int TASK_INITIATE             = 9;
    public static final int TASK_COMPLETE             = 10;
    public static final int TRAFFIC_SUMMARY           = 11;
    public static final int PULL_TRAFFIC_SUMMARY      = 12;
    public static final int TRAFFIC_MESSAGE           = 13;
    public static final int CONNECTION_REQUEST        = 14;

    public static final int EXIT_MESSAGE              = -1;

}
