package edu.touro.mco152.bm.commandPattern;

import edu.touro.mco152.bm.App;
import edu.touro.mco152.bm.DiskMark;
import edu.touro.mco152.bm.ObserverPattern.IObserve;
import edu.touro.mco152.bm.ObserverPattern.ISubject;
import edu.touro.mco152.bm.ObserverPattern.SubjectAbstract;
import edu.touro.mco152.bm.UIInterface;
import edu.touro.mco152.bm.Util;
import edu.touro.mco152.bm.persist.DatabaseObserver;
import edu.touro.mco152.bm.persist.DiskRun;
import edu.touro.mco152.bm.persist.EM;
import edu.touro.mco152.bm.ui.Gui;
import edu.touro.mco152.bm.ui.GuiObserver;
import jakarta.persistence.EntityManager;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import static edu.touro.mco152.bm.App.*;
import static edu.touro.mco152.bm.App.msg;
import static edu.touro.mco152.bm.DiskMark.MarkType.WRITE;

/*
*The WriteTestCommand Class is a concrete command that runs writing test benchmarking
* It inherits from the ICommand interface
*
* The execute() method is never directly called, only the InvokeCommand class calls it
*
* It needs a UIInterface in order to run and other various parameters to decouple
* the class from App. And allow for specific testing
*
* Extends the Subject Abstract to have all the functionality a Subject would need to add and notify observers
 */

public class WriteTestCommand extends SubjectAbstract implements ICommand{
    public UIInterface ui;
    public int marks;
    public int diskBlocks;
    public int sizeOfDiskBlocks;
    public DiskRun.BlockSequence sequenceOfIOOperations;
    //public ArrayList<IObserve> observerList;//list of observers

    public WriteTestCommand(UIInterface ui, int marks, int diskBlocks, int sizeOfDiskBlocks, DiskRun.BlockSequence sequenceOfIOOperations ){
        this.ui = ui;
        this.marks = marks;
        this.diskBlocks = diskBlocks;
        this.sizeOfDiskBlocks = sizeOfDiskBlocks;
        this.sequenceOfIOOperations = sequenceOfIOOperations;
         /*
        instantiates observerList and adds the various observers
         */
        observerList = new ArrayList<>();
        attach(new DatabaseObserver());
        attach(new GuiObserver());
    }

    @Override
    public boolean execute() {
        // declare local vars formerly in DiskWorker

        /*
          init local vars that keep track of benchmarks
         */

        int wUnitsComplete = 0;

        int wUnitsTotal = diskBlocks * marks;
        //int rUnitsTotal = App.readTest ? numOfBlocks * numOfMarks : 0;
        float percentComplete;

        int blockSize = blockSizeKb*KILOBYTE;
        byte [] blockArr = new byte [blockSize];
        for (int b=0; b<blockArr.length; b++) {
            if (b%2==0) {
                blockArr[b]=(byte)0xFF;
            }
        }

        DiskMark wMark;
        int startFileNum = nextMarkNumber;
        DiskRun run = new DiskRun(DiskRun.IOMode.WRITE, sequenceOfIOOperations);
        run.setNumMarks(marks);
        run.setNumBlocks(diskBlocks);
        run.setBlockSize(sizeOfDiskBlocks);
        run.setTxSize(App.targetTxSizeKb());
        run.setDiskInfo(Util.getDiskInfo(dataDir));

        // Tell logger and GUI to display what we know so far about the Run
        msg("disk info: (" + run.getDiskInfo() + ")");

        Gui.chartPanel.getChart().getTitle().setVisible(true);
        Gui.chartPanel.getChart().getTitle().setText(run.getDiskInfo());

        // Create a test data file using the default file system and config-specified location
        if (!App.multiFile) {
            testFile = new File(dataDir.getAbsolutePath() + File.separator + "testdata.jdm");
        }

            /*
              Begin an outer loop for specified duration (number of 'marks') of benchmark,
              that keeps writing data (in its own loop - for specified # of blocks). Each 'Mark' is timed
              and is reported to the GUI for display as each Mark completes.
             */
        for (int m = startFileNum; m < startFileNum + marks && !ui._isCancelled(); m++) {

            if (App.multiFile) {
                testFile = new File(dataDir.getAbsolutePath()
                        + File.separator + "testdata" + m + ".jdm");
            }
            wMark = new DiskMark(WRITE);    // starting to keep track of a new benchmark
            wMark.setMarkNum(m);
            long startTime = System.nanoTime();
            long totalBytesWrittenInMark = 0;

            String mode = "rw";
            if (App.writeSyncEnable) {
                mode = "rwd";
            }

            try {
                try (RandomAccessFile rAccFile = new RandomAccessFile(testFile, mode)) {
                    for (int b = 0; b < diskBlocks; b++) {
                        if (sequenceOfIOOperations == DiskRun.BlockSequence.RANDOM) {
                            int rLoc = Util.randInt(0, diskBlocks - 1);
                            rAccFile.seek((long) rLoc * blockSize);
                        } else {
                            rAccFile.seek((long) b * blockSize);
                        }
                        rAccFile.write(blockArr, 0, blockSize);
                        totalBytesWrittenInMark += blockSize;
                        wUnitsComplete++;
                        percentComplete = (float) wUnitsComplete / (float) wUnitsTotal * 100f;

                            /*
                              Report to GUI what percentage level of Entire BM (#Marks * #Blocks) is done.
                             */
                        ui._setProgress((int) percentComplete);
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
            }

                /*
                  Compute duration, throughput of this Mark's step of BM
                 */
            long endTime = System.nanoTime();
            long elapsedTimeNs = endTime - startTime;
            double sec = (double) elapsedTimeNs / (double) 1000000000;
            double mbWritten = (double) totalBytesWrittenInMark / (double) MEGABYTE;
            wMark.setBwMbSec(mbWritten / sec);
            msg("m:" + m + " write IO is " + wMark.getBwMbSecAsString() + " MB/s     "
                    + "(" + Util.displayString(mbWritten) + "MB written in "
                    + Util.displayString(sec) + " sec)");
            App.updateMetrics(wMark);

                /*
                  Let the GUI know the interim result described by the current Mark
                 */
            ui._publish(wMark);

            // Keep track of statistics to be displayed and persisted after all Marks are done.
            run.setRunMax(wMark.getCumMax());
            run.setRunMin(wMark.getCumMin());
            run.setRunAvg(wMark.getCumAvg());
            run.setEndTime(new Date());
        } // END outer loop for specified duration (number of 'marks') for WRITE benchmark


        /*
        notifies all observers attached in the constructor
        */
        notifyObservers(run);

        App.nextMarkNumber += marks;
        return true;
    }


}
