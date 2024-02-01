// SPDX-License-Identifier: Apache-2.0 and MIT
// Copyright 2023-20xx BooleWorks GmbH

package com.booleworks.logicng.propositions;

import com.booleworks.logicng.formulas.FormulaFactory;
import com.booleworks.logicng.formulas.Formulas;
import com.booleworks.logicng.propositions.ProtoBufPropositions.PBStandardProposition;

public interface Propositions {

    static PBStandardProposition serialize(final StandardProposition p) {
        final PBStandardProposition.Builder builder = PBStandardProposition.newBuilder();
        builder.setFormula(Formulas.serialize(p.formula()));
        return builder.setDescription(p.description()).build();
    }

    static StandardProposition deserialize(final FormulaFactory f, final PBStandardProposition bin) {
        return new StandardProposition(bin.getDescription(), Formulas.deserialize(f, bin.getFormula()));
    }
}
