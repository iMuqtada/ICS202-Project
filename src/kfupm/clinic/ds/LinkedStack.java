package kfupm.clinic.ds;

/** Students implement. */
public class LinkedStack<T> {

    private Node<T> top;

    private static class Node<T> {
        T data; Node<T> next;
        Node(T data, Node<T> next) { this.data = data; this.next = next; }
    }

    public void push(T item) { top = new Node<>(item, top); }

    public T pop() {
        if (top == null) return null;
        T data = top.data;
        top = top.next;
        return data;
    }

    public T peek() { return (top == null) ? null : top.data; }

    public boolean isEmpty() { return top == null; }
}
