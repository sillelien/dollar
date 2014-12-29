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

import me.neilellis.dollar.DollarException;
import org.pegdown.ast.*;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
class CodeExtractionVisitor implements Visitor {
    @Override
    public void visit(AbbreviationNode node) {

    }

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
    public void visit(ParaNode node) {

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
    public void visit(RootNode node) {
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
    public void visit(VerbatimNode node) {
        if ("dollar".equals(node.getType())) {
            try {
                new DollarParser(new ParserOptions()).parse(node.getText(), false);
            } catch (Exception e) {
                throw new DollarException(e, node.getText());
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
    public void visit(SuperNode node) {
        visitChildren(node);
    }

    @Override
    public void visit(Node node) {

    }

    void visitChildren(SuperNode node) {
        for (Node child : node.getChildren()) {
            child.accept(this);
        }
    }
}
