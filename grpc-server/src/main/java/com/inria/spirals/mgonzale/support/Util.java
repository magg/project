package com.inria.spirals.mgonzale.support;

import java.nio.file.attribute.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.*;
import java.io.*;
import org.slf4j.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class Util
{
    private static final Logger LOG;
    
    public static boolean isMac() {
        final String OS = System.getProperty("os.name").toLowerCase();
        return OS.contains("mac");
    }
    
    public static boolean isUnix() {
        final String OS = System.getProperty("os.name").toLowerCase();
        return OS.contains("nix") || OS.contains("nux") || OS.contains("aix");
    }
    
    static Path getResource(final String resource, final Path dir) throws IOException {
        final Path destPath = dir.resolve(resource);
        if (!Files.exists(destPath, new LinkOption[0])) {
            Files.createDirectories(destPath.getParent(), (FileAttribute<?>[])new FileAttribute[0]);
            final InputStream resourceAsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
            if (resourceAsStream == null) {
                System.out.println("Cannot find resource");
            }
            else {
                Files.copy(resourceAsStream, destPath, new CopyOption[0]);
            }
        }
        return destPath;
    }
    
    public static boolean runScriptPrintRetry(final int retry, final String script, final Object... args) {
        int count = 0;
        int result = 1;
        final String[] cmd = new String[args.length + 1];
		Resource resource = new ClassPathResource("scripts");
        
        try {
            cmd[0] = getResource(script, resource.getFile().toPath()).toString();
            final String[] src = Arrays.stream(args).map(Object::toString).toArray(String[]::new);
            System.arraycopy(src, 0, cmd, 1, args.length);
            while (count < retry && (result = runCmd(true, true, cmd)) != 0) {
                if (count++ > 0) {
                    Util.LOG.info("Execution #" + (count + 1));
                }
            }
        }
        catch (IOException e) {
            Util.LOG.error("Unable to find resource " + script, e);
        }
        return result == 0;
    }
    
    public static boolean runCmdPrintRetry(final int retry, final String... cmd) {
        int count = 0;
        int result = 1;
        while (count < retry && (result = runCmd(true, true, cmd)) != 0) {
            if (count++ > 0) {
                Util.LOG.info("Execution #" + (count + 1));
            }
        }
        return result == 0;
    }
    
    public static boolean runRawWaitAndIgnoreOutput(final String... cmd) {
        return runCmd(false, true, cmd) == 0;
    }
    
    public static boolean runCmdAndWait(final String... cmd) {
        return runCmd(true, true, cmd) == 0;
    }
    
    public static BufferedReader runCmdAndReturnOutput(final String... cmd) throws CommandException {
        try {
            final Process p = Runtime.getRuntime().exec(cmd);
            final int result = p.waitFor();
            readErrorStream(true, p);
            if (result != 0) {
                throw new CommandException(cmd);
            }
            return getInputStream(p);
        }
        catch (IOException | InterruptedException ex2) {
            /*final */Exception ex = null;
            /*final */ Exception e = ex;
            throw new CommandException(cmd);
        }
    }
    
    private static int runCmd(final boolean logStdOut, final boolean logStdErr, final String... cmd) {
        int result = 1;
        try {
            final Process p = Runtime.getRuntime().exec(cmd);
            result = p.waitFor();
            readOutStream(logStdOut, p);
            readErrorStream(logStdErr, p);
        }
        catch (Exception e) {
            Util.LOG.error("Executing command " + Arrays.toString(cmd), e);
        }
        return result;
    }
    
    private static void readErrorStream(final boolean show, final Process p) throws IOException {
        if (show) {
            try (final BufferedReader be = new BufferedReader(new InputStreamReader(p.getErrorStream()))) {
                if (be.ready()) {
                    Util.LOG.error("********************** STDERR **********************");
                    String line;
                    while ((line = be.readLine()) != null) {
                        Util.LOG.error(line);
                    }
                }
            }
        }
    }
    
    private static void readOutStream(final boolean show, final Process p) throws IOException {
        if (show && Util.LOG.isInfoEnabled()) {
            try (final BufferedReader be = getInputStream(p)) {
                if (be.ready()) {
                    Util.LOG.info("********************** STDOUT **********************");
                    String line;
                    while ((line = be.readLine()) != null) {
                        Util.LOG.debug(line);
                    }
                }
            }
        }
    }
    
    private static BufferedReader getInputStream(final Process p) {
        return new BufferedReader(new InputStreamReader(p.getInputStream()));
    }
    
    static {
        LOG = LoggerFactory.getLogger(Util.class);
    }
    
    public static class CommandException extends Exception
    {
    	private static final long serialVersionUID = 1L;
        private final String command;
        
        CommandException(final String command) {
            this.command = command;
        }
        
        CommandException(final String... command) {
            this(Arrays.toString(command));
        }
        
        @Override
        public String getMessage() {
            return "Failed running '" + this.command + "'";
        }
        
    }
}
