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

import com.sillelien.dollar.api.script.SourceSegment;
import com.sillelien.dollar.api.var;
import dollar.internal.runtime.script.DollarScriptSupport;
import dollar.internal.runtime.script.Operator;
import dollar.internal.runtime.script.api.DollarParser;
import dollar.internal.runtime.script.api.Scope;
import dollar.internal.runtime.script.api.exceptions.VariableNotFoundException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jparsec.functors.Binary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.UUID;

import static dollar.internal.runtime.script.DollarScriptSupport.createNode;
import static dollar.internal.runtime.script.DollarScriptSupport.currentScope;

public class ListenOperator implements Binary<var>, Operator {
    @NotNull
    private static final Logger log = LoggerFactory.getLogger("ListenOperator");

    private final boolean pure;
    @Nullable
    private SourceSegment source;
    @NotNull
    private DollarParser parser;


    public ListenOperator(boolean pure, @NotNull DollarParser parser) {
        this.pure = pure;
        this.parser = parser;
    }


    @NotNull
    @Override
    public var map(@NotNull var lhs, @NotNull var rhs) {
        String id = UUID.randomUUID().toString();
        return createNode(true, "listen-"+ id, parser, source, Arrays.asList(lhs, rhs), in -> {
            log.debug("Listening to " + lhs.getMetaObject("operation"));
            log.debug("Listening to " + lhs._source().getSourceMessage());
            String lhsFix = lhs.getMetaAttribute("variable");
            Scope scopeForVar = DollarScriptSupport.getScopeForVar(pure, lhsFix.toString(), false,
                                                                   null);
            if(scopeForVar == null) {
                throw new VariableNotFoundException(lhsFix.toString(),currentScope());
            }
            scopeForVar
                           .listen(lhsFix.toString(), id, rhs);
            return lhs;
        });
    }

    @Override
    public void setSource(@NotNull SourceSegment source) {
        this.source = source;
    }
}
