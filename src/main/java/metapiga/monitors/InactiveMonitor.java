/*    */ package metapiga.monitors;
/*    */ 
/*    */ import java.util.List;
/*    */ import java.util.Map;
/*    */ import metapiga.modelization.DistanceMatrix;
/*    */
/*    */
/*    */ import metapiga.trees.Consensus;
/*    */ import metapiga.trees.Tree;
/*    */ 
/*    */ public class InactiveMonitor
/*    */   implements Monitor
/*    */ {
/*    */   public Monitor.MonitorType getMonitorType()
/*    */   {
/* 26 */     return Monitor.MonitorType.INACTIVE;
/*    */   }
/*    */   public boolean trackDataMatrix() {
/* 29 */     return false; } 
/* 30 */   public boolean trackDistances() { return false; } 
/* 31 */   public boolean trackStartingTree() { return false; } 
/* 32 */   public boolean trackHeuristic() { return false; } 
/* 33 */   public boolean trackHeuristicTrees() { return false; } 
/* 34 */   public boolean trackConsensus() { return false; } 
/* 35 */   public boolean trackOperators() { return false; } 
/* 36 */   public boolean trackOperatorStats() { return false; } 
/* 37 */   public boolean trackPerformances() { return false; } 
/* 38 */   public boolean trackAncestralSequences() { return false; }
/*    */ 
/*    */ 
/*    */   public void end(List<Tree> solutionTrees)
/*    */   {
/*    */   }
/*    */ 
/*    */   public void endFromException(Exception e)
/*    */   {
/*    */   }
/*    */ 
/*    */   public void printAncestralSequences(List<Tree> trees)
/*    */   {
/*    */   }
/*    */ 
/*    */   public void printConsensus(int step, Consensus consensus)
/*    */   {
/*    */   }
/*    */ 
/*    */   public void printDataMatrix()
/*    */   {
/*    */   }
/*    */ 
/*    */   public void printDetailsCP(Tree[] mutantML, Parameters.Operator[] operator, int currentPop)
/*    */   {
/*    */   }
/*    */ 
/*    */   public void printDetailsCP(Tree[] hybridML, String[] parents, int currentPop)
/*    */   {
/*    */   }
/*    */ 
/*    */   public void printDetailsCP(int step, String[] selectionDetails, Tree[][] selectedML, double bestLikelihood)
/*    */   {
/*    */   }
/*    */ 
/*    */   public void printDetailsGA(int step, Tree[] mutantML, Parameters.Operator[] operator)
/*    */   {
/*    */   }
/*    */ 
/*    */   public void printDetailsGA(int step, String selectionDetails, Tree[] selectedML, double bestLikelihood)
/*    */   {
/*    */   }
/*    */ 
/*    */   public void printDetailsHC(int step, double bestLikelihood, double currentLikelihood, Parameters.Operator operator, boolean improvement)
/*    */   {
/*    */   }
/*    */ 
/*    */   public void printDetailsSA(int step, double bestLikelihood, double S0Likelihood, double currentLikelihood, Parameters.Operator operator, String status, double tempAcceptance, double temperature, int coolingSteps, int successes, int failures, int reheatingSteps)
/*    */   {
/*    */   }
/*    */ 
/*    */   public void printDistanceMatrix(DistanceMatrix dm)
/*    */   {
/*    */   }
/*    */ 
/*    */   public void printEndTreesHeuristic()
/*    */   {
/*    */   }
/*    */ 
/*    */   public void printOperatorFrequenciesUpdate(int currentStep, Map<Parameters.Operator, Integer> use, Map<Parameters.Operator, Double> scoreImprovements, Map<Parameters.Operator, Long> performances)
/*    */   {
/*    */   }
/*    */ 
/*    */   public void printOperatorFrequenciesUpdate(Map<Parameters.Operator, Double> frequencies)
/*    */   {
/*    */   }
/*    */ 
/*    */   public void printOperatorInfos(Tree tree, String infos)
/*    */   {
/*    */   }
/*    */ 
/*    */   public void printOperatorInfos(Tree tree, String infos, Consensus consensus)
/*    */   {
/*    */   }
/*    */ 
/*    */   public void printOperatorStatistics(int numStep, Map<Parameters.Operator, Integer> use, Map<Parameters.Operator, Double> scoreImprovements, Map<Parameters.Operator, Long> performances, int outgroupTargeted, int ingroupTargeted, Map<Parameters.Operator, Map<Integer, Integer>> cancelByConsensus)
/*    */   {
/*    */   }
/*    */ 
/*    */   public void printStartingTrees(List<Tree> startingTrees)
/*    */   {
/*    */   }
/*    */ 
/*    */   public void printTreeAfterOperator(Tree tree, Parameters.Operator operator, boolean consensus)
/*    */   {
/*    */   }
/*    */ 
/*    */   public void printTreeBeforeOperator(Tree tree, Parameters.Operator operator, boolean consensus)
/*    */   {
/*    */   }
/*    */ 
/*    */   public void printTreesCP(int step, Tree[] trees, int pop, boolean recombined)
/*    */   {
/*    */   }
/*    */ 
/*    */   public void printTreesCP(int step, Tree[][] trees)
/*    */   {
/*    */   }
/*    */ 
/*    */   public void printTreesGA(int step, Tree[] trees, boolean selectionDone)
/*    */   {
/*    */   }
/*    */ 
/*    */   public void printTreesHC(int step, Tree bestTree, Tree currentTree)
/*    */   {
/*    */   }
/*    */ 
/*    */   public void printTreesSA(int step, Tree bestTree, Tree S0Tree, Tree currentTree)
/*    */   {
/*    */   }
/*    */ 
/*    */   public void updateConsensusTree(Tree consensusTree)
/*    */   {
/*    */   }
/*    */ 
/*    */   public void showAutoStopDone(int noChangeSteps)
/*    */   {
/*    */   }
/*    */ 
/*    */   public void showCurrentCoolingSchedule(Parameters.SASchedule currentCoolingSchedule)
/*    */   {
/*    */   }
/*    */ 
/*    */   public void showCurrentTemperature(double currentTemperature)
/*    */   {
/*    */   }
/*    */ 
/*    */   public void showCurrentMRE(double currentMRE)
/*    */   {
/*    */   }
/*    */ 
/*    */   public void showCurrentTree(Tree tree)
/*    */   {
/*    */   }
/*    */ 
/*    */   public void showEvaluations(Map<String, Double> evaluationsToShow)
/*    */   {
/*    */   }
/*    */ 
/*    */   public void showRemainingTime(long time)
/*    */   {
/*    */   }
/*    */ 
/*    */   public void showReplicate()
/*    */   {
/*    */   }
/*    */ 
/*    */   public void showStageCPMetapopulation(int numOfSteps)
/*    */   {
/*    */   }
/*    */ 
/*    */   public void showStageDistanceMatrix(int numOfSteps)
/*    */   {
/*    */   }
/*    */ 
/*    */   public void showStageGAPopulation(int numOfSteps)
/*    */   {
/*    */   }
/*    */ 
/*    */   public void showStageOptimization(int active, String target)
/*    */   {
/*    */   }
/*    */ 
/*    */   public void showStageSATemperature(int numOfSteps)
/*    */   {
/*    */   }
/*    */ 
/*    */   public void showStageHCRestart()
/*    */   {
/*    */   }
/*    */ 
/*    */   public void showStageSearchProgress(int currentIteration, long remainingTime, int noChangeSteps)
/*    */   {
/*    */   }
/*    */ 
/*    */   public void showStageSearchStart(String heuristic, int maxSteps, double startingEvaluation)
/*    */   {
/*    */   }
/*    */ 
/*    */   public void showStageSearchStop(List<Tree> solutionTrees, Map<String, Double> evaluationsToShow)
/*    */   {
/*    */   }
/*    */ 
/*    */   public void showStageStartingTree(int numOfSteps)
/*    */   {
/*    */   }
/*    */ 
/*    */   public void showStartingTree(Tree tree)
/*    */   {
/*    */   }
/*    */ 
/*    */   public void showNextStep()
/*    */   {
/*    */   }
/*    */ 
/*    */   public void showText(String text)
/*    */   {
/*    */   }
/*    */ 
/*    */   public void run()
/*    */   {
/*    */   }
/*    */ 
/*    */   public void trackPerformances(String action, int level)
/*    */   {
/*    */   }
/*    */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.InactiveMonitor
 * JD-Core Version:    0.6.2
 */