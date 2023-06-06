package persistence.structure.list;

import persistence.base.*;
import persistence.structure.array.PersistentArray;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PersistentLinkedList<T> extends BasePersistentCollection<Integer, T, DoubleLinkedContent<T>> implements IUndoRedo<PersistentLinkedList<T>> {

    public PersistentLinkedList() {
        var head = new PersistentNode<>(modificationCount - 1, new DoubleLinkedData<T>(null, null, new PersistentNode<>(-1, null)));
        var tail = new PersistentNode<>(modificationCount - 1, new DoubleLinkedData<>(null, null, new PersistentNode<T>(-1, null)));
        head.update(modificationCount, new DoubleLinkedData<>(tail, null, head.value(modificationCount - 1).value, head.value(modificationCount - 1).id));
        tail.update(modificationCount, new DoubleLinkedData<>(null, head, tail.value(modificationCount - 1).value, tail.value(modificationCount - 1).id));

        nodes = new PersistentContent<>(new DoubleLinkedContent<>(head, tail), new ModificationCount(modificationCount));

    }

    private PersistentLinkedList(PersistentContent<DoubleLinkedContent<T>> nodes,
                                 int count,
                                 int modificationCount) {
        super(nodes, count, modificationCount);
    }

    public PersistentLinkedList(PersistentContent<DoubleLinkedContent<T>> nodes,
                                int count, int modificationCount,
                                int start) {
        super(nodes, count, modificationCount, start);
    }

    @Override
    protected int recalculateCount(int modificationStep) {
        return toList(modificationStep).size();
    }

    @Override
    protected PersistentContent<DoubleLinkedContent<T>> reassembleNodes() {
        var allModifications = new ArrayList<Map.Entry<UUID, Map.Entry<Integer, DoubleLinkedData<T>>>>();
        var nodeModificationCount = new LinkedHashMap<UUID, Integer>();
        var current = nodes.content.pseudoHead;

        for (var i = count; i != -2; i--) {
            var neededModifications = current.modifications.toList()
                    .stream()
                    .filter(m -> m.getKey() <= modificationCount)
                    .map(m -> Map.entry(m.getValue().id, m))
                    .sorted(Map.Entry.comparingByKey()).toList();

            allModifications.addAll(neededModifications);
            nodeModificationCount.put(current.value(modificationCount).id, neededModifications.size());
            current = current.value(modificationCount).next;
        }
        var orderedModifications = new ArrayList<>(allModifications.stream()
                .collect(Collectors.groupingBy(m -> m.getValue().getKey()))
                .values()
                .stream()
                .flatMap(entries -> entries
                        .stream()
                        .sorted((Comparator.comparing(o -> nodeModificationCount.get(o.getKey())))))
                .toList())
                .stream()
                .sorted(Comparator.comparing(o -> o.getValue().getKey()))
                .collect(Collectors.toList());

        var newNodes = new LinkedHashMap<UUID, PersistentNode<DoubleLinkedData<T>>>();
        newNodes.put(orderedModifications.get(0).getKey(),
                new PersistentNode<>(-1,
                        new DoubleLinkedData<>(null,
                                null,
                                new PersistentNode<>(-1, null),
                                orderedModifications.get(0).getKey())));


        orderedModifications.remove(0);

        newNodes.put(orderedModifications.get(0).getKey(),
                new PersistentNode<>(-1,
                        new DoubleLinkedData<>(null,
                                null,
                                new PersistentNode<>(-1, null),
                                orderedModifications.get(0).getKey())
                )
        );

        orderedModifications.remove(0);

        for (var entry : orderedModifications) {
            var nodeKey = entry.getKey();
            var step = entry.getValue().getKey();
            var nodeValue = entry.getValue().getValue();
            if (newNodes.containsKey(nodeKey)) {
                var node = newNodes.get(nodeKey);
                node.update(step,
                        new DoubleLinkedData<>(nodeValue.next == null ? null :
                                newNodes.get(nodeValue.next.value(step).id),
                                nodeValue.previous == null ? null : newNodes.get(nodeValue.previous.value(step).id),
                                node.value(step - 1).value.update(step, nodeValue.value.value(step)), nodeKey));
            } else {
                var newNode = new PersistentNode<>(step,
                        new DoubleLinkedData<>(newNodes.get(nodeValue.next.value(step).id),
                                newNodes.get(nodeValue.previous.value(step).id),
                                new PersistentNode<>(step, nodeValue.value.value(step)),
                                nodeKey)
                );
                newNodes.put(nodeKey, newNode);
            }
        }

        var newHead = newNodes.get(nodes.content.pseudoHead.value(modificationCount).id);
        var newTail = newNodes.get(nodes.content.pseudoTail.value(modificationCount).id);
        return new PersistentContent<>(new DoubleLinkedContent<>(newHead, newTail),
                new ModificationCount(modificationCount));

    }

    public T get(Integer num) {
        var node = findNode(num);
        return node == nodes.content.pseudoTail ? null :
                node.value(modificationCount).value == null ?
                        null :
                        node.value(modificationCount).value.value(modificationCount);
    }

    public PersistentLinkedList<T> clear() {
        if (count == 0) {
            return this;
        }

        Function<PersistentContent<DoubleLinkedContent<T>>, PersistentLinkedList<T>> updContent = x -> {
            x.update(m ->
            {
                m.pseudoHead.update(modificationCount + 1,
                        new DoubleLinkedData<>(m.pseudoTail, null, m.pseudoHead.value(modificationCount).value, m.pseudoHead.value(modificationCount).id));
                m.pseudoTail.update(modificationCount + 1,
                        new DoubleLinkedData<>(null, m.pseudoHead, m.pseudoTail.value(modificationCount).value, m.pseudoTail.value(modificationCount).id));
            });

            return new PersistentLinkedList<>(x, 0, modificationCount + 1);
        };

        if (nodes.maxModification.value > modificationCount) {
            var newContent = reassembleNodes();
            return updContent.apply(newContent);
        }

        return updContent.apply(nodes);
    }

    public boolean contains(T item) {
        var current = nodes.content.pseudoHead.value(modificationCount).next;
        for (var i = count; i != 0; i--) {
            if (current.value(modificationCount).value.value(modificationCount).equals(item)) {
                return true;
            }
            current = current.value(modificationCount).next;
        }
        return false;
    }

    public PersistentLinkedList<T> replace(Integer num, T value) {
        if (num > count) return this;
        if (nodes.maxModification.value > modificationCount) {
            var newContent = reassembleNodes();
            return replace(newContent, num, value);
        } else {
            return replace(nodes, num, value);
        }
    }

    public PersistentLinkedList<T> addLast(T value) {
        if (nodes.maxModification.value > modificationCount) {
            var newContent = reassembleNodes();
            return addLast(newContent, value);
        }

        return addLast(nodes, value);
    }

    public PersistentLinkedList<T> addFirst(T value) {
        if (nodes.maxModification.value > modificationCount) {
            var newContent = reassembleNodes();
            return addFirst(newContent, value);
        }

        return addFirst(nodes, value);
    }

    public PersistentLinkedList<T> removeLast() {
        if (count == 0) {
            return this;
        }

        if (nodes.maxModification.value > modificationCount) {
            var newContent = reassembleNodes();
            return removeLast(newContent);
        }

        return removeLast(nodes);
    }

    public PersistentLinkedList<T> removeFirst() {
        if (count == 0) {
            return this;
        }

        if (nodes.maxModification.value > modificationCount) {
            var newContent = reassembleNodes();
            return removeFirst(newContent);
        }

        return removeFirst(nodes);
    }

    private PersistentLinkedList<T> addFirst(PersistentContent<DoubleLinkedContent<T>> content, T value) {
        var oldHead = content.content.pseudoHead.value(modificationCount);
        var oldNextToHead = oldHead.next;
        var oldNextToHeadValue = oldNextToHead.value(modificationCount);
        var newHead = new PersistentNode<>(modificationCount + 1,
                new DoubleLinkedData<>(oldHead.next,
                        content.content.pseudoHead,
                        new PersistentNode<>(modificationCount + 1, value)
                )
        );
        content.update(m -> {
                    oldNextToHead.update(modificationCount + 1,
                            new DoubleLinkedData<>(oldNextToHeadValue.next,
                                    newHead,
                                    oldNextToHeadValue.value,
                                    oldNextToHeadValue.id)
                    );
                    m.pseudoHead.update(modificationCount + 1,
                            new DoubleLinkedData<>(newHead, null, oldHead.value, oldHead.id));
                }
        );

        return new PersistentLinkedList<>(content, count + 1, modificationCount + 1);
    }

    private PersistentNode<DoubleLinkedData<T>> findNode(int num) {
        var current = nodes.content.pseudoHead.value(modificationCount).next;
        for (var i = num; i != 0; i--) {
            current = current.value(modificationCount).next;
        }

        return current;
    }


    private PersistentLinkedList<T> replace(PersistentContent<DoubleLinkedContent<T>> content, int num, T value) {
        var node = findNode(num);
        var nodeValue = node.value(modificationCount);
        content.update(m ->
                node.update(modificationCount + 1,
                        new DoubleLinkedData<>(nodeValue.next,
                                nodeValue.previous,
                                nodeValue.value.update(modificationCount + 1, value),
                                nodeValue.id)
                )
        );

        return new PersistentLinkedList<>(content, count, modificationCount + 1);
    }

    private PersistentLinkedList<T> addLast(PersistentContent<DoubleLinkedContent<T>> content, T value) {
        var oldTail = content.content.pseudoTail.value(modificationCount);
        var oldNextToTail = oldTail.previous;
        var oldNextToTailValue = oldNextToTail.value(modificationCount);
        var newTail = new PersistentNode<>(modificationCount + 1,
                new DoubleLinkedData<>(content.content.pseudoTail,
                        oldTail.previous,
                        new PersistentNode<>(modificationCount + 1, value)
                )
        );
        content.update(m -> {
                    oldNextToTail.update(modificationCount + 1,
                            new DoubleLinkedData<>(newTail,
                                    oldNextToTailValue.previous,
                                    oldNextToTailValue.value,
                                    oldNextToTailValue.id)
                    );
                    m.pseudoTail.update(modificationCount + 1,
                            new DoubleLinkedData<>(null, newTail, oldTail.value, oldTail.id)
                    );
                }
        );

        return new PersistentLinkedList<>(content, count + 1, modificationCount + 1);
    }

    private PersistentLinkedList<T> removeFirst(PersistentContent<DoubleLinkedContent<T>> content) {
        var oldHead = content.content.pseudoHead;
        var oldHeadValue = oldHead.value(modificationCount);
        var oldNextToNextToHead = oldHeadValue.next.value(modificationCount).next;
        var oldNextToNextToHeadValue = oldNextToNextToHead.value(modificationCount);
        content.update(m -> {
                    oldNextToNextToHead.update(modificationCount + 1,
                            new DoubleLinkedData<>(oldNextToNextToHeadValue.next,
                                    oldHead,
                                    oldNextToNextToHeadValue.value,
                                    oldNextToNextToHeadValue.id)
                    );
                    oldHead.update(modificationCount + 1,
                            new DoubleLinkedData<>(oldNextToNextToHead,
                                    null, oldHeadValue.value,
                                    oldHeadValue.id)
                    );
                }
        );
        return new PersistentLinkedList<>(content, count - 1, modificationCount + 1);
    }

    private PersistentLinkedList<T> removeLast(PersistentContent<DoubleLinkedContent<T>> content) {
        var oldTail = content.content.pseudoTail;
        var oldTailValue = oldTail.value(modificationCount);
        var oldNextToNextToTail = oldTailValue.previous.value(modificationCount).previous;
        var oldNextToNextToTailValue = oldNextToNextToTail.value(modificationCount);
        content.update(m -> {
                    oldNextToNextToTail.update(modificationCount + 1,
                            new DoubleLinkedData<>(oldTail,
                                    oldNextToNextToTailValue.previous,
                                    oldNextToNextToTailValue.value,
                                    oldNextToNextToTailValue.id)
                    );
                    oldTail.update(modificationCount,
                            new DoubleLinkedData<>(null,
                                    oldNextToNextToTail,
                                    oldTailValue.value,
                                    oldTailValue.id)
                    );
                }
        );
        return new PersistentLinkedList<>(content, count - 1, modificationCount + 1);
    }


    private ArrayList<T> toList(int modificationStep) {
        var newList = new ArrayList<T>();
        var current = nodes.content.pseudoHead.value(modificationStep).next;
        for (var i = count; i != 0; i--) {
            newList.add(current.value(modificationStep).value.value(modificationStep));
            current = current.value(modificationStep).next;
        }
        return newList;
    }

    public PersistentArray<T> toPersistentArray() {
        var content = new PersistentContent<List<PersistentNode<T>>>(
                new ArrayList<>(), nodes.maxModification);

        var current = nodes.content.pseudoHead.value(modificationCount).next;
        for (var i = count; i != 0; i--) {
            var currentValue = current.value(modificationCount);
            content.content.add(currentValue.value);
            current = currentValue.next;
        }

        return new PersistentArray<>(content, count, modificationCount, modificationCount);
    }


    @Override
    public PersistentLinkedList<T> undo() {
        return modificationCount == startModificationCount ? this :
                new PersistentLinkedList<>(
                        nodes,
                        recalculateCount(modificationCount - 1),
                        modificationCount - 1);

    }

    @Override
    public PersistentLinkedList<T> redo() {
        return modificationCount == nodes.maxModification.value ? this :
                new PersistentLinkedList<>(nodes,
                        recalculateCount(modificationCount + 1),
                        modificationCount + 1);

    }

    public int size() {
        return count;
    }
}
