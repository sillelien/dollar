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

import com.sillelien.dollar.api.script.SourceSegment;
import com.sillelien.dollar.api.var;
import dollar.internal.runtime.script.api.DollarParser;
import org.jparsec.functors.Binary;
import org.jparsec.functors.Map2;

import static dollar.internal.runtime.script.DollarScriptSupport.wrapReactive;

public class BinaryOp implements Binary<var>, Operator {
    private final boolean immediate;
    private final Map2<var, var, var> function;
    private final String operation;
    private SourceSegment source;
    private DollarParser parser;


    public BinaryOp(DollarParser parser, String operation, Map2<var, var, var> function) {
        this.parser = parser;
        this.operation = operation;
        this.function = function;
        this.immediate = false;
    }

    public BinaryOp(boolean immediate, Map2<var, var, var> function, String operation, DollarParser parser) {
        this.immediate = immediate;
        this.function = function;
        this.operation = operation;
        this.parser = parser;
    }

    @Override
    public var map(var lhs, var rhs) {
        if (immediate) {
            return function.map(lhs, rhs);
        }
        //Lazy evaluation
        return wrapReactive( () -> function.map(lhs, rhs), source, operation, lhs, rhs, parser);
    }

    @Override
    public void setSource(SourceSegment source) {
        this.source = source;
    }
}
