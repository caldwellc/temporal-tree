package org.temporaltree;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

public class TreeUtils {
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * Creates an ObjectNode with children
     * 
     * @param key
     * @param title
     * @return ObjectNode
     */
    public static ObjectNode createNodeWithChildren(String key, String title) {
        ObjectNode node = OBJECT_MAPPER.createObjectNode();
        node.set("key", new TextNode(key));
        node.set("title", new TextNode(title));
        node.set("children", OBJECT_MAPPER.createArrayNode());
        return node;
    }
}
