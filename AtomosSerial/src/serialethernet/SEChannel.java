package serialethernet;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TooManyListenersException;

import javax.swing.JComponent;
import javax.swing.Timer;

import Serial.AtomosSerialCom;
import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;
import gui.SEGui;
import gui.SEParamsDialog;

/**
 * Class to handle a connection between 1 serial port and one tcpip port. 
 * @author dg50
 *
 */
public class SEChannel {


	private SEChannelParams channelParams = new SEChannelParams();
	private SerialPort serialPort;
	private InputStream rawInputStream;
	private BufferedReader serialInputStream;
	private OutputStream serialOutputStream;
	private Socket tcpSock;
	private InputStream tcpInputStream;
	private DataOutputStream tcpOutputStream;
	private boolean amdChannelOpen;
	private String chanOpenCmd = "CRAT0007204Vtr1\n";
	private String chanReaqCmd = "REAQ0007204Vtr1\n";
	private String chanCloseCmd = "STOP0000\n";
	private enum TCPAnswer  {ACK, NACK, ERROR, UNKNOWN, NOANS};
	//	private static final int ACK = 1;
	//	private static final int NACK = 2;
	//	private static final int WTF = -1;
	private static final byte[] ack = {49, 48, 48, 49}; //1001
	//	private static final byte[] nack = {49, 50, 50, 51}; //1221
	private static final byte[] nack = {49, 49, 49, 49}; //1111
	private static final byte[] err = {50, 50, 50, 50}; //2222
	private static final long MINTCPWAITTIME = 500;

	private SEPanel sePanel;
	private ArrayList<SEEventListener> eventListeners = new ArrayList<>();
	private long lastDataSendTime;

	private boolean simRecStat = false;
	private SEGui seGui;

	public SEChannel(SEGui seGui) {
		this.seGui = seGui;
		//		serialRead = new Timer(1000, new TimedSerialRead())
		//		Timer simtimer = new Timer(5000, new SimTimerAction());
		//		simtimer.start();
	}

	public JComponent getGUIComponent() {
		if (sePanel == null) {
			sePanel = new SEPanel(this, seGui);
		}
		return sePanel.getComponent();
	}

	private class SimTimerAction implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			if (simRecStat) {
				interpretCommandData("2000");
				simRecStat = false;
			}
			else {
				interpretCommandData("2002");
				simRecStat = true;
			}
		}

	}

	/* (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() throws Throwable {
		closeSerialPort();
	}

	/**
	 * Set the TCP IP address and port number. 
	 * @param tcpName
	 * @param portId
	 */
	public void setTCPSettings(String tcpName, int portId) {
		channelParams.setIpAddr(tcpName);
		channelParams.setIpPort(portId);
	}

	/**
	 * Set the udp port for receiving external commands. 
	 * @param udpPort
	 */
	public void setUDPPort(int udpPort) {
		channelParams.setUdpPort(udpPort);
	}

	/**
	 * Set the serial port name and baud rate. 
	 * @param comName
	 * @param baudRate
	 */
	public void setSerialSettings(String comName, int baudRate) {
		channelParams.setComPortName(comName);
		channelParams.setSerialBaudRate(baudRate);
	}

	/**
	 * @return the channelParams
	 */
	public SEChannelParams getChannelParams() {
		return channelParams;
	}

	/**
	 * @param channelParams the channelParams to set
	 */
	public void setChannelParams(SEChannelParams channelParams) {
		this.channelParams = channelParams;
	}

	private class SerialListener implements SerialPortEventListener {

		@Override
		public void serialEvent(SerialPortEvent serialEvent) {
			switch(serialEvent.getEventType()) {
			case SerialPortEvent.DATA_AVAILABLE:
				// we get here if data has been received
				try {
					String line = serialInputStream.readLine();
					System.out.println("Data read from Serial port \"" + line + "\"");
					String retString = interpretCommandData(line);
					if (retString != null) {
						serialOutputStream.write(retString.getBytes());
					}

				} catch (IOException e) {			
					/*
					 * Don't print this exception since it's thrown all the time
					 * when there aren't new strings or when strings are coming through slowely. 
					 */
					System.out.println("IO Exception message: 2" + e.getMessage());
				}
				break;
			default:
				System.out.println("Serial event " + serialEvent.getEventType());
			}

		}

	}

	public boolean openSerialPort() {

		CommPortIdentifier portId = AtomosSerialCom.findPortIdentifier(channelParams.getComPortName());

		String currentOwner = portId.getCurrentOwner();
		if (currentOwner != null) {
			String str = String.format("Serial Port %s is already being used by %s and cannot be opened by %s",
					portId.getName(), currentOwner, channelParams.getComPortName());
			str += "\nSelect a different COM port for one of these modules.";
			System.out.println(str);
			return false;

		}
		try {
			serialPort = (SerialPort) portId.open("AtomosTest", 0);
		} catch (PortInUseException e) {
			e.printStackTrace();
			return false;
		}

		try {
			rawInputStream = serialPort.getInputStream();
			serialInputStream = new BufferedReader(new InputStreamReader(rawInputStream), 500);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		try {
			serialOutputStream = serialPort.getOutputStream();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			serialPort.addEventListener(new SerialListener());
		} catch (TooManyListenersException e1) {
			e1.printStackTrace();
		}

		serialPort.notifyOnDataAvailable(true);


		try {
			// set port parameters
			serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
			serialPort.setSerialPortParams(channelParams.getSerialBaudRate(), SerialPort.DATABITS_8, 
					SerialPort.STOPBITS_1, 
					SerialPort.PARITY_NONE);
		} catch (UnsupportedCommOperationException e) {
			System.out.println("UnsupportedCommOperationException:"+e);
		}



		return true;
	}

	/**
	 * Use data recieved over either the serial line or UDP 
	 * and provide a string to send back again. 
	 * @param line
	 * @return
	 */
	public String interpretCommandData(String line) {
		String amdCommand = formAMDCommand(line);
		byte[] retDat = sendTCPData(amdCommand, true);
		TCPAnswer retAns = interpretReadData(retDat);
		String ret = DateString.getTimeString(true) + ": " + amdCommand + " " + retAns + "\n";
		return ret;
	}


	public String formAMDCommand(String line) {
		// need to wrap up the command .... "CMDS00042000\n"
		line = line.trim();
		String amdString = String.format("CMDS%04d%s\n", line.length(), line);
		return amdString;
	}

	public void closeSerialPort() {
		if (serialPort == null) {
			return;
		}
		serialPort.close();
		serialPort = null;
	}

	public synchronized boolean openTCPPort() {
		closeTCPPort();
		try {
			tcpSock = new Socket(channelParams.getIpAddr(), channelParams.getIpPort());
			// create a data output stream. 
			tcpOutputStream = new DataOutputStream(tcpSock.getOutputStream());
			// and get the input stream
			tcpInputStream = tcpSock.getInputStream();

			reportMsg("TCPIP port %d open on %s, connectionStatus = %s\n", channelParams.getIpPort(), channelParams.getIpAddr(), tcpSock.isConnected());
		}
		catch (IOException e) {
			reportMsg("Unable to open TCPIP socket to AMD Controller at "  + channelParams.getIpAddr() + " " + e.getMessage() + "\n");
			return false;
		}

		openAmdChannel();

		return true;
	}

	public synchronized void closeTCPPort() {
		if (tcpSock == null) {
			return;
		}
		//		if (amdChannelOpen) {
		closeAmdChannel();
		//		}
		amdChannelOpen = false;

		try {
			tcpSock.close();
			tcpInputStream.close();
			tcpOutputStream.close();
			System.out.println("TCP socket closed");
		} catch (IOException e) {
			e.printStackTrace();
		}
		tcpSock = null;
		tcpInputStream = null;
		tcpOutputStream = null;
	}

	/**
	 * open a UDP port for incoming data from Logger
	 * launch in a new thread so ti can wait in the background. 
	 */
	public void openUDPPort() {
		closeUDPPort();

		UDPThread udpThread = new UDPThread();
		new Thread(udpThread).start();
	}

	/**
	 * Close and cleanup the UDP port. 
	 */
	public void closeUDPPort() {

	}

	private class UDPThread implements Runnable {

		private volatile boolean keepWaiting;

		@Override
		public void run() {
			DatagramSocket udpSocket = null;
			byte[] buffer = new byte[150];

			try {
				udpSocket = new DatagramSocket(channelParams.getUdpPort());
				udpSocket.setSoTimeout(1000); 
			} catch (SocketException e) {
				e.printStackTrace();
			}

			// Create empty Datagram Packet
			DatagramPacket udpPacket = new DatagramPacket(buffer, buffer.length);
			keepWaiting = true;
			while (keepWaiting) {
				try {
					udpSocket.receive(udpPacket);
				} catch (SocketTimeoutException e) {
					continue;
				} catch (IOException e) {
					e.printStackTrace();
				}
				if (udpPacket.getData() != null) {
					String str = new String(udpPacket.getData());
					String retStr = interpretCommandData(str);
					System.out.println("Return string " + retStr);
					if (retStr != null) {
						try {
							InetAddress address = udpPacket.getAddress();
							int port = udpPacket.getPort();
							udpSocket.send(new DatagramPacket(retStr.getBytes(), retStr.length(), address, port));
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}

	}

	/**
	 * Use t
	 * @param data
	 * @return
	 */
	private synchronized byte[] sendTCPData(String data, boolean openAmdCon) {

		boolean ok;
		if (tcpSock == null) {
			ok  = openTCPPort();
			if (!ok) {
				reportError(SEEventListener.ERROR_NOCOMMS, "NO TCP Comms to Atomos. Check connections or Reset it!");
				return null;
			}
		}
		if (amdChannelOpen == false && openAmdCon) {
			ok = openAmdChannel();
			if (!ok) {
				//				return null;
			}
		}
		// always wait a minimum of some time before actually sending data
		// to the TCP port or it locks up. 
		long canTime = lastDataSendTime + MINTCPWAITTIME;
		if (lastDataSendTime > System.currentTimeMillis()) {
			// allow for someone setting the PC clock backwards in time !
			canTime = System.currentTimeMillis() + MINTCPWAITTIME;
		}
		if (System.currentTimeMillis() < canTime) {
			reportError(SEEventListener.ERROR_WAITING, "Waiting to send TCP Data\n");
			System.out.printf("Waiting %d millis to send data\n", canTime-System.currentTimeMillis());
			while (System.currentTimeMillis() < canTime) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		int nFails = 0;
		int maxTries = Math.max(1, seGui.getGuiParameters().repeatTriesOnError+1);
		byte[] readData = null;
		while (nFails < maxTries) {
			if (nFails > 0) {
				reportMsg(String.format("Attempt %d of %d writing TCP command %s", nFails+1, maxTries, data));
			}
			try {
				reportMsg(DateString.getTimeString(false) + " Writing: " + data);
				tcpOutputStream.write(data.getBytes());
				lastDataSendTime = System.currentTimeMillis();
			} catch (Exception e) {
				//			e.printStackTrace();
				String serStr = "TCP write error: " + e.getMessage() + "\n";
				reportError(SEEventListener.ERROR_NOCOMMS, serStr);

				// close the socket if there is a problem with it. 
				closeTCPPort();
				return null;
			}

			// try to read back data. AT least four bytes should always be available in the returned data.  
			readData = readTCPData(seGui.getGuiParameters().tcpReadTimeout);

			if (interpretReadData(readData) != TCPAnswer.ACK) {
				//			closeTCPPort();
				reportError(SEEventListener.ERROR_NOACK, "Invalid response\n");
				nFails++;
			}
			else {
				int state = getSucessState(data);
				String hc = getHumanCommand(state);
				reportError(state, hc);
				break; //all OK so can get out of this try loop. 
			}
			//closeTCPPort();

		}
		return readData;

	}

	private void reportToSerial(String str) {
		if (serialInputStream != null) {
			try {
				serialOutputStream.write(str.getBytes());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}
	private synchronized byte[] readTCPData(int timeoutMillis) {
		long then, now;
		try {
			then = System.currentTimeMillis();
			while (tcpInputStream.available() < 4) {
				if (System.currentTimeMillis()-then > timeoutMillis) {
					break;
				}
				Thread.sleep(100);
			}
			now = System.currentTimeMillis();
			int bytesAvail = tcpInputStream.available();

			String outString = String.format("%d bytes returned from AMD after %d millis :", bytesAvail, now-then);
			byte[] data = null;
			if (bytesAvail > 0) {
				data = new byte[bytesAvail];
				tcpInputStream.read(data);
				//				for (int i = 0; i < bytesAvail; i++) {
				//					outString += String.format("%02x", data[i]);
				//				}
				outString += String.format("  \"%s\"\n", new String(data));
				reportMsg(outString);
			}
			return data;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} catch (InterruptedException e) { // catch for sleep timer. 
			e.printStackTrace();
			return null;
		}
	}

	public boolean openAmdChannel() {
		closeAmdChannel();

		amdChannelOpen = false;

		if (tcpOutputStream != null) {
			try {
				tcpOutputStream.write(chanOpenCmd.getBytes());
				lastDataSendTime = System.currentTimeMillis();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// try to read back data. 
		byte[] readData = readTCPData(seGui.getGuiParameters().tcpReadTimeout);
		TCPAnswer ans = interpretReadData(readData);

		amdChannelOpen = ans == TCPAnswer.ACK;
		System.out.println("AMD Channel Open returned " + ans);
		return amdChannelOpen;
		//		return true;
	}

	public boolean closeAmdChannel() {
		boolean error = false;
		if (tcpOutputStream != null) {
			try {
				System.out.printf("Writing to tcp socket: " + chanCloseCmd);
				tcpOutputStream.write(chanCloseCmd.getBytes());
			} catch (IOException e) {
				System.out.println("Error writing to close channel: " + e.getMessage() );
				error = true;
			}
		}
		// quick check to see if anything read back ...
		amdChannelOpen = false;

		// try to read back data. 
		if (!error) {
			byte[] readData = readTCPData(seGui.getGuiParameters().tcpReadTimeout);
			if (readData != null) {
				System.out.println("Close amd returned \"" + new String(readData) + "\"");
			}
		}
		return true;

	}

	/**
	 * REturns an answer ACK / NOANS, etc. 
	 * @param data
	 * @return
	 */
	private TCPAnswer interpretReadData(byte[] data) {

		//		if (data != null) {
		//			System.out.printf("Returned data %s as string = %s\n", new String(data), returnedBytesToString(data));
		//		}

		if (data == null) {
			return TCPAnswer.NOANS;
		}
		else if (isSame(data, ack)) {
			return TCPAnswer.ACK;
		}
		else if (isSame(data, nack)) {
			return TCPAnswer.NACK;
		}
		else if (isSame(data, err)) {
			return TCPAnswer.ERROR;
		}
		System.out.printf("Unknown tcp return:");
		for (int i = 0; i < data.length; i++) {
			System.out.printf("%d", data[i]);
		}
		System.out.printf(" \"%s\"\n", new String(data));
		return TCPAnswer.UNKNOWN;
	}

	/**
	 * Convert the really weird returned data into 
	 * an ascii string. Basically it's hex ascii. 
	 * @return
	 */
	private String returnedBytesToString(byte[] data) {
		//		if (data == null) return null;
		//		int nWord = data.length/2;
		//		String ret = "";
		//		
		//		char ch = 0;
		//		for (int i = 0; i < nWord; i++) {
		//			String ss = "0x" + new String(data, i*2, 2);
		//			try {
		//				ch = (char) Integer.parseUnsignedInt(ss);
		//			}
		//			catch (NumberFormatException e) {
		//				System.out.println("Unable to parse string " + ss + " " + e.getMessage());
		//				continue;
		//			}
		//			ret += ch;
		//		}
		//		
		//		return ret;
		return new String(data);
	}

	private boolean isSame(byte[] data, byte[] tst) {
		if (data == null) {
			return false;
		}
		if (data.length < tst.length) {
			return false;
		}
		for (int i = 0; i < tst.length; i++) {
			if (data[i] != tst[i]) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Shut everythigndown. 
	 */
	public void shutDownComms() {
		closeUDPPort();
		closeTCPPort();
		closeSerialPort();
	}

	public void addEventListener(SEEventListener eventListener) {
		eventListeners.add(eventListener);
	}

	private void reportError(int errorState, String errorString) {
		for (SEEventListener sel:eventListeners) {
			sel.setErrorState(errorState, errorString);
		}

	}

	private String getHumanCommand(int state) {
		
		switch (state) {
		case SEEventListener.ERROR_IDLE:
			return "Idle";
		case SEEventListener.ERROR_RECORDING:
			return "Recording";
		}
		return "Unknown State";
	}

	private int getSucessState(String tcpCommand) {
		if (tcpCommand == null) {
			return SEEventListener.ERROR_UNKNOWN;
		}
		if (tcpCommand.startsWith("CMDS00042000")) {
			return SEEventListener.ERROR_IDLE;
		}
		else if (tcpCommand.startsWith("CMDS00042002")) {
			return SEEventListener.ERROR_RECORDING;
		}
		return SEEventListener.ERROR_UNKNOWN;
	}

	private void reportMsg(String format, Object... arguments) {
		String singleString = String.format(format, arguments);
		for (SEEventListener sel:eventListeners) {
			sel.newMessage(singleString);
		}
	}

	/*
	 * Show the settings dialog for this panel. 
	 */
	public boolean settingsDialog() {
		SEChannelParams newParams = SEParamsDialog.showDialog(SEGui.getMainFrame(), channelParams, seGui.getGuiParameters());
		if (newParams != null) {
			channelParams = newParams;
		}
		return (newParams != null);
	}
}
