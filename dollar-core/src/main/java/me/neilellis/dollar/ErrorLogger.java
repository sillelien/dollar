package me.neilellis.dollar;

/**
 * Created by neil on 10/16/14.
 */
public interface ErrorLogger {
    void log(String errorMessage);
    void log(Throwable error);
    void log();
    void log(String errorMessage, var.ErrorType type);
}
