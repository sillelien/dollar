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

package me.neilellis.dollar.script.relproxy.impl;

import me.neilellis.dollar.script.relproxy.RelProxyException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * @author jmarranz
 */
public abstract class GenericProxyVersionedObject<T> {
    protected final GenericProxyInvocationHandler parent;
    protected T obj;

    public GenericProxyVersionedObject(T obj, GenericProxyInvocationHandler parent) {
        this.obj = obj;
        this.parent = parent;
    }

    protected static void getTreeFields(@NotNull Class clasz, Object obj, @NotNull ArrayList<Field> fieldList,
                                        ArrayList<Object> valueList) throws IllegalAccessException {
        getFields(clasz, obj, fieldList, valueList);
        Class superClass = clasz.getSuperclass();
        if (superClass != null)
            getTreeFields(superClass, obj, fieldList, valueList);
    }

    protected static void getFields(
            @NotNull Class clasz, Object obj, @NotNull ArrayList<Field> fieldList,
            @Nullable ArrayList<Object> valueList) throws IllegalAccessException {
        Field[] fieldListClass = clasz.getDeclaredFields();
        for (Field field : fieldListClass) {
            fieldList.add(field);
            if (valueList != null) {
                field.setAccessible(true);
                Object value = field.get(obj);
                valueList.add(value);
            }
        }
    }

    public T getNewVersion() throws Throwable {
        Class<T> newClass = reloadClass();
        if (newClass == null)
            return obj;

        Class oldClass = obj.getClass();
        if (newClass != oldClass) {
            ArrayList<Field> fieldListOld = new ArrayList<>();
            ArrayList<Object> valueListOld = new ArrayList<>();

            getTreeFields(oldClass, obj, fieldListOld, valueListOld);

            try {
                this.obj = newClass.getConstructor(new Class[0]).newInstance();
            } catch (NoSuchMethodException ex) {
                throw new RelProxyException("Cannot reload " + newClass.getName() + " a default empty of params " +
                                            "constructor is required", ex);
            }

            ArrayList<Field> fieldListNew = new ArrayList<>();

            getTreeFields(newClass, obj, fieldListNew, null);

            if (fieldListOld.size() != fieldListNew.size())
                throw new RelProxyException("Cannot reload " + newClass.getName() + " number of fields have changed, redeploy");

            for (int i = 0; i < fieldListOld.size(); i++) {
                Field fieldOld = fieldListOld.get(i);
                Field fieldNew = fieldListNew.get(i);
                if ((!ignoreField(fieldOld) && !fieldOld.getName().equals(fieldNew.getName())) ||
                        !fieldOld.getType().equals(fieldNew.getType()))
                    throw new RelProxyException("Cannot reload " + newClass.getName() + " fields have changed, redeploy");

                Object fieldObj = valueListOld.get(i);
                fieldNew.setAccessible(true);
                fieldNew.set(obj, fieldObj);
            }
        }

        return obj;
    }

    @Nullable protected abstract <T> Class<T> reloadClass();

    protected abstract T getCurrent();

    protected abstract boolean ignoreField(Field field);
}
