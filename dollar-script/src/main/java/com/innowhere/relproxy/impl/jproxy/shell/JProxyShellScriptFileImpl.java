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
import com.innowhere.relproxy.impl.jproxy.JProxyUtil;
import com.innowhere.relproxy.impl.jproxy.core.clsmgr.ClassDescriptorSourceScript;
import com.innowhere.relproxy.impl.jproxy.core.clsmgr.SourceScript;
import com.innowhere.relproxy.impl.jproxy.core.clsmgr.SourceScriptFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.LinkedList;

/**
 * @author jmarranz
 */
public class JProxyShellScriptFileImpl extends JProxyShellImpl {
    protected File scriptFile;

    public void init(@NotNull String[] args) {
        File scriptFile = new File(args[0]);
        if (!scriptFile.exists())
            throw new RelProxyException("File " + args[0] + " does not exist");

        this.scriptFile = scriptFile;

        File parentDir = JProxyUtil.getParentDir(scriptFile);
        String inputPath = parentDir.getAbsolutePath();
        super.init(args, inputPath);
    }

    @NotNull @Override
    protected SourceScript getSourceScript(String[] args, LinkedList<String> argsToScript) {
        return SourceScriptFile.createSourceScriptFile(scriptFile);
    }

    @Nullable @Override
    protected JProxyShellClassLoader getJProxyShellClassLoader(@NotNull JProxyConfigImpl config) {
        String classFolder = config.getClassFolder();
        if (classFolder != null)
            return new JProxyShellClassLoader(getDefaultClassLoader(), new File(classFolder));
        else
            return null;
    }

    @Override
    protected void executeFirstTime(@NotNull ClassDescriptorSourceScript scriptFileDesc,
                                    @NotNull LinkedList<String> argsToScript, JProxyShellClassLoader classLoader) {
        fixLastLoadedClass(scriptFileDesc, classLoader);

        try {
            scriptFileDesc.callMainMethod(argsToScript);
        } catch (Throwable ex) {
            ex.printStackTrace(System.out);
        }
    }

    protected void fixLastLoadedClass(
            @NotNull ClassDescriptorSourceScript scriptFileDesc, @Nullable JProxyShellClassLoader classLoader) {
        Class scriptClass = scriptFileDesc.getLastLoadedClass();
        if (scriptClass != null) return;

        // Esto es esperable cuando especificamos un classFolder en donde está ya compilado el script lanzador y es más actual que el fuente
        // no ha habido necesidad de crear un class loader "reloader" ni de recargar todos los archivos fuente con él
        if (classLoader == null) throw new RelProxyException("INTERNAL ERROR");
        if (scriptFileDesc.getClassBytes() == null) throw new RelProxyException("INTERNAL ERROR");
        scriptClass = classLoader.defineClass(scriptFileDesc);
        scriptFileDesc.setLastLoadedClass(scriptClass);
    }
}
