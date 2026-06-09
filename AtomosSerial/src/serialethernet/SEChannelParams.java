package serialethernet;

import java.io.Serializable;

public class SEChannelParams implements Serializable, Cloneable {

	private static final long serialVersionUID = 1L;
	
	private String ipAddr = "192.168.2.230";
	private int ipPort = 3811;
	
	private String comPortName = "COM7";
	private int serialBaudRate = 19200;
	
	private int udpPort = 8000;
	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public SEChannelParams clone() {
		try {
			return (SEChannelParams) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}
	/**
	 * @return the ipAddr
	 */
	public String getIpAddr() {
		return ipAddr;
	}
	/**
	 * @param ipAddr the ipAddr to set
	 */
	public void setIpAddr(String ipAddr) {
		this.ipAddr = ipAddr;
	}
	/**
	 * @return the ipPort
	 */
	public int getIpPort() {
		return ipPort;
	}
	/**
	 * @param ipPort the ipPort to set
	 */
	public void setIpPort(int ipPort) {
		this.ipPort = ipPort;
	}
	/**
	 * @return the comPortName
	 */
	public String getComPortName() {
		return comPortName;
	}
	/**
	 * @param comPortName the comPortName to set
	 */
	public void setComPortName(String comPortName) {
		this.comPortName = comPortName;
	}
	/**
	 * @return the serialBaudRate
	 */
	public int getSerialBaudRate() {
		return serialBaudRate;
	}
	/**
	 * @param serialBaudRate the serialBaudRate to set
	 */
	public void setSerialBaudRate(int serialBaudRate) {
		this.serialBaudRate = serialBaudRate;
	}
	/**
	 * @return the serialversionuid
	 */
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	/**
	 * @return the udpPort
	 */
	public int getUdpPort() {
		return udpPort;
	}
	/**
	 * @param udpPort the udpPort to set
	 */
	public void setUdpPort(int udpPort) {
		this.udpPort = udpPort;
	}
	

}
