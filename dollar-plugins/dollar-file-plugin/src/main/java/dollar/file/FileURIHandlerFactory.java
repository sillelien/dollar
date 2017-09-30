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

import dollar.api.exceptions.DollarFailureException;
import dollar.api.types.ErrorType;
import dollar.api.uri.URI;
import dollar.api.uri.URIHandler;
import dollar.api.uri.URIHandlerFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;


public class FileURIHandlerFactory implements URIHandlerFactory {


    @NotNull
    @Override
    public URIHandlerFactory copy() {
        return this;
    }

    @Nullable
    @Override
    public URIHandler forURI(@NotNull String scheme, @NotNull URI uri) throws IOException {
        if ("file".equals(scheme) || "file+lines+json".equals(scheme)) {
            return new FileURIHandler(scheme, uri);
        }
        throw new DollarFailureException(ErrorType.INVALID_SCHEME, "Unrecognized scheme " + scheme);
    }

    @Override
    public boolean handlesScheme(@NotNull String scheme) {
        return "file".equals(scheme) || "file+lines+json".equals(scheme);
    }
}

