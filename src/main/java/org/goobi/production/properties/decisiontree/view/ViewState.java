package org.goobi.production.properties.decisiontree.view;

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

import java.util.List;
import java.util.Map;

/**
 * What a renderer sees. Plain records without cycles, so a later REST endpoint can return the same objects as JSON.
 *
 * @param state state of the component
 * @param path the answered questions from the top, empty if the way cannot be shown
 * @param open the open question, only in state ANSWERING
 * @param result the result, once a leaf is reached or stored fields are shown
 * @param ambiguous set if several results or several ways fit the stored fields
 * @param messages hints for the user, as message keys
 */
public record ViewState(State state, List<PathStep> path, OpenQuestion open, ResultView result, Ambiguous ambiguous,
        List<Message> messages) {

    public enum State {
        /** An open question; no leaf is reached and nothing is stored. */
        ANSWERING,
        /** A leaf is reached but not yet confirmed; nothing is written. */
        RESULT,
        /** The fields are written and fit a result. */
        CONFIRMED,
        /** Fields are stored but no result of the tree fits (tree changed or a field edited by hand). */
        MISMATCH,
        /** The same view without actions. */
        READ_ONLY
    }

    public ViewState {
        path = List.copyOf(path);
        messages = List.copyOf(messages);
    }

    /** @return the same view in state READ_ONLY */
    public ViewState asReadOnly() {
        return new ViewState(State.READ_ONLY, path, open, result, ambiguous, messages);
    }

    /**
     * An answered question.
     *
     * @param stepIndex position in the path
     * @param questionId id of the question, may be null
     * @param label label of the question
     * @param hint hint of the question, empty if there is none
     * @param answerIndex index of the chosen answer
     * @param answerLabel label of the chosen answer
     * @param answers all answers of the question, the chosen one included, so a renderer can show the alternatives
     * @param decided fields the answer sets
     */
    public record PathStep(int stepIndex, String questionId, String label, String hint, int answerIndex, String answerLabel,
            List<AnswerView> answers, Map<String, String> decided) {
        public PathStep {
            answers = List.copyOf(answers);
        }
    }

    /**
     * The open question.
     *
     * @param stepIndex position the answer will take in the path
     * @param id id of the question, may be null
     * @param label label of the question
     * @param hint hint of the question, empty if there is none
     * @param answers the possible answers
     */
    public record OpenQuestion(int stepIndex, String id, String label, String hint, List<AnswerView> answers) {
    }

    /**
     * @param index index of the answer in its question
     * @param label label of the answer
     * @param leaf whether the answer leads straight to a result, so a renderer can mark where a way ends
     */
    public record AnswerView(int index, String label, boolean leaf) {
    }

    /**
     * A result.
     *
     * @param id id of the result, null if several results fit or none does
     * @param fields the fields, in the order of the tree file
     */
    public record ResultView(String id, Map<String, String> fields) {
    }

    /** @param candidates ids of the results that fit */
    public record Ambiguous(List<String> candidates) {
    }

    /**
     * @param level INFO or WARN
     * @param key message key for the translation
     */
    public record Message(String level, String key) {
    }
}
