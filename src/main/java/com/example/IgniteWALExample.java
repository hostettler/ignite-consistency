
package com.example;

import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.apache.ignite.ShutdownPolicy;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.cluster.ClusterState;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.DataRegionConfiguration;
import org.apache.ignite.configuration.DataStorageConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.configuration.NoOpWarmUpConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.model.JobArg;
import com.example.model.Person;
import com.example.model.PersonKey;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "IgniteWALExample", mixinStandardHelpOptions = true, description = "Example application using Apache Ignite with WAL mode options.")
public class IgniteWALExample implements Runnable {

    
    public static final String DIRECT_CACHE="DIRECT_CACHE_PERSON";
    public static final String STREAMER_CACHE="STREAMER_CACHE_PERSON";
    
    private static final Logger logger = LoggerFactory.getLogger(IgniteWALExample.class);
    
    @CommandLine.Mixin
    private CommandLineOptions options = new CommandLineOptions();

    public static final String IGNITE_NAME = "cluster";

    public static void main(String[] args) {
        CommandLine cmd = new CommandLine(new IgniteWALExample());
        cmd.execute(args);
    }

    @Override
    public void run() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("**************** Shutdown hook called  ****************");
        }));
        
        logger.info(String.format("Ignite WAL Example initialized with %d jobs, job size %,d and sub-job size %,d", options.getNbJobs(), options.getJobSize(), options.getSubJobSize()));
        

        // Create a new Ignite configuration
        IgniteConfiguration cfg = new IgniteConfiguration();
        cfg.setIgniteInstanceName(IGNITE_NAME);
        cfg.setShutdownPolicy(ShutdownPolicy.IMMEDIATE);
        // Set Ignite home directory
        String igniteHome = options.getIgniteHome();
        cfg.setIgniteHome(igniteHome);

        // Set the custom failure handler
        cfg.setFailureHandler(new CustomFailureHandler());
        cfg.setConsistentId(IGNITE_NAME);

        
        DataRegionConfiguration mainRegionCfg = new DataRegionConfiguration();
        mainRegionCfg.setName("main");
        mainRegionCfg.setPersistenceEnabled(options.isPersistenceEnabled());
        mainRegionCfg.setMaxSize(options.getOffHeapMemory() * 1024L * 1024L); // 512 MB
        if (options.isPersistenceEnabled()) {
            logger.info("Persistence enabled");
            mainRegionCfg.setWarmUpConfiguration(new NoOpWarmUpConfiguration());
            mainRegionCfg.setCheckpointPageBufferSize(options.getCheckPointBufferSize() *1024L * 1024L);
        } else if(options.isSwapEnabled()) {
            logger.info("Swap enabled");
            mainRegionCfg.setSwapPath("swap");
        }

        // Enable persistence
        DataStorageConfiguration storageCfg = new DataStorageConfiguration();
        storageCfg.setWalMode(options.getWalMode());    
        storageCfg.setWriteThrottlingEnabled(options.isWriteThrottleEnabled());
        storageCfg.setCheckpointReadLockTimeout(options.getCheckPointReadLockTimeout());
        storageCfg.setDataRegionConfigurations(mainRegionCfg);        
        cfg.setDataStorageConfiguration(storageCfg);
        

        // Set the cache configuration
        CacheConfiguration<PersonKey, Person> personDirectCacheCfg = new CacheConfiguration();
        personDirectCacheCfg.setName(DIRECT_CACHE);
        personDirectCacheCfg.setIndexedTypes(PersonKey.class, Person.class);
        personDirectCacheCfg.setCacheMode(CacheMode.PARTITIONED);
        personDirectCacheCfg.setDataRegionName("main");

        
        
        CacheConfiguration<PersonKey, Person> personStreamerCacheCfg = new CacheConfiguration();
        personStreamerCacheCfg.setName(STREAMER_CACHE);
        personStreamerCacheCfg.setIndexedTypes(PersonKey.class, Person.class);
        personStreamerCacheCfg.setCacheMode(CacheMode.PARTITIONED);
        personStreamerCacheCfg.setDataRegionName("main");

        
        cfg.setCacheConfiguration(personStreamerCacheCfg, personDirectCacheCfg);

        JobArg jobArg = new JobArg(options.getNbJobs(), options.getJobSize(), options.getSubJobSize());
        
        // Start an Ignite node
        try (Ignite ignite = Ignition.start(cfg)) {
            logger.info(String.format("Ignite home: %s", ignite.configuration().getWorkDirectory()));
            ignite.cluster().state(ClusterState.ACTIVE);
            
            logger.info(String.format("Ignite node set to %s", ignite.cluster().state()));
            
            // Get or create a cache
            var cache = ignite.getOrCreateCache(DIRECT_CACHE);
            logger.info(String.format("Main Cache: %s -  size %,d", cache.getName(), cache.size()));
            
            
            var streamerCache = ignite.getOrCreateCache(STREAMER_CACHE);
            logger.info(String.format("Streamer Cache: %s -  size %,d", streamerCache.getName(), streamerCache.size()));

            
            // Execute a ComputeTask
            int result = ignite.compute().execute(SampleComputeTask.class, jobArg);
            logger.info("Compute task result: " + result);
            
            logger.info(String.format("Main Cache: %s -  size %,d", cache.getName(), cache.size()));
            logger.info(String.format("Streamer Cache: %s -  size %,d", streamerCache.getName(), streamerCache.size()));

            ignite.cluster().state(ClusterState.INACTIVE);
        }
    }
}
