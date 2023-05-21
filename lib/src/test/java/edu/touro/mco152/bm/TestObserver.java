package edu.touro.mco152.bm;

import edu.touro.mco152.bm.ObserverPattern.IObserve;
import edu.touro.mco152.bm.persist.DiskRun;
/*
*TestObserver was made for testing purposes to ensure that the update is
* called by the subject when notified
*
* implements the IObserve Class and contains an additional methods getWasNotified()
* to return the status if it was notified and updated.
 */
public class TestObserver implements IObserve {
    boolean wasNotified = false;
    @Override
    public void update(DiskRun dr) {
        wasNotified = true;
    }

    public boolean getWasNotified() {
        return wasNotified;
    }
}
