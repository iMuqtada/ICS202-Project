package kfupm.clinic.ds;

import java.util.List;
import java.util.ArrayList;

/** Students implement. */
public class SinglyLinkedList<T> {
    private class Node{
        T data;
        Node next;

        Node(T data){
            this.data = data;
            this.next = null;
        }
    }
    private Node head;
    private Node tail;

    public SinglyLinkedList(){
        head = null;
        tail = null;
    }
    public void addLast(T item) {
        Node newNode = new Node(item);

        if (head == null){
            head = tail = newNode;
        }else {
            tail.next = newNode;
            tail = newNode;
        }
    }
    public List<T> toList() {
        List<T> list = new ArrayList<>();
        for (Node n = head; n != null; n = n.next) list.add(n.data);
        return list;
    }
}
