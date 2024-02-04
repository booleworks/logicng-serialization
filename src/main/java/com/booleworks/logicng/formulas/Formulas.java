// SPDX-License-Identifier: Apache-2.0 and MIT
// Copyright 2023-20xx BooleWorks GmbH

package com.booleworks.logicng.formulas;

import static com.booleworks.logicng.formulas.CType.EQ;
import static com.booleworks.logicng.formulas.CType.GE;
import static com.booleworks.logicng.formulas.CType.GT;
import static com.booleworks.logicng.formulas.CType.LE;
import static com.booleworks.logicng.formulas.CType.LT;

import com.booleworks.logicng.formulas.ProtoBufFormulas.PBComparison;
import com.booleworks.logicng.formulas.ProtoBufFormulas.PBFormulaMapping;
import com.booleworks.logicng.formulas.ProtoBufFormulas.PBFormulaType;
import com.booleworks.logicng.formulas.ProtoBufFormulas.PBFormulas;
import com.booleworks.logicng.formulas.ProtoBufFormulas.PBInternalFormula;
import com.booleworks.logicng.formulas.ProtoBufFormulas.PBInternalPseudoBooleanConstraint;
import com.booleworks.logicng.functions.SubNodeFunction;
import com.booleworks.logicng.util.Pair;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Serialization methods for LogicNG formulas.
 * @version 3.0.0
 * @since 2.5.0
 */
public interface Formulas {

    String NOT_SYMBOL = "~";

    /**
     * Serialize a formula to a file.
     * @param formula  the formula
     * @param path     the file path
     * @param compress a flag whether the file should be compressed (zip)
     * @throws IOException if there is a problem writing the file
     */
    static void serializeToFile(final Formula formula, final Path path, final boolean compress) throws IOException {
        try (final OutputStream outputStream = compress ? new GZIPOutputStream(Files.newOutputStream(path)) : Files.newOutputStream(path)) {
            serializeToStream(formula, outputStream);
        }
    }

    /**
     * Deserialize a formula from a file.
     * @param f        the formula factory to generate the formula
     * @param path     the file path
     * @param compress a flag whether the file should be compressed (zip)
     * @return the formula
     * @throws IOException if there is a problem reading the file
     */
    static Formula deserializeFromFile(final FormulaFactory f, final Path path, final boolean compress) throws IOException {
        try (final InputStream inputStream = compress ? new GZIPInputStream(Files.newInputStream(path)) : Files.newInputStream(path)) {
            return deserializeFromStream(f, inputStream);
        }
    }

    /**
     * Serialize a list of formulas to a file.
     * @param formulas the formulas
     * @param path     the file path
     * @param compress a flag whether the file should be compressed (zip)
     * @throws IOException if there is a problem writing the file
     */
    static void serializeListToFile(final List<Formula> formulas, final Path path, final boolean compress) throws IOException {
        try (final OutputStream outputStream = compress ? new GZIPOutputStream(Files.newOutputStream(path)) : Files.newOutputStream(path)) {
            serializeListToStream(formulas, outputStream);
        }
    }

    /**
     * Deserialize a list of formulas from a file.
     * @param f        the formula factory to generate the formulas
     * @param path     the file path
     * @param compress a flag whether the file should be compressed (zip)
     * @return the list of formulas
     * @throws IOException if there is a problem reading the file
     */
    static List<Formula> deserializeListFromFile(final FormulaFactory f, final Path path, final boolean compress) throws IOException {
        try (final InputStream inputStream = compress ? new GZIPInputStream(Files.newInputStream(path)) : Files.newInputStream(path)) {
            return deserializeListFromStream(f, inputStream);
        }
    }

    /**
     * Serialize a formula to a stream.
     * @param formula the formula
     * @param stream  the stream
     * @throws IOException if there is a problem writing to the stream
     */
    static void serializeToStream(final Formula formula, final OutputStream stream) throws IOException {
        serialize(formula).writeTo(stream);
    }

    /**
     * Deserialize a formula from a stream.
     * @param f      the formula factory to generate the formula
     * @param stream the stream
     * @return the formula
     * @throws IOException if there is a problem reading from the stream
     */
    static Formula deserializeFromStream(final FormulaFactory f, final InputStream stream) throws IOException {
        return deserialize(f, PBFormulas.newBuilder().mergeFrom(stream).build());
    }

    /**
     * Serialize a list of formulas to a stream.
     * @param formulas the formulas
     * @param stream   the stream
     * @throws IOException if there is a problem writing to the stream
     */
    static void serializeListToStream(final Collection<Formula> formulas, final OutputStream stream) throws IOException {
        serialize(formulas).writeTo(stream);
    }

    /**
     * Deserialize a list of formulas from a stream.
     * @param f      the formula factory to generate the formulas
     * @param stream the stream
     * @return the list of formulas
     * @throws IOException if there is a problem reading from the stream
     */
    static List<Formula> deserializeListFromStream(final FormulaFactory f, final InputStream stream) throws IOException {
        return deserializeList(f, PBFormulas.newBuilder().mergeFrom(stream).build());
    }

    /**
     * Serializes a formula to a protocol buffer.
     * @param formula the formula
     * @return the protocol buffer
     */
    static PBFormulas serialize(final Formula formula) {
        return serialize(List.of(formula));
    }

    /**
     * Serializes a list of formulas to a protocol buffer.
     * @param formulas the formulas
     * @return the protocol buffer
     */
    static PBFormulas serialize(final Collection<Formula> formulas) {
        final var maps = computeMappings(formulas);
        final var ids = formulas.stream().map(maps.first()::get).collect(Collectors.toList());
        return PBFormulas.newBuilder()
                .addAllId(ids)
                .setMapping(PBFormulaMapping.newBuilder().putAllMapping(maps.second()).build())
                .build();
    }

    /**
     * Computes the serialization mappings for a given formula.
     * @param formula the formula
     * @return a mapping from formula to ID and a mapping from ID to serialized formula (protocol buffer)
     * for each sub-node of the formula.
     */
    static Pair<Map<Formula, Integer>, Map<Integer, PBInternalFormula>> computeMappings(final Formula formula) {
        return computeMappings(List.of(formula));
    }

    /**
     * Computes the serialization mappings for a given list of formulas.
     * @param formulas the formulas
     * @return a mapping from formula to ID and a mapping from ID to serialized formula (protocol buffer)
     * for each sub-node of the formulas.
     */
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

    /**
     * Serializes a formula to a protocol buffer with a given formula to ID mapping.
     * @param formula    the formula
     * @param formula2id a mapping from formula to ID (must contain all sub-nodes of the formula)
     * @return the protocol buffer.
     */
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

    /**
     * Deserializes a formula from a protocol buffer.
     * @param f   the formula factory to generate the formula
     * @param bin the protocol buffer
     * @return the formula
     */
    static Formula deserialize(final FormulaFactory f, final PBFormulas bin) {
        return deserializeList(f, bin).get(0);
    }

    /**
     * Deserializes a list of formulas from a protocol buffer.
     * @param f   the formula factory to generate the formulas
     * @param bin the protocol buffer
     * @return the list of formulas
     */
    static List<Formula> deserializeList(final FormulaFactory f, final PBFormulas bin) {
        final var id2formula = deserialize(f, bin.getMapping());
        return bin.getIdList().stream().map(id2formula::get).collect(Collectors.toList());
    }

    /**
     * Deserializes a mapping from integer to formulas from a protocol buffer.
     * @param f   the formula factory to generate the formulas
     * @param bin the protocol buffer
     * @return the mapping from ID to formula
     */
    static Map<Integer, Formula> deserialize(final FormulaFactory f, final PBFormulaMapping bin) {
        final var id2formula = new TreeMap<Integer, Formula>();
        bin.getMappingMap().forEach((k, v) -> {
            id2formula.put(k, deserialize(f, v, id2formula));
        });
        return id2formula;
    }

    /**
     * Deserializes a protocol buffer to a formula with a given ID to formula mapping.
     * @param f          the formula factory to generate the formulas
     * @param bin        the protocol buffer
     * @param id2formula a mapping from ID to formula (must contain all sub-nodes of the formula)
     * @return the formula
     */
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
                final int rhs = (int) bin.getPbConstraint().getRhs();
                final CType ctype = deserialize(bin.getPbConstraint().getComparator());
                final List<Literal> lits = bin.getPbConstraint().getLiteralList().stream()
                        .map(it -> it.startsWith(NOT_SYMBOL) ? f.literal(it.substring(1), false) : f.literal(it, true))
                        .collect(Collectors.toList());
                final List<Integer> coeffs = new ArrayList<>();
                for (final long l : bin.getPbConstraint().getCoefficientList()) {
                    coeffs.add((int) l);
                }
                return f.pbc(ctype, rhs, lits, coeffs);
            case PREDICATE:
                return null;
            default:
                throw new IllegalArgumentException("Cannot deserialize type " + bin.getType());
        }
    }

    /**
     * Serializes a pseudo-Boolean comparator.
     * @param comparison the comparator
     * @return the protocol buffer
     */
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

    /**
     * Deserializes a pseudo-Boolean comparator.
     * @param bin the protocol buffer
     * @return the comparator
     */
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
