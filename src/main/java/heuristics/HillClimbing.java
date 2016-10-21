/*     */ package heuristics;
/*     */ 
/*     */ import java.util.ArrayList;
/*     */ import java.util.HashMap;
/*     */ import java.util.HashSet;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Set;
/*     */ import metapiga.exceptions.OutgroupTooBigException;
/*     */ import metapiga.modelization.Dataset;
/*     */ import metapiga.monitors.Monitor;
/*     */ import metapiga.optimization.Optimizer;
/*     */ import metapiga.parameters.Parameters;
/*     */ import metapiga.parameters.Parameters.Heuristic;
/*     */ import metapiga.parameters.Parameters.HeuristicStopCondition;
/*     */ import metapiga.parameters.Parameters.LikelihoodCalculationType;
/*     */ import metapiga.parameters.Parameters.Optimization;
/*     */ import metapiga.parameters.Parameters.OptimizationAlgorithm;
/*     */ import metapiga.parameters.Parameters.StartingTreeGeneration;
/*     */ import metapiga.trees.Operators;
/*     */ import metapiga.trees.Tree;
/*     */ import metapiga.trees.exceptions.TooManyNeighborsException;
/*     */ import metapiga.trees.exceptions.UncompatibleOutgroupException;
/*     */ import metapiga.trees.exceptions.UnknownTaxonException;
/*     */ import metapiga.videoCard.VideocardContext;
/*     */ 
/*     */ public class HillClimbing extends Heuristic
/*     */ {
/*     */   private final Monitor monitor;
/*     */   private final boolean trackDetails;
/*     */   private final boolean trackTrees;
/*     */   private final boolean trackPerf;
/*  39 */   private volatile boolean stopAskedByUser = false;
/*     */   private Parameters P;
/*  42 */   private Set<Parameters.HeuristicStopCondition> sufficientStop = new HashSet();
/*  43 */   private Set<Parameters.HeuristicStopCondition> necessaryStop = new HashSet();
/*     */   private final int stopCriterionSteps;
/*     */   private final double stopCriterionTime;
/*     */   private final int stopCriterionAutoSteps;
/*     */   private final double stopCriterionAutoThreshold;
/*     */   private Operators operators;
/*     */   private final Parameters.Optimization optimization;
/*     */   private final double optimizationUse;
/*     */   private final Parameters.OptimizationAlgorithm optimizationAlgorithm;
/*  52 */   private boolean optimizationBestSolChange = true;
/*     */   private final int restart;
/*     */   private int step;
/*     */   private Tree bestSolution;
/*     */   private Tree S0;
/*     */   private Tree currentBestSolution;
/*     */ 
/*     */   public HillClimbing(Parameters P, Monitor monitor)
/*     */   {
/*  63 */     super(P);
/*  64 */     this.monitor = monitor;
/*  65 */     this.trackDetails = monitor.trackHeuristic();
/*  66 */     this.trackTrees = monitor.trackHeuristicTrees();
/*  67 */     this.trackPerf = monitor.trackPerformances();
/*  68 */     this.restart = P.hcRestart;
/*  69 */     this.sufficientStop = P.sufficientStopConditions;
/*  70 */     this.necessaryStop = P.necessaryStopConditions;
/*  71 */     this.stopCriterionSteps = P.stopCriterionSteps;
/*  72 */     this.stopCriterionTime = P.stopCriterionTime;
/*  73 */     this.stopCriterionAutoSteps = P.stopCriterionAutoSteps;
/*  74 */     this.stopCriterionAutoThreshold = P.stopCriterionAutoThreshold;
/*  75 */     this.operators = new Operators(P.operators, P.operatorsParameters, P.operatorsFrequencies, P.operatorIsDynamic, P.dynamicInterval, 
/*  76 */       P.dynamicMin, P.operatorSelection, P.optimizationUse, 1, monitor);
/*  77 */     this.optimization = P.optimization;
/*  78 */     this.optimizationUse = P.optimizationUse;
/*  79 */     this.optimizationAlgorithm = P.optimizationAlgorithm;
/*  80 */     this.P = P;
/*     */   }
/*     */ 
/*     */   public Tree getBestSolution() {
/*  84 */     return this.bestSolution;
/*     */   }
/*     */ 
/*     */   public String getName(boolean full) {
/*  88 */     return full ? Parameters.Heuristic.HC.verbose() : Parameters.Heuristic.HC.toString();
/*     */   }
/*     */ 
/*     */   public void smoothStop() {
/*  92 */     this.stopAskedByUser = true;
/*     */   }
/*     */ 
/*     */   private void setStartingTree(int currentRestart) throws TooManyNeighborsException, UnknownTaxonException, UncompatibleOutgroupException, OutgroupTooBigException {
/*  96 */     switch ($SWITCH_TABLE$metapiga$parameters$Parameters$StartingTreeGeneration()[this.P.startingTreeGeneration.ordinal()]) {
/*     */     case 4:
/*  98 */       if (currentRestart < this.P.startingTrees.size()) {
/*  99 */         this.monitor.showStageStartingTree(1);
/* 100 */         this.currentBestSolution = ((Tree)this.P.startingTrees.get(currentRestart)).clone();
/*     */       } else {
/* 102 */         this.monitor.showStageStartingTree(2);
/* 103 */         this.currentBestSolution = this.P.dataset.generateTree(this.P.outgroup, Parameters.StartingTreeGeneration.LNJ, this.P.startingTreeGenerationRange, this.P.startingTreeModel, 
/* 104 */           this.P.startingTreeDistribution, this.P.startingTreeDistributionShape, this.P.startingTreePInv, this.P.startingTreePInvPi, this.P, this.monitor);
/* 105 */         this.currentBestSolution.setName("Hill climbing starting tree");
/* 106 */         this.monitor.showNextStep();
/*     */       }
/* 108 */       break;
/*     */     case 1:
/* 110 */       if (currentRestart == 0) {
/* 111 */         this.monitor.showStageStartingTree(2);
/* 112 */         this.currentBestSolution = this.P.dataset.generateTree(this.P.outgroup, Parameters.StartingTreeGeneration.NJ, this.P.startingTreeGenerationRange, this.P.startingTreeModel, 
/* 113 */           this.P.startingTreeDistribution, this.P.startingTreeDistributionShape, this.P.startingTreePInv, this.P.startingTreePInvPi, this.P, this.monitor);
/* 114 */         this.currentBestSolution.setName("Hill climbing starting tree");
/* 115 */         this.monitor.showNextStep();
/*     */       } else {
/* 117 */         this.monitor.showStageStartingTree(2);
/* 118 */         this.currentBestSolution = this.P.dataset.generateTree(this.P.outgroup, Parameters.StartingTreeGeneration.LNJ, this.P.startingTreeGenerationRange, this.P.startingTreeModel, 
/* 119 */           this.P.startingTreeDistribution, this.P.startingTreeDistributionShape, this.P.startingTreePInv, this.P.startingTreePInvPi, this.P, this.monitor);
/* 120 */         this.currentBestSolution.setName("Hill climbing starting tree");
/* 121 */         this.monitor.showNextStep();
/*     */       }
/* 123 */       break;
/*     */     case 2:
/*     */     case 3:
/*     */     default:
/* 127 */       this.monitor.showStageStartingTree(2);
/* 128 */       this.currentBestSolution = this.P.dataset.generateTree(this.P.outgroup, this.P.startingTreeGeneration, this.P.startingTreeGenerationRange, this.P.startingTreeModel, 
/* 129 */         this.P.startingTreeDistribution, this.P.startingTreeDistributionShape, this.P.startingTreePInv, this.P.startingTreePInvPi, this.P, this.monitor);
/* 130 */       this.currentBestSolution.setName("Hill climbing starting tree");
/* 131 */       this.monitor.showNextStep();
/*     */     }
/*     */ 
/* 134 */     this.S0 = this.currentBestSolution.clone();
/* 135 */     this.S0.setName("Hill climbing S0");
/* 136 */     if (this.bestSolution == null) {
/* 137 */       this.bestSolution = this.currentBestSolution.clone();
/* 138 */       this.bestSolution.setName("Hill climbing best solution");
/*     */     }
/*     */ 
/* 142 */     if (this.P.getLikelihoodCalculationType() == Parameters.LikelihoodCalculationType.GPU) {
/* 143 */       this.S0.addMemGPUchunk(this.videocard);
/* 144 */       this.bestSolution.addMemGPUchunk(this.videocard);
/* 145 */       this.currentBestSolution.addMemGPUchunk(this.videocard);
/*     */     }
/*     */ 
/* 149 */     List startingTrees = new ArrayList();
/* 150 */     startingTrees.add(this.currentBestSolution);
/* 151 */     if (this.monitor.trackStartingTree()) this.monitor.printStartingTrees(startingTrees);
/* 152 */     this.currentBestSolution.setName("Hill climbing current solution");
/* 153 */     this.monitor.showNextStep();
/*     */   }
/*     */ 
/*     */   private boolean hasToContinue(int currentRestart, int step, long currentTime, long endTime, int noLikelihoodChangeStop) {
/* 157 */     if (this.stopAskedByUser) return false;
/* 158 */     if ((this.sufficientStop.isEmpty()) && (this.necessaryStop.isEmpty())) return false;
/*     */     String startString;
/*     */     String startString;
/* 160 */     if (this.restart > 0)
/*     */     {
/*     */       String startString;
/* 161 */       if (currentRestart < this.restart)
/* 162 */         startString = "Random-restart Hill Climbing will restart (" + (currentRestart + 1) + "/" + this.restart + ") because";
/*     */       else
/* 164 */         startString = "Random-restart Hill Climbing will stop (all restarts done) because";
/*     */     }
/*     */     else {
/* 167 */       startString = "Stochastic Hill Climbing will stop because";
/*     */     }
/* 169 */     for (Parameters.HeuristicStopCondition condition : this.sufficientStop) {
/* 170 */       switch (condition) {
/*     */       case AUTO:
/* 172 */         if (step >= this.stopCriterionSteps) {
/* 173 */           this.monitor.showText(startString + " it has met a sufficient stop condition : " + step + " steps have been done" + "\n");
/* 174 */           return false;
/*     */         }
/*     */         break;
/*     */       case CONSENSUS:
/* 178 */         if (currentTime >= endTime) {
/* 179 */           this.monitor.showText(startString + " it has met a sufficient stop condition : search duration has exceeded " + this.stopCriterionTime + " hours" + "\n");
/* 180 */           return false;
/*     */         }
/*     */         break;
/*     */       case STEPS:
/* 184 */         if (noLikelihoodChangeStop > this.stopCriterionAutoSteps) {
/* 185 */           this.monitor.showText(startString + " it has met a sufficient stop condition : no significantly better solution was found in the last " + this.stopCriterionAutoSteps + " steps" + "\n");
/* 186 */           return false;
/*     */         }
/*     */         break;
/*     */       }
/*     */     }
/* 191 */     if (!this.necessaryStop.isEmpty()) {
/* 192 */       boolean stop = true;
/* 193 */       String message = "";
/* 194 */       for (Parameters.HeuristicStopCondition condition : this.necessaryStop) {
/* 195 */         switch (condition) {
/*     */         case AUTO:
/* 197 */           if (step < this.stopCriterionSteps)
/* 198 */             stop = false;
/*     */           else {
/* 200 */             message = message + step + " steps have been done, ";
/*     */           }
/* 202 */           break;
/*     */         case CONSENSUS:
/* 204 */           if (currentTime < endTime)
/* 205 */             stop = false;
/*     */           else {
/* 207 */             message = message + "search duration has exceeded " + this.stopCriterionTime + " hours, ";
/*     */           }
/* 209 */           break;
/*     */         case STEPS:
/* 211 */           if (noLikelihoodChangeStop <= this.stopCriterionAutoSteps)
/* 212 */             stop = false;
/*     */           else {
/* 214 */             message = message + "no significantly better solution was found in the last " + this.stopCriterionAutoSteps + " steps, ";
/*     */           }
/*     */           break;
/*     */         }
/*     */       }
/* 219 */       if (stop) {
/* 220 */         this.monitor.showText(startString + " it has met all necessary stop conditions : " + message + "\n");
/* 221 */         return false;
/*     */       }
/*     */     }
/* 224 */     return true;
/*     */   }
/*     */ 
/*     */   public void run() {
/*     */     try {
/* 229 */       if (this.P.getLikelihoodCalculationType() == Parameters.LikelihoodCalculationType.GPU) {
/* 230 */         allocateGPUcontextAndMemory();
/*     */       }
/*     */ 
/* 233 */       Map evaluationsToMonitor = new HashMap();
/* 234 */       for (int currentHC = 0; (currentHC <= this.restart) && (!this.stopAskedByUser); currentHC++)
/*     */       {
/* 236 */         long startTime = System.currentTimeMillis();
/* 237 */         long currentTime = System.currentTimeMillis();
/* 238 */         long endTime = startTime + ()(this.stopCriterionTime * 3600.0D * 1000.0D);
/*     */ 
/* 240 */         int noLikelihoodChangeStop = 0;
/* 241 */         double lastBestSolution = 0.0D;
/*     */ 
/* 244 */         if (this.trackPerf) this.monitor.trackPerformances("Hill climbing starting trees generation", 0);
/* 245 */         setStartingTree(currentHC);
/* 246 */         if (this.restart > 0) this.monitor.showStageHCRestart();
/* 247 */         if (this.trackPerf) this.monitor.trackPerformances("Hill climbing starting trees generation", 0);
/*     */ 
/* 249 */         this.monitor.showStageSearchStart(this.restart > 0 ? "Random-restart Hill Climbing (" + (currentHC > 0 ? "restart " + currentHC + "/" + this.restart : "initial") + ")" : "Stochastic Hill Climbing", this.stopCriterionSteps, this.currentBestSolution.getEvaluation());
/* 250 */         this.monitor.showStartingTree(this.currentBestSolution);
/*     */ 
/* 253 */         for (this.step = 0; hasToContinue(currentHC, this.step, currentTime, endTime, noLikelihoodChangeStop); this.step += 1)
/*     */         {
/* 255 */           if (this.trackPerf) this.monitor.trackPerformances("Hill Climbing step " + this.step, 0);
/* 256 */           if (this.trackPerf) this.monitor.trackPerformances("Graphical monitoring", 1);
/* 257 */           evaluationsToMonitor.put("Best solution", Double.valueOf(this.restart > 0 ? this.bestSolution.getEvaluation() : this.currentBestSolution.getEvaluation()));
/* 258 */           if (this.restart > 0) evaluationsToMonitor.put("Current solution", Double.valueOf(this.currentBestSolution.getEvaluation()));
/* 259 */           this.monitor.showEvaluations(evaluationsToMonitor);
/* 260 */           this.monitor.showStageSearchProgress(this.step, endTime - currentTime, noLikelihoodChangeStop);
/* 261 */           if (this.trackPerf) this.monitor.trackPerformances("Graphical monitoring", 1);
/*     */ 
/* 263 */           if (this.trackPerf) this.monitor.trackPerformances("Clone topology of best solution to S0", 1);
/* 264 */           this.S0.clone(this.currentBestSolution);
/* 265 */           if (this.trackPerf) this.monitor.trackPerformances("Clone topology of best solution to S0", 1);
/* 266 */           if (this.trackPerf) this.monitor.trackPerformances("Mutation of S0 with " + this.operators.getCurrentOperator(), 1);
/* 267 */           this.S0.setName(getName(true) + " step " + this.step);
/* 268 */           this.operators.mutateTree(this.S0);
/* 269 */           if (this.trackPerf) this.monitor.trackPerformances("Mutation of S0 with " + this.operators.getCurrentOperator(), 1);
/* 270 */           if (this.trackDetails) this.monitor.printDetailsHC(this.step, this.currentBestSolution.getEvaluation(), this.S0.getEvaluation(), this.operators.getCurrentOperator(), this.S0.isBetterThan(this.currentBestSolution));
/* 271 */           if (this.trackTrees) this.monitor.printTreesHC(this.step, this.currentBestSolution, this.S0);
/* 272 */           if (this.trackPerf) this.monitor.trackPerformances("Compare with best solution (and update if better)", 1);
/* 273 */           if (this.S0.isBetterThan(this.currentBestSolution)) {
/* 274 */             this.currentBestSolution.clone(this.S0);
/* 275 */             this.optimizationBestSolChange = true;
/* 276 */             this.monitor.showCurrentTree(this.currentBestSolution);
/*     */           }
/* 278 */           if (this.trackPerf) this.monitor.trackPerformances("Compare with best solution (and update if better)", 1);
/* 279 */           this.operators.nextOperator();
/* 280 */           if ((this.optimizationBestSolChange) && (
/* 281 */             ((this.optimization == Parameters.Optimization.STOCH) && (Math.random() < this.optimizationUse)) || (
/* 282 */             (this.optimization == Parameters.Optimization.DISC) && (this.step % (int)this.optimizationUse == 0)))) {
/* 283 */             this.monitor.showStageOptimization(1, "best solution");
/* 284 */             if (this.trackPerf) this.monitor.trackPerformances("Optimize current best solution with " + this.optimizationAlgorithm, 1);
/* 285 */             this.currentBestSolution = this.P.getOptimizer(this.currentBestSolution).getOptimizedTree();
/* 286 */             this.optimizationBestSolChange = false;
/* 287 */             if (this.trackPerf) this.monitor.trackPerformances("Optimize current best solution with " + this.optimizationAlgorithm, 1);
/* 288 */             this.monitor.showStageOptimization(0, "best solution");
/*     */           }
/* 290 */           if ((this.sufficientStop.contains(Parameters.HeuristicStopCondition.AUTO)) || (this.necessaryStop.contains(Parameters.HeuristicStopCondition.AUTO))) {
/* 291 */             if (lastBestSolution - this.currentBestSolution.getEvaluation() < lastBestSolution * this.stopCriterionAutoThreshold)
/* 292 */               noLikelihoodChangeStop++;
/*     */             else {
/* 294 */               noLikelihoodChangeStop = 0;
/*     */             }
/* 296 */             lastBestSolution = this.currentBestSolution.getEvaluation();
/*     */           }
/* 298 */           if (this.trackPerf) this.monitor.trackPerformances("Hill Climbing step " + this.step, 0);
/* 299 */           currentTime = System.currentTimeMillis();
/*     */         }
/* 301 */         if ((this.optimization != Parameters.Optimization.NEVER) && (this.optimization != Parameters.Optimization.CONSENSUSTREE)) {
/* 302 */           this.monitor.showStageOptimization(1, "best solution");
/* 303 */           if (this.trackPerf) this.monitor.trackPerformances("Optimize best solution with " + this.optimizationAlgorithm, 0);
/* 304 */           this.currentBestSolution = this.P.getOptimizer(this.currentBestSolution).getOptimizedTree();
/* 305 */           if (this.trackPerf) this.monitor.trackPerformances("Optimize best solution with " + this.optimizationAlgorithm, 0);
/* 306 */           this.monitor.showStageOptimization(0, "best solution");
/*     */         }
/* 308 */         if (this.currentBestSolution.isBetterThan(this.bestSolution)) {
/* 309 */           this.bestSolution.clone(this.currentBestSolution);
/*     */         }
/*     */       }
/* 312 */       List solTree = new ArrayList();
/* 313 */       solTree.add(this.bestSolution);
/* 314 */       evaluationsToMonitor.put("Best solution", Double.valueOf(this.bestSolution.getEvaluation()));
/* 315 */       if (this.trackTrees) this.monitor.printEndTreesHeuristic();
/* 316 */       this.operators.printStatistics();
/* 317 */       this.monitor.showStageSearchStop(solTree, evaluationsToMonitor);
/*     */     } catch (OutOfMemoryError e) {
/* 319 */       this.monitor.endFromException(new Exception("Out of memory: please, assign more RAM to MetaPIGA. You can easily do so by using the menu 'Tools --> Memory settings'."));
/*     */     } catch (Exception e) {
/* 321 */       this.monitor.endFromException(e);
/*     */     }
/*     */ 
/* 324 */     if (this.P.getLikelihoodCalculationType() == Parameters.LikelihoodCalculationType.GPU)
/* 325 */       this.videocard.freeMemory();
/*     */   }
/*     */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.heuristics.HillClimbing
 * JD-Core Version:    0.6.2
 */