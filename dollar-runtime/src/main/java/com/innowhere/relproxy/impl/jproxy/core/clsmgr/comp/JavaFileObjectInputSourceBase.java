/*
 *
 *  * See: https://github.com/jmarranz
 *  *
 *  * Copyright (c) 2014 Jose M. Arranz (additional work by Neil Ellis)
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *       http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.innowhere.relproxy.impl.jproxy.core.clsmgr.comp;

import com.innowhere.relproxy.RelProxyException;

import javax.tools.SimpleJavaFileObject;
import java.io.*;
import java.net.URI;

/**
 * http://www.javablogging.com/dynamic-in-memory-compilation/
 *
 * @author jmarranz
 */
public abstract class JavaFileObjectInputSourceBase extends SimpleJavaFileObject implements JProxyJavaFileObjectInput {
    protected String binaryName;
    protected String encoding;

    public JavaFileObjectInputSourceBase(String name, String encoding) {
        super(URI.create("string:///" + name.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);  // La extensión .java es necesaria aunque sea falsa sino da error

        this.binaryName = name;
        this.encoding = encoding;
    }

    protected abstract String getSource();


    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
        return getSource();
    }

    public byte[] getBytes() {
        try {
            return getSource().getBytes(encoding);
        } catch (UnsupportedEncodingException ex) {
            throw new RelProxyException(ex);
        }
    }

    @Override
    public InputStream openInputStream() throws IOException {
        return new ByteArrayInputStream(getBytes());
    }

    @Override
    public OutputStream openOutputStream() throws IOException {
        throw new UnsupportedOperationException();
    }

    public String getBinaryName() {
        return binaryName;
    }

}
