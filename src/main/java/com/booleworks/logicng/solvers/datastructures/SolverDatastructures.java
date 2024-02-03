// SPDX-License-Identifier: Apache-2.0 and MIT
// Copyright 2023-20xx BooleWorks GmbH

package com.booleworks.logicng.solvers.datastructures;

import com.booleworks.logicng.collections.Collections;
import com.booleworks.logicng.collections.LNGIntVector;
import com.booleworks.logicng.datastructures.Tristate;
import com.booleworks.logicng.solvers.datastructures.ProtoBufSolverDatastructures.PBLngBoundedIntQueue;
import com.booleworks.logicng.solvers.datastructures.ProtoBufSolverDatastructures.PBLngBoundedLongQueue;
import com.booleworks.logicng.solvers.datastructures.ProtoBufSolverDatastructures.PBLngHeap;
import com.booleworks.logicng.solvers.datastructures.ProtoBufSolverDatastructures.PBMsClause;
import com.booleworks.logicng.solvers.datastructures.ProtoBufSolverDatastructures.PBMsVariable;
import com.booleworks.logicng.solvers.datastructures.ProtoBufSolverDatastructures.PBMsWatcher;
import com.booleworks.logicng.solvers.datastructures.ProtoBufSolverDatastructures.PBTristate;
import com.booleworks.logicng.solvers.sat.MiniSatStyleSolver;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Serialization methods for SAT solver datastructures.
 * @version 3.0.0
 * @since 2.5.0
 */
public interface SolverDatastructures {

    /**
     * Serializes a tristate to a protocol buffer.
     * @param tristate the tristate value
     * @return the protocol buffer
     */
    static PBTristate serialize(final Tristate tristate) {
        switch (tristate) {
            case FALSE:
                return PBTristate.FALSE;
            case TRUE:
                return PBTristate.TRUE;
            case UNDEF:
                return PBTristate.UNDEF;
            default:
                throw new IllegalArgumentException("Unknown tristate: " + tristate);
        }
    }

    /**
     * Deserializes a tristate from a protocol buffer.
     * @param bin the protocol buffer
     * @return the tristate
     */
    static Tristate deserialize(final PBTristate bin) {
        switch (bin) {
            case FALSE:
                return Tristate.FALSE;
            case TRUE:
                return Tristate.TRUE;
            case UNDEF:
                return Tristate.UNDEF;
            default:
                throw new IllegalArgumentException("Unknown tristate: " + bin);
        }
    }

    /**
     * Serializes a solver heap to a protocol buffer.
     * @param heap the heap
     * @return the protocol buffer
     */
    static PBLngHeap serialize(final LNGHeap heap) {
        return PBLngHeap.newBuilder()
                .setHeap(Collections.serialize(heap.getHeap()))
                .setIndices(Collections.serialize(heap.getIndices()))
                .build();
    }

    /**
     * Deserializes a solver heap from a protocol buffer.
     * @param bin the protocol buffer
     * @return the heap
     */
    static LNGHeap deserialize(final PBLngHeap bin, final MiniSatStyleSolver solver) {
        final LNGIntVector heap = Collections.deserialize(bin.getHeap());
        final LNGIntVector indices = Collections.deserialize(bin.getIndices());
        return new LNGHeap(solver, heap, indices);
    }

    /**
     * Serializes a MiniSat clause to a protocol buffer.
     * @param clause the clause
     * @param id     the clause ID
     * @return the protocol buffer
     */
    static PBMsClause serialize(final MSClause clause, final int id) {
        return PBMsClause.newBuilder()
                .setData(Collections.serialize(clause.getData()))
                .setLearnt(clause.learnt())
                .setIsAtMost(clause.isAtMost())
                .setActivity(clause.activity())
                .setSzWithoutSelectors(clause.sizeWithoutSelectors())
                .setSeen(clause.seen())
                .setLbd(clause.lbd())
                .setCanBeDel(clause.canBeDel())
                .setOneWatched(clause.oneWatched())
                .setAtMostWatchers(clause.isAtMost() ? clause.atMostWatchers() : -1)
                .setId(id)
                .build();
    }

    /**
     * Deserializes a MiniSat clause from a protocol buffer.
     * @param bin the protocol buffer
     * @return the clause
     */
    static MSClause deserialize(final PBMsClause bin) {
        return new MSClause(
                Collections.deserialize(bin.getData()),
                bin.getLearnt(),
                bin.getIsAtMost(),
                bin.getActivity(),
                bin.getSzWithoutSelectors(),
                bin.getSeen(),
                bin.getLbd(),
                bin.getCanBeDel(),
                bin.getOneWatched(),
                bin.getAtMostWatchers()
        );
    }

    /**
     * Serializes a MiniSat variable to a protocol buffer.
     * @param variable  the variable
     * @param clauseMap a mapping from clause to clause ID
     * @return the protocol buffer
     */
    static PBMsVariable serialize(final MSVariable variable, final IdentityHashMap<MSClause, Integer> clauseMap) {
        return PBMsVariable.newBuilder()
                .setAssignment(serialize(variable.assignment()))
                .setLevel(variable.level())
                .setActivity(variable.activity())
                .setPolarity(variable.polarity())
                .setDecision(variable.decision())
                .setReason(variable.reason() == null ? -1 : clauseMap.get(variable.reason())).build();
    }

    /**
     * Deserializes a MiniSat variable from a protocol buffer.
     * @param bin       the protocol buffer
     * @param clauseMap a mapping from clause ID to clause
     * @return the variable
     */
    static MSVariable deserialize(final PBMsVariable bin, final Map<Integer, MSClause> clauseMap) {
        final MSClause reason = bin.getReason() == -1 ? null : clauseMap.get(bin.getReason());
        return new MSVariable(deserialize(bin.getAssignment()), bin.getLevel(), reason, bin.getActivity(), bin.getPolarity(), bin.getDecision());
    }

    /**
     * Serializes a MiniSat watcher to a protocol buffer.
     * @param watcher   the watcher
     * @param clauseMap a mapping from clause to clause ID
     * @return the protocol buffer
     */
    static PBMsWatcher serialize(final MSWatcher watcher, final IdentityHashMap<MSClause, Integer> clauseMap) {
        return PBMsWatcher.newBuilder()
                .setClause(clauseMap.get(watcher.clause()))
                .setBlocker(watcher.blocker())
                .build();
    }

    /**
     * Deserializes a MiniSat watcher from a protocol buffer.
     * @param bin       the protocol buffer
     * @param clauseMap a mapping from clause ID to clause
     * @return the watcher
     */
    static MSWatcher deserialize(final PBMsWatcher bin, final Map<Integer, MSClause> clauseMap) {
        return new MSWatcher(clauseMap.get(bin.getClause()), bin.getBlocker());
    }

    /**
     * Serializes a bounded integer queue to a protocol buffer.
     * @param queue the queue
     * @return the protocol buffer
     */
    static PBLngBoundedIntQueue serialize(final LNGBoundedIntQueue queue) {
        return PBLngBoundedIntQueue.newBuilder()
                .setElems(Collections.serialize(queue.getElems()))
                .setFirst(queue.getFirst())
                .setLast(queue.getLast())
                .setSumOfQueue(queue.getSumOfQueue())
                .setMaxSize(queue.getMaxSize())
                .setQueueSize(queue.getQueueSize())
                .build();
    }

    /**
     * Deserializes a bounded integer queue from a protocol buffer.
     * @param bin the protocol buffer
     * @return the queue
     */
    static LNGBoundedIntQueue deserialize(final PBLngBoundedIntQueue bin) {
        return new LNGBoundedIntQueue(Collections.deserialize(bin.getElems()), bin.getFirst(), bin.getLast(),
                bin.getSumOfQueue(), bin.getMaxSize(), bin.getQueueSize());
    }

    /**
     * Serializes a bounded long queue to a protocol buffer.
     * @param queue the queue
     * @return the protocol buffer
     */
    static PBLngBoundedLongQueue serialize(final LNGBoundedLongQueue queue) {
        return PBLngBoundedLongQueue.newBuilder()
                .setElems(Collections.serialize(queue.getElems()))
                .setFirst(queue.getFirst())
                .setLast(queue.getLast())
                .setSumOfQueue(queue.getSumOfQueue())
                .setMaxSize(queue.getMaxSize())
                .setQueueSize(queue.getQueueSize())
                .build();
    }

    /**
     * Deserializes a bounded long queue from a protocol buffer.
     * @param bin the protocol buffer
     * @return the queue
     */
    static LNGBoundedLongQueue deserialize(final PBLngBoundedLongQueue bin) {
        return new LNGBoundedLongQueue(Collections.deserialize(bin.getElems()), bin.getFirst(), bin.getLast(),
                bin.getSumOfQueue(), bin.getMaxSize(), bin.getQueueSize());
    }
}
