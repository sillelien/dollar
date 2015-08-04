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

package com.sillelien.dollar.relproxy.impl.jproxy.core.clsmgr;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;

/**
 * @author jmarranz
 */
public abstract class ClassDescriptorSourceUnit extends ClassDescriptor {
    protected final JProxyEngine engine;
    protected final SourceUnit sourceFile;
    protected long timestamp;
    protected LinkedList<ClassDescriptorInner> innerClasses;

    public ClassDescriptorSourceUnit(JProxyEngine engine, @NotNull String className, SourceUnit sourceFile,
                                     long timestamp) {
        super(className);
        this.engine = engine;
        this.sourceFile = sourceFile;
        this.timestamp = timestamp;
    }

    @Nullable public static ClassDescriptorSourceUnit create(boolean script, JProxyEngine engine, String className,
                                                             SourceUnit sourceFile, long timestamp) {
        if (sourceFile instanceof SourceScript) {
            return new ClassDescriptorSourceScript(engine, className, (SourceScript) sourceFile, timestamp);
        } else if (sourceFile instanceof SourceFileJavaNormal) {
            return new ClassDescriptorSourceFileJava(engine, className, (SourceFileJavaNormal) sourceFile, timestamp);
        } else {
            return null; // WTF!!
        }
    }

    public void cleanOnSourceCodeChanged() {
        // Como ha cambiado la clase, reseteamos las dependencias
        setClassBytes(null);
        setLastLoadedClass(null);
        clearInnerClassDescriptors(); // El código fuente nuevo puede haber cambiado totalmente las innerclasses
        // antiguas (añadido, eliminado)
    }

    public void clearInnerClassDescriptors() {
        if (innerClasses != null) { innerClasses.clear(); }
    }

    @NotNull public String getEncoding() {
        return engine.getSourceEncoding();
    }

    @Nullable public ClassDescriptorInner getInnerClassDescriptor(@NotNull String className, boolean addWhenMissing) {
        if (innerClasses != null) {
            for (ClassDescriptorInner classDesc : innerClasses) {
                if (classDesc.getClassName().equals(className)) { return classDesc; }
            }
        }

        if (!addWhenMissing) { return null; }

        return addInnerClassDescriptor(className);
    }

    @Nullable public ClassDescriptorInner addInnerClassDescriptor(@NotNull String className) {
        if (!isInnerClass(className)) { return null; }

        if (innerClasses == null) { innerClasses = new LinkedList<>(); }

        ClassDescriptorInner classDesc = new ClassDescriptorInner(className, this);
        innerClasses.add(classDesc);
        return classDesc;
    }

    public boolean isInnerClass(@NotNull String className) {
        int pos = className.lastIndexOf('$');
        if (pos == -1) {
            return false; // No es innerclass
        }
        String baseClassName = className.substring(0, pos);
        return this.className.equals(baseClassName); // Si es false es que es una innerclass pero de otra clase
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public boolean isInnerClass() {
        return false;
    }

    @Override
    public void resetLastLoadedClass() {
        super.resetLastLoadedClass();

        LinkedList<ClassDescriptorInner> innerClassDescList = getInnerClassDescriptors();
        if (innerClassDescList != null) {
            for (ClassDescriptorInner innerClassDesc : innerClassDescList) { innerClassDesc.resetLastLoadedClass(); }
        }
    }

    public LinkedList<ClassDescriptorInner> getInnerClassDescriptors() {
        return innerClasses;
    }

    public void updateTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

}
