import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.schedulers.ScriptingScheduler;
import nl.esciencecenter.xenon.credentials.PasswordCredential;
import nl.esciencecenter.xenon.schedulers.JobDescription;
import nl.esciencecenter.xenon.schedulers.JobStatus;
import nl.esciencecenter.xenon.schedulers.QueueStatus;
import nl.esciencecenter.xenon.schedulers.Scheduler;
import okhttp3.OkHttpClient;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class remoteCMD {
    private static final boolean debugging=System.getenv("DEBUGGING").equals("True");
    private static Time miniTime = new Time("30");
    private Queue<Map<String, String>> queueStatus = new ArrayDeque<Map<String, String>>();
    private Map<String, String> partitionInfo = new TreeMap<>();
    private Queue<String> myJobQ = new ArrayDeque<>();
    private int minNode = 8;
    private String partitionName = "defq";
    private Map<NodeCates, Integer> nodesInfo;
    private int reservedNumber = 0;
    private int TimeOut=15000;
    enum NodeCates {
        A,
        I,
        O,
        T
    }
    private OkHttpClient client;

    remoteCMD(Integer MinNode, String PartitionName) {
        minNode = MinNode;
        partitionName = PartitionName;
        client=new OkHttpClient();
    }

    remoteCMD() {
        client=new OkHttpClient();
    }

    private Map<NodeCates, Integer> parseStatus(String value) {
        Map<NodeCates, Integer> parse = new HashMap<NodeCates, Integer>();

        String[] cates = value.split("/");
        parse.put(NodeCates.A, Integer.parseInt(cates[0]));
        parse.put(NodeCates.I, Integer.parseInt(cates[1]));
        parse.put(NodeCates.O, Integer.parseInt(cates[2]));
        parse.put(NodeCates.T, Integer.parseInt(cates[3]));
        return parse;
    }

    private void updateStatus(String PartitionName, Scheduler scheduler) throws XenonException {
        QueueStatus QS = scheduler.getQueueStatus(PartitionName);
        partitionInfo = QS.getSchedulerSpecificInformation();
        nodesInfo = parseStatus(partitionInfo.get("NODES(A/I/O/T)"));
        String[] jobs=scheduler.getJobs(PartitionName);
        queueStatus = new ArrayDeque<>(jobs.length);
        for (String jb : jobs) {

            if (jb.contains("_")) {
                if (jb.contains("[")) {
                    // jb=jb.substring(0,jb.indexOf("_"));
                    continue;
                } else {
                    //continue;
                }
            }
            System.out.println(jb + "\n____________________");
            try {
                JobStatus js = scheduler.getJobStatus(jb);
                System.out.println(js.getSchedulerSpecificInformation());
                if(js.getSchedulerSpecificInformation().containsKey("STATE"))
                {
                    queueStatus.offer( js.getSchedulerSpecificInformation());
                }


                //System.out.println("------------\n"+js.getSchedulerSpecificInformation());

            } catch (Exception e) {
                e.printStackTrace();
            }


        }
        reservedNumber=(int)queueStatus.stream().filter(x->x.get("NAME").equals("Calibration")&&x.get("STATE").equals("RUNNING")).count();


    }

    private Boolean anyPending() {
        return queueStatus.stream().anyMatch(x -> x.get("STATE").equals("PENDING"));
    }

    private void scaling(Scheduler scheduler) throws XenonException, IOException {
        List<Map<String, String>> scalingList;
        if (!anyPending()) {
            Map<String, String> SameJob = new HashMap<>();
            if(debugging)
            {
                SameJob.put("exec","sleep");
                SameJob.put("args","infinity");
            }else {
                SameJob.put("exec",System.getenv("DYNPRVDRIVER_HOME")+"/scripts/ipl-run");
            }

            SameJob.put("arg:p", partitionName);
            SameJob.put("arg:t", "UNLIMITED");
            SameJob.put("arg:J","Calibration");
            scalingList = Collections.nCopies(nodesInfo.get(NodeCates.I), SameJob);
            scalingUp(scalingList, scheduler);
        } else  {

             List<Map<String, String>> pl=queueStatus.stream().filter(x -> x.get("NODELIST(REASON)").contains("Resources")).collect(Collectors.toList());

            if (pl.size() != 0) {
                Map<String, String> pendingJobResource =pl.get(0);
                Time maxTime = maxTimeLimit(pendingJobResource);
                int numberCancel = makeWayBackfill(maxTime, pendingJobResource);
                if (numberCancel > 0) //make a way
                {
                    System.err.println("scaling down "+numberCancel);
                    scalingDown(numberCancel, scheduler);
                } else if (numberCancel < 0) // take them
                {
                    if (maxTime.compareTo(miniTime) > 0)// Long enough
                    {
                        Map<String, String> SameJob = new HashMap<>();
                        if(debugging)
                        {
                            SameJob.put("exec","sleep");
                            SameJob.put("args","infinity");
                        }else {
                            SameJob.put("exec",System.getenv("DYNPRVDRIVER_HOME")+"/scripts/ipl-run");
                        }
                        SameJob.put("arg:p", partitionName);
                        SameJob.put("arg:t", maxTime.Minus(new Time("2")).toString());
                        SameJob.put("arg:J","Calibration");
                        scalingList = Collections.nCopies(nodesInfo.get(NodeCates.I), SameJob);
                        scalingUp(scalingList, scheduler);
                    }
                }// else if numberCancel==0 when other pending jobs can be filled in
            }

        }
    }

    private Time maxTimeLimit(Map<String, String> PendingJobResource) {
        int requiredNodes = Integer.parseInt(PendingJobResource.get("NODES")) - nodesInfo.get(NodeCates.I);
        Time MaxTime = new Time("UNLIMITED");
        for (Map<String, String> job : queueStatus.stream().filter(x -> x.get("STATE").equals("RUNNING")).sorted(jobComp).collect(Collectors.toList())) {
            MaxTime = parseTime(job);
            requiredNodes -= Integer.parseInt(job.get("NODES"));
            if (requiredNodes <= 0) {
                break;
            }
        }
        return MaxTime;
    }

    private Time mostSoonTime() {
        return parseTime(queueStatus.stream().filter(x -> x.get("STATE").equals("RUNNING")).sorted(jobComp).limit(0).collect(Collectors.toList()).get(0));
    }

    private int makeWayBackfill(Time MaxTime, Map<String, String> PendingJobResource) throws IOException {
        // filter jobs possibly to run
        List<Map<String,String>> pendingJobs=queueStatus.stream().filter(x -> x.get("NODELIST(REASON)").contains("Priority")).filter(x-> parseTime(x).compareTo(MaxTime) < 0).collect(Collectors.toList());
        int idleNodesNum=nodesInfo.get(NodeCates.I);
        if(reservedNumber<minNode)
        {
            logNotChange("Reserve "+reservedNumber+" node, which smaller than miniNode"+minNode+", ask for "+idleNodesNum+"idle nodes");
            return -idleNodesNum;
        }
        if((Integer.parseInt(PendingJobResource.get("NODES"))-idleNodesNum)<=(reservedNumber-minNode))
        {
            // make a way for job waiting for resource
            logNotChange("Make a way for job waiting for "+PendingJobResource.get("NODES")+" nodes, give out "+(Integer.parseInt(PendingJobResource.get("NODES"))-idleNodesNum));
            return (Integer.parseInt(PendingJobResource.get("NODES"))-idleNodesNum);
        }
        if(pendingJobs.size()>0)
        {
            Optional<Time> MostCloseJob=queueStatus.stream().filter(x->x.get("STATE").equals("RUNNING")).map(this::parseTime).min(Time::compareTo);
            if(MostCloseJob.isPresent()&& MostCloseJob.get().compareTo(miniTime) < 0)
            {
                // if there are running jobs will finish soon
                // Then do not change
                logNotChange("Running jobs will finish in:"+MostCloseJob.get());
                return 0;
            }
        }

        for(Map<String,String> job:pendingJobs)
        {

            if(new Time(job.get("TIME_LIMIT")).compareTo(MaxTime) < 0)
            {
                if(Integer.parseInt(job.get("NODES"))<=idleNodesNum)
                {
                    // Dont change wait for SLURM schedule it
                    System.out.println("*************\n\n\n\n");
                    System.out.println("DONT MOVE");
                    System.out.println("Job:"+job);
                    System.out.println("MaxTime:"+MaxTime);
                    System.out.println("Parsed Time:"+parseTime(job));
                    System.out.println("IDEL NODES:"+idleNodesNum);
                    System.out.println("\n\n\n\n*************");
                    logNotChange("There is a pending job will be filled in soon");
                    return 0;
                }
                if((Integer.parseInt(job.get("NODES"))-idleNodesNum)<=(reservedNumber-minNode))
                {
                    // make a way
                    System.out.println("*************\n\n\n\n");
                    System.out.println("MAKE A WAY");
                    System.out.println("Job:"+job);
                    System.out.println("MaxTime:"+MaxTime);
                    System.out.println("Parsed Time:"+parseTime(job));
                    System.out.println("IDEL NODES:"+idleNodesNum);
                    System.out.println("\n\n\n\n*************");
                    logNotChange("make a way for pending job that can be back filled,requries "+(Integer.parseInt(job.get("NODES"))-idleNodesNum));
                    return (Integer.parseInt(job.get("NODES"))-idleNodesNum);

                }

            }

        }
        // no job can be filled in
        // Xenon take it
        logNotChange("No job can be filled in Xenon take it, with node numbers"+idleNodesNum);
        return -idleNodesNum;
    }

    private Comparator<Map<String, String>> jobComp = new Comparator<Map<String, String>>() {
        @Override
        public int compare(Map<String, String> o1, Map<String, String> o2) {
            Time t1=parseTime(o1);
            Time t2=parseTime(o2);
            if(t1.isUnlimit())
            {
                if(t2.isUnlimit())
                {
                    return Integer.parseInt(o2.get("JOBID"))<Integer.parseInt(o1.get("JOBID"))?-1:1;
                }
                else
                {
                    return 1;
                }
            }else
            {
                return t1.compareTo(t2);
            }
        }
    };

    private void scalingUp(List<Map<String, String>> scaleQueue, Scheduler scheduler) throws XenonException, IOException {
        for (Map<String, String> job : scaleQueue) {

            JobDescription jobDescription = new JobDescription();
            jobDescription.setExecutable(job.get("exec"));
            if(job.containsKey("args"))
            {
                jobDescription.setArguments(job.get("args"));
            }
            Long time=System.currentTimeMillis();
            jobDescription.setStderr(System.getenv("NODE_LOG_DIR")+time.toString()+".err");
            jobDescription.setStdout(System.getenv("NODE_LOG_DIR")+time.toString()+".out");
            jobDescription.setWorkingDirectory(System.getenv("DYNPRVDRIVER_HOME"));
            Map env=new HashMap<String, String>();
            env.put("DYNPRVDRIVER_HOME",System.getenv("DYNPRVDRIVER_HOME"));
            jobDescription.setEnvironment(env);
            System.out.println(jobDescription.getSchedulerArguments());
            String schedulerArgs = job.entrySet().stream().filter(x -> x.getKey().contains("arg:")).map(x -> "-" + x.getKey().substring(4) + " " + x.getValue()).reduce((x, y) -> x + " " + y).get();
            // schedulerArgs=String.join(" ",job.);
            jobDescription.setSchedulerArguments(schedulerArgs);
            String jobId = scheduler.submitBatchJob(jobDescription);
            // Runtime.getRuntime().exec("scontrol update jobid=<job_id> TimeLimit=<new_timelimit>".replace("<job_id>",jobId).replace("<new_timelimit>","UNLIMITED"));

            JobStatus js=scheduler.waitUntilRunning(jobId, TimeOut);
            if(!js.isRunning())
            {

                scheduler.cancelJob(jobId);
                updateStatus(partitionName,scheduler);
                TimeOut+=5000;
                break;
            }
            TimeOut=15000;

            // myJobQ.offer(jobId);

        }
    }

    private void scalingDown(int number, Scheduler scheduler) throws XenonException {
        List<Map<String,String>> runningList = queueStatus.stream()
                .filter(x -> x.get("STATE").equals("RUNNING")).collect(Collectors.toList());
        for(Map<String,String> job:runningList)
        {
            System.err.print(String.join(";","JobId="+job.get("JOBID"),"JobName"+job.get("NAME"),"Node="+job.get("NODES")));
            System.err.print("\n");
        }
        List<String> cancelList = queueStatus.stream()
                .filter(x -> x.get("STATE").equals("RUNNING") & x.get("NAME").equals("Calibration"))
                .sorted(jobComp)
                .limit(number).map(x->x.get("JOBID")).collect(Collectors.toList());
        for (String jobID : cancelList) {
            scheduler.cancelJob(jobID);
            //scheduler.cancelJob()
            System.err.println("Cancel job"+jobID);

        }
    }

    private Time parseTime(Map<String, String> JobStatus) {
        return new Time(JobStatus.get("TIME_LIMIT")).Minus(new Time(JobStatus.get("TIME")));
    }



    public void run(Scheduler scheduler) {
        while (true) {
            try {
                updateStatus(partitionName, scheduler);
                if(!debugging)
                {
                    updateMiniNode();
                }
                scaling(scheduler);
            } catch (XenonException | IOException e) {
                e.printStackTrace();
            }
        }
    }
    public void updateMiniNode() {
        String miniNode=ServiceUtil.getJobStatus("http://" +System.getenv("IPL_ADDRESS")+":5000/miniNode",client);
        StringBuilder sb=new StringBuilder(miniNode);
        sb.deleteCharAt(sb.length()-1);
        sb.deleteCharAt(sb.length()-1);
        sb.deleteCharAt(0);
        int tmpNode=Integer.parseInt(sb.toString());
        if(tmpNode>0)
        {
            minNode=tmpNode;
        }

    }
    public void logNotChange(String reason) throws IOException {
        BufferedWriter out=new BufferedWriter(new FileWriter(System.getenv("DEBUGGING_FILE"),true));
        out.write(String.join("\t","Time="+System.currentTimeMillis(),"Idle="+nodesInfo.get(NodeCates.I),"Reason="+reason));
        out.newLine();
        out.close();
    }
    public static void main(String[] args) throws Exception {

        // Assume the remote system is actually just a Docker container (e.g.
        // https://hub.docker.com/r/xenonmiddleware/slurm/), accessible to user 'xenon' via
        // port 10022 on localhost, using password 'javagat'
        String location = System.getenv("XENON_LOCATION");
        String username = System.getenv("XENON_USERNAME");
        String password = System.getenv("XENON_PWD");
        // PasswordCredential c=new PasswordCredential(username,password);
        PasswordCredential credential = new PasswordCredential(username, password);
        // create the SLURM scheduler representation
        Scheduler scheduler = ScriptingScheduler.create("slurm", location, credential);
        // QueueStatus QS=scheduler.getQueueStatus("defq");

        remoteCMD rCMD = new remoteCMD(10, "defq");
        rCMD.run(scheduler);
        scheduler.close();


    }
}
