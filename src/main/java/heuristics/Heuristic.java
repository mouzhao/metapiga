/*    */ package heuristics;
/*    */ 
/*    */ import jcuda.driver.CUcontext;
/*    */ import jcuda.driver.CUmodule;
/*    */ import jcuda.driver.JCudaDriver;
/*    */ import metapiga.modelization.Charset;
/*    */ import metapiga.modelization.Dataset;
/*    */ import metapiga.modelization.Dataset.Partition;
/*    */ import metapiga.modelization.data.DataType;
/*    */ import metapiga.parameters.Parameters;
/*    */ import metapiga.parameters.Parameters.EvaluationDistribution;
/*    */ import metapiga.trees.Tree;
/*    */ import metapiga.videoCard.VideocardContext;
/*    */ 
/*    */ public abstract class Heuristic
/*    */   implements Runnable
/*    */ {
/*    */   private final Parameters P;
/*    */   protected VideocardContext videocard;
/* 25 */   private CUcontext gpuContext = new CUcontext();
/* 26 */   private CUmodule gpuModule = new CUmodule();
/*    */ 
/*    */   public Heuristic(Parameters par) {
/* 29 */     this.P = par; } 
/*    */   public abstract void smoothStop();
/*    */ 
/*    */   public abstract Tree getBestSolution();
/*    */ 
/*    */   public abstract String getName(boolean paramBoolean);
/*    */ 
/* 36 */   protected void allocateGPUcontextAndMemory() { JCudaDriver.cuCtxCreate(this.gpuContext, 0, this.P.device);
/* 37 */     JCudaDriver.cuModuleLoad(this.gpuModule, this.P.ptxFilePath);
/*    */ 
/* 40 */     int numCategories = this.P.evaluationDistribution == Parameters.EvaluationDistribution.NONE ? 1 : this.P.evaluationDistributionSubsets;
/* 41 */     int maxNumCharComp = 0;
/* 42 */     int maxNumStates = 0;
/*    */ 
/* 44 */     for (Charset c : this.P.dataset.getPartitionCharsets()) {
/* 45 */       int charN = this.P.dataset.getPartition(c).getCompressedNChar();
/* 46 */       if (charN > maxNumCharComp) maxNumCharComp = charN;
/* 47 */       int statN = this.P.dataset.getPartition(c).getDataType().numOfStates();
/* 48 */       if (statN > maxNumStates) maxNumStates = statN;
/*    */     }
/*    */ 
/* 51 */     this.videocard = new VideocardContext(numCategories, maxNumCharComp, maxNumStates, this.gpuModule, this.P.gpuDevProperties, this.P.evaluationModel, this.gpuContext, this.P.device);
/*    */   }
/*    */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.heuristics.Heuristic
 * JD-Core Version:    0.6.2
 */