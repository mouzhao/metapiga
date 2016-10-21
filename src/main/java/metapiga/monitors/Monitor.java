/*    */ package metapiga.monitors;
/*    */ 
/*    */ import java.util.List;
/*    */ import java.util.Map;
/*    */ import metapiga.modelization.DistanceMatrix;
/*    */ import metapiga.parameters.Parameters.Operator;
/*    */ import metapiga.parameters.Parameters.SASchedule;
/*    */ import metapiga.trees.Consensus;
/*    */ import metapiga.trees.Tree;
/*    */ 
/*    */ public abstract interface Monitor extends Runnable
/*    */ {
/*    */   public static final String BEST_SOLUTION = "Best solution";
/*    */   public static final String CURRENT_SOLUTION = "Current solution";
/*    */   public static final String POPULATION_SOLUTION = "Population";
/*    */   public static final String INDIVIDUAL_SOLUTION = "Individual";
/*    */   public static final String TEMPERATURE = "Temperature";
/*    */   public static final String STARTING_TREE = "Starting tree (best)";
/*    */ 
/*    */   public abstract MonitorType getMonitorType();
/*    */ 
/*    */   public abstract boolean trackDataMatrix();
/*    */ 
/*    */   public abstract boolean trackDistances();
/*    */ 
/*    */   public abstract boolean trackStartingTree();
/*    */ 
/*    */   public abstract boolean trackHeuristic();
/*    */ 
/*    */   public abstract boolean trackHeuristicTrees();
/*    */ 
/*    */   public abstract boolean trackConsensus();
/*    */ 
/*    */   public abstract boolean trackOperators();
/*    */ 
/*    */   public abstract boolean trackOperatorStats();
/*    */ 
/*    */   public abstract boolean trackPerformances();
/*    */ 
/*    */   public abstract boolean trackAncestralSequences();
/*    */ 
/*    */   public abstract void showNextStep();
/*    */ 
/*    */   public abstract void showRemainingTime(long paramLong);
/*    */ 
/*    */   public abstract void showAutoStopDone(int paramInt);
/*    */ 
/*    */   public abstract void showReplicate();
/*    */ 
/*    */   public abstract void showEvaluations(Map<String, Double> paramMap);
/*    */ 
/*    */   public abstract void showCurrentTemperature(double paramDouble);
/*    */ 
/*    */   public abstract void showCurrentCoolingSchedule(Parameters.SASchedule paramSASchedule);
/*    */ 
/*    */   public abstract void showCurrentMRE(double paramDouble);
/*    */ 
/*    */   public abstract void showStartingTree(Tree paramTree);
/*    */ 
/*    */   public abstract void showCurrentTree(Tree paramTree);
/*    */ 
/*    */   public abstract void showText(String paramString);
/*    */ 
/*    */   public abstract void showStageDistanceMatrix(int paramInt);
/*    */ 
/*    */   public abstract void showStageStartingTree(int paramInt);
/*    */ 
/*    */   public abstract void showStageHCRestart();
/*    */ 
/*    */   public abstract void showStageSATemperature(int paramInt);
/*    */ 
/*    */   public abstract void showStageGAPopulation(int paramInt);
/*    */ 
/*    */   public abstract void showStageCPMetapopulation(int paramInt);
/*    */ 
/*    */   public abstract void showStageSearchStart(String paramString, int paramInt, double paramDouble);
/*    */ 
/*    */   public abstract void showStageSearchProgress(int paramInt1, long paramLong, int paramInt2);
/*    */ 
/*    */   public abstract void showStageSearchStop(List<Tree> paramList, Map<String, Double> paramMap);
/*    */ 
/*    */   public abstract void showStageOptimization(int paramInt, String paramString);
/*    */ 
/*    */   public abstract void end(List<Tree> paramList);
/*    */ 
/*    */   public abstract void endFromException(Exception paramException);
/*    */ 
/*    */   public abstract void printDataMatrix();
/*    */ 
/*    */   public abstract void printDistanceMatrix(DistanceMatrix paramDistanceMatrix);
/*    */ 
/*    */   public abstract void printStartingTrees(List<Tree> paramList);
/*    */ 
/*    */   public abstract void printTreeBeforeOperator(Tree paramTree, Parameters.Operator paramOperator, boolean paramBoolean);
/*    */ 
/*    */   public abstract void printOperatorInfos(Tree paramTree, String paramString);
/*    */ 
/*    */   public abstract void printOperatorInfos(Tree paramTree, String paramString, Consensus paramConsensus);
/*    */ 
/*    */   public abstract void printTreeAfterOperator(Tree paramTree, Parameters.Operator paramOperator, boolean paramBoolean);
/*    */ 
/*    */   public abstract void printOperatorFrequenciesUpdate(int paramInt, Map<Parameters.Operator, Integer> paramMap, Map<Parameters.Operator, Double> paramMap1, Map<Parameters.Operator, Long> paramMap2);
/*    */ 
/*    */   public abstract void printOperatorFrequenciesUpdate(Map<Parameters.Operator, Double> paramMap);
/*    */ 
/*    */   public abstract void printOperatorStatistics(int paramInt1, Map<Parameters.Operator, Integer> paramMap, Map<Parameters.Operator, Double> paramMap1, Map<Parameters.Operator, Long> paramMap2, int paramInt2, int paramInt3, Map<Parameters.Operator, Map<Integer, Integer>> paramMap3);
/*    */ 
/*    */   public abstract void printDetailsHC(int paramInt, double paramDouble1, double paramDouble2, Parameters.Operator paramOperator, boolean paramBoolean);
/*    */ 
/*    */   public abstract void printDetailsSA(int paramInt1, double paramDouble1, double paramDouble2, double paramDouble3, Parameters.Operator paramOperator, String paramString, double paramDouble4, double paramDouble5, int paramInt2, int paramInt3, int paramInt4, int paramInt5);
/*    */ 
/*    */   public abstract void printDetailsGA(int paramInt, Tree[] paramArrayOfTree, Parameters.Operator[] paramArrayOfOperator);
/*    */ 
/*    */   public abstract void printDetailsGA(int paramInt, String paramString, Tree[] paramArrayOfTree, double paramDouble);
/*    */ 
/*    */   public abstract void printDetailsCP(Tree[] paramArrayOfTree, Parameters.Operator[] paramArrayOfOperator, int paramInt);
/*    */ 
/*    */   public abstract void printDetailsCP(Tree[] paramArrayOfTree, String[] paramArrayOfString, int paramInt);
/*    */ 
/*    */   public abstract void printDetailsCP(int paramInt, String[] paramArrayOfString, Tree[][] paramArrayOfTree, double paramDouble);
/*    */ 
/*    */   public abstract void printTreesHC(int paramInt, Tree paramTree1, Tree paramTree2);
/*    */ 
/*    */   public abstract void printTreesSA(int paramInt, Tree paramTree1, Tree paramTree2, Tree paramTree3);
/*    */ 
/*    */   public abstract void printTreesGA(int paramInt, Tree[] paramArrayOfTree, boolean paramBoolean);
/*    */ 
/*    */   public abstract void printTreesCP(int paramInt1, Tree[] paramArrayOfTree, int paramInt2, boolean paramBoolean);
/*    */ 
/*    */   public abstract void printTreesCP(int paramInt, Tree[][] paramArrayOfTree);
/*    */ 
/*    */   public abstract void printEndTreesHeuristic();
/*    */ 
/*    */   public abstract void printConsensus(int paramInt, Consensus paramConsensus);
/*    */ 
/*    */   public abstract void trackPerformances(String paramString, int paramInt);
/*    */ 
/*    */   public abstract void printAncestralSequences(List<Tree> paramList);
/*    */ 
/*    */   public abstract void updateConsensusTree(Tree paramTree);
/*    */ 
/*    */   public static enum MonitorType
/*    */   {
/* 20 */     INACTIVE, SINGLE_SEARCH_GRAPHICAL, BATCH_SEARCH_GRAPHICAL, CONSOLE, SILENT, OPTIMIZATION;
/*    */   }
/*    */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.Monitor
 * JD-Core Version:    0.6.2
 */