/*
 *    Copyright (c) 2014-2017 Neil Ellis
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package dollar.api.monitor;

import dollar.api.DollarStatic;
import dollar.api.StateTracer;
import dollar.api.Value;
import dollar.api.types.DollarList;
import dollar.api.types.DollarMap;
import dollar.api.types.DollarVoid;
import org.jetbrains.annotations.NotNull;

public class SimpleLogStateTracer implements StateTracer {
    @NotNull
    private String format(@NotNull Object value) {
        Object unwrapped;
        String formatted;
        if (value instanceof Value) {
            unwrapped = ((Value) value).$unwrap();
        } else {
            unwrapped = value;
        }
        if ((value instanceof Value) &&
                    (((Value) unwrapped).dynamic() || (unwrapped instanceof DollarList) || (unwrapped instanceof DollarMap))) {
            formatted = "<" + unwrapped.getClass().getSimpleName() + ">";
        } else {
            formatted = String.valueOf(value);
        }
        return formatted;
    }

    @NotNull
    private String toDescription(@NotNull Object[] values) {
        StringBuilder result = new StringBuilder("[");
        for (Object value : values) {
            Object unwrapped;
            String formatted;
            formatted = format(value);
            result.append(formatted).append(", ");
        }
        result.append("]");
        return result.toString();
    }

    @NotNull
    @Override
    public <R> R trace(@NotNull Object before, @NotNull R after, @NotNull Operations operationType, @NotNull Object... values) {
        String beforeStr = "";
        String afterStr = "";
        String afterNotes = "";
        String beforeNotes = "";
        if (after instanceof Value) {
            afterStr = format(after);
        } else {
            if (after != null) {
                afterStr = String.format("%s(%s)", after.toString(), after.getClass().getName());
            }
        }
        if (before instanceof Value) {
            beforeStr = format(before);
        } else {
            if (before != null) {
                beforeStr = String.format("%s(%s)", before.toString(), before.getClass().getName());
            }
        }
        if (((before instanceof DollarVoid) || (before == null)) && ((after instanceof DollarVoid) || (after == null))) {
            DollarStatic.log(String.format("%s%s: %s->%s",
                                           operationType,
                                           toDescription(values),
                                           beforeNotes,
                                           afterNotes));
        } else if ((before instanceof DollarVoid) || (before == null)) {
            DollarStatic.log(String.format("%s%s: %s%s", operationType, toDescription(values), afterStr, afterNotes));
        } else {
            DollarStatic.log(String.format("%s%s: %s%s -> %s%s",
                                           operationType,
                                           toDescription(values),
                                           beforeStr,
                                           beforeNotes,
                                           afterStr,
                                           afterNotes));
        }
        return after;
    }
}
