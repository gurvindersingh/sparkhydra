package org.systemsbiology.hadoop;

/**
 * org.systemsbiology.hadoop.JobSizeEnum
 *    While not directly mentioning Hadoop it allows usert to give some sense
 *    if the job size which can be used for tuning
 * @author Steve Lewis
 * @date 06/11/13
 */
public enum JobSizeEnum {
    Micro,   // very small test jobs - usually appropriate for one machine
    Small,   // could run well on a small cluster - 4-8 machines
    Medium,    // between large and medium - might run well on 16 nodes
    Large,    // will run on a medium cluster but should use full resources
    Enormous  // cluster resources are stretched and the job may fail for lack of resources
}
