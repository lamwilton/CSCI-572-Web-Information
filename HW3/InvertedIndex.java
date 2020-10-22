import java.io.IOException;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.HashMap;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class InvertedIndex {

   public static class TokenizerMapper
           extends Mapper<Object, Text, Text, Text>{

      private Text word = new Text();
      private Text docID = new Text();

      public void map(Object key, Text value, Context context
      ) throws IOException, InterruptedException {
         String[] valueSplit = value.toString().split("\t");
         // Remove puntuation and lower case
         String valueStr = valueSplit[1].replaceAll("[^a-zA-Z ]", " ").toLowerCase();
         docID.set(valueSplit[0]);

         StringTokenizer itr = new StringTokenizer(valueStr);
         while (itr.hasMoreTokens()) {
            word.set(itr.nextToken());
            context.write(word, docID);
         }
      }
   }

   public static class IntSumReducer
           extends Reducer<Text,Text,Text,Text> {


      private Text result = new Text();

      public void reduce(Text key, Iterable<Text> values,
                         Context context
      ) throws IOException, InterruptedException {
         Map<String, Integer> invIndex = new HashMap<String, Integer>();

         for (Text val : values) {
            String val2 = val.toString();
            if (invIndex.containsKey(val2)) {
               invIndex.put(val2, (Integer) invIndex.get(val2) + 1);
            }
            else {
               invIndex.put(val2, 1);
            }
         }

         StringBuilder mapStr = new StringBuilder();
         for (String keyi: invIndex.keySet()) {
            if (!keyi.equals("")) {
               mapStr.append(keyi + ":" + invIndex.get(keyi) + " ");
            }
         }
         result.set(mapStr.toString());
         context.write(key, result);
      }
   }

   public static void main(String[] args) throws Exception {
      Configuration conf = new Configuration();
      Job job = Job.getInstance(conf, "word count");
      job.setJarByClass(InvertedIndex.class);
      job.setMapperClass(TokenizerMapper.class);
      job.setCombinerClass(IntSumReducer.class);
      job.setReducerClass(IntSumReducer.class);
      job.setOutputKeyClass(Text.class);
      job.setOutputValueClass(Text.class);
      FileInputFormat.addInputPath(job, new Path(args[0]));
      FileOutputFormat.setOutputPath(job, new Path(args[1]));
      System.exit(job.waitForCompletion(true) ? 0 : 1);
   }
}