package gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;

import control.ManagedParams;
import control.ParamsSet;
import serialethernet.SEChannel;
import serialethernet.SEChannelParams;

public class SEGui implements ManagedParams {

	private static JFrame mainFrame;
	private ArrayList<SEChannel> seChannels = new ArrayList<>();
	private JPanel mainPanel;
	private static final String paramsFileName = "AMDVideoControler.pss";
	
	private GuiParameters guiParameters = new GuiParameters();

	public void create() {
		mainFrame = new JFrame();
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.setLocation(500,  500);
		mainFrame.setSize(1000, 500);
		mainFrame.addWindowListener(new SEFrameListener());
		mainFrame.setLayout(new BorderLayout());
		mainFrame.setAlwaysOnTop(true);
		mainFrame.setType(javax.swing.JFrame.Type.UTILITY);
		mainFrame.setIconImage(new ImageIcon(ClassLoader
				.getSystemResource("resource/small-video-camera-icon.gif")).getImage());
//		mainFrame.getMenuBar()
		mainFrame.setTitle("AMP Video Controller Interface");
		
		
		mainPanel = new JPanel();
		mainFrame.add(BorderLayout.CENTER, mainPanel);
//		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.X_AXIS));
		mainPanel.setLayout(new GridLayout(1, 2));

		SEChannel seChannel;
		seChannel = new SEChannel(this);
		seChannel.setTCPSettings("192.168.2.230", 3811);
//		seChannel.setTCPSettings("localhost", 3811);
		seChannel.setUDPPort(8000);
		addSEChannel(seChannel);
//
//		seChannel = new SEChannel();
//		seChannel.setTCPSettings("192.168.2.231", 3811);
//		seChannel.setUDPPort(8001);
//		addSEChannel(seChannel);
	}

	private void addSEChannel(SEChannel seChannel) {
		seChannels.add(seChannel);
		mainPanel.add(seChannel.getGUIComponent());
		seChannel.openUDPPort();
	}

	public void show() {
		mainFrame.setVisible(true);
	}

	public void closeConnections() {
		for (SEChannel se:seChannels) {
			se.shutDownComms();
		}	
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		closeConnections();
	}

	private class SEFrameListener extends WindowAdapter {
		@Override
		public void windowClosing(WindowEvent e) {
			closeConnections();
			
			saveParameters();
		}

		@Override
		public void windowOpened(WindowEvent arg0) {
			loadParameters();
		}
	}

	@Override
	public ParamsSet getParams() {
		guiParameters.alwaysOnTop = mainFrame.isAlwaysOnTop();
		guiParameters.locationOnScreen = mainFrame.getLocationOnScreen();
		guiParameters.size = mainFrame.getSize();
		return guiParameters;
	}

	@Override
	public void setParams(ParamsSet paramsSet) {
		try {
			this.guiParameters = ((GuiParameters) paramsSet).clone();
			mainFrame.setAlwaysOnTop(guiParameters.alwaysOnTop);
			mainFrame.setSize(guiParameters.size);
			mainFrame.setLocation(guiParameters.locationOnScreen);
		}
		catch (ClassCastException e) {
			
		}
	}

	public void saveParameters() {
		File file = getParamsFile();
		try {
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
			oos.writeObject(this.getParams());
			ArrayList<SEChannelParams> seParams = new ArrayList<>();
			for (SEChannel se:seChannels) {
				seParams.add(se.getChannelParams());
			}
			oos.writeObject(seParams);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	public void loadParameters() {
		File file = getParamsFile();
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
			Object o = ois.readObject();
			this.setParams((ParamsSet) o);
			ArrayList<SEChannelParams> seParams;
			o = ois.readObject();
			seParams = (ArrayList<SEChannelParams>) o;
			int nChan = Math.min(seParams.size(), seChannels.size());
			for (int i = 0; i < nChan; i++) {
				seChannels.get(i).setChannelParams(seParams.get(i));
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private File getParamsFile() {
		String fileFolder = System.getProperty("user.home") + File.separator + paramsFileName;
		return new File(fileFolder);
	}

	/**
	 * @return the mainFrame
	 */
	public static JFrame getMainFrame() {
		return mainFrame;
	}

	/**
	 * @return the guiParameters
	 */
	public GuiParameters getGuiParameters() {
		return guiParameters;
	}
}
