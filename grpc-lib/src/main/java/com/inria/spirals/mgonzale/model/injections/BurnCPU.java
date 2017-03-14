package com.inria.spirals.mgonzale.model.injections;


import com.inria.spirals.mgonzale.model.*;
import com.inria.spirals.mgonzale.grpc.lib.*;
import com.inria.spirals.mgonzale.util.ArgumentChecker;


import org.slf4j.*;

public class BurnCPU extends Injection
{
    private static final Logger LOG;
    private final long amount;
    
    public BurnCPU(final Faultinjection.Injection injection) {
        super(injection);
        this.amount = injection.getAmount();
        this.checkArguments();
    }
    
    public BurnCPU(final Faultinjection.InjectionAction action, final long amount) {
        super(Faultinjection.InjectionType.BURNCPU, action);
        this.amount = amount;
        this.checkArguments();
    }
    
    public long getAmount() {
        return this.amount;
    }
    
    @Override
    public Injection setAction(final Faultinjection.InjectionAction action) {
        return new BurnCPU(action, this.amount);
    }
    
    @Override
    public String toString() {
        return "BurnCPU{amount=" + this.amount + "} " + super.toString();
    }
    
    @Override
    protected Faultinjection.Injection.Builder build(final Faultinjection.Injection.Builder builder) {
        return builder.setAmount(this.amount);
    }
    
    private void checkArguments() {
        ArgumentChecker.checkArgument(this.amount >= 1L && this.amount <= 200L, "Amount parameter value should be between 1 to 200.");
    }
    
    static {
        LOG = LoggerFactory.getLogger(BurnCPU.class);
    }
}
