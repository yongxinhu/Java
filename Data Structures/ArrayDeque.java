public class ArrayDeque<Item> {
    public ArrayDeque() {
        _size = 0;
        nextFirst = 4;
        nextLast = 5;
        items = (Item[]) new Object[8];
    }
    public void addFirst(Item it) {
        items[nextFirst] = it;
        nextFirst = move(nextFirst - 1);
        _size += 1;
        if (nextLast == nextFirst) {
            doubleSize();
        }
    }
    public void addLast(Item it) {
        items[nextLast] = it;
        nextLast = move(nextLast + 1);
        _size += 1;
        if (nextLast == nextFirst) {
            doubleSize();
        }
    }
    public Item removeFirst() {
        if (isEmpty()) {
            return null;
        }
        nextFirst = move(nextFirst + 1);
        Item ret = items[nextFirst];
        items[nextFirst] = null;
        _size -= 1;
        if (needSmall()) {
            smallerSize();
        }
        return ret;
    }
    public Item removeLast() {
        if (isEmpty()) {
            return null;
        }
        nextLast = move(nextLast - 1);
        Item ret = items[nextLast];
        items[nextLast] = null;
        _size -= 1;
        if (needSmall()) {
            smallerSize();
        }
        return ret;
    }
    public void printDeque() {
        for (int i = move(nextFirst + 1); i != nextLast; i = move(i + 1)) {
            System.out.print(items[i]);
            System.out.print(" ");
        }
        System.out.println();
    }
    public Item get(int index) {
        if (index < 0 || index >= _size) {
            return null;
        }
        return items[move(nextFirst + 1 + index)];
    }
    public boolean isEmpty() {
        return _size == 0;
    }
    public int size() {
        return _size;
    }
    //------------------------------------------------------//
    private int move(int index) {
        int i = index % items.length;
        return (index >= 0) ? (i) : (i + items.length);
    }
    private void doubleSize() {
        Item[] temp = (Item[]) new Object[items.length * 2];
        System.arraycopy(items, move(nextFirst + 1), temp, 1, _size - nextFirst);
        System.arraycopy(items, 0, temp, 1 + _size - nextFirst, nextFirst);
        items = temp;
        nextFirst = 0;
        nextLast = _size + 1;
    }
    private void smallerSize() {
        Item[] temp = (Item[]) new Object[items.length / 2];
        int i, j;
        for (i = move(nextFirst + 1), j = 1; i != nextLast; i = move(i + 1), j += 1) {
            temp[j] = items[i];
        }
        items = temp;
        nextFirst = 0;
        nextLast = j;
    }
    private boolean needSmall() {
        return items.length > 16 && (double) _size / items.length < 0.26;
    }
    private int _size;
    private int nextFirst;
    private int nextLast;
    private Item[] items;
}
