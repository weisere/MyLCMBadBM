package edu.touro.mco152.bm;

import java.beans.PropertyChangeEvent;

/**
 * This is an interface that will contain all methods that the App class
 * and DiskWorker class will need from a UI in order to display benchmark
 * progress
 */
public interface UIInterface {


    public void _cancel(boolean b);

    public void _addPropertyChangeListener(PropertyChangeEvent event);

    public void _execute();

    public boolean _isCancelled();

    public void _setProgress(int percentComplete);

    public void _publish(DiskMark wMark);
}
