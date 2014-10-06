/*
 * Copyright (c) 2014-2014 Cazcade Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cazcade.dollar;

import java.math.BigDecimal;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class DollarString extends AbstractDollarSingleValue<String> {


    public DollarString(String value) {
        super(value);
    }

    @Override
    public String $() {
        return value;
    }

    @Override
    public Integer $int() {
        return Integer.parseInt(value);
    }

    @Override
    public Number $number(String key) {
        return new BigDecimal(key);
    }

    @Override
    public var copy() {
        return new DollarString(value);
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && value.equals(obj.toString());
    }
}
