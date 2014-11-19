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

package me.neilellis.dollar.script;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class OperatorPriority {
    public static final int MEMBER_PRIORITY = 500;
    public static final int ASSIGNMENT_PRIORITY = 10;
    public static final int UNARY_PRIORITY = 400;
    public static final int INC_DEC_PRIORITY = 400;
    public static final int IN_PRIORITY = 400;
    public static final int LINE_PREFIX_PRIORITY = 0;
    public static final int PIPE_PRIORITY = 150;
    public static final int EQUIVALENCE_PRIORITY = 100;
    public static final int COMPARISON_PRIORITY = 150;
    public static final int PLUS_MINUS_PRIORITY = 200;
    public static final int OUTPUT_PRIORITY = 50;
    public static final int IF_PRIORITY = 20;
    public static final int CAST_PRIORITY = 80;
    public static final int CONTROL_FLOW_PRIORITY = 50;
    public static final int MULTIPLY_DIVIDE_PRIORITY = 300;
    public static final int RANGE_PRIORITY = 600;
    public static final int LOGICAL_AND_PRIORITY = 70;
    public static final int LOGICAL_OR_PRIORITY = 60;
}
