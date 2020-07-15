import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.schedulers.ScriptingScheduler;
import nl.esciencecenter.xenon.credentials.PasswordCredential;
import nl.esciencecenter.xenon.schedulers.JobDescription;
import nl.esciencecenter.xenon.schedulers.Scheduler;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Simulation {
    private static final boolean debugging=System.getenv("DEBUGGING").equals("True");
    private Queue<SimuJob> simulationQueue=new LinkedList<>();
    private String partitionName;
    private long checkpoint;
    Simulation(String PartitionName)
    {
        partitionName=PartitionName;
        checkpoint=System.currentTimeMillis();
    }
    public static void main(String[] args) throws XenonException, IOException, InterruptedException {
        String location = System.getenv("XENON_LOCATION");
        String username = System.getenv("XENON_USERNAME");
        String password = System.getenv("XENON_PWD");
        System.out.println(String.join("-",location,username,password));
        // PasswordCredential c=new PasswordCredential(username,password);
        PasswordCredential credential = new PasswordCredential(username, password);
        // create the SLURM scheduler representation
        Scheduler scheduler = ScriptingScheduler.create("slurm", location, credential);

        Thread monitorThread= new Thread(()-> {
            try {
                new Monitor().run(System.getenv("LOG_FILE_PATH"));
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        });
        monitorThread.start();
        Simulation simulation= new Simulation("defq");
        simulation.loadSubmitSequnce(System.getenv("SUBMIT_FILE_PATH"));
        // simulation.MIPMode(scheduler);
        simulation.ScaleMode(scheduler);
        // simulation.randomSimulation(scheduler);
        Thread.sleep(600000);
        monitorThread.interrupt();
        scheduler.close();
    }
    public void addJob(SimuJob jobEntry)
    {
        simulationQueue.offer(jobEntry);
    }
    public void MIPMode(Scheduler scheduler) throws XenonException, InterruptedException {
        long start =System.currentTimeMillis();
        SimuJob sj = simulationQueue.peek();
        while (sj!=null){
            if(start+(sj.getStartTime()*1000)<System.currentTimeMillis())
            {
                sj=simulationQueue.poll();
                assert sj != null;
                System.out.println("SUBMIT JOB AT START TIME:"+sj.getStartTime());
                String jobID=submitOneJob(scheduler,sj);
            }
            sj = simulationQueue.peek();
        }
        Thread.sleep(1800*1000);
    }
    public void ScaleMode(Scheduler scheduler) throws XenonException, InterruptedException {
        Thread p=new Thread(()->{remoteCMD rm=new remoteCMD(4,"defq");
                                    rm.run(scheduler);});
        p.start();
        long start =System.currentTimeMillis();
        SimuJob sj = simulationQueue.peek();
        while (sj!=null){
            if(start+(sj.getStartTime())*1000<System.currentTimeMillis())
            {
                sj=simulationQueue.poll();
                assert sj != null;
                System.out.println("SUBMIT JOB AT START TIME:"+sj.getStartTime());
                if(sj.getTypeFlag())
                {
                    /*
                    TODO: submit to web service
                     */
                    if(debugging)
                    {
                        System.out.println("\n---submit calibration job:Debug---\n");
                    }else {
                        String log=submitCalibrationJob(scheduler,sj);
                        System.out.println("\n---submit calibration job:Real submit---\n"+log);
                    }


                }
                else
                {
                    String jobID=submitOneJob(scheduler,sj);
                }
            Thread.sleep(500);
            }
            sj = simulationQueue.peek();
        }
        Thread.sleep(1800*1000);
        p.interrupt();

    }
    private String submitOneJob(@NotNull Scheduler Scheduler, @NotNull SimuJob job) throws XenonException {

        JobDescription jobDescription = new JobDescription();
        jobDescription.setExecutable("sleep");
        jobDescription.setArguments(job.getRealTime());
        jobDescription.setTasks(job.getNodeNum());
        jobDescription.setTasksPerNode(1);
        jobDescription.setSchedulerArguments("--job-name="+(job.getTypeFlag()?"Calibration":"Normal")+" --time="+job.getTimeLimit());
        String jobid=Scheduler.submitBatchJob(jobDescription);
        try
        {
            BufferedWriter out=new BufferedWriter(new FileWriter("JobSubmit.log",true));
            out.write(String.join("\t","NodeNum="+job.getNodeNum(),"Type="+(job.getTypeFlag()?"Calibration":"Normal"),"TimeLimit="+job.getTimeLimit(),"RealTime="+job.getRealTime(),"SubmitTimeStamp="+(System.currentTimeMillis()-checkpoint),"JobID="+jobid));
            out.newLine();
            out.close();
        } catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return jobid;
    }
    public int countPD(String info) {
        int count = 0;
        Pattern p = Pattern.compile("PD");
        Matcher m = p.matcher(info);
        while (m.find()) {
            count++;
        }
        return count;
    }
    public void randomSimulation(Scheduler scheduler) throws IOException, XenonException, InterruptedException {
        long startTime=System.currentTimeMillis()/1000;
        Monitor m=new Monitor();
        Random rand = new Random();
        while ((startTime + 86400/2) >= System.currentTimeMillis() / 1000) {
            String info = m.getStatus();

            int count = countPD(info);
            if (randomPendJob(count,rand)) {
                if ((rand.nextInt(10)  >1)) {
                    int nodeNum = rand.nextInt(8) + 2;

                    Time timeLimit = new Time((45 + rand.nextInt(180)) * 60);
                    Time realTime = timeLimit.Minus(new Time(150 + rand.nextInt(900)));
                    SimuJob newJob = new SimuJob(nodeNum, rand.nextInt(100)<10?"UNLIMITED":timeLimit.toString(), realTime.toString(), "0-0:0:0", false);
                    submitOneJob(scheduler, newJob);
                } else {
                    Time timeLimit = new Time((5) * 60 * 60);
                    Time realTime = timeLimit.Minus(new Time(150 + rand.nextInt(300)));
                    SimuJob newJob = new SimuJob(5, timeLimit.toString(), realTime.toString(), "0-0:0:0", true);
                    submitOneJob(scheduler, newJob);
                }
            }
            Thread.sleep(1000+60000*count);
        }

    }
    public Boolean randomPendJob(int pdCount,Random rand) {

        if(pdCount==0)
        {
            return rand.nextInt(10)<2;
        }
        if (pdCount==1)
        {
            return rand.nextInt(100)<5;
        }
        return (rand.nextInt(200)-195)>pdCount;
    }
    public void loadSubmitSequnce(String fileName) throws IOException {
        File file = new File(fileName);
        if (file.isFile() && file.exists())
        {
            InputStreamReader read = new InputStreamReader(
                    new FileInputStream(file));
            BufferedReader bufferedReader = new BufferedReader(read);
            String lineTxt = null;

            while ((lineTxt = bufferedReader.readLine()) != null)
            {
                simulationQueue.offer(parseLine(lineTxt));
            }
            bufferedReader.close();
            read.close();
        }
        else
        {
            System.out.println("File not found!");
        }
    }
    public SimuJob parseLine(@NotNull String stringLine) {
        String[] tokens=stringLine.split("\t");
        Boolean type=tokens[1].contains("Calibration");
        int nodeNum=Integer.parseInt(tokens[0].split("=")[1]);
        Time timeLimit=new Time(tokens[2].split("=")[1]);
        if(!timeLimit.isUnlimit())
        {
            timeLimit.setHour(timeLimit.getHour()+1);
            timeLimit.setMinute(0);
            timeLimit.setSecond(0);
        }
        Time realTime=new Time(Integer.parseInt(tokens[3].split("=")[1]));
        Time startTime=new Time(Integer.parseInt(tokens[4].split("=")[1])/1000);
        SimuJob simuJob=new SimuJob(nodeNum, timeLimit.toString(), realTime.toString(), startTime.toString(), type);
        if(tokens.length>5)
        {
            simuJob.setParameter(tokens[5].split("=")[1]);
        }
        return simuJob;
    }
    public String submitCalibrationJob(Scheduler Scheduler, @NotNull SimuJob job) throws XenonException {

        try
        {
            BufferedWriter out=new BufferedWriter(new FileWriter("JobSubmit.log",true));
            out.write(String.join("\t","NodeNum="+job.getNodeNum(),"Type="+(job.getTypeFlag()?"Calibration":"Normal"),"TimeLimit="+job.getTimeLimit(),"RealTime="+job.getRealTime(),"SubmitTimeStamp="+(System.currentTimeMillis()-checkpoint)));
            out.newLine();
            out.close();
        } catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
}

