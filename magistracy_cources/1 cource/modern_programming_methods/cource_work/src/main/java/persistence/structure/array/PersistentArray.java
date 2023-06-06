package persistence.structure.array;

import persistence.base.*;
import persistence.structure.list.DoubleLinkedContent;
import persistence.structure.list.DoubleLinkedData;
import persistence.structure.list.PersistentLinkedList;

import java.util.*;

public class PersistentArray<T> extends BasePersistentCollection<Integer, T, List<PersistentNode<T>>> implements Iterable<T>, IUndoRedo<PersistentArray<T>> {
    public PersistentArray() throws IndexOutOfBoundsException {
        nodes = new PersistentContent<>(new ArrayList<>(), new ModificationCount(modificationCount));
    }

    private PersistentArray(PersistentContent<List<PersistentNode<T>>> nodes, int count, int modificationCount) {
        super(nodes, count, modificationCount);
    }

    public PersistentArray(PersistentContent<List<PersistentNode<T>>> nodes, int count, int modificationCount, int start) {
        super(nodes, count, modificationCount, start);
    }

    @Override
    protected PersistentContent<List<PersistentNode<T>>> reassembleNodes() {
        var newContent = new PersistentContent<List<PersistentNode<T>>>(new ArrayList<>(),
                new ModificationCount(modificationCount));
        var allModifications = new ArrayList<Map.Entry<Integer, Map.Entry<Integer, T>>>();
        for (var i = 0; i < nodes.content.size(); i++) {
            var node = nodes.content.get(i);
            int finalI = i;
            var neededModifications = node
                    .modifications
                    .toList()
                    .stream()
                    .filter(
                            m -> m.getKey() <= modificationCount
                    ).sorted(
                            Comparator.comparingInt(Map.Entry::getKey)
                    ).map(val -> Map.entry(finalI, val)).toList();

            allModifications.addAll(neededModifications);
        }

        allModifications.forEach(m -> {
            if (m.getKey() >= newContent.content.size()) {
                newContent.update(c ->
                        c.add(new PersistentNode<>(m.getValue().getKey(), m.getValue().getValue())));
            } else {
                newContent.update(c -> c.get(m.getKey()).update(m.getValue().getKey(), m.getValue().getValue()));
            }
        });

        return newContent;
    }

    private void addImpl(PersistentContent<List<PersistentNode<T>>> content, int modificationCount, T value) {
        content.update(c -> c.add(new PersistentNode<>(modificationCount + 1, value)));
    }

    private void insertImpl(PersistentContent<List<PersistentNode<T>>> content, int modificationCount, int index, T value) {
        content.update(c ->
        {
            c.add(new PersistentNode<>(modificationCount + 1, c.get(c.size() - 1).value(modificationCount)));
            c.get(index).update(modificationCount + 1, value);
            for (var i = index + 1; i < c.size(); i++) {
                c.get(i).update(modificationCount + 1, c.get(i - 1).value(modificationCount));
            }
        });
    }

    private void replaceImpl(PersistentContent<List<PersistentNode<T>>> content, int modificationCount, int index, T value) {
        content.update(c -> c.get(index).update(modificationCount + 1, value));
    }

    private void removeImpl(PersistentContent<List<PersistentNode<T>>> content, int modificationCount, int index) {
        content.update(c -> {
            for (var i = index; i < c.size() - 1; i++) {
                c.get(i).update(modificationCount + 1, c.get(i + 1).value(modificationCount));
            }
            c.get(c.size() - 1).update(modificationCount + 1, null);
        });
    }

    private void clear(PersistentContent<List<PersistentNode<T>>> content, int modificationCount) {
        content.update(c -> c.forEach(n -> n.update(modificationCount + 1, null)));
    }

    public PersistentArray<T> add(T value) {
        if (nodes.maxModification.value > modificationCount) {
            var res = reassembleNodes();
            addImpl(res, modificationCount, value);

            return new PersistentArray<>(res, count + 1, modificationCount + 1);
        }

        addImpl(nodes, modificationCount, value);
        return new PersistentArray<>(nodes, count + 1, modificationCount + 1);
    }

    public PersistentArray<T> insert(int index, T value) {
        if (index < 0 || index > count) {
            throw new IndexOutOfBoundsException(index);
        }

        if (index == count) {
            return add(value);
        }

        if (nodes.maxModification.value > modificationCount) {
            var res = reassembleNodes();
            insertImpl(res, modificationCount, index, value);

            return new PersistentArray<>(res, count + 1, modificationCount + 1);
        }

        insertImpl(nodes, modificationCount, index, value);
        return new PersistentArray<>(nodes, count + 1, modificationCount + 1);
    }

    public PersistentArray<T> replace(Integer index, T value) {
        if (index < 0 || index > count) {
            throw new IndexOutOfBoundsException(index);
        }

        if (nodes.maxModification.value > modificationCount) {
            var res = reassembleNodes();
            replaceImpl(res, modificationCount, index, value);

            return new PersistentArray<>(res, count, modificationCount + 1);
        }

        replaceImpl(nodes, modificationCount, index, value);
        return new PersistentArray<>(nodes, count, modificationCount + 1);
    }

    public PersistentArray<T> remove(int index) {
        if (index < 0 || index >= count) {
            throw new IndexOutOfBoundsException(index);
        }

        if (nodes.maxModification.value > modificationCount) {
            var res = reassembleNodes();
            removeImpl(res, modificationCount, index);

            return new PersistentArray<>(res, count - 1, modificationCount + 1);
        }

        removeImpl(nodes, modificationCount, index);
        return new PersistentArray<>(nodes, count - 1, modificationCount + 1);
    }

    public PersistentArray<T> clearAll() {
        if (nodes.maxModification.value > modificationCount) {
            var res = reassembleNodes();
            clear(res, modificationCount);

            return new PersistentArray<>(res, 0, modificationCount + 1);
        }

        clear(nodes, modificationCount);
        return new PersistentArray<>(nodes, 0, modificationCount + 1);
    }

    public T get(Integer index) {
        if (index < 0 || index >= count) {
            throw new IndexOutOfBoundsException(index);
        }
        return nodes.content.get(index).value(modificationCount);
    }

    public Iterator<T> iterator() {
        return nodes.content
                .stream()
                .filter(
                        n -> n.modifications.toList()
                                .stream()
                                .anyMatch(m -> m.getKey() <= modificationCount))
                .map(n -> n.value(modificationCount))
                .iterator();
    }

    public PersistentArray<T> undo() {
        return modificationCount == startModificationCount ? this : new PersistentArray<>(nodes,
                recalculateCount(modificationCount - 1), modificationCount - 1);
    }

    public PersistentArray<T> redo() {
        return modificationCount == nodes.maxModification.value ? this : new PersistentArray<>(nodes,
                recalculateCount(modificationCount + 1), modificationCount + 1);
    }

    @Override
    protected int recalculateCount(int modificationStep) {
        return (int) nodes.content
                .stream()
                .filter(n -> n.modifications.toList()
                        .stream()
                        .anyMatch(m -> m.getKey() <= modificationStep)
                ).count();
    }

    public PersistentLinkedList<T> toPersistentLinkedList() {

        var head = new PersistentNode<>(
                modificationCount - 1,
                new DoubleLinkedData<T>(null, null,
                        new PersistentNode<>(-1, null)
                )
        );

        var tail = new PersistentNode<>(
                modificationCount - 1,
                new DoubleLinkedData<T>(null, null,
                        new PersistentNode<>(-1, null))
        );

        head.update(modificationCount, new DoubleLinkedData<>(tail, null,
                head.value(modificationCount - 1).value, head.value(modificationCount - 1).id));
        tail.update(modificationCount, new DoubleLinkedData<>(null, head,
                tail.value(modificationCount - 1).value, tail.value(modificationCount - 1).id));

        var content = new PersistentContent<>(new DoubleLinkedContent<>(head, tail), nodes.maxModification);

        for (var t : nodes.content) {
            var tailValue = content.content.pseudoTail.value(modificationCount);
            var prevToTail = tailValue.previous;
            var prevToTailValue = content.content.pseudoTail.value(modificationCount).previous.value(modificationCount);
            var dnode = new DoubleLinkedData<>(content.content.pseudoTail, prevToTail, t);
            var node = new PersistentNode<>(modificationCount - 1, dnode);

            prevToTail.update(modificationCount,
                    new DoubleLinkedData<>(node, prevToTailValue.previous, prevToTailValue.value, prevToTailValue.id));

            content.content.pseudoTail.update(modificationCount, new DoubleLinkedData<>(tailValue.next, node, tailValue.value, tailValue.id));
        }

        return new PersistentLinkedList<>(content, count, modificationCount, modificationCount);
    }
}
