package kfupm.clinic.ds;

import java.util.List;

/** Students implement. */
public class LinkedQueue<T> {

    private Node<T> head, tail;
    private int size;

    private static class Node<T> {
        T data; Node<T> next;
        Node(T data) { this.data = data; }
    }


    public void enqueue(T item) {
        Node<T> n = new Node<>(item);
        if (tail == null) { head = tail = n; }
        else              { tail.next = n; tail = n; }
        size++;
    }

    public T dequeue() {
        if (head == null) return null;
        T data = head.data;
        head = head.next;
        if (head == null) tail = null;
        size--;
        return data;
    }

    public boolean isEmpty() { return head == null; }

    /** Non-destructive view for printing. */
    public List<T> toList() {
        List<T> list = new ArrayList<>();
        for (Node<T> n = head; n != null; n = n.next) list.add(n.data);
        return list;
    }

}
