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

package com.innowhere.relproxy.impl.jproxy.core.clsmgr.comp;

import org.jetbrains.annotations.NotNull;

public class JavaFileObjectInputSourceInMemory extends JavaFileObjectInputSourceBase {
    protected final String source;
    protected final long timestamp;

    public JavaFileObjectInputSourceInMemory(@NotNull String name, String source, String encoding, long timestamp) {
        super(name, encoding);
        this.source = source;
        this.timestamp = timestamp;
    }

    @Override
    public long getLastModified() {
        return timestamp;
    }

    @Override
    protected String getSource() {
        return source;
    }
}
