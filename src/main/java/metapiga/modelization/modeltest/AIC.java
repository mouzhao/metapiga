/*     */ package metapiga.modelization.modeltest;
/*     */ 
/*     */ import java.util.ArrayList;
/*     */
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Set;
/*     */ import java.util.TreeMap;
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
/*     */ public class AIC
/*     */   implements ModelTest
/*     */ {
/*  41 */   private final String endl = "\n";
/*  42 */   private volatile boolean stopAskedByUser = false;
/*     */   private ExecutorService samplingExecutor;
/*     */   private final ModelSampling sampling;
/*     */   private final int sampleSize;
/*     */   private Parameters.EvaluationModel bestModel;
/*     */   private boolean bestDistribution;
/*     */   private boolean bestInvariant;
/*     */   private Parameters.EvaluationStateFrequencies bestFreq;
/*     */   private double bestScore;
/*     */   private final boolean AICc;
/*  54 */   private final Map<Double, String> results = new TreeMap();
/*     */ 
/*     */   public AIC(ModelSampling sampling) {
/*  57 */     this.sampling = sampling;
/*  58 */     this.AICc = false;
/*  59 */     this.sampleSize = -1;
/*     */   }
/*     */ 
/*     */   public AIC(ModelSampling sampling, int sampleSize) {
/*  63 */     this.sampling = sampling;
/*  64 */     this.AICc = true;
/*  65 */     this.sampleSize = sampleSize;
/*     */   }
/*     */ 
/*     */   public Parameters.EvaluationDistribution getBestDistribution()
/*     */   {
/*  70 */     return this.bestDistribution ? Parameters.EvaluationDistribution.GAMMA : Parameters.EvaluationDistribution.NONE;
/*     */   }
/*     */ 
/*     */   public double getBestDistributionShape(Charset c) throws Exception
/*     */   {
/*  75 */     return this.sampling.getDistributionShape(this.bestModel, this.bestDistribution, this.bestInvariant, this.bestFreq, c);
/*     */   }
/*     */ 
/*     */   public double getBestInvariant(Charset c) throws Exception
/*     */   {
/*  80 */     if (this.bestInvariant) return this.sampling.getInvariant(this.bestModel, this.bestDistribution, this.bestInvariant, this.bestFreq, c);
/*  81 */     return 0.0D;
/*     */   }
/*     */ 
/*     */   public boolean hasBestInvariant()
/*     */   {
/*  86 */     return this.bestInvariant;
/*     */   }
/*     */ 
/*     */   public Parameters.EvaluationModel getBestModel()
/*     */   {
/*  91 */     return this.bestModel;
/*     */   }
/*     */ 
/*     */   public Parameters.EvaluationStateFrequencies getBestStateFrequencies()
/*     */   {
/*  96 */     return this.bestFreq;
/*     */   }
/*     */ 
/*     */   public Map<RateParameter, Double> getBestRateParameters(Charset c) throws Exception
/*     */   {
/* 101 */     return this.sampling.getRateParameters(this.bestModel, this.bestDistribution, this.bestInvariant, this.bestFreq, c);
/*     */   }
/*     */ 
/*     */   public void testModels(int numCores, Set<Parameters.EvaluationModel> models) throws Exception
/*     */   {
/* 106 */     prepareSamplings(numCores, models);
/* 107 */     this.results.clear();
/* 108 */     if (this.stopAskedByUser) return;
/* 109 */     this.bestScore = 1.7976931348623157E+308D;
/*     */     int i;
/* 110 */     for (Iterator localIterator = models.iterator(); localIterator.hasNext(); 
/* 112 */       i < 2)
/*     */     {
/* 110 */       Parameters.EvaluationModel model = (Parameters.EvaluationModel)localIterator.next();
/* 111 */       boolean gamma = false;
/* 112 */       i = 0; continue;
/* 113 */       boolean invariant = false;
/* 114 */       for (int j = 0; j < 2; invariant = !invariant) {
/* 115 */         for (int k = 0; k < (model.isEmpirical() ? 2 : 1); k++) {
/* 116 */           Parameters.EvaluationStateFrequencies freq = k == 0 ? Parameters.EvaluationStateFrequencies.EMPIRICAL : Parameters.EvaluationStateFrequencies.ESTIMATED;
/* 117 */           double likelihood = this.sampling.getLikelihood(model, gamma, invariant, freq);
/* 118 */           int K = this.sampling.getNbrParameters(model, gamma, invariant, freq);
/* 119 */           double AIC = 2.0D * (likelihood + K);
/* 120 */           if (this.AICc) {
/* 121 */             AIC += 2.0D * K * (K + 1) / (this.sampleSize - K - 1.0D);
/*     */           }
/* 123 */           while (this.results.containsKey(Double.valueOf(AIC))) {
/* 124 */             AIC += 1.0E-010D;
/*     */           }
/* 126 */           String res = model + (gamma ? "+G" : "") + (invariant ? "+I" : "") + (
/* 127 */             (model.isEmpirical()) && (freq == Parameters.EvaluationStateFrequencies.ESTIMATED) ? "+F" : "") + 
/* 128 */             " : ML = " + Tools.doubletoString(likelihood, 4) + ", K = " + K + (this.AICc ? ", AICc = " : ", AIC = ") + Tools.doubletoString(AIC, 4);
/* 129 */           this.results.put(Double.valueOf(AIC), res);
/* 130 */           if (AIC < this.bestScore) {
/* 131 */             this.bestScore = AIC;
/* 132 */             this.bestModel = model;
/* 133 */             this.bestDistribution = gamma;
/* 134 */             this.bestInvariant = invariant;
/* 135 */             this.bestFreq = freq;
/*     */           }
/*     */         }
/* 114 */         j++;
/*     */       }
/* 112 */       i++; gamma = !gamma;
/*     */     }
/*     */   }
/*     */ 
/*     */   public void stop()
/*     */   {
/* 145 */     this.stopAskedByUser = true;
/* 146 */     this.samplingExecutor.shutdownNow();
/*     */   }
/*     */ 
/*     */   private void prepareSamplings(final int numCores, Set<Parameters.EvaluationModel> models) throws Exception {
/* 150 */     final List ids = new ArrayList();
/* 151 */     for (int i = 0; i < numCores; i++) {
/* 152 */       ids.add(Integer.valueOf(i));
/*     */     }
/* 154 */     this.samplingExecutor = Executors.newFixedThreadPool(numCores);
/* 155 */     final CountDownLatch latch = new CountDownLatch(models.size() * 2 * 2);
/*     */     int i;
/* 156 */     for (Iterator localIterator = models.iterator(); localIterator.hasNext(); 
/* 158 */       i < 2)
/*     */     {
/* 156 */       final Parameters.EvaluationModel model = (Parameters.EvaluationModel)localIterator.next();
/* 157 */       boolean gamma = true;
/* 158 */       i = 0; continue;
/* 159 */       boolean invariant = false;
/* 160 */       for (int j = 0; j < 2; invariant = !invariant) {
/* 161 */         for (int k = 0; k < (model.isEmpirical() ? 2 : 1); k++) {
/* 162 */           Parameters.EvaluationStateFrequencies freq = k == 0 ? Parameters.EvaluationStateFrequencies.EMPIRICAL : Parameters.EvaluationStateFrequencies.ESTIMATED;
/* 163 */           final boolean gm = gamma;
/* 164 */           final boolean inv = invariant;
/* 165 */           final Parameters.EvaluationStateFrequencies fr = freq;
/* 166 */           this.samplingExecutor.execute(new Runnable() {
/*     */             public void run() {
/* 168 */               int job = ((Integer)ids.remove(0)).intValue();
/*     */               try {
/* 170 */                 AIC.this.sampling.createSampling(model, gm, inv, fr, job, numCores);
/*     */               } catch (Exception e) {
/* 172 */                 e.printStackTrace();
/*     */               }
/* 174 */               ids.add(Integer.valueOf(job));
/* 175 */               latch.countDown();
/*     */             }
/*     */           });
/*     */         }
/* 160 */         j++;
/*     */       }
/* 158 */       i++; gamma = !gamma;
/*     */     }
/*     */ 
/* 182 */     latch.await();
/*     */   }
/*     */ 
/*     */   public String getResults() {
/* 186 */     StringBuilder sb = new StringBuilder();
/* 187 */     int max = 10;
/* 188 */     sb.append(max + " first ranking:" + "\n");
/* 189 */     for (Iterator localIterator = this.results.values().iterator(); localIterator.hasNext(); 
/* 191 */       max == 0)
/*     */     {
/* 189 */       String s = (String)localIterator.next();
/* 190 */       sb.append(s + "\n");
/* 191 */       max--;
/*     */     }
/* 193 */     return sb.toString();
/*     */   }
/*     */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.AIC
 * JD-Core Version:    0.6.2
 */