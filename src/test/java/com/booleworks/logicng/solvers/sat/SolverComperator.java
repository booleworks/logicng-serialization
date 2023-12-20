// SPDX-License-Identifier: Apache-2.0 and MIT
// Copyright 2023-20xx BooleWorks GmbH

package com.booleworks.logicng.solvers.sat;

import static com.booleworks.logicng.collections.CollectionComperator.assertVecEquals;
import static org.assertj.core.api.Assertions.assertThat;

import com.booleworks.logicng.solvers.datastructures.SolverDatastructureComparator;
import com.booleworks.logicng.solvers.MiniSat;

public class SolverComperator {

    public static void compareSolverStates(final MiniSat solver1, final MiniSat solver2) {
        final MiniSatStyleSolver s1 = solver1.underlyingSolver();
        final MiniSatStyleSolver s2 = solver2.underlyingSolver();

        assertThat(s1.ok).isEqualTo(s2.ok);
        assertThat(s1.qhead).isEqualTo(s2.qhead);
        SolverDatastructureComparator.assertClausesEquals(s1.clauses, s2.clauses);
        SolverDatastructureComparator.assertClausesEquals(s1.learnts, s2.learnts);
        SolverDatastructureComparator.assertWatchListsEquals(s1.watches, s2.watches);
        SolverDatastructureComparator.assertVariablesEquals(s1.vars, s2.vars);
        SolverDatastructureComparator.assertHeapEquals(s1.orderHeap, s2.orderHeap);
        assertVecEquals(s1.trail, s2.trail);
        assertVecEquals(s1.trailLim, s2.trailLim);
        assertVecEquals(s1.model, s2.model);
        assertVecEquals(s1.conflict, s2.conflict);
        assertVecEquals(s1.assumptions, s2.assumptions);
        assertVecEquals(s1.seen, s2.seen);
        assertThat(s1.analyzeBtLevel).isEqualTo(s2.analyzeBtLevel);
        assertThat(s1.claInc).isEqualTo(s2.claInc);
        assertThat(s1.simpDBAssigns).isEqualTo(s2.simpDBAssigns);
        assertThat(s1.simpDBProps).isEqualTo(s2.simpDBProps);
        assertThat(s1.clausesLiterals).isEqualTo(s2.clausesLiterals);
        assertThat(s1.learntsLiterals).isEqualTo(s2.learntsLiterals);

        assertThat(s1.varDecay).isEqualTo(s2.varDecay);
        assertThat(s1.varInc).isEqualTo(s2.varInc);
        assertThat(s1.ccminMode).isEqualTo(s2.ccminMode);
        assertThat(s1.restartFirst).isEqualTo(s2.restartFirst);
        assertThat(s1.restartInc).isEqualTo(s2.restartInc);
        assertThat(s1.clauseDecay).isEqualTo(s2.clauseDecay);
        assertThat(s1.shouldRemoveSatsisfied).isEqualTo(s2.shouldRemoveSatsisfied);
        assertThat(s1.learntsizeFactor).isEqualTo(s2.learntsizeFactor);
        assertThat(s1.learntsizeInc).isEqualTo(s2.learntsizeInc);
        assertThat(s1.incremental).isEqualTo(s2.incremental);

        assertThat(s1.name2idx).isEqualTo(s2.name2idx);
        assertThat(s1.idx2name).isEqualTo(s2.idx2name);

        assertThat(s1.backboneCandidates).isEqualTo(s2.backboneCandidates);
        assertVecEquals(s1.backboneAssumptions, s2.backboneAssumptions);
        assertThat(s1.backboneMap).isEqualTo(s2.backboneMap);
        assertThat(s1.computingBackbone).isEqualTo(s2.computingBackbone);

        assertVecEquals(s1.selectionOrder, s2.selectionOrder);
        assertThat(s1.selectionOrderIdx).isEqualTo(s2.selectionOrderIdx);

        assertThat(s1.learntsizeAdjustConfl).isEqualTo(s2.learntsizeAdjustConfl);
        assertThat(s1.learntsizeAdjustCnt).isEqualTo(s2.learntsizeAdjustCnt);
        assertThat(s1.learntsizeAdjustStartConfl).isEqualTo(s2.learntsizeAdjustStartConfl);
        assertThat(s1.learntsizeAdjustInc).isEqualTo(s2.learntsizeAdjustInc);
        assertThat(s1.maxLearnts).isEqualTo(s2.maxLearnts);
    }
}
