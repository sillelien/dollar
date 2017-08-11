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
import dollar.internal.runtime.script.api.DollarParser;
import dollar.internal.runtime.script.api.Scope;
import org.jetbrains.annotations.NotNull;
import org.jparsec.Token;
import org.jparsec.functors.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.sillelien.dollar.api.DollarStatic.$void;
import static dollar.internal.runtime.script.DollarScriptSupport.createNode;

public class BlockOperator implements Map<Token, var> {

    @NotNull
    private static final Logger log = LoggerFactory.getLogger("BlockOperator");

    @NotNull
    private final DollarParser dollarParser;
    private final boolean pure;

    public BlockOperator(@NotNull DollarParser dollarParser, boolean pure) {
        this.dollarParser = dollarParser;
        this.pure = pure;
    }

    @Override
    public var map(@NotNull Token token) {
        List<var> l = (List<var>) token.value();
        return createNode(true, false, "block", dollarParser, token, l, in1 -> {
            List<Scope> attachedScopes = new ArrayList<>(DollarScriptSupport.scopes());
            return createNode(false, true, "block-closure", dollarParser, token, l, in2 -> {

                    if (l.size() > 0) {
                        for (int i = 0; i < l.size() - 1; i++) {
                            l.get(i)._fixDeep(false);
                        }
                        return l.get(l.size() - 1);
                    } else {
                        return $void();
                    }



            });

        });
    }

}
