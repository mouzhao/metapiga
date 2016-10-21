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
/*     */ import metapiga.parameters.Parameters.GAOperatorChange;
/*     */ import metapiga.parameters.Parameters.GASelection;
/*     */ import metapiga.parameters.Parameters.Heuristic;
/*     */ import metapiga.parameters.Parameters.HeuristicStopCondition;
/*     */ import metapiga.parameters.Parameters.LikelihoodCalculationType;
/*     */ import metapiga.parameters.Parameters.Operator;
/*     */ import metapiga.parameters.Parameters.Optimization;
/*     */ import metapiga.parameters.Parameters.OptimizationAlgorithm;
/*     */ import metapiga.parameters.Parameters.StartingTreeGeneration;
/*     */ import metapiga.trees.Consensus;
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
/*     */ public class GeneticAlgorithm extends Heuristic
/*     */ {
/*     */   private final Monitor monitor;
/*     */   private final boolean trackDetails;
/*     */   private final boolean trackTrees;
/*     */   private final boolean trackPerf;
/*  46 */   private volatile boolean stopAskedByUser = false;
/*     */   private Parameters P;
/*  49 */   private Set<Parameters.HeuristicStopCondition> sufficientStop = new HashSet();
/*  50 */   private Set<Parameters.HeuristicStopCondition> necessaryStop = new HashSet();
/*     */   private final int stopCriterionSteps;
/*     */   private final double stopCriterionTime;
/*     */   private final int stopCriterionAutoSteps;
/*     */   private final double stopCriterionAutoThreshold;
/*     */   private final Operators operators;
/*     */   private final Parameters.Optimization optimization;
/*     */   private final double optimizationUse;
/*     */   private final Parameters.OptimizationAlgorithm optimizationAlgorithm;
/*     */   private final int indNum;
/*     */   private final Parameters.GAOperatorChange operatorChange;
/*     */   private final Parameters.GASelection selection;
/*     */   private final double recombination;
/*     */   private final double replacementStrength;
/*     */   private Tree[] population;
/*     */   private Tree[] offspring;
/*     */   private Tree bestSolution;
/*     */   private int step;
/*     */   private int curInd;
/*     */ 
/*     */   public GeneticAlgorithm(Parameters P, Monitor monitor)
/*     */   {
/*  71 */     super(P);
/*  72 */     this.monitor = monitor;
/*  73 */     this.trackDetails = monitor.trackHeuristic();
/*  74 */     this.trackTrees = monitor.trackHeuristicTrees();
/*  75 */     this.trackPerf = monitor.trackPerformances();
/*  76 */     this.sufficientStop = P.sufficientStopConditions;
/*  77 */     this.necessaryStop = P.necessaryStopConditions;
/*  78 */     this.stopCriterionSteps = P.stopCriterionSteps;
/*  79 */     this.stopCriterionTime = P.stopCriterionTime;
/*  80 */     this.stopCriterionAutoSteps = P.stopCriterionAutoSteps;
/*  81 */     this.stopCriterionAutoThreshold = P.stopCriterionAutoThreshold;
/*  82 */     this.operators = new Operators(P.operators, P.operatorsParameters, P.operatorsFrequencies, P.operatorIsDynamic, P.dynamicInterval, 
/*  83 */       P.dynamicMin, P.operatorSelection, P.optimizationUse, P.gaIndNum - 1, monitor);
/*  84 */     this.indNum = P.gaIndNum;
/*  85 */     this.recombination = P.gaRecombination;
/*  86 */     this.operatorChange = P.gaOperatorChange;
/*  87 */     this.selection = P.gaSelection;
/*  88 */     this.replacementStrength = P.gaReplacementStrength;
/*  89 */     this.population = new Tree[this.indNum];
/*  90 */     this.optimization = P.optimization;
/*  91 */     this.optimizationUse = P.optimizationUse;
/*  92 */     this.optimizationAlgorithm = P.optimizationAlgorithm;
/*  93 */     this.P = P;
/*     */   }
/*     */ 
/*     */   public Tree getBestSolution() {
/*  97 */     return this.bestSolution;
/*     */   }
/*     */ 
/*     */   public String getName(boolean full) {
/* 101 */     return full ? Parameters.Heuristic.GA.verbose() : Parameters.Heuristic.GA.toString();
/*     */   }
/*     */ 
/*     */   public void smoothStop() {
/* 105 */     this.stopAskedByUser = true;
/*     */   }
/*     */ 
/*     */   private void setStartingTrees() throws NullAncestorException, OutgroupTooBigException, TooManyNeighborsException, UnknownTaxonException, UncompatibleOutgroupException, UnrootableTreeException
/*     */   {
/* 110 */     this.monitor.showStageGAPopulation(2 * this.indNum + 1);
/* 111 */     if (this.P.startingTreeGeneration == Parameters.StartingTreeGeneration.GIVEN) {
/* 112 */       for (int ind = 0; ind < this.indNum; ind++) {
/* 113 */         this.population[ind] = ((Tree)this.P.startingTrees.get(ind)).clone();
/* 114 */         this.monitor.showNextStep();
/*     */       }
/* 116 */     } else if (this.P.startingTreeGeneration == Parameters.StartingTreeGeneration.NJ) {
/* 117 */       this.population[0] = this.P.dataset.generateTree(this.P.outgroup, Parameters.StartingTreeGeneration.NJ, this.P.startingTreeGenerationRange, this.P.startingTreeModel, 
/* 118 */         this.P.startingTreeDistribution, this.P.startingTreeDistributionShape, this.P.startingTreePInv, this.P.startingTreePInvPi, this.P, this.monitor);
/* 119 */       this.monitor.showNextStep();
/* 120 */       for (int ind = 1; ind < this.indNum; ind++)
/*     */       {
/* 123 */         this.population[ind] = this.population[0].clone();
/* 124 */         this.population[ind].setName("Tree " + ind);
/* 125 */         this.monitor.showNextStep();
/*     */       }
/*     */     } else {
/* 128 */       for (int ind = 0; ind < this.indNum; ind++) {
/* 129 */         this.population[ind] = this.P.dataset.generateTree(this.P.outgroup, this.P.startingTreeGeneration, this.P.startingTreeGenerationRange, this.P.startingTreeModel, 
/* 130 */           this.P.startingTreeDistribution, this.P.startingTreeDistributionShape, this.P.startingTreePInv, this.P.startingTreePInvPi, this.P, this.monitor);
/* 131 */         this.population[ind].setName("Tree " + ind);
/* 132 */         this.monitor.showNextStep();
/*     */       }
/*     */     }
/*     */ 
/* 136 */     if (this.P.getLikelihoodCalculationType() == Parameters.LikelihoodCalculationType.GPU) {
/* 137 */       for (int ind = 0; ind < this.indNum; ind++) {
/* 138 */         this.population[ind].addMemGPUchunk(this.videocard);
/*     */       }
/*     */     }
/*     */ 
/* 142 */     Tree best = this.population[0];
/* 143 */     for (int ind = 1; ind < this.indNum; ind++) {
/* 144 */       if (this.population[ind].isBetterThan(best)) best = this.population[ind];
/*     */     }
/* 146 */     this.bestSolution = best.clone();
/* 147 */     this.bestSolution.setName("Genetic algorithm best solution");
/* 148 */     this.monitor.showNextStep();
/*     */ 
/* 150 */     this.offspring = new Tree[this.indNum];
/* 151 */     switch ($SWITCH_TABLE$metapiga$parameters$Parameters$GASelection()[this.selection.ordinal()]) {
/*     */     case 3:
/*     */     case 4:
/*     */     case 5:
/* 155 */       break;
/*     */     case 1:
/*     */     case 2:
/* 158 */       for (int i = 0; i < this.indNum; i++) {
/* 159 */         this.offspring[i] = this.P.dataset.generateTree(this.P.outgroup, Parameters.StartingTreeGeneration.RANDOM, this.P.startingTreeGenerationRange, this.P.startingTreeModel, 
/* 160 */           this.P.startingTreeDistribution, this.P.startingTreeDistributionShape, this.P.startingTreePInv, this.P.startingTreePInvPi, this.P, this.monitor);
/* 161 */         this.offspring[i].setName("GA offspring " + i);
/* 162 */         this.monitor.showNextStep();
/*     */       }
/*     */     }
/*     */ 
/* 166 */     List startingTrees = new ArrayList();
/* 167 */     for (int ind = 0; ind < this.indNum; ind++) {
/* 168 */       startingTrees.add(this.population[ind]);
/*     */     }
/* 170 */     if (this.monitor.trackStartingTree()) this.monitor.printStartingTrees(startingTrees); 
/*     */   }
/*     */ 
/*     */   private boolean hasToContinue(int step, long currentTime, long endTime, int noLikelihoodChangeStop)
/*     */   {
/* 174 */     if (this.stopAskedByUser) return false;
/* 175 */     if ((this.sufficientStop.isEmpty()) && (this.necessaryStop.isEmpty())) return false;
/* 176 */     for (Parameters.HeuristicStopCondition condition : this.sufficientStop) {
/* 177 */       switch (condition) {
/*     */       case AUTO:
/* 179 */         if (step >= this.stopCriterionSteps) {
/* 180 */           this.monitor.showText("Genetic Algorithm will stop because it has met a sufficient stop condition : " + step + " steps have been done");
/* 181 */           return false;
/*     */         }
/*     */         break;
/*     */       case CONSENSUS:
/* 185 */         if (currentTime >= endTime) {
/* 186 */           this.monitor.showText("Genetic Algorithm will stop because it has met a sufficient stop condition : search duration has exceeded " + this.stopCriterionTime + " hours");
/* 187 */           return false;
/*     */         }
/*     */         break;
/*     */       case STEPS:
/* 191 */         if (noLikelihoodChangeStop > this.stopCriterionAutoSteps) {
/* 192 */           this.monitor.showText("Genetic Algorithm will stop because it has met a sufficient stop condition : no significantly better solution was found in the last " + this.stopCriterionAutoSteps + " steps");
/* 193 */           return false;
/*     */         }
/*     */         break;
/*     */       }
/*     */     }
/* 198 */     if (!this.necessaryStop.isEmpty()) {
/* 199 */       boolean stop = true;
/* 200 */       String message = "";
/* 201 */       for (Parameters.HeuristicStopCondition condition : this.necessaryStop) {
/* 202 */         switch (condition) {
/*     */         case AUTO:
/* 204 */           if (step < this.stopCriterionSteps)
/* 205 */             stop = false;
/*     */           else {
/* 207 */             message = message + step + " steps have been done, ";
/*     */           }
/* 209 */           break;
/*     */         case CONSENSUS:
/* 211 */           if (currentTime < endTime)
/* 212 */             stop = false;
/*     */           else {
/* 214 */             message = message + "search duration has exceeded " + this.stopCriterionTime + " hours, ";
/*     */           }
/* 216 */           break;
/*     */         case STEPS:
/* 218 */           if (noLikelihoodChangeStop <= this.stopCriterionAutoSteps)
/* 219 */             stop = false;
/*     */           else {
/* 221 */             message = message + "no significantly better solution was found in the last " + this.stopCriterionAutoSteps + " steps, ";
/*     */           }
/*     */           break;
/*     */         }
/*     */       }
/* 226 */       if (stop) {
/* 227 */         this.monitor.showText("Genetic Algorithm will stop because it has met all necessary stop conditions : " + message);
/* 228 */         return false;
/*     */       }
/*     */     }
/* 231 */     return true;
/*     */   }
/*     */ 
/*     */   public void run() {
/* 235 */     if (this.P.getLikelihoodCalculationType() == Parameters.LikelihoodCalculationType.GPU) {
/* 236 */       allocateGPUcontextAndMemory();
/*     */     }
/*     */ 
/* 240 */     long startTime = System.currentTimeMillis();
/* 241 */     long currentTime = System.currentTimeMillis();
/* 242 */     long endTime = startTime + ()(this.stopCriterionTime * 3600.0D * 1000.0D);
/*     */ 
/* 244 */     int noLikelihoodChangeStop = 0;
/* 245 */     double lastBestSolution = 0.0D;
/*     */     try
/*     */     {
/* 249 */       if (this.trackPerf) this.monitor.trackPerformances("Genetic Algorithm starting trees generation", 0);
/* 250 */       setStartingTrees();
/* 251 */       if (this.trackPerf) this.monitor.trackPerformances("Genetic Algorithm starting trees generation", 0);
/*     */ 
/* 253 */       List allTrees = new ArrayList();
/* 254 */       for (Tree ind : this.population) {
/* 255 */         allTrees.add(ind);
/*     */       }
/* 257 */       Map evaluationsToMonitor = new HashMap();
/* 258 */       this.monitor.showStageSearchStart("Genetic Algorithm", this.stopCriterionSteps * (this.indNum - 1), this.bestSolution.getEvaluation());
/* 259 */       this.monitor.showStartingTree(this.bestSolution);
/*     */ 
/* 262 */       for (this.step = 0; hasToContinue(this.step, currentTime, endTime, noLikelihoodChangeStop); this.step += 1) {
/* 263 */         if (this.trackPerf) this.monitor.trackPerformances("Genetic Algorithm step " + this.step, 0);
/*     */ 
/* 265 */         if (this.trackPerf) this.monitor.trackPerformances("Graphical monitoring", 1);
/* 266 */         evaluationsToMonitor.put("Best solution", Double.valueOf(this.bestSolution.getEvaluation()));
/* 267 */         this.monitor.showEvaluations(evaluationsToMonitor);
/* 268 */         if (this.trackPerf) this.monitor.trackPerformances("Graphical monitoring", 1);
/* 269 */         Tree bestSol = this.population[0];
/* 270 */         int bestInd = 0;
/* 271 */         Parameters.Operator[] usedOperators = new Parameters.Operator[this.indNum];
/* 272 */         for (this.curInd = 1; this.curInd < this.indNum; this.curInd += 1) {
/* 273 */           if (this.trackPerf) this.monitor.trackPerformances("Mutation of individual " + this.curInd + " with " + this.operators.getCurrentOperator(), 1);
/* 274 */           this.monitor.showStageSearchProgress(this.step, endTime - currentTime, noLikelihoodChangeStop);
/*     */ 
/* 276 */           this.population[this.curInd].setName(getName(true) + " step " + this.step + " individual " + this.curInd);
/* 277 */           this.operators.mutateTree(this.population[this.curInd]);
/* 278 */           usedOperators[this.curInd] = this.operators.getCurrentOperator();
/* 279 */           if (this.trackPerf) this.monitor.trackPerformances("Mutation of individual " + this.curInd + " with " + this.operators.getCurrentOperator(), 1);
/*     */ 
/* 281 */           if (this.operatorChange == Parameters.GAOperatorChange.IND) {
/* 282 */             this.operators.nextOperator();
/*     */           }
/*     */         }
/* 285 */         if (this.trackDetails) this.monitor.printDetailsGA(this.step, this.population, usedOperators);
/* 286 */         if (this.trackTrees) this.monitor.printTreesGA(this.step, this.population, false);
/* 287 */         if (((this.optimization == Parameters.Optimization.STOCH) && (Math.random() < this.optimizationUse)) || (
/* 288 */           (this.optimization == Parameters.Optimization.DISC) && (this.step % (int)this.optimizationUse == 0))) {
/* 289 */           if (this.trackPerf) this.monitor.trackPerformances("Optimize all individuals with " + this.optimizationAlgorithm, 1);
/* 290 */           for (this.curInd = 0; this.curInd < this.indNum; this.curInd += 1) {
/* 291 */             this.monitor.showStageOptimization(1, "individual " + this.curInd);
/* 292 */             this.population[this.curInd].clone(this.P.getOptimizer(this.population[this.curInd]).getOptimizedTree());
/*     */           }
/* 294 */           if (this.trackPerf) this.monitor.trackPerformances("Optimize all individuals with " + this.optimizationAlgorithm, 1);
/* 295 */           this.monitor.showStageOptimization(0, "");
/*     */         }
/*     */ 
/* 299 */         List selectionDetails = new ArrayList(this.indNum);
/* 300 */         if (this.trackPerf) this.monitor.trackPerformances("Selection", 1);
/* 301 */         switch ($SWITCH_TABLE$metapiga$parameters$Parameters$GASelection()[this.selection.ordinal()])
/*     */         {
/*     */         case 1:
/* 312 */           List ranking = new ArrayList();
/* 313 */           List rankingIndNum = new ArrayList();
/* 314 */           for (int ind = 0; ind < this.indNum; ind++) {
/* 315 */             int rank = 0;
/* 316 */             while (rank < ranking.size()) {
/* 317 */               if (this.population[ind].isBetterThan((Tree)ranking.get(rank))) {
/*     */                 break;
/*     */               }
/* 320 */               rank++;
/*     */             }
/*     */ 
/* 323 */             ranking.add(rank, this.population[ind]);
/* 324 */             rankingIndNum.add(rank, Integer.valueOf(ind));
/*     */           }
/*     */ 
/* 328 */           int numOfBestOffspring = (int)Math.ceil(this.indNum * 0.25D);
/* 329 */           for (int ind = 0; ind < numOfBestOffspring; ind++) {
/* 330 */             this.offspring[ind].clone((Tree)ranking.get(0));
/* 331 */             selectionDetails.add("(R1=" + rankingIndNum.get(0) + ")");
/*     */           }
/* 333 */           for (int ind = numOfBestOffspring; ind < this.indNum; ind++) {
/* 334 */             double randNum = Math.random();
/* 335 */             double lewisProba = 2.0D / (this.indNum * (this.indNum + 1)) * (this.indNum - 1 + 1);
/*     */ 
/* 337 */             for (int rank = 1; randNum > lewisProba; rank++) {
/* 338 */               randNum -= lewisProba;
/* 339 */               lewisProba = 2.0D / (this.indNum * (this.indNum + 1)) * (this.indNum - rank + 1);
/*     */             }
/* 341 */             this.offspring[ind].clone((Tree)ranking.get(rank - 1));
/* 342 */             selectionDetails.add("(R" + rank + "=" + rankingIndNum.get(rank - 1) + ")");
/*     */           }
/*     */ 
/* 345 */           for (int i = 0; i < this.indNum; i++) {
/* 346 */             this.population[i].clone(this.offspring[i]);
/*     */           }
/* 348 */           break;
/*     */         case 2:
/* 360 */           for (int i = 0; i < this.indNum; i++) {
/* 363 */             int ind1 = Tools.randInt(this.indNum - 1);
/*     */             int ind2;
/*     */             do
/* 365 */               ind2 = Tools.randInt(this.indNum - 1);
/* 366 */             while (ind1 == ind2);
/*     */             int weak;
/*     */             int strong;
/*     */             int weak;
/* 369 */             if (this.population[ind1].isBetterThan(this.population[ind2])) {
/* 370 */               int strong = ind1;
/* 371 */               weak = ind2;
/*     */             } else {
/* 373 */               strong = ind2;
/* 374 */               weak = ind1;
/*     */             }
/* 376 */             this.offspring[i].clone(this.population[strong]);
/* 377 */             if (Math.random() < this.recombination) {
/* 378 */               Consensus consensus = new Consensus(this.offspring[i], this.population[weak], this.P.dataset);
/* 379 */               if (consensus.recombination(this.offspring[i], this.population[weak]))
/* 380 */                 selectionDetails.add("(" + ind1 + "vs" + ind2 + "=" + strong + "+" + weak + ")");
/*     */               else
/* 382 */                 selectionDetails.add("(" + ind1 + "vs" + ind2 + "=" + strong + ")");
/*     */             }
/*     */             else {
/* 385 */               selectionDetails.add("(" + ind1 + "vs" + ind2 + "=" + strong + ")");
/*     */             }
/*     */           }
/*     */ 
/* 389 */           for (int i = 0; i < this.indNum; i++) {
/* 390 */             this.population[i].clone(this.offspring[i]);
/*     */           }
/* 392 */           putBestIndividualFirst();
/* 393 */           break;
/*     */         case 3:
/* 406 */           String[] replacedInd = new String[this.indNum];
/* 407 */           for (int i = 0; i < this.indNum; i++) {
/* 408 */             replacedInd[i] = i;
/*     */           }
/* 410 */           for (int i = 0; i < (int)Math.floor(this.replacementStrength * this.indNum); i++) {
/* 413 */             int ind1 = Tools.randInt(this.indNum - 1);
/*     */             int ind2;
/*     */             do
/* 415 */               ind2 = Tools.randInt(this.indNum - 1);
/* 416 */             while (ind1 == ind2);
/*     */             int weak;
/*     */             int strong;
/*     */             int weak;
/* 418 */             if (this.population[ind1].isBetterThan(this.population[ind2])) {
/* 419 */               int strong = ind1;
/* 420 */               weak = ind2;
/*     */             } else {
/* 422 */               strong = ind2;
/* 423 */               weak = ind1;
/*     */             }
/* 425 */             if ((Math.random() < this.recombination) && (!replacedInd[ind1].equals(replacedInd[ind2]))) {
/* 426 */               Consensus consensus = new Consensus(this.population[strong], this.population[weak], this.P.dataset);
/* 427 */               if (consensus.recombination(this.population[weak], this.population[strong])) {
/* 428 */                 selectionDetails.add("(" + replacedInd[ind1] + "vs" + replacedInd[ind2] + "=" + replacedInd[weak] + "+" + replacedInd[strong] + ")");
/* 429 */                 replacedInd[weak] = (replacedInd[weak] + "+" + replacedInd[strong]);
/*     */               } else {
/* 431 */                 this.population[weak].clone(this.population[strong]);
/* 432 */                 selectionDetails.add("(" + replacedInd[ind1] + "vs" + replacedInd[ind2] + "=" + replacedInd[strong] + "*)");
/* 433 */                 replacedInd[weak] = replacedInd[strong];
/*     */               }
/*     */             } else {
/* 436 */               this.population[weak].clone(this.population[strong]);
/* 437 */               selectionDetails.add("(" + replacedInd[ind1] + "vs" + replacedInd[ind2] + "=" + replacedInd[strong] + ")");
/* 438 */               replacedInd[weak] = replacedInd[strong];
/*     */             }
/*     */           }
/* 441 */           putBestIndividualFirst();
/* 442 */           break;
/*     */         case 5:
/* 452 */           for (int ind = 1; ind < this.indNum; ind++) {
/* 453 */             if (this.population[ind].isBetterThan(bestSol)) {
/* 454 */               bestSol = this.population[ind];
/* 455 */               bestInd = ind;
/*     */             }
/*     */           }
/* 458 */           selectionDetails.add(bestInd + " is best individual");
/*     */ 
/* 460 */           for (int ind = 0; ind < this.indNum; ind++) {
/* 461 */             if (ind != bestInd) {
/* 462 */               if (Math.random() < this.recombination) {
/* 463 */                 Consensus consensus = new Consensus(this.population[ind], bestSol, this.P.dataset);
/* 464 */                 if (consensus.recombination(this.population[ind], bestSol))
/* 465 */                   selectionDetails.add("(" + ind + " is recombined)");
/*     */                 else
/* 467 */                   this.population[ind].clone(bestSol);
/*     */               }
/*     */               else {
/* 470 */                 this.population[ind].clone(bestSol);
/*     */               }
/*     */             }
/*     */           }
/* 474 */           break;
/*     */         case 4:
/*     */         default:
/* 488 */           for (int ind = 1; ind < this.indNum; ind++) {
/* 489 */             if (this.population[ind].isBetterThan(bestSol)) {
/* 490 */               bestSol = this.population[ind];
/* 491 */               bestInd = ind;
/*     */             }
/*     */ 
/*     */           }
/*     */ 
/* 496 */           for (int ind = this.indNum - 1; ind >= 0; ind--) {
/* 497 */             if (ind != bestInd) {
/* 498 */               if (!this.population[ind].isBetterThan(this.population[0]))
/*     */               {
/* 500 */                 if (Math.random() < this.recombination) {
/* 501 */                   Consensus consensus = new Consensus(this.population[ind], bestSol, this.P.dataset);
/* 502 */                   if (consensus.recombination(this.population[ind], bestSol)) {
/* 503 */                     selectionDetails.add(ind + " is recombined");
/*     */                   } else {
/* 505 */                     this.population[ind].clone(bestSol);
/* 506 */                     selectionDetails.add(ind + " is replaced");
/*     */                   }
/*     */                 } else {
/* 509 */                   this.population[ind].clone(bestSol);
/* 510 */                   selectionDetails.add(ind + " is replaced");
/*     */                 }
/*     */               }
/* 513 */               else selectionDetails.add(ind + " is kept");
/*     */             }
/*     */             else {
/* 516 */               selectionDetails.add(ind + " is the best");
/*     */             }
/*     */           }
/*     */         }
/*     */ 
/* 521 */         if (this.trackPerf) this.monitor.trackPerformances("Selection", 1);
/*     */ 
/* 523 */         if (this.operatorChange == Parameters.GAOperatorChange.STEP) {
/* 524 */           this.operators.nextOperator();
/*     */         }
/* 526 */         if (this.trackPerf) this.monitor.trackPerformances("Update best solution", 1);
/* 527 */         Tree bestOfThisStep = this.population[0];
/* 528 */         if (bestOfThisStep.isBetterThan(this.bestSolution)) {
/* 529 */           this.bestSolution.clone(bestOfThisStep);
/* 530 */           this.monitor.showCurrentTree(this.bestSolution);
/*     */         }
/* 532 */         if (this.trackPerf) this.monitor.trackPerformances("Update best solution", 1);
/* 533 */         if (this.trackDetails) this.monitor.printDetailsGA(this.step, this.selection + " : " + selectionDetails, this.population, this.bestSolution.getEvaluation());
/* 534 */         if (this.trackTrees) this.monitor.printTreesGA(this.step, this.population, true);
/* 535 */         if ((this.sufficientStop.contains(Parameters.HeuristicStopCondition.AUTO)) || (this.necessaryStop.contains(Parameters.HeuristicStopCondition.AUTO))) {
/* 536 */           if (lastBestSolution - this.bestSolution.getEvaluation() < lastBestSolution * this.stopCriterionAutoThreshold)
/* 537 */             noLikelihoodChangeStop++;
/*     */           else {
/* 539 */             noLikelihoodChangeStop = 0;
/*     */           }
/* 541 */           lastBestSolution = this.bestSolution.getEvaluation();
/*     */         }
/* 543 */         currentTime = System.currentTimeMillis();
/* 544 */         if (this.trackPerf) this.monitor.trackPerformances("Genetic Algorithm step " + this.step, 0);
/*     */       }
/* 546 */       if ((this.optimization != Parameters.Optimization.NEVER) && (this.optimization != Parameters.Optimization.CONSENSUSTREE)) {
/* 547 */         this.monitor.showStageOptimization(1, "best solution");
/* 548 */         if (this.trackPerf) this.monitor.trackPerformances("Optimize best solution with " + this.optimizationAlgorithm, 0);
/* 549 */         this.bestSolution = this.P.getOptimizer(this.bestSolution).getOptimizedTree();
/* 550 */         if (this.trackPerf) this.monitor.trackPerformances("Optimize best solution with " + this.optimizationAlgorithm, 0);
/* 551 */         this.monitor.showStageOptimization(0, "best solution");
/*     */       }
/* 553 */       Object solTree = new ArrayList();
/* 554 */       ((List)solTree).add(this.bestSolution);
/* 555 */       evaluationsToMonitor.put("Best solution", Double.valueOf(this.bestSolution.getEvaluation()));
/* 556 */       if (this.trackTrees) this.monitor.printEndTreesHeuristic();
/* 557 */       this.operators.printStatistics();
/* 558 */       this.monitor.showStageSearchStop((List)solTree, evaluationsToMonitor);
/*     */     } catch (OutOfMemoryError e) {
/* 560 */       this.monitor.endFromException(new Exception("Out of memory: please, assign more RAM to MetaPIGA. You can easily do so by using the menu 'Tools --> Memory settings'."));
/*     */     } catch (Exception e) {
/* 562 */       this.monitor.endFromException(e);
/*     */     }
/* 564 */     if (this.P.getLikelihoodCalculationType() == Parameters.LikelihoodCalculationType.GPU)
/* 565 */       this.videocard.freeMemory();
/*     */   }
/*     */ 
/*     */   private void putBestIndividualFirst()
/*     */     throws NullAncestorException, UnrootableTreeException
/*     */   {
/* 572 */     int bestSol = 0;
/* 573 */     for (int ind = 1; ind < this.indNum; ind++) {
/* 574 */       if (this.population[ind].isBetterThan(this.population[bestSol])) {
/* 575 */         bestSol = ind;
/*     */       }
/*     */     }
/* 578 */     if (bestSol != 0) {
/* 579 */       Tree bestIndividual = this.population[bestSol];
/* 580 */       this.population[bestSol] = this.population[0];
/* 581 */       this.population[0] = bestIndividual;
/*     */     }
/*     */   }
/*     */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.heuristics.GeneticAlgorithm
 * JD-Core Version:    0.6.2
 */