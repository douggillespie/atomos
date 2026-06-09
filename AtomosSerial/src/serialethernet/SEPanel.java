package serialethernet;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import gui.SEGui;
import gui.SETextArea;
import gui.SETextField;

/**
 * Graphics panel to go with Shogun controller GUI. 
 * @author dg50
 *
 */
public class SEPanel {

	private SEChannel seChannel;

	private JPanel mainPanel;

	private SETextField tcpAddress;

	private SETextField tcpPort, tcpError, lastSuccess;

	private SETextArea mainText;

	private JButton stopButton, recButton, setButton;

	private JScrollPane verticalScroller;

	private int errorState;

	private Color okColour = Color.lightGray;
	private Color errorColour = Color.PINK;
	private final Color recordingColor = new Color(100, 255, 100);

	private JPanel tcpPanel, cmdPanel;

	private SEGui seGui;

	private static final int MAX_TEXTBUFF_LENGTH  = 10000;

	/**
	 * @param seChannel
	 * @param seGui 
	 */
	public SEPanel(SEChannel seChannel, SEGui seGui) {
		super();
		this.seChannel = seChannel;
		this.seGui = seGui;
		setButton = new JButton(new ImageIcon(ClassLoader
				.getSystemResource("resource/SettingsButtonSmall2.png")));
		tcpAddress = new SETextField(12);
		tcpPort = new SETextField(6);
		stopButton = new JButton(new ImageIcon(ClassLoader
				.getSystemResource("resource/playbackStop.png")));
		recButton = new JButton(new ImageIcon(ClassLoader
				.getSystemResource("resource/recordStart.png")));
				
		mainPanel = new JPanel(new BorderLayout());
		okColour = mainPanel.getBackground();
		tcpPanel = new JPanel(new BorderLayout());
		JPanel tcpTopPanel = new JPanel(new BorderLayout());
		tcpPanel.add(BorderLayout.NORTH, tcpTopPanel);
		FlowLayout flowLay = new FlowLayout();
		flowLay.setVgap(1);
		flowLay.setHgap(0);
		flowLay.setAlignOnBaseline(true);
		JPanel topRightPanel = new JPanel(flowLay);
		tcpTopPanel.add(BorderLayout.WEST, setButton);
		tcpTopPanel.add(BorderLayout.CENTER, tcpAddress);
		topRightPanel.add(tcpPort);
		topRightPanel.add(stopButton);
		topRightPanel.add(recButton);
		tcpTopPanel.add(BorderLayout.EAST, topRightPanel);
		
		
//		GridBagConstraints c = new GridBagConstraints();
//		c.fill = GridBagConstraints.BOTH;
//		c.gridx = c.gridy = 0;
//		tcpPanel.add(setButton = new JButton(new ImageIcon(ClassLoader
//				.getSystemResource("resource/SettingsButtonSmall2.png"))), c);
//		c.gridx++;
//		tcpPanel.add(tcpAddress = new SETextArea(12), c);
//		c.gridx++;
//		tcpPanel.add(tcpPort = new SETextArea(6), c);
//		c.gridx++;
//		tcpPanel.add(stopButton = new JButton(new ImageIcon(ClassLoader
//				.getSystemResource("resource/playbackStop.png"))), c);
//		c.gridx++;
//		tcpPanel.add(recButton = new JButton(new ImageIcon(ClassLoader
//				.getSystemResource("resource/recordStart.png"))), c);
//		c.gridx = 0;
//		c.gridy++;
//		c.gridwidth = 5;
//		tcpPanel.add(tcpError = new SETextArea(20), c);
		JPanel verySouth = new JPanel(new BorderLayout());
		tcpPanel.add(BorderLayout.SOUTH, verySouth);
		verySouth.add(BorderLayout.CENTER, tcpError = new SETextField(20));
		verySouth.add(BorderLayout.EAST,lastSuccess = new SETextField(10));
		mainPanel.add(BorderLayout.NORTH, tcpPanel);

		cmdPanel = new JPanel(new BorderLayout());
		//		cmdPanel.setBorder(new TitledBorder("Command history"));
		mainText = new SETextArea();
		mainText.setText("Command History\n");
		mainText.setEditable(false);
		mainText.setWrapStyleWord(true);
		mainText.setLineWrap(true);
		verticalScroller = new JScrollPane(mainText);
		cmdPanel.add(BorderLayout.CENTER, verticalScroller);
		mainPanel.add(BorderLayout.CENTER, cmdPanel);

		setButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setButton();
			}
		});
		stopButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				stopButton();
			}
		});
		recButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				recButton();
			}
		});
		stopButton.setToolTipText("Stop Recording");
		recButton.setToolTipText("Start Recording");
		setButton.setToolTipText("Settings");
		lastSuccess.setToolTipText("Last sucessful command");
		int b = 2;
		stopButton.setBorder(new EmptyBorder(b,b,b,b));
		recButton.setBorder(new EmptyBorder(b,b,b,b));
		setButton.setBorder(new EmptyBorder(b,b,b,b));

		updateStateData();
		seChannel.addEventListener(new SEListener());

		//		for (int i = 0; i < 100; i++) {
		//			newSEMessage(String.format("Append another line %d\n", i));
		//		}
	}

	protected void setButton() {
		if (seChannel.settingsDialog()) {
			updateStateData();
		}
	}

	protected void stopButton() {
		seChannel.interpretCommandData("2000");
	}
	private void recButton() {
		seChannel.interpretCommandData("2002");
	}

	public JComponent getComponent() {
		return mainPanel;
	}

	private class SEListener implements SEEventListener {

		@Override
		public void stateChange() {
			updateStateData();
		}

		@Override
		public void newMessage(String message) {
			newSEMessage(message);
		}

		@Override
		public void setErrorState(int errorState, String errorString) {
			newErrorState(errorState, errorString);
		}

	}

	public void updateStateData() {
		SEChannelParams cp = seChannel.getChannelParams();
		tcpAddress.setText(cp.getIpAddr());
		tcpPort.setText("Port " + cp.getIpPort());
		//		tcp

	}

	public void setBackground(int errorCol) {
		Color bgCol = okColour;
		Color txtColor = Color.WHITE;
		Color recColor = txtColor;
		switch (errorCol) {
		case 0:
		case 1:
			break;
		case 2:
//			txtColor = recordingColor;
			recColor = recordingColor;
			break;
		default:
			bgCol = txtColor = recColor = errorColour;
		}
		mainPanel.setBackground(bgCol);
		tcpPanel.setBackground(bgCol);
		cmdPanel.setBackground(bgCol);
		mainText.setBackground(txtColor);
		tcpError.setBackground(txtColor);
		lastSuccess.setBackground(recColor);
	}
	public void newErrorState(int errorState, String errorString) {
		this.errorState= errorState;
		if (errorString == null) errorString = "";
		switch(errorState) {
		case 0:
		case 1:
		case 2:
			setLastSuccess(errorState, errorString);
			break;
		default:
			errorString = "Error " + errorString;
		}
		setBackground(errorState);
		tcpError.setText(errorString);
		if (errorState > SEEventListener.ERROR_RECORDING) {
			newSEMessage(errorString);
			if (seGui.getGuiParameters().errorBeep) {
				Toolkit tk = Toolkit.getDefaultToolkit();
				tk.beep();
			}
		}
	}

	private void setLastSuccess(int errorState2, String errorString) {
		lastSuccess.setText(errorString);
		lastSuccess.setToolTipText(String.format("Last command at %s", DateString.getTimeString(true)));
	}

	public void newSEMessage(String message) {
		mainText.append(message);
		mainText.setSelectionStart(mainText.getText().length());
		// now check it's length ...
		if (mainText.getDocument().getLength() > MAX_TEXTBUFF_LENGTH) {
			String txt = mainText.getText();
			int minR = mainText.getDocument().getLength() - MAX_TEXTBUFF_LENGTH;
			int r = 0;
			while (r < minR) {
				r = txt.indexOf('\n', r+1);
				if (r < 0) {
					break;
				}
			}
			if (r > 0) {
				txt = txt.substring(r+1);
				mainText.setText(txt);
			}
		}
		//		JScrollBar vS = verticalScroller.getVerticalScrollBar();
		//		vS.setValue(vS.getMinimum());
	}
}
