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

package com.innowhere.relproxy.impl.jproxy.core.clsmgr;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author jmarranz
 */
public class ClassDescriptorSourceFileRegistry {
    protected Map<String, ClassDescriptorSourceUnit> sourceUnitMapByClassName;

    public ClassDescriptorSourceFileRegistry() {
        this.sourceUnitMapByClassName = new HashMap<String, ClassDescriptorSourceUnit>();
    }

    public boolean isEmpty() {
        return sourceUnitMapByClassName.isEmpty();
    }

    public Collection<ClassDescriptorSourceUnit> getClassDescriptorSourceFileColl() {
        return sourceUnitMapByClassName.values();
    }

    public ClassDescriptorSourceUnit getClassDescriptorSourceUnit(String className) {
        return sourceUnitMapByClassName.get(className);
    }

    public ClassDescriptorSourceUnit removeClassDescriptorSourceUnit(String className) {
        return sourceUnitMapByClassName.remove(className);
    }

    public void addClassDescriptorSourceUnit(ClassDescriptorSourceUnit sourceFile) {
        sourceUnitMapByClassName.put(sourceFile.getClassName(), sourceFile);
    }

    public ClassDescriptor getClassDescriptor(String className) {
        // Puede ser el de una innerclass
        // Las innerclasses no están como tales en sourceFileMap pues sólo está la clase contenedora pero también la consideramos hotloadable
        String parentClassName;
        int pos = className.lastIndexOf('$');
        boolean inner;
        if (pos != -1) {
            parentClassName = className.substring(0, pos);
            inner = true;
        } else {
            parentClassName = className;
            inner = false;
        }
        ClassDescriptorSourceUnit sourceDesc = sourceUnitMapByClassName.get(parentClassName);
        if (!inner) return sourceDesc;
        return sourceDesc.getInnerClassDescriptor(className, true);
    }
}
