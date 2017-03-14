package com.inria.spirals.mgonzale.model.injections;

import com.inria.spirals.mgonzale.model.*;
import com.inria.spirals.mgonzale.grpc.lib.*;
import com.inria.spirals.mgonzale.util.ArgumentChecker;


import java.util.*;
import org.slf4j.*;

public class DiskDelay extends DiskInjectionBase
{
    private static final Logger LOG;
    private final long delay;
    private final int probability;
    
    public DiskDelay(final Faultinjection.InjectionAction action, final String path, final String accessMode, final long delay, final int probability) {
        super(Faultinjection.InjectionType.DDELAY, action, path, accessMode);
        this.delay = delay;
        this.probability = probability;
    }
    
    public DiskDelay(final Faultinjection.Injection injection) {
        super(injection);
        this.delay = injection.getDealy();
        this.probability = injection.getProbability();
    }
    
    public long getDelay() {
        return this.delay;
    }
    
    public int getProbability() {
        return this.probability;
    }
    
    @Override
    public String toString() {
        return "DiskDelay{delay=" + this.delay + ", probability=" + this.probability + "} " + super.toString();
    }
    
    @Override
    public Injection setAction(final Faultinjection.InjectionAction action) {
        return new DiskDelay(action, this.getPath(), this.getAccessMode(), this.delay, this.probability);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.delay, this.probability);
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DiskDelay)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        final DiskDelay diskDelay = (DiskDelay)o;
        return this.delay == diskDelay.delay && this.probability == diskDelay.probability;
    }
    
    private void checkArguments() {
        Objects.requireNonNull(this.getPath(), "Target file/directory not defined.");
        ArgumentChecker.checkArgument(this.probability > 0 && this.probability <= 100, "Prob parameter value should be between 1 to 100.");
    }
    
    static {
        LOG = LoggerFactory.getLogger(DiskDelay.class);
    }
}
