package Serial;

/**
 * Serial port test. Do not use. 
 * @author dg50
 *
 */
public class AtomosSerial {

	public static void main(String[] args) {
		System.out.println("Hello");

		writePropertyString("java.home");
		writePropertyString("java.name");
		writePropertyString("java.version");
		writePropertyString("java.vendor");
		writePropertyString("java.vm.version");
		writePropertyString("java.vm.name");
		//		writePropertyString("java.specification.name");
		writePropertyString("os.name");
		writePropertyString("os.arch");
		writePropertyString("os.version");
		writePropertyString("java.library.path");
		
		SerialTest serialTest = new SerialTest();
		serialTest.run();

	}

	static private void writePropertyString(String key) {
		String property = System.getProperty(key);
		if (property == null) {
			System.out.println(String.format("%s: No such property", key));
		}
		else {
			System.out.println(String.format("%s %s", key, property));
		}
	}
}
