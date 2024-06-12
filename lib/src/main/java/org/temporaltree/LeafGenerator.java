package org.temporaltree;

import com.fasterxml.jackson.databind.node.ObjectNode;

public interface LeafGenerator<T> {
    ObjectNode generateLeaf(T record);
}
