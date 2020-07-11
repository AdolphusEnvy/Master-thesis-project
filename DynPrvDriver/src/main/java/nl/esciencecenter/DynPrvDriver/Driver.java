package nl.esciencecenter.DynPrvDriver;

import ibis.ipl.*;
import okhttp3.OkHttpClient;
import org.apache.commons.cli.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import org.mapdb.*;
import sun.misc.Signal;
import sun.misc.SignalHandler;

public class Driver implements MessageUpcall, RegistryEventHandler {
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
    private BTreeMap<IbisIdentifier, Task> runningNodes;
    private BlockingQueue<Task> workerTaskQueue = new LinkedBlockingQueue<>();
    private boolean masterCrashed = false;
    private boolean firstTime = true;
    private DB db;
    private OkHttpClient client;

    Driver() throws IbisCreationFailedException {
        Properties properties = new Properties();
        myibis = IbisFactory.createIbis(ibisCapabilities, properties, true, this, new PortType[]{one2one, one2one_UPCALL, one2many, one2many_UPCALL, many2one, many2one_UPCALL});
        System.out.println("Ibis created: " + myibis.identifier());
        masterCrashed = false;

    }


    private void server(Ibis myIbis) throws Exception {
        startSideExecutor();
        client = new OkHttpClient();
        // Init resource manager
        db = DBMaker.fileDB(System.getenv("DYNPRVDRIVER_HOME") + "/cache.db").transactionEnable().closeOnJvmShutdown().make();
        //Atomic.Var<Map<IbisIdentifier, Task>> runningJobMap=db.atomicVar("runningJobMap",Map.SERIALIZER)
        runningJobMap = db.treeMap("runningJobMap", Serializer.INTEGER, Serializer.JAVA).createOrOpen();
        runningNodes = db.treeMap("runningNodes", Serializer.JAVA, Serializer.JAVA).createOrOpen();
        if (runningNodes.size() > 0) {
            for (Task t : runningNodes.values()) {
                Job tmpJob = runningJobMap.get(t.getJobID());
                tmpJob.loadRedo(t);
                runningJobMap.put(t.getJobID(), tmpJob);
            }
        }
        db.commit();

        // init job scheduler
        JobScheduler JSch = new JobScheduler("http://" + System.getenv("IPL_ADDRESS") + ":5000/job", runningJobMap);
        Thread TJSch = new Thread(JSch, "service communicator");
        TJSch.start();


        // add a test job for debugging
        // Job testJob = new Job("/opt/DynPrvDriver/Webservice/test2", null, 1);
        // taskQueue.offer(testJob);

        masterSendPort = myIbis.createSendPort(one2one_UPCALL, "masterSendPort");
        controlPort = myIbis.createReceivePort(many2one_UPCALL, "controlPort", this);
        controlPort.enableConnections();
        controlPort.enableMessageUpcalls();
        while (!finished) {
            synchronized (runningJobMap) {
                for (int i : runningJobMap.keySet()) {
                    System.out.println("Job " + i + "Has " + runningJobMap.get(i).leftTasks() + " Tasks to do!");
                }
                for(Map.Entry<IbisIdentifier,Task> entry:runningNodes.entrySet())
                {
                    System.out.println("Node:"+entry.getKey()+" working on task "+entry.getValue().getTaskID());
                }
                db.commit();
            }

            masterDeliverTask();//storeStatus();
            uploadRecommendMiniNode(client);
            Thread.sleep(1000);
        }
        db.close();
        myIbis.registry().terminate();
    }

    private void client(Ibis myIbis, IbisIdentifier server) throws Exception {

        // Create a send port for sending requests and connect.
        SendPort controlSendPort = myIbis.createSendPort(many2one_UPCALL, "controlSendPort");
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
            System.out.println(myIbis.identifier()+" Start a task:" + task);
            if (task == null) {
                Thread.sleep(1000);
                if (masterCrashed == true) {
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
                out = SAGECAL(f, task);
                System.out.println(out[0]);
                System.out.println(out[1]);
                writerOut.write(out[0]);
                writerOut.newLine();
                writerErr.write(out[1]);
                writerErr.newLine();
            }
            System.out.println("establish new connect");
            if (masterCrashed == true) {
                System.err.println("SERVER FAILED!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                throw new masterFailException();
            }
            try {
                w = controlSendPort.newMessage();

                System.out.println("New message");
                ControlMessage feedBack = new ControlMessage(false);
                feedBack.FeedBack(task.getJobID(), task.getTaskID(), 0);
                System.out.println("write message");
                w.writeObject(feedBack);
                w.finish();
                System.out.println("write finished");
            } catch (Exception e) {
                myIbis.registry().assumeDead(server);
                writerOut.close();
                writerErr.close();
                throw new masterFailException();

            }


            writerOut.close();
            writerErr.close();

        }

    }

    public String[] SAGECAL(File f, Task t) throws IOException, InterruptedException {
        String s = null;
        System.out.println("handling File " + f.getAbsolutePath());
        File containerImage = new File(System.getenv("DYNPRVDRIVER_HOME") + "/AppContainers/Sagecal/SagecalContainer.simg");
        ProcessBuilder pb = RunContainer.run(containerImage, t.getExecutable(), t.getParameters(), f);
        Process p = pb.start();
        int exitCode = p.waitFor();
        System.out.println("EXIT CODE:" + exitCode);
        BufferedReader stdInput = new BufferedReader(new
                InputStreamReader(p.getInputStream()));

        BufferedReader stdError = new BufferedReader(new
                InputStreamReader(p.getErrorStream()));


        String[] out = new String[]{stdInput.lines().collect(Collectors.joining()), stdError.lines().collect(Collectors.joining())};
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(f.getAbsolutePath() + "/result"));
        bufferedWriter.write(out[0]);
        bufferedWriter.newLine();
        bufferedWriter.write(out[1]);
        bufferedWriter.close();
        return out;
    }

    private void run() throws Exception {


        // Create an ibis instance.
        //Ibis ibis = IbisFactory.createIbis(ibisCapabilities, new Properties(), true, null, new PortType[]{one2one, one2one_UPCALL, one2many, one2many_UPCALL, many2one, many2one_UPCALL});
        //myibis = ibis;
        Registry r = myibis.registry();
        r.enableEvents();
        // Elect a server
        masterId = r.elect("Server");
        DebugSignalHandler.listenTo("TERM", r, myibis.identifier(),myibis);
        // If I am the server, run server, else run client.
        while (finished == false) {
            try {
                if (masterId.equals(myibis.identifier())) {
                    server(myibis);
                } else {
                    client(myibis, masterId);


                }
            } catch (InterruptedException e) {
                // myibis.registry().assumeDead(myibis.identifier());
                myibis.end();
            } catch (IOException e) {
                System.out.println("-------------------Got IO Exception");
                e.printStackTrace();
                // myibis.registry().assumeDead(myibis.identifier());
                myibis.end();
                // throw new masterFailException();
            } catch (masterFailException e) {
                System.out.println("-------------------Got Master Fail Exception");
                masterId = r.elect("Server");
                System.out.println("----------------------------------------------------------------\n" +
                        "MASTER FAILS, RESTART\n" +
                        "------------------------------------------------------------------\n");

            } catch (Exception e) {
                e.printStackTrace();
                // myibis.registry().assumeDead(myibis.identifier());

                break;
            }
        }


        // End ibis.
         myibis.end();
    }

    public static void main(String[] args) throws ParseException {

        try {
            Driver d = new Driver();
            //d.sayHello("123");
            d.run();
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
                    Task runningTask = runningNodes.remove(readMessage.origin().ibisIdentifier());
                    if (m.isEmptyRequest()) {

                        if (runningTask != null) {
                            Job tmp = runningJobMap.get(runningTask.getJobID());
                            assert tmp != null;
                            tmp.loadRedo(runningTask);
                            runningJobMap.put(runningTask.getJobID(), tmp);
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
                    Job tmpjb = runningJobMap.get(m.getJobID());
                    tmpjb.finishOneTask();
                    if (tmpjb.isFinished()) {
                        runningJobMap.remove(m.getJobID());
                        logFinishJob(tmpjb);
                    } else {
                        runningJobMap.put(m.getJobID(), tmpjb);
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
                            runningJobMap.put(jobId, taskJob);
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
                                    System.out.println("------------------------------\nDELIVER TASK "+tmp.getTaskID()+" TO " + srcId +
                                            "\n with " + taskJob.leftTasks() + " task left\n---------------------------");
                                    break; //connection and passing message success
                                } catch (Exception e) {

                                    e.printStackTrace();
                                    srcId = idleWorkerQueue.poll();
                                }
                            }
                            if (srcId == null) {
                                taskJob.loadRedo(tmp);
                                runningJobMap.put(jobId, taskJob);
                            }
                        }
                    }
                    db.commit();
                }


            }
        }
    }

    private void logFinishJob(Job jb) throws IOException {
        BufferedWriter out = new BufferedWriter(new FileWriter(System.getenv("JOB_LOG_DYNPRVDRIVER"), true));
        out.write(String.join("\t", "JobID=" + jb.getJobID(), "Action=" + System.currentTimeMillis(), "STATE=FINISHING"));
        out.newLine();
        out.close();
    }

    public void uploadRecommendMiniNode(OkHttpClient client) {
        int taskNum = runningJobMap.values().stream().map(Job::leftTasks).mapToInt(Integer::intValue).sum();
        ServiceUtil.postJobStatus(System.getenv("IPL_ADDRESS"), client, taskNum > 10 ? taskNum / 10 : taskNum > 0 ? 1 : 0);
    }

    public void startSideExecutor() throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.directory(new File(System.getProperty("user.dir")));
        processBuilder.environment().put("MASTER_NODE","True");
        processBuilder.command("./scripts/ipl-run");
        Process p=processBuilder.start();
        Thread thread=new Thread(()->{
            try {


                BufferedReader stdInput = new BufferedReader(new
                        InputStreamReader(p.getInputStream()));

                BufferedReader stdError = new BufferedReader(new
                        InputStreamReader(p.getErrorStream()));

                while (p.isAlive())
                {
                    System.out.println(stdInput.lines().collect(Collectors.joining("\n")));
                    System.out.println(stdError.lines().collect(Collectors.joining("\n")));
                    Thread.sleep(1000);
                }
                int exitCode=p.waitFor();
                //String[] out = new String[]{stdInput.lines().collect(Collectors.joining("\n")), stdError.lines().collect(Collectors.joining("\n"))};
                System.out.println("Exit code:"+exitCode);
                System.out.println(stdInput.lines().collect(Collectors.joining("\n")));
                System.out.println(stdError.lines().collect(Collectors.joining("\n")));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }
    @Override
    public void joined(IbisIdentifier joinedIbis) {
        System.out.println("joined: " + joinedIbis);
    }

    @Override
    public void left(IbisIdentifier leftIbis) {
        //System.out.println("left: " + leftIbis);
        System.out.println("-----------------------\nleft: " + leftIbis + "\n-----------------");
        if (masterId.equals(myibis.identifier())) {
            synchronized (runningNodes) {
                synchronized (runningJobMap) {
                    Task t = runningNodes.remove(leftIbis);
                    if (t != null) {
                        Job tmp = runningJobMap.get(t.getJobID());
                        assert tmp != null;
                        tmp.loadRedo(t);
                        runningJobMap.put(t.getJobID(), tmp);
                        System.out.println("------REDO-----\n" +
                                "----TASK:" + t.getTaskID());
                    }


                }
                db.commit();
            }
        } else if (leftIbis.equals(masterId)) {
            System.out.println("Master:" + leftIbis + " creshed!!!!!!");
            masterCrashed = true;
            firstTime = false;
        }
    }

    @Override
    public void died(IbisIdentifier corpse) {

        System.out.println("-----------------------\ndied: " + corpse + "\n-----------------");
        if (masterId.equals(myibis.identifier())) {
            synchronized (runningNodes) {
                synchronized (runningJobMap) {
                    Task t = runningNodes.remove(corpse);
                    if (t != null) {
                        Job tmp = runningJobMap.get(t.getJobID());
                        assert tmp != null;
                        tmp.loadRedo(t);
                        runningJobMap.put(t.getJobID(), tmp);
                        System.out.println("------REDO-----\n" +
                                "----TASK:" + t.getTaskID());
                    }


                }
                db.commit();
            }
        } else if (corpse.equals(masterId)) {
            System.out.println("Master:" + corpse + " creshed!!!!!!");
            masterCrashed = true;
            firstTime = false;
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

class DebugSignalHandler implements SignalHandler {
    private final Registry registry;
    private final IbisIdentifier identifier;
    private final Ibis ibis;

    DebugSignalHandler(Registry R, IbisIdentifier Identifier, Ibis ibis) {
        registry = R;
        identifier = Identifier;
        this.ibis = ibis;
    }

    public static void listenTo(String name, Registry registry, IbisIdentifier identifier,Ibis ibis) {
        Signal signal = new Signal(name);
        Signal.handle(signal, new DebugSignalHandler(registry, identifier, ibis));
    }

    public void handle(Signal signal) {
        System.out.println("Signal: " + signal);
        if (signal.toString().trim().equals("SIGTERM")) {
            try {
                //registry.assumeDead(identifier);
                ibis.end();
            } catch (IOException e) {
                e.printStackTrace();
            }

            System.out.println("SIGTERM raised, terminating...");
            System.exit(1);

        }
    }
}