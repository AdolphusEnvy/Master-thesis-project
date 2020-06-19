package nl.esciencecenter.DynPrvDriver;

import java.io.File;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class Task implements Serializable {
    private String srcLocation;
    private String parameters;
    private int jobID;
    private Queue<File> fileQueue = new LinkedList<File>();
    private String taskID;
    private File[] fileArray;
    private String jobType;
    private String executable;

    public Task(String SrcLocation, String Parameters, String JobType, String Executable, int JobID, String TaskID) {
        srcLocation = SrcLocation;
        parameters = Parameters;
        jobID = JobID;
        taskID = TaskID;
        jobType = JobType;
        executable = Executable;
    }

    public int getQueueSize() {
        return fileQueue.size();
    }

    public void LoadFile(File f) {
        fileQueue.offer(f);
    }

    public Queue<File> ListFile() {
        return fileQueue;
    }

    public String getSrcLocation() {
        return srcLocation;
    }

    public String getTaskID() {
        return taskID;
    }

    public int getJobID() {
        return jobID;
    }

    public void loadToArray() {
//        Iterator i = fileQueue.iterator();
//        while(i.hasNext())
//        {
//
//        }
        fileArray = new File[fileQueue.size()];
        fileQueue.toArray(fileArray);
    }

    public String getParameters() {
        return parameters;
    }

    public String getJobType() {
        return jobType;
    }

    public String getExecutable() {
        return executable;
    }

    public File[] getFileArray() {
        return fileArray;
    }
}
