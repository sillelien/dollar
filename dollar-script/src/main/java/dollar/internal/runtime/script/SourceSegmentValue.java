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

package dollar.internal.runtime.script;

import com.sillelien.dollar.api.script.SourceSegment;
import dollar.internal.runtime.script.api.Scope;
import dollar.internal.runtime.script.api.exceptions.DollarAssertionException;
import dollar.internal.runtime.script.api.exceptions.DollarParserError;
import dollar.internal.runtime.script.util.FNV;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jparsec.Token;

import java.util.Objects;

public class SourceSegmentValue implements SourceSegment {
    @NotNull
    private final Scope scope;
    @Nullable
    private final String sourceFile;
    private final int length;
    @NotNull
    private final String source;
    private final int start;
    @NotNull
    private final String shortHash;

    public SourceSegmentValue(@NotNull Scope scope, @NotNull Token t) {
        this.scope = scope;
        sourceFile = scope.getFile();
        length = t.length();
        start = t.index();
        if(scope.getSource() == null) {
            throw new DollarParserError("Cannot create a SourceSegmentValue from a scope with no source: "+scope);
        }
        source = scope.getSource();
        shortHash = new FNV().fnv1_32(source.getBytes()).toString(36);
    }

    @NotNull
    @Override
    public String getCompleteSource() {
        return source;
    }

    @Override
    public int getLength() {
        return length;
    }

    @NotNull
    @Override
    public String getShortHash() {
        return shortHash;
    }

    @Nullable
    @Override
    public String getSourceFile() {
        return sourceFile;
    }

    @NotNull
    @Override
    public String getSourceMessage() {
        int index = getStart();
        int length = getLength();
        if ((index < 0) || (length < 0)) {
            return "<unknown location>";
        }
        if ((index + length) > source.length()) {
            throw new DollarParserError("Index=" + index + " Length=" + length + " SourceLength=" + source.length() + " Source='" + source + "'");
        }
        String[] lines = source.substring(0, index).split("\n");
        int line = lines.length;
        int column = (index == 0) ? 0 : ((source.charAt(index - 1) == '\n') ? 0 : lines[lines.length - 1].length());
        int end = ((index + length) >= source.length()) ? (source.length() - 1) : source.indexOf('\n', index + length);
        int start = index - column;
        return "\n    " +
                       source.substring(start, index).replaceAll("\n", "\n    ") +
                       " → " +
                       source.substring(index, index + length) +
                       " ← " +
                       source.substring(index + length, end).replaceAll("\n", "\n    ") +
                       "\n\n" + "see " + getSourceFile() + "(" + line + ":" + column + ")\n";
    }

    @NotNull
    @Override
    public String getShortSourceMessage() {
        int index = getStart();
        int length = getLength();
        if ((index < 0) || (length < 0)) {
            return "<unknown location>";
        }
        if ((index + length) > source.length()) {
            throw new DollarAssertionException("Index=" + index + " Length=" + length + " SourceLength=" + source.length() + " Source='" + source + "'");
        }
        String[] lines = source.substring(0, index).split("\n");
        int line = lines.length;
        int column = (index == 0) ? 0 : ((source.charAt(index - 1) == '\n') ? 0 : lines[lines.length - 1].length());
        int end = ((index + length) >= source.length()) ? (source.length() - 1) : source.indexOf('\n', index + length);
        int start = index - column;
        return " " +
                       source.substring(start, index).replaceAll("\n+", " ") +
                       " → " + source.substring(index, index + length) + " ← " +
                       source.substring(index + length, end).replaceAll("\n+", " ") +
                       " " + getSourceFile() + "(" + line + ":" + column + ")";
    }

    @NotNull
    @Override
    public String getSourceSegment() {
        return scope.getSource().substring(start, start + length);
    }

    @Override
    public int getStart() {
        return start; //TODO
    }

    @Override
    public int hashCode() {
        return Objects.hash(scope, sourceFile, length, start);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SourceSegmentValue that = (SourceSegmentValue) o;
        return (length == that.length) &&
                       (start == that.start) &&
                       Objects.equals(scope, that.scope) &&
                       Objects.equals(sourceFile, that.sourceFile);
    }
}
