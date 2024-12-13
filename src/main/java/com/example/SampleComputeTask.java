package com.example;

import java.util.ArrayList;
import java.util.List;

import org.apache.ignite.Ignite;
import org.apache.ignite.compute.ComputeJobAdapter;
import org.apache.ignite.compute.ComputeTaskSplitAdapter;
import org.apache.ignite.resources.IgniteInstanceResource;
import org.apache.ignite.resources.JobContextResource;

public class SampleComputeTask extends ComputeTaskSplitAdapter<Integer, Integer> {
    private static final long serialVersionUID = -6542966145522077823L;

    @Override
    protected List<ComputeJobAdapter> split(int gridSize, Integer arg) {
        List<ComputeJobAdapter> jobs = new ArrayList<>(gridSize);

        for (int i = 0; i < arg; i++) {
            final int jobArg = i;

            jobs.add(new Job(jobArg));
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

        private int jobArg;

        public Job(int jobArg) {
            this.jobArg = jobArg;
        }

        @IgniteInstanceResource
        transient Ignite ignite;

        @JobContextResource
        private transient org.apache.ignite.compute.ComputeJobContext jobCtx;

        @Override
        public void cancel() {
            // TODO Auto-generated method stub
            super.cancel();
        }

        @Override
        public Object execute() {
            for (int i = 0; i < 10_000_000; i++) {

                ignite.cache("exampleCache").put(String.format("%d %d", i, jobArg), jobArg);
                if (i % 100_000 == 0) {
                    System.out.println(String.format("Put %,d entries in job %d", i, jobArg));
                }

                if (i % 1_000 == 0) {
                    if (this.isCancelled()) {
                        System.out.println("************* Job cancelled");
                        return null;
                    }
                }
            }

            System.out.println("Executing job with argument: " + jobArg);
            return jobArg;
        }
    }

}
