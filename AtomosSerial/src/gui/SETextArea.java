package gui;

import javax.swing.BorderFactory;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class SETextArea extends JTextArea {

	private static final long serialVersionUID = 1L;

	/**
	 * @param arg0
	 */
	public SETextArea(int arg0) {
		super(1, arg0);
		setBorder(BorderFactory.createBevelBorder(1));
//		setEditable(false);
	}

	public SETextArea(int rows, int cols) {
		super(rows, cols);
		setBorder(BorderFactory.createBevelBorder(1));
	}

	public SETextArea() {
		setBorder(BorderFactory.createBevelBorder(1));
	}

//	/**
//	 * @param arg0
//	 */
//	public SETextArea(String arg0) {
//		super(arg0);
////		setEditable(false);
//	}

}
