# temporal-tree
library for generating a tree structure for sorted temporal records


# generate an index tree
```java
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
```


# generate a temporal tree
```java
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
```
