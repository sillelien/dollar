package me.neilellis.dollar.exceptions;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class SingleValueException extends UnsupportedOperationException {
    public SingleValueException() {
        super("Cannot perform this operation on a single value data");
    }

    public SingleValueException(String message) {
        super(message);
    }
}
