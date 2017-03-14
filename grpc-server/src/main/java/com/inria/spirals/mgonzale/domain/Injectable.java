package com.inria.spirals.mgonzale.domain;

import com.inria.spirals.mgonzale.model.*;

public interface Injectable
{
    boolean onStart(final Injection injection);
    
    boolean onStop(final Injection injection);
}
