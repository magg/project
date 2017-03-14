package com.inria.spirals.mgonzale.model;

import com.inria.spirals.mgonzale.util.*;

import com.inria.spirals.mgonzale.model.injections.*;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inria.spirals.mgonzale.grpc.lib.*;


public class DiskCorrupt extends DiskInjectionBase
{
    private static final Logger LOG;
    private final int probability;
    private final int percentage;
    
    public DiskCorrupt(final Faultinjection.InjectionAction action, final String path, final String accessMode, final int probability, final int percentage) {
        super(Faultinjection.InjectionType.DCORRUPT, action, path, accessMode);
        this.probability = probability;
        this.percentage = percentage;
        this.checkArguments();
    }
    
    public DiskCorrupt(final Faultinjection.Injection injection) {
        super(injection);
        this.probability = injection.getProbability();
        this.percentage = injection.getPercentage();
        this.checkArguments();
    }
    
    public int getPercentage() {
        return this.percentage;
    }
    
    public int getProbability() {
        return this.probability;
    }
    
    @Override
    public Injection setAction(final Faultinjection.InjectionAction action) {
        return new DiskCorrupt(action, this.getPath(), this.getAccessMode(), this.probability, this.percentage);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.probability, this.percentage);
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DiskCorrupt)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        final DiskCorrupt that = (DiskCorrupt)o;
        return this.probability == that.probability && this.percentage == that.percentage;
    }
    
    @Override
    public String toString() {
        return "DiskCorrupt{probability=" + this.probability + ", percentage=" + this.percentage + "} " + super.toString();
    }
    
    private void checkArguments() {
        Objects.requireNonNull(this.getPath(), "Target file/directory not defined.");
        ArgumentChecker.checkArgument(this.getProbability() > 0 && this.getProbability() <= 100, "Prob parameter value should be between 1 to 100.");
        ArgumentChecker.checkArgument(this.getPercentage() > 0 && this.getPercentage() <= 100, "Amount parameter value should be between 1 to 100.");
    }
    
    static {
        LOG = LoggerFactory.getLogger(DiskCorrupt.class);
    }
}
