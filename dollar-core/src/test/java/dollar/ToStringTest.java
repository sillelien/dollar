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
import dollar.api.Type;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;

public class ToStringTest {

    @Test
    public void test$S() throws Exception {

    }

    @Test
    public void testToDollarScript() throws Exception {

    }

    @Test
    public void testToHumanString() throws Exception {

    }

    @Test
    public void testToJsonString() throws Exception {
        assertEquals("\"test\"", DollarStatic.$("test").toJsonString());
        assertEquals("\"1\"", DollarStatic.$("1").toJsonString());
        assertEquals("1", DollarStatic.$(1).toJsonString());
        assertEquals("true", DollarStatic.$(true).toJsonString());
        assertEquals("{\"a\":1}", DollarStatic.$("{\"a\":1}").toJsonString());
        assertEquals("{\"a\":\"1\"}", DollarStatic.$("{\"a\":\"1\"}").toJsonString());
        assertEquals("null", DollarStatic.$null(Type._ANY).toJsonString());
    }

    @Test
    public void testToString() throws Exception {

    }
}
