package kfupm.clinic.ds;

/**
 * Students implement a hash table.
 * Recommended collision handling: separate chaining.
 */
public class HashTable<K, V> {
    private class Node{
        K key;
        V value;
        Node next;

        Node(K key, V value){
            this.key = key; 
            this.value = value;
            this.next = null;
        }
    }
    private Node[] table;
    private int size;
    
    public HashTable() {
        table = new Node[16];
        size = 0;
    }
    private int hash(K key){
        return Math.abs(key.hashCode())% table.length;
    }
    public void put(K key, V value) {
        int index = hash(key);
        Node current = table[index];

        // check if key needs to be updated
        while (current != null){
            if (current.key.equals(key)){
                current.value = value;
                return;
            }
            current = current.next;
        }
        // seprate chanining
        Node newNode = new Node(key, value);
        newNode.next = table[index];
        table[index] = newNode;

        size++;
    }
    public V get(K key) {
        int index = hash(key);

        Node current = table[index];

        while (current != null){
            if (current.key.equals(key)){
                return current.value;
            }
            current = current.next;
        }

        return null;
    }
    public V remove(K key) {
        int index = hash(key);

        Node current = table[index];
        Node prev = null;

        while (current != null){
            if (current.key.equals(key)){
                if (prev == null){
                    table[index] = current.next;
                }else {
                    prev.next = current.next;
                }

                size--;
                return current.value;
            }

            prev = current;
            current = current.next;
        }
        return null;
    }
    public int size() {return size;}
}
