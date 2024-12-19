package com.example;

import org.apache.ignite.configuration.WALMode;

import picocli.CommandLine.Option;

public class CommandLineOptions {

    @Option(names = { "-ih", "--igniteHome" }, description = "Ignite home directory", defaultValue = "${sys:java.io.tmpdir}/ignite")
    private String igniteHome;

    @Option(names = { "-wm", "--walMode" }, description = "WAL mode, default is NONE", defaultValue = "NONE")
    private WALMode walMode;

    @Option(names = { "-j", "--nbJobs" }, description = "number of Jobs", defaultValue = "10")
    private int nbJobs;

    @Option(names = { "-js", "--jobSize" }, description = "Jobs Size", defaultValue = "1_000_000")
    private int jobSize;

    @Option(names = { "-sjs", "--subJobSize" }, description = "Sub-Jobs Size", defaultValue = "10")
    private int subJobSize;

    @Option(names = { "-oh", "--offHeapMemory" }, description = "Off-heap memory size expressed in MB", defaultValue = "1024")
    private int offHeapMemory;

    @Option(names = { "-wt", "--writeThrottleEnabled" }, description = "Enable write throttle", defaultValue = "false")
    private boolean writeThrottleEnabled;

    @Option(names = { "-cpbs", "--checkPointBufferSize" }, description = "Checkpoint buffer size", defaultValue = "1024")
    private long checkPointBufferSize;

    @Option(names = { "-cprlt", "--checkPointReadLockTimeout" }, description = "Checkpoint read lock timeout", defaultValue = "1000")
    private long checkPointReadLockTimeout;

    @Option(names = { "-pe", "--persistenceEnabled" }, description = "Persistence enabled", defaultValue = "true")
    private boolean isPersistenceEnabled;

    @Option(names = { "-se", "--swapEnabled" }, description = "Swap enabled", defaultValue = "false")
    private boolean isSwapEnabled;

    public String getIgniteHome() {
        return igniteHome;
    }

    public WALMode getWalMode() {
        return walMode;
    }

    public int getNbJobs() {
        return nbJobs;
    }

    public int getJobSize() {
        return jobSize;
    }

    public int getOffHeapMemory() {
        return offHeapMemory;
    }

    public int getSubJobSize() {
        return subJobSize;
    }

    public boolean isWriteThrottleEnabled() {
        return writeThrottleEnabled;
    }

    public long getCheckPointBufferSize() {
        return checkPointBufferSize;
    }

    public long getCheckPointReadLockTimeout() {
        return checkPointReadLockTimeout;
    }

    public boolean isPersistenceEnabled() {
        return isPersistenceEnabled;
    }

    public boolean isSwapEnabled() {
        return isSwapEnabled;
    }

}