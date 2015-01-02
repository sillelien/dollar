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

package me.neilellis.dollar.relproxy.impl.jproxy.screngine;

import me.neilellis.dollar.relproxy.RelProxy;
import me.neilellis.dollar.relproxy.impl.jproxy.JProxyConfigImpl;
import me.neilellis.dollar.relproxy.jproxy.JProxyConfig;
import me.neilellis.dollar.relproxy.jproxy.JProxyScriptEngineFactory;
import org.jetbrains.annotations.NotNull;

import javax.script.ScriptEngine;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JProxyScriptEngineFactoryImpl extends JProxyScriptEngineFactory {
    protected static final String SHORT_NAME = "java";
    protected static final String LANGUAGE_NAME = "Java";

    @NotNull protected static final List names;
    @NotNull protected static final List extensions;
    @NotNull protected static final List mimeTypes;

    static {
        ArrayList<String> n;

        n = new ArrayList<>(2);
        n.add(SHORT_NAME);
        n.add(LANGUAGE_NAME);
        names = Collections.unmodifiableList(n);

        n = new ArrayList<>(1);
        n.add("java");
        extensions = Collections.unmodifiableList(n);

        n = new ArrayList<>(2);
        http:
//reference.sitepoint.com/html/mime-types-full
        n.add("text/x-java-source");
        n.add("text/plain");
        mimeTypes = Collections.unmodifiableList(n);
    }

    protected final JProxyConfigImpl config;

    public JProxyScriptEngineFactoryImpl(JProxyConfigImpl config) {
        this.config = config;
    }

    @NotNull public static JProxyScriptEngineFactory create(JProxyConfig config) {
        return new JProxyScriptEngineFactoryImpl((JProxyConfigImpl) config);
    }

    @NotNull @Override
    public String getEngineName() {
        return "RelProxy Java Script Engine";
    }

    @Override
    public String getEngineVersion() {
        return RelProxy.getVersion();
    }

    @NotNull @Override
    public List<String> getExtensions() {
        return extensions;
    }

    @NotNull @Override
    public List<String> getMimeTypes() {
        return mimeTypes;
    }

    @NotNull @Override
    public List<String> getNames() {
        return names;
    }

    @NotNull @Override
    public String getLanguageName() {
        return LANGUAGE_NAME;
    }

    @Override
    public String getLanguageVersion() {
        return System.getProperty("java.version"); // Ej 1.6.0_18
    }

    @Override
    public Object getParameter(@NotNull String key) {
        if (ScriptEngine.NAME.equals(key)) {
            return SHORT_NAME;
        } else if (ScriptEngine.ENGINE.equals(key)) {
            return getEngineName();
        } else if (ScriptEngine.ENGINE_VERSION.equals(key)) {
            return getEngineVersion();
        } else if (ScriptEngine.LANGUAGE.equals(key)) {
            return getLanguageName();
        } else if (ScriptEngine.LANGUAGE_VERSION.equals(key)) {
            return getLanguageVersion();
        } else if ("THREADING".equals(key)) {
            return "MULTITHREADED";
        } else {
            throw new IllegalArgumentException("Invalid key");
        }
    }

    @NotNull @Override
    public String getMethodCallSyntax(String obj, String method, @NotNull String... args) {
        StringBuilder ret = new StringBuilder();
        ret.append(obj + "." + method + "(");
        int len = args.length;
        if (len == 0) {
            ret.append(")");
            return ret.toString();
        }

        for (int i = 0; i < len; i++) {
            ret.append(args[i]);
            if (i != len - 1) {
                ret.append(",");
            } else {
                ret.append(")");
            }
        }
        return ret.toString();
    }

    @NotNull @Override
    public String getOutputStatement(@NotNull String toDisplay) {
        StringBuilder buf = new StringBuilder();
        buf.append("System.out.println(\"");
        int len = toDisplay.length();
        for (int i = 0; i < len; i++) {
            char ch = toDisplay.charAt(i);
            switch (ch) {
                case '"':
                    buf.append("\\\"");
                    break;
                case '\\':
                    buf.append("\\\\");
                    break;
                default:
                    buf.append(ch);
                    break;
            }
        }
        buf.append("\")");
        return buf.toString();
    }

    @NotNull @Override
    public String getProgram(@NotNull String... statements) {
        StringBuilder ret = new StringBuilder();
        int len = statements.length;
        for (String statement : statements) {
            ret.append(statement);
            ret.append('\n');
        }
        return ret.toString();
    }

    @NotNull @Override
    public ScriptEngine getScriptEngine() {
        return new JProxyScriptEngineImpl(this, config);
    }
}
