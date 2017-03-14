package com.inria.spirals.mgonzale.model.injections;


import com.inria.spirals.mgonzale.model.*;
import com.inria.spirals.mgonzale.grpc.lib.*;

import java.util.*;
import org.slf4j.*;

public class Suicide extends Injection
{
    private static final Logger LOG;
    
    public Suicide(final Faultinjection.Injection injection) {
        super(injection);
    }
    
    public Suicide(final Faultinjection.InjectionAction action) {
        super(Faultinjection.InjectionType.SUICIDE, action);
    }
    
    @Override
    public Injection setAction(final Faultinjection.InjectionAction action) {
        return new Suicide(action);
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
        return "Suicide{} " + super.toString();
    }
    
    @Override
    protected Faultinjection.Injection.Builder build(final Faultinjection.Injection.Builder builder) {
        return builder;
    }
    
    static {
        LOG = LoggerFactory.getLogger(Suicide.class);
    }
}
