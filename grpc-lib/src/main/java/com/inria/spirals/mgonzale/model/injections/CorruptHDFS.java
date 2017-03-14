package com.inria.spirals.mgonzale.model.injections;


import com.inria.spirals.mgonzale.model.*;
import com.inria.spirals.mgonzale.util.ArgumentChecker;

import com.inria.spirals.mgonzale.grpc.lib.*;
import java.util.*;
import org.slf4j.*;

public class CorruptHDFS extends Injection
{
    private static final Logger LOG;
    private final long size;
    private final long offset;
    
    public CorruptHDFS(final Faultinjection.Injection injection) {
        super(injection);
        this.size = injection.getSize();
        this.offset = injection.getOffset();
        this.checkArguments();
    }
    
    public CorruptHDFS(final Faultinjection.InjectionAction action, final long size, final long offset) {
        super(Faultinjection.InjectionType.CORRUPTHDFS, action);
        this.size = size;
        this.offset = offset;
        this.checkArguments();
    }
    
    public long getOffset() {
        return this.offset;
    }
    
    public long getSize() {
        return this.size;
    }
    
    @Override
    public Injection setAction(final Faultinjection.InjectionAction action) {
        return new CorruptHDFS(action, this.size, this.offset);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.size, this.offset);
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        final CorruptHDFS that = (CorruptHDFS)o;
        return this.size == that.size && this.offset == that.offset;
    }
    
    @Override
    public String toString() {
        return "CorruptHDFS{size=" + this.size + ", offset=" + this.offset + "} " + super.toString();
    }
    
    @Override
    protected Faultinjection.Injection.Builder build(final Faultinjection.Injection.Builder builder) {
        return builder.setSize(this.size).setOffset(this.offset);
    }
    
    private void checkArguments() {
        ArgumentChecker.checkArgument(this.size > 0L, "Unexpected value for size.");
        ArgumentChecker.checkArgument(this.offset > 0L, "Unexpected value for offset");
    }
    
    static {
        LOG = LoggerFactory.getLogger(CorruptHDFS.class);
    }
}
