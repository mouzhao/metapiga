/*     */ package grid;
/*     */ 
/*     */ import java.util.ArrayList;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.TreeMap;
/*     */ import xwchclientapi.XWCHClient;
/*     */ import xwchclientapi.util.JobStatsEnumType;
/*     */ 
/*     */ public class GridClient
/*     */ {
/*     */   private final XWCHClient client;
/*  22 */   private boolean checkGridStatus = true;
/*  23 */   private Map<String, GridMonitor.GridStatus> jobs = new TreeMap();
/*     */ 
/*     */   public GridClient(XWCHClient xhchClient) {
/*  26 */     this.client = xhchClient;
/*  27 */     new Thread(new Runnable() {
/*     */       public void run() {
/*  29 */         while (GridClient.this.checkGridStatus)
/*     */           try {
/*  31 */             Thread.sleep(4000L);
/*  32 */             int numJob = 0;
/*  33 */             List jobList = new ArrayList(100);
/*  34 */             for (String jobId : GridClient.this.jobs.keySet()) {
/*  35 */               if (numJob < 100) {
/*  36 */                 jobList.add(jobId);
/*     */               }
/*     */               else {
/*  39 */                 for (String job : jobList) {
/*  40 */                   GridClient.this.jobs.put(job, GridMonitor.GridStatus.valueOf(GridClient.this.client.GetJobStatus(job).toString()));
/*     */                 }
/*     */ 
/*  48 */                 jobList.clear();
/*  49 */                 jobList.add(jobId);
/*     */               }
/*     */             }
/*  52 */             if (!jobList.isEmpty())
/*     */             {
/*  54 */               for (String job : jobList) {
/*     */                 try {
/*  56 */                   GridClient.this.jobs.put(job, GridMonitor.GridStatus.valueOf(GridClient.this.client.GetJobStatus(job).toString()));
/*     */                 }
/*     */                 catch (NullPointerException localNullPointerException)
/*     */                 {
/*     */                 }
/*     */ 
/*     */               }
/*     */ 
/*     */             }
/*     */ 
/*     */           }
/*     */           catch (Exception e)
/*     */           {
/*  70 */             e.printStackTrace();
/*     */           }
/*     */       }
/*     */     }).start();
/*     */   }
/*     */ 
/*     */   public void close() {
/*  78 */     this.checkGridStatus = false;
/*     */   }
/*     */ 
/*     */   public synchronized String getUniqueId() throws Exception {
/*  82 */     String id = this.client.GetUniqueID();
/*  83 */     return id;
/*     */   }
/*     */ 
/*     */   public synchronized String addJob(String description, String applicationid, String moduleid, String cmdline, String inputfiles, String listefileout, String outputfilename, String extrafields) throws Exception
/*     */   {
/*  88 */     String jobId = this.client.AddJob(description, applicationid, moduleid, cmdline, 
/*  89 */       inputfiles, listefileout, outputfilename, extrafields);
/*  90 */     this.jobs.put(jobId, GridMonitor.GridStatus.WAITING);
/*  91 */     return jobId;
/*     */   }
/*     */ 
/*     */   public synchronized void getJobResult(String jobId, String outputFile) throws Exception {
/*  95 */     this.client.GetJobResult(jobId, outputFile);
/*     */   }
/*     */ 
/*     */   public void killJob(String jobId, String applicationId) throws Exception {
/*  99 */     this.client.KillJob(jobId, applicationId);
/*     */   }
/*     */ 
/*     */   public GridMonitor.GridStatus getJobStatus(String jobId)
/*     */   {
/*     */     try {
/* 105 */       return (GridMonitor.GridStatus)this.jobs.get(jobId); } catch (Exception e) {
/*     */     }
/* 107 */     return GridMonitor.GridStatus.ERROR;
/*     */   }
/*     */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.grid.GridClient
 * JD-Core Version:    0.6.2
 */