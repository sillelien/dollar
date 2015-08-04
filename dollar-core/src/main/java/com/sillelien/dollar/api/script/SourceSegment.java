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

package com.sillelien.dollar.api.script;

public interface SourceSegment {

    /**
     * Returns the complete source code from which this segment comes.
     *
     * @return the source
     */
    String getCompleteSource();

    /**
     * The length of the segment.
     *
     * @return the length
     */
    int getLength();

    /**
     * Gets a short hash code of the complete source.
     *
     * @return the short hash
     */
    String getShortHash();

    /**
     * Returns the name of the source file from which the source originates.
     *
     * @return the source file
     */
    String getSourceFile();

    /**
     * Returns a message describing the position in the source of this segment.
     *
     * @return the source message
     */
    String getSourceMessage();

    /**
     * Gets the source segment.
     *
     * @return the source segment
     */
    String getSourceSegment();

    /**
     * Get's the starting point in the {@link #getCompleteSource()} of this source code segment.
     *
     * @return the starting point
     */
    int getStart();
}
