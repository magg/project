package com.inria.spirals.mgonzale.model.injections;


import com.inria.spirals.mgonzale.model.*;
import com.inria.spirals.mgonzale.grpc.lib.*;
import com.inria.spirals.mgonzale.util.ArgumentChecker;

import java.util.Objects;

import org.slf4j.*;

public class Delete extends Injection
{
    private static final Logger LOG = LoggerFactory.getLogger(Delete.class);
    private final String path;
    private final int sleep;

    
    public Delete(final Faultinjection.Injection injection) {
        super(injection);
        this.path = injection.getPath();
        this.sleep = injection.getSleep();
        this.checkArguments();
    }
    
    public Delete(final Faultinjection.InjectionAction action, final String path, final int sleep) {
        super(Faultinjection.InjectionType.DELETE, action);
        this.path = path;
        this.sleep = sleep;
        this.checkArguments();
    }
    
    public String getPath() {
        return this.path;
    }
    
    @Override
    public Injection setAction(final Faultinjection.InjectionAction action) {
        return new Delete(action, this.path, this.sleep);
    }
    
    @Override
    public String toString() {
        return "Delete{path=" + this.path +", sleep="+this.sleep + "} " + super.toString();
    }
    
    @Override
    protected Faultinjection.Injection.Builder build(final Faultinjection.Injection.Builder builder) {
        return builder.setPath(this.path);
    }
    
    
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.path, this.sleep);
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
        final Delete del = (Delete)o;
        return this.sleep == del.sleep && Objects.equals(this.path, del.path);
    }
    
    private void checkArguments() {
    	Objects.requireNonNull(this.getPath(), "Target file/directory not defined.");
    }
    

}

