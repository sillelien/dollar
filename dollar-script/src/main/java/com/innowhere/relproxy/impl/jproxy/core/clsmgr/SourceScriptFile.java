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

import com.innowhere.relproxy.impl.jproxy.JProxyUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * @author jmarranz
 */
public abstract class SourceScriptFile extends SourceScript {
    protected final File sourceFile;

    public SourceScriptFile(File sourceFile) {
        this.sourceFile = sourceFile;
    }

    @NotNull public static SourceScriptFile createSourceScriptFile(File sourceFile) {
        String ext = JProxyUtil.getFileExtension(sourceFile); // Si no tiene extensión devuelve ""
        if ("java".equals(ext))
            return new SourceScriptFileJavaExt(sourceFile);
        else
            return new SourceScriptFileOtherExt(sourceFile);
    }

    @Nullable @Override
    public String getClassNameFromSourceFileScriptAbsPath(@NotNull File rootPathOfSourcesFile) {
        String path = sourceFile.getAbsolutePath();
        String rootPathOfSources = rootPathOfSourcesFile.getAbsolutePath();
        // path es absoluto, preferentemente obtenido con File.getAbsolutePath()
        int pos = path.indexOf(rootPathOfSources);
        if (pos != 0) // DEBE SER 0, NO debería ocurrir
            return null;
        path = path.substring(rootPathOfSources.length() + 1); // Sumamos +1 para quitar también el / separador del pathInput y el path relativo de la clase
        // Puede no tener extensión o bien ser .java o bien ser una inventada (ej .jsh), la quitamos si existe
        pos = path.lastIndexOf('.');
        if (pos != -1)
            path = path.substring(0, pos);

        path = path.replace(File.separatorChar, '.');  // getAbsolutePath() normaliza con el caracter de la plataforma
        return path;
    }

    public File getFile() {
        return sourceFile;
    }

    @Override
    public long lastModified() {
        return sourceFile.lastModified();
    }
}
