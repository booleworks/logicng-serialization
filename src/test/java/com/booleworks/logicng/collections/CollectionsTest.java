// SPDX-License-Identifier: Apache-2.0 and MIT
// Copyright 2023-20xx BooleWorks GmbH

package com.booleworks.logicng.collections;

import org.junit.jupiter.api.Test;
import com.booleworks.logicng.collections.ProtoBufCollections.PBBooleanVector;
import com.booleworks.logicng.collections.ProtoBufCollections.PBIntVector;
import com.booleworks.logicng.collections.ProtoBufCollections.PBLongVector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class CollectionsTest {

    @Test
    public void testLngBooleanVector() {
        final Random random = new Random(42);
        final List<LNGBooleanVector> vecs = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            final LNGBooleanVector vec = new LNGBooleanVector();
            for (int j = 0; j < 500; j++) {
                vec.push(random.nextBoolean());
            }
            vecs.add(vec);
        }
        final List<PBBooleanVector> serialized = vecs.stream().map(Collections::serialize).collect(Collectors.toList());
        final List<LNGBooleanVector> deserialized = serialized.stream().map(Collections::deserialize).collect(Collectors.toList());
        for (int i = 0; i < vecs.size(); i++) {
            CollectionComperator.assertVecEquals(vecs.get(i), deserialized.get(i));
        }
    }

    @Test
    public void testLngIntVector() {
        final Random random = new Random(42);
        final List<LNGIntVector> vecs = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            final LNGIntVector vec = new LNGIntVector();
            for (int j = 0; j < 500; j++) {
                vec.push(random.nextInt());
            }
            vecs.add(vec);
        }
        final List<PBIntVector> serialized = vecs.stream().map(Collections::serialize).collect(Collectors.toList());
        final List<LNGIntVector> deserialized = serialized.stream().map(Collections::deserialize).collect(Collectors.toList());
        for (int i = 0; i < vecs.size(); i++) {
            CollectionComperator.assertVecEquals(vecs.get(i), deserialized.get(i));
        }
    }

    @Test
    public void testLngLongVector() {
        final Random random = new Random(42);
        final List<LNGLongVector> vecs = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            final LNGLongVector vec = new LNGLongVector();
            for (int j = 0; j < 500; j++) {
                vec.push(random.nextLong());
            }
            vecs.add(vec);
        }
        final List<PBLongVector> serialized = vecs.stream().map(Collections::serialize).collect(Collectors.toList());
        final List<LNGLongVector> deserialized = serialized.stream().map(Collections::deserialize).collect(Collectors.toList());
        for (int i = 0; i < vecs.size(); i++) {
            CollectionComperator.assertVecEquals(vecs.get(i), deserialized.get(i));
        }
    }
}
