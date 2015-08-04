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

package com.sillelien.dollar.api;

import com.sillelien.dollar.api.json.JsonUtil;
import com.sillelien.dollar.api.types.DollarFactory;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public abstract class Unit extends DollarStatic implements Pipeable {

    private static final ThreadLocal<Class<? extends Unit>> $THIS = new ThreadLocal<>();
    protected static List<String> args;
    @NotNull
//  protected var passedIn = DollarStatic.threadContext.get().getPassValue();
    protected final var in = DollarFactory.fromValue(JsonUtil.argsToJson(args));
    protected var out;

    protected static void mainClass(Class<? extends Unit> main) {
        $THIS.set(main);
    }

//    public static void requires(String artifact) {
//        try {
//            DependencyRetriever.retrieve(new DefaultArtifact(artifact));
//        } catch (DependencyResolutionException e) {
//            DollarStatic.logAndRethrow(e);
//        }
//    }

    public static void main(String[] args) throws IllegalAccessException, InstantiationException {
        if ($THIS.get() == null) {
            System.err.println("Please add a line in this class which says 'public static void main(String[] args) {run(<classname>.class,args);}'");
            System.exit(-1);
        }

    }

    protected static void run(Class<? extends Unit> main, String[] args) {
        $THIS.set(main);
        Unit.args = Arrays.asList(args);
        $run(() -> {
            try {
                Unit $this = $THIS.get().newInstance();
                if ($this.in == null) {
                    throw new NullPointerException();
                }
                $this.out = $this.pipe($this.in);
                if ($this.out == null) {
                    $this.out = $void();
                }
                if (!$this.out.isVoid()) {
                    System.out.println($this.out);
                }
            } catch (@NotNull InstantiationException | IllegalAccessException e) {
                throw new Error(e.getCause());
            } catch (AssertionError e) {
                System.err.println(e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public var pipe(var... in) throws Exception {
        return in[0];
    }

    @NotNull
    public var result() {
        return out != null ? out : in;
    }


}
