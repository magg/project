package com.inria.spirals.mgonzale.model.injections;


import com.inria.spirals.mgonzale.model.*;
import com.inria.spirals.mgonzale.grpc.lib.*;
import com.inria.spirals.mgonzale.util.ArgumentChecker;


import java.util.*;
import org.slf4j.*;

public class FillDisk extends Injection
{
    private static final Logger LOG;
    private final long amount;
    private final String mountPoint;
    
    public FillDisk(final Faultinjection.Injection injection) {
        super(injection);
        this.amount = injection.getAmount();
        this.mountPoint = injection.getMount();
    }
    
    public FillDisk(final Faultinjection.InjectionAction action, final long amount, final String mountPoint) {
        super(Faultinjection.InjectionType.FILLDISK, action);
        this.amount = amount;
        this.mountPoint = mountPoint;
    }
    
    public long getAmount() {
        return this.amount;
    }
    
    public String getMountPoint() {
        return this.mountPoint;
    }
    
    @Override
    public Injection setAction(final Faultinjection.InjectionAction action) {
        return new FillDisk(action, this.amount, this.mountPoint);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.amount, this.mountPoint);
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
        final FillDisk fillDisk = (FillDisk)o;
        return this.amount == fillDisk.amount && Objects.equals(this.mountPoint, fillDisk.mountPoint);
    }
    
    @Override
    public String toString() {
        return "FillDisk{amount=" + this.amount + ", mountPoint='" + this.mountPoint + '\'' + "} " + super.toString();
    }
    
    @Override
    protected Faultinjection.Injection.Builder build(final Faultinjection.Injection.Builder builder) {
        return builder.setAmount(this.amount).setMount(this.mountPoint);
    }
    
    private void checkArguments() {
        Objects.requireNonNull(this.mountPoint, "Mounting point specified is unacceptably null.");
        ArgumentChecker.checkArgument(this.mountPoint.startsWith("."), "mount does not seem a mounting point.");
        ArgumentChecker.checkArgument(this.amount > 0L && this.amount <= 100L, "Amount parameter value should be between 1 to 100.");
    }
    
    static {
        LOG = LoggerFactory.getLogger(FillDisk.class);
    }
}
