// SPDX-License-Identifier: Apache-2.0 and MIT
// Copyright 2023-20xx BooleWorks GmbH

package com.booleworks.logicng.propositions;

import com.booleworks.logicng.formulas.FormulaFactory;
import com.booleworks.logicng.formulas.Formulas;
import com.booleworks.logicng.propositions.ProtoBufPropositions.PBStandardProposition;

/**
 * Serialization methods for LogicNG propositions.
 * There are only functions for serializing and deserializing standard propositions.
 * If you want to serialize your own extended propositions, you have to provide the
 * according functions yourself.
 * @version 3.0.0
 * @since 2.5.0
 */
public interface Propositions {

    /**
     * Serializes a standard proposition to a protocol buffer.
     * @param prop the proposition
     * @return the protocol buffer
     */
    static PBStandardProposition serialize(final StandardProposition prop) {
        final PBStandardProposition.Builder builder = PBStandardProposition.newBuilder();
        builder.setFormula(Formulas.serialize(prop.formula()));
        return builder.setDescription(prop.description()).build();
    }

    /**
     * Deserializes a standard proposition from a protocol buffer.
     * @param f   the formula factory to generate the proposition's formula
     * @param bin the protocol buffer
     * @return the proposition
     */
    static StandardProposition deserialize(final FormulaFactory f, final PBStandardProposition bin) {
        return new StandardProposition(bin.getDescription(), Formulas.deserialize(f, bin.getFormula()));
    }
}
