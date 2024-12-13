package com.example;

import org.apache.ignite.configuration.WALMode;

import picocli.CommandLine.Option;

public class CommandLineOptions {

    @Option(names = {"-ih", "--igniteHome"}, description = "Ignite home directory", defaultValue = "${sys:java.io.tmpdir}/ignite")
    private String igniteHome;
    
    @Option(names = {"-wm", "--walMode"}, description = "WAL mode, default is NONE", defaultValue = "NONE")
    private WALMode walMode;
    
    public String getIgniteHome() {
        return igniteHome;
    }
    public WALMode getWalMode() {
        return walMode;
    }

}