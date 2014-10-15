package me.neilellis.dollar;

import java.util.Arrays;

/**
 * Created by neil on 10/15/14.
 */
public class SimpleLogStateTracer implements StateTracer {
    @Override
    public <R> R trace(Object before, R after, Operations operationType, Object... values) {
        DollarStatic.log(String.format("%s (%s): %s -> %s",operationType, Arrays.toString(values),before,after));
        return after;
    }
}
