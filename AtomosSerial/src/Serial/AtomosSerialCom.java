package Serial;

import java.util.ArrayList;
import java.util.Enumeration;

import gnu.io.CommPortIdentifier;

public class AtomosSerialCom {

	private static ArrayList<CommPortIdentifier> comPortList;

	public static ArrayList<CommPortIdentifier> getPortArrayList(){
		if (comPortList == null) {
			comPortList = new ArrayList<CommPortIdentifier>();
			Enumeration pList = CommPortIdentifier.getPortIdentifiers(  );
			while (pList.hasMoreElements(  )) {
				CommPortIdentifier commPortId = (CommPortIdentifier)pList.nextElement(  );
//				if (!commPortId.isCurrentlyOwned()){
					if (commPortId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
						comPortList.add(commPortId);
					} 
//				}
			}
		}
		return comPortList;
	}

	public static CommPortIdentifier findPortIdentifier(String portName) {
		ArrayList<CommPortIdentifier> portList = getPortArrayList();
		if (portList == null) {
			return null;
		}
		for (CommPortIdentifier id:portList) {
			if (id.getName().equals(portName)) {
				return id;
			}
		}
		return null;
	}
}
