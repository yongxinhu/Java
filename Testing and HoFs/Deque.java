public interface Deque<Item> {
    void addFirst(Item it);

    void addLast(Item it);

    Item removeFirst();

    Item removeLast();

    void printDeque();

    Item get(int index);

    boolean isEmpty();

    int size();
}
