/*     */ package metapiga.grid;
/*     */ 
/*     */ import java.io.File;
/*     */ import java.io.FileWriter;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Date;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */
/*     */ import java.util.Map.Entry;
/*     */
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
/*     */ import xwchclientapi.XWCHClient.fileref;
/*     */ 
/*     */ public class SearchConsoleGrid
/*     */   implements GridMonitor
/*     */ {
/*     */   private GridMonitor.JobStatus jobStatus;
/*     */   private SearchConsole searchConsole;
/*     */   private SearchSilent searchSilent;
/*     */   private final Monitor.MonitorType parentType;
/*     */   private final String searchDir;
/*     */   private final Parameters parameters;
/*     */   private final int replicateNumber;
/*     */   private GridConnection grid;
/*     */   private String jobId;
/*     */   private String applicationId;
/*     */   private GridClient client;
/*     */   private CountDownLatch latch;
/*     */ 
/*     */   public SearchConsoleGrid(SearchConsole parent, Parameters parameters, int replicateNumber)
/*     */   {
/*  56 */     this.searchConsole = parent;
/*  57 */     this.parentType = Monitor.MonitorType.CONSOLE;
/*  58 */     this.searchDir = parent.dirPath;
/*  59 */     this.parameters = parameters;
/*  60 */     this.replicateNumber = replicateNumber;
/*  61 */     this.jobStatus = GridMonitor.JobStatus.NOT_INITIALIZED;
/*     */   }
/*     */ 
/*     */   public SearchConsoleGrid(SearchSilent parent, Parameters parameters, int replicateNumber) {
/*  65 */     this.searchSilent = parent;
/*  66 */     this.parentType = Monitor.MonitorType.SILENT;
/*  67 */     this.searchDir = parent.dirPath;
/*  68 */     this.parameters = parameters;
/*  69 */     this.replicateNumber = replicateNumber;
/*  70 */     this.jobStatus = GridMonitor.JobStatus.NOT_INITIALIZED;
/*     */   }
/*     */ 
/*     */   private void showText(String text) {
/*  74 */     switch ($SWITCH_TABLE$metapiga$monitors$Monitor$MonitorType()[this.parentType.ordinal()]) {
/*     */     case 4:
/*  76 */       this.searchConsole.showText(text);
/*  77 */       break;
/*     */     case 5:
/*  79 */       break;
/*     */     }
/*     */   }
/*     */ 
/*     */   public void initialize(GridConnection grid, CountDownLatch latch)
/*     */   {
/*  87 */     this.grid = grid;
/*  88 */     this.applicationId = grid.getApplicationId();
/*  89 */     this.client = grid.getClient();
/*  90 */     this.latch = latch;
/*  91 */     if (this.jobStatus == GridMonitor.JobStatus.NOT_INITIALIZED) this.jobStatus = GridMonitor.JobStatus.POOLED;
/*     */   }
/*     */ 
/*     */   public void run()
/*     */   {
/*  97 */     if (this.jobStatus == GridMonitor.JobStatus.POOLED) {
/*  98 */       if (this.client == null)
/*  99 */         showText("GRID client was unavailable when creating replicate " + this.replicateNumber + ", operation aborded.");
/*     */       else {
/*     */         try {
/* 102 */           String outputFile = this.client.getUniqueId();
/* 103 */           this.jobId = this.client.addJob("MetaPIGA_replicate_" + this.replicateNumber, this.applicationId, this.parameters.gridModule, 
/* 104 */             this.grid.getCommandLine(), this.grid.getInputReference().toJobReference(), 
/* 105 */             this.grid.getOutputName() + ";metapigagrid.log", 
/* 106 */             outputFile, "replication;5;freememory;" + this.grid.getXmxValue());
/* 107 */           if (this.jobId == null) {
/* 108 */             showText("Error when creating job on GRID for replicate " + this.replicateNumber);
/* 109 */             this.jobStatus = GridMonitor.JobStatus.CANCELLED;
/* 110 */           } else if (this.jobStatus == GridMonitor.JobStatus.CANCELLED) {
/* 111 */             new Thread(new Runnable() {
/*     */               public void run() {
/*     */                 try {
/* 114 */                   SearchConsoleGrid.this.client.killJob(SearchConsoleGrid.this.jobId, SearchConsoleGrid.this.applicationId);
/*     */                 } catch (Exception e) {
/* 116 */                   SearchConsoleGrid.this.showText("Problem when stopping replicate " + SearchConsoleGrid.this.replicateNumber + " : " + Tools.getErrorMessage(e));
/*     */                 }
/*     */               }
/*     */             }).start();
/* 120 */             showStatus(GridMonitor.GridStatus.KILLED);
/*     */           } else {
/* 122 */             this.jobStatus = GridMonitor.JobStatus.RUNNING;
/* 123 */             GridMonitor.GridStatus status = getGridStatus();
/* 124 */             showStatus(status);
/* 125 */             while ((status != GridMonitor.GridStatus.COMPLETE) && (status != GridMonitor.GridStatus.KILLED) && (this.jobStatus == GridMonitor.JobStatus.RUNNING)) {
/*     */               try {
/* 127 */                 Thread.sleep(500L);
/*     */               } catch (InterruptedException ex) {
/* 129 */                 ex.printStackTrace();
/*     */               }
/* 131 */               status = getGridStatus();
/* 132 */               showStatus(status);
/*     */             }
/* 134 */             if (this.jobStatus == GridMonitor.JobStatus.RUNNING) {
/* 135 */               this.jobStatus = GridMonitor.JobStatus.TERMINATED;
/* 136 */               if (status == GridMonitor.GridStatus.COMPLETE) {
/* 137 */                 this.client.getJobResult(this.jobId, outputFile);
/*     */ 
/* 139 */                 outputDir = new File(this.searchDir + "/" + "Replicate " + this.replicateNumber);
/* 140 */                 zipFile = new File(this.searchDir + "/" + outputFile);
/* 141 */                 if (zipFile.exists()) {
/* 142 */                   Tools.decompress(zipFile, outputDir, true);
/* 143 */                   nexus = new File(outputDir + "/" + this.parameters.cloudOutput);
/* 144 */                   if (nexus.exists()) {
/* 145 */                     MyNexusFileBuilder builder = new MyNexusFileBuilder();
/* 146 */                     NexusFileFormat.parseFile(builder, nexus);
/* 147 */                     NexusFile file = builder.getNexusFile();
/* 148 */                     List solutionTrees = new ArrayList();
/* 149 */                     for (Iterator it = file.blockIterator(); it.hasNext(); ) {
/* 150 */                       NexusBlock block = (NexusBlock)it.next();
/* 151 */                       if (block.getBlockName().equals("TREES")) {
/* 152 */                         TreesBlock tb = (TreesBlock)block;
/* 153 */                         int treeNum = 0;
/* 154 */                         for (Iterator tr = tb.getTrees().entrySet().iterator(); tr.hasNext(); ) {
/* 155 */                           Entry e = (Entry)tr.next();
/* 156 */                           NewickReader nr = new NewickReader(this.parameters, (String)e.getKey(), ((TreesBlock.NewickTreeString)e.getValue()).getTreeString(), tb.getTranslations());
/* 157 */                           Tree tree = nr.parseNewick();
/* 158 */                           NexusComment nc = (NexusComment)tb.getComments().get(++treeNum);
/* 159 */                           for (Iterator sub = nc.commentIterator(); sub.hasNext(); ) {
/* 160 */                             String comment = sub.next().toString();
/* 161 */                             if (comment.startsWith("Likelihood")) {
/* 162 */                               tree.parseEvaluationString(comment);
/*     */                             }
/*     */                           }
/* 165 */                           if (this.parameters.hasManyReplicates()) {
/* 166 */                             tree.setName(tree.getName() + "_Rep_" + this.replicateNumber);
/*     */                           }
/* 168 */                           solutionTrees.add(tree);
/*     */                         }
/*     */                       }
/*     */                     }
/* 172 */                     switch ($SWITCH_TABLE$metapiga$monitors$Monitor$MonitorType()[this.parentType.ordinal()]) {
/*     */                     case 4:
/* 174 */                       this.searchConsole.addSolutionTree(solutionTrees);
/* 175 */                       break;
/*     */                     case 5:
/* 177 */                       this.searchSilent.addSolutionTree(solutionTrees);
/* 178 */                       break;
/*     */                     default:
/* 180 */                       break;
/*     */                     }
/*     */                   } else {
/* 183 */                     showText("Job " + this.jobId + " was killed and replicate " + this.replicateNumber + " was not completed.");
/* 184 */                     this.jobStatus = GridMonitor.JobStatus.TERMINATED;
/*     */                   }
/*     */                 } else {
/* 187 */                   showText("Replicate " + this.replicateNumber + " results cannot be retrieved.");
/* 188 */                   this.jobStatus = GridMonitor.JobStatus.TERMINATED;
/* 189 */                   showStatus(GridMonitor.GridStatus.ERROR);
/*     */                 }
/*     */               } else {
/* 192 */                 showText("Replicate " + this.replicateNumber + " results cannot be retrieved.");
/* 193 */                 this.jobStatus = GridMonitor.JobStatus.TERMINATED;
/* 194 */                 showStatus(GridMonitor.GridStatus.ERROR);
/*     */               }
/*     */             }
/*     */           }
/*     */         }
/*     */         catch (Exception e)
/*     */         {
/* 200 */           File nexus;
/* 199 */           showText("\n Java exception : " + e.getCause() + " (" + e.getMessage() + ")");
/* 200 */           File zipFile = (nexus = e.getStackTrace()).length; for (File outputDir = 0; outputDir < zipFile; outputDir++) { StackTraceElement el = nexus[outputDir];
/* 201 */             showText("\tat " + el.toString());
/*     */           }
/* 203 */           showText("Replicate " + this.replicateNumber + " is probably not completed.");
/* 204 */           this.jobStatus = GridMonitor.JobStatus.TERMINATED;
/*     */         }
/*     */       }
/*     */     }
/* 208 */     this.latch.countDown();
/*     */   }
/*     */ 
/*     */   public GridMonitor.JobStatus getMonitorStatus()
/*     */   {
/* 214 */     return this.jobStatus;
/*     */   }
/*     */ 
/*     */   public void stop()
/*     */   {
/* 219 */     switch ($SWITCH_TABLE$metapiga$grid$GridMonitor$JobStatus()[this.jobStatus.ordinal()]) {
/*     */     case 1:
/* 221 */       this.jobStatus = GridMonitor.JobStatus.CANCELLED;
/* 222 */       showStatus(GridMonitor.GridStatus.KILLED);
/* 223 */       break;
/*     */     case 2:
/* 225 */       this.jobStatus = GridMonitor.JobStatus.CANCELLED;
/* 226 */       break;
/*     */     case 3:
/* 228 */       new Thread(new Runnable() {
/*     */         public void run() {
/*     */           try {
/* 231 */             SearchConsoleGrid.this.client.killJob(SearchConsoleGrid.this.jobId, SearchConsoleGrid.this.applicationId);
/* 232 */             SearchConsoleGrid.this.jobStatus = GridMonitor.JobStatus.CANCELLED;
/*     */           } catch (Exception e) {
/* 234 */             SearchConsoleGrid.this.showText("Problem when stopping replicate " + SearchConsoleGrid.this.replicateNumber + " : " + Tools.getErrorMessage(e));
/*     */           }
/*     */         }
/*     */       }).start();
/* 238 */       break;
/*     */     case 4:
/*     */     case 5:
/*     */     }
/*     */   }
/*     */ 
/*     */   public GridMonitor.GridMonitorType getGridMonitorType()
/*     */   {
/* 249 */     switch ($SWITCH_TABLE$metapiga$monitors$Monitor$MonitorType()[this.parentType.ordinal()]) {
/*     */     case 4:
/* 251 */       return GridMonitor.GridMonitorType.CONSOLE;
/*     */     case 5:
/*     */     }
/* 254 */     return GridMonitor.GridMonitorType.SILENT;
/*     */   }
/*     */ 
/*     */   public GridMonitor.GridStatus getGridStatus()
/*     */   {
/* 260 */     switch ($SWITCH_TABLE$metapiga$grid$GridMonitor$JobStatus()[this.jobStatus.ordinal()]) {
/*     */     case 1:
/*     */     case 2:
/* 263 */       return GridMonitor.GridStatus.QUEUED;
/*     */     case 3:
/*     */     case 4:
/* 266 */       return this.client.getJobStatus(this.jobId);
/*     */     case 5:
/* 268 */       return GridMonitor.GridStatus.KILLED;
/*     */     }
/* 270 */     return GridMonitor.GridStatus.ERROR;
/*     */   }
/*     */ 
/*     */   public boolean isTerminated()
/*     */   {
/* 278 */     return (this.client.getJobStatus(this.jobId) == GridMonitor.GridStatus.COMPLETE) || 
/* 277 */       (this.client.getJobStatus(this.jobId) == GridMonitor.GridStatus.ERROR) || 
/* 278 */       (this.client.getJobStatus(this.jobId) == GridMonitor.GridStatus.KILLED);
/*     */   }
/*     */ 
/*     */   public void showStatus(GridMonitor.GridStatus status)
/*     */   {
/* 283 */     final GridMonitor.GridStatus s = status;
/* 284 */     SwingUtilities.invokeLater(new Runnable() {
/*     */       public void run() {
/* 286 */         switch ($SWITCH_TABLE$metapiga$monitors$Monitor$MonitorType()[SearchConsoleGrid.this.parentType.ordinal()]) {
/*     */         case 4:
/* 288 */           SearchConsoleGrid.this.searchConsole.gridStatConsole.updateStatus(SearchConsoleGrid.this.replicateNumber, s);
/* 289 */           break;
/*     */         }
/*     */       }
/*     */     });
/*     */   }
/*     */ 
/*     */   public void updateConsensusTree(Tree consensusTree)
/*     */   {
/*     */     try
/*     */     {
/* 300 */       String endl = "\n";
/* 301 */       File output = new File(this.searchDir);
/* 302 */       if (!output.exists()) output.mkdir();
/* 303 */       output = new File(this.searchDir + "/" + "ConsensusTree.tre");
/* 304 */       fw = new FileWriter(output);
/* 305 */       fw.write("#NEXUS" + endl);
/* 306 */       fw.write(endl);
/* 307 */       fw.write("Begin trees;  [Treefile created " + new Date(System.currentTimeMillis()).toString() + "]" + endl);
/* 308 */       fw.write(consensusTree.toNewickLine(false, true) + endl);
/* 309 */       fw.write("End;" + endl);
/* 310 */       fw.close();
/*     */     } catch (Exception e) {
/* 312 */       e.printStackTrace();
/* 313 */       showText("\n Error when writing file ConsensusTree.tre");
/* 314 */       showText("\n Java exception : " + e.getCause() + " (" + e.getMessage() + ")");
/*     */       StackTraceElement[] arrayOfStackTraceElement;
/* 315 */       FileWriter localFileWriter1 = (arrayOfStackTraceElement = e.getStackTrace()).length; for (FileWriter fw = 0; fw < localFileWriter1; fw++) { StackTraceElement el = arrayOfStackTraceElement[fw];
/* 316 */         showText("\tat " + el.toString());
/*     */       }
/*     */     }
/*     */   }
/*     */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.SearchConsoleGrid
 * JD-Core Version:    0.6.2
 */