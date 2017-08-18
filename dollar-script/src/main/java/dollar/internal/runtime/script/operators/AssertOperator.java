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

import com.sillelien.dollar.api.var;
import dollar.internal.runtime.script.DollarScriptSupport;
import dollar.internal.runtime.script.SourceNodeOptions;
import dollar.internal.runtime.script.SourceSegmentValue;
import dollar.internal.runtime.script.api.DollarParser;
import dollar.internal.runtime.script.api.exceptions.DollarScriptException;
import org.jetbrains.annotations.NotNull;
import org.jparsec.Token;
import org.jparsec.functors.Map;

import static com.sillelien.dollar.api.DollarStatic.$void;

public class AssertOperator implements Map<Token, var> {

    private DollarParser parser;

    public AssertOperator(DollarParser parser, boolean pure) {
        this.parser = parser;
    }

    @NotNull
    @Override
    public var map(@NotNull Token token) {
        final SourceSegmentValue source = new SourceSegmentValue(DollarScriptSupport.currentScope(), token);
        Object[] objects = (Object[]) token.value();
        return DollarScriptSupport.reactiveNode("assert", SourceNodeOptions.NO_SCOPE, parser, token, (var) objects[1],
                                                args -> {
                                                    if (((var) objects[1]).isTrue()) { return $void(); } else {
                                                        throw new DollarScriptException("Assertion failed: " +
                                                                                                (objects[0] != null ? objects[0] : "") +
                                                                                                " : " +
                                                                                                source.getSourceMessage());
                                                    }
                                                });
    }
}
