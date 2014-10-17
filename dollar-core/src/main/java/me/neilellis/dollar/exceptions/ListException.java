package me.neilellis.dollar.exceptions;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class ListException extends UnsupportedOperationException {
    public ListException() {
        super("Cannot perform this operation on list type data");
    }

    public ListException(String message) {
        super(message);
    }
}
