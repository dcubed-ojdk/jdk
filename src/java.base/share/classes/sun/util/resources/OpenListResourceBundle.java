/*
 * Copyright (c) 2005, 2025, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

/*
 * (C) Copyright Taligent, Inc. 1996, 1997 - All Rights Reserved
 * (C) Copyright IBM Corp. 1996 - 1998 - All Rights Reserved
 *
 * The original version of this source code and documentation
 * is copyrighted and owned by Taligent, Inc., a wholly-owned
 * subsidiary of IBM. These materials are provided under terms
 * of a License Agreement between Taligent and Sun. This technology
 * is protected by multiple US and International patents.
 *
 * This notice and attribution to Taligent may not be removed.
 * Taligent is a registered trademark of Taligent, Inc.
 *
 */

package sun.util.resources;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.function.Supplier;

import sun.util.ResourceBundleEnumeration;

/**
 * Subclass of <code>ResourceBundle</code> which mimics
 * <code>ListResourceBundle</code>, but provides more hooks
 * for specialized subclass behavior. For general description,
 * see {@link java.util.ListResourceBundle}.
 * <p>
 * This class leaves handleGetObject non-final, and
 * adds a method createMap which allows subclasses to
 * use specialized Map implementations.
 */
public abstract class OpenListResourceBundle extends ResourceBundle {
    /**
     * Sole constructor.  (For invocation by subclass constructors, typically
     * implicit.)
     */
    protected OpenListResourceBundle() {
    }

    // Implements java.util.ResourceBundle.handleGetObject; inherits javadoc specification.
    @Override
    protected Object handleGetObject(String key) {
        Objects.requireNonNull(key);
        return lookup.get()
                .get(key); // this class ignores locales
    }

    /**
     * Implementation of ResourceBundle.getKeys.
     */
    @Override
    public Enumeration<String> getKeys() {
        ResourceBundle parentBundle = this.parent;
        return new ResourceBundleEnumeration(handleKeySet(),
                (parentBundle != null) ? parentBundle.getKeys() : null);
     }

    /**
     * Returns a set of keys provided in this resource bundle,
     * including no parents.
     */
    @Override
    protected Set<String> handleKeySet() {
        return lookup.get()
                .keySet();
    }

    @Override
    public Set<String> keySet() {
        return keyset.get();
    }

    /**
     * See ListResourceBundle class description.
     */
    protected abstract Object[][] getContents();

    /**
     * Lets subclasses provide specialized Map implementations.
     * Default uses HashMap.
     */
    protected <K, V> Map<K, V> createMap(int size) {
        return HashMap.newHashMap(size);
    }

    protected <E> Set<E> createSet() {
        return new HashSet<>();
    }

    private final Supplier<Map<String, Object>> lookup = StableValue.supplier(
            new Supplier<>() { public Map<String, Object> get() { return lookup0(); }});

    private Map<String, Object> lookup0() {
        final Object[][] contents = getContents();
        final Map<String, Object> temp = createMap(contents.length);
        for (Object[] content : contents) {
            // key must be non-null String, value must be non-null
            final String key = Objects.requireNonNull((String) content[0]);
            final Object value = Objects.requireNonNull(content[1]);
            temp.put(key, value);
        }
        return temp;
    }

    private final Supplier<Set<String>> keyset = StableValue.supplier(
            new Supplier<>() { public Set<String> get() { return keyset0(); }});

    private Set<String> keyset0() {
        final Set<String> ks = createSet();
        ks.addAll(handleKeySet());
        if (parent != null) {
            ks.addAll(parent.keySet());
        }
        return ks;
    }

}
