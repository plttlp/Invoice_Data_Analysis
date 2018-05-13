package cn.ccut.mapreduce;

import cn.ccut.SpecificAbnormality;
import cn.ccut.pojo.Enterprise;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;
import java.util.UUID;

/**
 * @authorMr.Robot
 * @create2018-05-13 16:54
 */
public class AnalyzeMain {
    /**
     * 分析具体异常
     *
     * @authorMr.Robot
     * @create2018-04-27 19:46
     */
    public static void analyzeMapReduce() throws IOException, ClassNotFoundException, InterruptedException{
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "AnalyzeJob");

        // 设置job中的资源所在的jar包
        job.setJarByClass(SpecificAbnormality.class);

        // job使用的mapper类
        job.setMapperClass(AnalyzeMapper.class);
        // job使用的reducer类
        job.setReducerClass(AnalyzeReducer.class);

        // job的mapper类输出的kv数据类型
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Enterprise.class);

        // job的reducer类输出的kv数据类型
        job.setOutputKeyClass(Enterprise.class);
        job.setOutputValueClass(NullWritable.class);

        // 指定原始数据存放的路径
        String inputPath = "F:\\Desktop\\sprcificabnormality\\analyzeData";
        FileInputFormat.setInputPaths(job, inputPath);

        // 指定输出数据存放的路径
        String s = UUID.randomUUID().toString();
        String outputPath = "F:\\Desktop\\sprcificabnormality\\analyzeResult\\" + s;
        FileOutputFormat.setOutputPath(job, new Path(outputPath));

        job.waitForCompletion(true);
    }
}
