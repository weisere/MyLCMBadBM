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

import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import static edu.touro.mco152.bm.App.*;
import static edu.touro.mco152.bm.DiskMark.MarkType.READ;
/*
 *The ReadTestCommand Class is a concrete command that runs reading test benchmarking
 * It inherits from the ICommand interface
 *
 * The execute() method is never directly called, only the InvokeCommand class calls it
 *
 * It needs a UIInterface in order to run and other various parameters to decouple
 * the class from App. And allow for specific testing
 *
 * Extends the Subject Abstract to have all the functionality a Subject would need to add and notify observers
 *
 */

public class ReadTestCommand extends SubjectAbstract implements ICommand {
    // declare local vars formerly in DiskWorker
    public UIInterface ui;
    public int marks;
    public int diskBlocks;
    public int sizeOfDiskBlocks;
    public DiskRun.BlockSequence sequenceOfIOOperations;
    //public ArrayList<IObserve> observerList;//list of observers

    public ReadTestCommand(UIInterface ui, int marks, int diskBlocks, int sizeOfDiskBlocks, DiskRun.BlockSequence sequenceOfIOOperations ){
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

        /*
          init local vars that keep track of benchmarks
         */
        int rUnitsComplete = 0;



        int rUnitsTotal =  diskBlocks * marks;
        float percentComplete;

        int blockSize = blockSizeKb*KILOBYTE;
        byte [] blockArr = new byte [blockSize];
        for (int b=0; b<blockArr.length; b++) {
            if (b%2==0) {
                blockArr[b]=(byte)0xFF;
            }
        }

        DiskMark rMark;
        int startFileNum = App.nextMarkNumber;
        DiskRun run = new DiskRun(DiskRun.IOMode.READ, sequenceOfIOOperations);
        run.setNumMarks(marks);
        run.setNumBlocks(diskBlocks);
        run.setBlockSize(sizeOfDiskBlocks);
        run.setTxSize(App.targetTxSizeKb());
        run.setDiskInfo(Util.getDiskInfo(dataDir));

        msg("disk info: (" + run.getDiskInfo() + ")");

        Gui.chartPanel.getChart().getTitle().setVisible(true);
        Gui.chartPanel.getChart().getTitle().setText(run.getDiskInfo());

        for (int m = startFileNum; m < startFileNum + marks && !ui._isCancelled(); m++) {

            if (App.multiFile) {
                testFile = new File(dataDir.getAbsolutePath()
                        + File.separator + "testdata" + m + ".jdm");
            }
            rMark = new DiskMark(READ);  // starting to keep track of a new benchmark
            rMark.setMarkNum(m);
            long startTime = System.nanoTime();
            long totalBytesReadInMark = 0;

            try {
                try (RandomAccessFile rAccFile = new RandomAccessFile(testFile, "r")) {
                    for (int b = 0; b < diskBlocks; b++) {
                        if (sequenceOfIOOperations == DiskRun.BlockSequence.RANDOM) {
                            int rLoc = Util.randInt(0, diskBlocks - 1);
                            rAccFile.seek((long) rLoc * blockSize);
                        } else {
                            rAccFile.seek((long) b * blockSize);
                        }
                        rAccFile.readFully(blockArr, 0, blockSize);
                        totalBytesReadInMark += blockSize;
                        rUnitsComplete++;
                        percentComplete = (float) rUnitsComplete / (float) rUnitsTotal * 100f;
                        ui._setProgress((int) percentComplete);
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
                String emsg = "May not have done Write Benchmarks, so no data available to read." +
                        ex.getMessage();
                JOptionPane.showMessageDialog(Gui.mainFrame, emsg, "Unable to READ", JOptionPane.ERROR_MESSAGE);
                msg(emsg);
                return false;
            }
            long endTime = System.nanoTime();
            long elapsedTimeNs = endTime - startTime;
            double sec = (double) elapsedTimeNs / (double) 1000000000;
            double mbRead = (double) totalBytesReadInMark / (double) MEGABYTE;
            rMark.setBwMbSec(mbRead / sec);
            msg("m:" + m + " READ IO is " + rMark.getBwMbSec() + " MB/s    "
                    + "(MBread " + mbRead + " in " + sec + " sec)");
            App.updateMetrics(rMark);
            ui._publish(rMark);

            run.setRunMax(rMark.getCumMax());
            run.setRunMin(rMark.getCumMin());
            run.setRunAvg(rMark.getCumAvg());
            run.setEndTime(new Date());
        }


        /*
         notifies all observers attached in the constructor
         */
        notifyObservers(run);

        App.nextMarkNumber += marks;
        return true;
    }


}
