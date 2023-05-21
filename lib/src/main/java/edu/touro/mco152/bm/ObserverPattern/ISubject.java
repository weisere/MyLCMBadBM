package edu.touro.mco152.bm.ObserverPattern;

import edu.touro.mco152.bm.persist.DiskRun;
/*
*ISubject is made to be implemented by a subject that has one or more observers
* Contains the 3 needed methods of all Subjects
 */
public interface ISubject {
    void attach(IObserve observer);
    void detach(IObserve observer);
    void notifyObservers(DiskRun dr);
}
