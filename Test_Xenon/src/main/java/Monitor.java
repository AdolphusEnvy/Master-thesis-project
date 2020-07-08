import java.io.*;
import java.sql.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Monitor {

    public static void main(String[] args) throws IOException {
        Monitor m=new Monitor();
        String info=m.getStatus();
        m.writeToFile(info,"/home/yhu310/log/test");
        System.out.println(info);

    }

    public String getStatus() throws IOException {
        Process squeueP=Runtime.getRuntime().exec("squeue");
        BufferedReader squeueReader = new BufferedReader(
                new InputStreamReader(squeueP.getInputStream()));
        List<String> squeueInputList=squeueReader.lines().collect(Collectors.toList());
        String jobInfoString= String.join("\n",squeueInputList.subList(1,squeueInputList.size()));

        Process sinfoP=Runtime.getRuntime().exec("sinfo -s");
        BufferedReader sinfoReader = new BufferedReader(
                new InputStreamReader(sinfoP.getInputStream()));
        List<String> inputList=sinfoReader.lines().collect(Collectors.toList());
        String clusterInfoString= String.join("\n",inputList.subList(1,inputList.size()));

        return "<>\n"+System.currentTimeMillis()+"\n$$\n"+jobInfoString+"\n$$\n"+clusterInfoString+"<>";
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
        while (true)
        {
            String info=getStatus();
            writeToFile(info,logFileName);
            Thread.sleep(1000);
        }

    }
}
