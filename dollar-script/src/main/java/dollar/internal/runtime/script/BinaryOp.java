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

import dollar.api.script.Source;
import dollar.api.var;
import dollar.internal.runtime.script.api.DollarParser;
import dollar.internal.runtime.script.api.exceptions.DollarParserError;
import dollar.internal.runtime.script.parser.OpDef;
import dollar.internal.runtime.script.parser.OpDefType;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;

import static dollar.internal.runtime.script.DollarScriptSupport.node;
import static dollar.internal.runtime.script.DollarScriptSupport.reactiveNode;

public class BinaryOp implements BinaryOperator<var>, Operator {
    @NotNull
    private final BiFunction<var, var, var> function;
    private final boolean immediate;
    @NotNull
    private final OpDef operation;
    @NotNull
    private final DollarParser parser;
    private final boolean pure;
    @NotNull
    private Source source;


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
    public var apply(@NotNull var lhs, @NotNull var rhs) {
        if (immediate) {
            return node(operation, pure, parser, source, Arrays.asList(lhs, rhs), vars -> function.apply(lhs, rhs));

        }
        //Lazy evaluation
        return reactiveNode(operation, pure, parser, source, lhs, rhs, args -> function.apply(lhs, rhs));
    }

    @Override
    public void setSource(@NotNull Source source) {
        this.source = source;
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
        if (pure && (operation.pure() != null) && !operation.pure()) {
            throw new AssertionError("The operation " + operation.name() + " is marked as " + (operation.pure() ? "pure" : "impure") + " yet this operator is set to be " + (pure ? "pure" : "impure"));
        }
    }
}
