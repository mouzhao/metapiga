/*     */ package metapiga.monitors;
/*     */ 
/*     */ import java.io.IOException;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */
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
/*     */ import metapiga.trees.Consensus;
/*     */ import metapiga.trees.Tree;
/*     */ import metapiga.trees.exceptions.NullAncestorException;
/*     */ import metapiga.trees.exceptions.UnrootableTreeException;
/*     */ 
/*     */ public class SearchSilentMonitor
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
/*     */   private SearchSilent parent;
/*     */   private Heuristic H;
/*     */   private Thread thread;
/*  34 */   private int currentReplicate = 1;
/*     */   private Parameters parameters;
/*     */   final PrintMonitor print;
/*     */ 
/*     */   public SearchSilentMonitor(SearchSilent parent, Parameters parameters, String runLabel)
/*     */   {
/*  39 */     this.parent = parent;
/*  40 */     this.parameters = parameters;
/*  41 */     this.DATA = parameters.logFiles.contains(Parameters.LogFile.DATA);
/*  42 */     this.DIST = parameters.logFiles.contains(Parameters.LogFile.DIST);
/*  43 */     this.TREESTART = parameters.logFiles.contains(Parameters.LogFile.TREESTART);
/*  44 */     this.HEUR = parameters.logFiles.contains(Parameters.LogFile.HEUR);
/*  45 */     this.TREEHEUR = parameters.logFiles.contains(Parameters.LogFile.TREEHEUR);
/*  46 */     this.CONSENSUS = parameters.logFiles.contains(Parameters.LogFile.CONSENSUS);
/*  47 */     this.OPDETAILS = parameters.logFiles.contains(Parameters.LogFile.OPDETAILS);
/*  48 */     this.OPSTATS = parameters.logFiles.contains(Parameters.LogFile.OPSTATS);
/*  49 */     this.ANCSEQ = parameters.logFiles.contains(Parameters.LogFile.ANCSEQ);
/*  50 */     this.PERF = parameters.logFiles.contains(Parameters.LogFile.PERF);
/*  51 */     this.print = new PrintMonitor(this, runLabel);
/*  52 */     this.print.setParameters(parameters);
/*     */   }
/*     */ 
/*     */   public Monitor.MonitorType getMonitorType()
/*     */   {
/*  57 */     return Monitor.MonitorType.SILENT;
/*     */   }
/*     */   public final boolean trackDataMatrix() {
/*  60 */     return this.DATA; } 
/*  61 */   public final boolean trackDistances() { return this.DIST; } 
/*  62 */   public final boolean trackStartingTree() { return this.TREESTART; } 
/*  63 */   public final boolean trackHeuristic() { return this.HEUR; } 
/*  64 */   public final boolean trackHeuristicTrees() { return this.TREEHEUR; } 
/*  65 */   public final boolean trackConsensus() { return this.CONSENSUS; } 
/*  66 */   public final boolean trackOperators() { return this.OPDETAILS; } 
/*  67 */   public final boolean trackOperatorStats() { return this.OPSTATS; } 
/*  68 */   public final boolean trackPerformances() { return this.PERF; } 
/*  69 */   public final boolean trackAncestralSequences() { return this.ANCSEQ; }
/*     */ 
/*     */   public void run()
/*     */   {
/*     */     try {
/*  74 */       if (trackDataMatrix()) printDataMatrix();
/*  75 */       while ((this.currentReplicate = this.parent.getNextReplicate()) > 0) {
/*  76 */         this.print.initLogFiles(this.currentReplicate);
/*  77 */         switch ($SWITCH_TABLE$metapiga$parameters$Parameters$Heuristic()[this.parameters.heuristic.ordinal()]) {
/*     */         case 1:
/*  79 */           this.H = new HillClimbing(this.parameters, this);
/*  80 */           break;
/*     */         case 2:
/*  82 */           this.H = new SimulatedAnnealing(this.parameters, this);
/*  83 */           break;
/*     */         case 3:
/*  85 */           this.H = new GeneticAlgorithm(this.parameters, this);
/*  86 */           break;
/*     */         case 4:
/*  88 */           this.H = new ConsensusPruning(this.parameters, this);
/*  89 */           break;
/*     */         case 5:
/*  91 */           this.H = new Bootstrapping(this.parameters, this);
/*     */         }
/*     */ 
/*  94 */         this.thread = new Thread(this.H, this.H.getName(true) + "-Rep-" + this.currentReplicate);
/*  95 */         this.thread.start();
/*  96 */         this.thread.join();
/*     */         try {
/*  98 */           this.print.closeOutputFiles();
/*     */         } catch (IOException e) {
/* 100 */           e.printStackTrace();
/*     */         }
/*     */       }
/*     */     } catch (Exception e) {
/* 104 */       endFromException(e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void end(List<Tree> solutionTrees)
/*     */   {
/* 111 */     if (this.parameters.hasManyReplicates()) {
/* 112 */       for (Tree tree : solutionTrees) {
/* 113 */         tree.setName(tree.getName() + "_Rep_" + this.currentReplicate);
/*     */       }
/*     */     }
/*     */ 
/* 117 */     if (this.ANCSEQ) printAncestralSequences(solutionTrees);
/* 118 */     for (Tree tree : solutionTrees) {
/* 119 */       tree.deleteLikelihoodComputation();
/*     */     }
/* 121 */     this.parent.addSolutionTree(solutionTrees);
/*     */   }
/*     */ 
/*     */   public void endFromException(Exception e)
/*     */   {
/* 126 */     this.parent.endFromException(e);
/*     */   }
/*     */ 
/*     */   public void printDataMatrix() {
/* 130 */     this.print.printDataMatrix();
/*     */   }
/*     */ 
/*     */   public void printDistanceMatrix(DistanceMatrix dm) {
/* 134 */     this.print.printDistanceMatrix(dm);
/*     */   }
/*     */ 
/*     */   public void printStartingTrees(List<Tree> startingTrees) {
/* 138 */     this.print.printStartingTrees(startingTrees, this.currentReplicate);
/*     */   }
/*     */ 
/*     */   public void printTreeBeforeOperator(Tree tree, Parameters.Operator operator, boolean consensus) {
/* 142 */     this.print.printTreeBeforeOperator(tree, operator, consensus);
/*     */   }
/*     */ 
/*     */   public void printOperatorInfos(Tree tree, String infos) {
/* 146 */     this.print.printOperatorInfos(tree, infos);
/*     */   }
/*     */ 
/*     */   public void printOperatorInfos(Tree tree, String infos, Consensus consensus) {
/* 150 */     this.print.printOperatorInfos(tree, infos, consensus);
/*     */   }
/*     */ 
/*     */   public void printTreeAfterOperator(Tree tree, Parameters.Operator operator, boolean consensus) {
/* 154 */     this.print.printTreeAfterOperator(tree, operator, consensus);
/*     */   }
/*     */ 
/*     */   public void printOperatorFrequenciesUpdate(int currentStep, Map<Parameters.Operator, Integer> use, Map<Parameters.Operator, Double> scoreImprovements, Map<Parameters.Operator, Long> performances) {
/* 158 */     this.print.printOperatorFrequenciesUpdate(currentStep, use, scoreImprovements, performances);
/*     */   }
/*     */ 
/*     */   public void printOperatorFrequenciesUpdate(Map<Parameters.Operator, Double> frequencies) {
/* 162 */     this.print.printOperatorFrequenciesUpdate(frequencies);
/*     */   }
/*     */ 
/*     */   public void printOperatorStatistics(int numStep, Map<Parameters.Operator, Integer> use, Map<Parameters.Operator, Double> scoreImprovements, Map<Parameters.Operator, Long> performances, int outgroupTargeted, int ingroupTargeted, Map<Parameters.Operator, Map<Integer, Integer>> cancelByConsensus)
/*     */   {
/* 167 */     this.print.printOperatorStatistics(numStep, use, scoreImprovements, performances, outgroupTargeted, ingroupTargeted, cancelByConsensus);
/*     */   }
/*     */ 
/*     */   public void printDetailsHC(int step, double bestLikelihood, double currentLikelihood, Parameters.Operator operator, boolean improvement) {
/* 171 */     this.print.printDetailsHC(step, bestLikelihood, currentLikelihood, operator, improvement);
/*     */   }
/*     */ 
/*     */   public void printDetailsSA(int step, double bestLikelihood, double S0Likelihood, double currentLikelihood, Parameters.Operator operator, String status, double tempAcceptance, double temperature, int coolingSteps, int successes, int failures, int reheatingDecrements)
/*     */   {
/* 176 */     this.print.printDetailsSA(step, bestLikelihood, S0Likelihood, currentLikelihood, operator, status, tempAcceptance, temperature, coolingSteps, successes, failures, reheatingDecrements);
/*     */   }
/*     */ 
/*     */   public void printDetailsGA(int step, Tree[] mutantML, Parameters.Operator[] operator) {
/* 180 */     this.print.printDetailsGA(step, mutantML, operator);
/*     */   }
/*     */ 
/*     */   public void printDetailsGA(int step, String selectionDetails, Tree[] selectedML, double bestLikelihood) {
/* 184 */     this.print.printDetailsGA(step, selectionDetails, selectedML, bestLikelihood);
/*     */   }
/*     */ 
/*     */   public void printDetailsCP(Tree[] mutantML, Parameters.Operator[] operator, int currentPop) {
/* 188 */     this.print.printDetailsCP(mutantML, operator, currentPop);
/*     */   }
/*     */ 
/*     */   public void printDetailsCP(Tree[] hybridML, String[] parents, int currentPop) {
/* 192 */     this.print.printDetailsCP(hybridML, parents, currentPop);
/*     */   }
/*     */ 
/*     */   public void printDetailsCP(int step, String[] selectionDetails, Tree[][] selectedML, double bestLikelihood) {
/* 196 */     this.print.printDetailsCP(step, selectionDetails, selectedML, bestLikelihood);
/*     */   }
/*     */ 
/*     */   public void printTreesHC(int step, Tree bestTree, Tree currentTree) {
/* 200 */     this.print.printTreesHC(step, bestTree, currentTree);
/*     */   }
/*     */ 
/*     */   public void printTreesSA(int step, Tree bestTree, Tree S0Tree, Tree currentTree) {
/* 204 */     this.print.printTreesSA(step, bestTree, S0Tree, currentTree);
/*     */   }
/*     */ 
/*     */   public void printTreesGA(int step, Tree[] trees, boolean selectionDone) {
/* 208 */     this.print.printTreesGA(step, trees, selectionDone);
/*     */   }
/*     */ 
/*     */   public void printTreesCP(int step, Tree[] trees, int pop, boolean recombined) {
/* 212 */     this.print.printTreesCP(step, trees, pop, recombined);
/*     */   }
/*     */ 
/*     */   public void printTreesCP(int step, Tree[][] trees) {
/* 216 */     this.print.printTreesCP(step, trees);
/*     */   }
/*     */ 
/*     */   public void printEndTreesHeuristic() {
/* 220 */     this.print.printEndTreesHeuristic();
/*     */   }
/*     */ 
/*     */   public void printConsensus(int step, Consensus consensus) {
/* 224 */     this.print.printConsensus(step, consensus);
/*     */   }
/*     */ 
/*     */   public void trackPerformances(String action, int level) {
/* 228 */     this.print.trackPerformances(action, level);
/*     */   }
/*     */ 
/*     */   public void printAncestralSequences(List<Tree> trees) {
/* 232 */     this.print.printAncestralSequences(trees);
/*     */   }
/*     */ 
/*     */   public void updateConsensusTree(Tree consensusTree) {
/* 236 */     this.print.updateConsensusTree(consensusTree);
/*     */   }
/*     */ 
/*     */   public double getBestLikelihood() {
/*     */     try {
/* 241 */       return this.H.getBestSolution().getEvaluation();
/*     */     } catch (UnrootableTreeException e) {
/* 243 */       e.printStackTrace();
/*     */     } catch (NullAncestorException e) {
/* 245 */       e.printStackTrace();
/*     */     }
/* 247 */     return (0.0D / 0.0D);
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
/*     */   }
/*     */ 
/*     */   public void showRemainingTime(long time)
/*     */   {
/*     */   }
/*     */ 
/*     */   public void showReplicate()
/*     */   {
/*     */   }
/*     */ 
/*     */   public void showStageCPMetapopulation(int numOfSteps)
/*     */   {
/*     */   }
/*     */ 
/*     */   public void showStageDistanceMatrix(int numOfSteps)
/*     */   {
/*     */   }
/*     */ 
/*     */   public void showStageGAPopulation(int numOfSteps)
/*     */   {
/*     */   }
/*     */ 
/*     */   public void showStageOptimization(int active, String target)
/*     */   {
/*     */   }
/*     */ 
/*     */   public void showStageSATemperature(int numOfSteps)
/*     */   {
/*     */   }
/*     */ 
/*     */   public void showStageHCRestart()
/*     */   {
/*     */   }
/*     */ 
/*     */   public synchronized void showStageSearchProgress(int currentIteration, long remainingTime, int noChangeSteps)
/*     */   {
/*     */   }
/*     */ 
/*     */   public void showStageSearchStart(String heuristic, int maxSteps, double startingEvaluation)
/*     */   {
/*     */   }
/*     */ 
/*     */   public void showStageSearchStop(List<Tree> solutionTrees, Map<String, Double> evaluationsToShow)
/*     */   {
/* 327 */     end(solutionTrees);
/*     */   }
/*     */ 
/*     */   public void showStageStartingTree(int numOfSteps)
/*     */   {
/*     */   }
/*     */ 
/*     */   public void showStartingTree(Tree tree)
/*     */   {
/*     */   }
/*     */ 
/*     */   public void showNextStep()
/*     */   {
/*     */   }
/*     */ 
/*     */   public void showAutoStopDone(int noChangeSteps)
/*     */   {
/*     */   }
/*     */ 
/*     */   public void showText(String text)
/*     */   {
/*     */   }
/*     */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.SearchSilentMonitor
 * JD-Core Version:    0.6.2
 */