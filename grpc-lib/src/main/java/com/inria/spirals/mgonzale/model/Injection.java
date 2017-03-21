package com.inria.spirals.mgonzale.model;

import java.util.*;
import com.inria.spirals.mgonzale.grpc.lib.*;

import com.inria.spirals.mgonzale.grpc.lib.*;

import com.inria.spirals.mgonzale.model.injections.*;


public abstract class Injection
{
    protected final Faultinjection.InjectionType type;
    protected final Faultinjection.InjectionAction action;
    protected int sleep = 0;
    
    public Injection(final Faultinjection.Injection injection) {
        this.type = injection.getName();
        this.action = injection.getAction();
        this.sleep = injection.getSleep();

    }
    
    public Injection(final Faultinjection.InjectionType type, final Faultinjection.InjectionAction action) {
        this.type = type;
        this.action = action;
    }
    
    public Faultinjection.InjectionAction getAction() {
        return this.action;
    }
    
    public abstract Injection setAction(final Faultinjection.InjectionAction action);
    
    public Faultinjection.InjectionType getInjection() {
        return this.type;
    }
    
    public Faultinjection.InjectionType getType() {
        return this.type;
    }
    
    public int getSleep(){
    	return this.sleep;
    }
    
    public Injection invert() {
    	if (this.action == Faultinjection.InjectionAction.START){
    		return this.setAction(Faultinjection.InjectionAction.STOP);
    	}
    	
    	if (this.action == Faultinjection.InjectionAction.START_WAIT_STOP){
    		return this.setAction(Faultinjection.InjectionAction.STOP);
    	}
    	
    	return this.setAction(Faultinjection.InjectionAction.START);
    	//return (this.action == Faultinjection.InjectionAction.START) ? this.setAction(Faultinjection.InjectionAction.STOP) : this.setAction(Faultinjection.InjectionAction.START);

    }
    
    @Override
    public int hashCode() {
        return Objects.hash(this.type);
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final Injection injection = (Injection)o;
        return Objects.equals(this.type, injection.type);
    }
    
    @Override
    public String toString() {
        return "Injection{type='" + this.type + '\'' + ", action='" + this.action + '\'' + '}';
    }
    
    protected abstract Faultinjection.Injection.Builder build(final Faultinjection.Injection.Builder builder);
    
    Faultinjection.Injection.Builder createBuilder() {
        return Faultinjection.Injection.newBuilder().setName(this.type).setAction(this.action);
    }
    
    Faultinjection.Injection createInjection() {
        return this.build(this.createBuilder()).build();
    }
    
    public static Injection create(final Faultinjection.Injection injection) {
        switch (injection.getName()) {
            case BLACK: {
                return new Black(injection);
            }
            case BURNCPU: {
                return new BurnCPU(injection);
            }
            case BURNIO: {
                return new BurnIO(injection);
            }
            case CORRUPT: {
                return new Corrupt(injection);
            }
            case CORRUPTHDFS: {
                return new CorruptHDFS(injection);
            }
            case DCORRUPT: {
            		//return null;
                return new DiskCorrupt(injection);
            }
            case DDELAY: {
                return new DiskDelay(injection);
            }
            case DELAY: {
                return new DiskDelay(injection);
            }
            case DFAIL: {
                return new DiskFail(injection);
            }
            case DNSFAIL: {
                return new DnsFail(injection);
            }
            case DROP: {
                return new Drop(injection);
            }
            case DUPLICATE: {
                return new Duplicate(injection);
            }
            case FILLDISK: {
                return new FillDisk(injection);
            }
            case FILLMEM: {
                return new FillMem(injection);
            }
            case FLOOD: {
                return new Flood(injection);
            }
            case HANG: {
                return new Hang(injection);
            }
            case LIMIT: {
                return new Limit(injection);
            }
            case LOSS: {
                return new Loss(injection);
            }
            case PANIC: {
                return new Panic(injection);
            }
            case REJECT1: {
                return new Reject1(injection);
            }
            case REJECT2: {
                return new Reject2(injection);
            }
            case REORDER: {
                return new Reorder(injection);
            }
            case RONLY: {
                return new ReadOnly(injection);
            }
            case SIGSTOP: {
                return new SigStop(injection);
            }
            case SUICIDE: {
                return new Suicide(injection);
            }
            case UNMOUNT: {
                return new UnMount(injection);
            }
            case DELETE: {
                return new Delete(injection);
            }
            case DOWN: {
                return new Down(injection);
            }
            
            default: {
                throw new IllegalArgumentException();
            }
        }
    }
}
