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

package me.neilellis.dollar.monitor;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import me.neilellis.dollar.api.monitor.DollarMonitor;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class MetricsMonitor implements DollarMonitor {

  private final MetricRegistry metrics = new MetricRegistry();
  @NotNull
  private final ConsoleReporter reporter;

  static {
  }

  private final Logger log = LoggerFactory.getLogger(this.getClass());


  public MetricsMonitor() {
    reporter = ConsoleReporter.forRegistry(metrics)
                              .convertRatesTo(TimeUnit.SECONDS)
                              .convertDurationsTo(TimeUnit.MILLISECONDS)
                              .build();
  }

    @NotNull @Override public DollarMonitor copy() {
    return this;
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
