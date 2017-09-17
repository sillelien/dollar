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

package dollar.java;

import dollar.api.DollarException;
import dollar.api.Scope;
import dollar.api.Value;
import dollar.api.plugin.ExtensionPoint;
import dollar.api.scripting.ScriptingLanguage;
import dollar.api.types.DollarFactory;
import jdk.jshell.JShell;
import jdk.jshell.JShellException;
import jdk.jshell.Snippet;
import jdk.jshell.SnippetEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

/**
 */
@SuppressWarnings("UseOfSystemOutOrSystemErr")

public final class Java9ScriptingLanguage implements ScriptingLanguage {


    @NotNull
    public static final String DROPPED_DESC = "The snippet is inactive because of an explicit call to the JShell.drop(Snippet).\n" +
                                                      "The snippet is not visible to other snippets (isDefined() == false) and thus cannot be referenced or executed.\n" +
                                                      "The snippet will not update as dependents change (isActive() == false), its Status will never change again.";
    @NotNull
    public static final String NONEXISTENT_DESC = "The snippet is inactive because it does not yet exist. Used only in SnippetEvent.previousStatus for new snippets. JShell.status(Snippet) will never return this Status.\n" +
                                                          "Vacuously, isDefined() and isActive() are both defined false.";
    @NotNull
    public static final String OVERWRITTEN_DESC = "The snippet is inactive because it has been replaced by a new snippet. This occurs when the new snippet added with JShell.eval matches a previous snippet. A TypeDeclSnippet will match another TypeDeclSnippet if the names match. For example class X { } will overwrite class X { int ii; } or interface X { }. A MethodSnippet will match another MethodSnippet if the names and parameter types match. For example void m(int a) { } will overwrite int m(int a) { return a+a; }. A VarSnippet will match another VarSnippet if the names match. For example double z; will overwrite long z = 2L;. Only a PersistentSnippet can have this Status.\n" +
                                                          "The snippet is not visible to other snippets (isDefined() == false) and thus cannot be referenced or executed.\n" +
                                                          "The snippet will not update as dependents change (isActive() == false), its Status will never change again.";
    @NotNull
    public static final String REC_DEF_DESC = "The snippet is a declaration snippet with potentially recoverable unresolved references or other issues in its body (in the context of current JShell state). Only a DeclarationSnippet can have this Status.\n" +
                                                      "The snippet has a valid signature and it is visible to other snippets (isDefined() == true) and thus can be referenced in existing or new snippets but the snippet cannot be executed. An UnresolvedReferenceException will be thrown on an attempt to execute it.\n" +
                                                      "The snippet will update as dependents change (isActive() == true), its status could become VALID, RECOVERABLE_NOT_DEFINED, DROPPED, or OVERWRITTEN.\n" +
                                                      "Note: both RECOVERABLE_DEFINED and RECOVERABLE_NOT_DEFINED indicate potentially recoverable errors, they differ in that, for RECOVERABLE_DEFINED, the snippet is defined.";
    @NotNull
    public static final String REC_NOT_DEF_DESC = "The snippet is a declaration snippet with potentially recoverable unresolved references or other issues (in the context of current JShell state). Only a DeclarationSnippet can have this Status.\n" +
                                                          "The snippet has an invalid signature or the implementation is otherwise unable to define it. The snippet it is not visible to other snippets (isDefined() == false) and thus cannot be referenced or executed.\n" +
                                                          "The snippet will update as dependents change (isActive() == true), its status could become VALID, RECOVERABLE_DEFINED, DROPPED, or OVERWRITTEN.\n" +
                                                          "Note: both RECOVERABLE_DEFINED and RECOVERABLE_NOT_DEFINED indicate potentially recoverable errors, they differ in that, for RECOVERABLE_DEFINED, the snippet is defined.";
    @NotNull
    public static final String REJECTED_DESC = "The snippet is inactive because it failed compilation on initial evaluation and it is not capable of becoming valid with further changes to the JShell state.\n" +
                                                       "The snippet is not visible to other snippets (isDefined() == false) and thus cannot be referenced or executed.\n" +
                                                       "The snippet will not update as dependents change (isActive() == false), its Status will never change again.";
    @NotNull
    private static final Logger log = LoggerFactory.getLogger("ScriptingSupport");

    @Override
    @NotNull
    public Value compile(@NotNull String script, @NotNull Scope scope) {

        Value result;
        try (JShell shell = JShell.builder()

                                    .out(System.out)
                                    .err(System.err)
                                    .build()) {

            List<String> imports = Arrays.asList("dollar.lang.*", "dollar.internal.runtime.script.api.*", "dollar.api.*",
                                                 "dollar.api.types.*", "java.io.*",
                                                 "java.math.*", "java.net.*", "java.nio.file.*", "java.util.*",
                                                 "java.util.concurrent.*",
                                                 "java.util.function.*", "java.util.prefs.*", "java.util.regex.*",
                                                 "java.util.stream.*");
            List<String> staticImports = Arrays.asList("dollar.api.DollarStatic.*", "dollar.java.JavaScriptingStaticImports.*");
            shell.addToClasspath(System.getProperty("java.class.path"));
            shell.addToClasspath(System.getProperty("user.home") + "/.dollar/tmp/classes");
            for (String anImport : imports) {
                jshell(shell, "import " + anImport + ";", false);
            }
            for (String anImport : staticImports) {
                jshell(shell, "import static " + anImport + ";", false);
            }
            jshell(shell, "java.util.List<dollar.api.Value> in= new java.util.ArrayList<dollar.api.Value>();", true);
            for (Value var : scope.parametersAsVars()) {
                jshell(shell, "in.add(DollarFactory.deserialize64(\"" + DollarFactory.serialize64(var) + "\"));", true);
            }
            jshell(shell, "Value out = DollarStatic.$void();", false);
            jshell(shell, script.trim(), true);
            List<SnippetEvent> out = shell.eval("DollarFactory.serialize64(out);");
            if (out.size() != 1) {
                throw new DollarException("Unexpected result " + out);
            }
            String resultStr = out.get(0).value();
            log.debug(resultStr);
            result = DollarFactory.deserialize64(resultStr.substring(1, resultStr.length() - 1));

        } catch (Exception e) {
            log.debug(e.getMessage(), e);
            return scope.handleError(e);
        }
        return result;
    }

    @Override
    public boolean provides(@NotNull String language) {
        return "java".equals(language);
    }

    @NotNull
    @Override
    public ExtensionPoint copy() {
        return null;
    }

    @Override
    public int priority() {
        return 0;
    }

    private void jshell(@NotNull JShell shell, @NotNull String s, boolean showLog) throws JShellException {

        if (showLog) {
            log.info("$ {}", s);
        }

        List<SnippetEvent> eval = shell.eval(s);

        if (showLog) {
            if (eval.isEmpty()) {
                log.info("> ");
            }
            for (SnippetEvent event : eval) {
                showResult(event);
            }
        }

    }

    private void showResult(@NotNull SnippetEvent event) throws JShellException {
        if (event.status() == Snippet.Status.VALID) {
            if (event.value() != null) {
                log.info("> {}", event.value());
            } else {
                if (event.exception() != null) {
                    log.error(event.exception().getMessage(), event.exception());
                    throw event.exception();
                } else {
                    log.debug("Event: {}", event);
                }
            }
        } else {
            if (event.exception() != null) {
                log.error(event.exception().getMessage(), event.exception());
                throw event.exception();
            }
            switch (event.status()) {
                case REJECTED:
                    log.warn("REJECTED: {}", event);
                    log.debug(REJECTED_DESC);
                    break;
                case DROPPED:
                    log.warn("DROPPED: {}", event);
                    log.debug(DROPPED_DESC);
                    break;
                case NONEXISTENT:
                    log.warn("NONEXISTENT: {}", event);
                    log.debug(NONEXISTENT_DESC);
                    break;
                case OVERWRITTEN:
                    log.warn("OVERWRITTEN: {}", event);
                    log.debug(OVERWRITTEN_DESC);
                    break;
                case RECOVERABLE_DEFINED:
                    log.warn("RECOVERABLE_DEFINED: {}", event);
                    log.debug(REC_DEF_DESC);
                    break;
                case RECOVERABLE_NOT_DEFINED:
                    log.warn("RECOVERABLE_NOT_DEFINED: {}", event);
                    log.debug(REC_NOT_DEF_DESC);
                    break;
                default:
                    throw new IllegalStateException();

            }
        }

    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void stop() throws Exception {

    }
}
