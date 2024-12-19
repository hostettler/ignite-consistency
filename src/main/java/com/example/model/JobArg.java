package com.example.model;

public class JobArg {

    private int nbJobs;
    private int jobSize;
    private int jobSubsize;

    public JobArg(int jobArg, int jobSize, int jobSubsize) {
        this.nbJobs = jobArg;
        this.jobSize = jobSize;
        this.jobSubsize = jobSubsize;
    }

    public int getNbJobs() {
        return nbJobs;
    }

    public int getJobSize() {
        return jobSize;
    }

    public int getJobSubsize() {
        return jobSubsize;
    }
}
