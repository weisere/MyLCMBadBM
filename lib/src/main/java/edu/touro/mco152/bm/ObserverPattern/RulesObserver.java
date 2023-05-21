package edu.touro.mco152.bm.ObserverPattern;

import edu.touro.mco152.bm.persist.DiskRun;
/*
*implements the IObserve interface
*
* It is an observer decorator that excepts an IObserve and a
* rule to qualify when the specific IObserve update should be called
 */
public class RulesObserver implements IObserve{
    IObserve observerWithRule;
    Boolean rule;

    public RulesObserver(IObserve observerWithRule, Boolean rule){
        this.observerWithRule = observerWithRule;
        this.rule = rule;
    }

    @Override
    public void update(DiskRun dr) {
        if(rule){
            observerWithRule.update(dr);
        }
    }
}
