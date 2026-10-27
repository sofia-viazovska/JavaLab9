package task2;

/**
 * Потокобезпечний кільцевий буфер на зв'язному списку, замкненому в кільце.
 * <p>
 * head вказує на елемент, з якого читаємо, tail на вільну клітинку, в яку писатимемо.
 * Якщо head == tail, буфер порожній. Щоб порожній буфер відрізнявся від повного,
 * у кільці на одну клітинку більше, ніж місткість, і вона завжди лишається вільною.
 */
public class RingBuffer<T> {

    private static class Node<T> {
        T value;
        Node<T> next;
    }

    private Node<T> head;
    private Node<T> tail;

    public RingBuffer(int capacity) {
        if (capacity < 1) {
            throw new IllegalArgumentException("Місткість має бути додатною");
        }
        Node<T> first = new Node<>();
        Node<T> last = first;
        for (int i = 0; i < capacity; i++) {
            Node<T> node = new Node<>();
            last.next = node;
            last = node;
        }
        last.next = first;
        head = first;
        tail = first;
    }

    /** Додає елемент у буфер. Якщо буфер повний, чекає, поки з'явиться місце. */
    public synchronized void put(T item) throws InterruptedException {
        while (isFull()) {
            wait();
        }
        tail.value = item;
        tail = tail.next;
        notifyAll();
    }

    /** Забирає елемент із буфера. Якщо буфер порожній, чекає, поки з'являться дані. */
    public synchronized T take() throws InterruptedException {
        while (isEmpty()) {
            wait();
        }
        T item = head.value;
        head.value = null;
        head = head.next;
        notifyAll();
        return item;
    }

    private boolean isEmpty() {
        return head == tail;
    }

    private boolean isFull() {
        return tail.next == head;
    }
}
