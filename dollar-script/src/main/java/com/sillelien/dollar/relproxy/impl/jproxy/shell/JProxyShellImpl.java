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
import com.sillelien.dollar.relproxy.RelProxyOnReloadListener;
import com.sillelien.dollar.relproxy.impl.jproxy.JProxyConfigImpl;
import com.sillelien.dollar.relproxy.impl.jproxy.core.JProxyImpl;
import com.sillelien.dollar.relproxy.impl.jproxy.core.clsmgr.ClassDescriptorSourceScript;
import com.sillelien.dollar.relproxy.impl.jproxy.core.clsmgr.SourceScript;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;

public abstract class JProxyShellImpl extends JProxyImpl {
    public static void main(String[] args) {
        if (args[0].isEmpty()) {
            // Esto tiene explicación: cuando invocamos jproxysh sin parámetros (o espacios da igual) invocamos
            // dentro jproxysh con com.sillelien.dollar.relproxy.jproxy.JProxyShell "$@"
            // el parámetro "$@" se convierte en "" que es un parámetro de verdad que recibimos pero de cadena vacía,
            // lo cual nos viene GENIAL para distinguir el caso shell interactive
            SINGLETON = new JProxyShellInteractiveImpl();
            ((JProxyShellInteractiveImpl) SINGLETON).init(args);
        } else {
            if (args[0].equals("-c")) {
                SINGLETON = new JProxyShellCodeSnippetImpl();
                ((JProxyShellCodeSnippetImpl) SINGLETON).init(args);
            } else {
                SINGLETON = new JProxyShellScriptFileImpl();
                ((JProxyShellScriptFileImpl) SINGLETON).init(args);
            }

        }
    }

    @NotNull @Override
    public Class getMainParamClass() {
        return String[].class;
    }

    protected ClassDescriptorSourceScript init(@NotNull String[] args, String inputPath) {
        // Esto quizás necesite una opción en plan "verbose" o "log" para mostrar por pantalla o nada
        RelProxyOnReloadListener proxyListener =
                (objOld, objNew, proxy, method,
                 args1) -> System.out.println("Reloaded " + objNew + " Calling method: " + method);

        JProxyConfigImpl config = new JProxyConfigImpl();
        config.setEnabled(true);
        config.setRelProxyOnReloadListener(proxyListener);
        config.setInputPath(inputPath);
        config.setJProxyDiagnosticsListener(
                null); // Nos vale el log por defecto y no hay manera de espeficar otra cosa via comando

        LinkedList<String> argsToScript = new LinkedList<>();
        processConfigParams(args, argsToScript, config);

        SourceScript sourceFileScript = getSourceScript(args, argsToScript);

        JProxyShellClassLoader classLoader = getJProxyShellClassLoader(config);

        ClassDescriptorSourceScript scriptFileDesc = init(config, sourceFileScript, classLoader);

        executeFirstTime(scriptFileDesc, argsToScript, classLoader);

        return scriptFileDesc;
    }

    protected void processConfigParams(
            @NotNull String[] args, @NotNull LinkedList<String> argsToScript, @NotNull JProxyConfigImpl config) {
        String classFolder = null;
        long scanPeriod = -1;
        Iterable<String> compilationOptions = null;
        boolean test = false;

        for (int i = 1; i < args.length; i++) {
            String arg = args[i];

            if (arg.startsWith("-D")) {
                String param = arg.substring(2);
                int pos = param.indexOf('=');
                if (pos == -1) { throw new RelProxyException("Bad parameter format: " + arg); }
                String name = param.substring(0, pos);
                String value = param.substring(pos + 1);

                if ("cacheClassFolder".equals(name)) {
                    classFolder = value;
                } else if ("scanPeriod".equals(name)) {
                    scanPeriod = Long.parseLong(value);
                } else if ("compilationOptions".equals(name)) {
                    compilationOptions = parseCompilationOptions(value);
                } else if ("test".equals(name)) {
                    test = Boolean.parseBoolean(value);
                } else { throw new RelProxyException("Unknown parameter: " + arg); }
            } else {
                argsToScript.add(arg);
            }
        }

        config.setClassFolder(classFolder);
        config.setScanPeriod(scanPeriod);
        config.setCompilationOptions(compilationOptions);
        config.setTest(test);
    }

    protected abstract SourceScript getSourceScript(String[] args, LinkedList<String> argsToScript);

    protected abstract JProxyShellClassLoader getJProxyShellClassLoader(JProxyConfigImpl config);

    protected abstract void executeFirstTime(ClassDescriptorSourceScript scriptFileDesc,
                                             LinkedList<String> argsToScript, JProxyShellClassLoader classLoader);

    @NotNull private static Iterable<String> parseCompilationOptions(@NotNull String value) {
        // Ej -source 1.6 -target 1.6  se convertiría en Arrays.asList(new String[]{"-source","1.6","-target","1.6"});
        String[] options = value.split(" ");
        LinkedList<String> opCol = new LinkedList<>();
        for (String option : options) {
            String op = option.trim(); // Por si hubiera dos espacios
            if (op.isEmpty()) { continue; }
            opCol.add(op);
        }
        return opCol;
    }

}
