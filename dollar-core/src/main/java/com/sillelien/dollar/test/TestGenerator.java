/*
 * Copyright (c) 2014-2015 Neil Ellis
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

package com.sillelien.dollar.test;/*
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

import com.sillelien.dollar.api.Type;
import com.sillelien.dollar.api.types.*;
import com.sillelien.dollar.api.var;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;

import static java.util.Collections.singletonMap;
import static com.sillelien.dollar.api.DollarStatic.$;
import static com.sillelien.dollar.api.DollarStatic.$range;

public class TestGenerator {

    @NotNull public static List<var> allValues() {
        List<var> all = new ArrayList<>();
        all.addAll(intValues());
        all.addAll(
                toVarValues(Float.MIN_VALUE, Float.MAX_VALUE, Double.MIN_VALUE, Double.MAX_VALUE, 0.0, 1.0, -1.0, 100.0,
                            -100.0, 0.5, -0.5, -0.1, 0.1));
        all.addAll(dateValues());
        all.addAll(stringValues());
        all.addAll(booleanValues());
        all.addAll(listValues());
        all.addAll(rangeValues());
        all.addAll(mapValues());
        all.addAll(infinities());
        all.addAll(emptyValues());
        return all;
    }

    @NotNull public static List<var> intValues() {
        return toVarValues(Integer.MIN_VALUE, Integer.MAX_VALUE, -Long.MAX_VALUE, Long.MAX_VALUE, 0, 1, -1, 100, -100);
    }

    @NotNull private static List<var> toVarValues(@NotNull Object... values) {
        ArrayList<var> result = new ArrayList<>();
        for (Object value : values) {
            result.add($(value));

        }
        return result;
    }

    @NotNull public static List<var> dateValues() {
        return toVarValues(Instant.ofEpochSecond(0), Instant.ofEpochSecond(-1), Instant.ofEpochSecond(1_000_000),
                           Instant.ofEpochSecond(2_000_000),
                           Instant.ofEpochSecond(3_000_000));
    }

    @NotNull public static List<var> stringValues() {
        return toVarValues("", "@", "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@", "@@ @@ @@@ @@ @@ @@", " ",
                           "ﬂ‡°·‚ÏÌÓÔ\uF8FF^øπåß∂ƒ©˙∆˚¬…æ…;…",
                           "\n\t\r", "1", "true", "false", "0");
    }

    @NotNull public static List<var> booleanValues() {
        return toVarValues(true, false);
    }

    @NotNull public static List<var> listValues() {
        return toVarValues(intValues(),
                           toVarValues(Float.MIN_VALUE, Float.MAX_VALUE, Double.MIN_VALUE, Double.MAX_VALUE, 0.0, 1.0,
                                       -1.0, 100.0,
                                       -100.0, 0.5, -0.5, -0.1, 0.1), dateValues(), stringValues(), booleanValues(),
                           Arrays.asList());
    }

    @NotNull public static List<var> rangeValues() {
        return toVarValues($range(Instant.ofEpochSecond(1_000_000), Instant.ofEpochSecond(2_000_000)), $range(-1, 1),
                           $range(0,
                                  -1), $range(
                        0, 0), $range(1, -1), $range(0, Long.MAX_VALUE), $range(-Long.MAX_VALUE, Long.MAX_VALUE),
                           $range("a", "z"), $range("@", "\uF8FF"), $range("z", "a"), $range(-0.1, -0.05),
                           $range(1, -0.1));
    }

    @NotNull public static List<var> mapValues() {
        HashMap<Object, Object> mixedMap = new HashMap<>();
        mixedMap.put(1, 0.1);
        mixedMap.put(true, 10);
        mixedMap.put("value1", true);
        mixedMap.put("value2", true);
        mixedMap.put("value3", true);
        mixedMap.put(true, "true");
        return toVarValues(Collections.EMPTY_MAP, singletonMap(1, "one"), singletonMap(true, "true"),
                           singletonMap(Instant.ofEpochSecond(1_000_000), true), singletonMap(0.1, "0.1"),
                           singletonMap("string", "string"),
                           mixedMap);
    }

    @NotNull public static List<var> infinities() {
        return Arrays.asList(new DollarInfinity(true), new DollarInfinity(false));
    }

    @NotNull public static List<var> emptyValues() {
        return Arrays.asList(new DollarNull(Type.STRING), new DollarNull(Type.INTEGER), new DollarNull(Type.ANY),
                             new DollarVoid(), DollarFactory.failure(ErrorType.EXCEPTION,
                                                                     "Test Exception", true));
    }

    @NotNull public static List<var> small() {
        List<var> all = new ArrayList<>();
        all.addAll(smallIntValues());
        all.addAll(smallDecimalValues());
        all.addAll(smallDateValues());
        all.addAll(stringValues());
        all.addAll(booleanValues());
        all.addAll(smallListValues());
        all.addAll(smallRangeValues());
        all.addAll(smallMapValues());
        all.addAll(emptyValues());
        return all;
    }

    @NotNull public static List<var> noSmallDecimals() {
        List<var> all = new ArrayList<>();
        all.addAll(largeIntValues());
        all.addAll(largeDecimalValues());
        all.addAll(largeDateValues());
        all.addAll(stringValues());
        all.addAll(booleanValues());
        all.addAll(largeListValues());
        all.addAll(largeRangeValues());
        all.addAll(largeMapValues());
        return all;
    }

    @NotNull public static List<var> minimal() {
        List<var> all = new ArrayList<>();
        all.addAll(toVarValues(0, 1, -1));
        all.addAll(toVarValues(0.1));
        all.addAll(toVarValues(Instant.ofEpochSecond(1)));
        all.addAll(toVarValues("", "@"));
        all.addAll(booleanValues());
        all.addAll(toVarValues(toVarValues(0, 1, -1)));
        all.addAll(toVarValues($range(-1, 1)));
        all.addAll(minimalEmptyValues());
        HashMap<Object, Object> mixedMap = new HashMap<>();
        mixedMap.put(1, 0.1);
        mixedMap.put(true, 5);
        mixedMap.put("value1", true);
        all.addAll(toVarValues(mixedMap));
        return all;
    }

    @NotNull public static List<var> minimalEmptyValues() {
        return Arrays.asList(new DollarNull(Type.ANY), new DollarVoid(), DollarFactory.failure(ErrorType.EXCEPTION,
                                                                                               "Test Exception", true));
    }

    @NotNull public static List<var> test1Param(@NotNull List<var> values, @NotNull Function<var, var> oneParam) {
        List<var> result = new ArrayList<>();
        for (var value : values) {
            result.add(oneParam.apply(value));
        }
        return result;
    }

    @NotNull
    public static List<List<var>> testBinary(@NotNull List<var> values1, @NotNull List<var> values2,
                                             @NotNull Function<List<var>, var> twoParam) {
        List<List<var>> result = new ArrayList<>();
        for (var value1 : values1) {
            for (var value2 : values2) {
                try {
                    final var resultValue = twoParam.apply(Arrays.asList(value1, value2));
                    result.add(Arrays.asList(value1, value2, resultValue));
                } catch (Exception e) {
                    result.add(Arrays.asList(value1, value2,
                                             DollarFactory.failure(ErrorType.EXCEPTION, e, true)));
                }
            }
        }
        return result;
    }

    @NotNull public static List<var> largeIntValues() {
        return intValues();
    }

    @NotNull public static List<var> smallIntValues() {
        return toVarValues(0, 1, -1, 10, -10);
    }

    @NotNull public static List<var> largeDecimalValues() {
        return toVarValues(-Float.MAX_VALUE, Float.MAX_VALUE, -Double.MAX_VALUE, Double.MAX_VALUE, 0.0, 1.0, -1.0,
                           100.0,
                           -100.0, 0.5, -0.5, -0.1, 0.1);
    }

    @NotNull public static List<var> smallDecimalValues() {
        return toVarValues(0.0, 1.0, -1.0, 10.0,
                           -10.0, 0.5, -0.5, -0.1, 0.1);
    }

    @NotNull public static List<var> largeDateValues() {
        return toVarValues(Instant.ofEpochSecond(1_000_000), Instant.ofEpochSecond(2_000_000),
                           Instant.ofEpochSecond(3_000_000));
    }

    @NotNull public static List<var> smallDateValues() {
        return toVarValues(Instant.ofEpochSecond(0), Instant.ofEpochSecond(-1));
    }

    @NotNull public static List<var> largeListValues() {
        return toVarValues(largeIntValues(), largeDecimalValues(), largeDateValues(), stringValues(), booleanValues(),
                           Arrays.asList());
    }

    @NotNull public static List<var> smallListValues() {
        return toVarValues(smallIntValues(), smallDecimalValues(), smallDateValues(), stringValues(), booleanValues(),
                           smallRangeValues(),
                           Arrays.asList());
    }

    @NotNull public static List<var> largeRangeValues() {
        return toVarValues($range(Instant.ofEpochSecond(1_000_000), Instant.ofEpochSecond(1_100_000)), $range(-1, 1),
                           $range(0,
                                  -1), $range(
                        0, 0), $range(1, -1), $range(0, Long.MAX_VALUE), $range(-Long.MAX_VALUE, Long.MAX_VALUE),
                           $range("a", "z"), $range("@", "\uF8FF"), $range("z", "a"), $range(-0.1, -0.05),
                           $range(1, -0.1));
    }

    @NotNull public static List<var> smallRangeValues() {
        return toVarValues($range(Instant.ofEpochSecond(1), Instant.ofEpochSecond(1)), $range(-1, 1),
                           $range(0,
                                  -1), $range(
                        0, 0), $range(1, -1), $range(0, 5), $range(-5, 5),
                           $range("a", "d"), $range("s", "a"), $range(-0.1, -0.05),
                           $range(1, -0.1));
    }

    @NotNull public static List<var> smallMapValues() {
        HashMap<Object, Object> mixedMap = new HashMap<>();
        mixedMap.put(1, 0.1);
        mixedMap.put(true, 5);
        mixedMap.put("value1", true);
        mixedMap.put("value2", true);
        mixedMap.put("value3", true);
        mixedMap.put(true, "true");
        return toVarValues(Collections.EMPTY_MAP, singletonMap(1, "one"), singletonMap(true, "true"),
                           singletonMap(Instant.ofEpochSecond(1_000_000), true), singletonMap(0.1, "0.1"),
                           singletonMap("string", "string"),
                           mixedMap);
    }

    @NotNull public static List<var> largeMapValues() {
        return mapValues();
    }
}
