/*     */ package cloud;
/*     */ 
/*     */ import java.io.File;
/*     */ import java.io.FileWriter;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Date;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Map.Entry;
/*     */ import java.util.Set;
/*     */ import java.util.concurrent.CountDownLatch;
/*     */ import javax.swing.SwingUtilities;
/*     */ import metapiga.io.NewickReader;
/*     */ import metapiga.monitors.Monitor.MonitorType;
/*     */ import metapiga.monitors.SearchConsole;
/*     */ import metapiga.monitors.SearchSilent;
/*     */ import metapiga.parameters.Parameters;
/*     */ import metapiga.trees.Tree;
/*     */ import metapiga.utilities.Tools;
/*     */ import org.biojavax.bio.phylo.io.nexus.MyNexusFileBuilder;
/*     */ import org.biojavax.bio.phylo.io.nexus.NexusBlock;
/*     */ import org.biojavax.bio.phylo.io.nexus.NexusComment;
/*     */ import org.biojavax.bio.phylo.io.nexus.NexusFile;
/*     */ import org.biojavax.bio.phylo.io.nexus.NexusFileFormat;
/*     */ import org.biojavax.bio.phylo.io.nexus.TreesBlock;
/*     */ import org.biojavax.bio.phylo.io.nexus.TreesBlock.NewickTreeString;
/*     */ 
/*     */ public class SearchConsoleCloud
/*     */   implements CloudMonitor
/*     */ {
/*     */   private CloudMonitor.JobStatus jobStatus;
/*     */   private SearchConsole searchConsole;
/*     */   private SearchSilent searchSilent;
/*     */   private final Monitor.MonitorType parentType;
/*     */   private final String searchDir;
/*     */   private final Parameters parameters;
/*     */   private final int replicateNumber;
/*     */   private CloudConnection cloud;
/*     */   private String jobId;
/*     */   private CloudClient client;
/*     */   private CountDownLatch latch;
/*     */ 
/*     */   public SearchConsoleCloud(SearchConsole parent, Parameters parameters, int replicateNumber)
/*     */   {
/*  47 */     this.searchConsole = parent;
/*  48 */     this.parentType = Monitor.MonitorType.CONSOLE;
/*  49 */     this.searchDir = parent.dirPath;
/*  50 */     this.parameters = parameters;
/*  51 */     this.replicateNumber = replicateNumber;
/*  52 */     this.jobStatus = CloudMonitor.JobStatus.NOT_INITIALIZED;
/*     */   }
/*     */ 
/*     */   public SearchConsoleCloud(SearchSilent parent, Parameters parameters, int replicateNumber) {
/*  56 */     this.searchSilent = parent;
/*  57 */     this.parentType = Monitor.MonitorType.SILENT;
/*  58 */     this.searchDir = parent.dirPath;
/*  59 */     this.parameters = parameters;
/*  60 */     this.replicateNumber = replicateNumber;
/*  61 */     this.jobStatus = CloudMonitor.JobStatus.NOT_INITIALIZED;
/*     */   }
/*     */ 
/*     */   private void showText(String text) {
/*  65 */     switch ($SWITCH_TABLE$metapiga$monitors$Monitor$MonitorType()[this.parentType.ordinal()]) {
/*     */     case 4:
/*  67 */       this.searchConsole.showText(text);
/*  68 */       break;
/*     */     case 5:
/*  70 */       break;
/*     */     }
/*     */   }
/*     */ 
/*     */   public CloudMonitor.CloudMonitorType getCloudMonitorType()
/*     */   {
/*  78 */     switch ($SWITCH_TABLE$metapiga$monitors$Monitor$MonitorType()[this.parentType.ordinal()]) {
/*     */     case 4:
/*  80 */       return CloudMonitor.CloudMonitorType.CONSOLE;
/*     */     case 5:
/*     */     }
/*  83 */     return CloudMonitor.CloudMonitorType.SILENT;
/*     */   }
/*     */ 
/*     */   public CloudMonitor.CloudStatus getCloudStatus()
/*     */   {
/*  89 */     switch ($SWITCH_TABLE$metapiga$cloud$CloudMonitor$JobStatus()[this.jobStatus.ordinal()]) {
/*     */     case 1:
/*     */     case 2:
/*  92 */       return CloudMonitor.CloudStatus.PENDING;
/*     */     case 3:
/*     */     case 4:
/*  95 */       return this.client.getJobStatus(this.jobId);
/*     */     case 5:
/*  97 */       return CloudMonitor.CloudStatus.CANCELLED;
/*     */     }
/*  99 */     return CloudMonitor.CloudStatus.FAILED;
/*     */   }
/*     */ 
/*     */   public CloudMonitor.JobStatus getMonitorStatus()
/*     */   {
/* 105 */     return this.jobStatus;
/*     */   }
/*     */ 
/*     */   public void initialize(CloudConnection cloud, CountDownLatch latch)
/*     */   {
/* 110 */     this.cloud = cloud;
/* 111 */     this.client = cloud.getClient();
/* 112 */     this.latch = latch;
/* 113 */     if (this.jobStatus == CloudMonitor.JobStatus.NOT_INITIALIZED)
/* 114 */       this.jobStatus = CloudMonitor.JobStatus.POOLED;
/*     */   }
/*     */ 
/*     */   public boolean isTerminated()
/*     */   {
/* 121 */     CloudMonitor.CloudStatus s = this.client.getJobStatus(this.jobId);
/*     */ 
/* 124 */     return (s == CloudMonitor.CloudStatus.CANCELLED) || 
/* 123 */       (s == CloudMonitor.CloudStatus.FAILED) || 
/* 124 */       (s == CloudMonitor.CloudStatus.FINISHED);
/*     */   }
/*     */ 
/*     */   public void run()
/*     */   {
/* 130 */     if (this.jobStatus == CloudMonitor.JobStatus.POOLED) {
/* 131 */       if (this.client == null)
/* 132 */         showText("Cloud client was unavailable when creating replicate " + 
/* 133 */           this.replicateNumber + ", operation aborted");
/*     */       else {
/*     */         try {
/* 136 */           String outputFile = this.client.getUniqueId();
/* 137 */           this.jobId = this.client.addJob("MetaPIGA_replicate_" + this.replicateNumber, "MetapigaApplication", 
/* 138 */             outputFile, this.cloud.getStorageConnectionString(), "");
/* 139 */           if (this.jobId == null) {
/* 140 */             showText("Error when creating job on cloud for replicate " + this.replicateNumber);
/* 141 */             this.jobStatus = CloudMonitor.JobStatus.CANCELLED;
/* 142 */           } else if (this.jobStatus == CloudMonitor.JobStatus.CANCELLED) {
/* 143 */             new Thread(new Runnable()
/*     */             {
/*     */               public void run()
/*     */               {
/*     */                 try {
/* 148 */                   SearchConsoleCloud.this.client.deleteAllTerminatedJobs();
/*     */                 } catch (Exception e) {
/* 150 */                   SearchConsoleCloud.this.showText("Problem when deleting cancelled and/or finished replicates " + 
/* 151 */                     SearchConsoleCloud.this.replicateNumber + " : " + Tools.getErrorMessage(e));
/*     */                 }
/*     */               }
/*     */             }).start();
/* 156 */             showStatus(CloudMonitor.CloudStatus.CANCELLED);
/*     */           } else {
/* 158 */             this.jobStatus = CloudMonitor.JobStatus.RUNNING;
/* 159 */             CloudMonitor.CloudStatus status = getCloudStatus();
/* 160 */             showStatus(status);
/* 161 */             while ((status != CloudMonitor.CloudStatus.FINISHED) && (status != CloudMonitor.CloudStatus.CANCELLED) && (this.jobStatus == CloudMonitor.JobStatus.RUNNING)) {
/*     */               try {
/* 163 */                 Thread.sleep(500L);
/*     */               } catch (InterruptedException ex) {
/* 165 */                 ex.printStackTrace();
/*     */               }
/* 167 */               status = getCloudStatus();
/* 168 */               showStatus(status);
/*     */             }
/* 170 */             if (this.jobStatus == CloudMonitor.JobStatus.RUNNING) {
/* 171 */               this.jobStatus = CloudMonitor.JobStatus.TERMINATED;
/* 172 */               if (status == CloudMonitor.CloudStatus.FINISHED) {
/* 173 */                 this.client.getJobResult(outputFile, this.cloud.getDataContainerName(), this.cloud.getStorageConnectionString());
/* 174 */                 this.client.getJobLogFile(outputFile, this.cloud.getDataContainerName(), this.cloud.getStorageConnectionString());
/* 175 */                 outputDir = new File(this.searchDir + "/" + "Replicate " + this.replicateNumber);
/* 176 */                 zipFile = new File(this.searchDir + "/" + outputFile);
/* 177 */                 if (zipFile.exists()) {
/* 178 */                   Tools.decompress(zipFile, outputDir, true);
/* 179 */                   nexus = new File(outputFile + "/" + this.parameters.cloudOutput);
/* 180 */                   if (nexus.exists()) {
/* 181 */                     MyNexusFileBuilder builder = new MyNexusFileBuilder();
/* 182 */                     NexusFileFormat.parseFile(builder, nexus);
/* 183 */                     NexusFile file = builder.getNexusFile();
/* 184 */                     List solutionTrees = new ArrayList();
/* 185 */                     for (Iterator it = file.blockIterator(); it.hasNext(); ) {
/* 186 */                       NexusBlock block = (NexusBlock)it.next();
/* 187 */                       if (block.getBlockName().equals("TREES")) {
/* 188 */                         TreesBlock tb = (TreesBlock)block;
/* 189 */                         int treeNum = 0;
/* 190 */                         for (Iterator tr = tb.getTrees().entrySet().iterator(); tr.hasNext(); ) {
/* 191 */                           Entry e = (Entry)tr.next();
/* 192 */                           NewickReader nr = new NewickReader(this.parameters, (String)e.getKey(), ((TreesBlock.NewickTreeString)e.getValue()).getTreeString(), tb.getTranslations());
/* 193 */                           Tree tree = nr.parseNewick();
/* 194 */                           NexusComment nc = (NexusComment)tb.getComments().get(++treeNum);
/* 195 */                           for (Iterator sub = nc.commentIterator(); sub.hasNext(); ) {
/* 196 */                             String comment = sub.next().toString();
/* 197 */                             if (comment.startsWith("Likelihood")) {
/* 198 */                               tree.parseEvaluationString(comment);
/*     */                             }
/*     */                           }
/*     */ 
/* 202 */                           if (this.parameters.hasManyReplicates()) {
/* 203 */                             tree.setName(tree.getName() + "_Rep_" + this.replicateNumber);
/*     */                           }
/* 205 */                           solutionTrees.add(tree);
/*     */                         }
/*     */                       }
/*     */                     }
/* 209 */                     switch ($SWITCH_TABLE$metapiga$monitors$Monitor$MonitorType()[this.parentType.ordinal()]) {
/*     */                     case 4:
/* 211 */                       this.searchConsole.addSolutionTree(solutionTrees);
/* 212 */                       break;
/*     */                     case 5:
/* 214 */                       this.searchSilent.addSolutionTree(solutionTrees);
/* 215 */                       break;
/*     */                     default:
/* 217 */                       break;
/*     */                     }
/*     */                   } else {
/* 220 */                     showText("Job " + this.jobId + " was not cancelled and replicate " + 
/* 221 */                       this.replicateNumber + " was not completed.");
/* 222 */                     this.jobStatus = CloudMonitor.JobStatus.TERMINATED;
/*     */                   }
/*     */                 } else {
/* 225 */                   showText("Replicate " + this.replicateNumber + " results cannot be retrived.");
/* 226 */                   this.jobStatus = CloudMonitor.JobStatus.TERMINATED;
/* 227 */                   showStatus(CloudMonitor.CloudStatus.FAILED);
/*     */                 }
/*     */               } else {
/* 230 */                 showText("Replicate " + this.replicateNumber + " results cannot be retrived.");
/* 231 */                 this.jobStatus = CloudMonitor.JobStatus.TERMINATED;
/* 232 */                 showStatus(CloudMonitor.CloudStatus.FAILED);
/*     */               }
/*     */             }
/*     */           }
/*     */         }
/*     */         catch (Exception e)
/*     */         {
/* 238 */           File nexus;
/* 237 */           showText("\n Java exception : " + e.getCause() + " (" + e.getMessage() + ")");
/* 238 */           File zipFile = (nexus = e.getStackTrace()).length; for (File outputDir = 0; outputDir < zipFile; outputDir++) { StackTraceElement el = nexus[outputDir];
/* 239 */             showText("\tat " + el.toString());
/*     */           }
/* 241 */           showText("Replicate " + this.replicateNumber + " is probably not complited. ");
/* 242 */           this.jobStatus = CloudMonitor.JobStatus.TERMINATED;
/*     */         }
/*     */       }
/*     */     }
/* 246 */     this.latch.countDown();
/*     */   }
/*     */ 
/*     */   public void showStatus(CloudMonitor.CloudStatus status)
/*     */   {
/* 251 */     final CloudMonitor.CloudStatus s = status;
/* 252 */     SwingUtilities.invokeLater(new Runnable()
/*     */     {
/*     */       public void run()
/*     */       {
/* 256 */         switch ($SWITCH_TABLE$metapiga$monitors$Monitor$MonitorType()[SearchConsoleCloud.this.parentType.ordinal()]) {
/*     */         case 4:
/* 258 */           SearchConsoleCloud.this.searchConsole.cloudStatConsole.updateStatus(SearchConsoleCloud.this.replicateNumber, s);
/* 259 */           break;
/*     */         }
/*     */       }
/*     */     });
/*     */   }
/*     */ 
/*     */   public void stop()
/*     */   {
/* 270 */     switch ($SWITCH_TABLE$metapiga$cloud$CloudMonitor$JobStatus()[this.jobStatus.ordinal()]) {
/*     */     case 1:
/* 272 */       this.jobStatus = CloudMonitor.JobStatus.CANCELLED;
/* 273 */       showStatus(CloudMonitor.CloudStatus.CANCELLED);
/* 274 */       break;
/*     */     case 2:
/* 276 */       this.jobStatus = CloudMonitor.JobStatus.CANCELLED;
/* 277 */       break;
/*     */     case 3:
/* 279 */       new Thread(new Runnable()
/*     */       {
/*     */         public void run()
/*     */         {
/*     */           try {
/* 284 */             SearchConsoleCloud.this.client.killJob(SearchConsoleCloud.this.jobId, SearchConsoleCloud.this.cloud.getApplicationId());
/* 285 */             SearchConsoleCloud.this.jobStatus = CloudMonitor.JobStatus.CANCELLED;
/*     */           } catch (Exception e) {
/* 287 */             SearchConsoleCloud.this.showText("Problem when stopping replicate " + SearchConsoleCloud.this.replicateNumber + " : " + Tools.getErrorMessage(e));
/*     */           }
/*     */         }
/*     */       }).start();
/* 292 */       break;
/*     */     case 4:
/*     */     case 5:
/*     */     }
/*     */   }
/*     */ 
/*     */   public void updateConsensusTree(Tree consensusTree)
/*     */   {
/*     */     try
/*     */     {
/* 304 */       String endl = "\n";
/* 305 */       File output = new File(this.searchDir);
/* 306 */       if (!output.exists()) output.mkdir();
/* 307 */       output = new File(this.searchDir + "/" + "ConsensusTree.tre");
/* 308 */       fw = new FileWriter(output);
/* 309 */       fw.write("#NEXUS" + endl);
/* 310 */       fw.write(endl);
/* 311 */       fw.write("Begin trees;  [Treefile created " + new Date(System.currentTimeMillis()).toString() + "]" + endl);
/* 312 */       fw.write(consensusTree.toNewickLine(false, true) + endl);
/* 313 */       fw.write("End;" + endl);
/* 314 */       fw.close();
/*     */     } catch (Exception e) {
/* 316 */       e.printStackTrace();
/* 317 */       showText("\n Error when writing file ConsensusTree.tre");
/* 318 */       showText("\n Java exception : " + e.getCause() + " (" + e.getMessage() + ")");
/*     */       StackTraceElement[] arrayOfStackTraceElement;
/* 319 */       FileWriter localFileWriter1 = (arrayOfStackTraceElement = e.getStackTrace()).length; for (FileWriter fw = 0; fw < localFileWriter1; fw++) { StackTraceElement el = arrayOfStackTraceElement[fw];
/* 320 */         showText("\tat " + el.toString());
/*     */       }
/*     */     }
/*     */   }
/*     */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.cloud.SearchConsoleCloud
 * JD-Core Version:    0.6.2
 */