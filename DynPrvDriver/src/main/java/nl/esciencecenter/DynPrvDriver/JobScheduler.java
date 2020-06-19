package nl.esciencecenter.DynPrvDriver;

import com.github.kevinsawicki.http.HttpRequest;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.util.*;
import java.util.stream.Collectors;

public class JobScheduler implements Runnable {

    String HostAddress;
    Map<String, List<Map<String, String>>> Map_job_detail;
    Map<Integer,Job>jobMap;

    public JobScheduler(String hostAddress, Map<Integer,Job>JobMap ) throws MalformedURLException {
        HostAddress = hostAddress;
        jobMap = JobMap;

    }

    @Override
    public void run() {
//        int round = 20;
        while (true) {
            System.out.println("Fetch job");
            try {
                HttpRequest request = HttpRequest.get(HostAddress);
                //request.connectTimeout(1000);
                String response = request.body();
                JSONObject jObj = new JSONObject(response);
                for (String userId : jObj.keySet()) {
                    System.out.println(userId + " " + jObj.get(userId));

                    JSONArray jsonArray = jObj.getJSONArray(userId);
                    System.out.println(jsonArray);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject ijobj = jsonArray.getJSONObject(i);
                        System.out.println(ijobj);
                        Job tmpJob = new Job(ijobj.getString("srcLocation"), ijobj.getString("parameter"), ijobj.getInt("jobId"), ijobj.getString("Job"),ijobj.getString("executable"),true);
                        //tmpJob.LoadTasks(10);
                        synchronized (jobMap)
                        {
                            jobMap.put(tmpJob.getJobID(),tmpJob);
                            System.out.println("LOADING JOB:"+tmpJob);
                        }

                    }

                }
                Thread.sleep(5000);
                //round -= 1;


            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    public static void main(String args[]) throws InterruptedException {

        Map<String, List<Map<String, String>>> Map_job_detail = Collections.synchronizedMap(new HashMap<String, List<Map<String, String>>>());
        Map<Integer,Job> jobMap = new TreeMap<>();
        try {

            //JobScheduler Scon = new JobScheduler("http://fs0.das5.cs.vu.nl:5000/job", taskQueue);
            JobScheduler Scon = new JobScheduler("http://127.0.0.1:5000/job", jobMap);
            Thread T = new Thread(Scon, "resource manager");
            T.start();
            //.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
        int round = 25;
        while (round > 0) {
            System.out.println(jobMap.size());
            Thread.sleep(5000);
            round -= 1;
        }
    }
}
