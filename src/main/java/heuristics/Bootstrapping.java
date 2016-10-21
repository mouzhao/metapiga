/*     */ package heuristics;
/*     */ 
/*     */ import java.util.ArrayList;
/*     */ import java.util.HashMap;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import metapiga.modelization.Dataset;
/*     */ import metapiga.monitors.Monitor;
/*     */ import metapiga.optimization.Optimizer;
/*     */ import metapiga.parameters.Parameters;
/*     */ import metapiga.parameters.Parameters.Heuristic;
/*     */ import metapiga.parameters.Parameters.LikelihoodCalculationType;
/*     */ import metapiga.parameters.Parameters.Optimization;
/*     */ import metapiga.parameters.Parameters.OptimizationAlgorithm;
/*     */ import metapiga.trees.Tree;
/*     */ import metapiga.videoCard.VideocardContext;
/*     */ 
/*     */ public class Bootstrapping extends Heuristic
/*     */ {
/*     */   private final Monitor monitor;
/*     */   private final boolean trackTrees;
/*     */   private final boolean trackPerf;
/*     */   private Parameters P;
/*     */   private final Parameters.Optimization optimization;
/*     */   private final Parameters.OptimizationAlgorithm optimizationAlgorithm;
/*     */   private Tree bestSolution;
/*     */ 
/*     */   public Bootstrapping(Parameters P, Monitor monitor)
/*     */   {
/*  37 */     super(P);
/*  38 */     this.monitor = monitor;
/*  39 */     this.trackTrees = monitor.trackHeuristicTrees();
/*  40 */     this.trackPerf = monitor.trackPerformances();
/*  41 */     this.optimization = P.optimization;
/*  42 */     this.optimizationAlgorithm = P.optimizationAlgorithm;
/*  43 */     this.P = P;
/*     */   }
/*     */ 
/*     */   public Tree getBestSolution() {
/*  47 */     return this.bestSolution;
/*     */   }
/*     */ 
/*     */   public String getName(boolean full) {
/*  51 */     return full ? Parameters.Heuristic.BS.verbose() : Parameters.Heuristic.BS.toString();
/*     */   }
/*     */ 
/*     */   public void smoothStop()
/*     */   {
/*     */   }
/*     */ 
/*     */   public void run() {
/*     */     try {
/*  60 */       if (this.P.getLikelihoodCalculationType() == Parameters.LikelihoodCalculationType.GPU) {
/*  61 */         allocateGPUcontextAndMemory();
/*     */       }
/*     */ 
/*  64 */       Map evaluationsToMonitor = new HashMap();
/*     */ 
/*  66 */       this.bestSolution = this.P.getNJT();
/*  67 */       if (this.P.getLikelihoodCalculationType() == Parameters.LikelihoodCalculationType.GPU) {
/*  68 */         this.bestSolution.addMemGPUchunk(this.videocard);
/*     */       }
/*     */ 
/*  71 */       this.monitor.showStageSearchStart("Bootstrapping", 0, this.bestSolution.getEvaluation());
/*  72 */       this.monitor.showStartingTree(this.bestSolution);
/*     */ 
/*  74 */       if (this.trackPerf) this.monitor.trackPerformances("Randomizing dataset", 0);
/*  75 */       Dataset randomizedDataset = this.P.dataset.randomSampling();
/*  76 */       if (this.trackPerf) this.monitor.trackPerformances("Randomizing dataset", 0);
/*     */ 
/*  78 */       if (this.trackPerf) this.monitor.trackPerformances("Building tree", 0);
/*  79 */       this.bestSolution = randomizedDataset.generateTree(this.P.outgroup, this.P.startingTreeGeneration, this.P.startingTreeGenerationRange, this.P.startingTreeModel, this.P.startingTreeDistribution, this.P.startingTreeDistributionShape, this.P.startingTreePInv, this.P.startingTreePInvPi, this.P, this.monitor);
/*  80 */       if (this.trackPerf) this.monitor.trackPerformances("Building tree", 0);
/*     */ 
/*  82 */       evaluationsToMonitor.put("Best solution", Double.valueOf(this.bestSolution.getEvaluation()));
/*     */ 
/*  84 */       for (int i = 0; i < 50; i++) {
/*  85 */         this.monitor.showEvaluations(evaluationsToMonitor);
/*     */       }
/*     */ 
/*  88 */       if ((this.optimization != Parameters.Optimization.NEVER) && (this.optimization != Parameters.Optimization.CONSENSUSTREE)) {
/*  89 */         this.monitor.showStageOptimization(1, "best solution");
/*  90 */         if (this.trackPerf) this.monitor.trackPerformances("Optimize best solution with " + this.optimizationAlgorithm, 0);
/*  91 */         this.bestSolution = this.P.getOptimizer(this.bestSolution).getOptimizedTree();
/*  92 */         if (this.P.getLikelihoodCalculationType() == Parameters.LikelihoodCalculationType.GPU) {
/*  93 */           this.bestSolution.addMemGPUchunk(this.videocard);
/*     */         }
/*  95 */         if (this.trackPerf) this.monitor.trackPerformances("Optimize best solution with " + this.optimizationAlgorithm, 0);
/*  96 */         this.monitor.showStageOptimization(0, "best solution");
/*     */       }
/*     */ 
/*  99 */       List solTree = new ArrayList();
/* 100 */       solTree.add(this.bestSolution);
/* 101 */       evaluationsToMonitor.put("Best solution", Double.valueOf(this.bestSolution.getEvaluation()));
/* 102 */       if (this.trackTrees) this.monitor.printEndTreesHeuristic();
/* 103 */       this.monitor.showStageSearchStop(solTree, evaluationsToMonitor);
/*     */     }
/*     */     catch (OutOfMemoryError e) {
/* 106 */       this.monitor.endFromException(new Exception("Out of memory: please, assign more RAM to MetaPIGA. You can easily do so by using the menu 'Tools --> Memory settings'."));
/*     */     } catch (Exception e) {
/* 108 */       this.monitor.endFromException(e);
/*     */     }
/* 110 */     if (this.P.getLikelihoodCalculationType() == Parameters.LikelihoodCalculationType.GPU)
/* 111 */       this.videocard.freeMemory();
/*     */   }
/*     */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.heuristics.Bootstrapping
 * JD-Core Version:    0.6.2
 */