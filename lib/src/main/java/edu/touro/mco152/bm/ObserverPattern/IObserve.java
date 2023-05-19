package edu.touro.mco152.bm.ObserverPattern;

import edu.touro.mco152.bm.persist.DiskRun;

public interface IObserve {
    void update(DiskRun dr);
}
