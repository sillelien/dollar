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

package com.innowhere.relproxy.impl.jproxy.core.clsmgr.comp;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

/**
 * @author jmarranz
 */
public class JavaFileObjectInputClassInJar extends JavaFileObjectInputClassInFileSystem {
    protected long timestamp;

    public JavaFileObjectInputClassInJar(String binaryName, URI uri, long timestamp) {
        super(binaryName, uri, uri.getSchemeSpecificPart());
        this.timestamp = timestamp;
    }

    @Override
    public InputStream openInputStream() throws IOException {
        return uri.toURL().openStream(); // easy way to handle any URI!
    }

    @Override
    public long getLastModified() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "JavaFileObjectInputClassInJar{uri=" + uri + '}';
    }
}