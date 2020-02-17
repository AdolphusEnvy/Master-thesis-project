import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;


import java.io.IOException;

// public class HdfsReader extends Configured implements Tool {
//     public int run(String[] args) throws Exception {
//         String localOutputPath = args[0];
//         Configuration conf = getConf();
//         FileSystem fs = FileSystem.get(conf);

//         InputStream is = fs.open(new Path("hdfs:/acadgild.txt"));
//         OutputStream os = new BufferedOutputStream(new FileOutputStream(localOutputPath)); // Data set is getting copied into local path in the file sysetm through buffer mechanism

//         IOUtils.copyBytes(is, os, conf);
//         return 0;
//     }

//     public static void main(String[] args) throws Exception {
//         int returnCode = ToolRunner.run(
//         new HdfsReader(), args);
//         System.exit(returnCode);
//     }

// }


public class HDFS_copy {
    public static void main(String args[]){
        try {

            Configuration conf = new Configuration();
            conf.set("fs.defaultFS", "hdfs://node1:9000/user/ubuntu/");

            FileSystem fs = FileSystem.get(conf);
            FileStatus[] status = fs.listStatus(new Path("sm.ms"));

            for(int i=0;i<status.length;i++){
                System.out.println(status[i].getPath());
                fs.copyToLocalFile(false, status[i].getPath(), new Path("test_copy_sm.ms"));
            }

        } catch (IOException e) {

            e.printStackTrace();

        }

    }
}


// Configuration configuration = new Configuration();
// configuration.set("fs.defaultFS", "hdfs://node1:9000");
// FileSystem filesystem = FileSystem.get(configuration);
// FileUtil.copy(filesystem, new Path("src/path"), filesystem, new Path("dst/path"), false, configuration);