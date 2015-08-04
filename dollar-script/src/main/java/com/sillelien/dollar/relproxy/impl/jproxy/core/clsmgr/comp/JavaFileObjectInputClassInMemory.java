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

import org.jetbrains.annotations.NotNull;

import javax.tools.SimpleJavaFileObject;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

public class JavaFileObjectInputClassInMemory extends SimpleJavaFileObject implements JProxyJavaFileObjectInput {
    protected final String binaryName;
    protected final byte[] byteCode;
    protected final long timestamp;

    public JavaFileObjectInputClassInMemory(@NotNull String name, byte[] byteCode, long timestamp) {
        super(URI.create("string:///" + name.replace('.', '/') + Kind.CLASS.extension), Kind.CLASS);

        this.binaryName = name;
        this.byteCode = byteCode;
        this.timestamp = timestamp;
    }

    @Override
    public String getBinaryName() {
        return binaryName;
    }

    @NotNull @Override
    public InputStream openInputStream() throws IOException {
        return new ByteArrayInputStream(getBytes());
    }

    public byte[] getBytes() {
        return byteCode;
    }

    @NotNull @Override
    public OutputStream openOutputStream() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getLastModified() {
        return timestamp;
    }

}
