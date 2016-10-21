/*     */ package grid;
/*     */ 
/*     */ import java.awt.Color;
/*     */ import java.awt.Dimension;
/*     */ import java.awt.FlowLayout;
/*     */ import java.awt.Font;
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
/*     */ import javax.swing.BorderFactory;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JLabel;
/*     */ import javax.swing.JPanel;
/*     */ import javax.swing.SwingUtilities;
/*     */ import javax.swing.border.TitledBorder;
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
/*     */ import xwchclientapi.XWCHClient.fileref;
/*     */ 
/*     */ public class SearchGraphicalGrid extends JPanel
/*     */   implements GridMonitor
/*     */ {
/*     */   private GridMonitor.JobStatus jobStatus;
/*     */   private SearchOnceGraphical searchOnceGraphical;
/*     */   private SearchBatchGraphical searchBatchGraphical;
/*     */   private final Monitor.MonitorType parentType;
/*     */   private final String searchDir;
/*     */   private final Parameters parameters;
/*     */   private final int replicateNumber;
/*     */   private GridConnection grid;
/*     */   private String jobId;
/*     */   private String applicationId;
/*     */   private GridClient client;
/*     */   private CountDownLatch latch;
/*  63 */   private String[] animations = { "—", "\\", "|", "/" };
/*  64 */   private int animationIndex = 0;
/*  65 */   private final JLabel statusLabel = new JLabel();
/*  66 */   private final JButton coloredSquare = new JButton();
/*  67 */   private final JLabel animatedLabel = new JLabel();
/*     */ 
/*     */   public SearchGraphicalGrid(SearchOnceGraphical parent, Parameters parameters, int replicateNumber) {
/*  70 */     this.searchOnceGraphical = parent;
/*  71 */     this.parentType = Monitor.MonitorType.SINGLE_SEARCH_GRAPHICAL;
/*  72 */     this.searchDir = parent.dirPath;
/*  73 */     this.parameters = parameters;
/*  74 */     this.replicateNumber = replicateNumber;
/*  75 */     this.jobStatus = GridMonitor.JobStatus.NOT_INITIALIZED;
/*     */     try {
/*  77 */       jbInit();
/*     */     }
/*     */     catch (Exception ex) {
/*  80 */       ex.printStackTrace();
/*     */     }
/*     */   }
/*     */ 
/*     */   public SearchGraphicalGrid(SearchBatchGraphical parent, Parameters parameters, int replicateNumber) {
/*  85 */     this.searchBatchGraphical = parent;
/*  86 */     this.parentType = Monitor.MonitorType.BATCH_SEARCH_GRAPHICAL;
/*  87 */     this.searchDir = parent.dirPath;
/*  88 */     this.parameters = parameters;
/*  89 */     this.replicateNumber = replicateNumber;
/*  90 */     this.jobStatus = GridMonitor.JobStatus.NOT_INITIALIZED;
/*     */     try {
/*  92 */       jbInit();
/*     */     }
/*     */     catch (Exception ex) {
/*  95 */       ex.printStackTrace();
/*     */     }
/*     */   }
/*     */ 
/*     */   private void jbInit() {
/* 100 */     setLayout(new FlowLayout(3, 5, 5));
/* 101 */     setBorder(new TitledBorder(BorderFactory.createEtchedBorder(Color.green, Color.black), "Replicate " + this.replicateNumber));
/* 102 */     this.animatedLabel.setFont(new Font("Tahoma", 1, 11));
/* 103 */     this.animatedLabel.setPreferredSize(new Dimension(15, 15));
/* 104 */     this.animatedLabel.setText(this.animations[this.animationIndex]);
/* 105 */     this.coloredSquare.setPreferredSize(new Dimension(15, 15));
/* 106 */     this.coloredSquare.setBackground(GridMonitor.GridStatus.QUEUED.getColor());
/* 107 */     this.coloredSquare.setBorder(BorderFactory.createRaisedBevelBorder());
/* 108 */     this.coloredSquare.setContentAreaFilled(false);
/* 109 */     this.coloredSquare.setOpaque(true);
/* 110 */     this.coloredSquare.setToolTipText(GridMonitor.GridStatus.QUEUED.getTooltipText());
/* 111 */     this.statusLabel.setPreferredSize(new Dimension(80, 15));
/* 112 */     this.statusLabel.setText(GridMonitor.GridStatus.QUEUED.toString());
/* 113 */     add(this.animatedLabel);
/* 114 */     add(this.coloredSquare);
/* 115 */     add(this.statusLabel);
/*     */   }
/*     */ 
/*     */   private void showText(String text) {
/* 119 */     switch ($SWITCH_TABLE$metapiga$monitors$Monitor$MonitorType()[this.parentType.ordinal()]) {
/*     */     case 2:
/* 121 */       this.searchOnceGraphical.showText(text);
/* 122 */       break;
/*     */     case 3:
/* 124 */       this.searchBatchGraphical.showText(text);
/* 125 */       break;
/*     */     }
/*     */   }
/*     */ 
/*     */   private void nextAnimation()
/*     */   {
/* 132 */     if (this.animationIndex == this.animations.length - 1) this.animationIndex = 0; else
/* 133 */       this.animationIndex += 1;
/* 134 */     this.animatedLabel.setText(this.animations[this.animationIndex]);
/*     */   }
/*     */ 
/*     */   public void initialize(GridConnection grid, CountDownLatch latch)
/*     */   {
/* 140 */     this.grid = grid;
/* 141 */     this.applicationId = grid.getApplicationId();
/* 142 */     this.client = grid.getClient();
/* 143 */     this.latch = latch;
/* 144 */     if (this.jobStatus == GridMonitor.JobStatus.NOT_INITIALIZED) this.jobStatus = GridMonitor.JobStatus.POOLED;
/*     */   }
/*     */ 
/*     */   public void run()
/*     */   {
/* 150 */     if (this.jobStatus == GridMonitor.JobStatus.POOLED) {
/* 151 */       if (this.client == null)
/* 152 */         showText("GRID client was unavailable when creating replicate " + this.replicateNumber + ", operation aborded.");
/*     */       else {
/*     */         try {
/* 155 */           String outputFile = this.client.getUniqueId();
/*     */ 
/* 162 */           this.jobId = this.client.addJob("MetaPIGA_replicate_" + this.replicateNumber, this.applicationId, this.parameters.gridModule, 
/* 163 */             this.grid.getCommandLine(), this.grid.getInputReference().toJobReference(), 
/* 164 */             this.grid.getOutputName() + ";metapigagrid.log", 
/* 165 */             outputFile, "replication;5;freememory;" + this.grid.getXmxValue());
/* 166 */           if (this.jobId == null) {
/* 167 */             showText("Error when creating job on GRID for replicate " + this.replicateNumber);
/* 168 */             this.jobStatus = GridMonitor.JobStatus.CANCELLED;
/* 169 */           } else if (this.jobStatus == GridMonitor.JobStatus.CANCELLED) {
/* 170 */             new Thread(new Runnable() {
/*     */               public void run() {
/*     */                 try {
/* 173 */                   SearchGraphicalGrid.this.client.killJob(SearchGraphicalGrid.this.jobId, SearchGraphicalGrid.this.applicationId);
/*     */                 } catch (Exception e) {
/* 175 */                   SearchGraphicalGrid.this.showText("Problem when stopping replicate " + SearchGraphicalGrid.this.replicateNumber + " : " + Tools.getErrorMessage(e));
/*     */                 }
/*     */               }
/*     */             }).start();
/* 179 */             showStatus(GridMonitor.GridStatus.KILLED);
/*     */           } else {
/* 181 */             this.jobStatus = GridMonitor.JobStatus.RUNNING;
/* 182 */             setToolTipText("job id: " + this.jobId);
/* 183 */             GridMonitor.GridStatus status = getGridStatus();
/* 184 */             showStatus(status);
/* 185 */             while ((status != GridMonitor.GridStatus.COMPLETE) && (status != GridMonitor.GridStatus.KILLED) && (this.jobStatus == GridMonitor.JobStatus.RUNNING)) {
/*     */               try {
/* 187 */                 Thread.sleep(500L);
/*     */               } catch (InterruptedException ex) {
/* 189 */                 ex.printStackTrace();
/*     */               }
/* 191 */               status = getGridStatus();
/* 192 */               nextAnimation();
/* 193 */               showStatus(status);
/*     */             }
/* 195 */             if (this.jobStatus == GridMonitor.JobStatus.RUNNING) {
/* 196 */               this.jobStatus = GridMonitor.JobStatus.TERMINATED;
/* 197 */               if (status == GridMonitor.GridStatus.COMPLETE) {
/* 198 */                 this.client.getJobResult(this.jobId, outputFile);
/*     */ 
/* 200 */                 outputDir = new File(this.searchDir + "/" + "Replicate " + this.replicateNumber);
/* 201 */                 outputDir.mkdirs();
/* 202 */                 zipFile = new File(this.searchDir + "/" + outputFile);
/* 203 */                 if (zipFile.exists()) {
/* 204 */                   Tools.decompress(zipFile, outputDir, true);
/* 205 */                   nexus = new File(outputDir + "/" + this.parameters.cloudOutput);
/* 206 */                   if (nexus.exists()) {
/* 207 */                     MyNexusFileBuilder builder = new MyNexusFileBuilder();
/* 208 */                     NexusFileFormat.parseFile(builder, nexus);
/* 209 */                     NexusFile file = builder.getNexusFile();
/* 210 */                     List solutionTrees = new ArrayList();
/* 211 */                     for (Iterator it = file.blockIterator(); it.hasNext(); ) {
/* 212 */                       NexusBlock block = (NexusBlock)it.next();
/* 213 */                       if (block.getBlockName().equals("TREES")) {
/* 214 */                         TreesBlock tb = (TreesBlock)block;
/* 215 */                         int treeNum = 0;
/* 216 */                         for (Iterator tr = tb.getTrees().entrySet().iterator(); tr.hasNext(); ) {
/* 217 */                           Entry e = (Entry)tr.next();
/* 218 */                           NewickReader nr = new NewickReader(this.parameters, (String)e.getKey(), ((TreesBlock.NewickTreeString)e.getValue()).getTreeString(), tb.getTranslations());
/* 219 */                           Tree tree = nr.parseNewick();
/* 220 */                           NexusComment nc = (NexusComment)tb.getComments().get(++treeNum);
/* 221 */                           for (Iterator sub = nc.commentIterator(); sub.hasNext(); ) {
/* 222 */                             String comment = sub.next().toString();
/* 223 */                             if (comment.startsWith("Likelihood")) {
/* 224 */                               tree.parseEvaluationString(comment);
/*     */                             }
/*     */                           }
/* 227 */                           if (this.parameters.hasManyReplicates()) {
/* 228 */                             tree.setName(tree.getName() + "_Rep_" + this.replicateNumber);
/*     */                           }
/* 230 */                           solutionTrees.add(tree);
/*     */                         }
/*     */                       }
/*     */                     }
/* 234 */                     switch ($SWITCH_TABLE$metapiga$monitors$Monitor$MonitorType()[this.parentType.ordinal()]) {
/*     */                     case 2:
/* 236 */                       this.searchOnceGraphical.addSolutionTree(solutionTrees);
/* 237 */                       break;
/*     */                     case 3:
/* 239 */                       this.searchBatchGraphical.addSolutionTree(solutionTrees);
/* 240 */                       break;
/*     */                     default:
/* 242 */                       break;
/*     */                     }
/*     */                   } else {
/* 245 */                     showText("Replicate " + this.replicateNumber + " results cannot be retrieved.");
/* 246 */                     this.jobStatus = GridMonitor.JobStatus.TERMINATED;
/* 247 */                     showStatus(GridMonitor.GridStatus.ERROR);
/*     */                   }
/*     */                 } else {
/* 250 */                   showText("Replicate " + this.replicateNumber + " results cannot be retrieved.");
/* 251 */                   this.jobStatus = GridMonitor.JobStatus.TERMINATED;
/* 252 */                   showStatus(GridMonitor.GridStatus.ERROR);
/*     */                 }
/*     */               } else {
/* 255 */                 showText("Job " + this.jobId + " was killed and replicate " + this.replicateNumber + " was not completed.");
/* 256 */                 this.jobStatus = GridMonitor.JobStatus.TERMINATED;
/*     */               }
/*     */             }
/*     */           }
/*     */         }
/*     */         catch (Exception e)
/*     */         {
/* 262 */           File nexus;
/* 261 */           showText("\n Java exception : " + e.getCause() + " (" + e.getMessage() + ")");
/* 262 */           File zipFile = (nexus = e.getStackTrace()).length; for (File outputDir = 0; outputDir < zipFile; outputDir++) { StackTraceElement el = nexus[outputDir];
/* 263 */             showText("\tat " + el.toString());
/*     */           }
/* 265 */           showText("Replicate " + this.replicateNumber + " is probably not completed.");
/* 266 */           this.jobStatus = GridMonitor.JobStatus.TERMINATED;
/* 267 */           showStatus(GridMonitor.GridStatus.ERROR);
/*     */         }
/*     */       }
/*     */     }
/* 271 */     this.animatedLabel.setText(this.animations[0]);
/* 272 */     this.latch.countDown();
/*     */   }
/*     */ 
/*     */   public GridMonitor.JobStatus getMonitorStatus()
/*     */   {
/* 278 */     return this.jobStatus;
/*     */   }
/*     */ 
/*     */   public void stop()
/*     */   {
/* 283 */     switch ($SWITCH_TABLE$metapiga$grid$GridMonitor$JobStatus()[this.jobStatus.ordinal()]) {
/*     */     case 1:
/* 285 */       this.jobStatus = GridMonitor.JobStatus.CANCELLED;
/* 286 */       showStatus(GridMonitor.GridStatus.KILLED);
/* 287 */       break;
/*     */     case 2:
/* 289 */       this.jobStatus = GridMonitor.JobStatus.CANCELLED;
/* 290 */       break;
/*     */     case 3:
/* 292 */       new Thread(new Runnable() {
/*     */         public void run() {
/*     */           try {
/* 295 */             SearchGraphicalGrid.this.client.killJob(SearchGraphicalGrid.this.jobId, SearchGraphicalGrid.this.applicationId);
/* 296 */             SearchGraphicalGrid.this.jobStatus = GridMonitor.JobStatus.CANCELLED;
/*     */           } catch (Exception e) {
/* 298 */             e.printStackTrace();
/*     */           }
/*     */         }
/*     */       }).start();
/* 303 */       break;
/*     */     case 4:
/*     */     case 5:
/*     */     }
/*     */   }
/*     */ 
/*     */   public GridMonitor.GridMonitorType getGridMonitorType()
/*     */   {
/* 314 */     switch ($SWITCH_TABLE$metapiga$monitors$Monitor$MonitorType()[this.parentType.ordinal()]) {
/*     */     case 2:
/* 316 */       return GridMonitor.GridMonitorType.SINGLE_SEARCH_GRAPHICAL;
/*     */     case 3:
/*     */     }
/* 319 */     return GridMonitor.GridMonitorType.BATCH_SEARCH_GRAPHICAL;
/*     */   }
/*     */ 
/*     */   public GridMonitor.GridStatus getGridStatus()
/*     */   {
/* 325 */     switch ($SWITCH_TABLE$metapiga$grid$GridMonitor$JobStatus()[this.jobStatus.ordinal()]) {
/*     */     case 1:
/*     */     case 2:
/* 328 */       return GridMonitor.GridStatus.QUEUED;
/*     */     case 3:
/*     */     case 4:
/* 331 */       return this.client.getJobStatus(this.jobId);
/*     */     case 5:
/* 333 */       return GridMonitor.GridStatus.KILLED;
/*     */     }
/* 335 */     return GridMonitor.GridStatus.ERROR;
/*     */   }
/*     */ 
/*     */   public boolean isTerminated()
/*     */   {
/* 344 */     return (this.client == null) || 
/* 342 */       (this.client.getJobStatus(this.jobId) == GridMonitor.GridStatus.COMPLETE) || 
/* 343 */       (this.client.getJobStatus(this.jobId) == GridMonitor.GridStatus.ERROR) || 
/* 344 */       (this.client.getJobStatus(this.jobId) == GridMonitor.GridStatus.KILLED);
/*     */   }
/*     */ 
/*     */   public void showStatus(GridMonitor.GridStatus status)
/*     */   {
/* 349 */     final GridMonitor.GridStatus s = status;
/* 350 */     SwingUtilities.invokeLater(new Runnable() {
/*     */       public void run() {
/* 352 */         SearchGraphicalGrid.this.coloredSquare.setBackground(s.getColor());
/* 353 */         SearchGraphicalGrid.this.coloredSquare.setToolTipText(s.getTooltipText());
/* 354 */         SearchGraphicalGrid.this.statusLabel.setText(s.toString());
/* 355 */         switch ($SWITCH_TABLE$metapiga$monitors$Monitor$MonitorType()[SearchGraphicalGrid.this.parentType.ordinal()]) {
/*     */         case 2:
/* 357 */           SearchGraphicalGrid.this.searchOnceGraphical.gridStatGraphical.updateStatus(SearchGraphicalGrid.this.replicateNumber, s);
/* 358 */           break;
/*     */         case 3:
/* 360 */           SearchGraphicalGrid.this.searchBatchGraphical.gridStatGraphical.updateStatus(SearchGraphicalGrid.this.replicateNumber, s);
/* 361 */           break;
/*     */         }
/*     */       }
/*     */     });
/*     */   }
/*     */ 
/*     */   public void updateConsensusTree(Tree consensusTree)
/*     */   {
/*     */     try
/*     */     {
/* 372 */       String endl = "\n";
/* 373 */       File output = new File(this.searchDir);
/* 374 */       if (!output.exists()) output.mkdir();
/* 375 */       output = new File(this.searchDir + "/" + "ConsensusTree.tre");
/* 376 */       fw = new FileWriter(output);
/* 377 */       fw.write("#NEXUS" + endl);
/* 378 */       fw.write(endl);
/* 379 */       fw.write("Begin trees;  [Treefile created " + new Date(System.currentTimeMillis()).toString() + "]" + endl);
/* 380 */       fw.write(consensusTree.toNewickLine(false, true) + endl);
/* 381 */       fw.write("End;" + endl);
/* 382 */       fw.close();
/*     */     } catch (Exception e) {
/* 384 */       e.printStackTrace();
/* 385 */       showText("\n Error when writing file ConsensusTree.tre");
/* 386 */       showText("\n Java exception : " + e.getCause() + " (" + e.getMessage() + ")");
/*     */       StackTraceElement[] arrayOfStackTraceElement;
/* 387 */       FileWriter localFileWriter1 = (arrayOfStackTraceElement = e.getStackTrace()).length; for (FileWriter fw = 0; fw < localFileWriter1; fw++) { StackTraceElement el = arrayOfStackTraceElement[fw];
/* 388 */         showText("\tat " + el.toString());
/*     */       }
/*     */     }
/*     */   }
/*     */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.grid.SearchGraphicalGrid
 * JD-Core Version:    0.6.2
 */