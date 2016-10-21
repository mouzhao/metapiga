/*      */ package metapiga.trees;
/*      */ 
/*      */ import java.util.ArrayList;
/*      */ import java.util.Collection;
/*      */ import java.util.EnumMap;
/*      */ import java.util.HashMap;
/*      */ import java.util.HashSet;
/*      */ import java.util.Iterator;
/*      */ import java.util.List;
/*      */ import java.util.Map;
/*      */ import java.util.Map.Entry;
/*      */ import java.util.Set;
/*      */ import metapiga.RateParameter;
/*      */ import metapiga.modelization.Charset;
/*      */ import metapiga.monitors.Monitor;
/*      */ import metapiga.parameters.Parameters.CPOperator;
/*      */ import metapiga.parameters.Parameters.Operator;
/*      */ import metapiga.parameters.Parameters.OperatorSelection;
/*      */ import metapiga.trees.exceptions.NoInclusionException;
/*      */ import metapiga.trees.exceptions.NullAncestorException;
/*      */ import metapiga.trees.exceptions.TooManyNeighborsException;
/*      */ import metapiga.trees.exceptions.UnrootableTreeException;
/*      */ import metapiga.utilities.Tools;
/*      */ 
/*      */ public class Operators
/*      */ {
/*      */   public static final int RANDOM = 1;
/*      */   public static final int ALL = 0;
/*      */   private final List<Parameters.Operator> availables;
/*      */   private final int numAvailable;
/*      */   private final Map<Parameters.Operator, Integer> parameters;
/*      */   private final Map<Parameters.Operator, Double> frequencies;
/*      */   private final Set<Parameters.Operator> isDynamic;
/*      */   private final boolean useDynamicFreq;
/*      */   private final int freqInt;
/*      */   private final double freqMin;
/*      */   private final Parameters.OperatorSelection selection;
/*      */   private final double optimizationUse;
/*      */   private Parameters.Operator currentOperator;
/*      */   private Iterator<Parameters.Operator> orderedIterator;
/*      */   private int stepIncrement;
/*      */   private int currentStep;
/*      */   private Map<Parameters.Operator, Double> globalScoreImprovements;
/*      */   private Map<Parameters.Operator, Double> localScoreImprovements;
/*      */   private Map<Parameters.Operator, Integer> globalUse;
/*      */   private Map<Parameters.Operator, Integer> localUse;
/*      */   private Map<Parameters.Operator, Long> globalPerformances;
/*      */   private Map<Parameters.Operator, Long> localPerformances;
/*      */   private Map<Parameters.Operator, Map<Integer, Integer>> cancelByConsensus;
/*      */   private int outgroupTargeted;
/*      */   private int ingroupTargeted;
/*      */   private final Monitor monitor;
/*      */   private final boolean trackDetails;
/*      */   private final boolean trackStats;
/*      */   private final boolean trackPerf;
/*      */ 
/*      */   public Operators(List<Parameters.Operator> availableOperators, Map<Parameters.Operator, Integer> operatorsParameters, Map<Parameters.Operator, Double> operatorsFrequencies, Set<Parameters.Operator> operatorIsDynamic, int dynamicInterval, double dynamicMin, Parameters.OperatorSelection operatorSelection, double optimizationUse, int stepIncrement, Monitor monitor)
/*      */   {
/*   76 */     this.monitor = monitor;
/*   77 */     this.trackDetails = monitor.trackOperators();
/*   78 */     this.trackStats = monitor.trackOperatorStats();
/*   79 */     this.trackPerf = monitor.trackPerformances();
/*   80 */     this.availables = availableOperators;
/*   81 */     this.numAvailable = availableOperators.size();
/*   82 */     this.parameters = operatorsParameters;
/*   83 */     this.frequencies = operatorsFrequencies;
/*   84 */     this.isDynamic = operatorIsDynamic;
/*   85 */     if (operatorIsDynamic.size() > 0) this.useDynamicFreq = true; else
/*   86 */       this.useDynamicFreq = false;
/*   87 */     this.freqInt = dynamicInterval;
/*   88 */     this.freqMin = dynamicMin;
/*   89 */     this.selection = operatorSelection;
/*   90 */     this.optimizationUse = optimizationUse;
/*   91 */     nextOperator();
/*   92 */     this.stepIncrement = stepIncrement;
/*   93 */     this.currentStep = 0;
/*   94 */     this.globalScoreImprovements = new EnumMap(Parameters.Operator.class);
/*   95 */     this.localScoreImprovements = new EnumMap(Parameters.Operator.class);
/*   96 */     this.globalUse = new EnumMap(Parameters.Operator.class);
/*   97 */     this.localUse = new EnumMap(Parameters.Operator.class);
/*   98 */     this.cancelByConsensus = new EnumMap(Parameters.Operator.class);
/*   99 */     if (this.trackPerf) {
/*  100 */       this.globalPerformances = new EnumMap(Parameters.Operator.class);
/*  101 */       this.localPerformances = new EnumMap(Parameters.Operator.class);
/*      */     }
/*  103 */     for (Parameters.Operator op : this.availables) {
/*  104 */       this.globalScoreImprovements.put(op, Double.valueOf(0.0D));
/*  105 */       this.localScoreImprovements.put(op, Double.valueOf(0.0D));
/*  106 */       this.globalUse.put(op, Integer.valueOf(0));
/*  107 */       this.localUse.put(op, Integer.valueOf(0));
/*  108 */       this.cancelByConsensus.put(op, new HashMap());
/*  109 */       if (this.trackPerf) {
/*  110 */         this.globalPerformances.put(op, Long.valueOf(0L));
/*  111 */         this.localPerformances.put(op, Long.valueOf(0L));
/*      */       }
/*      */     }
/*  114 */     this.outgroupTargeted = 0;
/*  115 */     this.ingroupTargeted = 0;
/*      */   }
/*      */ 
/*      */   public synchronized Parameters.Operator getCurrentOperator() {
/*  119 */     return this.currentOperator;
/*      */   }
/*      */ 
/*      */   public int getOperatorUse(Parameters.Operator operator) {
/*  123 */     if (this.globalUse.containsKey(operator)) {
/*  124 */       return ((Integer)this.globalUse.get(operator)).intValue();
/*      */     }
/*  126 */     return 0;
/*      */   }
/*      */ 
/*      */   private synchronized void addCancellationByConsensus(Parameters.Operator operator)
/*      */   {
/*  131 */     int step = this.currentStep / this.stepIncrement / 100;
/*  132 */     if (!((Map)this.cancelByConsensus.get(operator)).containsKey(Integer.valueOf(step))) {
/*  133 */       ((Map)this.cancelByConsensus.get(operator)).put(Integer.valueOf(step), Integer.valueOf(0));
/*      */     }
/*  135 */     int num = ((Integer)((Map)this.cancelByConsensus.get(operator)).get(Integer.valueOf(step))).intValue();
/*  136 */     ((Map)this.cancelByConsensus.get(operator)).put(Integer.valueOf(step), Integer.valueOf(num + 1));
/*      */   }
/*      */ 
/*      */   private synchronized void ingroupTargeted() {
/*  140 */     this.ingroupTargeted += 1;
/*      */   }
/*      */ 
/*      */   private synchronized void outgroupTargeted() {
/*  144 */     this.outgroupTargeted += 1;
/*      */   }
/*      */ 
/*      */   public String getCancellationsByConsensus(Parameters.Operator operator, String separator) {
/*  148 */     String res = operator.toString();
/*  149 */     int sum = 0;
/*  150 */     for (Iterator localIterator = ((Map)this.cancelByConsensus.get(operator)).values().iterator(); localIterator.hasNext(); ) { int c = ((Integer)localIterator.next()).intValue();
/*  151 */       sum += c;
/*      */     }
/*  153 */     res = res + separator + sum;
/*  154 */     Set todo = new HashSet(((Map)this.cancelByConsensus.get(operator)).keySet());
/*  155 */     for (int i = 0; !todo.isEmpty(); i++) {
/*  156 */       if (todo.remove(Integer.valueOf(i)))
/*  157 */         res = res + separator + ((Map)this.cancelByConsensus.get(operator)).get(Integer.valueOf(i));
/*      */       else {
/*  159 */         res = res + separator + "0";
/*      */       }
/*      */     }
/*  162 */     return res;
/*      */   }
/*      */ 
/*      */   public synchronized Parameters.Operator nextOperator() {
/*  166 */     switch ($SWITCH_TABLE$metapiga$parameters$Parameters$OperatorSelection()[this.selection.ordinal()]) {
/*      */     case 2:
/*  168 */       if ((this.orderedIterator == null) || (!this.orderedIterator.hasNext())) {
/*  169 */         this.orderedIterator = this.availables.iterator();
/*      */       }
/*  171 */       this.currentOperator = ((Parameters.Operator)this.orderedIterator.next());
/*  172 */       break;
/*      */     case 3:
/*  174 */       double frequency = Math.random();
/*  175 */       Iterator it = this.availables.iterator();
/*      */ 
/*  177 */       for (Parameters.Operator op = (Parameters.Operator)it.next(); (frequency > ((Double)this.frequencies.get(op)).doubleValue()) && (it.hasNext()); op = (Parameters.Operator)it.next()) {
/*  178 */         frequency -= ((Double)this.frequencies.get(op)).doubleValue();
/*      */       }
/*  180 */       if (frequency > ((Double)this.frequencies.get(op)).doubleValue())
/*  181 */         this.currentOperator = ((Parameters.Operator)this.availables.get(Tools.randInt(this.numAvailable)));
/*      */       else
/*  183 */         this.currentOperator = op;
/*  184 */       break;
/*      */     case 1:
/*      */     default:
/*  187 */       this.currentOperator = ((Parameters.Operator)this.availables.get(Tools.randInt(this.numAvailable)));
/*      */     }
/*  189 */     return this.currentOperator;
/*      */   }
/*      */ 
/*      */   private void updateFrequencies() {
/*  193 */     Set toUpdate = new HashSet(this.isDynamic);
/*      */ 
/*  195 */     double totalImprovement = 0.0D;
/*  196 */     for (Parameters.Operator op : this.availables) {
/*  197 */       if (this.isDynamic.contains(op)) {
/*  198 */         if (((Integer)this.localUse.get(op)).intValue() != 0) {
/*  199 */           this.localScoreImprovements.put(op, Double.valueOf(((Double)this.localScoreImprovements.get(op)).doubleValue() / ((Integer)this.localUse.get(op)).intValue()));
/*      */         }
/*  201 */         if (((Double)this.localScoreImprovements.get(op)).doubleValue() != 0.0D) {
/*  202 */           totalImprovement += ((Double)this.localScoreImprovements.get(op)).doubleValue();
/*      */         }
/*      */       }
/*      */     }
/*  206 */     if (totalImprovement != 0.0D)
/*      */     {
/*  208 */       for (Parameters.Operator op : this.availables) {
/*  209 */         if ((toUpdate.contains(op)) && (((Double)this.localScoreImprovements.get(op)).doubleValue() == 0.0D)) {
/*  210 */           if (((Integer)this.localUse.get(op)).intValue() != 0) {
/*  211 */             this.frequencies.put(op, Double.valueOf(this.freqMin));
/*      */           }
/*  213 */           toUpdate.remove(op);
/*      */         }
/*      */       }
/*      */ 
/*  217 */       double sumOfNonDynamic = 0.0D;
/*  218 */       for (Parameters.Operator op : this.availables) {
/*  219 */         if (!toUpdate.contains(op)) {
/*  220 */           sumOfNonDynamic += ((Double)this.frequencies.get(op)).doubleValue();
/*      */         }
/*      */       }
/*      */ 
/*  224 */       for (Parameters.Operator op : this.availables) {
/*  225 */         if (toUpdate.contains(op)) {
/*  226 */           this.frequencies.put(op, Double.valueOf(((Double)this.localScoreImprovements.get(op)).doubleValue() / totalImprovement * (1.0D - sumOfNonDynamic)));
/*      */         }
/*      */       }
/*      */ 
/*  230 */       for (Parameters.Operator op : this.availables) {
/*  231 */         this.localScoreImprovements.put(op, Double.valueOf(0.0D));
/*  232 */         this.localUse.put(op, Integer.valueOf(0));
/*  233 */         if (this.trackPerf) this.localPerformances.put(op, Long.valueOf(0L)); 
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public void printStatistics()
/*      */   {
/*  239 */     if (this.trackStats) this.monitor.printOperatorStatistics(this.currentStep / this.stepIncrement, this.globalUse, this.globalScoreImprovements, 
/*  240 */         this.globalPerformances, this.outgroupTargeted, this.ingroupTargeted, this.cancelByConsensus);
/*      */   }
/*      */ 
/*      */   public void mutateTree(Tree tree, Parameters.Operator op, Consensus consensus, Parameters.CPOperator behaviour)
/*      */     throws UnrootableTreeException, NullAncestorException, TooManyNeighborsException, NoInclusionException
/*      */   {
/*  255 */     double treeEv = tree.getEvaluation();
/*  256 */     long startTime = this.trackPerf ? System.nanoTime() : 0L;
/*  257 */     switch (op) {
/*      */     case APRM:
/*  259 */       if (consensus != null) NNI(tree, consensus, behaviour); else
/*  260 */         NNI(tree);
/*  261 */       break;
/*      */     case BLM:
/*  263 */       if (consensus != null) SPR(tree, consensus, behaviour); else
/*  264 */         SPR(tree);
/*  265 */       break;
/*      */     case BLMINT:
/*  267 */       if (consensus != null) TBR(tree, consensus, behaviour); else
/*  268 */         TBR(tree);
/*  269 */       break;
/*      */     case GDM:
/*  271 */       if (consensus != null) TXS(tree, ((Integer)this.parameters.get(op)).intValue(), consensus, behaviour); else
/*  272 */         TXS(tree, ((Integer)this.parameters.get(op)).intValue());
/*  273 */       break;
/*      */     case NNI:
/*  275 */       if (consensus != null) STS(tree, ((Integer)this.parameters.get(op)).intValue(), consensus, behaviour); else
/*  276 */         STS(tree, ((Integer)this.parameters.get(op)).intValue());
/*  277 */       break;
/*      */     case PIM:
/*  279 */       BLM(tree);
/*  280 */       break;
/*      */     case RPM:
/*  282 */       BLMint(tree);
/*  283 */       break;
/*      */     case SPR:
/*  285 */       RPM(tree, ((Integer)this.parameters.get(op)).intValue());
/*  286 */       break;
/*      */     case STS:
/*  288 */       GDM(tree);
/*  289 */       break;
/*      */     case TBR:
/*  291 */       PIM(tree);
/*  292 */       break;
/*      */     case TXS:
/*  294 */       APRM(tree);
/*      */     }
/*      */ 
/*  297 */     double improvement = tree.getEvaluation() - treeEv;
/*  298 */     synchronized (this) {
/*  299 */       if (this.trackPerf) {
/*  300 */         this.globalPerformances.put(op, Long.valueOf(((Long)this.globalPerformances.get(op)).longValue() + System.nanoTime() - startTime));
/*  301 */         this.localPerformances.put(op, Long.valueOf(((Long)this.localPerformances.get(op)).longValue() + System.nanoTime() - startTime));
/*      */       }
/*  303 */       this.currentStep += 1;
/*  304 */       this.globalUse.put(op, Integer.valueOf(((Integer)this.globalUse.get(op)).intValue() + 1));
/*  305 */       this.localUse.put(op, Integer.valueOf(((Integer)this.localUse.get(op)).intValue() + 1));
/*  306 */       if (improvement < 0.0D) {
/*  307 */         this.globalScoreImprovements.put(op, Double.valueOf(((Double)this.globalScoreImprovements.get(op)).doubleValue() + improvement));
/*  308 */         this.localScoreImprovements.put(op, Double.valueOf(((Double)this.localScoreImprovements.get(op)).doubleValue() + improvement));
/*      */       }
/*  310 */       if ((this.useDynamicFreq) && (this.currentStep % (this.freqInt * this.stepIncrement) == 0)) {
/*  311 */         if (this.trackStats) this.monitor.printOperatorFrequenciesUpdate(this.currentStep / this.stepIncrement, this.localUse, this.localScoreImprovements, this.localPerformances);
/*  312 */         updateFrequencies();
/*  313 */         if (this.trackStats) this.monitor.printOperatorFrequenciesUpdate(this.frequencies);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public void mutateTree(Tree tree, Parameters.Operator operator)
/*      */     throws UnrootableTreeException, NullAncestorException, TooManyNeighborsException, NoInclusionException
/*      */   {
/*  328 */     mutateTree(tree, operator, null, null);
/*      */   }
/*      */ 
/*      */   public void mutateTree(Tree tree)
/*      */     throws UnrootableTreeException, NullAncestorException, TooManyNeighborsException, NoInclusionException
/*      */   {
/*  341 */     mutateTree(tree, this.currentOperator, null, null);
/*      */   }
/*      */ 
/*      */   public void casualMutateTree(Tree tree, Parameters.Operator op) throws UnrootableTreeException, NullAncestorException, TooManyNeighborsException
/*      */   {
/*  346 */     switch (op) {
/*      */     case APRM:
/*  348 */       NNI(tree);
/*  349 */       break;
/*      */     case BLM:
/*  351 */       SPR(tree);
/*  352 */       break;
/*      */     case BLMINT:
/*  354 */       TBR(tree);
/*  355 */       break;
/*      */     case GDM:
/*  357 */       TXS(tree, ((Integer)this.parameters.get(op)).intValue());
/*  358 */       break;
/*      */     case NNI:
/*  360 */       STS(tree, ((Integer)this.parameters.get(op)).intValue());
/*  361 */       break;
/*      */     case PIM:
/*  363 */       BLM(tree);
/*  364 */       break;
/*      */     case RPM:
/*  366 */       BLMint(tree);
/*  367 */       break;
/*      */     case SPR:
/*  369 */       RPM(tree, ((Integer)this.parameters.get(op)).intValue());
/*  370 */       break;
/*      */     case STS:
/*  372 */       GDM(tree);
/*  373 */       break;
/*      */     case TBR:
/*  375 */       PIM(tree);
/*  376 */       break;
/*      */     case TXS:
/*  378 */       APRM(tree);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void NNI(Tree T)
/*      */     throws NullAncestorException, TooManyNeighborsException, UnrootableTreeException
/*      */   {
/*  393 */     if (this.trackDetails) this.monitor.printTreeBeforeOperator(T, Parameters.Operator.NNI, false);
/*  394 */     if (T.getNumOfIngroupInodes() < 2) {
/*  395 */       if (this.trackDetails) this.monitor.printOperatorInfos(T, "NNI not applied, ingroup contains less than 2 internal nodes, no change was done to the tree");
/*  396 */       if (this.trackDetails) this.monitor.printTreeAfterOperator(T, Parameters.Operator.NNI, false); 
/*      */     }
/*      */     else
/*      */     {
/*      */       Node nniSon;
/*      */       do
/*  401 */         nniSon = (Node)T.getIngroupInodes().get(Tools.randInt(T.getNumOfIngroupInodes()));
/*  402 */       while (nniSon == T.getRoot());
/*  403 */       Node nniParent = nniSon.getAncestorNode();
/*  404 */       Node swapSon = (Node)nniSon.getChildren().get(Tools.randInt(2));
/*  405 */       T.unroot();
/*  406 */       List availableSwapParent = new ArrayList();
/*  407 */       availableSwapParent.addAll(nniParent.getNeighborNodes());
/*  408 */       availableSwapParent.remove(nniSon);
/*  409 */       availableSwapParent.removeAll(T.getOutgroupInodes());
/*  410 */       Node swapParent = (Node)availableSwapParent.get(Tools.randInt(availableSwapParent.size()));
/*  411 */       nniSon.removeNeighborButKeepBranchLength(swapSon);
/*  412 */       nniParent.removeNeighborButKeepBranchLength(swapParent);
/*  413 */       nniSon.addNeighborWithBranchLength(swapParent);
/*  414 */       nniParent.addNeighborWithBranchLength(swapSon);
/*  415 */       T.root();
/*  416 */       T.markNodeToReEvaluate(swapSon);
/*  417 */       T.markNodeToReEvaluate(swapParent);
/*  418 */       T.markNodeToReEvaluate(nniSon);
/*  419 */       T.markNodeToReEvaluate(nniParent);
/*  420 */       T.fireInodeStructureChange();
/*  421 */       ingroupTargeted();
/*  422 */       if (this.trackDetails) this.monitor.printOperatorInfos(T, "NNI on branch " + nniParent + "-" + nniSon + " : swap branches " + nniParent + "-" + swapParent + " and " + nniSon + "-" + swapSon);
/*  423 */       if (this.trackDetails) this.monitor.printTreeAfterOperator(T, Parameters.Operator.NNI, false);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void NNI(Tree T, Consensus consensus, Parameters.CPOperator behaviour)
/*      */     throws NullAncestorException, TooManyNeighborsException, UnrootableTreeException
/*      */   {
/*  438 */     if (this.trackDetails) this.monitor.printTreeBeforeOperator(T, Parameters.Operator.NNI, true);
/*  439 */     if (T.getNumOfIngroupInodes() < 2) {
/*  440 */       if (this.trackDetails) this.monitor.printOperatorInfos(T, "NNI not applied, ingroup contains less than 2 internal nodes, no change was done to the tree");
/*  441 */       if (this.trackDetails) this.monitor.printTreeAfterOperator(T, Parameters.Operator.NNI, true);
/*      */       return;
/*      */     }
/*      */     Node nniParent;
/*      */     Node nniSon;
/*      */     Node nniParent;
/*  445 */     switch (behaviour) {
/*      */     case SUPERVISED:
/*  447 */       List validCandidates = consensus.getNNIValidCandidates(T);
/*  448 */       if (validCandidates.size() == 0) {
/*  449 */         addCancellationByConsensus(Parameters.Operator.NNI);
/*  450 */         if (this.trackDetails) this.monitor.printOperatorInfos(T, "Supervised NNI canceled by consensus, no valid candidate found.", consensus);
/*  451 */         if (this.trackDetails) this.monitor.printTreeAfterOperator(T, Parameters.Operator.NNI, true);
/*  452 */         return;
/*      */       }
/*  454 */       Branch b = (Branch)validCandidates.get(Tools.randInt(validCandidates.size()));
/*  455 */       Node nniSon = b.getNode();
/*  456 */       nniParent = b.getOtherNode();
/*  457 */       break;
/*      */     case BLIND:
/*      */     default:
/*      */       do
/*  461 */         nniSon = (Node)T.getIngroupInodes().get(Tools.randInt(T.getNumOfIngroupInodes()));
/*  462 */       while (nniSon == T.getRoot());
/*  463 */       nniParent = nniSon.getAncestorNode();
/*      */ 
/*  465 */       if (!consensus.acceptNNI(new Branch(nniSon, nniSon.getAncestorKey()))) {
/*  466 */         addCancellationByConsensus(Parameters.Operator.NNI);
/*  467 */         if (this.trackDetails) this.monitor.printOperatorInfos(T, "NNI on branch " + nniParent + "-" + nniSon + " canceled by consensus.", consensus);
/*  468 */         if (this.trackDetails) this.monitor.printTreeAfterOperator(T, Parameters.Operator.NNI, true);
/*      */         return;
/*      */       }
/*      */       break;
/*      */     }
/*  473 */     Node swapSon = (Node)nniSon.getChildren().get(Tools.randInt(2));
/*  474 */     T.unroot();
/*  475 */     List availableSwapParent = new ArrayList();
/*  476 */     availableSwapParent.addAll(nniParent.getNeighborNodes());
/*  477 */     availableSwapParent.remove(nniSon);
/*  478 */     availableSwapParent.removeAll(T.getOutgroupInodes());
/*  479 */     Node swapParent = (Node)availableSwapParent.get(Tools.randInt(availableSwapParent.size()));
/*  480 */     nniSon.removeNeighborButKeepBranchLength(swapSon);
/*  481 */     nniParent.removeNeighborButKeepBranchLength(swapParent);
/*  482 */     nniSon.addNeighborWithBranchLength(swapParent);
/*  483 */     nniParent.addNeighborWithBranchLength(swapSon);
/*  484 */     T.root();
/*  485 */     T.markNodeToReEvaluate(swapSon);
/*  486 */     T.markNodeToReEvaluate(swapParent);
/*  487 */     T.markNodeToReEvaluate(nniSon);
/*  488 */     T.markNodeToReEvaluate(nniParent);
/*  489 */     T.fireInodeStructureChange();
/*  490 */     ingroupTargeted();
/*  491 */     if (this.trackDetails) this.monitor.printOperatorInfos(T, "NNI on branch " + nniParent + "-" + nniSon + " : swap branches " + nniParent + "-" + swapParent + " and " + nniSon + "-" + swapSon);
/*  492 */     if (this.trackDetails) this.monitor.printTreeAfterOperator(T, Parameters.Operator.NNI, true);
/*      */   }
/*      */ 
/*      */   public void SPR(Tree T)
/*      */     throws TooManyNeighborsException, UnrootableTreeException
/*      */   {
/*  505 */     if (this.trackDetails) this.monitor.printTreeBeforeOperator(T, Parameters.Operator.SPR, false);
/*  506 */     List branches = new ArrayList();
/*      */     Iterator localIterator2;
/*  507 */     for (Iterator localIterator1 = T.getInodes().iterator(); localIterator1.hasNext(); 
/*  508 */       localIterator2.hasNext())
/*      */     {
/*  507 */       Node inode = (Node)localIterator1.next();
/*  508 */       localIterator2 = inode.getNeighborKeys().iterator(); continue; Node.Neighbor neigh = (Node.Neighbor)localIterator2.next();
/*  509 */       branches.add(new Branch(inode, neigh));
/*      */     }
/*  511 */     if (branches.size() < 6) {
/*  512 */       if (this.trackDetails) this.monitor.printOperatorInfos(T, "SPR not applied, tree contains less than 6 subtrees, no change was done to the tree");
/*  513 */       if (this.trackDetails) this.monitor.printTreeAfterOperator(T, Parameters.Operator.SPR, false);
/*  514 */       return;
/*      */     }
/*  516 */     Branch candidate = (Branch)branches.remove(Tools.randInt(branches.size()));
/*  517 */     List targets = getSPRValidTargets(T, candidate);
/*  518 */     while (targets.isEmpty()) {
/*  519 */       if (branches.isEmpty()) {
/*  520 */         if (this.trackDetails) this.monitor.printOperatorInfos(T, "SPR canceled because no valid branch candidate was found");
/*  521 */         if (this.trackDetails) this.monitor.printTreeAfterOperator(T, Parameters.Operator.SPR, false);
/*  522 */         return;
/*      */       }
/*  524 */       candidate = (Branch)branches.remove(Tools.randInt(branches.size()));
/*  525 */       targets = getSPRValidTargets(T, candidate);
/*      */     }
/*  527 */     boolean outgroupCandidate = false;
/*  528 */     if (T.isInOutgroup(candidate.getNode())) {
/*  529 */       outgroupTargeted();
/*  530 */       outgroupCandidate = true;
/*      */     } else {
/*  532 */       ingroupTargeted();
/*      */     }
/*      */ 
/*  535 */     T.unroot();
/*  536 */     Branch oldCandidateAnchor = candidate.detach();
/*  537 */     Branch target = (Branch)targets.remove(Tools.randInt(targets.size()));
/*      */ 
/*  540 */     while (((outgroupCandidate) && (!T.isInOutgroup(target.getNode()))) || (
/*  541 */       (!outgroupCandidate) && (
/*  541 */       T.isInOutgroup(target.getNode())))) {
/*  542 */       if (targets.isEmpty()) {
/*  543 */         oldCandidateAnchor.graft(candidate);
/*  544 */         T.root();
/*  545 */         if (this.trackDetails) this.monitor.printOperatorInfos(T, "SPR canceled because no valid branch target was found");
/*  546 */         if (this.trackDetails) this.monitor.printTreeAfterOperator(T, Parameters.Operator.SPR, false);
/*  547 */         return;
/*      */       }
/*  549 */       target = (Branch)targets.remove(Tools.randInt(targets.size()));
/*      */     }
/*  551 */     if (this.trackDetails) this.monitor.printOperatorInfos(T, "SPR : branch " + candidate + " pruned and regrafted on branch " + target);
/*  552 */     target.graft(candidate);
/*      */ 
/*  554 */     T.fireInodeStructureChange();
/*  555 */     T.root();
/*  556 */     T.markAllNodesToReEvaluate();
/*  557 */     if (this.trackDetails) this.monitor.printTreeAfterOperator(T, Parameters.Operator.SPR, false);
/*      */   }
/*      */ 
/*      */   public void SPR(Tree T, Consensus consensus, Parameters.CPOperator behaviour)
/*      */     throws TooManyNeighborsException, UnrootableTreeException
/*      */   {
/*  573 */     if (this.trackDetails) this.monitor.printTreeBeforeOperator(T, Parameters.Operator.SPR, true);
/*  574 */     List branches = new ArrayList();
/*      */     Iterator localIterator2;
/*  575 */     for (Iterator localIterator1 = T.getInodes().iterator(); localIterator1.hasNext(); 
/*  576 */       localIterator2.hasNext())
/*      */     {
/*  575 */       Node inode = (Node)localIterator1.next();
/*  576 */       localIterator2 = inode.getNeighborKeys().iterator(); continue; Node.Neighbor neigh = (Node.Neighbor)localIterator2.next();
/*  577 */       branches.add(new Branch(inode, neigh));
/*      */     }
/*  579 */     if (branches.size() < 6) {
/*  580 */       if (this.trackDetails) this.monitor.printOperatorInfos(T, "SPR not applied, tree contains less than 6 subtrees, no change was done to the tree");
/*  581 */       if (this.trackDetails) this.monitor.printTreeAfterOperator(T, Parameters.Operator.SPR, true);
/*  582 */       return;
/*      */     }
/*  584 */     Branch candidate = (Branch)branches.remove(Tools.randInt(branches.size()));
/*  585 */     List targets = consensus.getSPRValidTargets(T, candidate);
/*  586 */     switch (behaviour) {
/*      */     case SUPERVISED:
/*  588 */       while (targets.isEmpty()) {
/*  589 */         if (branches.isEmpty()) {
/*  590 */           addCancellationByConsensus(Parameters.Operator.SPR);
/*  591 */           if (this.trackDetails) this.monitor.printOperatorInfos(T, "Supervised SPR canceled by consensus, no valid candidate found.", consensus);
/*  592 */           if (this.trackDetails) this.monitor.printTreeAfterOperator(T, Parameters.Operator.SPR, true);
/*  593 */           return;
/*      */         }
/*  595 */         candidate = (Branch)branches.remove(Tools.randInt(branches.size()));
/*  596 */         targets = consensus.getSPRValidTargets(T, candidate);
/*      */       }case BLIND:
/*  598 */       if ((goto 424) && 
/*  600 */         (targets.isEmpty())) {
/*  601 */         addCancellationByConsensus(Parameters.Operator.SPR);
/*  602 */         if (this.trackDetails) this.monitor.printOperatorInfos(T, "SPR on branch " + candidate + " canceled by consensus.", consensus);
/*  603 */         if (this.trackDetails) this.monitor.printTreeAfterOperator(T, Parameters.Operator.SPR, true);
/*      */         return;
/*      */       }
/*      */       break;
/*      */     }
/*  608 */     boolean outgroupCandidate = false;
/*  609 */     if (T.isInOutgroup(candidate.getNode())) {
/*  610 */       outgroupTargeted();
/*  611 */       outgroupCandidate = true;
/*      */     } else {
/*  613 */       ingroupTargeted();
/*      */     }
/*      */ 
/*  616 */     T.unroot();
/*  617 */     Branch oldCandidateAnchor = candidate.detach();
/*  618 */     Branch target = (Branch)targets.remove(Tools.randInt(targets.size()));
/*      */ 
/*  621 */     while (((outgroupCandidate) && (!T.isInOutgroup(target.getNode()))) || (
/*  622 */       (!outgroupCandidate) && (
/*  622 */       T.isInOutgroup(target.getNode())))) {
/*  623 */       if (targets.isEmpty()) {
/*  624 */         oldCandidateAnchor.graft(candidate);
/*  625 */         T.root();
/*  626 */         if (this.trackDetails) this.monitor.printOperatorInfos(T, "SPR canceled because no valid branch target was found");
/*  627 */         if (this.trackDetails) this.monitor.printTreeAfterOperator(T, Parameters.Operator.SPR, false);
/*  628 */         return;
/*      */       }
/*  630 */       target = (Branch)targets.remove(Tools.randInt(targets.size()));
/*      */     }
/*  632 */     if (this.trackDetails) this.monitor.printOperatorInfos(T, "SPR : branch " + candidate + " pruned and regrafted on branch " + target);
/*  633 */     target.graft(candidate);
/*      */ 
/*  635 */     T.fireInodeStructureChange();
/*  636 */     T.root();
/*  637 */     T.markAllNodesToReEvaluate();
/*  638 */     if (this.trackDetails) this.monitor.printTreeAfterOperator(T, Parameters.Operator.SPR, true);
/*      */   }
/*      */ 
/*      */   public void TBR(Tree T)
/*      */     throws TooManyNeighborsException, UnrootableTreeException, NullAncestorException
/*      */   {
/*  652 */     if (this.trackDetails) this.monitor.printTreeBeforeOperator(T, Parameters.Operator.TBR, false);
/*  653 */     List branches = new ArrayList();
/*  654 */     for (Node inode : T.getIngroupInodes()) {
/*  655 */       if (inode != T.getRoot()) branches.add(new Branch(inode, inode.getAncestorKey()));
/*      */     }
/*  657 */     if (branches.size() < 3) {
/*  658 */       if (this.trackDetails) this.monitor.printOperatorInfos(T, "TBR not applied, ingroup contains less than 3 internal branches, no change was done to the tree");
/*  659 */       if (this.trackDetails) this.monitor.printTreeAfterOperator(T, Parameters.Operator.TBR, false);
/*  660 */       return;
/*      */     }
/*      */ 
/*  663 */     T.unroot();
/*  664 */     Branch candidate = (Branch)branches.remove(Tools.randInt(branches.size()));
/*  665 */     Branch mirror = candidate.getMirrorBranch();
/*  666 */     List leftTargets = getSPRValidTargets(T, candidate);
/*  667 */     List rightTargets = getSPRValidTargets(T, mirror);
/*  668 */     while ((leftTargets.isEmpty()) || (rightTargets.isEmpty())) {
/*  669 */       if (branches.isEmpty()) {
/*  670 */         if (this.trackDetails) this.monitor.printOperatorInfos(T, "TBR canceled because no valid branch candidate was found");
/*  671 */         if (this.trackDetails) this.monitor.printTreeAfterOperator(T, Parameters.Operator.TBR, false);
/*  672 */         T.root();
/*  673 */         return;
/*      */       }
/*  675 */       candidate = (Branch)branches.remove(Tools.randInt(branches.size()));
/*  676 */       mirror = candidate.getMirrorBranch();
/*  677 */       leftTargets = getSPRValidTargets(T, candidate);
/*  678 */       rightTargets = getSPRValidTargets(T, mirror);
/*      */     }
/*  680 */     if (T.isInOutgroup(candidate.getNode())) outgroupTargeted(); else
/*  681 */       ingroupTargeted();
/*  682 */     candidate.detach();
/*  683 */     mirror.detach();
/*  684 */     Branch leftTarget = (Branch)leftTargets.get(Tools.randInt(leftTargets.size()));
/*  685 */     Branch rightTarget = (Branch)rightTargets.get(Tools.randInt(rightTargets.size()));
/*  686 */     if (this.trackDetails) this.monitor.printOperatorInfos(T, "TBR : branch " + candidate + " removed and reconnected on branches " + leftTarget + " and " + rightTarget);
/*  687 */     leftTarget.graft(candidate);
/*  688 */     rightTarget.graft(mirror);
/*      */ 
/*  690 */     T.fireInodeStructureChange();
/*  691 */     T.root();
/*  692 */     T.markAllNodesToReEvaluate();
/*  693 */     if (this.trackDetails) this.monitor.printTreeAfterOperator(T, Parameters.Operator.TBR, false);
/*      */   }
/*      */ 
/*      */   public void TBR(Tree T, Consensus consensus, Parameters.CPOperator behaviour)
/*      */     throws TooManyNeighborsException, UnrootableTreeException, NullAncestorException
/*      */   {
/*  709 */     if (this.trackDetails) this.monitor.printTreeBeforeOperator(T, Parameters.Operator.TBR, true);
/*  710 */     List branches = new ArrayList();
/*  711 */     for (Node inode : T.getIngroupInodes()) {
/*  712 */       if (inode != T.getRoot()) branches.add(new Branch(inode, inode.getAncestorKey()));
/*      */     }
/*  714 */     if (branches.size() < 3) {
/*  715 */       if (this.trackDetails) this.monitor.printOperatorInfos(T, "TBR not applied, ingroup contains less than 3 internal branches, no change was done to the tree");
/*  716 */       if (this.trackDetails) this.monitor.printTreeAfterOperator(T, Parameters.Operator.TBR, true);
/*  717 */       return;
/*      */     }
/*      */ 
/*  720 */     T.unroot();
/*  721 */     Branch candidate = (Branch)branches.remove(Tools.randInt(branches.size()));
/*  722 */     Branch mirror = candidate.getMirrorBranch();
/*  723 */     List leftTargets = consensus.getSPRValidTargets(T, candidate);
/*  724 */     List rightTargets = consensus.getSPRValidTargets(T, mirror);
/*  725 */     switch (behaviour) {
/*      */     case SUPERVISED:
/*  727 */       while ((leftTargets.isEmpty()) || (rightTargets.isEmpty())) {
/*  728 */         if (branches.isEmpty()) {
/*  729 */           addCancellationByConsensus(Parameters.Operator.TBR);
/*  730 */           if (this.trackDetails) this.monitor.printOperatorInfos(T, "Supervised TBR canceled by consensus, no valid candidate found.", consensus);
/*  731 */           if (this.trackDetails) this.monitor.printTreeAfterOperator(T, Parameters.Operator.TBR, true);
/*  732 */           T.root();
/*  733 */           return;
/*      */         }
/*  735 */         candidate = (Branch)branches.remove(Tools.randInt(branches.size()));
/*  736 */         mirror = candidate.getMirrorBranch();
/*  737 */         leftTargets = consensus.getSPRValidTargets(T, candidate);
/*  738 */         rightTargets = consensus.getSPRValidTargets(T, mirror);
/*      */       }
/*  740 */       break;
/*      */     case BLIND:
/*  742 */       if ((leftTargets.isEmpty()) || (rightTargets.isEmpty())) {
/*  743 */         addCancellationByConsensus(Parameters.Operator.TBR);
/*  744 */         if (this.trackDetails) this.monitor.printOperatorInfos(T, "TBR on branch " + candidate + " canceled by consensus.", consensus);
/*  745 */         if (this.trackDetails) this.monitor.printTreeAfterOperator(T, Parameters.Operator.TBR, true); T.root();
/*      */         return;
/*      */       }
/*      */       break;
/*      */     }
/*  751 */     if (T.isInOutgroup(candidate.getNode())) outgroupTargeted(); else
/*  752 */       ingroupTargeted();
/*  753 */     candidate.detach();
/*  754 */     mirror.detach();
/*  755 */     Branch leftTarget = (Branch)leftTargets.get(Tools.randInt(leftTargets.size()));
/*  756 */     Branch rightTarget = (Branch)rightTargets.get(Tools.randInt(rightTargets.size()));
/*  757 */     if (this.trackDetails) this.monitor.printOperatorInfos(T, "TBR : branch " + candidate + " removed and reconnected on branches " + leftTarget + " and " + rightTarget);
/*  758 */     leftTarget.graft(candidate);
/*  759 */     rightTarget.graft(mirror);
/*      */ 
/*  761 */     T.fireInodeStructureChange();
/*  762 */     T.root();
/*  763 */     T.markAllNodesToReEvaluate();
/*  764 */     if (this.trackDetails) this.monitor.printTreeAfterOperator(T, Parameters.Operator.TBR, true);
/*      */   }
/*      */ 
/*      */   public void TXS(Tree T, int numToSwap)
/*      */     throws NullAncestorException, TooManyNeighborsException
/*      */   {
/*  781 */     if (this.trackDetails) this.monitor.printTreeBeforeOperator(T, Parameters.Operator.TXS, false);
/*  782 */     boolean applyOnOutgroup = (T.getNumOfOutgroupLeaves() > 2) && (Math.random() < T.getNumOfOutgroupLeaves() / T.getNumOfLeaves());
/*  783 */     List leaves = applyOnOutgroup ? new ArrayList(T.getOutgroupLeaves()) : new ArrayList(T.getIngroupLeaves());
/*  784 */     if (leaves.size() < 3) {
/*  785 */       if (this.trackDetails) this.monitor.printOperatorInfos(T, "TXS not applied, ingroup contains less than 3 nodes, no change was done to the tree");
/*  786 */       if (this.trackDetails) this.monitor.printTreeAfterOperator(T, Parameters.Operator.TXS, false);
/*  787 */       return;
/*      */     }
/*  789 */     if (numToSwap == 1) numToSwap = Tools.randInt(leaves.size() - 1) + 2;
/*  790 */     if (numToSwap == 0) numToSwap = leaves.size();
/*  791 */     List toSwap = new ArrayList();
/*  792 */     while ((toSwap.size() < numToSwap) && (leaves.size() > 0)) {
/*  793 */       toSwap.add((Node)leaves.remove(Tools.randInt(leaves.size())));
/*      */     }
/*  795 */     switch (toSwap.size()) {
/*      */     case 0:
/*      */     case 1:
/*  798 */       if (this.trackDetails) this.monitor.printOperatorInfos(T, "This TXS instance don't find 2 taxas that can be legaly swapped, no change was done to the tree");
/*  799 */       if (this.trackDetails) this.monitor.printTreeAfterOperator(T, Parameters.Operator.TXS, false);
/*  800 */       return;
/*      */     case 2:
/*  802 */       Node nodeA = (Node)toSwap.get(0);
/*  803 */       Node nodeB = (Node)toSwap.get(1);
/*  804 */       Node ancestorA = nodeA.getAncestorNode();
/*  805 */       Node ancestorB = nodeB.getAncestorNode();
/*  806 */       while (ancestorA == ancestorB) {
/*  807 */         toSwap.remove(1);
/*  808 */         toSwap.add((Node)leaves.remove(Tools.randInt(leaves.size())));
/*  809 */         nodeB = (Node)toSwap.get(1);
/*  810 */         ancestorB = nodeB.getAncestorNode();
/*      */       }
/*  812 */       ancestorA.removeNeighborButKeepBranchLength(nodeA);
/*  813 */       ancestorB.removeNeighborButKeepBranchLength(nodeB);
/*  814 */       ancestorA.addNeighborWithBranchLength(nodeB);
/*  815 */       ancestorB.addNeighborWithBranchLength(nodeA);
/*  816 */       T.markNodeToReEvaluate(nodeA);
/*  817 */       T.markNodeToReEvaluate(nodeB);
/*  818 */       if (this.trackDetails) this.monitor.printOperatorInfos(T, "TXS(" + numToSwap + ") on " + (applyOnOutgroup ? "outgroup" : "ingroup") + " : [" + nodeA + "," + nodeB + "]");
/*  819 */       break;
/*      */     default:
/*  821 */       if (this.trackDetails) this.monitor.printOperatorInfos(T, "TXS(" + numToSwap + ") on " + (applyOnOutgroup ? "outgroup" : "ingroup") + " : " + toSwap);
/*  822 */       for (Node node : toSwap) {
/*  823 */         T.markNodeToReEvaluate(node);
/*      */       }
/*  825 */       List ancestors = new ArrayList();
/*  826 */       for (Node node : toSwap) {
/*  827 */         Node ancestor = node.getAncestorNode();
/*  828 */         ancestor.removeNeighborButKeepBranchLength(node);
/*  829 */         ancestors.add(ancestor);
/*      */       }
/*  831 */       for (Node node : ancestors) {
/*  832 */         node.addNeighborWithBranchLength((Node)toSwap.remove(Tools.randInt(toSwap.size())));
/*      */       }
/*      */     }
/*      */ 
/*  836 */     if (applyOnOutgroup) outgroupTargeted(); else
/*  837 */       ingroupTargeted();
/*  838 */     if (this.trackDetails) this.monitor.printTreeAfterOperator(T, Parameters.Operator.TXS, false);
/*      */   }
/*      */ 
/*      */   public void TXS(Tree T, int numToSwap, Consensus consensus, Parameters.CPOperator behaviour)
/*      */     throws NullAncestorException, TooManyNeighborsException, NoInclusionException
/*      */   {
/*  859 */     if (this.trackDetails) this.monitor.printTreeBeforeOperator(T, Parameters.Operator.TXS, true);
/*  860 */     boolean applyOnOutgroup = (T.getNumOfOutgroupLeaves() > 2) && (Math.random() < T.getNumOfOutgroupLeaves() / T.getNumOfLeaves());
/*  861 */     List leaves = applyOnOutgroup ? new ArrayList(T.getOutgroupLeaves()) : new ArrayList(T.getIngroupLeaves());
/*  862 */     if (leaves.size() < 3) {
/*  863 */       if (this.trackDetails) this.monitor.printOperatorInfos(T, "TXS not applied, ingroup contains less than 3 nodes, no change was done to the tree");
/*  864 */       if (this.trackDetails) this.monitor.printTreeAfterOperator(T, Parameters.Operator.TXS, true);
/*  865 */       return;
/*      */     }
/*  867 */     if (numToSwap == 1) numToSwap = Tools.randInt(leaves.size() - 1) + 2;
/*  868 */     if (numToSwap == 0) numToSwap = leaves.size();
/*  869 */     if (behaviour == Parameters.CPOperator.SUPERVISED) {
/*  870 */       leaves = consensus.getTXSValidCandidates(T, numToSwap, leaves);
/*      */     }
/*  872 */     List toSwap = new ArrayList();
/*  873 */     while ((toSwap.size() < numToSwap) && (leaves.size() > 0)) {
/*  874 */       toSwap.add((Node)leaves.remove(Tools.randInt(leaves.size())));
/*      */     }
/*  876 */     switch (toSwap.size()) {
/*      */     case 0:
/*      */     case 1:
/*  879 */       if (this.trackDetails) this.monitor.printOperatorInfos(T, "This TXS instance don't find 2 taxas that can be legaly swapped, no change was done to the tree");
/*  880 */       if (this.trackDetails) this.monitor.printTreeAfterOperator(T, Parameters.Operator.TXS, true);
/*  881 */       return;
/*      */     case 2:
/*  883 */       Node nodeA = (Node)toSwap.get(0);
/*  884 */       Node nodeB = (Node)toSwap.get(1);
/*  885 */       Node ancestorA = nodeA.getAncestorNode();
/*  886 */       Node ancestorB = nodeB.getAncestorNode();
/*  887 */       while (ancestorA == ancestorB) {
/*  888 */         toSwap.remove(1);
/*  889 */         toSwap.add((Node)leaves.remove(Tools.randInt(leaves.size())));
/*  890 */         nodeB = (Node)toSwap.get(1);
/*  891 */         ancestorB = nodeB.getAncestorNode();
/*      */       }
/*  893 */       if ((behaviour == Parameters.CPOperator.BLIND) && 
/*  894 */         (!consensus.acceptTXS(T, toSwap))) {
/*  895 */         addCancellationByConsensus(Parameters.Operator.TXS);
/*  896 */         if (this.trackDetails) this.monitor.printOperatorInfos(T, "TXS on taxas " + toSwap + " canceled by consensus.", consensus);
/*  897 */         if (this.trackDetails) this.monitor.printTreeAfterOperator(T, Parameters.Operator.TXS, true);
/*  898 */         return;
/*      */       }
/*      */ 
/*  901 */       ancestorA.removeNeighborButKeepBranchLength(nodeA);
/*  902 */       ancestorB.removeNeighborButKeepBranchLength(nodeB);
/*  903 */       ancestorA.addNeighborWithBranchLength(nodeB);
/*  904 */       ancestorB.addNeighborWithBranchLength(nodeA);
/*  905 */       T.markNodeToReEvaluate(nodeA);
/*  906 */       T.markNodeToReEvaluate(nodeB);
/*  907 */       if (this.trackDetails) this.monitor.printOperatorInfos(T, "TXS(" + numToSwap + ") on " + (applyOnOutgroup ? "outgroup" : "ingroup") + " : [" + nodeA + "," + nodeB + "]");
/*  908 */       break;
/*      */     default:
/*  910 */       if ((behaviour == Parameters.CPOperator.BLIND) && 
/*  911 */         (!consensus.acceptTXS(T, toSwap))) {
/*  912 */         addCancellationByConsensus(Parameters.Operator.TXS);
/*  913 */         if (this.trackDetails) this.monitor.printOperatorInfos(T, "TXS on taxas " + toSwap + " canceled by consensus.", consensus);
/*  914 */         if (this.trackDetails) this.monitor.printTreeAfterOperator(T, Parameters.Operator.TXS, true);
/*  915 */         return;
/*      */       }
/*      */ 
/*  918 */       if (this.trackDetails) this.monitor.printOperatorInfos(T, "TXS(" + numToSwap + ") on " + (applyOnOutgroup ? "outgroup" : "ingroup") + " : " + toSwap);
/*  919 */       for (Node node : toSwap) {
/*  920 */         T.markNodeToReEvaluate(node);
/*      */       }
/*  922 */       List ancestors = new ArrayList();
/*  923 */       for (Node node : toSwap) {
/*  924 */         Node ancestor = node.getAncestorNode();
/*  925 */         ancestor.removeNeighborButKeepBranchLength(node);
/*  926 */         ancestors.add(ancestor);
/*      */       }
/*  928 */       for (Node node : ancestors) {
/*  929 */         node.addNeighborWithBranchLength((Node)toSwap.remove(Tools.randInt(toSwap.size())));
/*      */       }
/*      */     }
/*      */ 
/*  933 */     if (applyOnOutgroup) outgroupTargeted(); else
/*  934 */       ingroupTargeted();
/*  935 */     if (this.trackDetails) this.monitor.printTreeAfterOperator(T, Parameters.Operator.TXS, true);
/*      */   }
/*      */ 
/*      */   public void STS(Tree T, int numToSwap)
/*      */     throws UnrootableTreeException, NullAncestorException, TooManyNeighborsException
/*      */   {
/*  950 */     if (this.trackDetails) this.monitor.printTreeBeforeOperator(T, Parameters.Operator.STS, false);
/*  951 */     List inodes = new ArrayList(T.getIngroupInodes());
/*  952 */     inodes.remove(T.getRoot());
/*  953 */     List toSwap = new ArrayList();
/*      */     Node n;
/*  954 */     for (; (inodes.size() > 0) && ((toSwap.size() != 2) || (numToSwap != 2)); 
/*  960 */       n != T.getRoot())
/*      */     {
/*  955 */       n = (Node)inodes.get(Tools.randInt(inodes.size()));
/*  956 */       toSwap.add(n);
/*  957 */       inodes.removeAll(T.getPreorderTraversal(n));
/*  958 */       n = n.getAncestorNode();
/*  959 */       inodes.removeAll(n.getChildren());
/*  960 */       continue;
/*  961 */       inodes.remove(n);
/*  962 */       n = n.getAncestorNode();
/*      */     }
/*      */ 
/*  965 */     if (toSwap.size() < 2) {
/*  966 */       if (this.trackDetails) this.monitor.printOperatorInfos(T, "This STS instance don't find 2 subtrees that can be legaly swapped, no change was done to the tree");
/*  967 */       if (this.trackDetails) this.monitor.printTreeAfterOperator(T, Parameters.Operator.STS, false);
/*  968 */       return;
/*      */     }
/*  970 */     if (this.trackDetails) this.monitor.printOperatorInfos(T, "STS(" + (numToSwap == 2 ? "2" : "RANDOM") + ") on ingroup : " + toSwap);
/*  971 */     for (Node node : toSwap)
/*  972 */       T.markNodeToReEvaluate(node);
/*      */     Node ancestorA;
/*  974 */     if (numToSwap == 2) {
/*  975 */       Node nodeA = (Node)toSwap.get(0);
/*  976 */       Node nodeB = (Node)toSwap.get(1);
/*  977 */       ancestorA = nodeA.getAncestorNode();
/*  978 */       Node ancestorB = nodeB.getAncestorNode();
/*  979 */       ancestorA.removeNeighborButKeepBranchLength(nodeA);
/*  980 */       ancestorB.removeNeighborButKeepBranchLength(nodeB);
/*  981 */       ancestorA.addNeighborWithBranchLength(nodeB);
/*  982 */       ancestorB.addNeighborWithBranchLength(nodeA);
/*      */     } else {
/*  984 */       List ancestors = new ArrayList();
/*  985 */       for (Node node : toSwap) {
/*  986 */         Node ancestor = node.getAncestorNode();
/*  987 */         ancestor.removeNeighborButKeepBranchLength(node);
/*  988 */         ancestors.add(ancestor);
/*      */       }
/*  990 */       for (Node node : ancestors) {
/*  991 */         node.addNeighborWithBranchLength((Node)toSwap.remove(Tools.randInt(toSwap.size())));
/*      */       }
/*      */     }
/*  994 */     T.fireInodeStructureChange();
/*  995 */     ingroupTargeted();
/*  996 */     if (this.trackDetails) this.monitor.printTreeAfterOperator(T, Parameters.Operator.STS, false);
/*      */   }
/*      */ 
/*      */   public void STS(Tree T, int numToSwap, Consensus consensus, Parameters.CPOperator behaviour)
/*      */     throws UnrootableTreeException, NullAncestorException, TooManyNeighborsException
/*      */   {
/* 1013 */     if (this.trackDetails) this.monitor.printTreeBeforeOperator(T, Parameters.Operator.STS, true);
/* 1014 */     List inodes = new ArrayList(T.getIngroupInodes());
/* 1015 */     inodes.remove(T.getRoot());
/* 1016 */     List toSwap = new ArrayList();
/*      */     Node n;
/* 1017 */     for (; (inodes.size() > 0) && ((toSwap.size() != 2) || (numToSwap != 2)); 
/* 1023 */       n != T.getRoot())
/*      */     {
/* 1018 */       n = (Node)inodes.get(Tools.randInt(inodes.size()));
/* 1019 */       toSwap.add(n);
/* 1020 */       inodes.removeAll(T.getPreorderTraversal(n));
/* 1021 */       n = n.getAncestorNode();
/* 1022 */       inodes.removeAll(n.getChildren());
/* 1023 */       continue;
/* 1024 */       inodes.remove(n);
/* 1025 */       n = n.getAncestorNode();
/*      */     }
/*      */ 
/* 1028 */     if (toSwap.size() < 2) {
/* 1029 */       if (this.trackDetails) this.monitor.printOperatorInfos(T, "This STS instance don't find 2 subtrees that can be legaly swapped, no change was done to the tree");
/* 1030 */       if (this.trackDetails) this.monitor.printTreeAfterOperator(T, Parameters.Operator.STS, true);
/* 1031 */       return;
/*      */     }
/* 1033 */     if (!consensus.acceptSTS(T, toSwap)) {
/* 1034 */       addCancellationByConsensus(Parameters.Operator.STS);
/* 1035 */       if (this.trackDetails) this.monitor.printOperatorInfos(T, "STS(" + (numToSwap == 2 ? "2" : "RANDOM") + ") on nodes " + toSwap + " canceled by consensus.", consensus);
/* 1036 */       if (this.trackDetails) this.monitor.printTreeAfterOperator(T, Parameters.Operator.STS, true);
/* 1037 */       return;
/*      */     }
/* 1039 */     if (this.trackDetails) this.monitor.printOperatorInfos(T, "STS(" + (numToSwap == 2 ? "2" : "RANDOM") + ") on ingroup : " + toSwap);
/* 1040 */     for (Node node : toSwap)
/* 1041 */       T.markNodeToReEvaluate(node);
/*      */     Node ancestorA;
/* 1043 */     if (numToSwap == 2) {
/* 1044 */       Node nodeA = (Node)toSwap.get(0);
/* 1045 */       Node nodeB = (Node)toSwap.get(1);
/* 1046 */       ancestorA = nodeA.getAncestorNode();
/* 1047 */       Node ancestorB = nodeB.getAncestorNode();
/* 1048 */       ancestorA.removeNeighborButKeepBranchLength(nodeA);
/* 1049 */       ancestorB.removeNeighborButKeepBranchLength(nodeB);
/* 1050 */       ancestorA.addNeighborWithBranchLength(nodeB);
/* 1051 */       ancestorB.addNeighborWithBranchLength(nodeA);
/*      */     } else {
/* 1053 */       List ancestors = new ArrayList();
/* 1054 */       for (Node node : toSwap) {
/* 1055 */         Node ancestor = node.getAncestorNode();
/* 1056 */         ancestor.removeNeighborButKeepBranchLength(node);
/* 1057 */         ancestors.add(ancestor);
/*      */       }
/* 1059 */       for (Node node : ancestors) {
/* 1060 */         node.addNeighborWithBranchLength((Node)toSwap.remove(Tools.randInt(toSwap.size())));
/*      */       }
/*      */     }
/* 1063 */     T.fireInodeStructureChange();
/* 1064 */     ingroupTargeted();
/* 1065 */     if (this.trackDetails) this.monitor.printTreeAfterOperator(T, Parameters.Operator.STS, true);
/*      */   }
/*      */ 
/*      */   public void BLM(Tree T)
/*      */     throws NullAncestorException
/*      */   {
/* 1077 */     if (this.trackDetails) this.monitor.printTreeBeforeOperator(T, Parameters.Operator.BLM, false);
/* 1078 */     double mutation = Tools.exponentialMultiplierRand();
/* 1079 */     Node node = (Node)T.getInodes().get(Tools.randInt(T.getNumOfInodes()));
/* 1080 */     switch (Tools.randInt(3)) {
/*      */     case 0:
/* 1082 */       if (this.trackDetails) this.monitor.printOperatorInfos(T, "Branch " + node + "-" + node.getNeighbor(Node.Neighbor.A) + " of length " + node.getBranchLength(Node.Neighbor.A) + " mutated to " + node.getBranchLength(Node.Neighbor.A) * mutation);
/* 1083 */       node.setBranchLength(Node.Neighbor.A, node.getBranchLength(Node.Neighbor.A) * mutation);
/* 1084 */       if ((node != T.getRoot()) && (node.getAncestorKey() == Node.Neighbor.A)) T.markNodeToReEvaluate(node.getAncestorNode()); else
/* 1085 */         T.markNodeToReEvaluate(node);
/* 1086 */       break;
/*      */     case 1:
/* 1088 */       if (this.trackDetails) this.monitor.printOperatorInfos(T, "Branch " + node + "-" + node.getNeighbor(Node.Neighbor.B) + " of length " + node.getBranchLength(Node.Neighbor.B) + " mutated to " + node.getBranchLength(Node.Neighbor.B) * mutation);
/* 1089 */       node.setBranchLength(Node.Neighbor.B, node.getBranchLength(Node.Neighbor.B) * mutation);
/* 1090 */       if ((node != T.getRoot()) && (node.getAncestorKey() == Node.Neighbor.B)) T.markNodeToReEvaluate(node.getAncestorNode()); else
/* 1091 */         T.markNodeToReEvaluate(node);
/* 1092 */       break;
/*      */     case 2:
/* 1094 */       if (this.trackDetails) this.monitor.printOperatorInfos(T, "Branch " + node + "-" + node.getNeighbor(Node.Neighbor.C) + " of length " + node.getBranchLength(Node.Neighbor.C) + " mutated to " + node.getBranchLength(Node.Neighbor.C) * mutation);
/* 1095 */       node.setBranchLength(Node.Neighbor.C, node.getBranchLength(Node.Neighbor.C) * mutation);
/* 1096 */       if ((node != T.getRoot()) && (node.getAncestorKey() == Node.Neighbor.C)) T.markNodeToReEvaluate(node.getAncestorNode()); else
/* 1097 */         T.markNodeToReEvaluate(node);
/*      */       break;
/*      */     }
/* 1100 */     if (T.isInOutgroup(node)) outgroupTargeted(); else
/* 1101 */       ingroupTargeted();
/* 1102 */     if (this.trackDetails) this.monitor.printTreeAfterOperator(T, Parameters.Operator.BLM, false);
/*      */   }
/*      */ 
/*      */   public void BLMint(Tree T)
/*      */     throws NullAncestorException
/*      */   {
/* 1114 */     if (this.trackDetails) this.monitor.printTreeBeforeOperator(T, Parameters.Operator.BLMINT, false);
/* 1115 */     double mutation = Tools.exponentialMultiplierRand();
/* 1116 */     Node node = (Node)T.getInodes().get(Tools.randInt(T.getNumOfInodes()));
/* 1117 */     List neighbors = new ArrayList();
/* 1118 */     if (node.getNeighbor(Node.Neighbor.A).isInode()) neighbors.add(Node.Neighbor.A);
/* 1119 */     if (node.getNeighbor(Node.Neighbor.B).isInode()) neighbors.add(Node.Neighbor.B);
/* 1120 */     if (node.getNeighbor(Node.Neighbor.C).isInode()) neighbors.add(Node.Neighbor.C);
/* 1121 */     Node.Neighbor randNeighbor = (Node.Neighbor)neighbors.get(Tools.randInt(neighbors.size()));
/* 1122 */     if (this.trackDetails) this.monitor.printOperatorInfos(T, "Branch " + node + "-" + node.getNeighbor(randNeighbor) + " of length " + node.getBranchLength(randNeighbor) + " mutated to " + node.getBranchLength(randNeighbor) * mutation);
/* 1123 */     node.setBranchLength(randNeighbor, node.getBranchLength(randNeighbor) * mutation);
/* 1124 */     if ((node != T.getRoot()) && (node.getAncestorKey() == randNeighbor)) T.markNodeToReEvaluate(node.getAncestorNode()); else
/* 1125 */       T.markNodeToReEvaluate(node);
/* 1126 */     if (T.isInOutgroup(node)) outgroupTargeted(); else
/* 1127 */       ingroupTargeted();
/* 1128 */     if (this.trackDetails) this.monitor.printTreeAfterOperator(T, Parameters.Operator.BLMINT, false);
/*      */   }
/*      */ 
/*      */   public void RPM(Tree T, int numParam)
/*      */   {
/* 1142 */     if (this.trackDetails) this.monitor.printTreeBeforeOperator(T, Parameters.Operator.RPM, false);
/* 1143 */     List L = T.getPartitions();
/* 1144 */     Charset C = (Charset)L.get(Tools.randInt(L.size()));
/* 1145 */     Map param = T.getEvaluationRateParameters(C);
/*      */ 
/* 1147 */     switch (numParam) {
/*      */     case 1:
/* 1149 */       RateParameter key = (RateParameter)new ArrayList(param.keySet()).get(Tools.randInt(param.size()));
/* 1150 */       double p = ((Double)param.get(key)).doubleValue();
/* 1151 */       double mutation = Tools.exponentialMultiplierRand();
/* 1152 */       p *= mutation;
/* 1153 */       if (this.trackDetails) this.monitor.printOperatorInfos(T, "R parameter " + key + (L.size() > 1 ? " of partition " + C.getLabel() : "") + " with value " + param.get(key) + " mutated to " + p);
/* 1154 */       T.setEvaluationRateParameter(C, key, p);
/* 1155 */       break;
/*      */     case 0:
/*      */     default:
/* 1158 */       for (Map.Entry e : param.entrySet()) {
/* 1159 */         double p = ((Double)e.getValue()).doubleValue();
/* 1160 */         double mutation = Tools.exponentialMultiplierRand();
/* 1161 */         p *= mutation;
/* 1162 */         if (this.trackDetails) this.monitor.printOperatorInfos(T, "R parameter " + e.getKey() + (L.size() > 1 ? " of partition " + C.getLabel() : "") + " with value " + e.getValue() + " mutated to " + p);
/* 1163 */         T.setEvaluationRateParameter(C, (RateParameter)e.getKey(), p);
/*      */       }
/*      */     }
/*      */ 
/* 1167 */     if (this.trackDetails) this.monitor.printTreeAfterOperator(T, Parameters.Operator.RPM, false);
/*      */   }
/*      */ 
/*      */   public void GDM(Tree T)
/*      */   {
/* 1179 */     if (this.trackDetails) this.monitor.printTreeBeforeOperator(T, Parameters.Operator.GDM, false);
/* 1180 */     List L = T.getPartitions();
/* 1181 */     Charset C = (Charset)L.get(Tools.randInt(L.size()));
/* 1182 */     double shape = T.getEvaluationGammaShape(C);
/* 1183 */     double mutation = Tools.exponentialMultiplierRand();
/* 1184 */     shape *= mutation;
/* 1185 */     if (this.trackDetails) this.monitor.printOperatorInfos(T, "Gamma dist. shape parameter of " + T.getEvaluationGammaShape(C) + " mutated to " + shape + (L.size() > 1 ? " in partition " + C.getLabel() : ""));
/* 1186 */     T.setEvaluationDistributionShape(C, shape);
/* 1187 */     if (this.trackDetails) this.monitor.printTreeAfterOperator(T, Parameters.Operator.GDM, false);
/*      */   }
/*      */ 
/*      */   public void PIM(Tree T)
/*      */   {
/* 1199 */     if (this.trackDetails) this.monitor.printTreeBeforeOperator(T, Parameters.Operator.PIM, false);
/* 1200 */     List L = T.getPartitions();
/* 1201 */     Charset C = (Charset)L.get(Tools.randInt(L.size()));
/* 1202 */     double pinv = T.getEvaluationPInv(C);
/* 1203 */     double mutation = Tools.positiveNormalRand();
/* 1204 */     pinv *= mutation;
/* 1205 */     if (pinv >= 1.0D) pinv = 0.99D;
/* 1206 */     if (this.trackDetails) this.monitor.printOperatorInfos(T, "P-Inv of " + T.getEvaluationPInv(C) + " mutated to " + pinv + (L.size() > 1 ? " in partition " + C.getLabel() : ""));
/* 1207 */     T.setEvaluationPInv(C, pinv);
/* 1208 */     if (this.trackDetails) this.monitor.printTreeAfterOperator(T, Parameters.Operator.PIM, false);
/*      */   }
/*      */ 
/*      */   public void APRM(Tree T)
/*      */   {
/* 1221 */     if (this.trackDetails) this.monitor.printTreeBeforeOperator(T, Parameters.Operator.APRM, false);
/* 1222 */     List L = T.getPartitions();
/* 1223 */     if (L.size() < 2) {
/* 1224 */       if (this.trackDetails) this.monitor.printOperatorInfos(T, "Tree has only one partition, cannot mutate the among-partition rate.");
/* 1225 */       if (this.trackDetails) this.monitor.printTreeAfterOperator(T, Parameters.Operator.APRM, true);
/* 1226 */       return;
/*      */     }
/* 1228 */     Charset C1 = (Charset)L.get(Tools.randInt(L.size()));
/*      */     Charset C2;
/*      */     do C2 = (Charset)L.get(Tools.randInt(L.size()));
/* 1230 */     while (
/* 1232 */       C2.toString().equals(C1.toString()));
/* 1233 */     double apRate = T.getEvaluationAmongPartitionRate(C1);
/* 1234 */     double mutation = Tools.positiveNormalRand();
/* 1235 */     apRate *= mutation;
/* 1236 */     if (apRate <= 0.0D) apRate = 0.01D;
/* 1237 */     if (this.trackDetails) this.monitor.printOperatorInfos(T, "AP rate in partition " + C1.getLabel() + " of " + T.getEvaluationAmongPartitionRate(C1) + " mutated to " + apRate + ", and AP rate of partition " + C2.getLabel() + " is adjusted accordingly.");
/* 1238 */     T.setEvaluationAmongPartitionRate(C1, apRate, C2);
/* 1239 */     if (this.trackDetails) this.monitor.printTreeAfterOperator(T, Parameters.Operator.APRM, false);
/*      */   }
/*      */ 
/*      */   List<Branch> getSPRValidTargets(Tree T, Branch candidate)
/*      */   {
/* 1251 */     List candidates = buildSPRValidTargets(T, candidate, !T.isInOutgroup(candidate.getNode()));
/* 1252 */     for (Iterator it = candidates.iterator(); it.hasNext(); ) {
/* 1253 */       Branch b = (Branch)it.next();
/* 1254 */       for (Branch neigh : candidate.getAllNeighborBranches()) {
/* 1255 */         if (b.equals(neigh)) {
/* 1256 */           it.remove();
/* 1257 */           break;
/*      */         }
/*      */       }
/*      */     }
/* 1261 */     return candidates;
/*      */   }
/*      */ 
/*      */   private List<Branch> buildSPRValidTargets(Tree T, Branch currentBranch, boolean ingroup)
/*      */   {
/* 1273 */     List candidates = new ArrayList();
/* 1274 */     for (Branch b : currentBranch.getAllNeighborBranches()) {
/* 1275 */       candidates.add(b);
/* 1276 */       if ((!ingroup) || (!T.isInOutgroup(b.getNode()))) {
/* 1277 */         candidates.addAll(buildSPRValidTargets(T, b, ingroup));
/*      */       }
/*      */     }
/* 1280 */     return candidates;
/*      */   }
/*      */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.trees.Operators
 * JD-Core Version:    0.6.2
 */