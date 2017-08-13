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

import com.sillelien.dollar.api.types.DollarFactory;
import com.sillelien.dollar.api.var;
import dollar.internal.runtime.script.DollarScriptSupport;
import dollar.internal.runtime.script.SourceNodeOptions;
import dollar.internal.runtime.script.UnaryOp;
import dollar.internal.runtime.script.api.DollarParser;
import org.jetbrains.annotations.NotNull;

import static dollar.internal.runtime.script.parser.Symbols.READ;

public class SimpleReadOperator extends UnaryOp {


    public SimpleReadOperator(@NotNull DollarParser parser) {
        super(parser, READ, null);
    }


    @NotNull
    @Override
    public var map(@NotNull var from) {
        return DollarScriptSupport.createReactiveNode(operation.name(), SourceNodeOptions.NO_SCOPE, source, parser, from,
                                                      args -> DollarFactory.fromURI(from).$read()
        );
    }

}
