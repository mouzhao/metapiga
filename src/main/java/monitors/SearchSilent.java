/*     */ package monitors;
/*     */ 
/*     */ import java.io.BufferedWriter;
/*     */ import java.io.File;
/*     */ import java.io.FileWriter;
/*     */ import java.io.PrintStream;
/*     */ import java.text.DateFormat;
/*     */ import java.text.SimpleDateFormat;
/*     */ import java.util.List;
/*     */ import java.util.Vector;
/*     */ import java.util.concurrent.CountDownLatch;
/*     */ import java.util.concurrent.ExecutorService;
/*     */ import java.util.concurrent.Executors;
/*     */ import java.util.concurrent.TimeUnit;
/*     */ import java.util.concurrent.atomic.AtomicInteger;
/*     */ import javax.swing.DefaultListModel;
/*     */ import javax.swing.JOptionPane;
/*     */ import metapiga.grid.GridConnection;
/*     */ import metapiga.grid.GridMonitor.JobStatus;
/*     */ import metapiga.grid.SearchConsoleGrid;
/*     */ import metapiga.optimization.Optimizer;
/*     */ import metapiga.parameters.Parameters;
/*     */ import metapiga.parameters.Parameters.Optimization;
/*     */ import metapiga.parameters.Parameters.ReplicatesStopCondition;
/*     */ import metapiga.parameters.Parameters.StartingTreeGeneration;
/*     */ import metapiga.trees.Consensus;
/*     */ import metapiga.trees.ConsensusMRE;
/*     */ import metapiga.trees.Tree;
/*     */ import metapiga.utilities.Tools;
/*     */ import org.biojavax.bio.phylo.io.nexus.CharactersBlock;
/*     */ import org.biojavax.bio.phylo.io.nexus.MetapigaBlock;
/*     */ 
/*     */ public class SearchSilent
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
/*  46 */   private List<Tree> allSolutionTrees = new Vector();
/*     */   private String runDirectory;
/*     */   private String runLabel;
/*     */   public String dirPath;
/*     */   private SearchSilentMonitor[] monitors;
/*     */   private SearchConsoleGrid[] grids;
/*     */   private CountDownLatch latch;
/*  53 */   private ConsensusMRE consensusMRE = new ConsensusMRE();
/*  54 */   private GridConnection grid = null;
/*  55 */   private boolean stopGrid = false;
/*     */ 
/*     */   public SearchSilent(DefaultListModel parameters) {
/*  58 */     this.batch = parameters;
/*     */   }
/*     */ 
/*     */   public void run()
/*     */   {
/*  63 */     for (int i = 0; i < this.batch.getSize(); i++) {
/*  64 */       this.currentParameters = ((Parameters)this.batch.get(i));
/*  65 */       DateFormat df = new SimpleDateFormat("yyyy-MM-dd - HH_mm_ss");
/*  66 */       this.runDirectory = this.currentParameters.outputDir;
/*  67 */       File dir = new File(this.runDirectory);
/*  68 */       if (!dir.exists()) {
/*  69 */         dir.mkdirs();
/*     */       }
/*  71 */       this.runLabel = (this.currentParameters.label + " - " + df.format(Long.valueOf(System.currentTimeMillis())));
/*  72 */       this.dirPath = (this.runDirectory + "/" + this.runLabel);
/*  73 */       dir = new File(this.dirPath);
/*  74 */       if ((!dir.exists()) && (!this.currentParameters.gridReplicate)) dir.mkdir();
/*  75 */       switch ($SWITCH_TABLE$metapiga$parameters$Parameters$ReplicatesStopCondition()[this.currentParameters.replicatesStopCondition.ordinal()]) {
/*     */       case 1:
/*  77 */         this.replicatesTotal = new AtomicInteger(this.currentParameters.replicatesNumber);
/*  78 */         break;
/*     */       case 2:
/*  80 */         this.replicatesTotal = new AtomicInteger(this.currentParameters.replicatesMaximum);
/*     */       }
/*     */ 
/*  83 */       this.replicatesDone = new AtomicInteger();
/*  84 */       this.replicatesAvailable = new AtomicInteger();
/*  85 */       this.countMRE = new AtomicInteger();
/*  86 */       if (this.currentParameters.useGrid) {
/*  87 */         this.nJobs = this.replicatesTotal.get();
/*  88 */         this.latch = new CountDownLatch(this.nJobs);
/*  89 */         this.grids = new SearchConsoleGrid[this.nJobs];
/*  90 */         for (int j = 0; j < this.nJobs; j++)
/*  91 */           this.grids[j] = new SearchConsoleGrid(this, this.currentParameters, j + 1);
/*     */       }
/*     */       else {
/*  94 */         this.nJobs = this.currentParameters.replicatesParallel;
/*  95 */         this.latch = null;
/*  96 */         this.monitors = new SearchSilentMonitor[this.nJobs];
/*  97 */         for (int j = 0; j < this.nJobs; j++) {
/*  98 */           this.monitors[j] = new SearchSilentMonitor(this, this.currentParameters, this.dirPath);
/*     */         }
/*     */       }
/*     */       try
/*     */       {
/* 103 */         long startTime = System.currentTimeMillis();
/* 104 */         this.grid = (this.currentParameters.useGrid ? new GridConnection(this, this.currentParameters, this.dirPath) : null);
/* 105 */         this.executor = Executors.newFixedThreadPool(this.currentParameters.useGrid ? Math.min(this.nJobs, 1000) : this.nJobs);
/* 106 */         for (int k = 0; k < this.nJobs; k++) {
/* 107 */           if (this.currentParameters.useGrid) {
/* 108 */             this.grids[k].initialize(this.grid, this.latch);
/* 109 */             this.executor.execute(this.grids[k]);
/* 110 */             while (this.grids[k].getMonitorStatus() == GridMonitor.JobStatus.POOLED)
/*     */               try {
/* 112 */                 Thread.sleep(200L);
/*     */               } catch (InterruptedException ex) {
/* 114 */                 ex.printStackTrace();
/*     */               }
/*     */           }
/*     */           else {
/* 118 */             this.executor.execute(this.monitors[k]);
/*     */           }
/*     */         }
/* 121 */         if (this.currentParameters.useGrid) this.latch.await();
/* 122 */         this.executor.shutdown();
/* 123 */         this.executor.awaitTermination(1000L, TimeUnit.DAYS);
/* 124 */         Thread.sleep(500L);
/* 125 */         if (this.currentParameters.useGrid) stopGrid();
/* 126 */         end(startTime);
/*     */       } catch (Exception e) {
/* 128 */         endFromException(e);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   private void end(long startTime)
/*     */   {
/* 135 */     Tree consensusTree = null;
/*     */     Tree optimizedConsensusTree;
/* 136 */     if (this.currentParameters.hasManyReplicates()) {
/*     */       try {
/* 138 */         Consensus consensus = new Consensus(this.allSolutionTrees, this.currentParameters.dataset);
/* 139 */         consensusTree = consensus.getConsensusTree(this.currentParameters);
/* 140 */         if (this.currentParameters.optimization == Parameters.Optimization.CONSENSUSTREE) {
/* 141 */           optimizedConsensusTree = this.currentParameters.getOptimizer(consensusTree).getOptimizedTree();
/* 142 */           consensusTree.cloneWithConsensus(optimizedConsensusTree);
/*     */         }
/*     */       } catch (Exception ex) {
/* 145 */         JOptionPane.showMessageDialog(null, Tools.getErrorPanel("Cannot build consensus tree", ex), "Consensus tree Error", 0);
/* 146 */         System.out.println("Cannot build consensus tree : " + ex.getMessage());
/* 147 */         ex.printStackTrace();
/*     */       }
/*     */     }
/*     */ 
/* 151 */     if (!this.currentParameters.gridReplicate) {
/* 152 */       if (consensusTree != null) consensusTree.setName(this.runLabel + " - " + consensusTree.getName());
/* 153 */       for (Tree t : this.allSolutionTrees) {
/* 154 */         t.setName(this.runLabel + " - " + t.getName());
/*     */       }
/*     */     }
/*     */ 
/* 158 */     if (!this.currentParameters.gridReplicate) {
/* 159 */       File output = new File(this.dirPath + "/" + "Results.nex");
/*     */       try {
/* 161 */         FileWriter fw = new FileWriter(output);
/* 162 */         BufferedWriter bw = new BufferedWriter(fw);
/* 163 */         bw.write("#NEXUS");
/* 164 */         bw.newLine();
/* 165 */         bw.newLine();
/* 166 */         this.currentParameters.getMetapigaBlock().writeObject(bw);
/* 167 */         bw.newLine();
/* 168 */         this.currentParameters.charactersBlock.writeObject(bw);
/* 169 */         bw.newLine();
/* 170 */         if (this.currentParameters.startingTreeGeneration == Parameters.StartingTreeGeneration.GIVEN) {
/* 171 */           this.currentParameters.writeTreeBlock(bw);
/* 172 */           bw.newLine();
/*     */         }
/* 174 */         bw.write("Begin trees;  [Result trees]");
/* 175 */         bw.newLine();
/* 176 */         if (consensusTree != null) {
/* 177 */           bw.write(consensusTree.toNewickLineWithML(consensusTree.getName(), false, true));
/* 178 */           bw.newLine();
/*     */         }
/* 180 */         for (Tree t : this.allSolutionTrees) {
/* 181 */           bw.write(t.toNewickLineWithML(t.getName(), false, true));
/* 182 */           bw.newLine();
/*     */         }
/* 184 */         bw.write("End;");
/* 185 */         bw.newLine();
/* 186 */         bw.close();
/* 187 */         fw.close();
/*     */       } catch (Exception e) {
/* 189 */         e.printStackTrace();
/*     */       }
/*     */     } else {
/* 192 */       File output = new File(this.currentParameters.cloudOutput);
/*     */       try {
/* 194 */         FileWriter fw = new FileWriter(output);
/* 195 */         BufferedWriter bw = new BufferedWriter(fw);
/* 196 */         bw.write("#NEXUS");
/* 197 */         bw.newLine();
/* 198 */         bw.newLine();
/* 199 */         bw.write("Begin trees;  [Result trees]");
/* 200 */         bw.newLine();
/* 201 */         for (Tree t : this.allSolutionTrees) {
/* 202 */           bw.write(t.toNewickLineWithML(t.getName(), false, true));
/* 203 */           bw.newLine();
/*     */         }
/* 205 */         bw.write("End;");
/* 206 */         bw.newLine();
/* 207 */         bw.close();
/* 208 */         fw.close();
/*     */       } catch (Exception e) {
/* 210 */         e.printStackTrace();
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public void addSolutionTree(List<Tree> trees) {
/* 216 */     this.allSolutionTrees.addAll(trees);
/* 217 */     showReplicate();
/*     */   }
/*     */ 
/*     */   public void endFromException(Exception e) {
/* 221 */     e.printStackTrace();
/*     */   }
/*     */ 
/*     */   public int getNextReplicate() {
/* 225 */     if (this.replicatesAvailable.get() < this.replicatesTotal.get()) {
/* 226 */       return this.replicatesAvailable.incrementAndGet();
/*     */     }
/* 228 */     return -1;
/*     */   }
/*     */ 
/*     */   public double[] getBestSolutions() {
/* 232 */     double[] bestSolutions = new double[this.monitors.length];
/* 233 */     for (int i = 0; i < this.monitors.length; i++) {
/* 234 */       bestSolutions[i] = this.monitors[i].getBestLikelihood();
/*     */     }
/* 236 */     return bestSolutions;
/*     */   }
/*     */ 
/*     */   public void showReplicate() {
/* 240 */     if (this.replicatesDone.incrementAndGet() > 1)
/*     */       try {
/* 242 */         Consensus consensus = new Consensus(this.allSolutionTrees, this.currentParameters.dataset);
/* 243 */         synchronized (this.consensusMRE) {
/* 244 */           Tree consensusTree = consensus.getConsensusTree(this.currentParameters);
/* 245 */           consensusTree.setName("Consensus_tree_" + this.replicatesDone + "_replicates");
/* 246 */           this.consensusMRE.addConsensus(consensusTree, this.currentParameters, false);
/* 247 */           double mre = this.consensusMRE.meanRelativeError();
/* 248 */           consensusTree.setName("Consensus_tree_" + this.replicatesDone + "_replicates" + " [MRE: " + Tools.doubleToPercent(mre, 2) + "]");
/* 249 */           if ((this.replicatesDone.get() > this.currentParameters.replicatesMinimum) && 
/* 250 */             (mre < this.currentParameters.replicatesMRE)) {
/* 251 */             this.countMRE.incrementAndGet();
/* 252 */             if ((this.currentParameters.replicatesStopCondition == Parameters.ReplicatesStopCondition.MRE) && 
/* 253 */               (this.countMRE.get() >= this.currentParameters.replicatesInterval)) {
/* 254 */               this.replicatesAvailable = this.replicatesTotal;
/* 255 */               if (this.currentParameters.useGrid)
/* 256 */                 stopGrid();
/*     */             }
/*     */           }
/*     */           else {
/* 260 */             this.countMRE.set(0);
/* 261 */             this.consensusMRE.addConsensus(consensusTree, this.currentParameters, true);
/*     */           }
/* 263 */           if (this.currentParameters.useGrid) this.grids[0].updateConsensusTree(consensusTree); else
/* 264 */             this.monitors[0].updateConsensusTree(consensusTree);
/*     */         }
/*     */       } catch (Exception e) {
/* 267 */         e.printStackTrace();
/*     */       }
/*     */   }
/*     */ 
/*     */   public void stopGrid()
/*     */   {
/* 273 */     if (!this.stopGrid) {
/* 274 */       this.stopGrid = true;
/* 275 */       new Thread(new Runnable() {
/*     */         public void run() {
/* 277 */           for (int i = 0; i < SearchSilent.this.nJobs; i++) {
/* 278 */             if (SearchSilent.this.grids[i].getMonitorStatus() == GridMonitor.JobStatus.NOT_INITIALIZED) SearchSilent.this.grids[i].stop();
/*     */           }
/* 280 */           for (int i = 0; i < SearchSilent.this.nJobs; i++) {
/* 281 */             if (SearchSilent.this.grids[i].getMonitorStatus() == GridMonitor.JobStatus.POOLED) SearchSilent.this.grids[i].stop();
/*     */           }
/* 283 */           for (int i = 0; i < SearchSilent.this.nJobs; i++) {
/* 284 */             if ((SearchSilent.this.grids[i].getMonitorStatus() != GridMonitor.JobStatus.NOT_INITIALIZED) && (SearchSilent.this.grids[i].getMonitorStatus() != GridMonitor.JobStatus.POOLED)) SearchSilent.this.grids[i].stop();
/*     */           }
/* 286 */           boolean allIsClosed = false;
/* 287 */           while (!allIsClosed) {
/* 288 */             allIsClosed = true;
/* 289 */             for (int i = 0; i < SearchSilent.this.nJobs; i++) {
/* 290 */               if (!SearchSilent.this.grids[i].isTerminated()) {
/* 291 */                 allIsClosed = false;
/*     */                 try {
/* 293 */                   Thread.sleep(500L);
/*     */                 } catch (Exception e) {
/* 295 */                   e.printStackTrace();
/*     */                 }
/* 297 */                 SearchSilent.this.grids[i].stop();
/*     */               }
/*     */             }
/* 300 */             if (!allIsClosed) {
/*     */               try {
/* 302 */                 Thread.sleep(1000L);
/*     */               } catch (Exception e) {
/* 304 */                 e.printStackTrace();
/*     */               }
/*     */             }
/*     */           }
/* 308 */           SearchSilent.this.grid.endApplication();
/* 309 */           SearchSilent.this.stopGrid = false;
/*     */         }
/*     */       }).start();
/*     */     }
/*     */   }
/*     */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.monitors.SearchSilent
 * JD-Core Version:    0.6.2
 */