package persistence.base.tree;

import java.util.*;

public class BinaryTree<TK, TV> implements Iterable<Map.Entry<TK, TV>> {
    public Node<TK, TV> root;

    public Node<TK, TV> find(TK key) {
        var isFound = false;
        var temp = root;
        Node<TK, TV> item = null;
        var hash = key.hashCode();
        while (!isFound) {
            if (temp == null) {
                break;
            }

            if (hash < temp.hash) {
                temp = temp.left;
            } else if (hash > temp.hash) {
                temp = temp.right;
            }

            if (temp != null && hash == temp.hash) {
                isFound = true;
                item = temp;
            }
        }

        if (isFound) {
            return item;
        } else {
            return null;
        }
    }

    public void insert(TK key, TV item) {
        var node = find(key);

        if (node != null) {
            node.data = item;
            return;
        }

        var newItem = new Node<>(key, item);
        if (root == null) {
            root = newItem;
            root.colour = Color.Black;
            return;
        }

        Node<TK, TV> Y = null;
        var X = root;
        while (X != null) {
            Y = X;
            if (newItem.hash < X.hash) {
                X = X.left;
            } else {
                X = X.right;
            }
        }

        newItem.parent = Y;
        if (Y == null) {
            root = newItem;
        } else if (newItem.hash < Y.hash) {
            Y.left = newItem;
        } else {
            Y.right = newItem;
        }

        newItem.left = null;
        newItem.right = null;
        newItem.colour = Color.Red; //colour the new node red
        insertFixUp(newItem); //call method to check for violations and fix
    }

    public TV findNearestLess(TK key) {
        var hashedKey = key.hashCode();
        var node = this.root;
        Node<TK, TV> optimalNode = null;
        while (node != null) {
            if (node.hash <= hashedKey &&
                    (optimalNode == null ||
                            hashedKey - optimalNode.hash > hashedKey - node.hash
                    )
            ) {
                optimalNode = node;
            }

            node = node.hash > hashedKey ? node.left : node.right;
        }

        return optimalNode == null ? null : optimalNode.data;
    }

    private void leftRotate(Node<TK, TV> X) {
        var Y = X.right; // set Y
        X.right = Y.left; //turn Y's left subtree into X's right subtree
        if (Y.left != null) {
            Y.left.parent = X;
        }

        if (Y != null) {
            Y.parent = X.parent; //link X's parent to Y
        }

        if (X.parent == null) {
            root = Y;
        }

        if (X.parent != null && X == X.parent.left) {
            X.parent.left = Y;
        } else if (X.parent != null) {
            X.parent.right = Y;
        }

        Y.left = X; //put X on Y's left
        if (X != null) {
            X.parent = Y;
        }

    }

    private void rightRotate(Node<TK, TV> Y) {
        // right rotate is simply mirror code from left rotate
        var X = Y.left;
        Y.left = X.right;
        if (X.right != null) {
            X.right.parent = Y;
        }

        if (X != null) {
            X.parent = Y.parent;
        }

        if (Y.parent == null) {
            root = X;
        }

        assert Y.parent != null;
        if (Y == Y.parent.right) {
            Y.parent.right = X;
        }

        if (Y == Y.parent.left) {
            Y.parent.left = X;
        }

        X.right = Y; //put Y on X's right
        if (Y != null) {
            Y.parent = X;
        }
    }


    private void insertFixUp(Node<TK, TV> item) {
        //Checks Red-Black Tree properties
        while (item != root && item.parent.colour == Color.Red) {
            /*We have a violation*/
            if (item.parent == item.parent.parent.left) {
                var Y = item.parent.parent.right;
                if (Y != null && Y.colour == Color.Red) //Case 1: uncle is red
                {
                    item.parent.colour = Color.Black;
                    Y.colour = Color.Black;
                    item.parent.parent.colour = Color.Red;
                    item = item.parent.parent;
                } else //Case 2: uncle is black
                {
                    if (item == item.parent.right) {
                        item = item.parent;
                        leftRotate(item);
                    }

                    //Case 3: recolour & rotate
                    item.parent.colour = Color.Black;
                    item.parent.parent.colour = Color.Red;
                    rightRotate(item.parent.parent);
                }
            } else {
                //mirror image of code above
                Node<TK, TV> X;

                X = item.parent.parent.left;
                if (X != null && X.colour == Color.Black) //Case 1
                {
                    X.colour = Color.Red;
                    item.parent.parent.colour = Color.Black;
                    item = item.parent.parent;
                } else //Case 2
                {
                    if (item == item.parent.left) {
                        item = item.parent;
                        rightRotate(item);
                    }

                    //Case 3: recolour & rotate
                    item.parent.colour = Color.Black;
                    item.parent.parent.colour = Color.Red;
                    leftRotate(item.parent.parent);
                }
            }

            root.colour = Color.Black; //re-colour the root black as necessary
        }
    }

    public TV get(TK key) {
        var node = find(key);
        return node == null ? null : node.data;
    }

    public boolean contains(TK key) {
        return find(key) != null;
    }

    public List<Map.Entry<TK, TV>> toList() {
        var res = new ArrayList<Map.Entry<TK, TV>>();

        addToList(res, root);

        return res;
    }

    private void addToList(Collection<Map.Entry<TK, TV>> list, Node<TK, TV> node) {
        while (true) {
            if (node == null) {
                return;
            }

            addToList(list, node.left);
            list.add(new AbstractMap.SimpleEntry<>(node.key, node.data));
            node = node.right;
        }
    }

    public Iterator<Map.Entry<TK, TV>> iterator() {
        return toList().iterator();
    }
}
