/*     */ package metapiga.heuristics;
/*     */ 
/*     */ import java.util.ArrayList;
/*     */ import java.util.HashMap;
/*     */ import java.util.HashSet;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Set;
/*     */ import metapiga.exceptions.OutgroupTooBigException;
/*     */ import metapiga.modelization.Dataset;
/*     */ import metapiga.modelization.Dataset.Partition;
/*     */ import metapiga.monitors.Monitor;
/*     */ import metapiga.optimization.Optimizer;
/*     */ import metapiga.parameters.Parameters;
/*     */ import metapiga.parameters.Parameters.Heuristic;
/*     */ import metapiga.parameters.Parameters.HeuristicStopCondition;
/*     */ import metapiga.parameters.Parameters.LikelihoodCalculationType;
/*     */ import metapiga.parameters.Parameters.Operator;
/*     */ import metapiga.parameters.Parameters.Optimization;
/*     */ import metapiga.parameters.Parameters.OptimizationAlgorithm;
/*     */ import metapiga.parameters.Parameters.SACooling;
/*     */ import metapiga.parameters.Parameters.SADeltaL;
/*     */ import metapiga.parameters.Parameters.SAReheating;
/*     */ import metapiga.parameters.Parameters.SASchedule;
/*     */ import metapiga.parameters.Parameters.StartingTreeGeneration;
/*     */ import metapiga.trees.Operators;
/*     */ import metapiga.trees.Tree;
/*     */ import metapiga.trees.exceptions.NoInclusionException;
/*     */ import metapiga.trees.exceptions.NullAncestorException;
/*     */ import metapiga.trees.exceptions.TooManyNeighborsException;
/*     */ import metapiga.trees.exceptions.UncompatibleOutgroupException;
/*     */ import metapiga.trees.exceptions.UnknownTaxonException;
/*     */ import metapiga.trees.exceptions.UnrootableTreeException;
/*     */ import metapiga.videoCard.VideocardContext;
/*     */ 
/*     */ public class SimulatedAnnealing extends Heuristic
/*     */ {
/*     */   public static final int BURN_IN = 20;
/*     */   private final Monitor monitor;
/*     */   private final boolean trackDetails;
/*     */   private final boolean trackTrees;
/*     */   private final boolean trackPerf;
/*  53 */   private volatile boolean stopAskedByUser = false;
/*     */   private Parameters P;
/*  56 */   private Set<Parameters.HeuristicStopCondition> sufficientStop = new HashSet();
/*  57 */   private Set<Parameters.HeuristicStopCondition> necessaryStop = new HashSet();
/*     */   private final int stopCriterionSteps;
/*     */   private final double stopCriterionTime;
/*     */   private final int stopCriterionAutoSteps;
/*     */   private final double stopCriterionAutoThreshold;
/*     */   private Operators operators;
/*     */   private final Parameters.Optimization optimization;
/*     */   private final double optimizationUse;
/*     */   private final Parameters.OptimizationAlgorithm optimizationAlgorithm;
/*  66 */   private boolean optimizationBestSolChange = true;
/*     */   private int step;
/*     */   private double lundyBeta;
/*     */   private double lundyC;
/*     */   private double lundyAlpha;
/*     */   private double scheduleParam;
/*     */   private double initAcceptance;
/*     */   private double finalAcceptance;
/*     */   private double To;
/*     */   private double Tn;
/*     */   private Parameters.SADeltaL dL;
/*     */   private double dLpercent;
/*     */   private double dML;
/*     */   private double temperature;
/*     */   private Parameters.SASchedule schedule;
/*     */   private Parameters.SACooling cooling;
/*     */   private int coolingSteps;
/*     */   private int coolingSuccesses;
/*     */   private int coolingFailures;
/*     */   private Parameters.SAReheating reheating;
/*     */   private int reheatingDecrements;
/*     */   private double reheatingThreshold;
/*     */   private Tree bestSolution;
/*     */   private Tree S0;
/*     */   private Tree S1;
/*     */ 
/*     */   public SimulatedAnnealing(Parameters P, Monitor monitor)
/*     */   {
/* 100 */     super(P);
/* 101 */     this.monitor = monitor;
/* 102 */     this.trackDetails = monitor.trackHeuristic();
/* 103 */     this.trackTrees = monitor.trackHeuristicTrees();
/* 104 */     this.trackPerf = monitor.trackPerformances();
/* 105 */     this.sufficientStop = P.sufficientStopConditions;
/* 106 */     this.necessaryStop = P.necessaryStopConditions;
/* 107 */     this.stopCriterionSteps = P.stopCriterionSteps;
/* 108 */     this.stopCriterionTime = P.stopCriterionTime;
/* 109 */     this.stopCriterionAutoSteps = P.stopCriterionAutoSteps;
/* 110 */     this.stopCriterionAutoThreshold = P.stopCriterionAutoThreshold;
/* 111 */     this.operators = new Operators(P.operators, P.operatorsParameters, 
/* 112 */       P.operatorsFrequencies, P.operatorIsDynamic, P.dynamicInterval, 
/* 113 */       P.dynamicMin, P.operatorSelection, P.optimizationUse, 1, 
/* 114 */       monitor);
/* 115 */     this.lundyC = P.saLundyC;
/* 116 */     this.lundyAlpha = P.saLundyAlpha;
/* 117 */     this.initAcceptance = P.saInitAccept;
/* 118 */     this.finalAcceptance = P.saFinalAccept;
/* 119 */     this.dL = P.saDeltaL;
/* 120 */     this.dLpercent = P.saDeltaLPercent;
/* 121 */     this.schedule = P.saSchedule;
/* 122 */     this.scheduleParam = P.saScheduleParam;
/* 123 */     this.cooling = P.saCoolingType;
/* 124 */     this.coolingSteps = P.saCoolingSteps;
/* 125 */     this.coolingSuccesses = P.saCoolingSuccesses;
/* 126 */     this.coolingFailures = P.saCoolingFailures;
/* 127 */     this.reheating = P.saReheatingType;
/* 128 */     this.reheatingDecrements = ((int)P.saReheatingValue);
/* 129 */     this.reheatingThreshold = P.saReheatingValue;
/* 130 */     this.optimization = P.optimization;
/* 131 */     this.optimizationUse = P.optimizationUse;
/* 132 */     this.optimizationAlgorithm = P.optimizationAlgorithm;
/* 133 */     this.P = P;
/*     */   }
/*     */ 
/*     */   public Tree getBestSolution()
/*     */   {
/* 138 */     return this.bestSolution;
/*     */   }
/*     */ 
/*     */   public String getName(boolean full) {
/* 142 */     return full ? Parameters.Heuristic.SA.verbose() : Parameters.Heuristic.SA.toString();
/*     */   }
/*     */ 
/*     */   public void smoothStop()
/*     */   {
/* 147 */     this.stopAskedByUser = true;
/*     */   }
/*     */ 
/*     */   private void setTemperature(int burnInSteps)
/*     */     throws NoInclusionException, NullAncestorException, OutgroupTooBigException, TooManyNeighborsException, UncompatibleOutgroupException, UnknownTaxonException, UnrootableTreeException
/*     */   {
/*     */     Iterator localIterator;
/* 153 */     switch ($SWITCH_TABLE$metapiga$parameters$Parameters$SADeltaL()[this.dL.ordinal()]) {
/*     */     case 1:
/* 155 */       this.monitor.showStageSATemperature(1);
/* 156 */       double lnNJT = this.P.getNJT().getEvaluation();
/* 157 */       this.monitor.showNextStep();
/* 158 */       this.dML = (lnNJT * this.dLpercent);
/* 159 */       break;
/*     */     case 2:
/* 161 */       this.monitor.showStageSATemperature(this.P.operators.size() * burnInSteps + 1);
/* 162 */       double likelihoodS0 = this.S0.getEvaluation();
/* 163 */       this.monitor.showNextStep();
/* 164 */       this.dML = 0.0D;
/*     */       int i;
/* 165 */       for (localIterator = this.P.operators.iterator(); localIterator.hasNext(); 
/* 166 */         i < burnInSteps)
/*     */       {
/* 165 */         Parameters.Operator op = (Parameters.Operator)localIterator.next();
/* 166 */         i = 0; continue;
/* 167 */         this.monitor.showNextStep();
/* 168 */         this.S1.clone(this.S0);
/* 169 */         this.operators.mutateTree(this.S1, op);
/* 170 */         double diff = Math.abs(likelihoodS0 - this.S1.getEvaluation());
/* 171 */         if (diff > this.dML) this.dML = diff;
/* 166 */         i++;
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 176 */     this.To = Math.abs(-this.dML / Math.log(this.initAcceptance));
/* 177 */     this.reheatingThreshold = (this.To * this.reheatingThreshold);
/* 178 */     this.Tn = Math.abs(-this.dML / Math.log(this.finalAcceptance));
/* 179 */     if (this.schedule == Parameters.SASchedule.LUNDY) {
/* 180 */       this.To = this.dML;
/* 181 */       double n = this.P.dataset.getNTax();
/* 182 */       double m = 0.0D;
/* 183 */       for (Dataset.Partition partition : this.P.dataset.getPartitions()) {
/* 184 */         m += partition.getNChar();
/*     */       }
/* 186 */       double lnNJT = this.P.getNJT().getEvaluation();
/* 187 */       this.lundyBeta = (this.lundyC / ((1.0D - this.lundyAlpha) * n + Math.pow(this.lundyAlpha, lnNJT / m)));
/*     */     }
/* 189 */     this.temperature = this.To;
/*     */   }
/*     */ 
/*     */   private void setStartingTree() throws TooManyNeighborsException, UnknownTaxonException, UncompatibleOutgroupException, OutgroupTooBigException {
/* 193 */     if (this.P.startingTreeGeneration == Parameters.StartingTreeGeneration.GIVEN) {
/* 194 */       this.monitor.showStageStartingTree(1);
/* 195 */       this.bestSolution = ((Tree)this.P.startingTrees.get(0)).clone();
/*     */     } else {
/* 197 */       this.monitor.showStageStartingTree(2);
/* 198 */       this.bestSolution = this.P.dataset.generateTree(this.P.outgroup, this.P.startingTreeGeneration, this.P.startingTreeGenerationRange, this.P.startingTreeModel, 
/* 199 */         this.P.startingTreeDistribution, this.P.startingTreeDistributionShape, this.P.startingTreePInv, this.P.startingTreePInvPi, this.P, this.monitor);
/* 200 */       this.bestSolution.setName("Simulated annealing starting tree");
/* 201 */       this.monitor.showNextStep();
/*     */     }
/* 203 */     this.S0 = this.bestSolution.clone();
/* 204 */     this.S0.setName("Simulated annealing S0");
/* 205 */     this.S1 = this.S0.clone();
/* 206 */     this.S1.setName("Simulated annealing S1");
/* 207 */     List startingTrees = new ArrayList();
/* 208 */     startingTrees.add(this.bestSolution);
/* 209 */     if (this.monitor.trackStartingTree()) this.monitor.printStartingTrees(startingTrees);
/* 210 */     this.bestSolution.setName("Simulated annealing best solution");
/* 211 */     this.monitor.showNextStep();
/*     */ 
/* 214 */     if (this.P.getLikelihoodCalculationType() == Parameters.LikelihoodCalculationType.GPU) {
/* 215 */       this.S0.addMemGPUchunk(this.videocard);
/* 216 */       this.bestSolution.addMemGPUchunk(this.videocard);
/* 217 */       this.S1.addMemGPUchunk(this.videocard);
/*     */     }
/*     */   }
/*     */ 
/*     */   private boolean hasToContinue(int step, long currentTime, long endTime, int noLikelihoodChangeStop) {
/* 222 */     if (this.stopAskedByUser) return false;
/* 223 */     if ((this.sufficientStop.isEmpty()) && (this.necessaryStop.isEmpty())) return false;
/* 224 */     for (Parameters.HeuristicStopCondition condition : this.sufficientStop) {
/* 225 */       switch (condition) {
/*     */       case AUTO:
/* 227 */         if (step >= this.stopCriterionSteps) {
/* 228 */           this.monitor.showText("Simulated Annealing will stop because it has met a sufficient stop condition : " + step + " steps have been done");
/* 229 */           return false;
/*     */         }
/*     */         break;
/*     */       case CONSENSUS:
/* 233 */         if (currentTime >= endTime) {
/* 234 */           this.monitor.showText("Simulated Annealing will stop because it has met a sufficient stop condition : search duration has exceeded " + this.stopCriterionTime + " hours");
/* 235 */           return false;
/*     */         }
/*     */         break;
/*     */       case STEPS:
/* 239 */         if (noLikelihoodChangeStop > this.stopCriterionAutoSteps) {
/* 240 */           this.monitor.showText("Simulated Annealing will stop because it has met a sufficient stop condition : no significantly better solution was found in the last " + this.stopCriterionAutoSteps + " steps");
/* 241 */           return false;
/*     */         }
/*     */         break;
/*     */       }
/*     */     }
/* 246 */     if (!this.necessaryStop.isEmpty()) {
/* 247 */       boolean stop = true;
/* 248 */       String message = "";
/* 249 */       for (Parameters.HeuristicStopCondition condition : this.necessaryStop) {
/* 250 */         switch (condition) {
/*     */         case AUTO:
/* 252 */           if (step < this.stopCriterionSteps)
/* 253 */             stop = false;
/*     */           else {
/* 255 */             message = message + step + " steps have been done, ";
/*     */           }
/* 257 */           break;
/*     */         case CONSENSUS:
/* 259 */           if (currentTime < endTime)
/* 260 */             stop = false;
/*     */           else {
/* 262 */             message = message + "search duration has exceeded " + this.stopCriterionTime + " hours, ";
/*     */           }
/* 264 */           break;
/*     */         case STEPS:
/* 266 */           if (noLikelihoodChangeStop <= this.stopCriterionAutoSteps)
/* 267 */             stop = false;
/*     */           else {
/* 269 */             message = message + "no significantly better solution was found in the last " + this.stopCriterionAutoSteps + " steps, ";
/*     */           }
/*     */           break;
/*     */         }
/*     */       }
/* 274 */       if (stop) {
/* 275 */         this.monitor.showText("Simulated Annealing will stop because it has met all necessary stop conditions : " + message);
/* 276 */         return false;
/*     */       }
/*     */     }
/* 279 */     return true;
/*     */   }
/*     */ 
/*     */   public void run() {
/* 283 */     if (this.P.getLikelihoodCalculationType() == Parameters.LikelihoodCalculationType.GPU) {
/* 284 */       allocateGPUcontextAndMemory();
/*     */     }
/*     */ 
/* 288 */     long startTime = System.currentTimeMillis();
/* 289 */     long currentTime = System.currentTimeMillis();
/* 290 */     long endTime = startTime + ()(this.stopCriterionTime * 3600.0D * 1000.0D);
/*     */ 
/* 292 */     int noLikelihoodChangeStop = 0;
/* 293 */     double lastBestSolution = 0.0D;
/* 294 */     int coolingCount = 0;
/* 295 */     int successes = 0;
/* 296 */     int failures = 0;
/* 297 */     int i = 0;
/*     */     try
/*     */     {
/* 301 */       if (this.trackPerf) this.monitor.trackPerformances("Simulated Annealing starting trees generation", 0);
/* 302 */       setStartingTree();
/* 303 */       if (this.trackPerf) this.monitor.trackPerformances("Simulated Annealing starting trees generation", 0);
/*     */ 
/* 306 */       if (this.trackPerf) this.monitor.trackPerformances("Simulated Annealing temperature burn in", 0);
/* 307 */       setTemperature(20);
/* 308 */       if (this.trackPerf) this.monitor.trackPerformances("Simulated Annealing temperature burn in", 0);
/*     */ 
/* 310 */       Map evaluationsToMonitor = new HashMap();
/* 311 */       this.monitor.showStageSearchStart("Simulated Annealing", this.stopCriterionSteps, this.bestSolution.getEvaluation());
/* 312 */       this.monitor.showStartingTree(this.bestSolution);
/*     */ 
/* 315 */       for (this.step = 0; hasToContinue(this.step, currentTime, endTime, noLikelihoodChangeStop); this.step += 1) {
/* 316 */         if (this.trackPerf) this.monitor.trackPerformances("Simulated Annealing step " + this.step, 0);
/*     */ 
/* 318 */         if (((this.reheating == Parameters.SAReheating.DECREMENTS) && (i == this.reheatingDecrements)) || (
/* 319 */           (this.reheating == Parameters.SAReheating.THRESHOLD) && (this.temperature < this.reheatingThreshold))) {
/* 320 */           if (this.trackPerf) this.monitor.trackPerformances("Reheating", 1);
/* 321 */           this.temperature = this.To;
/* 322 */           i = 0;
/* 323 */           this.S0.clone(this.bestSolution);
/* 324 */           if (this.trackPerf) this.monitor.trackPerformances("Reheating", 1);
/*     */         }
/*     */ 
/* 327 */         if (((this.cooling == Parameters.SACooling.STEPS) && (coolingCount == this.coolingSteps)) || (
/* 328 */           (this.cooling == Parameters.SACooling.SF) && ((successes == this.coolingSuccesses) || (failures == this.coolingFailures)))) {
/* 329 */           if (this.trackPerf) this.monitor.trackPerformances("Decrease temperature", 1);
/* 330 */           i++;
/*     */ 
/* 332 */           coolingCount = 0;
/* 333 */           successes = 0;
/* 334 */           failures = 0;
/*     */ 
/* 336 */           int n = this.reheatingDecrements;
/* 337 */           switch ($SWITCH_TABLE$metapiga$parameters$Parameters$SASchedule()[this.schedule.ordinal()]) {
/*     */           case 1:
/* 339 */             this.temperature = (this.dML / (1.0D + i * this.lundyBeta));
/* 340 */             break;
/*     */           case 5:
/* 342 */             this.temperature = (this.scheduleParam * this.temperature);
/* 343 */             break;
/*     */           case 2:
/* 345 */             this.temperature = (this.To / (i + 1));
/* 346 */             break;
/*     */           case 3:
/* 348 */             this.temperature = (this.To / Math.log(i + 1));
/* 349 */             break;
/*     */           case 4:
/* 351 */             this.temperature = (this.To * Math.pow(this.scheduleParam, i));
/* 352 */             break;
/*     */           case 6:
/* 354 */             this.temperature = (this.To - i * (this.To - this.Tn) / n);
/* 355 */             break;
/*     */           case 7:
/* 357 */             this.temperature = (this.To * Math.pow(this.Tn / this.To, i / n));
/* 358 */             break;
/*     */           case 8:
/* 360 */             this.temperature = ((this.To - this.Tn) * (n + 1.0D) / (n * (i + 1.0D)) + this.To - (this.To - this.Tn) * (n + 1.0D) / n);
/* 361 */             break;
/*     */           case 9:
/* 363 */             this.temperature = (this.Tn + (this.To - this.Tn) / (1.0D + Math.exp(3.0D * (i - n / 2.0D))));
/* 364 */             break;
/*     */           case 10:
/* 366 */             this.temperature = (this.To * Math.exp(-(Math.pow(i / n, 2.0D) * Math.log(this.To / this.Tn))));
/* 367 */             break;
/*     */           case 11:
/* 369 */             this.temperature = (0.5D * (this.To - this.Tn) * (1.0D + Math.cos(i * 3.141592653589793D / n)) + this.Tn);
/* 370 */             break;
/*     */           case 12:
/* 372 */             this.temperature = (0.25D * (this.To - this.Tn) * (2.0D + Math.cos(8.0D * i * 3.141592653589793D / n)) * Math.exp(-(i / (0.2D * n))));
/* 373 */             break;
/*     */           case 13:
/* 375 */             this.temperature = (0.5D * (this.To - this.Tn) * (1.0D - Math.tanh(10.0D * i / n - 5.0D)) + this.Tn);
/* 376 */             break;
/*     */           case 14:
/* 378 */             this.temperature = ((this.To - this.Tn) / Math.cosh(10.0D * i / n) + this.Tn);
/*     */           }
/*     */ 
/* 381 */           if (this.trackPerf) this.monitor.trackPerformances("Decrease temperature", 1);
/*     */         }
/*     */ 
/* 384 */         if (this.trackPerf) this.monitor.trackPerformances("Graphical monitoring", 1);
/* 385 */         evaluationsToMonitor.put("Best solution", Double.valueOf(this.bestSolution.getEvaluation()));
/* 386 */         evaluationsToMonitor.put("Current solution", Double.valueOf(this.S0.getEvaluation()));
/* 387 */         this.monitor.showEvaluations(evaluationsToMonitor);
/* 388 */         this.monitor.showStageSearchProgress(this.step, endTime - currentTime, noLikelihoodChangeStop);
/* 389 */         this.monitor.showCurrentTemperature(this.temperature);
/* 390 */         if (this.trackPerf) this.monitor.trackPerformances("Graphical monitoring", 1);
/*     */ 
/* 392 */         if (this.trackPerf) this.monitor.trackPerformances("Clone topology of S0 to S1", 1);
/* 393 */         this.S1.clone(this.S0);
/* 394 */         if (this.trackPerf) this.monitor.trackPerformances("Clone topology of S0 to S1", 1);
/* 395 */         if (this.trackPerf) this.monitor.trackPerformances("Mutation of S1 with " + this.operators.getCurrentOperator(), 1);
/* 396 */         this.S1.setName(getName(true) + " step " + this.step);
/* 397 */         this.operators.mutateTree(this.S1);
/* 398 */         if (this.trackPerf) this.monitor.trackPerformances("Mutation of S1 with " + this.operators.getCurrentOperator(), 1);
/* 399 */         double temperatureAcceptance = Math.exp((this.S0.getEvaluation() - this.S1.getEvaluation()) / this.temperature);
/* 400 */         double temprand = Math.random();
/*     */         String status;
/* 402 */         if (this.S1.isBetterThan(this.bestSolution)) {
/* 403 */           String status = "Improvement (global)";
/* 404 */           successes++;
/* 405 */         } else if (this.S1.isBetterThan(this.S0)) {
/* 406 */           String status = "Improvement (local)";
/* 407 */           successes++;
/* 408 */         } else if (temperatureAcceptance > temprand) {
/* 409 */           String status = "Decrease (local)";
/* 410 */           failures++;
/*     */         } else {
/* 412 */           status = "Stability (local)";
/* 413 */           failures++;
/*     */         }
/* 415 */         if (this.trackDetails) this.monitor.printDetailsSA(this.step, this.bestSolution.getEvaluation(), this.S0.getEvaluation(), this.S1.getEvaluation(), this.operators.getCurrentOperator(), 
/* 416 */             status, temperatureAcceptance, this.temperature, coolingCount + 1, successes, failures, i);
/* 417 */         if (this.trackTrees) this.monitor.printTreesSA(this.step, this.bestSolution, this.S0, this.S1);
/*     */ 
/* 419 */         if (this.trackPerf) this.monitor.trackPerformances("Compare S1 to S0 and Best solution, and update if necessary", 1);
/* 420 */         if (this.S1.isBetterThan(this.bestSolution))
/*     */         {
/* 422 */           this.bestSolution.clone(this.S1);
/* 423 */           this.S0.clone(this.S1);
/* 424 */           this.monitor.showCurrentTree(this.bestSolution);
/* 425 */         } else if (this.S1.isBetterThan(this.S0)) {
/* 426 */           this.S0.clone(this.S1);
/* 427 */           this.optimizationBestSolChange = true;
/* 428 */         } else if (temperatureAcceptance > temprand)
/*     */         {
/* 430 */           this.S0.clone(this.S1);
/* 431 */           this.optimizationBestSolChange = true;
/*     */         }
/*     */ 
/* 435 */         if (this.trackPerf) this.monitor.trackPerformances("Compare S1 to S0 and Best solution, and update if necessary", 1);
/* 436 */         this.operators.nextOperator();
/* 437 */         if ((this.optimizationBestSolChange) && (
/* 438 */           ((this.optimization == Parameters.Optimization.STOCH) && (Math.random() < this.optimizationUse)) || (
/* 439 */           (this.optimization == Parameters.Optimization.DISC) && (this.step % (int)this.optimizationUse == 0)))) {
/* 440 */           this.monitor.showStageOptimization(1, "current solution");
/* 441 */           if (this.trackPerf) this.monitor.trackPerformances("Optimize current solution with " + this.optimizationAlgorithm, 1);
/* 442 */           this.S0 = this.P.getOptimizer(this.S0).getOptimizedTree();
/* 443 */           this.optimizationBestSolChange = false;
/* 444 */           if (this.trackPerf) this.monitor.trackPerformances("Optimize current solution with " + this.optimizationAlgorithm, 1);
/* 445 */           this.monitor.showStageOptimization(0, "current solution");
/*     */         }
/* 447 */         if ((this.sufficientStop.contains(Parameters.HeuristicStopCondition.AUTO)) || (this.necessaryStop.contains(Parameters.HeuristicStopCondition.AUTO))) {
/* 448 */           if (lastBestSolution - this.bestSolution.getEvaluation() < lastBestSolution * this.stopCriterionAutoThreshold)
/* 449 */             noLikelihoodChangeStop++;
/*     */           else {
/* 451 */             noLikelihoodChangeStop = 0;
/*     */           }
/* 453 */           lastBestSolution = this.bestSolution.getEvaluation();
/*     */         }
/* 455 */         currentTime = System.currentTimeMillis();
/* 456 */         coolingCount++;
/* 457 */         if (this.trackPerf) this.monitor.trackPerformances("Simulated Annealing step " + this.step, 0);
/*     */       }
/* 459 */       if ((this.optimization != Parameters.Optimization.NEVER) && (this.optimization != Parameters.Optimization.CONSENSUSTREE)) {
/* 460 */         this.monitor.showStageOptimization(1, "best solution");
/* 461 */         if (this.trackPerf) this.monitor.trackPerformances("Optimize best solution with " + this.optimizationAlgorithm, 0);
/* 462 */         this.bestSolution = this.P.getOptimizer(this.bestSolution).getOptimizedTree();
/* 463 */         if (this.trackPerf) this.monitor.trackPerformances("Optimize best solution with " + this.optimizationAlgorithm, 0);
/* 464 */         this.monitor.showStageOptimization(0, "best solution");
/*     */       }
/* 466 */       List solTree = new ArrayList();
/* 467 */       solTree.add(this.bestSolution);
/* 468 */       evaluationsToMonitor.put("Best solution", Double.valueOf(this.bestSolution.getEvaluation()));
/* 469 */       evaluationsToMonitor.put("Current solution", Double.valueOf(this.S0.getEvaluation()));
/* 470 */       if (this.trackTrees) this.monitor.printEndTreesHeuristic();
/* 471 */       this.operators.printStatistics();
/* 472 */       this.monitor.showStageSearchStop(solTree, evaluationsToMonitor);
/*     */     } catch (OutOfMemoryError e) {
/* 474 */       this.monitor.endFromException(new Exception("Out of memory: please, assign more RAM to MetaPIGA. You can easily do so by using the menu 'Tools --> Memory settings'."));
/*     */     } catch (Exception e) {
/* 476 */       this.monitor.endFromException(e);
/*     */     }
/*     */ 
/* 479 */     if (this.P.getLikelihoodCalculationType() == Parameters.LikelihoodCalculationType.GPU)
/* 480 */       this.videocard.freeMemory();
/*     */   }
/*     */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.SimulatedAnnealing
 * JD-Core Version:    0.6.2
 */