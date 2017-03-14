package com.inria.spirals.mgonzale.model.injections;


import com.inria.spirals.mgonzale.model.*;
import com.inria.spirals.mgonzale.util.ArgumentChecker;
import com.inria.spirals.mgonzale.grpc.lib.*;

import java.util.*;
import org.slf4j.*;

public class UnMount extends Injection
{
    private static final Logger LOG;
    private final String mountPoint;
    
    public UnMount(final Faultinjection.Injection injection) {
        super(injection);
        this.mountPoint = injection.getMount();
        this.checkArguments();
    }
    
    public UnMount(final Faultinjection.InjectionAction action, final String mountPoint) {
        super(Faultinjection.InjectionType.UNMOUNT, action);
        this.mountPoint = mountPoint;
        this.checkArguments();
    }
    
    public String getMountPoint() {
        return this.mountPoint;
    }
    
    @Override
    public Injection setAction(final Faultinjection.InjectionAction action) {
        return new UnMount(action, this.mountPoint);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.mountPoint);
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
        final UnMount unMount = (UnMount)o;
        return Objects.equals(this.mountPoint, unMount.mountPoint);
    }
    
    @Override
    public String toString() {
        return "UnMount{mountPoint='" + this.mountPoint + '\'' + "} " + super.toString();
    }
    
    @Override
    protected Faultinjection.Injection.Builder build(final Faultinjection.Injection.Builder builder) {
        return builder.setMount(this.mountPoint);
    }
    
    private void checkArguments() {
        Objects.requireNonNull(this.mountPoint, "Mounting point specified is unacceptably null.");
        ArgumentChecker.checkArgument(this.mountPoint.startsWith("."), "mount does not seem a mounting point.");
    }
    
    static {
        LOG = LoggerFactory.getLogger(UnMount.class);
    }
}
