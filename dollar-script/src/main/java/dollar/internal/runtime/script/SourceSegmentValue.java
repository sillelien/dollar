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
import dollar.internal.runtime.script.util.FNV;
import org.jetbrains.annotations.NotNull;
import org.jparsec.Token;

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

    @Override
    public String getCompleteSource() {
        return source;
    }

    @Override
    public int getLength() {
        return length;
    }

    @Override
    public String getShortHash() {
        return shortHash;
    }

    @Override
    public String getSourceFile() {
        return sourceFile;
    }

    @NotNull
    @Override
    public String getSourceMessage() {
        int index = getStart();
        int length = getLength();
        if (index < 0 || length < 0) {
            return "<unknown location>";
        }
        String theSource = scope.getSource();
        String[] lines = theSource.substring(0, index).split("\n");
        int line = lines.length;
        int column = index == 0 ? 0 : (theSource.charAt(index - 1) == '\n' ? 0 : lines[lines.length - 1].length());
        int end = index + length >= theSource.length() ? theSource.length() - 1 : theSource.indexOf('\n', index + length);
        int start = index - column;
        String
                highlightedSource =
                "\n    " +
                        theSource.substring(start, index).replaceAll("\n", "\n    ") +
                        " → " +
                        theSource.substring(index, index + length) +
                        " ← " +
                        theSource.substring(index + length, end).replaceAll("\n", "\n    ") +
                        "\n\n" + "see " + getSourceFile() + "(" + line + ":" + column + ")\n";
        return highlightedSource;
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
}
