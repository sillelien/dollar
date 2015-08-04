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

package com.sillelien.dollar.relproxy.impl.jproxy.shell;

import com.sillelien.dollar.relproxy.RelProxyException;
import com.sillelien.dollar.relproxy.impl.jproxy.JProxyConfigImpl;
import com.sillelien.dollar.relproxy.impl.jproxy.core.clsmgr.ClassDescriptorSourceScript;
import com.sillelien.dollar.relproxy.impl.jproxy.core.clsmgr.SourceScript;
import com.sillelien.dollar.relproxy.impl.jproxy.core.clsmgr.SourceScriptInMemory;
import com.sillelien.dollar.relproxy.impl.jproxy.shell.inter.JProxyShellProcessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;

public class JProxyShellInteractiveImpl extends JProxyShellImpl {
    @NotNull protected final JProxyShellProcessor processor = new JProxyShellProcessor(this);
    protected boolean test = false;
    protected ClassDescriptorSourceScript classDescSourceScript;

    public ClassDescriptorSourceScript getClassDescriptorSourceScript() {
        return classDescSourceScript;
    }

    @NotNull public SourceScriptInMemory getSourceScriptInMemory() {
        return (SourceScriptInMemory) classDescSourceScript.getSourceScript();
    }

    public void init(@NotNull String[] args) {
        this.classDescSourceScript = super.init(args, null);

        if (test) {
            processor.test();
            return;
        }

        processor.loop();
    }

    @Override
    public ClassDescriptorSourceScript init(@NotNull JProxyConfigImpl config, SourceScript scriptFile,
                                            ClassLoader classLoader) {
        ClassDescriptorSourceScript script = super.init(config, scriptFile, classLoader);

        this.test = config.isTest();

        return script;
    }

    @Override
    protected void processConfigParams(
            @NotNull String[] args, @NotNull LinkedList<String> argsToScript, @NotNull JProxyConfigImpl config) {
        super.processConfigParams(args, argsToScript, config);

        String classFolder = config.getClassFolder();
        if (classFolder != null && !classFolder.trim().isEmpty()) {
            throw new RelProxyException("cacheClassFolder is useless to execute in interactive mode");
        }

        // No tiene sentido especificar un tiempo de scan porque no hay directorio de entrada en el que escanear
        // archivos
        if (config.getScanPeriod() >= 0) // 0 no puede ser porque da error antes pero lo ponemos para reforzar la idea
        { throw new RelProxyException("scanPeriod positive value has no sense in interactive execution"); }
    }

    @NotNull @Override
    protected SourceScript getSourceScript(String[] args, LinkedList<String> argsToScript) {
        return SourceScriptInMemory.createSourceScriptInMemory(
                ""); // La primera vez no hace nada, sirve para "calentar" la app
    }

    @Nullable @Override
    protected JProxyShellClassLoader getJProxyShellClassLoader(JProxyConfigImpl config) {
        // No hay classFolder => no hay necesidad de nuevo ClassLoader
        return null;
    }

    @Override
    protected void executeFirstTime(ClassDescriptorSourceScript scriptFileDesc, LinkedList<String> argsToScript,
                                    JProxyShellClassLoader classLoader) {
        // La primera vez el script es vacío, no hay nada que ejecutar
    }
}
