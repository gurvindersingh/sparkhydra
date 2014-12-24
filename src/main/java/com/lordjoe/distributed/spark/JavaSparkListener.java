package com.lordjoe.distributed.spark;

import org.apache.spark.scheduler.*;

/**
* com.lordjoe.distributed.spark.JavaSparkListener
* User: Steve
* Date: 12/16/2014
*/
public class JavaSparkListener implements SparkListener {
     @Override
    public void onStageCompleted(final SparkListenerStageCompleted stageCompleted) {
         StageInfo stageInfo = stageCompleted.stageInfo();
       }

    @Override
    public void onStageSubmitted(final SparkListenerStageSubmitted stageSubmitted) {
        StageInfo stageInfo = stageSubmitted.stageInfo();
    }

    @Override
    public void onTaskStart(final SparkListenerTaskStart taskStart) {
        TaskInfo taskInfo = taskStart.taskInfo();
        int id = taskStart.stageId();
      }

    @Override
    public void onTaskGettingResult(final SparkListenerTaskGettingResult taskGettingResult) {
        TaskInfo taskInfo = taskGettingResult.taskInfo();
        }

    @Override
    public void onTaskEnd(final SparkListenerTaskEnd taskEnd) {
        TaskInfo taskInfo = taskEnd.taskInfo();
        int id = taskEnd.stageId();
     }

    @Override
    public void onJobStart(final SparkListenerJobStart jobStart) {
       }

    @Override
    public void onJobEnd(final SparkListenerJobEnd jobEnd) {
     }

    @Override
    public void onEnvironmentUpdate(final SparkListenerEnvironmentUpdate environmentUpdate) {
      }

    @Override
    public void onBlockManagerAdded(final SparkListenerBlockManagerAdded blockManagerAdded) {
     }

    @Override
    public void onBlockManagerRemoved(final SparkListenerBlockManagerRemoved blockManagerRemoved) {
      }

    @Override
    public void onUnpersistRDD(final SparkListenerUnpersistRDD unpersistRDD) {
      }

    @Override
    public void onApplicationStart(final SparkListenerApplicationStart applicationStart) {
      }

    @Override
    public void onApplicationEnd(final SparkListenerApplicationEnd applicationEnd) {
     }

    @Override
    public void onExecutorMetricsUpdate(final SparkListenerExecutorMetricsUpdate executorMetricsUpdate) {
     }
}
