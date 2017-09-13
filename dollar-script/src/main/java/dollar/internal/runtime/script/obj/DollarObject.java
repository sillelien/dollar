/*
 *    Copyright (c) 2014-2017 Neil Ellis
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package dollar.internal.runtime.script.obj;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import dollar.api.DollarException;
import dollar.api.DollarStatic;
import dollar.api.Pipeable;
import dollar.api.Type;
import dollar.api.VarFlags;
import dollar.api.VarKey;
import dollar.api.Variable;
import dollar.api.types.AbstractDollar;
import dollar.api.types.DollarFactory;
import dollar.api.var;
import dollar.internal.runtime.script.api.ScopeExecutable;
import dollar.internal.runtime.script.api.exceptions.DollarScriptException;
import dollar.internal.runtime.script.parser.scope.ScriptScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.Yaml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static dollar.api.types.DollarFactory.FALSE;
import static dollar.api.types.DollarFactory.TRUE;
import static dollar.internal.runtime.script.DollarUtilFactory.util;

public class DollarObject extends AbstractDollar {


    @NotNull
    private final var constructor;
    @NotNull
    private final Map<VarKey, Variable> fields = new ConcurrentHashMap<>();
    private final boolean mutable;
    @NotNull
    private final String name;
    @NotNull
    private final Type type;

    public DollarObject(@NotNull String name, @NotNull var constructor,
                        @NotNull Map<VarKey, Variable> fields) {

        this(name, constructor, fields, false);
    }

    public DollarObject(@NotNull String name, @NotNull var constructor,
                        @NotNull Map<VarKey, Variable> fields, boolean mutable) {
        super();
        this.name = name;
        this.constructor = constructor;
        this.mutable = mutable;
        this.fields.putAll(fields);
        type = Type.of(name);

    }

    @NotNull
    @Override
    public var $abs() {
        return this;
    }

    @NotNull
    @Override
    public var $append(@NotNull var value) {
        final LinkedHashMap<var, var> newMap = new LinkedHashMap<>(toVarMap());
        newMap.put(value.$pairKey(), value.$pairValue());
        return DollarFactory.fromValue(newMap);
    }

    @Override
    public @NotNull
    var $as(@NotNull Type type) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public var $containsKey(@NotNull var value) {
        return DollarStatic.$(fields.containsKey(VarKey.of(value)));
    }

    @Override
    @NotNull
    public var $containsValue(@NotNull var value) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public var $divide(@NotNull var rhs) {
        throw new DollarScriptException("Cannot divide instance of class " + name + " all fields are fixed.");
    }

    @Override
    public @NotNull
    var $equals(@Nullable var other) {
        return (other == this) ? TRUE : FALSE;
    }

    @Override
    public @NotNull
    var $get(@NotNull var key) {
        return inThisScope(s -> {
            Variable variable = fields.get(VarKey.of(key));
            if (variable == null) {
                throw new DollarException("No such field " + key + " in class " + name);
            }
            return variable.getValue();
        });
    }

    @NotNull
    @Override
    public var $has(@NotNull var key) {
        return DollarStatic.$(fields.containsKey(VarKey.of(key)));
    }

    @NotNull
    @Override
    public var $insert(@NotNull var value, int position) {
        final LinkedHashMap<var, var> newMap = new LinkedHashMap<>();
        int count = 0;
        for (Map.Entry<var, var> entry : newMap.entrySet()) {
            if (count == position) {
                newMap.put(value.$pairKey(), value.$pairValue());
            }
            newMap.put(entry.getKey(), entry.getValue());

        }
        newMap.putAll(toVarMap());
        return DollarFactory.fromValue(newMap);
    }

    @NotNull
    @Override
    public var $listen(@NotNull Pipeable pipe) {
        String key = UUID.randomUUID().toString();
        $listen(pipe, key);
        return DollarStatic.$(key);
    }

    @NotNull
    @Override
    public var $listen(@NotNull Pipeable pipe, @NotNull String key) {
        for (Variable v : fields.values()) {
            //Join the children to this, so if the children change
            //listeners to this get the latest value of this.
            v.getValue().$listen(i -> this, key);
        }
        return DollarStatic.$(key);
    }

    @NotNull
    @Override
    public var $map() {
        return this;
    }

    @NotNull
    @Override
    public var $mimeType() {
        return DollarStatic.$("application/json");
    }

    @NotNull
    @Override
    public var $minus(@NotNull var rhs) {
        throw new DollarScriptException("Cannot remove field (using minus) " + rhs + " in class " + name + " no fields are " +
                                                "removeable.");


    }

    @NotNull
    @Override
    public var $modulus(@NotNull var rhs) {
        throw new DollarScriptException("Cannot modulus instance of class " + name + " all fields are fixed.");
    }

    @NotNull
    @Override
    public var $multiply(@NotNull var v) {
        throw new DollarScriptException("Cannot multiply instance of class " + name + " all fields are fixed.");
    }

    @NotNull
    @Override
    public var $negate() {
        throw new DollarScriptException("Cannot negate instance of class " + name + " all fields are fixed.");
    }

    @NotNull
    @Override
    public var $plus(@NotNull var rhs) {
        throw new DollarScriptException("Cannot remove add " + rhs + " to class " + name + " all fields are fixed.");

    }

    @NotNull
    @Override
    public var $prepend(@NotNull var value) {
        final LinkedHashMap<var, var> newMap = new LinkedHashMap<>();
        newMap.put(value.$pairKey(), value.$pairValue());
        newMap.putAll(toVarMap());
        return DollarFactory.fromValue(newMap);
    }

    @NotNull
    @Override
    public var $remove(@NotNull var key) {
        throw new DollarScriptException("Cannot remove field " + key + " in class " + name + " no fields are removeable.");
    }

    @NotNull
    @Override
    public var $removeByKey(@NotNull String key) {
        throw new DollarScriptException("Cannot remove field " + key + " in class " + name + " no fields are removeable.");
    }

    @Override
    public @NotNull
    var $set(@NotNull var key, @NotNull Object value) {
        if (mutable) {

            Variable variable = fields.get(VarKey.of(key));
            if (variable.isReadonly()) {
                throw new DollarScriptException("Cannot change field " + key + " in class " + name + " it is readonly (const)");
            } else {
                variable.setValue(DollarStatic.$(value));
            }
        } else {
            throw new DollarScriptException(
                                                   "Cannot update field " + key + " in class " + name + " all fields are immutable outside " +
                                                           "of the " +
                                                           "instance, use a member function to update the field.");
        }
        return this;
    }

    @NotNull
    @Override
    public var $size() {
        return DollarStatic.$(toJavaMap().size());
    }

    @Override
    public @NotNull
    Type $type() {
        return type;
    }

    @Override
    public boolean collection() {
        return true;
    }

    @Override
    public boolean is(@NotNull Type... types) {
        List<@NotNull Type> typeList = Arrays.asList(types);
        return typeList.contains(type) || typeList.contains(Type._ANY);
    }

    @Override
    public boolean isBoolean() {
        return false;
    }

    @Override
    public boolean isFalse() {
        return false;
    }

    @Override
    public boolean isTrue() {
        return false;
    }

    @Override
    public boolean isVoid() {
        return false;
    }

    @Override
    public boolean neitherTrueNorFalse() {
        return true;
    }

    @NotNull
    @Override
    public int size() {
        return fields.size();
    }

    @NotNull
    @Override
    public String toDollarScript() {
        StringBuilder builder = new StringBuilder("class ");
        builder.append(name);
//        builder.append("{\n");
//        for (Map.Entry<String, Variable> entry : fields.entrySet()) {
//            if (entry.getValue().isReadonly()) {
//                builder.append("const");
//            } else {
//                builder.append("var");
//            }
//            builder.append(" ");
//            builder.append("<").append(entry.getValue().getValue().$type()).append("> ");
//            if (entry.getValue().getConstraint() != null) {
//                builder.append("(").append(entry.getValue().getConstraint().toDollarScript()).append(") ");
//            }
//            builder.append(entry.getKey());
//            builder.append(" = ");
//            builder.append(entry.getValue().getValue().toDollarScript())
//                    .append(";\n");
//        }
//        builder.append("}");
        return builder.toString();
    }

    @NotNull
    @Override
    public String toHumanString() {
        return "class " + name;
    }

    @NotNull
    @Override
    public Integer toInteger() {
        throw new DollarScriptException("Cannot convert instance of class " + name + " to integer.");
    }

    @NotNull
    @Override
    public <K extends Comparable<K>, V> ImmutableMap<K, V> toJavaMap() {
        Map<K, V> varMap = varMapToMap();
        return ImmutableMap.copyOf(varMap);
    }

    @NotNull
    @Override
    public <R> R toJavaObject() {
        return (R) varMapToMap();
    }

    @NotNull
    @Override
    public ImmutableList<Object> toList() {
        final ArrayList<Object> entries = new ArrayList<>();
        for (Map.Entry<VarKey, Variable> entry : fields.entrySet()) {
            entries.add(entry.getValue().getValue().toJavaObject());
        }
        return ImmutableList.copyOf(entries);
    }

    @NotNull
    @Override
    public Number toNumber() {
        throw new DollarScriptException("Cannot convert instance of class " + name + " to number.");
    }

    @Override
    public ImmutableList<String> toStrings() {
        List<String> values = new ArrayList<>();
        ImmutableMap<String, Object> map = toJavaMap();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            assert entry.getKey() != null;
            values.add(entry.getKey());
            values.add(entry.getValue().toString());
        }
        return ImmutableList.copyOf(values);
    }

    @NotNull
    @Override
    public ImmutableList<var> toVarList() {
        final List<var> entries =
                fields.entrySet()
                        .stream()
                        .map(entry -> DollarStatic.$(entry.getKey(), entry.getValue()))
                        .collect(Collectors.toList());
        return ImmutableList.copyOf(entries);
    }

    @NotNull
    @Override
    public ImmutableMap<var, var> toVarMap() {

        LinkedHashMap<var, var> result = new LinkedHashMap<>();
        for (Map.Entry<VarKey, Variable> entry : fields.entrySet()) {
            result.put(DollarFactory.fromStringValue(entry.getKey().asString()), entry.getValue().getValue().$fix(false));
        }
        return ImmutableMap.copyOf(result);
    }

    @NotNull
    @Override
    public String toYaml() {
        Yaml yaml = new Yaml();
        return yaml.dump(fields);
    }

    @Override
    public boolean truthy() {
        return !fields.isEmpty();
    }

    @NotNull
    @Override
    public var $copy() {
        return DollarFactory.wrap(new DollarObject(name, constructor, fields));
    }

    @NotNull
    @Override
    public var $fix(int depth, boolean parallel) {
        return this;
    }

    @NotNull
    @Override
    public var $notify() {
        fields.values().forEach(v -> v.getValue().$notify());
        return this;
    }

    @NotNull
    @Override
    public Stream<var> $stream(boolean parallel) {
        return split().values().stream();
    }

    @Override
    public boolean map() {
        return true;
    }

    @Override
    public boolean pair() {
        return fields.size() == 1;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), constructor, fields, mutable);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        DollarObject that = (DollarObject) o;
        return mutable == that.mutable &&
                       Objects.equal(constructor, that.constructor) &&
                       Objects.equal(fields, that.fields);
    }

    @Override
    public @NotNull
    String toString() {
        return "class " + name;
    }

    @Override
    public int compareTo(@NotNull var o) {
        return Comparator.<var>naturalOrder().<var>compare(this, o);
    }

    @NotNull
    private LinkedHashMap<var, var> deepClone(@NotNull LinkedHashMap<var, var> o) {
        LinkedHashMap<var, var> result = new LinkedHashMap<>();
        for (Map.Entry<var, var> entry : o.entrySet()) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    @NotNull
    private var inThisScope(@NotNull ScopeExecutable<var> exe) {
        ScriptScope subScope = new ScriptScope(util().scope(), "this-" + name, false, true);
        DollarObject thisObject = new DollarObject(name, constructor, fields, true);
        subScope.set(VarKey.THIS, thisObject, null, null, new VarFlags(true, true, false, false, false, true));
        return util().inScope(true, subScope, exe).orElseThrow(() -> new AssertionError("Optional should not be null " +
                                                                                                "here"));
    }

    @NotNull
    private LinkedHashMap<var, var> mapToVarMap(@NotNull Map<?, ?> stringObjectMap) {
        LinkedHashMap<var, var> result = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : stringObjectMap.entrySet()) {
            result.put(DollarFactory.fromValue(entry.getKey()), DollarFactory.fromValue(entry.getValue()));
        }
        return result;
    }

    @NotNull
    Map<var, var> split() {
        return varMapToMap();
    }

    @NotNull
    private <K extends Comparable<K>, V> Map<K, V> varMapToMap() {
        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<var, var> entry : toVarMap().entrySet()) {
            result.put(entry.getKey().toJavaObject(), entry.getValue().toJavaObject());
        }
        return result;
    }
}
