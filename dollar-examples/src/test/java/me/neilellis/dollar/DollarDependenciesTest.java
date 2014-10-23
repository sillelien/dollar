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

import me.neilellis.dollar.deps.DependencyRetriever;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sonatype.aether.resolution.DependencyResolutionException;
import org.sonatype.aether.util.artifact.DefaultArtifact;

import static me.neilellis.dollar.DollarStatic.$list;

public class DollarDependenciesTest {
    private static var list;

    @BeforeClass
    public static void setUp() {
        list = $list("Neil", "Dimple", "Charlie");
    }

    @Test
    public void testBasics() throws InterruptedException, DependencyResolutionException, ClassNotFoundException {
        DependencyRetriever.retrieve(new DefaultArtifact("org.twitter4j:twitter4j-core:4.0.2"));
        Thread.currentThread().getContextClassLoader().loadClass("twitter4j.Twitter");
    }


}