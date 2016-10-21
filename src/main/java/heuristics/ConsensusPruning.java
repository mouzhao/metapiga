/*     */ package heuristics;
/*     */ 
/*     */ import java.util.ArrayList;
/*     */ import java.util.Arrays;
/*     */ import java.util.HashMap;
/*     */ import java.util.HashSet;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Set;
/*     */ import java.util.concurrent.ExecutorService;
/*     */ import java.util.concurrent.Executors;
/*     */ import java.util.concurrent.TimeUnit;
/*     */ import metapiga.exceptions.OutgroupTooBigException;
/*     */ import metapiga.modelization.Dataset;
/*     */ import metapiga.monitors.Monitor;
/*     */ import metapiga.monitors.Monitor.MonitorType;
/*     */ import metapiga.optimization.Optimizer;
/*     */ import metapiga.parameters.Parameters;
/*     */ import metapiga.parameters.Parameters.CPConsensus;
/*     */ import metapiga.parameters.Parameters.CPOperator;
/*     */ import metapiga.parameters.Parameters.CPOperatorChange;
/*     */ import metapiga.parameters.Parameters.CPSelection;
/*     */ import metapiga.parameters.Parameters.Heuristic;
/*     */ import metapiga.parameters.Parameters.HeuristicStopCondition;
/*     */ import metapiga.parameters.Parameters.LikelihoodCalculationType;
/*     */ import metapiga.parameters.Parameters.Operator;
/*     */ import metapiga.parameters.Parameters.Optimization;
/*     */ import metapiga.parameters.Parameters.OptimizationAlgorithm;
/*     */ import metapiga.parameters.Parameters.StartingTreeGeneration;
/*     */ import metapiga.trees.Consensus;
/*     */ import metapiga.trees.ConsensusMRE;
/*     */ import metapiga.trees.Operators;
/*     */ import metapiga.trees.Tree;
/*     */ import metapiga.trees.exceptions.NullAncestorException;
/*     */ import metapiga.trees.exceptions.TooManyNeighborsException;
/*     */ import metapiga.trees.exceptions.UncompatibleOutgroupException;
/*     */ import metapiga.trees.exceptions.UnknownTaxonException;
/*     */ import metapiga.trees.exceptions.UnrootableTreeException;
/*     */ import metapiga.utilities.Tools;
/*     */ import metapiga.videoCard.VideocardContext;
/*     */ 
/*     */ public class ConsensusPruning extends Heuristic
/*     */ {
/*     */   private final Monitor monitor;
/*     */   private final boolean trackDetails;
/*     */   private final boolean trackTrees;
/*     */   private final boolean trackConsensus;
/*     */   private final boolean trackPerf;
/*  63 */   private volatile boolean stopAskedByUser = false;
/*     */   private final Parameters P;
/*  66 */   private Set<Parameters.HeuristicStopCondition> sufficientStop = new HashSet();
/*  67 */   private Set<Parameters.HeuristicStopCondition> necessaryStop = new HashSet();
/*     */   private final int stopCriterionSteps;
/*     */   private final double stopCriterionTime;
/*     */   private final int stopCriterionAutoSteps;
/*     */   private final double stopCriterionAutoThreshold;
/*     */   private final double stopCriterionConsensusMRE;
/*     */   private final int stopCriterionConsensusGeneration;
/*     */   private final int stopCriterionConsensusInterval;
/*     */   private Operators operators;
/*     */   private final Parameters.Optimization optimization;
/*     */   private final double optimizationUse;
/*     */   private final Parameters.OptimizationAlgorithm optimizationAlgorithm;
/*     */   private final int popNum;
/*     */   private final int indNum;
/*     */   private final int coreNum;
/*     */   private final Parameters.CPConsensus consensusType;
/*     */   private final Parameters.CPOperator operatorBehaviour;
/*     */   private final double tolerance;
/*     */   private final double hybridization;
/*     */   private final Parameters.CPOperatorChange operatorChange;
/*     */   private final Parameters.CPSelection selection;
/*     */   private final double recombination;
/*     */   private final double replacementStrength;
/*     */   private final Dataset dataset;
/*     */   private Tree[][] populations;
/*     */   private Tree[][] offspring;
/*     */   private Tree bestSolution;
/*     */   private int step;
/*     */   private String[] allSelectionDetails;
/*     */   private ConsensusMRE consensusMRE;
/*     */   private int noLikelihoodChangeStop;
/*     */ 
/*     */   public ConsensusPruning(Parameters P, Monitor monitor)
/*     */   {
/* 102 */     super(P);
/* 103 */     this.monitor = monitor;
/* 104 */     this.trackDetails = monitor.trackHeuristic();
/* 105 */     this.trackTrees = monitor.trackHeuristicTrees();
/* 106 */     this.trackConsensus = monitor.trackConsensus();
/* 107 */     this.trackPerf = monitor.trackPerformances();
/* 108 */     this.sufficientStop = P.sufficientStopConditions;
/* 109 */     this.necessaryStop = P.necessaryStopConditions;
/* 110 */     this.stopCriterionSteps = P.stopCriterionSteps;
/* 111 */     this.stopCriterionTime = P.stopCriterionTime;
/* 112 */     this.stopCriterionAutoSteps = P.stopCriterionAutoSteps;
/* 113 */     this.stopCriterionAutoThreshold = P.stopCriterionAutoThreshold;
/* 114 */     this.stopCriterionConsensusMRE = P.stopCriterionConsensusMRE;
/* 115 */     this.stopCriterionConsensusInterval = P.stopCriterionConsensusInterval;
/* 116 */     this.stopCriterionConsensusGeneration = P.stopCriterionConsensusGeneration;
/* 117 */     this.operators = new Operators(P.operators, P.operatorsParameters, P.operatorsFrequencies, P.operatorIsDynamic, P.dynamicInterval, 
/* 118 */       P.dynamicMin, P.operatorSelection, P.optimizationUse, P.cpPopNum * (P.cpIndNum - 1), monitor);
/* 119 */     this.popNum = P.cpPopNum;
/* 120 */     this.indNum = P.cpIndNum;
/* 121 */     this.coreNum = P.cpCoreNum;
/* 122 */     this.consensusType = P.cpConsensus;
/* 123 */     this.operatorBehaviour = P.cpOperator;
/* 124 */     this.tolerance = P.cpTolerance;
/* 125 */     this.hybridization = P.cpHybridization;
/* 126 */     this.operatorChange = P.cpOperatorChange;
/* 127 */     this.selection = P.cpSelection;
/* 128 */     this.recombination = P.cpRecombination;
/* 129 */     this.replacementStrength = P.cpReplacementStrength;
/* 130 */     this.dataset = P.dataset;
/* 131 */     this.populations = new Tree[this.popNum][this.indNum];
/* 132 */     this.optimization = P.optimization;
/* 133 */     this.optimizationUse = P.optimizationUse;
/* 134 */     this.optimizationAlgorithm = P.optimizationAlgorithm;
/* 135 */     this.P = P;
/*     */   }
/*     */ 
/*     */   public Tree getBestSolution() {
/* 139 */     return this.bestSolution;
/*     */   }
/*     */ 
/*     */   public String getName(boolean full) {
/* 143 */     return full ? Parameters.Heuristic.CP.verbose() : Parameters.Heuristic.CP.toString();
/*     */   }
/*     */ 
/*     */   public void smoothStop() {
/* 147 */     this.stopAskedByUser = true;
/*     */   }
/*     */ 
/*     */   private void setStartingTrees() throws NullAncestorException, OutgroupTooBigException, TooManyNeighborsException, UncompatibleOutgroupException, UnknownTaxonException, UnrootableTreeException
/*     */   {
/* 152 */     int numOfSteps = this.popNum * this.indNum + 1;
/* 153 */     int offpop = this.coreNum > 1 ? this.popNum : 1;
/* 154 */     switch ($SWITCH_TABLE$metapiga$parameters$Parameters$CPSelection()[this.selection.ordinal()]) {
/*     */     case 3:
/*     */     case 4:
/*     */     case 5:
/* 158 */       break;
/*     */     case 1:
/*     */     case 2:
/* 161 */       numOfSteps += offpop * this.indNum;
/*     */     }
/* 163 */     this.monitor.showStageCPMetapopulation(numOfSteps);
/* 164 */     for (int pop = 0; pop < this.popNum; pop++) {
/* 165 */       if (this.P.startingTreeGeneration == Parameters.StartingTreeGeneration.GIVEN)
/* 166 */         this.populations[pop][0] = ((Tree)this.P.startingTrees.get(pop)).clone();
/* 167 */       else if (this.P.startingTreeGeneration == Parameters.StartingTreeGeneration.NJ) {
/* 168 */         if (pop == 0)
/* 169 */           this.populations[pop][0] = this.P.dataset.generateTree(this.P.outgroup, Parameters.StartingTreeGeneration.NJ, this.P.startingTreeGenerationRange, this.P.startingTreeModel, 
/* 170 */             this.P.startingTreeDistribution, this.P.startingTreeDistributionShape, this.P.startingTreePInv, this.P.startingTreePInvPi, this.P, this.monitor);
/*     */         else
/* 172 */           this.populations[pop][0] = this.P.dataset.generateTree(this.P.outgroup, Parameters.StartingTreeGeneration.LNJ, this.P.startingTreeGenerationRange, this.P.startingTreeModel, 
/* 173 */             this.P.startingTreeDistribution, this.P.startingTreeDistributionShape, this.P.startingTreePInv, this.P.startingTreePInvPi, this.P, this.monitor);
/*     */       }
/*     */       else {
/* 176 */         this.populations[pop][0] = this.P.dataset.generateTree(this.P.outgroup, this.P.startingTreeGeneration, this.P.startingTreeGenerationRange, this.P.startingTreeModel, 
/* 177 */           this.P.startingTreeDistribution, this.P.startingTreeDistributionShape, this.P.startingTreePInv, this.P.startingTreePInvPi, this.P, this.monitor);
/*     */       }
/* 179 */       if (this.P.getLikelihoodCalculationType() == Parameters.LikelihoodCalculationType.GPU) {
/* 180 */         this.populations[pop][0].addMemGPUchunk(this.videocard);
/*     */       }
/*     */ 
/* 183 */       this.populations[pop][0].setName("Best tree of population " + pop);
/* 184 */       this.monitor.showNextStep();
/* 185 */       for (int ind = 1; ind < this.indNum; ind++) {
/* 186 */         this.populations[pop][ind] = this.populations[pop][0].clone();
/* 187 */         this.populations[pop][ind].setName("Tree " + ind + " of population " + pop);
/* 188 */         if (this.P.getLikelihoodCalculationType() == Parameters.LikelihoodCalculationType.GPU) {
/* 189 */           this.populations[pop][ind].addMemGPUchunk(this.videocard);
/*     */         }
/* 191 */         this.monitor.showNextStep();
/*     */       }
/*     */     }
/*     */ 
/* 195 */     Tree best = this.populations[0][0];
/* 196 */     for (int pop = 1; pop < this.popNum; pop++) {
/* 197 */       if (this.populations[pop][0].isBetterThan(best)) best = this.populations[pop][0];
/*     */     }
/* 199 */     this.bestSolution = this.P.dataset.generateTree(this.P.outgroup, Parameters.StartingTreeGeneration.RANDOM, this.P.startingTreeGenerationRange, this.P.startingTreeModel, 
/* 200 */       this.P.startingTreeDistribution, this.P.startingTreeDistributionShape, this.P.startingTreePInv, this.P.startingTreePInvPi, this.P, this.monitor);
/* 201 */     this.bestSolution.setName("Consensus pruning best solution");
/* 202 */     if (this.P.getLikelihoodCalculationType() == Parameters.LikelihoodCalculationType.GPU) {
/* 203 */       this.bestSolution.addMemGPUchunk(this.videocard);
/*     */     }
/* 205 */     this.bestSolution.clone(best);
/* 206 */     this.monitor.showNextStep();
/*     */ 
/* 208 */     this.offspring = new Tree[offpop][this.indNum];
/* 209 */     switch ($SWITCH_TABLE$metapiga$parameters$Parameters$CPSelection()[this.selection.ordinal()]) {
/*     */     case 3:
/*     */     case 4:
/*     */     case 5:
/* 213 */       break;
/*     */     case 1:
/*     */     case 2:
/* 216 */       for (int p = 0; p < offpop; p++) {
/* 217 */         for (int i = 0; i < this.indNum; i++) {
/* 218 */           this.offspring[p][i] = this.P.dataset.generateTree(this.P.outgroup, Parameters.StartingTreeGeneration.RANDOM, this.P.startingTreeGenerationRange, this.P.startingTreeModel, 
/* 219 */             this.P.startingTreeDistribution, this.P.startingTreeDistributionShape, this.P.startingTreePInv, this.P.startingTreePInvPi, this.P, this.monitor);
/* 220 */           this.offspring[p][i].setName("CP offspring " + p + "-" + i);
/* 221 */           if (this.P.getLikelihoodCalculationType() == Parameters.LikelihoodCalculationType.GPU) {
/* 222 */             this.offspring[p][i].addMemGPUchunk(this.videocard);
/*     */           }
/* 224 */           this.monitor.showNextStep();
/*     */         }
/*     */       }
/*     */     }
/* 228 */     List startingTrees = new ArrayList();
/* 229 */     for (int pop = 0; pop < this.popNum; pop++) {
/* 230 */       for (int ind = 0; ind < this.indNum; ind++) {
/* 231 */         startingTrees.add(this.populations[pop][ind]);
/*     */       }
/*     */     }
/* 234 */     if (this.monitor.trackStartingTree()) this.monitor.printStartingTrees(startingTrees); 
/*     */   }
/*     */ 
/*     */   private boolean hasToContinue(int step, long currentTime, long endTime, int noMREChangeStop)
/*     */   {
/* 238 */     if (this.stopAskedByUser) return false;
/* 239 */     if ((this.sufficientStop.isEmpty()) && (this.necessaryStop.isEmpty())) return false;
/* 240 */     for (Parameters.HeuristicStopCondition condition : this.sufficientStop) {
/* 241 */       switch (condition) {
/*     */       case AUTO:
/* 243 */         if (step >= this.stopCriterionSteps) {
/* 244 */           this.monitor.showText("Consensus Pruning will stop because it has met a sufficient stop condition : " + step + " steps have been done");
/* 245 */           return false;
/*     */         }
/*     */         break;
/*     */       case CONSENSUS:
/* 249 */         if (currentTime >= endTime) {
/* 250 */           this.monitor.showText("Consensus Pruning will stop because it has met a sufficient stop condition : search duration has exceeded " + this.stopCriterionTime + " hours");
/* 251 */           return false;
/*     */         }
/*     */         break;
/*     */       case STEPS:
/* 255 */         if (this.noLikelihoodChangeStop > this.stopCriterionAutoSteps) {
/* 256 */           this.monitor.showText("Consensus Pruning will stop because it has met a sufficient stop condition : no significantly better solution was found in the last " + this.stopCriterionAutoSteps + " steps");
/* 257 */           return false;
/*     */         }
/*     */         break;
/*     */       case TIME:
/* 261 */         if (noMREChangeStop >= this.stopCriterionConsensusInterval) {
/* 262 */           this.monitor.showText("Consensus Pruning will stop because it has met a sufficient stop condition : mean relative error of " + this.stopCriterionConsensusInterval + " consecutive consensus trees have stayed below " + Tools.doubleToPercent(this.stopCriterionConsensusMRE, 0) + " using trees sampled every " + this.stopCriterionConsensusGeneration + " generations");
/* 263 */           return false;
/*     */         }
/*     */         break;
/*     */       }
/*     */     }
/* 268 */     if (!this.necessaryStop.isEmpty()) {
/* 269 */       boolean stop = true;
/* 270 */       String message = "";
/* 271 */       for (Parameters.HeuristicStopCondition condition : this.necessaryStop) {
/* 272 */         switch (condition) {
/*     */         case AUTO:
/* 274 */           if (step < this.stopCriterionSteps)
/* 275 */             stop = false;
/*     */           else {
/* 277 */             message = message + step + " steps have been done, ";
/*     */           }
/* 279 */           break;
/*     */         case CONSENSUS:
/* 281 */           if (currentTime < endTime)
/* 282 */             stop = false;
/*     */           else {
/* 284 */             message = message + "search duration has exceeded " + this.stopCriterionTime + " hours, ";
/*     */           }
/* 286 */           break;
/*     */         case STEPS:
/* 288 */           if (this.noLikelihoodChangeStop <= this.stopCriterionAutoSteps)
/* 289 */             stop = false;
/*     */           else {
/* 291 */             message = message + "no significantly better solution was found in the last " + this.stopCriterionAutoSteps + " steps, ";
/*     */           }
/* 293 */           break;
/*     */         case TIME:
/* 295 */           if (noMREChangeStop < this.stopCriterionConsensusInterval)
/* 296 */             stop = false;
/*     */           else {
/* 298 */             message = message + "mean relative error of " + this.stopCriterionConsensusInterval + " consecutive consensus trees have stayed below " + Tools.doubleToPercent(this.stopCriterionConsensusMRE, 0) + " using trees sampled every " + this.stopCriterionConsensusGeneration + " generations, ";
/*     */           }
/*     */           break;
/*     */         }
/*     */       }
/* 303 */       if (stop) {
/* 304 */         this.monitor.showText("Consensus Pruning will stop because it has met all necessary stop conditions : " + message);
/* 305 */         return false;
/*     */       }
/*     */     }
/* 308 */     return true;
/*     */   }
/*     */ 
/*     */   public void run()
/*     */   {
/* 313 */     long startTime = System.currentTimeMillis();
/* 314 */     long currentTime = System.currentTimeMillis();
/* 315 */     long endTime = startTime + ()(this.stopCriterionTime * 3600.0D * 1000.0D);
/* 316 */     if (this.P.getLikelihoodCalculationType() == Parameters.LikelihoodCalculationType.GPU) {
/* 317 */       allocateGPUcontextAndMemory();
/*     */     }
/* 319 */     this.noLikelihoodChangeStop = 0;
/* 320 */     int noMREChangeStop = 0;
/* 321 */     double lastBestSolution = 0.0D;
/*     */     try
/*     */     {
/* 325 */       if (this.trackPerf) this.monitor.trackPerformances("Consensus Pruning starting trees generation", 0);
/* 326 */       setStartingTrees();
/* 327 */       if (this.trackPerf) this.monitor.trackPerformances("Consensus Pruning starting trees generation", 0);
/*     */ 
/* 329 */       List allTrees = new ArrayList();
/* 330 */       for (Tree[] pop : this.populations) {
/* 331 */         allTrees.addAll(Arrays.asList(pop));
/*     */       }
/* 333 */       Map evaluationsToMonitor = new HashMap();
/* 334 */       this.monitor.showStageSearchStart("Consensus Pruning", this.stopCriterionSteps * (this.indNum - 1) * this.popNum, this.bestSolution.getEvaluation());
/* 335 */       this.monitor.showStartingTree(this.bestSolution);
/*     */ 
/* 338 */       for (this.step = 0; hasToContinue(this.step, currentTime, endTime, noMREChangeStop); this.step += 1)
/*     */       {
/* 342 */         if (this.trackPerf) this.monitor.trackPerformances("Consensus Pruning step " + this.step, 0);
/*     */ 
/* 344 */         if (this.trackPerf) this.monitor.trackPerformances("Build consensus list", 1);
/* 345 */         Consensus consensus = new Consensus(allTrees, this.dataset, this.consensusType);
/* 346 */         if (this.trackPerf) this.monitor.trackPerformances("Build consensus list", 1);
/* 347 */         if (this.trackConsensus) this.monitor.printConsensus(this.step, consensus);
/* 348 */         if (((this.sufficientStop.contains(Parameters.HeuristicStopCondition.CONSENSUS)) || (this.necessaryStop.contains(Parameters.HeuristicStopCondition.CONSENSUS))) && 
/* 349 */           (this.step % this.stopCriterionConsensusGeneration == 0)) {
/* 350 */           if (this.step == 0) {
/* 351 */             this.consensusMRE = new ConsensusMRE();
/* 352 */             this.consensusMRE.addConsensus(consensus.getConsensusTree(this.P), this.P, true);
/*     */           } else {
/* 354 */             this.consensusMRE.addConsensus(consensus.getConsensusTree(this.P), this.P, false);
/* 355 */             double mre = this.consensusMRE.meanRelativeError();
/* 356 */             this.monitor.showCurrentMRE(mre);
/* 357 */             if (mre < this.stopCriterionConsensusMRE) {
/* 358 */               noMREChangeStop++;
/*     */             } else {
/* 360 */               noMREChangeStop = 0;
/* 361 */               this.consensusMRE.addConsensus(consensus.getConsensusTree(this.P), this.P, true);
/*     */             }
/*     */           }
/*     */         }
/* 365 */         evaluationsToMonitor.put("Best solution", Double.valueOf(this.bestSolution.getEvaluation()));
/* 366 */         this.allSelectionDetails = new String[this.popNum];
/*     */ 
/* 369 */         int recombinedPop = -1;
/* 370 */         if (Math.random() < this.hybridization) {
/* 371 */           recombinedPop = Tools.randInt(this.popNum);
/* 372 */           evaluationsToMonitor.put("Population " + recombinedPop, Double.valueOf(this.populations[recombinedPop][0].getEvaluation()));
/* 373 */           String[] hybridizationResults = new String[this.indNum];
/* 374 */           for (int curInd = 1; curInd < this.indNum; curInd++) {
/* 375 */             if (this.trackPerf) this.monitor.trackPerformances("Recombination in population " + recombinedPop + " of individual " + curInd, 1);
/* 376 */             this.monitor.showStageSearchProgress(this.step, endTime - System.currentTimeMillis(), this.noLikelihoodChangeStop);
/* 377 */             this.populations[recombinedPop][curInd].setName(getName(true) + " step " + this.step + " population " + recombinedPop + " individual " + curInd);
/* 378 */             hybridizationResults[curInd] = consensus.hybridization(this.populations[recombinedPop][curInd], this.populations[recombinedPop]);
/* 379 */             if (this.trackPerf) this.monitor.trackPerformances("Recombination in population " + recombinedPop + " of individual " + curInd, 1);
/*     */           }
/* 381 */           if (this.trackDetails) this.monitor.printDetailsCP(this.populations[recombinedPop], hybridizationResults, recombinedPop);
/* 382 */           if (this.trackTrees) this.monitor.printTreesCP(this.step, this.populations[recombinedPop], recombinedPop, true);
/* 383 */           putBestIndividualFirst(recombinedPop);
/*     */         }
/*     */ 
/* 387 */         boolean optimize = ((this.optimization == Parameters.Optimization.STOCH) && (Math.random() < this.optimizationUse)) || ((this.optimization == Parameters.Optimization.DISC) && (this.step % (int)this.optimizationUse == 0));
/*     */ 
/* 389 */         if (this.P.getLikelihoodCalculationType() == Parameters.LikelihoodCalculationType.CLASSIC) {
/* 390 */           ExecutorService executor = Executors.newFixedThreadPool(this.coreNum);
/* 391 */           for (int curPop = 0; curPop < this.popNum; curPop++) {
/* 392 */             if (curPop != recombinedPop) {
/* 393 */               evaluationsToMonitor.put("Population " + curPop, Double.valueOf(this.populations[curPop][0].getEvaluation()));
/* 394 */               executor.execute(new IterateGA(curPop, this.populations[curPop], this.coreNum > 1 ? this.offspring[curPop] : this.offspring[0], endTime, consensus, optimize));
/*     */             }
/*     */           }
/* 397 */           executor.shutdown();
/* 398 */           executor.awaitTermination(100L, TimeUnit.DAYS);
/* 399 */         } else if (this.P.getLikelihoodCalculationType() == Parameters.LikelihoodCalculationType.GPU) {
/* 400 */           for (int curPop = 0; curPop < this.popNum; curPop++) {
/* 401 */             if (curPop != recombinedPop) {
/* 402 */               evaluationsToMonitor.put("Population " + curPop, Double.valueOf(this.populations[curPop][0].getEvaluation()));
/* 403 */               IterateGA iterationOfGA = new IterateGA(curPop, this.populations[curPop], this.coreNum > 1 ? this.offspring[curPop] : this.offspring[0], endTime, consensus, optimize);
/* 404 */               iterationOfGA.run();
/*     */             }
/*     */           }
/*     */         }
/*     */ 
/* 409 */         this.monitor.showEvaluations(evaluationsToMonitor);
/*     */ 
/* 412 */         if (this.operatorChange == Parameters.CPOperatorChange.STEP) {
/* 413 */           this.operators.nextOperator();
/*     */         }
/* 415 */         if (this.trackPerf) this.monitor.trackPerformances("Update best solution", 1);
/* 416 */         Tree bestOfThisStep = this.populations[0][0];
/* 417 */         for (int pop = 1; pop < this.popNum; pop++) {
/* 418 */           if (this.populations[pop][0].isBetterThan(bestOfThisStep)) {
/* 419 */             bestOfThisStep = this.populations[pop][0];
/*     */           }
/*     */         }
/* 422 */         if (bestOfThisStep.isBetterThan(this.bestSolution)) {
/* 423 */           this.bestSolution.clone(bestOfThisStep);
/*     */         }
/* 425 */         if ((!this.P.hasManyReplicates()) && (this.monitor.getMonitorType() == Monitor.MonitorType.SINGLE_SEARCH_GRAPHICAL)) {
/* 426 */           List bestTrees = new ArrayList();
/* 427 */           for (int pop = 0; pop < this.popNum; pop++) {
/* 428 */             bestTrees.add(this.populations[pop][0]);
/*     */           }
/* 430 */           this.monitor.showCurrentTree(new Consensus(bestTrees, this.P.dataset).getConsensusTree(this.P));
/*     */         } else {
/* 432 */           this.monitor.showCurrentTree(this.bestSolution);
/*     */         }
/* 434 */         if (this.trackPerf) this.monitor.trackPerformances("Update best solution", 1);
/* 435 */         if (this.trackDetails) this.monitor.printDetailsCP(this.step, this.allSelectionDetails, this.populations, this.bestSolution.getEvaluation());
/* 436 */         if (this.trackTrees) this.monitor.printTreesCP(this.step, this.populations);
/* 437 */         if ((this.sufficientStop.contains(Parameters.HeuristicStopCondition.AUTO)) || (this.necessaryStop.contains(Parameters.HeuristicStopCondition.AUTO))) {
/* 438 */           if (lastBestSolution - this.bestSolution.getEvaluation() < lastBestSolution * this.stopCriterionAutoThreshold)
/* 439 */             this.noLikelihoodChangeStop += 1;
/*     */           else {
/* 441 */             this.noLikelihoodChangeStop = 0;
/*     */           }
/* 443 */           lastBestSolution = this.bestSolution.getEvaluation();
/*     */         }
/* 445 */         currentTime = System.currentTimeMillis();
/* 446 */         if (this.trackPerf) this.monitor.trackPerformances("Consensus Pruning step " + this.step, 0);
/*     */       }
/* 448 */       if ((this.optimization != Parameters.Optimization.NEVER) && (this.optimization != Parameters.Optimization.CONSENSUSTREE)) {
/* 449 */         for (int pop = 0; pop < this.popNum; pop++) {
/* 450 */           this.monitor.showStageOptimization(1, "best solution of population " + pop);
/* 451 */           if (this.trackPerf) this.monitor.trackPerformances("Optimize best solution of population " + pop + " with " + this.optimizationAlgorithm, 0);
/* 452 */           this.populations[pop][0] = this.P.getOptimizer(this.populations[pop][0]).getOptimizedTree();
/* 453 */           if (this.trackPerf) this.monitor.trackPerformances("Optimize best solution of population " + pop + " with " + this.optimizationAlgorithm, 0);
/* 454 */           this.monitor.showStageOptimization(0, "best solution of population " + pop);
/*     */         }
/*     */       }
/* 457 */       Object solTree = new ArrayList();
/* 458 */       ((List)solTree).add(this.bestSolution);
/* 459 */       for (int pop = 0; pop < this.popNum; pop++) {
/* 460 */         if (this.populations[pop][0].getEvaluation() != this.bestSolution.getEvaluation()) {
/* 461 */           this.populations[pop][0].setName("Best individual of population " + pop);
/* 462 */           ((List)solTree).add(this.populations[pop][0]);
/*     */         }
/* 464 */         evaluationsToMonitor.put("Population " + pop, Double.valueOf(this.populations[pop][0].getEvaluation()));
/*     */       }
/* 466 */       evaluationsToMonitor.put("Best solution", Double.valueOf(this.bestSolution.getEvaluation()));
/* 467 */       if (this.trackTrees) this.monitor.printEndTreesHeuristic();
/* 468 */       this.operators.printStatistics();
/* 469 */       this.monitor.showStageSearchStop((List)solTree, evaluationsToMonitor);
/*     */     } catch (OutOfMemoryError e) {
/* 471 */       this.monitor.endFromException(new Exception("Out of memory: please, assign more RAM to MetaPIGA. You can easily do so by using the menu 'Tools --> Memory settings'."));
/*     */     } catch (Exception e) {
/* 473 */       this.monitor.endFromException(e);
/*     */     }
/*     */ 
/* 476 */     if (this.P.getLikelihoodCalculationType() == Parameters.LikelihoodCalculationType.GPU)
/* 477 */       this.videocard.freeMemory();
/*     */   }
/*     */ 
/*     */   private void putBestIndividualFirst(int pop)
/*     */     throws NullAncestorException, UnrootableTreeException
/*     */   {
/* 484 */     int bestSol = 0;
/* 485 */     for (int ind = 1; ind < this.indNum; ind++) {
/* 486 */       if (this.populations[pop][ind].isBetterThan(this.populations[pop][bestSol])) {
/* 487 */         bestSol = ind;
/*     */       }
/*     */     }
/* 490 */     if (bestSol != 0) {
/* 491 */       Tree bestIndividual = this.populations[pop][bestSol];
/* 492 */       this.populations[pop][bestSol] = this.populations[pop][0];
/* 493 */       this.populations[pop][0] = bestIndividual;
/*     */     }
/*     */   }
/*     */ 
/*     */   private synchronized void addSelectionDetails(int curPop, String selectionDetails) {
/* 498 */     this.allSelectionDetails[curPop] = selectionDetails; } 
/*     */   private class IterateGA implements Runnable { int curPop;
/*     */     Tree[] population;
/*     */     Tree[] offspring;
/*     */     long endTime;
/*     */     Consensus consensus;
/*     */     boolean optimize;
/*     */ 
/* 510 */     public IterateGA(int curPop, Tree[] population, Tree[] offspring, long endTime, Consensus consensus, boolean optimize) { this.curPop = curPop;
/* 511 */       this.population = population;
/* 512 */       this.offspring = offspring;
/* 513 */       this.endTime = endTime;
/* 514 */       this.consensus = consensus;
/* 515 */       this.optimize = optimize; }
/*     */ 
/*     */     public void run() {
/*     */       try {
/* 519 */         Tree bestSol = this.population[0];
/* 520 */         int bestInd = 0;
/* 521 */         Parameters.Operator[] usedOperators = new Parameters.Operator[ConsensusPruning.this.indNum];
/*     */ 
/* 523 */         if (ConsensusPruning.this.operatorChange != Parameters.CPOperatorChange.IND) {
/* 524 */           Parameters.Operator op = ConsensusPruning.this.operatorChange == Parameters.CPOperatorChange.POP ? ConsensusPruning.this.operators.nextOperator() : ConsensusPruning.this.operators.getCurrentOperator();
/* 525 */           for (int curInd = 1; curInd < ConsensusPruning.this.indNum; curInd++) {
/* 526 */             usedOperators[curInd] = op;
/*     */           }
/*     */         }
/* 529 */         for (int curInd = 1; curInd < ConsensusPruning.this.indNum; curInd++)
/*     */         {
/* 531 */           if (ConsensusPruning.this.operatorChange == Parameters.CPOperatorChange.IND) {
/* 532 */             usedOperators[curInd] = ConsensusPruning.this.operators.nextOperator();
/*     */           }
/* 534 */           if (ConsensusPruning.this.trackPerf) ConsensusPruning.this.monitor.trackPerformances("Mutation in population " + this.curPop + " of individual " + curInd + " with " + usedOperators[curInd], 1);
/* 535 */           ConsensusPruning.this.monitor.showStageSearchProgress(ConsensusPruning.this.step, this.endTime - System.currentTimeMillis(), ConsensusPruning.this.noLikelihoodChangeStop);
/*     */ 
/* 537 */           this.population[curInd].setName(ConsensusPruning.this.getName(true) + " step " + ConsensusPruning.this.step + " population " + this.curPop + " individual " + curInd);
/* 538 */           if (Math.random() > ConsensusPruning.this.tolerance) {
/* 539 */             ConsensusPruning.this.operators.mutateTree(this.population[curInd], usedOperators[curInd], this.consensus, ConsensusPruning.this.operatorBehaviour);
/*     */           }
/*     */           else {
/* 542 */             ConsensusPruning.this.operators.mutateTree(this.population[curInd], usedOperators[curInd]);
/*     */           }
/* 544 */           if (ConsensusPruning.this.trackPerf) ConsensusPruning.this.monitor.trackPerformances("Mutation in population " + this.curPop + " of individual " + curInd + " with " + usedOperators[curInd], 1);
/*     */         }
/* 546 */         if (ConsensusPruning.this.trackDetails) ConsensusPruning.this.monitor.printDetailsCP(this.population, usedOperators, this.curPop);
/* 547 */         if (ConsensusPruning.this.trackTrees) ConsensusPruning.this.monitor.printTreesCP(ConsensusPruning.this.step, this.population, this.curPop, false);
/*     */ 
/* 549 */         if (this.optimize) {
/* 550 */           if (ConsensusPruning.this.trackPerf) ConsensusPruning.this.monitor.trackPerformances("Optimize all individuals with " + ConsensusPruning.this.optimizationAlgorithm, 1);
/* 551 */           for (int curInd = 0; curInd < ConsensusPruning.this.indNum; curInd++) {
/* 552 */             ConsensusPruning.this.monitor.showStageOptimization(1, "individual " + curInd + " of population " + this.curPop);
/* 553 */             this.population[curInd].clone(ConsensusPruning.this.P.getOptimizer(this.population[curInd]).getOptimizedTree());
/*     */           }
/* 555 */           if (ConsensusPruning.this.trackPerf) ConsensusPruning.this.monitor.trackPerformances("Optimize all individuals with " + ConsensusPruning.this.optimizationAlgorithm, 1);
/* 556 */           ConsensusPruning.this.monitor.showStageOptimization(0, "");
/*     */         }
/*     */ 
/* 560 */         List selectionDetails = new ArrayList(ConsensusPruning.this.indNum);
/*     */ 
/* 562 */         if (ConsensusPruning.this.trackPerf) ConsensusPruning.this.monitor.trackPerformances("Selection on population " + this.curPop, 1);
/* 563 */         switch ($SWITCH_TABLE$metapiga$parameters$Parameters$CPSelection()[ConsensusPruning.this.selection.ordinal()])
/*     */         {
/*     */         case 1:
/* 574 */           List ranking = new ArrayList();
/* 575 */           List rankingIndNum = new ArrayList();
/* 576 */           for (int ind = 0; ind < ConsensusPruning.this.indNum; ind++) {
/* 577 */             int rank = 0;
/* 578 */             while (rank < ranking.size()) {
/* 579 */               if (this.population[ind].isBetterThan((Tree)ranking.get(rank))) {
/*     */                 break;
/*     */               }
/* 582 */               rank++;
/*     */             }
/*     */ 
/* 585 */             ranking.add(rank, this.population[ind]);
/* 586 */             rankingIndNum.add(rank, Integer.valueOf(ind));
/*     */           }
/*     */ 
/* 590 */           int numOfBestOffspring = (int)Math.ceil(ConsensusPruning.this.indNum * 0.25D);
/* 591 */           for (int ind = 0; ind < numOfBestOffspring; ind++) {
/* 592 */             this.offspring[ind].clone((Tree)ranking.get(0));
/* 593 */             selectionDetails.add("(R1=" + rankingIndNum.get(0) + ")");
/*     */           }
/* 595 */           for (int ind = numOfBestOffspring; ind < ConsensusPruning.this.indNum; ind++) {
/* 596 */             double randNum = Math.random();
/* 597 */             double lewisProba = 2.0D / (ConsensusPruning.this.indNum * (ConsensusPruning.this.indNum + 1)) * (ConsensusPruning.this.indNum - 1 + 1);
/*     */ 
/* 599 */             for (int rank = 1; randNum > lewisProba; rank++) {
/* 600 */               randNum -= lewisProba;
/* 601 */               lewisProba = 2.0D / (ConsensusPruning.this.indNum * (ConsensusPruning.this.indNum + 1)) * (ConsensusPruning.this.indNum - rank + 1);
/*     */             }
/* 603 */             this.offspring[ind].clone((Tree)ranking.get(rank - 1));
/* 604 */             selectionDetails.add("(R" + rank + "=" + rankingIndNum.get(rank - 1) + ")");
/*     */           }
/*     */ 
/* 607 */           for (int i = 0; i < ConsensusPruning.this.indNum; i++) {
/* 608 */             this.population[i].clone(this.offspring[i]);
/*     */           }
/* 610 */           break;
/*     */         case 2:
/* 622 */           for (int i = 0; i < ConsensusPruning.this.indNum; i++) {
/* 625 */             int ind1 = Tools.randInt(ConsensusPruning.this.indNum - 1);
/*     */             int ind2;
/*     */             do
/* 627 */               ind2 = Tools.randInt(ConsensusPruning.this.indNum - 1);
/* 628 */             while (ind1 == ind2);
/*     */             int weak;
/*     */             int strong;
/*     */             int weak;
/* 631 */             if (this.population[ind1].isBetterThan(this.population[ind2])) {
/* 632 */               int strong = ind1;
/* 633 */               weak = ind2;
/*     */             } else {
/* 635 */               strong = ind2;
/* 636 */               weak = ind1;
/*     */             }
/* 638 */             this.offspring[i].clone(this.population[strong]);
/* 639 */             if (Math.random() < ConsensusPruning.this.recombination) {
/* 640 */               Consensus cons = new Consensus(this.offspring[i], this.population[weak], ConsensusPruning.this.P.dataset);
/* 641 */               if (cons.recombination(this.offspring[i], this.population[weak]))
/* 642 */                 selectionDetails.add("(" + ind1 + "vs" + ind2 + "=" + strong + "+" + weak + ")");
/*     */               else
/* 644 */                 selectionDetails.add("(" + ind1 + "vs" + ind2 + "=" + strong + ")");
/*     */             }
/*     */             else {
/* 647 */               selectionDetails.add("(" + ind1 + "vs" + ind2 + "=" + strong + ")");
/*     */             }
/*     */           }
/*     */ 
/* 651 */           for (int i = 0; i < ConsensusPruning.this.indNum; i++) {
/* 652 */             this.population[i].clone(this.offspring[i]);
/*     */           }
/* 654 */           ConsensusPruning.this.putBestIndividualFirst(this.curPop);
/* 655 */           break;
/*     */         case 3:
/* 668 */           String[] replacedInd = new String[ConsensusPruning.this.indNum];
/* 669 */           for (int i = 0; i < ConsensusPruning.this.indNum; i++) {
/* 670 */             replacedInd[i] = i;
/*     */           }
/* 672 */           for (int i = 0; i < (int)Math.floor(ConsensusPruning.this.replacementStrength * ConsensusPruning.this.indNum); i++) {
/* 675 */             int ind1 = Tools.randInt(ConsensusPruning.this.indNum - 1);
/*     */             int ind2;
/*     */             do
/* 677 */               ind2 = Tools.randInt(ConsensusPruning.this.indNum - 1);
/* 678 */             while (ind1 == ind2);
/*     */             int weak;
/*     */             int strong;
/*     */             int weak;
/* 680 */             if (this.population[ind1].isBetterThan(this.population[ind2])) {
/* 681 */               int strong = ind1;
/* 682 */               weak = ind2;
/*     */             } else {
/* 684 */               strong = ind2;
/* 685 */               weak = ind1;
/*     */             }
/* 687 */             if (Math.random() < ConsensusPruning.this.recombination) {
/* 688 */               Consensus cons = new Consensus(this.population[strong], this.population[weak], ConsensusPruning.this.P.dataset);
/* 689 */               if (cons.recombination(this.population[weak], this.population[strong])) {
/* 690 */                 selectionDetails.add("(" + replacedInd[ind1] + "vs" + replacedInd[ind2] + "=" + replacedInd[weak] + "+" + replacedInd[strong] + ")");
/* 691 */                 replacedInd[weak] = (replacedInd[weak] + "+" + replacedInd[strong]);
/*     */               } else {
/* 693 */                 this.population[weak].clone(this.population[strong]);
/* 694 */                 selectionDetails.add("(" + replacedInd[ind1] + "vs" + replacedInd[ind2] + "=" + replacedInd[strong] + ")");
/* 695 */                 replacedInd[weak] = replacedInd[strong];
/*     */               }
/*     */             } else {
/* 698 */               this.population[weak].clone(this.population[strong]);
/* 699 */               selectionDetails.add("(" + replacedInd[ind1] + "vs" + replacedInd[ind2] + "=" + replacedInd[strong] + ")");
/* 700 */               replacedInd[weak] = replacedInd[strong];
/*     */             }
/*     */           }
/* 703 */           ConsensusPruning.this.putBestIndividualFirst(this.curPop);
/* 704 */           break;
/*     */         case 5:
/* 714 */           for (int ind = 1; ind < ConsensusPruning.this.indNum; ind++) {
/* 715 */             if (this.population[ind].isBetterThan(bestSol)) {
/* 716 */               bestSol = this.population[ind];
/* 717 */               bestInd = ind;
/*     */             }
/*     */           }
/* 720 */           selectionDetails.add(bestInd + " is best individual");
/*     */ 
/* 722 */           for (int ind = 0; ind < ConsensusPruning.this.indNum; ind++) {
/* 723 */             if (ind != bestInd) {
/* 724 */               if (Math.random() < ConsensusPruning.this.recombination) {
/* 725 */                 Consensus cons = new Consensus(this.population[ind], bestSol, ConsensusPruning.this.P.dataset);
/* 726 */                 if (cons.recombination(this.population[ind], bestSol))
/* 727 */                   selectionDetails.add("(" + ind + " is recombined)");
/*     */                 else
/* 729 */                   this.population[ind].clone(bestSol);
/*     */               }
/*     */               else {
/* 732 */                 this.population[ind].clone(bestSol);
/*     */               }
/*     */             }
/*     */           }
/* 736 */           ConsensusPruning.this.putBestIndividualFirst(this.curPop);
/* 737 */           break;
/*     */         case 4:
/*     */         default:
/* 751 */           for (int ind = 1; ind < ConsensusPruning.this.indNum; ind++) {
/* 752 */             if (this.population[ind].isBetterThan(bestSol)) {
/* 753 */               bestSol = this.population[ind];
/* 754 */               bestInd = ind;
/*     */             }
/*     */ 
/*     */           }
/*     */ 
/* 759 */           for (int ind = ConsensusPruning.this.indNum - 1; ind >= 0; ind--) {
/* 760 */             if (ind != bestInd) {
/* 761 */               if (!this.population[ind].isBetterThan(this.population[0]))
/*     */               {
/* 763 */                 if (Math.random() < ConsensusPruning.this.recombination) {
/* 764 */                   Consensus cons = new Consensus(this.population[ind], bestSol, ConsensusPruning.this.P.dataset);
/* 765 */                   if (cons.recombination(this.population[ind], bestSol)) {
/* 766 */                     selectionDetails.add(ind + " is recombined");
/*     */                   } else {
/* 768 */                     this.population[ind].clone(bestSol);
/* 769 */                     selectionDetails.add(ind + " is replaced");
/*     */                   }
/*     */                 } else {
/* 772 */                   this.population[ind].clone(bestSol);
/* 773 */                   selectionDetails.add(ind + " is replaced");
/*     */                 }
/*     */               }
/* 776 */               else selectionDetails.add(ind + " is kept");
/*     */             }
/*     */             else {
/* 779 */               selectionDetails.add(ind + " is the best");
/*     */             }
/*     */           }
/* 782 */           ConsensusPruning.this.putBestIndividualFirst(this.curPop);
/*     */         }
/*     */ 
/* 785 */         ConsensusPruning.this.addSelectionDetails(this.curPop, ConsensusPruning.this.selection + " : " + selectionDetails);
/* 786 */         if (ConsensusPruning.this.trackPerf) ConsensusPruning.this.monitor.trackPerformances("Selection on population " + this.curPop, 1); 
/*     */       }
/* 788 */       catch (OutOfMemoryError e) { ConsensusPruning.this.monitor.endFromException(new Exception("Out of memory: please, assign more RAM to MetaPIGA. You can easily do so by using the menu 'Tools --> Memory settings'."));
/*     */       } catch (Exception e) {
/* 790 */         ConsensusPruning.this.monitor.endFromException(e);
/*     */       }
/*     */     }
/*     */   }
/*     */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.heuristics.ConsensusPruning
 * JD-Core Version:    0.6.2
 */