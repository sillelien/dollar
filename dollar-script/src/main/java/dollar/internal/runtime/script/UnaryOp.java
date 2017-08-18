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
import dollar.internal.runtime.script.parser.OpDef;
import dollar.internal.runtime.script.parser.OpDefType;
import org.jetbrains.annotations.NotNull;
import org.jparsec.functors.Unary;

import java.util.Collections;
import java.util.function.Function;

public class UnaryOp implements Unary<var>, Operator {
    protected final OpDef operation;
    private final boolean immediate;
    protected SourceSegment source;
    protected DollarParser parser;
    private Function<var, var> function;

    private boolean pure;

    public UnaryOp(DollarParser parser, OpDef operation, Function<var, var> function, boolean pure) {
        this.operation = operation;
        this.function = function;
        this.parser = parser;
        this.pure = pure;
        immediate = false;
        validate(operation);
    }

    public UnaryOp(boolean immediate,
                   Function<var, var> function,
                   OpDef operation,
                   DollarParser parser, boolean pure) {
        this.operation = operation;
        this.immediate = immediate;
        this.function = function;
        this.parser = parser;
        this.pure = pure;
        validate(operation);
    }

    public void validate(OpDef operation) {
        if (operation.reactive() == immediate) {
            throw new AssertionError("The operation " + operation.name() + " is marked as " + (operation.reactive()
                                                                                                       ? "reactive" : "unreactive") + " " +
                                             "yet this operator is set to be " + (immediate
                                                                                          ? "unreactive" : "reactive"));
        }
        if (operation.type() != OpDefType.PREFIX && operation.type() != OpDefType.POSTFIX) {
            throw new AssertionError("The operator " + operation.name() + " is not defined as a unary type but used in a unary " +
                                             "operator.");
        }
        if (pure && !operation.pure()) {
            throw new AssertionError("The operation " + operation.name() + " is marked as " + (operation.pure() ? "pure" : "impure") + " yet this operator is set to be " + (pure ? "pure" : "impure"));
        }
    }

    @NotNull
    @Override
    public var map(@NotNull var from) {

        if (immediate) {
            final var lambda = DollarScriptSupport.createNode(operation.name(), SourceNodeOptions.NO_SCOPE, parser,
                                                              source,
                                                              Collections.singletonList(from),
                                                              new Pipeable() {
                                                                  @Override
                                                                  public var pipe(var... vars) throws Exception {
                                                                      return function.apply(from);
                                                                  }
                                                              });
            return lambda;

        }

        //Lazy evaluation
        final var lambda = DollarScriptSupport.reactiveNode(operation.name(), SourceNodeOptions.NO_SCOPE, source, parser,
                                                            from,
                                                            args -> function.apply(from));
        return lambda;

    }


    @Override
    public void setSource(SourceSegment source) {
        this.source = source;
    }
}
