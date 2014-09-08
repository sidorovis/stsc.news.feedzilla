package stsc.distributed.hadoop;

import java.io.IOException;

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import stsc.distributed.hadoop.types.SimulatorSettingsWritable;
import stsc.distributed.hadoop.types.TradingStrategyWritable;

public class HadoopStarterExample extends Configured implements Tool {

	@Override
	public int run(String[] args) throws Exception {
		@SuppressWarnings("deprecation")
		final Job job = new Job(new JobConf(this.getConf()), "StscOnHadoopExample");
		job.setJarByClass(HadoopStarterExample.class);

		job.setInputFormatClass(GridInputFormat.class);
		job.setOutputFormatClass(GridOutputFormat.class);

		job.setOutputKeyClass(LongWritable.class);
		job.setOutputValueClass(SimulatorSettingsWritable.class);

		job.setMapperClass(SimulatorMapper.class);
		job.setReducerClass(SimulatorReducer.class);

		job.waitForCompletion(true);
		return 0;
	}

	public static void main(String[] args) throws IOException {
		try {
			ToolRunner.run(new HadoopStarterExample(), args);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
