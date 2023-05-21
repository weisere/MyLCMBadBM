package edu.touro.mco152.bm.ObserverPattern;

import edu.touro.mco152.bm.externalsys.SlackManager;
import edu.touro.mco152.bm.persist.DiskRun;

/*
*Implements IObserve and when notified wil leverage the
* SlackManger class and send a message to slack
 */
public class SlackObserver implements IObserve{
    SlackManager slackmgr;

    @Override
    public void update(DiskRun dr) {
            slackmgr = new SlackManager("Benchmark Channel");
            slackmgr.postMsg2OurChannel(": Read benchmark result has an iteration 'max time' that exceeds 3 per cent of the benchmarks average time");
    }
}
