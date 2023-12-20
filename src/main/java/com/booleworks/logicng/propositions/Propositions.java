// SPDX-License-Identifier: Apache-2.0 and MIT
// Copyright 2023-20xx BooleWorks GmbH

package com.booleworks.logicng.propositions;

import com.booleworks.logicng.formulas.FormulaFactory;
import com.booleworks.logicng.formulas.Formulas;
import com.booleworks.logicng.propositions.ProtoBufPropositions.PBProposition;
import com.booleworks.logicng.propositions.ProtoBufPropositions.PBPropositionBackpack;
import com.booleworks.logicng.propositions.ProtoBufPropositions.PBPropositionList;
import com.google.protobuf.ByteString;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public interface Propositions {

    static PBProposition serialize(final Proposition p) {
        final PBProposition.Builder builder = PBProposition.newBuilder();
        builder.setFormula(Formulas.serialize(p.formula()));
        if (p instanceof StandardProposition) {
            return builder.setDescription(((StandardProposition) p).description()).build();
        } else if (p instanceof ExtendedProposition<?>) {
            final PropositionBackpack backpack = ((ExtendedProposition<?>) p).backpack();
            if (backpack instanceof SerializablePropositionBackpack) {
                final PBPropositionBackpack bp = PBPropositionBackpack.newBuilder()
                        .setPropositionType(((SerializablePropositionBackpack) backpack).propositionType().ordinal())
                        .setBackpack(((SerializablePropositionBackpack) backpack).serialize())
                        .build();
                return builder.setBackpack(bp).build();
            } else {
                throw new IllegalArgumentException("Cannot serialize a backpack of type " + backpack.getClass());
            }
        } else {
            throw new IllegalArgumentException("Cannot serialize a proposition of type " + p.getClass());
        }
    }

    static PBPropositionList serialize(final Collection<Proposition> propositions) {
        final PBPropositionList.Builder builder = PBPropositionList.newBuilder();
        propositions.forEach(it -> builder.addProposition(serialize(it)));
        return builder.build();
    }

    static StandardProposition deserialize(final FormulaFactory f, final PBProposition bin) {
        if (bin.hasDescription()) {
            return new StandardProposition(bin.getDescription(), Formulas.deserialize(f, bin.getFormula()));
        }
        throw new IllegalArgumentException("Cannot deserialize an extended proposition without a deserializer");
    }

    static <T extends SerializablePropositionBackpack> ExtendedProposition<T> deserialize(final FormulaFactory f, final PBProposition bin,
                                                                                          final Function<ByteString, T> deserializer) {
        return new ExtendedProposition<>(deserializer.apply(bin.getBackpack().getBackpack()), Formulas.deserialize(f, bin.getFormula()));
    }

    static Proposition deserialize(final FormulaFactory f, final PBProposition bin,
                                   final Map<Integer, Function<ByteString, SerializablePropositionBackpack>> deserializer) {
        if (bin.hasDescription()) {
            return new StandardProposition(bin.getDescription(), Formulas.deserialize(f, bin.getFormula()));
        } else {
            final Function<ByteString, SerializablePropositionBackpack> pDeserializer = deserializer.get(bin.getBackpack().getPropositionType());
            if (pDeserializer == null) {
                throw new IllegalArgumentException("Did not find a deserializer for proposition type " + bin.getBackpack().getPropositionType());
            }
            return new ExtendedProposition<>(pDeserializer.apply(bin.getBackpack().getBackpack()), Formulas.deserialize(f, bin.getFormula()));
        }
    }

    static List<StandardProposition> deserialize(final FormulaFactory f, final PBPropositionList bin) {
        return bin.getPropositionList().stream().map(it -> deserialize(f, it)).collect(Collectors.toList());
    }

    static List<ExtendedProposition<?>> deserialize(final FormulaFactory f, final PBPropositionList bin,
                                                    final Function<ByteString, SerializablePropositionBackpack> deserializer) {
        return bin.getPropositionList().stream().map(it -> deserialize(f, it, deserializer)).collect(Collectors.toList());
    }

    static List<Proposition> deserialize(final FormulaFactory f, final PBPropositionList bin,
                                         final Map<Integer, Function<ByteString, SerializablePropositionBackpack>> deserializer) {
        return bin.getPropositionList().stream().map(it -> deserialize(f, it, deserializer))
                .collect(Collectors.toList());
    }
}
