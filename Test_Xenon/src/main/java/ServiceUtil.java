import okhttp3.*;
import org.json.*;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class ServiceUtil {
    public static final MediaType JSON
            = MediaType.get("application/json; charset=utf-8");
    public static String getJobStatus(String serviceAddress, OkHttpClient client)
    {
        Request request=new Request.Builder().url(serviceAddress).build();
        try (Response response=client.newCall(request).execute()){
            return Objects.requireNonNull(response.body()).string();
        }catch (Exception e)
        {
            e.printStackTrace();
            return "-1";
        }

    }
    public static String postJobStatus(String serviceAddress,OkHttpClient client,Integer recommandMiniNode)
    {
//        RequestBody body=FormBody.create("{\"miniNode\":"+recommandMiniNode+"}",JSON);
        RequestBody formBody = new FormBody.Builder().add("miniNode", recommandMiniNode.toString()).build();
        Request request=new Request.Builder().url(serviceAddress).post(formBody).build();
        try (Response response=client.newCall(request).execute()){
            return Objects.requireNonNull(response.body()).string();
        }catch (Exception e)
        {

            return e.getMessage();
        }
    }
    public static String submitJob(String serviceAddress,OkHttpClient client,String submitString) {
        JSONObject submitForm=new JSONObject(submitString);

        FormBody.Builder builder=new FormBody.Builder();
        for(Map.Entry<String, Object> e:submitForm.toMap().entrySet())
        {
            builder.add(e.getKey(),(String)e.getValue());
        }
        RequestBody body=builder.build();
        Request request=new Request.Builder().url(serviceAddress).post(body).build();
        try (Response response=client.newCall(request).execute()){
            return Objects.requireNonNull(response.body()).string();
        }catch (Exception e)
        {

            return e.getMessage();
        }
    }
    public static void main(String[] args) throws IOException {
        try {
        String address="http://localhost:5000/job";
        OkHttpClient client=new OkHttpClient();
        //String log=postJobStatus(address,client,5);
        String submitString="{\"location\":\"/home/yhu310/scratch/data.m\" ,\"user\":\"yhu310\" ,\"parameter\":\"-d FILE_DIR -s 3c196.sky.txt -c 3c196.sky.txt.cluster -n NUMPROCESS -t 10 -p sm.ms.solutions -e 4 -g 2 -l 10 -m 7 -x 30 -F 1 -j 5  -k -1 -B 1 -W 0\" ,\"executable\":\"/opt/sagecal/bin/sagecal\",\"Job\":\"Sagecal\"}";
        org.json.JSONObject jsonObject = new org.json.JSONObject(submitString);
        System.out.println(jsonObject);
        System.out.println(submitString);
        String log=submitJob(address,client,submitString);
        System.out.println(log);
        System.out.println(getJobStatus(address,client));}
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
