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

package dollar.internal.runtime.script;

import dollar.api.DollarException;
import dollar.internal.runtime.script.api.ParserOptions;
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

public class CodeExtractionVisitor implements Visitor {
    @Override
    public void visit(@NotNull AbbreviationNode node) {

    }

    @Override
    public void visit(@NotNull AnchorLinkNode anchorLinkNode) {

    }

    @Override
    public void visit(@NotNull AutoLinkNode node) {

    }

    @Override
    public void visit(@NotNull BlockQuoteNode node) {

    }

    @Override
    public void visit(@NotNull BulletListNode node) {

    }

    @Override
    public void visit(@NotNull CodeNode node) {

    }

    @Override
    public void visit(@NotNull DefinitionListNode node) {

    }

    @Override
    public void visit(@NotNull DefinitionNode node) {

    }

    @Override
    public void visit(@NotNull DefinitionTermNode node) {

    }

    @Override
    public void visit(@NotNull ExpImageNode node) {

    }

    @Override
    public void visit(@NotNull ExpLinkNode node) {

    }

    @Override
    public void visit(@NotNull HeaderNode node) {

    }

    @Override
    public void visit(@NotNull HtmlBlockNode node) {

    }

    @Override
    public void visit(@NotNull InlineHtmlNode node) {

    }

    @Override
    public void visit(@NotNull ListItemNode node) {

    }

    @Override
    public void visit(@NotNull MailLinkNode node) {

    }

    @Override
    public void visit(@NotNull OrderedListNode node) {

    }

    @Override
    public void visit(@NotNull ParaNode node) {

        visitChildren(node);
    }

    @Override
    public void visit(@NotNull QuotedNode node) {

    }

    @Override
    public void visit(@NotNull ReferenceNode node) {

    }

    @Override
    public void visit(@NotNull RefImageNode node) {

    }

    @Override
    public void visit(@NotNull RefLinkNode node) {

    }

    @Override
    public void visit(@NotNull RootNode node) {
        node.getReferences().forEach(this::visitChildren);
        node.getAbbreviations().forEach(this::visitChildren);
        visitChildren(node);
    }

    @Override
    public void visit(@NotNull SimpleNode node) {

    }

    @Override
    public void visit(@NotNull SpecialTextNode node) {
    }

    @Override
    public void visit(@NotNull StrikeNode node) {

    }

    @Override
    public void visit(@NotNull StrongEmphSuperNode node) {

    }

    @Override
    public void visit(@NotNull TableBodyNode node) {

    }

    @Override
    public void visit(@NotNull TableCaptionNode node) {

    }

    @Override
    public void visit(@NotNull TableCellNode node) {

    }

    @Override
    public void visit(@NotNull TableColumnNode node) {

    }

    @Override
    public void visit(@NotNull TableHeaderNode node) {

    }

    @Override
    public void visit(@NotNull TableNode node) {

    }

    @Override
    public void visit(@NotNull TableRowNode node) {

    }

    @Override
    public void visit(@NotNull VerbatimNode node) {
        if ("dollar".equals(node.getType())) {
            try {
                new DollarParserImpl(new ParserOptions()).parse(
                        new ScriptScope(node.getText(), "(markdown)", true, false), node.getText());
            } catch (Exception e) {
                throw new DollarException(e, node.getText());
            }
        }
    }

    @Override
    public void visit(@NotNull WikiLinkNode node) {

    }

    @Override
    public void visit(@NotNull TextNode node) {

    }

    @Override
    public void visit(@NotNull SuperNode node) {
        visitChildren(node);
    }

    @Override
    public void visit(@NotNull Node node) {

    }

    void visitChildren(@NotNull SuperNode node) {
        for (Node child : node.getChildren()) {
            child.accept(this);
        }
    }
}
