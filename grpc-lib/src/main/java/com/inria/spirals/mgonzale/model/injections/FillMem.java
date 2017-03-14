package com.inria.spirals.mgonzale.model.injections;


import com.inria.spirals.mgonzale.model.*;
import com.inria.spirals.mgonzale.util.ArgumentChecker;
import com.inria.spirals.mgonzale.grpc.lib.*;


import java.util.*;
import org.slf4j.*;

public class FillMem extends Injection
{
    private static final Logger LOG;
    private final long amount;
    
    public FillMem(final Faultinjection.Injection injection) {
        super(injection);
        this.amount = injection.getAmount();
        ArgumentChecker.checkArgument(this.amount > 0L && this.amount <= 100L, "Amount parameter value should be between 1 to 100.");
    }
    
    public FillMem(final Faultinjection.InjectionAction action, final long amount) {
        super(Faultinjection.InjectionType.FILLMEM, action);
        this.amount = amount;
        ArgumentChecker.checkArgument(amount > 0L && amount <= 100L, "Amount parameter value should be between 1 to 100.");
    }
    
    public long getAmount() {
        return this.amount;
    }
    
    @Override
    public Injection setAction(final Faultinjection.InjectionAction action) {
        return new FillMem(action, this.amount);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.amount);
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
        final FillMem fillMem = (FillMem)o;
        return this.amount == fillMem.amount;
    }
    
    @Override
    public String toString() {
        return "FillMem{amount=" + this.amount + "} " + super.toString();
    }
    
    @Override
    protected Faultinjection.Injection.Builder build(final Faultinjection.Injection.Builder builder) {
        return builder.setAmount(this.amount);
    }
    
    static {
        LOG = LoggerFactory.getLogger(FillMem.class);
    }
}
