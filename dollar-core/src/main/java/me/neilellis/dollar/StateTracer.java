package me.neilellis.dollar;

/**
 * Created by neil on 10/15/14.
 */
public interface StateTracer {
    enum Operations {EVAL, LOAD, PIPE, POP, REMOVE_BY_VALUE, REMOVE_BY_KEY, SAVE, SPLIT, SET}
    <R> R trace(Object before, R after, Operations operationType, Object... values);
}
