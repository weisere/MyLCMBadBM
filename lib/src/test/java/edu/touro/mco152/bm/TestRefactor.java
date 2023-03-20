package edu.touro.mco152.bm;

import edu.touro.mco152.bm.ui.Gui;
import edu.touro.mco152.bm.ui.MainFrame;
import org.junit.jupiter.api.Test;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests that show the benchmark can be run independent of
 * the SwingUI. Proven using a very limited version of
 * the benchmark being run using a stripped down UIInterface
 * that has just enough to pass the tests
 *
 * @implement UIInterface
 */
public class TestRefactor implements UIInterface{
    private int currentPercentComplete;

    public TestRefactor(){
        setupDefaultAsPerProperties();
    }
    /**
     * Bruteforce setup of static classes/fields to allow DiskWorker to run.
     *
     * @author lcmcohen
     */
    private void setupDefaultAsPerProperties()
    {
        /// Do the minimum of what  App.init() would do to allow to run.
        Gui.mainFrame = new MainFrame();
        App.p = new Properties();
        App.loadConfig();
        System.out.println(App.getConfigString());
        Gui.progressBar = Gui.mainFrame.getProgressBar(); //must be set or get Nullptr

        // configure the embedded DB in .jDiskMark
        System.setProperty("derby.system.home", App.APP_CACHE_DIR);

        // code from startBenchmark
        //4. create data dir reference
        App.dataDir = new File(App.locationDir.getAbsolutePath()+File.separator+App.DATADIRNAME);

        //5. remove existing test data if exist
        if (App.dataDir.exists()) {
            if (App.dataDir.delete()) {
                App.msg("removed existing data dir");
            } else {
                App.msg("unable to remove existing data dir");
            }
        }
        else
        {
            App.dataDir.mkdirs(); // create data dir if not already present
        }
    }


    @Override
    public void _cancel(boolean b) {}//never called

    @Override
    public void _addPropertyChangeListener(PropertyChangeListener pcl) {}//no need for listener

    @Test
    @Override
    public void _execute() {
        try {
            new DiskWorker()._doInBackground(this);
            assertEquals(100, currentPercentComplete);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean _isCancelled() {//impossible to cancel
        return false;
    }


    @Override
    public void _setProgress(int percentComplete) {
        assertTrue(percentComplete >= 0 && percentComplete <= 100);
        currentPercentComplete = percentComplete;
    }


    @Override
    public void _publish(DiskMark wMark) {
        assertNotNull(wMark);
    }

    @Override
    public void setDiskWorkerForUI(DiskWorker DW) {}//no need for implementation for testing
}
