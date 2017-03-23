package com.inria.spirals.mgonzale.model.injections;


import com.inria.spirals.mgonzale.model.*;
import com.inria.spirals.mgonzale.grpc.lib.*;
import java.util.*;
import org.slf4j.*;

public class Loss extends Injection {
	
	private static final Logger LOG = LoggerFactory.getLogger(Loss.class);
    private final long amount;
    private final int sleep;
    
    public Loss(final Faultinjection.Injection injection) {
        super(injection);
        this.amount = injection.getAmount();
        this.sleep = injection.getSleep();
    }
    
    public Loss(final Faultinjection.InjectionAction action, final long amount, final int sleep) {
        super(Faultinjection.InjectionType.LOSS, action);
        this.amount = amount;
        this.sleep = sleep;
    }
    
    public long getAmount(){
    	return this.amount;
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.amount, this.sleep);
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
        final Loss del = (Loss)o;
        return this.sleep == del.sleep && del.amount == this.amount;

    }
 @Override
    public Injection setAction(final Faultinjection.InjectionAction action) {
        return new Loss(action, this.amount, this.sleep);
    }
    
    @Override
    public String toString() {
        return "Loss{amount=" + this.amount +", sleep="+this.sleep + "} " + super.toString();
    }
    
    @Override
    protected Faultinjection.Injection.Builder build(final Faultinjection.Injection.Builder builder) {
        return builder.setAmount(this.amount).setSleep(this.sleep);
    }
	
}
