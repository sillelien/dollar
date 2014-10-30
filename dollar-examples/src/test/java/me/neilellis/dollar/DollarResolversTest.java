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

import static org.junit.Assert.assertEquals;

public class DollarResolversTest {

    @BeforeClass
    public static void setUp() {

    }

    @Test
    public void testClass() {
        assertEquals("world", DollarStatic.$().$pipe("class:me.neilellis.dollar.TestPipe").$("hello").$S());
    }

    @Test
    public void testGithub() {
        assertEquals("Hello World", DollarStatic.$().$pipe("github:neilellis/dollar-example-module").$S());
    }

    @Test
    public void testMaven() {
//        assertEquals("hello",DollarStatic.$("hello").$pipe("mvn:me.neilellis.dollar.script.TestPipe:me.neilellis:dollar-script:0-SNAPSHOT").$S());
    }

}