package com.booleworks.logicng.solvers;

import com.booleworks.logicng.collections.Collections;
import com.booleworks.logicng.collections.LNGIntVector;
import com.booleworks.logicng.datastructures.Tristate;
import com.booleworks.logicng.solvers.MiniSat.SolverStyle;
import com.booleworks.logicng.solvers.ProtoBufSatSolver.PBSolverStyle;
import com.booleworks.logicng.solvers.datastructures.SolverDatastructures;

/**
 * A class which captures some information from the {@link MiniSat} wrapper class,
 * required for serializing and deserializing SAT solvers.
 * @version 3.0.0
 * @since 2.5.0
 */
public class SolverWrapperState {
    public final Tristate result;
    public final LNGIntVector validStates;
    public final int nextStateId;
    public final boolean lastComputationWithAssumptions;
    public final SolverStyle solverStyle;

    /**
     * Constructs a new solver wrapper state.
     * @param solver the solver
     */
    public SolverWrapperState(final MiniSat solver) {
        result = solver.result;
        validStates = solver.validStates;
        nextStateId = solver.nextStateId;
        lastComputationWithAssumptions = solver.lastComputationWithAssumptions;
        solverStyle = solver.style;
    }

    /**
     * Sets a solver wrapper state to a given solver.
     * @param miniSat the solver
     * @param wrapper the wrapper state
     */
    public static void setWrapperState(final MiniSat miniSat, final ProtoBufSatSolver.PBWrapperState wrapper) {
        miniSat.result = SolverDatastructures.deserialize(wrapper.getResult());
        miniSat.validStates = Collections.deserialize(wrapper.getValidStates());
        miniSat.nextStateId = wrapper.getNextStateId();
        miniSat.lastComputationWithAssumptions = wrapper.getLastComputationWithAssumptions();
        miniSat.style = deserialize(wrapper.getSolverStyle());
    }

    /**
     * Serializes a solver style to a protocol buffer.
     * @param solverStyle the solver style
     * @return the protocol buffer
     */
    public static PBSolverStyle serialize(final SolverStyle solverStyle) {
        switch (solverStyle) {
            case MINISAT:
                return PBSolverStyle.MINISAT;
            case GLUCOSE:
                return PBSolverStyle.GLUCOSE;
            case MINICARD:
                return PBSolverStyle.MINICARD;
            default:
                throw new IllegalArgumentException("Unknwon solver style " + solverStyle);
        }
    }

    /**
     * Deserializes a solver style from a protocol buffer.
     * @param bin the protocol buffer
     * @return the solver style
     */
    private static SolverStyle deserialize(final PBSolverStyle bin) {
        switch (bin) {
            case MINISAT:
                return SolverStyle.MINISAT;
            case GLUCOSE:
                return SolverStyle.GLUCOSE;
            case MINICARD:
                return SolverStyle.MINICARD;
            default:
                throw new IllegalArgumentException("Unknwon solver style " + bin);
        }
    }
}
