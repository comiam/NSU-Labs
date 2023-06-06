package persistence.base;

import java.util.Arrays;

public abstract class BasePersistentCollection<K, OT, BT> {
    protected final int modificationCount, startModificationCount;
    public int count;
    protected PersistentContent<BT> nodes;

    protected BasePersistentCollection() {
        modificationCount = 0;
        startModificationCount = 0;
    }

    protected BasePersistentCollection(PersistentContent<BT> nodes, int count, int modificationCount) {
        this(nodes, count, modificationCount, 0);
    }

    protected BasePersistentCollection(PersistentContent<BT> nodes, int count, int modificationCount, int startModificationCount) {
        this.nodes = nodes;
        this.modificationCount = modificationCount;
        this.startModificationCount = startModificationCount;
        this.count = count;
    }

    public abstract OT get(K key);

    public abstract BasePersistentCollection<K, OT, BT> replace(K key, OT newValue);

    protected abstract int recalculateCount(int modificationStep);

    protected abstract PersistentContent<BT> reassembleNodes();

    public Object getIn(Object... keys) {
        Object item = get((K) keys[0]);
        var keysLength = keys.length;

        for (int i = 1; i < keysLength; i++) {
            if (item instanceof BasePersistentCollection bpc) {
                item = bpc.get(keys[i]);
            } else {
                throw new IndexOutOfBoundsException(String.format(
                        "out of nested bounds - real nest:%d, got keys: %d",
                        (i - 1),
                        keysLength
                ));
            }
        }

        return item;
    }

    public BasePersistentCollection<K, OT, BT> setIn(Object value, Object... keys) {
        if (keys.length == 1) {
            return replace((K) keys[0], (OT) value);
        }

        if (!(get((K) keys[0]) instanceof BasePersistentCollection bpc)) {
            throw new IndexOutOfBoundsException(String.format(
                    "out of nested bounds - real nest:%d from the keys end", keys.length
            ));
        }

        return replace((K) keys[0], (OT) bpc.setIn(value, removeFirstKey(keys)));
    }

    private Object[] removeFirstKey(Object... keys) {
        return Arrays.copyOfRange(keys, 1, keys.length);
    }
}


