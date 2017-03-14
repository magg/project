package com.inria.spirals.mgonzale.domain.netinjection;

import com.inria.spirals.mgonzale.domain.*;
import com.inria.spirals.mgonzale.support.*;
import com.inria.spirals.mgonzale.model.*;
import com.inria.spirals.mgonzale.model.injections.*;
import com.inria.spirals.mgonzale.grpc.lib.*;
import java.util.*;
import org.apache.commons.logging.*;

public class DoS implements Injectable
{
    private static final Log log;
    private static final ArrayList<Thread> RUNNING_THREADS;
    private static final int THREADS = 8;
    private int counter;
    
    public DoS() {
        this.counter = 8;
    }
    
    @Override
    public boolean onStart(final Injection injection) {
        return this.startDoS((Flood)injection);
    }
    
    @Override
    public boolean onStop(final Injection injection) {
        return this.stopDoS();
    }
    
    private boolean startDoS(final Flood flood) {
        try {
            final int port = flood.getPort();
            synchronized (DoS.RUNNING_THREADS) {
                if (!DoS.RUNNING_THREADS.isEmpty()) {
                    DoS.log.error("It seems that another DoS is already running.");
                    return false;
                }
                DoS.log.info("Starting DoS attack with " + this.counter + " clients.");
                for (int counter = 0; counter < 8; ++counter) {
                    DoS.RUNNING_THREADS.add(new Thread(new DoSRunner(port)));
                    DoS.RUNNING_THREADS.get(counter).start();
                }
            }
            DoS.log.info("All clients started.");
            return true;
        }
        catch (Exception e) {
            DoS.log.error(e);
            return false;
        }
    }
    
    private boolean stopDoS() {
        synchronized (DoS.RUNNING_THREADS) {
            if (DoS.RUNNING_THREADS.size() < this.counter) {
                DoS.log.error("Unexpected number of thread executing the Dos");
                return false;
            }
            DoS.log.info("Stopping DoS attack.");
            while (!DoS.RUNNING_THREADS.isEmpty()) {
                DoS.RUNNING_THREADS.remove(0).interrupt();
                --this.counter;
            }
        }
        return true;
    }
    
    static {
        log = LogFactory.getLog(DoS.class);
        RUNNING_THREADS = new ArrayList<Thread>();
    }
}
