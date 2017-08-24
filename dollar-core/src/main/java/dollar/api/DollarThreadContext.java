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
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DollarThreadContext {


    @NotNull
    private List<String> labels = new ArrayList<>();
    @NotNull
    private DollarMonitor monitor = Plugins.newInstance(DollarMonitor.class);
    @NotNull
    private var passValue;
    @NotNull
    private String threadKey = UUID.randomUUID().toString();
    @NotNull
    private ClassLoader classLoader = DollarThreadContext.class.getClassLoader();

    private DollarThreadContext(@NotNull List<String> labels, @NotNull DollarMonitor monitor, @NotNull var passValue,
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
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public void setClassLoader(@NotNull ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @NotNull
    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(@NotNull List<String> labels) {
        this.labels = labels;
    }

    @NotNull
    public DollarMonitor getMonitor() {
        return monitor;
    }

    public void setMonitor(@NotNull DollarMonitor monitor) {
        this.monitor = monitor;
    }

    @NotNull
    public var getPassValue() {
        return passValue;
    }

    public void setPassValue(@NotNull var passValue) {
        this.passValue = passValue;
    }

    @NotNull
    public String getThreadKey() {
        return threadKey;
    }

    public void setThreadKey(@NotNull String threadKey) {
        this.threadKey = threadKey;
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
}
