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

package me.neilellis.dollar;

import me.neilellis.dollar.monitor.DollarMonitor;
import me.neilellis.dollar.plugin.Plugins;
import me.neilellis.dollar.pubsub.DollarPubSub;
import me.neilellis.dollar.store.DollarStore;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class DollarThreadContext {


    private List<String> labels = new ArrayList<>();
    private DollarMonitor monitor = Plugins.newInstance(DollarMonitor.class);
    private var passValue;
    private DollarPubSub pubsub = Plugins.newInstance(DollarPubSub.class);
    private DollarStore store = Plugins.newInstance(DollarStore.class);
    private String threadKey = UUID.randomUUID().toString();
    private ClassLoader classLoader = DollarThreadContext.class.getClassLoader();

    public DollarThreadContext(List<String> labels, DollarMonitor monitor, var passValue, DollarPubSub pubsub,
                               DollarStore store, String threadKey) {
        this.labels = labels;
        this.monitor = monitor;
        this.passValue = passValue;
        this.pubsub = pubsub;
        this.store = store;
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
        return new DollarThreadContext(new ArrayList<>(labels), monitor, passValue, pubsub, store, threadKey + ":" + s);
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

    public DollarPubSub getPubsub() {
        return pubsub;
    }

    public void setPubsub(DollarPubSub pubsub) {
        this.pubsub = pubsub;
    }

    public DollarStore getStore() {

        System.err.println("STORE IS DEPRECATED, PLEASE USE URIs INSTEAD");
        return store;
    }

    public void setStore(DollarStore store) {
        this.store = store;
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

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }
}
