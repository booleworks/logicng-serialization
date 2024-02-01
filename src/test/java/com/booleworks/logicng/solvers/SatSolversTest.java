// SPDX-License-Identifier: Apache-2.0 and MIT
// Copyright 2023-20xx BooleWorks GmbH

package com.booleworks.logicng.solvers;

import static com.booleworks.logicng.solvers.sat.SolverComperator.compareSolverStates;
import static org.assertj.core.api.Assertions.assertThat;

import com.booleworks.logicng.datastructures.Tristate;
import com.booleworks.logicng.formulas.Formula;
import com.booleworks.logicng.formulas.FormulaFactory;
import com.booleworks.logicng.formulas.Variable;
import com.booleworks.logicng.formulas.implementation.cached.CachingFormulaFactory;
import com.booleworks.logicng.io.parsers.ParserException;
import com.booleworks.logicng.io.readers.FormulaReader;
import com.booleworks.logicng.solvers.sat.GlucoseConfig;
import com.booleworks.logicng.solvers.sat.MiniSatConfig;
import com.booleworks.logicng.solvers.sat.SolverSerializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.SortedSet;

public class SatSolversTest {

    private static FormulaFactory f;
    private static SolverSerializer serializer;
    private static Formula formula;
    private static Path tempFile;

    @BeforeAll
    public static void init() throws ParserException, IOException {
        f = FormulaFactory.caching();
        serializer = SolverSerializer.withoutProofs(f);
        tempFile = Files.createTempFile("temp", "pb");
        formula = FormulaReader.readPropositionalFormula(f, Paths.get("src/test/resources/large_formula.txt").toFile());
    }

    @AfterAll
    public static void cleanUp() throws IOException {
        Files.deleteIfExists(tempFile);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    public void testMiniSatSimple(final boolean compress) throws IOException {
        final MiniSat solverBefore = MiniSat.miniSat(f);
        solverBefore.add(formula);
        serializer.serializeSolverToFile(solverBefore, tempFile, compress);
        final MiniSat solverAfter = SolverSerializer.withoutProofs(FormulaFactory.caching()).deserializeMiniSatFromFile(tempFile, compress);
        compareSolverStates(solverBefore, solverAfter);
        compareSolverModels(solverBefore, solverAfter);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    public void testMiniCardSimple(final boolean compress) throws IOException {
        final MiniSat solverBefore = MiniSat.miniCard(f);
        solverBefore.add(formula);
        serializer.serializeSolverToFile(solverBefore, tempFile, compress);
        final MiniSat solverAfter = SolverSerializer.withoutProofs(FormulaFactory.caching()).deserializeMiniSatFromFile(tempFile, compress);
        compareSolverStates(solverBefore, solverAfter);
        compareSolverModels(solverBefore, solverAfter);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    public void testGlucoseSimple(final boolean compress) throws IOException {
        final MiniSat solverBefore = MiniSat.glucose(f);
        solverBefore.add(formula);
        serializer.serializeSolverToFile(solverBefore, tempFile, compress);
        final MiniSat solverAfter = SolverSerializer.withoutProofs(FormulaFactory.caching()).deserializeGlucoseFromFile(tempFile, compress);
        compareSolverStates(solverBefore, solverAfter);
        compareSolverModels(solverBefore, solverAfter);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    public void testMiniSatSolved(final boolean compress) throws IOException {
        final MiniSat solverBefore = MiniSat.miniSat(f);
        solverBefore.add(formula);
        solverBefore.sat();
        serializer.serializeSolverToFile(solverBefore, tempFile, compress);
        final var ff = FormulaFactory.caching();
        final MiniSat solverAfter = SolverSerializer.withoutProofs(ff).deserializeMiniSatFromFile(tempFile, compress);
        compareSolverStates(solverBefore, solverAfter);
        compareSolverModels(solverBefore, solverAfter);
        solverBefore.add(f.variable("v3025").negate(ff));
        solverAfter.add(f.variable("v3025").negate(ff));
        compareSolverStates(solverBefore, solverAfter);
        compareSolverModels(solverBefore, solverAfter);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    public void testMiniCardSolved(final boolean compress) throws IOException {
        final MiniSat solverBefore = MiniSat.miniCard(f);
        solverBefore.add(formula);
        solverBefore.sat();
        serializer.serializeSolverToFile(solverBefore, tempFile, compress);
        final CachingFormulaFactory ff = FormulaFactory.caching();
        final MiniSat solverAfter = SolverSerializer.withoutProofs(ff).deserializeMiniSatFromFile(tempFile, compress);
        compareSolverStates(solverBefore, solverAfter);
        compareSolverModels(solverBefore, solverAfter);
        solverBefore.add(f.variable("v3025").negate(ff));
        solverAfter.add(f.variable("v3025").negate(ff));
        compareSolverStates(solverBefore, solverAfter);
        compareSolverModels(solverBefore, solverAfter);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    public void testGlucoseSolved(final boolean compress) throws IOException {
        final MiniSat solverBefore = MiniSat.glucose(f);
        solverBefore.add(formula);
        solverBefore.sat();
        serializer.serializeSolverToFile(solverBefore, tempFile, compress);
        final CachingFormulaFactory ff = FormulaFactory.caching();
        final MiniSat solverAfter = SolverSerializer.withoutProofs(ff).deserializeGlucoseFromFile(tempFile, compress);
        compareSolverStates(solverBefore, solverAfter);
        compareSolverModels(solverBefore, solverAfter);
        solverBefore.add(f.variable("v3025").negate(ff));
        solverAfter.add(f.variable("v3025").negate(ff));
        compareSolverStates(solverBefore, solverAfter);
        compareSolverModels(solverBefore, solverAfter);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    public void testMiniSatWithProof(final boolean compress) throws IOException, ParserException {
        final MiniSat solverBefore = MiniSat.miniSat(f, MiniSatConfig.builder().proofGeneration(true).build());
        solverBefore.add(formula);
        serializer.serializeSolverToFile(solverBefore, tempFile, compress);
        final CachingFormulaFactory ff = FormulaFactory.caching();
        final MiniSat solverAfter = SolverSerializer.withoutProofs(ff).deserializeMiniSatFromFile(tempFile, compress);
        compareSolverStates(solverBefore, solverAfter);
        solverBefore.add(f.parse("v1668 & v1671"));
        solverAfter.add(ff.parse("v1668 & v1671"));
        assertThat(solverBefore.sat()).isEqualTo(Tristate.FALSE);
        assertThat(solverAfter.sat()).isEqualTo(Tristate.FALSE);
        compareSolverStates(solverBefore, solverAfter);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    public void testGlucoseWithProof(final boolean compress) throws IOException, ParserException {
        final MiniSat solverBefore = MiniSat.glucose(f, MiniSatConfig.builder().proofGeneration(true).incremental(false).build(),
                GlucoseConfig.builder().build());
        solverBefore.add(formula);
        serializer.serializeSolverToFile(solverBefore, tempFile, compress);
        final CachingFormulaFactory ff = FormulaFactory.caching();
        final MiniSat solverAfter = SolverSerializer.withoutProofs(ff).deserializeGlucoseFromFile(tempFile, compress);
        solverBefore.add(f.parse("v1668 & v1671"));
        solverAfter.add(ff.parse("v1668 & v1671"));
        solverBefore.sat();
        solverAfter.sat();
        compareSolverStates(solverBefore, solverAfter);
        solverBefore.add(f.parse("v1668 & v1671"));
        solverAfter.add(ff.parse("v1668 & v1671"));
        assertThat(solverBefore.sat()).isEqualTo(Tristate.FALSE);
        assertThat(solverAfter.sat()).isEqualTo(Tristate.FALSE);
        compareSolverStates(solverBefore, solverAfter);
    }

    private static void compareSolverModels(final MiniSat solver1, final MiniSat solver2) {
        solver1.sat();
        solver2.sat();
        final SortedSet<Variable> model1 = solver1.model(solver1.knownVariables()).positiveVariables();
        final SortedSet<Variable> model2 = solver2.model(solver1.knownVariables()).positiveVariables();
        assertThat(model2).isEqualTo(model1);
    }
}
