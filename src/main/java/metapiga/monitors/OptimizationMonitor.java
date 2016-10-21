/*     */ package metapiga.monitors;
/*     */ 
/*     */ import java.awt.BorderLayout;
/*     */ import java.awt.Color;
/*     */
/*     */ import java.awt.Dimension;
/*     */ import java.awt.Font;
/*     */ import java.awt.GridBagConstraints;
/*     */ import java.awt.GridBagLayout;
/*     */ import java.awt.Insets;
/*     */ import java.awt.Toolkit;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JDialog;
/*     */ import javax.swing.JOptionPane;
/*     */ import javax.swing.JPanel;
/*     */ import javax.swing.JProgressBar;
/*     */ import metapiga.MainFrame;
/*     */ import metapiga.modelization.DistanceMatrix;
/*     */ import metapiga.optimization.Optimizer;
/*     */
/*     */
/*     */ import metapiga.trees.Consensus;
/*     */ import metapiga.trees.Tree;
/*     */ import metapiga.utilities.Tools;
/*     */ 
/*     */ public class OptimizationMonitor
/*     */   implements Monitor
/*     */ {
/*     */   private final JDialog progressDialog;
/*  40 */   private final JProgressBar progressBar = new JProgressBar();
/*     */   private Optimizer optimizer;
/*     */ 
/*     */   public OptimizationMonitor(JDialog owner, String title, int maxAuto, int idBar, int maxBar)
/*     */   {
/*  44 */     this.progressDialog = new JDialog(owner, false);
/*  45 */     this.progressDialog.setLayout(new BorderLayout());
/*  46 */     this.progressDialog.setIconImage(Tools.getScaledIcon(MainFrame.imageMetapiga, 32).getImage());
/*  47 */     this.progressDialog.setDefaultCloseOperation(0);
/*  48 */     this.progressDialog.setResizable(false);
/*  49 */     this.progressDialog.setUndecorated(true);
/*  50 */     JButton btnStop = new JButton("STOP CURRENT");
/*  51 */     btnStop.addActionListener(new ActionListener() {
/*     */       public void actionPerformed(ActionEvent arg0) {
/*  53 */         if (OptimizationMonitor.this.optimizer != null) OptimizationMonitor.this.optimizer.stop();
/*     */       }
/*     */     });
/*  56 */     btnStop.setFont(new Font("Tahoma", 1, 16));
/*  57 */     btnStop.setForeground(Color.RED);
/*  58 */     this.progressDialog.getContentPane().add(btnStop, "East");
/*  59 */     btnStop.setIcon(MainFrame.imageMetapiga);
/*  60 */     btnStop.setVerticalTextPosition(3);
/*  61 */     btnStop.setHorizontalTextPosition(0);
/*     */ 
/*  63 */     JPanel panel = new JPanel();
/*  64 */     this.progressDialog.getContentPane().add(panel, "Center");
/*  65 */     panel.setLayout(new GridBagLayout());
/*  66 */     panel.add(this.progressBar, new GridBagConstraints(0, 0, 1, 1, 1.0D, 1.0D, 
/*  67 */       10, 1, new Insets(0, 0, 0, 0), 0, 0));
/*  68 */     this.progressBar.setMaximum(maxAuto);
/*  69 */     this.progressBar.setString(title);
/*  70 */     this.progressBar.setStringPainted(true);
/*  71 */     Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
/*  72 */     int height = 170;
/*  73 */     if (height * maxBar > screenSize.height) {
/*  74 */       height = screenSize.height / (maxBar + 2);
/*  75 */       btnStop.setHorizontalTextPosition(4);
/*  76 */       btnStop.setVerticalTextPosition(0);
/*  77 */       btnStop.setIcon(Tools.getScaledIcon(MainFrame.imageMetapiga, height));
/*     */     }
/*     */ 
/*  84 */     this.progressDialog.setSize((int)screenSize.getWidth() / 2, height);
/*  85 */     Dimension windowSize = this.progressDialog.getSize();
/*  86 */     this.progressDialog.setLocation(Math.max(0, (screenSize.width - windowSize.width) / 2), Math.max(0, (screenSize.height - windowSize.height * maxBar) / 2 + idBar * windowSize.height));
/*  87 */     this.progressDialog.setTitle(title);
/*     */   }
/*     */ 
/*     */   public void setOptimizer(Optimizer optimizer) {
/*  91 */     this.optimizer = optimizer;
/*     */   }
/*     */ 
/*     */   public Monitor.MonitorType getMonitorType()
/*     */   {
/*  96 */     return Monitor.MonitorType.OPTIMIZATION;
/*     */   }
/*     */   public boolean trackDataMatrix() {
/*  99 */     return false; } 
/* 100 */   public boolean trackDistances() { return false; } 
/* 101 */   public boolean trackStartingTree() { return false; } 
/* 102 */   public boolean trackHeuristic() { return false; } 
/* 103 */   public boolean trackHeuristicTrees() { return false; } 
/* 104 */   public boolean trackConsensus() { return false; } 
/* 105 */   public boolean trackOperators() { return false; } 
/* 106 */   public boolean trackOperatorStats() { return false; } 
/* 107 */   public boolean trackPerformances() { return false; } 
/* 108 */   public boolean trackAncestralSequences() { return false; }
/*     */ 
/*     */ 
/*     */   public void end(List<Tree> solutionTrees)
/*     */   {
/*     */   }
/*     */ 
/*     */   public void endFromException(Exception e)
/*     */   {
/* 118 */     e.printStackTrace();
/* 119 */     JOptionPane.showMessageDialog(this.progressDialog, Tools.getErrorPanel("Error during optimization", e), "Error", 0);
/*     */   }
/*     */ 
/*     */   public void printAncestralSequences(List<Tree> trees)
/*     */   {
/*     */   }
/*     */ 
/*     */   public void printConsensus(int step, Consensus consensus)
/*     */   {
/*     */   }
/*     */ 
/*     */   public void printDataMatrix()
/*     */   {
/*     */   }
/*     */ 
/*     */   public void printDetailsCP(Tree[] mutantML, Parameters.Operator[] operator, int currentPop)
/*     */   {
/*     */   }
/*     */ 
/*     */   public void printDetailsCP(Tree[] hybridML, String[] parents, int currentPop)
/*     */   {
/*     */   }
/*     */ 
/*     */   public void printDetailsCP(int step, String[] selectionDetails, Tree[][] selectedML, double bestLikelihood)
/*     */   {
/*     */   }
/*     */ 
/*     */   public void printDetailsGA(int step, Tree[] mutantML, Parameters.Operator[] operator)
/*     */   {
/*     */   }
/*     */ 
/*     */   public void printDetailsGA(int step, String selectionDetails, Tree[] selectedML, double bestLikelihood)
/*     */   {
/*     */   }
/*     */ 
/*     */   public void printDetailsHC(int step, double bestLikelihood, double currentLikelihood, Parameters.Operator operator, boolean improvement)
/*     */   {
/*     */   }
/*     */ 
/*     */   public void printDetailsSA(int step, double bestLikelihood, double S0Likelihood, double currentLikelihood, Parameters.Operator operator, String status, double tempAcceptance, double temperature, int coolingSteps, int successes, int failures, int reheatingSteps)
/*     */   {
/*     */   }
/*     */ 
/*     */   public void printDistanceMatrix(DistanceMatrix dm)
/*     */   {
/*     */   }
/*     */ 
/*     */   public void printEndTreesHeuristic()
/*     */   {
/*     */   }
/*     */ 
/*     */   public void printOperatorFrequenciesUpdate(int currentStep, Map<Parameters.Operator, Integer> use, Map<Parameters.Operator, Double> scoreImprovements, Map<Parameters.Operator, Long> performances)
/*     */   {
/*     */   }
/*     */ 
/*     */   public void printOperatorFrequenciesUpdate(Map<Parameters.Operator, Double> frequencies)
/*     */   {
/*     */   }
/*     */ 
/*     */   public void printOperatorInfos(Tree tree, String infos)
/*     */   {
/*     */   }
/*     */ 
/*     */   public void printOperatorInfos(Tree tree, String infos, Consensus consensus)
/*     */   {
/*     */   }
/*     */ 
/*     */   public void printOperatorStatistics(int numStep, Map<Parameters.Operator, Integer> use, Map<Parameters.Operator, Double> scoreImprovements, Map<Parameters.Operator, Long> performances, int outgroupTargeted, int ingroupTargeted, Map<Parameters.Operator, Map<Integer, Integer>> cancelByConsensus)
/*     */   {
/*     */   }
/*     */ 
/*     */   public void printStartingTrees(List<Tree> startingTrees)
/*     */   {
/*     */   }
/*     */ 
/*     */   public void printTreeAfterOperator(Tree tree, Parameters.Operator operator, boolean consensus)
/*     */   {
/*     */   }
/*     */ 
/*     */   public void printTreeBeforeOperator(Tree tree, Parameters.Operator operator, boolean consensus)
/*     */   {
/*     */   }
/*     */ 
/*     */   public void printTreesCP(int step, Tree[] trees, int pop, boolean recombined)
/*     */   {
/*     */   }
/*     */ 
/*     */   public void printTreesCP(int step, Tree[][] trees)
/*     */   {
/*     */   }
/*     */ 
/*     */   public void printTreesGA(int step, Tree[] trees, boolean selectionDone)
/*     */   {
/*     */   }
/*     */ 
/*     */   public void printTreesHC(int step, Tree bestTree, Tree currentTree)
/*     */   {
/*     */   }
/*     */ 
/*     */   public void printTreesSA(int step, Tree bestTree, Tree S0Tree, Tree currentTree)
/*     */   {
/*     */   }
/*     */ 
/*     */   public void updateConsensusTree(Tree consensusTree)
/*     */   {
/*     */   }
/*     */ 
/*     */   public void showAutoStopDone(int noChangeSteps)
/*     */   {
/* 289 */     this.progressBar.setValue(noChangeSteps);
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
/*     */   public void showStageSearchProgress(int currentIteration, long remainingTime, int noChangeSteps)
/*     */   {
/* 369 */     showAutoStopDone(noChangeSteps);
/*     */   }
/*     */ 
/*     */   public void showStageSearchStart(String heuristic, int maxSteps, double startingEvaluation)
/*     */   {
/* 375 */     this.progressDialog.setVisible(true);
/* 376 */     this.progressDialog.toFront();
/* 377 */     this.progressDialog.requestFocus();
/* 378 */     showAutoStopDone(0);
/*     */   }
/*     */ 
/*     */   public void showStageSearchStop(List<Tree> solutionTrees, Map<String, Double> evaluationsToShow)
/*     */   {
/* 384 */     this.progressDialog.setVisible(false);
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
/*     */   public void showText(String text)
/*     */   {
/*     */   }
/*     */ 
/*     */   public void run()
/*     */   {
/*     */   }
/*     */ 
/*     */   public void trackPerformances(String action, int level)
/*     */   {
/*     */   }
/*     */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.OptimizationMonitor
 * JD-Core Version:    0.6.2
 */