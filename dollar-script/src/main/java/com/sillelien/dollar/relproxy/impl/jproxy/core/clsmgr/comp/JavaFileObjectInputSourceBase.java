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

package com.sillelien.dollar.relproxy.impl.jproxy.core.clsmgr.comp;

import com.sillelien.dollar.relproxy.RelProxyException;
import org.jetbrains.annotations.NotNull;

import javax.tools.SimpleJavaFileObject;
import java.io.*;
import java.net.URI;

public abstract class JavaFileObjectInputSourceBase extends SimpleJavaFileObject implements JProxyJavaFileObjectInput {
    protected final String binaryName;
    protected final String encoding;

    public JavaFileObjectInputSourceBase(@NotNull String name, String encoding) {
        super(URI.create("string:///" + name.replace('.', '/') + Kind.SOURCE.extension),
              Kind.SOURCE);  // La extensión .java es necesaria aunque sea falsa sino da error

        this.binaryName = name;
        this.encoding = encoding;
    }

    public String getBinaryName() {
        return binaryName;
    }

    @NotNull @Override
    public InputStream openInputStream() throws IOException {
        return new ByteArrayInputStream(getBytes());
    }

    @NotNull public byte[] getBytes() {
        try {
            return getSource().getBytes(encoding);
        } catch (UnsupportedEncodingException ex) {
            throw new RelProxyException(ex);
        }
    }

    protected abstract String getSource();

    @NotNull @Override
    public OutputStream openOutputStream() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
        return getSource();
    }

}
