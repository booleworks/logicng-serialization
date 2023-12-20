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
import com.booleworks.logicng.formulas.ProtoBufFormulas.PBFormulaType;
import com.booleworks.logicng.formulas.ProtoBufFormulas.PBPseudoBooleanConstraint;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
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
        final PBFormulaList.Builder builder = PBFormulaList.newBuilder();
        formulas.forEach(it -> builder.addFormula(serialize(it)));
        return builder.build();
    }

    static PBFormula serialize(final Formula formula) {
        final PBFormula.Builder builder = PBFormula.newBuilder();
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
                builder.addOperand(serialize(not.operand()));
                break;
            case EQUIV:
                builder.setType(PBFormulaType.EQUIV);
                final Equivalence eq = (Equivalence) formula;
                builder.addOperand(serialize(eq.left()));
                builder.addOperand(serialize(eq.right()));
                break;
            case IMPL:
                builder.setType(PBFormulaType.IMPL);
                final Implication impl = (Implication) formula;
                builder.addOperand(serialize(impl.left()));
                builder.addOperand(serialize(impl.right()));
                break;
            case OR:
                builder.setType(PBFormulaType.OR);
                final Or or = (Or) formula;
                for (final Formula op : or) {
                    builder.addOperand(serialize(op));
                }
                break;
            case AND:
                builder.setType(PBFormulaType.AND);
                final And and = (And) formula;
                for (final Formula op : and) {
                    builder.addOperand(serialize(op));
                }
                break;
            case PBC:
                builder.setType(PBFormulaType.PBC);
                final PBConstraint pbc = (PBConstraint) formula;
                final PBPseudoBooleanConstraint.Builder pbBuilder = PBPseudoBooleanConstraint.newBuilder();
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
        return bin.getFormulaList().stream().map(it -> deserialize(f, it)).collect(Collectors.toList());
    }

    static Formula deserialize(final FormulaFactory f, final PBFormula bin) {
        switch (bin.getType()) {
            case CONST:
                return f.constant(bin.getValue());
            case LITERAL:
                return f.literal(bin.getVariable(), bin.getValue());
            case NOT:
                return f.not(deserialize(f, bin.getOperand(0)));
            case IMPL:
            case EQUIV:
                final FType binType = bin.getType() == PBFormulaType.IMPL ? FType.IMPL : FType.EQUIV;
                return f.binaryOperator(binType, deserialize(f, bin.getOperand(0)), deserialize(f, bin.getOperand(1)));
            case AND:
            case OR:
                final FType naryType = bin.getType() == PBFormulaType.AND ? FType.AND : FType.OR;
                return f.naryOperator(naryType, bin.getOperandList().stream().map(it -> deserialize(f, it)).collect(Collectors.toList()));
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
