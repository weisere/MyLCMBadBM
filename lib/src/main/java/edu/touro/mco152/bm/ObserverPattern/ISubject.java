package edu.touro.mco152.bm.ObserverPattern;

import edu.touro.mco152.bm.persist.DiskRun;

public interface ISubject {
    void attach(IObserve observer);
    void detach(IObserve observer);
    void notifyObservers(DiskRun dr);
}
