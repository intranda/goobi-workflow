package org.goobi.production.properties.decisiontree.parser;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. - https://goobi.io - https://www.intranda.com - https://github.com/intranda/goobi-workflow
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.goobi.production.properties.decisiontree.engine.TreeEngine;
import org.goobi.production.properties.decisiontree.model.Answer;
import org.goobi.production.properties.decisiontree.model.GotoRef;
import org.goobi.production.properties.decisiontree.model.Node;
import org.goobi.production.properties.decisiontree.model.Question;
import org.goobi.production.properties.decisiontree.model.Result;
import org.goobi.production.properties.decisiontree.model.Text;
import org.goobi.production.properties.decisiontree.model.TreeDefinition;
import org.goobi.production.properties.decisiontree.parser.LoadReport.Level;
import org.goobi.production.properties.decisiontree.parser.LoadReport.LoadMessage;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

/**
 * Reads the trees file ({@code goobi_decisionTrees.xml}) with all trees and checks every tree on its own.
 */
public class TreeFileParser {

    private static final String LANGUAGE_PREFIX = "-";

    /** Everything collected while one tree is parsed. */
    private static final class Context {
        private final String treeId;
        private final List<LoadMessage> messages;
        private final Map<String, Question> questions = new LinkedHashMap<>();
        private final Map<String, Result> results = new LinkedHashMap<>();
        private final Set<String> owned = new LinkedHashSet<>();
        private final List<Question> allQuestions = new ArrayList<>();
        private final List<GotoRef> gotos = new ArrayList<>();
        private final List<String> gotoPositions = new ArrayList<>();
        private boolean failed;

        Context(String treeId, List<LoadMessage> messages) {
            this.treeId = treeId;
            this.messages = messages;
        }

        void error(String position, String text) {
            failed = true;
            messages.add(new LoadMessage(Level.ERROR, treeId, position, text));
        }

        void warn(String position, String text) {
            messages.add(new LoadMessage(Level.WARN, treeId, position, text));
        }
    }

    /**
     * Parses a trees file.
     *
     * @param in the trees file
     * @return the loaded trees and the messages about them
     * @throws TreeLoadException if the file is not well-formed or has the wrong root element
     */
    public LoadReport parse(InputStream in) throws TreeLoadException {
        Document doc;
        try {
            SAXBuilder builder = new SAXBuilder();
            builder.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            doc = builder.build(in);
        } catch (JDOMException | IOException e) {
            throw new TreeLoadException("The trees file cannot be read: " + e.getMessage(), e);
        }
        Element root = doc.getRootElement();
        if (!"decisionTrees".equals(root.getName())) {
            throw new TreeLoadException("The root element must be <decisionTrees>, found <" + root.getName() + ">");
        }
        List<LoadMessage> messages = new ArrayList<>();
        Map<String, TreeDefinition> trees = new LinkedHashMap<>();
        Set<String> seen = new LinkedHashSet<>();
        Set<String> duplicates = new LinkedHashSet<>();
        for (Element element : root.getChildren()) {
            if (!"decisionTree".equals(element.getName())) {
                messages.add(new LoadMessage(Level.ERROR, null, "decisionTrees", "unknown element <" + element.getName() + ">"));
                continue;
            }
            String id = element.getAttributeValue("id");
            if (id == null || id.isBlank()) {
                messages.add(new LoadMessage(Level.ERROR, null, "decisionTree", "the tree has no id"));
                continue;
            }
            if (!seen.add(id)) {
                duplicates.add(id);
                messages.add(new LoadMessage(Level.ERROR, id, "", "the id is used by more than one tree; all trees with this id are dropped"));
                continue;
            }
            TreeDefinition tree = parseTree(id, element, messages);
            if (tree != null) {
                trees.put(id, tree);
            }
        }
        duplicates.forEach(trees::remove);
        return new LoadReport(trees, messages);
    }

    private TreeDefinition parseTree(String id, Element element, List<LoadMessage> messages) {
        Context ctx = new Context(id, messages);
        String position = "decisionTree[" + id + "]";
        Map<String, String> defaults = new LinkedHashMap<>();
        Question root = null;
        int roots = 0;
        for (Element child : element.getChildren()) {
            switch (child.getName()) {
                case "defaults" -> defaults.putAll(parseFields(child, position + "/defaults", ctx));
                case "question" -> {
                    roots++;
                    if (root == null) {
                        root = parseQuestion(child, position, ctx);
                    }
                }
                default -> ctx.error(position, "unknown element <" + child.getName() + ">");
            }
        }
        if (roots != 1) {
            ctx.error(position, "a tree needs exactly one top-level <question>, found " + roots);
        }
        if (root == null || ctx.failed) {
            return null;
        }
        checkGotos(ctx);
        if (ctx.failed) {
            return null;
        }
        if (hasCycle(root, ctx)) {
            ctx.error(position, "the tree has a cycle (goto leads back to a question above it)");
            return null;
        }
        TreeDefinition tree = new TreeDefinition(id, defaults, root, ctx.questions, ctx.results, ctx.owned);
        checkPaths(tree, ctx);
        return ctx.failed ? null : tree;
    }

    private Question parseQuestion(Element element, String parentPosition, Context ctx) {
        String id = blankToNull(element.getAttributeValue("id"));
        String position = parentPosition + "/question" + (id == null ? "" : "[" + id + "]");
        checkAttributes(element, position, ctx, "id", "label", "hint", "type");
        String type = element.getAttributeValue("type", Question.TYPE_CHOICE);
        if (!Question.TYPE_CHOICE.equals(type)) {
            ctx.error(position, "unknown type '" + type + "', only '" + Question.TYPE_CHOICE + "' is supported");
        }
        Text label = text(element, "label", position, ctx);
        if (label.isEmpty()) {
            ctx.error(position, "the question has no label");
        }
        Text hint = text(element, "hint", position, ctx);
        List<Answer> answers = new ArrayList<>();
        Set<Text> labels = new LinkedHashSet<>();
        for (Element child : element.getChildren()) {
            if (!"answer".equals(child.getName())) {
                ctx.error(position, "unknown element <" + child.getName() + ">");
                continue;
            }
            Answer answer = parseAnswer(child, position, ctx);
            if (answer != null) {
                if (!labels.add(answer.label())) {
                    ctx.error(position, "the answer '" + answer.label().get(null) + "' exists more than once");
                }
                answers.add(answer);
            }
        }
        if (element.getChildren("answer").size() < 2) {
            ctx.error(position, "a question needs at least two answers");
        }
        Question question = new Question(id, label, hint, type, answers);
        ctx.allQuestions.add(question);
        if (id != null && ctx.questions.put(id, question) != null) {
            ctx.error(position, "the question id '" + id + "' is used more than once");
        }
        return question;
    }

    private Answer parseAnswer(Element element, String parentPosition, Context ctx) {
        Text label = text(element, "label", parentPosition, ctx);
        String position = parentPosition + "/answer[" + (label.isEmpty() ? "?" : label.get(null)) + "]";
        checkAttributes(element, position, ctx, "label");
        if (label.isEmpty()) {
            ctx.error(position, "the answer has no label");
        }
        Map<String, String> fields = new LinkedHashMap<>();
        Node next = null;
        int nexts = 0;
        for (Element child : element.getChildren()) {
            switch (child.getName()) {
                case "field" -> addField(child, fields, position, ctx);
                case "question" -> {
                    nexts++;
                    Question question = parseQuestion(child, position, ctx);
                    next = next == null ? question : next;
                }
                case "result" -> {
                    nexts++;
                    Result result = parseResult(child, position, ctx);
                    next = next == null ? result : next;
                }
                case "goto" -> {
                    nexts++;
                    String ref = blankToNull(child.getAttributeValue("ref"));
                    if (ref == null) {
                        ctx.error(position, "<goto> needs a ref");
                    } else {
                        GotoRef target = new GotoRef(ref);
                        ctx.gotos.add(target);
                        ctx.gotoPositions.add(position);
                        next = next == null ? target : next;
                    }
                }
                default -> ctx.error(position, "unknown element <" + child.getName() + ">");
            }
        }
        if (nexts != 1) {
            ctx.error(position, "an answer needs exactly one of <question>, <result> and <goto>, found " + nexts);
        }
        return next == null ? null : new Answer(label, fields, next);
    }

    private Result parseResult(Element element, String parentPosition, Context ctx) {
        String id = blankToNull(element.getAttributeValue("id"));
        String position = parentPosition + "/result[" + (id == null ? "?" : id) + "]";
        checkAttributes(element, position, ctx, "id");
        if (id == null) {
            ctx.error(position, "the result has no id");
        }
        if (!element.getChildren().isEmpty()) {
            ctx.error(position, "<result> must be empty, set <field> on the answer");
        }
        Result result = new Result(id);
        if (id != null && ctx.results.put(id, result) != null) {
            ctx.error(position, "the result id '" + id + "' is used more than once");
        }
        return result;
    }

    private Map<String, String> parseFields(Element parent, String position, Context ctx) {
        Map<String, String> fields = new LinkedHashMap<>();
        for (Element child : parent.getChildren()) {
            if ("field".equals(child.getName())) {
                addField(child, fields, position, ctx);
            } else {
                ctx.error(position, "unknown element <" + child.getName() + ">");
            }
        }
        return fields;
    }

    private void addField(Element element, Map<String, String> fields, String position, Context ctx) {
        String name = blankToNull(element.getAttributeValue("name"));
        if (name == null) {
            ctx.error(position, "<field> needs a name");
            return;
        }
        if (name.contains(")") || name.contains("}")) {
            ctx.error(position, "the field name '" + name + "' must not contain ')' or '}' (it could not be resolved in {process.<name>})");
            return;
        }
        ctx.owned.add(name);
        fields.put(name, element.getTextTrim());
    }

    /** Reads {@code <prefix>} and {@code <prefix>-<lang>} attributes. */
    private Text text(Element element, String prefix, String position, Context ctx) {
        String base = blankToNull(element.getAttributeValue(prefix));
        Map<String, String> byLanguage = new LinkedHashMap<>();
        for (Attribute attribute : element.getAttributes()) {
            if (attribute.getName().startsWith(prefix + LANGUAGE_PREFIX)) {
                byLanguage.put(attribute.getName().substring(prefix.length() + 1), attribute.getValue());
            }
        }
        if (base == null && !byLanguage.isEmpty()) {
            ctx.warn(position, prefix + "-<lang> without " + prefix);
        }
        return new Text(base, byLanguage);
    }

    private void checkAttributes(Element element, String position, Context ctx, String... allowed) {
        for (Attribute attribute : element.getAttributes()) {
            String name = attribute.getName();
            if ("when".equals(name)) {
                ctx.error(position, "'when' is not supported in this version");
                continue;
            }
            boolean known = false;
            for (String a : allowed) {
                known |= a.equals(name) || name.startsWith(a + LANGUAGE_PREFIX) && ("label".equals(a) || "hint".equals(a));
            }
            if (!known) {
                ctx.error(position, "unknown attribute '" + name + "'");
            }
        }
    }

    private void checkGotos(Context ctx) {
        for (int i = 0; i < ctx.gotos.size(); i++) {
            String ref = ctx.gotos.get(i).ref();
            if (!ctx.questions.containsKey(ref)) {
                ctx.error(ctx.gotoPositions.get(i), "goto points to the unknown question '" + ref + "'");
            }
        }
    }

    private boolean hasCycle(Question root, Context ctx) {
        return cycle(root, ctx, new IdentityHashMap<>());
    }

    /** Depth-first over the questions; the value is false while the question is on the current way and true once it is done. */
    private boolean cycle(Question question, Context ctx, IdentityHashMap<Question, Boolean> state) {
        Boolean done = state.get(question);
        if (done != null) {
            return !done;
        }
        state.put(question, false);
        for (Answer answer : question.answers()) {
            Question next = answer.next() instanceof Question q ? q : answer.next() instanceof GotoRef g ? ctx.questions.get(g.ref()) : null;
            if (next != null && cycle(next, ctx, state)) {
                return true;
            }
        }
        state.put(question, true);
        return false;
    }

    private void checkPaths(TreeDefinition tree, Context ctx) {
        String position = "decisionTree[" + tree.id() + "]";
        if (countPaths(tree, tree.root(), new IdentityHashMap<>()) > TreeDefinition.MAX_PATHS) {
            ctx.error(position, "the tree has more than " + TreeDefinition.MAX_PATHS + " paths from the top question to a result");
            return;
        }
        Set<String> reported = new LinkedHashSet<>();
        for (TreeEngine.LeafPath leaf : new TreeEngine().allPaths(tree)) {
            if (leaf.fields().isEmpty() && reported.add(leaf.result().id())) {
                ctx.error(position + "/result[" + leaf.result().id() + "]",
                        "the result has no fields on some path (after merging defaults and answers)");
            }
        }
    }

    private long countPaths(TreeDefinition tree, Node node, IdentityHashMap<Question, Long> memo) {
        if (node instanceof Result) {
            return 1;
        }
        Question question = tree.asQuestion(node);
        Long known = memo.get(question);
        if (known != null) {
            return known;
        }
        long sum = 0;
        for (Answer answer : question.answers()) {
            sum += countPaths(tree, answer.next(), memo);
            if (sum > TreeDefinition.MAX_PATHS) {
                sum = TreeDefinition.MAX_PATHS + 1L;
                break;
            }
        }
        memo.put(question, sum);
        return sum;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
