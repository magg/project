package com.inria.spirals.mgonzale.domain.miscinjection;

import com.inria.spirals.mgonzale.domain.*;
import com.inria.spirals.mgonzale.support.*;
import com.inria.spirals.mgonzale.model.*;
import com.inria.spirals.mgonzale.model.injections.*;
import com.inria.spirals.mgonzale.grpc.lib.*;

import java.io.IOException;

import org.apache.commons.logging.*;
import org.springframework.core.io.ClassPathResource;

public class Memory implements Injectable
{
    private static final int MAX_RETRY = 1;
    private static final Log log;
    
    
    public String getFilePath(){
    	String path = null;
		try {

		path =  new ClassPathResource("scripts/inject.sh").getFile().getAbsolutePath();
			
		} catch (IOException  e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return path;
    	
    }
    
    @Override
    public boolean onStart(final Injection injection) {
        return this.fill((FillMem)injection);
    }
    
    @Override
    public boolean onStop(final Injection injection) {
        return this.unfill();
    }
    
    private boolean fill(final FillMem injection) {
        try {
            final long amount = injection.getAmount();
            Memory.log.info("Starting FILLMEM injection");
            return Util.runScriptPrintRetry(1, getFilePath(), "-m", Long.toString(amount));
        }
        catch (Exception e) {
            Memory.log.error(e);
            return false;
        }
    }
    
    private boolean unfill() {
        try {
            Memory.log.info("Stopping FILLMEM injection");
            return Util.runScriptPrintRetry(1, getFilePath(), "--cleanmem");
        }
        catch (Exception e) {
            Memory.log.error(e);
            return false;
        }
    }
    
    static {
        log = LogFactory.getLog(Memory.class);
    }
}
