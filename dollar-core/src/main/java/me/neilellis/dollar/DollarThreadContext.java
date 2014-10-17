package me.neilellis.dollar;

import me.neilellis.dollar.monitor.DefaultMonitor;
import me.neilellis.dollar.monitor.Monitor;
import me.neilellis.dollar.pubsub.DollarPubSub;
import me.neilellis.dollar.pubsub.RedisPubSub;
import me.neilellis.dollar.store.DollarStore;
import me.neilellis.dollar.store.RedisStore;
import com.codahale.metrics.MetricRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class DollarThreadContext {

    private static final MetricRegistry metrics = new MetricRegistry();
    private List<String> labels = new ArrayList<>();
    private Monitor monitor = new DefaultMonitor(metrics);
    private var passValue;
    private DollarPubSub pubsub = new RedisPubSub();
    private DollarStore store = new RedisStore();
    private String threadKey = UUID.randomUUID().toString();

    public DollarThreadContext(List<String> labels, Monitor monitor, var passValue, DollarPubSub pubsub, DollarStore store, String threadKey) {
        this.labels = labels;
        this.monitor = monitor;
        this.passValue = passValue;
        this.pubsub = pubsub;
        this.store = store;
        this.threadKey = threadKey;
    }

    public DollarThreadContext() {

    }

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    @NotNull
    public MetricRegistry getMetrics() {
        return metrics;
    }

    public Monitor getMonitor() {
        return monitor;
    }

    public void setMonitor(Monitor monitor) {
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

    @NotNull
    public DollarThreadContext child() {
       return child(UUID.randomUUID().toString());
    }

    @NotNull
    public DollarThreadContext child(String s) {
        return new DollarThreadContext(new ArrayList<>(labels), monitor, passValue, pubsub, store, threadKey + ":" + s);
    }
}
