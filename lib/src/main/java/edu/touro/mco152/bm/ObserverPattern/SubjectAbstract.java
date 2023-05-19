package edu.touro.mco152.bm.ObserverPattern;

import edu.touro.mco152.bm.persist.DiskRun;

import java.util.ArrayList;
/*
*Abstract Class that contains all the functionality and the one
* parameter that would be needed to implement the ISubject Interface
 */
public abstract class SubjectAbstract implements ISubject {
    public ArrayList<IObserve> observerList;

    @Override
    public void attach(IObserve observer) {
        observerList.add(observer);
    }

    @Override
    public void detach(IObserve observer) {
        observerList.remove(observer);
    }

    @Override
    public void notifyObservers(DiskRun dr) {
        for (IObserve observer: observerList) {
            observer.update(dr);
        }
    }
}
