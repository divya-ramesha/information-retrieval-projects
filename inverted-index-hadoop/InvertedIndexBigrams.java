import java.io.*;
import java.util.*;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class InvertedIndexBigrams {

    public static class TokenizerMapper extends Mapper<Object, Text, Text, Text>{
        private Text BIGRAM = new Text();

        @Override
        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
            String[] tokens = value.toString().split("\t", 2);
            Text docID = new Text(tokens[0]);
            String document = tokens[1].toLowerCase().replaceAll("[^a-z ]+", " ");
            StringTokenizer itr = new StringTokenizer(document);
            String prev = null;
            while (itr.hasMoreTokens()) {
                String cur = itr.nextToken();
                if (prev != null)
                {
                    BIGRAM.set(prev + " " + cur);
                    context.write(BIGRAM, docID);
                }
                prev = cur;
            }
        }
    }

    public static class WordDocumentReducer extends Reducer<Text, Text, Text, Text>{

        @Override
        public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            HashMap<String, Integer> counter = new HashMap<>();
            for(Text val : values) {
                String value = val.toString();
                if(counter.containsKey(value)) {
                    counter.put(value, counter.get(value) + 1);
                } else {
                    counter.put(value, new Integer(1));
                }
            }
            StringBuilder docIdCount = new StringBuilder();
            for(String docId: counter.keySet())
            {
                docIdCount.append(docId + ":" + counter.get(docId) + " ");
            }
            Text outputValue = new Text(docIdCount.toString());
            context.write(key, outputValue);
        }
    }

    public static void main(String[] args) throws Exception {
        if(args.length != 2) {
            System.out.println("Not enough arguments");
        } else {
            Configuration conf = new Configuration();
            Job job = Job.getInstance(conf, "inverted index bigrams");
            job.setJarByClass(InvertedIndexBigrams.class);
            job.setMapperClass(TokenizerMapper.class);
            job.setReducerClass(WordDocumentReducer.class);
            job.setMapOutputKeyClass(Text.class);
            job.setMapOutputValueClass(Text.class);
            job.setOutputKeyClass(Text.class);
            job.setOutputValueClass(Text.class);
            FileInputFormat.addInputPath(job, new Path(args[0]));
            FileOutputFormat.setOutputPath(job, new Path(args[1]));
            System.exit(job.waitForCompletion(true) ? 0 : 1);
        }
    }
}