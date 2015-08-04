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

package com.sillelien.dollar.script;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public interface OperatorPriority {
    int MEMBER_PRIORITY = 500;
    int ASSIGNMENT_PRIORITY = 10;
    int UNARY_PRIORITY = 400;
    int INC_DEC_PRIORITY = 400;
    int IN_PRIORITY = 400;
    int LINE_PREFIX_PRIORITY = 0;
    int PIPE_PRIORITY = 150;
    int EQUIVALENCE_PRIORITY = 100;
    int COMPARISON_PRIORITY = 150;
    int PLUS_MINUS_PRIORITY = 200;
    int OUTPUT_PRIORITY = 50;
    int IF_PRIORITY = 20;
    int CAST_PRIORITY = 80;
    int CONTROL_FLOW_PRIORITY = 50;
    int MULTIPLY_DIVIDE_PRIORITY = 300;
    int RANGE_PRIORITY = 600;
    int LOGICAL_AND_PRIORITY = 70;
    int LOGICAL_OR_PRIORITY = 60;
    int SIGNAL_PRIORITY = 20;

}
