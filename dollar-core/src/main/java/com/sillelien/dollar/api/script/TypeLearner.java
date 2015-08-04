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

package com.sillelien.dollar.api.script;

import com.sillelien.dollar.api.Type;
import com.sillelien.dollar.api.TypePrediction;
import com.sillelien.dollar.api.plugin.ExtensionPoint;
import com.sillelien.dollar.api.var;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public interface TypeLearner extends ExtensionPoint<TypeLearner> {

    @NotNull static ArrayList<String> perms(@NotNull List<var> inputs) {
        ArrayList<String> perms = new ArrayList<>();
        boolean first = true;
        for (var input : inputs) {
            TypePrediction inputPrediction = input._predictType();
            final Set<String> types;
            if (inputPrediction == null || inputPrediction.empty()) {
                types = Collections.singleton(Type.ANY.toString());
            } else {
                types = inputPrediction.types().stream().map(Type::toString).collect(Collectors.toSet());
            }
            if (first) {
                perms.addAll(types);
                first = false;
            } else {
                for (String type : types) {
                    ArrayList<String> newPerms = new ArrayList<>();
                    for (String perm : perms) {
                        newPerms.add(perm + "-" + type);
                    }
                    perms = newPerms;
                }
            }
        }
        return perms;
    }

    void learn(String name, SourceSegment source, List<var> inputs, Type type);

    TypePrediction predict(String name, SourceSegment source, List<var> inputs);

}
