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

package dollar.file;

import com.sun.nio.file.SensitivityWatchEventModifier;
import dollar.api.Pipeable;
import dollar.api.Value;
import dollar.api.exceptions.DollarFailureException;
import dollar.api.types.DollarFactory;
import dollar.api.types.ErrorType;
import dollar.api.uri.URI;
import dollar.api.uri.URIHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static dollar.api.DollarStatic.$void;

public class FileURIHandler implements URIHandler {

    public static final int BLOCKING_TIMEOUT = 10;
    @NotNull
    private static final Logger log = LoggerFactory.getLogger(FileURIHandler.class);
    @NotNull
    private final File canonicalFile;
    @NotNull
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    @NotNull
    private final File file;
    @NotNull
    private final ConcurrentHashMap<String, FileChangeHandler> subscriptions = new ConcurrentHashMap<>();
    @NotNull
    private final URI uri;
    private long cursor;
    @Nullable
    private Future<@NotNull Value> future;
    private boolean stopped = true;

    public FileURIHandler(@NotNull String scheme, @NotNull URI uri) {

        File canonicalFile = new File(uri.sub().asString());
        File file;
        do {
            file = canonicalFile;
            try {
                canonicalFile = file.getCanonicalFile();
            } catch (IOException e) {
                throw new DollarFailureException(e, ErrorType.IO);
            }
        } while (!canonicalFile.equals(file));
        this.file = new File(uri.sub().asString());
        this.canonicalFile = canonicalFile;
        this.uri = uri;
    }

    @NotNull
    @Override
    public Value all() {
        try {
            return DollarFactory.fromStreamStrings(Files.lines(file.toPath()));
        } catch (IOException e) {
            throw new DollarFailureException(e, ErrorType.IO);
        }
    }

    @Override
    public void destroy() {
        if (future != null) {
            future.cancel(true);
            future = null;
        }
        executor.shutdown();
    }

    @NotNull
    @Override
    public Value drain() {
        if (file.exists()) {
            Value result = all();
            try {
                Files.write(file.toPath(), new byte[0], StandardOpenOption.TRUNCATE_EXISTING);
                cursor = 0;
            } catch (IOException e) {
                throw new DollarFailureException(e, ErrorType.IO);
            }
            return result;
        } else {
            return $void();
        }
    }

    @NotNull
    @Override
    public Value get(@NotNull Value key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void init() {
        stopped = true;
        Callable<Value>[] callable = new Callable[1];
        callable[0] = () -> {
            log.info("Watching files");
            try (FileSystem fileSystem = FileSystems.getDefault()) {
                try (WatchService watcher = fileSystem.newWatchService()) {
                    if ("sun.nio.fs.PollingWatchService".equals(watcher.getClass().getName())) {
                        log.warn(
                                "There is no native file watching service for your platform, the JDK has defaulted you to the PollingWatchService which can be very slow (> 1s) in responding to changes; learn more here: https://stackoverflow.com/questions/9588737/is-java-7-watchservice-slow-for-anyone-else ");
                    }
                    Path path = canonicalFile.getParentFile().toPath();
                    path.register(watcher, new WatchEvent.Kind[]{StandardWatchEventKinds.ENTRY_MODIFY},
                                  SensitivityWatchEventModifier.HIGH);

                    while (!stopped) {
                        log.info("Waiting for changes to " + path);
                        WatchKey key;
                        try {
                            key = watcher.take();
                        } catch (InterruptedException e) {
                            Thread.interrupted();
                            log.debug(e.getMessage(), e);
                            return $void();
                        }

                        log.debug("Key: {}", key);

                        for (WatchEvent<?> event : key.pollEvents()) {
                            log.debug("Event: {}", event);
                            WatchEvent.Kind<?> kind = event.kind();

                            @SuppressWarnings("unchecked")
                            WatchEvent<Path> ev = (WatchEvent<Path>) event;
                            Path filename = ev.context();

                            if (Objects.equals(kind, StandardWatchEventKinds.OVERFLOW)) {
                                continue;
                            } else if (filename.endsWith(canonicalFile.getName())) {
                                log.debug("Calling subscriptions");
                                try {
                                    for (FileChangeHandler changeHandler : subscriptions.values()) {
                                        changeHandler.notifyFileChange(true);
                                    }
                                } catch (RuntimeException e) {
                                    log.error(e.getMessage(), e);
                                    throw e;
                                }
                            } else {
                                log.debug("{} != {} ", filename, canonicalFile.getName());
                            }
                            boolean invalid = !key.reset();
                            if (invalid) { break; }
                        }
                    }
                    if (stopped) {
                        Thread.sleep(100);
                    }
                    return $void();
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                    return $void();
                } finally {
                    future = executor.submit(callable[0]);
                }
            }
        };
        future = executor.submit(callable[0]);
    }

    @Override
    public void pause() {
        stopped = true;
    }

    @NotNull
    @Override
    public Value read(boolean blocking, boolean mutating) {
        if (mutating) {
            throw new DollarFailureException(ErrorType.INVALID_URI_OPERATION,
                                             "Mutating reads are not yet supported on" +
                                                     " file+lines URIs");
        } else {
            try {
                return Files.lines(file.toPath())
                               .skip(cursor++)
                               .map(DollarFactory::fromJsonString)
                               .findFirst()
                               .orElse($void());
            } catch (IOException e) {
                throw new DollarFailureException(e, ErrorType.IO);
            }
        }

    }

    @NotNull
    @Override
    public Value remove(@NotNull Value key) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public Value removeValue(@NotNull Value v) {
        throw new UnsupportedOperationException();
    }

    @Override
    @NotNull
    public Value set(@NotNull Value key, @NotNull Value value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int size() {
        try {
            return (int) Files.lines(file.toPath()).count();
        } catch (IOException e) {
            throw new DollarFailureException(e, ErrorType.IO);
        }
    }

    @Override
    public void start() {
        stopped = false;
    }

    @Override
    public void stop() {
        stopped = true;
    }

    @Override
    public void subscribe(@NotNull Pipeable consumer, @NotNull String id) throws IOException {
        subscriptions.put(id, new FileChangeHandler(consumer));
    }

    @Override
    public void unpause() {
        stopped = false;
    }

    @Override
    public void unsubscribe(@NotNull String subId) {
        subscriptions.remove(subId);
    }

    @NotNull
    @Override
    public Value write(@NotNull Value value, boolean blocking, boolean mutating) {

        try {
            Object jsonValue = DollarFactory.toJson(value);
            assert jsonValue != null;
            Files.write(file.toPath(), jsonValue.toString().getBytes(), StandardOpenOption.APPEND, StandardOpenOption.CREATE,
                        StandardOpenOption.WRITE);
            Files.write(file.toPath(), "\n".getBytes(), StandardOpenOption.APPEND);
            return DollarFactory.fromValue(jsonValue);
        } catch (IOException e) {
            throw new DollarFailureException(e, ErrorType.IO);
        }
    }

    @Override
    public @NotNull Value writeAll(Value all) {

        try {
            for (Value value : all.toVarList()) {
                Object jsonValue = DollarFactory.toJson(value);
                assert jsonValue != null;
                Files.write(file.toPath(), jsonValue.toString().getBytes(), StandardOpenOption.APPEND, StandardOpenOption.CREATE,
                            StandardOpenOption.WRITE);
                Files.write(file.toPath(), "\n".getBytes(), StandardOpenOption.APPEND);

            }
            cursor = 0;
            return all;
        } catch (IOException e) {
            throw new DollarFailureException(e, ErrorType.IO);
        }
    }

    public class FileChangeHandler {
        @NotNull
        private final Pipeable consumer;

        public FileChangeHandler(@NotNull Pipeable consumer) {
            this.consumer = consumer;
        }

        public void notifyFileChange(boolean reset) {
            if (reset) {
                cursor = 0;
            }
            try {
                int currSize = size();
                if (cursor < currSize) {
                    Files.lines(file.toPath())
                            .skip(cursor++)
                            .map(DollarFactory::fromJsonString)
                            .forEach(i -> {
                                try {
                                    consumer.pipe(i);
                                } catch (Exception e) {
                                    throw new DollarFailureException(e);
                                }
                            });
                } else if (cursor > currSize) {
                    cursor = 0;
                }
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                throw new DollarFailureException(e, ErrorType.IO);
            }
        }
    }
}
