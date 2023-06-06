package persistence.base;

import persistence.base.tree.BinaryTree;

public class PersistentNode<TV> {
    public BinaryTree<Integer, TV> modifications = new BinaryTree<>();

    public PersistentNode(int creationStep, TV initialValue) {
        update(creationStep, initialValue);
    }

    public TV value(int accessStep) {
        return modifications.findNearestLess(accessStep);
    }

    public PersistentNode<TV> update(int accessStep, TV value) {
        modifications.insert(accessStep, value);
        return this;
    }
}
