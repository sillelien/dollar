package me.neilellis.dollar;

import java.util.Arrays;

/**
 * Created by neil on 10/15/14.
 */
public class SimpleLogStateTracer implements StateTracer {
    @Override
    public <R> R trace(Object before, R after, Operations operationType, Object... values) {
        String notes="";
        if(after instanceof var) {
            if(((var) after).hasErrors()) {
                notes += "*A:E* ";
            }
        }
        if(before instanceof var) {
            if(((var) before).hasErrors()) {
                notes += "*B:E* ";
            }
        }
        if((before instanceof DollarNull || before == null )&& (after instanceof DollarNull|| after == null)) {
            DollarStatic.log(String.format("%s%s: %s",operationType, Arrays.toString(values),notes));
        } else if(before instanceof DollarNull || before == null) {
            DollarStatic.log(String.format("%s%s: %s%s",operationType, Arrays.toString(values),notes,after));
        } else {
            DollarStatic.log(String.format("%s%s: %s%s -> %s", operationType, Arrays.toString(values), notes,before, after));
        }
        return after;
    }
}
