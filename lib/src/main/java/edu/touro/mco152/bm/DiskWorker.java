package edu.touro.mco152.bm;

import edu.touro.mco152.bm.commandPattern.InvokeCommands;
import edu.touro.mco152.bm.commandPattern.ReadTestCommand;
import edu.touro.mco152.bm.commandPattern.WriteTestCommand;
import edu.touro.mco152.bm.ui.Gui;

import javax.swing.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static edu.touro.mco152.bm.App.*;


/**
 * Run the disk benchmarking as a UIInterface thread (only one of these threads can run at
 * once.) Cooperates with UIInterface to provide and make use of interim and final progress and
 * information, which is also recorded as needed to the persistence store, and log.
 * <p>
 * The DiskRun class is used to keep track of and persist info about each benchmark at a higher level (a run),
 * while the DiskMark class described each iteration's result, which is displayed by the UI as the benchmark run
 * progresses.
 * <p>
 * This class only knows how to use the InvokeCommand class which in turn will call
 * 'read' or 'write' disk benchmarks. It is instantiated by the
 * startBenchmark() method.
 * <p>
 * To be UIInterface compliant this class declares that intermediate results are communicated to
 * UIInterface using an instance of the DiskMark class.
 */

public class DiskWorker {

    /**
     * Does the actual benchmarking and communicates those results
     * to the given Interface that would be of type UIInterface
     *
     * @param ui
     * @return boolean
     * @throws Exception
     */
    protected Boolean _doInBackground(UIInterface ui) throws Exception {

        /*
          We 'got here' because: 1: End-user clicked 'Start' on the benchmark UI,
          which triggered the start-benchmark event associated with the App::startBenchmark()
          method.  2: startBenchmark() then instantiated a DiskWorker, and called
          its (super class's) execute() method, causing Swing to eventually
          call this doInBackground() method.
         */
        Logger.getLogger(App.class.getName()).log(Level.INFO, "*** New worker thread started ***");
        msg("Running readTest " + App.readTest + "   writeTest " + App.writeTest);
        msg("num files: " + App.numOfMarks + ", num blks: " + App.numOfBlocks
                + ", blk size (kb): " + App.blockSizeKb + ", blockSequence: " + App.blockSequence);


        InvokeCommands invoker = new InvokeCommands();
        int blockSize = blockSizeKb * KILOBYTE;
        byte[] blockArr = new byte[blockSize];
        for (int b = 0; b < blockArr.length; b++) {
            if (b % 2 == 0) {
                blockArr[b] = (byte) 0xFF;
            }
        }


        Gui.updateLegend();  // init chart legend info

        if (App.autoReset) {
            App.resetTestData();
            Gui.resetTestData();
        }


        /*
          The GUI allows a Write, Read, or both types of BMs to be started. They are done serially.
         */
        if (App.writeTest) {
            WriteTestCommand wt = new WriteTestCommand(ui, numOfMarks, numOfBlocks, blockSizeKb,
                    blockSequence);
            invoker.setCommand(wt);
            return invoker.invokeCommand();
        }


        /*
          Most benchmarking systems will try to do some cleanup in between 2 benchmark operations to
          make it more 'fair'. For example a networking benchmark might close and re-open sockets,
          a memory benchmark might clear or invalidate the Op Systems TLB or other caches, etc.
         */

        // try renaming all files to clear catch
        if (App.readTest && App.writeTest && !ui._isCancelled()) {
            JOptionPane.showMessageDialog(Gui.mainFrame,
                    """
                            For valid READ measurements please clear the disk cache by
                            using the included RAMMap.exe or flushmem.exe utilities.
                            Removable drives can be disconnected and reconnected.
                            For system drives use the WRITE and READ operations\s
                            independantly by doing a cold reboot after the WRITE""",
                    "Clear Disk Cache Now", JOptionPane.PLAIN_MESSAGE);
        }

        // Same as above, just for Read operations instead of Writes.
        if (App.readTest) {
            ReadTestCommand rt = new ReadTestCommand(ui, numOfMarks, numOfBlocks, blockSizeKb,
                    blockSequence);
            invoker.setCommand(rt);
            return invoker.invokeCommand();
        }

        return false;
    }
}