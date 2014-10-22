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

import org.junit.BeforeClass;
import org.junit.Test;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static me.neilellis.dollar.DollarStatic.$;
import static me.neilellis.dollar.DollarStatic.$list;

public class DollarLambdaTest {
    private static var list;

    @BeforeClass
    public static void setUp() {
        list = $list("Neil", "Dimple", "Charlie");
    }

    @Test
    public void testBasics() throws InterruptedException {
        var lambda = $((v) -> {
            return $(System.currentTimeMillis());
        });
        long time = System.currentTimeMillis();
        System.out.println(time);
        Thread.sleep(50);
        System.out.println(lambda.L());
        assertTrue(lambda.L() > time);
        assertTrue(lambda.isLambda());

    }

    @Test
    public void testIdentityEqualsHashCode() throws InterruptedException {
        var lambda = $((v) -> {
            return $("Hello World");
        });
        System.out.println(lambda);


    }


    @Test
    public void testErrors() throws InterruptedException {
        var lambda = $((v) -> {
            throw new RuntimeException("OMG!");
        });
        //At this point the lambda is not evaluated (hasErrors does not trigger evaluation)
        assertFalse(lambda.hasErrors());
        //Now perform any operation to trigger the exception and we have errors
        assertTrue(lambda.$dec().hasErrors());

    }

}