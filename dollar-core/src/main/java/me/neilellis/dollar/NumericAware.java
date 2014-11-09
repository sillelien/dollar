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

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public interface NumericAware {

    default var $dec(String key) {
        return $dec(key, 1);
    }

    var $dec(String key, long amount);

    var $dec(long amount);

    default var $dec() {
        return $dec(1);
    }

    default var $inc(String key) {
        return $inc(key, 1);
    }

    var $inc(String key, long amount);

    var $inc(long amount);

    var $negate();

    default var $inc() {
        return $inc(1);
    }

    var $multiply(var v);

    var $divide(var v);

    var $modulus(var v);

    var $abs();
}
