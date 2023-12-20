// SPDX-License-Identifier: Apache-2.0 and MIT
// Copyright 2023-20xx BooleWorks GmbH

package com.booleworks.logicng.formulas;

import static com.booleworks.logicng.formulas.Formulas.deserialize;
import static com.booleworks.logicng.formulas.Formulas.serialize;
import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import com.booleworks.logicng.RandomTag;
import com.booleworks.logicng.formulas.ProtoBufFormulas.PBFormula;
import com.booleworks.logicng.formulas.ProtoBufFormulas.PBFormulaList;
import com.booleworks.logicng.io.parsers.ParserException;
import com.booleworks.logicng.util.FormulaRandomizer;
import com.booleworks.logicng.util.FormulaRandomizerConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class FormulasTest {

    final FormulaFactory f = FormulaFactory.caching();

    @Test
    public void testFormulaTypes() {
        final FormulaContext ctx = new FormulaContext(f);
        for (final Formula formula : ctx.allFormulas()) {
            assertThat(deserialize(f, serialize(formula))).isEqualTo(formula);
        }
    }

    private static final Path ORIGINAL = Paths.get("src/test/resources/large_formula.txt");
    private static final Path PROTO = Paths.get("list.proto");
    private static final Path ZIP = Paths.get("list.zip");

    @Test
    public void testConstraintPerformance() throws IOException, ParserException {
        final var f1 = FormulaFactory.caching();
        final long t00 = System.currentTimeMillis();
        final List<String> lines = Files.readAllLines(ORIGINAL);
        final long t0 = System.currentTimeMillis();
        final List<Formula> constraints = new ArrayList<>();
        for (final String line : lines) {
            constraints.add(f1.parse(line));
        }

        final var f2 = FormulaFactory.caching();
        final long t1 = System.currentTimeMillis();
        final PBFormulaList bin = PBFormulaList.newBuilder().addAllFormula(constraints.stream().map(Formulas::serialize).collect(Collectors.toList())).build();
        final long t2 = System.currentTimeMillis();
        bin.writeTo(Files.newOutputStream(PROTO));
        final long t3 = System.currentTimeMillis();
        final GZIPOutputStream output = new GZIPOutputStream(Files.newOutputStream(ZIP));
        bin.writeTo(output);
        output.close();
        final long t4 = System.currentTimeMillis();

        final var f3 = FormulaFactory.caching();
        final PBFormulaList binList = PBFormulaList.newBuilder().mergeFrom(Files.newInputStream(PROTO)).build();
        final long t5 = System.currentTimeMillis();
        final List<Formula> deserialized = binList.getFormulaList().stream().map(it -> Formulas.deserialize(f2, it)).collect(Collectors.toList());
        final long t6 = System.currentTimeMillis();
        final PBFormulaList zipList = PBFormulaList.newBuilder().mergeFrom(new GZIPInputStream(Files.newInputStream(ZIP))).build();
        final long t7 = System.currentTimeMillis();
        final List<Formula> deserializedZipped = zipList.getFormulaList().stream().map(it -> Formulas.deserialize(f3, it)).collect(Collectors.toList());

        System.out.printf("Read Original:   %d ms%n", t0 - t00);
        System.out.printf("Parse Original:  %d ms%n", t1 - t0);
        System.out.println();

        System.out.printf("Serialization:   %d ms%n", t2 - t1);
        System.out.printf("Write ProtoBuf:  %d ms%n", t3 - t2);
        System.out.printf("Write Zipped:    %d ms%n", t4 - t3);
        System.out.println();

        System.out.printf("Read ProtoBuf:   %d ms%n", t5 - t4);
        System.out.printf("Read Zipped:     %d ms%n", t7 - t6);
        System.out.printf("Deserialization: %d ms%n", t6 - t5);
        System.out.println();

        System.out.printf("Original size:   %.2f MB%n", Files.size(ORIGINAL) / (1024 * 1024.0));
        System.out.printf("ProtoBuf size:   %.2f MB%n", Files.size(PROTO) / (1024 * 1024.0));
        System.out.printf("Zipped size:     %.2f MB%n", Files.size(ZIP) / (1024 * 1024.0));
        System.out.println();

        System.out.printf("Reading with Parsing:  %d ms%n", (t0 - t00) + (t1 - t0));
        System.out.printf("Reading with ProtoBuf: %d ms%n", (t5 - t4) + (t6 - t5));
        System.out.printf("Reading with Zipping:  %d ms%n", (t7 - t6) + (t6 - t5));
        System.out.printf("%n");
        Assertions.assertThat(deserialized).isEqualTo(constraints);
        Assertions.assertThat(deserializedZipped).isEqualTo(constraints);

        Files.deleteIfExists(PROTO);
        Files.deleteIfExists(ZIP);
    }

    @Test
    @RandomTag
    public void testRandomizedFormulas() {
        final FormulaRandomizer randomizer = new FormulaRandomizer(f, FormulaRandomizerConfig.builder().seed(42).build());
        for (int i = 0; i < 1000; i++) {
            final Formula original = randomizer.formula(5);
            final PBFormula serialized = serialize(original);
            final Formula deserialized = deserialize(f, serialized);
            assertThat(deserialized).isEqualTo(original);
        }
    }
}
