// SPDX-License-Identifier: Apache-2.0 and MIT
// Copyright 2023-20xx BooleWorks GmbH

package com.booleworks.logicng.solvers.datastructures;

import static com.booleworks.logicng.collections.CollectionComperator.assertVecEquals;
import static org.assertj.core.api.Assertions.assertThat;

import com.booleworks.logicng.collections.LNGVector;

public class SolverDatastructureComparator {

    public static void assertClausesEquals(final LNGVector<MSClause> c1, final LNGVector<MSClause> c2) {
        assertThat(c1.size()).isEqualTo(c2.size());
        for (int i = 0; i < c1.size(); i++) {
            assertClauseEquals(c1.get(i), c2.get(i));
        }
    }

    public static void assertClauseEquals(final MSClause c1, final MSClause c2) {
        if (c1 == null && c2 == null) {
            return;
        }
        assertVecEquals(c1.getData(), c2.getData());
        assertThat(c1.learnt()).isEqualTo(c2.learnt());
        assertThat(c1.isAtMost()).isEqualTo(c2.isAtMost());
        assertThat(c1.activity()).isEqualTo(c2.activity());
        assertThat(c1.sizeWithoutSelectors()).isEqualTo(c2.sizeWithoutSelectors());
        assertThat(c1.seen()).isEqualTo(c2.seen());
        assertThat(c1.lbd()).isEqualTo(c2.lbd());
        assertThat(c1.canBeDel()).isEqualTo(c2.canBeDel());
        assertThat(c1.oneWatched()).isEqualTo(c2.oneWatched());
        if (c1.isAtMost()) {
            assertThat(c1.atMostWatchers()).isEqualTo(c2.atMostWatchers());
        }
    }

    public static void assertVariablesEquals(final LNGVector<MSVariable> v1, final LNGVector<MSVariable> v2) {
        assertThat(v1.size()).isEqualTo(v2.size());
        for (int i = 0; i < v1.size(); i++) {
            assertVariableEquals(v1.get(i), v2.get(i));
        }
    }

    public static void assertVariableEquals(final MSVariable v1, final MSVariable v2) {
        assertThat(v1.assignment()).isEqualTo(v2.assignment());
        assertThat(v1.level()).isEqualTo(v2.level());
        assertClauseEquals(v1.reason(), v2.reason());
        assertThat(v1.assignment()).isEqualTo(v2.assignment());
        assertThat(v1.activity()).isEqualTo(v2.activity());
        assertThat(v1.polarity()).isEqualTo(v2.polarity());
        assertThat(v1.decision()).isEqualTo(v2.decision());
    }

    public static void assertHeapEquals(final LNGHeap heap1, final LNGHeap heap2) {
        assertVecEquals(heap1.getHeap(), heap2.getHeap());
        assertVecEquals(heap1.getIndices(), heap2.getIndices());
    }

    public static void assertWatchListsEquals(final LNGVector<LNGVector<MSWatcher>> w1, final LNGVector<LNGVector<MSWatcher>> w2) {
        assertThat(w1.size()).isEqualTo(w2.size());
        for (int i = 0; i < w1.size(); i++) {
            assertWatchesEquals(w1.get(i), w2.get(i));
        }
    }

    public static void assertWatchesEquals(final LNGVector<MSWatcher> w1, final LNGVector<MSWatcher> w2) {
        assertThat(w1.size()).isEqualTo(w2.size());
        for (int i = 0; i < w1.size(); i++) {
            assertWatchEquals(w1.get(i), w2.get(i));
        }
    }

    public static void assertWatchEquals(final MSWatcher w1, final MSWatcher w2) {
        assertClauseEquals(w1.clause(), w2.clause());
        assertThat(w1.blocker()).isEqualTo(w2.blocker());
    }
}
