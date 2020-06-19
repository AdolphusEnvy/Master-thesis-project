package nl.esciencecenter.DynPrvDriver;

import ibis.ipl.*;
import org.apache.commons.cli.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import nl.esciencecenter.xenon.credentials.PasswordCredential;
import org.mapdb.*;

public class Driver implements MessageUpcall,RegistryEventHandler {
    PortType one2one = new PortType(PortType.COMMUNICATION_RELIABLE, PortType.SERIALIZATION_OBJECT, PortType.CONNECTION_ONE_TO_ONE, PortType.RECEIVE_EXPLICIT, PortType.COMMUNICATION_FIFO, PortType.RECEIVE_TIMEOUT);
    PortType one2one_UPCALL = new PortType(PortType.COMMUNICATION_RELIABLE, PortType.SERIALIZATION_OBJECT, PortType.CONNECTION_ONE_TO_ONE, PortType.RECEIVE_AUTO_UPCALLS, PortType.COMMUNICATION_FIFO, PortType.RECEIVE_TIMEOUT);

    PortType many2one = new PortType(PortType.COMMUNICATION_RELIABLE, PortType.SERIALIZATION_OBJECT, PortType.CONNECTION_MANY_TO_ONE, PortType.RECEIVE_EXPLICIT, PortType.COMMUNICATION_FIFO, PortType.RECEIVE_TIMEOUT);
    PortType many2one_UPCALL = new PortType(PortType.COMMUNICATION_RELIABLE, PortType.SERIALIZATION_OBJECT, PortType.CONNECTION_MANY_TO_ONE, PortType.RECEIVE_AUTO_UPCALLS, PortType.COMMUNICATION_FIFO, PortType.RECEIVE_TIMEOUT);

    PortType one2many = new PortType(PortType.COMMUNICATION_RELIABLE, PortType.SERIALIZATION_OBJECT, PortType.CONNECTION_ONE_TO_MANY, PortType.RECEIVE_EXPLICIT, PortType.COMMUNICATION_FIFO, PortType.RECEIVE_TIMEOUT);
    PortType one2many_UPCALL = new PortType(PortType.COMMUNICATION_RELIABLE, PortType.SERIALIZATION_OBJECT, PortType.CONNECTION_ONE_TO_MANY, PortType.RECEIVE_AUTO_UPCALLS, PortType.COMMUNICATION_FIFO, PortType.RECEIVE_TIMEOUT);


    IbisCapabilities ibisCapabilities = new IbisCapabilities(IbisCapabilities.MALLEABLE, IbisCapabilities.TERMINATION,
            IbisCapabilities.ELECTIONS_STRICT, IbisCapabilities.MEMBERSHIP_TOTALLY_ORDERED);
    private IbisIdentifier masterId;
    private BTreeMap<Integer, Job> runningJobMap;
    Ibis myibis;
    public static Boolean finished = Boolean.FALSE;
    SendPort masterSendPort;
    ReceivePort controlPort;
    private BlockingQueue<IbisIdentifier> idleWorkerQueue = new LinkedBlockingQueue<>();
    private BTreeMap<IbisIdentifier, Task> runningNodes ;
    private BlockingQueue<Task> workerTaskQueue = new LinkedBlockingQueue<>();
    private boolean masterCrashed =false;
    private boolean firstTime = true;
    private DB db;
    Driver() throws IbisCreationFailedException {
        Properties properties = new Properties();
        myibis = IbisFactory.createIbis(ibisCapabilities, properties, true, this, new PortType[]{one2one, one2one_UPCALL, one2many, one2many_UPCALL, many2one, many2one_UPCALL});
        System.out.println("Ibis created: " + myibis.identifier());
        masterCrashed =false;

    }
    private void server(Ibis myIbis, CommandLine cmd) throws Exception {

        // Init resource manager
        db=DBMaker.fileDB(System.getProperty("user.dir")+"/cache.db").transactionEnable().closeOnJvmShutdown().make();
        //Atomic.Var<Map<IbisIdentifier, Task>> runningJobMap=db.atomicVar("runningJobMap",Map.SERIALIZER)
        runningJobMap= db.treeMap("runningJobMap",Serializer.INTEGER, Serializer.JAVA).createOrOpen();
        runningNodes= db.treeMap("runningNodes",Serializer.JAVA, Serializer.JAVA).createOrOpen();
        if(runningNodes.size()>0)
        {
            for(Task t:runningNodes.values())
            {
                Job tmpJob=runningJobMap.get(t.getJobID());
                tmpJob.loadRedo(t);
                runningJobMap.put(t.getJobID(),tmpJob);
            }
        }
        db.commit();


        if (cmd.getOptionValue("m").equals("false")) {
            String location = "ssh://" + cmd.getOptionValue("sa") + ":22";
            String username = cmd.getOptionValue("un");
            char[] password = cmd.getOptionValue("pw").toCharArray();
            PasswordCredential credential = new PasswordCredential(username, password);
            ResourceManager rm = new ResourceManager(location, credential);
            Thread TRM = new Thread(rm, "resource manager");
            TRM.start();
        }
        // init job scheduler
        JobScheduler JSch = new JobScheduler("http://" + cmd.getOptionValue("sa") + ":5000/job", runningJobMap);
        Thread TJSch = new Thread(JSch, "service communicator");
        TJSch.start();


        // add a test job for debugging
        // Job testJob = new Job("/opt/DynPrvDriver/Webservice/test2", null, 1);
        // taskQueue.offer(testJob);

        masterSendPort = myIbis.createSendPort(one2one_UPCALL, "masterSendPort");
        controlPort = myIbis.createReceivePort(one2one_UPCALL, "controlPort", this);
        controlPort.enableConnections();
        controlPort.enableMessageUpcalls();
        while (!finished) {
            synchronized (runningJobMap)
            {
                for(int i:runningJobMap.keySet())
                {
                    System.out.println("Job "+i+"Has "+runningJobMap.get(i).leftTasks()+" Tasks to do!");
                }
                db.commit();
            }
            masterDeliverTask();//storeStatus();
            Thread.sleep(1000);
        }
        db.close();
        myIbis.registry().terminate();
    }

    private void client(Ibis myIbis, IbisIdentifier server) throws Exception {

        // Create a send port for sending requests and connect.
        SendPort controlSendPort = myIbis.createSendPort(one2one_UPCALL, "controlSendPort");
        controlSendPort.connect(server, "controlPort");


        ReceivePort taskReceivePort = myIbis.createReceivePort(one2one_UPCALL, "taskReceivePort", this);
        taskReceivePort.enableConnections();
        taskReceivePort.enableMessageUpcalls();
        System.out.println("Worker start");
        WriteMessage w = controlSendPort.newMessage();
        w.writeObject(new ControlMessage(true));
        w.finish();
        //controlSendPort.close();
        while (true) {

            Task task = workerTaskQueue.poll(1, TimeUnit.SECONDS);
            System.out.println("Start a task:" + task);
            if (task == null) {
                Thread.sleep(1000);
                if(masterCrashed == true)
                {
                    System.err.println("SERVER FAILED!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                    throw new masterFailException();
                }
                continue;
            }
            Thread.sleep(5000);

            String[] out = null;
            BufferedWriter writerOut = new BufferedWriter(new FileWriter(task.getSrcLocation() + "/taskOut_" + task.getTaskID()));
            BufferedWriter writerErr = new BufferedWriter(new FileWriter(task.getSrcLocation() + "/taskErr_" + task.getTaskID()));
            System.out.println("WC task" + task.getTaskID());
            for (File f : task.getFileArray()) {
                System.out.println(f.getAbsoluteFile());
                System.out.println(f.getAbsolutePath());
                out = SAGECAL(f,task);
                System.out.println(out[0]);
                System.out.println(out[1]);
                writerOut.append(out[0]);
                writerOut.newLine();
                writerErr.append(out[1]);
                writerErr.newLine();
            }
            System.out.println("establish new connect");
            if(masterCrashed == true)
            {
                System.err.println("SERVER FAILED!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                throw new masterFailException();
            }
            try
            {
                w = controlSendPort.newMessage();

                System.out.println("New message");
                ControlMessage feedBack = new ControlMessage(false);
                feedBack.FeedBack(task.getJobID(), task.getTaskID(), 0);
                System.out.println("write message");
                w.writeObject(feedBack);
                w.finish();
                System.out.println("write finished");
            }catch (Exception e)
            {
                myIbis.registry().assumeDead(server);
                writerOut.close();
                writerErr.close();
                throw new masterFailException();

            }


            writerOut.close();
            writerErr.close();

        }

    }

    public String[] SAGECAL(File f,Task t) throws IOException, InterruptedException {
        String s = null;
        System.out.println("handling File " + f.getAbsolutePath());
        File containerImage=new File(System.getProperty("user.dir")+"/AppContainers/Sagecal/SagecalContainer.simg");
        ProcessBuilder pb = RunContainer.run(containerImage,t.getExecutable(),t.getParameters(),f);
        Process p = pb.start();
        int exitCode=p.waitFor();
        System.out.println("EXIT CODE:"+exitCode);
        BufferedReader stdInput = new BufferedReader(new
                InputStreamReader(p.getInputStream()));

        BufferedReader stdError = new BufferedReader(new
                InputStreamReader(p.getErrorStream()));


        String[] out = new String[]{stdInput.lines().collect(Collectors.joining()), stdError.lines().collect(Collectors.joining())};
        return out;
    }

    private void run(CommandLine cmd) throws Exception {


        // Create an ibis instance.
        //Ibis ibis = IbisFactory.createIbis(ibisCapabilities, new Properties(), true, null, new PortType[]{one2one, one2one_UPCALL, one2many, one2many_UPCALL, many2one, many2one_UPCALL});
        //myibis = ibis;
        Registry r = myibis.registry();
        r.enableEvents();
        // Elect a server
        masterId = r.elect("Server");

        // If I am the server, run server, else run client.
        while (finished==false)
        {
            try {
                if (masterId.equals(myibis.identifier())) {
                    server(myibis, cmd);
                } else {
                    client(myibis, masterId);


                }
            } catch (InterruptedException e) {
                myibis.registry().assumeDead(myibis.identifier());
                myibis.end();
            } catch (IOException e) {
                System.out.println("-------------------Got IO Exception");
                e.printStackTrace();
                // throw new masterFailException();
            } catch (masterFailException e) {
                System.out.println("-------------------Got Master Fail Exception");
                masterId=r.elect("Server");
                System.out.println("----------------------------------------------------------------\n" +
                        "MASTER FAILS, RESTART\n" +
                        "------------------------------------------------------------------\n");

            }
        }



        // End ibis.
        myibis.end();
    }

    public static void main(String args[]) throws ParseException {
        // System.loadLibrary("Driver");
        Options options = new Options();


        Option managerSiteAddress = Option.builder("sa").required(true).desc("Manager Site Address").hasArg().build();
        options.addOption(managerSiteAddress);


        Option userName = Option.builder("un").required(false).desc("User Name").hasArg().build();
        options.addOption(userName);


        Option passwd = Option.builder("pd").required(false).desc("Pass word").hasArg().build();
        options.addOption(passwd);

        Option manual = Option.builder("m").required(true).desc("manual(without resource manager").hasArg().build();
        options.addOption(manual);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;

        cmd = parser.parse(options, args);
        System.out.println(cmd.getOptionValue("m"));
        System.out.println(args[1]);
        try {
            Driver d = new Driver();
            //d.sayHello("123");
            d.run(cmd);
        } catch (masterFailException e) {
            System.out.println("----------------------------------------------------------------\n" +
                    "MASTER FAILS, RESTART\n" +
                    "------------------------------------------------------------------\n");
            e.printStackTrace();
        } catch (Exception e) {
            finished = true;
            e.printStackTrace(System.err);
        }

    }

    public void upcall(ReadMessage readMessage) throws IOException, ClassNotFoundException {

        if (readMessage.localPort().name().equals("controlPort")) {
            ControlMessage m = (ControlMessage) readMessage.readObject();
            readMessage.finish();
            synchronized (idleWorkerQueue) {
                synchronized (runningNodes) {
                    Task runningTask=runningNodes.remove(readMessage.origin().ibisIdentifier());
                    if(m.isEmptyRequest()==true)
                    {

                        if(runningTask!=null)
                        {
                            Job tmp=runningJobMap.get(runningTask.getJobID());
                            tmp.loadRedo(runningTask);
                            runningJobMap.put(runningTask.getJobID(),tmp);
                            // runningJobMap.get(runningTask.getJobID()).loadRedo(runningTask);

                            db.commit();
                        }
                    }

                    idleWorkerQueue.offer(readMessage.origin().ibisIdentifier());
                    db.commit();
                }


            }

            if (m.isEmptyRequest() == false) {
                System.out.println("Job:" + m.getJobID() + " Task:" + m.getTaskID() + " Finished with code:" + m.getStatusCode());
                synchronized (runningJobMap) {
                    runningJobMap.get(m.getJobID()).finishOneTask();
                    if (runningJobMap.get(m.getJobID()).isFinished()) {
                        runningJobMap.remove(m.getJobID());
                    }
                    db.commit();
                }
            }
        } else {
            Task t = (Task) readMessage.readObject();
            readMessage.finish();
            synchronized (workerTaskQueue) {
                workerTaskQueue.offer(t);
            }
        }


    }

    void masterDeliverTask() throws IOException {
        if (idleWorkerQueue.isEmpty() != true) {
            // myibis.registry().joinedIbises()

            if (runningJobMap.isEmpty() != true) {

                synchronized (runningJobMap) {
                    for (Integer jobId : runningJobMap.keySet()) {
                        // FIFO JobId
                        if (runningJobMap.get(jobId).isEmpty() != true) {
                            // IF still task left
                            Job taskJob = runningJobMap.get(jobId);
                            Task tmp = taskJob.PopTask();
                            //System.out.println(runningJobMap.get(jobId));
                            runningJobMap.put(jobId,taskJob);
                            //System.out.println(taskJob);

                            // runningNodesMap.put(srcId, new AbstractMap.SimpleEntry<>(taskJob, tmp));
                /*
                   TODO: one2one connection cost too much overhead(1-2s for connection establishment)
                    Think about a way to reduce this part, possible solutions:
                    1. one2one+multiple ports stored
                    2. one2many, broadcast plus tags
                */
                            IbisIdentifier srcId = idleWorkerQueue.poll();


                            while (srcId != null) {
                                try {


                                    if (masterSendPort.connectedTo().length > 0) {
                                        if (!masterSendPort.connectedTo()[0].equals(srcId)) {
                                            masterSendPort.disconnect(masterSendPort.connectedTo()[0]);
                                            masterSendPort.connect(srcId, "taskReceivePort", 3000, false);
                                        }


                                    } else {
                                        masterSendPort.connect(srcId, "taskReceivePort", 3000, false);
                                    }
                                    WriteMessage w = masterSendPort.newMessage();
                                    w.writeObject(tmp);
                                    w.finish();
                                    runningNodes.put(srcId, tmp);
                                    System.out.println("------------------------------\nDELIVER TASK TO " + srcId+
                                            "\n with "+taskJob.leftTasks()+" task left\n---------------------------");
                                    break; //connection and passing message success
                                } catch (Exception e) {

                                    e.printStackTrace();
                                    srcId = idleWorkerQueue.poll();
                                }
                            }
                            if (srcId == null) {
                                taskJob.loadRedo(tmp);
                                runningJobMap.put(jobId,taskJob);
                            }
                        }
                    }
                    db.commit();
                }


            }
        }
    }

    @Override
    public void joined(IbisIdentifier joinedIbis) {
        System.out.println("joined: " + joinedIbis);
    }

    @Override
    public void left(IbisIdentifier leftIbis) {
        System.out.println("left: " + leftIbis);
    }

    @Override
    public void died(IbisIdentifier corpse) {

        System.out.println("-----------------------\ndied: " + corpse+"\n-----------------");
        if(masterId.equals(myibis.identifier()))
        {
            synchronized (runningNodes) {
                synchronized (runningJobMap) {
                    Task t = runningNodes.remove(corpse);
                    if(t!=null)
                    {
                        Job tmp=runningJobMap.get(t.getJobID());
                        tmp.loadRedo(t);
                        runningJobMap.put(t.getJobID(),tmp);
                        System.out.println("------REDO-----\n" +
                                "----TASK:" + t.getTaskID());
                    }


                }
                db.commit();
            }
        }
        else if(corpse.equals(masterId))
        {
            System.out.println("Master:"+corpse+" creshed!!!!!!");
            masterCrashed = true;
            firstTime=false;
        }
    }

    @Override
    public void gotSignal(String signal, IbisIdentifier source) {
        System.out.println("signal: " + signal + " " + source);
    }

    @Override
    public void electionResult(String electionName, IbisIdentifier winner) {
        System.out.println("electionResult: " + electionName + " " + winner);
    }

    @Override
    public void poolClosed() {
        System.out.println("pool closed");
    }

    @Override
    public void poolTerminated(IbisIdentifier source) {
        System.out.println("poolTerminated: " + source);
    }

}
class ControlMessage implements Serializable {
    private final boolean emptyRequest;
    private int jobID;
    private String taskID;
    private int statusCode;

    ControlMessage(boolean EmptyRequest) {
        emptyRequest = EmptyRequest;
    }

    public void FeedBack(int JobID, String TaskID, int StatusCode) {
        jobID = JobID;
        taskID = TaskID;
        statusCode = StatusCode;
        //return this;
    }

    public boolean isEmptyRequest() {
        return emptyRequest;
    }

    public int getJobID() {
        return jobID;
    }

    public String getTaskID() {
        return taskID;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
