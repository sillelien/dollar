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

import com.sillelien.dollar.api.Pipeable;
import com.sillelien.dollar.api.script.SourceSegment;
import com.sillelien.dollar.api.var;
import dollar.internal.runtime.script.api.DollarParser;
import org.jparsec.functors.Map;
import org.jparsec.functors.Unary;

import java.util.Collections;

public class UnaryOp implements Unary<var>, Operator {
    protected final String operation;
    private final boolean immediate;
    protected SourceSegment source;
    protected DollarParser parser;
    private Map<var, var> function;


    public UnaryOp(String operation, Map<var, var> function, DollarParser parser) {
        this.operation = operation;
        this.function = function;
        this.parser = parser;
        this.immediate = false;
    }

    public UnaryOp(boolean immediate,
                   Map<var, var> function,
                   String operation,
                   DollarParser parser) {
        this.operation = operation;
        this.immediate = immediate;
        this.function = function;
        this.parser = parser;
    }

    @Override
    public var map(var from) {

        if (immediate) {
            final var lambda = DollarScriptSupport.createNode(false, operation, parser,
                                                              source,
                                                              Collections.singletonList(from),
                                                              new Pipeable() {
                                                                  @Override
                                                                  public var pipe(var... vars) throws Exception {
                                                                      return function.map(from);
                                                                  }
                                                              }
            );
            return lambda;

        }

        //Lazy evaluation
        final var lambda = DollarScriptSupport.createReactiveNode(false, operation, source, parser, from,
                                                                  args -> function.map(from));
        return lambda;

    }


    @Override
    public void setSource(SourceSegment source) {
        this.source = source;
    }
}
