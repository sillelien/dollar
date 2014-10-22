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

package me.neilellis.dollar.json;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JsonUtilTest {

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testArgsToJson() throws Exception {
        final JsonObject jsonObject = JsonUtil.argsToJson(Arrays.asList("--remove",
                "filename",
                "-add",
                "file1", "file2",
                "-send",
                "fileA", "fileB", "fileC",
                "--count", "1",
                "--rate", "2.343434",
                "--quiet", "false",
                "--fast"));
        System.out.println(jsonObject);
        assertEquals("filename", jsonObject.getString("remove"));
        assertEquals(true, jsonObject.getBoolean("fast"));
        assertEquals(false, jsonObject.getBoolean("quiet"));
        assertEquals(1, (long) jsonObject.getInteger("count"));
        assertEquals(3, (long) jsonObject.getArray("send").size());
        assertEquals(2, (long) jsonObject.getArray("add").size());
        assertEquals(2.343434, jsonObject.getNumber("rate").floatValue(), 0.001);

    }

    @Test
    public void testMore() throws Exception {
        final JsonObject jsonObject = JsonUtil.argsToJson(Arrays.asList("--test", "1", "2", "3"));
        System.out.println(jsonObject);
        assertEquals(3, (long) jsonObject.getArray("test").size());
        assertTrue(jsonObject.getArray("test").get(2) instanceof Long);
    }
}