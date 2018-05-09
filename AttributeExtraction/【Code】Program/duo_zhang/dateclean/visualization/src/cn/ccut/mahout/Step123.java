package cn.ccut.mahout;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;
import org.apache.mahout.classifier.df.data.DescriptorException;
import org.apache.mahout.classifier.df.mapreduce.BuildForest;
import org.apache.mahout.classifier.df.mapreduce.TestForest;
import org.apache.mahout.classifier.df.tools.Describe;
import org.apache.mahout.common.HadoopUtil;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;

public class Step123 {
    private static FileSystem fileSystem;

    static {
        Configuration conf = new Configuration();
        try {
            fileSystem = FileSystem.get(new URI("hdfs://111.116.20.110:9000/"), conf, "hadoop");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        //删除本地f1score文件
        File f1scoreFile = new File("C:/Users/zhipeng-Tong/Desktop/异常企业资料/F1_Score/f1score_list.txt");
        if(f1scoreFile.exists()) {
            f1scoreFile.delete();
        }

        //查询HDFS文件系统中训练集测试集则数
        int CVNum = getCVNumByHDFS();

        for(int i = 0; i < CVNum; i++) {
            Step1Describe(i);
            Step2BuildForest(i);
            Step3TestForest(i);
        }

        fileSystem.close();
        //计算F1score
        computeF1score();
    }

    /**
     * Step1：描述数据文件
     *
     * @throws IOException
     * @throws DescriptorException
     */
    public static void Step1Describe(int i) throws IOException, DescriptorException {
        String[] args =new String[]{
                "-p", "hdfs://111.116.20.110:9000/user/hadoop/mahout/" + i + "-CV_train.dat",
                "-f", "hdfs://111.116.20.110:9000/user/hadoop/mahout/train.info",
                "-d", "I", "2", "N", "13", "C", "L"
        };

        HadoopUtil.delete(new Configuration(), new Path(args[Arrays.asList(args).indexOf("-f") + 1]));
        Describe.main(args);
    }

    /**
     * Step2：训练森林
     *
     * @throws Exception
     */
    private static void Step2BuildForest(int i) throws Exception {
        String[] args = new String[]{
                "-Drapred.max.split.size=1874231",
                "-d", "hdfs://111.116.20.110:9000/user/hadoop/mahout/" + i + "-CV_train.dat",
                "-ds", "hdfs://111.116.20.110:9000/user/hadoop/mahout/train.info",
                "-o", "hdfs://111.116.20.110:9000/user/hadoop/mahout/forest_result",
                "-sl", "4",
                "-p", "-t", "100"
        };

        HadoopUtil.delete(new Configuration(), new Path(args[Arrays.asList(args).indexOf("-o") + 1]));
        BuildForest.main(args);
    }

    /**
     * Step3：测试模型
     *
     * @throws Exception
     */
    private static void Step3TestForest(int i) throws Exception {
        String[] args = new String[]{
                "-i", "hdfs://111.116.20.110:9000/user/hadoop/mahout/" + i + "-CV_test.dat",
                "-ds", "hdfs://111.116.20.110:9000/user/hadoop/mahout/train.info",
                "-o", "hdfs://111.116.20.110:9000/user/hadoop/mahout/predictions",
                "-m", "hdfs://111.116.20.110:9000/user/hadoop/mahout/forest_result",
                "-a"
        };

        //删除hdfs中的文件
        HadoopUtil.delete(new Configuration(), new Path(args[Arrays.asList(args).indexOf("-o") + 1]));

        TestForest.main(args);
    }

    /**
     * 计算平均F1score分数
     *
     * @throws IOException
     */
    private static void computeF1score() throws IOException {
        File f1scoreFile = new File("D:\\视频诗词/异常企业资料/F1_Score/f1score_list.txt");
        BufferedReader reader = new BufferedReader(new FileReader(f1scoreFile));
        //文件原始数据
        ArrayList<Double> data = new ArrayList<>();

        String line = null;
        while ((line = reader.readLine()) != null) {
            data.add(Double.parseDouble(line));
        }

        reader.close();

        //计算平均f1score
        Double f1score = 0.0;
        Double count = 0.0;
        Double sum = 0.0;

        for(Double d : data) {
            count += d;
            sum ++;
        }

        f1score = count/sum;
        System.out.println("平均WeightedF1score:" + f1score);
    }

    /**
     * 查询HDFS文件系统VC个数
     *
     * @return
     * @throws Exception
     */
    public static int getCVNumByHDFS() throws Exception {
        /*Configuration conf = new Configuration();
        FileSystem fileSystem = FileSystem.get(new URI("hdfs://111.116.20.110:9000/"), conf, "hadoop");*/
        int count = 0;

        FileStatus[] fileStatuses = HadoopUtil.listStatus(fileSystem,
                new Path("/user/hadoop/mahout"), new PathFilter() {
                    @Override
                    public boolean accept(Path path) {
                        String filePath = path.toString();
                        if(filePath.contains("-CV_train.dat")) {
                            return true;
                        } else {
                            return false;
                        }
                    }
                });

        for(FileStatus file : fileStatuses) {
            count++;
        }

        return count;
    }
}
