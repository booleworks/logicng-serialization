package com.booleworks.logicng.solvers.sat;

import com.booleworks.logicng.solvers.sat.MiniSatConfig.CNFMethod;
import com.booleworks.logicng.solvers.sat.MiniSatConfig.ClauseMinimization;
import com.booleworks.logicng.solvers.sat.ProtoBufSolverCommons.PBClauseMinimization;
import com.booleworks.logicng.solvers.sat.ProtoBufSolverCommons.PBCnfMethod;

public interface SatSolverConfigs {
    static ProtoBufSolverCommons.PBMiniSatConfig serialize(final MiniSatConfig config) {
        return ProtoBufSolverCommons.PBMiniSatConfig.newBuilder()
                .setVarDecay(config.varDecay)
                .setClauseMin(serialize(config.clauseMin))
                .setRestartFirst(config.restartFirst)
                .setRestartInc(config.restartInc)
                .setClauseDecay(config.clauseDecay)
                .setRemoveSatisfied(config.removeSatisfied)
                .setLearntsizeFactor(config.learntsizeFactor)
                .setLearntsizeInc(config.learntsizeInc)
                .setIncremental(config.incremental)
                .setInitialPhase(config.initialPhase)
                .setProofGeneration(config.proofGeneration)
                .setCnfMethod(serialize(config.cnfMethod))
                .setBbInitialUBCheckForRotatableLiterals(config.bbInitialUBCheckForRotatableLiterals)
                .setBbCheckForComplementModelLiterals(config.bbCheckForComplementModelLiterals)
                .setBbCheckForRotatableLiterals(config.bbCheckForRotatableLiterals)
                .build();
    }

    static MiniSatConfig deserialize(final ProtoBufSolverCommons.PBMiniSatConfig config) {
        return MiniSatConfig.builder()
                .varDecay(config.getVarDecay())
                .clMinimization(deserialize(config.getClauseMin()))
                .restartFirst(config.getRestartFirst())
                .restartInc(config.getRestartInc())
                .clauseDecay(config.getClauseDecay())
                .removeSatisfied(config.getRemoveSatisfied())
                .lsFactor(config.getLearntsizeFactor())
                .lsInc(config.getLearntsizeInc())
                .incremental(config.getIncremental())
                .initialPhase(config.getInitialPhase())
                .proofGeneration(config.getProofGeneration())
                .cnfMethod(deserialize(config.getCnfMethod()))
                .bbInitialUBCheckForRotatableLiterals(config.getBbInitialUBCheckForRotatableLiterals())
                .bbCheckForComplementModelLiterals(config.getBbCheckForComplementModelLiterals())
                .bbCheckForRotatableLiterals(config.getBbCheckForRotatableLiterals())
                .build();
    }

    static ProtoBufSolverCommons.PBGlucoseConfig serialize(final GlucoseConfig config) {
        return ProtoBufSolverCommons.PBGlucoseConfig.newBuilder()
                .setLbLBDMinimizingClause(config.lbLBDMinimizingClause)
                .setLbLBDFrozenClause(config.lbLBDFrozenClause)
                .setLbSizeMinimizingClause(config.lbSizeMinimizingClause)
                .setFirstReduceDB(config.firstReduceDB)
                .setSpecialIncReduceDB(config.specialIncReduceDB)
                .setIncReduceDB(config.incReduceDB)
                .setFactorK(config.factorK)
                .setFactorR(config.factorR)
                .setSizeLBDQueue(config.sizeLBDQueue)
                .setSizeTrailQueue(config.sizeTrailQueue)
                .setReduceOnSize(config.reduceOnSize)
                .setReduceOnSizeSize(config.reduceOnSizeSize)
                .setMaxVarDecay(config.maxVarDecay)
                .build();
    }

    static GlucoseConfig deserialize(final ProtoBufSolverCommons.PBGlucoseConfig config) {
        return GlucoseConfig.builder()
                .lbLBDMinimizingClause(config.getLbLBDMinimizingClause())
                .lbLBDFrozenClause(config.getLbLBDFrozenClause())
                .lbSizeMinimizingClause(config.getLbSizeMinimizingClause())
                .firstReduceDB(config.getFirstReduceDB())
                .specialIncReduceDB(config.getSpecialIncReduceDB())
                .incReduceDB(config.getIncReduceDB())
                .factorK(config.getFactorK())
                .factorR(config.getFactorR())
                .sizeLBDQueue(config.getSizeLBDQueue())
                .sizeTrailQueue(config.getSizeTrailQueue())
                .reduceOnSize(config.getReduceOnSize())
                .reduceOnSizeSize(config.getReduceOnSizeSize())
                .maxVarDecay(config.getMaxVarDecay())
                .build();
    }

    static PBClauseMinimization serialize(final ClauseMinimization minimization) {
        switch (minimization) {
            case NONE:
                return PBClauseMinimization.NONE;
            case BASIC:
                return PBClauseMinimization.BASIC;
            case DEEP:
                return PBClauseMinimization.DEEP;
            default:
                throw new IllegalArgumentException("Unknown clause minimization: " + minimization);
        }
    }

    static ClauseMinimization deserialize(final PBClauseMinimization minimization) {
        switch (minimization) {
            case NONE:
                return ClauseMinimization.NONE;
            case BASIC:
                return ClauseMinimization.BASIC;
            case DEEP:
                return ClauseMinimization.DEEP;
            default:
                throw new IllegalArgumentException("Unknown clause minimization: " + minimization);
        }
    }

    private static PBCnfMethod serialize(final CNFMethod cnf) {
        switch (cnf) {
            case FACTORY_CNF:
                return PBCnfMethod.FACTORY_CNF;
            case PG_ON_SOLVER:
                return PBCnfMethod.PG_ON_SOLVER;
            case FULL_PG_ON_SOLVER:
                return PBCnfMethod.FULL_PG_ON_SOLVER;
            default:
                throw new IllegalArgumentException("Unknown CNF method: " + cnf);
        }
    }

    private static CNFMethod deserialize(final PBCnfMethod cnf) {
        switch (cnf) {
            case FACTORY_CNF:
                return CNFMethod.FACTORY_CNF;
            case PG_ON_SOLVER:
                return CNFMethod.PG_ON_SOLVER;
            case FULL_PG_ON_SOLVER:
                return CNFMethod.FULL_PG_ON_SOLVER;
            default:
                throw new IllegalArgumentException("Unknown CNF method: " + cnf);
        }
    }
}
