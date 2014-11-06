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

import java.io.*;
import java.net.URI;

/**
 * @author jmarranz
 */
public class JavaFileObjectInputClassInFile extends JavaFileObjectInputClassInFileSystem {
    protected File file;

    public JavaFileObjectInputClassInFile(File file, String binaryName, URI uri) {
        super(binaryName, uri, uri.getPath());
        this.file = file;
    }

    @Override
    public InputStream openInputStream() throws IOException {
        // Podríamos hacer uri.toURL().openStream() pero si tenemos el File es para algo
        return new BufferedInputStream(new FileInputStream(file), 10 * 1024);
    }

    @Override
    public long getLastModified() {
        return file.lastModified();
    }

    @Override
    public String toString() {
        return "JavaFileObjectInputClassInFile{uri=" + uri + '}';
    }
}