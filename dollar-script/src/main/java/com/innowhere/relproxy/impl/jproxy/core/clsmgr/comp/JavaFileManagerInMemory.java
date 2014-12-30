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

import com.innowhere.relproxy.impl.jproxy.core.clsmgr.ClassDescriptorSourceFileRegistry;
import com.innowhere.relproxy.impl.jproxy.core.clsmgr.ClassDescriptorSourceUnit;
import org.jetbrains.annotations.NotNull;

import javax.tools.*;
import javax.tools.JavaFileObject.Kind;
import java.io.IOException;
import java.util.*;


public class JavaFileManagerInMemory extends ForwardingJavaFileManager {
    private final LinkedList<JavaFileObjectOutputClass> outputClassList = new LinkedList<>();
    @NotNull private final JavaFileObjectInputClassFinderByClassLoader classFinder;
    private final ClassDescriptorSourceFileRegistry sourceRegistry;

    public JavaFileManagerInMemory(@NotNull StandardJavaFileManager standardFileManager, ClassLoader classLoader,
                                   ClassDescriptorSourceFileRegistry sourceRegistry) {
        super(standardFileManager);
        this.sourceRegistry = sourceRegistry;
        this.classFinder = new JavaFileObjectInputClassFinderByClassLoader(classLoader);
    }

    @NotNull public LinkedList<JavaFileObjectOutputClass> getJavaFileObjectOutputClassList() {
        return outputClassList;
    }

    @Override
    public Iterable list(Location location, @NotNull String packageName, @NotNull Set kinds, boolean recurse) throws
                                                                                                              IOException {
        if (location == StandardLocation.PLATFORM_CLASS_PATH) // let standard manager hanfle
            return super.list(location, packageName, kinds, recurse);  // En este caso nunca (con PLATFORM_CLASS_PATH) va a encontrar nuestros sources ni .class
        else if (location == StandardLocation.CLASS_PATH && kinds.contains(Kind.CLASS)) {
            if (packageName.startsWith("java.") || packageName.startsWith("javax."))  // a hack to let standard manager handle locations like "java.lang" or "java.util". Estrictamente no es necesario pero derivamos la inmensa mayoría de las clases estándar al método por defecto
                return super.list(location, packageName, kinds, recurse);
            else {
                // El StandardJavaFileManager al que hacemos forward es "configurado" por el compilador al que está asociado cuando hay una tarea de compilación
                // dicha configuración es por ejemplo el classpath tanto para encontrar .class como .java
                // En nuestro caso no disponemos del classpath de los .class, disponemos del ClassLoader a través del cual podemos obtener "a mano" via resources los
                // JavaFileObject de los .class que necesitamos.
                // Ahora bien, no es el caso de los archivos fuente en donde sí tenemos un path claro el cual pasamos como classpath al compilador y por tanto un super.list(location, packageName, kinds, recurse)
                // nos devolverá los .java (como JavaFileObject claro) si encuentra archivos correspondientes al package buscado.

                LinkedList<JavaFileObject> result = new LinkedList<>();

                Iterable inFileMgr = super.list(location, packageName, kinds, recurse); // Esperamos o archivos fuente o .class de clases no recargables
                if (inFileMgr instanceof Collection) {
                    result.addAll((Collection) inFileMgr);
                } else {
                    for (Object anInFileMgr : inFileMgr) {
                        JavaFileObject file = (JavaFileObject) anInFileMgr;
                        result.add(file);
                    }
                }

                List<JavaFileObjectInputClassInFileSystem> classList = classFinder.find(packageName);

                // Reemplazamos los .class de classList que son los que están en archivo "deployados" que pueden ser más antiguos que los que están en memoria
                for (JavaFileObjectInputClassInFileSystem fileObj : classList) {
                    String className = fileObj.getBinaryName();
                    ClassDescriptorSourceUnit sourceFileDesc = sourceRegistry.getClassDescriptorSourceUnit(className);
                    if (sourceFileDesc != null && sourceFileDesc.getClassBytes() != null) {
                        JavaFileObjectInputClassInMemory fileInput = new JavaFileObjectInputClassInMemory(className, sourceFileDesc.getClassBytes(), sourceFileDesc.getTimestamp());
                        result.add(fileInput);
                    } else {
                        result.add(fileObj);
                    }
                }

                // Los JavaFileObject de archivos fuente pueden ser los mimas clases que los de .class, el compilador se encargará de comparar los timestamp y elegir el .class o el source

                return result;
            }
        }
        return Collections.emptyList();
    }

    @Override
    public String inferBinaryName(Location location, JavaFileObject file) {
        if (file instanceof JProxyJavaFileObjectInput)
            return ((JProxyJavaFileObjectInput) file).getBinaryName();

        return super.inferBinaryName(location, file);
    }

    @NotNull @Override
    public JavaFileObject getJavaFileForOutput(Location location, String className, Kind kind,
                                               FileObject sibling) throws IOException {
        // Normalmente sólo habrá un resultado pero se da el caso de compilar una clase con una o varias inner
        // classes, el compilador las compila de una vez
        JavaFileObjectOutputClass outClass = new JavaFileObjectOutputClass(className, kind);
        outputClassList.add(outClass);
        return outClass;
    }

}