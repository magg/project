package com.inria.spirals.mgonzale.model.injections;


import com.inria.spirals.mgonzale.model.*;
import com.inria.spirals.mgonzale.grpc.lib.*;

import java.util.*;
import org.slf4j.*;

public class Hang extends Injection
{
    private static final Logger LOG;
    
    public Hang(final Faultinjection.Injection injection) {
        super(injection);
    }
    
    public Hang(final Faultinjection.InjectionAction action) {
        super(Faultinjection.InjectionType.HANG, action);
    }
    
    @Override
    public Injection setAction(final Faultinjection.InjectionAction action) {
        return new Hang(action);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode());
    }
    
    @Override
    public boolean equals(final Object o) {
        return this == o || (o != null && this.getClass() == o.getClass() && super.equals(o));
    }
    
    @Override
    public String toString() {
        return "Hang{} " + super.toString();
    }
    
    @Override
    protected Faultinjection.Injection.Builder build(final Faultinjection.Injection.Builder builder) {
        return builder;
    }
    
    static {
        LOG = LoggerFactory.getLogger(Hang.class);
    }
}
