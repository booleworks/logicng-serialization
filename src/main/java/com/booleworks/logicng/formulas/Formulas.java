// SPDX-License-Identifier: Apache-2.0 and MIT
// Copyright 2023-20xx BooleWorks GmbH

package com.booleworks.logicng.formulas;

import static com.booleworks.logicng.formulas.CType.EQ;
import static com.booleworks.logicng.formulas.CType.GE;
import static com.booleworks.logicng.formulas.CType.GT;
import static com.booleworks.logicng.formulas.CType.LE;
import static com.booleworks.logicng.formulas.CType.LT;

import com.booleworks.logicng.formulas.ProtoBufFormulas.PBComparison;
import com.booleworks.logicng.formulas.ProtoBufFormulas.PBFormula;
import com.booleworks.logicng.formulas.ProtoBufFormulas.PBFormulaList;
import com.booleworks.logicng.formulas.ProtoBufFormulas.PBFormulaMapping;
import com.booleworks.logicng.formulas.ProtoBufFormulas.PBFormulaType;
import com.booleworks.logicng.formulas.ProtoBufFormulas.PBInternalFormula;
import com.booleworks.logicng.formulas.ProtoBufFormulas.PBInternalPseudoBooleanConstraint;
import com.booleworks.logicng.functions.SubNodeFunction;
import com.booleworks.logicng.util.Pair;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public interface Formulas {

    String NOT_SYMBOL = "~";

    static void serializeToFile(final Formula formula, final Path path, final boolean compress) throws IOException {
        try (final OutputStream outputStream = compress ? new GZIPOutputStream(Files.newOutputStream(path)) : Files.newOutputStream(path)) {
            serializeToStream(formula, outputStream);
        }
    }

    static Formula deserializeFromFile(final FormulaFactory f, final Path path, final boolean compress) throws IOException {
        try (final InputStream inputStream = compress ? new GZIPInputStream(Files.newInputStream(path)) : Files.newInputStream(path)) {
            return deserializeFromStream(f, inputStream);
        }
    }

    static void serializeListToFile(final List<Formula> formulas, final Path path, final boolean compress) throws IOException {
        try (final OutputStream outputStream = compress ? new GZIPOutputStream(Files.newOutputStream(path)) : Files.newOutputStream(path)) {
            serializeListToStream(formulas, outputStream);
        }
    }

    static List<Formula> deserializeListFromFile(final FormulaFactory f, final Path path, final boolean compress) throws IOException {
        try (final InputStream inputStream = compress ? new GZIPInputStream(Files.newInputStream(path)) : Files.newInputStream(path)) {
            return deserializeListFromStream(f, inputStream);
        }
    }

    static void serializeListToStream(final Collection<Formula> formulas, final OutputStream stream) throws IOException {
        serialize(formulas).writeTo(stream);
    }

    static List<Formula> deserializeListFromStream(final FormulaFactory f, final InputStream stream) throws IOException {
        return deserialize(f, PBFormulaList.newBuilder().mergeFrom(stream).build());
    }

    static void serializeToStream(final Formula formula, final OutputStream stream) throws IOException {
        serialize(formula).writeTo(stream);
    }

    static Formula deserializeFromStream(final FormulaFactory f, final InputStream stream) throws IOException {
        return deserialize(f, PBFormula.newBuilder().mergeFrom(stream).build());
    }

    static PBFormulaList serialize(final Collection<Formula> formulas) {
        final var maps = computeMappings(formulas);
        final var ids = formulas.stream().map(maps.first()::get).collect(Collectors.toList());
        return PBFormulaList.newBuilder()
                .addAllId(ids)
                .setMapping(PBFormulaMapping.newBuilder().putAllMapping(maps.second()).build())
                .build();
    }

    static PBFormula serialize(final Formula formula) {
        final var maps = computeMappings(formula);
        return PBFormula.newBuilder()
                .setId(maps.first().get(formula))
                .setMapping(PBFormulaMapping.newBuilder().putAllMapping(maps.second()).build())
                .build();
    }

    static Pair<Map<Formula, Integer>, Map<Integer, PBInternalFormula>> computeMappings(final Formula formula) {
        return computeMappings(List.of(formula));
    }

    static Pair<Map<Formula, Integer>, Map<Integer, PBInternalFormula>> computeMappings(final Collection<Formula> formulas) {
        final var formula2id = new LinkedHashMap<Formula, Integer>();
        final var id2formula = new LinkedHashMap<Integer, PBInternalFormula>();
        int id = 0;
        for (final Formula formula : formulas) {
            for (final Formula subnode : formula.apply(new SubNodeFunction(formula.factory()))) {
                if (!formula2id.containsKey(subnode)) {
                    formula2id.put(subnode, id);
                    id2formula.put(id, serialize(subnode, formula2id));
                    id++;
                }
            }
        }
        return new Pair<>(formula2id, id2formula);
    }

    static PBInternalFormula serialize(final Formula formula, final Map<Formula, Integer> formula2id) {
        final PBInternalFormula.Builder builder = PBInternalFormula.newBuilder();
        switch (formula.type()) {
            case FALSE:
            case TRUE:
                builder.setType(PBFormulaType.CONST);
                builder.setValue(formula.type() == FType.TRUE);
                break;
            case LITERAL:
                builder.setType(PBFormulaType.LITERAL);
                final Literal lit = (Literal) formula;
                builder.setValue(lit.phase());
                builder.setVariable(lit.name());
                break;
            case NOT:
                builder.setType(PBFormulaType.NOT);
                final Not not = (Not) formula;
                builder.addOperand(formula2id.get(not.operand()));
                break;
            case EQUIV:
                builder.setType(PBFormulaType.EQUIV);
                final Equivalence eq = (Equivalence) formula;
                builder.addOperand(formula2id.get(eq.left()));
                builder.addOperand(formula2id.get(eq.right()));
                break;
            case IMPL:
                builder.setType(PBFormulaType.IMPL);
                final Implication impl = (Implication) formula;
                builder.addOperand(formula2id.get(impl.left()));
                builder.addOperand(formula2id.get(impl.right()));
                break;
            case OR:
                builder.setType(PBFormulaType.OR);
                final Or or = (Or) formula;
                for (final Formula op : or) {
                    builder.addOperand(formula2id.get(op));
                }
                break;
            case AND:
                builder.setType(PBFormulaType.AND);
                final And and = (And) formula;
                for (final Formula op : and) {
                    builder.addOperand(formula2id.get(op));
                }
                break;
            case PBC:
                builder.setType(PBFormulaType.PBC);
                final PBConstraint pbc = (PBConstraint) formula;
                final PBInternalPseudoBooleanConstraint.Builder pbBuilder = PBInternalPseudoBooleanConstraint.newBuilder();
                pbBuilder.setRhs(pbc.rhs());
                pbBuilder.setComparator(serialize(pbc.comparator()));
                pbc.coefficients().forEach(pbBuilder::addCoefficient);
                pbc.operands().forEach(it -> pbBuilder.addLiteral(it.toString()));
                builder.setPbConstraint(pbBuilder.build());
                break;
            case PREDICATE:
                builder.setType(PBFormulaType.PREDICATE);
                break;
        }
        return builder.build();
    }

    static List<Formula> deserialize(final FormulaFactory f, final PBFormulaList bin) {
        final var id2formula = deserialize(f, bin.getMapping());
        return bin.getIdList().stream().map(id2formula::get).collect(Collectors.toList());
    }

    static Formula deserialize(final FormulaFactory f, final PBFormula bin) {
        final var id2formula = deserialize(f, bin.getMapping());
        return id2formula.get(bin.getId());
    }

    static Map<Integer, Formula> deserialize(final FormulaFactory f, final PBFormulaMapping bin) {
        final var id2formula = new TreeMap<Integer, Formula>();
        bin.getMappingMap().forEach((k, v) -> {
            id2formula.put(k, deserialize(f, v, id2formula));
        });
        return id2formula;
    }

    static Formula deserialize(final FormulaFactory f, final PBInternalFormula bin, final Map<Integer, Formula> id2formula) {
        switch (bin.getType()) {
            case CONST:
                return f.constant(bin.getValue());
            case LITERAL:
                return f.literal(bin.getVariable(), bin.getValue());
            case NOT:
                return f.not(id2formula.get(bin.getOperand(0)));
            case IMPL:
            case EQUIV:
                final FType binType = bin.getType() == PBFormulaType.IMPL ? FType.IMPL : FType.EQUIV;
                return f.binaryOperator(binType, id2formula.get(bin.getOperand(0)), id2formula.get(bin.getOperand(1)));
            case AND:
            case OR:
                final FType naryType = bin.getType() == PBFormulaType.AND ? FType.AND : FType.OR;
                return f.naryOperator(naryType, bin.getOperandList().stream().map(id2formula::get).collect(Collectors.toList()));
            case PBC:
                final int rhs = bin.getPbConstraint().getRhs();
                final CType ctype = deserialize(bin.getPbConstraint().getComparator());
                final List<Literal> lits = bin.getPbConstraint().getLiteralList().stream()
                        .map(it -> it.startsWith(NOT_SYMBOL) ? f.literal(it.substring(1), false) : f.literal(it, true))
                        .collect(Collectors.toList());
                return f.pbc(ctype, rhs, lits, bin.getPbConstraint().getCoefficientList());
            case PREDICATE:
                return null;
            default:
                throw new IllegalArgumentException("Cannot deserialize type " + bin.getType());
        }
    }

    private static PBComparison serialize(final CType comparison) {
        switch (comparison) {
            case EQ:
                return PBComparison.EQ;
            case GT:
                return PBComparison.GT;
            case GE:
                return PBComparison.GE;
            case LT:
                return PBComparison.LT;
            case LE:
                return PBComparison.LE;
            default:
                throw new IllegalArgumentException("Unknown comparison type" + comparison);
        }
    }

    private static CType deserialize(final PBComparison bin) {
        switch (bin) {
            case EQ:
                return EQ;
            case GT:
                return GT;
            case GE:
                return GE;
            case LT:
                return LT;
            case LE:
                return LE;
            default:
                throw new IllegalArgumentException("Unknown comparison type" + bin);
        }
    }
}
