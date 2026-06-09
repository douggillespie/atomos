package gui;

import java.awt.GridBagConstraints;
import java.awt.Insets;

public class AMPGridbagConstriants extends GridBagConstraints {

	private static final long serialVersionUID = 1L;

	public AMPGridbagConstriants() {
		gridx = gridy = 0;
		fill = HORIZONTAL;
		anchor = WEST;
		insets = new Insets(2,2,2,2);
	}
}
