/*
 * Copyright (c) 2014 Neil Ellis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.innowhere.relproxy.impl.jproxy.core.clsmgr;

import com.innowhere.relproxy.impl.jproxy.JProxyUtil;

import java.io.File;

/**
 * @author jmarranz
 */
public class SourceScriptFileOtherExt extends SourceScriptFile {
    public SourceScriptFileOtherExt(File sourceFile) {
        super(sourceFile);
    }

    @Override
    public String getScriptCode(String encoding, boolean[] hasHashBang) {
        String codeBody = JProxyUtil.readTextFile(sourceFile, encoding);
        // Eliminamos la primera línea #!  (debe estar en la primera línea y sin espacios antes)
        if (codeBody.startsWith("#!")) {
            hasHashBang[0] = true;
            int pos = codeBody.indexOf('\n');
            if (pos != -1) // Rarísimo que sólo esté el hash bang (script vacío)
            {
                codeBody = codeBody.substring(pos + 1);
            }
        } else hasHashBang[0] = false;
        return codeBody;
    }
}
