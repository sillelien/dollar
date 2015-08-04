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

package com.sillelien.dollar.relproxy.impl.jproxy.screngine;

import com.sillelien.dollar.relproxy.impl.jproxy.JProxyConfigImpl;
import com.sillelien.dollar.relproxy.impl.jproxy.JProxyUtil;
import com.sillelien.dollar.relproxy.jproxy.JProxyScriptEngine;
import org.jetbrains.annotations.NotNull;

import javax.script.*;
import java.io.Reader;

/**
 * @author jmarranz
 */
public class JProxyScriptEngineImpl extends AbstractScriptEngine implements JProxyScriptEngine {
    protected final JProxyScriptEngineFactoryImpl factory;
    protected final JProxyScriptEngineDelegateImpl delegate;

    public JProxyScriptEngineImpl(JProxyScriptEngineFactoryImpl factory, JProxyConfigImpl config) {
        this.factory = factory;
        this.delegate = new JProxyScriptEngineDelegateImpl(this, config);
    }

    @Override
    public <T> T create(T obj, Class<T> clasz) {
        return delegate.create(obj, clasz);
    }

    @Override
    public boolean start() {
        return delegate.start();
    }

    @Override
    public boolean stop() {
        return delegate.stop();
    }

    @Override
    public Object eval(String script, ScriptContext context) throws ScriptException {
        return delegate.execute(script, context);
    }


    @Override
    public Object eval(Reader reader, ScriptContext context) throws ScriptException {
        String script = JProxyUtil.readTextFile(reader);
        return eval(script, context);
    }


    @NotNull @Override
    public Bindings createBindings() {
        return new BindingsImpl();
    }

    @Override
    public ScriptEngineFactory getFactory() {
        return factory;
    }

}
