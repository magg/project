package com.inria.spirals.mgonzale.domain.miscinjection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


import com.inria.spirals.mgonzale.domain.*;
import com.inria.spirals.mgonzale.support.*;
import com.inria.spirals.mgonzale.model.*;
import com.inria.spirals.mgonzale.model.injections.*;
import com.inria.spirals.mgonzale.grpc.lib.*;


public class DownInterface implements Injectable {
	
    private static final Log log = LogFactory.getLog(DownInterface.class);
    private static final int MAX_RETRY = 1;



	@Override
	public boolean onStart(Injection injection) {
		return down((Down)injection);
	}

	@Override
	public boolean onStop(Injection injection) {
		return up((Down)injection);
	}
	
	public boolean down(Down injection) {
        String iface = injection.getIface();
        log.info("Starting DOWN injection");
        return Util.runCmdPrintRetry(1, ("ifconfig "+iface+" down").split("\\s"));
	}
	
	public boolean up(Down injection){
		String iface = injection.getIface();
        log.info("Removing DOWN injection");
        return Util.runCmdPrintRetry(1, ("ifconfig "+ iface +" up").split("\\s"));

	}

}

