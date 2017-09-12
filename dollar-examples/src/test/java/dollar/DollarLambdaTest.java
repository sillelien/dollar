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

import static dollar.api.DollarStatic.$;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DollarLambdaTest {
    private static var list;

    @BeforeAll
    public static void setUp() {
        list = DollarStatic.$list("Neil", "Dimple", "Charlie");
    }

    @Test
    public void testBasics() throws InterruptedException {
        var lambda = $((v) -> DollarStatic.$(System.currentTimeMillis()));
        long time = System.currentTimeMillis();
        System.out.println(time);
        Thread.sleep(50);
        System.out.println(lambda.toLong());
        assertTrue(lambda.toLong() > time);
        assertTrue(lambda.dynamic());

    }

    @Test
    public void testIdentityEqualsHashCode() {
        var lambda = $((v) -> DollarStatic.$("Hello World"));
        assertEquals("Hello World".hashCode(), lambda.hashCode());
        assertEquals(lambda, "Hello World");
    }


}
