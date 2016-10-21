/*     */ package monitors;
/*     */ 
/*     */ import java.awt.Color;
/*     */ import java.awt.GridBagConstraints;
/*     */ import java.awt.GridBagLayout;
/*     */ import java.awt.Insets;
/*     */ import java.io.IOException;
/*     */ import java.util.Date;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Set;
/*     */ import javax.swing.JLabel;
/*     */ import javax.swing.JPanel;
/*     */ import javax.swing.JProgressBar;
/*     */ import javax.swing.SwingUtilities;
/*     */ import metapiga.heuristics.Bootstrapping;
/*     */ import metapiga.heuristics.ConsensusPruning;
/*     */ import metapiga.heuristics.GeneticAlgorithm;
/*     */ import metapiga.heuristics.Heuristic;
/*     */ import metapiga.heuristics.HillClimbing;
/*     */ import metapiga.heuristics.SimulatedAnnealing;
/*     */ import metapiga.modelization.DistanceMatrix;
/*     */ import metapiga.parameters.Parameters;
/*     */ import metapiga.parameters.Parameters.HeuristicStopCondition;
/*     */ import metapiga.parameters.Parameters.LogFile;
/*     */ import metapiga.parameters.Parameters.Operator;
/*     */ import metapiga.parameters.Parameters.ReplicatesStopCondition;
/*     */ import metapiga.parameters.Parameters.SASchedule;
/*     */ import metapiga.trees.Consensus;
/*     */ import metapiga.trees.Tree;
/*     */ import metapiga.utilities.Tools;
/*     */ 
/*     */ public class SearchBatchGraphicalMonitor extends JPanel
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
/*     */   private final SearchBatchGraphical parent;
/*     */   private final String runLabel;
/*     */   private Thread thread;
/*     */   private Heuristic H;
/*     */   private Parameters parameters;
/*     */   final PrintMonitor print;
/*  48 */   private int maxSteps = 0;
/*     */   private int currentStep;
/*     */   private int currentReplicate;
/*     */   private long repStartTime;
/*  52 */   private String progressBarSaveString = "";
/*     */ 
/*  54 */   final JProgressBar runProgressBar = new JProgressBar();
/*  55 */   final JProgressBar timeLeftProgress = new JProgressBar();
/*  56 */   final JLabel startingLikelihoodLabel = new JLabel();
/*  57 */   final JLabel currentLikelihoodLabel = new JLabel();
/*     */ 
/*     */   public SearchBatchGraphicalMonitor(SearchBatchGraphical parent, Parameters parameters, String runLabel) {
/*  60 */     this.parent = parent;
/*  61 */     this.parameters = parameters;
/*  62 */     this.runLabel = runLabel;
/*  63 */     this.DATA = parameters.logFiles.contains(Parameters.LogFile.DATA);
/*  64 */     this.DIST = parameters.logFiles.contains(Parameters.LogFile.DIST);
/*  65 */     this.TREESTART = parameters.logFiles.contains(Parameters.LogFile.TREESTART);
/*  66 */     this.HEUR = parameters.logFiles.contains(Parameters.LogFile.HEUR);
/*  67 */     this.TREEHEUR = parameters.logFiles.contains(Parameters.LogFile.TREEHEUR);
/*  68 */     this.CONSENSUS = parameters.logFiles.contains(Parameters.LogFile.CONSENSUS);
/*  69 */     this.OPDETAILS = parameters.logFiles.contains(Parameters.LogFile.OPDETAILS);
/*  70 */     this.OPSTATS = parameters.logFiles.contains(Parameters.LogFile.OPSTATS);
/*  71 */     this.ANCSEQ = parameters.logFiles.contains(Parameters.LogFile.ANCSEQ);
/*  72 */     this.PERF = parameters.logFiles.contains(Parameters.LogFile.PERF);
/*  73 */     this.print = new PrintMonitor(this, runLabel);
/*  74 */     this.print.setParameters(parameters);
/*     */     try {
/*  76 */       jbInit();
/*     */     }
/*     */     catch (Exception ex) {
/*  79 */       ex.printStackTrace();
/*     */     }
/*     */   }
/*     */ 
/*     */   public Monitor.MonitorType getMonitorType()
/*     */   {
/*  85 */     return Monitor.MonitorType.BATCH_SEARCH_GRAPHICAL;
/*     */   }
/*     */ 
/*     */   private void jbInit() {
/*  89 */     GridBagLayout gridBagLayout_1 = new GridBagLayout();
/*  90 */     gridBagLayout_1.columnWidths = new int[] { 516 };
/*  91 */     gridBagLayout_1.rowHeights = new int[] { 14, 14 };
/*  92 */     gridBagLayout_1.columnWeights = new double[] { 0.0D, 0.0D, 4.9E-324D };
/*  93 */     gridBagLayout_1.rowWeights = new double[] { 0.0D, 0.0D, 4.9E-324D };
/*  94 */     setLayout(gridBagLayout_1);
/*     */ 
/*  96 */     JPanel likelihoodPanel = new JPanel();
/*  97 */     GridBagLayout gridBagLayout_2 = new GridBagLayout();
/*  98 */     gridBagLayout_2.columnWidths = new int[3];
/*  99 */     likelihoodPanel.setLayout(gridBagLayout_2);
/* 100 */     GridBagConstraints gbc_1 = new GridBagConstraints();
/* 101 */     gbc_1.anchor = 18;
/* 102 */     gbc_1.insets = new Insets(0, 15, 5, 0);
/* 103 */     gbc_1.gridx = 0;
/* 104 */     gbc_1.gridy = 0;
/* 105 */     add(likelihoodPanel, gbc_1);
/*     */ 
/* 107 */     this.startingLikelihoodLabel.setText("Starting likelihood : ");
/* 108 */     this.startingLikelihoodLabel.setForeground(Color.red);
/* 109 */     GridBagConstraints gridBagConstraints_12 = new GridBagConstraints();
/* 110 */     gridBagConstraints_12.anchor = 17;
/* 111 */     gridBagConstraints_12.insets = new Insets(0, 5, 0, 5);
/* 112 */     gridBagConstraints_12.weightx = 0.0D;
/* 113 */     gridBagConstraints_12.gridy = 0;
/* 114 */     gridBagConstraints_12.gridx = 0;
/* 115 */     likelihoodPanel.add(this.startingLikelihoodLabel, gridBagConstraints_12);
/*     */ 
/* 117 */     this.currentLikelihoodLabel.setText("Current likelihood : ");
/* 118 */     this.currentLikelihoodLabel.setForeground(Color.BLUE);
/* 119 */     GridBagConstraints gridBagConstraints_11 = new GridBagConstraints();
/* 120 */     gridBagConstraints_11.insets = new Insets(0, 25, 0, 5);
/* 121 */     gridBagConstraints_11.anchor = 17;
/* 122 */     gridBagConstraints_11.weightx = 0.0D;
/* 123 */     gridBagConstraints_11.gridy = 0;
/* 124 */     gridBagConstraints_11.gridx = 1;
/* 125 */     likelihoodPanel.add(this.currentLikelihoodLabel, gridBagConstraints_11);
/*     */ 
/* 127 */     JPanel emptyPanel = new JPanel();
/* 128 */     GridBagConstraints gridBagConstraints_13 = new GridBagConstraints();
/* 129 */     gridBagConstraints_13.weightx = 1.0D;
/* 130 */     gridBagConstraints_13.fill = 2;
/* 131 */     gridBagConstraints_13.gridy = 0;
/* 132 */     gridBagConstraints_13.gridx = 2;
/* 133 */     likelihoodPanel.add(emptyPanel, gridBagConstraints_13);
/* 134 */     JPanel runProgressPanel = new JPanel();
/* 135 */     GridBagConstraints gbc_1_1 = new GridBagConstraints();
/* 136 */     gbc_1_1.weightx = 1.0D;
/* 137 */     gbc_1_1.fill = 2;
/* 138 */     gbc_1_1.anchor = 18;
/* 139 */     gbc_1_1.gridx = 1;
/* 140 */     gbc_1_1.gridy = 0;
/* 141 */     add(runProgressPanel, gbc_1_1);
/* 142 */     GridBagLayout gridBagLayout = new GridBagLayout();
/* 143 */     gridBagLayout.columnWidths = new int[6];
/* 144 */     runProgressPanel.setLayout(gridBagLayout);
/*     */ 
/* 146 */     JLabel runProgressLabel = new JLabel();
/* 147 */     runProgressLabel.setText("Run progress :");
/* 148 */     GridBagConstraints gridBagConstraints_3 = new GridBagConstraints();
/* 149 */     gridBagConstraints_3.insets = new Insets(0, 5, 0, 5);
/* 150 */     gridBagConstraints_3.gridy = 0;
/* 151 */     gridBagConstraints_3.gridx = 0;
/* 152 */     runProgressPanel.add(runProgressLabel, gridBagConstraints_3);
/*     */ 
/* 154 */     GridBagConstraints gridBagConstraints_4 = new GridBagConstraints();
/* 155 */     gridBagConstraints_4.weightx = 1.0D;
/* 156 */     gridBagConstraints_4.fill = 1;
/* 157 */     gridBagConstraints_4.insets = new Insets(0, 5, 0, 10);
/* 158 */     gridBagConstraints_4.gridy = 0;
/* 159 */     gridBagConstraints_4.gridx = 1;
/* 160 */     runProgressPanel.add(this.runProgressBar, gridBagConstraints_4);
/*     */ 
/* 162 */     JLabel timeLeftLabel = new JLabel();
/* 163 */     timeLeftLabel.setText("Time left :");
/* 164 */     GridBagConstraints gridBagConstraints_5 = new GridBagConstraints();
/* 165 */     gridBagConstraints_5.insets = new Insets(0, 5, 0, 5);
/* 166 */     gridBagConstraints_5.gridy = 0;
/* 167 */     gridBagConstraints_5.gridx = 2;
/* 168 */     runProgressPanel.add(timeLeftLabel, gridBagConstraints_5);
/*     */ 
/* 170 */     GridBagConstraints gridBagConstraints_6 = new GridBagConstraints();
/* 171 */     gridBagConstraints_6.insets = new Insets(0, 5, 0, 5);
/* 172 */     gridBagConstraints_6.fill = 1;
/* 173 */     gridBagConstraints_6.weightx = 1.0D;
/* 174 */     gridBagConstraints_6.gridy = 0;
/* 175 */     gridBagConstraints_6.gridx = 3;
/* 176 */     runProgressPanel.add(this.timeLeftProgress, gridBagConstraints_6);
/*     */   }
/*     */   public final boolean trackDataMatrix() {
/* 179 */     return this.DATA; } 
/* 180 */   public final boolean trackDistances() { return this.DIST; } 
/* 181 */   public final boolean trackStartingTree() { return this.TREESTART; } 
/* 182 */   public final boolean trackHeuristic() { return this.HEUR; } 
/* 183 */   public final boolean trackHeuristicTrees() { return this.TREEHEUR; } 
/* 184 */   public final boolean trackConsensus() { return this.CONSENSUS; } 
/* 185 */   public final boolean trackOperators() { return this.OPDETAILS; } 
/* 186 */   public final boolean trackOperatorStats() { return this.OPSTATS; } 
/* 187 */   public final boolean trackPerformances() { return this.PERF; } 
/* 188 */   public final boolean trackAncestralSequences() { return this.ANCSEQ; }
/*     */ 
/*     */   public void run()
/*     */   {
/*     */     try {
/* 193 */       if (trackDataMatrix()) printDataMatrix();
/*     */       int j;
/*     */       int i;
/* 194 */       for (; (this.currentReplicate = this.parent.getNextReplicate()) > 0; 
/* 222 */         i < j)
/*     */       {
/* 195 */         this.print.initLogFiles(this.currentReplicate);
/* 196 */         switch ($SWITCH_TABLE$metapiga$parameters$Parameters$Heuristic()[this.parameters.heuristic.ordinal()]) {
/*     */         case 1:
/* 198 */           this.H = new HillClimbing(this.parameters, this);
/* 199 */           break;
/*     */         case 2:
/* 201 */           this.H = new SimulatedAnnealing(this.parameters, this);
/* 202 */           break;
/*     */         case 3:
/* 204 */           this.H = new GeneticAlgorithm(this.parameters, this);
/* 205 */           break;
/*     */         case 4:
/* 207 */           this.H = new ConsensusPruning(this.parameters, this);
/* 208 */           break;
/*     */         case 5:
/* 210 */           this.H = new Bootstrapping(this.parameters, this); } 
/*     */ this.thread = new Thread(this.H, this.H.getName(true) + "-Rep-" + this.currentReplicate);
/* 214 */         this.thread.start();
/* 215 */         this.thread.join();
/*     */         StackTraceElement[] arrayOfStackTraceElement;
/*     */         try { this.print.closeOutputFiles();
/*     */         } catch (IOException e) {
/* 219 */           e.printStackTrace();
/* 220 */           showText("\n Error when closing log files");
/* 221 */           showText("\n Java exception : " + e.getCause() + " (" + e.getMessage() + ")");
/* 222 */           j = (arrayOfStackTraceElement = e.getStackTrace()).length; i = 0; continue; } StackTraceElement el = arrayOfStackTraceElement[i];
/* 223 */         showText("\tat " + el.toString());
/*     */ 
/* 222 */         i++;
/*     */       }
/*     */ 
/* 227 */       SwingUtilities.invokeLater(new Runnable() {
/*     */         public void run() {
/* 229 */           SearchBatchGraphicalMonitor.this.runProgressBar.setValue(SearchBatchGraphicalMonitor.this.runProgressBar.getMaximum());
/* 230 */           SearchBatchGraphicalMonitor.this.runProgressBar.setString("job finished");
/* 231 */           SearchBatchGraphicalMonitor.this.runProgressBar.setStringPainted(true);
/*     */         } } );
/*     */     }
/*     */     catch (Exception e) {
/* 235 */       endFromException(e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void stop() {
/* 240 */     this.H.smoothStop();
/*     */   }
/*     */ 
/*     */   public void end(List<Tree> solutionTrees)
/*     */   {
/* 245 */     if (((this.parameters.replicatesStopCondition == Parameters.ReplicatesStopCondition.NONE) && (this.parameters.replicatesNumber > 1)) || 
/* 246 */       (this.parameters.replicatesStopCondition == Parameters.ReplicatesStopCondition.MRE)) {
/* 247 */       for (Tree tree : solutionTrees) {
/* 248 */         tree.setName(tree.getName() + "_Rep_" + this.currentReplicate);
/*     */       }
/*     */     }
/*     */ 
/* 252 */     if (this.ANCSEQ) printAncestralSequences(solutionTrees);
/* 253 */     for (Tree tree : solutionTrees) {
/* 254 */       tree.deleteLikelihoodComputation();
/*     */     }
/* 256 */     this.parent.addSolutionTree(solutionTrees);
/*     */   }
/*     */ 
/*     */   public void endFromException(Exception e)
/*     */   {
/* 261 */     this.parent.endFromException(e);
/*     */   }
/*     */ 
/*     */   public void printDataMatrix() {
/* 265 */     this.print.printDataMatrix();
/*     */   }
/*     */ 
/*     */   public void printDistanceMatrix(DistanceMatrix dm) {
/* 269 */     this.print.printDistanceMatrix(dm);
/*     */   }
/*     */ 
/*     */   public void printStartingTrees(List<Tree> startingTrees) {
/* 273 */     this.print.printStartingTrees(startingTrees, this.currentReplicate);
/*     */   }
/*     */ 
/*     */   public void printTreeBeforeOperator(Tree tree, Parameters.Operator operator, boolean consensus) {
/* 277 */     this.print.printTreeBeforeOperator(tree, operator, consensus);
/*     */   }
/*     */ 
/*     */   public void printOperatorInfos(Tree tree, String infos) {
/* 281 */     this.print.printOperatorInfos(tree, infos);
/*     */   }
/*     */ 
/*     */   public void printOperatorInfos(Tree tree, String infos, Consensus consensus) {
/* 285 */     this.print.printOperatorInfos(tree, infos, consensus);
/*     */   }
/*     */ 
/*     */   public void printTreeAfterOperator(Tree tree, Parameters.Operator operator, boolean consensus) {
/* 289 */     this.print.printTreeAfterOperator(tree, operator, consensus);
/*     */   }
/*     */ 
/*     */   public void printOperatorFrequenciesUpdate(int currentStep, Map<Parameters.Operator, Integer> use, Map<Parameters.Operator, Double> scoreImprovements, Map<Parameters.Operator, Long> performances) {
/* 293 */     this.print.printOperatorFrequenciesUpdate(currentStep, use, scoreImprovements, performances);
/*     */   }
/*     */ 
/*     */   public void printOperatorFrequenciesUpdate(Map<Parameters.Operator, Double> frequencies) {
/* 297 */     this.print.printOperatorFrequenciesUpdate(frequencies);
/*     */   }
/*     */ 
/*     */   public void printOperatorStatistics(int numStep, Map<Parameters.Operator, Integer> use, Map<Parameters.Operator, Double> scoreImprovements, Map<Parameters.Operator, Long> performances, int outgroupTargeted, int ingroupTargeted, Map<Parameters.Operator, Map<Integer, Integer>> cancelByConsensus)
/*     */   {
/* 302 */     this.print.printOperatorStatistics(numStep, use, scoreImprovements, performances, outgroupTargeted, ingroupTargeted, cancelByConsensus);
/*     */   }
/*     */ 
/*     */   public void printDetailsHC(int step, double bestLikelihood, double currentLikelihood, Parameters.Operator operator, boolean improvement) {
/* 306 */     this.print.printDetailsHC(step, bestLikelihood, currentLikelihood, operator, improvement);
/*     */   }
/*     */ 
/*     */   public void printDetailsSA(int step, double bestLikelihood, double S0Likelihood, double currentLikelihood, Parameters.Operator operator, String status, double tempAcceptance, double temperature, int coolingSteps, int successes, int failures, int reheatingDecrements)
/*     */   {
/* 311 */     this.print.printDetailsSA(step, bestLikelihood, S0Likelihood, currentLikelihood, operator, status, tempAcceptance, temperature, coolingSteps, successes, failures, reheatingDecrements);
/*     */   }
/*     */ 
/*     */   public void printDetailsGA(int step, Tree[] mutantML, Parameters.Operator[] operator) {
/* 315 */     this.print.printDetailsGA(step, mutantML, operator);
/*     */   }
/*     */ 
/*     */   public void printDetailsGA(int step, String selectionDetails, Tree[] selectedML, double bestLikelihood) {
/* 319 */     this.print.printDetailsGA(step, selectionDetails, selectedML, bestLikelihood);
/*     */   }
/*     */ 
/*     */   public void printDetailsCP(Tree[] mutantML, Parameters.Operator[] operator, int currentPop) {
/* 323 */     this.print.printDetailsCP(mutantML, operator, currentPop);
/*     */   }
/*     */ 
/*     */   public void printDetailsCP(Tree[] hybridML, String[] parents, int currentPop) {
/* 327 */     this.print.printDetailsCP(hybridML, parents, currentPop);
/*     */   }
/*     */ 
/*     */   public void printDetailsCP(int step, String[] selectionDetails, Tree[][] selectedML, double bestLikelihood) {
/* 331 */     this.print.printDetailsCP(step, selectionDetails, selectedML, bestLikelihood);
/*     */   }
/*     */ 
/*     */   public void printTreesHC(int step, Tree bestTree, Tree currentTree) {
/* 335 */     this.print.printTreesHC(step, bestTree, currentTree);
/*     */   }
/*     */ 
/*     */   public void printTreesSA(int step, Tree bestTree, Tree S0Tree, Tree currentTree) {
/* 339 */     this.print.printTreesSA(step, bestTree, S0Tree, currentTree);
/*     */   }
/*     */ 
/*     */   public void printTreesGA(int step, Tree[] trees, boolean selectionDone) {
/* 343 */     this.print.printTreesGA(step, trees, selectionDone);
/*     */   }
/*     */ 
/*     */   public void printTreesCP(int step, Tree[] trees, int pop, boolean recombined) {
/* 347 */     this.print.printTreesCP(step, trees, pop, recombined);
/*     */   }
/*     */ 
/*     */   public void printTreesCP(int step, Tree[][] trees) {
/* 351 */     this.print.printTreesCP(step, trees);
/*     */   }
/*     */ 
/*     */   public void printEndTreesHeuristic() {
/* 355 */     this.print.printEndTreesHeuristic();
/*     */   }
/*     */ 
/*     */   public void printConsensus(int step, Consensus consensus) {
/* 359 */     this.print.printConsensus(step, consensus);
/*     */   }
/*     */ 
/*     */   public void trackPerformances(String action, int level) {
/* 363 */     this.print.trackPerformances(action, level);
/*     */   }
/*     */ 
/*     */   public void printAncestralSequences(List<Tree> trees) {
/* 367 */     this.print.printAncestralSequences(trees);
/*     */   }
/*     */ 
/*     */   public void updateConsensusTree(Tree consensusTree) {
/* 371 */     this.print.updateConsensusTree(consensusTree);
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
/* 396 */     this.currentLikelihoodLabel.setText("Best solution : " + Tools.doubletoString(((Double)evaluationsToShow.get("Best solution")).doubleValue(), 4));
/*     */   }
/*     */ 
/*     */   public void showRemainingTime(long time)
/*     */   {
/* 401 */     SwingUtilities.invokeLater(new TimeLeftProgressUpdate(time));
/*     */   }
/*     */ 
/*     */   public void showReplicate()
/*     */   {
/* 429 */     this.parent.showReplicate();
/*     */   }
/*     */ 
/*     */   public void showStageCPMetapopulation(int numOfSteps)
/*     */   {
/* 435 */     final int val = numOfSteps;
/* 436 */     SwingUtilities.invokeLater(new Runnable() {
/*     */       public void run() {
/* 438 */         SearchBatchGraphicalMonitor.this.runProgressBar.setMinimum(0);
/* 439 */         SearchBatchGraphicalMonitor.this.runProgressBar.setMaximum(val);
/* 440 */         SearchBatchGraphicalMonitor.this.runProgressBar.setValue(0);
/* 441 */         SearchBatchGraphicalMonitor.this.runProgressBar.setString("Creating metapopulation");
/* 442 */         SearchBatchGraphicalMonitor.this.runProgressBar.setStringPainted(true);
/*     */       }
/*     */     });
/*     */   }
/*     */ 
/*     */   public void showStageDistanceMatrix(int numOfSteps)
/*     */   {
/* 449 */     final int val = numOfSteps;
/* 450 */     SwingUtilities.invokeLater(new Runnable() {
/*     */       public void run() {
/* 452 */         SearchBatchGraphicalMonitor.this.runProgressBar.setMinimum(0);
/* 453 */         SearchBatchGraphicalMonitor.this.runProgressBar.setMaximum(val);
/* 454 */         SearchBatchGraphicalMonitor.this.runProgressBar.setValue(0);
/* 455 */         SearchBatchGraphicalMonitor.this.runProgressBar.setString("Building distance matrix");
/* 456 */         SearchBatchGraphicalMonitor.this.runProgressBar.setStringPainted(true);
/*     */       }
/*     */     });
/*     */   }
/*     */ 
/*     */   public void showStageGAPopulation(int numOfSteps)
/*     */   {
/* 463 */     final int val = numOfSteps;
/* 464 */     SwingUtilities.invokeLater(new Runnable() {
/*     */       public void run() {
/* 466 */         SearchBatchGraphicalMonitor.this.runProgressBar.setMinimum(0);
/* 467 */         SearchBatchGraphicalMonitor.this.runProgressBar.setMaximum(val);
/* 468 */         SearchBatchGraphicalMonitor.this.runProgressBar.setValue(0);
/* 469 */         SearchBatchGraphicalMonitor.this.runProgressBar.setString("Creating population");
/* 470 */         SearchBatchGraphicalMonitor.this.runProgressBar.setStringPainted(true);
/*     */       }
/*     */     });
/*     */   }
/*     */ 
/*     */   public void showStageOptimization(int active, String target)
/*     */   {
/* 477 */     if (active > 0) {
/* 478 */       final String val = target;
/* 479 */       SwingUtilities.invokeLater(new Runnable() {
/*     */         public void run() {
/* 481 */           SearchBatchGraphicalMonitor.this.progressBarSaveString = SearchBatchGraphicalMonitor.this.runProgressBar.getString();
/* 482 */           SearchBatchGraphicalMonitor.this.runProgressBar.setString("Intra-step optimization of " + val);
/* 483 */           SearchBatchGraphicalMonitor.this.runProgressBar.setStringPainted(true);
/*     */         } } );
/*     */     }
/*     */     else {
/* 487 */       SwingUtilities.invokeLater(new Runnable() {
/*     */         public void run() {
/* 489 */           SearchBatchGraphicalMonitor.this.runProgressBar.setString(SearchBatchGraphicalMonitor.this.progressBarSaveString);
/* 490 */           SearchBatchGraphicalMonitor.this.runProgressBar.setStringPainted(true);
/*     */         }
/*     */       });
/*     */     }
/*     */   }
/*     */ 
/*     */   public void showStageSATemperature(int numOfSteps)
/*     */   {
/* 498 */     final int val = numOfSteps;
/* 499 */     SwingUtilities.invokeLater(new Runnable() {
/*     */       public void run() {
/* 501 */         SearchBatchGraphicalMonitor.this.runProgressBar.setMinimum(0);
/* 502 */         SearchBatchGraphicalMonitor.this.runProgressBar.setMaximum(val);
/* 503 */         SearchBatchGraphicalMonitor.this.runProgressBar.setValue(0);
/* 504 */         SearchBatchGraphicalMonitor.this.runProgressBar.setString("Setting temperature");
/* 505 */         SearchBatchGraphicalMonitor.this.runProgressBar.setStringPainted(true);
/*     */       }
/*     */     });
/*     */   }
/*     */ 
/*     */   public void showStageHCRestart()
/*     */   {
/*     */   }
/*     */ 
/*     */   public synchronized void showStageSearchProgress(int currentIteration, long remainingTime, int noChangeSteps)
/*     */   {
/* 517 */     if ((this.parameters.sufficientStopConditions.contains(Parameters.HeuristicStopCondition.STEPS)) || 
/* 518 */       (this.parameters.necessaryStopConditions.contains(Parameters.HeuristicStopCondition.STEPS))) showNextStep();
/* 519 */     else if ((this.parameters.sufficientStopConditions.contains(Parameters.HeuristicStopCondition.AUTO)) || 
/* 520 */       (this.parameters.necessaryStopConditions.contains(Parameters.HeuristicStopCondition.AUTO))) showAutoStopDone(noChangeSteps);
/* 521 */     if ((this.parameters.sufficientStopConditions.contains(Parameters.HeuristicStopCondition.TIME)) || 
/* 522 */       (this.parameters.necessaryStopConditions.contains(Parameters.HeuristicStopCondition.TIME))) showRemainingTime(remainingTime);
/* 523 */     final int val = currentIteration;
/* 524 */     SwingUtilities.invokeLater(new Runnable() {
/*     */       public void run() {
/* 526 */         String stage = SearchBatchGraphicalMonitor.this.runProgressBar.getString();
/* 527 */         if (stage.indexOf(" - ") > 0) stage = stage.substring(0, stage.indexOf(" - "));
/* 528 */         SearchBatchGraphicalMonitor.this.runProgressBar.setString(stage + " - iteration " + val);
/* 529 */         SearchBatchGraphicalMonitor.this.runProgressBar.setStringPainted(true);
/*     */       }
/*     */     });
/*     */   }
/*     */ 
/*     */   public void showStageSearchStart(String heuristic, int maxSteps, double startingEvaluation)
/*     */   {
/* 536 */     this.repStartTime = System.currentTimeMillis();
/* 537 */     StringBuilder s = new StringBuilder();
/* 538 */     if ((this.parameters.sufficientStopConditions.isEmpty()) && (this.parameters.necessaryStopConditions.isEmpty()))
/* 539 */       s.append("Don't start " + heuristic + " as user commands" + "\n");
/*     */     else {
/* 541 */       s.append(heuristic + (this.parameters.hasManyReplicates() ? " (Replicate " + this.currentReplicate + ")" : "") + " started at " + new Date(this.repStartTime).toString() + "\n");
/*     */     }
/* 543 */     if (!this.parameters.sufficientStopConditions.isEmpty()) {
/* 544 */       s.append("Stop when ANY of the following sufficient conditions is met: \n");
/* 545 */       for (Parameters.HeuristicStopCondition condition : this.parameters.sufficientStopConditions) {
/* 546 */         switch (condition) {
/*     */         case AUTO:
/* 548 */           s.append("- " + this.parameters.stopCriterionSteps + " iterations performed" + "\n");
/* 549 */           break;
/*     */         case CONSENSUS:
/* 551 */           s.append("- Pass over " + new Date(this.repStartTime + ()this.parameters.stopCriterionTime * 3600L * 1000L).toString() + "\n");
/* 552 */           break;
/*     */         case STEPS:
/* 554 */           s.append("- Likelihood stop increasing after " + this.parameters.stopCriterionAutoSteps + " iterations" + "\n");
/* 555 */           break;
/*     */         case TIME:
/* 557 */           s.append("- Mean relative error of " + this.parameters.stopCriterionConsensusInterval + " consecutive consensus trees stay below " + Tools.doubleToPercent(this.parameters.stopCriterionConsensusMRE, 0) + " using trees sampled every " + this.parameters.stopCriterionConsensusGeneration + " generations" + "\n");
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 562 */     if (!this.parameters.necessaryStopConditions.isEmpty()) {
/* 563 */       s.append("Stop when ALL of the following necessary conditions are met: \n");
/* 564 */       for (Parameters.HeuristicStopCondition condition : this.parameters.necessaryStopConditions) {
/* 565 */         switch (condition) {
/*     */         case AUTO:
/* 567 */           s.append("- " + this.parameters.stopCriterionSteps + " iterations performed" + "\n");
/* 568 */           break;
/*     */         case CONSENSUS:
/* 570 */           s.append("- Pass over " + new Date(this.repStartTime + ()this.parameters.stopCriterionTime * 3600L * 1000L).toString() + "\n");
/* 571 */           break;
/*     */         case STEPS:
/* 573 */           s.append("- Likelihood stop increasing after " + this.parameters.stopCriterionAutoSteps + " iterations" + "\n");
/* 574 */           break;
/*     */         case TIME:
/* 576 */           s.append("- Mean relative error of " + this.parameters.stopCriterionConsensusInterval + " consecutive consensus trees stay below " + Tools.doubleToPercent(this.parameters.stopCriterionConsensusMRE, 0) + " using trees sampled every " + this.parameters.stopCriterionConsensusGeneration + " generations" + "\n");
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 581 */     showText(s.toString());
/* 582 */     if ((this.parameters.sufficientStopConditions.contains(Parameters.HeuristicStopCondition.TIME)) || 
/* 583 */       (this.parameters.necessaryStopConditions.contains(Parameters.HeuristicStopCondition.TIME))) {
/* 584 */       SwingUtilities.invokeLater(new Runnable() {
/*     */         public void run() {
/* 586 */           SearchBatchGraphicalMonitor.this.timeLeftProgress.setMinimum(0);
/* 587 */           SearchBatchGraphicalMonitor.this.timeLeftProgress.setMaximum((int)(SearchBatchGraphicalMonitor.this.parameters.stopCriterionTime * 3600.0D * 1000.0D));
/* 588 */           SearchBatchGraphicalMonitor.this.timeLeftProgress.setValue(0);
/*     */         } } );
/*     */     }
/*     */     else {
/* 592 */       final String v2 = heuristic;
/* 593 */       SwingUtilities.invokeLater(new Runnable() {
/*     */         public void run() {
/* 595 */           SearchBatchGraphicalMonitor.this.timeLeftProgress.setIndeterminate(true);
/* 596 */           SearchBatchGraphicalMonitor.this.timeLeftProgress.setString(v2 + " running (no time stop)");
/* 597 */           SearchBatchGraphicalMonitor.this.timeLeftProgress.setStringPainted(true);
/*     */         }
/*     */       });
/*     */     }
/* 601 */     if ((this.parameters.sufficientStopConditions.contains(Parameters.HeuristicStopCondition.STEPS)) || 
/* 602 */       (this.parameters.necessaryStopConditions.contains(Parameters.HeuristicStopCondition.STEPS))) {
/* 603 */       this.maxSteps = maxSteps;
/* 604 */       final int v1 = maxSteps;
/* 605 */       final String v2 = heuristic;
/* 606 */       SwingUtilities.invokeLater(new Runnable() {
/*     */         public void run() {
/* 608 */           SearchBatchGraphicalMonitor.this.runProgressBar.setMinimum(0);
/* 609 */           SearchBatchGraphicalMonitor.this.runProgressBar.setMaximum(v1);
/* 610 */           SearchBatchGraphicalMonitor.this.runProgressBar.setValue(0);
/* 611 */           SearchBatchGraphicalMonitor.this.runProgressBar.setString(v2 + " running");
/* 612 */           SearchBatchGraphicalMonitor.this.runProgressBar.setStringPainted(true);
/*     */         } } );
/*     */     }
/* 615 */     else if ((this.parameters.sufficientStopConditions.contains(Parameters.HeuristicStopCondition.AUTO)) || 
/* 616 */       (this.parameters.necessaryStopConditions.contains(Parameters.HeuristicStopCondition.AUTO))) {
/* 617 */       final String v2 = heuristic;
/* 618 */       SwingUtilities.invokeLater(new Runnable() {
/*     */         public void run() {
/* 620 */           SearchBatchGraphicalMonitor.this.runProgressBar.setMinimum(0);
/* 621 */           SearchBatchGraphicalMonitor.this.runProgressBar.setMaximum(SearchBatchGraphicalMonitor.this.parameters.stopCriterionAutoSteps);
/* 622 */           SearchBatchGraphicalMonitor.this.runProgressBar.setValue(0);
/* 623 */           SearchBatchGraphicalMonitor.this.runProgressBar.setString(v2 + " running (auto stop)");
/* 624 */           SearchBatchGraphicalMonitor.this.runProgressBar.setStringPainted(true);
/*     */         } } );
/*     */     }
/*     */     else {
/* 628 */       this.maxSteps = 0;
/* 629 */       final String v2 = heuristic;
/* 630 */       SwingUtilities.invokeLater(new Runnable() {
/*     */         public void run() {
/* 632 */           SearchBatchGraphicalMonitor.this.runProgressBar.setIndeterminate(true);
/* 633 */           SearchBatchGraphicalMonitor.this.runProgressBar.setString(v2 + " running (no step stop)");
/* 634 */           SearchBatchGraphicalMonitor.this.runProgressBar.setStringPainted(true);
/*     */         }
/*     */       });
/*     */     }
/* 638 */     this.currentStep = 0;
/* 639 */     this.startingLikelihoodLabel.setText("Starting tree (best) : " + Tools.doubletoString(startingEvaluation, 4));
/*     */   }
/*     */ 
/*     */   public void showStageSearchStop(List<Tree> solutionTrees, Map<String, Double> evaluationsToShow)
/*     */   {
/* 645 */     if ((this.parameters.sufficientStopConditions.contains(Parameters.HeuristicStopCondition.STEPS)) || 
/* 646 */       (this.parameters.necessaryStopConditions.contains(Parameters.HeuristicStopCondition.STEPS))) {
/* 647 */       this.currentStep = (this.parameters.stopCriterionSteps - 1);
/* 648 */       showNextStep();
/* 649 */     } else if ((this.parameters.sufficientStopConditions.contains(Parameters.HeuristicStopCondition.AUTO)) || 
/* 650 */       (this.parameters.necessaryStopConditions.contains(Parameters.HeuristicStopCondition.AUTO))) {
/* 651 */       showAutoStopDone(this.parameters.stopCriterionAutoSteps);
/*     */     }
/* 653 */     if ((this.parameters.sufficientStopConditions.contains(Parameters.HeuristicStopCondition.TIME)) || 
/* 654 */       (this.parameters.necessaryStopConditions.contains(Parameters.HeuristicStopCondition.TIME)))
/* 655 */       showRemainingTime(0L);
/* 656 */     SwingUtilities.invokeLater(new Runnable() {
/*     */       public void run() {
/* 658 */         if (SearchBatchGraphicalMonitor.this.runProgressBar.isIndeterminate()) {
/* 659 */           SearchBatchGraphicalMonitor.this.runProgressBar.setIndeterminate(false);
/* 660 */           SearchBatchGraphicalMonitor.this.runProgressBar.setValue(SearchBatchGraphicalMonitor.this.runProgressBar.getMaximum());
/*     */         }
/*     */       }
/*     */     });
/* 664 */     SwingUtilities.invokeLater(new Runnable() {
/*     */       public void run() {
/* 666 */         if (SearchBatchGraphicalMonitor.this.timeLeftProgress.isIndeterminate()) {
/* 667 */           SearchBatchGraphicalMonitor.this.timeLeftProgress.setIndeterminate(false);
/* 668 */           SearchBatchGraphicalMonitor.this.timeLeftProgress.setValue(SearchBatchGraphicalMonitor.this.runProgressBar.getMaximum());
/*     */         }
/*     */       }
/*     */     });
/* 672 */     showEvaluations(evaluationsToShow);
/* 673 */     StringBuilder s = new StringBuilder();
/* 674 */     s.append(this.H.getName(true) + (this.parameters.hasManyReplicates() ? " (Replicate " + this.currentReplicate + ")" : "") + " finished at " + new Date(System.currentTimeMillis()).toString() + "\n");
/* 675 */     s.append("End after " + Tools.doubletoString((System.currentTimeMillis() - this.repStartTime) / 60000.0D, 2) + " minutes" + "\n");
/* 676 */     s.append("Best tree likelihood : " + Tools.doubletoString(((Double)evaluationsToShow.get("Best solution")).doubleValue(), 4) + "\n");
/* 677 */     showText(s.toString());
/* 678 */     this.parent.showBatchText(s.toString());
/* 679 */     end(solutionTrees);
/*     */   }
/*     */ 
/*     */   public void showStageStartingTree(int numOfSteps)
/*     */   {
/* 684 */     final int val = numOfSteps;
/* 685 */     SwingUtilities.invokeLater(new Runnable() {
/*     */       public void run() {
/* 687 */         SearchBatchGraphicalMonitor.this.runProgressBar.setMinimum(0);
/* 688 */         SearchBatchGraphicalMonitor.this.runProgressBar.setMaximum(val);
/* 689 */         SearchBatchGraphicalMonitor.this.runProgressBar.setValue(0);
/* 690 */         SearchBatchGraphicalMonitor.this.runProgressBar.setString("Building starting tree");
/* 691 */         SearchBatchGraphicalMonitor.this.runProgressBar.setStringPainted(true);
/*     */       }
/*     */     });
/*     */   }
/*     */ 
/*     */   public void showStartingTree(Tree tree)
/*     */   {
/*     */   }
/*     */ 
/*     */   public void showNextStep()
/*     */   {
/* 703 */     SwingUtilities.invokeLater(new Runnable() {
/*     */       public void run() {
/* 705 */         SearchBatchGraphicalMonitor.this.runProgressBar.setValue(++SearchBatchGraphicalMonitor.this.currentStep);
/* 706 */         SearchBatchGraphicalMonitor.this.runProgressBar.setStringPainted(true);
/* 707 */         if ((SearchBatchGraphicalMonitor.this.maxSteps > 0) && (!SearchBatchGraphicalMonitor.this.parameters.hasManyReplicates())) {
/* 708 */           int step = (int)(SearchBatchGraphicalMonitor.this.currentStep / SearchBatchGraphicalMonitor.this.maxSteps * 100.0D);
/* 709 */           SearchBatchGraphicalMonitor.this.parent.setTitle("Batch : " + SearchBatchGraphicalMonitor.this.runLabel + " : " + step + "% search");
/*     */         }
/*     */       }
/*     */     });
/*     */   }
/*     */ 
/*     */   public void showAutoStopDone(final int noChangeSteps)
/*     */   {
/* 717 */     SwingUtilities.invokeLater(new Runnable() {
/*     */       public void run() {
/* 719 */         SearchBatchGraphicalMonitor.this.runProgressBar.setValue(noChangeSteps);
/* 720 */         SearchBatchGraphicalMonitor.this.runProgressBar.setStringPainted(true);
/*     */       }
/*     */     });
/*     */   }
/*     */ 
/*     */   public void showText(String text)
/*     */   {
/* 727 */     this.parent.showText(text);
/*     */   }
/*     */ 
/*     */   private class TimeLeftProgressUpdate
/*     */     implements Runnable
/*     */   {
/*     */     long sec;
/*     */     long min;
/*     */     long h;
/*     */     long time;
/*     */ 
/*     */     public TimeLeftProgressUpdate(long time)
/*     */     {
/* 407 */       this.time = time;
/* 408 */       this.sec = (time / 1000L);
/* 409 */       this.h = (this.sec / 3600L);
/* 410 */       this.sec -= this.h * 3600L;
/* 411 */       this.min = (this.sec / 60L);
/* 412 */       this.sec -= this.min * 60L;
/*     */     }
/*     */     public void run() {
/* 415 */       SwingUtilities.invokeLater(new Runnable() {
/*     */         public void run() {
/* 417 */           SearchBatchGraphicalMonitor.this.timeLeftProgress.setValue((int)(()(SearchBatchGraphicalMonitor.this.parameters.stopCriterionTime * 3600.0D * 1000.0D) - TimeLeftProgressUpdate.this.time));
/* 418 */           SearchBatchGraphicalMonitor.this.timeLeftProgress.setString((int)TimeLeftProgressUpdate.this.h + "h " + (int)TimeLeftProgressUpdate.this.min + "m " + (int)TimeLeftProgressUpdate.this.sec + "s");
/* 419 */           SearchBatchGraphicalMonitor.this.timeLeftProgress.setStringPainted(true);
/*     */         }
/*     */       });
/* 422 */       if ((SearchBatchGraphicalMonitor.this.maxSteps == 0) && (!SearchBatchGraphicalMonitor.this.parameters.hasManyReplicates()))
/* 423 */         SearchBatchGraphicalMonitor.this.parent.setTitle("Batch : " + SearchBatchGraphicalMonitor.this.runLabel + " : " + (int)this.h + "h " + (int)this.min + "m " + (int)this.sec + "s" + " left");
/*     */     }
/*     */   }
/*     */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.monitors.SearchBatchGraphicalMonitor
 * JD-Core Version:    0.6.2
 */