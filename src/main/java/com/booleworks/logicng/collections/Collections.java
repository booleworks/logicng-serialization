// SPDX-License-Identifier: Apache-2.0 and MIT
// Copyright 2023-20xx BooleWorks GmbH

package com.booleworks.logicng.collections;

import com.booleworks.logicng.collections.ProtoBufCollections.PBBooleanVector;
import com.booleworks.logicng.collections.ProtoBufCollections.PBIntVector;
import com.booleworks.logicng.collections.ProtoBufCollections.PBIntVectorVector;
import com.booleworks.logicng.collections.ProtoBufCollections.PBLongVector;

public interface Collections {

    static PBBooleanVector serialize(final LNGBooleanVector vec) {
        final PBBooleanVector.Builder builder = PBBooleanVector.newBuilder().setSize(vec.size());
        for (int i = 0; i < vec.size(); i++) {
            builder.addElement(vec.get(i));
        }
        return builder.build();
    }

    static LNGBooleanVector deserialize(final PBBooleanVector bin) {
        final boolean[] elements = new boolean[bin.getElementCount()];
        for (int i = 0; i < elements.length; i++) {
            elements[i] = bin.getElement(i);
        }
        return new LNGBooleanVector(elements, bin.getSize());
    }

    static PBIntVector serialize(final LNGIntVector vec) {
        final PBIntVector.Builder builder = PBIntVector.newBuilder().setSize(vec.size());
        for (int i = 0; i < vec.size(); i++) {
            builder.addElement(vec.get(i));
        }
        return builder.build();
    }

    static PBIntVectorVector serialize(final LNGVector<LNGIntVector> vec) {
        final PBIntVectorVector.Builder builder = PBIntVectorVector.newBuilder().setSize(vec.size());
        for (int i = 0; i < vec.size(); i++) {
            builder.addElement(serialize(vec.get(i)));
        }
        return builder.build();
    }

    static LNGIntVector deserialize(final PBIntVector bin) {
        final int[] elements = new int[bin.getElementCount()];
        for (int i = 0; i < elements.length; i++) {
            elements[i] = bin.getElement(i);
        }
        return new LNGIntVector(elements, bin.getSize());
    }

    static LNGVector<LNGIntVector> deserialize(final PBIntVectorVector bin) {
        final var vec = new LNGVector<LNGIntVector>(bin.getSize());
        for (final PBIntVector i : bin.getElementList()) {
            vec.push(deserialize(i));
        }
        return vec;
    }

    static PBLongVector serialize(final LNGLongVector vec) {
        final PBLongVector.Builder builder = PBLongVector.newBuilder().setSize(vec.size());
        for (int i = 0; i < vec.size(); i++) {
            builder.addElement(vec.get(i));
        }
        return builder.build();
    }

    static LNGLongVector deserialize(final PBLongVector bin) {
        final long[] elements = new long[bin.getElementCount()];
        for (int i = 0; i < elements.length; i++) {
            elements[i] = bin.getElement(i);
        }
        return new LNGLongVector(elements, bin.getSize());
    }
}
