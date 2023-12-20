// SPDX-License-Identifier: Apache-2.0 and MIT
// Copyright 2023-20xx BooleWorks GmbH

package com.booleworks.logicng.propositions;

import static com.booleworks.logicng.propositions.Propositions.deserialize;
import static com.booleworks.logicng.propositions.Propositions.serialize;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.protobuf.ByteString;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import com.booleworks.logicng.formulas.FormulaFactory;
import com.booleworks.logicng.io.parsers.ParserException;
import com.booleworks.logicng.propositions.ProtoBufPropositions.PBPropositionList;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class PropositionsTest {

    final FormulaFactory f = FormulaFactory.nonCaching();

    @Test
    public void testStandardProposition() throws ParserException {
        final var p = new StandardProposition("description", f.parse("a & (b => c + d = 1) <=> ~x"));
        assertThat(deserialize(f, serialize(p))).isEqualTo(p);
    }

    @Test
    public void testStandardPropositions() throws ParserException {
        final var p1 = new StandardProposition("desc1", f.parse("a & (b => c + d = 1) <=> ~x"));
        final var p2 = new StandardProposition("desc2", f.parse("x | y"));
        final var p3 = new StandardProposition("desc3", f.parse("$false"));
        final List<Proposition> l = List.of(p1, p2, p3);
        assertThat(deserialize(f, serialize(l))).isEqualTo(l);
    }

    @Test
    public void testExtendedProposition() throws ParserException {
        final var p = new ExtendedProposition<>(new MyBackpack1(42, "more"), f.parse("a & (b => c + d = 1) <=> ~x"));
        assertThat(deserialize(f, serialize(p), MyBackpack1::deserialize)).isEqualTo(p);
    }

    @Test
    public void testExtendedPropositions() throws ParserException {
        final var p1 = new ExtendedProposition<>(new MyBackpack1(42, "more"), f.parse("a & (b => c + d = 1) <=> ~x"));
        final var p2 = new ExtendedProposition<>(new MyBackpack1(44, "more & more"), f.parse("x | y"));
        final var p3 = new ExtendedProposition<>(new MyBackpack1(48, "more & less"), f.parse("$false"));
        final List<Proposition> l = List.of(p1, p2, p3);
        assertThat(deserialize(f, serialize(l), MyBackpack1::deserialize)).isEqualTo(l);
    }

    @Test
    public void testSerializiationWithDifferentTypes() throws ParserException {
        final StandardProposition s1 = new StandardProposition("desc1", f.parse("a & (b => c + d = 1) <=> ~x"));
        final StandardProposition s2 = new StandardProposition("desc2", f.parse("x | y"));
        final ExtendedProposition<MyBackpack1> e1 = new ExtendedProposition<>(new MyBackpack1(42, "more"), f.parse("a & (b => c + d = 1) <=> ~x"));
        final ExtendedProposition<MyBackpack1> e2 = new ExtendedProposition<>(new MyBackpack1(44, "more & more"), f.parse("x | y"));
        final ExtendedProposition<MyBackpack2> e3 = new ExtendedProposition<>(new MyBackpack2(18, 13), f.parse("a & ~(b => c + d = 1) <=> x"));
        final ExtendedProposition<MyBackpack2> e4 = new ExtendedProposition<>(new MyBackpack2(22, 17), f.parse("x | y | z"));
        final List<Proposition> ps = List.of(s1, s2, e1, e2, e3, e4);

        final Map<Integer, Function<ByteString, SerializablePropositionBackpack>> deserializerMap = Map.of(
                MyBackpackType.BP1.ordinal(), MyBackpack1::deserialize,
                MyBackpackType.BP2.ordinal(), MyBackpack2::deserialize
        );

        final PBPropositionList serialized = serialize(ps);
        final List<Proposition> deserialized = deserialize(FormulaFactory.caching(), serialized, deserializerMap);

        Assertions.assertThat(deserialized).containsExactlyInAnyOrderElementsOf(List.of(s1, s2, e1, e2, e3, e4));
    }

    private enum MyBackpackType implements PropositionType {BP1, BP2}

    private static class MyBackpack1 implements SerializablePropositionBackpack {
        private final int id;
        private final String desc;

        private MyBackpack1(final int id, final String desc) {
            this.id = id;
            this.desc = desc;
        }

        @Override
        public PropositionType propositionType() {
            return MyBackpackType.BP1;
        }

        @Override
        public ByteString serialize() {
            return ByteString.copyFromUtf8(id + "#" + desc);
        }

        static MyBackpack1 deserialize(final ByteString bin) {
            final String[] tokens = bin.toStringUtf8().split("#");
            return new MyBackpack1(Integer.parseInt(tokens[0]), tokens[1]);
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            final MyBackpack1 that = (MyBackpack1) o;
            return id == that.id && Objects.equals(desc, that.desc);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, desc);
        }
    }

    private static class MyBackpack2 implements SerializablePropositionBackpack {
        private final int id1;
        private final int id2;

        public MyBackpack2(final int id1, final int id2) {
            this.id1 = id1;
            this.id2 = id2;
        }

        @Override
        public PropositionType propositionType() {
            return MyBackpackType.BP2;
        }

        @Override
        public ByteString serialize() {
            return ByteString.copyFromUtf8(id1 + "-" + id2);
        }

        static MyBackpack2 deserialize(final ByteString bin) {
            final String[] tokens = bin.toStringUtf8().split("-");
            return new MyBackpack2(Integer.parseInt(tokens[0]), Integer.parseInt(tokens[1]));
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            final MyBackpack2 that = (MyBackpack2) o;
            return id1 == that.id1 && id2 == that.id2;
        }

        @Override
        public int hashCode() {
            return Objects.hash(id1, id2);
        }
    }
}
