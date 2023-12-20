package com.booleworks.logicng.solvers;

import com.booleworks.logicng.collections.Collections;
import com.booleworks.logicng.collections.LNGIntVector;
import com.booleworks.logicng.datastructures.Tristate;
import com.booleworks.logicng.solvers.ProtoBufSatSolver.PBSolverStyle;
import com.booleworks.logicng.solvers.datastructures.SolverDatastructures;

public class SolverWrapperState {
    private final Tristate result;
    private final LNGIntVector validStates;
    private final int nextStateId;
    private final boolean lastComputationWithAssumptions;
    private final MiniSat.SolverStyle solverStyle;

    public SolverWrapperState(final MiniSat solver) {
        result = solver.result;
        validStates = solver.validStates;
        nextStateId = solver.nextStateId;
        lastComputationWithAssumptions = solver.lastComputationWithAssumptions;
        solverStyle = solver.style;
    }

    public static void setWrapperState(final MiniSat miniSat, final ProtoBufSatSolver.PBWrapperState wrapper) {
        miniSat.result = SolverDatastructures.deserialize(wrapper.getResult());
        miniSat.validStates = Collections.deserialize(wrapper.getValidStates());
        miniSat.nextStateId = wrapper.getNextStateId();
        miniSat.lastComputationWithAssumptions = wrapper.getLastComputationWithAssumptions();
        miniSat.style = deserialize(wrapper.getSolverStyle());
    }

    public Tristate getResult() {
        return result;
    }

    public LNGIntVector getValidStates() {
        return validStates;
    }

    public int getNextStateId() {
        return nextStateId;
    }

    public boolean isLastComputationWithAssumptions() {
        return lastComputationWithAssumptions;
    }

    public MiniSat.SolverStyle getSolverStyle() {
        return solverStyle;
    }

    public static PBSolverStyle serialize(final MiniSat.SolverStyle solverStyle) {
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

    private static MiniSat.SolverStyle deserialize(final PBSolverStyle solverStyle) {
        switch (solverStyle) {
            case MINISAT:
                return MiniSat.SolverStyle.MINISAT;
            case GLUCOSE:
                return MiniSat.SolverStyle.GLUCOSE;
            case MINICARD:
                return MiniSat.SolverStyle.MINICARD;
            default:
                throw new IllegalArgumentException("Unknwon solver style " + solverStyle);
        }
    }
}
