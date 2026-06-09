package Serial;

import java.io.BufferedReader;
import java.io.IOException;

import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;;

public class SerialTestListener implements SerialPortEventListener {

	private SerialTest serialTest;
	private BufferedReader inputStream;

	public SerialTestListener(SerialTest serialTest) {
		this.serialTest = serialTest;
		this.inputStream = serialTest.getInputStream();
	}

	@Override
	public void serialEvent(SerialPortEvent portEvent) {		

		switch (portEvent.getEventType()) {
		case SerialPortEvent.DATA_AVAILABLE:
			// we get here if data has been received
			try {
				String line = inputStream.readLine();
				System.out.println("Data read from Atomos \"" + line + "\"");
			} catch (IOException e) {			
				/*
				 * Don't print this exception since it's thrown all the time
				 * when there aren't new strings or when strings are coming through slowely. 
				 */
				//			System.out.println(e);
			}

			break;
		default:
			System.out.println("Serial event " + portEvent);
		}

	}

}
