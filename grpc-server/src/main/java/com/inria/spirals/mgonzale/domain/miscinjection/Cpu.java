package com.inria.spirals.mgonzale.domain.miscinjection;

import com.inria.spirals.mgonzale.domain.*;
import com.inria.spirals.mgonzale.support.*;
import com.inria.spirals.mgonzale.model.*;
import com.inria.spirals.mgonzale.model.injections.*;
import com.inria.spirals.mgonzale.grpc.lib.*;
import org.slf4j.*;

public class Cpu implements Injectable
{
    private static final Logger LOG = LoggerFactory.getLogger(Cpu.class);
    private static final int MAX_RETRY = 1;
    
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
            return Util.runScriptPrintRetry(1, "/scripts/inject.sh", "-b", injection.getAmount());
        }
        catch (Exception e) {
            Cpu.LOG.error("Exception", e);
            return false;
        }
    }
    
    private boolean stop(final SigStop injection) {
        try {
            Cpu.LOG.info("Stopping SIGSTOP injection {}", injection);
            return Util.runScriptPrintRetry(1, "/scripts/inject.sh", "-s", injection.getPid());
        }
        catch (Exception e) {
            Cpu.LOG.error("Exception", e);
            return false;
        }
    }
    
    private boolean unburn(final BurnCPU injection) {
        try {
            Cpu.LOG.info("Stopping BurnCPU injection {}", injection);
            return Util.runScriptPrintRetry(1, "/scripts/inject.sh", "--cleancpu");
        }
        catch (Exception e) {
            Cpu.LOG.error("Exception", e);
            return false;
        }
    }
    
    private boolean unstop(final SigStop injection) {
        try {
            Cpu.LOG.info("Stopping SIGSTOP injection");
            return Util.runScriptPrintRetry(1, "/scripts/inject.sh", "-c ", injection.getPid());
        }
        catch (Exception e) {
            Cpu.LOG.error("Exception", e);
            return false;
        }
    }
    
}
