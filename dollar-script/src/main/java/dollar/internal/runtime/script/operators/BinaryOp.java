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

package dollar.internal.runtime.script.operators;

import dollar.api.Value;
import dollar.api.script.DollarParser;
import dollar.api.script.Source;
import dollar.internal.runtime.script.api.Operator;
import dollar.internal.runtime.script.api.exceptions.DollarParserError;
import dollar.internal.runtime.script.parser.Op;
import dollar.internal.runtime.script.parser.OpType;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;

import static dollar.internal.runtime.script.DollarUtilFactory.util;

public class BinaryOp implements BinaryOperator<Value>, Operator {
    @NotNull
    private final BiFunction<Value, Value, Value> function;
    private final boolean immediate;
    @NotNull
    private final Op operation;
    @NotNull
    private final DollarParser parser;
    private final boolean pure;
    @NotNull
    private Source source;


    public BinaryOp(@NotNull DollarParser parser,
                    @NotNull Op operation,
                    @NotNull BiFunction<Value, Value, Value> function, boolean pure) {
        this.parser = parser;
        this.operation = operation;
        this.function = function;
        this.pure = pure;
        immediate = false;
        validate(operation);
    }

    public BinaryOp(boolean immediate,
                    @NotNull Op operation,
                    @NotNull DollarParser parser,
                    @NotNull BiFunction<Value, Value, Value> function, boolean pure) {
        this.immediate = immediate;
        this.function = function;
        this.operation = operation;
        this.parser = parser;
        this.pure = pure;
        validate(operation);
    }

    @NotNull
    @Override
    public Value apply(@NotNull Value lhs, @NotNull Value rhs) {
        if (immediate) {
            return util().node(operation, pure, parser, source, Arrays.asList(lhs, rhs),
                               vars -> function.apply(lhs, rhs));

        }
        //Lazy evaluation
        return util().reactiveNode(operation, pure, parser, source, lhs, rhs, args -> function.apply(lhs, rhs));
    }

    @Override
    public void setSource(@NotNull Source source) {
        this.source = source;
    }

    public void validate(@NotNull Op op) {
        if (op.reactive() == immediate) {
            throw new DollarParserError("The operation " + op.name() + " is marked as " + (op.reactive()
                                                                                                   ? "reactive" : "unreactive") + " " +
                                                "yet this operator is set to be " + (immediate
                                                                                             ? "unreactive" : "reactive"));
        }
        if (op.type() != OpType.BINARY) {
            throw new DollarParserError("The operator " + op.name() + " is defined as not BINARY but used in a binary " +
                                                "operator.");
        }
        Boolean opIsPure = op.pure();
        if (pure && (opIsPure != null) && !opIsPure) {
            throw new AssertionError(
                                            "The operation " + op.name() + " is marked as " + (opIsPure ? "pure" : "impure") + " yet this operator is set to be " + (pure ? "pure" : "impure"));
        }
    }
}
