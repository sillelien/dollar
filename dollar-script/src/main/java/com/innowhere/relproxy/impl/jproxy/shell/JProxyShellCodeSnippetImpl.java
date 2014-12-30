/*
 * Copyright (c) 2014-2015 Neil Ellis
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

package com.innowhere.relproxy.impl.jproxy.shell;

import com.innowhere.relproxy.RelProxyException;
import com.innowhere.relproxy.impl.jproxy.JProxyConfigImpl;
import com.innowhere.relproxy.impl.jproxy.core.clsmgr.ClassDescriptorSourceScript;
import com.innowhere.relproxy.impl.jproxy.core.clsmgr.SourceScript;
import com.innowhere.relproxy.impl.jproxy.core.clsmgr.SourceScriptInMemory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;

/**
 * @author jmarranz
 */
public class JProxyShellCodeSnippetImpl extends JProxyShellImpl {
    public void init(@NotNull String[] args) {
        super.init(args, null);
    }

    @Override
    protected void processConfigParams(
            @NotNull String[] args, @NotNull LinkedList<String> argsToScript, @NotNull JProxyConfigImpl config) {
        super.processConfigParams(args, argsToScript, config);

        String classFolder = config.getClassFolder();
        if (classFolder != null && !classFolder.trim().isEmpty())
            throw new RelProxyException("cacheClassFolder is useless to execute a code snippet");

        // No tiene sentido especificar un tiempo de scan porque no hay directorio de entrada en el que escanear archivos
        if (config.getScanPeriod() >= 0) // 0 no puede ser porque da error antes pero lo ponemos para reforzar la idea
            throw new RelProxyException("scanPeriod positive value has no sense in code snippet execution");
    }

    @NotNull @Override
    protected SourceScript getSourceScript(String[] args, @NotNull LinkedList<String> argsToScript) {
        // En argsToScript no está el args[0] ni falta que hace porque es el flag "-c"
        StringBuilder code = new StringBuilder();
        for (String chunk : argsToScript)
            code.append(chunk);
        return SourceScriptInMemory.createSourceScriptInMemory(code.toString());
    }

    @Nullable @Override
    protected JProxyShellClassLoader getJProxyShellClassLoader(JProxyConfigImpl config) {
        // No hay classFolder => no hay necesidad de nuevo ClassLoader
        return null;
    }

    @Override
    protected void executeFirstTime(@NotNull ClassDescriptorSourceScript scriptFileDesc,
                                    @NotNull LinkedList<String> argsToScript, JProxyShellClassLoader classLoader) {
        try {
            scriptFileDesc.callMainMethod(argsToScript);
        } catch (Throwable ex) {
            ex.printStackTrace(System.out);
        }
    }

}
