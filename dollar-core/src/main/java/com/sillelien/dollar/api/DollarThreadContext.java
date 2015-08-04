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

package com.sillelien.dollar.api;

import com.sillelien.dollar.api.monitor.DollarMonitor;
import com.sillelien.dollar.api.plugin.Plugins;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DollarThreadContext {


    private List<String> labels = new ArrayList<>();
    private DollarMonitor monitor = Plugins.newInstance(DollarMonitor.class);
    private var passValue;
    private String threadKey = UUID.randomUUID().toString();
    private ClassLoader classLoader = DollarThreadContext.class.getClassLoader();

    private DollarThreadContext(List<String> labels, DollarMonitor monitor, var passValue,
                                String threadKey) {
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
    public DollarThreadContext child(String s) {
        return new DollarThreadContext(new ArrayList<>(labels), monitor, passValue, threadKey + ":" + s);
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    public DollarMonitor getMonitor() {
        return monitor;
    }

    public void setMonitor(DollarMonitor monitor) {
        this.monitor = monitor;
    }

    public var getPassValue() {
        return passValue;
    }

    public void setPassValue(var passValue) {
        this.passValue = passValue;
    }

    public String getThreadKey() {
        return threadKey;
    }

    public void setThreadKey(String threadKey) {
        this.threadKey = threadKey;
    }

    public void popLabel(@NotNull String label) {
        String removedLabel = labels.remove(0);
        if (!label.equals(removedLabel)) {
            throw new IllegalStateException("Unbalanced label removal");
        }
    }

    public void pushLabel(String label) {
        labels.add(label);
    }
}
