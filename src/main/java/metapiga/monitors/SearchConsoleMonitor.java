/*     */ package metapiga.monitors;
/*     */ 
/*     */ import java.io.IOException;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Date;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */
/*     */ import metapiga.ProgressHandling;
/*     */ import metapiga.heuristics.Bootstrapping;
/*     */ import metapiga.heuristics.ConsensusPruning;
/*     */ import metapiga.heuristics.GeneticAlgorithm;
/*     */ import metapiga.heuristics.Heuristic;
/*     */ import metapiga.heuristics.HillClimbing;
/*     */ import metapiga.heuristics.SimulatedAnnealing;
/*     */ import metapiga.modelization.DistanceMatrix;
/*     */ import metapiga.parameters.Parameters;
/*     */
/*     */
/*     */
/*     */
/*     */ import metapiga.trees.Consensus;
/*     */ import metapiga.trees.Tree;
/*     */ import metapiga.trees.exceptions.NullAncestorException;
/*     */ import metapiga.trees.exceptions.UnrootableTreeException;
/*     */ import metapiga.utilities.Tools;
/*     */ 
/*     */ public class SearchConsoleMonitor
/*     */   implements Monitor
/*     */ {
/*     */   private boolean DATA;
/*     */   private boolean DIST;
/*     */   private boolean TREESTART;
/*     */   private boolean HEUR;
/*     */   private boolean TREEHEUR;
/*     */   private boolean CONSENSUS;
/*     */   private boolean OPDETAILS;
/*     */   private boolean OPSTATS;
/*     */   private boolean ANCSEQ;
/*     */   private boolean PERF;
/*     */   private SearchConsole parent;
/*     */   private Heuristic H;
/*     */   private Thread thread;
/*  39 */   private int maxSteps = 0;
/*     */   int currentStep;
/*  41 */   private int currentReplicate = 1;
/*     */   private long repStartTime;
/*     */   private Parameters parameters;
/*     */   final PrintMonitor print;
/*     */   private final ProgressHandling progress;
/*     */   private final int progressId;
/*     */ 
/*     */   public SearchConsoleMonitor(SearchConsole parent, ProgressHandling progress, int progressId, Parameters parameters, String runLabel)
/*     */   {
/*  50 */     this.parent = parent;
/*  51 */     this.progress = progress;
/*  52 */     this.progressId = progressId;
/*  53 */     this.parameters = parameters;
/*  54 */     this.DATA = parameters.logFiles.contains(Parameters.LogFile.DATA);
/*  55 */     this.DIST = parameters.logFiles.contains(Parameters.LogFile.DIST);
/*  56 */     this.TREESTART = parameters.logFiles.contains(Parameters.LogFile.TREESTART);
/*  57 */     this.HEUR = parameters.logFiles.contains(Parameters.LogFile.HEUR);
/*  58 */     this.TREEHEUR = parameters.logFiles.contains(Parameters.LogFile.TREEHEUR);
/*  59 */     this.CONSENSUS = parameters.logFiles.contains(Parameters.LogFile.CONSENSUS);
/*  60 */     this.OPDETAILS = parameters.logFiles.contains(Parameters.LogFile.OPDETAILS);
/*  61 */     this.OPSTATS = parameters.logFiles.contains(Parameters.LogFile.OPSTATS);
/*  62 */     this.ANCSEQ = parameters.logFiles.contains(Parameters.LogFile.ANCSEQ);
/*  63 */     this.PERF = parameters.logFiles.contains(Parameters.LogFile.PERF);
/*  64 */     this.print = new PrintMonitor(this, runLabel);
/*  65 */     this.print.setParameters(parameters);
/*     */   }
/*     */ 
/*     */   public Monitor.MonitorType getMonitorType()
/*     */   {
/*  70 */     return Monitor.MonitorType.CONSOLE;
/*     */   }
/*     */   public final boolean trackDataMatrix() {
/*  73 */     return this.DATA; } 
/*  74 */   public final boolean trackDistances() { return this.DIST; } 
/*  75 */   public final boolean trackStartingTree() { return this.TREESTART; } 
/*  76 */   public final boolean trackHeuristic() { return this.HEUR; } 
/*  77 */   public final boolean trackHeuristicTrees() { return this.TREEHEUR; } 
/*  78 */   public final boolean trackConsensus() { return this.CONSENSUS; } 
/*  79 */   public final boolean trackOperators() { return this.OPDETAILS; } 
/*  80 */   public final boolean trackOperatorStats() { return this.OPSTATS; } 
/*  81 */   public final boolean trackPerformances() { return this.PERF; } 
/*  82 */   public final boolean trackAncestralSequences() { return this.ANCSEQ; }
/*     */ 
/*     */   public void run()
/*     */   {
/*     */     try {
/*  87 */       if (trackDataMatrix()) printDataMatrix();
/*     */       int j;
/*     */       int i;
/*  88 */       for (; (this.currentReplicate = this.parent.getNextReplicate()) > 0; 
/* 116 */         i < j)
/*     */       {
/*  89 */         this.print.initLogFiles(this.currentReplicate);
/*  90 */         switch ($SWITCH_TABLE$metapiga$parameters$Parameters$Heuristic()[this.parameters.heuristic.ordinal()]) {
/*     */         case 1:
/*  92 */           this.H = new HillClimbing(this.parameters, this);
/*  93 */           break;
/*     */         case 2:
/*  95 */           this.H = new SimulatedAnnealing(this.parameters, this);
/*  96 */           break;
/*     */         case 3:
/*  98 */           this.H = new GeneticAlgorithm(this.parameters, this);
/*  99 */           break;
/*     */         case 4:
/* 101 */           this.H = new ConsensusPruning(this.parameters, this);
/* 102 */           break;
/*     */         case 5:
/* 104 */           this.H = new Bootstrapping(this.parameters, this); } 
/*     */ this.thread = new Thread(this.H, this.H.getName(true) + "-Rep-" + this.currentReplicate);
/* 108 */         this.thread.start();
/* 109 */         this.thread.join();
/*     */         StackTraceElement[] arrayOfStackTraceElement;
/*     */         try { this.print.closeOutputFiles();
/*     */         } catch (IOException e) {
/* 113 */           e.printStackTrace();
/* 114 */           showText("\n Error when closing log files");
/* 115 */           showText("\n Java exception : " + e.getCause() + " (" + e.getMessage() + ")");
/* 116 */           j = (arrayOfStackTraceElement = e.getStackTrace()).length; i = 0; continue; } StackTraceElement el = arrayOfStackTraceElement[i];
/* 117 */         showText("\tat " + el.toString());
/*     */ 
/* 116 */         i++;
/*     */       }
/*     */ 
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 122 */       endFromException(e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void end(List<Tree> solutionTrees)
/*     */   {
/* 129 */     if (this.parameters.hasManyReplicates()) {
/* 130 */       for (Tree tree : solutionTrees) {
/* 131 */         tree.setName(tree.getName() + "_Rep_" + this.currentReplicate);
/*     */       }
/*     */     }
/*     */ 
/* 135 */     if (this.ANCSEQ) printAncestralSequences(solutionTrees);
/* 136 */     for (Tree tree : solutionTrees) {
/* 137 */       tree.deleteLikelihoodComputation();
/*     */     }
/* 139 */     this.parent.addSolutionTree(solutionTrees);
/*     */   }
/*     */ 
/*     */   public void endFromException(Exception e)
/*     */   {
/* 144 */     this.parent.endFromException(e);
/*     */   }
/*     */ 
/*     */   public double getBestLikelihood() {
/*     */     try {
/* 149 */       return this.H.getBestSolution().getEvaluation();
/*     */     } catch (UnrootableTreeException e) {
/* 151 */       e.printStackTrace();
/*     */     } catch (NullAncestorException e) {
/* 153 */       e.printStackTrace();
/*     */     }
/* 155 */     return (0.0D / 0.0D);
/*     */   }
/*     */ 
/*     */   public void printDataMatrix() {
/* 159 */     this.print.printDataMatrix();
/*     */   }
/*     */ 
/*     */   public void printDistanceMatrix(DistanceMatrix dm) {
/* 163 */     this.print.printDistanceMatrix(dm);
/*     */   }
/*     */ 
/*     */   public void printStartingTrees(List<Tree> startingTrees) {
/* 167 */     this.print.printStartingTrees(startingTrees, this.currentReplicate);
/*     */   }
/*     */ 
/*     */   public void printTreeBeforeOperator(Tree tree, Parameters.Operator operator, boolean consensus) {
/* 171 */     this.print.printTreeBeforeOperator(tree, operator, consensus);
/*     */   }
/*     */ 
/*     */   public void printOperatorInfos(Tree tree, String infos) {
/* 175 */     this.print.printOperatorInfos(tree, infos);
/*     */   }
/*     */ 
/*     */   public void printOperatorInfos(Tree tree, String infos, Consensus consensus) {
/* 179 */     this.print.printOperatorInfos(tree, infos, consensus);
/*     */   }
/*     */ 
/*     */   public void printTreeAfterOperator(Tree tree, Parameters.Operator operator, boolean consensus) {
/* 183 */     this.print.printTreeAfterOperator(tree, operator, consensus);
/*     */   }
/*     */ 
/*     */   public void printOperatorFrequenciesUpdate(int currentStep, Map<Parameters.Operator, Integer> use, Map<Parameters.Operator, Double> scoreImprovements, Map<Parameters.Operator, Long> performances) {
/* 187 */     this.print.printOperatorFrequenciesUpdate(currentStep, use, scoreImprovements, performances);
/*     */   }
/*     */ 
/*     */   public void printOperatorFrequenciesUpdate(Map<Parameters.Operator, Double> frequencies) {
/* 191 */     this.print.printOperatorFrequenciesUpdate(frequencies);
/*     */   }
/*     */ 
/*     */   public void printOperatorStatistics(int numStep, Map<Parameters.Operator, Integer> use, Map<Parameters.Operator, Double> scoreImprovements, Map<Parameters.Operator, Long> performances, int outgroupTargeted, int ingroupTargeted, Map<Parameters.Operator, Map<Integer, Integer>> cancelByConsensus)
/*     */   {
/* 196 */     this.print.printOperatorStatistics(numStep, use, scoreImprovements, performances, outgroupTargeted, ingroupTargeted, cancelByConsensus);
/*     */   }
/*     */ 
/*     */   public void printDetailsHC(int step, double bestLikelihood, double currentLikelihood, Parameters.Operator operator, boolean improvement) {
/* 200 */     this.print.printDetailsHC(step, bestLikelihood, currentLikelihood, operator, improvement);
/*     */   }
/*     */ 
/*     */   public void printDetailsSA(int step, double bestLikelihood, double S0Likelihood, double currentLikelihood, Parameters.Operator operator, String status, double tempAcceptance, double temperature, int coolingSteps, int successes, int failures, int reheatingDecrements)
/*     */   {
/* 205 */     this.print.printDetailsSA(step, bestLikelihood, S0Likelihood, currentLikelihood, operator, status, tempAcceptance, temperature, coolingSteps, successes, failures, reheatingDecrements);
/*     */   }
/*     */ 
/*     */   public void printDetailsGA(int step, Tree[] mutantML, Parameters.Operator[] operator) {
/* 209 */     this.print.printDetailsGA(step, mutantML, operator);
/*     */   }
/*     */ 
/*     */   public void printDetailsGA(int step, String selectionDetails, Tree[] selectedML, double bestLikelihood) {
/* 213 */     this.print.printDetailsGA(step, selectionDetails, selectedML, bestLikelihood);
/*     */   }
/*     */ 
/*     */   public void printDetailsCP(Tree[] mutantML, Parameters.Operator[] operator, int currentPop) {
/* 217 */     this.print.printDetailsCP(mutantML, operator, currentPop);
/*     */   }
/*     */ 
/*     */   public void printDetailsCP(Tree[] hybridML, String[] parents, int currentPop) {
/* 221 */     this.print.printDetailsCP(hybridML, parents, currentPop);
/*     */   }
/*     */ 
/*     */   public void printDetailsCP(int step, String[] selectionDetails, Tree[][] selectedML, double bestLikelihood) {
/* 225 */     this.print.printDetailsCP(step, selectionDetails, selectedML, bestLikelihood);
/*     */   }
/*     */ 
/*     */   public void printTreesHC(int step, Tree bestTree, Tree currentTree) {
/* 229 */     this.print.printTreesHC(step, bestTree, currentTree);
/*     */   }
/*     */ 
/*     */   public void printTreesSA(int step, Tree bestTree, Tree S0Tree, Tree currentTree) {
/* 233 */     this.print.printTreesSA(step, bestTree, S0Tree, currentTree);
/*     */   }
/*     */ 
/*     */   public void printTreesGA(int step, Tree[] trees, boolean selectionDone) {
/* 237 */     this.print.printTreesGA(step, trees, selectionDone);
/*     */   }
/*     */ 
/*     */   public void printTreesCP(int step, Tree[] trees, int pop, boolean recombined) {
/* 241 */     this.print.printTreesCP(step, trees, pop, recombined);
/*     */   }
/*     */ 
/*     */   public void printTreesCP(int step, Tree[][] trees) {
/* 245 */     this.print.printTreesCP(step, trees);
/*     */   }
/*     */ 
/*     */   public void printEndTreesHeuristic() {
/* 249 */     this.print.printEndTreesHeuristic();
/*     */   }
/*     */ 
/*     */   public void printConsensus(int step, Consensus consensus) {
/* 253 */     this.print.printConsensus(step, consensus);
/*     */   }
/*     */ 
/*     */   public void trackPerformances(String action, int level) {
/* 257 */     this.print.trackPerformances(action, level);
/*     */   }
/*     */ 
/*     */   public void printAncestralSequences(List<Tree> trees) {
/* 261 */     this.print.printAncestralSequences(trees);
/*     */   }
/*     */ 
/*     */   public void updateConsensusTree(Tree consensusTree) {
/* 265 */     this.print.updateConsensusTree(consensusTree);
/*     */   }
/*     */ 
/*     */   public void showCurrentCoolingSchedule(Parameters.SASchedule currentCoolingSchedule)
/*     */   {
/*     */   }
/*     */ 
/*     */   public void showCurrentTemperature(double currentTemperature)
/*     */   {
/*     */   }
/*     */ 
/*     */   public void showCurrentMRE(double currentMRE)
/*     */   {
/*     */   }
/*     */ 
/*     */   public void showCurrentTree(Tree tree)
/*     */   {
/*     */   }
/*     */ 
/*     */   public void showEvaluations(Map<String, Double> evaluationsToShow)
/*     */   {
/* 290 */     this.progress.setText(this.progressId, "Best ML : " + Tools.doubletoString(((Double)evaluationsToShow.get("Best solution")).doubleValue(), 4));
/*     */   }
/*     */ 
/*     */   public void showRemainingTime(long time)
/*     */   {
/* 295 */     this.progress.setTime(this.progressId, time);
/*     */   }
/*     */ 
/*     */   public void showReplicate()
/*     */   {
/*     */   }
/*     */ 
/*     */   public void showStageCPMetapopulation(int numOfSteps)
/*     */   {
/* 305 */     this.progress.newMultiProgress(this.progressId, 0, numOfSteps, "Creating metapopulation");
/*     */   }
/*     */ 
/*     */   public void showStageDistanceMatrix(int numOfSteps)
/*     */   {
/* 310 */     this.progress.newMultiProgress(this.progressId, 0, numOfSteps, "Building distance matrix");
/*     */   }
/*     */ 
/*     */   public void showStageGAPopulation(int numOfSteps)
/*     */   {
/* 315 */     this.progress.newMultiProgress(this.progressId, 0, numOfSteps, "Creating population");
/*     */   }
/*     */ 
/*     */   public void showStageOptimization(int active, String target)
/*     */   {
/* 320 */     if (active > 0)
/* 321 */       this.progress.setText(this.progressId, "Intra-step optimization of " + target);
/*     */   }
/*     */ 
/*     */   public void showStageSATemperature(int numOfSteps)
/*     */   {
/* 327 */     this.progress.newMultiProgress(this.progressId, 0, numOfSteps, "Setting temperature");
/*     */   }
/*     */ 
/*     */   public void showStageHCRestart()
/*     */   {
/*     */   }
/*     */ 
/*     */   public synchronized void showStageSearchProgress(int currentIteration, long remainingTime, int noChangeSteps)
/*     */   {
/* 337 */     if ((this.parameters.sufficientStopConditions.contains(Parameters.HeuristicStopCondition.STEPS)) || 
/* 338 */       (this.parameters.necessaryStopConditions.contains(Parameters.HeuristicStopCondition.STEPS))) showNextStep();
/* 339 */     if ((this.parameters.sufficientStopConditions.contains(Parameters.HeuristicStopCondition.TIME)) || 
/* 340 */       (this.parameters.necessaryStopConditions.contains(Parameters.HeuristicStopCondition.TIME))) showRemainingTime(remainingTime);
/*     */   }
/*     */ 
/*     */   public void showStageSearchStart(String heuristic, int maxSteps, double startingEvaluation)
/*     */   {
/* 346 */     this.repStartTime = System.currentTimeMillis();
/* 347 */     List text = new ArrayList();
/* 348 */     if ((this.parameters.sufficientStopConditions.isEmpty()) && (this.parameters.necessaryStopConditions.isEmpty()))
/* 349 */       text.add("Don't start " + heuristic + " as user commands");
/*     */     else {
/* 351 */       text.add(heuristic + (this.parameters.hasManyReplicates() ? " (Replicate " + this.currentReplicate + ")" : "") + " started at " + new Date(this.repStartTime).toString());
/*     */     }
/* 353 */     if (!this.parameters.sufficientStopConditions.isEmpty()) {
/* 354 */       text.add("Stop when ANY of the following sufficient conditions is met: \n");
/* 355 */       for (Parameters.HeuristicStopCondition condition : this.parameters.sufficientStopConditions) {
/* 356 */         switch (condition) {
/*     */         case AUTO:
/* 358 */           text.add("- " + this.parameters.stopCriterionSteps + " iterations performed" + "\n");
/* 359 */           break;
/*     */         case CONSENSUS:
/* 361 */           text.add("- Pass over " + new Date(this.repStartTime + ()this.parameters.stopCriterionTime * 3600L * 1000L).toString() + "\n");
/* 362 */           break;
/*     */         case STEPS:
/* 364 */           text.add("- Likelihood stop increasing after " + this.parameters.stopCriterionAutoSteps + " iterations" + "\n");
/* 365 */           break;
/*     */         case TIME:
/* 367 */           text.add("- Mean relative error of " + this.parameters.stopCriterionConsensusInterval + " consecutive consensus trees stay below " + Tools.doubleToPercent(this.parameters.stopCriterionConsensusMRE, 0) + " using trees sampled every " + this.parameters.stopCriterionConsensusGeneration + " generations" + "\n");
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 372 */     if (!this.parameters.necessaryStopConditions.isEmpty()) {
/* 373 */       text.add("Stop when ALL of the following necessary conditions are met: \n");
/* 374 */       for (Parameters.HeuristicStopCondition condition : this.parameters.necessaryStopConditions) {
/* 375 */         switch (condition) {
/*     */         case AUTO:
/* 377 */           text.add("- " + this.parameters.stopCriterionSteps + " iterations performed" + "\n");
/* 378 */           break;
/*     */         case CONSENSUS:
/* 380 */           text.add("- Pass over " + new Date(this.repStartTime + ()this.parameters.stopCriterionTime * 3600L * 1000L).toString() + "\n");
/* 381 */           break;
/*     */         case STEPS:
/* 383 */           text.add("- Likelihood stop increasing after " + this.parameters.stopCriterionAutoSteps + " iterations" + "\n");
/* 384 */           break;
/*     */         case TIME:
/* 386 */           text.add("- Mean relative error of " + this.parameters.stopCriterionConsensusInterval + " consecutive consensus trees stay below " + Tools.doubleToPercent(this.parameters.stopCriterionConsensusMRE, 0) + " using trees sampled every " + this.parameters.stopCriterionConsensusGeneration + " generations" + "\n");
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 391 */     text.add("");
/* 392 */     this.progress.displayEndMessage(text);
/* 393 */     if (this.parameters.stopCriterionSteps > 0)
/* 394 */       this.maxSteps = maxSteps;
/*     */     else {
/* 396 */       this.maxSteps = 0;
/*     */     }
/* 398 */     this.currentStep = 0;
/* 399 */     this.progress.newSearchProgress(this.progressId, this.maxSteps, ()(this.parameters.stopCriterionTime * 3600.0D * 1000.0D), Tools.doubletoString(startingEvaluation, 4));
/*     */   }
/*     */ 
/*     */   public void showStageSearchStop(List<Tree> solutionTrees, Map<String, Double> evaluationsToShow)
/*     */   {
/* 405 */     if ((this.parameters.sufficientStopConditions.contains(Parameters.HeuristicStopCondition.STEPS)) || 
/* 406 */       (this.parameters.necessaryStopConditions.contains(Parameters.HeuristicStopCondition.STEPS)))
/* 407 */       this.currentStep = (this.parameters.stopCriterionSteps - 1);
/* 408 */     showNextStep();
/* 409 */     if ((this.parameters.sufficientStopConditions.contains(Parameters.HeuristicStopCondition.TIME)) || 
/* 410 */       (this.parameters.necessaryStopConditions.contains(Parameters.HeuristicStopCondition.TIME)))
/* 411 */       showRemainingTime(0L);
/* 412 */     showEvaluations(evaluationsToShow);
/* 413 */     List text = new ArrayList();
/* 414 */     text.add(this.H.getName(true) + (this.parameters.hasManyReplicates() ? " (Replicate " + this.currentReplicate + ")" : "") + " finished at " + new Date(System.currentTimeMillis()).toString());
/* 415 */     text.add("End after " + Tools.doubletoString((System.currentTimeMillis() - this.repStartTime) / 60000.0D, 2) + " minutes");
/* 416 */     text.add("Best tree likelihood : " + Tools.doubletoString(((Double)evaluationsToShow.get("Best solution")).doubleValue(), 4) + "\n");
/* 417 */     this.progress.displayEndMessage(text);
/* 418 */     end(solutionTrees);
/*     */   }
/*     */ 
/*     */   public void showStageStartingTree(int numOfSteps)
/*     */   {
/* 423 */     this.progress.newMultiProgress(this.progressId, 0, numOfSteps, "Building starting tree");
/*     */   }
/*     */ 
/*     */   public void showStartingTree(Tree tree)
/*     */   {
/*     */   }
/*     */ 
/*     */   public void showNextStep()
/*     */   {
/* 433 */     this.progress.setValue(this.progressId, ++this.currentStep);
/*     */   }
/*     */ 
/*     */   public void showAutoStopDone(int noChangeSteps)
/*     */   {
/*     */   }
/*     */ 
/*     */   public void showText(String text)
/*     */   {
/* 443 */     this.parent.showText(text);
/*     */   }
/*     */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.SearchConsoleMonitor
 * JD-Core Version:    0.6.2
 */