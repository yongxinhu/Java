public class LinkedListDeque<Item> {
    private class DNode {
        public DNode(Item i) {
            this.prev = null;
            this.next = null;
            item = i;
        }
        public DNode(Item i, DNode prev, DNode next) {
            this.prev = prev;
            this.next = next;
            item = i;
        }
        private DNode prev;
        private Item item;
        private DNode next;
    }

    public LinkedListDeque() {
        _size = 0;
        sentinel = new DNode(null);
        sentinel.next = sentinel;
        sentinel.prev = sentinel;
    }
    public void addFirst(Item it) {
        sentinel.next = new DNode(it, sentinel, sentinel.next);
        sentinel.next.next.prev = sentinel.next;
        _size += 1;
    }
    public void addLast(Item it) {
        sentinel.prev = new DNode(it, sentinel.prev, sentinel);
        sentinel.prev.prev.next = sentinel.prev;
        _size += 1;
    }
    public Item removeFirst() {
        if (isEmpty()) {
            return null;
        }
        DNode r = sentinel.next;
        sentinel.next = r.next;
        r.next.prev = sentinel;
        _size -= 1;
        return r.item;
    }
    public Item removeLast() {
        if (isEmpty()) {
            return null;
        }
        DNode r = sentinel.prev;
        sentinel.prev = r.prev;
        r.prev.next = sentinel;
        _size -= 1;
        return r.item;
    }
    public void printDeque() {
        for (DNode p = sentinel.next; p != sentinel; p = p.next) {
            System.out.print(p.item);
            System.out.print(" ");
        }
        System.out.println();
    }
    public Item get(int index) {
        if (index < 0 || index >= _size) {
            return null;
        }
        DNode p;
        if (index < _size / 2) {
            p = sentinel.next;
            while (index > 0) {
                p = p.next;
                index -= 1;
            }
        } else {
            p = sentinel.prev;
            while (index != _size - 1) {
                p = p.prev;
                index += 1;
            }
        }
        return p.item;
    }
    public Item getRecursive(int index) {
        if (index < 0 || index >= _size) {
            return null;
        }
        return getRecursiveHelper(sentinel.next, index);
    }
    private Item getRecursiveHelper(DNode node, int index) {
        if (node == sentinel) {
            return null;
        }
        if (index == 0) {
            return node.item;
        }
        return getRecursiveHelper(node.next, index - 1);
    }
    public boolean isEmpty() {
        return _size == 0;
    }
    public int size() {
        return _size;
    }
    private int _size;
    private DNode sentinel;
}
