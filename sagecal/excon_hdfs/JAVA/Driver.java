// package com.sagecal;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.Path;
import java.io.IOException;

import java.io.File;
import java.util.List;
import org.apache.commons.io.FileUtils;

import org.apache.spark.SparkFiles;

public class Driver
{
  public native String stringMethod(String text);
  public native int readMSAndBack(String[] argc);


  public static void main(String[] args)
  {

    String fileName = SparkFiles.get("sm.ms");
    System.out.println("\n\n fileName: " + fileName + "\n\n");


    System.loadLibrary("Driver");
    Driver driver=new Driver();

    // String text=driver.stringMethod("sm1.ms");
    // System.out.println("stringMethod: "+text);

    // int I1=driver.readMSAndBack(args);    
    // System.out.println("quitting with output "+I1);

    try {

      Configuration conf = new Configuration();
      // conf.set("fs.defaultFS", "hdfs://node1:9000/user/ubuntu/");
      conf.set("fs.defaultFS", "hdfs://node1:9000");
      FileSystem fs = FileSystem.get(conf);


      // FileSystem fileStatus = FileSystem.get(new URI("hdfs://localhost:9000/"), conf);      
      FileStatus[] fileStatus = fs.listStatus(new Path("/user/ubuntu/sm.ms"));


      //spark submit --files argument

      // // list the content of hdfs folder
      // System.out.println("\n\n Content of /user/ubuntu/sm.ms \n\n");
      // // FileStatus[] fileStatus = fs.listStatus(new Path("hdfs://localhost:9000/user/ubuntu/sm.ms"));
      // for(FileStatus status : fileStatus){
      //     System.out.println(status.getPath().toString());
      // }    

      // // copy hdfs folder to local folder
      // System.out.println("\n\nCopying \n hdfs://node1:9000//user/ubuntu/sm.ms --> /tmp/test_copy_sm.ms \n\n");
      // for(int i=0;i<fileStatus.length;i++){
      //     System.out.println(fileStatus[i].getPath());
      //     fs.copyToLocalFile(false, fileStatus[i].getPath(), new Path("/tmp/test_copy_sm.ms"));
      // }

      // // list the content of the local folder
      // System.out.println("\n\n Content of sm.ms/ \n\n");
      // String dirName = "sm.ms/";
      // List<File> files = (List<File>) FileUtils.listFiles(new File(dirName), null, true);
      // files.forEach(System.out::println);


      // // parse arguments and run the driver
      int I1=driver.readMSAndBack(args);
      System.out.println("quitting with output "+I1);


      // // temporarily use hard-coded arguments
      // String[] args_test = new String[]{"-m", "sm.ms"};      
      // int I1=driver.readMSAndBack(args_test);
      // System.out.println("argc: "+ args_test);


    } catch (IOException e) {

        e.printStackTrace();

    }



  }
}
