/*     */ package optimization;
/*     */ 
/*     */ import java.io.PrintStream;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Arrays;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Set;
/*     */ import javax.swing.JDialog;
/*     */ import metapiga.RateParameter;
/*     */ import metapiga.modelization.Charset;
/*     */ import metapiga.parameters.Parameters.EvaluationModel;
/*     */ import metapiga.parameters.Parameters.OptimizationTarget;
/*     */ import metapiga.trees.Branch;
/*     */ import metapiga.trees.Node;
/*     */ import metapiga.trees.Tree;
/*     */ import metapiga.trees.exceptions.BranchNotFoundException;
/*     */ import metapiga.trees.exceptions.NullAncestorException;
/*     */ import metapiga.trees.exceptions.UnrootableTreeException;
/*     */ 
/*     */ public class Powell
/*     */   implements Optimizer
/*     */ {
/*     */   private static final int POWELL_ITMAX = 200;
/*     */   private static final double GOLD_NUMBER = 1.618034D;
/*     */   private static final double GOLD_LIMIT = 100.0D;
/*     */   private static final double GOLD_TINY = 1.0E-020D;
/*     */   private static final int BRENT_ITMAX = 100;
/*     */   private static final double BRENT_CGOLD = 0.381966D;
/*     */   private static final double BRENT_ZEPS = 1.0E-010D;
/*     */   private static final double BRENT_TOL = 0.0001D;
/*     */   private final Tree T;
/*     */   private final List<Charset> partitions;
/*     */   private final int nPart;
/*  41 */   private final List<Branch> branches = new ArrayList();
/*     */   private final int N;
/*     */   private final int nBranch;
/*     */   private final int nParam;
/*     */   private final int nGamma;
/*     */   private final int nPinv;
/*     */   private final double[] p;
/*     */   private final double[] pcom;
/*     */   private final double[] xicom;
/*     */   private final double[][] xi;
/*     */ 
/*     */   public Powell(Tree tree, List<Branch> branchesToOptimize, Set<Parameters.OptimizationTarget> targetsToOptimize)
/*     */     throws BranchNotFoundException
/*     */   {
/*  48 */     this.T = tree.clone();
/*  49 */     this.partitions = this.T.getPartitions();
/*  50 */     this.nPart = this.partitions.size();
/*  51 */     for (Branch b : branchesToOptimize) {
/*  52 */       this.branches.add(this.T.getBranch(this.T.getNode(b.getNode().getLabel()), this.T.getNode(b.getOtherNode().getLabel())));
/*     */     }
/*  54 */     this.nBranch = this.branches.size();
/*  55 */     if (targetsToOptimize.contains(Parameters.OptimizationTarget.R))
/*  56 */       this.nParam = (this.T.getEvaluationModel().getNumRateParameters() * this.nPart);
/*     */     else {
/*  58 */       this.nParam = 0;
/*     */     }
/*  60 */     this.nGamma = (targetsToOptimize.contains(Parameters.OptimizationTarget.GAMMA) ? this.nPart : 0);
/*  61 */     this.nPinv = (targetsToOptimize.contains(Parameters.OptimizationTarget.PINV) ? this.nPart : 0);
/*  62 */     this.N = (this.nBranch + this.nParam + this.nGamma + this.nPinv);
/*     */ 
/*  64 */     this.xi = new double[this.N][this.N];
/*  65 */     for (int i = 0; i < this.N; i++) {
/*  66 */       Arrays.fill(this.xi[i], 0.0D);
/*  67 */       this.xi[i][i] = 1.0D;
/*     */     }
/*  69 */     this.p = new double[this.N];
/*  70 */     this.pcom = new double[this.N];
/*  71 */     this.xicom = new double[this.N];
/*     */ 
/*  73 */     for (int k = 0; k < this.nBranch; k++) {
/*  74 */       this.p[k] = ((Branch)this.branches.get(k)).getLength();
/*     */     }
/*  76 */     for (Charset part : this.partitions) {
/*  77 */       if (this.nParam > 0) {
/*  78 */         Map rateParam = this.T.getEvaluationRateParameters(part);
/*  79 */         switch ($SWITCH_TABLE$metapiga$parameters$Parameters$EvaluationModel()[this.T.getEvaluationModel().ordinal()]) {
/*     */         case 16:
/*  81 */           this.p[(k++)] = ((Double)rateParam.get(RateParameter.A)).doubleValue();
/*  82 */           this.p[(k++)] = ((Double)rateParam.get(RateParameter.B)).doubleValue();
/*  83 */           this.p[(k++)] = ((Double)rateParam.get(RateParameter.C)).doubleValue();
/*  84 */           this.p[(k++)] = ((Double)rateParam.get(RateParameter.D)).doubleValue();
/*  85 */           this.p[(k++)] = ((Double)rateParam.get(RateParameter.E)).doubleValue();
/*  86 */           break;
/*     */         case 17:
/*  88 */           this.p[(k++)] = ((Double)rateParam.get(RateParameter.K1)).doubleValue();
/*  89 */           this.p[(k++)] = ((Double)rateParam.get(RateParameter.K2)).doubleValue();
/*  90 */           break;
/*     */         case 18:
/*  92 */           this.p[(k++)] = ((Double)rateParam.get(RateParameter.K)).doubleValue();
/*  93 */           break;
/*     */         case 19:
/*  95 */           this.p[(k++)] = ((Double)rateParam.get(RateParameter.K)).doubleValue();
/*  96 */           break;
/*     */         case 20:
/*     */         }
/*     */       }
/*     */ 
/* 101 */       if (this.nGamma > 0) {
/* 102 */         this.p[(k++)] = this.T.getEvaluationGammaShape(part);
/*     */       }
/* 104 */       if (this.nPinv > 0)
/* 105 */         this.p[(k++)] = this.T.getEvaluationPInv(part);
/*     */     }
/*     */   }
/*     */ 
/*     */   public Tree getOptimizedTreeWithProgress(JDialog owner, String title, int idBar, int maxBar)
/*     */     throws NullAncestorException, UnrootableTreeException
/*     */   {
/* 114 */     return null;
/*     */   }
/*     */ 
/*     */   public Tree getOptimizedTreeWithProgress(JDialog owner, String title) throws NullAncestorException, UnrootableTreeException
/*     */   {
/* 119 */     return getOptimizedTreeWithProgress(owner, title, 0, 1);
/*     */   }
/*     */ 
/*     */   public Tree getOptimizedTree() throws NullAncestorException, UnrootableTreeException
/*     */   {
/* 124 */     powell(1.0E-010D);
/* 125 */     updatedLikelihood(this.p);
/* 126 */     return this.T;
/*     */   }
/*     */ 
/*     */   public void stop()
/*     */   {
/*     */   }
/*     */ 
/*     */   private double updatedLikelihood(double[] newParameters)
/*     */     throws NullAncestorException, UnrootableTreeException
/*     */   {
/* 136 */     for (int k = 0; k < this.nBranch; k++) {
/* 137 */       ((Branch)this.branches.get(k)).setLength(newParameters[k]);
/* 138 */       this.T.markNodeToReEvaluate(((Branch)this.branches.get(k)).getNode());
/*     */     }
/* 140 */     for (Charset part : this.partitions) {
/* 141 */       if (this.nParam > 0) {
/* 142 */         switch ($SWITCH_TABLE$metapiga$parameters$Parameters$EvaluationModel()[this.T.getEvaluationModel().ordinal()]) {
/*     */         case 16:
/* 144 */           this.T.setEvaluationRateParameter(part, RateParameter.A, newParameters[(k++)]);
/* 145 */           this.T.setEvaluationRateParameter(part, RateParameter.B, newParameters[(k++)]);
/* 146 */           this.T.setEvaluationRateParameter(part, RateParameter.C, newParameters[(k++)]);
/* 147 */           this.T.setEvaluationRateParameter(part, RateParameter.D, newParameters[(k++)]);
/* 148 */           this.T.setEvaluationRateParameter(part, RateParameter.E, newParameters[(k++)]);
/* 149 */           break;
/*     */         case 17:
/* 151 */           this.T.setEvaluationRateParameter(part, RateParameter.K1, newParameters[(k++)]);
/* 152 */           this.T.setEvaluationRateParameter(part, RateParameter.K2, newParameters[(k++)]);
/* 153 */           break;
/*     */         case 18:
/* 155 */           this.T.setEvaluationRateParameter(part, RateParameter.K, newParameters[(k++)]);
/* 156 */           break;
/*     */         case 19:
/* 158 */           this.T.setEvaluationRateParameter(part, RateParameter.K, newParameters[(k++)]);
/* 159 */           break;
/*     */         case 20:
/*     */         }
/*     */       }
/*     */ 
/* 164 */       if (this.nGamma > 0) this.T.setEvaluationDistributionShape(part, newParameters[(k++)]);
/* 165 */       if (this.nPinv > 0) this.T.setEvaluationPInv(part, newParameters[(k++)]);
/*     */     }
/* 167 */     return this.T.getEvaluation();
/*     */   }
/*     */ 
/*     */   private void powell(double ftol)
/*     */     throws NullAncestorException, UnrootableTreeException
/*     */   {
/* 187 */     double[] pt = new double[this.N];
/* 188 */     double[] ptt = new double[this.N];
/* 189 */     double[] xit = new double[this.N];
/* 190 */     double[] pabs = new double[this.N];
/* 191 */     double fret = this.T.getEvaluation();
/* 192 */     System.arraycopy(this.p, 0, pt, 0, this.N);
/*     */ 
/* 194 */     for (int iter = 0; ; iter++)
/*     */     {
/* 196 */       double fp = fret;
/* 197 */       int ibig = 0;
/* 198 */       double del = 0.0D;
/* 199 */       for (int i = 0; i < this.N; i++) {
/* 200 */         for (int j = 0; j < this.N; j++) {
/* 201 */           xit[j] = this.xi[j][i];
/*     */         }
/* 203 */         double fptt = fret;
/* 204 */         fret = linmin(xit);
/* 205 */         if (fptt - fret > del) {
/* 206 */           del = Math.abs(fptt - fret);
/* 207 */           ibig = i;
/*     */         }
/*     */       }
/* 210 */       if (2.0D * (fp - fret) <= ftol * (Math.abs(fp) + Math.abs(fret)))
/*     */       {
/* 212 */         for (int k = 0; k < this.N; k++) {
/* 213 */           this.p[k] = Math.abs(this.p[k]);
/*     */         }
/* 215 */         return;
/*     */       }
/* 217 */       if (iter == 200) System.out.println("powell exceeding maximum iterations");
/* 218 */       for (int j = 0; j < this.N; j++) {
/* 219 */         ptt[j] = (2.0D * this.p[j] - pt[j]);
/* 220 */         xit[j] = (this.p[j] - pt[j]);
/* 221 */         pt[j] = this.p[j];
/*     */       }
/* 223 */       for (int j = 0; j < this.N; j++) {
/* 224 */         pabs[j] = Math.abs(ptt[j]);
/*     */       }
/* 226 */       double fptt = updatedLikelihood(pabs);
/*     */ 
/* 228 */       if (fptt < fp) {
/* 229 */         double t = 2.0D * (fp - 2.0D * fret + fptt) * Math.pow(fp - fret - del, 2.0D) - del * Math.pow(fp - fptt, 2.0D);
/* 230 */         if (t < 0.0D) {
/* 231 */           fret = linmin(xit);
/*     */ 
/* 233 */           for (int j = 0; j < this.N; j++) {
/* 234 */             this.xi[j][ibig] = this.xi[j][(this.N - 1)];
/* 235 */             this.xi[j][(this.N - 1)] = xit[j];
/*     */           }
/*     */         }
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   private double linmin(double[] xit)
/*     */     throws NullAncestorException, UnrootableTreeException
/*     */   {
/* 254 */     System.arraycopy(this.p, 0, this.pcom, 0, this.N);
/* 255 */     System.arraycopy(xit, 0, this.xicom, 0, this.N);
/* 256 */     double[] brent = brent(mnbrak2());
/* 257 */     double xmin = brent[0];
/* 258 */     for (int i = 0; i < this.N; i++) {
/* 259 */       xit[i] = (xmin * xit[i]);
/* 260 */       this.p[i] += xit[i];
/*     */     }
/* 262 */     return brent[1];
/*     */   }
/*     */ 
/*     */   private double f1dim(double x) throws NullAncestorException, UnrootableTreeException {
/* 266 */     double[] xt = new double[this.N];
/* 267 */     double[] pabs = new double[this.N];
/* 268 */     for (int j = 0; j < this.N; j++) {
/* 269 */       xt[j] = (this.pcom[j] + x * this.xicom[j]);
/*     */     }
/* 271 */     for (int j = 0; j < this.N; j++) {
/* 272 */       pabs[j] = Math.abs(xt[j]);
/*     */     }
/* 274 */     return updatedLikelihood(pabs);
/*     */   }
/*     */ 
/*     */   private double[] mnbrak2()
/*     */     throws NullAncestorException, UnrootableTreeException
/*     */   {
/* 286 */     double ax = 0.0D;
/* 287 */     double bx = 1.0D;
/*     */ 
/* 289 */     double fa = f1dim(ax);
/* 290 */     double fb = f1dim(bx);
/*     */ 
/* 292 */     if (fb > fa) {
/* 293 */       double dum = ax;
/* 294 */       ax = bx;
/* 295 */       bx = dum;
/* 296 */       dum = fb;
/* 297 */       fb = fa;
/* 298 */       fa = dum;
/*     */     }
/* 300 */     double cx = bx + 1.618034D * (bx - ax);
/* 301 */     double fc = f1dim(cx);
/* 302 */     while (fb > fc)
/*     */     {
/* 305 */       double r = (bx - ax) * (fb - fc);
/*     */ 
/* 307 */       double q = (bx - cx) * (fb - fa);
/* 308 */       double u = bx - ((bx - cx) * q - (bx - ax) * r) / (2.0D * sign(Math.max(Math.abs(q - r), 1.0E-020D), q - r));
/* 309 */       double ulim = bx + 100.0D * (cx - bx);
/*     */       double fu;
/* 311 */       if ((bx - u) * (u - cx) > 0.0D) {
/* 312 */         double fu = f1dim(u);
/* 313 */         if (fu < fc) {
/* 314 */           ax = bx;
/* 315 */           fa = fb;
/* 316 */           bx = u;
/* 317 */           fb = fu;
/*     */ 
/* 319 */           return new double[] { ax, bx, cx };
/* 320 */         }if (fu > fb) {
/* 321 */           cx = u;
/* 322 */           fc = fu;
/*     */ 
/* 324 */           return new double[] { ax, bx, cx };
/*     */         }
/* 326 */         u = cx + 1.618034D * (cx - bx);
/* 327 */         fu = f1dim(u);
/* 328 */       } else if ((cx - u) * (u - ulim) > 0.0D) {
/* 329 */         double fu = f1dim(u);
/* 330 */         if (fu < fc) {
/* 331 */           bx = cx;
/* 332 */           cx = u;
/* 333 */           u = cx + 1.618034D * (cx - bx);
/* 334 */           fb = fc;
/* 335 */           fc = fu;
/* 336 */           fu = f1dim(u);
/*     */         }
/*     */       }
/*     */       else
/*     */       {
/*     */         double fu;
/* 338 */         if ((u - ulim) * (ulim - cx) >= 0.0D) {
/* 339 */           u = ulim;
/* 340 */           fu = f1dim(u);
/*     */         } else {
/* 342 */           u = cx + 1.618034D * (cx - bx);
/* 343 */           fu = f1dim(u);
/*     */         }
/*     */       }
/* 345 */       ax = bx;
/* 346 */       bx = cx;
/* 347 */       cx = u;
/* 348 */       fa = fb;
/* 349 */       fb = fc;
/* 350 */       fc = fu;
/*     */     }
/*     */ 
/* 353 */     return new double[] { ax, bx, cx };
/*     */   }
/*     */ 
/*     */   private double[] brent(double[] xs)
/*     */     throws NullAncestorException, UnrootableTreeException
/*     */   {
/* 369 */     double ax = xs[0];
/* 370 */     double bx = xs[1];
/* 371 */     double cx = xs[2];
/* 372 */     double a = Math.min(ax, cx);
/* 373 */     double b = Math.max(ax, cx);
/* 374 */     double v = bx;
/* 375 */     double w = v;
/* 376 */     double x = v;
/* 377 */     double e = 0.0D;
/* 378 */     double fx = f1dim(x);
/* 379 */     double fv = fx;
/* 380 */     double fw = fx;
/* 381 */     double d = 0.0D;
/* 382 */     for (int iter = 0; iter < 100; iter++)
/*     */     {
/* 384 */       double xm = 0.5D * (a + b);
/* 385 */       double tol1 = 0.0001D * Math.abs(x) + 1.0E-010D;
/* 386 */       double tol2 = 2.0D * tol1;
/*     */ 
/* 388 */       if (Math.abs(x - xm) <= tol2 - 0.5D * (b - a))
/*     */       {
/* 390 */         return new double[] { x, fx };
/*     */       }
/* 392 */       if (Math.abs(e) > tol1) {
/* 393 */         double r = (x - w) * (fx - fv);
/* 394 */         double q = (x - v) * (fx - fw);
/* 395 */         double p = (x - v) * q - (x - w) * r;
/* 396 */         q = 2.0D * (q - r);
/* 397 */         if (q > 0.0D) p = -p;
/* 398 */         q = Math.abs(q);
/* 399 */         double etemp = e;
/* 400 */         e = d;
/* 401 */         if ((Math.abs(p) >= Math.abs(0.5D * q * etemp)) || (p <= q * (a - x)) || (p >= q * (b - x)))
/*     */         {
/* 403 */           if (x >= xm)
/* 404 */             e = a - x;
/*     */           else {
/* 406 */             e = b - x;
/*     */           }
/* 408 */           d = 0.381966D * e;
/*     */         }
/*     */         else
/*     */         {
/* 412 */           d = p / q;
/* 413 */           double u = x + d;
/* 414 */           if ((u - a < tol2) || (b - u < tol2)) d = sign(tol1, xm - x);
/*     */         }
/*     */       }
/*     */       double u;
/*     */       double u;
/* 417 */       if (Math.abs(d) >= tol1)
/* 418 */         u = x + d;
/*     */       else {
/* 420 */         u = x + sign(tol1, d);
/*     */       }
/* 422 */       double fu = f1dim(u);
/* 423 */       if (fu <= fx) {
/* 424 */         if (u >= x)
/* 425 */           a = x;
/*     */         else
/* 427 */           b = x;
/* 428 */         v = w;
/* 429 */         fv = fw;
/* 430 */         w = x;
/* 431 */         fw = fx;
/* 432 */         x = u;
/* 433 */         fx = fu;
/*     */       }
/*     */       else {
/* 436 */         if (u < x)
/* 437 */           a = u;
/*     */         else {
/* 439 */           b = u;
/*     */         }
/* 441 */         if ((fu <= fw) || (w == x)) {
/* 442 */           v = w;
/* 443 */           fv = fw;
/* 444 */           w = u;
/* 445 */           fw = fu;
/* 446 */         } else if ((fu <= fv) || (v == x) || (v == w)) {
/* 447 */           v = u;
/* 448 */           fv = fu;
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 453 */     return new double[] { x, fx };
/*     */   }
/*     */ 
/*     */   private double sign(double x, double y) {
/* 457 */     if (y >= 0.0D) {
/* 458 */       return Math.abs(x);
/*     */     }
/* 460 */     return -Math.abs(x);
/*     */   }
/*     */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.optimization.Powell
 * JD-Core Version:    0.6.2
 */