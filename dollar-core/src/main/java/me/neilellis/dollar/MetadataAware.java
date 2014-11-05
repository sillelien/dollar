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
 * I wouldn't use this for everyday programming.
 *
 * Once an object is given a value it cannot be changed.
 * Also, very importantly if a method returns an object based on this object the new object
 * may or may not contain the metadata.
 *
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public interface MetadataAware {

    void setMetaAttribute(String key, String value);

    String getMetaAttribute(String key);
}
