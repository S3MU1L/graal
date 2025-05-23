/*
 * Copyright (c) 2012, 2025, Oracle and/or its affiliates. All rights reserved.
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
package jdk.graal.compiler.replacements.gc;

import static jdk.graal.compiler.nodes.extended.BranchProbabilityNode.LIKELY_PROBABILITY;
import static jdk.graal.compiler.nodes.extended.BranchProbabilityNode.NOT_LIKELY_PROBABILITY;
import static jdk.graal.compiler.nodes.extended.BranchProbabilityNode.probability;

import org.graalvm.word.LocationIdentity;

import jdk.graal.compiler.debug.GraalError;
import jdk.graal.compiler.nodes.FieldLocationIdentity;
import jdk.graal.compiler.nodes.NamedLocationIdentity;
import jdk.graal.compiler.nodes.PiNode;
import jdk.graal.compiler.nodes.SnippetAnchorNode;
import jdk.graal.compiler.replacements.nodes.AssertionNode;
import jdk.graal.compiler.word.Word;
import jdk.vm.ci.meta.MetaAccessProvider;
import jdk.vm.ci.meta.ResolvedJavaField;

public abstract class WriteBarrierSnippets {
    public static final LocationIdentity GC_CARD_LOCATION = NamedLocationIdentity.mutable("GC-Card");

    public static FieldLocationIdentity getClassComponentTypeLocation(MetaAccessProvider metaAccessProvider) {
        ResolvedJavaField componentTypeField;

        try {
            componentTypeField = metaAccessProvider.lookupJavaField(Class.class.getDeclaredField("componentType"));
        } catch (NoSuchFieldException e) {
            throw GraalError.shouldNotReachHere("Class.componentType is not present");
        }

        return new FieldLocationIdentity(componentTypeField);
    }

    protected static void verifyNotArray(Object object) {
        if (probability(LIKELY_PROBABILITY, object != null)) {
            // Manually build the null check and cast because we're in snippet that's lowered late.
            AssertionNode.dynamicAssert(!PiNode.piCastNonNull(object, SnippetAnchorNode.anchor()).getClass().isArray(), "imprecise card mark used with array");
        }
    }

    public static Word getPointerToFirstArrayElement(Word address, long length, int elementStride) {
        long result = address.rawValue();
        if (probability(NOT_LIKELY_PROBABILITY, elementStride < 0)) {
            // the address points to the place after the last array element
            result = result + elementStride * length;
        }
        return Word.unsigned(result);
    }

    public static Word getPointerToLastArrayElement(Word address, long length, int elementStride) {
        long result = address.rawValue();
        if (probability(NOT_LIKELY_PROBABILITY, elementStride < 0)) {
            // the address points to the place after the last array element
            result = result + elementStride;
        } else {
            result = result + (length - 1) * elementStride;
        }
        return Word.unsigned(result);
    }
}
