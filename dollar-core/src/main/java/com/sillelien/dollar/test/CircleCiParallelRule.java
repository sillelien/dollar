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

package com.sillelien.dollar.test;

import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.Assume;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public final class CircleCiParallelRule implements TestRule {
    @Override
    public Statement apply(Statement statement, @NotNull Description description) {

        boolean runTest = true;

        final String tName = description.getClassName() + "#" + description.getMethodName();

        final String numNodes = System.getenv("CIRCLE_NODE_TOTAL");
        final String curNode = System.getenv("CIRCLE_NODE_INDEX");

        if (StringUtils.isBlank(numNodes) || StringUtils.isBlank(curNode)) {
            System.out.println("Running locally, so skipping");
        } else {
            final int hashCode = Math.abs(tName.hashCode());

            int nodeToRunOn = hashCode % Integer.parseInt(numNodes);
            final int curNodeInt = Integer.parseInt(curNode);

            runTest = nodeToRunOn == curNodeInt;

            System.out.println("currentNode: " + curNodeInt + ", targetNode: " + nodeToRunOn + ", runTest: " + runTest);

            if (!runTest) {
                return new Statement() {
                    @Override
                    public void evaluate() {
                        Assume.assumeTrue("Skipping test, currentNode: " + curNode + ", targetNode: " + nodeToRunOn,
                                          false);
                    }
                };
            }
        }

        return statement;
    }
}