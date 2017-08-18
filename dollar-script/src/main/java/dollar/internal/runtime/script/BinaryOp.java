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
import dollar.internal.runtime.script.api.exceptions.DollarParserError;
import dollar.internal.runtime.script.parser.OpDef;
import dollar.internal.runtime.script.parser.OpDefType;
import org.jetbrains.annotations.NotNull;
import org.jparsec.functors.Binary;

import java.util.Arrays;
import java.util.function.BiFunction;

public class BinaryOp implements Binary<var>, Operator {
    private final boolean immediate;
    @NotNull
    private final BiFunction<var, var, var> function;
    @NotNull
    private final OpDef operation;
    @NotNull
    private SourceSegment source;
    @NotNull
    private final DollarParser parser;
    private final boolean pure;


    public BinaryOp(@NotNull DollarParser parser,
                    @NotNull OpDef operation,
                    @NotNull BiFunction<var, var, var> function, boolean pure) {
        this.parser = parser;
        this.operation = operation;
        this.function = function;
        this.pure = pure;
        immediate = false;
        validate(operation);
    }

    public void validate(@NotNull OpDef operation) {
        if (operation.reactive() == immediate) {
            throw new DollarParserError("The operation " + operation.name() + " is marked as " + (operation.reactive()
                                                                                                          ? "reactive" : "unreactive") + " " +
                                                "yet this operator is set to be " + (immediate
                                                                                             ? "unreactive" : "reactive"));
        }
        if (operation.type() != OpDefType.BINARY) {
            throw new DollarParserError("The operator " + operation.name() + " is defined as not BINARY but used in a binary " +
                                                "operator.");
        }
        if (pure && !operation.pure()) {
            throw new AssertionError("The operation " + operation.name() + " is marked as " + (operation.pure() ? "pure" : "impure") + " yet this operator is set to be " + (pure ? "pure" : "impure"));
        }
    }

    public BinaryOp(boolean immediate,
                    @NotNull OpDef operation,
                    @NotNull DollarParser parser,
                    @NotNull BiFunction<var, var, var> function, boolean pure) {
        this.immediate = immediate;
        this.function = function;
        this.operation = operation;
        this.parser = parser;
        this.pure = pure;
        validate(operation);
    }

    @NotNull
    @Override
    public var map(@NotNull var lhs, @NotNull var rhs) {
        if (immediate) {
            final var lambda = DollarScriptSupport.node(operation.name(), pure, SourceNodeOptions.NO_SCOPE, parser,
                                                        source,
                                                        Arrays.asList(lhs, rhs),
                                                        new Pipeable() {
                                                            @Override
                                                            public var pipe(var... vars) throws Exception {
                                                                return function.apply(lhs, rhs);
                                                            }
                                                        });
            return lambda;

        }
        //Lazy evaluation
        return DollarScriptSupport.reactiveNode(operation.name(), pure, SourceNodeOptions.NO_SCOPE, parser, source, lhs, rhs,
                                                args -> function.apply(lhs, rhs));
    }

    @Override
    public void setSource(@NotNull SourceSegment source) {
        this.source = source;
    }
}
