package com.inria.spirals.mgonzale.domain.miscinjection;

import com.inria.spirals.mgonzale.domain.*;
import com.inria.spirals.mgonzale.support.*;
import com.inria.spirals.mgonzale.model.*;
import com.inria.spirals.mgonzale.model.injections.*;
import com.inria.spirals.mgonzale.grpc.lib.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.stream.Stream;

import org.slf4j.*;
import org.springframework.core.io.ClassPathResource;

public class Cpu implements Injectable
{
    private static final Logger LOG = LoggerFactory.getLogger(Cpu.class);
    private static final int MAX_RETRY = 1;
        
    
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
        try {
            switch (injection.getInjection()) {
                case BURNCPU: {
                    return this.burn((BurnCPU)injection);
                }
                case SIGSTOP: {
                    return this.stop((SigStop)injection);
                }
                default: {
                    throw new IllegalArgumentException("Do not know how to handle " + injection.getInjection());
                }
            }
        }
        catch (Exception e) {
            Cpu.LOG.error("Exception", e);
            return false;
        }
    }
    
    @Override
    public boolean onStop(final Injection injection) {
        try {
            switch (injection.getInjection()) {
                case BURNCPU: {
                    return this.unburn((BurnCPU)injection);
                }
                case SIGSTOP: {
                    return this.unstop((SigStop)injection);
                }
                default: {
                    throw new IllegalArgumentException("Do not know how to handle " + injection.getInjection());
                }
            }
        }
        catch (Exception e) {
            Cpu.LOG.error("Exception", e);
            return false;
        }
    }
    
    private boolean burn(final BurnCPU injection) {
        try {
            Cpu.LOG.info("Starting BurnCPU injection {}", injection);
            return Util.runScriptPrintRetry(1, getFilePath(), "-b", injection.getAmount());
        }
        catch (Exception e) {
            Cpu.LOG.error("Exception", e);
            return false;
        }
    }
    
    private boolean stop(final SigStop injection) {
        try {
            Cpu.LOG.info("Stopping SIGSTOP injection {}", injection);
            return Util.runScriptPrintRetry(1, getFilePath(), "-s", injection.getPid());
        }
        catch (Exception e) {
            Cpu.LOG.error("Exception", e);
            return false;
        }
    }
    
    private boolean unburn(final BurnCPU injection) {
        try {
            Cpu.LOG.info("Stopping BurnCPU injection {}", injection);
            return Util.runScriptPrintRetry(1, getFilePath(), "--cleancpu");
        }
        catch (Exception e) {
            Cpu.LOG.error("Exception", e);
            return false;
        }
    }
    
    private boolean unstop(final SigStop injection) {
        try {
            Cpu.LOG.info("Stopping SIGSTOP injection");
            return Util.runScriptPrintRetry(1, getFilePath(), "-c ", injection.getPid());
        }
        catch (Exception e) {
            Cpu.LOG.error("Exception", e);
            return false;
        }
    }
    
}
