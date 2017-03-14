package com.inria.spirals.mgonzale.domain.netinjection;

import com.inria.spirals.mgonzale.domain.*;
import com.inria.spirals.mgonzale.support.*;
import com.inria.spirals.mgonzale.model.*;
import com.inria.spirals.mgonzale.grpc.lib.*;
import java.util.*;
import java.io.*;
//import org.apache.commons.validator.routines.*;
import org.apache.commons.logging.*;
import java.util.concurrent.*;

public class IpRoute implements Injectable
{
    private static final Log log;
    private static final int MAX_RETRY = 3;
    private static final List<String> NULL_ROUTES;
    
    @Override
    public boolean onStart(final Injection injection) {
        return setNullRoute("10.0.0.0/8");
    }
    
    @Override
    public boolean onStop(final Injection injection) {
        return resetNullRoute("10.0.0.0/8");
    }
    
    public static boolean cleanRules() {
        boolean flag = true;
        for (final String nullRoute : IpRoute.NULL_ROUTES) {
            if (!resetNullRoute(nullRoute)) {
                flag = false;
            }
        }
        return flag;
    }
    
    public static boolean checkSetup() throws IOException {
        try (final BufferedReader outputCommand = Util.runCmdAndReturnOutput("/bin/sh", "-c", "which ip")) {
            if (outputCommand == null || outputCommand.readLine() == null) {
                IpRoute.log.warn("No 'ip' command found.");
                return false;
            }
        }
        catch (Util.CommandException e) {
            IpRoute.log.error("Failed running command", e);
            return false;
        }
        try (final BufferedReader outputCommand = Util.runCmdAndReturnOutput("/bin/sh", "-c", "echo $(id -u)")) {
            final String result;
            if (outputCommand != null && (result = outputCommand.readLine()) != null && result.compareTo("0") == 0) {
                IpRoute.log.info("ip Check: passed.");
                return true;
            }
            IpRoute.log.warn("root privileges required.");
            return false;
        }
        catch (Util.CommandException e) {
            IpRoute.log.error("Failed running command", e);
            return false;
        }
    }
    
    private static boolean setNullRoute(final String addressNetmask) {
        if (!isValidNetTarget(addressNetmask)) {
            IpRoute.log.error(addressNetmask + " is not valid.");
            return false;
        }
        if (IpRoute.NULL_ROUTES.contains(addressNetmask)) {
            IpRoute.log.error("NullRoute on" + addressNetmask + " already active.");
            return false;
        }
        IpRoute.NULL_ROUTES.add(addressNetmask);
        return Util.runCmdPrintRetry(3, ("ip route add blackhole " + addressNetmask).split("\\s"));
    }
    
    private static boolean resetNullRoute(final String addressNetmask) {
        if (!isValidNetTarget(addressNetmask)) {
            IpRoute.log.error(addressNetmask + " is not valid.");
            return false;
        }
        if (IpRoute.NULL_ROUTES.contains(addressNetmask)) {
            IpRoute.NULL_ROUTES.remove(addressNetmask);
            return Util.runCmdPrintRetry(3, ("ip route delete " + addressNetmask).split("\\s"));
        }
        IpRoute.log.error("NullRoute on" + addressNetmask + " is not active.");
        return false;
    }
    
    private static boolean isValidNetTarget(final String addressNetmask) {
        if (addressNetmask == null) {
            return false;
        }
        final String[] components = addressNetmask.split("/");
        return components.length == 2 && Integer.valueOf(components[0]) >= 0 && Integer.valueOf(components[0]) <= 32; //&& InetAddressValidator.getInstance().isValid(components[0]);
    }
    
    static {
        log = LogFactory.getLog(IpRoute.class);
        NULL_ROUTES = new CopyOnWriteArrayList<String>();
    }
}
