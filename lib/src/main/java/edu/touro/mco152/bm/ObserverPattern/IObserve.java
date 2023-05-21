package edu.touro.mco152.bm.ObserverPattern;

import edu.touro.mco152.bm.persist.DiskRun;
/*
*IObserve is an interface to be used by all observers
* to make them generic and all have the same update method
 */
public interface IObserve {
    void update(DiskRun dr);
}
