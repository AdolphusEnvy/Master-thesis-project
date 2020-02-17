import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.util.Progressable;
import java.io.IOException;


public class hdfs_copy {

	public static void main (String args[]){
		try {

			Configuration conf = new Configuration();
			conf.set("fs.defaultFS", "hdfs://hadoop:50020/user/dirac/");
			System.out.println(conf);

			FileSystem fs = FileSystem.get(conf);
			System.out.println(fs);

			//FileStatus[] status = fs.listStatus(new Path("data"));

			//for(int i = 0; i < status.length; i++){
			//	System.out.println(status[i].getPath());
			//	fs.copyToLocalFile(false, status[i].getPath(), new Path("localdir"));
			//}


			// DOC --> http://hadoop.apache.org/docs/current/api/org/apache/hadoop/fs/FileSystem.html
			//fs.copyFromLocalFile(new Path("/home/user/directory/"), new Path("/user/hadoop/dir"));
			//fd.copyToLocalFile


		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
