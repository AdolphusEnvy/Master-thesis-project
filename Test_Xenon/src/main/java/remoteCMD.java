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
        //reservedNumber=(int)queueStatus.stream().filter(x->x.get("NAME").equals("xenon")&&x.get("STATE").equals("RUNNING")).count();


    }

    private Boolean anyPending() {
        return queueStatus.stream().anyMatch(x -> x.get("STATE").equals("PENDING"));
    }

    private void scaling(Scheduler scheduler) throws XenonException, IOException {
        List<Map<String, String>> scalingList;
        if (!anyPending()) {
            Map<String, String> SameJob = new HashMap<>();
            SameJob.put("exec",System.getenv("DYNPRVDRIVER_HOME")+"/scripts/ipl-run");
//            SameJob.put("args","36000");
            SameJob.put("arg:p", partitionName);
            SameJob.put("arg:t", "UNLIMITED");
            SameJob.put("arg:J","Calibration");
            scalingList = Collections.nCopies(nodesInfo.get(NodeCates.I), SameJob);
            scalingUp(scalingList, scheduler);
//            BufferedWriter writer = new BufferedWriter(new FileWriter("log",true));
//            writer.write("Scaling up:"+nodesInfo.get(NodeCates.I));
//            writer.write("\n");
//
//            writer.close();
        } else  {

             List<Map<String, String>> pl=queueStatus.stream().filter(x -> x.get("NODELIST(REASON)").contains("Resources")).collect(Collectors.toList());

            if (pl.size() != 0) {
                Map<String, String> pendingJobResource =pl.get(0);
                Time maxTime = maxTimeLimit(pendingJobResource);
                int numberCancel = makeWayBackfill(maxTime, pendingJobResource);
                if (numberCancel > 0) //make a way
                {
                    scalingDown(numberCancel, scheduler);
                } else if (numberCancel < 0) // take them
                {
                    if (maxTime.compareTo(miniTime) == 1)// Long enough
                    {
                        Map<String, String> SameJob = new HashMap<>();
                        SameJob.put("exec",System.getenv("DYNPRVDRIVER_HOME")+"/scripts/ipl-run");
                        //SameJob.put("args","36000");
                        SameJob.put("arg:p", partitionName);
                        SameJob.put("arg:t", maxTime.Minus(new Time("2")).toString());
                        SameJob.put("arg:J","Calibration");
                        scalingList = Collections.nCopies(nodesInfo.get(NodeCates.I), SameJob);
                        scalingUp(scalingList, scheduler);
                    }
                }// else if numberCencel==0 when other pending jobs can be filled in
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

    private int makeWayBackfill(Time MaxTime, Map<String, String> PendingJobResource) {
        // filter jobs possibly to run
        List<Map<String,String>> pendingJobs=queueStatus.stream().filter(x -> x.get("NODELIST(REASON)").contains("Priority")).filter(x-> parseTime(x).compareTo(MaxTime) < 0).collect(Collectors.toList());
        int idleNodesNum=nodesInfo.get(NodeCates.I);
        if(reservedNumber<minNode)
        {
            return -idleNodesNum;
        }
        if((Integer.parseInt(PendingJobResource.get("NODES"))-idleNodesNum)<=(reservedNumber-minNode))
        {
            // make a way for job waiting for resource
            return (Integer.parseInt(PendingJobResource.get("NODES"))-idleNodesNum);
        }
        if(pendingJobs.size()>0)
        {
            Optional<Time> MostCloseJob=queueStatus.stream().filter(x->x.get("STATE").equals("RUNNING")).map(this::parseTime).min(Time::compareTo);
            if(MostCloseJob.isPresent()&&MostCloseJob.get().compareTo(miniTime)==-1)
            {
                // if there are running jobs will finish soon
                // Then do not change
                return 0;
            }
        }

        for(Map<String,String> job:pendingJobs)
        {

            if(parseTime(job).compareTo(MaxTime) == -1)
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
                    return (Integer.parseInt(job.get("NODES"))-idleNodesNum);

                }

            }

        }
        // no job can be filled in
        // Xenon take it
        return -idleNodesNum;
    }

    private Comparator<Map<String, String>> jobComp = new Comparator<Map<String, String>>() {
        @Override
        public int compare(Map<String, String> o1, Map<String, String> o2) {
            return parseTime(o1).compareTo(parseTime(o2));
        }
    };

    private void scalingUp(List<Map<String, String>> scaleQueue, Scheduler scheduler) throws XenonException, IOException {
        for (Map<String, String> job : scaleQueue) {

            JobDescription jobDescription = new JobDescription();
            jobDescription.setExecutable(job.get("exec"));
            jobDescription.setArguments(job.get("args"));
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
            reservedNumber+=1;
            TimeOut=15000;

            // myJobQ.offer(jobId);

        }
    }

    private void scalingDown(int number, Scheduler scheduler) throws XenonException {
        List<String> cancelList = queueStatus.stream()
                .filter(x -> x.get("STATE").equals("RUNNING") & x.get("NAME").equals("xenon"))
                .sorted(Comparator.comparing(a -> parseTime(a)))
                .limit(number).map(x->x.get("JOBID")).collect(Collectors.toList());
        for (String jobID : cancelList) {
            scheduler.cancelJob(jobID);
            //scheduler.cancelJob()

            reservedNumber-=1;
        }
    }

    private Time parseTime(Map<String, String> JobStatus) {
        return new Time(JobStatus.get("TIME_LIMIT")).Minus(new Time(JobStatus.get("TIME")));
    }



    public void run(Scheduler scheduler) {
        while (true) {
            try {
                updateStatus(partitionName, scheduler);
                updateMiniNode();
                scaling(scheduler);
            } catch (XenonException | IOException e) {
                e.printStackTrace();
            }
        }
    }
    public void updateMiniNode() {
        int tmpNode=Integer.parseInt(ServiceUtil.getJobStatus(System.getenv("IPL_ADDRESS"),client));
        if(tmpNode>0)
        {
            minNode=tmpNode;
        }
    }
    public static void main(String[] args) throws Exception {

        // Assume the remote system is actually just a Docker container (e.g.
        // https://hub.docker.com/r/xenonmiddleware/slurm/), accessible to user 'xenon' via
        // port 10022 on localhost, using password 'javagat'
        String location = "ssh://fs1.das5.liacs.nl";
        String username = "yhu310";
        String password = "i52nztPF";
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
