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

package dollar;

import com.google.common.io.Files;
import org.jetbrains.annotations.NotNull;
import org.pegdown.ast.AbbreviationNode;
import org.pegdown.ast.AnchorLinkNode;
import org.pegdown.ast.AutoLinkNode;
import org.pegdown.ast.BlockQuoteNode;
import org.pegdown.ast.BulletListNode;
import org.pegdown.ast.CodeNode;
import org.pegdown.ast.DefinitionListNode;
import org.pegdown.ast.DefinitionNode;
import org.pegdown.ast.DefinitionTermNode;
import org.pegdown.ast.ExpImageNode;
import org.pegdown.ast.ExpLinkNode;
import org.pegdown.ast.HeaderNode;
import org.pegdown.ast.HtmlBlockNode;
import org.pegdown.ast.InlineHtmlNode;
import org.pegdown.ast.ListItemNode;
import org.pegdown.ast.MailLinkNode;
import org.pegdown.ast.Node;
import org.pegdown.ast.OrderedListNode;
import org.pegdown.ast.ParaNode;
import org.pegdown.ast.QuotedNode;
import org.pegdown.ast.RefImageNode;
import org.pegdown.ast.RefLinkNode;
import org.pegdown.ast.ReferenceNode;
import org.pegdown.ast.RootNode;
import org.pegdown.ast.SimpleNode;
import org.pegdown.ast.SpecialTextNode;
import org.pegdown.ast.StrikeNode;
import org.pegdown.ast.StrongEmphSuperNode;
import org.pegdown.ast.SuperNode;
import org.pegdown.ast.TableBodyNode;
import org.pegdown.ast.TableCaptionNode;
import org.pegdown.ast.TableCellNode;
import org.pegdown.ast.TableColumnNode;
import org.pegdown.ast.TableHeaderNode;
import org.pegdown.ast.TableNode;
import org.pegdown.ast.TableRowNode;
import org.pegdown.ast.TextNode;
import org.pegdown.ast.VerbatimNode;
import org.pegdown.ast.Visitor;
import org.pegdown.ast.WikiLinkNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.util.Locale;

public class DocTestingVisitor implements Visitor {
    @NotNull
    private static final Logger log = LoggerFactory.getLogger(DocTestingVisitor.class);
    @Override
    public void visit(AbbreviationNode node) {

    }

    @Override
    public void visit(AnchorLinkNode node) {

    }

//    @Override
//    public void visit(AnchorLinkNode anchorLinkNode) {
//
//    }

    @Override
    public void visit(AutoLinkNode node) {

    }

    @Override
    public void visit(BlockQuoteNode node) {

    }

    @Override
    public void visit(BulletListNode node) {

    }

    @Override
    public void visit(CodeNode node) {

    }

    @Override
    public void visit(DefinitionListNode node) {

    }

    @Override
    public void visit(DefinitionNode node) {

    }

    @Override
    public void visit(DefinitionTermNode node) {

    }

    @Override
    public void visit(ExpImageNode node) {

    }

    @Override
    public void visit(ExpLinkNode node) {

    }

    @Override
    public void visit(HeaderNode node) {

    }

    @Override
    public void visit(HtmlBlockNode node) {

    }

    @Override
    public void visit(InlineHtmlNode node) {

    }

    @Override
    public void visit(ListItemNode node) {

    }

    @Override
    public void visit(MailLinkNode node) {

    }

    @Override
    public void visit(OrderedListNode node) {

    }

    @Override
    public void visit(@NotNull ParaNode node) {

        visitChildren(node);
    }

    @Override
    public void visit(QuotedNode node) {

    }

    @Override
    public void visit(ReferenceNode node) {

    }

    @Override
    public void visit(RefImageNode node) {

    }

    @Override
    public void visit(RefLinkNode node) {

    }

    @Override
    public void visit(@NotNull RootNode node) {
        node.getReferences().forEach(this::visitChildren);
        node.getAbbreviations().forEach(this::visitChildren);
        visitChildren(node);
    }

    @Override
    public void visit(SimpleNode node) {

    }

    @Override
    public void visit(SpecialTextNode node) {
    }

    @Override
    public void visit(StrikeNode node) {

    }

    @Override
    public void visit(StrongEmphSuperNode node) {

    }

    @Override
    public void visit(TableBodyNode node) {

    }

    @Override
    public void visit(TableCaptionNode node) {

    }

    @Override
    public void visit(TableCellNode node) {

    }

    @Override
    public void visit(TableColumnNode node) {

    }

    @Override
    public void visit(TableHeaderNode node) {

    }

    @Override
    public void visit(TableNode node) {

    }

    @Override
    public void visit(TableRowNode node) {

    }

    @Override
    public void visit(@NotNull VerbatimNode node) {
        if ("java".equals(node.getType())) {
            try {
                String name = "DocTemp" + System.currentTimeMillis();
                File javaFile = new File("/tmp/" + name + ".java");
                File clazzFile = new File("/tmp/" + name + ".class");
                clazzFile.getParentFile().mkdirs();
                String string = "import dollar.api.*;\n" +
                                        "import static dollar.api.DollarStatic.*;\n" +
                                        "public class " + name + " implements java.lang.Runnable{\n" +
                                        "    public void run() {\n" +
                                        "        " + node.getText() + "\n" +
                                        "    }\n" +
                                        "}";
                Files.write(string, javaFile,
                            Charset.forName("utf-8"));
                final JavaCompiler javac
                        = ToolProvider.getSystemJavaCompiler();
                final StandardJavaFileManager jfm
                        = javac.getStandardFileManager(null, null, null);
                JavaCompiler.CompilationTask task;
                DiagnosticListener<JavaFileObject> diagnosticListener = new DiagnosticListener<JavaFileObject>() {

                    @Override
                    public void report(Diagnostic diagnostic) {
                        System.out.println(diagnostic);
                        throw new RuntimeException(diagnostic.getMessage(Locale.getDefault()));
                    }
                };

                try (BufferedWriter fileOutputStream = Files.newWriter(clazzFile, Charset.forName("utf-8"))) {
                    task = javac.getTask(fileOutputStream, jfm, diagnosticListener, null, null,
                                         jfm.getJavaFileObjects(javaFile));
                }
                task.call();

                try {
                    // Convert File to a URL
                    URL url = clazzFile.getParentFile().toURL();
                    URL[] urls = {url};
                    ClassLoader cl = new URLClassLoader(urls);
                    Class cls = cl.loadClass(name);
                    ((Runnable) cls.newInstance()).run();
                } catch (MalformedURLException e) {
                    System.err.println(string);
                    log.debug(e.getMessage(), e);
                } catch (ClassNotFoundException e) {
                    System.err.println(string);
                    log.debug(e.getMessage(), e);
                } catch (InstantiationException e) {
                    System.err.println(string);
                    log.debug(e.getMessage(), e);
                } catch (IllegalAccessException e) {
                    System.err.println(string);
                    log.debug(e.getMessage(), e);
                }
                System.out.println("Parsed: " + node.getText());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
    }

    @Override
    public void visit(WikiLinkNode node) {

    }

    @Override
    public void visit(TextNode node) {

    }

    @Override
    public void visit(@NotNull SuperNode node) {
        visitChildren(node);
    }

    @Override
    public void visit(Node node) {

    }

    void visitChildren(@NotNull SuperNode node) {
        for (Node child : node.getChildren()) {
            child.accept(this);
        }
    }
}
