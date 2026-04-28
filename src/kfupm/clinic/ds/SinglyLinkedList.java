package kfupm.clinic.ds;

import java.util.ArrayList;
import java.util.List;
/** Students implement. */


/** Singly linked list supporting append and full traversal. */
public class SinglyLinkedList<T> {

    private Node<T> head, tail;

    private static class Node<T> {
        T data; Node<T> next;
        Node(T data) { this.data = data; }
    }

    public void addLast(T item) {
        Node<T> n = new Node<>(item);
        if (tail == null) { head = tail = n; }
        else              { tail.next = n; tail = n; }
    }

    public List<T> toList() {
        List<T> list = new ArrayList<>();
        for (Node<T> n = head; n != null; n = n.next) list.add(n.data);
        return list;
    }
}
