/*
 * Copyright 2006-2007 The MZmine Development Team
 * 
 * This file is part of MZmine.
 * 
 * MZmine is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * MZmine is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * MZmine; if not, write to the Free Software Foundation, Inc., 51 Franklin St,
 * Fifth Floor, Boston, MA 02110-1301 USA
 */

package net.sf.mzmine.modules.identification.pubchem;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.logging.Logger;

import net.sf.mzmine.data.ParameterSet;
import net.sf.mzmine.data.PeakList;
import net.sf.mzmine.data.PeakListRow;
import net.sf.mzmine.data.RawDataFile;
import net.sf.mzmine.desktop.Desktop;
import net.sf.mzmine.desktop.MZmineMenu;
import net.sf.mzmine.main.MZmineCore;
import net.sf.mzmine.modules.batchmode.BatchStep;
import net.sf.mzmine.modules.batchmode.BatchStepCategory;
import net.sf.mzmine.taskcontrol.Task;
import net.sf.mzmine.taskcontrol.TaskGroup;
import net.sf.mzmine.taskcontrol.TaskGroupListener;
import net.sf.mzmine.util.dialogs.ExitCode;

/**
 * 
 */
public class PubChemSearch implements BatchStep, ActionListener {
	
	private Logger logger = Logger.getLogger(this.getClass().getName());

    public static final String MODULE_NAME = "PubChem online search";

    private Desktop desktop;

    private PubChemSearchParameters parameters;
    
    private static PubChemSearch myInstance;
    
    private float rawMass;
    
    private PubChemSearchWindow newWindow;

    /**
     * @see net.sf.mzmine.main.MZmineModule#getParameterSet()
     */
    public ParameterSet getParameterSet() {
        return parameters;
    }

    /**
     * @see net.sf.mzmine.main.MZmineModule#initModule(net.sf.mzmine.main.MZmineCore)
     */
    public void initModule() {
        this.desktop = MZmineCore.getDesktop();

        parameters = new PubChemSearchParameters();

        desktop.addMenuItem(MZmineMenu.IDENTIFICATION, MODULE_NAME,
                "Identification by searching in PubChem databases",
                KeyEvent.VK_C, this, null);
        
        myInstance = this;

    }

    /**
     * @see net.sf.mzmine.main.MZmineModule#setParameters(net.sf.mzmine.data.ParameterSet)
     */
    public void setParameters(ParameterSet parameterValues) {
        this.parameters = (PubChemSearchParameters) parameterValues;
    }

	public BatchStepCategory getBatchStepCategory() {
        return BatchStepCategory.IDENTIFICATION;
	}

	public ExitCode setupParameters(ParameterSet parameters) {
	       PubChemSearchDialog dialog = new PubChemSearchDialog(
	                (PubChemSearchParameters) parameters, rawMass);
	        dialog.setVisible(true);
	        return dialog.getExitCode();
	}

	public void actionPerformed(ActionEvent arg0) {
        PeakList[] selectedPeakLists = desktop.getSelectedPeakLists();
        if (selectedPeakLists.length < 1) {
            desktop.displayErrorMessage("Please select a peak list");
            return;
        }

        ExitCode exitCode = setupParameters(parameters);
        if (exitCode != ExitCode.OK)
            return;
        
        /*newWindow = new PubChemSearchWindow();
        desktop.addInternalFrame(newWindow);
        
        runModule(null, null, parameters.clone(), null);*/
    }
	
    public void showPubChemSearchDialog(PeakList peakList, PeakListRow row, float rawMass) {
        
        newWindow = new PubChemSearchWindow(row);
        desktop.addInternalFrame(newWindow);

        PeakList[] selectedPeakLists = new PeakList[] { peakList}; 
    	this.rawMass = rawMass;
    	ExitCode exitCode = setupParameters(parameters);
        if (exitCode != ExitCode.OK)
            return;
        
        
        runModule(null, selectedPeakLists, parameters.clone(), null);
   }
    
    /**
     * @see net.sf.mzmine.modules.BatchStep#runModule(net.sf.mzmine.data.RawDataFile[],
     *      net.sf.mzmine.data.PeakList[], net.sf.mzmine.data.ParameterSet,
     *      net.sf.mzmine.taskcontrol.TaskGroupListener)
     */
    public TaskGroup runModule(RawDataFile[] dataFiles, PeakList[] peakLists,
            ParameterSet parameters, TaskGroupListener methodListener) {

        if (peakLists == null) {
            throw new IllegalArgumentException(
                    "Cannot run identification without a peak list");
        }

        // prepare a new sequence of tasks
        Task tasks[] = new PubChemSearchTask[peakLists.length];
        for (int i = 0; i < peakLists.length; i++) {
            tasks[i] = new PubChemSearchTask((PubChemSearchParameters) parameters, newWindow);
        }
        TaskGroup newSequence = new TaskGroup(tasks, null, methodListener);

        // execute the sequence
        newSequence.start();

        return newSequence;

    }

    
    public static PubChemSearch getInstance(){
    	return myInstance;
    }
    
    
    public String toString() {
        return MODULE_NAME;
    }

}
