/* This file is part of VoltDB.
 * Copyright (C) 2008-2010 VoltDB L.L.C.
 *
 * VoltDB is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * VoltDB is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with VoltDB.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.oltpbenchmark.util;

/**
 * Class representing a triple of generic-ized types. Supports equality, hashing
 * and all that other nice Java stuff. Based on the pair class in from oltpbench/util.
 *
 */
public class Triple<T, U, M> implements Comparable<Triple<T, U, M>> {

    public final T left;
    public final U middle;
    public final M right;
    private transient Integer hash;

    public Triple(T left, U middle, M right, boolean precomputeHash) {
        this.left = left;
        this.middle = middle;
        this.right = right;
        hash = (precomputeHash ? this.computeHashCode() : null);
    }

    public Triple(T left, U middle, M right) {
        this(left, middle, right, true);
    }

    private int computeHashCode() {
        return (left == null ? 0 : left.hashCode() * 31) +
               (middle == null ? 0 : middle.hashCode()) +
               (right == null ? 0 : right.hashCode() * 63);
    }

    public int hashCode() {
        if (hash != null) return (hash.intValue());
        return (this.computeHashCode());
    }

    public String toString() {
        return String.format("<%s, %s, %s>", left, middle, right);
    }

    @Override
    public int compareTo(Triple<T, U, M> other) {
        return (other.hash - this.hash);
    }

    public Object get(int idx) {
        if (idx == 0) return left;
        else if (idx == 1) return middle;
        else if (idx == 2) return right;
        return null;
    }

    /**
     * @param o Object to compare to.
     * @return Is the object equal to a value in the triple.
     */
    public boolean contains(Object o) {
        if ((left != null) && (left.equals(o))) return true;
        if ((middle != null) && (middle.equals(o))) return true;
        if ((right != null) && (right.equals(o))) return true;
        if (o != null) return false;
        return ((left == null) || (middle == null) || (right == null));
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || !(o instanceof Triple)) {
            return false;
        }

        @SuppressWarnings("unchecked")
        Triple<T, U, M> other = (Triple<T, U, M>) o;

        return (left == null ? other.left == null : left.equals(other.left))
                && (middle == null ? other.middle == null : middle.equals(other.middle))
                && (right == null ? other.right == null : right.equals(other.right));
    }

    /**
     * Convenience class method for constructing triples using Java's generic type
     * inference.
     */
    public static <T, U, M> Triple<T, U, M> of(T x, U y, M z) {
        return new Triple<T, U, M>(x, y, z);
    }
}
