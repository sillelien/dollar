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

package me.neilellis.dollar.relproxy.impl.jproxy.core.clsmgr;

import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * @author jmarranz
 */
public class ClassDescriptorSourceFileJava extends ClassDescriptorSourceUnit {
    public ClassDescriptorSourceFileJava(JProxyEngine engine, @NotNull String className,
                                         SourceFileJavaNormal sourceFile, long timestamp) {
        super(engine, className, sourceFile, timestamp);
    }

    public File getSourceFile() {
        return getSourceFileJavaNormal().getFile();
    }

    @NotNull public SourceFileJavaNormal getSourceFileJavaNormal() {
        return (SourceFileJavaNormal) sourceFile;
    }

}
