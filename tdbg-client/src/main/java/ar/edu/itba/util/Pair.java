package ar.edu.itba.util;

public class Pair<T, K>{

    private T left;
    private K right;

    public Pair(T left, K right) {
        this.left = left;
        this.right = right;
    }

    public T getLeft() {
        return this.left;
    }

    public K getRight() {
        return this.right;
    }

    @Override
    public int hashCode() {
        return this.left.hashCode() * 31 + this.right.hashCode() * 37;
    }

    @Override
    public boolean equals(Object o) {
        if (! (o instanceof Pair)) {
            return false;
        }
        if (o == this) return true;
        Pair<T, K> obj = (Pair<T, K>) o;
        return this.left.equals(obj.left) && this.right.equals(obj.right);
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append("[")
                .append(left)
                .append("-")
                .append(right)
                .append("]")
                .toString();
    }
}
