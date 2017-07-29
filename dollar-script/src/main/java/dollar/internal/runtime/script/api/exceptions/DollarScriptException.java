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

package dollar.internal.runtime.script.api.exceptions;

import com.sillelien.dollar.api.DollarException;
import com.sillelien.dollar.api.var;

public class DollarScriptException extends DollarException {
    public DollarScriptException(Throwable e) {
        super(e);
    }

    public DollarScriptException(String errorMessage) {
        super(errorMessage);
    }

    public DollarScriptException(Throwable t, String s) {
        super(t, s);
    }

    public DollarScriptException(String s, var rhs) {
        super(s+":\n"+rhs._source().getSourceMessage());
    }
}
