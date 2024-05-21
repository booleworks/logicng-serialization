// SPDX-License-Identifier: Apache-2.0 and MIT
// Copyright 2023-20xx BooleWorks GmbH

package com.booleworks.logicng.serialization;

import com.booleworks.logicng.solvers.sat.ProtoBufSolverCommons.PBClauseMinimization;
import com.booleworks.logicng.solvers.sat.ProtoBufSolverCommons.PBCnfMethod;
import com.booleworks.logicng.solvers.sat.ProtoBufSolverCommons.PBSatSolverConfig;
import com.booleworks.logicng.solvers.sat.SATSolverConfig;
import com.booleworks.logicng.solvers.sat.SATSolverConfig.CNFMethod;
import com.booleworks.logicng.solvers.sat.SATSolverConfig.ClauseMinimization;
import com.booleworks.logicng.solvers.sat.SATSolverLowLevelConfig;

/**
 * Serialization methods for SAT solver configurations.
 * @version 3.0.0
 * @since 2.5.0
 */
public interface SatSolverConfigs {

    /**
     * Serializes a SAT solver configuration to a protocol buffer.
     * @param config the configuration
     * @return the protocol buffer
     */
    static PBSatSolverConfig serializeSatSolverConfig(final SATSolverConfig config) {
        return PBSatSolverConfig.newBuilder()
                .setProofGeneration(config.proofGeneration())
                .setUseAtMostClauses(config.useAtMostClauses())
                .setCnfMethod(serializeCnfMode(config.cnfMethod()))
                .setClauseMinimization(serializeMinMode(config.clauseMinimization()))
                .setInitialPhase(config.initialPhase())

                .setVarDecay(config.lowLevelConfig().getVarDecay())
                .setVarInc(config.lowLevelConfig().getVarInc())
                .setRestartFirst(config.lowLevelConfig().getRestartFirst())
                .setRestartInc(config.lowLevelConfig().getRestartInc())
                .setClauseDecay(config.lowLevelConfig().getClauseDecay())

                .setLbLBDMinimizingClause(config.lowLevelConfig().getLbLBDMinimizingClause())
                .setLbLBDFrozenClause(config.lowLevelConfig().getLbLBDFrozenClause())
                .setLbSizeMinimizingClause(config.lowLevelConfig().getLbSizeMinimizingClause())
                .setFirstReduceDB(config.lowLevelConfig().getFirstReduceDB())
                .setSpecialIncReduceDB(config.lowLevelConfig().getSpecialIncReduceDB())
                .setIncReduceDB(config.lowLevelConfig().getIncReduceDB())
                .setFactorK(config.lowLevelConfig().getFactorK())
                .setFactorR(config.lowLevelConfig().getFactorR())
                .setSizeLBDQueue(config.lowLevelConfig().getSizeLBDQueue())
                .setSizeTrailQueue(config.lowLevelConfig().getSizeTrailQueue())
                .setReduceOnSize(config.lowLevelConfig().isReduceOnSize())
                .setReduceOnSizeSize(config.lowLevelConfig().getReduceOnSizeSize())
                .setMaxVarDecay(config.lowLevelConfig().getMaxVarDecay())

                .build();
    }

    /**
     * Deserializes a SAT solver from a protocol buffer.
     * @param bin the protocol buffer
     * @return the configuration
     */
    static SATSolverConfig deserializeSatSolverConfig(final PBSatSolverConfig bin) {
        final var llConfig = SATSolverLowLevelConfig.builder()
                .varDecay(bin.getVarDecay())
                .varInc(bin.getVarInc())
                .restartFirst(bin.getRestartFirst())
                .restartInc(bin.getRestartInc())
                .clauseDecay(bin.getClauseDecay())

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

        return SATSolverConfig.builder()
                .proofGeneration(bin.getProofGeneration())
                .useAtMostClauses(bin.getUseAtMostClauses())
                .cnfMethod(deserializeCnfMode(bin.getCnfMethod()))
                .clauseMinimization(deserializeMinMode(bin.getClauseMinimization()))
                .initialPhase(bin.getInitialPhase())
                .lowLevelConfig(llConfig)
                .build();
    }

    /**
     * Serializes the clause minimization algorithm to a protocol buffer.
     * @param minimization the algorithm
     * @return the protocol buffer
     */
    static PBClauseMinimization serializeMinMode(final ClauseMinimization minimization) {
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
    static ClauseMinimization deserializeMinMode(final PBClauseMinimization bin) {
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
    static PBCnfMethod serializeCnfMode(final CNFMethod cnf) {
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
    static CNFMethod deserializeCnfMode(final PBCnfMethod bin) {
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
