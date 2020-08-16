import java.util.HashSet;

public class ArrayHashing {
    public static void main(String[] args) {
        Integer[] arr1 = {1,2,3};
        Integer[] arr2 = {1,2,3};

        HashSet<Integer[]> set = new HashSet<>();
        set.add(arr1);
        set.add(arr2);

        if (set.size() != 1) {
            throw new BensAnIdiotException("array.hashCode is inherited from Object");
        }
    }

    public static class BensAnIdiotException extends RuntimeException {
        public BensAnIdiotException(String msg) {
            super("Ben's an idiot: " + msg);
        }
    }
}
