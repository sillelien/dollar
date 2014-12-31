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

/**
 * Metadata is really only for use by the DollarScript language,
 * Don't use this for Java programming.
 *
 * Once an object is given a value it cannot be changed.
 * Also, very importantly if a method returns an object based on this object the new object
 * may or may not contain the metadata.
 *
 * @author  <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public interface MetadataAware {

    /**
     * Gets meta attribute.
     *
     * @param key the key
     *
     * @return the meta attribute
     */
    String getMetaAttribute(String key);

    /**
     * Sets meta attribute.
     *
     * @param key   the key
     * @param value the value
     */
    void setMetaAttribute(String key, String value);
}
