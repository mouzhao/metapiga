/*     */ package metapiga.modelization.modeltest;
/*     */ 
/*     */ import java.util.HashMap;
/*     */ import java.util.HashSet;
/*     */ import java.util.Map;
/*     */ import java.util.Map.Entry;
/*     */ import java.util.Set;
/*     */ import javax.swing.JDialog;
/*     */ import metapiga.RateParameter;
/*     */ import metapiga.modelization.Charset;
/*     */ import metapiga.modelization.data.DataType;
/*     */ import metapiga.modelization.data.EmpiricalModels;
/*     */ import metapiga.optimization.GA;
/*     */ import metapiga.parameters.Parameters;
/*     */ import metapiga.parameters.Parameters.EvaluationDistribution;
/*     */ import metapiga.parameters.Parameters.EvaluationModel;
/*     */ import metapiga.parameters.Parameters.EvaluationStateFrequencies;
/*     */ import metapiga.parameters.Parameters.OptimizationTarget;
/*     */ import metapiga.trees.Tree;
/*     */ import metapiga.trees.exceptions.NullAncestorException;
/*     */ import metapiga.trees.exceptions.UnrootableTreeException;
/*     */ 
/*     */ public class ModelSampling
/*     */ {
/*     */   private Parameters P;
/*  33 */   private Map<String, Double> likelihoods = new HashMap();
/*  34 */   private Map<String, Integer> nbrParameters = new HashMap();
/*  35 */   private Map<String, Map<Charset, Map<RateParameter, Double>>> paramOfR = new HashMap();
/*  36 */   private Map<String, Map<Charset, Double>> gammaShapes = new HashMap();
/*  37 */   private Map<String, Map<Charset, Double>> invariants = new HashMap();
/*  38 */   private Map<String, Parameters.EvaluationStateFrequencies> stateFrequencies = new HashMap();
/*     */   private JDialog parentDialog;
/*     */ 
/*     */   public ModelSampling(Parameters parameters)
/*     */   {
/*  43 */     this.P = parameters;
/*     */   }
/*     */ 
/*     */   public void setParentDialog(JDialog parent) {
/*  47 */     this.parentDialog = parent;
/*     */   }
/*     */ 
/*     */   private String getModelKey(Parameters.EvaluationModel model, boolean withGamma, boolean withInvariant, Parameters.EvaluationStateFrequencies freq) {
/*  51 */     String s = model.toString();
/*  52 */     if (withGamma) s = s + "+G"; else
/*  53 */       s = s + "-G";
/*  54 */     if (withInvariant) s = s + "+I"; else
/*  55 */       s = s + "-I";
/*  56 */     if (model.isEmpirical()) {
/*  57 */       switch (freq) {
/*     */       case ESTIMATED:
/*  59 */         s = s + "+F";
/*  60 */         break;
/*     */       case EMPIRICAL:
/*  62 */         s = s + "-F";
/*     */       }
/*     */     }
/*     */ 
/*  66 */     return s;
/*     */   }
/*     */ 
/*     */   private void addSampling(Tree T, boolean withInvariant, Parameters.EvaluationStateFrequencies freq) throws NullAncestorException, UnrootableTreeException {
/*  70 */     String key = getModelKey(T.getEvaluationModel(), T.getEvaluationDistribution() == Parameters.EvaluationDistribution.GAMMA, withInvariant, freq);
/*  71 */     T.setName(key);
/*     */ 
/*  73 */     this.likelihoods.put(key, Double.valueOf(T.getEvaluation()));
/*  74 */     this.stateFrequencies.put(key, freq);
/*  75 */     int nParam = 0;
/*  76 */     for (Charset c : T.getPartitions()) {
/*  77 */       if (!T.getEvaluationModel().hasEqualBaseFrequencies())
/*  78 */         nParam += T.getEvaluationModel().getDataType().numOfStates() - 1;
/*     */       Map map1;
/*     */       Map map1;
/*  81 */       if (this.paramOfR.containsKey(key))
/*  82 */         map1 = (Map)this.paramOfR.get(key);
/*     */       else {
/*  84 */         map1 = new HashMap();
/*     */       }
/*  86 */       map1.put(c, new HashMap(T.getEvaluationRateParameters(c)));
/*  87 */       this.paramOfR.put(key, map1);
/*  88 */       nParam += T.getEvaluationModel().getNumRateParameters();
/*  89 */       if (T.getEvaluationDistribution() == Parameters.EvaluationDistribution.GAMMA)
/*     */       {
/*     */         Map map2;
/*     */         Map map2;
/*  91 */         if (this.gammaShapes.containsKey(key))
/*  92 */           map2 = (Map)this.gammaShapes.get(key);
/*     */         else {
/*  94 */           map2 = new HashMap();
/*     */         }
/*  96 */         map2.put(c, Double.valueOf(T.getEvaluationGammaShape(c)));
/*  97 */         this.gammaShapes.put(key, map2);
/*  98 */         nParam++;
/*     */       }
/* 100 */       if (withInvariant)
/*     */       {
/*     */         Map map3;
/*     */         Map map3;
/* 102 */         if (this.invariants.containsKey(key))
/* 103 */           map3 = (Map)this.invariants.get(key);
/*     */         else {
/* 105 */           map3 = new HashMap();
/*     */         }
/* 107 */         map3.put(c, Double.valueOf(T.getEvaluationPInv(c)));
/* 108 */         this.invariants.put(key, map3);
/* 109 */         nParam++;
/*     */       }
/*     */     }
/* 112 */     this.nbrParameters.put(key, Integer.valueOf(nParam));
/* 113 */     T.deleteLikelihoodComputation();
/*     */   }
/*     */ 
/*     */   public void createSampling(Parameters.EvaluationModel model, boolean withGamma, boolean withInvariant, Parameters.EvaluationStateFrequencies freq, int idSampling, int maxSamplings) throws Exception
/*     */   {
/* 118 */     Tree NJT = this.P.getNJT().clone();
/* 119 */     NJT.setEvaluationModel(model);
/* 120 */     for (Charset c : NJT.getPartitions()) {
/* 121 */       switch (model) {
/*     */       case POISSON:
/* 123 */         NJT.setEvaluationRateParameter(c, RateParameter.A, 0.5D);
/* 124 */         NJT.setEvaluationRateParameter(c, RateParameter.B, 0.5D);
/* 125 */         NJT.setEvaluationRateParameter(c, RateParameter.C, 0.5D);
/* 126 */         NJT.setEvaluationRateParameter(c, RateParameter.D, 0.5D);
/* 127 */         NJT.setEvaluationRateParameter(c, RateParameter.E, 0.5D);
/* 128 */         break;
/*     */       case RTREV:
/* 130 */         NJT.setEvaluationRateParameter(c, RateParameter.K1, 0.5D);
/* 131 */         NJT.setEvaluationRateParameter(c, RateParameter.K2, 0.5D);
/* 132 */         break;
/*     */       case TN93:
/* 134 */         NJT.setEvaluationRateParameter(c, RateParameter.K, 0.5D);
/* 135 */         break;
/*     */       case VT:
/* 137 */         NJT.setEvaluationRateParameter(c, RateParameter.K, 0.5D);
/* 138 */         break;
/*     */       case CPREV:
/* 140 */         for (RateParameter r : RateParameter.getParametersOfModel(Parameters.EvaluationModel.GTR20)) {
/* 141 */           NJT.setEvaluationRateParameter(c, r, 0.5D);
/*     */         }
/* 143 */         break;
/*     */       case DAYHOFF:
/* 145 */         NJT.setEvaluationRateParameter(c, RateParameter.KAPPA, 0.5D);
/* 146 */         NJT.setEvaluationRateParameter(c, RateParameter.OMEGA, 0.5D);
/* 147 */         break;
/*     */       case ECM:
/* 149 */         for (RateParameter r : RateParameter.getParametersOfModel(Parameters.EvaluationModel.GTR64)) {
/* 150 */           NJT.setEvaluationRateParameter(c, r, 0.5D);
/*     */         }
/* 152 */         break;
/*     */       case GTR:
/*     */       case GTR2:
/*     */       case GTR20:
/*     */       case GTR64:
/*     */       case GY:
/*     */       case HKY85:
/*     */       case JC:
/*     */       case JTT:
/*     */       case K2P:
/*     */       case MTMAM:
/* 163 */         for (Entry e : EmpiricalModels.getRateParameters(model).entrySet()) {
/* 164 */           NJT.setEvaluationRateParameter(c, (RateParameter)e.getKey(), ((Double)e.getValue()).doubleValue());
/*     */         }
/* 166 */         break;
/*     */       case BLOSUM62:
/* 168 */         break;
/*     */       case WAG:
/* 170 */         break;
/*     */       case MTREV:
/* 172 */         break;
/*     */       }
/*     */ 
/* 176 */       NJT.setEvaluationDistributionShape(c, 1.0D);
/*     */     }
/* 178 */     NJT.setEvaluationDistribution(withGamma ? Parameters.EvaluationDistribution.GAMMA : Parameters.EvaluationDistribution.NONE);
/* 179 */     NJT.setEvaluationStateFrequencies(freq);
/* 180 */     Set targets = new HashSet();
/* 181 */     if ((model.getNumRateParameters() > 0) && (!model.isEmpirical())) targets.add(Parameters.OptimizationTarget.R);
/* 182 */     if (withGamma) targets.add(Parameters.OptimizationTarget.GAMMA);
/* 183 */     if (withInvariant) targets.add(Parameters.OptimizationTarget.PINV);
/* 184 */     if (targets.size() > 0) {
/* 185 */       GA optimizer = new GA(NJT, targets, 200);
/* 186 */       String message = "Optimizing parameters values for " + model;
/* 187 */       if (model.isEmpirical()) {
/* 188 */         if (freq == Parameters.EvaluationStateFrequencies.EMPIRICAL)
/* 189 */           message = message + " (using empirical aa frequencies)";
/* 190 */         else if (freq == Parameters.EvaluationStateFrequencies.ESTIMATED) {
/* 191 */           message = message + " (using estimated aa frequencies)";
/*     */         }
/*     */       }
/* 194 */       message = message + (withGamma ? " with Gamma" : "") + (withInvariant ? (withGamma ? " and" : "") + " with Invariant" : "");
/* 195 */       NJT = optimizer.getOptimizedTreeWithProgress(this.parentDialog, message, idSampling, maxSamplings);
/*     */     }
/* 197 */     addSampling(NJT, withInvariant, freq);
/*     */   }
/*     */ 
/*     */   public void createSampling(Parameters.EvaluationModel model, boolean withGamma, boolean withInvariant, Parameters.EvaluationStateFrequencies freq) throws Exception {
/* 201 */     createSampling(model, withGamma, withInvariant, freq, 0, 1);
/*     */   }
/*     */ 
/*     */   public void clear() {
/* 205 */     this.likelihoods.clear();
/* 206 */     this.nbrParameters.clear();
/* 207 */     this.paramOfR.clear();
/* 208 */     this.gammaShapes.clear();
/* 209 */     this.invariants.clear();
/* 210 */     this.stateFrequencies.clear();
/*     */   }
/*     */ 
/*     */   public double getLikelihood(Parameters.EvaluationModel model, boolean withGamma, boolean withInvariant, Parameters.EvaluationStateFrequencies freq) throws Exception {
/* 214 */     String key = getModelKey(model, withGamma, withInvariant, freq);
/* 215 */     if (!this.likelihoods.containsKey(key)) {
/* 216 */       createSampling(model, withGamma, withInvariant, freq);
/*     */     }
/* 218 */     return ((Double)this.likelihoods.get(key)).doubleValue();
/*     */   }
/*     */ 
/*     */   public int getNbrParameters(Parameters.EvaluationModel model, boolean withGamma, boolean withInvariant, Parameters.EvaluationStateFrequencies freq) throws Exception {
/* 222 */     String key = getModelKey(model, withGamma, withInvariant, freq);
/* 223 */     if (!this.nbrParameters.containsKey(key)) {
/* 224 */       createSampling(model, withGamma, withInvariant, freq);
/*     */     }
/* 226 */     return ((Integer)this.nbrParameters.get(key)).intValue();
/*     */   }
/*     */ 
/*     */   public Map<RateParameter, Double> getRateParameters(Parameters.EvaluationModel model, boolean withGamma, boolean withInvariant, Parameters.EvaluationStateFrequencies freq, Charset c) throws Exception {
/* 230 */     String key = getModelKey(model, withGamma, withInvariant, freq);
/* 231 */     if (!this.paramOfR.containsKey(key)) {
/* 232 */       createSampling(model, withGamma, withInvariant, freq);
/*     */     }
/* 234 */     if (!((Map)this.paramOfR.get(key)).containsKey(c)) throw new Exception("Charset " + c.getLabel() + " was not found.");
/* 235 */     return (Map)((Map)this.paramOfR.get(key)).get(c);
/*     */   }
/*     */ 
/*     */   public double getDistributionShape(Parameters.EvaluationModel model, boolean withGamma, boolean withInvariant, Parameters.EvaluationStateFrequencies freq, Charset c) throws Exception {
/* 239 */     String key = getModelKey(model, withGamma, withInvariant, freq);
/* 240 */     if (!this.gammaShapes.containsKey(key)) {
/* 241 */       createSampling(model, withGamma, withInvariant, freq);
/*     */     }
/* 243 */     if (!((Map)this.gammaShapes.get(key)).containsKey(c)) throw new Exception("Charset " + c.getLabel() + " was not found.");
/* 244 */     return ((Double)((Map)this.gammaShapes.get(key)).get(c)).doubleValue();
/*     */   }
/*     */ 
/*     */   public double getInvariant(Parameters.EvaluationModel model, boolean withGamma, boolean withInvariant, Parameters.EvaluationStateFrequencies freq, Charset c) throws Exception {
/* 248 */     String key = getModelKey(model, withGamma, withInvariant, freq);
/* 249 */     if (!this.invariants.containsKey(key)) {
/* 250 */       createSampling(model, withGamma, withInvariant, freq);
/*     */     }
/* 252 */     if (!((Map)this.invariants.get(key)).containsKey(c)) throw new Exception("Charset " + c.getLabel() + " was not found.");
/* 253 */     return ((Double)((Map)this.invariants.get(key)).get(c)).doubleValue();
/*     */   }
/*     */ 
/*     */   public Parameters.EvaluationStateFrequencies getStateFrequencies(Parameters.EvaluationModel model, boolean withGamma, boolean withInvariant, Parameters.EvaluationStateFrequencies freq) throws Exception {
/* 257 */     String key = getModelKey(model, withGamma, withInvariant, freq);
/* 258 */     if (!this.stateFrequencies.containsKey(key)) {
/* 259 */       createSampling(model, withGamma, withInvariant, freq);
/*     */     }
/* 261 */     return (Parameters.EvaluationStateFrequencies)this.stateFrequencies.get(key);
/*     */   }
/*     */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.ModelSampling
 * JD-Core Version:    0.6.2
 */