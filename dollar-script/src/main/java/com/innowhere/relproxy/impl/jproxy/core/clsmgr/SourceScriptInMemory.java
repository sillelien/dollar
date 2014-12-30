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

package com.innowhere.relproxy.impl.jproxy.core.clsmgr;

import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * @author jmarranz
 */
public class SourceScriptInMemory extends SourceScript {
    public static final String DEFAULT_CLASS_NAME = "_jproxyMainClass_";  // OJO NO CAMBIAR, está ya documentada

    protected final String className;
    protected String code;
    protected long timestamp;

    private SourceScriptInMemory(String className, String code) {
        this.className = className;
        setScriptCode(code, System.currentTimeMillis());
    }

    public final void setScriptCode(String code, long timestamp) {
        this.code = code;
        this.timestamp = timestamp;
    }

    @NotNull public static SourceScriptInMemory createSourceScriptInMemory(String code) {
        return new SourceScriptInMemory(DEFAULT_CLASS_NAME, code);
    }

    @Override
    public String getScriptCode(String encoding, boolean[] hasHashBang) {
        hasHashBang[0] = false;
        return code;
    }

    @Override
    public String getClassNameFromSourceFileScriptAbsPath(File rootPathOfSourcesFile) {
        return className;
    }

    public String getScriptCode() {
        return code;
    }

    @Override
    public long lastModified() {
        return timestamp; // Siempre ha sido modificado
    }
}
