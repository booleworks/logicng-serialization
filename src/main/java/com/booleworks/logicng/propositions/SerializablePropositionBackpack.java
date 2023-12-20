package com.booleworks.logicng.propositions;

import com.google.protobuf.ByteString;

public interface SerializablePropositionBackpack extends PropositionBackpack {
    PropositionType propositionType();

    ByteString serialize();
}
