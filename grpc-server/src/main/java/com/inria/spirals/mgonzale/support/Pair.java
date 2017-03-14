package com.inria.spirals.mgonzale.support;

import java.util.*;

public class Pair<L, R>
{
    private final L l;
    private final R r;
    
    public Pair(final L l, final R r) {
        this.l = l;
        this.r = r;
    }
    
    public L getL() {
        return this.l;
    }
    
    public R getR() {
        return this.r;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(this.l, this.r);
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final Pair<?, ?> pair = (Pair<?, ?>)o;
        return Objects.equals(this.l, pair.l) && Objects.equals(this.r, pair.r);
    }
    
    @Override
    public String toString() {
        return "Pair{l=" + this.l + ", r=" + this.r + '}';
    }
}
