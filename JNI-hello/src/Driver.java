import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.Path;

import org.apache.commons.io.FileUtils;

import org.apache.spark.SparkFiles;
public class Driver {  // Save as HelloJNI.java
//    static {
//        System.loadLibrary("Driver"); // Load native library hello.dll (Windows) or libhello.so (Unixes)
//        //  at runtime
//        // This library contains a native method called sayHello()
//    }

    // Declare an instance native method sayHello() which receives no parameter and returns void
    private native int sayHello(String args);
    private void syaHello(String s)
    {
        System.out.println("normal:"+s);
    }
    // Test Driver
    public static void main(String[] args) {
        System.loadLibrary("Driver");
        Driver driver=new Driver();
        try {
            int stat=driver.sayHello("123");
            System.out.println("STATUS:"+stat);
            driver.syaHello("123");
        }
        catch (Exception e) {

            e.printStackTrace();

        }
        }
}
