package edu.touro.mco152.bm;

import edu.touro.mco152.bm.ui.Gui;
import edu.touro.mco152.bm.ui.MainFrame;
import org.junit.jupiter.api.Test;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

// a Junit test that can start a benchmark; can determine during execution that the intermediate results
// (e.g. percent-complete) look minimally valid; determine whether it completed OK or not; and can (at least minimally)
// assert that it acted properly (e.g. produced some valid-looking results into DiskRun).
public class TestRefactor implements UIInterface{
    int currentPercentComplete;

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

//    @Test
//    public void test(){
//        //setupDefaultAsPerProperties();
//        assertTrue((BooleanSupplier) Gui.mainFrame.getProgressBar());
//        System.out.println(Gui.progressBar.getAccessibleContext());
//    }

    @Override
    public void _cancel(boolean b) {}//never called

    @Override
    public void _addPropertyChangeListener(PropertyChangeListener pcl) {}//no need for listener

    @Test
    @Override
    public void _execute() {
        try {
            new DiskWorker(this)._doInBackground();
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
