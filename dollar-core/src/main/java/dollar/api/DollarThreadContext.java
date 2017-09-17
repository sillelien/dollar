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

package dollar.api;

import dollar.api.monitor.DollarMonitor;
import dollar.api.plugin.Plugins;
import dollar.api.script.DollarParser;
import dollar.api.script.Source;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class DollarThreadContext {


    @NotNull
    private ClassLoader classLoader = DollarThreadContext.class.getClassLoader();
    @NotNull
    private List<String> labels = new ArrayList<>();
    @NotNull
    private DollarMonitor monitor = Plugins.newInstance(DollarMonitor.class);
    @Nullable
    private DollarParser parser;
    @Nullable
    private Value passValue;
    @Nullable
    private Source source;
    @NotNull
    private String threadKey = UUID.randomUUID().toString();

    private DollarThreadContext(@NotNull List<String> labels, @NotNull DollarMonitor monitor, @Nullable Value passValue,
                                @NotNull String threadKey) {
        this.labels = labels;
        this.monitor = monitor;
        this.passValue = passValue;
        this.threadKey = threadKey;
    }

    public DollarThreadContext() {

    }

    @NotNull
    public DollarThreadContext child() {
        return child(UUID.randomUUID().toString());
    }

    @NotNull
    public DollarThreadContext child(@NotNull String s) {
        return new DollarThreadContext(new ArrayList<>(labels), monitor, passValue, threadKey + ":" + s);
    }


    @NotNull
    public ClassLoader classLoader() {
        return classLoader;
    }

    public void classLoader(@NotNull ClassLoader classLoader) {
        this.classLoader = classLoader;
    }


    @NotNull
    public List<String> labels() {
        return Collections.unmodifiableList(labels);
    }

    public void labels(@NotNull List<String> labels) {
        this.labels = labels;
    }


    @NotNull
    public DollarMonitor monitor() {
        return monitor;
    }

    public void monitor(@NotNull DollarMonitor monitor) {
        this.monitor = monitor;
    }


    public @Nullable DollarParser parser() {
        return parser;
    }

    public void parser(@NotNull DollarParser parser) {
        this.parser = parser;
    }


    @Nullable
    public Value passValue() {
        return passValue;
    }

    public void passValue(@NotNull Value passValue) {
        this.passValue = passValue;
    }


    public void popLabel(@NotNull String label) {
        String removedLabel = labels.remove(0);
        if (!label.equals(removedLabel)) {
            throw new IllegalStateException("Unbalanced label removal");
        }
    }

    public void pushLabel(@NotNull String label) {
        labels.add(label);
    }


    public @Nullable Source source() {
        return source;
    }

    public void source(@NotNull Source source) {
        this.source = source;
    }

    @NotNull
    public String threadKey() {
        return threadKey;
    }

    public void threadKey(@NotNull String threadKey) {
        this.threadKey = threadKey;
    }
}
