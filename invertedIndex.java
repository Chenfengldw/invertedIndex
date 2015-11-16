import java.io.IOException;
import java.util.StringTokenizer;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
 
 
public class InvertedIndex {
     
    public static class InvertedIndexMap extends Mapper<Object,Text,Text,Text>{
         
        private Text valueInfo = new Text();
        private Text keyInfo = new Text();
        private FileSplit split;
         
        public void map(Object key, Text value,Context context)
                throws IOException, InterruptedException {
            //��ȡ<key value>��������FileSplit����
            split = (FileSplit) context.getInputSplit();
            StringTokenizer stk = new StringTokenizer(value.toString());
            while (stk.hasMoreElements()) {
                //keyֵ�ɣ����ʣ�URI�����
                keyInfo.set(stk.nextToken()+":"+split.getPath().toString());
                //��Ƶ
                valueInfo.set("1");
                context.write(keyInfo, valueInfo);
                 
            }
             
             
        }
    }
     
    public static class InvertedIndexCombiner extends Reducer<Text,Text,Text,Text>{
         
        Text info = new Text();
 
        public void reduce(Text key, Iterable<Text> values,Context contex)
                throws IOException, InterruptedException {
            int sum = 0;
            for (Text value : values) {
                sum += Integer.parseInt(value.toString());
            }
             
            int splitIndex = key.toString().indexOf(":");
            //��������valueֵ�ɣ�URI+:��Ƶ��ɣ�
            info.set(key.toString().substring(splitIndex+1) +":"+ sum);
            //��������keyֵΪ����
            key.set(key.toString().substring(0,splitIndex));
            contex.write(key, info);
        }
    }
     
    public static class InvertedIndexReduce extends Reducer<Text,Text,Text,Text>{
         
        private Text result = new Text();
         
         public void reduce(Text key, Iterable<Text> values, Context context)
                throws IOException, InterruptedException {
            //�����ĵ��б�
            String fileList = new String();
            for (Text value : values) {
                fileList += value.toString()+";";
            }
            result.set(fileList);
            context.write(key, result);
        }
    }
     
    public static void main(String[] args) throws IOException, InterruptedException, ClassNotFoundException {
         
        Configuration conf = new Configuration();
         
        Job job = new Job(conf,"InvertedIndex");
         
        job.setJarByClass(InvertedIndex.class);
         
        job.setMapperClass(InvertedIndexMap.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Text.class);
         
        job.setCombinerClass(InvertedIndexCombiner.class);
         
        job.setReducerClass(InvertedIndexReduce.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
         
        FileInputFormat.addInputPath(job, new Path("./in/invertedindex/"));
        FileOutputFormat.setOutputPath(job, new Path("./out/"));
        System.exit(job.waitForCompletion(true)?0:1);
               
    }
}