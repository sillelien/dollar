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

import dollar.api.Pipeable;
import dollar.api.Value;
import dollar.api.exceptions.DollarFailureException;
import dollar.api.types.DollarFactory;
import dollar.api.types.ErrorType;
import dollar.api.uri.URI;
import dollar.api.uri.URIHandler;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Stream;

import static dollar.api.DollarStatic.$void;

public class FileResourceURIHandler implements URIHandler {

    @NotNull
    private static final Logger log = LoggerFactory.getLogger(FileResourceURIHandler.class);

    private final @NotNull String file;

    private long cursor;

    public FileResourceURIHandler(@NotNull String scheme, @NotNull URI uri) {
        file = uri.sub().asString();
    }

    @NotNull
    @Override
    public Value all() {
        try {
            return DollarFactory.fromStreamStrings(lineStream());
        } catch (IOException e) {
            throw new DollarFailureException(e, ErrorType.IO);
        }
    }

    @Override
    public void destroy() {

    }

    @NotNull
    @Override
    public Value drain() {
        throw new UnsupportedOperationException("Cannot drain a resource file");
    }

    @NotNull
    @Override
    public Value get(@NotNull Value key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void init() {

    }

    @Override
    public void pause() {

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
                return lineStream()
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
            return (int) lineStream().count();
        } catch (IOException e) {
            throw new DollarFailureException(e, ErrorType.IO);
        }
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void subscribe(@NotNull Pipeable consumer, @NotNull String id) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void unpause() {

    }

    @Override
    public void unsubscribe(@NotNull String subId) {
    }

    @NotNull
    @Override
    public Value write(@NotNull Value value, boolean blocking, boolean mutating) {

        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull Value writeAll(Value all) {
        throw new UnsupportedOperationException();
    }

    private InputStream getStream() {
        InputStream resourceAsStream = getClass().getResourceAsStream(file);
        if (resourceAsStream == null) {
            throw new DollarFailureException("Could not find resource " + file);
        }
        return resourceAsStream;
    }

    private Stream<String> lineStream() throws IOException {
        return com.google.common.io.CharStreams.readLines(new BufferedReader(new InputStreamReader(getStream()))).stream();
    }

}
