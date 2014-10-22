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

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public interface BasicIOAware {
    @NotNull
    var $load(@NotNull String location);

    @NotNull
    var $pop(@NotNull String location, int timeoutInMillis);

    @NotNull
    var $pub(@NotNull String... locations);

    @NotNull
    var $push(@NotNull String location);

    var $read(File file);

    var $read(InputStream in);

    @NotNull
    var $save(@NotNull String location);

    @NotNull
    var $save(@NotNull String location, int expiryInMilliseconds);

    var $write(File file);

    var $write(OutputStream out);

}
