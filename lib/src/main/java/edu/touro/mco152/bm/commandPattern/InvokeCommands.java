package edu.touro.mco152.bm.commandPattern;

/*
*The InvokeCommands class is made to execute commands.
* the commands are set using the setCommand() and call the commands execute() method
* in invokeCommand
*
* It can accept any commend of type ICommand
*
 */

public class InvokeCommands {
    ICommand command;

    public void setCommand(ICommand command) {
        this.command = command;
    }

    /*
    *@return boolean
    * return whether or not the command executed successfully
     */
    public boolean invokeCommand(){
        return command.execute();
    }


}
