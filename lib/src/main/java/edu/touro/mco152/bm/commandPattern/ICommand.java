package edu.touro.mco152.bm.commandPattern;
/*
*The ICommand interface is the interface which commands will have to implement if
* executed using the InvokeCommand Class
 */
public interface ICommand {

    public boolean execute();
}
