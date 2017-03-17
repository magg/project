package com.inria.spirals.mgonzale.domain.netinjection;

import java.util.concurrent.atomic.*;
import com.google.common.base.*;
import com.inria.spirals.mgonzale.domain.*;
import com.inria.spirals.mgonzale.support.*;
import com.inria.spirals.mgonzale.model.*;
import com.inria.spirals.mgonzale.model.injections.*;
import com.inria.spirals.mgonzale.grpc.lib.*;


import java.io.*;
import java.util.*;
import java.util.Objects;

import org.slf4j.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;

@Component
public class Tc implements Injectable
{
    private static final int MAX_RETRY = 1;
    private static final Logger log;
    private static final List<Pair<Integer, Pair<NetworkCorruptInjection, String>>> FILTER_REMOVE_COMMANDS;
    private static boolean initFlag;
    private static boolean hostInjected;
    @Value("${tc.interface}")
    private static String dev;
        
    private static String prefix;
    private static String[] cmd;
    private static AtomicInteger nextLabelId;
    
    private static String getPrefix() {
        return Tc.prefix;
    }
    
    private static void setPrefix(final String prefix) {
        Tc.prefix = prefix;
    }
    
    @Override
    public boolean onStart(final Injection injection) {
        switch (injection.getInjection()) {
            case LOSS:
            case CORRUPT:
            case REORDER:
            case DUPLICATE:
            case DELAY:
            case LIMIT: {
                return this.filterPort((NetworkCorruptInjection)injection);
            }
            default: {
                throw new UnsupportedOperationException("Do not know how to handle " + injection.getInjection());
            }
        }
    }
    
    @Override
    public boolean onStop(final Injection injection) {
        switch (injection.getInjection()) {
            case LOSS:
            case CORRUPT:
            case REORDER:
            case DUPLICATE:
            case DELAY:
            case LIMIT: {
                return removeFilter(new Pair<NetworkCorruptInjection, String>((NetworkCorruptInjection)injection, ""));
            }
            default: {
                throw new UnsupportedOperationException("Do not know how to handle " + injection.getInjection());
            }
        }
    }
    
    private boolean filterPort(final NetworkCorruptInjection injection) {
        final int nextID = Tc.nextLabelId.get();
        String hostCmd = null;
        String middleCmd1 = "tc class add dev " + Tc.dev + " parent 1:1 classid 1:" + nextID + " htb rate 500mbps";
        String middleCmd2 = "";
        Tc.log.info("Filter port {} ", injection);
        boolean result = true;
        final SocketAddress address = injection.getSource();
        final String ipAddress = address.getHost();
        final int port = address.getPort();
        try {
            Preconditions.checkState(Tc.initFlag, (Object)"Tc init() phase failed.");
            if (!this.isFilterApplicable(port)) {
                return false;
            }
            switch (injection.getType()) {
                case LIMIT: {
                    hostCmd = getPrefix() + " ; tc class add dev " + Tc.dev + " parent 1:1 classid 1:11 htb rate " + ipAddress + "kbps ceil " + ipAddress + "kbps ; tc filter add dev " + Tc.dev + " parent 1: protocol ip prio 1 u32 match ip dst 0.0.0.0/0 flowid 1:11 ; tc filter add dev " + Tc.dev + " parent 1: protocol ip prio 1 u32 match ip src 0.0.0.0/0 flowid 1:11;";
                    middleCmd1 = "tc class add dev " + Tc.dev + " parent 1:1 classid 1:" + nextID + " htb rate " + ipAddress + "kbps ceil " + ipAddress + "kbps";
                    break;
                }
                case DELAY: {
                    hostCmd = "tc qdisc add dev " + Tc.dev + " root netem delay " + ipAddress + "ms";
                    middleCmd2 = "tc qdisc add dev " + Tc.dev + " parent 1:" + nextID + " handle A" + nextID + ": netem delay " + ipAddress + "ms";
                    break;
                }
                case LOSS: {
                    hostCmd = "tc qdisc add dev " + Tc.dev + " root netem loss " + ipAddress + "%";
                    middleCmd2 = "tc qdisc add dev " + Tc.dev + " parent 1:" + nextID + " handle B" + nextID + ": netem loss " + ipAddress + "%";
                    break;
                }
                case CORRUPT: {
                    hostCmd = "tc qdisc add dev " + Tc.dev + " root netem corrupt " + ipAddress + "%";
                    middleCmd2 = "tc qdisc add dev " + Tc.dev + " parent 1:" + nextID + " handle C" + nextID + ": netem corrupt " + ipAddress + "%";
                    break;
                }
                case REORDER: {
                    hostCmd = "tc qdisc add dev " + Tc.dev + " root netem delay 10ms reorder " + ipAddress + "% 50%";
                    middleCmd2 = "tc qdisc add dev " + Tc.dev + " parent 1:" + nextID + " handle D" + nextID + ": netem delay 10ms reorder " + ipAddress + "% 50%";
                    break;
                }
                case DUPLICATE: {
                    hostCmd = "tc qdisc add dev " + Tc.dev + " root netem duplicate " + ipAddress + "%";
                    middleCmd2 = "tc qdisc add dev " + Tc.dev + " parent 1:" + nextID + " handle E" + nextID + ": netem duplicate " + ipAddress + "%";
                    break;
                }
            }
        }
        catch (Exception e) {
            Tc.log.error("Error ", e);
            return false;
        }
        if (port == -1) {
            Tc.log.info("Applying {} injection to the host.", injection);
            final String[] split;
            final String[] cmds = split = hostCmd.split(" ; ");
            for (final String cmd1 : split) {
                Tc.cmd[2] = cmd1;
                result = (result && Util.runCmdPrintRetry(1, Tc.cmd));
            }
            if (result) {
                Tc.log.info("Injection {} on the host started.", injection);
                return Tc.hostInjected = true;
            }
            cleanBasic();
        }
        else {
            final String suffixAddCmd = "tc filter add dev " + Tc.dev + " protocol ip prio 1 handle ::" + nextID + " u32 match ip dport " + port + " 0xffff flowid 1:" + nextID + " ; tc filter add dev " + Tc.dev + " protocol ip prio 1 handle ::" + nextID + " u32 match ip sport " + port + " 0xffff flowid 1:" + nextID;
            Tc.log.info("Applying {} ", injection);
            String addCmd;
            String deleteCmd;
            if (injection.getType() == Faultinjection.InjectionType.LIMIT) {
                addCmd = middleCmd1 + " ; " + suffixAddCmd;
                deleteCmd = (suffixAddCmd.replace("::", "800::") + " ; " + middleCmd1).replace("add", "del");
            }
            else {
                addCmd = middleCmd1 + " ; " + middleCmd2 + " ; " + suffixAddCmd;
                deleteCmd = (suffixAddCmd.replace("::", "800::") + " ; " + middleCmd2 + " ; " + middleCmd1).replace("add", "del");
            }
            if (Tc.FILTER_REMOVE_COMMANDS.isEmpty()) {
                final String[] split2;
                final String[] cmds2 = split2 = getPrefix().split(" ; ");
                for (final String cmd2 : split2) {
                    Tc.cmd[2] = cmd2;
                    result = (result && Util.runCmdPrintRetry(1, Tc.cmd));
                }
            }
            final String[] split3;
            final String[] cmds2 = split3 = addCmd.split(" ; ");
            for (final String cmd2 : split3) {
                Tc.cmd[2] = cmd2;
                result = (result && Util.runCmdPrintRetry(1, Tc.cmd));
            }
            if (result) {
                Tc.nextLabelId.incrementAndGet();
                Tc.FILTER_REMOVE_COMMANDS.add(new Pair<Integer, Pair<NetworkCorruptInjection, String>>(port, new Pair<NetworkCorruptInjection, String>(injection, deleteCmd)));
                Tc.log.info("Injection {} on port started.", injection);
                return true;
            }
        }
        return false;
    }
    
    private boolean isFilterApplicable(final int port) throws IllegalArgumentException {
        if (Tc.hostInjected) {
            Tc.log.info("Filter already present on the host.");
            return false;
        }
        if (port == -1 && Tc.FILTER_REMOVE_COMMANDS.size() > 0) {
            Tc.log.info("Injection not possible. Single port injections need to be stopped first.");
            return false;
        }
        if (getFilter(port) != null) {
            Tc.log.info("Filter already present on port: " + port + ".");
            return false;
        }
        return true;
    }
    
    public static boolean checkSetup() throws IOException, InterruptedException {
        Tc.cmd[2] = "which tc";
        if (!Util.runRawWaitAndIgnoreOutput(Tc.cmd)) {
            Tc.log.info("No 'tc' traffic control found.");
            return false;
        }
        Tc.cmd[2] = "echo $(id -u)";
        try (final BufferedReader outputCommand = Util.runCmdAndReturnOutput(Tc.cmd)) {
            if (!Objects.equals(outputCommand.readLine(), "0")) {
                Tc.log.warn("Root privileges are required.");
                return false;
            }
            Tc.log.info("tc Check: passed.");
            return true;
        }
        catch (Util.CommandException e) {
            Tc.log.error("Failed running command", e);
            return false;
        }
    }
    
    public static void cleanRules() throws IOException, InterruptedException {
        Tc.log.info("Resetting queuing discipline.");
        if (Tc.hostInjected) {
            Tc.hostInjected = false;
            Tc.FILTER_REMOVE_COMMANDS.clear();
        }
        else {
            while (!Tc.FILTER_REMOVE_COMMANDS.isEmpty()) {
                final Pair<Integer, Pair<NetworkCorruptInjection, String>> pair = Tc.FILTER_REMOVE_COMMANDS.remove(0);
                removeFilter(pair.getR());
            }
        }
        cleanBasic();
    }
    
    private static boolean isFilterRemovable(final int port) throws IllegalArgumentException {
        if (!Tc.hostInjected) {
            return getFilter(port) != null;
        }
        if (port == -1) {
            Tc.log.info("Removing the filter from the host.");
            return true;
        }
        Tc.log.info("Trying to remove a port injection while there is an injection on the host.");
        return false;
    }
    
    private static Pair<Integer, Pair<NetworkCorruptInjection, String>> getFilter(final int port) throws IllegalArgumentException {
        Preconditions.checkArgument(port >= -1 && port <= 65535, (Object)"Port out of range.");
        for (final Pair<Integer, Pair<NetworkCorruptInjection, String>> filterRemoveCommand : Tc.FILTER_REMOVE_COMMANDS) {
            if (filterRemoveCommand.getL() == port) {
                return filterRemoveCommand;
            }
        }
        return null;
    }
    
    private static boolean removeFilter(final Pair<NetworkCorruptInjection, String> pair) {
        boolean result = true;
        final NetworkCorruptInjection injection = pair.getL();
        Tc.log.info("Removing filter {}", injection);
        try {
            final SocketAddress address = injection.getSource();
            final int port = address.getPort();
            if (!isFilterRemovable(port)) {
                return false;
            }
            if (port == -1) {
                Tc.log.info("Filter on the host removed.");
                Tc.hostInjected = false;
                cleanBasic();
                return true;
            }
            final Pair<Integer, Pair<NetworkCorruptInjection, String>> filter = getFilter(port);
            assert filter != null;
            final String[] split;
            final String[] cmds = split = filter.getR().getR().split(";");
            for (final String cmd1 : split) {
                Tc.cmd[2] = cmd1;
                result = (result && Util.runCmdPrintRetry(1, Tc.cmd));
            }
            Tc.FILTER_REMOVE_COMMANDS.remove(filter);
            if (Tc.FILTER_REMOVE_COMMANDS.isEmpty()) {
                cleanBasic();
            }
            Tc.log.info("Filter on port: {} removed.", injection);
            return result;
        }
        catch (Exception e) {
            Tc.log.error("Exception", e);
            return false;
        }
    }
    
    private static void cleanBasic() {
        Tc.cmd[2] = "tc qdisc show dev " + Tc.dev + "| grep -c 'qdisc htb 1: root\\|qdisc netem'";
        final boolean result = Util.runRawWaitAndIgnoreOutput(Tc.cmd);
        if (result) {
            Tc.cmd[2] = "tc qdisc del dev " + Tc.dev + " root";
            Util.runRawWaitAndIgnoreOutput(Tc.cmd);
        }
    }
    
    static {
        log = LoggerFactory.getLogger(Tc.class);
        FILTER_REMOVE_COMMANDS = new CopyOnWriteArrayList<Pair<Integer, Pair<NetworkCorruptInjection, String>>>();
        Tc.initFlag = false;
        Tc.hostInjected = false;
        Tc.dev = "eth0";
        Tc.cmd = new String[] { "/bin/sh", "-c", "" };
        Tc.nextLabelId = new AtomicInteger(11);
        //Tc.dev = Configuration.getConfiguration().getDevice();
        setPrefix("tc qdisc add dev " + Tc.dev + " root handle 1: htb default 10 ; tc class add dev " + Tc.dev + " parent 1: classid 1:1 htb rate 500mbps ; tc class add dev " + Tc.dev + " parent 1:1 classid 1:10 htb rate 300mbps ceil 500mbps");
        cleanBasic();
        Tc.cmd[2] = "if ! lsmod | fgrep -q \"sch_netem\"; then modprobe sch_netem; fi";
        if (!Util.runRawWaitAndIgnoreOutput(Tc.cmd)) {
            Tc.log.error("Error during the Tc init phase");
        }
        else {
            Tc.initFlag = true;
        }
        Tc.log.debug("Adding new queuing discipline.");
    }
}
