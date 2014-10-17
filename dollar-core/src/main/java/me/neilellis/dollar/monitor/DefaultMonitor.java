package me.neilellis.dollar.monitor;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class DefaultMonitor implements Monitor {
    @NotNull
    private final ConsoleReporter reporter;

    static {
    }
    private Logger log = LoggerFactory.getLogger(this.getClass());
    private MetricRegistry metrics;

    public DefaultMonitor(MetricRegistry metrics) {
        this.metrics = metrics;
        reporter = ConsoleReporter.forRegistry(metrics)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build();
    }

    @Override
    public void dump() {
        reporter.report();
    }

    @Override
    public void dumpThread() {
        //TODO
    }

    @Override
    public <R> R run(String simpleLabel, String namespacedLabel, String info, @NotNull Supplier<R> code) {
        Timer timer = metrics.timer(namespacedLabel);
        log.debug("BEFORE : " + simpleLabel + " : " + info);
        Timer.Context time = timer.time();
        R r = code.get();
        time.stop();
        log.debug("AFTER : " + simpleLabel + " : " + info);
        return r;
    }

    @Override
    public void run(String simpleLabel, String namespacedLabel, String info, @NotNull Runnable code) {
        Timer timer = metrics.timer(namespacedLabel);
        log.debug("BEFORE : " + simpleLabel + " : " + info);
        Timer.Context time = timer.time();
        code.run();
        time.stop();
        log.debug("AFTER : " + simpleLabel + " : " + info);
    }

}
