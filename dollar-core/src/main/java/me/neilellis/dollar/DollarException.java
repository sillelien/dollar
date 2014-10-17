package me.neilellis.dollar;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class DollarException extends RuntimeException {
    public DollarException(Throwable e) {
        super(e);
    }

    public DollarException(String errorMessage) {
        super(errorMessage);
    }

    public int httpCode() {
        return 500;
    }
}
