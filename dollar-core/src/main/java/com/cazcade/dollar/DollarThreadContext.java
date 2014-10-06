package com.cazcade.dollar;

import com.cazcade.dollar.pubsub.DollarPubSub;
import com.cazcade.dollar.pubsub.RedisPubSub;
import com.cazcade.dollar.store.DollarStore;
import com.cazcade.dollar.store.RedisStore;
import com.codahale.metrics.MetricRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class DollarThreadContext {

    private List<String> labels = new ArrayList<>();
    private MetricRegistry metrics = new MetricRegistry();
    private var passValue;
    private DollarPubSub pubsub = new RedisPubSub();
    private DollarStore store = new RedisStore();
    private String threadKey = UUID.randomUUID().toString();

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    public MetricRegistry getMetrics() {
        return metrics;
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

    public void popLabel(String label) {
        String removedLabel = labels.remove(0);
        if (!label.equals(removedLabel)) {
            throw new IllegalStateException("Unbalanced label removal");
        }
    }

    public void pushLabel(String label) {
        labels.add(label);
    }

}
