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

package dollar.internal.runtime.script;

import dollar.api.var;
import org.jetbrains.annotations.NotNull;
import org.jparsec.functors.Unary;

enum UnaryOperator implements Unary<var> {
    ERR {
        @NotNull public var map(@NotNull var val) {
            return val.out();
        }
    },
    OUT {
        @NotNull public var map(@NotNull var val) {
            return val.out();
        }
    },
    DEC {
        @NotNull public var map(@NotNull var val) {
            return val.$dec();
        }
    }
}
