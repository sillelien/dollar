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

package com.sillelien.dollar.api.uri;

import com.sillelien.dollar.api.DollarException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.*;

public class URI implements Serializable {
    @NotNull
    protected String uri;
    private volatile Map<String, List<String>> query;


    protected URI(@NotNull final String scheme, @NotNull final String path) {
        this(scheme + ":" + path);
    }


    protected URI(@Nullable final String uri) {
        if (uri == null) {
            throw new NullPointerException("Attempted to create a LURI with a null uri string parameter");
        }
        if (uri.isEmpty()) {
            throw new IllegalArgumentException("Attempted to create an empty URI.");
        }
        //Various  URIs should be lowercase only
        if (uri.startsWith("http:")
            || uri.startsWith("mailto:")
            || uri.startsWith("ftp:")
            || uri.startsWith("https:")) {
            this.uri = uri.toLowerCase();
        } else {
            this.uri = uri;
        }
    }

    protected URI(@NotNull final String scheme, @NotNull final URI url) {
        this(scheme + ":" + url.asString());
    }

    @NotNull
    public String asString() {
        return uri;
    }

    protected URI(@NotNull final URI url, @NotNull final String subPath) {
        this(url.path().endsWith("/")
             ? url + subPath
             : subPath +
               "/" +
               subPath);
    }

    @NotNull public String path() {
        final String withoutQuery;
        withoutQuery = withoutQuery().asString();
        int i = withoutQuery.indexOf(':');
        if (i >= 0) {
            final String withoutScheme = withoutQuery.substring(i + 1);
            if (withoutScheme.startsWith("//")) {
                i = withoutScheme.indexOf('/', 3);
            }
            return withoutScheme.substring(i);
        } else {
            return withoutQuery;
        }
    }

    @NotNull public URI withoutQuery() {
        if (uri.contains("?")) {
            final int i = uri.indexOf('?');
            return new URI(uri.substring(0, i));
        } else {
            return new URI(uri);
        }
    }

    protected URI() {
    }

    @NotNull public static URI parse(String s) {
        return new URI(s);
    }

    @NotNull
    public String asReverseDNSString() {
        final String scheme = schemeString();
        String reverseDNS = "";
        if (scheme == null) {
            int count = 0;
            for (final String element : pathElements()) {
                if (element.length() > 0) {
                    reverseDNS = (count == 0 ? "" : reverseDNS + ".") + element;
                    count++;
                }
            }
            if (getFragment() != null && getFragment().length() > 0) {
                reverseDNS = (count == 0 ? "" : reverseDNS + ".") + getFragment().substring(1);
            }
        } else {
            reverseDNS = scheme + "." + sub().asReverseDNSString();
        }
        return reverseDNS;
    }

    public String authority() {
        try {
            return new java.net.URI(uri).getAuthority();
        } catch (URISyntaxException e) {
            throw new DollarException(e);
        }
    }

    @NotNull public String getFragment() {
        final int index = uri.indexOf('#');
        if (index < 0) {
            return "";
        } else {
            return uri.substring(index);
        }
    }

    public boolean hasFragment() {
        return uri.contains("#");
    }

    public boolean hasParam(String param) {
        return query().containsKey(param);
    }

    public Map<String, List<String>> query() {
        if (this.query == null) {
            final Map<String, List<String>> query_pairs = new LinkedHashMap<>();
            final String[] pairs = queryString().split("&");
            for (String pair : pairs) {
                final int idx = pair.indexOf("=");
                final String key;
                try {
                    key = idx > 0 ? URLDecoder.decode(pair.substring(0, idx), "UTF-8") : pair;
                    if (!query_pairs.containsKey(key)) {
                        query_pairs.put(key, new LinkedList<>());
                    }
                    final String value;
                    value =
                            idx > 0 && pair.length() > idx + 1 ?
                            URLDecoder.decode(pair.substring(idx + 1), "UTF-8") :
                            null;
                    query_pairs.get(key).add(value);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            this.query = query_pairs;
            return query_pairs;
        } else {
            return this.query;
        }

    }

    @NotNull public String queryString() {return uri.contains("?") ? uri.split("\\?")[1] : "";}

    public boolean hasSubScheme() {
        final int i = getColonPos();
        if (i <= 0) {
            return false;
        }
        final String substring = uri.substring(i + 1);
        return substring.matches("^[a-zA-Z0-9_-]+:.*");
    }

    private int getColonPos() {
        final int i = uri.indexOf(':');
        if (i < 0) {
            throw new DollarException("No scheme.");
        }
        return i;
    }

    @Override
    public int hashCode() {
        return uri.hashCode();
    }

    @Override
    public boolean equals(@Nullable final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final URI oUri = (URI) o;

        return uri.equals(oUri.uri);

    }

    @NotNull
    public String toString() {
        return uri;
    }

    public String host() {
        try {
            return new java.net.URI(uri).getHost();
        } catch (URISyntaxException e) {
            throw new DollarException(e);
        }
    }

    @Nullable
    public String lastPath() {
        final String[] pathElements = pathElements();
        if (pathElements.length == 0) {
            return null;
        }
        return pathElements[pathElements.length - 1];
    }

    @NotNull
    private String[] pathElements() {
        final String path = path();
        return path.split("/");
    }

    public List<String> paramWithDefault(String key, String defaultValue) {
        return query().getOrDefault(key, Arrays.asList(defaultValue));
    }

    /**
     * parent().equals(this) if  top level path element
     */
    @NotNull
    public URI parent() {
        final String scheme = schemeString() + ":";
        final String path = path();
        final int i = path.lastIndexOf('/');
        if (i <= 0) {
            return new URI(uri);
        }
        return new URI(scheme + path.substring(0, i));
    }

    @Nullable
    public String schemeString() {
        final int index = uri.indexOf(':');
        if (index <= 0) {
            return null;
        }
        return uri.substring(0, index);
    }

    public int port() {
        try {
            return new java.net.URI(uri).getPort();
        } catch (URISyntaxException e) {
            throw new DollarException(e);
        }
    }

    @Nullable public String scheme() {
        final String schemeStr = schemeString();
        if (schemeStr == null) {
            throw new DollarException("No scheme.");
        }
        return schemeStr;
    }

    @NotNull public URI sub() {
        final int i = getColonPos();
        return new URI(uri.substring(i + 1));
    }

    public String userInfo() {
        try {
            return new java.net.URI(uri).getUserInfo();
        } catch (URISyntaxException e) {
            throw new DollarException(e);
        }
    }

    @NotNull public URI withoutFragment() {
        if (uri.contains("#")) {
            final int i = uri.indexOf('#');
            return new URI(uri.substring(0, i));
        } else {
            return new URI(uri);
        }
    }

    @NotNull public URI withoutFragmentOrComment() {
        String newStr = uri;
        if (uri.contains("#")) {
            final int i = uri.indexOf('#');
            newStr = uri.substring(0, i);
        }
        if (uri.contains("~")) {
            final int i = uri.indexOf('~');
            newStr = uri.substring(0, i);
        }
        return new URI(newStr);
    }

}
