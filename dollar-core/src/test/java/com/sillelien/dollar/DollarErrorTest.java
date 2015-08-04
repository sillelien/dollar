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

package com.sillelien.dollar;

import com.sillelien.dollar.api.var;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.sillelien.dollar.api.DollarStatic.$list;
import static org.junit.Assert.*;

public class DollarErrorTest {
  private static var list;

  @BeforeClass
  public static void setUp() {
    list = $list("Neil", "Dimple", "Charlie");
  }

  @Test
  public void testBasics() {
    assertTrue(list.$pipe((i) -> {
      throw new NullPointerException();
    }).hasErrors());

    assertFalse(list.$pipe((i) -> {
      throw new NullPointerException();
    }).isVoid());

    assertTrue(list.$pipe((i) -> {
      throw new NullPointerException();
    }).clearErrors().equals(list));

    assertFalse(list.$pipe((i) -> {
      throw new NullPointerException();
    }).clearErrors().hasErrors());

    list.$pipe((i) -> {
      throw new NullPointerException();
    }).clearErrors().$fail((i) -> {
      fail();
    });

    final boolean[] failed = {false};
    list.$pipe((i) -> {
      throw new NullPointerException();
    }).$fail((i) -> {
      failed[0] = true;
    });
    assertTrue(failed[0]);
  }

}