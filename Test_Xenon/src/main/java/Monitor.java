import java.io.*;
import java.sql.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Monitor {

    public static void main(String[] args) throws IOException, InterruptedException {
        Monitor m=new Monitor();

        for(int i =0;i<600;i++)
        {

            String info=m.getStatus();
            m.writeToFile(info,"/home/yhu310/log/test");
            // System.out.println(info);
            Thread.sleep(1000);
        }


    }

    public String getStatus() throws IOException {
        Long time=System.currentTimeMillis();
        Process squeueP=Runtime.getRuntime().exec("squeue");
        BufferedReader squeueReader = new BufferedReader(
                new InputStreamReader(squeueP.getInputStream()));
        List<String> squeueInputList=squeueReader.lines().map(x->time+" "+x).collect(Collectors.toList());
        String jobInfoString= String.join("\n",squeueInputList.subList(1,squeueInputList.size()));

        return jobInfoString;
    }
    public void writeToFile(String info,String fileName)
    {

        try
        {
            BufferedWriter out=new BufferedWriter(new FileWriter(fileName,true));
            out.write(info);
            out.newLine();
            out.close();
        } catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    public void run(String logFileName) throws IOException, InterruptedException {
        while (!Thread.currentThread().isInterrupted())
        {
            try {
                String info=getStatus();
                writeToFile(info,logFileName);
                Thread.sleep(1000);
            }catch (Exception e)
            {

                System.err.println(e.toString());
                System.err.println("Exception handled!");
                Thread.currentThread().interrupt();
            }

        }


    }
}
