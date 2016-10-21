/*     */ package optimization;
/*     */ 
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Set;
/*     */ import javax.swing.JDialog;
/*     */ import metapiga.heuristics.GeneticAlgorithm;
/*     */ import metapiga.monitors.InactiveMonitor;
/*     */ import metapiga.monitors.OptimizationMonitor;
/*     */ import metapiga.parameters.Parameters;
/*     */ import metapiga.parameters.Parameters.GAOperatorChange;
/*     */ import metapiga.parameters.Parameters.GASelection;
/*     */ import metapiga.parameters.Parameters.HeuristicStopCondition;
/*     */ import metapiga.parameters.Parameters.Operator;
/*     */ import metapiga.parameters.Parameters.OperatorSelection;
/*     */ import metapiga.parameters.Parameters.Optimization;
/*     */ import metapiga.parameters.Parameters.OptimizationTarget;
/*     */ import metapiga.parameters.Parameters.StartingTreeGeneration;
/*     */ import metapiga.trees.Tree;
/*     */ import metapiga.trees.exceptions.NullAncestorException;
/*     */ import metapiga.trees.exceptions.UnrootableTreeException;
/*     */ 
/*     */ public class GA
/*     */   implements Optimizer
/*     */ {
/*     */   private final Parameters P;
/*     */   private final int steps;
/*     */   private GeneticAlgorithm ga;
/*     */ 
/*     */   public GA(Tree tree, Set<Parameters.OptimizationTarget> targetsToOptimize)
/*     */   {
/*  37 */     this(tree, targetsToOptimize, 200);
/*     */   }
/*     */ 
/*     */   public GA(Tree tree, Set<Parameters.OptimizationTarget> targetsToOptimize, int steps) {
/*  41 */     this.P = new Parameters("Optimization");
/*  42 */     this.steps = steps;
/*  43 */     this.P.necessaryStopConditions.add(Parameters.HeuristicStopCondition.AUTO);
/*  44 */     this.P.stopCriterionAutoSteps = steps;
/*  45 */     this.P.stopCriterionAutoThreshold = 0.0001D;
/*  46 */     this.P.gaIndNum = 8;
/*  47 */     this.P.startingTreeGeneration = Parameters.StartingTreeGeneration.GIVEN;
/*  48 */     for (int i = 0; i < this.P.gaIndNum; i++)
/*  49 */       this.P.startingTrees.add(tree);
/*  50 */     this.P.gaOperatorChange = Parameters.GAOperatorChange.STEP;
/*  51 */     this.P.gaRecombination = 0.0D;
/*  52 */     this.P.gaSelection = Parameters.GASelection.TOURNAMENT;
/*  53 */     this.P.operatorSelection = Parameters.OperatorSelection.RANDOM;
/*  54 */     this.P.optimization = Parameters.Optimization.NEVER;
/*  55 */     this.P.operators.clear();
/*  56 */     for (Parameters.OptimizationTarget target : targetsToOptimize) {
/*  57 */       switch (target) {
/*     */       case APRATE:
/*  59 */         this.P.operators.add(Parameters.Operator.BLM);
/*  60 */         break;
/*     */       case BL:
/*  62 */         this.P.operators.add(Parameters.Operator.RPM);
/*  63 */         this.P.operatorsParameters.put(Parameters.Operator.RPM, Integer.valueOf(1));
/*  64 */         break;
/*     */       case GAMMA:
/*  66 */         this.P.operators.add(Parameters.Operator.GDM);
/*  67 */         break;
/*     */       case PINV:
/*  69 */         this.P.operators.add(Parameters.Operator.PIM);
/*  70 */         break;
/*     */       case R:
/*  72 */         this.P.operators.add(Parameters.Operator.APRM);
/*     */       }
/*     */     }
/*     */ 
/*  76 */     this.P.device = tree.parameters.device;
/*  77 */     this.P.gpuDevProperties = tree.parameters.gpuDevProperties;
/*     */ 
/*  79 */     this.P.evaluationDistribution = tree.getEvaluationDistribution();
/*  80 */     this.P.evaluationModel = tree.getEvaluationModel();
/*     */ 
/*  82 */     this.P.setLikelihoodCalcualtionType(tree.getLikelihoodCalculationType());
/*  83 */     this.P.dataset = tree.getDataset();
/*     */   }
/*     */ 
/*     */   public Tree getOptimizedTree()
/*     */     throws NullAncestorException, UnrootableTreeException
/*     */   {
/*  89 */     if (this.P.operators.isEmpty()) return (Tree)this.P.startingTrees.get(0);
/*  90 */     this.ga = new GeneticAlgorithm(this.P, new InactiveMonitor());
/*  91 */     Thread T = new Thread(this.ga);
/*  92 */     T.start();
/*     */     try {
/*  94 */       T.join();
/*     */     } catch (InterruptedException ie) {
/*  96 */       ie.printStackTrace();
/*     */     }
/*  98 */     Tree bestSol = this.ga.getBestSolution();
/*  99 */     this.ga = null;
/* 100 */     return bestSol;
/*     */   }
/*     */ 
/*     */   public Tree getOptimizedTreeWithProgress(JDialog owner, String title, int idBar, int maxBar) throws NullAncestorException, UnrootableTreeException
/*     */   {
/* 105 */     OptimizationMonitor monitor = new OptimizationMonitor(owner, title, this.steps, idBar, maxBar);
/* 106 */     this.ga = new GeneticAlgorithm(this.P, monitor);
/* 107 */     monitor.setOptimizer(this);
/* 108 */     Thread T = new Thread(this.ga);
/* 109 */     T.start();
/*     */     try {
/* 111 */       T.join();
/*     */     }
/*     */     catch (InterruptedException ie)
/*     */     {
/* 115 */       this.ga.smoothStop();
/*     */     }
/* 117 */     Tree bestSol = this.ga.getBestSolution();
/* 118 */     this.ga = null;
/* 119 */     return bestSol;
/*     */   }
/*     */ 
/*     */   public Tree getOptimizedTreeWithProgress(JDialog owner, String title) throws NullAncestorException, UnrootableTreeException
/*     */   {
/* 124 */     return getOptimizedTreeWithProgress(owner, title, 0, 1);
/*     */   }
/*     */ 
/*     */   public void stop()
/*     */   {
/* 129 */     this.ga.smoothStop();
/*     */   }
/*     */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.optimization.GA
 * JD-Core Version:    0.6.2
 */