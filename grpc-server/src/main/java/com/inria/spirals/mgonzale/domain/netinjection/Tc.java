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
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;

@Component
public class Tc implements Injectable
{
    private  final int MAX_RETRY = 1;
    private  final Logger log = LoggerFactory.getLogger(Tc.class);
    private  final List<Pair<Integer, Pair<NetworkCorruptInjection, String>>> FILTER_REMOVE_COMMANDS  = new CopyOnWriteArrayList<Pair<Integer, Pair<NetworkCorruptInjection, String>>>();
    private  boolean initFlag = false;
    private  boolean hostInjected = false;
    @Value("${tc.interface}")
    private String dev;

    private  String prefix;
    private  String[] cmd;
    private  AtomicInteger nextLabelId;



    private String getPrefix() {
        return prefix;
    }

    private void setPrefix(final String prefix) {
        this.prefix = prefix;
    }


	@EventListener(ContextRefreshedEvent.class)
    private void initCode(){
    	 cmd = new String[] { "/bin/sh", "-c", "" };
         nextLabelId = new AtomicInteger(11);
         //dev = Configuration.getConfiguration().getDevice();
         setPrefix("tc qdisc add dev " + dev + " root handle 1: htb default 10 ; tc class add dev " + dev + " parent 1: classid 1:1 htb rate 500mbps ; tc class add dev " + dev + " parent 1:1 classid 1:10 htb rate 300mbps ceil 500mbps");
         cleanBasic();
         cmd[2] = "if ! lsmod | fgrep -q \"sch_netem\"; then modprobe sch_netem; fi";

         if (!Util.runRawWaitAndIgnoreOutput(cmd)) {
             log.error("Error during the Tc init phase");
         }
         else {
             initFlag = true;
         }
         log.debug("Adding new queuing discipline.");

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
        final int nextID = nextLabelId.get();
        String hostCmd = null;
        String middleCmd1 = "tc class add dev " + dev + " parent 1:1 classid 1:" + nextID + " htb rate 500mbps";
        String middleCmd2 = "";
        log.info("Filter port {} ", injection);
        boolean result = true;
        final SocketAddress address = injection.getSource();
        final String ipAddress = address.getHost();
        final int port = address.getPort();
        try {
            Preconditions.checkState(initFlag, (Object)"Tc init() phase failed.");
            if (!this.isFilterApplicable(port)) {
                return false;
            }
            switch (injection.getType()) {
                case LIMIT: {
                    hostCmd = getPrefix() + " ; tc class add dev " + dev + " parent 1:1 classid 1:11 htb rate " + ipAddress + "kbps ceil " + ipAddress + "kbps ; tc filter add dev " + dev + " parent 1: protocol ip prio 1 u32 match ip dst 0.0.0.0/0 flowid 1:11 ; tc filter add dev " + dev + " parent 1: protocol ip prio 1 u32 match ip src 0.0.0.0/0 flowid 1:11;";
                    middleCmd1 = "tc class add dev " + dev + " parent 1:1 classid 1:" + nextID + " htb rate " + ipAddress + "kbps ceil " + ipAddress + "kbps";
                    break;
                }
                case DELAY: {
                    hostCmd = "tc qdisc add dev " + dev + " root netem delay " + ipAddress + "ms";
                    middleCmd2 = "tc qdisc add dev " + dev + " parent 1:" + nextID + " handle A" + nextID + ": netem delay " + ipAddress + "ms";
                    break;
                }
                case LOSS: {
                    hostCmd = "tc qdisc add dev " + dev + " root netem loss " + ipAddress + "%";
                    middleCmd2 = "tc qdisc add dev " + dev + " parent 1:" + nextID + " handle B" + nextID + ": netem loss " + ipAddress + "%";
                    break;
                }
                case CORRUPT: {
                    hostCmd = "tc qdisc add dev " + dev + " root netem corrupt " + ipAddress + "%";
                    middleCmd2 = "tc qdisc add dev " + dev + " parent 1:" + nextID + " handle C" + nextID + ": netem corrupt " + ipAddress + "%";
                    break;
                }
                case REORDER: {
                    hostCmd = "tc qdisc add dev " + dev + " root netem delay 10ms reorder " + ipAddress + "% 50%";
                    middleCmd2 = "tc qdisc add dev " + dev + " parent 1:" + nextID + " handle D" + nextID + ": netem delay 10ms reorder " + ipAddress + "% 50%";
                    break;
                }
                case DUPLICATE: {
                    hostCmd = "tc qdisc add dev " + dev + " root netem duplicate " + ipAddress + "%";
                    middleCmd2 = "tc qdisc add dev " + dev + " parent 1:" + nextID + " handle E" + nextID + ": netem duplicate " + ipAddress + "%";
                    break;
                }
            }
        }
        catch (Exception e) {
            log.error("Error ", e);
            return false;
        }
        if (port == -1) {
            log.info("Applying {} injection to the host.", injection);
            final String[] split;
            final String[] cmds = split = hostCmd.split(" ; ");
            for (final String cmd1 : split) {
                cmd[2] = cmd1;
                result = (result && Util.runCmdPrintRetry(1, cmd));
            }
            if (result) {
                log.info("Injection {} on the host started.", injection);
                return hostInjected = true;
            }
            cleanBasic();
        }
        else {
            final String suffixAddCmd = "tc filter add dev " + dev + " protocol ip prio 1 handle ::" + nextID + " u32 match ip dport " + port + " 0xffff flowid 1:" + nextID + " ; tc filter add dev " + dev + " protocol ip prio 1 handle ::" + nextID + " u32 match ip sport " + port + " 0xffff flowid 1:" + nextID;
            log.info("Applying {} ", injection);
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
            if (FILTER_REMOVE_COMMANDS.isEmpty()) {
                final String[] split2;
                final String[] cmds2 = split2 = getPrefix().split(" ; ");
                for (final String cmd2 : split2) {
                    cmd[2] = cmd2;
                    result = (result && Util.runCmdPrintRetry(1, cmd));
                }
            }
            final String[] split3;
            final String[] cmds2 = split3 = addCmd.split(" ; ");
            for (final String cmd2 : split3) {
                cmd[2] = cmd2;
                result = (result && Util.runCmdPrintRetry(1, cmd));
            }
            if (result) {
                nextLabelId.incrementAndGet();
                FILTER_REMOVE_COMMANDS.add(new Pair<Integer, Pair<NetworkCorruptInjection, String>>(port, new Pair<NetworkCorruptInjection, String>(injection, deleteCmd)));
                log.info("Injection {} on port started.", injection);
                return true;
            }
        }
        return false;
    }

    private boolean isFilterApplicable(final int port) throws IllegalArgumentException {
        if (hostInjected) {
            log.info("Filter already present on the host.");
            return false;
        }
        if (port == -1 && FILTER_REMOVE_COMMANDS.size() > 0) {
            log.info("Injection not possible. Single port injections need to be stopped first.");
            return false;
        }
        if (getFilter(port) != null) {
            log.info("Filter already present on port: " + port + ".");
            return false;
        }
        return true;
    }

    public  boolean checkSetup() throws IOException, InterruptedException {
        cmd[2] = "which tc";
        if (!Util.runRawWaitAndIgnoreOutput(cmd)) {
            log.info("No 'tc' traffic control found.");
            return false;
        }
        cmd[2] = "echo $(id -u)";
        try (final BufferedReader outputCommand = Util.runCmdAndReturnOutput(cmd)) {
            if (!Objects.equals(outputCommand.readLine(), "0")) {
                log.warn("Root privileges are required.");
                return false;
            }
            log.info("tc Check: passed.");
            return true;
        }
        catch (Util.CommandException e) {
            log.error("Failed running command", e);
            return false;
        }
    }

    public  void cleanRules() throws IOException, InterruptedException {
        log.info("Resetting queuing discipline.");
        if (hostInjected) {
            hostInjected = false;
            FILTER_REMOVE_COMMANDS.clear();
        }
        else {
            while (!FILTER_REMOVE_COMMANDS.isEmpty()) {
                final Pair<Integer, Pair<NetworkCorruptInjection, String>> pair = FILTER_REMOVE_COMMANDS.remove(0);
                removeFilter(pair.getR());
            }
        }
        cleanBasic();
    }

    private  boolean isFilterRemovable(final int port) throws IllegalArgumentException {
        if (!hostInjected) {
            return getFilter(port) != null;
        }
        if (port == -1) {
            log.info("Removing the filter from the host.");
            return true;
        }
        log.info("Trying to remove a port injection while there is an injection on the host.");
        return false;
    }

    private  Pair<Integer, Pair<NetworkCorruptInjection, String>> getFilter(final int port) throws IllegalArgumentException {
        Preconditions.checkArgument(port >= -1 && port <= 65535, (Object)"Port out of range.");
        for (final Pair<Integer, Pair<NetworkCorruptInjection, String>> filterRemoveCommand : FILTER_REMOVE_COMMANDS) {
            if (filterRemoveCommand.getL() == port) {
                return filterRemoveCommand;
            }
        }
        return null;
    }

    private  boolean removeFilter(final Pair<NetworkCorruptInjection, String> pair) {
        boolean result = true;
        final NetworkCorruptInjection injection = pair.getL();
        log.info("Removing filter {}", injection);
        try {
            final SocketAddress address = injection.getSource();
            final int port = address.getPort();
            if (!isFilterRemovable(port)) {
                return false;
            }
            if (port == -1) {
                log.info("Filter on the host removed.");
                hostInjected = false;
                cleanBasic();
                return true;
            }
            final Pair<Integer, Pair<NetworkCorruptInjection, String>> filter = getFilter(port);
            assert filter != null;
            final String[] split;
            final String[] cmds = split = filter.getR().getR().split(";");
            for (final String cmd1 : split) {
                cmd[2] = cmd1;
                result = (result && Util.runCmdPrintRetry(1, cmd));
            }
            FILTER_REMOVE_COMMANDS.remove(filter);
            if (FILTER_REMOVE_COMMANDS.isEmpty()) {
                cleanBasic();
            }
            log.info("Filter on port: {} removed.", injection);
            return result;
        }
        catch (Exception e) {
            log.error("Exception", e);
            return false;
        }
    }

    private void cleanBasic() {
        cmd[2] = "tc qdisc show dev " + dev + "| grep -c 'qdisc htb 1: root\\|qdisc netem'";
        final boolean result = Util.runRawWaitAndIgnoreOutput(cmd);
        if (result) {
            cmd[2] = "tc qdisc del dev " + dev + " root";
            Util.runRawWaitAndIgnoreOutput(cmd);
        }
    }

}
