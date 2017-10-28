public class OffByN implements CharacterComparator {
    public OffByN(int n) {
        N = n;
    }

    @Override
    public boolean equalChars(char x, char y) {
        if (x - y == N || y - x == N) {
            return true;
        }
        return false;
    }

    private int N;
}
