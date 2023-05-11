package edu.touro.mco152.bm;

import edu.touro.mco152.bm.commandPattern.ReadTest;
import edu.touro.mco152.bm.commandPattern.WriteTest;
import edu.touro.mco152.bm.ui.Gui;
import edu.touro.mco152.bm.ui.MainFrame;
import org.junit.jupiter.api.Test;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests that shows that the read and write functionality
 * (which was split made into commands) runs properly
 *
 * Made a "Dumbed down" version of an interface in order to properly
 * test code. The main test which shows read and write are functional
 * is the _execute() test
 *
 * @implement UIInterface
 */
public class WriteReadSplitTest implements UIInterface{
    private int currentPercentComplete;
    ReadTest rt;
    WriteTest wt;

    public WriteReadSplitTest(){
        setupDefaultAsPerProperties();
        ReadTest rt = new ReadTest();
        WriteTest wt = new WriteTest();
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
            rt.runReadTest(this);
            assertEquals(100, currentPercentComplete);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            wt.runWriteTest(this);
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

}
