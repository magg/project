package com.inria.spirals.mgonzale.model.injections;


import com.inria.spirals.mgonzale.model.*;
import com.inria.spirals.mgonzale.grpc.lib.*;
import java.util.*;
import org.slf4j.*;
import com.inria.spirals.mgonzale.util.ArgumentChecker;


public class DiskFail extends DiskInjectionBase
{
    private static final Logger LOG;
    private final int probability;
    private final int error;
    
    public DiskFail(final Faultinjection.InjectionAction action, final String path, final String accessMode, final int probability, final int error) {
        super(Faultinjection.InjectionType.DFAIL, action, path, accessMode);
        this.probability = probability;
        this.error = error;
        this.checkArguments();
    }
    
    public DiskFail(final Faultinjection.Injection injection) {
        super(injection);
        this.probability = injection.getProbability();
        this.error = injection.getErrorCode();
    }
    
    public int getError() {
        return this.error;
    }
    
    public int getProbability() {
        return this.probability;
    }
    
    @Override
    public Injection setAction(final Faultinjection.InjectionAction action) {
        return new DiskFail(action, this.getPath(), this.getAccessMode(), this.probability, this.error);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.probability, this.error);
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DiskFail)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        final DiskFail diskFail = (DiskFail)o;
        return this.probability == diskFail.probability && this.error == diskFail.error;
    }
    
    @Override
    public String toString() {
        return "DiskFail{probability=" + this.probability + ", error=" + this.error + "} " + super.toString();
    }
    
    private void checkArguments() {
        Objects.requireNonNull(this.getPath(), "Target file/directory not defined.");
        ArgumentChecker.checkArgument(this.probability > 0 && this.probability <= 100, "Prob parameter value should be between 1 to 100.");
        this.checkFailCode();
    }
    
    private void checkFailCode() {
    }
    
    static {
        LOG = LoggerFactory.getLogger(DiskFail.class);
    }
}
