package kfupm.clinic.ds;

import java.util.Comparator;
import java.util.List;

/**
 * MaxHeap starter.
 *
 * Day-1 safety rule:
 * - Constructors must NEVER throw, so the program can start.
 * - Operations may throw UnsupportedOperationException until students implement them;
 *   the command dispatcher will catch and print [NOT SUPPORTED] instead of crashing.
 */
public class MaxHeap<T> {

    protected final Comparator<T> comparator;
    private Object[] heap;
    private int size;

    public MaxHeap(Comparator<T> comparator) {
        if (comparator == null) throw new IllegalArgumentException("comparator is null");
        this.comparator = comparator;
        this.heap = new Object[16];
        this.size = 0;
    }

    public void push(T item) {
        if (item == null){return;}
        // we check the capacity
        ensureCapacity();
        
        heap[size] = item;
        siftUp(size);
        size++;
    }
    public T pop() {
        if (isEmpty()){return null;}

        T root = (T) heap[0];
        size--;
        heap[0] = heap[size];
        heap[size] = null;
        
        if (!isEmpty()){
            siftDown(0);
        }
        
        return root;
    }
    public T peek() {
        if (isEmpty()){return null;}
        return (T) heap[0];
    }
    public boolean isEmpty() {
        return size == 0 ;
    }

    /** Non-destructive view for printing. */
    public List<T> toListSnapshot() {
        List<T> list = new ArrayList<>();

        for (int i = 0; i < size; i++){
            list.add((T) heap[i]);
        }
        return list;
    }

    private void ensureCapacity(){
        if (size < heap.length){return;}

        Object[] newHeap = new Object[heap.length*2];

        for (int i =0 ; i < heap.length; i++){
            newHeap[i] = heap[i];
        }
        heap = newHeap;
    }

    private void siftUp(int index){
        while (index > 0){
            int parent = (index - 1) / 2;

            T currentItem = (T) heap[index];
            T parentItem = (T) heap[parent];

            if (comparator.compare(currentItem, parentItem) <= 0){
                break;
            }

            swap(index, parent);
            index = parent;
        }
    }

    private void siftDown(int index){
        while (true){
            int left = 2*index + 1;
            int right = 2* index + 2;
            int largest = index;

            if (left < size){
                T leftItem = (T) heap[left];
                T largestItem = (T) heap[largest];
                if (comparator.compare(leftItem, largestItem) > 0){
                    largest = left;
                }
            }
            if (right < size){
                T rightItem = (T) heap[right];
                T largestItem = (T) heap[largest];

                if (comparator.compare(rightItem, largestItem) > 0){
                    largest = right;
                }
            }

            if (largest == index){
                break;
            }
            swap(index, largest);
            index = largest;
            
        }
    }
    
    private void swap (int i, int j){
        Object temp = heap[i];
        heap[i] = heap[j];
        heap[j] = temp;
    }
}

