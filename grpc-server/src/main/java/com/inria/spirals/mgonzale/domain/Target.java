package com.inria.spirals.mgonzale.domain;


import com.inria.spirals.mgonzale.domain.miscinjection.*;
import com.inria.spirals.mgonzale.domain.netinjection.*;

import com.inria.spirals.mgonzale.support.*;
import com.inria.spirals.mgonzale.model.*;
import com.inria.spirals.mgonzale.grpc.lib.*;

import java.util.*;
import java.util.function.*;



public enum Target
{
    BLACK(Faultinjection.InjectionType.BLACK, (Injectable)new IpRoute()), 
    BURNCPU(Faultinjection.InjectionType.BURNCPU, (Injectable)new Cpu()), 
    BURNIO(Faultinjection.InjectionType.BURNIO, (Injectable)new Disk()), 
    DNSFAIL(Faultinjection.InjectionType.DNSFAIL, (Injectable)new IpTables()), 
    FILLDISK(Faultinjection.InjectionType.FILLDISK, (Injectable)new Disk()), 
    FILLMEM(Faultinjection.InjectionType.FILLMEM, (Injectable)new Memory()), 
    FLOOD(Faultinjection.InjectionType.FLOOD, (Injectable)new DoS()), 
    HANG(Faultinjection.InjectionType.HANG, (Injectable)new Misc()), 
    CORRUPTHDFS(Faultinjection.InjectionType.CORRUPTHDFS, (Injectable)new CorruptHDFS()), 
    PANIC(Faultinjection.InjectionType.PANIC, (Injectable)new Misc()), 
    DROP(Faultinjection.InjectionType.DROP, (Injectable)new IpTables()), 
    REJECT1(Faultinjection.InjectionType.REJECT1, (Injectable)new IpTables()), 
    REJECT2(Faultinjection.InjectionType.REJECT2, (Injectable)new IpTables()), 
    LOSS(Faultinjection.InjectionType.LOSS, (Injectable)new Tc()), 
    RONLY(Faultinjection.InjectionType.RONLY, (Injectable)new Disk()), 
    UNMOUNT(Faultinjection.InjectionType.UNMOUNT, (Injectable)new Disk()), 
    SIGSTOP(Faultinjection.InjectionType.SIGSTOP, (Injectable)new Cpu()), 
    SUICIDE(Faultinjection.InjectionType.SUICIDE, (Injectable)new Misc()), 
    CORRUPT(Faultinjection.InjectionType.CORRUPT, (Injectable)new Tc()), 
    REORDER(Faultinjection.InjectionType.REORDER, (Injectable)new Tc()), 
    DUPLICATE(Faultinjection.InjectionType.DUPLICATE, (Injectable)new Tc()), 
    DELAY(Faultinjection.InjectionType.DELAY, (Injectable)new Tc()), 
    LIMIT(Faultinjection.InjectionType.LIMIT, (Injectable)new Tc()), 
    DDELAY(Faultinjection.InjectionType.DDELAY, (Injectable)new Disk()), 
    DCORRUPT(Faultinjection.InjectionType.DCORRUPT, (Injectable)new Disk()), 
    DFAIL(Faultinjection.InjectionType.DFAIL, (Injectable)new Disk()),
    DELETE(Faultinjection.InjectionType.DELETE, (Injectable)new DeleteFiles()),
    DOWN(Faultinjection.InjectionType.DOWN, (Injectable)new DownInterface());


    
    private final Injectable injectable;
    private final Faultinjection.InjectionType type;
    
    private Target(final Faultinjection.InjectionType type, final Injectable injectable) {
        this.type = type;
        this.injectable = injectable;
    }
    
    public Faultinjection.InjectionType getInjectionType() {
        return this.type;
    }
    
    public static boolean handle(final Injection injection) throws Throwable {
    	System.out.println(injection.toString());
    	
        final Target target = Arrays.stream(values()).filter(v -> v.type == injection.getInjection()).findFirst().orElseThrow(IllegalArgumentException::new);
        switch (injection.getAction()) {
        	case START_WAIT_STOP:
            case START: {
                return target.injectable.onStart(injection);
            }
            case STOP: {
                return target.injectable.onStop(injection);
            }
            default: {
                throw new IllegalArgumentException("Unknown action " + injection.getAction());
            }
        }
    }
}
