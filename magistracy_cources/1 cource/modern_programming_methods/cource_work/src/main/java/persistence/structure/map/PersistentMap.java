package persistence.structure.map;

import persistence.base.*;
import persistence.base.tree.BinaryTree;
import persistence.structure.array.PersistentArray;
import persistence.structure.list.PersistentLinkedList;

import java.util.*;
import java.util.stream.Collectors;

public class PersistentMap<TK, TV> extends BasePersistentCollection<TK, TV, BinaryTree<TK, PersistentNode<TV>>> implements Iterable<Map.Entry<TK, TV>>, IUndoRedo<PersistentMap<TK, TV>> {
    public PersistentMap() {
        nodes = new PersistentContent<>(new BinaryTree<>(), new ModificationCount(modificationCount));
    }

    private PersistentMap(PersistentContent<BinaryTree<TK, PersistentNode<TV>>> nodes, int count, int modificationCount) {
        super(nodes, count, modificationCount);
    }

    protected PersistentContent<BinaryTree<TK, PersistentNode<TV>>> reassembleNodes() {
        var newContent = new PersistentContent<>(
                new BinaryTree<TK, PersistentNode<TV>>(),
                new ModificationCount(modificationCount)
        );

        var allModifications = new ArrayList<Map.Entry<TK, Map.Entry<Integer, TV>>>();

        for (var entry : nodes.content) {
            var nodeKey = entry.getKey();
            var persistentNode = entry.getValue();

            var neededModifications = persistentNode.modifications.toList()
                    .stream()
                    .filter(m -> m.getKey() <= modificationCount)
                    .map(m -> Map.entry(nodeKey, m))
                    .sorted(Comparator.comparing(m -> m.getValue().getKey())).toList();

            allModifications.addAll(neededModifications);
        }

        for (var mod : allModifications) {
            var nodeKey = mod.getKey();
            var step = mod.getValue().getKey();
            var nodeVal = mod.getValue().getValue();

            newContent.update(c -> {
                var node = c.get(nodeKey);
                if (node == null) {
                    c.insert(nodeKey, new PersistentNode<>(step, nodeVal));
                } else {
                    node.update(step, nodeVal);
                }
            });
        }

        return newContent;
    }

    private void implAdd(PersistentContent<BinaryTree<TK, PersistentNode<TV>>> nodes, int modificationCount, TK key, TV value) {
        nodes.update(c -> c.insert(key, new PersistentNode<>(modificationCount + 1, value)));
    }

    private void implRemove(PersistentContent<BinaryTree<TK, PersistentNode<TV>>> nodes, int modificationCount, TK key) {
        nodes.update(c -> c.get(key).update(modificationCount + 1, null));
    }

    private void implClear(PersistentContent<BinaryTree<TK, PersistentNode<TV>>> nodes, int modificationCount) {
        nodes.update(c -> {
            for (var keyValuePair : c.toList()) {
                keyValuePair.getValue().update(modificationCount + 1, null);
            }
        });
    }

    private void implReplace(PersistentContent<BinaryTree<TK, PersistentNode<TV>>> nodes, int modificationCount, TK key, TV value) {
        nodes.update(c -> c.get(key).update(modificationCount + 1, value));
    }

    public PersistentMap<TK, TV> add(TK key, TV value) {
        var tryNode = nodes.content.get(key);
        if (tryNode != null && tryNode.modifications.toList().stream().anyMatch(m -> m.getKey() <= modificationCount)) {
            throw new IllegalArgumentException("Such a key is already exists!");
        }

        if (nodes.maxModification.value > modificationCount) {
            var res = reassembleNodes();
            implAdd(res, modificationCount, key, value);

            return new PersistentMap<>(res, count + 1, modificationCount + 1);
        }

        implAdd(nodes, modificationCount, key, value);

        return new PersistentMap<>(nodes, count + 1, modificationCount + 1);
    }

    public PersistentMap<TK, TV> remove(TK key) {
        var tryNode = nodes.content.get(key);
        if (tryNode == null || tryNode.modifications.toList().stream().allMatch(m -> m.getKey() > modificationCount)) {
            return this;
        }

        if (nodes.maxModification.value > modificationCount) {
            var res = reassembleNodes();
            implRemove(res, modificationCount, key);

            return new PersistentMap<>(res, count - 1, modificationCount + 1);
        }

        implRemove(nodes, modificationCount, key);

        return new PersistentMap<>(nodes, count - 1, modificationCount + 1);
    }

    public PersistentMap<TK, TV> clear() {
        if (nodes.maxModification.value > modificationCount) {
            var res = reassembleNodes();
            implClear(res, modificationCount);

            return new PersistentMap<>(res, 0, modificationCount + 1);
        }

        implClear(nodes, modificationCount);

        return new PersistentMap<>(nodes, 0, modificationCount + 1);
    }

    public PersistentMap<TK, TV> replace(TK key, TV value) {
        var tryNode = nodes.content.get(key);
        if (tryNode == null || tryNode.modifications.toList().stream().allMatch(m -> m.getKey() > modificationCount)) {
            throw new IllegalArgumentException("Such a key does not exists!");
        }

        if (nodes.maxModification.value > modificationCount) {
            var res = reassembleNodes();
            implReplace(res, modificationCount, key, value);

            return new PersistentMap<>(res, count, modificationCount + 1);
        }

        implReplace(nodes, modificationCount, key, value);

        return new PersistentMap<>(nodes, count, modificationCount + 1);
    }

    public TV get(TK key) {
        var node = nodes.content.get(key);

        return node == null
                ? null
                : node.modifications.findNearestLess(modificationCount);
    }

    public Set<TK> keySet() {
        return nodes.content.
                toList().
                stream().
                filter(k -> k.getValue().modifications.
                        toList().
                        stream().
                        anyMatch(m -> m.getKey() <= modificationCount)
                ).
                map(Map.Entry::getKey).
                collect(Collectors.toSet());
    }


    public Set<TV> valueSet() {
        return nodes.content.
                toList().
                stream().
                filter(k -> k.getValue().modifications.
                        toList().
                        stream().
                        anyMatch(m -> m.getKey() <= modificationCount)
                ).
                map(k -> k.getValue().value(modificationCount)).
                collect(Collectors.toSet());
    }

    public Iterator<Map.Entry<TK, TV>> iterator() {
        return nodes.content.toList().stream()
                .filter(k ->
                        k.getValue().modifications.toList().stream().anyMatch(m -> m.getKey() <= modificationCount))
                .map(k ->
                        Map.entry(k.getKey(), k.getValue().value(modificationCount))).toList().iterator();
    }

    public PersistentMap<TK, TV> undo() {
        return modificationCount == startModificationCount ? this : new PersistentMap<>(nodes,
                recalculateCount(modificationCount - 1), modificationCount - 1);
    }

    public PersistentMap<TK, TV> redo() {
        return modificationCount == nodes.maxModification.value
                ? this
                : new PersistentMap<>(
                nodes,
                recalculateCount(modificationCount + 1),
                modificationCount + 1
        );
    }

    protected int recalculateCount(int modificationStep) {
        return (int) nodes.content.toList()
                .stream()
                .filter(n -> n.getValue().modifications.toList()
                        .stream()
                        .anyMatch(m -> m.getKey() <= modificationStep))
                .count();
    }

    public PersistentArray<TV> toPersistentArray() {
        var content = new PersistentContent<List<PersistentNode<TV>>>(new ArrayList<>(), nodes.maxModification);

        var persistentNodes = nodes.content.toList().stream().map(Map.Entry::getValue).toList();
        content.content.addAll(persistentNodes);

        return new PersistentArray<>(content, count, modificationCount, modificationCount);
    }

    public PersistentLinkedList<TV> toPersistentLinkedList() {
        return toPersistentArray().toPersistentLinkedList();
    }
}
