/*     */ package metapiga.modelization.modeltest;
/*     */ 
/*     */ import cern.jet.stat.Probability;
/*     */ import java.util.ArrayList;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Set;
/*     */ import java.util.concurrent.CountDownLatch;
/*     */ import java.util.concurrent.ExecutorService;
/*     */ import java.util.concurrent.Executors;
/*     */ import metapiga.RateParameter;
/*     */ import metapiga.modelization.Charset;
/*     */
/*     */
/*     */
/*     */ import metapiga.utilities.Tools;
/*     */ 
/*     */ public class LRT
/*     */   implements ModelTest
/*     */ {
/*  33 */   private final double alpha = 0.01D;
/*  34 */   private final String endl = "\n";
/*  35 */   private volatile boolean stopAskedByUser = false;
/*     */   private ExecutorService samplingExecutor;
/*     */   private final ModelSampling sampling;
/*     */   private Parameters.EvaluationModel bestModel;
/*     */   private boolean bestDistribution;
/*     */   private boolean bestInvariant;
/*  42 */   private Parameters.EvaluationStateFrequencies bestFreq = null;
/*     */   private StringBuilder results;
/*     */ 
/*     */   public LRT(ModelSampling sampling)
/*     */   {
/*  46 */     this.sampling = sampling;
/*     */   }
/*     */ 
/*     */   public Parameters.EvaluationDistribution getBestDistribution()
/*     */   {
/*  51 */     return this.bestDistribution ? Parameters.EvaluationDistribution.GAMMA : Parameters.EvaluationDistribution.NONE;
/*     */   }
/*     */ 
/*     */   public double getBestDistributionShape(Charset c) throws Exception
/*     */   {
/*  56 */     return this.sampling.getDistributionShape(this.bestModel, this.bestDistribution, this.bestInvariant, this.bestFreq, c);
/*     */   }
/*     */ 
/*     */   public double getBestInvariant(Charset c) throws Exception
/*     */   {
/*  61 */     if (this.bestInvariant) return this.sampling.getInvariant(this.bestModel, this.bestDistribution, this.bestInvariant, this.bestFreq, c);
/*  62 */     return 0.0D;
/*     */   }
/*     */ 
/*     */   public boolean hasBestInvariant()
/*     */   {
/*  67 */     return this.bestInvariant;
/*     */   }
/*     */ 
/*     */   public Parameters.EvaluationModel getBestModel()
/*     */   {
/*  72 */     return this.bestModel;
/*     */   }
/*     */ 
/*     */   public Parameters.EvaluationStateFrequencies getBestStateFrequencies()
/*     */   {
/*  77 */     return this.bestFreq;
/*     */   }
/*     */ 
/*     */   public Map<RateParameter, Double> getBestRateParameters(Charset c) throws Exception
/*     */   {
/*  82 */     return this.sampling.getRateParameters(this.bestModel, this.bestDistribution, this.bestInvariant, this.bestFreq, c);
/*     */   }
/*     */ 
/*     */   public void testModels(int numCores, Set<Parameters.EvaluationModel> models) throws Exception
/*     */   {
/*  87 */     prepareSamplings(numCores, models);
/*  88 */     this.results = new StringBuilder();
/*  89 */     if (this.stopAskedByUser) return;
/*  90 */     this.bestModel = Parameters.EvaluationModel.JC;
/*  91 */     if (ratioTest(this.bestModel, false, false, Parameters.EvaluationModel.K2P, false, false) < 0.01D)
/*  92 */       this.bestModel = Parameters.EvaluationModel.K2P;
/*  93 */     if (ratioTest(this.bestModel, false, false, Parameters.EvaluationModel.HKY85, false, false) < 0.01D)
/*  94 */       this.bestModel = Parameters.EvaluationModel.HKY85;
/*  95 */     if (ratioTest(this.bestModel, false, false, Parameters.EvaluationModel.TN93, false, false) < 0.01D)
/*  96 */       this.bestModel = Parameters.EvaluationModel.TN93;
/*  97 */     if (ratioTest(this.bestModel, false, false, Parameters.EvaluationModel.GTR, false, false) < 0.01D)
/*  98 */       this.bestModel = Parameters.EvaluationModel.GTR;
/*  99 */     this.bestDistribution = false;
/* 100 */     if (ratioTest(this.bestModel, this.bestDistribution, false, this.bestModel, true, false) < 0.01D)
/* 101 */       this.bestDistribution = true;
/* 102 */     if (this.stopAskedByUser) return;
/* 103 */     this.bestInvariant = false;
/* 104 */     if (ratioTest(this.bestModel, this.bestDistribution, this.bestInvariant, this.bestModel, this.bestDistribution, true) < 0.01D)
/* 105 */       this.bestInvariant = true;
/*     */   }
/*     */ 
/*     */   public void stop()
/*     */   {
/* 110 */     this.stopAskedByUser = true;
/* 111 */     this.samplingExecutor.shutdownNow();
/*     */   }
/*     */ 
/*     */   private void prepareSamplings(final int numCores, Set<Parameters.EvaluationModel> models) throws Exception {
/* 115 */     final List ids = new ArrayList();
/* 116 */     for (int i = 0; i < numCores; i++) {
/* 117 */       ids.add(Integer.valueOf(i));
/*     */     }
/* 119 */     this.samplingExecutor = Executors.newFixedThreadPool(numCores);
/* 120 */     final CountDownLatch latch = new CountDownLatch(models.size());
/* 121 */     for (final Parameters.EvaluationModel model : models) {
/* 122 */       this.samplingExecutor.execute(new Runnable() {
/*     */         public void run() {
/* 124 */           int job = ((Integer)ids.remove(0)).intValue();
/*     */           try {
/* 126 */             LRT.this.sampling.createSampling(model, false, false, null, job, numCores);
/*     */           } catch (Exception e) {
/* 128 */             e.printStackTrace();
/*     */           }
/* 130 */           ids.add(Integer.valueOf(job));
/* 131 */           latch.countDown();
/*     */         }
/*     */       });
/*     */     }
/* 135 */     latch.await();
/*     */   }
/*     */ 
/*     */   public double ratioTest(Parameters.EvaluationModel model1, boolean gamma1, boolean invariant1, Parameters.EvaluationModel model2, boolean gamma2, boolean invariant2) throws Exception
/*     */   {
/* 140 */     double delta = 2.0D * (this.sampling.getLikelihood(model1, gamma1, invariant1, null) - this.sampling.getLikelihood(model2, gamma2, invariant2, null));
/* 141 */     int df = this.sampling.getNbrParameters(model2, gamma2, invariant2, null) - this.sampling.getNbrParameters(model1, gamma1, invariant1, null);
/* 142 */     double pvalue = delta > 0.0D ? Probability.chiSquareComplemented(df, delta) : 1.0D;
/* 143 */     this.results.append(model1 + (gamma1 ? "+G" : "") + (invariant1 ? "+I" : "") + " (ML = " + Tools.doubletoString(this.sampling.getLikelihood(model1, gamma1, invariant1, null), 4) + ")");
/* 144 */     this.results.append(" vs ");
/* 145 */     this.results.append(model2 + (gamma2 ? "+G" : "") + (invariant2 ? "+I" : "") + " (ML = " + Tools.doubletoString(this.sampling.getLikelihood(model2, gamma2, invariant2, null), 4) + ")");
/* 146 */     this.results.append("\n");
/* 147 */     this.results.append("Delta = " + Tools.doubletoString(delta, 4));
/* 148 */     this.results.append(", df = " + df);
/* 149 */     if (pvalue < 0.01D) this.results.append(", P-value < 0.01");
/* 150 */     else if (pvalue == 1.0D) this.results.append(", P-value > 1"); else
/* 151 */       this.results.append(", P-value >= 0.01");
/* 152 */     this.results.append("\n\n");
/* 153 */     return pvalue;
/*     */   }
/*     */ 
/*     */   public String getResults() {
/* 157 */     return this.results.toString();
/*     */   }
/*     */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.LRT
 * JD-Core Version:    0.6.2
 */