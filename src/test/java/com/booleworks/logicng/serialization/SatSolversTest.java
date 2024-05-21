// SPDX-License-Identifier: Apache-2.0 and MIT
// Copyright 2023-20xx BooleWorks GmbH

package com.booleworks.logicng.serialization;

import static org.assertj.core.api.Assertions.assertThat;

import com.booleworks.logicng.formulas.Formula;
import com.booleworks.logicng.formulas.FormulaFactory;
import com.booleworks.logicng.formulas.Variable;
import com.booleworks.logicng.io.parsers.ParserException;
import com.booleworks.logicng.io.parsers.PropositionalParser;
import com.booleworks.logicng.io.readers.FormulaReader;
import com.booleworks.logicng.solvers.SATSolver;
import com.booleworks.logicng.solvers.sat.SATSolverConfig;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

public class SatSolversTest {

    private static FormulaFactory f;
    private static SolverSerializer serializer;
    private static Formula formula;
    private static Set<Variable> variables;
    private static Path tempFile;

    @BeforeAll
    public static void init() throws ParserException, IOException {
        f = FormulaFactory.caching();
        serializer = SolverSerializer.withoutProofs(f);
        tempFile = Files.createTempFile("temp", "pb");
        formula = FormulaReader.readPropositionalFormula(f, Paths.get("src/test/resources/large_formula.txt").toFile());
        variables = formula.variables(f);
    }

    @AfterAll
    public static void cleanUp() throws IOException {
        Files.deleteIfExists(tempFile);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    public void testSolverSimple(final boolean compress) throws IOException {
        final SATSolver solverBefore = SATSolver.newSolver(f);
        solverBefore.add(formula);
        serializer.serializeSolverToFile(solverBefore, tempFile, compress);
        final SATSolver solverAfter = SolverSerializer.withoutProofs(FormulaFactory.caching()).deserializeSatSolverFromFile(tempFile, compress);
        SolverComperator.compareSolverStates(solverBefore, solverAfter);
        compareSolverModels(solverBefore, solverAfter);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    public void testSolverSolved(final boolean compress) throws IOException {
        final SATSolver solverBefore = SATSolver.newSolver(f);
        solverBefore.add(formula);
        solverBefore.sat();
        serializer.serializeSolverToFile(solverBefore, tempFile, compress);
        final FormulaFactory ff = FormulaFactory.caching();
        final SATSolver solverAfter = SolverSerializer.withoutProofs(ff).deserializeSatSolverFromFile(tempFile, compress);
        SolverComperator.compareSolverStates(solverBefore, solverAfter);
        compareSolverModels(solverBefore, solverAfter);
        solverBefore.add(f.variable("v3025").negate(f));
        solverAfter.add(f.variable("v3025").negate(f));
        SolverComperator.compareSolverStates(solverBefore, solverAfter);
        compareSolverModels(solverBefore, solverAfter);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    public void testSolverWithProof(final boolean compress) throws IOException, ParserException {
        final SATSolver solverBefore = SATSolver.newSolver(f, SATSolverConfig.builder().proofGeneration(true).build());
        solverBefore.add(formula);
        serializer.serializeSolverToFile(solverBefore, tempFile, compress);
        final FormulaFactory ff = FormulaFactory.caching();
        final SATSolver solverAfter = SolverSerializer.withoutProofs(ff).deserializeSatSolverFromFile(tempFile, compress);
        SolverComperator.compareSolverStates(solverBefore, solverAfter);
        final PropositionalParser p = new PropositionalParser(f);
        final PropositionalParser pp = new PropositionalParser(ff);
        solverBefore.add(p.parse("v1668 & v1671"));
        solverAfter.add(pp.parse("v1668 & v1671"));
        assertThat(solverBefore.sat()).isEqualTo(false);
        assertThat(solverAfter.sat()).isEqualTo(false);
        SolverComperator.compareSolverStates(solverBefore, solverAfter);
    }

    private static void compareSolverModels(final SATSolver solver1, final SATSolver solver2) {
        solver1.sat();
        solver2.sat();
        final var model1 = solver1.satCall().model(variables).positiveVariables();
        final var model2 = solver2.satCall().model(variables).positiveVariables();
        assertThat(model2).isEqualTo(model1);
    }
}
