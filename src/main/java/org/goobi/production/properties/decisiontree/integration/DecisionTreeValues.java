package org.goobi.production.properties.decisiontree.integration;

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

import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.goobi.beans.GoobiProperty;
import org.goobi.production.properties.decisiontree.binding.BindingConfig;
import org.goobi.production.properties.decisiontree.binding.TreeRegistry;
import org.goobi.production.properties.decisiontree.engine.TreeEngine;
import org.goobi.production.properties.decisiontree.model.TreeDefinition;
import org.goobi.production.properties.decisiontree.view.ViewState;
import org.goobi.production.properties.decisiontree.view.ViewState.State;
import org.goobi.production.properties.decisiontree.writer.PropertiesResultWriter;

import de.sub.goobi.config.ConfigurationHelper;

/**
 * Derives the value of a decision tree property from the result fields that are stored as properties. The tree itself stores nothing, so this value
 * is what the finish check of a task looks at.
 */
public class DecisionTreeValues {

    public static final String TREES_FILE = "goobi_decisionTrees.xml";

    private static DecisionTreeValues instance;

    private final TreeRegistry registry;
    private final TreeEngine engine = new TreeEngine();

    public DecisionTreeValues(TreeRegistry registry) {
        this.registry = registry;
    }

    /** @return the instance for the trees file in the configuration folder */
    public static synchronized DecisionTreeValues getInstance() {
        if (instance == null) {
            instance = new DecisionTreeValues(new TreeRegistry(Path.of(ConfigurationHelper.getInstance().getConfigurationFolder(), TREES_FILE)));
        }
        return instance;
    }

    public TreeRegistry getRegistry() {
        return registry;
    }

    /**
     * The value of the property: the id of the result the stored fields fit; the ids of all results that fit, separated by a comma, if they fit
     * several; empty if nothing is stored, the stored fields fit no result, or the tree is not available.
     *
     * @param binding the binding of the property
     * @param properties the properties of the process
     * @return the value, never null
     */
    public String derive(BindingConfig binding, List<GoobiProperty> properties) {
        Optional<TreeDefinition> tree = registry.tree(binding.treeId());
        if (tree.isEmpty()) {
            return "";
        }
        Map<String, String> stored = new PropertiesResultWriter(new ListPropertyStore(properties), binding.fieldMap()).read(tree.get().ownedFields());
        ViewState state = engine.restore(tree.get(), stored, Locale.ROOT);
        if (state.state() != State.CONFIRMED) {
            return "";
        }
        if (state.ambiguous() != null && state.ambiguous().candidates().size() > 1) {
            return String.join(", ", state.ambiguous().candidates());
        }
        return state.result().id() == null ? "" : state.result().id();
    }
}
