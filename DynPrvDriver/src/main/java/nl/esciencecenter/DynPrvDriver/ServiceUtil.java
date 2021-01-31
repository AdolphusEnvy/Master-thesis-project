package nl.esciencecenter.DynPrvDriver;

import okhttp3.*;

import java.util.Objects;

public class ServiceUtil {
    public static final MediaType JSON
            = MediaType.get("application/json; charset=utf-8");
    public static final MediaType FORM = MediaType.parse("multipart/form-data");
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
        if(!serviceAddress.startsWith("http"))
        {
            StringBuffer sb=new StringBuffer(serviceAddress);
            sb.insert(0,"http://");
            serviceAddress=sb.toString();
        }
        RequestBody formBody = new FormBody.Builder().add("miniNode", recommandMiniNode.toString()).build();
        Request request=new Request.Builder().url(serviceAddress).post(formBody).build();
        try (Response response=client.newCall(request).execute()){
            return Objects.requireNonNull(response.body()).string();
        }catch (Exception e)
        {

            return e.getMessage();
        }
    }
    public static void main(String[] args)
    {
        String address="http://localhost:5000/miniNode";
        OkHttpClient client=new OkHttpClient();
        String log=postJobStatus(address,client,5);
        System.out.println(log);
        try {
            String miniNode=getJobStatus(address,client);
            System.out.println(miniNode.chars().asDoubleStream());
            StringBuilder sb=new StringBuilder(miniNode);
            sb.deleteCharAt(sb.length()-1);
            sb.deleteCharAt(sb.length()-1);
            sb.deleteCharAt(0);
            System.out.println(sb.toString());
            System.out.println(Integer.parseInt(sb.toString()));
        }catch (Exception e)
        {
            e.printStackTrace();
        }

    }
}
