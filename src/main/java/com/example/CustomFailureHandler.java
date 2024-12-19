package com.example;

import java.io.File;

import org.apache.ignite.Ignite;
import org.apache.ignite.failure.FailureContext;
import org.apache.ignite.failure.StopNodeOrHaltFailureHandler;
import org.apache.ignite.internal.processors.cache.persistence.StorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomFailureHandler extends StopNodeOrHaltFailureHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(CustomFailureHandler.class);
    
    
    @Override
    public boolean handle(Ignite ignite, FailureContext failureCtx) {
        
        logger.info("CustomFailureHandler called");

        
        // Custom logic for handling failure
        if (failureCtx.error() instanceof StorageException && failureCtx.error().getMessage()
                .contains("Failed to read checkpoint record from WAL, persistence consistency cannot be guaranteed.")) {
            logger.error("*******************  DELETE THE WAL DIRECTORY *******************");

            String workDir =  ignite.configuration().getWorkDirectory();
            deleteDirectory(new File(workDir));

        } else {
            logger.info("Exception is not managed by the custom handler. Call the super method.");
        }

        
        // Call the super method to retain the original behavior
        return super.handle(ignite, failureCtx);
    }

    private void deleteDirectory(File directory) {
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteDirectory(file);
                }
            }
        }
        directory.delete();
    }
}