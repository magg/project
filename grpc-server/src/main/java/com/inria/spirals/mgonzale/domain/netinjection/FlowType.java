package com.inria.spirals.mgonzale.domain.netinjection;

import com.inria.spirals.mgonzale.domain.*;
import com.inria.spirals.mgonzale.support.*;
import com.inria.spirals.mgonzale.model.*;
import com.inria.spirals.mgonzale.grpc.lib.*;

import java.util.concurrent.*;
import java.util.*;
import org.apache.commons.logging.*;

enum FlowType
{
    INPUT, 
    OUTPUT, 
    ALL;
    
    private static final Log log;
    private final List<Pair<Integer, String>> blockedList;
    
    private FlowType() {
        this.blockedList = new CopyOnWriteArrayList<Pair<Integer, String>>();
    }
    
    public Pair<Integer, String> getPairMatch(final Pair<Integer, String> match) {
        for (final Pair<Integer, String> pair : this.blockedList) {
            if (match.equals(pair) || pair.getL() == 0) {
                return pair;
            }
        }
        return null;
    }
    
    public Pair<Integer, String> getPairMatch(final int port) {
        for (final Pair<Integer, String> pair : this.blockedList) {
            if (port == pair.getL() || pair.getL() == 0) {
                return pair;
            }
        }
        return null;
    }
    
    public boolean checkAndAdd(final Pair<Integer, String> pair) {
        try {
            final Pair<Integer, String> cmpA = FlowType.ALL.getPairMatch(pair.getL());
            final Pair<Integer, String> cmpI = FlowType.INPUT.getPairMatch(pair.getL());
            final Pair<Integer, String> cmpO = FlowType.OUTPUT.getPairMatch(pair.getL());
            if (cmpA != null) {
                if (cmpA.getL() == 0) {
                    FlowType.log.error("Filter already present at this address.");
                }
                else {
                    FlowType.log.error("Filter already present on port: " + cmpA + ".");
                }
                return false;
            }
            if ((this == FlowType.ALL || this == FlowType.INPUT) && cmpI != null) {
                if (cmpI.getL() == 0) {
                    FlowType.log.error("Filter already present at this address.");
                }
                else {
                    FlowType.log.error("Filter already present on port: " + cmpI + ".");
                }
                return false;
            }
            if ((this == FlowType.ALL || this == FlowType.OUTPUT) && cmpO != null) {
                if (cmpO.getL() == 0) {
                    FlowType.log.error("Filter already present at this address.");
                }
                else {
                    FlowType.log.error("Filter already present on port: " + cmpO + ".");
                }
                return false;
            }
            if (pair.getL() == 0 && (FlowType.ALL.hasFilter() || (this == FlowType.INPUT && FlowType.INPUT.hasFilter()) || (this == FlowType.OUTPUT && FlowType.OUTPUT.hasFilter()))) {
                FlowType.log.error("Filter already present on some of the ports.");
                return false;
            }
            this.add(pair);
            return true;
        }
        catch (Exception e) {
            FlowType.log.error(e);
            return false;
        }
    }
    
    public boolean checkAndRemove(final Pair<Integer, String> pair) {
        try {
            final Pair<Integer, String> cmp = this.getPairMatch(pair);
            if (cmp == null) {
                FlowType.log.error("There is no matching filter on port: " + pair + ".");
                return false;
            }
            if (pair.equals(cmp)) {
                this.remove(pair);
                return true;
            }
        }
        catch (Exception e) {
            FlowType.log.error(e);
        }
        return false;
    }
    
    private boolean hasFilter() {
        return !this.blockedList.isEmpty();
    }
    
    private void add(final Pair<Integer, String> pair) {
        this.blockedList.add(pair);
    }
    
    private void remove(final Pair<Integer, String> pair) {
        this.blockedList.remove(pair);
    }
    
    public static void clear() {
        for (final FlowType ft : values()) {
            ft.blockedList.clear();
        }
    }
    
    public static FlowType fromDirection(final Faultinjection.Direction direction) {
        return valueOf(direction.name());
    }
    
    static {
        log = LogFactory.getLog(FlowType.class);
    }
}
