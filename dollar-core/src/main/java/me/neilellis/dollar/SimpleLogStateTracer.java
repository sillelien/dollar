/*
 * Copyright (c) 2014 Neil Ellis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.neilellis.dollar;

import me.neilellis.dollar.types.DollarVoid;

import java.util.Arrays;

/**
 * Created by neil on 10/15/14.
 */
public class SimpleLogStateTracer implements StateTracer {
    @Override
    public <R> R trace(Object before, R after, Operations operationType, Object... values) {
        String beforeStr = "";
        String afterStr = "";
        String afterNotes = "";
        String beforeNotes = "";
        if (after instanceof var) {
            if (((var) after).hasErrors()) {
                afterNotes += "*ERROR*";
            }
            if (((var) after).isLambda()) {
                afterStr = "<LAMBDA>";
            } else {
                afterStr = after.toString();
            }
        } else {
            if (after != null) {
                afterStr = String.format("%s(%s)", after.toString(), after.getClass().getName());
            }
        }
        if (before instanceof var) {
            if (((var) before).hasErrors()) {
                beforeNotes += "*ERROR*";
            }
            if (((var) before).isLambda()) {
                beforeStr = "<LAMBDA>";
            } else {
                beforeStr = before.toString();
            }
        } else {
            if (before != null) {
                beforeStr = String.format("%s(%s)", before.toString(), before.getClass().getName());
            }
        }
        if ((before instanceof DollarVoid || before == null) && (after instanceof DollarVoid || after == null)) {
            DollarStatic.log(String.format("%s%s: %s->%s",
                    operationType,
                    Arrays.toString(values),
                    beforeNotes,
                    afterNotes));
        } else if (before instanceof DollarVoid || before == null) {
            DollarStatic.log(String.format("%s%s: %s%s", operationType, Arrays.toString(values), afterStr, afterNotes));
        } else {
            DollarStatic.log(String.format("%s%s: %s%s -> %s%s",
                    operationType,
                    Arrays.toString(values),
                    beforeStr,
                    beforeNotes,
                    afterStr,
                    afterNotes));
        }
        return after;
    }
}
