// SPDX-License-Identifier: Apache-2.0 and MIT
// Copyright 2023-20xx BooleWorks GmbH

package com.booleworks.logicng.solvers.datastructures;

import static com.booleworks.logicng.collections.CollectionComperator.assertVecEquals;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import com.booleworks.logicng.collections.LNGIntVector;
import com.booleworks.logicng.collections.LNGLongVector;
import com.booleworks.logicng.datastructures.Tristate;
import com.booleworks.logicng.formulas.FormulaFactory;
import com.booleworks.logicng.solvers.MiniSat;
import com.booleworks.logicng.solvers.datastructures.ProtoBufSolverDatastructures.PBLngBoundedIntQueue;
import com.booleworks.logicng.solvers.datastructures.ProtoBufSolverDatastructures.PBLngBoundedLongQueue;
import com.booleworks.logicng.solvers.datastructures.ProtoBufSolverDatastructures.PBLngHeap;
import com.booleworks.logicng.solvers.datastructures.ProtoBufSolverDatastructures.PBMsClause;
import com.booleworks.logicng.solvers.datastructures.ProtoBufSolverDatastructures.PBMsVariable;
import com.booleworks.logicng.solvers.datastructures.ProtoBufSolverDatastructures.PBMsWatcher;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

public class SolverDatastructuresTest {

    @Test
    public void testLngHeap() {
        final FormulaFactory f = FormulaFactory.caching();
        final MiniSat solver = MiniSat.miniSat(f);

        final LNGIntVector heapContent = new LNGIntVector(1, 3, 5, -7, 9);
        final LNGIntVector indices = new LNGIntVector(42, 43, 44);

        final LNGHeap heap = new LNGHeap(solver.underlyingSolver(), heapContent, indices);

        final PBLngHeap serialized = SolverDatastructures.serialize(heap);
        final LNGHeap deserialized = SolverDatastructures.deserialize(serialized, solver.underlyingSolver());
        assertVecEquals(heap.getHeap(), deserialized.getHeap());
        assertVecEquals(heap.getIndices(), deserialized.getIndices());
    }

    @Test
    public void testMsClause() {
        final LNGIntVector data = new LNGIntVector(1, 3, 5, -7, 9);
        final MSClause clause = new MSClause(data, true, true, 3.3, 78, true, 8990, true, false, 7);
        final PBMsClause serialized = SolverDatastructures.serialize(clause, 47);
        final MSClause deserialized = SolverDatastructures.deserialize(serialized);

        assertThat(deserialized.get(0)).isEqualTo(1);
        assertThat(deserialized.get(1)).isEqualTo(3);
        assertThat(deserialized.get(2)).isEqualTo(5);
        assertThat(deserialized.get(3)).isEqualTo(-7);
        assertThat(deserialized.get(4)).isEqualTo(9);

        assertThat(deserialized.learnt()).isEqualTo(true);
        assertThat(deserialized.isAtMost()).isEqualTo(true);
        assertThat(deserialized.activity()).isEqualTo(3.3);
        assertThat(deserialized.sizeWithoutSelectors()).isEqualTo(78);
        assertThat(deserialized.seen()).isEqualTo(true);
        assertThat(deserialized.lbd()).isEqualTo(8990);
        assertThat(deserialized.canBeDel()).isEqualTo(true);
        assertThat(deserialized.oneWatched()).isEqualTo(false);
        assertThat(deserialized.atMostWatchers()).isEqualTo(7);
    }

    @Test
    public void testMsVariable() {
        final LNGIntVector data = new LNGIntVector(1, 3, 5, -7, 9);
        final MSClause clause = new MSClause(data, true, true, 3.3, 78, true, 8990, true, false, 7);
        final MSVariable variable = new MSVariable(true);
        variable.assign(Tristate.UNDEF);
        variable.setLevel(42);
        variable.setReason(clause);
        variable.incrementActivity(23.3);
        variable.setDecision(true);
        final IdentityHashMap<MSClause, Integer> clauseMap = new IdentityHashMap<>();
        clauseMap.put(clause, 42);
        final Map<Integer, MSClause> reverseMap = new HashMap<>();
        reverseMap.put(42, clause);

        final PBMsVariable serialized = SolverDatastructures.serialize(variable, clauseMap);
        final MSVariable deserialized = SolverDatastructures.deserialize(serialized, reverseMap);

        assertThat(deserialized.assignment()).isEqualTo(Tristate.UNDEF);
        assertThat(deserialized.level()).isEqualTo(42);
        assertThat(deserialized.activity()).isEqualTo(23.3);
        assertThat(deserialized.polarity()).isEqualTo(true);
        assertThat(deserialized.decision()).isEqualTo(true);
    }

    @Test
    public void testMsWatcher() {
        final LNGIntVector data = new LNGIntVector(1, 3, 5, -7, 9);
        final MSClause clause = new MSClause(data, true, true, 3.3, 78, true, 8990, true, false, 7);
        final MSWatcher watcher = new MSWatcher(clause, 42);
        final IdentityHashMap<MSClause, Integer> clauseMap = new IdentityHashMap<>();
        clauseMap.put(clause, 42);
        final Map<Integer, MSClause> reverseMap = new HashMap<>();
        reverseMap.put(42, clause);

        final PBMsWatcher serialized = SolverDatastructures.serialize(watcher, clauseMap);
        final MSWatcher deserialized = SolverDatastructures.deserialize(serialized, reverseMap);

        assertThat(deserialized.blocker()).isEqualTo(42);
        assertThat(deserialized.clause().get(1)).isEqualTo(3);
        assertThat(deserialized.clause().get(2)).isEqualTo(5);
        assertThat(deserialized.clause().get(3)).isEqualTo(-7);
        assertThat(deserialized.clause().get(4)).isEqualTo(9);

        assertThat(deserialized.clause().learnt()).isEqualTo(true);
        assertThat(deserialized.clause().isAtMost()).isEqualTo(true);
        assertThat(deserialized.clause().activity()).isEqualTo(3.3);
        assertThat(deserialized.clause().sizeWithoutSelectors()).isEqualTo(78);
        assertThat(deserialized.clause().seen()).isEqualTo(true);
        assertThat(deserialized.clause().lbd()).isEqualTo(8990);
        assertThat(deserialized.clause().canBeDel()).isEqualTo(true);
        assertThat(deserialized.clause().oneWatched()).isEqualTo(false);
        assertThat(deserialized.clause().atMostWatchers()).isEqualTo(7);
    }

    @Test
    public void testLngBoundedIntQueue() {
        final LNGBoundedIntQueue queue = new LNGBoundedIntQueue(new LNGIntVector(1, 3, 5, 8), 1, 3, 5, 17, 42);
        final PBLngBoundedIntQueue serialized = SolverDatastructures.serialize(queue);
        final LNGBoundedIntQueue deserialized = SolverDatastructures.deserialize(serialized);

        assertThat(deserialized.getElems().get(0)).isEqualTo(1);
        assertThat(deserialized.getElems().get(1)).isEqualTo(3);
        assertThat(deserialized.getElems().get(2)).isEqualTo(5);
        assertThat(deserialized.getElems().get(3)).isEqualTo(8);

        assertThat(deserialized.getFirst()).isEqualTo(1);
        assertThat(deserialized.getLast()).isEqualTo(3);
        assertThat(deserialized.getSumOfQueue()).isEqualTo(5);
        assertThat(deserialized.getMaxSize()).isEqualTo(17);
        assertThat(deserialized.getQueueSize()).isEqualTo(42);
    }

    @Test
    public void testLngBoundedLongQueue() {
        final LNGBoundedLongQueue queue = new LNGBoundedLongQueue(new LNGLongVector(1, 3, 5, 8), 1, 3, 5, 17, 42);
        final PBLngBoundedLongQueue serialized = SolverDatastructures.serialize(queue);
        final LNGBoundedLongQueue deserialized = SolverDatastructures.deserialize(serialized);

        assertThat(deserialized.getElems().get(0)).isEqualTo(1);
        assertThat(deserialized.getElems().get(1)).isEqualTo(3);
        assertThat(deserialized.getElems().get(2)).isEqualTo(5);
        assertThat(deserialized.getElems().get(3)).isEqualTo(8);

        assertThat(deserialized.getFirst()).isEqualTo(1);
        assertThat(deserialized.getLast()).isEqualTo(3);
        assertThat(deserialized.getSumOfQueue()).isEqualTo(5);
        assertThat(deserialized.getMaxSize()).isEqualTo(17);
        assertThat(deserialized.getQueueSize()).isEqualTo(42);
    }
}
