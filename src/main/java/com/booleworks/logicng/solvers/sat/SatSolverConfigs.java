package com.booleworks.logicng.solvers.sat;

import com.booleworks.logicng.solvers.sat.MiniSatConfig.CNFMethod;
import com.booleworks.logicng.solvers.sat.MiniSatConfig.ClauseMinimization;
import com.booleworks.logicng.solvers.sat.ProtoBufSolverCommons.PBClauseMinimization;
import com.booleworks.logicng.solvers.sat.ProtoBufSolverCommons.PBCnfMethod;
import com.booleworks.logicng.solvers.sat.ProtoBufSolverCommons.PBGlucoseConfig;
import com.booleworks.logicng.solvers.sat.ProtoBufSolverCommons.PBMiniSatConfig;

/**
 * Serialization methods for SAT solver configurations.
 * @version 3.0.0
 * @since 2.5.0
 */
public interface SatSolverConfigs {

    /**
     * Serializes a MiniSat configuration to a protocol buffer.
     * @param config the configuration
     * @return the protocol buffer
     */
    static PBMiniSatConfig serialize(final MiniSatConfig config) {
        return PBMiniSatConfig.newBuilder()
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

    /**
     * Deserializes a MiniSat configuration from a protocol buffer.
     * @param bin the protocol buffer
     * @return the configuration
     */
    static MiniSatConfig deserialize(final PBMiniSatConfig bin) {
        return MiniSatConfig.builder()
                .varDecay(bin.getVarDecay())
                .clMinimization(deserialize(bin.getClauseMin()))
                .restartFirst(bin.getRestartFirst())
                .restartInc(bin.getRestartInc())
                .clauseDecay(bin.getClauseDecay())
                .removeSatisfied(bin.getRemoveSatisfied())
                .lsFactor(bin.getLearntsizeFactor())
                .lsInc(bin.getLearntsizeInc())
                .incremental(bin.getIncremental())
                .initialPhase(bin.getInitialPhase())
                .proofGeneration(bin.getProofGeneration())
                .cnfMethod(deserialize(bin.getCnfMethod()))
                .bbInitialUBCheckForRotatableLiterals(bin.getBbInitialUBCheckForRotatableLiterals())
                .bbCheckForComplementModelLiterals(bin.getBbCheckForComplementModelLiterals())
                .bbCheckForRotatableLiterals(bin.getBbCheckForRotatableLiterals())
                .build();
    }

    /**
     * Serializes a Glucose configuration to a protocol buffer.
     * @param config the configuration
     * @return the protocol buffer
     */
    static PBGlucoseConfig serialize(final GlucoseConfig config) {
        return PBGlucoseConfig.newBuilder()
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

    /**
     * Deserializes a Glucose configuration from a protocol buffer.
     * @param bin the protocol buffer
     * @return the configuration
     */
    static GlucoseConfig deserialize(final PBGlucoseConfig bin) {
        return GlucoseConfig.builder()
                .lbLBDMinimizingClause(bin.getLbLBDMinimizingClause())
                .lbLBDFrozenClause(bin.getLbLBDFrozenClause())
                .lbSizeMinimizingClause(bin.getLbSizeMinimizingClause())
                .firstReduceDB(bin.getFirstReduceDB())
                .specialIncReduceDB(bin.getSpecialIncReduceDB())
                .incReduceDB(bin.getIncReduceDB())
                .factorK(bin.getFactorK())
                .factorR(bin.getFactorR())
                .sizeLBDQueue(bin.getSizeLBDQueue())
                .sizeTrailQueue(bin.getSizeTrailQueue())
                .reduceOnSize(bin.getReduceOnSize())
                .reduceOnSizeSize(bin.getReduceOnSizeSize())
                .maxVarDecay(bin.getMaxVarDecay())
                .build();
    }

    /**
     * Serializes the clause minimization algorithm to a protocol buffer.
     * @param minimization the algorithm
     * @return the protocol buffer
     */
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

    /**
     * Deserializes the clause minimization algorithm from a protocol buffer.
     * @param bin the protocol buffer
     * @return the algorithm
     */
    static ClauseMinimization deserialize(final PBClauseMinimization bin) {
        switch (bin) {
            case NONE:
                return ClauseMinimization.NONE;
            case BASIC:
                return ClauseMinimization.BASIC;
            case DEEP:
                return ClauseMinimization.DEEP;
            default:
                throw new IllegalArgumentException("Unknown clause minimization: " + bin);
        }
    }

    /**
     * Serializes the CNF algorithm to a protocol buffer.
     * @param cnf the algorithm
     * @return the protocol buffer
     */
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

    /**
     * Deserializes the CNF algorithm from a protocol buffer.
     * @param bin the protocol buffer
     * @return the algorithm
     */
    private static CNFMethod deserialize(final PBCnfMethod bin) {
        switch (bin) {
            case FACTORY_CNF:
                return CNFMethod.FACTORY_CNF;
            case PG_ON_SOLVER:
                return CNFMethod.PG_ON_SOLVER;
            case FULL_PG_ON_SOLVER:
                return CNFMethod.FULL_PG_ON_SOLVER;
            default:
                throw new IllegalArgumentException("Unknown CNF method: " + bin);
        }
    }
}
