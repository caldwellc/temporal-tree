package org.temporaltree;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TreeUtilsTest {
	@Test
	public void testTree() {
		ObjectNode node = TreeUtils.createNodeWithChildren("key", "title");
		assertEquals(node.get("key").asText(), "key");
		assertEquals(node.get("title").asText(), "title");
		assertEquals(((ArrayNode)node.get("children")).size(), 0);
	}
}
