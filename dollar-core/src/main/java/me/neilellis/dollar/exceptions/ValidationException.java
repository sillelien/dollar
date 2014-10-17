package me.neilellis.dollar.exceptions;

import me.neilellis.dollar.DollarException;

/**
 * Created by neil on 10/16/14.
 */
public class ValidationException extends DollarException{
    public ValidationException(Throwable e) {
        super(e);
    }

    public ValidationException(String errorMessage) {
        super(errorMessage);
    }

    @Override
    public int httpCode() {
        return 400;
    }
}
