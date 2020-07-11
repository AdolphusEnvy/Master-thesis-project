package nl.esciencecenter.DynPrvDriver;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class RunContainer {
    public static ProcessBuilder run(File container,String executable ,String parameter, File f) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.directory(f.getParentFile());
        System.out.println(processBuilder.directory().getAbsoluteFile());
        // processBuilder.command("singularity exec "+container.getPath());
        int coreNum=Runtime.getRuntime().availableProcessors()/2;
        System.out.println("AVAILABLE CORES:"+coreNum);
        String cmd=("singularity exec "+container.getPath() +" "+executable+" "+parameter.replace("FILE_DIR",f.getName()).replace("NUMPROCESS",Integer.toString(System.getenv("MASTER_NODE").equals("False")?coreNum:coreNum-2)));
        System.out.println(cmd);
        processBuilder.command(cmd.split("\\s+"));
        return processBuilder;
//        try {
//
//            Process process = processBuilder.start();
//
//            // blocked :(
//            BufferedReader reader =
//                    new BufferedReader(new InputStreamReader(process.getInputStream()));
//
//            String line;
//            String out="";
//            while ((line = reader.readLine()) != null) {
//                out+=line;
//                //System.out.println(line);
//            }
//
//
//            int exitCode = process.waitFor();
//            System.out.println("\nExited with error code : " + exitCode);
//            return exitCode;
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        return -1;

    }
    public static void main(String args[]) throws IOException, InterruptedException {
        File f=new File("/home/adolphus/Calibration/DATA0");
        File containerFile=new File("/home/adolphus/DynPrvDriver/AppContainers/Sagecal/SagecalContainer.simg");
        System.out.println(f.getParent());
        System.out.println(containerFile.getPath());
        ProcessBuilder pb =run(containerFile,"/opt/sagecal/bin/sagecal","-d FILE_DIR -s 3c196.sky.txt -c 3c196.sky.txt.cluster -n NUMPROCESS -t 10 -p sm.ms.solutions -e 4 -g 2 -l 10 -m 7 -x 30 -F 1 -j 5  -k -1 -B 1 -W 0" ,f);
        Process p = pb.start();
        int exitCode=p.waitFor();
        System.out.println("EXIT CODE:"+exitCode);
        BufferedReader stdInput = new BufferedReader(new
                InputStreamReader(p.getInputStream()));

        BufferedReader stdError = new BufferedReader(new
                InputStreamReader(p.getErrorStream()));


        String[] out = new String[]{stdInput.lines().collect(Collectors.joining()), stdError.lines().collect(Collectors.joining())};
        System.out.println(out[0]);
        System.out.println(out[1]);
    }
}
