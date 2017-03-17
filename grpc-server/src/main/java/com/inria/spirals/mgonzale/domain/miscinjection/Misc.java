package com.inria.spirals.mgonzale.domain.miscinjection;

import com.inria.spirals.mgonzale.domain.*;
import com.inria.spirals.mgonzale.support.*;
import com.inria.spirals.mgonzale.model.*;
import com.inria.spirals.mgonzale.grpc.lib.*;

import java.io.IOException;

import org.apache.commons.logging.*;
import org.springframework.core.io.ClassPathResource;

public class Misc implements Injectable
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
        switch (injection.getInjection()) {
            case HANG: {
                return this.hang();
            }
            case PANIC: {
                return this.panic();
            }
            case SUICIDE: {
                return this.suicide();
            }
            default: {
                throw new UnsupportedOperationException("Do not know how to handle " + injection.getInjection());
            }
        }
    }
    
    @Override
    public boolean onStop(final Injection injection) {
        switch (injection.getInjection()) {
            case HANG: {
                return this.unhang();
            }
            case PANIC:
            case SUICIDE: {
                throw new IllegalArgumentException("Should not happen");
            }
            default: {
                throw new UnsupportedOperationException("Do not know how to handle " + injection.getInjection());
            }
        }
    }
    
    private boolean hang() {
        try {
            Misc.log.info("Starting HANG injection");
            return Util.runScriptPrintRetry(1, getFilePath(), "-a");
        }
        catch (Exception e) {
            Misc.log.error(e);
            return false;
        }
    }
    
    private boolean unhang() {
        try {
            Misc.log.info("Stopping HANG injection");
            return Util.runScriptPrintRetry(1, getFilePath(), "--cleanhang");
        }
        catch (Exception e) {
            Misc.log.error(e);
            return false;
        }
    }
    
    private boolean suicide() {
        try {
            Misc.log.info("Starting SUICIDE injection");
            return Util.runScriptPrintRetry(1, "/bin/injectOnce4All.sh", "-s");
        }
        catch (Exception e) {
            Misc.log.error(e);
            return false;
        }
    }
    
    private boolean panic() {
        try {
            Misc.log.info("Starting PANIC injection");
            return Util.runScriptPrintRetry(1, "/bin/injectOnce4All.sh", "-p");
        }
        catch (Exception e) {
            Misc.log.error(e);
            return false;
        }
    }
    
    static {
        log = LogFactory.getLog(Misc.class);
    }
}
