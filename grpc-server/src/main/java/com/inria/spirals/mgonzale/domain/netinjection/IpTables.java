package com.inria.spirals.mgonzale.domain.netinjection;


import com.inria.spirals.mgonzale.domain.*;
import com.inria.spirals.mgonzale.support.*;
import com.inria.spirals.mgonzale.model.*;
import com.inria.spirals.mgonzale.model.injections.*;
import com.inria.spirals.mgonzale.grpc.lib.*;

import java.util.*;
import java.io.*;
import org.slf4j.*;

public class IpTables implements Injectable
{
    private static final int MAX_RETRY = 3;
    private static final Logger LOG;
    private static boolean partition_flag;
    
    @Override
    public boolean onStart(final Injection injection) {
        switch (injection.getInjection()) {
            case DNSFAIL: {
                return this.blockDNS();
            }
            case DROP: {
                return this.blockPort((NetworkBlock)injection);
            }
            case REJECT1: {
                return this.blockPort((NetworkBlock)injection);
            }
            case REJECT2: {
                return this.blockPort((NetworkBlock)injection);
            }
            default: {
                throw new UnsupportedOperationException("Do not know how to handle " + injection.getInjection());
            }
        }
    }
    
    @Override
    public boolean onStop(final Injection injection) {
        switch (injection.getInjection()) {
            case DNSFAIL: {
                return this.unblockDNS();
            }
            case DROP: {
                return this.unblockPort((NetworkBlock)injection);
            }
            case REJECT1: {
                return this.unblockPort((NetworkBlock)injection);
            }
            case REJECT2: {
                return this.unblockPort((NetworkBlock)injection);
            }
            default: {
                throw new UnsupportedOperationException("Do not know how to handle " + injection.getInjection());
            }
        }
    }
    
    private boolean blockPort(final NetworkBlock injection) {
        final AddressBlock addressBlock = injection.getAddressBlocks().stream().findFirst().orElse(null);
        final int port = addressBlock.getPort();
        final FlowType ft = FlowType.fromDirection(addressBlock.getDirection());
        final ActionType at = ActionType.fromInjection(injection);
        final String record = at.name() + "-" + ft.name().charAt(0) + "-" + port;
        if (!ft.checkAndAdd(IpTables.partition_flag ? new Pair<Integer, String>(0, "PARTITION") : new Pair<Integer, String>(port, record))) {
            return false;
        }
        IpTables.LOG.info("Block {} port with ip tables ({}) on port {}", ft.name().charAt(0), at.name(), port);
        try {
            if (!ft.name().equals("ALL")) {
                return Util.runCmdPrintRetry(3, ("iptables -A " + ft.toString() + " -p tcp --dport " + port + " -j " + at.getValue()).split("\\s"));
            }
            final boolean result = Util.runCmdPrintRetry(3, ("iptables -A INPUT -p tcp --dport " + port + " -j " + at.getValue()).split("\\s"));
            if (result) {
                return Util.runCmdPrintRetry(3, ("iptables -A OUTPUT -p tcp --dport " + port + " -j " + at.getValue()).split("\\s"));
            }
        }
        catch (Exception e) {
            IpTables.LOG.error("Exception", e);
        }
        return false;
    }
    
    private boolean unblockPort(final NetworkBlock injection) {
        final AddressBlock addressBlock = injection.getAddressBlocks().stream().findFirst().orElse(null);
        final int port = addressBlock.getPort();
        final FlowType ft = FlowType.fromDirection(addressBlock.getDirection());
        final ActionType at = ActionType.fromInjection(injection);
        final String record = at.name() + "-" + ft.name().charAt(0) + "-" + port;
        if (!ft.checkAndRemove(IpTables.partition_flag ? new Pair<Integer, String>(0, "PARTITION") : new Pair<Integer, String>(port, record))) {
            return false;
        }
        IpTables.LOG.info("Unblock {} port with iptables ({}) on port {}", ft.name().charAt(0), at.name(), port);
        try {
            if (!ft.toString().equals("ALL")) {
                return Util.runCmdPrintRetry(3, ("iptables -D " + ft.toString() + " -p tcp --dport " + port + " -j " + at.getValue()).split("\\s"));
            }
            final boolean result = Util.runCmdPrintRetry(3, "iptables", "-D INPUT", "-p", "tcp", "--dport", Integer.toString(port), "-j", at.getValue());
            if (result) {
                return Util.runCmdPrintRetry(3, "iptables", "-D", "OUTPUT", "-p", "tcp", "--dport", Integer.toString(port), "-j", at.getValue());
            }
        }
        catch (Exception e) {
            IpTables.LOG.error("Exception", e);
        }
        return false;
    }
    
    private boolean blockDNS() {
        if (!FlowType.INPUT.checkAndAdd(new Pair<Integer, String>(53, "DNS"))) {
            return false;
        }
        IpTables.LOG.info("Block DNS with iptables.");
        try {
            final boolean result = Util.runCmdPrintRetry(3, "iptables -A INPUT -p tcp -m tcp --sport 53 -j DROP".split("\\s"));
            if (result) {
                return Util.runCmdPrintRetry(3, "iptables -A INPUT -p udp -m udp --sport 53 -j DROP".split("\\s"));
            }
        }
        catch (Exception e) {
            IpTables.LOG.error("Exception", e);
        }
        return false;
    }
    
    private boolean unblockDNS() {
        if (!FlowType.INPUT.checkAndRemove(new Pair<Integer, String>(53, "DNS"))) {
            return false;
        }
        IpTables.LOG.info("Unblock DNS with iptables.");
        try {
            final boolean result = Util.runCmdPrintRetry(3, "iptables -D INPUT -p tcp -m tcp --sport 53 -j DROP".split("\\s"));
            if (result) {
                return Util.runCmdPrintRetry(3, "iptables -D INPUT -p udp -m udp --sport 53 -j DROP".split("\\s"));
            }
        }
        catch (Exception e) {
            IpTables.LOG.error("Error", e);
        }
        return false;
    }
    
    private boolean blockAddresses(final List<String> hosts) {
        if (hosts == null || hosts.isEmpty()) {
            IpTables.LOG.warn("Block not applied. Hosts list empty.");
            return false;
        }
        try {
            cleanRules();
            for (final String host : hosts) {
                IpTables.LOG.info("Block with iptables from host {}", host);
                Util.runCmdAndWait("iptables -A INPUT -s " + host);
            }
            return IpTables.partition_flag = true;
        }
        catch (Exception e) {
            IpTables.LOG.error("Exception", e);
            return false;
        }
    }
    
    private boolean unblockAddresses(final List<String> hosts) {
        if (hosts == null || hosts.isEmpty()) {
            IpTables.LOG.info("Unlock not applied. Hosts list empty.");
            return false;
        }
        try {
            for (final String host : hosts) {
                IpTables.LOG.info("Unlock with iptables from host {}", host);
                Util.runCmdPrintRetry(3, ("iptables -D INPUT -s " + host).split("\\s"));
            }
            IpTables.partition_flag = false;
        }
        catch (Exception e) {
            IpTables.LOG.error("Exception", e);
            return false;
        }
        return true;
    }
    
    public static void saveRules() {
        IpTables.LOG.debug("Saving the iptables rules.");
        if (!Util.runCmdPrintRetry(3, "/bin/sh", "-c", "iptables-save > ipfilter.bak")) {
            IpTables.LOG.warn("Was not possible to save the iptables rules");
        }
    }
    
    public static boolean checkSetup() throws IOException, InterruptedException {
        String[] cmd = { "/bin/sh", "-c", "which iptables" };
        try (final BufferedReader outputCommand = Util.runCmdAndReturnOutput(cmd)) {
            if (outputCommand.readLine() == null) {
                IpTables.LOG.warn("No 'iptables' firewall found");
                return false;
            }
        }
        catch (Util.CommandException e) {
            IpTables.LOG.error("Failed running command", e);
            return false;
        }
        cmd = new String[] { "/bin/sh", "-c", "echo $(id -u)" };
        try (final BufferedReader outputCommand = Util.runCmdAndReturnOutput(cmd)) {
            final String result;
            if (outputCommand != null && (result = outputCommand.readLine()) != null && result.compareTo("0") == 0) {
                IpTables.LOG.info("iptables Check: passed.");
                return true;
            }
            IpTables.LOG.warn("root privileges required.");
            return false;
        }
        catch (Util.CommandException e) {
            IpTables.LOG.error("Failed running command", e);
            return false;
        }
    }
    
    private static void cleanRules() {
        IpTables.LOG.info("Restoring the iptables rules.");
        FlowType.clear();
        if (!Util.runRawWaitAndIgnoreOutput("/bin/sh", "-c", "iptables --flush ; iptables-restore < ipfilter.bak")) {
            IpTables.LOG.warn("Was not possible to restore the iptables rules");
        }
    }
    
    static {
        LOG = LoggerFactory.getLogger(IpTables.class);
        IpTables.partition_flag = false;
    }
}
