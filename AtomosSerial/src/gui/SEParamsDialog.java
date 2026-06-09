package gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import serialethernet.SEChannelParams;

public class SEParamsDialog extends AMPDialog {

	private static SEParamsDialog singleInstance;
	private SEChannelParams seParams;
	private JTextField udpPort;
	private JTextField tcpAddress;
	private JTextField ampTCPPort;
	private JTextField tcpWaitTimeout;
	private JTextField tcpRetries;
	private JCheckBox beep;
	private GuiParameters guiParams;
	
	private SEParamsDialog(Window parentFrame) {
		super(parentFrame, "Control", false);
		super.setType(javax.swing.JFrame.Type.UTILITY);
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		
		GridBagLayout gb;
		JPanel udpInPanel = new JPanel(gb = new GridBagLayout());
		AMPGridbagConstriants c = new AMPGridbagConstriants();
		udpInPanel.setBorder(new TitledBorder("UDP from Logger"));
		udpInPanel.add(new JLabel("UDP Port ", JLabel.RIGHT), c);
		c.gridx++;
		udpInPanel.add(udpPort = new JTextField(4), c);
		
		JPanel tcpPanel = new JPanel(new GridBagLayout());
		c = new AMPGridbagConstriants();
		c.fill = GridBagConstraints.NONE;
		tcpPanel.setBorder(new TitledBorder("AMP Control"));
		tcpPanel.add(new JLabel("TCP Address ", JLabel.RIGHT), c);
		c.gridx++;
		tcpPanel.add(tcpAddress = new JTextField(10), c);
		c.gridy ++;
		c.gridx = 0;
		tcpPanel.add(new JLabel("TCP Port ", JLabel.RIGHT), c);
		c.gridx++;
		tcpPanel.add(ampTCPPort = new JTextField(4), c);
		c.gridy ++;
		c.gridx = 0;
		tcpPanel.add(new JLabel("Wait time ", JLabel.RIGHT), c);
		c.gridx++;
		tcpPanel.add(tcpWaitTimeout = new JTextField(4), c);
		c.gridx++;
		tcpPanel.add(new JLabel(" ms"));
		c.gridy ++;
		c.gridx = 0;
		tcpPanel.add(new JLabel("TCP Retries ", JLabel.RIGHT), c);
		c.gridx++;
		tcpPanel.add(tcpRetries = new JTextField(4), c);

		JPanel ctrlPanel = new JPanel(new GridBagLayout());
		c = new AMPGridbagConstriants();
		ctrlPanel.setBorder(new TitledBorder("General"));
		ctrlPanel.add(beep = new JCheckBox("Beep on Errors"), c);
		
		mainPanel.add(udpInPanel);
		mainPanel.add(tcpPanel);
		mainPanel.add(ctrlPanel);
		setDialogComponent(mainPanel);
	}

	public static SEChannelParams showDialog(Window parent, SEChannelParams seParams, GuiParameters guiParams) {
		if (singleInstance == null || singleInstance.getOwner() != parent) {
			singleInstance = new SEParamsDialog(parent);
		}
		singleInstance.seParams = seParams.clone();
		singleInstance.guiParams = guiParams;
		singleInstance.setParams();
		singleInstance.setVisible(true);
		return singleInstance.seParams;
	}
	
	private void setParams() {
		udpPort.setText(String.format("%d", seParams.getUdpPort()));
		tcpAddress.setText(seParams.getIpAddr());
		ampTCPPort.setText(String.format("%d", seParams.getIpPort()));
		tcpWaitTimeout.setText(String.format("%d", guiParams.tcpReadTimeout));
		tcpRetries.setText(String.format("%d", guiParams.repeatTriesOnError));
		beep.setSelected(guiParams.errorBeep);
	}

	@Override
	public boolean getParams() {
		try {
			int u = Integer.valueOf(udpPort.getText());
			seParams.setUdpPort(u);
			u = Integer.valueOf(ampTCPPort.getText());
			seParams.setIpPort(u);
		}
		catch (NumberFormatException e) {
			return showWarning("Invalid UDP Port");
		}
		seParams.setIpAddr(tcpAddress.getText());
		try {
			int r = Integer.valueOf(tcpRetries.getText());
			guiParams.repeatTriesOnError = r;
		}
		catch (NumberFormatException e) {
			return showWarning("Invalid retries number (must be integer)");
		}
		try {
			int t = Integer.valueOf(tcpWaitTimeout.getText());
			guiParams.tcpReadTimeout = t;
		}
		catch (NumberFormatException e) {
			return showWarning("Invalid rtcp timeout (must be integer)");
		}
		guiParams.errorBeep = beep.isSelected();
		return true;
	}

	@Override
	public void cancelButtonPressed() {
		seParams = null;
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}

}
