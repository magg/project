package com.inria.spirals.mgonzale.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ArgumentChecker
{
    private static final Logger LOG;
    
    public static void checkArgument(final boolean check, final String message) {
        if (check) {
            return;
        }
        throw new IllegalArgumentException(message);
    }
    
    static {
        LOG = LoggerFactory.getLogger(ArgumentChecker.class);
    }
}
