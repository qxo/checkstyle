////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code for adherence to a set of rules.
// Copyright (C) 2001-2020 the original author or authors.
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
////////////////////////////////////////////////////////////////////////////////

package com.puppycrawl.tools.checkstyle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.puppycrawl.tools.checkstyle.api.Configuration;

/**
 * InheritConfiguration for inherit the config with parent attribute on root module.
 *
 * @noinspection SerializableHasSerializationMethods
 */
public final class InheritConfiguration extends DefaultConfiguration {

    private static final long serialVersionUID = 2331785991766158817L;

    /**
     * The parent configuration.
     */
    private final Configuration parentConfig;

    /**
     * Instantiates a configuration with parent.
     *
     * @param parent The parent configuration.
     * @param name the name for this InheritConfiguration.
     * @param threadModeSettings the thread mode configuration.
     */
    public InheritConfiguration(final Configuration parent, final String name,
        final ThreadModeSettings threadModeSettings) {
        super(name, threadModeSettings);
        parentConfig = parent;
    }

    //    /**
    //     * Instantiates a configuration with parent.
    //     *
    //     * @param parent The parent configuration.
    //     * @param name the name for this InheritConfiguration.
    //     */
    //    public InheritConfiguration(final Configuration parent, final String name) {
    //        super(name);
    //        parentConfig = parent;
    //    }

    /**
     * Do merge for init.
     *
     * @throws IllegalStateException If doMergeParent error.
     */
    public void doMergeParent() {
        doMergeParent((DefaultConfiguration) parentConfig, this);
    }

    /**
     * Merge parent config to current config.
     *
     * @param parent Parent config.
     * @param current  Current config.
     */
    public static void doMergeParent(final DefaultConfiguration parent,
            final DefaultConfiguration current) {
        mergeAttributes(parent, current);
        mergeMessages(parent, current);
        mergeChildren(parent, current);
    }

    private static void mergeChildren(final DefaultConfiguration parent,
            final DefaultConfiguration current) {
        final Configuration[] children = parent.getChildren();
        final Map<String, Configuration> parents = toChildrenMap(children);

        final Configuration[] currentChildren = current.getChildren();
        final Map<String, Configuration> currents = toChildrenMap(currentChildren);
        final Collection<String> addKeys = new ArrayList<>(parents.keySet());
        addKeys.removeAll(currents.keySet());
        for (final String id : addKeys) {
            current.addChild(parents.get(id));
        }
        final Collection<String> mergeKeys = intersection(parents.keySet(), currents.keySet());
        for (final String id : mergeKeys) {
            final Configuration parentChild = parents.get(id);
            final DefaultConfiguration currentChild = (DefaultConfiguration) currents.get(id);
            doMergeParent((DefaultConfiguration) parentChild, currentChild);
        }
    }

    private static void mergeMessages(final Configuration parent,
            final DefaultConfiguration current) {
        final String[] names = parent.getMessageNames();
        for (final String key : names) {
            if (!current.containsMessage(key)) {
                current.addMessage(key, parent.getMessage(key));
            }
        }
    }

    private static void mergeAttributes(final DefaultConfiguration parent,
            final DefaultConfiguration current) {
        final String[] attributeNames = parent.getAttributeNames();
        for (final String attr : attributeNames) {
            if (!current.containsAttribute(attr)) {
                current.addAttribute(attr, parent.getTheAttribute(attr));
            }
        }
    }

    private static Collection<String> intersection(final Collection<String> list1,
        final Collection<String> list2) {
        final List<String> ret = new ArrayList<>();
        for (final String name : list1) {
            if (list2.contains(name)) {
                ret.add(name);
            }
        }
        return ret;
    }

    private static Map<String, Configuration> toChildrenMap(
            final Configuration... children) {
        final Map<String, Configuration> map = new HashMap<>();
        for (Configuration child : children) {
            String id = ((DefaultConfiguration) child).getTheAttribute("id");
            if (id == null) {
                id = child.getName();
            }
            map.put(id, child);
        }
        return map;
    }
}
