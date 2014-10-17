package me.neilellis.dollar;

/**
 * Created by neil on 10/16/14.
 */
public class SimpleErrorLogger implements ErrorLogger {
    @Override
    public void log(String errorMessage) {
      DollarStatic.log("ERROR: "+errorMessage);
    }

    @Override
    public void log(Throwable error) {
        DollarStatic.log("ERROR: "+error.getMessage());
    }

    @Override
    public void log() {
        DollarStatic.log("ERROR");
    }

    @Override
    public void log(String errorMessage, var.ErrorType type) {
        DollarStatic.log(type+" ERROR: "+errorMessage);
    }
}
