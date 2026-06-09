package gui;

import java.awt.Dimension;
import java.awt.Point;
import java.io.Serializable;

import control.ParamsSet;

/**
 * This is not so much GUI params as it is global params. 
 * @author Doug
 *
 */
public class GuiParameters implements ParamsSet, Serializable, Cloneable {
	
	private static final long serialVersionUID = 1L;

	public Point locationOnScreen;
	
	public Dimension size;
	
	public boolean alwaysOnTop = true;
	
	public int repeatTriesOnError = 1;
	
	public int tcpReadTimeout = 2000;
	
	public boolean errorBeep = true;

	private int version = 1;
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected GuiParameters clone() {
		try {
			GuiParameters newParams = (GuiParameters) super.clone();
			if (version < 1) {
				newParams.repeatTriesOnError = 1;
				newParams.tcpReadTimeout = 2000;
				newParams.errorBeep = true;
			}
			return newParams;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

}
