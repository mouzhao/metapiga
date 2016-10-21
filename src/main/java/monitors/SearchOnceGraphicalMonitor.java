/*     */ package monitors;
/*     */ 
/*     */ import java.awt.BorderLayout;
/*     */ import java.awt.Color;
/*     */ import java.awt.Component;
/*     */ import java.awt.Dimension;
/*     */ import java.awt.FlowLayout;
/*     */ import java.awt.GridBagConstraints;
/*     */ import java.awt.GridBagLayout;
/*     */ import java.awt.Insets;
/*     */ import java.io.IOException;
/*     */ import java.util.Date;
/*     */ import java.util.HashMap;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Map.Entry;
/*     */ import java.util.Set;
/*     */ import java.util.TreeMap;
/*     */ import javax.swing.BorderFactory;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JLabel;
/*     */ import javax.swing.JPanel;
/*     */ import javax.swing.JProgressBar;
/*     */ import javax.swing.JScrollPane;
/*     */ import javax.swing.JViewport;
/*     */ import javax.swing.SwingUtilities;
/*     */ import javax.swing.border.TitledBorder;
/*     */ import metapiga.ScrollableFlowPanel;
/*     */ import metapiga.heuristics.Bootstrapping;
/*     */ import metapiga.heuristics.ConsensusPruning;
/*     */ import metapiga.heuristics.GeneticAlgorithm;
/*     */ import metapiga.heuristics.Heuristic;
/*     */ import metapiga.heuristics.HillClimbing;
/*     */ import metapiga.heuristics.SimulatedAnnealing;
/*     */ import metapiga.modelization.DistanceMatrix;
/*     */ import metapiga.parameters.Parameters;
/*     */ import metapiga.parameters.Parameters.Heuristic;
/*     */ import metapiga.parameters.Parameters.HeuristicStopCondition;
/*     */ import metapiga.parameters.Parameters.LogFile;
/*     */ import metapiga.parameters.Parameters.Operator;
/*     */ import metapiga.parameters.Parameters.SASchedule;
/*     */ import metapiga.trees.Consensus;
/*     */ import metapiga.trees.Tree;
/*     */ import metapiga.utilities.Tools;
/*     */ 
/*     */ public class SearchOnceGraphicalMonitor extends JPanel
/*     */   implements Monitor
/*     */ {
/*     */   private final boolean DATA;
/*     */   private final boolean DIST;
/*     */   private final boolean TREESTART;
/*     */   private final boolean HEUR;
/*     */   private final boolean TREEHEUR;
/*     */   private final boolean CONSENSUS;
/*     */   private final boolean OPDETAILS;
/*     */   private final boolean OPSTATS;
/*     */   private final boolean ANCSEQ;
/*     */   private final boolean PERF;
/*  52 */   private final String likelihoodTitle = "Negative likelihood progression in current replicate : ";
/*     */   private final SearchOnceGraphical parent;
/*     */   private final Parameters parameters;
/*     */   private final PrintMonitor print;
/*  57 */   private final Map<String, JLabel> curves = new TreeMap();
/*     */   private Heuristic H;
/*     */   private Thread thread;
/*  60 */   private int maxSteps = 0;
/*     */   private int currentStep;
/*     */   private int currentReplicate;
/*     */   private long repStartTime;
/*  64 */   private String progressBarSaveString = "";
/*     */ 
/*  66 */   private final JPanel progressPanel = new JPanel();
/*  67 */   private final JProgressBar progressBar = new JProgressBar();
/*  68 */   private final JLabel progressLabel = new JLabel();
/*  69 */   private final JLabel timeLeftLabel = new JLabel();
/*  70 */   private final JLabel timeLabel = new JLabel();
/*  71 */   private final JPanel likelihoodPanel = new JPanel();
/*  72 */   private final JScrollPane likelihoodGraphScrollPane = new JScrollPane();
/*  73 */   private final JScrollPane graphYAxisScrollPane = new JScrollPane();
/*     */   private Graph likelihoodGraph;
/*     */   private GraphY graphYAxis;
/*  76 */   private final JPanel coolingSchedulePanel = new JPanel();
/*     */   private Graph coolingScheduleGraph;
/*  78 */   private final JScrollPane coolingScheduleGraphScrollPane = new JScrollPane();
/*  79 */   private final GridBagLayout gridBagLayout1 = new GridBagLayout();
/*  80 */   private final GridBagLayout gridBagLayout2 = new GridBagLayout();
/*     */   private TitledBorder titledBorder1;
/*     */   private TitledBorder titledBorder3;
/*  83 */   private final BorderLayout borderLayout4 = new BorderLayout();
/*  84 */   private ScrollableFlowPanel curvesPanel = null;
/*     */ 
/*     */   public SearchOnceGraphicalMonitor(SearchOnceGraphical parent, String runLabel) {
/*  87 */     this.parent = parent;
/*  88 */     this.parameters = parent.parameters;
/*  89 */     this.DATA = this.parameters.logFiles.contains(Parameters.LogFile.DATA);
/*  90 */     this.DIST = this.parameters.logFiles.contains(Parameters.LogFile.DIST);
/*  91 */     this.TREESTART = this.parameters.logFiles.contains(Parameters.LogFile.TREESTART);
/*  92 */     this.HEUR = this.parameters.logFiles.contains(Parameters.LogFile.HEUR);
/*  93 */     this.TREEHEUR = this.parameters.logFiles.contains(Parameters.LogFile.TREEHEUR);
/*  94 */     this.CONSENSUS = this.parameters.logFiles.contains(Parameters.LogFile.CONSENSUS);
/*  95 */     this.OPDETAILS = this.parameters.logFiles.contains(Parameters.LogFile.OPDETAILS);
/*  96 */     this.OPSTATS = this.parameters.logFiles.contains(Parameters.LogFile.OPSTATS);
/*  97 */     this.ANCSEQ = this.parameters.logFiles.contains(Parameters.LogFile.ANCSEQ);
/*  98 */     this.PERF = this.parameters.logFiles.contains(Parameters.LogFile.PERF);
/*  99 */     this.print = new PrintMonitor(this, runLabel);
/* 100 */     this.print.setParameters(this.parameters);
/*     */     try {
/* 102 */       initGraphs();
/* 103 */       jbInit();
/*     */     }
/*     */     catch (Exception ex) {
/* 106 */       ex.printStackTrace();
/*     */     }
/*     */   }
/*     */ 
/*     */   public Monitor.MonitorType getMonitorType()
/*     */   {
/* 112 */     return Monitor.MonitorType.SINGLE_SEARCH_GRAPHICAL;
/*     */   }
/*     */ 
/*     */   private void initGraphs() {
/* 116 */     this.graphYAxis = new GraphY(true, 20);
/* 117 */     switch ($SWITCH_TABLE$metapiga$parameters$Parameters$Heuristic()[this.parameters.heuristic.ordinal()]) {
/*     */     case 1:
/* 119 */       if (this.parameters.hcRestart > 0) {
/* 120 */         this.likelihoodGraph = new Graph(new String[] { "Best solution", "Current solution" }, 1, true, false, true, 100, this.graphYAxis);
/* 121 */         this.curves.put("Best solution", new JLabel("Best solution : "));
/* 122 */         this.curves.put("Current solution", new JLabel("Current solution : "));
/*     */       } else {
/* 124 */         this.likelihoodGraph = new Graph(new String[] { "Best solution" }, 0, true, false, true, 100, this.graphYAxis);
/* 125 */         this.curves.put("Best solution", new JLabel("Best solution : "));
/*     */       }
/* 127 */       break;
/*     */     case 2:
/* 129 */       this.likelihoodGraph = new Graph(new String[] { "Best solution", "Current solution" }, 0, true, false, true, 100, this.graphYAxis);
/* 130 */       this.curves.put("Best solution", new JLabel("Best solution : "));
/* 131 */       this.curves.put("Current solution", new JLabel("Current solution : "));
/* 132 */       this.coolingScheduleGraph = new Graph(new String[] { "Temperature" }, 0, false, false, false, 100, null);
/* 133 */       break;
/*     */     case 3:
/* 135 */       this.likelihoodGraph = new Graph(new String[] { "Best solution" }, 0, true, false, true, 100, this.graphYAxis);
/* 136 */       this.curves.put("Best solution", new JLabel("Best solution : "));
/* 137 */       break;
/*     */     case 4:
/* 139 */       String[] curvesNames = new String[this.parameters.cpPopNum + 1];
/* 140 */       curvesNames[0] = "Best solution";
/* 141 */       this.curves.put("Best solution", new JLabel("Best solution : "));
/* 142 */       for (int i = 1; i < curvesNames.length; i++) {
/* 143 */         curvesNames[i] = ("Population " + (i - 1));
/* 144 */         this.curves.put("Population " + (i - 1), new JLabel("Population " + (i - 1) + " : "));
/*     */       }
/* 146 */       this.likelihoodGraph = new Graph(curvesNames, 0, true, false, true, 100, this.graphYAxis);
/* 147 */       break;
/*     */     case 5:
/* 149 */       this.likelihoodGraph = new Graph(new String[] { "Best solution" }, 0, true, false, true, 100, this.graphYAxis);
/* 150 */       this.curves.put("Best solution", new JLabel("Best solution : "));
/*     */     }
/*     */ 
/* 153 */     this.curves.put("Starting tree (best)", new JLabel("Starting tree (best) : "));
/*     */   }
/*     */ 
/*     */   private void jbInit() {
/* 157 */     this.titledBorder1 = new TitledBorder(BorderFactory.createEtchedBorder(Color.white, new Color(165, 163, 151)), "Negative likelihood progression in current replicate : ");
/* 158 */     this.titledBorder3 = new TitledBorder(BorderFactory.createEtchedBorder(Color.white, new Color(165, 163, 151)), "Cooling schedule (" + this.parameters.saSchedule.verbose() + ")");
/* 159 */     setLayout(this.gridBagLayout1);
/* 160 */     this.progressPanel.setLayout(this.gridBagLayout2);
/* 161 */     this.progressLabel.setText("Progess : ");
/* 162 */     this.timeLeftLabel.setText("Time left before stopping : ");
/* 163 */     this.timeLabel.setText("No time stop");
/* 164 */     this.likelihoodPanel.setBorder(this.titledBorder1);
/* 165 */     this.likelihoodPanel.setDebugGraphicsOptions(0);
/* 166 */     this.likelihoodPanel.setLayout(new BorderLayout());
/* 167 */     this.likelihoodGraph.setPreferredSize(new Dimension(500, 100));
/* 168 */     this.graphYAxis.setPreferredSize(new Dimension(70, 100));
/* 169 */     add(this.progressPanel, new GridBagConstraints(0, 0, 1, 1, 1.0D, 0.0D, 
/* 170 */       10, 2, new Insets(5, 10, 5, 0), 0, 0));
/* 171 */     this.progressPanel.add(this.progressBar, new GridBagConstraints(1, 0, 1, 1, 1.0D, 1.0D, 
/* 172 */       10, 1, new Insets(5, 0, 5, 0), 0, 0));
/* 173 */     this.progressPanel.add(this.progressLabel, new GridBagConstraints(0, 0, 1, 1, 0.0D, 0.0D, 
/* 174 */       10, 0, new Insets(0, 5, 0, 5), 0, 0));
/* 175 */     this.progressPanel.add(this.timeLeftLabel, new GridBagConstraints(2, 0, 1, 1, 0.0D, 0.0D, 
/* 176 */       10, 0, new Insets(0, 15, 0, 0), 0, 0));
/* 177 */     this.progressPanel.add(this.timeLabel, new GridBagConstraints(3, 0, 1, 1, 0.0D, 0.0D, 
/* 178 */       10, 2, new Insets(0, 5, 0, 15), 0, 0));
/* 179 */     add(this.likelihoodPanel, new GridBagConstraints(0, 1, 1, 1, 1.0D, 1.0D, 
/* 180 */       10, 1, new Insets(5, 10, 5, 10), 0, 0));
/* 181 */     this.likelihoodGraphScrollPane.setHorizontalScrollBarPolicy(32);
/* 182 */     this.likelihoodGraphScrollPane.setVerticalScrollBarPolicy(21);
/* 183 */     this.likelihoodGraphScrollPane.getViewport().add(this.likelihoodGraph, null);
/* 184 */     this.likelihoodGraphScrollPane.setBorder(null);
/* 185 */     this.graphYAxisScrollPane.setHorizontalScrollBarPolicy(32);
/* 186 */     this.graphYAxisScrollPane.setVerticalScrollBarPolicy(21);
/* 187 */     this.graphYAxisScrollPane.getViewport().add(this.graphYAxis, null);
/* 188 */     this.graphYAxisScrollPane.setBorder(null);
/* 189 */     for (Entry e : this.curves.entrySet()) {
/* 190 */       JPanel curveSubPanel = new JPanel();
/* 191 */       JButton coloredSquare = new JButton();
/* 192 */       coloredSquare.setPreferredSize(new Dimension(15, 15));
/* 193 */       coloredSquare.setBackground(this.likelihoodGraph.getColor((String)e.getKey()));
/* 194 */       if (((String)e.getKey()).equals("Starting tree (best)")) coloredSquare.setBackground(Color.red);
/* 195 */       coloredSquare.setBorder(BorderFactory.createRaisedBevelBorder());
/* 196 */       coloredSquare.setContentAreaFilled(false);
/* 197 */       coloredSquare.setOpaque(true);
/* 198 */       curveSubPanel.add(coloredSquare);
/* 199 */       curveSubPanel.add((Component)e.getValue());
/* 200 */       getCurvesPanel().add(curveSubPanel);
/*     */     }
/* 202 */     this.likelihoodPanel.add(getCurvesPanel(), "North");
/* 203 */     this.likelihoodPanel.add(this.likelihoodGraphScrollPane, "Center");
/* 204 */     this.likelihoodPanel.add(this.graphYAxisScrollPane, "West");
/* 205 */     if (this.parameters.heuristic == Parameters.Heuristic.SA) {
/* 206 */       this.coolingScheduleGraph.setPreferredSize(new Dimension(500, 100));
/* 207 */       this.coolingSchedulePanel.setLayout(this.borderLayout4);
/* 208 */       this.coolingSchedulePanel.setBorder(this.titledBorder3);
/* 209 */       this.coolingScheduleGraphScrollPane.setHorizontalScrollBarPolicy(32);
/* 210 */       this.coolingScheduleGraphScrollPane.setVerticalScrollBarPolicy(21);
/* 211 */       this.coolingScheduleGraphScrollPane.getViewport().add(this.coolingScheduleGraph, null);
/* 212 */       this.coolingSchedulePanel.add(this.coolingScheduleGraphScrollPane, "Center");
/* 213 */       add(this.coolingSchedulePanel, new GridBagConstraints(0, 3, 1, 1, 1.0D, 0.75D, 
/* 214 */         10, 1, new Insets(5, 10, 5, 10), 0, 0));
/*     */     }
/*     */   }
/*     */ 
/*     */   private ScrollableFlowPanel getCurvesPanel()
/*     */   {
/* 224 */     if (this.curvesPanel == null) {
/* 225 */       this.curvesPanel = new ScrollableFlowPanel();
/* 226 */       this.curvesPanel.setLayout(new FlowLayout(3));
/*     */     }
/* 228 */     return this.curvesPanel;
/*     */   }
/*     */   public final boolean trackDataMatrix() {
/* 231 */     return this.DATA; } 
/* 232 */   public final boolean trackDistances() { return this.DIST; } 
/* 233 */   public final boolean trackStartingTree() { return this.TREESTART; } 
/* 234 */   public final boolean trackHeuristic() { return this.HEUR; } 
/* 235 */   public final boolean trackHeuristicTrees() { return this.TREEHEUR; } 
/* 236 */   public final boolean trackConsensus() { return this.CONSENSUS; } 
/* 237 */   public final boolean trackOperators() { return this.OPDETAILS; } 
/* 238 */   public final boolean trackOperatorStats() { return this.OPSTATS; } 
/* 239 */   public final boolean trackPerformances() { return this.PERF; } 
/* 240 */   public final boolean trackAncestralSequences() { return this.ANCSEQ; }
/*     */ 
/*     */   public void run()
/*     */   {
/*     */     try {
/* 245 */       if (trackDataMatrix()) printDataMatrix();
/*     */       int j;
/*     */       int i;
/* 246 */       for (; (this.currentReplicate = this.parent.getNextReplicate()) > 0; 
/* 277 */         i < j)
/*     */       {
/* 247 */         this.print.initLogFiles(this.currentReplicate);
/* 248 */         this.likelihoodGraph.newReplicate(this.currentReplicate);
/* 249 */         if (this.parameters.heuristic == Parameters.Heuristic.SA)
/* 250 */           this.coolingScheduleGraph.newReplicate(this.currentReplicate);
/* 251 */         switch ($SWITCH_TABLE$metapiga$parameters$Parameters$Heuristic()[this.parameters.heuristic.ordinal()]) {
/*     */         case 1:
/* 253 */           this.H = new HillClimbing(this.parameters, this);
/* 254 */           break;
/*     */         case 2:
/* 256 */           this.H = new SimulatedAnnealing(this.parameters, this);
/* 257 */           break;
/*     */         case 3:
/* 259 */           this.H = new GeneticAlgorithm(this.parameters, this);
/* 260 */           break;
/*     */         case 4:
/* 262 */           this.H = new ConsensusPruning(this.parameters, this);
/* 263 */           break;
/*     */         case 5:
/* 265 */           this.H = new Bootstrapping(this.parameters, this); } 
/*     */ this.thread = new Thread(this.H, this.H.getName(true) + "-Rep-" + this.currentReplicate);
/* 269 */         this.thread.start();
/* 270 */         this.thread.join();
/*     */         StackTraceElement[] arrayOfStackTraceElement;
/*     */         try { this.print.closeOutputFiles();
/*     */         } catch (IOException e) {
/* 274 */           e.printStackTrace();
/* 275 */           showText("\n Error when closing log files");
/* 276 */           showText("\n Java exception : " + e.getCause() + " (" + e.getMessage() + ")");
/* 277 */           j = (arrayOfStackTraceElement = e.getStackTrace()).length; i = 0; continue; } StackTraceElement el = arrayOfStackTraceElement[i];
/* 278 */         showText("\tat " + el.toString());
/*     */ 
/* 277 */         i++;
/*     */       }
/*     */ 
/* 282 */       SwingUtilities.invokeLater(new Runnable() {
/*     */         public void run() {
/* 284 */           SearchOnceGraphicalMonitor.this.progressBar.setValue(SearchOnceGraphicalMonitor.this.progressBar.getMaximum());
/* 285 */           SearchOnceGraphicalMonitor.this.progressBar.setString("job finished");
/* 286 */           SearchOnceGraphicalMonitor.this.progressBar.setStringPainted(true);
/*     */         } } );
/*     */     }
/*     */     catch (Exception e) {
/* 290 */       endFromException(e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void end(List<Tree> solutionTrees)
/*     */   {
/* 296 */     if (this.parameters.hasManyReplicates()) {
/* 297 */       for (Tree tree : solutionTrees) {
/* 298 */         tree.setName(tree.getName() + "_Rep_" + this.currentReplicate);
/*     */       }
/*     */     }
/*     */ 
/* 302 */     if (this.ANCSEQ) printAncestralSequences(solutionTrees);
/* 303 */     for (Tree tree : solutionTrees) {
/* 304 */       tree.deleteLikelihoodComputation();
/*     */     }
/* 306 */     this.parent.addSolutionTree(solutionTrees);
/*     */   }
/*     */ 
/*     */   public void endFromException(Exception e)
/*     */   {
/* 311 */     this.parent.endFromException(e);
/*     */   }
/*     */ 
/*     */   public void printAncestralSequences(List<Tree> trees)
/*     */   {
/* 316 */     this.print.printAncestralSequences(trees);
/*     */   }
/*     */ 
/*     */   public void printDataMatrix()
/*     */   {
/* 321 */     this.print.printDataMatrix();
/*     */   }
/*     */ 
/*     */   public void printDistanceMatrix(DistanceMatrix dm) {
/* 325 */     this.print.printDistanceMatrix(dm);
/*     */   }
/*     */ 
/*     */   public void printStartingTrees(List<Tree> startingTrees) {
/* 329 */     this.print.printStartingTrees(startingTrees, this.currentReplicate);
/*     */   }
/*     */ 
/*     */   public void printTreeBeforeOperator(Tree tree, Parameters.Operator operator, boolean consensus) {
/* 333 */     this.print.printTreeBeforeOperator(tree, operator, consensus);
/*     */   }
/*     */ 
/*     */   public void printOperatorInfos(Tree tree, String infos) {
/* 337 */     this.print.printOperatorInfos(tree, infos);
/*     */   }
/*     */ 
/*     */   public void printOperatorInfos(Tree tree, String infos, Consensus consensus) {
/* 341 */     this.print.printOperatorInfos(tree, infos, consensus);
/*     */   }
/*     */ 
/*     */   public void printTreeAfterOperator(Tree tree, Parameters.Operator operator, boolean consensus) {
/* 345 */     this.print.printTreeAfterOperator(tree, operator, consensus);
/*     */   }
/*     */ 
/*     */   public void printOperatorFrequenciesUpdate(int currentStep, Map<Parameters.Operator, Integer> use, Map<Parameters.Operator, Double> scoreImprovements, Map<Parameters.Operator, Long> performances) {
/* 349 */     this.print.printOperatorFrequenciesUpdate(currentStep, use, scoreImprovements, performances);
/*     */   }
/*     */ 
/*     */   public void printOperatorFrequenciesUpdate(Map<Parameters.Operator, Double> frequencies) {
/* 353 */     this.print.printOperatorFrequenciesUpdate(frequencies);
/*     */   }
/*     */ 
/*     */   public void printOperatorStatistics(int numStep, Map<Parameters.Operator, Integer> use, Map<Parameters.Operator, Double> scoreImprovements, Map<Parameters.Operator, Long> performances, int outgroupTargeted, int ingroupTargeted, Map<Parameters.Operator, Map<Integer, Integer>> cancelByConsensus)
/*     */   {
/* 358 */     this.print.printOperatorStatistics(numStep, use, scoreImprovements, performances, outgroupTargeted, ingroupTargeted, cancelByConsensus);
/*     */   }
/*     */ 
/*     */   public void printDetailsHC(int step, double bestLikelihood, double currentLikelihood, Parameters.Operator operator, boolean improvement) {
/* 362 */     this.print.printDetailsHC(step, bestLikelihood, currentLikelihood, operator, improvement);
/*     */   }
/*     */ 
/*     */   public void printDetailsSA(int step, double bestLikelihood, double S0Likelihood, double currentLikelihood, Parameters.Operator operator, String status, double tempAcceptance, double temperature, int coolingSteps, int successes, int failures, int reheatingDecrements)
/*     */   {
/* 367 */     this.print.printDetailsSA(step, bestLikelihood, S0Likelihood, currentLikelihood, operator, status, tempAcceptance, temperature, coolingSteps, successes, failures, reheatingDecrements);
/*     */   }
/*     */ 
/*     */   public void printDetailsGA(int step, Tree[] mutantML, Parameters.Operator[] operator) {
/* 371 */     this.print.printDetailsGA(step, mutantML, operator);
/*     */   }
/*     */ 
/*     */   public void printDetailsGA(int step, String selectionDetails, Tree[] selectedML, double bestLikelihood) {
/* 375 */     this.print.printDetailsGA(step, selectionDetails, selectedML, bestLikelihood);
/*     */   }
/*     */ 
/*     */   public void printDetailsCP(Tree[] mutantML, Parameters.Operator[] operator, int currentPop) {
/* 379 */     this.print.printDetailsCP(mutantML, operator, currentPop);
/*     */   }
/*     */ 
/*     */   public void printDetailsCP(Tree[] hybridML, String[] parents, int currentPop) {
/* 383 */     this.print.printDetailsCP(hybridML, parents, currentPop);
/*     */   }
/*     */ 
/*     */   public void printDetailsCP(int step, String[] selectionDetails, Tree[][] selectedML, double bestLikelihood) {
/* 387 */     this.print.printDetailsCP(step, selectionDetails, selectedML, bestLikelihood);
/*     */   }
/*     */ 
/*     */   public void printTreesHC(int step, Tree bestTree, Tree currentTree) {
/* 391 */     this.print.printTreesHC(step, bestTree, currentTree);
/*     */   }
/*     */ 
/*     */   public void printTreesSA(int step, Tree bestTree, Tree S0Tree, Tree currentTree) {
/* 395 */     this.print.printTreesSA(step, bestTree, S0Tree, currentTree);
/*     */   }
/*     */ 
/*     */   public void printTreesGA(int step, Tree[] trees, boolean selectionDone) {
/* 399 */     this.print.printTreesGA(step, trees, selectionDone);
/*     */   }
/*     */ 
/*     */   public void printTreesCP(int step, Tree[] trees, int pop, boolean recombined) {
/* 403 */     this.print.printTreesCP(step, trees, pop, recombined);
/*     */   }
/*     */ 
/*     */   public void printTreesCP(int step, Tree[][] trees) {
/* 407 */     this.print.printTreesCP(step, trees);
/*     */   }
/*     */ 
/*     */   public void printEndTreesHeuristic() {
/* 411 */     this.print.printEndTreesHeuristic();
/*     */   }
/*     */ 
/*     */   public void printConsensus(int step, Consensus consensus) {
/* 415 */     this.print.printConsensus(step, consensus);
/*     */   }
/*     */ 
/*     */   public void trackPerformances(String action, int level) {
/* 419 */     this.print.trackPerformances(action, level);
/*     */   }
/*     */ 
/*     */   public void updateConsensusTree(Tree consensusTree) {
/* 423 */     this.print.updateConsensusTree(consensusTree);
/*     */   }
/*     */ 
/*     */   public void showCurrentCoolingSchedule(final Parameters.SASchedule currentCoolingSchedule)
/*     */   {
/* 428 */     showText("Changed cooling schedule to " + currentCoolingSchedule + " at " + new Date(System.currentTimeMillis()).toString());
/* 429 */     SwingUtilities.invokeLater(new Runnable() {
/*     */       public void run() {
/* 431 */         SearchOnceGraphicalMonitor.this.titledBorder3.setTitle("Cooling schedule (" + currentCoolingSchedule + ")");
/* 432 */         SearchOnceGraphicalMonitor.this.coolingSchedulePanel.updateUI();
/*     */       }
/*     */     });
/*     */   }
/*     */ 
/*     */   public void showCurrentTemperature(double currentTemperature)
/*     */   {
/* 439 */     Map temperMap = new HashMap();
/* 440 */     temperMap.put("Temperature", Double.valueOf(currentTemperature));
/* 441 */     if (!this.coolingScheduleGraph.addPoints(temperMap))
/* 442 */       SwingUtilities.invokeLater(new Runnable() {
/*     */         public void run() {
/* 444 */           SearchOnceGraphicalMonitor.this.coolingScheduleGraphScrollPane.getViewport().remove(SearchOnceGraphicalMonitor.this.coolingScheduleGraph);
/* 445 */           SearchOnceGraphicalMonitor.this.coolingScheduleGraph.setPreferredSize(new Dimension(SearchOnceGraphicalMonitor.this.coolingScheduleGraph.getSize().width + 300, SearchOnceGraphicalMonitor.this.coolingScheduleGraph.getPreferredSize().height));
/* 446 */           SearchOnceGraphicalMonitor.this.coolingScheduleGraphScrollPane.getViewport().add(SearchOnceGraphicalMonitor.this.coolingScheduleGraph, null);
/* 447 */           SearchOnceGraphicalMonitor.this.coolingScheduleGraph.repaint();
/* 448 */           SearchOnceGraphicalMonitor.this.coolingScheduleGraphScrollPane.getViewport().setViewPosition(SearchOnceGraphicalMonitor.this.coolingScheduleGraph.getViewPosition());
/*     */         }
/*     */       });
/*     */   }
/*     */ 
/*     */   public void showCurrentMRE(final double currentMRE)
/*     */   {
/* 456 */     SwingUtilities.invokeLater(new Runnable() {
/*     */       public void run() {
/* 458 */         SearchOnceGraphicalMonitor.this.titledBorder1.setTitle("Negative likelihood progression in current replicate : (Inter-generation MRE of " + Tools.doubleToPercent(currentMRE, 2) + ")");
/* 459 */         SearchOnceGraphicalMonitor.this.likelihoodPanel.repaint();
/*     */       }
/*     */     });
/*     */   }
/*     */ 
/*     */   public void showCurrentTree(Tree tree)
/*     */   {
/* 466 */     this.parent.showCurrentTree(tree);
/*     */   }
/*     */ 
/*     */   public void showEvaluations(Map<String, Double> evaluationsToShow)
/*     */   {
/* 471 */     for (Entry e : evaluationsToShow.entrySet()) {
/* 472 */       ((JLabel)this.curves.get(e.getKey())).setText((String)e.getKey() + " : " + Tools.doubletoString(((Double)e.getValue()).doubleValue(), 4));
/*     */     }
/* 474 */     if (!this.likelihoodGraph.addPoints(evaluationsToShow))
/* 475 */       SwingUtilities.invokeLater(new Runnable() {
/*     */         public void run() {
/* 477 */           SearchOnceGraphicalMonitor.this.likelihoodGraphScrollPane.getViewport().remove(SearchOnceGraphicalMonitor.this.likelihoodGraph);
/* 478 */           SearchOnceGraphicalMonitor.this.likelihoodGraph.setPreferredSize(new Dimension(SearchOnceGraphicalMonitor.this.likelihoodGraph.getSize().width + 300, SearchOnceGraphicalMonitor.this.likelihoodGraph.getPreferredSize().height));
/* 479 */           SearchOnceGraphicalMonitor.this.likelihoodGraphScrollPane.getViewport().add(SearchOnceGraphicalMonitor.this.likelihoodGraph, null);
/* 480 */           SearchOnceGraphicalMonitor.this.likelihoodGraph.repaint();
/* 481 */           SearchOnceGraphicalMonitor.this.likelihoodGraphScrollPane.getViewport().setViewPosition(SearchOnceGraphicalMonitor.this.likelihoodGraph.getViewPosition());
/*     */         }
/*     */       });
/*     */   }
/*     */ 
/*     */   public void showNextStep()
/*     */   {
/* 489 */     SwingUtilities.invokeLater(new Runnable() {
/*     */       public void run() {
/* 491 */         SearchOnceGraphicalMonitor.this.progressBar.setValue(++SearchOnceGraphicalMonitor.this.currentStep);
/* 492 */         SearchOnceGraphicalMonitor.this.progressBar.setStringPainted(true);
/*     */       }
/*     */     });
/* 495 */     if (this.maxSteps > 0) {
/* 496 */       int step = (int)(this.currentStep / this.maxSteps * 100.0D);
/* 497 */       if (!this.parameters.hasManyReplicates()) this.parent.setTitle(step + "% search");
/*     */     }
/*     */   }
/*     */ 
/*     */   public void showRemainingTime(long time)
/*     */   {
/* 503 */     long sec = time / 1000L;
/* 504 */     final long h = sec / 3600L;
/* 505 */     sec -= h * 3600L;
/* 506 */     long min = sec / 60L;
/* 507 */     sec -= min * 60L;
/* 508 */     final long s = sec;
/* 509 */     SwingUtilities.invokeLater(new Runnable() {
/*     */       public void run() {
/* 511 */         SearchOnceGraphicalMonitor.this.timeLabel.setText((int)h + "h " + (int)s + "m " + (int)this.val$s + "s");
/*     */       }
/*     */     });
/*     */   }
/*     */ 
/*     */   public void showAutoStopDone(int noChangeSteps)
/*     */   {
/* 518 */     final int val = noChangeSteps;
/* 519 */     SwingUtilities.invokeLater(new Runnable() {
/*     */       public void run() {
/* 521 */         SearchOnceGraphicalMonitor.this.progressBar.setValue(val);
/* 522 */         SearchOnceGraphicalMonitor.this.progressBar.setStringPainted(true);
/*     */       }
/*     */     });
/*     */   }
/*     */ 
/*     */   public void showReplicate()
/*     */   {
/* 529 */     this.parent.showReplicate();
/*     */   }
/*     */ 
/*     */   public void showStageCPMetapopulation(int numOfSteps)
/*     */   {
/* 534 */     final int val = numOfSteps;
/* 535 */     SwingUtilities.invokeLater(new Runnable() {
/*     */       public void run() {
/* 537 */         SearchOnceGraphicalMonitor.this.progressBar.setMinimum(0);
/* 538 */         SearchOnceGraphicalMonitor.this.progressBar.setMaximum(val);
/* 539 */         SearchOnceGraphicalMonitor.this.progressBar.setValue(0);
/* 540 */         SearchOnceGraphicalMonitor.this.progressBar.setString("Creating metapopulation");
/* 541 */         SearchOnceGraphicalMonitor.this.progressBar.setStringPainted(true);
/*     */       }
/*     */     });
/*     */   }
/*     */ 
/*     */   public void showStageDistanceMatrix(int numOfSteps)
/*     */   {
/* 548 */     final int val = numOfSteps;
/* 549 */     SwingUtilities.invokeLater(new Runnable() {
/*     */       public void run() {
/* 551 */         SearchOnceGraphicalMonitor.this.progressBar.setMinimum(0);
/* 552 */         SearchOnceGraphicalMonitor.this.progressBar.setMaximum(val);
/* 553 */         SearchOnceGraphicalMonitor.this.progressBar.setValue(0);
/* 554 */         SearchOnceGraphicalMonitor.this.progressBar.setString("Building distance matrix");
/* 555 */         SearchOnceGraphicalMonitor.this.progressBar.setStringPainted(true);
/*     */       }
/*     */     });
/*     */   }
/*     */ 
/*     */   public void showStageGAPopulation(int numOfSteps)
/*     */   {
/* 562 */     final int val = numOfSteps;
/* 563 */     SwingUtilities.invokeLater(new Runnable() {
/*     */       public void run() {
/* 565 */         SearchOnceGraphicalMonitor.this.progressBar.setMinimum(0);
/* 566 */         SearchOnceGraphicalMonitor.this.progressBar.setMaximum(val);
/* 567 */         SearchOnceGraphicalMonitor.this.progressBar.setValue(0);
/* 568 */         SearchOnceGraphicalMonitor.this.progressBar.setString("Creating population");
/* 569 */         SearchOnceGraphicalMonitor.this.progressBar.setStringPainted(true);
/*     */       }
/*     */     });
/*     */   }
/*     */ 
/*     */   public void showStageOptimization(int active, String target)
/*     */   {
/* 576 */     final String val = target;
/* 577 */     if (active > 0)
/* 578 */       SwingUtilities.invokeLater(new Runnable() {
/*     */         public void run() {
/* 580 */           SearchOnceGraphicalMonitor.this.progressBarSaveString = SearchOnceGraphicalMonitor.this.progressBar.getString();
/* 581 */           SearchOnceGraphicalMonitor.this.progressBar.setString("Intra-step optimization of " + val);
/* 582 */           SearchOnceGraphicalMonitor.this.progressBar.setStringPainted(true);
/*     */         }
/*     */       });
/*     */     else
/* 586 */       SwingUtilities.invokeLater(new Runnable() {
/*     */         public void run() {
/* 588 */           SearchOnceGraphicalMonitor.this.progressBar.setString(SearchOnceGraphicalMonitor.this.progressBarSaveString);
/* 589 */           SearchOnceGraphicalMonitor.this.progressBar.setStringPainted(true);
/*     */         }
/*     */       });
/*     */   }
/*     */ 
/*     */   public void showStageSATemperature(int numOfSteps)
/*     */   {
/* 597 */     final int val = numOfSteps;
/* 598 */     SwingUtilities.invokeLater(new Runnable() {
/*     */       public void run() {
/* 600 */         SearchOnceGraphicalMonitor.this.progressBar.setMinimum(0);
/* 601 */         SearchOnceGraphicalMonitor.this.progressBar.setMaximum(val);
/* 602 */         SearchOnceGraphicalMonitor.this.progressBar.setValue(0);
/* 603 */         SearchOnceGraphicalMonitor.this.progressBar.setString("Setting temperature");
/* 604 */         SearchOnceGraphicalMonitor.this.progressBar.setStringPainted(true);
/*     */       }
/*     */     });
/*     */   }
/*     */ 
/*     */   public void showStageHCRestart()
/*     */   {
/* 611 */     this.likelihoodGraph.restart();
/*     */   }
/*     */ 
/*     */   public void showStageSearchProgress(int currentIteration, long remainingTime, int noChangeSteps)
/*     */   {
/* 616 */     if ((this.parameters.sufficientStopConditions.contains(Parameters.HeuristicStopCondition.STEPS)) || 
/* 617 */       (this.parameters.necessaryStopConditions.contains(Parameters.HeuristicStopCondition.STEPS))) showNextStep();
/* 618 */     else if ((this.parameters.sufficientStopConditions.contains(Parameters.HeuristicStopCondition.AUTO)) || 
/* 619 */       (this.parameters.necessaryStopConditions.contains(Parameters.HeuristicStopCondition.AUTO))) showAutoStopDone(noChangeSteps);
/* 620 */     if ((this.parameters.sufficientStopConditions.contains(Parameters.HeuristicStopCondition.TIME)) || 
/* 621 */       (this.parameters.necessaryStopConditions.contains(Parameters.HeuristicStopCondition.TIME))) showRemainingTime(remainingTime);
/* 622 */     final int val = currentIteration;
/* 623 */     SwingUtilities.invokeLater(new Runnable() {
/*     */       public void run() {
/* 625 */         String stage = SearchOnceGraphicalMonitor.this.progressBar.getString();
/* 626 */         if (stage.indexOf(" - ") > 0) stage = stage.substring(0, stage.indexOf(" - "));
/* 627 */         SearchOnceGraphicalMonitor.this.progressBar.setString(stage + " - iteration " + val);
/* 628 */         SearchOnceGraphicalMonitor.this.progressBar.setStringPainted(true);
/*     */       }
/*     */     });
/*     */   }
/*     */ 
/*     */   public void showStageSearchStart(String heuristic, int maxSteps, double startingEvaluation)
/*     */   {
/* 635 */     this.repStartTime = System.currentTimeMillis();
/* 636 */     StringBuilder s = new StringBuilder();
/* 637 */     if ((this.parameters.sufficientStopConditions.isEmpty()) && (this.parameters.necessaryStopConditions.isEmpty()) && (this.parameters.heuristic != Parameters.Heuristic.BS))
/* 638 */       s.append("Don't start " + heuristic + " as user commands" + "\n");
/*     */     else {
/* 640 */       s.append(heuristic + (this.parameters.hasManyReplicates() ? " (Replicate " + this.currentReplicate + ")" : "") + " started at " + new Date(this.repStartTime).toString() + "\n");
/*     */     }
/* 642 */     if (!this.parameters.sufficientStopConditions.isEmpty()) {
/* 643 */       s.append("Stop when ANY of the following sufficient conditions is met: \n");
/* 644 */       for (Parameters.HeuristicStopCondition condition : this.parameters.sufficientStopConditions) {
/* 645 */         switch (condition) {
/*     */         case AUTO:
/* 647 */           s.append("- " + this.parameters.stopCriterionSteps + " iterations performed" + "\n");
/* 648 */           break;
/*     */         case CONSENSUS:
/* 650 */           s.append("- Pass over " + new Date(this.repStartTime + ()this.parameters.stopCriterionTime * 3600L * 1000L).toString() + "\n");
/* 651 */           break;
/*     */         case STEPS:
/* 653 */           s.append("- Likelihood stop increasing after " + this.parameters.stopCriterionAutoSteps + " iterations" + "\n");
/* 654 */           break;
/*     */         case TIME:
/* 656 */           s.append("- Mean relative error of " + this.parameters.stopCriterionConsensusInterval + " consecutive consensus trees stay below " + Tools.doubleToPercent(this.parameters.stopCriterionConsensusMRE, 0) + " using trees sampled every " + this.parameters.stopCriterionConsensusGeneration + " generations" + "\n");
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 661 */     if (!this.parameters.necessaryStopConditions.isEmpty()) {
/* 662 */       s.append("Stop when ALL of the following necessary conditions are met: \n");
/* 663 */       for (Parameters.HeuristicStopCondition condition : this.parameters.necessaryStopConditions) {
/* 664 */         switch (condition) {
/*     */         case AUTO:
/* 666 */           s.append("- " + this.parameters.stopCriterionSteps + " iterations performed" + "\n");
/* 667 */           break;
/*     */         case CONSENSUS:
/* 669 */           s.append("- Pass over " + new Date(this.repStartTime + ()this.parameters.stopCriterionTime * 3600L * 1000L).toString() + "\n");
/* 670 */           break;
/*     */         case STEPS:
/* 672 */           s.append("- Likelihood stop increasing after " + this.parameters.stopCriterionAutoSteps + " iterations" + "\n");
/* 673 */           break;
/*     */         case TIME:
/* 675 */           s.append("- Mean relative error of " + this.parameters.stopCriterionConsensusInterval + " consecutive consensus trees stay below " + Tools.doubleToPercent(this.parameters.stopCriterionConsensusMRE, 0) + " using trees sampled every " + this.parameters.stopCriterionConsensusGeneration + " generations" + "\n");
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 680 */     showText(s.toString());
/* 681 */     if ((this.parameters.sufficientStopConditions.contains(Parameters.HeuristicStopCondition.STEPS)) || 
/* 682 */       (this.parameters.necessaryStopConditions.contains(Parameters.HeuristicStopCondition.STEPS))) {
/* 683 */       this.maxSteps = maxSteps;
/* 684 */       final int val1 = maxSteps;
/* 685 */       final String val2 = heuristic;
/* 686 */       SwingUtilities.invokeLater(new Runnable() {
/*     */         public void run() {
/* 688 */           SearchOnceGraphicalMonitor.this.progressBar.setMinimum(0);
/* 689 */           SearchOnceGraphicalMonitor.this.progressBar.setMaximum(val1);
/* 690 */           SearchOnceGraphicalMonitor.this.progressBar.setValue(0);
/* 691 */           SearchOnceGraphicalMonitor.this.progressBar.setString(val2 + " running");
/* 692 */           SearchOnceGraphicalMonitor.this.progressBar.setStringPainted(true);
/*     */         } } );
/*     */     }
/* 695 */     else if ((this.parameters.sufficientStopConditions.contains(Parameters.HeuristicStopCondition.AUTO)) || 
/* 696 */       (this.parameters.necessaryStopConditions.contains(Parameters.HeuristicStopCondition.AUTO))) {
/* 697 */       final String val = heuristic;
/* 698 */       SwingUtilities.invokeLater(new Runnable() {
/*     */         public void run() {
/* 700 */           SearchOnceGraphicalMonitor.this.progressBar.setMinimum(0);
/* 701 */           SearchOnceGraphicalMonitor.this.progressBar.setMaximum(SearchOnceGraphicalMonitor.this.parameters.stopCriterionAutoSteps);
/* 702 */           SearchOnceGraphicalMonitor.this.progressBar.setValue(0);
/* 703 */           SearchOnceGraphicalMonitor.this.progressBar.setString(val + " running (auto stop)");
/* 704 */           SearchOnceGraphicalMonitor.this.progressBar.setStringPainted(true);
/*     */         } } );
/*     */     }
/*     */     else {
/* 708 */       final String val = heuristic;
/* 709 */       SwingUtilities.invokeLater(new Runnable() {
/*     */         public void run() {
/* 711 */           SearchOnceGraphicalMonitor.this.progressBar.setIndeterminate(true);
/* 712 */           SearchOnceGraphicalMonitor.this.progressBar.setString(val + " running (no step stop)");
/* 713 */           SearchOnceGraphicalMonitor.this.progressBar.setStringPainted(true);
/*     */         }
/*     */       });
/*     */     }
/* 717 */     this.currentStep = 0;
/* 718 */     ((JLabel)this.curves.get("Starting tree (best)")).setText("Starting tree (best) : " + Tools.doubletoString(startingEvaluation, 4));
/*     */   }
/*     */ 
/*     */   public void showStageSearchStop(List<Tree> solutionTrees, Map<String, Double> evaluationsToShow)
/*     */   {
/* 723 */     if ((this.parameters.sufficientStopConditions.contains(Parameters.HeuristicStopCondition.STEPS)) || 
/* 724 */       (this.parameters.necessaryStopConditions.contains(Parameters.HeuristicStopCondition.STEPS))) {
/* 725 */       this.currentStep = (this.parameters.stopCriterionSteps - 1);
/* 726 */       showNextStep();
/* 727 */     } else if ((this.parameters.sufficientStopConditions.contains(Parameters.HeuristicStopCondition.AUTO)) || 
/* 728 */       (this.parameters.necessaryStopConditions.contains(Parameters.HeuristicStopCondition.AUTO))) {
/* 729 */       showAutoStopDone(this.parameters.stopCriterionAutoSteps);
/*     */     }
/* 731 */     if ((this.parameters.sufficientStopConditions.contains(Parameters.HeuristicStopCondition.TIME)) || 
/* 732 */       (this.parameters.necessaryStopConditions.contains(Parameters.HeuristicStopCondition.TIME))) {
/* 733 */       showRemainingTime(0L);
/*     */     }
/* 735 */     SwingUtilities.invokeLater(new Runnable() {
/*     */       public void run() {
/* 737 */         if (SearchOnceGraphicalMonitor.this.progressBar.isIndeterminate()) {
/* 738 */           SearchOnceGraphicalMonitor.this.progressBar.setIndeterminate(false);
/* 739 */           SearchOnceGraphicalMonitor.this.progressBar.setValue(SearchOnceGraphicalMonitor.this.progressBar.getMaximum());
/*     */         }
/*     */       }
/*     */     });
/* 743 */     showEvaluations(evaluationsToShow);
/* 744 */     StringBuilder s = new StringBuilder();
/* 745 */     s.append(this.H.getName(true) + (this.parameters.hasManyReplicates() ? " (Replicate " + this.currentReplicate + ")" : "") + " finished at " + new Date(System.currentTimeMillis()).toString() + "\n");
/* 746 */     s.append("End after " + Tools.doubletoString((System.currentTimeMillis() - this.repStartTime) / 60000.0D, 2) + " minutes" + "\n");
/* 747 */     s.append("Best tree likelihood : " + Tools.doubletoString(((Double)evaluationsToShow.get("Best solution")).doubleValue(), 4) + "\n");
/* 748 */     showText(s.toString());
/* 749 */     end(solutionTrees);
/*     */   }
/*     */ 
/*     */   public void showStageStartingTree(int numOfSteps)
/*     */   {
/* 754 */     final int val = numOfSteps;
/* 755 */     SwingUtilities.invokeLater(new Runnable() {
/*     */       public void run() {
/* 757 */         SearchOnceGraphicalMonitor.this.progressBar.setMinimum(0);
/* 758 */         SearchOnceGraphicalMonitor.this.progressBar.setMaximum(val);
/* 759 */         SearchOnceGraphicalMonitor.this.progressBar.setValue(0);
/* 760 */         SearchOnceGraphicalMonitor.this.progressBar.setString("Building starting tree");
/* 761 */         SearchOnceGraphicalMonitor.this.progressBar.setStringPainted(true);
/*     */       }
/*     */     });
/*     */   }
/*     */ 
/*     */   public void showStartingTree(Tree tree)
/*     */   {
/* 768 */     this.parent.showStartingTree(tree);
/*     */   }
/*     */ 
/*     */   public void showText(String text)
/*     */   {
/* 773 */     this.parent.showText(text);
/*     */   }
/*     */ 
/*     */   public void stop()
/*     */   {
/* 778 */     this.H.smoothStop();
/*     */   }
/*     */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.monitors.SearchOnceGraphicalMonitor
 * JD-Core Version:    0.6.2
 */