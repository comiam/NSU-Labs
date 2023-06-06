package persistence.base.tree;

public class Node<TK, TV> {
    public TK key;
    public TV data;
    public Node<TK, TV> parent;
    public Node<TK, TV> left;
    public Node<TK, TV> right;
    public int hash;
    Color colour;


    public Node(TK key, TV data) {
        this.key = key;
        this.data = data;
        hash = key.hashCode();
    }
}
