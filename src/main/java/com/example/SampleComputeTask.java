package com.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteAtomicLong;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteDataStreamer;
import org.apache.ignite.compute.ComputeJobAdapter;
import org.apache.ignite.compute.ComputeTaskSplitAdapter;
import org.apache.ignite.resources.IgniteInstanceResource;
import org.apache.ignite.resources.JobContextResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.model.JobArg;
import com.example.model.Person;
import com.example.model.PersonKey;

public class SampleComputeTask extends ComputeTaskSplitAdapter<JobArg, Integer> {

    private static final Logger logger = LoggerFactory.getLogger(SampleComputeTask.class);
    private static final long serialVersionUID = -6542966145522077823L;

    @Override
    protected List<ComputeJobAdapter> split(int gridSize, JobArg arg) {
        List<ComputeJobAdapter> jobs = new ArrayList<>(gridSize);

        for (int i = 0; i < arg.getNbJobs(); i++) {
            jobs.add(new Job(i, arg.getJobSize(), arg.getJobSubsize()));
        }

        return jobs;
    }

    @Override
    public Integer reduce(List<org.apache.ignite.compute.ComputeJobResult> results) {
        int sum = 0;

        for (org.apache.ignite.compute.ComputeJobResult res : results) {
            sum += res.<Integer>getData();
        }

        return sum;
    }

    public static class Job extends ComputeJobAdapter {

        private static final long serialVersionUID = -756274716452031729L;

        private int jobId;
        private int jobSize;
        private int jobSubsize;

        public Job(int jobId, int jobSize, int jobSubsize) {
            this.jobSize = jobSize;
            this.jobSubsize = jobSubsize;
            this.jobId = jobId;
        }

        @IgniteInstanceResource
        transient Ignite ignite;

        @JobContextResource
        private transient org.apache.ignite.compute.ComputeJobContext jobCtx;

        @Override
        public void cancel() {
            super.cancel();
        }

        @Override
        public Object execute() {
            logger.info(String.format("Job %d started", jobId));

            IgniteCache<PersonKey, Person> personCache = ignite.cache(IgniteWALExample.DIRECT_CACHE);

            try (IgniteDataStreamer<PersonKey, Person> streamer = ignite.dataStreamer(IgniteWALExample.STREAMER_CACHE)) {
                for (int i = 0; i < this.jobSize; i++) {

                                      
                    String id = UUID.randomUUID().toString();
                    PersonKey key = new PersonKey(id);
                    Person person = new Person(id, "Person " + id, "Person " + id, 21, "Paris", "FRANCE");

                    personCache.put(key, person);

                    Map<PersonKey, Person> batch = new HashMap<>();
                    for (int j = 0; j < this.jobSubsize; j++) {
                        id = UUID.randomUUID().toString();
                        batch.put(new PersonKey(id), new Person(id, "firstName", "lastName", 21, "city", "country"));
                    }
                    streamer.addData(batch);

                    if (i % (1_000) == 0) {
                        logger.info(String.format("Put %,d entries in job %d. Person Cache size %,d", i, jobId, personCache.size()));
                    }

                    
                    if (i % (1_000) == 0) {
                        streamer.flush();
                    }

                    if (i % 1_000 == 0) {
                        if (this.isCancelled()) {
                            logger.info("************* Job cancelled");
                            return null;
                        }
                    }
                }
            }

            return jobId;
        }
    }

}
