/*     */ package metapiga.monitors;
/*     */ 
/*     */ import java.io.BufferedWriter;
/*     */ import java.io.File;
/*     */ import java.io.FileWriter;
/*     */
/*     */ import java.text.DateFormat;
/*     */ import java.text.SimpleDateFormat;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ import java.util.Vector;
/*     */ import java.util.concurrent.CountDownLatch;
/*     */ import java.util.concurrent.ExecutorService;
/*     */ import java.util.concurrent.Executors;
/*     */ import java.util.concurrent.TimeUnit;
/*     */ import java.util.concurrent.atomic.AtomicInteger;
/*     */ import javax.swing.DefaultListModel;
/*     */ import javax.swing.JOptionPane;
/*     */ import metapiga.MetaPIGA.UI;
/*     */ import metapiga.ProgressHandling;
/*     */ import metapiga.cloud.CloudConnection;
/*     */ import metapiga.cloud.CloudStatConsole;
/*     */ import metapiga.cloud.SearchConsoleCloud;
/*     */ import metapiga.grid.GridConnection;
/*     */
/*     */ import metapiga.grid.GridStatConsole;
/*     */ import metapiga.grid.SearchConsoleGrid;
/*     */
/*     */ import metapiga.parameters.Parameters;
/*     */
/*     */
/*     */
/*     */ import metapiga.trees.Consensus;
/*     */ import metapiga.trees.ConsensusMRE;
/*     */ import metapiga.trees.Tree;
/*     */ import metapiga.utilities.Tools;
/*     */ import org.biojavax.bio.phylo.io.nexus.CharactersBlock;
/*     */ import org.biojavax.bio.phylo.io.nexus.MetapigaBlock;
/*     */ 
/*     */ public class SearchConsole
/*     */   implements Runnable
/*     */ {
/*     */   private ExecutorService executor;
/*     */   private int nJobs;
/*     */   private AtomicInteger replicatesDone;
/*     */   private AtomicInteger replicatesAvailable;
/*     */   private AtomicInteger replicatesTotal;
/*     */   private AtomicInteger countMRE;
/*     */   private DefaultListModel batch;
/*     */   private Parameters currentParameters;
/*  52 */   private List<Tree> allSolutionTrees = new Vector();
/*     */   private String runDirectory;
/*     */   private String runLabel;
/*     */   public String dirPath;
/*     */   ProgressHandling progress;
/*     */   private SearchConsoleMonitor[] monitors;
/*     */   private SearchConsoleGrid[] grids;
/*     */   private CountDownLatch latch;
/*     */   public GridStatConsole gridStatConsole;
/*  61 */   private ConsensusMRE consensusMRE = new ConsensusMRE();
/*  62 */   private GridConnection grid = null;
/*  63 */   private boolean stopGrid = false;
/*     */   private SearchConsoleCloud[] clouds;
/*     */   public CloudStatConsole cloudStatConsole;
/*  66 */   private CloudConnection cloud = null;
/*     */ 
/*     */   public SearchConsole(DefaultListModel parameters) {
/*  69 */     this.batch = parameters;
/*     */   }
/*     */ 
/*     */   public void run()
/*     */   {
/*  74 */     for (int i = 0; i < this.batch.getSize(); i++) {
/*  75 */       this.currentParameters = ((Parameters)this.batch.get(i));
/*  76 */       DateFormat df = new SimpleDateFormat("yyyy-MM-dd - HH_mm_ss");
/*  77 */       this.runDirectory = this.currentParameters.outputDir;
/*  78 */       File dir = new File(this.runDirectory);
/*  79 */       if (!dir.exists()) {
/*  80 */         dir.mkdirs();
/*     */       }
/*  82 */       this.runLabel = (this.currentParameters.label + " - " + df.format(Long.valueOf(System.currentTimeMillis())));
/*  83 */       this.dirPath = (this.runDirectory + "/" + this.runLabel);
/*  84 */       dir = new File(this.dirPath);
/*  85 */       if (!dir.exists()) dir.mkdir();
/*  86 */       switch ($SWITCH_TABLE$metapiga$parameters$Parameters$ReplicatesStopCondition()[this.currentParameters.replicatesStopCondition.ordinal()]) {
/*     */       case 1:
/*  88 */         this.replicatesTotal = new AtomicInteger(this.currentParameters.replicatesNumber);
/*  89 */         break;
/*     */       case 2:
/*  91 */         this.replicatesTotal = new AtomicInteger(this.currentParameters.replicatesMaximum);
/*     */       }
/*     */ 
/*  94 */       this.replicatesDone = new AtomicInteger();
/*  95 */       this.replicatesAvailable = new AtomicInteger();
/*  96 */       this.countMRE = new AtomicInteger();
/*  97 */       if (this.currentParameters.useGrid) {
/*  98 */         this.nJobs = this.replicatesTotal.get();
/*  99 */         this.latch = new CountDownLatch(this.nJobs);
/* 100 */         this.grids = new SearchConsoleGrid[this.nJobs];
/* 101 */         for (int j = 0; j < this.nJobs; j++) {
/* 102 */           this.grids[j] = new SearchConsoleGrid(this, this.currentParameters, j + 1);
/*     */         }
/* 104 */         this.gridStatConsole = new GridStatConsole(this.nJobs);
/* 105 */       } else if (this.currentParameters.useCloud) {
/* 106 */         this.nJobs = this.replicatesTotal.get();
/* 107 */         this.latch = new CountDownLatch(this.nJobs);
/* 108 */         this.clouds = new SearchConsoleCloud[this.nJobs];
/* 109 */         for (int j = 0; j < this.nJobs; j++) {
/* 110 */           this.clouds[j] = new SearchConsoleCloud(this, this.currentParameters, j + 1);
/*     */         }
/* 112 */         this.cloudStatConsole = new CloudStatConsole(this.nJobs);
/*     */       } else {
/* 114 */         this.nJobs = this.currentParameters.replicatesParallel;
/* 115 */         this.latch = null;
/* 116 */         this.monitors = new SearchConsoleMonitor[this.nJobs];
/* 117 */         this.progress = new ProgressHandling(this.nJobs);
/* 118 */         this.progress.setUI(MetaPIGA.UI.CONSOLE);
/* 119 */         for (int j = 0; j < this.nJobs; j++) {
/* 120 */           this.monitors[j] = new SearchConsoleMonitor(this, this.progress, j, this.currentParameters, this.dirPath);
/*     */         }
/*     */       }
/*     */       try
/*     */       {
/* 125 */         long startTime = System.currentTimeMillis();
/* 126 */         showText("\nRunning " + this.runLabel + "\n" + this.currentParameters.printParameters() + "\n");
/* 127 */         this.grid = (this.currentParameters.useGrid ? new GridConnection(this, this.currentParameters, this.dirPath) : null);
/* 128 */         this.executor = Executors.newFixedThreadPool(this.currentParameters.useGrid ? Math.min(this.nJobs, 1000) : this.nJobs);
/* 129 */         for (int k = 0; k < this.nJobs; k++) {
/* 130 */           if (this.currentParameters.useGrid) {
/* 131 */             this.grids[k].initialize(this.grid, this.latch);
/* 132 */             this.executor.execute(this.grids[k]);
/* 133 */             while (this.grids[k].getMonitorStatus() == GridMonitor.JobStatus.POOLED)
/*     */               try {
/* 135 */                 Thread.sleep(200L);
/*     */               } catch (InterruptedException ex) {
/* 137 */                 ex.printStackTrace();
/*     */               }
/*     */           }
/*     */           else {
/* 141 */             this.executor.execute(this.monitors[k]);
/*     */           }
/*     */         }
/* 144 */         if (this.currentParameters.useGrid) this.latch.await();
/* 145 */         this.executor.shutdown();
/* 146 */         this.executor.awaitTermination(1000L, TimeUnit.DAYS);
/* 147 */         Thread.sleep(500L);
/*     */ 
/* 149 */         if (this.currentParameters.useGrid) stopGrid();
/* 150 */         end(startTime);
/*     */       } catch (Exception e) {
/* 152 */         endFromException(e);
/*     */       }
/*     */     }
/* 155 */     showText("Everything is finished !");
/*     */   }
/*     */ 
/*     */   private void end(long startTime) {
/* 159 */     if (this.currentParameters.hasManyReplicates()) showText("\nAll replicates done in " + Tools.doubletoString((System.currentTimeMillis() - startTime) / 60000.0D, 2) + " minutes");
/*     */ 
/* 161 */     Tree consensusTree = null;
/*     */     Tree optimizedConsensusTree;
/* 162 */     if (this.currentParameters.hasManyReplicates()) {
/*     */       try {
/* 164 */         Consensus consensus = new Consensus(this.allSolutionTrees, this.currentParameters.dataset);
/* 165 */         consensusTree = consensus.getConsensusTree(this.currentParameters);
/* 166 */         if (this.currentParameters.optimization == Parameters.Optimization.CONSENSUSTREE) {
/* 167 */           showText("\nOptimizing final consensus tree\n");
/* 168 */           optimizedConsensusTree = this.currentParameters.getOptimizer(consensusTree).getOptimizedTree();
/* 169 */           consensusTree.cloneWithConsensus(optimizedConsensusTree);
/*     */         }
/*     */       } catch (Exception ex) {
/* 172 */         JOptionPane.showMessageDialog(null, Tools.getErrorPanel("Cannot display result tree(s)", ex), "Consensus tree Error", 0);
/* 173 */         System.out.println("Cannot build consensus tree : " + ex.getMessage());
/* 174 */         ex.printStackTrace();
/*     */       }
/*     */     }
/*     */ 
/* 178 */     if (consensusTree != null) consensusTree.setName(this.runLabel + " - " + consensusTree.getName());
/* 179 */     for (Tree t : this.allSolutionTrees) {
/* 180 */       t.setName(this.runLabel + " - " + t.getName());
/*     */     }
/* 182 */     showText("\nJOB DONE\n");
/*     */ 
/* 191 */     File output = new File(this.dirPath + "/" + "Results.nex");
/*     */     try {
/* 193 */       FileWriter fw = new FileWriter(output);
/* 194 */       BufferedWriter bw = new BufferedWriter(fw);
/* 195 */       bw.write("#NEXUS");
/* 196 */       bw.newLine();
/* 197 */       bw.newLine();
/* 198 */       this.currentParameters.getMetapigaBlock().writeObject(bw);
/* 199 */       bw.newLine();
/* 200 */       this.currentParameters.charactersBlock.writeObject(bw);
/* 201 */       bw.newLine();
/* 202 */       if (this.currentParameters.startingTreeGeneration == Parameters.StartingTreeGeneration.GIVEN) {
/* 203 */         this.currentParameters.writeTreeBlock(bw);
/* 204 */         bw.newLine();
/*     */       }
/* 206 */       bw.write("Begin trees;  [Result trees]");
/* 207 */       bw.newLine();
/* 208 */       if (consensusTree != null) {
/* 209 */         bw.write(consensusTree.toNewickLineWithML(consensusTree.getName(), false, true));
/* 210 */         bw.newLine();
/*     */       }
/* 212 */       for (Iterator localIterator = this.allSolutionTrees.iterator(); localIterator.hasNext(); ) { t = (Tree)localIterator.next();
/* 213 */         bw.write(t.toNewickLineWithML(t.getName(), false, true));
/* 214 */         bw.newLine();
/*     */       }
/* 216 */       bw.write("End;");
/* 217 */       bw.newLine();
/* 218 */       bw.close();
/* 219 */       fw.close();
/*     */     } catch (Exception e) {
/* 221 */       e.printStackTrace();
/* 222 */       showText("\n Error when writing results file");
/* 223 */       showText("\n Java exception : " + e.getCause() + " (" + e.getMessage() + ")");
/*     */       StackTraceElement[] arrayOfStackTraceElement;
/* 224 */       Tree localTree1 = (arrayOfStackTraceElement = e.getStackTrace()).length; for (Tree t = 0; t < localTree1; t++) { StackTraceElement el = arrayOfStackTraceElement[t];
/* 225 */         showText("\tat " + el.toString());
/*     */       }
/*     */     }
/* 228 */     System.gc();
/*     */   }
/*     */ 
/*     */   public void addSolutionTree(List<Tree> trees) {
/* 232 */     this.allSolutionTrees.addAll(trees);
/* 233 */     showReplicate();
/*     */   }
/*     */ 
/*     */   public void endFromException(Exception e) {
/* 237 */     e.printStackTrace();
/* 238 */     showText("\n Java exception : " + e.getCause() + " (" + e.getMessage() + ")");
/* 239 */     for (StackTraceElement el : e.getStackTrace())
/* 240 */       showText("\tat " + el.toString());
/*     */   }
/*     */ 
/*     */   public void showText(String text)
/*     */   {
/* 245 */     System.out.println(text);
/*     */   }
/*     */ 
/*     */   public int getNextReplicate() {
/* 249 */     if (this.replicatesAvailable.get() < this.replicatesTotal.get()) {
/* 250 */       return this.replicatesAvailable.incrementAndGet();
/*     */     }
/* 252 */     return -1;
/*     */   }
/*     */ 
/*     */   public void showReplicate() {
/* 256 */     if (this.replicatesDone.incrementAndGet() > 1)
/*     */       try {
/* 258 */         Consensus consensus = new Consensus(this.allSolutionTrees, this.currentParameters.dataset);
/* 259 */         synchronized (this.consensusMRE) {
/* 260 */           Tree consensusTree = consensus.getConsensusTree(this.currentParameters);
/* 261 */           consensusTree.setName("Consensus_tree_" + this.replicatesDone + "_replicates");
/* 262 */           this.consensusMRE.addConsensus(consensusTree, this.currentParameters, false);
/* 263 */           double mre = this.consensusMRE.meanRelativeError();
/* 264 */           consensusTree.setName("Consensus_tree_" + this.replicatesDone + "_replicates" + " [MRE: " + Tools.doubleToPercent(mre, 2) + "]");
/* 265 */           if ((this.replicatesDone.get() > this.currentParameters.replicatesMinimum) && (mre < this.currentParameters.replicatesMRE)) {
/* 266 */             this.countMRE.incrementAndGet();
/* 267 */             if ((this.currentParameters.replicatesStopCondition == Parameters.ReplicatesStopCondition.MRE) && (this.countMRE.get() >= this.currentParameters.replicatesInterval)) {
/* 268 */               this.replicatesAvailable = this.replicatesTotal;
/* 269 */               if (this.currentParameters.useGrid) {
/* 270 */                 stopGrid();
/*     */               }
/* 272 */               showText("MRE condition has been met, all remaining replicates have been cancelled.");
/*     */             }
/*     */           } else { this.countMRE.set(0);
/* 275 */             this.consensusMRE.addConsensus(consensusTree, this.currentParameters, true);
/*     */           }
/* 277 */           if (this.currentParameters.useGrid) this.grids[0].updateConsensusTree(consensusTree); else
/* 278 */             this.monitors[0].updateConsensusTree(consensusTree);
/*     */         }
/*     */       } catch (Exception e) {
/* 281 */         e.printStackTrace();
/*     */       }
/*     */   }
/*     */ 
/*     */   public void stopGrid()
/*     */   {
/* 287 */     if (!this.stopGrid) {
/* 288 */       this.stopGrid = true;
/* 289 */       new Thread(new Runnable() {
/*     */         public void run() {
/* 291 */           for (int i = 0; i < SearchConsole.this.nJobs; i++) {
/* 292 */             if (SearchConsole.this.grids[i].getMonitorStatus() == GridMonitor.JobStatus.NOT_INITIALIZED) SearchConsole.this.grids[i].stop();
/*     */           }
/* 294 */           for (int i = 0; i < SearchConsole.this.nJobs; i++) {
/* 295 */             if (SearchConsole.this.grids[i].getMonitorStatus() == GridMonitor.JobStatus.POOLED) SearchConsole.this.grids[i].stop();
/*     */           }
/* 297 */           for (int i = 0; i < SearchConsole.this.nJobs; i++) {
/* 298 */             if ((SearchConsole.this.grids[i].getMonitorStatus() != GridMonitor.JobStatus.NOT_INITIALIZED) && (SearchConsole.this.grids[i].getMonitorStatus() != GridMonitor.JobStatus.POOLED)) SearchConsole.this.grids[i].stop();
/*     */           }
/* 300 */           boolean allIsClosed = false;
/* 301 */           while (!allIsClosed) {
/* 302 */             allIsClosed = true;
/* 303 */             for (int i = 0; i < SearchConsole.this.nJobs; i++) {
/* 304 */               if (!SearchConsole.this.grids[i].isTerminated()) {
/* 305 */                 allIsClosed = false;
/*     */                 try {
/* 307 */                   Thread.sleep(500L);
/*     */                 } catch (Exception e) {
/* 309 */                   e.printStackTrace();
/*     */                 }
/* 311 */                 SearchConsole.this.grids[i].stop();
/*     */               }
/*     */             }
/* 314 */             if (!allIsClosed) {
/*     */               try {
/* 316 */                 Thread.sleep(1000L);
/*     */               } catch (Exception e) {
/* 318 */                 e.printStackTrace();
/*     */               }
/*     */             }
/*     */           }
/* 322 */           SearchConsole.this.showText("Ending GRID application  : " + (SearchConsole.this.grid.endApplication() ? "OK" : "FAILED"));
/* 323 */           SearchConsole.this.stopGrid = false;
/*     */         }
/*     */       }).start();
/*     */     }
/*     */   }
/*     */ 
/*     */   public double[] getBestSolutions() {
/* 330 */     double[] bestSolutions = new double[this.monitors.length];
/* 331 */     for (int i = 0; i < this.monitors.length; i++) {
/* 332 */       bestSolutions[i] = this.monitors[i].getBestLikelihood();
/*     */     }
/* 334 */     return bestSolutions;
/*     */   }
/*     */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.SearchConsole
 * JD-Core Version:    0.6.2
 */