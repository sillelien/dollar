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

import com.innowhere.relproxy.impl.jproxy.JProxyUtil;

import java.io.File;

/**
 * http://www.javablogging.com/dynamic-in-memory-compilation/
 *
 * @author jmarranz
 */
public class JavaFileObjectInputSourceInFile extends JavaFileObjectInputSourceBase {
    protected File file;
    protected String source;

    public JavaFileObjectInputSourceInFile(String name, File file, String encoding) {
        super(name, encoding);
        this.file = file;
    }

    @Override
    protected String getSource() {
        if (source != null)
            return source;
        this.source = JProxyUtil.readTextFile(file, encoding);
        return source;
    }

    @Override
    public long getLastModified() {
        return file.lastModified();
    }
}
