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

import com.sillelien.dollar.api.Pipeable;
import com.sillelien.dollar.api.var;
import dollar.internal.runtime.script.DollarScriptSupport;
import dollar.internal.runtime.script.api.DollarParser;
import org.jetbrains.annotations.NotNull;
import org.jparsec.Token;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.function.Function;

public class FunctionCallOperator implements Function<Token, var> {

    @NotNull
    private static final Logger log = LoggerFactory.getLogger("FunctionCallOperator");
    @NotNull
    private DollarParser parser;

    public FunctionCallOperator(@NotNull DollarParser parser) {this.parser = parser;}

    @NotNull
    @Override
    public var apply(@NotNull Token token) {
        Object[] objects = (Object[]) token.value();
        log.debug("objects[0]="+objects[0]);
        var node = DollarScriptSupport.createNode(false, "function-name", parser, token,
                                                  Collections.singletonList((var) objects[0]),
                                                  new Pipeable() {
                                                      @Override
                                                      public var pipe(var... args) throws Exception {
                                                          return (var) objects[0];
                                                      }
                                                  });
        return node;
    }
}
