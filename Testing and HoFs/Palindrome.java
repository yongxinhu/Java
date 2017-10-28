public class Palindrome {
    public static Deque<Character> wordToDeque(String word) {
        Deque<Character> dword = new LinkedListDeque<>();
        for (int i = 0; i < word.length(); i++) {
            dword.addLast(word.charAt(i));
        }
        return dword;
    }

    public static boolean isPalindrome(String word) {
        if (word.length() < 2) {
            return true;
        }
        Deque<Character> dword = wordToDeque(word);
        if (dword.removeFirst() == dword.removeLast()) {
            return isPalindrome(word.substring(1, word.length() - 1));
        }
        return false;
    }

    public static boolean isPalindrome(String word, CharacterComparator cc) {
        if (word.length() < 2) {
            return true;
        }
        Deque<Character> dword = wordToDeque(word);
        if (cc.equalChars(dword.removeFirst(), dword.removeLast())) {
            return isPalindrome(word.substring(1, word.length() - 1), cc);
        }
        return false;
    }
    /*
    public static void main(String[] args) {
        isPalindrome("depend");
    }
    */

}
