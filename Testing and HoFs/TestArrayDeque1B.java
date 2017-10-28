import static org.junit.Assert.*;

import org.junit.Test;


public class TestArrayDeque1B {
    @Test
    public void testAD() {
        StudentArrayDeque<Integer> sad = new StudentArrayDeque<>();
        ArrayDequeSolution<Integer> solution = new ArrayDequeSolution<>();
        OperationSequence fs = new OperationSequence();
        for (int i = 0; i < 100; i += 1) {
            int number = StdRandom.uniform(100);
            if (StdRandom.uniform() < 0.5) {
                fs.addOperation(new DequeOperation("addFirst", number));
                sad.addFirst(number);
                solution.addFirst(number);
            } else {
                fs.addOperation(new DequeOperation("addLast", number));
                sad.addLast(number);
                solution.addLast(number);
            }
        }
        for (int i = 0; i < 90; i += 1) {
            Integer expected = 0, actual = 0;
            if (StdRandom.uniform() < 0.5) {
                fs.addOperation(new DequeOperation("removeFirst"));
                expected = solution.removeFirst();
                actual = sad.removeFirst();
            } else {
                fs.addOperation(new DequeOperation("removeLast"));
                expected = solution.removeLast();
                actual = sad.removeLast();
            }
            assertEquals(fs.toString(), expected, actual);
        }
    }
}
