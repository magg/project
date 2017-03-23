package com.inria.spirals.mgonzale.domain.miscinjection;

import org.springframework.beans.factory.annotation.Value;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


import com.inria.spirals.mgonzale.domain.*;
import com.inria.spirals.mgonzale.support.*;
import com.inria.spirals.mgonzale.model.*;
import com.inria.spirals.mgonzale.model.injections.*;
import com.inria.spirals.mgonzale.grpc.lib.*;

public class TC implements Injectable {
	
    private static final Log log = LogFactory.getLog(TC.class);

    @Value("${tc.interface}")
    private String dev;
    private boolean res = false;
	

	@Override
	public boolean onStart(Injection injection) {
		
		switch (injection.getInjection()) {
        case LOSS:
        	return loss ((Loss) injection);
        case CORRUPT:
        	return corrupt ((Corrupt) injection );
        case REORDER:
        	return reorder((Reorder) injection);
        case DUPLICATE:
        	return makeDups((Duplicate) injection);
        case DELAY:
        	return addLatency((Delay)injection);
        default: {
            throw new UnsupportedOperationException("Do not know how to handle " + injection.getInjection());
        }
}
	}

	@Override
	public boolean onStop(Injection injection) {
		// TODO Auto-generated method stub
        switch (injection.getInjection()) {
        case LOSS:
        case CORRUPT:
        case REORDER:
        case DUPLICATE:
        case DELAY: {
            //return removeFilter(new Pair<NetworkCorruptInjection, String>((NetworkCorruptInjection)injection, ""));
        	return clean();
        }
        default: {
            throw new UnsupportedOperationException("Do not know how to handle " + injection.getInjection());
        	}
        }
	}

	public boolean addLatency (Delay injection){
		log.info("Applying "+ injection.toString()+" injection to the host.");
		res = Util.runCmdPrintRetry(1, "tc qdisc add dev "+dev+" root netem delay "+injection.getAmount()+"ms");
		
        if (res == true) {
            log.info("Injection "+ injection.toString()+ " on the host started.");
            return true;
        } else {
        	return false;
        }
		
	}
	
	public boolean makeDups(Duplicate injection){
		log.info("Applying "+ injection.toString()+" injection to the host.");

		res = Util.runCmdPrintRetry(1, "tc qdisc add dev "+dev+" root netem duplicate "+injection.getAmount()+"%");
        if (res == true) {
            log.info("Injection "+ injection.toString()+ " on the host started.");
            return true;
        } else {
        	return false;
        }
	}
	
	public boolean reorder(Reorder injection){
		log.info("Applying "+ injection.toString()+" injection to the host.");

		res = Util.runCmdPrintRetry(1,"tc qdisc change dev "+dev+" root netem delay 10ms reorder "+injection.getAmount()+"% 50%");
        if (res == true) {
            log.info("Injection "+ injection.toString()+ " on the host started.");
            return true;
        } else {
        	return false;
        }
	}
	
	public boolean loss(Loss injection){
		log.info("Applying "+ injection.toString()+" injection to the host.");

		res = Util.runCmdPrintRetry(1,"tc qdisc add dev " + dev + " root netem loss " + injection.getAmount() + "%");
        if (res == true) {
            log.info("Injection "+ injection.toString()+ " on the host started.");
            return true;
        } else {
        	return false;
        }
	}
	
	public boolean corrupt(Corrupt injection){
		log.info("Applying "+ injection.toString()+" injection to the host.");

		res = Util.runCmdPrintRetry(1,"tc qdisc add dev " + dev + " root netem corrupt " + injection.getAmount() + "%");
        if (res == true) {
            log.info("Injection "+ injection.toString()+ " on the host started.");

            return true;
        } else {
        	return false;
        }
	}
	
	public boolean clean(){
		return Util.runCmdPrintRetry(1,"tc qdisc del dev "+dev+" root netem");
	}
}
