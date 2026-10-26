package org.goobi.production.properties.decisiontree.model;

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

import java.util.Map;
import java.util.Set;

/**
 * One parsed decision tree. Immutable; parsed once per file change.
 *
 * <p>
 * Records compare their components structurally, so never use a {@link Node} as a key of a hash map; use {@link #questionsById()} or an
 * {@link java.util.IdentityHashMap}.
 * </p>
 *
 * @param id id of the tree in the trees file
 * @param defaults base fields that apply to every result
 * @param root first question
 * @param questionsById all questions that have an id (goto targets)
 * @param resultsById all results
 * @param ownedFields all field names used anywhere in the tree (logical names, before any mapping)
 */
public record TreeDefinition(String id, Map<String, String> defaults, Question root, Map<String, Question> questionsById,
        Map<String, Result> resultsById, Set<String> ownedFields) {

    /** Upper limit of root-to-leaf paths a tree may have, see the load checks and restoring from stored fields. */
    public static final int MAX_PATHS = 10_000;

    public TreeDefinition {
        defaults = Ordered.copyOf(defaults);
        questionsById = Ordered.copyOf(questionsById);
        resultsById = Ordered.copyOf(resultsById);
        ownedFields = Set.copyOf(ownedFields);
    }

    /** @return the question a node stands for (following a goto), or null if the node is a result */
    public Question asQuestion(Node node) {
        if (node instanceof Question q) {
            return q;
        }
        if (node instanceof GotoRef g) {
            return questionsById.get(g.ref());
        }
        return null;
    }
}
