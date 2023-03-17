package edu.touro.mco152.bm;

/**
 * This is an interface that will contain all methods that the App class
 * and DiskWorker class will need from a UI in order to display benchmark
 * progress
 */
public interface UIInterface {


    public void _cancel();

    public void _addPropertyChangeListener();

    public void _execute();

    public boolean _isCancelled();

    public void _progress();

    public void _publish();
}
