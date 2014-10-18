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
 * Created by neil on 10/15/14.
 */
public interface StateTracer {
    enum Operations {
        EVAL, LOAD, PIPE, POP, REMOVE_BY_VALUE, REMOVE_BY_KEY, SAVE, SPLIT, PUBLISH, PUSH, CREATE, HTTP_RESPONSE,
        CLEAR_ERRORS, SET
    }
    <R> R trace(Object before, R after, Operations operationType, Object... values);
}
