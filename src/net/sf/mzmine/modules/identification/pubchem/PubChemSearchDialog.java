package net.sf.mzmine.modules.identification.pubchem;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Logger;

import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JTextField;

import net.sf.mzmine.data.Parameter;
import net.sf.mzmine.main.MZmineCore;
import net.sf.mzmine.util.dialogs.ParameterSetupDialog;

public class PubChemSearchDialog extends ParameterSetupDialog implements
ActionListener, PropertyChangeListener {
	
	private Logger logger = Logger.getLogger(this.getClass().getName());

	private PubChemSearchParameters parameters;
	private static final Color BACKGROUND_COLOR = new Color(173, 216, 230);
	private float rawMassValue;
	
	/**
	 * Constructor
	 */
	public PubChemSearchDialog(PubChemSearchParameters parameters, float massValue) {

		// Make dialog modal
		super("PubChem search setup dialog ", parameters);

		this.parameters = parameters;
		this.rawMassValue = massValue;
		
		Component[] fields = pnlFields.getComponents();
		Parameter[] params = parameters.getParameters();
		for (int i=0; i< params.length; i++) {
			if (params[i].getName() == "Neutral mass"){
				((JTextField)fields[i]).setEditable(false);
				((JTextField)fields[i]).setBackground(BACKGROUND_COLOR);
				continue;
			}
			if (params[i].getName() == "Peak mass"){
				((JTextField)fields[i]).setText(String.valueOf(massValue));
				((JTextField)fields[i]).setEditable(false);
				((JTextField)fields[i]).setBackground(BACKGROUND_COLOR);
				continue;
			}
			if (fields[i] instanceof JComboBox){
				((JComboBox) fields[i]).addActionListener(this);
			}

			fields[i].addPropertyChangeListener("value", this);

		}
		
		setNeutralMassValue();

		setResizable(false);
		setLocationRelativeTo(MZmineCore.getDesktop().getMainFrame());

	}

	/**
	 * Implementation for ActionListener interface
	 */
	public void actionPerformed(ActionEvent ae) {
		
		Object src = ae.getSource();
		if (src instanceof JComboBox){
			setNeutralMassValue();			
		}

		super.actionPerformed(ae);

	}

	public void propertyChange(PropertyChangeEvent arg0) {
		setNeutralMassValue();
	}
	
	private void setNeutralMassValue(){
		int chargeLevel = 1;
		float ion = 0.0f;
		float neutral = rawMassValue;
		int sign = 1;
		
		Component[] fields = pnlFields.getComponents();
		Parameter[] params = parameters.getParameters();
		for (int i=0; i< params.length; i++) {
			if (params[i].getName() == "Charge"){
				Integer o = (Integer) ((JFormattedTextField)fields[i]).getValue();
				chargeLevel = o.intValue();
				continue;
			}
			if (params[i].getName() == "Ionization method"){
				Object a = ((JComboBox)fields[i]).getSelectedItem();
				ion = ((TypeOfIonization)a).getMass();
				sign = ((TypeOfIonization)a).getSign();
				ion *= sign;
				continue;
			}
		}
		
		neutral /= chargeLevel;
		neutral += ion;
		
		for (int i=0; i< params.length; i++) {
			if (params[i].getName() == "Neutral mass"){
				((JFormattedTextField)fields[i]).setValue(neutral);
				break;
			}
		}
	}

}
