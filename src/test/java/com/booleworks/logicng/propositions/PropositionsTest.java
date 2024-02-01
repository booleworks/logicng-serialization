// SPDX-License-Identifier: Apache-2.0 and MIT
// Copyright 2023-20xx BooleWorks GmbH

package com.booleworks.logicng.propositions;

import static com.booleworks.logicng.propositions.Propositions.deserialize;
import static com.booleworks.logicng.propositions.Propositions.serialize;
import static org.assertj.core.api.Assertions.assertThat;

import com.booleworks.logicng.formulas.FormulaFactory;
import com.booleworks.logicng.io.parsers.ParserException;
import org.junit.jupiter.api.Test;

public class PropositionsTest {

    final FormulaFactory f = FormulaFactory.nonCaching();

    @Test
    public void testStandardProposition() throws ParserException {
        final var p = new StandardProposition("description", f.parse("a & (b => c + d = 1) <=> ~x"));
        assertThat(deserialize(f, serialize(p))).isEqualTo(p);
    }

}
