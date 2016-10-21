/*     */ package metapiga.videoCard;
/*     */ 
/*     */ import jcuda.NativePointerObject;
/*     */ import jcuda.Pointer;
/*     */ import jcuda.driver.CUcontext;
/*     */ import jcuda.driver.CUdevice;
/*     */ import jcuda.driver.CUdeviceptr;
/*     */ import jcuda.driver.CUdevprop;
/*     */ import jcuda.driver.CUfunction;
/*     */ import jcuda.driver.CUmodule;
/*     */ import jcuda.driver.CUstream;
/*     */ import jcuda.driver.JCudaDriver;
/*     */ import jcuda.runtime.JCuda;
/*     */ import jcuda.runtime.cudaDeviceProp;
/*     */ import metapiga.modelization.Charset;
/*     */ import metapiga.modelization.Dataset;
/*     */ import metapiga.modelization.Dataset.Partition;
/*     */ import metapiga.modelization.data.DataType;
/*     */ import metapiga.parameters.Parameters;
/*     */ import metapiga.parameters.Parameters.EvaluationDistribution;
/*     */ import metapiga.parameters.Parameters.EvaluationModel;
/*     */ 
/*     */ public class VideocardContext
/*     */ {
/*  31 */   private final CUdeviceptr ratesDevPtr = new CUdeviceptr();
/*  32 */   private final CUdeviceptr eigenVectorsDevPtr = new CUdeviceptr();
/*  33 */   private final CUdeviceptr eigenVectorsInverseDevPtr = new CUdeviceptr();
/*  34 */   private final CUdeviceptr eigenValuesDevPtr = new CUdeviceptr();
/*  35 */   private final CUdeviceptr cummulativeUnderflowScalingDevPtr = new CUdeviceptr();
/*  36 */   private final CUdeviceptr invSitesDevPtr = new CUdeviceptr();
/*  37 */   private final CUdeviceptr weightsDevPtr = new CUdeviceptr();
/*  38 */   private final CUdeviceptr equiFreqDevPtr = new CUdeviceptr();
/*     */ 
/*  40 */   private final CUdeviceptr leftSequenceDevPtr = new CUdeviceptr();
/*  41 */   private final CUdeviceptr rightSequenceDevPtr = new CUdeviceptr();
/*  42 */   private final CUdeviceptr ancestralSequenceDevPtr = new CUdeviceptr();
/*     */ 
/*  44 */   private final CUdeviceptr branchLengthsDevPtr = new CUdeviceptr();
/*  45 */   private final CUdeviceptr PIjDevPtr = new CUdeviceptr();
/*  46 */   private final CUdeviceptr TPMleftDevPtr = new CUdeviceptr();
/*  47 */   private final CUdeviceptr TPMrightDevPtr = new CUdeviceptr();
/*     */   private final CUcontext context;
/*     */   private final CUmodule module;
/*     */   private final int sequenceSize;
/*     */   private final CUdevprop devProperties;
/*     */   private cudaDeviceProp runtimeDeviceProperties;
/*  58 */   private CUfunction initUfScalingKernel = new CUfunction();
/*  59 */   private CUfunction reduceStatesKernel = new CUfunction();
/*     */ 
/*  61 */   private boolean insuficientMemory = false;
/*  62 */   private boolean isDisposed = false;
/*     */   private final Parameters.EvaluationModel evaluationModel;
/*     */   private final int maxNumChars;
/*     */   private final int maxNumStates;
/*     */   private final int maxNumCategories;
/*     */   private int maxAvailableChars;
/*     */   private long availableMemoryForSequences;
/*     */   private long globalMemorySize;
/*     */   private int memoryNeededForSequences;
/*     */   private long eigenVectorsMemory;
/*     */   private int eigenValuesMemory;
/*     */   private int ratesMemory;
/*     */   private int invSitesMemory;
/*     */   private int weightsMemory;
/*     */   private int equiFreqsMemory;
/*     */   private int branchLengthsMemory;
/*     */   private int PIJmemory;
/*     */   private int TPMmemory;
/*     */   private int ufscalingMemory;
/*     */   private long auxiliaryDataMemory;
/*     */   private boolean notEnoughMemory;
/*     */   private boolean notEnoughMemoryForWholeSequences;
/*     */   private long memoryUsedBySequences;
/*     */ 
/*     */   public VideocardContext(int numCategories, int numCharacters, int numStates, CUmodule mod, CUdevprop prop, Parameters.EvaluationModel model, CUcontext ctx, CUdevice device)
/*     */   {
/* 109 */     this.context = ctx;
/* 110 */     this.module = mod;
/* 111 */     this.devProperties = prop;
/* 112 */     this.evaluationModel = model;
/*     */ 
/* 115 */     JCudaDriver.cuModuleGetFunction(this.initUfScalingKernel, this.module, "initUnderflowScaling");
/* 116 */     JCudaDriver.cuModuleGetFunction(this.reduceStatesKernel, this.module, "reduceStates");
/*     */ 
/* 118 */     this.runtimeDeviceProperties = new cudaDeviceProp();
/* 119 */     JCuda.cudaGetDeviceProperties(this.runtimeDeviceProperties, 0);
/*     */ 
/* 122 */     int warpSize = this.devProperties.SIMDWidth;
/* 123 */     numCharacters += warpSize - 1;
/* 124 */     this.maxNumCategories = numCategories;
/* 125 */     this.maxNumChars = numCharacters;
/* 126 */     this.maxNumStates = numStates;
/*     */ 
/* 128 */     this.globalMemorySize = this.runtimeDeviceProperties.totalGlobalMem;
/* 129 */     this.sequenceSize = (this.maxNumCategories * this.maxNumChars * this.maxNumStates);
/* 130 */     calculateMemoryConsumption();
/* 131 */     this.availableMemoryForSequences = (this.globalMemorySize - this.auxiliaryDataMemory);
/*     */ 
/* 146 */     int minimumCharactersMemory = 3 * warpSize * numCategories * numStates * 4;
/* 147 */     if (this.availableMemoryForSequences <= minimumCharactersMemory)
/* 148 */       this.notEnoughMemory = true;
/*     */     else {
/* 150 */       this.notEnoughMemory = false;
/*     */     }
/*     */ 
/* 158 */     if (this.availableMemoryForSequences < this.memoryNeededForSequences)
/* 159 */       this.notEnoughMemoryForWholeSequences = true;
/*     */     else {
/* 161 */       this.notEnoughMemoryForWholeSequences = false;
/*     */     }
/*     */ 
/* 167 */     if (this.notEnoughMemoryForWholeSequences)
/* 168 */       this.memoryUsedBySequences = this.availableMemoryForSequences;
/*     */     else {
/* 170 */       this.memoryUsedBySequences = this.memoryNeededForSequences;
/*     */     }
/*     */ 
/* 173 */     if (!this.notEnoughMemory) {
/* 174 */       allocateMemoryOnGPU();
/*     */     }
/*     */ 
/* 183 */     calculateCharsInMemory();
/*     */   }
/*     */ 
/*     */   private void calculateCharsInMemory()
/*     */   {
/* 190 */     int memoryNeededPerCharacter = 3 * this.maxNumStates * this.maxNumCategories * 4;
/* 191 */     this.maxAvailableChars = ((int)(this.memoryUsedBySequences / memoryNeededPerCharacter));
/*     */   }
/*     */ 
/*     */   private void allocateMemoryOnGPU()
/*     */   {
/* 198 */     JCudaDriver.cuMemAlloc(this.cummulativeUnderflowScalingDevPtr, this.ufscalingMemory);
/* 199 */     JCudaDriver.cuMemAlloc(this.ratesDevPtr, this.ratesMemory);
/* 200 */     JCudaDriver.cuMemAlloc(this.invSitesDevPtr, this.invSitesMemory);
/* 201 */     JCudaDriver.cuMemAlloc(this.weightsDevPtr, this.weightsMemory);
/* 202 */     JCudaDriver.cuMemAlloc(this.equiFreqDevPtr, this.equiFreqsMemory);
/* 203 */     switch ($SWITCH_TABLE$metapiga$parameters$Parameters$EvaluationModel()[this.evaluationModel.ordinal()]) {
/*     */     case 15:
/*     */     case 19:
/*     */     case 20:
/* 207 */       break;
/*     */     case 18:
/* 209 */       JCudaDriver.cuMemAlloc(this.PIjDevPtr, this.PIJmemory);
/* 210 */       JCudaDriver.cuMemAlloc(this.branchLengthsDevPtr, this.branchLengthsMemory);
/* 211 */       break;
/*     */     case 16:
/*     */     case 17:
/*     */     default:
/* 214 */       JCudaDriver.cuMemAlloc(this.eigenValuesDevPtr, this.eigenValuesMemory);
/* 215 */       JCudaDriver.cuMemAlloc(this.eigenVectorsDevPtr, this.eigenVectorsMemory / 2L);
/* 216 */       JCudaDriver.cuMemAlloc(this.eigenVectorsInverseDevPtr, this.eigenVectorsMemory / 2L);
/* 217 */       JCudaDriver.cuMemAlloc(this.TPMleftDevPtr, this.TPMmemory / 2);
/* 218 */       JCudaDriver.cuMemAlloc(this.TPMrightDevPtr, this.TPMmemory / 2);
/*     */     }
/*     */ 
/* 224 */     long memoryUsedPerSequence = this.memoryUsedBySequences / 3L;
/* 225 */     JCudaDriver.cuMemAlloc(this.leftSequenceDevPtr, memoryUsedPerSequence);
/* 226 */     JCudaDriver.cuMemAlloc(this.rightSequenceDevPtr, memoryUsedPerSequence);
/* 227 */     JCudaDriver.cuMemAlloc(this.ancestralSequenceDevPtr, memoryUsedPerSequence);
/*     */   }
/*     */ 
/*     */   private void calculateMemoryConsumption()
/*     */   {
/* 238 */     int eigenVectorsSize = this.maxNumStates * this.maxNumStates;
/* 239 */     int eigenValuesSize = this.maxNumStates;
/* 240 */     int ratesSize = this.maxNumCategories;
/* 241 */     int invariateSitesSize = this.maxNumChars;
/* 242 */     int weightsSize = this.maxNumChars;
/* 243 */     int equilibriumFreqsSize = this.maxNumStates;
/* 244 */     int branchLengthsVectorSize = 2;
/*     */ 
/* 246 */     int TPMsize = this.maxNumStates * this.maxNumStates * this.maxNumCategories;
/* 247 */     int PIjSize = this.maxNumStates;
/* 248 */     int underflowScalingSize = this.maxNumChars * this.maxNumCategories;
/*     */ 
/* 252 */     this.memoryNeededForSequences = (3 * this.sequenceSize * 4);
/*     */ 
/* 257 */     this.eigenVectorsMemory = (2 * eigenVectorsSize * 8);
/* 258 */     this.eigenValuesMemory = (eigenValuesSize * 8);
/* 259 */     this.ratesMemory = (ratesSize * 8);
/* 260 */     this.invSitesMemory = (invariateSitesSize * 8);
/* 261 */     this.weightsMemory = (weightsSize * 4);
/* 262 */     this.equiFreqsMemory = (equilibriumFreqsSize * 8);
/* 263 */     this.branchLengthsMemory = (branchLengthsVectorSize * 8);
/* 264 */     this.PIJmemory = (PIjSize * 8);
/* 265 */     this.TPMmemory = (2 * TPMsize * 8);
/* 266 */     this.ufscalingMemory = (underflowScalingSize * 8);
/* 267 */     this.auxiliaryDataMemory = 0L;
/*     */ 
/* 270 */     this.auxiliaryDataMemory += this.ufscalingMemory;
/* 271 */     this.auxiliaryDataMemory += this.ratesMemory;
/* 272 */     this.auxiliaryDataMemory += this.invSitesMemory;
/* 273 */     this.auxiliaryDataMemory += this.equiFreqsMemory;
/* 274 */     this.auxiliaryDataMemory += this.weightsMemory;
/* 275 */     switch ($SWITCH_TABLE$metapiga$parameters$Parameters$EvaluationModel()[this.evaluationModel.ordinal()]) {
/*     */     case 20:
/* 277 */       break;
/*     */     case 15:
/* 279 */       break;
/*     */     case 19:
/* 281 */       break;
/*     */     case 18:
/* 283 */       this.auxiliaryDataMemory += this.PIJmemory;
/* 284 */       this.auxiliaryDataMemory += this.branchLengthsMemory;
/*     */     case 16:
/*     */     case 17:
/*     */     default:
/* 287 */       this.auxiliaryDataMemory += this.TPMmemory;
/* 288 */       this.auxiliaryDataMemory += this.eigenValuesMemory;
/* 289 */       this.auxiliaryDataMemory += this.eigenVectorsMemory;
/*     */     }
/*     */   }
/*     */ 
/*     */   public CUdeviceptr getLeftSeqMemPtr()
/*     */   {
/* 297 */     return this.leftSequenceDevPtr;
/*     */   }
/*     */ 
/*     */   public CUdeviceptr getRightSeqMemPtr() {
/* 301 */     return this.rightSequenceDevPtr;
/*     */   }
/*     */ 
/*     */   public CUdeviceptr getAncestorSeqMemPtr() {
/* 305 */     return this.ancestralSequenceDevPtr;
/*     */   }
/*     */ 
/*     */   public CUdeviceptr getPIjDevPtr(CUstream stream) {
/* 309 */     return this.PIjDevPtr;
/*     */   }
/*     */ 
/*     */   public CUdeviceptr getCummulativeUfscalingDevPtr() {
/* 313 */     return this.cummulativeUnderflowScalingDevPtr;
/*     */   }
/*     */ 
/*     */   public CUdeviceptr getRatesDevPtr() {
/* 317 */     return this.ratesDevPtr;
/*     */   }
/*     */ 
/*     */   public CUdeviceptr getInvSitesDevPtr() {
/* 321 */     return this.invSitesDevPtr;
/*     */   }
/*     */ 
/*     */   public CUdeviceptr getWeightsDevPtr() {
/* 325 */     return this.weightsDevPtr;
/*     */   }
/*     */ 
/*     */   public CUdeviceptr getEigenVectorsDevPtr() {
/* 329 */     return this.eigenVectorsDevPtr;
/*     */   }
/*     */ 
/*     */   public CUdeviceptr getEigenVectorsInverseDevPtr() {
/* 333 */     return this.eigenVectorsInverseDevPtr;
/*     */   }
/*     */ 
/*     */   public CUdeviceptr getEigenValuesDevPtr() {
/* 337 */     return this.eigenValuesDevPtr;
/*     */   }
/*     */ 
/*     */   public CUdeviceptr getTransProbabMatrixLeftDevPtr() {
/* 341 */     return this.TPMleftDevPtr;
/*     */   }
/*     */ 
/*     */   public CUdeviceptr getTransProbabMatrixRightDevPtr() {
/* 345 */     return this.TPMrightDevPtr;
/*     */   }
/*     */ 
/*     */   public CUdeviceptr getPIjDevPtr() {
/* 349 */     return this.PIjDevPtr;
/*     */   }
/*     */ 
/*     */   public CUdeviceptr getEquiFreqDevPtr() {
/* 353 */     return this.equiFreqDevPtr;
/*     */   }
/*     */ 
/*     */   public CUdeviceptr getBranchLengthsDevPtr() {
/* 357 */     return this.branchLengthsDevPtr;
/*     */   }
/*     */ 
/*     */   public CUmodule getModule() {
/* 361 */     return this.module;
/*     */   }
/*     */ 
/*     */   public CUdevprop getDeviceProperties() {
/* 365 */     return this.devProperties;
/*     */   }
/*     */ 
/*     */   public int getMaxCharactersOnDeviceMemory() {
/* 369 */     return this.maxAvailableChars;
/*     */   }
/*     */ 
/*     */   public void resetUfScaling(int numElements) {
/* 373 */     int numBlocks = (int)Math.ceil(numElements / 1024.0D);
/* 374 */     int numThreads = Math.min(1024, numElements);
/*     */ 
/* 378 */     Pointer argz = Pointer.to(new NativePointerObject[] { 
/* 376 */       Pointer.to(new NativePointerObject[] { 
/* 376 */       this.cummulativeUnderflowScalingDevPtr }), 
/* 377 */       Pointer.to(new double[] { 1.0D }), 
/* 378 */       Pointer.to(new int[] { numElements }) });
/*     */ 
/* 381 */     JCudaDriver.cuLaunchKernel(this.initUfScalingKernel, 
/* 382 */       numBlocks, 1, 1, 
/* 383 */       numThreads, 1, 1, 
/* 384 */       0, null, 
/* 385 */       argz, null);
/*     */   }
/*     */ 
/*     */   public boolean isMemoryInsuficient() {
/* 389 */     return this.insuficientMemory;
/*     */   }
/*     */ 
/*     */   public boolean isDisposed() {
/* 393 */     return this.isDisposed;
/*     */   }
/*     */ 
/*     */   public void freeMemory() {
/* 397 */     if (this.isDisposed) {
/* 398 */       return;
/*     */     }
/* 400 */     this.isDisposed = true;
/*     */ 
/* 403 */     JCudaDriver.cuMemFree(this.cummulativeUnderflowScalingDevPtr);
/* 404 */     JCudaDriver.cuMemFree(this.ratesDevPtr);
/* 405 */     JCudaDriver.cuMemFree(this.invSitesDevPtr);
/* 406 */     JCudaDriver.cuMemFree(this.weightsDevPtr);
/* 407 */     JCudaDriver.cuMemFree(this.equiFreqDevPtr);
/* 408 */     switch ($SWITCH_TABLE$metapiga$parameters$Parameters$EvaluationModel()[this.evaluationModel.ordinal()]) {
/*     */     case 15:
/*     */     case 19:
/*     */     case 20:
/* 412 */       break;
/*     */     case 18:
/* 414 */       JCudaDriver.cuMemFree(this.PIjDevPtr);
/* 415 */       JCudaDriver.cuMemFree(this.branchLengthsDevPtr);
/* 416 */       break;
/*     */     case 16:
/*     */     case 17:
/*     */     default:
/* 419 */       JCudaDriver.cuMemFree(this.eigenValuesDevPtr);
/* 420 */       JCudaDriver.cuMemFree(this.eigenVectorsDevPtr);
/* 421 */       JCudaDriver.cuMemFree(this.eigenVectorsInverseDevPtr);
/* 422 */       JCudaDriver.cuMemFree(this.TPMleftDevPtr);
/* 423 */       JCudaDriver.cuMemFree(this.TPMrightDevPtr);
/*     */     }
/*     */ 
/* 428 */     JCudaDriver.cuMemFree(this.leftSequenceDevPtr);
/* 429 */     JCudaDriver.cuMemFree(this.rightSequenceDevPtr);
/* 430 */     JCudaDriver.cuMemFree(this.ancestralSequenceDevPtr);
/*     */ 
/* 432 */     JCudaDriver.cuCtxDestroy(this.context);
/*     */   }
/*     */ 
/*     */   public static VideocardContext getVCcontext(Parameters params) {
/* 436 */     CUcontext gpuContext = new CUcontext();
/* 437 */     JCudaDriver.cuCtxCreate(gpuContext, 0, params.device);
/* 438 */     CUmodule gpuModule = new CUmodule();
/* 439 */     JCudaDriver.cuModuleLoad(gpuModule, params.ptxFilePath);
/*     */ 
/* 442 */     int numCategories = params.evaluationDistribution == Parameters.EvaluationDistribution.NONE ? 1 : params.evaluationDistributionSubsets;
/* 443 */     int maxNumCharComp = 0;
/* 444 */     int maxNumStates = 0;
/*     */ 
/* 446 */     for (Charset c : params.dataset.getPartitionCharsets()) {
/* 447 */       int charN = params.dataset.getPartition(c).getCompressedNChar();
/* 448 */       if (charN > maxNumCharComp) maxNumCharComp = charN;
/* 449 */       int statN = params.dataset.getPartition(c).getDataType().numOfStates();
/* 450 */       if (statN > maxNumStates) maxNumStates = statN;
/*     */ 
/*     */     }
/*     */ 
/* 454 */     maxNumCharComp += params.gpuDevProperties.SIMDWidth - 1;
/* 455 */     VideocardContext videocardContext = new VideocardContext(numCategories, maxNumCharComp, maxNumStates, gpuModule, params.gpuDevProperties, params.evaluationModel, gpuContext, params.device);
/* 456 */     return videocardContext;
/*     */   }
/*     */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.videoCard.VideocardContext
 * JD-Core Version:    0.6.2
 */