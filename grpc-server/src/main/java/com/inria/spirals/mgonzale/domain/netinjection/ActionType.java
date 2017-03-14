package com.inria.spirals.mgonzale.domain.netinjection;

import com.inria.spirals.mgonzale.domain.*;
import com.inria.spirals.mgonzale.support.*;
import com.inria.spirals.mgonzale.model.*;
import com.inria.spirals.mgonzale.grpc.lib.*;

public enum ActionType
{
    DROP("DROP"), 
    REJECT1("REJECT"), 
    REJECT2("REJECT --reject-with tcp-reset");
    
    public static final int size;
    private final String value;
    
    private ActionType(final String value) {
        this.value = value;
    }
    
    public String getValue() {
        return this.value;
    }
    
    public static ActionType fromInjection(final Injection injection) {
        return valueOf(injection.getType().name());
    }
    
    static {
        size = values().length;
    }
}
