package nl.esciencecenter.DynPrvDriver;


import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class Job implements Comparable<Job>, Serializable {
    private String srcLocation;
    private String parameters;
    private int jobID;
    private Queue<Task> taskQueue;
    private int maxTaskId = 0;

    public int getRunningTask() {
        return runningTask;
    }

    public void setRunningTask(int runningTask) {
        this.runningTask = runningTask;
    }

    private int runningTask = 0;
    private String jobType;
    private String executable;

    public Job(String SrcLocation, String Parameters, int JobID, String JobType, String Executable, boolean init) throws IOException {
        srcLocation = SrcLocation;
        parameters = Parameters;
        jobID = JobID;
        jobType = JobType;
        executable = Executable;
        if (init) {
            LoadTasks(5);
        }

    }

    public int leftTasks() {
        return taskQueue.size();
    }

    public void LoadTasks(Integer BatchSize) throws IOException {
        taskQueue = new LinkedList<Task>();
        int count = 0;
        Task tmp = new Task(srcLocation, parameters, jobType, executable, jobID, String.valueOf(maxTaskId));
        System.out.println(new File(srcLocation).toPath());
        for (File file : new File(srcLocation).listFiles()) {
            if (!file.getName().startsWith("DATA")) {
                continue;
            }
            if (count == BatchSize) {
                tmp.loadToArray();
                taskQueue.offer(tmp);
                maxTaskId += 1;
                tmp = new Task(srcLocation, parameters, jobType, executable, jobID, String.valueOf(maxTaskId));
                count = 0;
            }
            System.out.println("Load file:" + file.getAbsolutePath());
            tmp.LoadFile(file);
            count += 1;

        }
        if (tmp.getQueueSize() > 0) {
            tmp.loadToArray();
            taskQueue.offer(tmp);
        }
    }


    public int getJobID() {
        return jobID;
    }


    public Task PopTask() {
        runningTask += 1;
        return taskQueue.poll();
    }

    public boolean isEmpty() {
        return taskQueue.isEmpty();
    }

    public boolean isFinished() {
        return runningTask == 0 && isEmpty();
    }

    public void loadRedo(Task t) {
        runningTask -= 1;
        taskQueue.offer(t);
    }

    @Override
    public int compareTo(Job o) {
        return this.jobID - o.jobID;
    }

    public void finishOneTask() {
        runningTask -= 1;
    }

}

