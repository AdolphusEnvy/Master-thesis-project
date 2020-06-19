package nl.esciencecenter.DynPrvDriver;

import nl.esciencecenter.xenon.credentials.PasswordCredential;
import nl.esciencecenter.xenon.schedulers.JobDescription;
import nl.esciencecenter.xenon.schedulers.Scheduler;

import java.util.Map;

class ResourceManager implements Runnable {
    private Scheduler scheduler;
    public ResourceManager(String location, PasswordCredential credential) throws Exception
    {
        scheduler=Scheduler.create("slurm", location, credential);

    }
    public void start_one(String Ibis_server) throws Exception
    {
        JobDescription description = new JobDescription();
        description.setExecutable("singularity ");
        description.addEnvironment("IPL_HOME",System.getenv("IPL_HOME"));
        description.setArguments( "run","dynprvdriver:0.2.1","-Dibis.server.address="+Ibis_server,"-Dibis.pool.name=Task","DynPrvDriver.Drive", "-m false", "-sa "+Ibis_server);
        description.setStdout("test1.stdout");
        description.setStderr("test1.err");
        description.setSchedulerArguments();
        description.setTasksPerNode(2);
        description.setCoresPerTask(1);
        description.setTasks(2);
        // submit the job
        String jobId = scheduler.submitBatchJob(description);

    }
    public void  run()
    {
        //start_one();
        try{
            start_one("fs0.das5.cs.vu.nl");
        }
        catch (Exception e)
        {

        }
    }
}