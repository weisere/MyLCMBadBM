package edu.touro.mco152.bm;

import edu.touro.mco152.bm.ui.Gui;

import javax.swing.*;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.logging.Logger;

import static edu.touro.mco152.bm.App.dataDir;

/**
 * SwingUI extends SwingWorker and implements the UIInterface
 * It contains the required SwingWorker doInBackground() method
 * and the process() and done() method
 *<p>
 * All UIInterface methods have been overridden and call the
 * corresponding SwingWorker methods
 *
 * @implement UIInterface
 * @extends SwingWorker
 */
public class SwingUI extends SwingWorker<Boolean, DiskMark> implements UIInterface {

    // Record any success or failure status returned from SwingWorker (might be us or super)
    Boolean lastStatus = null;  // so far unknown
    DiskWorker dW = null;

    public SwingUI(DiskWorker dw){
        dW = dw;
    }


    /**
     * Required method of SwingWorker which calls the class
     * instantiation of DiskWorkers' method _doInBackground()
     * in which the benchmark is set and actually tested
     *
     * Returns a boolean of whether the benchmark was successful
     * and an exception if access to any files does not work
     *
     * @return boolean
     * @throws Exception
     */
    @Override
    protected Boolean doInBackground() throws Exception {
        return dW._doInBackground(this);//which will return a boolean
    }

    /**
     * Process a list of 'chunks' that have been processed, ie that our thread has previously
     * published to Swing. For my info, watch Professor Cohen's video -
     * Module_6_RefactorBadBM Swing_DiskWorker_Tutorial.mp4
     * @param markList a list of DiskMark objects reflecting some completed benchmarks
     */
    @Override
    protected void process(List<DiskMark> markList) {
        markList.stream().forEach((dm) -> {
            if (dm.type == DiskMark.MarkType.WRITE) {
                Gui.addWriteMark(dm);
            } else {
                Gui.addReadMark(dm);
            }
        });
    }


    @Override
    protected void done() {
        // Obtain final status, might from doInBackground ret value, or SwingWorker error
        try {
            lastStatus = super.get();   // record for future access
        } catch (Exception e) {
            Logger.getLogger(App.class.getName()).warning("Problem obtaining final status: " + e.getMessage());
        }

        if (App.autoRemoveData) {
            Util.deleteDirectory(dataDir);
        }
        App.state = App.State.IDLE_STATE;
        Gui.mainFrame.adjustSensitivity();
    }

    public Boolean getLastStatus() {
        return lastStatus;
    }


    @Override
    public void _cancel(boolean b) {
        cancel(b);
    }

    @Override
    public void _addPropertyChangeListener(PropertyChangeListener pcl) {
        addPropertyChangeListener((PropertyChangeListener) pcl);
    }

    @Override
    public void _execute() {
        execute();
    }

    @Override
    public boolean _isCancelled() {
        return isCancelled();
    }

    @Override
    public void _setProgress(int percentComplete) {
        setProgress(percentComplete);
    }

    @Override
    public void _publish(DiskMark wMark) {
        publish(wMark);
    }


}
