package edu.touro.mco152.bm.ui;

import edu.touro.mco152.bm.ObserverPattern.IObserve;
import edu.touro.mco152.bm.persist.DiskRun;
/*
 *Takes the persist info about write or read BM Run and updates the GUI to display the results
 *
 * Implements the IObserve interface
 */
public class GuiObserver implements IObserve {
    @Override
    public void update(DiskRun dr) {
        Gui.runPanel.addRun(dr);
    }
}
