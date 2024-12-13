
package com.example;

import java.security.SecurityPermission;

import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.apache.ignite.ShutdownPolicy;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.cluster.ClusterState;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.DataStorageConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.plugin.security.SecurityPermissionSet;
import org.apache.ignite.plugin.security.SecurityPermissionSetBuilder;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "IgniteWALExample", mixinStandardHelpOptions = true, description = "Example application using Apache Ignite with WAL mode options.")
public class IgniteWALExample implements Runnable {

    @CommandLine.Mixin
    private CommandLineOptions options = new CommandLineOptions();

    public static final String IGNITE_NAME = "cluster";

    public static void main(String[] args) {
        CommandLine cmd = new CommandLine(new IgniteWALExample());
        cmd.execute(args);
    }

    @Override
    public void run() {

        // Create a new Ignite configuration
        IgniteConfiguration cfg = new IgniteConfiguration();
        cfg.setIgniteInstanceName(IGNITE_NAME);
        cfg.setShutdownPolicy(ShutdownPolicy.GRACEFUL);
        // Set Ignite home directory
        String igniteHome = options.getIgniteHome();
        cfg.setIgniteHome(igniteHome);

        // Set the custom failure handler
        cfg.setFailureHandler(new CustomFailureHandler());

        // Enable persistence
        DataStorageConfiguration storageCfg = new DataStorageConfiguration();
        storageCfg.getDefaultDataRegionConfiguration().setName("main").setPersistenceEnabled(true).setMaxSize(250 * 1024 * 1024); // 50MB
        storageCfg.setWalMode(options.getWalMode());

        cfg.setDataStorageConfiguration(storageCfg);

        // Set the cache configuration
        CacheConfiguration cacheCfg = new CacheConfiguration();
        cacheCfg.setName("exampleCache");
        cacheCfg.setCacheMode(CacheMode.PARTITIONED);
        cacheCfg.setDataRegionName("main");
        cfg.setCacheConfiguration(cacheCfg);

   
        // Start an Ignite node
        try (Ignite ignite = Ignition.start(cfg)) {
            System.out.println("Ignite home: " + ignite.configuration().getWorkDirectory());
            ignite.cluster().state(ClusterState.ACTIVE);
            // Get or create a cache
            var cache = ignite.getOrCreateCache("exampleCache");

            System.out.println(String.format("Cache: %s size %,d", cache.getName(), cache.size()));
            // Execute a ComputeTask
            int result = ignite.compute().execute(SampleComputeTask.class, 10);
            System.out.println("Compute task result: " + result);
        }
    }
}
