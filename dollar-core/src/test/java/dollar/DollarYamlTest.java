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

package dollar;

import dollar.api.DollarStatic;
import dollar.api.var;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;

public class DollarYamlTest {
    private static String yamlString1;
    private static String yamlString2;

    @BeforeAll
    public static void setUp() {
        yamlString1 = "\n- Hesperiidae\n- Papilionidae\n- Apatelodidae\n- Epiplemidae";
        yamlString2 = "map: \n  red: 1\n  blue: 2\n  green: leaves";
    }

    @Test
    public void testBasics() {
        var yamlList = DollarStatic.$yaml(yamlString1);
        assertEquals(4, yamlList.size());


        var yamlMap = DollarStatic.$yaml(yamlString2);
        assertEquals(1L, (long) yamlMap.$get(DollarStatic.$("map")).$get(DollarStatic.$("red")).toInteger());
        assertEquals(2L, (long) yamlMap.$get(DollarStatic.$("map")).$get(DollarStatic.$("blue")).toInteger());
        assertEquals("leaves", yamlMap.$get(DollarStatic.$("map")).$get(DollarStatic.$("green")).toString());

    }


}
