package org.temporaltree;

import org.temporaltree.TreeUtils;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Test;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TreeGeneratorTest {
	@Test
	public void testGenerateIndexTree() {
		TreeGenerator<String> generator = new TreeGenerator<>();
		LeafGenerator<String> leafGenerator = new LeafGenerator<String>() {
			@Override
			public ObjectNode generateLeaf(String record) {
				ObjectNode leaf = TreeUtils.OBJECT_MAPPER.createObjectNode();
				leaf.put("key", record);
				leaf.put("title", record);
				leaf.put("isLeaf", true);
				return leaf;
			}
		};
		List<String> records = new ArrayList<>();
		for (int i = 0; i < 1000; i++) {
			records.add(i + "");
		}
		ObjectNode indexTree = generator.generateIndexTree(records, leafGenerator, 0, records.size() - 1);

		assertEquals(indexTree.get("key").asText(), "0-999");
		assertEquals(indexTree.get("title").asText(), "0-999");
		assertEquals(indexTree.get("children").size(), 10);

		var ref = new Object() {
			int start = 0;
		};
		((ArrayNode) indexTree.get("children")).forEach(child -> {
			assertEquals(child.get("key").asText(), ref.start + "-" + (ref.start + 99));
			assertEquals(child.get("title").asText(), ref.start + "-" + (ref.start + 99));
			assertEquals(child.get("children").size(), 100);
			ref.start += 100;
		});
	}

	@Test
	public void testGenerateTemporalTreeChildren() {
		TreeGenerator<Integer> generator = new TreeGenerator<>();
		LeafGenerator<Integer> leafGenerator = record -> {
			ObjectNode leaf = TreeUtils.OBJECT_MAPPER.createObjectNode();
			leaf.put("key", record + "");
			leaf.put("title", record + "");
			leaf.put("isLeaf", true);
			return leaf;
		};
		long start = 1717027200000L;
		DateAccessor<Integer> dateAccessor = new DateAccessor<Integer>() {
			@Override
			public Instant getDateTime(Integer record) {
				return Instant.ofEpochMilli(start + record);
			}
		};
		List<Integer> records = new ArrayList<>();
		for (int i = 0; i < 60*60*24; i++) {
			records.add(i * 1000);
		}
		int[] idx = new int[]{0};
		ArrayNode indexTree = generator.generateTemporalTreeChildren(records, dateAccessor, leafGenerator, ZoneId.of("Z"), null);
		assertEquals(indexTree.size(), 1);
		indexTree.forEach(dayNode -> {
			assertEquals(dayNode.get("key").asText(), "2024-05-30");
			ArrayNode hourNodes = (ArrayNode) dayNode.get("children");
			assertEquals(hourNodes.size(), 24);
			int[] hour = new int[]{0};
			hourNodes.forEach(hourNode -> {
				assertEquals(hourNode.get("key").asText(), "2024-05-30-" + String.format("%02d", hour[0]) + " Z");
				ArrayNode minuteNodes = (ArrayNode) hourNode.get("children");
				assertEquals(minuteNodes.size(), 60);
				int[] minute = new int[]{0};
				minuteNodes.forEach(minuteNode -> {
					assertEquals(minuteNode.get("key").asText(), "2024-05-30-" + String.format("%02d", hour[0]) + "-" + String.format("%02d", minute[0]) + " Z");
					ArrayNode children = (ArrayNode) minuteNode.get("children");
					assertEquals(children.size(), 60);
					children.forEach(child -> {
						assertEquals(child.get("key").asText(), records.get(idx[0]) + "");
						assertEquals(child.get("title").asText(), records.get(idx[0]) + "");
						idx[0]++;
					});
					minute[0]++;
				});
				hour[0]++;
			});
		});
	}
}
