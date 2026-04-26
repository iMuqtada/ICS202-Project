package kfupm.clinic.ds;

import java.util.function.BiConsumer;

/**
 * Students implement AVL tree (balanced BST).
 */
public class AVLTree<K extends Comparable<K>, V> {

    private Node root;

    private class Node {
        K key; V value;
        Node left, right;
        int height;
        Node(K key, V value) { this.key = key; this.value = value; this.height = 1; }
    }

    private int height(Node n) {
        return (n == null) ? 0 :
            n.height;
    }
    private void updateHeight(Node n) { n.height = 1 + Math.max(height(n.left), height(n.right)); }
    private int bf(Node n) { return (n == null) ? 0 : height(n.left) - height(n.right); }

    private Node rotateRight(Node y) {
        Node x = y.left; Node T2 = x.right;
        x.right = y; y.left = T2;
        updateHeight(y); updateHeight(x); return x;
    }
    private Node rotateLeft(Node x) {
        Node y = x.right; Node T2 = y.left;
        y.left = x; x.right = T2;
        updateHeight(x); updateHeight(y); return y;
    }
    private Node balance(Node n) {
        updateHeight(n);
        int b = bf(n);
        if (b > 1)  { if (bf(n.left)  < 0) n.left  = rotateLeft(n.left);   return rotateRight(n); }
        if (b < -1) { if (bf(n.right) > 0) n.right = rotateRight(n.right); return rotateLeft(n);  }
        return n;
    }

    public void put(K key, V value) {
        root = insert(root, key, value);
    }
    private Node insert(Node n, K key, V value) {
        if (n == null) return new Node(key, value);
        int c = key.compareTo(n.key);
        if      (c < 0) n.left  = insert(n.left,  key, value);
        else if (c > 0) n.right = insert(n.right, key, value);
        else            n.value = value;
        return balance(n);
    }
    public V get(K key) {
        Node n = root;
        while (n != null) {
            int c = key.compareTo(n.key);
            if (c < 0) n = n.left; else if (c > 0) n = n.right; else return n.value;
        }
        return null;
    }
    public void remove(K key) { throw new UnsupportedOperationException("TODO: AVLTree.remove"); }

    /** In-order traversal (sorted by key). */
    public void inOrder(BiConsumer<K, V> visitor) { throw new UnsupportedOperationException("TODO: AVLTree.inOrder"); }

    /** Returns the smallest key/value (leftmost node), or null if empty. */
    public Entry<K, V> minEntry() { throw new UnsupportedOperationException("TODO: AVLTree.minEntry"); }

    public record Entry<K, V>(K key, V value) {}
}
