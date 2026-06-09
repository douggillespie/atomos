package Serial;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.TooManyListenersException;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

public class SerialTest {

	private int baud = 38400;
	private String portName = "COM8";
	private CommPortIdentifier portId;
	private SerialPort serialPort;
	private BufferedReader inputStream;
	private OutputStream outputStream;
	private InputStream rawInputStream;

	/**
	 * 
	 */
	public SerialTest() {
		super();
	}

	public void run() {
		if (!openSerial()) {
			System.out.println("Unable to open serial port " + portName);
		}


		byte[] data = {0x20, 0, 0x20};
		
		data = new String("CRAT0007204Vtr1\nCMDS00042002\n").getBytes();
		data = new String("REC\n").getBytes();
		writeData(data);


		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		byte[] inChars = null;
		int avail = 0;
		try {
			avail = rawInputStream.available();
			if (avail > 0) {
				inChars = new byte[avail];
				rawInputStream.read(inChars);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (inChars != null) {
			System.out.printf("Data read back from serial port % d bytes \"%s\"\n", avail, makeHextArray(inChars));
		}
		else {
			System.out.println("No data returned from serial port");
		}

		closeSerial();
	}
	
//	private boolean writeString(String data) {
//		outputStream.w
//	}

	private boolean writeData(byte[] data) {
		String cr = "\r\n";
		byte[] crbytes = cr.getBytes();

		System.out.printf("Wrting data to Com port \"%s\"\n", makeHextArray(data));
		try {
			outputStream.write(data);
			for (int i = 0; i < 1; i++) {
				outputStream.write(crbytes);
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return false;
		}

		return true;
	}

	String makeHextArray(byte[] data) {
		if (data == null || data.length == 0) {
			return "";
		}
		String str = String.format("0X:%02X", data[0]);
		for (int i = 1; i < data.length; i++) {
			str += String.format(",%02X", data[i]);
		}
		return str;
	}

	private boolean  openSerial() {
		portId = AtomosSerialCom.findPortIdentifier(portName);

		String currentOwner = portId.getCurrentOwner();
		if (currentOwner != null) {
			String str = String.format("Serial Port %s is already being used by %s and cannot be opened by %s",
					portId.getName(), currentOwner, portName);
			str += "\nSelect a different COM port for one of these modules.";
			System.out.println(str);
			return false;

		}
		try {
			serialPort = (SerialPort) portId.open("AtomosTest", 2000);
		} catch (PortInUseException e) {
			e.printStackTrace();
			return false;
		}

		try {
			rawInputStream = serialPort.getInputStream();
			inputStream = new BufferedReader(new InputStreamReader(rawInputStream, "ASCII"), 500);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		try {
			outputStream = serialPort.getOutputStream();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			serialPort.addEventListener(new SerialTestListener(this));
		} catch (TooManyListenersException e1) {
			e1.printStackTrace();
		}


		try {
			// set port parameters
			serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
			serialPort.setSerialPortParams(baud, SerialPort.DATABITS_8, 
					SerialPort.STOPBITS_1, 
					SerialPort.PARITY_ODD);
		} catch (UnsupportedCommOperationException e) {
			System.out.println("UnsupportedCommOperationException:"+e);
		}

		return true;
	}

	private void closeSerial() {
		if (serialPort == null) {
			return;
		}
		serialPort.close();
	}

	/**
	 * @return the inputStream
	 */
	public BufferedReader getInputStream() {
		return inputStream;
	}
}