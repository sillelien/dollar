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

package me.neilellis.dollar.script;

import me.neilellis.dollar.api.script.SourceSegment;
import me.neilellis.dollar.script.api.Scope;
import me.neilellis.dollar.script.util.FNV;
import org.codehaus.jparsec.Token;
import org.jetbrains.annotations.NotNull;

public class SourceSegmentValue implements SourceSegment {
    private final Scope scope;
    private final String sourceFile;
    private final int length;
    private final String source;
    private final int start;
    private String shortHash;

    public SourceSegmentValue(Scope scope, String sourceFile, String source, int start, int length) {
        this.scope = scope;
        this.sourceFile = sourceFile;
        this.length = length;
        this.source = source;
        this.start = start;
    }

    public SourceSegmentValue(@NotNull Scope scope, @NotNull Token t) {
        this.scope = scope;
        this.sourceFile = scope.getFile();
        this.length = t.length();
        this.start = t.index();
        this.source = scope.getSource();
        this.shortHash = new FNV().fnv1_32(source.getBytes()).toString(36);
    }

    @Override public String getCompleteSource() {
        return source;
    }

    @Override public int getLength() {
        return length;
    }

    @Override public String getShortHash() {
        return shortHash;
    }

    @Override public String getSourceFile() {
        return sourceFile;
    }

    @NotNull @Override public String getSourceMessage() {
        int index = getStart();
        int length = getLength();
        if (index < 0 || length < 0) {
            return "<unknown location>";
        }
        String theSource = scope.getSource();
        int end = theSource.indexOf('\n', index + length);
        int start = index > 10 ? index - 10 : 0;
        String
                highlightedSource =
                "... " +
                theSource.substring(start, index) +
                " \u261E " +
                theSource.substring(index, index + length) +
                " \u261C " +
                theSource.substring(index + length, end) +
                " ..." + " in " + getSourceFile();
        return highlightedSource.replaceAll("\n", "\\\\n");
    }

    @NotNull @Override public String getSourceSegment() {
        return scope.getSource().substring(start, start + length);
    }

    @Override public int getStart() {
        return start; //TODO
    }
}
