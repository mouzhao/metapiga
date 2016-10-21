/*      */ package parameters;
/*      */ 
/*      */ import java.awt.Color;
/*      */ import java.io.File;
/*      */ import java.io.IOException;
/*      */ import java.io.PrintStream;
/*      */ import java.io.Writer;
/*      */ import java.util.ArrayList;
/*      */ import java.util.BitSet;
/*      */ import java.util.Collection;
/*      */ import java.util.EnumMap;
/*      */ import java.util.EnumSet;
/*      */ import java.util.HashMap;
/*      */ import java.util.HashSet;
/*      */ import java.util.Hashtable;
/*      */ import java.util.Iterator;
/*      */ import java.util.LinkedHashMap;
/*      */ import java.util.LinkedList;
/*      */ import java.util.List;
/*      */ import java.util.Map;
/*      */ import java.util.Map.Entry;
/*      */ import java.util.Set;
/*      */ import java.util.TreeMap;
/*      */ import java.util.TreeSet;
/*      */ import java.util.concurrent.ExecutionException;
/*      */ import java.util.concurrent.ExecutorService;
/*      */ import java.util.concurrent.Executors;
/*      */ import java.util.concurrent.Future;
/*      */ import java.util.concurrent.TimeUnit;
/*      */ import java.util.concurrent.TimeoutException;
/*      */ import javax.swing.JOptionPane;
/*      */ import javax.swing.text.AttributeSet;
/*      */ import javax.swing.text.BadLocationException;
/*      */ import javax.swing.text.DefaultStyledDocument;
/*      */ import javax.swing.text.SimpleAttributeSet;
/*      */ import javax.swing.text.StyleConstants;
/*      */ import jcuda.driver.CUdevice;
/*      */ import jcuda.driver.CUdevprop;
/*      */ import jcuda.driver.JCudaDriver;
/*      */ import jcuda.runtime.JCuda;
/*      */ import metapiga.MetaPIGA;
/*      */ import metapiga.ProgressHandling;
/*      */ import metapiga.RateParameter;
/*      */ import metapiga.exceptions.CharsetIntersectionException;
/*      */ import metapiga.exceptions.IncompatibleDataException;
/*      */ import metapiga.exceptions.NexusInconsistencyException;
/*      */ import metapiga.exceptions.NoGPUException;
/*      */ import metapiga.exceptions.OutgroupTooBigException;
/*      */ import metapiga.exceptions.UnknownDataException;
/*      */ import metapiga.io.NewickReader;
/*      */ import metapiga.io.ParseTreeException;
/*      */ import metapiga.modelization.Charset;
/*      */ import metapiga.modelization.CodonCharactersBlock;
/*      */ import metapiga.modelization.Dataset;
/*      */ import metapiga.modelization.Dataset.Partition;
/*      */ import metapiga.modelization.data.Codon;
/*      */ import metapiga.modelization.data.Data;
/*      */ import metapiga.modelization.data.DataType;
/*      */ import metapiga.modelization.data.EmpiricalModels;
/*      */ import metapiga.modelization.data.codons.tables.CDHNuclearCode;
/*      */ import metapiga.modelization.data.codons.tables.CodonTransitionTable;
/*      */ import metapiga.modelization.data.codons.tables.CodonTransitionTableFactory;
/*      */ import metapiga.modelization.data.codons.tables.EchinodermFlatwormMitochCode;
/*      */ import metapiga.modelization.data.codons.tables.EuploidNuclearCode;
/*      */ import metapiga.modelization.data.codons.tables.InvertebrateMitochondrialCode;
/*      */ import metapiga.modelization.data.codons.tables.MoldProtoCoelMitochCode;
/*      */ import metapiga.modelization.data.codons.tables.UniversalCodonTransitionTable;
/*      */ import metapiga.modelization.data.codons.tables.VertebrateMitochondrialCode;
/*      */ import metapiga.modelization.modeltest.ModelSampling;
/*      */ import metapiga.monitors.InactiveMonitor;
/*      */ import metapiga.optimization.DFO;
/*      */ import metapiga.optimization.GA;
/*      */ import metapiga.optimization.Optimizer;
/*      */ import metapiga.optimization.Powell;
/*      */ import metapiga.trees.Tree;
/*      */ import metapiga.trees.exceptions.BranchNotFoundException;
/*      */ import metapiga.trees.exceptions.NullAncestorException;
/*      */ import metapiga.trees.exceptions.TooManyNeighborsException;
/*      */ import metapiga.trees.exceptions.UncompatibleOutgroupException;
/*      */ import metapiga.trees.exceptions.UnknownTaxonException;
/*      */ import metapiga.trees.exceptions.UnrootableTreeException;
/*      */ import metapiga.utilities.CudaTools;
/*      */ import metapiga.utilities.Tools;
/*      */ import org.biojavax.bio.phylo.io.nexus.CharactersBlock;
/*      */ import org.biojavax.bio.phylo.io.nexus.DataBlock;
/*      */ import org.biojavax.bio.phylo.io.nexus.MetapigaBlock;
/*      */ import org.biojavax.bio.phylo.io.nexus.NexusFileFormat;
/*      */ import org.biojavax.bio.phylo.io.nexus.TreesBlock;
/*      */ import org.biojavax.bio.phylo.io.nexus.TreesBlock.NewickTreeString;
/*      */ 
/*      */ public class Parameters
/*      */ {
/*      */   public static final String AMBIGUOUS_CODONS = "Ambiguous_codons";
/*      */   public static final String STOP_CODONS = "Stop_codons";
/*  304 */   public static final Color[] availableColors = { 
/*  305 */     new Color(145, 214, 134), 
/*  306 */     new Color(255, 160, 122), 
/*  307 */     new Color(127, 255, 212), 
/*  308 */     new Color(224, 102, 255), 
/*  309 */     new Color(135, 137, 211), 
/*  310 */     new Color(216, 134, 134), 
/*  311 */     new Color(138, 43, 226), 
/*  312 */     new Color(255, 215, 0), 
/*  313 */     new Color(133, 178, 209), 
/*  314 */     new Color(255, 62, 150), 
/*  315 */     new Color(127, 255, 0), 
/*  316 */     new Color(103, 148, 255), 
/*  317 */     new Color(0, 206, 209), 
/*  318 */     new Color(206, 176, 134), 
/*  319 */     new Color(30, 250, 100), 
/*  320 */     new Color(255, 48, 48), 
/*  321 */     new Color(134, 206, 189), 
/*  322 */     new Color(0, 0, 255), 
/*  323 */     new Color(255, 192, 203), 
/*  324 */     new Color(0, 255, 255), 
/*  325 */     new Color(255, 165, 0), 
/*  326 */     new Color(219, 219, 112), 
/*  327 */     new Color(255, 0, 255), 
/*  328 */     new Color(255, 255, 0), 
/*  329 */     new Color(0, 191, 255), 
/*  330 */     new Color(139, 69, 19), 
/*  331 */     new Color(0, 250, 154), 
/*  332 */     new Color(255, 127, 0), 
/*  333 */     new Color(133, 99, 99), 
/*  334 */     new Color(187, 255, 255), 
/*  335 */     new Color(0, 255, 0), 
/*  336 */     new Color(255, 127, 36), 
/*  337 */     new Color(194, 204, 134), 
/*  338 */     new Color(255, 20, 147), 
/*  339 */     new Color(202, 255, 112), 
/*  340 */     new Color(221, 160, 221), 
/*  341 */     new Color(46, 139, 87), 
/*  342 */     new Color(205, 201, 165), 
/*  343 */     new Color(16, 78, 139), 
/*  344 */     new Color(139, 139, 0), 
/*  345 */     new Color(152, 245, 255), 
/*  346 */     new Color(255, 36, 0), 
/*  347 */     new Color(60, 220, 220), 
/*  348 */     new Color(125, 38, 205) };
/*      */   public Heuristic heuristic;
/*      */   public int hcRestart;
/*      */   public SASchedule saSchedule;
/*      */   public double saScheduleParam;
/*      */   public double saLundyC;
/*      */   public double saLundyAlpha;
/*      */   public double saInitAccept;
/*      */   public double saFinalAccept;
/*      */   public SADeltaL saDeltaL;
/*      */   public double saDeltaLPercent;
/*      */   public SAReheating saReheatingType;
/*      */   public double saReheatingValue;
/*      */   public SACooling saCoolingType;
/*      */   public int saCoolingSteps;
/*      */   public int saCoolingSuccesses;
/*      */   public int saCoolingFailures;
/*      */   public int gaIndNum;
/*      */   public GASelection gaSelection;
/*      */   public double gaReplacementStrength;
/*      */   public double gaRecombination;
/*      */   public GAOperatorChange gaOperatorChange;
/*      */   public CPConsensus cpConsensus;
/*      */   public CPOperator cpOperator;
/*      */   public int cpPopNum;
/*      */   public int cpIndNum;
/*      */   public double cpTolerance;
/*      */   public double cpHybridization;
/*      */   public CPSelection cpSelection;
/*      */   public double cpReplacementStrength;
/*      */   public double cpRecombination;
/*      */   public CPOperatorChange cpOperatorChange;
/*      */   public int cpCoreNum;
/*      */   public EvaluationRate evaluationRate;
/*      */   public EvaluationModel evaluationModel;
/*  448 */   private Map<RateParameter, Double> evaluationRateParameters = new EnumMap(RateParameter.class);
/*      */   public EvaluationStateFrequencies evaluationStateFrequencies;
/*      */   public EvaluationDistribution evaluationDistribution;
/*      */   public int evaluationDistributionSubsets;
/*      */   private double evaluationDistributionShape;
/*      */   private double evaluationPInv;
/*  454 */   private Map<Charset, Map<RateParameter, Double>> specificRateParameters = new TreeMap();
/*  455 */   private Map<Charset, Double> specificDistributionShapes = new TreeMap();
/*  456 */   private Map<Charset, Double> specificsPInvs = new TreeMap();
/*      */   public Optimization optimization;
/*      */   public double optimizationUse;
/*      */   public OptimizationAlgorithm optimizationAlgorithm;
/*  460 */   public Set<OptimizationTarget> optimizationTargets = EnumSet.noneOf(OptimizationTarget.class);
/*      */   public StartingTreeGeneration startingTreeGeneration;
/*      */   public double startingTreeGenerationRange;
/*      */   public DistanceModel startingTreeModel;
/*      */   public StartingTreeDistribution startingTreeDistribution;
/*      */   public double startingTreeDistributionShape;
/*      */   public double startingTreePInv;
/*      */   public StartingTreePInvPi startingTreePInvPi;
/*  468 */   public List<Operator> operators = new ArrayList();
/*  469 */   public Map<Operator, Integer> operatorsParameters = new EnumMap(Operator.class);
/*  470 */   public Map<Operator, Double> operatorsFrequencies = new EnumMap(Operator.class);
/*  471 */   public Set<Operator> operatorIsDynamic = EnumSet.noneOf(Operator.class);
/*      */   public int dynamicInterval;
/*      */   public double dynamicMin;
/*      */   public ColumnRemoval columnRemoval;
/*      */   public String outputDir;
/*      */   public String label;
/*      */   public boolean useGrid;
/*      */   public String gridServer;
/*      */   public String gridClient;
/*      */   public String gridModule;
/*      */   public boolean useCloud;
/*      */   public String cloudServer;
/*      */   public String cloudClient;
/*      */   public String cloudModulething;
/*      */   public OperatorSelection operatorSelection;
/*  486 */   public Set<HeuristicStopCondition> sufficientStopConditions = new HashSet();
/*  487 */   public Set<HeuristicStopCondition> necessaryStopConditions = new HashSet();
/*      */   public int stopCriterionSteps;
/*      */   public double stopCriterionTime;
/*      */   public int stopCriterionAutoSteps;
/*      */   public double stopCriterionAutoThreshold;
/*      */   public double stopCriterionConsensusMRE;
/*      */   public int stopCriterionConsensusGeneration;
/*      */   public int stopCriterionConsensusInterval;
/*      */   public ReplicatesStopCondition replicatesStopCondition;
/*      */   public double replicatesMRE;
/*      */   public int replicatesNumber;
/*      */   public int replicatesMaximum;
/*      */   public int replicatesMinimum;
/*      */   public int replicatesInterval;
/*      */   public int replicatesParallel;
/*  502 */   public Set<String> outgroup = new TreeSet();
/*  503 */   public Set<String> deletedTaxa = new TreeSet();
/*      */ 
/*  508 */   public CharsetsContainer charsets = new CharsetsContainer(this);
/*  509 */   public Map<Charset, Color> partitionColors = new TreeMap();
/*  510 */   public Set<LogFile> logFiles = EnumSet.noneOf(LogFile.class);
/*      */   public boolean gridReplicate;
/*      */   public boolean cloudReplicate;
/*      */   public String cloudOutput;
/*      */   private LikelihoodCalculationType likelihoodCalculationType;
/*  515 */   private boolean hasGPU = false;
/*      */   public Dataset dataset;
/*      */   public CharactersBlock charactersBlock;
/*      */   public File nexusFile;
/*  521 */   private CodonCharactersBlock codonCharactersBlock = null;
/*  522 */   public CodonDomainDefinition codonDomain = null;
/*  523 */   private final String stopCodonsWarningMessage = "Your dataset, under current DNA code, contains stop codons. Characters with stop codons will be excluded.";
/*  524 */   private final String ambiguousCodonsWarningMessage = "Your dataset contains ambiguous codons. Characters with ambiguous codons will be excluded.";
/*      */   private CodonTransitionTable codonTransitionTable;
/*  526 */   private CodonTransitionTableType currentDNAtable = CodonTransitionTableType.UNIVERSAL;
/*      */ 
/*  529 */   public List<Tree> startingTrees = new LinkedList();
/*  530 */   public List<String> userSelectionTree = new LinkedList();
/*  531 */   public Map<String, TreesBlock.NewickTreeString> loadedTrees = new LinkedHashMap();
/*  532 */   public Map<String, String> loadedTreesTranslation = new LinkedHashMap();
/*  533 */   public boolean treeLoaded = false;
/*      */ 
/*  535 */   private Tree NJT = null;
/*  536 */   public final ModelSampling modelSampling = new ModelSampling(this);
/*      */ 
/*  539 */   public CUdevice device = new CUdevice();
/*  540 */   public CUdevprop gpuDevProperties = new CUdevprop();
/*  541 */   public String ptxFilePath = "";
/*      */ 
/*      */   public int calibrateLastDomainPosition(int startPosition, int endPosition)
/*      */   {
/*  404 */     return endPosition - domainSize(startPosition, endPosition) % 3;
/*      */   }
/*      */ 
/*      */   private int domainSize(int startPosition, int endPosition) {
/*  408 */     return endPosition - startPosition + 1;
/*      */   }
/*      */ 
/*      */   public Parameters(String label)
/*      */   {
/*  544 */     this.label = label;
/*  545 */     checkForGPU();
/*  546 */     setDefault();
/*      */   }
/*      */ 
/*      */   private void checkForGPU() {
/*  550 */     ExecutorService executor = Executors.newSingleThreadExecutor();
/*      */     try {
/*  552 */       executor.submit(new GPUchecker(null)).get(1L, TimeUnit.SECONDS);
/*  553 */       executor.shutdown();
/*      */     }
/*      */     catch (InterruptedException localInterruptedException)
/*      */     {
/*      */     }
/*      */     catch (ExecutionException localExecutionException)
/*      */     {
/*      */     }
/*      */     catch (TimeoutException localTimeoutException)
/*      */     {
/*      */     }
/*      */   }
/*      */ 
/*      */   public void resetDataset()
/*      */   {
/*  583 */     this.dataset = null;
/*  584 */     this.startingTrees.clear();
/*  585 */     this.userSelectionTree.clear();
/*  586 */     this.loadedTrees.clear();
/*  587 */     this.loadedTreesTranslation.clear();
/*  588 */     this.treeLoaded = false;
/*      */ 
/*  592 */     this.charsets.clearAll();
/*  593 */     this.outgroup.clear();
/*  594 */     this.deletedTaxa.clear();
/*  595 */     this.partitionColors.clear();
/*  596 */     this.NJT = null;
/*      */   }
/*      */ 
/*      */   public void resetAll() {
/*  600 */     this.dataset = null;
/*  601 */     this.startingTrees.clear();
/*  602 */     this.userSelectionTree.clear();
/*  603 */     this.loadedTrees.clear();
/*  604 */     this.loadedTreesTranslation.clear();
/*  605 */     this.treeLoaded = false;
/*  606 */     this.NJT = null;
/*  607 */     setDefault();
/*      */   }
/*      */ 
/*      */   public void setDefault() {
/*  611 */     this.heuristic = Heuristic.CP;
/*  612 */     this.hcRestart = 20;
/*  613 */     this.saSchedule = SASchedule.LUNDY;
/*  614 */     this.saLundyC = 0.5D;
/*  615 */     this.saLundyAlpha = 0.5D;
/*  616 */     this.saInitAccept = 0.7D;
/*  617 */     this.saFinalAccept = 0.01D;
/*  618 */     this.saDeltaL = SADeltaL.BURNIN;
/*  619 */     this.saDeltaLPercent = 0.001D;
/*  620 */     this.saReheatingType = SAReheating.DECREMENTS;
/*  621 */     this.saReheatingValue = 300.0D;
/*  622 */     this.saCoolingType = SACooling.SF;
/*  623 */     this.saCoolingSteps = 20;
/*  624 */     this.saCoolingSuccesses = 10;
/*  625 */     this.saCoolingFailures = 100;
/*  626 */     this.gaIndNum = 8;
/*  627 */     this.gaSelection = GASelection.TOURNAMENT;
/*  628 */     this.gaReplacementStrength = 1.0D;
/*  629 */     this.gaRecombination = 0.1D;
/*  630 */     this.gaOperatorChange = GAOperatorChange.IND;
/*  631 */     this.cpConsensus = CPConsensus.STOCHASTIC;
/*  632 */     this.cpOperator = CPOperator.SUPERVISED;
/*  633 */     this.cpPopNum = 4;
/*  634 */     this.cpIndNum = 4;
/*  635 */     this.cpTolerance = 0.05D;
/*  636 */     this.cpHybridization = 0.1D;
/*  637 */     this.cpSelection = CPSelection.IMPROVE;
/*  638 */     this.cpReplacementStrength = 1.0D;
/*  639 */     this.cpRecombination = 0.1D;
/*  640 */     this.cpOperatorChange = CPOperatorChange.IND;
/*  641 */     this.cpCoreNum = 1;
/*  642 */     this.evaluationRate = EvaluationRate.TREE;
/*  643 */     if (this.dataset != null)
/*  644 */       switch ($SWITCH_TABLE$metapiga$modelization$data$DataType()[this.dataset.getDataType().ordinal()]) {
/*      */       case 1:
/*  646 */         this.evaluationModel = EvaluationModel.JC;
/*  647 */         break;
/*      */       case 2:
/*  649 */         this.evaluationModel = EvaluationModel.POISSON;
/*  650 */         break;
/*      */       case 3:
/*  652 */         this.evaluationModel = EvaluationModel.GTR2;
/*  653 */         break;
/*      */       case 4:
/*  655 */         this.evaluationModel = EvaluationModel.GY;
/*      */       default:
/*  657 */         break;
/*      */       }
/*  659 */     else this.evaluationModel = EvaluationModel.JC;
/*      */ 
/*  661 */     for (RateParameter r : RateParameter.values()) {
/*  662 */       this.evaluationRateParameters.put(r, Double.valueOf(0.5D));
/*      */     }
/*  664 */     setCodonTransitionTable(new UniversalCodonTransitionTable());
/*  665 */     this.evaluationStateFrequencies = EvaluationStateFrequencies.EMPIRICAL;
/*  666 */     this.evaluationDistribution = EvaluationDistribution.NONE;
/*  667 */     this.evaluationDistributionSubsets = 4;
/*  668 */     this.evaluationDistributionShape = 1.0D;
/*  669 */     this.evaluationPInv = 0.0D;
/*  670 */     this.startingTreeGeneration = StartingTreeGeneration.LNJ;
/*  671 */     this.startingTreeGenerationRange = 0.1D;
/*  672 */     if (this.dataset != null)
/*  673 */       switch ($SWITCH_TABLE$metapiga$modelization$data$DataType()[this.dataset.getDataType().ordinal()]) {
/*      */       case 1:
/*  675 */         this.startingTreeModel = DistanceModel.JC;
/*  676 */         break;
/*      */       case 2:
/*  678 */         this.startingTreeModel = DistanceModel.POISSON;
/*  679 */         break;
/*      */       case 3:
/*  681 */         this.startingTreeModel = DistanceModel.GTR2;
/*  682 */         break;
/*      */       case 4:
/*  684 */         this.startingTreeModel = DistanceModel.GY;
/*      */       default:
/*  685 */         break;
/*      */       }
/*  687 */     else this.startingTreeModel = DistanceModel.JC;
/*      */ 
/*  689 */     this.startingTreeDistribution = StartingTreeDistribution.NONE;
/*  690 */     this.startingTreeDistributionShape = 0.5D;
/*  691 */     this.startingTreePInv = 0.0D;
/*  692 */     this.startingTreePInvPi = StartingTreePInvPi.CONSTANT;
/*  693 */     this.optimization = Optimization.CONSENSUSTREE;
/*  694 */     this.optimizationUse = 0.0D;
/*  695 */     this.optimizationAlgorithm = OptimizationAlgorithm.GA;
/*  696 */     this.optimizationTargets.clear();
/*  697 */     this.optimizationTargets.add(OptimizationTarget.BL);
/*  698 */     this.operators.clear();
/*  699 */     this.operatorsParameters.clear();
/*  700 */     this.operatorsFrequencies.clear();
/*  701 */     this.operatorIsDynamic.clear();
/*  702 */     this.operators.add(Operator.NNI);
/*  703 */     this.operators.add(Operator.SPR);
/*  704 */     this.operators.add(Operator.TBR);
/*  705 */     this.operators.add(Operator.TXS);
/*  706 */     this.operatorsParameters.put(Operator.TXS, Integer.valueOf(2));
/*  707 */     this.operators.add(Operator.STS);
/*  708 */     this.operatorsParameters.put(Operator.STS, Integer.valueOf(2));
/*  709 */     this.operators.add(Operator.BLM);
/*  710 */     this.operators.add(Operator.BLMINT);
/*  711 */     this.operatorSelection = OperatorSelection.RANDOM;
/*  712 */     this.dynamicInterval = 100;
/*  713 */     this.dynamicMin = 0.04D;
/*  714 */     this.columnRemoval = ColumnRemoval.NONE;
/*  715 */     this.outputDir = (Tools.getHomeDirectory() + "/MetaPIGA results");
/*  716 */     this.useGrid = false;
/*  717 */     this.gridServer = "";
/*  718 */     this.gridClient = "";
/*  719 */     this.gridModule = "";
/*  720 */     this.useCloud = false;
/*      */ 
/*  724 */     this.charsets.clearAll();
/*  725 */     this.outgroup.clear();
/*  726 */     this.deletedTaxa.clear();
/*  727 */     this.partitionColors.clear();
/*  728 */     this.sufficientStopConditions.add(HeuristicStopCondition.AUTO);
/*  729 */     this.sufficientStopConditions.add(HeuristicStopCondition.CONSENSUS);
/*  730 */     this.stopCriterionSteps = 0;
/*  731 */     this.stopCriterionTime = 0.0D;
/*  732 */     this.stopCriterionAutoSteps = 200;
/*  733 */     this.stopCriterionAutoThreshold = 0.0001D;
/*  734 */     this.stopCriterionConsensusMRE = 0.05D;
/*  735 */     this.stopCriterionConsensusGeneration = 5;
/*  736 */     this.stopCriterionConsensusInterval = 10;
/*  737 */     this.replicatesStopCondition = ReplicatesStopCondition.MRE;
/*  738 */     this.replicatesMRE = 0.05D;
/*  739 */     this.replicatesNumber = 1;
/*  740 */     this.replicatesMinimum = 100;
/*  741 */     this.replicatesMaximum = 10000;
/*  742 */     this.replicatesInterval = 10;
/*  743 */     this.replicatesParallel = 1;
/*  744 */     this.logFiles.clear();
/*  745 */     this.gridReplicate = false;
/*  746 */     this.cloudOutput = "";
/*  747 */     this.treeLoaded = false;
/*  748 */     this.likelihoodCalculationType = LikelihoodCalculationType.CLASSIC;
/*  749 */     this.currentDNAtable = CodonTransitionTableType.UNIVERSAL;
/*      */ 
/*  752 */     if (this.likelihoodCalculationType == LikelihoodCalculationType.GPU)
/*  753 */       initializeGraphicCard();
/*      */   }
/*      */ 
/*      */   public void setParameters(MetapigaBlock mp)
/*      */   {
/*  758 */     this.heuristic = mp.getHeuristic();
/*  759 */     this.hcRestart = mp.getHcRestart();
/*  760 */     this.saSchedule = mp.getSaSchedule();
/*  761 */     this.saScheduleParam = mp.getSaScheduleParam();
/*  762 */     this.saLundyC = mp.getSaLundyC();
/*  763 */     this.saLundyAlpha = mp.getSaLundyAlpha();
/*  764 */     this.saInitAccept = mp.getSaInitAccept();
/*  765 */     this.saFinalAccept = mp.getSaFinalAccept();
/*  766 */     this.saDeltaL = mp.getSaDeltaL();
/*  767 */     this.saDeltaLPercent = mp.getSaDeltaLPercent();
/*  768 */     this.saReheatingType = mp.getSaReheatingType();
/*  769 */     this.saReheatingValue = mp.getSaReheatingValue();
/*  770 */     this.saCoolingType = mp.getSaCoolingType();
/*  771 */     this.saCoolingSteps = mp.getSaCoolingSteps();
/*  772 */     this.saCoolingSuccesses = mp.getSaCoolingSuccesses();
/*  773 */     this.saCoolingFailures = mp.getSaCoolingFailures();
/*  774 */     this.gaIndNum = mp.getGaIndNum();
/*  775 */     this.gaSelection = mp.getGaSelection();
/*  776 */     this.gaReplacementStrength = mp.getGaReplacementStrength();
/*  777 */     this.gaRecombination = mp.getGaRecombination();
/*  778 */     this.gaOperatorChange = mp.getGaOperatorChange();
/*  779 */     this.cpConsensus = mp.getCpConsensus();
/*  780 */     this.cpOperator = mp.getCpOperator();
/*  781 */     this.cpPopNum = mp.getCpPopNum();
/*  782 */     this.cpIndNum = mp.getCpIndNum();
/*  783 */     this.cpTolerance = mp.getCpTolerance();
/*  784 */     this.cpHybridization = mp.getCpHybridization();
/*  785 */     this.cpSelection = mp.getCpSelection();
/*  786 */     this.cpReplacementStrength = mp.getCpReplacementStrength();
/*  787 */     this.cpRecombination = mp.getCpRecombination();
/*  788 */     this.cpOperatorChange = mp.getCpOperatorChange();
/*  789 */     this.cpCoreNum = mp.getCpCoreNum();
/*  790 */     this.evaluationRate = mp.getEvaluationRate();
/*  791 */     this.evaluationModel = mp.getEvaluationModel();
/*      */ 
/*  793 */     Map mpRateParams = mp.getRateParameter();
/*  794 */     for (RateParameter r : mpRateParams.keySet()) {
/*  795 */       Double value = (Double)mpRateParams.get(r);
/*  796 */       this.evaluationRateParameters.put(r, value);
/*      */     }
/*  798 */     this.evaluationStateFrequencies = mp.getEvaluationStateFrequencies();
/*  799 */     this.evaluationDistribution = mp.getEvaluationDistribution();
/*  800 */     this.evaluationDistributionSubsets = mp.getEvaluationDistributionSubsets();
/*  801 */     this.evaluationDistributionShape = mp.getEvaluationDistributionShape();
/*  802 */     this.evaluationPInv = mp.getEvaluationPInv();
/*      */     Charset c;
/*  803 */     for (??? = mp.getSpecificRateParameterCharsets().iterator(); ???.hasNext(); this.specificRateParameters.put(c, mp.getSpecificRateParameters(c))) c = (Charset)???.next();
/*  804 */     this.specificDistributionShapes = mp.getSpecificDistributionShapes();
/*  805 */     this.specificsPInvs = mp.getSpecificPInvs();
/*  806 */     this.startingTreeGeneration = mp.getStartingTreeGeneration();
/*  807 */     this.startingTreeGenerationRange = mp.getStartingTreeGenerationRange();
/*  808 */     this.startingTreeModel = (this.startingTreeGeneration == StartingTreeGeneration.RANDOM ? DistanceModel.NONE : mp.getStartingTreeModel());
/*  809 */     this.startingTreeDistribution = mp.getStartingTreeDistribution();
/*  810 */     this.startingTreeDistributionShape = mp.getStartingTreeDistributionShape();
/*  811 */     this.startingTreePInv = mp.getStartingTreePInv();
/*  812 */     this.startingTreePInvPi = mp.getStartingTreePInvPi();
/*  813 */     this.optimization = mp.getOptimization();
/*  814 */     this.optimizationUse = mp.getOptimizationUse();
/*  815 */     this.optimizationAlgorithm = mp.getOptimizationAlgorithm();
/*  816 */     this.optimizationTargets = mp.getOptimizationTargets();
/*  817 */     if ((this.optimization != Optimization.NEVER) && (this.optimizationTargets.isEmpty())) this.optimizationTargets.add(OptimizationTarget.BL);
/*  818 */     this.operators = mp.getOperators();
/*  819 */     this.operatorsParameters = mp.getOperatorsParameters();
/*  820 */     this.operatorsFrequencies = mp.getOperatorsFrequencies();
/*  821 */     this.operatorIsDynamic = mp.getOperatorIsDynamic();
/*  822 */     this.operatorSelection = mp.getOperatorSelection();
/*  823 */     this.dynamicInterval = mp.getDynamicInterval();
/*  824 */     this.dynamicMin = mp.getDynamicMin();
/*  825 */     this.columnRemoval = mp.getColumnRemoval();
/*  826 */     if (mp.getOutputDir() != null) this.outputDir = mp.getOutputDir();
/*  827 */     if (mp.getLabel() != null) this.label = mp.getLabel();
/*  828 */     this.outgroup = mp.getOutgroup();
/*  829 */     this.deletedTaxa = mp.getDeletedTaxa();
/*  830 */     for (Charset ch : mp.getCharset())
/*      */     {
/*  832 */       this.charsets.addCharset(ch.getLabel(), ch);
/*      */     }
/*      */ 
/*  835 */     this.charsets.replaceExcludedCharsets(mp.getExcludedCharsets());
/*      */ 
/*  837 */     this.charsets.replacePartitionCharsets(mp.getPartitions());
/*  838 */     assignPartitionColors();
/*  839 */     this.sufficientStopConditions = mp.getSufficientStopConditions();
/*  840 */     this.necessaryStopConditions = mp.getNecessaryStopConditions();
/*  841 */     this.stopCriterionSteps = mp.getStopCriterionSteps();
/*  842 */     this.stopCriterionTime = mp.getStopCriterionTime();
/*  843 */     this.stopCriterionAutoSteps = mp.getStopCriterionAutoSteps();
/*  844 */     this.stopCriterionAutoThreshold = mp.getStopCriterionAutoThreshold();
/*  845 */     this.stopCriterionConsensusMRE = mp.getStopCriterionConsensusMRE();
/*  846 */     this.stopCriterionConsensusGeneration = mp.getStopCriterionConsensusGeneration();
/*  847 */     this.stopCriterionConsensusInterval = mp.getStopCriterionConsensusInterval();
/*  848 */     this.replicatesStopCondition = mp.getReplicatesStopCondition();
/*  849 */     this.replicatesMRE = mp.getReplicatesMRE();
/*  850 */     this.replicatesNumber = mp.getReplicatesNumber();
/*  851 */     this.replicatesMinimum = mp.getReplicatesMinimum();
/*  852 */     this.replicatesMaximum = mp.getReplicatesMaximum();
/*  853 */     this.replicatesInterval = mp.getReplicatesInterval();
/*  854 */     this.replicatesParallel = mp.getReplicatesParallel();
/*  855 */     this.logFiles = mp.getLogFiles();
/*  856 */     this.gridReplicate = mp.getGridReplicate();
/*  857 */     this.cloudOutput = mp.getGridOutput();
/*  858 */     this.useGrid = mp.getUseGrid();
/*  859 */     this.gridServer = mp.getGridServer();
/*  860 */     this.gridClient = mp.getGridClient();
/*  861 */     this.gridModule = mp.getGridModule();
/*  862 */     this.useCloud = mp.getUseCloud();
/*  863 */     this.treeLoaded = false;
/*  864 */     this.likelihoodCalculationType = mp.getLikelihoodCalculationType();
/*      */ 
/*  866 */     if (this.likelihoodCalculationType == LikelihoodCalculationType.GPU) {
/*  867 */       initializeGraphicCard();
/*      */     }
/*      */ 
/*  870 */     if (this.heuristic == Heuristic.BS) {
/*  871 */       this.startingTreeGeneration = StartingTreeGeneration.NJ;
/*      */     }
/*  873 */     if ((this.sufficientStopConditions.isEmpty()) && (this.necessaryStopConditions.isEmpty()) && (this.heuristic != Heuristic.BS)) {
/*  874 */       this.sufficientStopConditions.add(HeuristicStopCondition.AUTO);
/*  875 */       if (this.heuristic == Heuristic.CP) this.sufficientStopConditions.add(HeuristicStopCondition.CONSENSUS);
/*      */     }
/*  877 */     if ((this.operators.isEmpty()) && (this.heuristic != Heuristic.BS)) {
/*  878 */       this.operators.add(Operator.NNI);
/*  879 */       this.operators.add(Operator.SPR);
/*  880 */       this.operators.add(Operator.TBR);
/*  881 */       this.operators.add(Operator.TXS);
/*  882 */       this.operatorsParameters.put(Operator.TXS, Integer.valueOf(2));
/*  883 */       this.operators.add(Operator.STS);
/*  884 */       this.operatorsParameters.put(Operator.STS, Integer.valueOf(2));
/*  885 */       this.operators.add(Operator.BLM);
/*  886 */       this.operators.add(Operator.BLMINT);
/*  887 */       if (this.evaluationModel.getNumRateParameters() > 0) {
/*  888 */         this.operators.add(Operator.RPM);
/*  889 */         this.operatorsParameters.put(Operator.RPM, Integer.valueOf(1));
/*      */       }
/*  891 */       if (this.evaluationDistribution == EvaluationDistribution.GAMMA) this.operators.add(Operator.GDM);
/*  892 */       if (this.evaluationPInv > 0.0D) this.operators.add(Operator.PIM);
/*  893 */       if (this.charsets.numPartitions() > 1) this.operators.add(Operator.APRM);
/*      */     }
/*  895 */     for (Operator o : this.operators)
/*  896 */       if (!this.operatorsFrequencies.containsKey(o))
/*  897 */         this.operatorsFrequencies.put(o, Double.valueOf(0.0D));
/*      */   }
/*      */ 
/*      */   public Tree getNJT()
/*      */     throws OutgroupTooBigException, UncompatibleOutgroupException, TooManyNeighborsException, UnknownTaxonException
/*      */   {
/*  903 */     if (this.NJT == null) {
/*  904 */       this.NJT = this.dataset.generateTree(new HashSet(), StartingTreeGeneration.NJ, 0.1D, this.startingTreeModel, 
/*  905 */         this.startingTreeDistribution, this.startingTreeDistributionShape, this.startingTreePInv, this.startingTreePInvPi, this, new InactiveMonitor());
/*  906 */       this.NJT.deleteLikelihoodComputation();
/*      */     }
/*  908 */     return this.NJT;
/*      */   }
/*      */ 
/*      */   public void updateNJTParameters() {
/*  912 */     if (this.NJT != null)
/*  913 */       this.NJT.setEvaluationParameters(this);
/*      */   }
/*      */ 
/*      */   public void assignPartitionColors()
/*      */   {
/*  921 */     this.partitionColors.clear();
/*  922 */     int i = 1;
/*  923 */     for (Iterator iterator = this.charsets.getPartitionIterator(); iterator.hasNext(); ) {
/*  924 */       Charset ch = (Charset)iterator.next();
/*  925 */       if (i >= availableColors.length) i = 1;
/*  926 */       this.partitionColors.put(ch, availableColors[i]);
/*  927 */       i++;
/*      */     }
/*      */   }
/*      */ 
/*      */   public void setRateParameter(String partition, RateParameter r, double value) {
/*  932 */     if (!partition.equals("FULL SET"))
/*      */       try {
/*  934 */         Charset c = this.dataset.getCharset(partition);
/*      */         Map map;
/*      */         Map map;
/*  936 */         if (this.specificRateParameters.containsKey(c)) {
/*  937 */           map = (Map)this.specificRateParameters.get(c);
/*      */         } else {
/*  939 */           map = new EnumMap(RateParameter.class);
/*  940 */           for (RateParameter rp : RateParameter.values()) {
/*  941 */             map.put(rp, Double.valueOf(0.5D));
/*      */           }
/*      */         }
/*  944 */         map.put(r, Double.valueOf(value));
/*  945 */         this.specificRateParameters.put(c, map);
/*      */       } catch (Exception e) {
/*  947 */         System.err.println("Unknown partition: " + partition);
/*      */       }
/*      */     else
/*  950 */       this.evaluationRateParameters.put(r, Double.valueOf(value));
/*      */   }
/*      */ 
/*      */   public void setLikelihoodCalcualtionType(LikelihoodCalculationType type)
/*      */   {
/*  955 */     if ((type == LikelihoodCalculationType.GPU) && (this.hasGPU)) {
/*  956 */       this.likelihoodCalculationType = type;
/*  957 */       initializeGraphicCard();
/*  958 */     } else if (type == LikelihoodCalculationType.CLASSIC) {
/*  959 */       this.likelihoodCalculationType = type;
/*  960 */     } else if ((type == LikelihoodCalculationType.GPU) && (!this.hasGPU)) {
/*  961 */       if (!$assertionsDisabled) throw new AssertionError("Trying to define GPU calculation without a GPU");
/*      */     }
/*  963 */     else if (!$assertionsDisabled) { throw new AssertionError("Unknown calculation method"); }
/*      */   }
/*      */ 
/*      */   public void setEmpiricalRates(EvaluationModel model)
/*      */   {
/*  968 */     this.evaluationRateParameters.putAll(EmpiricalModels.getRateParameters(model));
/*  969 */     this.specificRateParameters.clear();
/*      */   }
/*      */ 
/*      */   public LikelihoodCalculationType getLikelihoodCalculationType() {
/*  973 */     return this.likelihoodCalculationType;
/*      */   }
/*      */ 
/*      */   public boolean hasGPU() {
/*  977 */     return this.hasGPU;
/*      */   }
/*      */   public Map<RateParameter, Double> getRateParameters(Charset c) {
/*  980 */     if (this.specificRateParameters.containsKey(c)) return (Map)this.specificRateParameters.get(c);
/*  981 */     return this.evaluationRateParameters;
/*      */   }
/*      */ 
/*      */   public void setEvaluationDistributionShape(String partition, double value) {
/*  985 */     if (!partition.equals("FULL SET"))
/*      */       try {
/*  987 */         this.specificDistributionShapes.put(this.dataset.getCharset(partition), Double.valueOf(value));
/*      */       } catch (Exception e) {
/*  989 */         System.err.println("Unknown partition: " + partition);
/*      */       }
/*      */     else
/*  992 */       this.evaluationDistributionShape = value;
/*      */   }
/*      */ 
/*      */   public double getEvaluationDistributionShape(Charset c)
/*      */   {
/*  997 */     if (this.specificDistributionShapes.containsKey(c)) return ((Double)this.specificDistributionShapes.get(c)).doubleValue();
/*  998 */     return this.evaluationDistributionShape;
/*      */   }
/*      */ 
/*      */   public void setEvaluationPInv(String partition, double value) {
/* 1002 */     if (!partition.equals("FULL SET"))
/*      */       try {
/* 1004 */         this.specificsPInvs.put(this.dataset.getCharset(partition), Double.valueOf(value));
/*      */       } catch (Exception e) {
/* 1006 */         System.err.println("Unknown partition: " + partition);
/*      */       }
/*      */     else
/* 1009 */       this.evaluationPInv = value;
/*      */   }
/*      */ 
/*      */   public double getEvaluationPInv(Charset c)
/*      */   {
/* 1014 */     if (this.specificsPInvs.containsKey(c)) return ((Double)this.specificsPInvs.get(c)).doubleValue();
/* 1015 */     return this.evaluationPInv;
/*      */   }
/*      */ 
/*      */   public boolean hasPInv()
/*      */   {
/* 1020 */     if ((this.charsets.numPartitions() == 0) || ((this.charsets.numPartitions() == 1) && (this.charsets.containsPartition("FULL SET"))))
/* 1021 */       return this.evaluationPInv > 0.0D;
/* 1022 */     if (this.charsets.numPartitions() == this.specificsPInvs.keySet().size()) {
/* 1023 */       for (localIterator = this.specificsPInvs.values().iterator(); localIterator.hasNext(); ) { double pinv = ((Double)localIterator.next()).doubleValue();
/* 1024 */         if (pinv > 0.0D) return true;
/*      */       }
/* 1026 */       return false;
/*      */     }
/* 1028 */     if (this.evaluationPInv > 0.0D) return true;
/* 1029 */     for (Iterator localIterator = this.specificsPInvs.values().iterator(); localIterator.hasNext(); ) { double pinv = ((Double)localIterator.next()).doubleValue();
/* 1030 */       if (pinv > 0.0D) return true;
/*      */     }
/* 1032 */     return false;
/*      */   }
/*      */ 
/*      */   public void setParameters(CharactersBlock cb)
/*      */   {
/* 1037 */     this.charactersBlock = cb;
/*      */   }
/*      */ 
/*      */   public void setParameters(DataBlock cb) {
/* 1041 */     this.charactersBlock = cb;
/*      */   }
/*      */ 
/*      */   public void setParameters(TreesBlock tb)
/*      */   {
/* 1046 */     this.loadedTrees = tb.getTrees();
/* 1047 */     this.loadedTreesTranslation = tb.getTranslations();
/* 1048 */     this.userSelectionTree = new ArrayList(this.loadedTrees.keySet());
/* 1049 */     setStartingTrees();
/*      */   }
/*      */ 
/*      */   public void addStartingTrees(TreesBlock tb) throws NullAncestorException, UnrootableTreeException
/*      */   {
/* 1054 */     this.loadedTrees.putAll(tb.getTrees());
/* 1055 */     this.loadedTreesTranslation.putAll(tb.getTranslations());
/* 1056 */     this.userSelectionTree = new ArrayList(this.loadedTrees.keySet());
/* 1057 */     setStartingTrees();
/*      */   }
/*      */ 
/*      */   public void addStartingTrees(Collection<Tree> trees) throws NullAncestorException, UnrootableTreeException {
/* 1061 */     for (Tree tree : trees) {
/* 1062 */       boolean treeIsCompatible = tree.getDataset() == this.dataset;
/* 1063 */       if (!treeIsCompatible) {
/* 1064 */         treeIsCompatible = tree.getNumOfLeaves() == this.dataset.getNTax();
/* 1065 */         for (String taxon : this.dataset.getTaxa()) {
/* 1066 */           if (!tree.getDataset().getTaxa().contains(taxon)) treeIsCompatible = false;
/*      */         }
/*      */       }
/* 1069 */       if (treeIsCompatible) this.loadedTrees.put(tree.getName(), tree.toNewick(false, false));
/*      */     }
/* 1071 */     this.userSelectionTree = new ArrayList(this.loadedTrees.keySet());
/* 1072 */     setStartingTrees();
/*      */   }
/*      */ 
/*      */   public void setStartingTrees() {
/* 1076 */     this.startingTrees.clear();
/* 1077 */     int neededTrees = 0;
/* 1078 */     switch ($SWITCH_TABLE$metapiga$parameters$Parameters$Heuristic()[this.heuristic.ordinal()]) {
/*      */     case 5:
/* 1080 */       neededTrees = 0;
/* 1081 */       break;
/*      */     case 1:
/* 1083 */       neededTrees = Math.min(this.hcRestart + 1, this.userSelectionTree.size());
/* 1084 */       break;
/*      */     case 2:
/* 1086 */       neededTrees = 1;
/* 1087 */       break;
/*      */     case 3:
/* 1089 */       neededTrees = this.gaIndNum;
/* 1090 */       break;
/*      */     case 4:
/* 1092 */       neededTrees = this.cpPopNum;
/*      */     }
/*      */ 
/* 1095 */     while ((neededTrees > 0) && (this.userSelectionTree.size() > 0)) {
/* 1096 */       Iterator it = this.userSelectionTree.iterator();
/* 1097 */       while (it.hasNext()) {
/* 1098 */         String treeName = (String)it.next();
/* 1099 */         if (neededTrees == 0) break;
/* 1100 */         NewickReader nr = new NewickReader(this, treeName, ((TreesBlock.NewickTreeString)this.loadedTrees.get(treeName)).getTreeString(), this.loadedTreesTranslation);
/*      */         try {
/* 1102 */           this.startingTrees.add(nr.parseNewick());
/* 1103 */           neededTrees--;
/*      */         } catch (ParseTreeException e) {
/* 1105 */           e.printStackTrace();
/* 1106 */           JOptionPane.showMessageDialog(null, Tools.getErrorPanel("Error", e));
/* 1107 */           it.remove();
/* 1108 */           this.loadedTrees.remove(treeName);
/*      */         }
/*      */       }
/*      */     }
/* 1112 */     if (neededTrees > 0) {
/* 1113 */       JOptionPane.showMessageDialog(null, "Cannot parse any given tree, set starting tree(s) to Loose Neighbor Joining");
/* 1114 */       this.startingTreeGeneration = StartingTreeGeneration.LNJ;
/* 1115 */       this.treeLoaded = false;
/*      */     } else {
/* 1117 */       this.treeLoaded = true;
/*      */     }
/*      */   }
/*      */ 
/*      */   public void buildDataset()
/*      */     throws CharsetIntersectionException, NexusInconsistencyException, UnknownDataException, IncompatibleDataException
/*      */   {
/* 1129 */     this.charsets.removePartition("REMAINING");
/* 1130 */     this.charsets.removePartition("FULL SET");
/* 1131 */     for (Iterator iterator = this.charsets.getPartitionIterator(); iterator.hasNext(); ) {
/* 1132 */       Charset c = (Charset)iterator.next();
/* 1133 */       if (c.isEmpty()) {
/* 1134 */         if (this.charsets.contains(c.getLabel())) {
/* 1135 */           c.merge(this.charsets.getCharset(c.getLabel()));
/*      */         } else {
/* 1137 */           c.addRange(c.getLabel());
/* 1138 */           this.charsets.addCharset(c.getLabel(), c);
/*      */         }
/*      */       }
/*      */     }
/* 1142 */     Set toTest = new HashSet(this.charsets.getPartitions());
/*      */     Iterator localIterator1;
/* 1143 */     for (Iterator iterator = this.charsets.getPartitionIterator(); iterator.hasNext(); 
/* 1146 */       localIterator1.hasNext())
/*      */     {
/* 1144 */       Charset c1 = (Charset)iterator.next();
/* 1145 */       toTest.remove(c1);
/* 1146 */       localIterator1 = toTest.iterator(); continue; Charset c2 = (Charset)localIterator1.next();
/* 1147 */       if (c2.intersect(c1)) {
/* 1148 */         throw new CharsetIntersectionException(c2, c1, 
/* 1149 */           "Cannot be used as partitions.");
/*      */       }
/*      */     }
/* 1152 */     for (Iterator iterator = this.charsets.getExcludedCharsetIterator(); iterator.hasNext(); ) {
/* 1153 */       Charset c = (Charset)iterator.next();
/* 1154 */       if (c.isEmpty()) {
/* 1155 */         if (this.charsets.contains(c.getLabel()))
/*      */         {
/* 1157 */           c.merge(this.charsets.getCharset(c.getLabel()));
/*      */         } else {
/* 1159 */           c.addRange(c.getLabel());
/* 1160 */           this.charsets.addCharset(c.getLabel(), c);
/*      */         }
/*      */       }
/*      */     }
/* 1164 */     assignPartitionColors();
/*      */ 
/* 1166 */     boolean isCodons = areCodons();
/* 1167 */     int dimensionsNChar = 0;
/*      */ 
/* 1169 */     if (!isCodons)
/* 1170 */       dimensionsNChar = this.charactersBlock.getDimensionsNChar();
/*      */     else
/* 1172 */       dimensionsNChar = this.codonDomain.getDimensionsNChar();
/*      */     boolean isInChar;
/* 1174 */     if (this.charsets.isPartitionsEmpty()) {
/* 1175 */       Charset full = new Charset("FULL SET");
/* 1176 */       full.addRange(1, dimensionsNChar);
/* 1177 */       this.charsets.partitionCharset(full);
/* 1178 */       this.partitionColors.put(full, availableColors[0]);
/*      */     } else {
/* 1180 */       Charset remainingCharacters = new Charset("REMAINING");
/* 1181 */       for (int i = 1; i <= dimensionsNChar; i++) {
/* 1182 */         isInChar = false;
/* 1183 */         for (Iterator iterator = this.charsets.getPartitionIterator(); iterator.hasNext(); ) {
/* 1184 */           Charset c = (Charset)iterator.next();
/* 1185 */           if (c.isInCharset(i)) {
/* 1186 */             isInChar = true;
/* 1187 */             break;
/*      */           }
/*      */         }
/* 1190 */         if (!isInChar) {
/* 1191 */           int start = i;
/* 1192 */           isInChar = false;
/* 1193 */           while ((i <= dimensionsNChar) && (!isInChar)) {
/* 1194 */             i++;
/* 1195 */             for (Iterator iterator = this.charsets.getPartitionIterator(); iterator.hasNext(); ) {
/* 1196 */               Charset c = (Charset)iterator.next();
/* 1197 */               if (c.isInCharset(i)) {
/* 1198 */                 isInChar = true;
/* 1199 */                 break;
/*      */               }
/*      */             }
/*      */           }
/* 1203 */           remainingCharacters.addRange(start, i - 1);
/*      */         }
/*      */       }
/* 1206 */       if (!remainingCharacters.isEmpty())
/*      */       {
/* 1208 */         this.charsets.partitionCharset(remainingCharacters);
/* 1209 */         this.charsets.addCharset(remainingCharacters.getLabel(), 
/* 1210 */           remainingCharacters);
/* 1211 */         this.partitionColors.put(remainingCharacters, availableColors[0]);
/*      */       }
/*      */     }
/*      */ 
/* 1215 */     if (isCodons) {
/* 1216 */       this.codonCharactersBlock = new CodonCharactersBlock(this.charactersBlock, 
/* 1217 */         this.codonDomain);
/* 1218 */       Set poorCodons = findStopAndAmbiguousCodons(
/* 1219 */         this.codonCharactersBlock.getDataMatrix(), 
/* 1220 */         getCodonTransitionTable(), this.deletedTaxa);
/* 1221 */       for (Charset poorCharsetCods : poorCodons) {
/* 1222 */         this.charsets.addCharset(poorCharsetCods.getLabel(), poorCharsetCods);
/* 1223 */         this.charsets.excludeCharset(poorCharsetCods);
/*      */       }
/*      */ 
/* 1226 */       this.dataset = new Dataset(this.codonCharactersBlock, this.deletedTaxa, 
/* 1227 */         this.charsets.getExcludedCharsets(), this.charsets.getPartitions(), this.columnRemoval, 
/* 1229 */         getCodonTransitionTable());
/*      */     } else {
/* 1231 */       this.dataset = new Dataset(this.charactersBlock, this.deletedTaxa, 
/* 1232 */         this.charsets.getExcludedCharsets(), this.charsets.getPartitions(), this.columnRemoval);
/*      */     }
/*      */   }
/*      */ 
/*      */   public boolean defineTransitionCodonTable(CodonTransitionTableType tabType)
/*      */   {
/* 1244 */     if (this.currentDNAtable != tabType) {
/* 1245 */       this.currentDNAtable = tabType;
/*      */ 
/* 1247 */       setCodonTransitionTable(CodonTransitionTableFactory.getInstance(tabType));
/* 1248 */       return true;
/*      */     }
/* 1250 */     return false;
/*      */   }
/*      */ 
/*      */   private Set<Charset> findStopAndAmbiguousCodons(Map<String, List<Data>> matrix, CodonTransitionTable transTable, Set<String> deletedTaxa)
/*      */   {
/* 1255 */     Charset stopCodonsSet = new Charset("Stop_codons");
/* 1256 */     stopCodonsSet.setAsNonRecordable();
/* 1257 */     Charset ambiguousCodonsSet = new Charset("Ambiguous_codons");
/* 1258 */     ambiguousCodonsSet.setAsNonRecordable();
/* 1259 */     boolean isWarnedStopCods = false;
/* 1260 */     boolean isWarnedAmbigCods = false;
/* 1261 */     for (Entry entry : matrix.entrySet()) {
/* 1262 */       String taxa = (String)entry.getKey();
/* 1263 */       if (!deletedTaxa.contains(taxa)) {
/* 1264 */         for (int i = 0; i < ((List)entry.getValue()).size(); i++) {
/* 1265 */           Codon codon = (Codon)((List)entry.getValue()).get(i);
/*      */           try {
/* 1267 */             if (DataType.CODON.getUndeterminateData() == codon) {
/* 1268 */               ambiguousCodonsSet.addRange(i + 1);
/* 1269 */               if (!isWarnedStopCods)
/*      */               {
/* 1271 */                 Tools.showWarningMessage(null, "Your dataset contains ambiguous codons. Characters with ambiguous codons will be excluded.", "Warning");
/* 1272 */                 isWarnedStopCods = true;
/*      */               }
/* 1274 */             } else if (transTable.isStopCodon(codon)) {
/* 1275 */               stopCodonsSet.addRange(i + 1);
/* 1276 */               if (!isWarnedAmbigCods)
/*      */               {
/* 1278 */                 Tools.showWarningMessage(null, "Your dataset, under current DNA code, contains stop codons. Characters with stop codons will be excluded.", "Warning");
/* 1279 */                 isWarnedAmbigCods = true;
/*      */               }
/*      */             }
/*      */           } catch (UnknownDataException e) {
/* 1283 */             e.printStackTrace();
/* 1284 */             System.exit(-1);
/*      */           }
/*      */         }
/*      */       }
/*      */     }
/* 1289 */     HashSet poorCodons = new HashSet(2);
/* 1290 */     if (!stopCodonsSet.isEmpty()) poorCodons.add(stopCodonsSet);
/* 1291 */     if (!ambiguousCodonsSet.isEmpty()) poorCodons.add(ambiguousCodonsSet);
/* 1292 */     return poorCodons;
/*      */   }
/*      */ 
/*      */   public void checkParameters()
/*      */   {
/* 1300 */     if (this.evaluationModel.getDataType() != this.dataset.getDataType()) {
/* 1301 */       switch ($SWITCH_TABLE$metapiga$modelization$data$DataType()[this.dataset.getDataType().ordinal()]) {
/*      */       case 1:
/* 1303 */         this.evaluationModel = EvaluationModel.JC;
/* 1304 */         break;
/*      */       case 2:
/* 1306 */         this.evaluationModel = EvaluationModel.POISSON;
/* 1307 */         break;
/*      */       case 3:
/* 1309 */         this.evaluationModel = EvaluationModel.GTR2;
/* 1310 */         break;
/*      */       case 4:
/* 1312 */         this.evaluationModel = EvaluationModel.GY;
/*      */       }
/*      */     }
/*      */ 
/* 1316 */     if ((this.startingTreeModel.getDataType() != null) && (this.startingTreeModel.getDataType() != this.dataset.getDataType()))
/* 1317 */       switch ($SWITCH_TABLE$metapiga$modelization$data$DataType()[this.dataset.getDataType().ordinal()]) {
/*      */       case 1:
/* 1319 */         this.startingTreeModel = DistanceModel.JC;
/* 1320 */         break;
/*      */       case 2:
/* 1322 */         this.startingTreeModel = DistanceModel.POISSON;
/* 1323 */         break;
/*      */       case 3:
/* 1325 */         this.startingTreeModel = DistanceModel.GTR2;
/* 1326 */         break;
/*      */       case 4:
/* 1328 */         this.startingTreeModel = DistanceModel.GY;
/*      */       }
/*      */   }
/*      */ 
/*      */   public boolean hasManyReplicates()
/*      */   {
/* 1334 */     return ((this.replicatesStopCondition == ReplicatesStopCondition.NONE) && (this.replicatesNumber > 1)) || (this.replicatesStopCondition == ReplicatesStopCondition.MRE);
/*      */   }
/*      */ 
/*      */   public DefaultStyledDocument showNexusDataMatrix()
/*      */     throws BadLocationException, UnknownDataException
/*      */   {
/* 1344 */     ProgressHandling progress = MetaPIGA.progressHandling;
/* 1345 */     progress.newSingleProgress(0, this.charactersBlock.getMatrixLabels().size(), "Preparing Nexus data matrix display");
/* 1346 */     int prog = 0;
/* 1347 */     String NORMAL = "Normal";
/* 1348 */     String ITALIC = "Italic";
/* 1349 */     String BOLD = "Bold";
/* 1350 */     DefaultStyledDocument doc = new DefaultStyledDocument();
/* 1351 */     Hashtable paraStyles = new Hashtable();
/* 1352 */     SimpleAttributeSet attr = new SimpleAttributeSet();
/* 1353 */     StyleConstants.setFontFamily(attr, "courier");
/* 1354 */     paraStyles.put("Normal", attr);
/* 1355 */     attr = new SimpleAttributeSet();
/* 1356 */     StyleConstants.setFontFamily(attr, "courier");
/* 1357 */     StyleConstants.setItalic(attr, true);
/* 1358 */     paraStyles.put("Italic", attr);
/* 1359 */     attr = new SimpleAttributeSet();
/* 1360 */     StyleConstants.setBold(attr, true);
/* 1361 */     StyleConstants.setFontFamily(attr, "courier");
/* 1362 */     paraStyles.put("Bold", attr);
/* 1363 */     AttributeSet defaultStyle = (AttributeSet)paraStyles.get("Normal");
/*      */ 
/* 1365 */     AttributeSet boldStyle = (AttributeSet)paraStyles.get("Bold");
/* 1366 */     int longestTaxon = 0;
/* 1367 */     for (Iterator localIterator1 = this.charactersBlock.getMatrixLabels().iterator(); localIterator1.hasNext(); ) { Object taxa = localIterator1.next();
/* 1368 */       if (taxa.toString().length() > longestTaxon) {
/* 1369 */         longestTaxon = taxa.toString().length();
/*      */       }
/*      */     }
/* 1372 */     if ("Positions".toString().length() > longestTaxon) {
/* 1373 */       longestTaxon = "Positions".toString().length();
/*      */     }
/* 1375 */     int nchar = 0;
/* 1376 */     for (Dataset.Partition p : this.dataset.getPartitions()) {
/* 1377 */       nchar += p.getNChar();
/*      */     }
/* 1379 */     doc.insertString(doc.getLength(), 
/* 1380 */       "Character matrix from your nexus file :\n\n", boldStyle);
/* 1381 */     doc.insertString(doc.getLength(), this.dataset.getDataType().verbose() + 
/* 1382 */       " frequencies: " + this.dataset.getNTax() + " taxa and " + nchar + 
/* 1383 */       " " + this.dataset.getDataType().verbose().toLowerCase() + "s.\n", 
/* 1384 */       defaultStyle);
/* 1385 */     doc.insertString(doc.getLength(), "Red " + 
/* 1386 */       this.dataset.getDataType().verbose().toLowerCase() + 
/* 1387 */       "s will be deleted.\n", defaultStyle);
/* 1388 */     doc.insertString(doc.getLength(), 
/* 1389 */       "Equates are replaced by their standard IUB code.\n", 
/* 1390 */       defaultStyle);
/* 1391 */     String matchSymbol = this.charactersBlock.getMatchChar() == null ? "." : 
/* 1392 */       this.charactersBlock.getMatchChar();
/* 1393 */     doc.insertString(doc.getLength(), "'" + matchSymbol + 
/* 1394 */       "' will be replaced by " + 
/* 1395 */       this.dataset.getDataType().verbose().toLowerCase() + 
/* 1396 */       "s at the same position on the first line.\n", defaultStyle);
/* 1397 */     String indeterminateString = "";
/* 1398 */     switch ($SWITCH_TABLE$metapiga$modelization$data$DataType()[this.dataset.getDataType().ordinal()]) {
/*      */     case 1:
/* 1400 */       indeterminateString = "N (A, C, G or T)";
/* 1401 */       break;
/*      */     case 2:
/* 1403 */       indeterminateString = "X (any amino acid)";
/* 1404 */       break;
/*      */     case 3:
/* 1406 */       indeterminateString = "? (0 or 1)";
/* 1407 */       break;
/*      */     case 4:
/* 1409 */       indeterminateString = "__X (any nucleotide codon)";
/*      */     default:
/* 1411 */       indeterminateString = "the undeterminate state";
/*      */     }
/*      */ 
/* 1414 */     String missingSymbol = this.charactersBlock.getMissing() == null ? "." : 
/* 1415 */       this.charactersBlock.getMissing();
/* 1416 */     doc.insertString(doc.getLength(), "'" + missingSymbol + 
/* 1417 */       "' will be replaced by " + indeterminateString + ".\n", 
/* 1418 */       defaultStyle);
/* 1419 */     String gapSymbol = this.charactersBlock.getGap() == null ? "." : 
/* 1420 */       this.charactersBlock.getGap();
/* 1421 */     if (this.columnRemoval == ColumnRemoval.NONE)
/* 1422 */       doc.insertString(doc.getLength(), "'" + gapSymbol + 
/* 1423 */         "' will be replaced by " + indeterminateString + ".\n", 
/* 1424 */         defaultStyle);
/*      */     else {
/* 1426 */       doc.insertString(doc.getLength(), "Column with gaps ('" + gapSymbol + 
/* 1427 */         "') will be deleted.\n", defaultStyle);
/*      */     }
/*      */ 
/* 1430 */     if (!this.charsets.isPartitionsEmpty()) {
/* 1431 */       doc.insertString(doc.getLength(), 
/* 1432 */         "Partitions are colored this way : ", defaultStyle);
/* 1433 */       for (Entry e : this.partitionColors.entrySet()) {
/* 1434 */         StyleConstants.setBackground(attr, (Color)e.getValue());
/* 1435 */         StyleConstants.setForeground(attr, Color.BLACK);
/* 1436 */         doc.insertString(doc.getLength(), ((Charset)e.getKey()).toString(), attr);
/* 1437 */         StyleConstants.setBackground(attr, Color.BLACK);
/* 1438 */         StyleConstants.setForeground(attr, Color.GREEN);
/* 1439 */         doc.insertString(doc.getLength(), " ", attr);
/*      */       }
/* 1441 */       doc.insertString(doc.getLength(), "\n", defaultStyle);
/*      */     }
/* 1443 */     doc.insertString(doc.getLength(), "\n", defaultStyle);
/* 1444 */     boolean nextLine = false;
/* 1445 */     int line = 0;
/* 1446 */     String ws = "Positions";
/* 1447 */     int spaces = longestTaxon - ws.toString().length();
/* 1448 */     for (int j = 0; j < spaces; j++) {
/* 1449 */       ws = ws + " ";
/*      */     }
/* 1451 */     ws = ws + "    ";
/* 1452 */     attr = new SimpleAttributeSet();
/* 1453 */     StyleConstants.setFontFamily(attr, "courier");
/* 1454 */     DataType dataType = this.dataset.getDataType();
/* 1455 */     StyleConstants.setBackground(attr, Color.BLACK);
/* 1456 */     StyleConstants.setForeground(attr, new Color(104, 221, 255));
/* 1457 */     doc.insertString(doc.getLength(), ws, attr);
/* 1458 */     int numOfDigits = this.charactersBlock.getDimensionsNChar().length();
/* 1459 */     int startingPosition = 1;
/* 1460 */     int endPosition = this.charactersBlock.getDimensionsNChar();
/* 1461 */     if (dataType == DataType.CODON) {
/* 1462 */       startingPosition = this.codonDomain.startCodonDomainPosition;
/* 1463 */       endPosition = this.codonDomain.endCodonDomainPosition;
/*      */     }
/*      */ 
/* 1466 */     int grayness = 20;
/* 1467 */     Color notInDomainColor = new Color(grayness, grayness, grayness);
/*      */ 
/* 1469 */     for (int p = 1; p <= this.charactersBlock.getDimensionsNChar(); p++) {
/* 1470 */       String s = p;
/* 1471 */       while (s.length() < numOfDigits) {
/* 1472 */         s = " " + s;
/*      */       }
/* 1474 */       if (s.length() > line + 1) {
/* 1475 */         s = s.charAt(line);
/* 1476 */         nextLine = true;
/*      */       }
/* 1478 */       if (!s.equals(" "))
/*      */       {
/* 1480 */         for (Iterator iterator = this.charsets.getPartitionIterator(); iterator.hasNext(); ) {
/* 1481 */           Charset ch = (Charset)iterator.next();
/* 1482 */           int relativePosition = (p - startingPosition) / dataType.getRenderingSize() + 1;
/* 1483 */           if ((p < startingPosition) || (p > endPosition)) {
/* 1484 */             StyleConstants.setBackground(attr, notInDomainColor);
/* 1485 */             StyleConstants.setForeground(attr, Color.BLACK);
/* 1486 */             break;
/*      */           }
/* 1488 */           if (ch.isInCharset(relativePosition)) {
/* 1489 */             StyleConstants.setBackground(attr, (Color)this.partitionColors.get(ch));
/* 1490 */             StyleConstants.setForeground(attr, Color.BLACK);
/* 1491 */             break;
/*      */           }
/*      */         }
/*      */       }
/* 1495 */       doc.insertString(doc.getLength(), s, attr);
/*      */     }
/* 1497 */     doc.insertString(doc.getLength(), " ", defaultStyle);
/* 1498 */     doc.insertString(doc.getLength(), "\n", defaultStyle);
/* 1499 */     String empty = "";
/* 1500 */     for (int i = 0; i < ws.length(); i++) empty = empty + " ";
/*      */     int relativePosition;
/* 1501 */     while (nextLine) {
/* 1502 */       line++;
/* 1503 */       nextLine = false;
/* 1504 */       doc.insertString(doc.getLength(), empty, defaultStyle);
/* 1505 */       attr = new SimpleAttributeSet();
/* 1506 */       StyleConstants.setFontFamily(attr, "courier");
/* 1507 */       StyleConstants.setBackground(attr, Color.BLACK);
/* 1508 */       StyleConstants.setForeground(attr, new Color(104, 221, 255));
/* 1509 */       for (int p = 1; p <= this.charactersBlock.getDimensionsNChar(); p++) {
/* 1510 */         s = p;
/* 1511 */         while (s.length() < numOfDigits) {
/* 1512 */           s = " " + s;
/*      */         }
/* 1514 */         if (s.length() < line + 1) {
/* 1515 */           s = " ";
/* 1516 */         } else if (s.length() == line + 1) {
/* 1517 */           s = s.charAt(line);
/* 1518 */         } else if (s.length() > line + 1) {
/* 1519 */           s = s.charAt(line);
/* 1520 */           nextLine = true;
/*      */         }
/* 1522 */         if (!s.equals(" "))
/*      */         {
/* 1524 */           for (Iterator iterator = this.charsets.getPartitionIterator(); iterator.hasNext(); ) {
/* 1525 */             Charset ch = (Charset)iterator.next();
/* 1526 */             relativePosition = (p - startingPosition) / dataType.getRenderingSize() + 1;
/* 1527 */             if ((p < startingPosition) || (p > endPosition)) {
/* 1528 */               StyleConstants.setBackground(attr, notInDomainColor);
/* 1529 */               StyleConstants.setForeground(attr, Color.BLACK);
/* 1530 */               break;
/*      */             }
/* 1532 */             if (ch.isInCharset(relativePosition)) {
/* 1533 */               StyleConstants.setBackground(attr, (Color)this.partitionColors.get(ch));
/* 1534 */               StyleConstants.setForeground(attr, Color.BLACK);
/* 1535 */               break;
/*      */             }
/*      */           }
/*      */         }
/* 1539 */         doc.insertString(doc.getLength(), s, attr);
/*      */       }
/* 1541 */       doc.insertString(doc.getLength(), " ", defaultStyle);
/* 1542 */       doc.insertString(doc.getLength(), "\n", defaultStyle);
/*      */     }
/* 1544 */     doc.insertString(doc.getLength(), "\n", defaultStyle);
/*      */ 
/* 1546 */     for (String s = this.charactersBlock.getMatrixLabels().iterator(); s.hasNext(); ) { Object taxa = s.next();
/* 1547 */       progress.setValue(prog++);
/* 1548 */       List data = new LinkedList();
/* 1549 */       for (relativePosition = this.charactersBlock.getMatrixData(taxa.toString()).iterator(); relativePosition.hasNext(); ) { Object obj = relativePosition.next();
/* 1550 */         String nucl = obj.toString();
/* 1551 */         if (nucl.length() > 0) {
/* 1552 */           if (nucl.length() > 1) {
/* 1553 */             BitSet bitSet = new BitSet(dataType.numOfStates());
/* 1554 */             for (char c : nucl.toCharArray()) {
/* 1555 */               bitSet.set(dataType.getStateOf(c));
/*      */             }
/* 1557 */             data.add(dataType.getData(bitSet).toString());
/*      */           }
/* 1559 */           else if ((nucl.equals(matchSymbol)) || (
/* 1560 */             (this.charactersBlock.isRespectCase()) && 
/* 1561 */             (nucl
/* 1561 */             .equalsIgnoreCase(matchSymbol)))) {
/* 1562 */             data.add(matchSymbol);
/* 1563 */           } else if ((nucl.equals(missingSymbol)) || (
/* 1564 */             (this.charactersBlock.isRespectCase()) && 
/* 1565 */             (nucl
/* 1565 */             .equalsIgnoreCase(missingSymbol)))) {
/* 1566 */             data.add(missingSymbol);
/* 1567 */           } else if ((nucl.equals(gapSymbol)) || (
/* 1568 */             (this.charactersBlock.isRespectCase()) && 
/* 1569 */             (nucl
/* 1569 */             .equalsIgnoreCase(gapSymbol)))) {
/* 1570 */             data.add(gapSymbol);
/*      */           } else {
/*      */             try {
/* 1573 */               data.add(nucl.toUpperCase());
/*      */             }
/*      */             catch (Exception e) {
/* 1576 */               e.printStackTrace();
/* 1577 */               throw new UnknownDataException(nucl, 
/* 1578 */                 taxa.toString(), e.getCause());
/*      */             }
/*      */           }
/*      */         }
/*      */       }
/*      */ 
/* 1584 */       attr = new SimpleAttributeSet();
/* 1585 */       StyleConstants.setFontFamily(attr, "courier");
/* 1586 */       if (this.deletedTaxa.contains(taxa.toString()))
/* 1587 */         StyleConstants.setForeground(attr, Color.RED);
/*      */       else {
/* 1589 */         StyleConstants.setForeground(attr, Color.GREEN);
/*      */       }
/* 1591 */       String t = taxa.toString();
/* 1592 */       spaces = longestTaxon - taxa.toString().length();
/* 1593 */       for (int j = 0; j < spaces; j++) {
/* 1594 */         t = t + " ";
/*      */       }
/* 1596 */       t = t + "    ";
/* 1597 */       doc.insertString(doc.getLength(), t, attr);
/* 1598 */       for (int i = 1; i <= data.size(); i++) {
/* 1599 */         attr = new SimpleAttributeSet();
/* 1600 */         StyleConstants.setFontFamily(attr, "courier");
/* 1601 */         String s = (String)data.get(i - 1);
/* 1602 */         startingPosition = 1;
/* 1603 */         endPosition = data.size();
/* 1604 */         if (dataType == DataType.CODON) {
/* 1605 */           startingPosition = this.codonDomain.startCodonDomainPosition;
/* 1606 */           endPosition = this.codonDomain.endCodonDomainPosition;
/*      */         }
/* 1608 */         int relativePosition = (i - startingPosition) / dataType.getRenderingSize() + 1;
/* 1609 */         StyleConstants.setForeground(attr, Color.BLACK);
/* 1610 */         if ((this.deletedTaxa.contains(taxa.toString())) || 
/* 1611 */           ((this.columnRemoval == ColumnRemoval.GAP) && (this.dataset.hasGapAtPos(i))) || (
/* 1612 */           (this.columnRemoval == ColumnRemoval.NGAP) && (this.dataset.hasGapOrNAtPos(i)))) {
/* 1613 */           StyleConstants.setForeground(attr, Color.RED);
/*      */         }
/*      */         else {
/* 1616 */           for (Iterator iterator = this.charsets.getExcludedCharsetIterator(); iterator.hasNext(); ) {
/* 1617 */             Charset ch = (Charset)iterator.next();
/* 1618 */             if (ch.isInCharset(relativePosition)) {
/* 1619 */               StyleConstants.setForeground(attr, Color.RED);
/* 1620 */               break;
/*      */             }
/*      */           }
/*      */         }
/*      */ 
/* 1625 */         for (Iterator iterator = this.charsets.getPartitionIterator(); iterator.hasNext(); ) {
/* 1626 */           Charset ch = (Charset)iterator.next();
/* 1627 */           if ((i < startingPosition) || (i > endPosition)) {
/* 1628 */             StyleConstants.setBackground(attr, notInDomainColor);
/* 1629 */             break;
/*      */           }
/* 1631 */           if (ch.isInCharset(relativePosition)) {
/* 1632 */             StyleConstants.setBackground(attr, 
/* 1633 */               (Color)this.partitionColors.get(ch));
/* 1634 */             break;
/*      */           }
/*      */         }
/* 1637 */         doc.insertString(doc.getLength(), s, attr);
/*      */       }
/* 1639 */       doc.insertString(doc.getLength(), "\n", defaultStyle);
/*      */     }
/* 1641 */     return doc;
/*      */   }
/*      */ 
/*      */   public DefaultStyledDocument showDataset()
/*      */     throws BadLocationException, UnknownDataException
/*      */   {
/* 1651 */     ProgressHandling progress = MetaPIGA.progressHandling;
/* 1652 */     progress.newSingleProgress(0, this.dataset.getTaxa().size(), "Preparing dataset display");
/* 1653 */     int prog = 0;
/* 1654 */     String endl = "\n";
/* 1655 */     String NORMAL = "Normal";
/* 1656 */     String ITALIC = "Italic";
/* 1657 */     String BOLD = "Bold";
/* 1658 */     DataType dataType = this.dataset.getDataType();
/* 1659 */     DefaultStyledDocument doc = new DefaultStyledDocument();
/* 1660 */     Hashtable paraStyles = new Hashtable();
/* 1661 */     SimpleAttributeSet attr = new SimpleAttributeSet();
/* 1662 */     StyleConstants.setFontFamily(attr, "courier");
/* 1663 */     paraStyles.put("Normal", attr);
/* 1664 */     attr = new SimpleAttributeSet();
/* 1665 */     StyleConstants.setFontFamily(attr, "courier");
/* 1666 */     StyleConstants.setItalic(attr, true);
/* 1667 */     paraStyles.put("Italic", attr);
/* 1668 */     attr = new SimpleAttributeSet();
/* 1669 */     StyleConstants.setFontFamily(attr, "courier");
/* 1670 */     StyleConstants.setBold(attr, true);
/* 1671 */     paraStyles.put("Bold", attr);
/* 1672 */     AttributeSet defaultStyle = (AttributeSet)paraStyles.get("Normal");
/*      */ 
/* 1674 */     AttributeSet boldStyle = (AttributeSet)paraStyles.get("Bold");
/* 1675 */     int longestTaxon = 0;
/* 1676 */     for (String taxa : this.dataset.getTaxa()) {
/* 1677 */       if (taxa.length() > longestTaxon) {
/* 1678 */         longestTaxon = taxa.length();
/*      */       }
/*      */     }
/* 1681 */     if ("Weights".toString().length() > longestTaxon) {
/* 1682 */       longestTaxon = "Weights".toString().length();
/*      */     }
/* 1684 */     doc.insertString(doc.getLength(), "Character matrices used in MetaPIGA :" + endl + endl, boldStyle);
/* 1685 */     doc.insertString(doc.getLength(), "Your Nexus matrix has been compressed, you can see the weight of each column on the last line." + endl, defaultStyle);
/* 1686 */     doc.insertString(doc.getLength(), this.dataset.getNTax() + " taxa where kept." + endl, defaultStyle);
/* 1687 */     doc.insertString(doc.getLength(), this.dataset.getDataType().verbose() + " frequencies : " + this.dataset.getDataFrequenciesToString() + endl, defaultStyle);
/*      */ 
/* 1689 */     if (!this.charsets.isPartitionsEmpty()) {
/* 1690 */       doc.insertString(doc.getLength(), "Partitions (each one is used separatly during computation) : " + endl, defaultStyle);
/* 1691 */       for (Entry e : this.partitionColors.entrySet()) {
/* 1692 */         StyleConstants.setBackground(attr, (Color)e.getValue());
/* 1693 */         StyleConstants.setForeground(attr, Color.BLACK);
/* 1694 */         doc.insertString(doc.getLength(), ((Charset)e.getKey()).toString(), attr);
/* 1695 */         doc.insertString(doc.getLength(), " : " + this.dataset.getPartition((Charset)e.getKey()).getNChar() + 
/* 1696 */           " characters (" + this.dataset.getPartition((Charset)e.getKey()).getCompression() + " compression giving " + 
/* 1697 */           this.dataset.getPartition((Charset)e.getKey()).getCompressedNChar() + " characters)" + " - Frequencies : " + 
/* 1698 */           this.dataset.getPartition((Charset)e.getKey()).getDataFrequenciesToString() + endl, defaultStyle);
/*      */       }
/* 1700 */       doc.insertString(doc.getLength(), endl, defaultStyle);
/*      */     }
/* 1702 */     doc.insertString(doc.getLength(), endl, defaultStyle);
/*      */     String characterString;
/* 1703 */     for (String taxa : this.dataset.getTaxa()) {
/* 1704 */       progress.setValue(prog++);
/* 1705 */       int spaces = longestTaxon - taxa.toString().length();
/* 1706 */       String stax = taxa;
/* 1707 */       for (int j = 0; j < spaces; j++) {
/* 1708 */         stax = stax + " ";
/*      */       }
/* 1710 */       stax = stax + "    ";
/* 1711 */       doc.insertString(doc.getLength(), stax, defaultStyle);
/* 1712 */       for (Charset ch : this.dataset.getPartitionCharsets()) {
/* 1713 */         attr = new SimpleAttributeSet();
/* 1714 */         StyleConstants.setFontFamily(attr, "courier");
/* 1715 */         StyleConstants.setBackground(attr, (Color)this.partitionColors.get(ch));
/* 1716 */         StyleConstants.setForeground(attr, Color.BLACK);
/* 1717 */         for (Data data : this.dataset.getPartition(ch).getAllData(taxa)) {
/* 1718 */           characterString = data.toString();
/* 1719 */           if (dataType.getRenderingSize() > 1) characterString = characterString + " ";
/* 1720 */           doc.insertString(doc.getLength(), characterString, attr);
/*      */         }
/* 1722 */         doc.insertString(doc.getLength(), " ", defaultStyle);
/*      */       }
/* 1724 */       doc.insertString(doc.getLength(), endl, defaultStyle);
/*      */     }
/* 1726 */     doc.insertString(doc.getLength(), endl, defaultStyle);
/* 1727 */     boolean nextLine = false;
/* 1728 */     int line = 0;
/* 1729 */     String ws = "Weights";
/* 1730 */     int spaces = longestTaxon - ws.toString().length();
/* 1731 */     for (int j = 0; j < spaces; j++) {
/* 1732 */       ws = ws + " ";
/*      */     }
/* 1734 */     ws = ws + "    ";
/*      */ 
/* 1736 */     doc.insertString(doc.getLength(), ws, defaultStyle);
/*      */     int w;
/*      */     String s;
/* 1737 */     for (Charset ch : this.dataset.getPartitionCharsets()) {
/* 1738 */       attr = new SimpleAttributeSet();
/* 1739 */       StyleConstants.setFontFamily(attr, "courier");
/* 1740 */       StyleConstants.setBackground(attr, (Color)this.partitionColors.get(ch));
/* 1741 */       StyleConstants.setForeground(attr, Color.BLACK);
/*      */       int[] arrayOfInt;
/* 1742 */       characterString = (arrayOfInt = this.dataset.getPartition(ch).getAllWeights()).length; for (String str1 = 0; str1 < characterString; str1++) { w = arrayOfInt[str1];
/* 1743 */         s = w;
/* 1744 */         if (s.length() > line + 1) {
/* 1745 */           s = s.charAt(line);
/* 1746 */           nextLine = true;
/*      */         }
/* 1748 */         s = addSpacesToWeightValue(dataType, s);
/* 1749 */         if (dataType.getRenderingSize() > 1) s = s + " ";
/* 1750 */         doc.insertString(doc.getLength(), s, attr);
/*      */       }
/* 1752 */       doc.insertString(doc.getLength(), " ", defaultStyle);
/*      */     }
/* 1754 */     doc.insertString(doc.getLength(), endl, defaultStyle);
/* 1755 */     String empty = "";
/* 1756 */     for (int i = 0; i < ws.length(); i++) empty = empty + " ";
/* 1757 */     while (nextLine) {
/* 1758 */       line++;
/* 1759 */       nextLine = false;
/* 1760 */       doc.insertString(doc.getLength(), empty, defaultStyle);
/* 1761 */       for (Charset ch : this.dataset.getPartitionCharsets()) {
/* 1762 */         attr = new SimpleAttributeSet();
/* 1763 */         StyleConstants.setFontFamily(attr, "courier");
/* 1764 */         StyleConstants.setBackground(attr, (Color)this.partitionColors.get(ch));
/* 1765 */         StyleConstants.setForeground(attr, Color.BLACK);
/* 1766 */         String str2 = (s = this.dataset.getPartition(ch).getAllWeights()).length; for (characterString = 0; characterString < str2; characterString++) { int w = s[characterString];
/* 1767 */           String s = w;
/* 1768 */           if (s.length() < line + 1) {
/* 1769 */             s = " ";
/* 1770 */           } else if (s.length() == line + 1) {
/* 1771 */             s = s.charAt(line);
/* 1772 */           } else if (s.length() > line + 1) {
/* 1773 */             s = s.charAt(line);
/* 1774 */             nextLine = true;
/*      */           }
/* 1776 */           s = addSpacesToWeightValue(dataType, s);
/* 1777 */           if (dataType.getRenderingSize() > 1) s = s + " ";
/* 1778 */           doc.insertString(doc.getLength(), s, attr);
/*      */         }
/* 1780 */         doc.insertString(doc.getLength(), " ", defaultStyle);
/*      */       }
/* 1782 */       doc.insertString(doc.getLength(), endl, defaultStyle);
/*      */     }
/* 1784 */     return doc;
/*      */   }
/*      */ 
/*      */   private String addSpacesToWeightValue(DataType dataType, String s)
/*      */   {
/* 1793 */     for (int renderingSpaces = 0; renderingSpaces < dataType.getRenderingSize() / 2; renderingSpaces++) {
/* 1794 */       s = " " + s + " ";
/*      */     }
/* 1796 */     if (dataType.getRenderingSize() % 2 == 0) {
/* 1797 */       s = s.substring(0, s.length() - 1);
/*      */     }
/* 1799 */     return s;
/*      */   }
/*      */ 
/*      */   public DataBlock getModifiedDataBlock()
/*      */   {
/* 1811 */     DataType dataType = this.dataset.getDataType();
/* 1812 */     DataBlock newBlock = new DataBlock();
/* 1813 */     newBlock.setGap("-");
/* 1814 */     newBlock.setMatchChar(".");
/* 1815 */     newBlock.setMissing("?");
/* 1816 */     newBlock.setDataType(dataType.name());
/* 1817 */     newBlock.setDimensionsNTax(this.dataset.getNTax());
/* 1818 */     newBlock.setDimensionsNChar(this.dataset.getNChar());
/* 1819 */     List keepedRows = new ArrayList();
/* 1820 */     List deletedCharacters = new ArrayList();
/*      */ 
/* 1822 */     for (Iterator iterator = this.charsets.getCharsetIterator(); iterator.hasNext(); ) {
/* 1823 */       Charset charset = (Charset)iterator.next();
/* 1824 */       deletedCharacters.addAll(charset.getCharacters());
/*      */     }
/* 1826 */     char[][] data = new char[this.charactersBlock.getMatrixLabels().size()][this.charactersBlock.getDimensionsNChar()];
/* 1827 */     List taxaList = new ArrayList();
/* 1828 */     String matchSymbol = this.charactersBlock.getMatchChar() == null ? "." : this.charactersBlock.getMatchChar();
/* 1829 */     String missingSymbol = this.charactersBlock.getMissing() == null ? "?" : this.charactersBlock.getMissing();
/* 1830 */     String gapSymbol = this.charactersBlock.getGap() == null ? "-" : this.charactersBlock.getGap();
/* 1831 */     int currentRow = 0;
/* 1832 */     for (Iterator localIterator1 = this.charactersBlock.getMatrixLabels().iterator(); localIterator1.hasNext(); ) { Object taxa = localIterator1.next();
/* 1833 */       if (!this.deletedTaxa.contains(taxa.toString())) {
/* 1834 */         keepedRows.add(Integer.valueOf(currentRow));
/* 1835 */         taxaList.add(taxa.toString());
/*      */       }
/* 1837 */       int currentCol = 0;
/* 1838 */       for (Iterator localIterator2 = this.charactersBlock.getMatrixData(taxa.toString()).iterator(); localIterator2.hasNext(); ) { Object obj = localIterator2.next();
/* 1839 */         String nucl = obj.toString();
/* 1840 */         if (nucl.length() > 0) {
/* 1841 */           if (nucl.length() > 1) {
/* 1842 */             BitSet bitSet = new BitSet(dataType.numOfStates());
/* 1843 */             for (char c : nucl.toCharArray())
/*      */               try {
/* 1845 */                 bitSet.set(dataType.getStateOf(c));
/*      */               } catch (UnknownDataException e) {
/* 1847 */                 e.printStackTrace();
/*      */               }
/*      */             try
/*      */             {
/* 1851 */               data[currentRow][currentCol] = dataType.getData(bitSet).toChar();
/*      */             } catch (Exception e) {
/* 1853 */               e.printStackTrace();
/*      */             }
/*      */           }
/* 1856 */           else if ((nucl.equals(matchSymbol)) || ((this.charactersBlock.isRespectCase()) && (nucl.equalsIgnoreCase(matchSymbol)))) {
/* 1857 */             data[currentRow][currentCol] = (currentRow > 0 ? data[(currentRow - 1)][currentCol] : 46);
/* 1858 */           } else if ((nucl.equals(missingSymbol)) || ((this.charactersBlock.isRespectCase()) && (nucl.equalsIgnoreCase(missingSymbol)))) {
/* 1859 */             data[currentRow][currentCol] = 63;
/* 1860 */           } else if ((nucl.equals(gapSymbol)) || ((this.charactersBlock.isRespectCase()) && (nucl.equalsIgnoreCase(gapSymbol)))) {
/* 1861 */             data[currentRow][currentCol] = 45;
/*      */           } else {
/*      */             try {
/* 1864 */               data[currentRow][currentCol] = dataType.getData(nucl.toUpperCase()).toChar();
/*      */             } catch (Exception e) {
/* 1866 */               e.printStackTrace();
/*      */             }
/*      */           }
/*      */ 
/* 1870 */           currentCol++;
/*      */         }
/*      */       }
/* 1873 */       currentRow++;
/*      */     }
/* 1875 */     int rows = keepedRows.size();
/* 1876 */     int columns = this.charactersBlock.getDimensionsNChar() - deletedCharacters.size();
/* 1877 */     char[][] dataMatrix = new char[rows][columns];
/* 1878 */     int curCol = 0;
/* 1879 */     for (int c = 0; c < data[0].length; c++) {
/* 1880 */       int character = c + 1;
/* 1881 */       if (!deletedCharacters.contains(Integer.valueOf(character))) {
/* 1882 */         int curRow = 0;
/* 1883 */         for (Iterator localIterator3 = keepedRows.iterator(); localIterator3.hasNext(); ) { int r = ((Integer)localIterator3.next()).intValue();
/* 1884 */           dataMatrix[curRow][curCol] = data[r][c];
/* 1885 */           curRow++;
/*      */         }
/* 1887 */         curCol++;
/*      */       }
/*      */     }
/* 1890 */     for (int i = 0; i < dataMatrix.length; i++) {
/* 1891 */       String taxon = (String)taxaList.get(i);
/* 1892 */       newBlock.addMatrixEntry(taxon);
/* 1893 */       for (int j = 0; j < dataMatrix[i].length; j++) {
/* 1894 */         newBlock.appendMatrixData(taxon, dataMatrix[i][j]);
/*      */       }
/*      */     }
/* 1897 */     return newBlock;
/*      */   }
/*      */ 
/*      */   public MetapigaBlock getMetapigaBlock() {
/* 1901 */     MetapigaBlock mp = new MetapigaBlock();
/* 1902 */     mp.setHeuristic(this.heuristic);
/* 1903 */     mp.setHcRestart(this.hcRestart);
/* 1904 */     mp.setSaSchedule(this.saSchedule);
/* 1905 */     mp.setSaScheduleParam(this.saScheduleParam);
/* 1906 */     mp.setSaLundyC(this.saLundyC);
/* 1907 */     mp.setSaLundyAlpha(this.saLundyAlpha);
/* 1908 */     mp.setSaInitAccept(this.saInitAccept);
/* 1909 */     mp.setSaFinalAccept(this.saFinalAccept);
/* 1910 */     mp.setSaDeltaL(this.saDeltaL);
/* 1911 */     mp.setSaDeltaLPercent(this.saDeltaLPercent);
/* 1912 */     mp.setSaReheatingType(this.saReheatingType);
/* 1913 */     mp.setSaReheatingValue(this.saReheatingValue);
/* 1914 */     mp.setSaCoolingType(this.saCoolingType);
/* 1915 */     mp.setSaCoolingSteps(this.saCoolingSteps);
/* 1916 */     mp.setSaCoolingSuccesses(this.saCoolingSuccesses);
/* 1917 */     mp.setSaCoolingFailures(this.saCoolingFailures);
/* 1918 */     mp.setGaIndNum(this.gaIndNum);
/* 1919 */     mp.setGaSelection(this.gaSelection);
/* 1920 */     mp.setGaReplacementStrength(this.gaReplacementStrength);
/* 1921 */     mp.setGaRecombination(this.gaRecombination);
/* 1922 */     mp.setGaOperatorChange(this.gaOperatorChange);
/* 1923 */     mp.setCpConsensus(this.cpConsensus);
/* 1924 */     mp.setCpOperator(this.cpOperator);
/* 1925 */     mp.setCpPopNum(this.cpPopNum);
/* 1926 */     mp.setCpIndNum(this.cpIndNum);
/* 1927 */     mp.setCpTolerance(this.cpTolerance);
/* 1928 */     mp.setCpHybridization(this.cpHybridization);
/* 1929 */     mp.setCpSelection(this.cpSelection);
/* 1930 */     mp.setCpReplacementStrength(this.cpReplacementStrength);
/* 1931 */     mp.setCpRecombination(this.cpRecombination);
/* 1932 */     mp.setCpOperatorChange(this.cpOperatorChange);
/* 1933 */     mp.setCpCoreNum(this.cpCoreNum);
/* 1934 */     mp.setEvaluationRate(this.evaluationRate);
/* 1935 */     mp.setEvaluationModel(this.evaluationModel);
/*      */     Entry x;
/* 1936 */     for (Iterator localIterator1 = this.evaluationRateParameters.entrySet().iterator(); localIterator1.hasNext(); mp.addRateParameter((RateParameter)x.getKey(), ((Double)x.getValue()).doubleValue())) x = (Entry)localIterator1.next();
/* 1937 */     mp.setEvaluationStateFrequencies(this.evaluationStateFrequencies);
/* 1938 */     mp.setEvaluationDistribution(this.evaluationDistribution, this.evaluationDistributionSubsets);
/* 1939 */     mp.setEvaluationDistributionShape(this.evaluationDistributionShape);
/* 1940 */     mp.setEvaluationPInv(this.evaluationPInv);
/*      */     Iterator localIterator2;
/* 1941 */     for (localIterator1 = this.specificRateParameters.entrySet().iterator(); localIterator1.hasNext(); 
/* 1942 */       localIterator2.hasNext())
/*      */     {
/* 1941 */       Entry y = (Entry)localIterator1.next();
/* 1942 */       localIterator2 = ((Map)y.getValue()).entrySet().iterator(); continue; Entry x = (Entry)localIterator2.next();
/* 1943 */       mp.addSpecificRateParameter((Charset)y.getKey(), (RateParameter)x.getKey(), ((Double)x.getValue()).doubleValue());
/*      */     }
/* 1945 */     Entry x;
/* 1945 */     for (localIterator1 = this.specificDistributionShapes.entrySet().iterator(); localIterator1.hasNext(); mp.addSpecificDistributionShape((Charset)x.getKey(), (Double)x.getValue())) x = (Entry)localIterator1.next();
/* 1946 */     Entry x;
/* 1946 */     for (localIterator1 = this.specificsPInvs.entrySet().iterator(); localIterator1.hasNext(); mp.addSpecificPInv((Charset)x.getKey(), (Double)x.getValue())) x = (Entry)localIterator1.next();
/* 1947 */     mp.setStartingTreeGeneration(this.startingTreeGeneration);
/* 1948 */     mp.setStartingTreeGenerationRange(this.startingTreeGenerationRange);
/* 1949 */     mp.setStartingTreeModel(this.startingTreeModel);
/* 1950 */     mp.setStartingTreeDistribution(this.startingTreeDistribution, this.startingTreeDistributionShape);
/* 1951 */     mp.setStartingTreePInv(this.startingTreePInv);
/* 1952 */     mp.setStartingTreePInvPi(this.startingTreePInvPi);
/* 1953 */     mp.setOptimization(this.optimization);
/* 1954 */     mp.setOptimizationUse(this.optimizationUse);
/* 1955 */     mp.setOptimizationAlgorithm(this.optimizationAlgorithm);
/*      */     OptimizationTarget x;
/* 1956 */     for (localIterator1 = this.optimizationTargets.iterator(); localIterator1.hasNext(); mp.addOptimizationTarget(x)) x = (OptimizationTarget)localIterator1.next();
/* 1957 */     Operator x;
/* 1957 */     for (localIterator1 = this.operators.iterator(); localIterator1.hasNext(); mp.addOperator(x)) x = (Operator)localIterator1.next();
/* 1958 */     Entry x;
/* 1958 */     for (localIterator1 = this.operatorsParameters.entrySet().iterator(); localIterator1.hasNext(); mp.addOperatorsParameter((Operator)x.getKey(), ((Integer)x.getValue()).intValue())) x = (Entry)localIterator1.next();
/* 1959 */     Entry x;
/* 1959 */     for (localIterator1 = this.operatorsFrequencies.entrySet().iterator(); localIterator1.hasNext(); mp.addOperatorsFrequency((Operator)x.getKey(), ((Double)x.getValue()).doubleValue())) x = (Entry)localIterator1.next();
/* 1960 */     Operator x;
/* 1960 */     for (localIterator1 = this.operatorIsDynamic.iterator(); localIterator1.hasNext(); mp.addOperatorIsDynamic(x)) x = (Operator)localIterator1.next();
/* 1961 */     mp.setOperatorSelection(this.operatorSelection);
/* 1962 */     mp.setDynamicInterval(this.dynamicInterval);
/* 1963 */     mp.setDynamicMin(this.dynamicMin);
/* 1964 */     mp.setColumnRemoval(this.columnRemoval);
/* 1965 */     mp.setOutputDir(this.outputDir);
/* 1966 */     mp.setLabel(this.label);
/*      */     String x;
/* 1967 */     for (localIterator1 = this.outgroup.iterator(); localIterator1.hasNext(); mp.addOutgroup(x)) x = (String)localIterator1.next();
/* 1968 */     String x;
/* 1968 */     for (localIterator1 = this.deletedTaxa.iterator(); localIterator1.hasNext(); mp.addDeletedTaxa(x)) x = (String)localIterator1.next();
/*      */ 
/* 1970 */     for (Iterator iterator = this.charsets.getCharsetIterator(); iterator.hasNext(); ) {
/* 1971 */       Charset x = (Charset)iterator.next();
/* 1972 */       mp.addCharset(x);
/*      */     }
/*      */ 
/* 1975 */     for (Iterator iterator = this.charsets.getExcludedCharsetIterator(); iterator.hasNext(); ) {
/* 1976 */       Charset x = (Charset)iterator.next();
/* 1977 */       mp.addExcludedCharset(x);
/*      */     }
/*      */ 
/* 1980 */     for (Iterator iterator = this.charsets.getPartitionIterator(); iterator.hasNext(); ) {
/* 1981 */       x = (Charset)iterator.next();
/* 1982 */       mp.addPartition((Charset)x);
/*      */     }
/* 1984 */     HeuristicStopCondition x;
/* 1984 */     for (Object x = this.sufficientStopConditions.iterator(); ((Iterator)x).hasNext(); mp.addSufficientStopCondition(x)) x = (HeuristicStopCondition)((Iterator)x).next();
/* 1985 */     HeuristicStopCondition x;
/* 1985 */     for (x = this.necessaryStopConditions.iterator(); ((Iterator)x).hasNext(); mp.addNecessaryStopCondition(x)) x = (HeuristicStopCondition)((Iterator)x).next();
/* 1986 */     mp.setStopCriterionSteps(this.stopCriterionSteps);
/* 1987 */     mp.setStopCriterionTime(this.stopCriterionTime);
/* 1988 */     mp.setStopCriterionAutoSteps(this.stopCriterionAutoSteps);
/* 1989 */     mp.setStopCriterionAutoThreshold(this.stopCriterionAutoThreshold);
/* 1990 */     mp.setStopCriterionConsensusMRE(this.stopCriterionConsensusMRE);
/* 1991 */     mp.setStopCriterionConsensusGeneration(this.stopCriterionConsensusGeneration);
/* 1992 */     mp.setStopCriterionConsensusInterval(this.stopCriterionConsensusInterval);
/* 1993 */     mp.setReplicatesStopCondition(this.replicatesStopCondition);
/* 1994 */     mp.setReplicatesMRE(this.replicatesMRE);
/* 1995 */     mp.setReplicatesNumber(this.replicatesNumber);
/* 1996 */     mp.setReplicatesMinimum(this.replicatesMinimum);
/* 1997 */     mp.setReplicatesMaximum(this.replicatesMaximum);
/* 1998 */     mp.setReplicatesInterval(this.replicatesInterval);
/* 1999 */     mp.setReplicatesParallel(this.replicatesParallel);
/*      */     LogFile x;
/* 2000 */     for (x = this.logFiles.iterator(); ((Iterator)x).hasNext(); mp.addLogFile(x)) x = (LogFile)((Iterator)x).next();
/* 2001 */     mp.setGridReplicate(this.gridReplicate);
/* 2002 */     mp.setGridOutput(this.cloudOutput);
/* 2003 */     mp.setUseGrid(this.useGrid);
/* 2004 */     mp.setGridServer(this.gridServer);
/* 2005 */     mp.setGridClient(this.gridClient);
/* 2006 */     mp.setGridModule(this.gridModule);
/* 2007 */     mp.setLikelihoodCalculationType(this.likelihoodCalculationType);
/* 2008 */     if (areCodons()) {
/* 2009 */       mp.setDataType(this.dataset.getDataType());
/* 2010 */       mp.setCodonDomainRange(this.codonDomain.getStartCodonDomainPosition(), this.codonDomain.getEndCodonDomainPosition());
/* 2011 */       mp.setCodonTable(this.currentDNAtable);
/*      */     }
/* 2013 */     return mp;
/*      */   }
/*      */ 
/*      */   public void writeTreeBlock(Writer writer) throws IOException, NullAncestorException, UnrootableTreeException {
/* 2017 */     String endl = NexusFileFormat.NEW_LINE;
/* 2018 */     writer.write(endl + endl + "BEGIN TREES; [Starting trees]" + endl);
/* 2019 */     for (Tree tree : this.startingTrees) {
/* 2020 */       writer.write(tree.toNewickLine(false, false) + endl);
/*      */     }
/* 2022 */     writer.write("END;" + endl + endl);
/*      */   }
/*      */ 
/*      */   public String toString() {
/* 2026 */     return this.label;
/*      */   }
/*      */ 
/*      */   public String printParameters()
/*      */   {
/* 2033 */     String s = "";
/* 2034 */     s = s + "Current parameters are :\n";
/* 2035 */     s = s + "Output directory : " + this.outputDir + "/" + this.label + "\n";
/* 2036 */     s = s + "use heuristic : ";
/* 2037 */     s = s + this.heuristic.verbose();
/* 2038 */     switch ($SWITCH_TABLE$metapiga$parameters$Parameters$Heuristic()[this.heuristic.ordinal()]) {
/*      */     case 5:
/* 2040 */       break;
/*      */     case 1:
/* 2042 */       if (this.hcRestart > 0)
/* 2043 */         s = s + " (random-restart hill climbing with " + this.hcRestart + " restart)";
/*      */       else {
/* 2045 */         s = s + " (stochastic hill climbing)";
/*      */       }
/* 2047 */       break;
/*      */     case 2:
/* 2049 */       s = s + " using " + this.saSchedule + " cooling schedule";
/* 2050 */       if ((this.saSchedule == SASchedule.GEOM) || (this.saSchedule == SASchedule.RP)) s = s + " (with parameter " + Tools.doubletoString(this.saScheduleParam, 6) + ")";
/* 2051 */       s = s + "\n";
/* 2052 */       if (this.saSchedule == SASchedule.LUNDY) {
/* 2053 */         s = s + "Parameter c = " + this.saLundyC + " and parameter alpha = " + this.saLundyAlpha + "\n";
/*      */       } else {
/* 2055 */         s = s + "Inital maximum acceptance probability of " + Tools.doubleToPercent(this.saInitAccept, 2);
/* 2056 */         if ((this.saSchedule == SASchedule.LIN) || (this.saSchedule == SASchedule.TRI) || (this.saSchedule == SASchedule.EXP) || (this.saSchedule == SASchedule.LOG) || 
/* 2057 */           (this.saSchedule == SASchedule.PER) || (this.saSchedule == SASchedule.SPER) || (this.saSchedule == SASchedule.COSH) || (this.saSchedule == SASchedule.TANH)) {
/* 2058 */           s = s + " and final maximum acceptance probability of " + Tools.doubleToPercent(this.saFinalAccept, 2);
/*      */         }
/*      */       }
/* 2061 */       s = s + "\n";
/* 2062 */       switch ($SWITCH_TABLE$metapiga$parameters$Parameters$SADeltaL()[this.saDeltaL.ordinal()]) {
/*      */       case 2:
/* 2064 */         s = s + "A burn-in period is done to compute delta L for starting temperature.\n";
/* 2065 */         break;
/*      */       case 1:
/* 2067 */         s = s + "The delta L used in starting temperature is " + Tools.doubleToPercent(this.saDeltaLPercent, 4) + "% of the NJT.\n";
/*      */       }
/*      */ 
/* 2070 */       switch ($SWITCH_TABLE$metapiga$parameters$Parameters$SAReheating()[
/* 2070 */         this.saReheatingType.ordinal()]) {
/*      */       case 1:
/* 2072 */         s = s + "Temperature is reset when it has been decreased " + (int)this.saReheatingValue + " times.";
/* 2073 */         break;
/*      */       case 3:
/* 2075 */         s = s + "Temperature is never reset.";
/* 2076 */         break;
/*      */       case 2:
/* 2078 */         s = s + "Temperature is reset when it reach " + Tools.doubleToPercent(this.saReheatingValue, 6) + " of starting temperature.";
/*      */       }
/*      */ 
/* 2081 */       switch ($SWITCH_TABLE$metapiga$parameters$Parameters$SACooling()[
/* 2081 */         this.saCoolingType.ordinal()]) {
/*      */       case 1:
/* 2083 */         s = s + " Temperature is decreased after " + this.saCoolingSteps + " steps.";
/* 2084 */         break;
/*      */       case 2:
/* 2086 */         s = s + " Temperature is decreased after " + this.saCoolingSuccesses + " successes or " + this.saCoolingFailures + " failures.";
/*      */       }
/*      */ 
/* 2089 */       break;
/*      */     case 3:
/* 2091 */       s = s + " with " + this.gaIndNum + " individuals and changing operator at each " + this.gaOperatorChange + "\n";
/* 2092 */       s = s + "Selection used is " + this.gaSelection;
/* 2093 */       if (this.gaSelection == GASelection.REPLACEMENT) {
/* 2094 */         s = s + " with a strength of " + this.gaReplacementStrength;
/*      */       }
/* 2096 */       if (this.gaRecombination <= 0.0D) s = s + " without recombination"; else
/* 2097 */         s = s + " with " + Tools.doubleToPercent(this.gaRecombination, 0) + " recombination";
/* 2098 */       break;
/*      */     case 4:
/* 2100 */       s = s + "(" + this.cpConsensus + " - " + this.cpOperator + ") with " + this.cpPopNum + " populations, " + this.cpIndNum + 
/* 2101 */         " individuals, a tolerance of " + Tools.doubleToPercent(this.cpTolerance, 0) + (
/* 2102 */         this.cpHybridization <= 0.0D ? ", no interpopulation hybridization" : new StringBuilder(", ").append(Tools.doubleToPercent(this.cpHybridization, 0)).append(" of interpopulation hybridization").toString()) + 
/* 2103 */         " and changing operator at each " + this.cpOperatorChange + "\n";
/* 2104 */       s = s + "Selection used is " + this.cpSelection;
/* 2105 */       if (this.cpSelection == CPSelection.REPLACEMENT) {
/* 2106 */         s = s + " with a strength of " + this.cpReplacementStrength;
/*      */       }
/* 2108 */       if (this.cpRecombination <= 0.0D) s = s + " without recombination"; else
/* 2109 */         s = s + " with " + Tools.doubleToPercent(this.cpRecombination, 0) + " recombination";
/* 2110 */       if (this.cpCoreNum > 1) {
/* 2111 */         s = s + "\n";
/* 2112 */         s = s + "Parallelization is active : populations are distributed on " + this.cpCoreNum + " cores";
/*      */       }
/*      */       break;
/*      */     }
/* 2116 */     s = s + "\n";
/* 2117 */     s = s + "Use evaluation criterion";
/* 2118 */     s = s + " Maximum Likelihood with rate matrix R for " + this.evaluationRate;
/* 2119 */     s = s + " using " + this.evaluationModel + " model";
/* 2120 */     if (this.evaluationModel.isEmpirical()) {
/* 2121 */       switch ($SWITCH_TABLE$metapiga$parameters$Parameters$EvaluationStateFrequencies()[this.evaluationStateFrequencies.ordinal()]) {
/*      */       case 1:
/* 2123 */         s = s + " (with empirical equilibrium amino acid frequencies)";
/* 2124 */         break;
/*      */       case 2:
/* 2126 */         s = s + " (with estimated equilibrium amino acid frequencies)";
/*      */       }
/*      */     }
/*      */ 
/* 2130 */     s = s + " and " + this.evaluationDistribution + " distribution";
/* 2131 */     if ((this.evaluationDistribution == EvaluationDistribution.GAMMA) || (this.evaluationDistribution == EvaluationDistribution.VDP)) {
/* 2132 */       s = s + " (with " + this.evaluationDistributionSubsets + " subsets)";
/*      */     }
/* 2134 */     s = s + ".\n";
/* 2135 */     Set specifics = new TreeSet();
/* 2136 */     specifics.addAll(this.specificRateParameters.keySet());
/* 2137 */     specifics.addAll(this.specificDistributionShapes.keySet());
/* 2138 */     specifics.addAll(this.specificsPInvs.keySet());
/* 2139 */     for (Charset ch : specifics) {
/* 2140 */       s = s + "Model parameters specifics to partition " + ch.getLabel() + ": ";
/* 2141 */       if (this.specificRateParameters.containsKey(ch))
/* 2142 */         switch ($SWITCH_TABLE$metapiga$parameters$Parameters$EvaluationModel()[this.evaluationModel.ordinal()]) {
/*      */         case 18:
/*      */         case 19:
/* 2145 */           s = s + "rate matrix parameter is ";
/* 2146 */           s = s + RateParameter.K.verbose() + "(" + ((Map)this.specificRateParameters.get(ch)).get(RateParameter.K) + ")";
/* 2147 */           s = s + ", ";
/* 2148 */           break;
/*      */         case 17:
/* 2150 */           s = s + "rate matrix parameters are ";
/* 2151 */           s = s + RateParameter.K1.verbose() + "(" + ((Map)this.specificRateParameters.get(ch)).get(RateParameter.K1) + ") and ";
/* 2152 */           s = s + RateParameter.K2.verbose() + "(" + ((Map)this.specificRateParameters.get(ch)).get(RateParameter.K2) + ")";
/* 2153 */           s = s + ", ";
/* 2154 */           break;
/*      */         case 16:
/* 2156 */           s = s + "rate matrix parameters are ";
/* 2157 */           s = s + RateParameter.A.verbose() + "(" + ((Map)this.specificRateParameters.get(ch)).get(RateParameter.A) + "), ";
/* 2158 */           s = s + RateParameter.B.verbose() + "(" + ((Map)this.specificRateParameters.get(ch)).get(RateParameter.B) + "), ";
/* 2159 */           s = s + RateParameter.C.verbose() + "(" + ((Map)this.specificRateParameters.get(ch)).get(RateParameter.C) + "), ";
/* 2160 */           s = s + RateParameter.D.verbose() + "(" + ((Map)this.specificRateParameters.get(ch)).get(RateParameter.D) + ") and ";
/* 2161 */           s = s + RateParameter.E.verbose() + "(" + ((Map)this.specificRateParameters.get(ch)).get(RateParameter.E) + ")";
/* 2162 */           s = s + ", ";
/* 2163 */           break;
/*      */         case 2:
/* 2165 */           s = s + "rate matrix parameters are ";
/* 2166 */           RateParameter[] rp = RateParameter.getParametersOfModel(EvaluationModel.GTR20);
/* 2167 */           for (int r = 0; r < rp.length; r++) {
/* 2168 */             s = s + rp[r].verbose() + "(" + ((Map)this.specificRateParameters.get(ch)).get(rp[r]) + ")";
/* 2169 */             if (r < rp.length - 2)
/* 2170 */               s = s + ", ";
/* 2171 */             else if (r == rp.length - 2) {
/* 2172 */               s = s + " and ";
/*      */             }
/*      */           }
/* 2175 */           s = s + ", ";
/* 2176 */           break;
/*      */         case 3:
/* 2178 */           s = s + "rate matrix parameters are ";
/* 2179 */           s = s + RateParameter.KAPPA.verbose() + "(" + ((Map)this.specificRateParameters.get(ch)).get(RateParameter.KAPPA) + ") and ";
/* 2180 */           s = s + RateParameter.OMEGA.verbose() + "(" + ((Map)this.specificRateParameters.get(ch)).get(RateParameter.OMEGA) + ")";
/* 2181 */           s = s + ", ";
/* 2182 */           break;
/*      */         case 4:
/* 2184 */           s = s + "There is too much matrix parameters too be  shown in this dialog, please check the matrix itself.";
/*      */         default:
/* 2186 */           break;
/*      */         }
/* 2188 */       else switch ($SWITCH_TABLE$metapiga$parameters$Parameters$EvaluationModel()[this.evaluationModel.ordinal()]) {
/*      */         case 18:
/*      */         case 19:
/* 2191 */           s = s + "rate matrix parameter is ";
/* 2192 */           s = s + RateParameter.K.verbose() + "(" + this.evaluationRateParameters.get(RateParameter.K) + ")";
/* 2193 */           s = s + ", ";
/* 2194 */           break;
/*      */         case 17:
/* 2196 */           s = s + "rate matrix parameters are ";
/* 2197 */           s = s + RateParameter.K1.verbose() + "(" + this.evaluationRateParameters.get(RateParameter.K1) + ") and ";
/* 2198 */           s = s + RateParameter.K2.verbose() + "(" + this.evaluationRateParameters.get(RateParameter.K2) + ")";
/* 2199 */           s = s + ", ";
/* 2200 */           break;
/*      */         case 16:
/* 2202 */           s = s + "rate matrix parameters are ";
/* 2203 */           s = s + RateParameter.A.verbose() + "(" + this.evaluationRateParameters.get(RateParameter.A) + "), ";
/* 2204 */           s = s + RateParameter.B.verbose() + "(" + this.evaluationRateParameters.get(RateParameter.B) + "), ";
/* 2205 */           s = s + RateParameter.C.verbose() + "(" + this.evaluationRateParameters.get(RateParameter.C) + "), ";
/* 2206 */           s = s + RateParameter.D.verbose() + "(" + this.evaluationRateParameters.get(RateParameter.D) + ") and ";
/* 2207 */           s = s + RateParameter.E.verbose() + "(" + this.evaluationRateParameters.get(RateParameter.E) + ")";
/* 2208 */           s = s + ", ";
/* 2209 */           break;
/*      */         case 2:
/* 2211 */           s = s + "rate matrix parameters are ";
/* 2212 */           RateParameter[] rp = RateParameter.getParametersOfModel(EvaluationModel.GTR20);
/* 2213 */           for (int r = 0; r < rp.length; r++) {
/* 2214 */             s = s + rp[r].verbose() + "(" + this.evaluationRateParameters.get(rp[r]) + ")";
/* 2215 */             if (r < rp.length - 2)
/* 2216 */               s = s + ", ";
/* 2217 */             else if (r == rp.length - 2) {
/* 2218 */               s = s + " and ";
/*      */             }
/*      */           }
/* 2221 */           s = s + ", ";
/* 2222 */           break;
/*      */         case 3:
/* 2224 */           s = s + "rate matrix parameters are ";
/* 2225 */           s = s + RateParameter.KAPPA.verbose() + "(" + this.evaluationRateParameters.get(RateParameter.KAPPA) + ") and ";
/* 2226 */           s = s + RateParameter.OMEGA.verbose() + "(" + this.evaluationRateParameters.get(RateParameter.OMEGA) + ")";
/* 2227 */           s = s + ", ";
/* 2228 */           break;
/*      */         case 4:
/* 2230 */           s = s + "There is too much matrix parameters too be  shown in this dialog, please check the matrix itself.";
/*      */         }
/*      */ 
/*      */ 
/* 2234 */       if (this.evaluationDistribution == EvaluationDistribution.GAMMA) {
/* 2235 */         if (this.specificDistributionShapes.containsKey(ch))
/* 2236 */           s = s + "shape of the gamma distribution is " + this.specificDistributionShapes.get(ch) + ", ";
/*      */         else {
/* 2238 */           s = s + "shape of the gamma distribution is " + this.evaluationDistributionShape + ", ";
/*      */         }
/*      */       }
/* 2241 */       if (this.specificsPInvs.containsKey(ch))
/* 2242 */         s = s + Tools.doubleToPercent(((Double)this.specificsPInvs.get(ch)).doubleValue(), 2) + " of invariant sites.\n";
/*      */       else {
/* 2244 */         s = s + Tools.doubleToPercent(this.evaluationPInv, 2) + " of invariant sites.\n";
/*      */       }
/*      */     }
/* 2247 */     if ((!specifics.isEmpty()) && (!specifics.containsAll(this.charsets.getPartitions())))
/*      */     {
/* 2249 */       Set general = this.charsets.getPartitions();
/* 2250 */       general.removeAll(specifics);
/* 2251 */       s = s + "Model parameters of other partition" + (general.size() > 1 ? "s" : "") + " " + general.toString() + ": ";
/* 2252 */     } else if (specifics.isEmpty()) {
/* 2253 */       s = s + "Model parameters: ";
/*      */     }
/*      */     int r;
/* 2255 */     if ((this.charsets.isPartitionsEmpty()) || (!specifics.containsAll(this.charsets.getPartitions()))) {
/* 2256 */       switch ($SWITCH_TABLE$metapiga$parameters$Parameters$EvaluationModel()[this.evaluationModel.ordinal()]) {
/*      */       case 18:
/*      */       case 19:
/* 2259 */         s = s + "rate matrix parameter is ";
/* 2260 */         s = s + RateParameter.K.verbose() + "(" + this.evaluationRateParameters.get(RateParameter.K) + ")";
/* 2261 */         s = s + ", ";
/* 2262 */         break;
/*      */       case 17:
/* 2264 */         s = s + "rate matrix parameters are ";
/* 2265 */         s = s + RateParameter.K1.verbose() + "(" + this.evaluationRateParameters.get(RateParameter.K1) + ") and ";
/* 2266 */         s = s + RateParameter.K2.verbose() + "(" + this.evaluationRateParameters.get(RateParameter.K2) + ")";
/* 2267 */         s = s + ", ";
/* 2268 */         break;
/*      */       case 16:
/* 2270 */         s = s + "rate matrix parameters are ";
/* 2271 */         s = s + RateParameter.A.verbose() + "(" + this.evaluationRateParameters.get(RateParameter.A) + "), ";
/* 2272 */         s = s + RateParameter.B.verbose() + "(" + this.evaluationRateParameters.get(RateParameter.B) + "), ";
/* 2273 */         s = s + RateParameter.C.verbose() + "(" + this.evaluationRateParameters.get(RateParameter.C) + "), ";
/* 2274 */         s = s + RateParameter.D.verbose() + "(" + this.evaluationRateParameters.get(RateParameter.D) + ") and ";
/* 2275 */         s = s + RateParameter.E.verbose() + "(" + this.evaluationRateParameters.get(RateParameter.E) + ")";
/* 2276 */         s = s + ", ";
/* 2277 */         break;
/*      */       case 2:
/* 2279 */         s = s + "rate matrix parameters are ";
/* 2280 */         RateParameter[] rp = RateParameter.getParametersOfModel(EvaluationModel.GTR20);
/* 2281 */         for (r = 0; r < rp.length; r++) {
/* 2282 */           s = s + rp[r].verbose() + "(" + this.evaluationRateParameters.get(rp[r]) + ")";
/* 2283 */           if (r < rp.length - 2)
/* 2284 */             s = s + ", ";
/* 2285 */           else if (r == rp.length - 2) {
/* 2286 */             s = s + " and ";
/*      */           }
/*      */         }
/* 2289 */         s = s + ", ";
/*      */       }
/*      */ 
/* 2292 */       if (this.evaluationDistribution == EvaluationDistribution.GAMMA) {
/* 2293 */         s = s + "shape of the gamma distribution is " + this.evaluationDistributionShape + ", ";
/*      */       }
/* 2295 */       s = s + Tools.doubleToPercent(this.evaluationPInv, 2) + " of invariant sites.\n";
/*      */     }
/* 2297 */     if ((this.evaluationRate == EvaluationRate.TREE) || (this.evaluationRate == EvaluationRate.BRANCH)) {
/* 2298 */       if (this.optimization == Optimization.NEVER) {
/* 2299 */         s = s + "Intra-step optimization will never be performed.";
/*      */       } else {
/* 2301 */         s = s + "Intra-step optimization will be performed ";
/* 2302 */         switch ($SWITCH_TABLE$metapiga$parameters$Parameters$Optimization()[this.optimization.ordinal()]) {
/*      */         case 2:
/* 2304 */           s = s + "only the consensus tree";
/* 2305 */           break;
/*      */         case 3:
/* 2307 */           s = s + "only at the end of (each) search";
/* 2308 */           break;
/*      */         case 4:
/* 2310 */           s = s + "on " + this.optimizationUse * 100.0D + "% of case";
/* 2311 */           break;
/*      */         case 5:
/* 2313 */           s = s + "every " + this.optimizationUse + " steps";
/*      */         }
/*      */ 
/* 2316 */         s = s + " using " + this.optimizationAlgorithm.verbose();
/* 2317 */         s = s + ", on ";
/* 2318 */         if (this.optimizationTargets.isEmpty()) {
/* 2319 */           s = s + "nothing ! (You should select at least a target)";
/*      */         }
/* 2321 */         for (OptimizationTarget target : this.optimizationTargets) {
/* 2322 */           s = s + target.verbose().toLowerCase() + ", ";
/*      */         }
/*      */       }
/*      */     }
/* 2326 */     s = s + "\n";
/* 2327 */     s = s + "Tree generation using " + this.startingTreeGeneration;
/* 2328 */     if (this.startingTreeGeneration == StartingTreeGeneration.LNJ) {
/* 2329 */       s = s + " (range " + Tools.doubleToPercent(this.startingTreeGenerationRange, 0) + ")";
/*      */     }
/* 2331 */     if (this.startingTreeGeneration != StartingTreeGeneration.RANDOM) {
/* 2332 */       s = s + " with " + this.startingTreeModel + " model";
/* 2333 */       s = s + " and " + this.startingTreeDistribution + " distribution";
/* 2334 */       if ((this.startingTreeDistribution == StartingTreeDistribution.GAMMA) || (this.startingTreeDistribution == StartingTreeDistribution.VDP)) {
/* 2335 */         s = s + " (with parameter of " + this.startingTreeDistributionShape + ")";
/*      */       }
/* 2337 */       s = s + " with " + Tools.doubleToPercent(this.startingTreePInv, 2) + " of invariant";
/* 2338 */       if (this.startingTreePInv > 0.0D) s = s + " (base composition " + this.startingTreePInvPi + ")";
/*      */     }
/* 2340 */     s = s + "\n";
/* 2341 */     s = s + "Operators that will be used on trees : ";
/* 2342 */     for (Operator op : this.operators) {
/* 2343 */       s = s + op.toString();
/* 2344 */       if (this.operatorsParameters.containsKey(op)) {
/* 2345 */         s = s + "(";
/* 2346 */         switch (((Integer)this.operatorsParameters.get(op)).intValue()) {
/*      */         case 0:
/* 2348 */           s = s + "ALL";
/* 2349 */           break;
/*      */         case 1:
/* 2351 */           if (op != Operator.RPM)
/* 2352 */             s = s + "RANDOM";
/*      */           else {
/* 2354 */             s = s + this.operatorsParameters.get(op);
/*      */           }
/* 2356 */           break;
/*      */         default:
/* 2358 */           s = s + this.operatorsParameters.get(op);
/*      */         }
/* 2360 */         s = s + ")";
/*      */       }
/* 2362 */       s = s + ", ";
/*      */     }
/* 2364 */     s = s + "\n";
/* 2365 */     s = s + "Operator selection " + this.operatorSelection;
/* 2366 */     if (this.operatorSelection == OperatorSelection.FREQLIST) {
/* 2367 */       s = s + " = (";
/* 2368 */       for (Operator op : this.operators) {
/* 2369 */         s = s + Tools.doubleToPercent(((Double)this.operatorsFrequencies.get(op)).doubleValue(), 2);
/* 2370 */         if (this.operatorIsDynamic.contains(op)) {
/* 2371 */           s = s + "(dyn)";
/*      */         }
/* 2373 */         s = s + " ";
/*      */       }
/* 2375 */       s = s + ")";
/* 2376 */       s = s + "\n";
/* 2377 */       s = s + "For operators dynamic frequencies use interval of " + this.dynamicInterval + 
/* 2378 */         " steps, with a minimum frequency of " + Tools.doubleToPercent(this.dynamicMin, 2) + "\n";
/*      */     }
/* 2380 */     s = s + "\n";
/* 2381 */     s = s + "Taxas selected as the outgroup : ";
/* 2382 */     if (this.outgroup.isEmpty())
/* 2383 */       s = s + "none !";
/*      */     else {
/* 2385 */       for (String st : this.outgroup) {
/* 2386 */         s = s + st + " ";
/*      */       }
/*      */     }
/* 2389 */     s = s + "\n";
/* 2390 */     s = s + "Taxas removed for the analysis : ";
/* 2391 */     if (this.deletedTaxa.isEmpty())
/* 2392 */       s = s + "none !";
/*      */     else {
/* 2394 */       for (String st : this.deletedTaxa) {
/* 2395 */         s = s + st + " ";
/*      */       }
/*      */     }
/* 2398 */     s = s + "\n";
/* 2399 */     s = s + "Defined charsets : ";
/*      */ 
/* 2401 */     if (this.charsets.isCharsetsEmpty()) {
/* 2402 */       s = s + "none !";
/*      */     }
/*      */     else {
/* 2405 */       for (Iterator iterator = this.charsets.getCharsetIterator(); iterator.hasNext(); ) {
/* 2406 */         Charset st = (Charset)iterator.next();
/* 2407 */         s = s + st.getLabel() + "={" + st.getAllRanges() + "} ";
/*      */       }
/*      */     }
/* 2410 */     s = s + "\n";
/* 2411 */     s = s + "Characters excluded for the analysis : ";
/*      */ 
/* 2413 */     if (this.charsets.isExcludedCharsetsEmpty()) {
/* 2414 */       s = s + "none !";
/*      */     }
/*      */     else {
/* 2417 */       for (Iterator iterator = this.charsets.getExcludedCharsetIterator(); iterator.hasNext(); ) {
/* 2418 */         Charset st = (Charset)iterator.next();
/* 2419 */         s = s + st + " ";
/*      */       }
/*      */     }
/* 2422 */     s = s + "\n";
/* 2423 */     s = s + "Partitionning analysis in different charsets : ";
/*      */     Object st;
/* 2425 */     if ((this.charsets.isPartitionsEmpty()) || (this.charsets.containsPartition("FULL SET"))) {
/* 2426 */       s = s + "no !";
/*      */     }
/*      */     else {
/* 2429 */       for (Iterator iterator = this.charsets.getPartitionIterator(); iterator.hasNext(); ) {
/* 2430 */         st = (Charset)iterator.next();
/* 2431 */         s = s + st + " ";
/*      */       }
/*      */     }
/* 2434 */     s = s + "\n";
/* 2435 */     if (this.columnRemoval == ColumnRemoval.NONE)
/* 2436 */       s = s + "Gaps will be interpreted as N\n";
/* 2437 */     else if (this.columnRemoval == ColumnRemoval.GAP)
/* 2438 */       s = s + "Column containing gaps will be removed\n";
/* 2439 */     else if (this.columnRemoval == ColumnRemoval.NGAP) {
/* 2440 */       s = s + "Column containing gaps or N will be removed\n";
/*      */     }
/* 2442 */     if ((this.sufficientStopConditions.isEmpty()) && (this.necessaryStopConditions.isEmpty())) {
/* 2443 */       if (this.heuristic != Heuristic.BS)
/* 2444 */         s = s + "Heuristic will not start, only starting tree will be computed\n";
/*      */     }
/*      */     else {
/* 2447 */       if (!this.sufficientStopConditions.isEmpty()) {
/* 2448 */         s = s + "Sufficient stopping conditions for heuristic: ";
/* 2449 */         if (this.sufficientStopConditions.contains(HeuristicStopCondition.STEPS)) {
/* 2450 */           s = s + "stop after " + this.stopCriterionSteps + " steps, ";
/*      */         }
/* 2452 */         if (this.sufficientStopConditions.contains(HeuristicStopCondition.TIME)) {
/* 2453 */           s = s + "stop after " + this.stopCriterionTime + " hours, ";
/*      */         }
/* 2455 */         if (this.sufficientStopConditions.contains(HeuristicStopCondition.AUTO)) {
/* 2456 */           s = s + "automatic stop after " + this.stopCriterionAutoSteps + " steps without significative improvement of likelihood (" + Tools.doubleToPercent(this.stopCriterionAutoThreshold, 4) + "), ";
/*      */         }
/* 2458 */         if (this.sufficientStopConditions.contains(HeuristicStopCondition.CONSENSUS)) {
/* 2459 */           s = s + "automatic stop when mean relative error of " + this.stopCriterionConsensusInterval + " consecutive consensus trees stay below " + Tools.doubleToPercent(this.stopCriterionConsensusMRE, 0) + " using trees sampled every " + this.stopCriterionConsensusGeneration + " generations, ";
/*      */         }
/* 2461 */         s = s + "\n";
/*      */       }
/* 2463 */       if (!this.necessaryStopConditions.isEmpty()) {
/* 2464 */         s = s + "Necessary stopping conditions for heuristic: ";
/* 2465 */         if (this.necessaryStopConditions.contains(HeuristicStopCondition.STEPS)) {
/* 2466 */           s = s + this.stopCriterionSteps + " steps, ";
/*      */         }
/* 2468 */         if (this.necessaryStopConditions.contains(HeuristicStopCondition.TIME)) {
/* 2469 */           s = s + this.stopCriterionTime + " hours, ";
/*      */         }
/* 2471 */         if (this.necessaryStopConditions.contains(HeuristicStopCondition.AUTO)) {
/* 2472 */           s = s + this.stopCriterionAutoSteps + " steps without significative improvement of likelihood (" + Tools.doubleToPercent(this.stopCriterionAutoThreshold, 15) + "), ";
/*      */         }
/* 2474 */         if (this.necessaryStopConditions.contains(HeuristicStopCondition.CONSENSUS)) {
/* 2475 */           s = s + "mean relative error of " + this.stopCriterionConsensusInterval + " consecutive consensus trees stay below " + Tools.doubleToPercent(this.stopCriterionConsensusMRE, 0) + " using trees sampled every " + this.stopCriterionConsensusGeneration + " generations, ";
/*      */         }
/* 2477 */         s = s + "\n";
/*      */       }
/*      */     }
/* 2480 */     switch ($SWITCH_TABLE$metapiga$parameters$Parameters$ReplicatesStopCondition()[this.replicatesStopCondition.ordinal()]) {
/*      */     case 1:
/* 2482 */       if (this.replicatesNumber > 1) {
/* 2483 */         s = s + "Make " + this.replicatesNumber + " replicates and compute majority-rule consensus tree" + "\n";
/* 2484 */         if (this.replicatesParallel > 1)
/* 2485 */           s = s + this.replicatesParallel + " replicates will run in parallel\n";
/*      */       }
/* 2487 */       break;
/*      */     case 2:
/* 2489 */       s = s + "Make between " + this.replicatesMinimum + " and " + this.replicatesMaximum + " replicates, stopping when mean relative error between at least " + this.replicatesInterval + " consecutive trees stay below " + Tools.doubleToPercent(this.replicatesMRE, 0) + ".\n";
/* 2490 */       if (this.replicatesParallel > 1)
/* 2491 */         s = s + this.replicatesParallel + " replicates will run in parallel\n";
/*      */       break;
/*      */     }
/* 2494 */     if (this.useGrid) {
/* 2495 */       s = s + "MetaPIGA will run on the following GRID:\n";
/* 2496 */       s = s + "Server address: " + this.gridServer + "\n";
/* 2497 */       s = s + "Client id: " + this.gridClient + "\n";
/* 2498 */       s = s + "MetaPIGA module id: " + this.gridModule + "\n";
/* 2499 */       s = s + "log files are ignored on GRID.\n";
/*      */     } else {
/* 2501 */       s = s + "log files : ";
/* 2502 */       if (this.logFiles.isEmpty())
/* 2503 */         s = s + "none !";
/*      */       else {
/* 2505 */         for (st = this.logFiles.iterator(); ((Iterator)st).hasNext(); ) { LogFile st = (LogFile)((Iterator)st).next();
/* 2506 */           s = s + st.verbose() + " ";
/*      */         }
/*      */       }
/* 2509 */       s = s + "\n";
/*      */     }
/* 2511 */     if (this.gridReplicate) s = s + "This is a single replicate sent through the GRID that will be run on this machine. Output file wille be named '" + this.cloudOutput + "'.\n";
/* 2512 */     return s;
/*      */   }
/*      */ 
/*      */   public Parameters duplicateButShareDataset()
/*      */     throws UnknownDataException, NexusInconsistencyException, CharsetIntersectionException, IncompatibleDataException
/*      */   {
/* 2525 */     Parameters P = new Parameters(this.label);
/* 2526 */     P.setParameters(this.charactersBlock);
/* 2527 */     applyParametersTo(P, false);
/* 2528 */     return P;
/*      */   }
/*      */ 
/*      */   public Parameters duplicate()
/*      */     throws UnknownDataException, NexusInconsistencyException, CharsetIntersectionException, IncompatibleDataException
/*      */   {
/* 2539 */     Parameters P = new Parameters(this.label);
/* 2540 */     P.setParameters(this.charactersBlock);
/* 2541 */     applyParametersTo(P);
/* 2542 */     return P;
/*      */   }
/*      */ 
/*      */   public void applyParametersTo(Parameters P)
/*      */     throws UnknownDataException, NexusInconsistencyException, CharsetIntersectionException, IncompatibleDataException
/*      */   {
/* 2553 */     applyParametersTo(P, true);
/*      */   }
/*      */ 
/*      */   public void translateToCodonsInRange(int startPos, int endPos, CodonTransitionTableType tabType)
/*      */   {
/* 2567 */     endPos = calibrateLastDomainPosition(startPos, endPos);
/* 2568 */     this.charsets.translateToCodons(startPos, endPos);
/* 2569 */     this.codonDomain = new CodonDomainDefinition(startPos, endPos);
/* 2570 */     defineTransitionCodonTable(tabType);
/*      */   }
/*      */ 
/*      */   public void setCodonsInRange(int startPos, int endPos, CodonTransitionTableType tableType)
/*      */   {
/* 2583 */     this.codonDomain = new CodonDomainDefinition(startPos, endPos);
/* 2584 */     defineTransitionCodonTable(tableType);
/*      */   }
/*      */ 
/*      */   public void revertCodons() {
/* 2588 */     this.charsets.translateCodonToNucleotideCharsets();
/* 2589 */     this.codonDomain = null;
/* 2590 */     this.codonCharactersBlock = null;
/* 2591 */     this.evaluationModel = EvaluationModel.JC;
/* 2592 */     if (this.operators.contains(Operator.RPM))
/* 2593 */       this.operators.remove(Operator.RPM);
/*      */   }
/*      */ 
/*      */   public boolean areCodons()
/*      */   {
/* 2598 */     return this.codonDomain != null;
/*      */   }
/*      */ 
/*      */   public CodonCharactersBlock getCodonCharactersBlock() {
/* 2602 */     return this.codonCharactersBlock;
/*      */   }
/*      */ 
/*      */   public void applyParametersTo(Parameters P, boolean dupDataset)
/*      */     throws UnknownDataException, NexusInconsistencyException, CharsetIntersectionException, IncompatibleDataException
/*      */   {
/* 2614 */     P.heuristic = this.heuristic;
/* 2615 */     P.hcRestart = this.hcRestart;
/* 2616 */     P.saSchedule = this.saSchedule;
/* 2617 */     P.saScheduleParam = this.saScheduleParam;
/* 2618 */     P.saLundyC = this.saLundyC;
/* 2619 */     P.saLundyAlpha = this.saLundyAlpha;
/* 2620 */     P.saInitAccept = this.saInitAccept;
/* 2621 */     P.saFinalAccept = this.saFinalAccept;
/* 2622 */     P.saDeltaL = this.saDeltaL;
/* 2623 */     P.saDeltaLPercent = this.saDeltaLPercent;
/* 2624 */     P.saReheatingType = this.saReheatingType;
/* 2625 */     P.saReheatingValue = this.saReheatingValue;
/* 2626 */     P.saCoolingType = this.saCoolingType;
/* 2627 */     P.saCoolingSteps = this.saCoolingSteps;
/* 2628 */     P.saCoolingSuccesses = this.saCoolingSuccesses;
/* 2629 */     P.saCoolingFailures = this.saCoolingFailures;
/* 2630 */     P.gaIndNum = this.gaIndNum;
/* 2631 */     P.gaSelection = this.gaSelection;
/* 2632 */     P.gaReplacementStrength = this.gaReplacementStrength;
/* 2633 */     P.gaRecombination = this.gaRecombination;
/* 2634 */     P.gaOperatorChange = this.gaOperatorChange;
/* 2635 */     P.cpConsensus = this.cpConsensus;
/* 2636 */     P.cpOperator = this.cpOperator;
/* 2637 */     P.cpPopNum = this.cpPopNum;
/* 2638 */     P.cpIndNum = this.cpIndNum;
/* 2639 */     P.cpTolerance = this.cpTolerance;
/* 2640 */     P.cpHybridization = this.cpHybridization;
/* 2641 */     P.cpSelection = this.cpSelection;
/* 2642 */     P.cpReplacementStrength = this.cpReplacementStrength;
/* 2643 */     P.cpRecombination = this.cpRecombination;
/* 2644 */     P.cpOperatorChange = this.cpOperatorChange;
/* 2645 */     P.cpCoreNum = this.cpCoreNum;
/* 2646 */     P.evaluationRate = this.evaluationRate;
/* 2647 */     P.evaluationModel = this.evaluationModel;
/* 2648 */     P.evaluationRateParameters = new HashMap(this.evaluationRateParameters);
/* 2649 */     P.evaluationStateFrequencies = this.evaluationStateFrequencies;
/* 2650 */     P.evaluationDistribution = this.evaluationDistribution;
/* 2651 */     P.evaluationDistributionSubsets = this.evaluationDistributionSubsets;
/* 2652 */     P.evaluationDistributionShape = this.evaluationDistributionShape;
/* 2653 */     P.evaluationPInv = this.evaluationPInv;
/* 2654 */     P.startingTreeGeneration = this.startingTreeGeneration;
/* 2655 */     P.startingTreeGenerationRange = this.startingTreeGenerationRange;
/* 2656 */     P.startingTreeModel = this.startingTreeModel;
/* 2657 */     P.startingTreeDistribution = this.startingTreeDistribution;
/* 2658 */     P.startingTreeDistributionShape = this.startingTreeDistributionShape;
/* 2659 */     P.startingTreePInv = this.startingTreePInv;
/* 2660 */     P.startingTreePInvPi = this.startingTreePInvPi;
/* 2661 */     P.optimization = this.optimization;
/* 2662 */     P.optimizationUse = this.optimizationUse;
/* 2663 */     P.optimizationAlgorithm = this.optimizationAlgorithm;
/* 2664 */     P.optimizationTargets = new HashSet(this.optimizationTargets);
/* 2665 */     P.operators = new ArrayList(this.operators);
/* 2666 */     P.operatorsParameters = new HashMap(this.operatorsParameters);
/* 2667 */     P.operatorsFrequencies = new HashMap(this.operatorsFrequencies);
/* 2668 */     P.operatorIsDynamic = new HashSet(this.operatorIsDynamic);
/* 2669 */     P.operatorSelection = this.operatorSelection;
/* 2670 */     P.dynamicInterval = this.dynamicInterval;
/* 2671 */     P.dynamicMin = this.dynamicMin;
/* 2672 */     P.columnRemoval = this.columnRemoval;
/* 2673 */     P.outputDir = this.outputDir;
/* 2674 */     P.sufficientStopConditions = new HashSet(this.sufficientStopConditions);
/* 2675 */     P.necessaryStopConditions = new HashSet(this.necessaryStopConditions);
/* 2676 */     P.stopCriterionSteps = this.stopCriterionSteps;
/* 2677 */     P.stopCriterionTime = this.stopCriterionTime;
/* 2678 */     P.stopCriterionAutoSteps = this.stopCriterionAutoSteps;
/* 2679 */     P.stopCriterionAutoThreshold = this.stopCriterionAutoThreshold;
/* 2680 */     P.stopCriterionConsensusMRE = this.stopCriterionConsensusMRE;
/* 2681 */     P.stopCriterionConsensusGeneration = this.stopCriterionConsensusGeneration;
/* 2682 */     P.stopCriterionConsensusInterval = this.stopCriterionConsensusInterval;
/* 2683 */     P.replicatesStopCondition = this.replicatesStopCondition;
/* 2684 */     P.replicatesMRE = this.replicatesMRE;
/* 2685 */     P.replicatesNumber = this.replicatesNumber;
/* 2686 */     P.replicatesMinimum = this.replicatesMinimum;
/* 2687 */     P.replicatesMaximum = this.replicatesMaximum;
/* 2688 */     P.replicatesInterval = this.replicatesInterval;
/* 2689 */     P.replicatesParallel = this.replicatesParallel;
/* 2690 */     P.logFiles = new HashSet(this.logFiles);
/* 2691 */     P.gridReplicate = this.gridReplicate;
/* 2692 */     P.cloudOutput = this.cloudOutput;
/* 2693 */     P.useGrid = this.useGrid;
/* 2694 */     P.gridServer = this.gridServer;
/* 2695 */     P.gridClient = this.gridClient;
/* 2696 */     P.gridModule = this.gridModule;
/* 2697 */     if (this.charactersBlock == P.charactersBlock) {
/* 2698 */       P.outgroup = new HashSet(this.outgroup);
/* 2699 */       P.deletedTaxa = new HashSet(this.deletedTaxa);
/*      */ 
/* 2701 */       P.charsets.addCharsets(this.charsets.getCharsets());
/*      */ 
/* 2703 */       P.charsets.replaceExcludedCharsets(this.charsets.getExcludedCharsets());
/*      */ 
/* 2705 */       P.charsets.replacePartitionCharsets(this.charsets.getPartitions());
/* 2706 */       for (Charset key : this.specificRateParameters.keySet()) {
/* 2707 */         Map map = new TreeMap();
/* 2708 */         map.putAll((Map)this.specificRateParameters.get(key));
/* 2709 */         P.specificRateParameters.put(key, map);
/*      */       }
/* 2711 */       P.specificDistributionShapes = new HashMap(this.specificDistributionShapes);
/* 2712 */       P.specificsPInvs = new HashMap(this.specificsPInvs);
/* 2713 */       if (dupDataset) {
/* 2714 */         P.assignPartitionColors();
/* 2715 */         P.buildDataset();
/*      */       } else {
/* 2717 */         P.dataset = this.dataset;
/*      */       }
/* 2719 */       P.loadedTrees = new HashMap(this.loadedTrees);
/* 2720 */       P.loadedTreesTranslation = new HashMap(this.loadedTreesTranslation);
/* 2721 */       P.userSelectionTree = new ArrayList(this.userSelectionTree);
/* 2722 */       if (P.startingTreeGeneration == StartingTreeGeneration.GIVEN)
/* 2723 */         P.setStartingTrees();
/*      */     }
/*      */   }
/*      */ 
/*      */   public boolean isDatasetModified()
/*      */   {
/* 2733 */     return (!this.deletedTaxa.isEmpty()) || (!this.charsets.isExcludedCharsetsEmpty());
/*      */   }
/*      */ 
/*      */   public Optimizer getOptimizer(Tree tree)
/*      */     throws BranchNotFoundException
/*      */   {
/*      */     Optimizer optimizer;
/*      */     Optimizer optimizer;
/*      */     Optimizer optimizer;
/* 2738 */     switch ($SWITCH_TABLE$metapiga$parameters$Parameters$OptimizationAlgorithm()[this.optimizationAlgorithm.ordinal()]) {
/*      */     case 3:
/* 2740 */       optimizer = new DFO();
/* 2741 */       break;
/*      */     case 2:
/* 2743 */       optimizer = new Powell(tree, tree.getBranches(), this.optimizationTargets);
/* 2744 */       break;
/*      */     case 1:
/*      */     default:
/* 2747 */       optimizer = new GA(tree, this.optimizationTargets);
/*      */     }
/*      */ 
/* 2750 */     return optimizer;
/*      */   }
/*      */ 
/*      */   private void initializeGraphicCard() {
/* 2754 */     if (this.hasGPU) {
/* 2755 */       JCudaDriver.setExceptionsEnabled(true);
/*      */ 
/* 2758 */       JCudaDriver.cuInit(0);
/* 2759 */       this.device = new CUdevice();
/* 2760 */       JCudaDriver.cuDeviceGet(this.device, 0);
/* 2761 */       preparePtxFile();
/*      */ 
/* 2763 */       JCudaDriver.cuDeviceGetProperties(this.gpuDevProperties, this.device);
/*      */     } else {
/* 2765 */       JOptionPane.showMessageDialog(null, Tools.getErrorPanel("Error", new NoGPUException()));
/* 2766 */       this.likelihoodCalculationType = LikelihoodCalculationType.CLASSIC;
/*      */     }
/*      */   }
/*      */ 
/*      */   private void preparePtxFile()
/*      */   {
/* 2774 */     String kernelFilePath = "/native_lib/likelihoodKernel.ptx";
/* 2775 */     String kernelFolderPath = System.getProperty("user.dir");
/*      */ 
/* 2777 */     this.ptxFilePath = "";
/*      */     try {
/* 2779 */       this.ptxFilePath = CudaTools.preparePtxFile(kernelFolderPath + kernelFilePath);
/*      */     } catch (IOException e) {
/* 2781 */       e.printStackTrace();
/*      */     }
/*      */   }
/*      */ 
/*      */   public CodonTransitionTable getCodonTransitionTable() {
/* 2786 */     return this.codonTransitionTable;
/*      */   }
/*      */ 
/*      */   public CodonTransitionTableType getCodonTaransitionTableType() {
/* 2790 */     CodonTransitionTable tab = getCodonTransitionTable();
/* 2791 */     if ((tab instanceof UniversalCodonTransitionTable))
/* 2792 */       return CodonTransitionTableType.UNIVERSAL;
/* 2793 */     if ((tab instanceof VertebrateMitochondrialCode))
/* 2794 */       return CodonTransitionTableType.VERTMITOCH;
/* 2795 */     if ((tab instanceof MoldProtoCoelMitochCode))
/* 2796 */       return CodonTransitionTableType.MPCMMITOCH;
/* 2797 */     if ((tab instanceof InvertebrateMitochondrialCode))
/* 2798 */       return CodonTransitionTableType.INVERTMITOCH;
/* 2799 */     if ((tab instanceof CDHNuclearCode))
/* 2800 */       return CodonTransitionTableType.CDHNNUC;
/* 2801 */     if ((tab instanceof EchinodermFlatwormMitochCode))
/* 2802 */       return CodonTransitionTableType.EFMITOCH;
/* 2803 */     if ((tab instanceof EuploidNuclearCode)) {
/* 2804 */       return CodonTransitionTableType.EUPLOTIDNUC;
/*      */     }
/* 2806 */     if (!$assertionsDisabled) throw new AssertionError("Unknown codon transition table");
/* 2807 */     return CodonTransitionTableType.NONE;
/*      */   }
/*      */ 
/*      */   private void setCodonTransitionTable(CodonTransitionTable codonTransitionTable)
/*      */   {
/* 2812 */     this.codonTransitionTable = codonTransitionTable;
/*      */   }
/*      */ 
/*      */   public CodonTransitionTableType getCurrentCodonTable() {
/* 2816 */     return this.currentDNAtable;
/*      */   }
/*      */ 
/*      */   public static enum CPConsensus
/*      */   {
/*  143 */     STRICT, STOCHASTIC; } 
/*  144 */   public static enum CPOperator { BLIND, SUPERVISED; } 
/*      */   public static enum CPOperatorChange {
/*  146 */     STEP, POP, IND;
/*      */   }
/*      */ 
/*      */   public static enum CPSelection
/*      */   {
/*  145 */     RANK, TOURNAMENT, REPLACEMENT, IMPROVE, KEEPBEST;
/*      */   }
/*      */ 
/*      */   public class CodonDomainDefinition
/*      */   {
/*      */     private final int startCodonDomainPosition;
/*      */     private final int endCodonDomainPosition;
/*      */     private final int domainSize;
/*      */     private final int domainSizeInCodons;
/*      */ 
/*      */     public CodonDomainDefinition(int startPosition, int endPosition)
/*      */     {
/*  358 */       if ((startPosition + 2 > endPosition) || (startPosition < 1) || 
/*  359 */         (endPosition > Parameters.this.charactersBlock.getDimensionsNChar())) {
/*  360 */         throw new IndexOutOfBoundsException(
/*  361 */           "Codon domain definitions out of bounds");
/*      */       }
/*  363 */       this.startCodonDomainPosition = startPosition;
/*  364 */       this.endCodonDomainPosition = Parameters.this.calibrateLastDomainPosition(startPosition, 
/*  365 */         endPosition);
/*  366 */       this.domainSize = Parameters.this.domainSize(startPosition, endPosition);
/*  367 */       this.domainSizeInCodons = (Parameters.this.domainSize(startPosition, endPosition) / 3);
/*      */     }
/*      */ 
/*      */     public int getStartCodonDomainPosition()
/*      */     {
/*  372 */       return this.startCodonDomainPosition;
/*      */     }
/*      */ 
/*      */     public int getEndCodonDomainPosition() {
/*  376 */       return this.endCodonDomainPosition;
/*      */     }
/*      */ 
/*      */     public int getDomainSize() {
/*  380 */       return this.domainSize;
/*      */     }
/*      */ 
/*      */     public int getDimensionsNChar() {
/*  384 */       return this.domainSizeInCodons;
/*      */     }
/*      */ 
/*      */     public boolean isPositionInCodonDomain(int pos) {
/*  388 */       return (pos >= this.startCodonDomainPosition) && (pos <= this.endCodonDomainPosition);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static enum CodonTransitionTableType
/*      */   {
/*  290 */     UNIVERSAL("Universal Codon Table"), 
/*  291 */     CDHNNUC("The Ciliate, Dasycladacean and Hexamita Nuclear Code"), 
/*  292 */     EFMITOCH("The Echinoderm and Flatworm Mitochondrial Code"), 
/*  293 */     EUPLOTIDNUC("The Euplotid Nuclear Code"), 
/*  294 */     INVERTMITOCH("The Invertebrate Mitochondrial Code"), 
/*  295 */     MPCMMITOCH("The Mold, Protozoan, Coelenterate Mitoch. & Myco/Spiroplasma Code"), 
/*      */ 
/*  297 */     VERTMITOCH("The Vertebrate Mitochondrial Code"), 
/*  298 */     NONE("None");
/*      */ 
/*      */     private final String name;
/*      */ 
/*  300 */     private CodonTransitionTableType(String name) { this.name = name; } 
/*  301 */     public String verbose() { return this.name; }
/*      */ 
/*      */   }
/*      */ 
/*      */   public static enum ColumnRemoval
/*      */   {
/*  263 */     NONE, GAP, NGAP;
/*      */   }
/*      */ 
/*      */   public static enum DistanceModel
/*      */   {
/*  226 */     GTR2("General-Time-Reversible model for standard binary data", DataType.STANDARD), 
/*  227 */     GTR20("General-Time-Reversible model for proteins", DataType.PROTEIN), 
/*  228 */     POISSON("Poisson model, (Bishop & Friday, in Molecules and morphology in evolution: conflict or compromise?, Cambridge University Press, Cambridge (1987))", DataType.PROTEIN), 
/*  229 */     GTR("General-Time-Reversible model for nucleotides", DataType.DNA), 
/*  230 */     TN93("Tamura-Nei 1993 model, (Tamura & Nei, Mol. Biol. Evol. 10:512-526 (1993))", DataType.DNA), 
/*  231 */     HKY85("Hasegewa-Kishino-Yano 1985 model, (Hasegewa, Kishino & Yano, J. Mol. Evol. 22:160-174 (1985))", DataType.DNA), 
/*  232 */     K2P("Kimura's 2 Parameter model, (Kimura, J. Mol. Evol. 16:111-120 (1980))", DataType.DNA), 
/*  233 */     JC("Jukes Cantor 1969 model, (Jukes & Cantor, in Mammalian protein metabolism, vol. III, Academic Press, New York (1969))", DataType.DNA), 
/*  234 */     UNCORRECTED("Uncorrected distances (no substitution model)", null), 
/*  235 */     ABSOLUTE("Absolute number of differences (no substitution model)", null), 
/*  236 */     GY("Goldamn-Yang model for codons", DataType.CODON), 
/*  237 */     GTR64("General-Time-Reversible model for codons", DataType.CODON), 
/*  238 */     NONE("Distances are not used", null);
/*      */ 
/*      */     private final String name;
/*      */     private final DataType dataType;
/*      */ 
/*  241 */     private DistanceModel(String name, DataType dataType) { this.name = name; this.dataType = dataType; } 
/*  242 */     public String verbose() { return this.name; } 
/*  243 */     public DataType getDataType() { return this.dataType; }
/*      */ 
/*      */   }
/*      */ 
/*      */   public static enum EvaluationDistribution
/*      */   {
/*  199 */     NONE, GAMMA, VDP;
/*      */   }
/*      */ 
/*      */   public static enum EvaluationModel
/*      */   {
/*  149 */     GTR2("General-Time-Reversible model for standard binary data", 0, false, false, DataType.STANDARD), 
/*  150 */     GTR20("General-Time-Reversible model for proteins", 189, false, false, DataType.PROTEIN), 
/*  151 */     GY("GY model for codon substitution (Goldman, Nick, and Ziheng Yang. Molecular biology and evolution 11.5 (1994): 725-736)", 
/*  153 */       2, false, false, DataType.CODON), 
/*  154 */     GTR64("General-Time-Reversible model for codons", 2015, false, false, DataType.CODON), 
/*  155 */     ECM("Empirical Codon Model, (Kosiol, Holmes & Goldman, Mol. Biol. Evol. 24(7):1464-1479 (2007)", 2015, false, true, DataType.CODON), 
/*  156 */     WAG("Wheland and Goldman model, (Wheland and Goldman, Mol. Biol. Evol. 18:691-699 (2001))", 189, false, true, DataType.PROTEIN), 
/*  157 */     JTT("Jones-Taylor-Thornton model, (Jones, Taylor & Thornton, Comput. Appl. Biosci. 8:275-282 (1992))", 189, false, true, DataType.PROTEIN), 
/*  158 */     DAYHOFF("Dayhoff model, (Dayhoff, Schwartz and Orcutt, in Atlas of protein sequence and structure. Vol. V, Suppl. 3, National Biomedical Research Foundation, Washington, D.C. (1978))", 189, false, true, DataType.PROTEIN), 
/*  159 */     VT("Variable Time substitution matrix, (Muller and Vingron, J. Comp. Biol. 7:761-776 (2000))", 189, false, true, DataType.PROTEIN), 
/*  160 */     BLOSUM62("BLOSUM62 (BLOcks of amino acid SUbstitution Matrix) substitution matrix, (Henikoff and Henikoff, Proc. Natl. Acad. Sci., U.S.A. 89:10915-10919 (1992))", 189, false, true, DataType.PROTEIN), 
/*  161 */     CPREV("General Reversible Chloroplast model, (Adachi, Waddell, Martin & Hasegawa, J. Mol. Evol. 50:348-358 (2000))", 189, false, true, DataType.PROTEIN), 
/*  162 */     MTREV("General Reversible Mitochondrial model, (Adachi and Hasegawa, Computer Science Monographs of Institute of Statistical Mathematics 28:1-150 (1996))", 189, false, true, DataType.PROTEIN), 
/*  163 */     RTREV("General Reverse Transcriptase model, (Dimmic, Rest, Mindell & Goldstein, J. Mol. Evol. 55:65-73 (2002))", 189, false, true, DataType.PROTEIN), 
/*  164 */     MTMAM("Mammal Mitochondrial model, (Yang, Nielsen & Hasegawa, Mol. Biol. Evol. 15(12):1600-11 (1998))", 189, false, true, DataType.PROTEIN), 
/*  165 */     POISSON("Poisson model, (Bishop & Friday, in Molecules and morphology in evolution: conflict or compromise?, Cambridge University Press, Cambridge (1987))", 0, true, false, DataType.PROTEIN), 
/*  166 */     GTR("General-Time-Reversible model for nucleotides", 5, false, false, DataType.DNA), 
/*  167 */     TN93("Tamura-Nei 1993 model, (Tamura & Nei, Mol. Biol. Evol. 10:512-526 (1993))", 2, false, false, DataType.DNA), 
/*  168 */     HKY85("Hasegewa-Kishino-Yano 1985 model, (Hasegewa, Kishino & Yano, J. Mol. Evol. 22:160-174 (1985))", 1, false, false, DataType.DNA), 
/*  169 */     K2P("Kimura's 2 Parameter model, (Kimura, J. Mol. Evol. 16:111-120 (1980))", 1, true, false, DataType.DNA), 
/*  170 */     JC("Jukes Cantor 1969 model, (Jukes & Cantor, in Mammalian protein metabolism, vol. III, Academic Press, New York (1969))", 0, true, false, DataType.DNA);
/*      */ 
/*      */     private final String name;
/*      */     private final int rateParameters;
/*      */     private final boolean hasEqualBaseFrequencies;
/*      */     private final boolean isEmpirical;
/*      */     private final DataType dataType;
/*      */ 
/*  177 */     private EvaluationModel(String name, int rateParam, boolean hasEqBF, boolean isEmp, DataType dataType) { this.name = name;
/*  178 */       this.rateParameters = rateParam;
/*  179 */       this.hasEqualBaseFrequencies = hasEqBF;
/*  180 */       this.isEmpirical = isEmp;
/*  181 */       this.dataType = dataType; } 
/*      */     public int getNumRateParameters() {
/*  183 */       return this.rateParameters; } 
/*  184 */     public boolean hasEqualBaseFrequencies() { return this.hasEqualBaseFrequencies; } 
/*  185 */     public boolean isEmpirical() { return this.isEmpirical; } 
/*  186 */     public DataType getDataType() { return this.dataType; } 
/*      */     public static EvaluationModel[] availableModels(DataType dataType) {
/*  188 */       Set set = new TreeSet();
/*  189 */       for (EvaluationModel p : values()) {
/*  190 */         if (p.dataType == dataType) set.add(p);
/*      */       }
/*  192 */       if (set.contains(GTR64)) {
/*  193 */         set.remove(GTR64);
/*      */       }
/*  195 */       return (EvaluationModel[])set.toArray(new EvaluationModel[0]);
/*      */     }
/*  197 */     public String verbose() { return this.name; }
/*      */ 
/*      */   }
/*      */ 
/*      */   public static enum EvaluationRate
/*      */   {
/*  147 */     BRANCH, TREE;
/*      */   }
/*      */ 
/*      */   public static enum EvaluationStateFrequencies
/*      */   {
/*  198 */     EMPIRICAL, ESTIMATED;
/*      */   }
/*      */ 
/*      */   public static enum FileFormat
/*      */   {
/*  109 */     NEXUS, FASTA;
/*      */   }
/*      */ 
/*      */   public static enum GAOperatorChange
/*      */   {
/*  142 */     STEP, IND;
/*      */   }
/*      */ 
/*      */   public static enum GASelection
/*      */   {
/*  141 */     RANK, TOURNAMENT, REPLACEMENT, IMPROVE, KEEPBEST;
/*      */   }
/*      */ 
/*      */   private class GPUchecker
/*      */     implements Runnable
/*      */   {
/*      */     private GPUchecker()
/*      */     {
/*      */     }
/*      */ 
/*      */     public void run()
/*      */     {
/*  573 */       int[] devCount = new int[1];
/*  574 */       JCuda.cudaGetDeviceCount(devCount);
/*  575 */       if (devCount[0] > 0)
/*  576 */         Parameters.this.hasGPU = true;
/*      */     }
/*      */   }
/*      */ 
/*      */   public static enum Heuristic
/*      */   {
/*  111 */     HC("Hill Climbing"), 
/*  112 */     SA("Simulated Annealing"), 
/*  113 */     GA("Genetic Algorithm"), 
/*  114 */     CP("Consensus Pruning (metaGA)"), 
/*  115 */     BS("Bootstrapping");
/*      */ 
/*      */     private final String name;
/*      */ 
/*  117 */     private Heuristic(String name) { this.name = name; } 
/*  118 */     public String verbose() { return this.name; }
/*      */ 
/*      */   }
/*      */ 
/*      */   public static enum HeuristicStopCondition
/*      */   {
/*  265 */     STEPS, TIME, AUTO, CONSENSUS;
/*      */   }
/*      */ 
/*      */   public static enum LikelihoodCalculationType
/*      */   {
/*  282 */     CLASSIC("Classic likelihood calculation with loops"), 
/*  283 */     GPU("Likelihood calculation using GPU");
/*      */ 
/*      */     private final String name;
/*      */ 
/*  285 */     private LikelihoodCalculationType(String name) { this.name = name; } 
/*  286 */     public String verbose() { return this.name; }
/*      */ 
/*      */   }
/*      */ 
/*      */   public static enum LogFile
/*      */   {
/*  267 */     DATA("Working matrix log file"), 
/*  268 */     DIST("Distance matrix log file"), 
/*  269 */     TREESTART("Starting Tree log file"), 
/*  270 */     HEUR("Heuristic search log file"), 
/*  271 */     TREEHEUR("Heuristic search tree file"), 
/*  272 */     CONSENSUS("Consensus log file"), 
/*  273 */     OPDETAILS("Operators log file"), 
/*  274 */     OPSTATS("Operator statistics file"), 
/*  275 */     ANCSEQ("Ancestral sequences log file"), 
/*  276 */     PERF("Perfomances log file");
/*      */ 
/*      */     private final String name;
/*      */ 
/*  278 */     private LogFile(String name) { this.name = name; } 
/*  279 */     public String verbose() { return this.name; }
/*      */ 
/*      */   }
/*      */ 
/*      */   public static enum Operator
/*      */   {
/*  248 */     NNI("Nearest Neighbor Interchange"), 
/*  249 */     SPR("Subtree Pruning and Regrafting"), 
/*  250 */     TBR("Tree Bisection Reconnection"), 
/*  251 */     TXS("TaXa Swap"), 
/*  252 */     STS("SubTree Swap"), 
/*  253 */     BLM("Branch Length Mutation"), 
/*  254 */     BLMINT("Branch Length Mutation on INTernal branches only"), 
/*  255 */     RPM("Rate Parameters Mutation"), 
/*  256 */     GDM("Gamma Distribution Mutation"), 
/*  257 */     PIM("Proportion of Invariant Mutation"), 
/*  258 */     APRM("Among-Partition Rate Mutation");
/*      */ 
/*      */     private final String name;
/*      */ 
/*  260 */     private Operator(String name) { this.name = name; } 
/*  261 */     public String verbose() { return this.name; }  } 
/*  262 */   public static enum OperatorSelection { RANDOM, ORDERED, FREQLIST; }
/*      */ 
/*      */ 
/*      */   public static enum Optimization
/*      */   {
/*  200 */     NEVER, CONSENSUSTREE, ENDSEARCH, STOCH, DISC;
/*      */   }
/*  202 */   public static enum OptimizationAlgorithm { GA("Genetic Algorithm"), 
/*  203 */     POWELL("Powell's method"), 
/*  204 */     DFO("Derivative-Free Optimization");
/*      */ 
/*      */     private final String name;
/*      */ 
/*  206 */     private OptimizationAlgorithm(String name) { this.name = name; } 
/*  207 */     public String verbose() { return this.name; }  } 
/*      */   public static enum OptimizationTarget {
/*  209 */     BL("Branch lengths"), 
/*  210 */     R("Rate matrix parameters"), 
/*  211 */     GAMMA("Shape of Gamma distribution"), 
/*  212 */     PINV("Proportion of invariable sites"), 
/*  213 */     APRATE("Among-partition rate variation");
/*      */ 
/*      */     private final String name;
/*      */ 
/*  215 */     private OptimizationTarget(String name) { this.name = name; } 
/*  216 */     public String verbose() { return this.name; }
/*      */ 
/*      */   }
/*      */ 
/*      */   public static enum ReplicatesStopCondition
/*      */   {
/*  264 */     NONE, MRE;
/*      */   }
/*      */ 
/*      */   public static enum SACooling
/*      */   {
/*  139 */     STEPS, SF; } 
/*  140 */   public static enum SADeltaL { PERCENT, BURNIN; }
/*      */ 
/*      */ 
/*      */   public static enum SAReheating
/*      */   {
/*  138 */     DECREMENTS, THRESHOLD, NEVER;
/*      */   }
/*      */ 
/*      */   public static enum SASchedule
/*      */   {
/*  121 */     LUNDY("Lundy"), 
/*  122 */     CAUCHY("Fast Cauchy"), 
/*  123 */     BOLTZMANN("Boltzmann"), 
/*  124 */     GEOM("Geometric"), 
/*  125 */     RP("Ratio-Percent"), 
/*  126 */     LIN("Linear"), 
/*  127 */     TRI("Triangular"), 
/*  128 */     POLY("Polynomial"), 
/*  129 */     EXP("Transcendental (exponential)"), 
/*  130 */     LOG("Transcendental (logarithmic)"), 
/*  131 */     PER("Transcendental (periodic)"), 
/*  132 */     SPER("Transcendental (smoothed periodic)"), 
/*  133 */     TANH("Hyperbolic (tangent)"), 
/*  134 */     COSH("Hyperbolic (cosinus)");
/*      */ 
/*      */     private final String name;
/*      */ 
/*  136 */     private SASchedule(String name) { this.name = name; } 
/*  137 */     public String verbose() { return this.name; }
/*      */ 
/*      */   }
/*      */ 
/*      */   public static enum StartingTreeDistribution
/*      */   {
/*  245 */     NONE, GAMMA, VDP;
/*      */   }
/*      */ 
/*      */   public static enum StartingTreeGeneration
/*      */   {
/*  218 */     NJ("Neighbor Joining"), 
/*  219 */     LNJ("Loose Neighbor Joining"), 
/*  220 */     RANDOM("True Random"), 
/*  221 */     GIVEN("User tree(s)");
/*      */ 
/*      */     private final String name;
/*      */ 
/*  223 */     private StartingTreeGeneration(String name) { this.name = name; } 
/*  224 */     public String verbose() { return this.name; }
/*      */ 
/*      */   }
/*      */ 
/*      */   public static enum StartingTreePInvPi
/*      */   {
/*  246 */     EQUAL, ESTIMATED, CONSTANT;
/*      */   }
/*      */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.parameters.Parameters
 * JD-Core Version:    0.6.2
 */