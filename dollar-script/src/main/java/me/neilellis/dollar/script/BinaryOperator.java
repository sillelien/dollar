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

package me.neilellis.dollar.script;

import me.neilellis.dollar.DollarStatic;
import me.neilellis.dollar.var;
import org.codehaus.jparsec.functors.Binary;

import java.io.File;

import static me.neilellis.dollar.DollarStatic.$;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
enum BinaryOperator implements Binary<var> {
    CHOOSE {
        public var map(var lhs, var rhs) {
            return lhs.$choose(rhs);
        }
    },
    GET {
        public var map(var lhs, var rhs) {
            return lhs.$(rhs.$S());
        }
    },
    PAIR {
        public var map(var lhs, var rhs) {
            return $(lhs.$S(), rhs);
        }
    },
    OUT {
        public var map(var lhs, var rhs) {
            return lhs.$write(new File(rhs.$S()));
        }
    },
    ERR {
        public var map(var lhs, var rhs) {
            return lhs.$write(new File(rhs.$S()));
        }
    },
    WRITE {
        public var map(var lhs, var rhs) {
            return lhs.$write(new File(rhs.$S()));
        }
    },
    READ {
        public var map(var lhs, var rhs) {
            return lhs.$read(new File(rhs.$S()));
        }
    },
    LOAD {
        public var map(var lhs, var rhs) {
            return lhs.$load(rhs.$S());
        }
    },
    SAVE {
        public var map(var lhs, var rhs) {
            return lhs.$save(rhs.$S());
        }
    },
    APPEND {
        public var map(var lhs, var rhs) {
            return lhs.$append(rhs);
        }
    },
    REMOVE {
        public var map(var lhs, var rhs) {
            return lhs.$rm(rhs.$S());
        }
    },
    ELVIS {
        public var map(var lhs, var rhs) {
            return lhs.$default(rhs);
        }
    },
    PIPE {
        public var map(var lhs, var rhs) {
            try {
                return lhs.$pipe(rhs.$S());
            } catch (Exception e) {
                return DollarStatic.handleError(e, lhs);
            }
        }
    },
    PUB {
        public var map(var lhs, var rhs) {
            return lhs.$pub(rhs.S());
        }
    },
    SEND {
        public var map(var lhs, var rhs) {
            return lhs.$send(rhs.S());
        }
    },
    DISPATCH {
        public var map(var lhs, var rhs) {
            lhs.$dispatch(rhs.S());
            return lhs;
        }
    },
}

