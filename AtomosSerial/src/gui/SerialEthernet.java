package gui;

public class SerialEthernet {

	/**
	 * THis is the one - the program that we actually use !
	 * @param args
	 */
	public static void main(String[] args) {
		
		SEGui seGui = new SEGui();
		seGui.create();

		seGui.show();
	}

}
