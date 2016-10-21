/*      */ package modelization.likelihood;
/*      */ 
/*      */ import Jama.Matrix;
/*      */ import java.io.PrintStream;
/*      */ import java.util.ArrayList;
/*      */ import java.util.HashMap;
/*      */ import java.util.List;
/*      */ import java.util.Map;
/*      */ import java.util.Map.Entry;
/*      */ import java.util.Set;
/*      */ import jcuda.NativePointerObject;
/*      */ import jcuda.Pointer;
/*      */ import jcuda.driver.CUdevprop;
/*      */ import jcuda.driver.CUfunction;
/*      */ import jcuda.driver.CUstream;
/*      */ import jcuda.driver.JCudaDriver;
/*      */ import metapiga.RateParameter;
/*      */ import metapiga.modelization.Dataset.Partition;
/*      */ import metapiga.modelization.data.DNA;
/*      */ import metapiga.parameters.Parameters.EvaluationDistribution;
/*      */ import metapiga.parameters.Parameters.EvaluationModel;
/*      */ import metapiga.parameters.Parameters.EvaluationRate;
/*      */ import metapiga.parameters.Parameters.EvaluationStateFrequencies;
/*      */ import metapiga.trees.Node;
/*      */ import metapiga.trees.Tree;
/*      */ import metapiga.trees.exceptions.NullAncestorException;
/*      */ import metapiga.trees.exceptions.UnrootableTreeException;
/*      */ import metapiga.videoCard.VideocardContext;
/*      */ 
/*      */ public class LikelihoodGpu extends Likelihood
/*      */ {
/*   44 */   private CUdevprop gpuDeviceProperties = new CUdevprop();
/*      */ 
/*   46 */   private final HashMap<CUstream, Boolean> streamOccupation = new HashMap();
/*   47 */   private final HashMap<CUstream, Node> nodeInStream = new HashMap();
/*      */   private ArrayList<CUstream> streams;
/*      */   private VideocardContext videocardContext;
/*      */   private final ModelSpecificQueuing modelQueuing;
/*      */   private final boolean isModelGTRlike;
/*      */   private final SequenceLinearArray sequencesCastedToLinear;
/*      */   private final Map<Node, double[]> cummulativeUnderflowScalingRecord;
/*      */   private int maxBlockSiteSize;
/*      */   private int blockSiteSize;
/*      */   private int blockStateSize;
/*      */   private int gridSizeX;
/*      */   private int gridSizeY;
/*      */   private int gridSizeStRedX;
/*      */   private int gridSizeStRedY;
/*      */   private int blockSiteSizeStRed;
/*      */   private int blockStateSizeStRed;
/*      */   private int gridSizeCatRedX;
/*      */   private int gridSizeCatRedY;
/*      */   private int blockSiteSizeCatRed;
/*      */   private int blockCatSizeCatRed;
/*   79 */   private int GPUwarpSize = 32;
/*      */   private CharacterSplits charSplits;
/*   83 */   private CUfunction reduceStatesKernel = new CUfunction();
/*   84 */   private CUfunction reduceCategoriesKernel = new CUfunction();
/*   85 */   private CUfunction reduceSitesKernel = new CUfunction();
/*      */   private boolean splitsNecessary;
/*      */ 
/*      */   protected LikelihoodGpu(Dataset.Partition partition, Parameters.EvaluationRate rate, Parameters.EvaluationModel model, Parameters.EvaluationDistribution distribution, double distributionShape, double pinv, double apRate, Map<RateParameter, Double> rateParameters, Parameters.EvaluationStateFrequencies stateFreq, Tree tree, int numSubsets, SequenceLinearArray seq)
/*      */     throws UnrootableTreeException
/*      */   {
/*   94 */     super(partition, rate, model, distribution, distributionShape, pinv, apRate, rateParameters, stateFreq, tree, numSubsets, seq);
/*      */ 
/*   96 */     this.sequencesCastedToLinear = ((SequenceLinearArray)this.sequences);
/*   97 */     this.cummulativeUnderflowScalingRecord = new HashMap();
/*      */ 
/*   99 */     switch (model) {
/*      */     case WAG:
/*  101 */       this.isModelGTRlike = false;
/*  102 */       this.modelQueuing = new JukesCantorQueuing();
/*  103 */       break;
/*      */     case VT:
/*  105 */       this.isModelGTRlike = false;
/*  106 */       this.modelQueuing = new KimuraQueuing();
/*  107 */       break;
/*      */     case TN93:
/*  109 */       this.isModelGTRlike = false;
/*  110 */       this.modelQueuing = new HasegawaQueuing();
/*  111 */       break;
/*      */     case MTREV:
/*  113 */       this.isModelGTRlike = false;
/*  114 */       this.modelQueuing = new JukesCantorQueuing();
/*  115 */       break;
/*      */     case BLOSUM62:
/*      */     case CPREV:
/*      */     case DAYHOFF:
/*      */     case ECM:
/*      */     case GTR:
/*      */     case GTR2:
/*      */     case GTR20:
/*      */     case GTR64:
/*      */     case GY:
/*      */     case HKY85:
/*      */     case JC:
/*      */     case JTT:
/*      */     case K2P:
/*      */     case MTMAM:
/*      */     case POISSON:
/*      */     case RTREV:
/*  132 */       this.isModelGTRlike = true;
/*  133 */       this.modelQueuing = new GTRqueuing();
/*  134 */       break;
/*      */     default:
/*  137 */       this.isModelGTRlike = false;
/*  138 */       this.modelQueuing = new GTRqueuing();
/*      */     }
/*      */   }
/*      */ 
/*      */   protected LikelihoodGpu(LikelihoodGpu L, Tree tree) throws UnrootableTreeException
/*      */   {
/*  144 */     super(L, tree);
/*      */ 
/*  146 */     this.sequencesCastedToLinear = ((SequenceLinearArray)this.sequences);
/*      */ 
/*  148 */     switch ($SWITCH_TABLE$metapiga$parameters$Parameters$EvaluationModel()[this.model.ordinal()]) {
/*      */     case 20:
/*  150 */       this.isModelGTRlike = false;
/*  151 */       this.modelQueuing = new JukesCantorQueuing();
/*  152 */       break;
/*      */     case 19:
/*  154 */       this.isModelGTRlike = false;
/*  155 */       this.modelQueuing = new KimuraQueuing();
/*  156 */       break;
/*      */     case 18:
/*  158 */       this.isModelGTRlike = false;
/*  159 */       this.modelQueuing = new HasegawaQueuing();
/*  160 */       break;
/*      */     case 1:
/*      */     case 2:
/*      */     case 6:
/*      */     case 7:
/*      */     case 8:
/*      */     case 9:
/*      */     case 10:
/*      */     case 11:
/*      */     case 12:
/*      */     case 13:
/*      */     case 14:
/*      */     case 16:
/*      */     case 17:
/*  174 */       this.isModelGTRlike = true;
/*  175 */       this.modelQueuing = new GTRqueuing();
/*  176 */       break;
/*      */     case 3:
/*      */     case 4:
/*      */     case 5:
/*      */     case 15:
/*      */     default:
/*  179 */       this.isModelGTRlike = true;
/*  180 */       this.modelQueuing = new GTRqueuing();
/*      */     }
/*      */ 
/*  184 */     this.cummulativeUnderflowScalingRecord = new HashMap();
/*  185 */     int sitesWithPadding = this.sequencesCastedToLinear.getCharacterCountWithPadding();
/*  186 */     for (Entry E : L.cummulativeUnderflowScalingRecord.entrySet()) {
/*  187 */       double[] ufs = new double[this.numCat * sitesWithPadding];
/*  188 */       double[] Lufs = (double[])E.getValue();
/*  189 */       System.arraycopy(Lufs, 0, ufs, 0, sitesWithPadding * this.numCat);
/*  190 */       Node key = E.getKey() == null ? null : tree.getNode(((Node)E.getKey()).getLabel());
/*  191 */       this.cummulativeUnderflowScalingRecord.put(key, ufs);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void initGPUblockAndGridSizes()
/*      */   {
/*  197 */     this.GPUwarpSize = this.gpuDeviceProperties.SIMDWidth;
/*  198 */     int GPUwarpSizeTemp = this.GPUwarpSize;
/*      */     do
/*      */     {
/*  202 */       this.maxBlockSiteSize = (this.gpuDeviceProperties.maxThreadsPerBlock / this.numStates);
/*  203 */       this.maxBlockSiteSize = (this.maxBlockSiteSize / GPUwarpSizeTemp * GPUwarpSizeTemp);
/*  204 */       GPUwarpSizeTemp /= 2;
/*  205 */     }while (this.maxBlockSiteSize == 0);
/*  206 */     int numCharCompAllignedToWarp = (int)(Math.ceil(this.numCharComp / this.GPUwarpSize) * this.GPUwarpSize);
/*      */ 
/*  209 */     this.blockSiteSize = ((this.numCharComp < this.maxBlockSiteSize) && (this.maxBlockSiteSize >= this.GPUwarpSize) ? numCharCompAllignedToWarp : this.maxBlockSiteSize);
/*      */ 
/*  211 */     this.blockStateSize = this.numStates;
/*  212 */     this.gridSizeX = ((int)Math.ceil(this.numCharComp / this.blockSiteSize));
/*  213 */     this.gridSizeY = this.numCat;
/*      */ 
/*  216 */     int reduceNumCharCompAllignedToWarp = (int)(Math.ceil(this.numCharComp / this.GPUwarpSize) * this.GPUwarpSize);
/*  217 */     this.blockSiteSizeStRed = ((this.numCharComp < this.maxBlockSiteSize) && (this.maxBlockSiteSize >= this.GPUwarpSize) ? reduceNumCharCompAllignedToWarp : this.maxBlockSiteSize);
/*  218 */     this.blockStateSizeStRed = this.numStates;
/*  219 */     this.gridSizeStRedX = ((int)Math.ceil(this.numCharComp / this.blockSiteSize));
/*  220 */     this.gridSizeStRedY = this.numCat;
/*      */ 
/*  222 */     int maxBlockSiteSizeReduceCats = this.gpuDeviceProperties.maxThreadsPerBlock / this.numCat;
/*  223 */     maxBlockSiteSizeReduceCats = maxBlockSiteSizeReduceCats / this.GPUwarpSize * this.GPUwarpSize;
/*  224 */     this.blockSiteSizeCatRed = (this.numCharComp < maxBlockSiteSizeReduceCats ? numCharCompAllignedToWarp : maxBlockSiteSizeReduceCats);
/*  225 */     this.blockCatSizeCatRed = this.numCat;
/*  226 */     this.gridSizeCatRedX = ((int)Math.ceil(this.numCharComp / this.blockSiteSizeCatRed));
/*  227 */     this.gridSizeCatRedY = 1;
/*      */   }
/*      */ 
/*      */   public void update(Node node)
/*      */     throws NullAncestorException
/*      */   {
/*  234 */     if (node == this.tree.getRoot())
/*      */     {
/*  239 */       updateByLevels();
/*  240 */       return;
/*      */     }
/*      */   }
/*      */ 
/*      */   private void updateWithoutLikelihood(Node ancNode) {
/*  245 */     updateInRecursion(ancNode, false);
/*      */   }
/*      */   private void updateInRecursion(Node ancNode, boolean isInReqursion) {
/*  248 */     if (!isInReqursion) {
/*  249 */       if (this.isModelGTRlike) {
/*  250 */         sendEigensToGPU(this.ev, this.eg, this.evi);
/*      */       }
/*      */ 
/*  253 */       JCudaDriver.cuMemcpyHtoD(this.videocardContext.getRatesDevPtr(), Pointer.to(this.rates), this.numCat * 8);
/*  254 */       JCudaDriver.cuMemcpyHtoD(this.videocardContext.getInvSitesDevPtr(), Pointer.to(this.invariantSites), this.numCharComp * 8);
/*  255 */       JCudaDriver.cuMemcpyHtoD(this.videocardContext.getWeightsDevPtr(), Pointer.to(this.part.getAllWeights()), this.numCharComp * 4);
/*  256 */       JCudaDriver.cuMemcpyHtoD(this.videocardContext.getEquiFreqDevPtr(), Pointer.to(this.equiFreq), this.equiFreq.length * 8);
/*  257 */       int sitesWithPadding = this.sequencesCastedToLinear.getCharacterCountWithPadding();
/*  258 */       this.videocardContext.resetUfScaling(this.numCat * sitesWithPadding);
/*  259 */       this.underflow = true;
/*      */     }
/*  261 */     List children = ancNode.getChildren();
/*  262 */     for (Node child : children) {
/*  263 */       if (!child.isLeaf()) {
/*  264 */         updateInRecursion(child, true);
/*      */       }
/*      */     }
/*      */     try
/*      */     {
/*  269 */       calculateNode(ancNode);
/*      */     } catch (NullAncestorException e) {
/*  271 */       e.printStackTrace();
/*      */     } catch (Exception e) {
/*  273 */       e.printStackTrace();
/*  274 */       System.exit(-1);
/*      */     }
/*      */   }
/*      */ 
/*      */   private void updateByLevels()
/*      */   {
/*  282 */     Node node = this.tree.getRoot();
/*      */     try {
/*  284 */       Map levelsOfNodes = this.tree.getNodesInLevels();
/*  285 */       if (this.isModelGTRlike) {
/*  286 */         sendEigensToGPU(this.ev, this.eg, this.evi);
/*      */       }
/*      */ 
/*  289 */       JCudaDriver.cuMemcpyHtoD(this.videocardContext.getRatesDevPtr(), Pointer.to(this.rates), this.numCat * 8);
/*  290 */       JCudaDriver.cuMemcpyHtoD(this.videocardContext.getInvSitesDevPtr(), Pointer.to(this.invariantSites), this.numCharComp * 8);
/*  291 */       JCudaDriver.cuMemcpyHtoD(this.videocardContext.getWeightsDevPtr(), Pointer.to(this.part.getAllWeights()), this.numCharComp * 4);
/*  292 */       JCudaDriver.cuMemcpyHtoD(this.videocardContext.getEquiFreqDevPtr(), Pointer.to(this.equiFreq), this.equiFreq.length * 8);
/*  293 */       int sitesWithPadding = this.sequencesCastedToLinear.getCharacterCountWithPadding();
/*  294 */       this.videocardContext.resetUfScaling(this.numCat * sitesWithPadding);
/*  295 */       this.underflow = true;
/*      */ 
/*  300 */       for (int level = 2; levelsOfNodes.containsKey(Integer.valueOf(level)); level++) {
/*  301 */         List nodesAtLevel = (List)levelsOfNodes.get(Integer.valueOf(level));
/*  302 */         for (Node ancNode : nodesAtLevel)
/*  303 */           calculateNode(ancNode);
/*      */       }
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  308 */       e.printStackTrace();
/*  309 */       System.exit(-1);
/*      */     }
/*      */ 
/*  312 */     this.toUpdate.remove(node);
/*  313 */     calculateLikelihoodAtRoot();
/*      */   }
/*      */ 
/*      */   private void calculateNode(Node ancNode)
/*      */     throws NullAncestorException, Exception
/*      */   {
/*  322 */     assert (ancNode.isInode()) : "Cannot use a leaf node in this method";
/*      */ 
/*  324 */     List children = ancNode.getChildren();
/*  325 */     Node leftChild = (Node)children.get(0);
/*  326 */     Node rightChild = (Node)children.get(1);
/*      */ 
/*  328 */     for (int kidCount = 1; kidCount < children.size(); kidCount++)
/*      */     {
/*      */       double branchLengthRight;
/*      */       double branchLengthLeft;
/*      */       double branchLengthRight;
/*  330 */       if (kidCount > 1) {
/*  331 */         leftChild = (Node)children.get(kidCount);
/*  332 */         rightChild = ancNode;
/*  333 */         double branchLengthLeft = leftChild.getAncestorBranchLength() / (1.0D - this.pInv);
/*  334 */         branchLengthRight = 0.0D;
/*      */       } else {
/*  336 */         branchLengthLeft = leftChild.getAncestorBranchLength() / (1.0D - this.pInv);
/*  337 */         branchLengthRight = rightChild.getAncestorBranchLength() / (1.0D - this.pInv);
/*      */       }
/*      */ 
/*  343 */       int nodeIdx = ((Integer)this.nodeIndex.get(ancNode)).intValue();
/*  344 */       int leftChildIdx = ((Integer)this.nodeIndex.get(leftChild)).intValue();
/*  345 */       int rightChildIdx = ((Integer)this.nodeIndex.get(rightChild)).intValue();
/*      */ 
/*  347 */       for (int splitIdx = 0; splitIdx < this.charSplits.getNumOfSplits(); splitIdx++) {
/*  348 */         this.modelQueuing.updateStreamQueue(branchLengthLeft, branchLengthRight, leftChildIdx, rightChildIdx, nodeIdx, splitIdx);
/*      */       }
/*      */ 
/*  354 */       if (this.splitsNecessary)
/*  355 */         this.charSplits.completeSequenceFromSplitsBuffer(nodeIdx);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void calculateLikelihoodAtRoot()
/*      */   {
/*  404 */     this.likelihoodValue = 0.0D;
/*  405 */     if (this.splitsNecessary) {
/*  406 */       reduceStatesWithSplits();
/*  407 */       reduceCategoriesWithSplits();
/*      */     }
/*      */     else {
/*  410 */       reduceStates();
/*  411 */       reduceCategories();
/*      */     }
/*  413 */     this.likelihoodValue = reduceSites();
/*  414 */     assert ((!Double.isInfinite(this.likelihoodValue)) && (!Double.isNaN(this.likelihoodValue))) : "Invalid likelihood value.";
/*      */ 
/*  416 */     this.toUpdate.remove(this.tree.getRoot());
/*      */   }
/*      */ 
/*      */   private void compareExpectActual(int fromIdx, int toIdx, double[] expected, double[] actual)
/*      */   {
/*  424 */     for (int i = 0; i < toIdx; i++) {
/*  425 */       double ex = expected[i];
/*  426 */       double ac = actual[i];
/*  427 */       if (Math.abs((ex - ac) / ex) > 0.01D) {
/*  428 */         System.out.println("Diff @ " + i);
/*  429 */         System.out.println("expected = " + expected[i]);
/*  430 */         System.out.println("actual = " + actual[i]);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   private void reduceStatesWithSplits() {
/*  436 */     int rootNodeIdx = ((Integer)this.nodeIndex.get(this.tree.getRoot())).intValue();
/*  437 */     for (int splitIdx = 0; splitIdx < this.charSplits.getNumOfSplits(); splitIdx++) {
/*  438 */       int seqSplitSize = this.charSplits.getSplitSizeWithPadding(splitIdx) * this.numCat * this.numStates;
/*  439 */       int scalingSplitOffset = this.charSplits.getSplitOffset(splitIdx) * this.numCat;
/*      */ 
/*  444 */       float[] sequenceSplit = this.sequencesCastedToLinear.getSubsequenceAtNode(
/*  445 */         rootNodeIdx, 
/*  446 */         this.charSplits.getSplitOffset(splitIdx), 
/*  447 */         this.charSplits.getSplitSizeWithPadding(splitIdx));
/*      */ 
/*  449 */       JCudaDriver.cuMemcpyHtoD(this.videocardContext.getAncestorSeqMemPtr(), Pointer.to(sequenceSplit), seqSplitSize * 4);
/*      */ 
/*  453 */       int blockStateSize = this.numStates;
/*  454 */       int blockSiteSize = this.charSplits.getBlockSiteSize(splitIdx);
/*  455 */       int gridSiteSize = this.charSplits.getGridSiteSize(splitIdx);
/*  456 */       int gridCatSize = this.numCat;
/*      */ 
/*  470 */       Pointer kernelParams = Pointer.to(new NativePointerObject[] { 
/*  462 */         Pointer.to(new int[] { this.numCat }), 
/*  463 */         Pointer.to(new int[] { this.numStates }), 
/*  464 */         Pointer.to(new int[] { this.charSplits.getSplitSizeWithPadding(splitIdx) }), 
/*  465 */         Pointer.to(new int[] { this.charSplits.getSplitSizeNoPadding(splitIdx) }), 
/*  466 */         Pointer.to(new double[] { this.pInv }), 
/*  467 */         Pointer.to(new NativePointerObject[] { 
/*  467 */         this.videocardContext.getEquiFreqDevPtr() }), 
/*  468 */         Pointer.to(new NativePointerObject[] { 
/*  468 */         this.videocardContext.getAncestorSeqMemPtr() }), 
/*  469 */         Pointer.to(new NativePointerObject[] { 
/*  469 */         this.videocardContext.getCummulativeUfscalingDevPtr() }), 
/*  470 */         Pointer.to(new int[] { scalingSplitOffset }) });
/*      */ 
/*  475 */       JCudaDriver.cuLaunchKernel(
/*  476 */         this.reduceStatesKernel, gridSiteSize, gridCatSize, 1, 
/*  477 */         blockSiteSize, blockStateSize, 1, 
/*  478 */         0, null, kernelParams, null);
/*  479 */       JCudaDriver.cuCtxSynchronize();
/*      */     }
/*      */   }
/*      */ 
/*      */   private void reduceStates()
/*      */   {
/*  503 */     Pointer kernelArgsPtx = Pointer.to(new NativePointerObject[] { 
/*  495 */       Pointer.to(new int[] { this.numCat }), 
/*  496 */       Pointer.to(new int[] { this.numStates }), 
/*  497 */       Pointer.to(new int[] { this.sequencesCastedToLinear.getCharacterCountWithPadding() }), 
/*  498 */       Pointer.to(new int[] { this.sequencesCastedToLinear.getCharacterCountNoPadding() }), 
/*  499 */       Pointer.to(new double[] { this.pInv }), 
/*  500 */       Pointer.to(new NativePointerObject[] { 
/*  500 */       this.videocardContext.getEquiFreqDevPtr() }), 
/*  501 */       Pointer.to(new NativePointerObject[] { 
/*  501 */       this.videocardContext.getAncestorSeqMemPtr() }), 
/*  502 */       Pointer.to(new NativePointerObject[] { 
/*  502 */       this.videocardContext.getCummulativeUfscalingDevPtr() }), 
/*  503 */       Pointer.to(new int[1]) });
/*      */ 
/*  506 */     JCudaDriver.cuLaunchKernel(this.reduceStatesKernel, 
/*  507 */       this.gridSizeStRedX, this.gridSizeStRedY, 1, 
/*  508 */       this.blockSiteSizeStRed, this.blockStateSizeStRed, 1, 
/*  509 */       0, null, kernelArgsPtx, null);
/*      */   }
/*      */ 
/*      */   private void reduceCategories()
/*      */   {
/*  536 */     Pointer kernelArgsPtx = Pointer.to(new NativePointerObject[] { 
/*  529 */       Pointer.to(new int[] { this.numCat }), 
/*  530 */       Pointer.to(new int[] { this.sequencesCastedToLinear.getCharacterCountNoPadding() }), 
/*  531 */       Pointer.to(new int[] { this.sequencesCastedToLinear.getCharacterCountWithPadding() }), 
/*  532 */       Pointer.to(new double[] { this.pInv }), 
/*  533 */       Pointer.to(new NativePointerObject[] { 
/*  533 */       this.videocardContext.getCummulativeUfscalingDevPtr() }), 
/*  534 */       Pointer.to(new NativePointerObject[] { 
/*  534 */       this.videocardContext.getInvSitesDevPtr() }), 
/*  535 */       Pointer.to(new NativePointerObject[] { 
/*  535 */       this.videocardContext.getWeightsDevPtr() }), 
/*  536 */       Pointer.to(new int[1]) });
/*      */ 
/*  538 */     JCudaDriver.cuLaunchKernel(this.reduceCategoriesKernel, this.gridSizeCatRedX, this.gridSizeCatRedY, 1, 
/*  539 */       this.blockSiteSizeCatRed, this.blockCatSizeCatRed, 1, 
/*  540 */       0, null, kernelArgsPtx, null);
/*      */   }
/*      */ 
/*      */   private void reduceCategoriesWithSplits()
/*      */   {
/*  551 */     int maxBlockSiteSizeReduceCats = this.gpuDeviceProperties.maxThreadsPerBlock / this.numCat;
/*      */ 
/*  553 */     for (int splitIdx = 0; splitIdx < this.charSplits.getNumOfSplits(); splitIdx++)
/*      */     {
/*  555 */       int blockSiteNum = this.charSplits.getSplitSizeWithPadding(splitIdx) < maxBlockSiteSizeReduceCats ? 
/*  556 */         this.charSplits.getSplitSizeWithPadding(splitIdx) : maxBlockSiteSizeReduceCats;
/*  557 */       int blockCatNum = this.numCat;
/*  558 */       int gridX = (int)Math.ceil(this.charSplits.getSplitSizeNoPadding(splitIdx) / blockSiteNum);
/*  559 */       int gridY = 1;
/*      */ 
/*  569 */       Pointer kernelArgsPtx = Pointer.to(new NativePointerObject[] { 
/*  562 */         Pointer.to(new int[] { this.numCat }), 
/*  563 */         Pointer.to(new int[] { this.charSplits.getSplitSizeNoPadding(splitIdx) }), 
/*  564 */         Pointer.to(new int[] { this.charSplits.getSplitSizeWithPadding(splitIdx) }), 
/*  565 */         Pointer.to(new double[] { this.pInv }), 
/*  566 */         Pointer.to(new NativePointerObject[] { 
/*  566 */         this.videocardContext.getCummulativeUfscalingDevPtr() }), 
/*  567 */         Pointer.to(new NativePointerObject[] { 
/*  567 */         this.videocardContext.getInvSitesDevPtr() }), 
/*  568 */         Pointer.to(new NativePointerObject[] { 
/*  568 */         this.videocardContext.getWeightsDevPtr() }), 
/*  569 */         Pointer.to(new int[] { this.charSplits.getSplitOffset(splitIdx) }) });
/*  570 */       JCudaDriver.cuLaunchKernel(this.reduceCategoriesKernel, gridX, gridY, 1, 
/*  571 */         blockSiteNum, blockCatNum, 1, 
/*  572 */         0, null, kernelArgsPtx, null);
/*      */     }
/*      */   }
/*      */ 
/*      */   private double reduceSites() {
/*  577 */     int numOfBlocks = (this.numCharComp + 2047) / 2048;
/*  578 */     int liveElements = this.numCharComp;
/*  579 */     int numOfActiveBlocks = numOfBlocks;
/*  580 */     double[] lik = new double[1];
/*  581 */     while (liveElements > 1)
/*      */     {
/*  585 */       Pointer kernelArgsPtx = Pointer.to(new NativePointerObject[] { 
/*  583 */         Pointer.to(new NativePointerObject[] { 
/*  583 */         this.videocardContext.getCummulativeUfscalingDevPtr() }), 
/*  584 */         Pointer.to(new NativePointerObject[] { 
/*  584 */         this.videocardContext.getCummulativeUfscalingDevPtr() }), 
/*  585 */         Pointer.to(new int[] { liveElements }) });
/*      */ 
/*  587 */       JCudaDriver.cuLaunchKernel(this.reduceSitesKernel, 
/*  588 */         numOfActiveBlocks, 1, 1, 
/*  589 */         this.gpuDeviceProperties.maxThreadsPerBlock, 1, 1, 
/*  590 */         0, null, 
/*  591 */         kernelArgsPtx, null);
/*  592 */       liveElements = numOfActiveBlocks;
/*  593 */       numOfActiveBlocks = (liveElements + 2047) / 2048;
/*      */     }
/*  595 */     JCudaDriver.cuCtxSynchronize();
/*      */ 
/*  600 */     JCudaDriver.cuMemcpyDtoH(Pointer.to(lik), this.videocardContext.getCummulativeUfscalingDevPtr(), 8L);
/*  601 */     return lik[0];
/*      */   }
/*      */ 
/*      */   public double[][] getAncestralStates(Node node) throws NullAncestorException
/*      */   {
/*  606 */     double[][] ancStates = new double[this.numCharComp][this.numStates];
/*  607 */     int nodeIdx = ((Integer)this.nodeIndex.get(node)).intValue();
/*      */ 
/*  609 */     if (!this.tree.getLeaves().contains(node))
/*      */     {
/*  614 */       Node previousRoot = this.tree.getRoot();
/*  615 */       this.tree.root(node);
/*      */ 
/*  620 */       updateWithoutLikelihood(node);
/*  621 */       double[] underflowScalingValues = new double[this.sequencesCastedToLinear.getCharacterCountWithPadding() * this.numCat];
/*  622 */       JCudaDriver.cuMemcpyDtoH(
/*  623 */         Pointer.to(underflowScalingValues), 
/*  624 */         this.videocardContext.getCummulativeUfscalingDevPtr(), 
/*  625 */         underflowScalingValues.length * 8);
/*  626 */       JCudaDriver.cuCtxSynchronize();
/*      */ 
/*  628 */       double[] siteLikelihoods = new double[this.numCharComp];
/*  629 */       for (int site = 0; site < this.numCharComp; site++) {
/*  630 */         for (int state = 0; state < this.numStates; state++) {
/*  631 */           ancStates[site][state] = 0.0D;
/*  632 */           for (int cat = 0; cat < this.numCat; cat++) {
/*  633 */             double ufscaling = underflowScalingValues[(cat * this.sequencesCastedToLinear.getCharacterCountWithPadding() + site)];
/*  634 */             float sequenceValue = this.sequences.getElement(nodeIdx, cat, site, state);
/*  635 */             ancStates[site][state] += 
/*  636 */               sequenceValue * 
/*  637 */               this.equiFreq[state] * ((1.0D - this.pInv) / this.numCat) * ufscaling;
/*  638 */             siteLikelihoods[site] += ancStates[site][state];
/*      */           }
/*  640 */           if (ancStates[site][state] < 1.0E-300D) {
/*  641 */             ancStates[site][state] = 0.0D;
/*      */           }
/*      */ 
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*  648 */       for (int site = 0; site < this.numCharComp; site++) {
/*  649 */         for (int state = 0; state < this.numStates; state++) {
/*  650 */           ancStates[site][state] /= siteLikelihoods[site];
/*      */         }
/*      */       }
/*  653 */       this.tree.root(previousRoot);
/*      */     } else {
/*  655 */       for (int site = 0; site < this.numCharComp; site++) {
/*  656 */         for (int state = 0; state < this.numStates; state++) {
/*  657 */           ancStates[site][state] = this.sequences.getElement(nodeIdx, 0, site, state);
/*      */         }
/*      */       }
/*      */     }
/*  661 */     return ancStates;
/*      */   }
/*      */ 
/*      */   private void sendEigensToGPU(Matrix ev, Matrix eg, Matrix evi)
/*      */   {
/*  704 */     double[] evArray = ev.getRowPackedCopy();
/*  705 */     double[] eviArray = evi.getRowPackedCopy();
/*  706 */     double[] egArray = new double[this.numStates];
/*  707 */     for (int i = 0; i < eg.getColumnDimension(); i++) {
/*  708 */       for (int j = 0; j < eg.getRowDimension(); j++) {
/*  709 */         if (i == j) egArray[i] = eg.get(i, j);
/*      */       }
/*      */     }
/*      */ 
/*  713 */     JCudaDriver.cuMemcpyHtoD(this.videocardContext.getEigenVectorsDevPtr(), Pointer.to(evArray), evArray.length * 8);
/*  714 */     JCudaDriver.cuMemcpyHtoD(this.videocardContext.getEigenVectorsInverseDevPtr(), Pointer.to(eviArray), eviArray.length * 8);
/*  715 */     JCudaDriver.cuMemcpyHtoD(this.videocardContext.getEigenValuesDevPtr(), Pointer.to(egArray), egArray.length * 8);
/*      */   }
/*      */ 
/*      */   public void addGPUMemory(VideocardContext vc)
/*      */   {
/*  721 */     this.videocardContext = vc;
/*  722 */     this.GPUwarpSize = vc.getDeviceProperties().SIMDWidth;
/*  723 */     if ((this.modelQueuing instanceof JukesCantorQueuing)) {
/*  724 */       if (this.model == Parameters.EvaluationModel.JC)
/*  725 */         JCudaDriver.cuModuleGetFunction(this.modelQueuing.kernelFunc, vc.getModule(), "JukesCantorGpu");
/*  726 */       else if (this.model == Parameters.EvaluationModel.POISSON)
/*  727 */         JCudaDriver.cuModuleGetFunction(this.modelQueuing.kernelFunc, vc.getModule(), "PoissonGpu");
/*      */     }
/*  729 */     else if ((this.modelQueuing instanceof KimuraQueuing)) {
/*  730 */       JCudaDriver.cuModuleGetFunction(this.modelQueuing.kernelFunc, vc.getModule(), "KimuraGpu");
/*  731 */     } else if ((this.modelQueuing instanceof HasegawaQueuing)) {
/*  732 */       JCudaDriver.cuModuleGetFunction(this.modelQueuing.kernelFunc, vc.getModule(), "HasegawaGPU");
/*  733 */     } else if ((this.modelQueuing instanceof GTRqueuing)) {
/*  734 */       JCudaDriver.cuModuleGetFunction(this.modelQueuing.kernelFunc, vc.getModule(), "GTRGPU");
/*  735 */       JCudaDriver.cuModuleGetFunction(((GTRqueuing)this.modelQueuing).matrixFunction, vc.getModule(), "transMatrixCalc");
/*      */     }
/*      */ 
/*  738 */     this.gpuDeviceProperties = vc.getDeviceProperties();
/*  739 */     this.charSplits = new CharacterSplits(
/*  740 */       this.sequencesCastedToLinear.getCharacterCountNoPadding(), 
/*  741 */       this.sequencesCastedToLinear.getCharacterCountWithPadding(), 
/*  742 */       vc.getMaxCharactersOnDeviceMemory());
/*      */ 
/*  744 */     if (this.charSplits.getNumOfSplits() > 1)
/*  745 */       this.splitsNecessary = true;
/*      */     else {
/*  747 */       this.splitsNecessary = false;
/*      */     }
/*      */ 
/*  751 */     JCudaDriver.cuModuleGetFunction(this.reduceStatesKernel, vc.getModule(), "reduceStates");
/*  752 */     JCudaDriver.cuModuleGetFunction(this.reduceCategoriesKernel, vc.getModule(), "reduceCategories");
/*  753 */     JCudaDriver.cuModuleGetFunction(this.reduceSitesKernel, vc.getModule(), "reduceSites");
/*  754 */     initGPUblockAndGridSizes(); } 
/*      */   private class CharacterSplits { private int[] charSplitSize;
/*      */     private int[] charSplitSizeNoPadding;
/*      */     private int[] charSplitOffsets;
/*      */     private int numOfCharSplits;
/*      */     private float[][] splittedSequences;
/*      */     private int[] splitBlockSizes;
/*      */     private int[] splitGridSizes;
/*      */ 
/*  768 */     public CharacterSplits(int allCharacterNumNoPadding, int allCharacterNumWithPadding, int maxAvailableChars) { assert (maxAvailableChars >= LikelihoodGpu.this.GPUwarpSize) : "max chars number is too small";
/*      */ 
/*  771 */       if (maxAvailableChars > LikelihoodGpu.this.GPUwarpSize) {
/*  772 */         maxAvailableChars = maxAvailableChars / LikelihoodGpu.this.GPUwarpSize * LikelihoodGpu.this.GPUwarpSize;
/*      */       }
/*  774 */       if (allCharacterNumWithPadding <= maxAvailableChars) {
/*  775 */         this.numOfCharSplits = 1;
/*  776 */         maxAvailableChars = allCharacterNumWithPadding;
/*      */       } else {
/*  778 */         this.numOfCharSplits = ((int)Math.ceil(allCharacterNumWithPadding / maxAvailableChars));
/*      */       }
/*  780 */       this.charSplitSize = new int[this.numOfCharSplits];
/*  781 */       this.charSplitSizeNoPadding = new int[this.numOfCharSplits];
/*  782 */       this.charSplitOffsets = new int[this.numOfCharSplits];
/*  783 */       this.splittedSequences = new float[this.numOfCharSplits][];
/*  784 */       this.splitBlockSizes = new int[this.numOfCharSplits];
/*  785 */       this.splitGridSizes = new int[this.numOfCharSplits];
/*      */ 
/*  787 */       for (int split = 0; split < this.numOfCharSplits; split++) {
/*  788 */         if (split < this.numOfCharSplits - 1) {
/*  789 */           this.charSplitSize[split] = maxAvailableChars;
/*  790 */           this.charSplitSizeNoPadding[split] = maxAvailableChars;
/*  791 */           this.splittedSequences[split] = new float[maxAvailableChars * LikelihoodGpu.this.numStates * LikelihoodGpu.this.numCat];
/*      */         }
/*      */         else {
/*  794 */           int reminderSize = allCharacterNumWithPadding % maxAvailableChars;
/*  795 */           if (reminderSize == 0) {
/*  796 */             this.charSplitSize[split] = maxAvailableChars;
/*  797 */             this.charSplitSizeNoPadding[split] = maxAvailableChars;
/*  798 */             this.splittedSequences[split] = new float[maxAvailableChars * LikelihoodGpu.this.numStates * LikelihoodGpu.this.numCat];
/*      */           } else {
/*  800 */             this.charSplitSize[split] = reminderSize;
/*  801 */             this.charSplitSizeNoPadding[split] = (allCharacterNumNoPadding % maxAvailableChars);
/*  802 */             this.splittedSequences[split] = new float[reminderSize * LikelihoodGpu.this.numStates * LikelihoodGpu.this.numCat];
/*      */           }
/*      */         }
/*  805 */         calculateSplitBlockGridSize(split);
/*  806 */         this.charSplitOffsets[split] = (split == 0 ? 0 : this.charSplitOffsets[(split - 1)] + maxAvailableChars);
/*      */       } } 
/*      */     private void calculateSplitBlockGridSize(int splitIdx) {
/*  811 */       int splitSize = this.charSplitSize[splitIdx];
/*  812 */       assert (splitSize % LikelihoodGpu.this.GPUwarpSize == 0) : "splitSize is not divisible by the warp size.";
/*  813 */       int GPUwarpTemp = LikelihoodGpu.this.GPUwarpSize;
/*      */       int maxBlockSize;
/*      */       do { maxBlockSize = LikelihoodGpu.this.gpuDeviceProperties.maxThreadsPerBlock / LikelihoodGpu.this.numStates;
/*  817 */         maxBlockSize = maxBlockSize / GPUwarpTemp * GPUwarpTemp;
/*  818 */         GPUwarpTemp /= 2; }
/*  819 */       while (maxBlockSize == 0);
/*  820 */       int splitBlockSize = (splitSize < maxBlockSize) && (maxBlockSize >= LikelihoodGpu.this.GPUwarpSize) ? splitSize : 
/*  821 */         maxBlockSize;
/*  822 */       this.splitBlockSizes[splitIdx] = splitBlockSize;
/*  823 */       int gridSize = (int)Math.ceil(splitSize / this.splitBlockSizes[splitIdx]);
/*  824 */       this.splitGridSizes[splitIdx] = gridSize;
/*      */     }
/*      */ 
/*      */     public int getSplitOffset(int splitIdx) {
/*  828 */       return this.charSplitOffsets[splitIdx];
/*      */     }
/*      */ 
/*      */     public int getSplitSizeWithPadding(int splitIdx) {
/*  832 */       return this.charSplitSize[splitIdx];
/*      */     }
/*      */ 
/*      */     public int getSplitSizeNoPadding(int splitIdx) {
/*  836 */       return this.charSplitSizeNoPadding[splitIdx];
/*      */     }
/*      */ 
/*      */     public int getNumOfSplits() {
/*  840 */       return this.numOfCharSplits;
/*      */     }
/*      */ 
/*      */     public float[] getSplittedSequence(int splitIdx) {
/*  844 */       return this.splittedSequences[splitIdx];
/*      */     }
/*      */ 
/*      */     public int getBlockSiteSize(int splitIdx) {
/*  848 */       return this.splitBlockSizes[splitIdx];
/*      */     }
/*      */ 
/*      */     public int getGridSiteSize(int splitIdx) {
/*  852 */       return this.splitGridSizes[splitIdx];
/*      */     }
/*      */ 
/*      */     public void completeSequenceFromSplitsBuffer(int nodeIdx) {
/*  856 */       for (int split = 0; split < this.numOfCharSplits; split++)
/*  857 */         LikelihoodGpu.this.sequencesCastedToLinear.setSubsequenceAtNode(nodeIdx, getSplitOffset(split), getSplitSizeWithPadding(split), getSplittedSequence(split));
/*      */     }
/*      */   }
/*      */ 
/*      */   private class GTRqueuing extends ModelSpecificQueuing
/*      */   {
/* 1125 */     public final CUfunction matrixFunction = new CUfunction();
/*      */ 
/* 1127 */     public GTRqueuing() { super(null); }
/*      */ 
/*      */ 
/*      */     public void updateStreamQueue(double blLeft, double blRight, int leftChild, int rightChild, int ancNodeIndex, int splitIdx)
/*      */       throws Exception
/*      */     {
/* 1136 */       calculateTransProbabilitiesMatrices(blLeft, blRight);
/*      */ 
/* 1138 */       int seqSplitSize = LikelihoodGpu.this.charSplits.getSplitSizeWithPadding(splitIdx) * LikelihoodGpu.this.numCat * LikelihoodGpu.this.numStates;
/*      */ 
/* 1140 */       float[] leftSequence = null;
/* 1141 */       float[] rightSequence = null;
/* 1142 */       if (LikelihoodGpu.this.splitsNecessary) {
/* 1143 */         leftSequence = LikelihoodGpu.this.sequencesCastedToLinear.getSubsequenceAtNode(
/* 1144 */           leftChild, 
/* 1145 */           LikelihoodGpu.this.charSplits.getSplitOffset(splitIdx), 
/* 1146 */           LikelihoodGpu.this.charSplits.getSplitSizeWithPadding(splitIdx));
/*      */ 
/* 1148 */         rightSequence = LikelihoodGpu.this.sequencesCastedToLinear.getSubsequenceAtNode(
/* 1149 */           rightChild, 
/* 1150 */           LikelihoodGpu.this.charSplits.getSplitOffset(splitIdx), 
/* 1151 */           LikelihoodGpu.this.charSplits.getSplitSizeWithPadding(splitIdx));
/*      */       }
/*      */       else {
/* 1154 */         leftSequence = LikelihoodGpu.this.sequencesCastedToLinear.getSequenceAtNode(leftChild);
/* 1155 */         rightSequence = LikelihoodGpu.this.sequencesCastedToLinear.getSequenceAtNode(rightChild);
/*      */       }
/* 1157 */       JCudaDriver.cuMemcpyHtoD(
/* 1158 */         LikelihoodGpu.this.videocardContext.getLeftSeqMemPtr(), 
/* 1159 */         Pointer.to(leftSequence), 
/* 1160 */         seqSplitSize * 4);
/* 1161 */       JCudaDriver.cuMemcpyHtoD(
/* 1162 */         LikelihoodGpu.this.videocardContext.getRightSeqMemPtr(), 
/* 1163 */         Pointer.to(rightSequence), 
/* 1164 */         seqSplitSize * 4);
/*      */ 
/* 1179 */       Pointer kernelArgsPtx = Pointer.to(new NativePointerObject[] { 
/* 1169 */         Pointer.to(new int[] { LikelihoodGpu.this.numCat }), 
/* 1170 */         Pointer.to(new int[] { LikelihoodGpu.this.numStates }), 
/* 1171 */         Pointer.to(new int[] { LikelihoodGpu.this.charSplits.getSplitSizeWithPadding(splitIdx) }), 
/* 1172 */         Pointer.to(new int[] { LikelihoodGpu.this.charSplits.getSplitSizeNoPadding(splitIdx) }), 
/* 1173 */         Pointer.to(new NativePointerObject[] { 
/* 1173 */         LikelihoodGpu.this.videocardContext.getLeftSeqMemPtr() }), 
/* 1174 */         Pointer.to(new NativePointerObject[] { 
/* 1174 */         LikelihoodGpu.this.videocardContext.getRightSeqMemPtr() }), 
/* 1175 */         Pointer.to(new NativePointerObject[] { 
/* 1175 */         LikelihoodGpu.this.videocardContext.getAncestorSeqMemPtr() }), 
/* 1176 */         Pointer.to(new NativePointerObject[] { 
/* 1176 */         LikelihoodGpu.this.videocardContext.getTransProbabMatrixLeftDevPtr() }), 
/* 1177 */         Pointer.to(new NativePointerObject[] { 
/* 1177 */         LikelihoodGpu.this.videocardContext.getTransProbabMatrixRightDevPtr() }), 
/* 1178 */         Pointer.to(new NativePointerObject[] { 
/* 1178 */         LikelihoodGpu.this.videocardContext.getCummulativeUfscalingDevPtr() }), 
/* 1179 */         Pointer.to(new int[] { LikelihoodGpu.this.charSplits.getSplitOffset(splitIdx) }) });
/*      */ 
/* 1183 */       JCudaDriver.cuLaunchKernel(this.kernelFunc, 
/* 1184 */         LikelihoodGpu.this.charSplits.getGridSiteSize(splitIdx), LikelihoodGpu.this.gridSizeY, 1, 
/* 1185 */         LikelihoodGpu.this.charSplits.getBlockSiteSize(splitIdx), LikelihoodGpu.this.blockStateSize, 1, 
/* 1186 */         0, null, 
/* 1187 */         kernelArgsPtx, null);
/* 1188 */       JCudaDriver.cuCtxSynchronize();
/*      */ 
/* 1190 */       float[] ancestralSequence = null;
/* 1191 */       if (LikelihoodGpu.this.splitsNecessary)
/* 1192 */         ancestralSequence = LikelihoodGpu.this.charSplits.getSplittedSequence(splitIdx);
/*      */       else {
/* 1194 */         ancestralSequence = LikelihoodGpu.this.sequencesCastedToLinear.getSequenceAtNode(ancNodeIndex);
/*      */       }
/* 1196 */       JCudaDriver.cuMemcpyDtoH(
/* 1197 */         Pointer.to(ancestralSequence), 
/* 1198 */         LikelihoodGpu.this.videocardContext.getAncestorSeqMemPtr(), 
/* 1199 */         seqSplitSize * 4);
/*      */     }
/*      */ 
/*      */     private void calculateTransProbabilitiesMatrices(double bl1, double bl2)
/*      */     {
/* 1215 */       int blockSizeXmatrix = 32;
/* 1216 */       int blockSizeYmatrix = 32;
/* 1217 */       int gridSizeXmatrix = (LikelihoodGpu.this.numStates + 31) / 32;
/* 1218 */       int gridSizeYmatrix = (LikelihoodGpu.this.numStates + 31) / 32;
/*      */ 
/* 1220 */       for (int cat = 0; cat < LikelihoodGpu.this.numCat; cat++)
/*      */       {
/* 1232 */         Pointer kernelArgsPtx = Pointer.to(new NativePointerObject[] { 
/* 1222 */           Pointer.to(new int[] { LikelihoodGpu.this.numStates }), 
/* 1223 */           Pointer.to(new NativePointerObject[] { 
/* 1223 */           LikelihoodGpu.this.videocardContext.getEigenVectorsDevPtr() }), 
/* 1224 */           Pointer.to(new NativePointerObject[] { 
/* 1224 */           LikelihoodGpu.this.videocardContext.getEigenVectorsInverseDevPtr() }), 
/* 1225 */           Pointer.to(new NativePointerObject[] { 
/* 1225 */           LikelihoodGpu.this.videocardContext.getEigenValuesDevPtr() }), 
/* 1226 */           Pointer.to(new NativePointerObject[] { 
/* 1226 */           LikelihoodGpu.this.videocardContext.getTransProbabMatrixLeftDevPtr() }), 
/* 1227 */           Pointer.to(new NativePointerObject[] { 
/* 1227 */           LikelihoodGpu.this.videocardContext.getTransProbabMatrixRightDevPtr() }), 
/* 1228 */           Pointer.to(new double[] { bl1 }), 
/* 1229 */           Pointer.to(new double[] { bl2 }), 
/* 1230 */           Pointer.to(new double[] { LikelihoodGpu.this.rates[cat] }), 
/* 1231 */           Pointer.to(new double[] { LikelihoodGpu.this.apRate }), 
/* 1232 */           Pointer.to(new int[] { cat }) });
/*      */ 
/* 1235 */         JCudaDriver.cuLaunchKernel(this.matrixFunction, 
/* 1236 */           gridSizeXmatrix, gridSizeYmatrix, 1, 
/* 1237 */           blockSizeXmatrix, blockSizeYmatrix, 1, 
/* 1238 */           0, null, 
/* 1239 */           kernelArgsPtx, null);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   private class HasegawaQueuing extends ModelSpecificQueuing
/*      */   {
/*      */     public HasegawaQueuing()
/*      */     {
/* 1029 */       super(null);
/*      */     }
/*      */ 
/*      */     public void updateStreamQueue(double blLeft, double blRight, int leftChild, int rightChild, int ancNodeIndex, int splitIdx)
/*      */       throws Exception
/*      */     {
/* 1037 */       int A = DNA.A.state;
/* 1038 */       int C = DNA.C.state;
/* 1039 */       int G = DNA.G.state;
/* 1040 */       int T = DNA.T.state;
/*      */ 
/* 1042 */       double[] bl = new double[2];
/* 1043 */       double[] PIj = new double[LikelihoodGpu.this.numStates];
/*      */       double tmp77_76 = (LikelihoodGpu.this.equiFreq[A] + LikelihoodGpu.this.equiFreq[G]); PIj[G] = tmp77_76; PIj[A] = tmp77_76;
/*      */       double tmp109_108 = (LikelihoodGpu.this.equiFreq[C] + LikelihoodGpu.this.equiFreq[T]); PIj[T] = tmp109_108; PIj[C] = tmp109_108;
/*      */ 
/* 1048 */       bl[0] = blLeft;
/* 1049 */       bl[1] = blRight;
/*      */ 
/* 1051 */       double kappa = ((Double)LikelihoodGpu.this.rateParameters.get(RateParameter.K)).doubleValue();
/* 1052 */       int seqSplitSize = LikelihoodGpu.this.charSplits.getSplitSizeWithPadding(splitIdx) * LikelihoodGpu.this.numCat * LikelihoodGpu.this.numStates;
/*      */ 
/* 1054 */       float[] leftSequence = null;
/* 1055 */       float[] rightSequence = null;
/* 1056 */       if (LikelihoodGpu.this.splitsNecessary) {
/* 1057 */         leftSequence = LikelihoodGpu.this.sequencesCastedToLinear.getSubsequenceAtNode(
/* 1058 */           leftChild, 
/* 1059 */           LikelihoodGpu.this.charSplits.getSplitOffset(splitIdx), 
/* 1060 */           LikelihoodGpu.this.charSplits.getSplitSizeWithPadding(splitIdx));
/*      */ 
/* 1062 */         rightSequence = LikelihoodGpu.this.sequencesCastedToLinear.getSubsequenceAtNode(
/* 1063 */           rightChild, 
/* 1064 */           LikelihoodGpu.this.charSplits.getSplitOffset(splitIdx), 
/* 1065 */           LikelihoodGpu.this.charSplits.getSplitSizeWithPadding(splitIdx));
/*      */       }
/*      */       else {
/* 1068 */         leftSequence = LikelihoodGpu.this.sequencesCastedToLinear.getSequenceAtNode(leftChild);
/* 1069 */         rightSequence = LikelihoodGpu.this.sequencesCastedToLinear.getSequenceAtNode(rightChild);
/*      */       }
/*      */ 
/* 1072 */       JCudaDriver.cuMemcpyHtoD(
/* 1073 */         LikelihoodGpu.this.videocardContext.getLeftSeqMemPtr(), 
/* 1074 */         Pointer.to(leftSequence), 
/* 1075 */         seqSplitSize * 4);
/* 1076 */       JCudaDriver.cuMemcpyHtoD(
/* 1077 */         LikelihoodGpu.this.videocardContext.getRightSeqMemPtr(), 
/* 1078 */         Pointer.to(rightSequence), 
/* 1079 */         seqSplitSize * 4);
/*      */ 
/* 1081 */       JCudaDriver.cuMemcpyHtoD(LikelihoodGpu.this.videocardContext.getBranchLengthsDevPtr(), 
/* 1082 */         Pointer.to(bl), bl.length * 
/* 1083 */         8);
/*      */ 
/* 1085 */       JCudaDriver.cuMemcpyHtoD(LikelihoodGpu.this.videocardContext.getPIjDevPtr(), 
/* 1086 */         Pointer.to(PIj), PIj.length * 
/* 1087 */         8);
/*      */ 
/* 1105 */       Pointer kernelArgsPtx = Pointer.to(new NativePointerObject[] { Pointer.to(new int[] { LikelihoodGpu.this.numCat }), 
/* 1092 */         Pointer.to(new int[] { LikelihoodGpu.this.numStates }), 
/* 1093 */         Pointer.to(new int[] { LikelihoodGpu.this.charSplits.getSplitSizeWithPadding(splitIdx) }), 
/* 1094 */         Pointer.to(new int[] { LikelihoodGpu.this.charSplits.getSplitSizeNoPadding(splitIdx) }), 
/* 1095 */         Pointer.to(new double[] { kappa }), 
/* 1096 */         Pointer.to(new double[] { LikelihoodGpu.this.apRate }), 
/* 1097 */         Pointer.to(new NativePointerObject[] { 
/* 1097 */         LikelihoodGpu.this.videocardContext.getRatesDevPtr() }), 
/* 1098 */         Pointer.to(new double[] { LikelihoodGpu.this.rateScaling }), 
/* 1099 */         Pointer.to(new NativePointerObject[] { 
/* 1099 */         LikelihoodGpu.this.videocardContext.getLeftSeqMemPtr() }), 
/* 1100 */         Pointer.to(new NativePointerObject[] { 
/* 1100 */         LikelihoodGpu.this.videocardContext.getRightSeqMemPtr() }), 
/* 1101 */         Pointer.to(new NativePointerObject[] { 
/* 1101 */         LikelihoodGpu.this.videocardContext.getAncestorSeqMemPtr() }), 
/* 1102 */         Pointer.to(new NativePointerObject[] { 
/* 1102 */         LikelihoodGpu.this.videocardContext.getPIjDevPtr() }), 
/* 1103 */         Pointer.to(new NativePointerObject[] { 
/* 1103 */         LikelihoodGpu.this.videocardContext.getEquiFreqDevPtr() }), 
/* 1104 */         Pointer.to(new NativePointerObject[] { 
/* 1104 */         LikelihoodGpu.this.videocardContext.getBranchLengthsDevPtr() }), 
/* 1105 */         Pointer.to(new NativePointerObject[] { 
/* 1105 */         LikelihoodGpu.this.videocardContext.getCummulativeUfscalingDevPtr() }) });
/*      */ 
/* 1107 */       JCudaDriver.cuLaunchKernel(this.kernelFunc, LikelihoodGpu.this.gridSizeX, LikelihoodGpu.this.gridSizeY, 1, LikelihoodGpu.this.blockSiteSize, 
/* 1108 */         LikelihoodGpu.this.blockStateSize, 1, 0, null, kernelArgsPtx, null);
/* 1109 */       JCudaDriver.cuCtxSynchronize();
/*      */ 
/* 1111 */       float[] ancestralSequence = null;
/* 1112 */       if (LikelihoodGpu.this.splitsNecessary)
/* 1113 */         ancestralSequence = LikelihoodGpu.this.charSplits.getSplittedSequence(splitIdx);
/*      */       else {
/* 1115 */         ancestralSequence = LikelihoodGpu.this.sequencesCastedToLinear.getSequenceAtNode(ancNodeIndex);
/*      */       }
/* 1117 */       JCudaDriver.cuMemcpyDtoH(
/* 1118 */         Pointer.to(ancestralSequence), 
/* 1119 */         LikelihoodGpu.this.videocardContext.getAncestorSeqMemPtr(), 
/* 1120 */         seqSplitSize * 4);
/*      */     }
/*      */   }
/*      */ 
/*      */   private class JukesCantorQueuing extends ModelSpecificQueuing
/*      */   {
/*      */     public JukesCantorQueuing()
/*      */     {
/*  869 */       super(null);
/*      */     }
/*      */ 
/*      */     public void updateStreamQueue(double blLeft, double blRight, int leftChild, int rightChild, int ancNodeIndex, int splitIdx)
/*      */       throws Exception
/*      */     {
/*  878 */       int seqSplitSize = LikelihoodGpu.this.charSplits.getSplitSizeWithPadding(splitIdx) * LikelihoodGpu.this.numCat * LikelihoodGpu.this.numStates;
/*      */ 
/*  880 */       float[] leftSequence = null;
/*  881 */       float[] rightSequence = null;
/*  882 */       if (LikelihoodGpu.this.splitsNecessary) {
/*  883 */         leftSequence = LikelihoodGpu.this.sequencesCastedToLinear.getSubsequenceAtNode(
/*  884 */           leftChild, 
/*  885 */           LikelihoodGpu.this.charSplits.getSplitOffset(splitIdx), 
/*  886 */           LikelihoodGpu.this.charSplits.getSplitSizeWithPadding(splitIdx));
/*      */ 
/*  888 */         rightSequence = LikelihoodGpu.this.sequencesCastedToLinear.getSubsequenceAtNode(
/*  889 */           rightChild, 
/*  890 */           LikelihoodGpu.this.charSplits.getSplitOffset(splitIdx), 
/*  891 */           LikelihoodGpu.this.charSplits.getSplitSizeWithPadding(splitIdx));
/*      */       }
/*      */       else {
/*  894 */         leftSequence = LikelihoodGpu.this.sequencesCastedToLinear.getSequenceAtNode(leftChild);
/*  895 */         rightSequence = LikelihoodGpu.this.sequencesCastedToLinear.getSequenceAtNode(rightChild);
/*      */       }
/*      */ 
/*  898 */       JCudaDriver.cuMemcpyHtoD(
/*  899 */         LikelihoodGpu.this.videocardContext.getLeftSeqMemPtr(), 
/*  900 */         Pointer.to(leftSequence), 
/*  901 */         seqSplitSize * 4);
/*  902 */       JCudaDriver.cuMemcpyHtoD(
/*  903 */         LikelihoodGpu.this.videocardContext.getRightSeqMemPtr(), 
/*  904 */         Pointer.to(rightSequence), 
/*  905 */         seqSplitSize * 4);
/*      */ 
/*  922 */       Pointer kernelArgsPtx = Pointer.to(new NativePointerObject[] { 
/*  910 */         Pointer.to(new int[] { LikelihoodGpu.this.numCat }), 
/*  911 */         Pointer.to(new int[] { LikelihoodGpu.this.numStates }), 
/*  912 */         Pointer.to(new int[] { LikelihoodGpu.this.charSplits.getSplitSizeWithPadding(splitIdx) }), 
/*  913 */         Pointer.to(new int[] { LikelihoodGpu.this.charSplits.getSplitSizeNoPadding(splitIdx) }), 
/*  914 */         Pointer.to(new double[] { LikelihoodGpu.this.apRate }), 
/*  915 */         Pointer.to(new NativePointerObject[] { 
/*  915 */         LikelihoodGpu.this.videocardContext.getRatesDevPtr() }), 
/*  916 */         Pointer.to(new double[] { LikelihoodGpu.this.rateScaling }), 
/*  917 */         Pointer.to(new NativePointerObject[] { 
/*  917 */         LikelihoodGpu.this.videocardContext.getLeftSeqMemPtr() }), 
/*  918 */         Pointer.to(new NativePointerObject[] { 
/*  918 */         LikelihoodGpu.this.videocardContext.getRightSeqMemPtr() }), 
/*  919 */         Pointer.to(new NativePointerObject[] { 
/*  919 */         LikelihoodGpu.this.videocardContext.getAncestorSeqMemPtr() }), 
/*  920 */         Pointer.to(new double[] { blLeft }), 
/*  921 */         Pointer.to(new double[] { blRight }), 
/*  922 */         Pointer.to(new NativePointerObject[] { 
/*  922 */         LikelihoodGpu.this.videocardContext.getCummulativeUfscalingDevPtr() }) });
/*      */ 
/*  925 */       JCudaDriver.cuLaunchKernel(this.kernelFunc, 
/*  926 */         LikelihoodGpu.this.gridSizeX, LikelihoodGpu.this.gridSizeY, 1, 
/*  927 */         LikelihoodGpu.this.blockSiteSize, LikelihoodGpu.this.blockStateSize, 1, 
/*  928 */         0, null, 
/*  929 */         kernelArgsPtx, null);
/*      */ 
/*  932 */       JCudaDriver.cuCtxSynchronize();
/*      */ 
/*  934 */       float[] ancestralSequence = null;
/*  935 */       if (LikelihoodGpu.this.splitsNecessary)
/*  936 */         ancestralSequence = LikelihoodGpu.this.charSplits.getSplittedSequence(splitIdx);
/*      */       else {
/*  938 */         ancestralSequence = LikelihoodGpu.this.sequencesCastedToLinear.getSequenceAtNode(ancNodeIndex);
/*      */       }
/*  940 */       JCudaDriver.cuMemcpyDtoH(
/*  941 */         Pointer.to(ancestralSequence), 
/*  942 */         LikelihoodGpu.this.videocardContext.getAncestorSeqMemPtr(), 
/*  943 */         seqSplitSize * 4);
/*      */     }
/*      */   }
/*      */ 
/*      */   private class KimuraQueuing extends ModelSpecificQueuing {
/*      */     public KimuraQueuing() {
/*  949 */       super(null);
/*      */     }
/*      */ 
/*      */     public void updateStreamQueue(double blLeft, double blRight, int leftChild, int rightChild, int ancNodeIndex, int splitIdx)
/*      */       throws Exception
/*      */     {
/*  956 */       double kappa = ((Double)LikelihoodGpu.this.rateParameters.get(RateParameter.K)).doubleValue();
/*  957 */       int seqSplitSize = LikelihoodGpu.this.charSplits.getSplitSizeWithPadding(splitIdx) * LikelihoodGpu.this.numCat * LikelihoodGpu.this.numStates;
/*      */ 
/*  960 */       float[] leftSequence = null;
/*  961 */       float[] rightSequence = null;
/*  962 */       if (LikelihoodGpu.this.splitsNecessary) {
/*  963 */         leftSequence = LikelihoodGpu.this.sequencesCastedToLinear.getSubsequenceAtNode(
/*  964 */           leftChild, 
/*  965 */           LikelihoodGpu.this.charSplits.getSplitOffset(splitIdx), 
/*  966 */           LikelihoodGpu.this.charSplits.getSplitSizeWithPadding(splitIdx));
/*      */ 
/*  968 */         rightSequence = LikelihoodGpu.this.sequencesCastedToLinear.getSubsequenceAtNode(
/*  969 */           rightChild, 
/*  970 */           LikelihoodGpu.this.charSplits.getSplitOffset(splitIdx), 
/*  971 */           LikelihoodGpu.this.charSplits.getSplitSizeWithPadding(splitIdx));
/*      */       }
/*      */       else {
/*  974 */         leftSequence = LikelihoodGpu.this.sequencesCastedToLinear.getSequenceAtNode(leftChild);
/*  975 */         rightSequence = LikelihoodGpu.this.sequencesCastedToLinear.getSequenceAtNode(rightChild);
/*      */       }
/*      */ 
/*  978 */       JCudaDriver.cuMemcpyHtoD(
/*  979 */         LikelihoodGpu.this.videocardContext.getLeftSeqMemPtr(), 
/*  980 */         Pointer.to(leftSequence), 
/*  981 */         seqSplitSize * 4);
/*  982 */       JCudaDriver.cuMemcpyHtoD(
/*  983 */         LikelihoodGpu.this.videocardContext.getRightSeqMemPtr(), 
/*  984 */         Pointer.to(rightSequence), 
/*  985 */         seqSplitSize * 4);
/*      */ 
/* 1004 */       Pointer kernelArgsPtx = Pointer.to(new NativePointerObject[] { 
/*  991 */         Pointer.to(new int[] { LikelihoodGpu.this.numCat }), 
/*  992 */         Pointer.to(new int[] { LikelihoodGpu.this.numStates }), 
/*  993 */         Pointer.to(new int[] { LikelihoodGpu.this.charSplits.getSplitSizeWithPadding(splitIdx) }), 
/*  994 */         Pointer.to(new int[] { LikelihoodGpu.this.charSplits.getSplitSizeNoPadding(splitIdx) }), 
/*  995 */         Pointer.to(new double[] { kappa }), 
/*  996 */         Pointer.to(new double[] { LikelihoodGpu.this.apRate }), 
/*  997 */         Pointer.to(new NativePointerObject[] { 
/*  997 */         LikelihoodGpu.this.videocardContext.getRatesDevPtr() }), 
/*  998 */         Pointer.to(new double[] { LikelihoodGpu.this.rateScaling }), 
/*  999 */         Pointer.to(new NativePointerObject[] { 
/*  999 */         LikelihoodGpu.this.videocardContext.getLeftSeqMemPtr() }), 
/* 1000 */         Pointer.to(new NativePointerObject[] { 
/* 1000 */         LikelihoodGpu.this.videocardContext.getRightSeqMemPtr() }), 
/* 1001 */         Pointer.to(new NativePointerObject[] { 
/* 1001 */         LikelihoodGpu.this.videocardContext.getAncestorSeqMemPtr() }), 
/* 1002 */         Pointer.to(new double[] { blLeft }), 
/* 1003 */         Pointer.to(new double[] { blRight }), 
/* 1004 */         Pointer.to(new NativePointerObject[] { 
/* 1004 */         LikelihoodGpu.this.videocardContext.getCummulativeUfscalingDevPtr() }) });
/*      */ 
/* 1007 */       JCudaDriver.cuLaunchKernel(this.kernelFunc, 
/* 1008 */         LikelihoodGpu.this.gridSizeX, LikelihoodGpu.this.gridSizeY, 1, 
/* 1009 */         LikelihoodGpu.this.blockSiteSize, LikelihoodGpu.this.blockStateSize, 1, 
/* 1010 */         0, null, 
/* 1011 */         kernelArgsPtx, null);
/*      */ 
/* 1013 */       JCudaDriver.cuCtxSynchronize();
/*      */ 
/* 1015 */       float[] ancestralSequence = null;
/* 1016 */       if (LikelihoodGpu.this.splitsNecessary)
/* 1017 */         ancestralSequence = LikelihoodGpu.this.charSplits.getSplittedSequence(splitIdx);
/*      */       else {
/* 1019 */         ancestralSequence = LikelihoodGpu.this.sequencesCastedToLinear.getSequenceAtNode(ancNodeIndex);
/*      */       }
/* 1021 */       JCudaDriver.cuMemcpyDtoH(
/* 1022 */         Pointer.to(ancestralSequence), 
/* 1023 */         LikelihoodGpu.this.videocardContext.getAncestorSeqMemPtr(), 
/* 1024 */         seqSplitSize * 4);
/*      */     }
/*      */   }
/*      */ 
/*      */   private abstract class ModelSpecificQueuing
/*      */   {
/*  863 */     public final CUfunction kernelFunc = new CUfunction();
/*      */ 
/*      */     private ModelSpecificQueuing()
/*      */     {
/*      */     }
/*      */ 
/*      */     public abstract void updateStreamQueue(double paramDouble1, double paramDouble2, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
/*      */       throws Exception;
/*      */   }
/*      */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.modelization.likelihood.LikelihoodGpu
 * JD-Core Version:    0.6.2
 */