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

import static me.neilellis.dollar.DollarStatic.$;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public interface NumericAware {


    var $dec(var key, var amount);

    default var $dec(String key, var amount) {
        return $dec($(key), amount);
    }

    default var $dec(String key, Number amount) {
        return $dec($(key), $(amount));
    }

    var $dec(var amount);

    default var $dec(Number amount) {
        return $dec($(amount));
    }

    default var $dec() {
        return $dec($(1));
    }


    var $inc(var key, var amount);

    default var $inc(String key, var amount) {
        return $inc($(key), amount);
    }

    default var $inc(String key, Number amount) {
        return $inc($(key), $(amount));
    }

    var $inc(var amount);

    default var $inc(Number amount) {
        return $inc($(amount));
    }

    var $negate();

    default var $inc() {
        return $inc($(1));
    }

    var $multiply(var v);

    var $divide(var v);

    var $modulus(var v);

    var $abs();
}
