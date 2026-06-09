package serialethernet;

public interface SEEventListener {
	
	public static final int ERROR_NONE = 0;
	public static final int ERROR_IDLE = 1;
	public static final int ERROR_RECORDING = 2;
	public static final int ERROR_NOCOMMS = 3;
	public static final int ERROR_NOACK = 4;
	public static final int ERROR_WAITING = 5;
	public static final int ERROR_UNKNOWN = 6;
	
	public void stateChange();
	
	public void newMessage(String message);
	
	public void setErrorState(int errorState, String errorString);
	
}
