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

package com.innowhere.relproxy.impl.jproxy.shell;

import com.innowhere.relproxy.RelProxyException;
import com.innowhere.relproxy.impl.jproxy.core.clsmgr.ClassDescriptor;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * @author jmarranz
 */
public class JProxyShellClassLoader extends URLClassLoader {
    public JProxyShellClassLoader(ClassLoader parent, File classFolder) {
        super(toURLArray(classFolder), parent);
    }

    private static URL[] toURLArray(File file) {
        try {
            return new URL[]{file.toURI().toURL()};
        } catch (MalformedURLException ex) {
            throw new RelProxyException(ex);
        }
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        return super.findClass(name);
    }

    @Override
    protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        return super.loadClass(name, resolve);
    }

    public synchronized Class defineClass(ClassDescriptor classDesc) {
        String className = classDesc.getClassName();
        byte[] classBytes = classDesc.getClassBytes();
        Class clasz = defineClass(className, classBytes, 0, classBytes.length);
        classDesc.setLastLoadedClass(clasz);
        return clasz;
    }
}
