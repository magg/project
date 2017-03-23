package com.inria.spirals.mgonzale.model.injections;


import com.inria.spirals.mgonzale.model.*;
import com.inria.spirals.mgonzale.grpc.lib.*;

import java.util.*;
import org.slf4j.*;

public class Reorder extends Injection {
	
	 private static final Logger LOG = LoggerFactory.getLogger(Reorder.class);
	    private final long amount;
	    private final int sleep;
	    
	    public Reorder(final Faultinjection.Injection injection) {
	        super(injection);
	        this.amount = injection.getAmount();
	        this.sleep = injection.getSleep();
	    }
	    
	    public Reorder(final Faultinjection.InjectionAction action, final long amount, final int sleep) {
	        super(Faultinjection.InjectionType.REORDER, action);
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
	        final Reorder del = (Reorder)o;
	        return this.sleep == del.sleep && del.amount == this.amount;
	    }
	
	 @Override
	    public Injection setAction(final Faultinjection.InjectionAction action) {
	        return new Reorder(action, this.amount, this.sleep);
	    }
	    
	    @Override
	    public String toString() {
	        return "Reorder{amount=" + this.amount +", sleep="+this.sleep + "} " + super.toString();
	    }
	    
	    @Override
	    protected Faultinjection.Injection.Builder build(final Faultinjection.Injection.Builder builder) {
	        return builder.setAmount(this.amount).setSleep(this.sleep);
	    }
	
}
