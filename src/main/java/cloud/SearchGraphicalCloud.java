/*     */ package cloud;
/*     */ 
/*     */ import java.awt.Color;
/*     */ import java.awt.Dimension;
/*     */ import java.awt.FlowLayout;
/*     */ import java.awt.Font;
/*     */ import java.io.File;
/*     */ import java.io.FileInputStream;
/*     */ import java.io.FileNotFoundException;
/*     */ import java.io.FileOutputStream;
/*     */ import java.io.FileWriter;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.io.OutputStream;
/*     */ import java.io.PrintStream;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Date;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Map.Entry;
/*     */ import java.util.Set;
/*     */ import java.util.concurrent.CountDownLatch;
/*     */ import javax.swing.BorderFactory;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JLabel;
/*     */ import javax.swing.JPanel;
/*     */ import javax.swing.SwingUtilities;
/*     */ import javax.swing.border.TitledBorder;
/*     */ import metapiga.grid.GridMonitor.GridStatus;
/*     */ import metapiga.io.NewickReader;
/*     */ import metapiga.monitors.Monitor.MonitorType;
/*     */ import metapiga.monitors.SearchBatchGraphical;
/*     */ import metapiga.monitors.SearchOnceGraphical;
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
/*     */ public class SearchGraphicalCloud extends JPanel
/*     */   implements CloudMonitor
/*     */ {
/*     */   private CloudMonitor.JobStatus jobStatus;
/*     */   private SearchOnceGraphical searchOnceGraphical;
/*     */   private SearchBatchGraphical searchBatchGraphical;
/*     */   private final Monitor.MonitorType parentType;
/*     */   private final String searchDir;
/*     */   private final Parameters parameters;
/*     */   private final int replicateNumber;
/*     */   private CloudConnection cloud;
/*     */   private String jobId;
/*     */   private String applicationId;
/*     */   private CloudClient client;
/*     */   private CountDownLatch latch;
/*  62 */   private String[] animations = { "—", "\\", "|", "/" };
/*  63 */   private int animationIndex = 0;
/*  64 */   private final JLabel statusLabel = new JLabel();
/*  65 */   private final JButton coloredSquare = new JButton();
/*  66 */   private final JLabel animatedLabel = new JLabel();
/*     */ 
/*     */   public SearchGraphicalCloud(SearchOnceGraphical parent, Parameters parameters, int replicateNumber) {
/*  69 */     this.searchOnceGraphical = parent;
/*  70 */     this.parentType = Monitor.MonitorType.SINGLE_SEARCH_GRAPHICAL;
/*  71 */     this.searchDir = parent.dirPath;
/*  72 */     this.parameters = parameters;
/*  73 */     this.replicateNumber = replicateNumber;
/*  74 */     this.jobStatus = CloudMonitor.JobStatus.NOT_INITIALIZED;
/*     */     try {
/*  76 */       jbinit();
/*     */     } catch (Exception ex) {
/*  78 */       ex.printStackTrace();
/*     */     }
/*     */   }
/*     */ 
/*     */   public SearchGraphicalCloud(SearchBatchGraphical parent, Parameters parameters, int replicateNumber) {
/*  83 */     this.searchBatchGraphical = parent;
/*  84 */     this.parentType = Monitor.MonitorType.BATCH_SEARCH_GRAPHICAL;
/*  85 */     this.searchDir = parent.dirPath;
/*  86 */     this.parameters = parameters;
/*  87 */     this.replicateNumber = replicateNumber;
/*  88 */     this.jobStatus = CloudMonitor.JobStatus.NOT_INITIALIZED;
/*     */     try {
/*  90 */       jbinit();
/*     */     } catch (Exception ex) {
/*  92 */       ex.printStackTrace();
/*     */     }
/*     */   }
/*     */ 
/*     */   private void jbinit() {
/*  97 */     setLayout(new FlowLayout(3, 5, 5));
/*  98 */     setBorder(new TitledBorder(BorderFactory.createEtchedBorder(Color.green, Color.black), "Replicate " + this.replicateNumber));
/*  99 */     this.animatedLabel.setFont(new Font("Tahoma", 1, 11));
/* 100 */     this.animatedLabel.setPreferredSize(new Dimension(15, 15));
/* 101 */     this.animatedLabel.setText(this.animations[this.animationIndex]);
/* 102 */     this.coloredSquare.setPreferredSize(new Dimension(15, 15));
/* 103 */     this.coloredSquare.setBackground(GridMonitor.GridStatus.QUEUED.getColor());
/* 104 */     this.coloredSquare.setBorder(BorderFactory.createRaisedBevelBorder());
/* 105 */     this.coloredSquare.setContentAreaFilled(false);
/* 106 */     this.coloredSquare.setOpaque(true);
/* 107 */     this.coloredSquare.setToolTipText(GridMonitor.GridStatus.QUEUED.getTooltipText());
/* 108 */     this.statusLabel.setPreferredSize(new Dimension(80, 15));
/* 109 */     this.statusLabel.setText(GridMonitor.GridStatus.QUEUED.toString());
/* 110 */     add(this.animatedLabel);
/* 111 */     add(this.coloredSquare);
/* 112 */     add(this.statusLabel);
/*     */   }
/*     */ 
/*     */   private boolean cutPasteResultFile(String srFile, String dtDirectory, String dtFile, boolean isResult) {
/*     */     try {
/* 117 */       File f1 = new File(srFile);
/* 118 */       if (!f1.exists()) return false;
/* 119 */       File f2 = new File(dtDirectory + "/" + dtFile);
/* 120 */       InputStream in = new FileInputStream(f1);
/*     */ 
/* 126 */       OutputStream out = new FileOutputStream(f2);
/*     */ 
/* 128 */       byte[] buf = new byte[2048];
/*     */       int len;
/* 130 */       while ((len = in.read(buf)) > 0)
/*     */       {
/*     */         int len;
/* 131 */         out.write(buf, 0, len);
/*     */       }
/* 133 */       in.close();
/* 134 */       out.close();
/* 135 */       f1.delete();
/* 136 */       if (isResult) {
/* 137 */         f2.renameTo(new File(dtDirectory + "/" + this.parameters.cloudOutput));
/*     */       }
/*     */     }
/*     */     catch (FileNotFoundException ex)
/*     */     {
/* 142 */       System.out.println(ex.getMessage() + " in the specified directory.");
/* 143 */       return false;
/*     */     }
/*     */     catch (IOException e) {
/* 146 */       System.out.println(e.getMessage());
/* 147 */       return false;
/*     */     }
/* 149 */     return true;
/*     */   }
/*     */ 
/*     */   public CloudMonitor.CloudMonitorType getCloudMonitorType()
/*     */   {
/* 154 */     switch ($SWITCH_TABLE$metapiga$monitors$Monitor$MonitorType()[this.parentType.ordinal()]) {
/*     */     case 2:
/* 156 */       return CloudMonitor.CloudMonitorType.SINGLE_SEARCH_GRAPHICAL;
/*     */     case 3:
/* 158 */       return CloudMonitor.CloudMonitorType.BATCH_SEARCH_GRAPHICAL;
/*     */     }
/*     */ 
/* 162 */     return null;
/*     */   }
/*     */ 
/*     */   private void showText(String text) {
/* 166 */     switch ($SWITCH_TABLE$metapiga$monitors$Monitor$MonitorType()[this.parentType.ordinal()]) {
/*     */     case 2:
/* 168 */       this.searchOnceGraphical.showText(text);
/* 169 */       break;
/*     */     case 3:
/* 171 */       this.searchBatchGraphical.showText(text);
/* 172 */       break;
/*     */     }
/*     */   }
/*     */ 
/*     */   private void nextAnimation()
/*     */   {
/* 179 */     if (this.animationIndex == this.animations.length - 1) this.animationIndex = 0; else
/* 180 */       this.animationIndex += 1;
/* 181 */     this.animatedLabel.setText(this.animations[this.animationIndex]);
/*     */   }
/*     */ 
/*     */   public CloudMonitor.CloudStatus getCloudStatus()
/*     */   {
/* 186 */     switch ($SWITCH_TABLE$metapiga$cloud$CloudMonitor$JobStatus()[this.jobStatus.ordinal()]) {
/*     */     case 1:
/*     */     case 2:
/* 189 */       return CloudMonitor.CloudStatus.PENDING;
/*     */     case 3:
/*     */     case 4:
/* 192 */       return this.client.getJobStatus(this.jobId);
/*     */     case 5:
/* 194 */       return CloudMonitor.CloudStatus.CANCELLED;
/*     */     }
/* 196 */     return CloudMonitor.CloudStatus.FAILED;
/*     */   }
/*     */ 
/*     */   public CloudMonitor.JobStatus getMonitorStatus()
/*     */   {
/* 202 */     return this.jobStatus;
/*     */   }
/*     */ 
/*     */   public void initialize(CloudConnection cloud, CountDownLatch latch)
/*     */   {
/* 207 */     this.cloud = cloud;
/* 208 */     this.applicationId = cloud.getApplicationId();
/* 209 */     this.client = cloud.getClient();
/* 210 */     this.latch = latch;
/* 211 */     if (this.jobStatus == CloudMonitor.JobStatus.NOT_INITIALIZED) this.jobStatus = CloudMonitor.JobStatus.POOLED;
/*     */   }
/*     */ 
/*     */   public boolean isTerminated()
/*     */   {
/* 218 */     return (this.client == null) || (this.client.getJobStatus(this.jobId) == CloudMonitor.CloudStatus.FINISHED) || 
/* 217 */       (this.client.getJobStatus(this.jobId) == CloudMonitor.CloudStatus.FAILED) || 
/* 218 */       (this.client.getJobStatus(this.jobId) == CloudMonitor.CloudStatus.CANCELLED);
/*     */   }
/*     */ 
/*     */   public void run()
/*     */   {
/* 223 */     if (this.jobStatus == CloudMonitor.JobStatus.POOLED) {
/* 224 */       if (this.client == null)
/* 225 */         showText("Cloud client was unavailable when creating replicate " + 
/* 226 */           this.replicateNumber + ", operation aborted.");
/*     */       else {
/*     */         try {
/* 229 */           String outputFile = this.client.getUniqueId();
/* 230 */           this.jobId = this.client.addJob("MetaPIGA_replicate_" + this.replicateNumber, "MetapigaApplication", 
/* 231 */             outputFile, this.cloud.getStorageConnectionString(), "");
/* 232 */           if (this.jobId == null) {
/* 233 */             showText("Error when creating job on the CLOUD for replicate  " + 
/* 234 */               this.replicateNumber);
/* 235 */             this.jobStatus = CloudMonitor.JobStatus.CANCELLED;
/* 236 */           } else if (this.jobStatus == CloudMonitor.JobStatus.CANCELLED) {
/* 237 */             new Thread(new Runnable()
/*     */             {
/*     */               public void run()
/*     */               {
/*     */                 try {
/* 242 */                   SearchGraphicalCloud.this.client.killJob(SearchGraphicalCloud.this.jobId, SearchGraphicalCloud.this.applicationId);
/*     */                 } catch (Exception e) {
/* 244 */                   SearchGraphicalCloud.this.showText("Problem when stopping replicate " + 
/* 245 */                     SearchGraphicalCloud.this.replicateNumber + " : " + Tools.getErrorMessage(e));
/*     */                 }
/*     */               }
/*     */             }).start();
/* 249 */             showStatus(CloudMonitor.CloudStatus.CANCELLED);
/*     */           } else {
/* 251 */             this.jobStatus = CloudMonitor.JobStatus.RUNNING;
/* 252 */             setToolTipText("job id: " + this.jobId);
/* 253 */             CloudMonitor.CloudStatus status = getCloudStatus();
/* 254 */             showStatus(status);
/* 255 */             while ((status != CloudMonitor.CloudStatus.FINISHED) && (status != CloudMonitor.CloudStatus.CANCELLED) && 
/* 256 */               (this.jobStatus == CloudMonitor.JobStatus.RUNNING) && (status != CloudMonitor.CloudStatus.UNKNOWN)) {
/*     */               try {
/* 258 */                 Thread.sleep(500L);
/*     */               } catch (Exception e) {
/* 260 */                 e.printStackTrace();
/*     */               }
/* 262 */               status = getCloudStatus();
/* 263 */               nextAnimation();
/* 264 */               showStatus(status);
/* 265 */               if (status == CloudMonitor.CloudStatus.UNKNOWN) {
/* 266 */                 this.jobStatus = CloudMonitor.JobStatus.CANCELLED;
/* 267 */                 showStatus(CloudMonitor.CloudStatus.FAILED);
/*     */               }
/*     */             }
/* 270 */             if (this.jobStatus == CloudMonitor.JobStatus.RUNNING) {
/* 271 */               this.jobStatus = CloudMonitor.JobStatus.TERMINATED;
/* 272 */               if (status == CloudMonitor.CloudStatus.FINISHED) {
/* 273 */                 resultFile = "results" + outputFile + ".nex";
/* 274 */                 logFile = "logfile" + outputFile;
/* 275 */                 this.client.getJobResult(resultFile, this.cloud.getDataContainerName(), this.cloud.getStorageConnectionString());
/* 276 */                 this.client.getJobLogFile(logFile, this.cloud.getDataContainerName(), this.cloud.getStorageConnectionString());
/* 277 */                 outputDir = new File(this.searchDir + "/" + "Replicate " + this.replicateNumber);
/* 278 */                 outputDir.mkdirs();
/* 279 */                 boolean resultFileExists = cutPasteResultFile(resultFile, outputDir.getAbsolutePath(), resultFile, true);
/* 280 */                 cutPasteResultFile(logFile, outputDir.getAbsolutePath(), logFile, false);
/*     */ 
/* 282 */                 if (resultFileExists)
/*     */                 {
/* 284 */                   File nexus = new File(outputDir + "/" + this.parameters.cloudOutput);
/* 285 */                   if (nexus.exists()) {
/* 286 */                     MyNexusFileBuilder builder = new MyNexusFileBuilder();
/* 287 */                     NexusFileFormat.parseFile(builder, nexus);
/* 288 */                     NexusFile file = builder.getNexusFile();
/* 289 */                     List solutionTrees = new ArrayList();
/* 290 */                     for (Iterator it = file.blockIterator(); it.hasNext(); ) {
/* 291 */                       NexusBlock block = (NexusBlock)it.next();
/* 292 */                       if (block.getBlockName().equals("TREES")) {
/* 293 */                         TreesBlock tb = (TreesBlock)block;
/* 294 */                         int treeNum = 0;
/* 295 */                         for (Iterator tr = tb.getTrees().entrySet().iterator(); tr.hasNext(); ) {
/* 296 */                           Entry e = (Entry)tr.next();
/* 297 */                           NewickReader nr = new NewickReader(this.parameters, (String)e.getKey(), ((TreesBlock.NewickTreeString)e.getValue()).getTreeString(), tb.getTranslations());
/* 298 */                           Tree tree = nr.parseNewick();
/* 299 */                           NexusComment nc = (NexusComment)tb.getComments().get(++treeNum);
/* 300 */                           for (Iterator sub = nc.commentIterator(); sub.hasNext(); ) {
/* 301 */                             String comment = sub.next().toString();
/* 302 */                             if (comment.startsWith("Likelihood")) {
/* 303 */                               tree.parseEvaluationString(comment);
/*     */                             }
/*     */                           }
/* 306 */                           if (this.parameters.hasManyReplicates()) {
/* 307 */                             tree.setName(tree.getName() + "_Rep_" + this.replicateNumber);
/*     */                           }
/* 309 */                           solutionTrees.add(tree);
/*     */                         }
/*     */                       }
/*     */                     }
/* 313 */                     switch ($SWITCH_TABLE$metapiga$monitors$Monitor$MonitorType()[this.parentType.ordinal()]) {
/*     */                     case 2:
/* 315 */                       this.searchOnceGraphical.addSolutionTree(solutionTrees);
/* 316 */                       break;
/*     */                     case 3:
/* 318 */                       this.searchBatchGraphical.addSolutionTree(solutionTrees);
/* 319 */                       break;
/*     */                     default:
/* 321 */                       break;
/*     */                     }
/*     */                   } else {
/* 324 */                     showText("Replicate " + this.replicateNumber + " results cannot be retrieved.");
/* 325 */                     this.jobStatus = CloudMonitor.JobStatus.TERMINATED;
/* 326 */                     showStatus(CloudMonitor.CloudStatus.FAILED);
/*     */                   }
/*     */                 } else {
/* 329 */                   showText("Replicate " + this.replicateNumber + " results cannot be retrieved.");
/* 330 */                   this.jobStatus = CloudMonitor.JobStatus.TERMINATED;
/* 331 */                   showStatus(CloudMonitor.CloudStatus.FAILED);
/*     */                 }
/*     */               } else {
/* 334 */                 showText("Job " + this.jobId + " was killed and replicate " + 
/* 335 */                   this.replicateNumber + " was not complited");
/* 336 */                 this.jobStatus = CloudMonitor.JobStatus.TERMINATED;
/*     */               }
/*     */             }
/*     */           }
/*     */         }
/*     */         catch (Exception e)
/*     */         {
/* 342 */           File outputDir;
/* 341 */           showText("\n Java exception : " + e.getCause() + " (" + e.getMessage() + ")");
/* 342 */           String logFile = (outputDir = e.getStackTrace()).length; for (String resultFile = 0; resultFile < logFile; resultFile++) { StackTraceElement el = outputDir[resultFile];
/* 343 */             showText("\tat " + el.toString());
/*     */           }
/* 345 */           showText("Replicate " + this.replicateNumber + " is probably not completed.");
/* 346 */           this.jobStatus = CloudMonitor.JobStatus.TERMINATED;
/* 347 */           showStatus(CloudMonitor.CloudStatus.FAILED);
/*     */         }
/*     */       }
/*     */     }
/* 351 */     this.animatedLabel.setText(this.animations[0]);
/* 352 */     this.latch.countDown();
/*     */   }
/*     */ 
/*     */   public void showStatus(CloudMonitor.CloudStatus status)
/*     */   {
/* 357 */     final CloudMonitor.CloudStatus s = status;
/* 358 */     SwingUtilities.invokeLater(new Runnable() {
/*     */       public void run() {
/* 360 */         SearchGraphicalCloud.this.coloredSquare.setBackground(s.getColor());
/* 361 */         SearchGraphicalCloud.this.coloredSquare.setToolTipText(s.getTooltipText());
/* 362 */         SearchGraphicalCloud.this.statusLabel.setText(s.toString());
/* 363 */         switch ($SWITCH_TABLE$metapiga$monitors$Monitor$MonitorType()[SearchGraphicalCloud.this.parentType.ordinal()])
/*     */         {
/*     */         case 2:
/* 366 */           SearchGraphicalCloud.this.searchOnceGraphical.cloudStatGraphical.updateStatus(SearchGraphicalCloud.this.replicateNumber, s);
/* 367 */           break;
/*     */         case 3:
/* 370 */           SearchGraphicalCloud.this.searchBatchGraphical.cloudStatGraphical.updateStatus(SearchGraphicalCloud.this.replicateNumber, s);
/* 371 */           break;
/*     */         }
/*     */       }
/*     */     });
/*     */   }
/*     */ 
/*     */   public void stop()
/*     */   {
/* 382 */     switch ($SWITCH_TABLE$metapiga$cloud$CloudMonitor$JobStatus()[this.jobStatus.ordinal()]) {
/*     */     case 1:
/* 384 */       this.jobStatus = CloudMonitor.JobStatus.CANCELLED;
/* 385 */       showStatus(CloudMonitor.CloudStatus.CANCELLED);
/* 386 */       break;
/*     */     case 2:
/* 388 */       this.jobStatus = CloudMonitor.JobStatus.CANCELLED;
/* 389 */       break;
/*     */     case 3:
/* 391 */       new Thread(new Runnable() {
/*     */         public void run() {
/*     */           try {
/* 394 */             SearchGraphicalCloud.this.client.killJob(SearchGraphicalCloud.this.jobId, SearchGraphicalCloud.this.applicationId);
/* 395 */             SearchGraphicalCloud.this.jobStatus = CloudMonitor.JobStatus.CANCELLED;
/*     */           } catch (Exception e) {
/* 397 */             e.printStackTrace();
/*     */           }
/*     */         }
/*     */       }).start();
/* 402 */       break;
/*     */     case 4:
/*     */     case 5:
/*     */     }
/*     */   }
/*     */ 
/*     */   public void updateConsensusTree(Tree consensusTree)
/*     */   {
/*     */     try
/*     */     {
/* 415 */       String endl = "\n";
/* 416 */       File output = new File(this.searchDir);
/* 417 */       if (!output.exists()) output.mkdir();
/* 418 */       output = new File(this.searchDir + "/" + "ConsensusTree.tre");
/* 419 */       fw = new FileWriter(output);
/* 420 */       fw.write("#NEXUS" + endl);
/* 421 */       fw.write(endl);
/* 422 */       fw.write("Begin trees;  [Treefile created " + new Date(System.currentTimeMillis()).toString() + "]" + endl);
/* 423 */       fw.write(consensusTree.toNewickLine(false, true) + endl);
/* 424 */       fw.write("End;" + endl);
/* 425 */       fw.close();
/*     */     } catch (Exception e) {
/* 427 */       e.printStackTrace();
/* 428 */       showText("\n Error when writing file ConsensusTree.tre");
/* 429 */       showText("\n Java exception : " + e.getCause() + " (" + e.getMessage() + ")");
/*     */       StackTraceElement[] arrayOfStackTraceElement;
/* 430 */       FileWriter localFileWriter1 = (arrayOfStackTraceElement = e.getStackTrace()).length; for (FileWriter fw = 0; fw < localFileWriter1; fw++) { StackTraceElement el = arrayOfStackTraceElement[fw];
/* 431 */         showText("\tat " + el.toString());
/*     */       }
/*     */     }
/*     */   }
/*     */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.cloud.SearchGraphicalCloud
 * JD-Core Version:    0.6.2
 */