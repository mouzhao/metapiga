/*     */ package cloud;
/*     */ 
/*     */ import java.util.ArrayList;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.TreeMap;
/*     */ import java.util.UUID;
/*     */ import org.hepia.venusc.AzureJavaAPI;
/*     */ import org.hepia.venusc.JobStatus;
/*     */ import org.hepia.venusc.VMStatus;
/*     */ import org.hepia.venusc.VenusCloudClient;
/*     */ 
/*     */ public class CloudClient
/*     */ {
/*     */   private final VenusCloudClient client;
/*  17 */   private boolean checkCloudStatus = true;
/*  18 */   private Map<String, CloudMonitor.CloudStatus> jobsMap = new TreeMap();
/*  19 */   private Map<String, String> jobsOutFileMap = new TreeMap();
/*     */ 
/*     */   public CloudClient(VenusCloudClient venusCloudClient) {
/*  22 */     this.client = venusCloudClient;
/*  23 */     new Thread(new Runnable()
/*     */     {
/*     */       public void run()
/*     */       {
/*  27 */         while (CloudClient.this.checkCloudStatus)
/*     */           try {
/*  29 */             Thread.sleep(4000L);
/*  30 */             int numJob = 0;
/*  31 */             List jobList = new ArrayList(100);
/*  32 */             for (String jobId : CloudClient.this.jobsMap.keySet()) {
/*  33 */               if (numJob < 100) {
/*  34 */                 jobList.add(jobId);
/*  35 */                 numJob++;
/*     */               } else {
/*  37 */                 for (String job : jobList) {
/*  38 */                   CloudClient.this.jobsMap.put(job, CloudMonitor.CloudStatus.valueOf(CloudClient.this.client.getJobStatus(job).toString()));
/*     */                 }
/*  40 */                 jobList.clear();
/*  41 */                 jobList.add(jobId);
/*  42 */                 numJob = 1;
/*     */               }
/*     */             }
/*  45 */             if (!jobList.isEmpty()) {
/*  46 */               for (String job : jobList)
/*  47 */                 CloudClient.this.jobsMap.put(job, CloudMonitor.CloudStatus.valueOf(CloudClient.this.client.getJobStatus(job).toString()));
/*     */             }
/*     */           }
/*     */           catch (Exception e)
/*     */           {
/*  52 */             e.printStackTrace();
/*     */           }
/*     */       }
/*     */     }).start();
/*     */   }
/*     */ 
/*     */   public void close()
/*     */   {
/*  62 */     this.checkCloudStatus = false;
/*     */   }
/*     */   public synchronized String getUniqueId() throws Exception {
/*  65 */     String id = UUID.randomUUID().toString();
/*  66 */     return id;
/*     */   }
/*     */ 
/*     */   public synchronized String addJob(String description, String applicationid, String outputfilename, String storageConnectionString, String extrafields) throws Exception
/*     */   {
/*  71 */     String jobId = this.client.submitJob("Metapiga job", outputfilename, applicationid, storageConnectionString, extrafields);
/*  72 */     if (jobId == null) {
/*  73 */       throw new Exception("Cannot submit job to the cloud!");
/*     */     }
/*  75 */     this.jobsMap.put(jobId, CloudMonitor.CloudStatus.PENDING);
/*  76 */     this.jobsOutFileMap.put(jobId, outputfilename);
/*     */ 
/*  78 */     return jobId;
/*     */   }
/*     */ 
/*     */   public synchronized boolean getJobResult(String outputFile, String containerName, String storageConnectionString) throws Exception
/*     */   {
/*  83 */     return AzureJavaAPI.downloadFile(outputFile, containerName, storageConnectionString);
/*     */   }
/*     */ 
/*     */   public synchronized boolean getJobLogFile(String outputFile, String containerName, String storageConnectionString) throws Exception {
/*  87 */     return AzureJavaAPI.downloadFile(outputFile, containerName, storageConnectionString);
/*     */   }
/*     */ 
/*     */   public void killJob(String jobId, String applicationId) throws Exception
/*     */   {
/*     */   }
/*     */ 
/*     */   public CloudMonitor.CloudStatus getJobStatus(String jobId)
/*     */   {
/*     */     try {
/*  97 */       return (CloudMonitor.CloudStatus)this.jobsMap.get(jobId); } catch (Exception e) {
/*     */     }
/*  99 */     return CloudMonitor.CloudStatus.FAILED;
/*     */   }
/*     */ 
/*     */   public CloudMonitor.CloudStatus getJobStatusDirectly(String jobId)
/*     */   {
/* 104 */     return CloudMonitor.CloudStatus.valueOf(this.client.getJobStatus(jobId).toString());
/*     */   }
/*     */ 
/*     */   public void deleteAllTerminatedJobs() {
/* 108 */     this.client.deleteAllTerminatedJobs();
/*     */   }
/*     */ 
/*     */   public VMStatus deleteVirtualMachines() {
/* 112 */     return this.client.deleteVirtualMachines();
/*     */   }
/*     */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.cloud.CloudClient
 * JD-Core Version:    0.6.2
 */