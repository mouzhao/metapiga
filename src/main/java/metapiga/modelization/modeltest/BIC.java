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
/*     */ public class BIC
/*     */   implements ModelTest
/*     */ {
/*  45 */   private final String endl = "\n";
/*  46 */   private volatile boolean stopAskedByUser = false;
/*     */   private ExecutorService samplingExecutor;
/*     */   private final ModelSampling sampling;
/*     */   private final int sampleSize;
/*     */   private Parameters.EvaluationModel bestModel;
/*     */   private boolean bestDistribution;
/*     */   private boolean bestInvariant;
/*     */   private Parameters.EvaluationStateFrequencies bestFreq;
/*     */   private double bestScore;
/*  57 */   private final Map<Double, String> results = new TreeMap();
/*     */ 
/*     */   public BIC(ModelSampling sampling, int sampleSize) {
/*  60 */     this.sampling = sampling;
/*  61 */     this.sampleSize = sampleSize;
/*     */   }
/*     */ 
/*     */   public Parameters.EvaluationDistribution getBestDistribution()
/*     */   {
/*  66 */     return this.bestDistribution ? Parameters.EvaluationDistribution.GAMMA : Parameters.EvaluationDistribution.NONE;
/*     */   }
/*     */ 
/*     */   public double getBestDistributionShape(Charset c) throws Exception
/*     */   {
/*  71 */     return this.sampling.getDistributionShape(this.bestModel, this.bestDistribution, this.bestInvariant, this.bestFreq, c);
/*     */   }
/*     */ 
/*     */   public double getBestInvariant(Charset c) throws Exception
/*     */   {
/*  76 */     if (this.bestInvariant) return this.sampling.getInvariant(this.bestModel, this.bestDistribution, this.bestInvariant, this.bestFreq, c);
/*  77 */     return 0.0D;
/*     */   }
/*     */ 
/*     */   public boolean hasBestInvariant()
/*     */   {
/*  82 */     return this.bestInvariant;
/*     */   }
/*     */ 
/*     */   public Parameters.EvaluationModel getBestModel()
/*     */   {
/*  87 */     return this.bestModel;
/*     */   }
/*     */ 
/*     */   public Parameters.EvaluationStateFrequencies getBestStateFrequencies()
/*     */   {
/*  92 */     return this.bestFreq;
/*     */   }
/*     */ 
/*     */   public Map<RateParameter, Double> getBestRateParameters(Charset c) throws Exception
/*     */   {
/*  97 */     return this.sampling.getRateParameters(this.bestModel, this.bestDistribution, this.bestInvariant, this.bestFreq, c);
/*     */   }
/*     */ 
/*     */   public void testModels(int numCores, Set<Parameters.EvaluationModel> models) throws Exception
/*     */   {
/* 102 */     prepareSamplings(numCores, models);
/* 103 */     this.results.clear();
/* 104 */     if (this.stopAskedByUser) return;
/* 105 */     this.bestScore = 1.7976931348623157E+308D;
/*     */     int i;
/* 106 */     for (Iterator localIterator = models.iterator(); localIterator.hasNext(); 
/* 108 */       i < 2)
/*     */     {
/* 106 */       Parameters.EvaluationModel model = (Parameters.EvaluationModel)localIterator.next();
/* 107 */       boolean gamma = false;
/* 108 */       i = 0; continue;
/* 109 */       boolean invariant = false;
/* 110 */       for (int j = 0; j < 2; invariant = !invariant) {
/* 111 */         for (int k = 0; k < (model.isEmpirical() ? 2 : 1); k++) {
/* 112 */           Parameters.EvaluationStateFrequencies freq = k == 0 ? Parameters.EvaluationStateFrequencies.EMPIRICAL : Parameters.EvaluationStateFrequencies.ESTIMATED;
/* 113 */           double likelihood = this.sampling.getLikelihood(model, gamma, invariant, freq);
/* 114 */           int K = this.sampling.getNbrParameters(model, gamma, invariant, freq);
/* 115 */           double BIC = 2.0D * likelihood + K * Math.log(this.sampleSize);
/* 116 */           while (this.results.containsKey(Double.valueOf(BIC))) {
/* 117 */             BIC += 1.0E-010D;
/*     */           }
/* 119 */           String res = model + (gamma ? "+G" : "") + (invariant ? "+I" : "") + (
/* 120 */             (model.isEmpirical()) && (freq == Parameters.EvaluationStateFrequencies.ESTIMATED) ? "+F" : "") + 
/* 121 */             " : ML = " + Tools.doubletoString(likelihood, 4) + ", K = " + K + ", BIC = " + Tools.doubletoString(BIC, 4);
/* 122 */           this.results.put(Double.valueOf(BIC), res);
/* 123 */           if (BIC < this.bestScore) {
/* 124 */             this.bestScore = BIC;
/* 125 */             this.bestModel = model;
/* 126 */             this.bestDistribution = gamma;
/* 127 */             this.bestInvariant = invariant;
/*     */           }
/*     */         }
/* 110 */         j++;
/*     */       }
/* 108 */       i++; gamma = !gamma;
/*     */     }
/*     */   }
/*     */ 
/*     */   public void stop()
/*     */   {
/* 137 */     this.stopAskedByUser = true;
/* 138 */     this.samplingExecutor.shutdownNow();
/*     */   }
/*     */ 
/*     */   private void prepareSamplings(final int numCores, Set<Parameters.EvaluationModel> models) throws Exception {
/* 142 */     final List ids = new ArrayList();
/* 143 */     for (int i = 0; i < numCores; i++) {
/* 144 */       ids.add(Integer.valueOf(i));
/*     */     }
/* 146 */     this.samplingExecutor = Executors.newFixedThreadPool(numCores);
/* 147 */     final CountDownLatch latch = new CountDownLatch(models.size() * 2 * 2);
/*     */     int i;
/* 148 */     for (Iterator localIterator = models.iterator(); localIterator.hasNext(); 
/* 150 */       i < 2)
/*     */     {
/* 148 */       final Parameters.EvaluationModel model = (Parameters.EvaluationModel)localIterator.next();
/* 149 */       boolean gamma = false;
/* 150 */       i = 0; continue;
/* 151 */       boolean invariant = false;
/* 152 */       for (int j = 0; j < 2; invariant = !invariant) {
/* 153 */         for (int k = 0; k < (model.isEmpirical() ? 2 : 1); k++) {
/* 154 */           Parameters.EvaluationStateFrequencies freq = k == 0 ? Parameters.EvaluationStateFrequencies.EMPIRICAL : Parameters.EvaluationStateFrequencies.ESTIMATED;
/* 155 */           final boolean gm = gamma;
/* 156 */           final boolean inv = invariant;
/* 157 */           final Parameters.EvaluationStateFrequencies fr = freq;
/* 158 */           this.samplingExecutor.execute(new Runnable() {
/*     */             public void run() {
/* 160 */               int job = ((Integer)ids.remove(0)).intValue();
/*     */               try {
/* 162 */                 BIC.this.sampling.createSampling(model, gm, inv, fr, job, numCores);
/*     */               } catch (Exception e) {
/* 164 */                 e.printStackTrace();
/*     */               }
/* 166 */               ids.add(Integer.valueOf(job));
/* 167 */               latch.countDown();
/*     */             }
/*     */           });
/*     */         }
/* 152 */         j++;
/*     */       }
/* 150 */       i++; gamma = !gamma;
/*     */     }
/*     */ 
/* 174 */     latch.await();
/*     */   }
/*     */ 
/*     */   public String getResults() {
/* 178 */     StringBuilder sb = new StringBuilder();
/* 179 */     int max = 10;
/* 180 */     sb.append(max + " first ranking (sample size = " + this.sampleSize + "):" + "\n");
/* 181 */     for (Iterator localIterator = this.results.values().iterator(); localIterator.hasNext(); 
/* 183 */       max == 0)
/*     */     {
/* 181 */       String s = (String)localIterator.next();
/* 182 */       sb.append(s + "\n");
/* 183 */       max--;
/*     */     }
/* 185 */     return sb.toString();
/*     */   }
/*     */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.BIC
 * JD-Core Version:    0.6.2
 */