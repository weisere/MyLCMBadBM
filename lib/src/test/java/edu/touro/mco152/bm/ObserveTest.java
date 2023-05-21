package edu.touro.mco152.bm;

import edu.touro.mco152.bm.commandPattern.InvokeCommands;
import edu.touro.mco152.bm.commandPattern.ReadTestCommand;
import edu.touro.mco152.bm.commandPattern.WriteTestCommand;
import edu.touro.mco152.bm.persist.DiskRun;
import edu.touro.mco152.bm.ui.Gui;
import edu.touro.mco152.bm.ui.MainFrame;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests that shows that the read and write attached
 * observers are executed
 *
 * It utilizes the TestObserver class. which implements the IObserve interface and has
 * an additional method that returns the boolean which tracks if was updated or not.
 *
 *
 * Made a "Dumbed down" version of an interface in order to properly
 * test code. The main test which shows read and write are functional
 * is the _execute() test
 *
 * @implement UIInterface
 */
public class ObserveTest implements UIInterface{
    private int currentPercentComplete;
    ReadTestCommand rt;
    WriteTestCommand wt;
    InvokeCommands invoker;
    static TestObserver testObserverForWrite;
    static TestObserver testObserverForRead;


    //num files: 25, num blks: 128, blk size (kb): 2048, blockSequence: SEQUENTIAL

    public ObserveTest(){
        setupDefaultAsPerProperties();
        rt = new ReadTestCommand(this, 25,128,2048, DiskRun.BlockSequence.SEQUENTIAL);
        wt = new WriteTestCommand(this, 25,128,2048, DiskRun.BlockSequence.SEQUENTIAL);
        invoker = new InvokeCommands();
        //attach Observers
        wt.attach(testObserverForWrite = new TestObserver());
        rt.attach(testObserverForRead = new TestObserver());
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
            //ensure that wasNotified is currently false
            assertFalse(testObserverForWrite.getWasNotified());
            invoker.setCommand(wt);
            invoker.invokeCommand();
            assertEquals(100, currentPercentComplete);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            //ensure that wasNotified is currently false
            assertFalse(testObserverForRead.getWasNotified());;
            invoker.setCommand(rt);
            invoker.invokeCommand();
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

    @AfterAll
    public static void observerNotified(){
        assertTrue(testObserverForWrite.getWasNotified());
        assertTrue(testObserverForRead.getWasNotified());
    }

}
