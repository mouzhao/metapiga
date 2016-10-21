/*      */ package metapiga.optimization;
/*      */ 
/*      */ import java.awt.Point;
/*      */
/*      */ import javax.swing.JDialog;
/*      */ import metapiga.trees.Tree;
/*      */ import metapiga.trees.exceptions.NullAncestorException;
/*      */ import metapiga.trees.exceptions.UnrootableTreeException;
/*      */ 
/*      */ public class DFO
/*      */   implements Optimizer
/*      */ {
/*   21 */   private final int MXSPIT = 10;
/*   22 */   private final int GQTIT = 100;
/*   23 */   private final double ZERO = 0.0D; private final double ONE = 1.0D; private final double EPS = 0.0001D; private final double TEN = 10.0D; private final double TENTH = 0.1D; private final double HUGE = 1.0E+020D; private final double THRVLD = 1.5D;
/*   24 */   private final double ETA0 = 0.01D; private final double ETA1 = 0.95D; private final double GAMMathExp = 2.0D; private final double GAMDIV = 1.75D;
/*      */   private double MINRD;
/*      */   private double SMPIV;
/*      */   private double NFPMAX;
/*      */   private double GFDMAX;
/*   34 */   private double ERRMAX = -1.0D;
/*      */   private int NARCH;
/*      */   private int MXARCH;
/*      */   private int NF;
/*      */   private int MAXNF;
/*      */   private int NP;
/*      */   private int NIT;
/*      */   private int NFEVAL;
/*      */ 
/*      */   public DFO()
/*      */   {
/*   52 */     int NMAX = 100;
/*   53 */     int N = 2;
/*   54 */     int NBX = 0;
/*   55 */     double[] X = new double[100];
/*   56 */     X[0] = 1.0D;
/*   57 */     X[1] = 2.0D;
/*   58 */     double[] PAR = new double[10];
/*   59 */     PAR[0] = -1.0D;
/*   60 */     PAR[1] = -1.0D;
/*   61 */     PAR[2] = 0.0D;
/*   62 */     PAR[3] = -1.0D;
/*   63 */     PAR[4] = -1.0D;
/*   64 */     PAR[5] = -1.0D;
/*   65 */     PAR[6] = -1.0D;
/*   66 */     PAR[7] = -1.0D;
/*   67 */     PAR[8] = -1.0D;
/*   68 */     PAR[9] = -1.0D;
/*   69 */     int[] IPAR = new int[5];
/*   70 */     IPAR[0] = 1000;
/*   71 */     IPAR[1] = 1000;
/*   72 */     IPAR[2] = 0;
/*   73 */     IPAR[3] = 100;
/*   74 */     IPAR[4] = 0;
/*   75 */     int[] HESSTR = new int[1];
/*   76 */     int LW = 160000;
/*   77 */     double[] W = new double[160000];
/*   78 */     double FX = UFN(N, X, 0);
/*      */     try {
/*   80 */       UDFO(N, NBX, X, FX, PAR, IPAR, HESSTR, 160000, W);
/*   81 */       System.out.println("Optimum  = " + UFN(N, X, 0) + " in (" + X[0] + "," + X[1] + ") found in " + this.NIT + " iteration and " + this.NFEVAL + " function evaluations");
/*      */     } catch (OptimizationError oe) {
/*   83 */       oe.printStackTrace();
/*      */     }
/*      */   }
/*      */ 
/*      */   public Tree getOptimizedTreeWithProgress(JDialog owner, String title, int idBar, int maxBar)
/*      */     throws NullAncestorException, UnrootableTreeException
/*      */   {
/*   91 */     return null;
/*      */   }
/*      */ 
/*      */   public Tree getOptimizedTreeWithProgress(JDialog owner, String title) throws NullAncestorException, UnrootableTreeException
/*      */   {
/*   96 */     return getOptimizedTreeWithProgress(owner, title, 0, 1);
/*      */   }
/*      */ 
/*      */   public void stop()
/*      */   {
/*      */   }
/*      */ 
/*      */   public Tree getOptimizedTree()
/*      */     throws NullAncestorException, UnrootableTreeException
/*      */   {
/*  108 */     return null;
/*      */   }
/*      */ 
/*      */   private void UDFO(int N, int NBX, double[] X, double FX, double[] PAR, int[] IPAR, int[] HESSTR, int LW, double[] W)
/*      */     throws OptimizationError
/*      */   {
/*  518 */     int LYS = -1;
/*      */ 
/*  523 */     char[] ITINFO = new char[3];
/*      */ 
/*  533 */     double DELTA = PAR[0];
/*  534 */     if (DELTA < 0.0D) DELTA = 1.0D;
/*      */ 
/*  537 */     double GXTOL = PAR[1];
/*  538 */     if (GXTOL < 0.0D) GXTOL = 0.001D;
/*  539 */     this.MINRD = (0.01D * GXTOL);
/*      */ 
/*  542 */     double SNTOL = PAR[2];
/*  543 */     if (SNTOL < 0.0D) SNTOL = 1.0E-012D;
/*      */ 
/*  546 */     double RTOL = PAR[6];
/*  547 */     double ATOL = PAR[8];
/*  548 */     if (RTOL < 0.0D) RTOL = 0.1D;
/*  549 */     if (ATOL < 0.0D) ATOL = 0.1D;
/*      */ 
/*  552 */     double LWBND = PAR[8];
/*  553 */     double LWBTOL = Math.min(0.9999D, PAR[9]);
/*  554 */     if (LWBTOL < 0.0D) LWBTOL = 1.0E-008D;
/*      */     double FXLOW;
/*      */     double FXLOW;
/*  557 */     if (LWBND == 0.0D) {
/*  558 */       FXLOW = LWBTOL;
/*      */     }
/*      */     else
/*      */     {
/*      */       double FXLOW;
/*  559 */       if (LWBND < 0.0D)
/*  560 */         FXLOW = (1.0D - LWBTOL) * LWBND;
/*      */       else {
/*  562 */         FXLOW = (1.0D + LWBTOL) * LWBND;
/*      */       }
/*      */     }
/*      */ 
/*  566 */     this.MAXNF = IPAR[0];
/*  567 */     if (this.MAXNF < 0) this.MAXNF = 1000;
/*      */ 
/*  570 */     int ITMAX = IPAR[1];
/*  571 */     if (ITMAX < 0) ITMAX = 1000;
/*      */ 
/*  579 */     int NP1 = N + 1;
/*  580 */     int NH = IPAR[2];
/*      */     char HTYPE;
/*      */     int LP;
/*  581 */     if (NH > 0) {
/*  582 */       if (NH > N * (N - 1) / 2) {
/*  583 */         throw new OptimizationError("Error: IPAR[2] = " + IPAR[2] + " EXCEEDS N!. \nThe number of nonzero entries specified by the user in the strictly lower triangular part of the objective Hessian (i.e. the number of pairs\tof indices in HESSTR) exceeds the size of this part (only possible if IPAR[2] > 0).");
/*      */       }
/*  585 */       char HTYPE = 'S';
/*  586 */       int LP = N + N + NH;
/*  587 */       for (int I = 0; I < NH + NH; I += 2) {
/*  588 */         int IH = HESSTR[I];
/*  589 */         int IS = HESSTR[(I + 1)];
/*  590 */         if ((IH < 0) || (IH > N) || (IS < 0) || (IS > N)) {
/*  591 */           throw new OptimizationError("Error: THE " + I + "-TH PAIR IN HESSTR IS OUT OF RANGE!. \nThe position (in HESSTR) of a nonzero entry in the strictly lower triangular part of the Hessian has indices that are out of range (from 0 to N-1) (only possible if IPAR[2] > 0).");
/*      */         }
/*  593 */         if (IH < IS) {
/*  594 */           throw new OptimizationError("Error: THE " + I + "-TH PAIR IN HESSTR IS IN THE UPPER TRIANGLE!. \nThe position of a nonzero entry in the strict lower triangular part of the Hessian corresponds to an entry in the upper triangular part (only possible if IPAR[2] > 0).");
/*      */         }
/*  596 */         for (int IW1 = 0; IW1 < I - 1; IW1 += 2)
/*  597 */           if ((IH == HESSTR[IW1]) && (IS == HESSTR[(IW1 + 1)]))
/*  598 */             throw new OptimizationError("Error: THE " + I + "-TH PAIR IN HESSTR ALREADY OCCURS AS THE " + IW1 + "-TH PAIR!. \nThe position of a nonzero entry in the strict lower triangular part occurs more than once in HESSTR (only possible if IPAR[2] > 0).");
/*      */       }
/*      */     }
/*      */     else
/*      */     {
/*      */       int LP;
/*  603 */       if (NH < 0) {
/*  604 */         char HTYPE = 'B';
/*  605 */         NH = -NH;
/*  606 */         LP = N + NH * N - NH * (NH - 1) / 2;
/*      */       } else {
/*  608 */         HTYPE = 'D';
/*  609 */         LP = NP1 * (N + 2) / 2 - 1;
/*      */       }
/*      */     }
/*      */ 
/*  613 */     this.NARCH = NBX;
/*  614 */     double GAMLIM = Math.max(10, LP) * 1.75D;
/*      */ 
/*  622 */     int MXNUNS = 3 * N;
/*      */ 
/*  630 */     this.NARCH = Math.max(this.NARCH, 0);
/*  631 */     this.NF = 0;
/*  632 */     if (this.NARCH >= LW) {
/*  633 */       throw new OptimizationError("Error: LW TOO SHORT OF " + (LW - this.NARCH) + "!\nThe value of NBX exceeds LW.");
/*      */     }
/*      */ 
/*  637 */     double DELREF = DELTA;
/*      */ 
/*  640 */     int IXSET = 1;
/*  641 */     int IYSET = IXSET + LP * N;
/*  642 */     int IMODEL = IYSET + LP;
/*  643 */     int INFP = IMODEL + LP;
/*  644 */     int IDIST = INFP + N * N + LP * (LP - N);
/*  645 */     int IS = IDIST + LP;
/*  646 */     int IH = IS + N;
/*  647 */     int IW1 = IH + N * N;
/*  648 */     int IW2 = IW1 + N;
/*  649 */     int IW3 = IW2 + N;
/*  650 */     int IXARCH = IW3 + N * (N + 6);
/*  651 */     this.MXARCH = (IXARCH + Math.max(this.NARCH, 20) * NP1);
/*  652 */     if (LW < this.MXARCH) {
/*  653 */       throw new OptimizationError("Error: LW TOO SHORT OF " + (this.MXARCH - LW) + "!\nThe double precision workspace is too small");
/*      */     }
/*  655 */     this.MXARCH = ((LW - IXARCH) / NP1);
/*  656 */     int IYARCH = IXARCH + this.MXARCH * N;
/*      */ 
/*  659 */     if (this.NARCH > 0) {
/*  660 */       LYS = this.NARCH * N;
/*      */ 
/*  662 */       double FXMIN = FX;
/*  663 */       int IXMIN = 0;
/*  664 */       for (int I = LYS; I < LYS + this.NARCH; I++) {
/*  665 */         if (W[I] < FX) {
/*  666 */           FXMIN = W[I];
/*  667 */           IXMIN = I - LYS + 1;
/*      */         }
/*      */       }
/*      */ 
/*  671 */       if (IXMIN > 0) {
/*  672 */         DSWAP(N, W, (IXMIN - 1) * N, 1, X, 0, 1);
/*  673 */         W[(LYS + IXMIN - 1)] = FX;
/*  674 */         FX = FXMIN;
/*      */       }
/*      */     }
/*      */ 
/*  678 */     if (this.NARCH > LP)
/*      */     {
/*  680 */       this.NP = LP;
/*      */ 
/*  685 */       double FXMAX = -1.0E+020D;
/*  686 */       int IXMAX = 0;
/*  687 */       if (LYS < 0) throw new OptimizationError("LYS has not been initialized !");
/*  688 */       for (int I = LYS; I < LYS + LP; I++) {
/*  689 */         if (W[I] > FXMAX) {
/*  690 */           FXMAX = W[I];
/*  691 */           IXMAX = I - LYS + 1;
/*      */         }
/*      */       }
/*      */ 
/*  695 */       double FXMIN = FXMAX;
/*  696 */       int IXMIN = 0;
/*  697 */       for (int I = LYS + LP; I < LYS + this.NARCH; I++)
/*  698 */         if (W[I] < FXMIN) {
/*  699 */           FXMIN = W[I];
/*  700 */           IXMIN = I - LYS + 1;
/*      */         }
/*      */       int I;
/*  704 */       for (; IXMIN > 0; 
/*  720 */         I < LYS + this.NARCH)
/*      */       {
/*  705 */         DSWAP(N, W, (IXMAX - 1) * N, 1, W, (IXMIN - 1) * N, 1);
/*  706 */         W[(LYS + IXMAX - 1)] = FXMIN;
/*  707 */         W[(LYS + IXMIN - 1)] = FXMAX;
/*      */ 
/*  709 */         FXMAX = -1.0E+020D;
/*  710 */         IXMAX = 0;
/*  711 */         for (int I = LYS; I < LYS + LP; I++) {
/*  712 */           if (W[I] > FXMAX) {
/*  713 */             FXMAX = W[I];
/*  714 */             IXMAX = I - LYS + 1;
/*      */           }
/*      */         }
/*      */ 
/*  718 */         FXMIN = FXMAX;
/*  719 */         IXMIN = 0;
/*  720 */         I = LYS + LP; continue;
/*  721 */         if (W[I] < FXMIN) {
/*  722 */           FXMIN = W[I];
/*  723 */           IXMIN = I - LYS + 1;
/*      */         }
/*  720 */         I++;
/*      */       }
/*      */ 
/*  733 */       int IXS = this.NARCH;
/*  734 */       this.NARCH = (IXARCH / NP1);
/*  735 */       if (this.NARCH < IXS)
/*  736 */         System.arraycopy(W, N * IXS, W, N * this.NARCH, this.NARCH);
/*      */     }
/*      */     else
/*      */     {
/*  740 */       this.NP = this.NARCH;
/*      */     }
/*      */ 
/*  743 */     if (this.NP <= 0) {
/*  744 */       LYS = N;
/*  745 */       double TMP = N;
/*  746 */       double RHO = DELTA / Math.sqrt(TMP);
/*  747 */       for (int i = 0; i < N; i++) {
/*  748 */         X[i] += RHO;
/*      */       }
/*      */ 
/*  751 */       this.NF += 1;
/*  752 */       if (this.NF <= this.MAXNF)
/*  753 */         W[(NP1 - 1)] = UFN(N, W, 0);
/*      */       else {
/*  755 */         throw new OptimizationError("Error: TOO MANY FUNCTION CALLS! NF = " + this.MAXNF + "\nThe maximum number of function calls has been reached and optimization stopped");
/*      */       }
/*  757 */       this.NP = 1;
/*  758 */       this.NARCH = 1;
/*      */ 
/*  760 */       if (W[(NP1 - 1)] < FX) {
/*  761 */         DSWAP(N, X, 0, 1, W, 0, 1);
/*  762 */         TMP = W[(NP1 - 1)];
/*  763 */         W[(NP1 - 1)] = FX;
/*  764 */         FX = TMP;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  772 */     int IXN = IXARCH;
/*  773 */     int IXS = 1 + this.NP * N;
/*  774 */     if (LYS < 0) throw new OptimizationError("LYS has not been initialized !");
/*  775 */     for (int i = 0; i < this.NARCH - this.NP; i++) {
/*  776 */       System.arraycopy(W, IXS - 1, W, IXN - 1, N);
/*  777 */       W[(IYARCH + i - 1)] = W[(LYS + i)];
/*  778 */       IXN += N;
/*  779 */       IXS += N;
/*      */     }
/*  781 */     this.NARCH -= this.NP;
/*      */ 
/*  786 */     if (this.NP == LP) {
/*  787 */       for (int i = 0; i < this.NP; i++)
/*  788 */         W[(IYSET + i - 1)] = W[(LYS + i)];
/*      */     }
/*      */     else {
/*  791 */       for (int i = this.NP; i > 0; i--) {
/*  792 */         W[(IYSET + i - 1)] = W[(LYS + i)];
/*      */       }
/*      */     }
/*      */ 
/*  796 */     double DMAX = UPDIST(N, W, IXSET - 1, X, W, IDIST - 1);
/*      */ 
/*  798 */     boolean SUCCES = true;
/*  799 */     int NUNS = 0;
/*  800 */     double SNORM = DELTA;
/*  801 */     boolean VALID = false;
/*  802 */     double RADOK = 1.0E+020D;
/*      */ 
/*  809 */     for (int IT = 1; IT <= ITMAX; IT++)
/*      */     {
/*  811 */       if (FX <= FXLOW) {
/*  812 */         this.NIT = (IT - 1);
/*  813 */         this.NFEVAL = this.NF;
/*  814 */         return;
/*      */       }
/*  816 */       ITINFO[0] = ' ';
/*  817 */       ITINFO[1] = ' ';
/*  818 */       ITINFO[2] = ' ';
/*      */ 
/*  823 */       if ((SUCCES) && (this.NP >= N)) {
/*  824 */         GETNFP(N, LP, W, IXSET - 1, W, IYSET - 1, 0, X, FX, W, IDIST - 1, DELTA, -1.0D, W, INFP - 1, W, IXARCH - 1, W, IYARCH - 1, HTYPE, NH, HESSTR, W, IW1 - 1);
/*      */       }
/*      */       else
/*      */       {
/*  828 */         GETNFP(N, LP, W, IXSET - 1, W, IYSET - 1, N - 1, X, FX, W, IDIST - 1, DELTA, -1.0D, W, INFP - 1, W, IXARCH - 1, W, IYARCH - 1, HTYPE, NH, HESSTR, W, IW1 - 1);
/*      */       }double PRERED;
/*      */       boolean doitagain;
/*      */       do {
/*  833 */         INTERP(N, LP, W, IXSET - 1, W, IYSET - 1, X, FX, W, IMODEL - 1, W, INFP - 1, HTYPE, NH, HESSTR, W, IW1 - 1);
/*      */ 
/*  835 */         double GNORM = DNRM2(N, W, IMODEL - 1, 1);
/*      */ 
/*  840 */         double VRAD = Math.max(this.MINRD, Math.min(GXTOL, DELTA));
/*      */ 
/*  842 */         if ((GNORM <= GXTOL) || ((!VALID) && (NUNS > MXNUNS))) {
/*  843 */           if (GNORM <= GXTOL)
/*  844 */             ITINFO[0] = 'V';
/*      */           else
/*  846 */             ITINFO[0] = 'U';
/*      */           boolean doitagain;
/*      */           do
/*      */           {
/*  850 */             doitagain = false;
/*  851 */             GETNFP(N, LP, W, IXSET - 1, W, IYSET - 1, LP, X, FX, W, IDIST - 1, VRAD, 1.5D, W, INFP - 1, W, IXARCH - 1, W, IYARCH - 1, HTYPE, NH, HESSTR, W, IW1 - 1);
/*  852 */             INTERP(N, LP, W, IXSET - 1, W, IYSET - 1, X, FX, W, IMODEL - 1, W, INFP - 1, HTYPE, NH, HESSTR, W, IW1 - 1);
/*  853 */             VALID = true;
/*  854 */             RADOK = VRAD;
/*      */ 
/*  856 */             GNORM = DNRM2(N, W, IMODEL - 1, 1);
/*  857 */             if (GNORM <= GXTOL) {
/*  858 */               this.NIT = (IT - 1);
/*  859 */               this.NFEVAL = this.NF;
/*  860 */               return;
/*      */             }
/*      */ 
/*  863 */             VRAD *= 0.1D;
/*  864 */             if (GNORM <= VRAD)
/*  865 */               doitagain = true;
/*      */           }
/*  849 */           while (
/*  868 */             doitagain);
/*      */         }
/*      */ 
/*  874 */         MKHESS(N, W, IMODEL - 1, LP, W, IH - 1, HTYPE, NH, HESSTR);
/*      */ 
/*  876 */         double LAMBDA = 0.0D;
/*  877 */         DGQToutput dgqt = DGQT(N, W, IH - 1, N, W, IMODEL - 1, DELTA, RTOL, ATOL, 10, LAMBDA, W, IS - 1, 100, W, IW1 - 1, W, IW2 - 1, W, IW3 - 1);
/*  878 */         LAMBDA = dgqt.PAR;
/*  879 */         PRERED = dgqt.F;
/*      */ 
/*  881 */         PRERED = -PRERED;
/*  882 */         SNORM = DNRM2(N, W, IS - 1, 1);
/*      */ 
/*  892 */         doitagain = false;
/*  893 */         if (this.ERRMAX < 0.0D) throw new OptimizationError("ERRMAX has not been initialized !");
/*  894 */         if (((this.ERRMAX >= 0.1D * PRERED) || (SNORM <= SNTOL)) && (!VALID)) {
/*  895 */           doitagain = true;
/*  896 */           GETNFP(N, LP, W, IXSET - 1, W, IYSET - 1, LP, X, FX, W, IDIST - 1, DELTA, 0.0D, W, INFP - 1, W, IXARCH - 1, W, IYARCH - 1, HTYPE, NH, HESSTR, W, IW1 - 1);
/*  897 */           VALID = true;
/*  898 */           RADOK = DELTA;
/*  899 */           ITINFO[0] = 'R';
/*      */         }
/*      */       }
/*  832 */       while (
/*  901 */         doitagain);
/*      */ 
/*  903 */       if (SNORM <= SNTOL) {
/*  904 */         this.NIT = (IT - 1);
/*  905 */         this.NFEVAL = this.NF;
/*  906 */         throw new OptimizationError("Error: STEP TOO SHORT: " + SNORM + "!\nThe length of the predicted step is too short, indicating that the algorithm stalls. Optimization has been terminated.");
/*      */       }
/*      */ 
/*  910 */       System.arraycopy(X, 0, W, IW1 - 1, N);
/*  911 */       DAXPY(N, 1.0D, W, IS - 1, 1, W, IW1 - 1, 1);
/*      */ 
/*  913 */       System.arraycopy(X, 0, W, IS - 1, N);
/*      */ 
/*  915 */       double FXOLD = FX;
/*  916 */       this.NF += 1;
/*      */       double FXT;
/*  917 */       if (this.NF <= this.MAXNF)
/*  918 */         FXT = UFN(N, W, IW1 - 1);
/*      */       else
/*  920 */         throw new OptimizationError("Error: TOO MANY FUNCTION CALLS! NF = " + this.MAXNF + "\nThe maximum number of function calls has been reached and optimization stopped");
/*      */       double FXT;
/*  923 */       double ARED = FXOLD - FXT;
/*  924 */       double RHO = ARED / PRERED;
/*  925 */       SUCCES = RHO >= 0.01D;
/*      */       double RADIUS;
/*      */       double RADIUS;
/*  927 */       if (SUCCES)
/*      */       {
/*  929 */         NUNS = 0;
/*      */ 
/*  931 */         DSWAP(N, W, IW1 - 1, 1, X, 0, 1);
/*  932 */         double TMP = FXT;
/*  933 */         FXT = FX;
/*  934 */         FX = TMP;
/*      */ 
/*  936 */         DMAX = UPDIST(N, W, IXSET - 1, X, W, IDIST - 1);
/*      */ 
/*  938 */         RADIUS = Math.min(SNORM, DELTA);
/*      */       } else {
/*  940 */         NUNS++;
/*      */         double RADIUS;
/*  941 */         if (VALID)
/*  942 */           RADIUS = Math.min(DELTA, SNORM) / 1.75D;
/*      */         else {
/*  944 */           RADIUS = Math.min(SNORM, DELREF / GAMLIM);
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*  949 */       ITINFO[1] = TRYRPL(N, LP, W, IXSET - 1, W, IYSET - 1, W, IW1 - 1, FXT, X, FX, W, IDIST - 1, RADIUS, W, INFP - 1, W, IXARCH - 1, W, IYARCH - 1, HTYPE, NH, HESSTR, W, IW2 - 1);
/*  950 */       if (ITINFO[1] != '-')
/*      */       {
/*  952 */         GETNFP(N, LP, W, IXSET - 1, W, IYSET - 1, 0, X, FX, W, IDIST - 1, RADIUS, -1.0D, W, INFP - 1, W, IXARCH - 1, W, IYARCH - 1, HTYPE, NH, HESSTR, W, IW1 - 1);
/*      */       }
/*      */       else
/*      */       {
/*  956 */         ARCHIV(N, W, IW1 - 1, FXT, W, IXARCH - 1, W, IYARCH - 1);
/*      */ 
/*  958 */         if (!VALID) {
/*  959 */           ITINFO[1] = IMPRVE(N, LP, W, IXSET - 1, W, IYSET - 1, X, FX, W, IDIST - 1, RADIUS, W, INFP - 1, W, IXARCH - 1, W, IYARCH - 1, HTYPE, NH, HESSTR, W, IW1 - 1);
/*  960 */           if (this.NF > this.MAXNF) {
/*  961 */             throw new OptimizationError("Error: TOO MANY FUNCTION CALLS! NF = " + this.MAXNF + "\nThe maximum number of function calls has been reached and optimization stopped");
/*      */           }
/*      */ 
/*  964 */           if (ITINFO[1] != '-') {
/*  965 */             GETNFP(N, LP, W, IXSET - 1, W, IYSET - 1, 0, X, FX, W, IDIST - 1, DELTA, -1.0D, W, INFP - 1, W, IXARCH - 1, W, IYARCH - 1, HTYPE, NH, HESSTR, W, IW1 - 1);
/*      */           }
/*      */           else {
/*  968 */             VALID = true;
/*  969 */             RADOK = RADIUS;
/*      */           }
/*      */ 
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*  980 */       double FXMIN = FX;
/*  981 */       int IXMIN = 0;
/*  982 */       for (int I = 0; I < this.NP; I++) {
/*  983 */         double TMP = W[(IYSET + I - 1)];
/*  984 */         if (TMP < FXMIN) {
/*  985 */           FXMIN = TMP;
/*  986 */           IXMIN = I + 1;
/*      */         }
/*      */       }
/*  989 */       double SNORMT = SNORM;
/*      */ 
/*  991 */       if (FXMIN < FX)
/*      */       {
/*  993 */         double AREDHT = FXOLD - FXMIN;
/*  994 */         double RHOHAT = AREDHT / PRERED;
/*      */ 
/*  996 */         SUCCES = RHOHAT > 0.01D;
/*  997 */         if (SUCCES)
/*      */         {
/*  999 */           int IM1 = IXMIN - 1;
/* 1000 */           DSWAP(N, W, IXSET + IM1 * N - 1, 1, X, 0, 1);
/* 1001 */           W[(IYSET + IM1 - 1)] = FX;
/* 1002 */           FX = FXMIN;
/*      */ 
/* 1004 */           ITINFO[2] = 'I';
/*      */ 
/* 1006 */           DMAX = UPDIST(N, W, IXSET - 1, X, W, IDIST - 1);
/*      */ 
/* 1008 */           DAXPY(N, -1.0D, X, 0, 1, W, IS - 1, 1);
/* 1009 */           SNORMT = DNRM2(N, W, IS - 1, 1);
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/* 1018 */       if ((SUCCES) || (VALID)) DELREF = DELTA;
/*      */ 
/* 1021 */       if (RHO >= 0.01D) {
/* 1022 */         if (RHO > 0.95D)
/*      */         {
/* 1024 */           if (Math.abs(RHO - 1.0D) <= 1.E-005D)
/* 1025 */             DELTA = Math.max(SNORM * 100.0D, DELTA);
/*      */           else {
/* 1027 */             DELTA = Math.max(SNORM * 2.0D, DELTA);
/*      */           }
/*      */         }
/*      */ 
/*      */       }
/* 1032 */       else if (VALID)
/* 1033 */         DELTA = Math.min(DELTA, Math.max(SNORM, DMAX)) / 1.75D;
/*      */       else {
/* 1035 */         DELTA = Math.max(Math.min(SNORM, DELTA), DELREF / GAMLIM);
/*      */       }
/*      */ 
/* 1039 */       VALID = (VALID) && (!SUCCES) && (RADOK <= DELTA);
/*      */ 
/* 1041 */       SNORM = SNORMT;
/*      */     }
/*      */ 
/* 1049 */     throw new OptimizationError("Error: TOO MANY ITERATIONS!\nThe maximum number of iterations has been reached and optimization stopped");
/*      */   }
/*      */ 
/*      */   private double UFN(int N, double[] X, int startX)
/*      */   {
/* 1061 */     return Math.pow(1.0D - X[startX], 2.0D) + 100.0D * Math.pow(X[(1 + startX)] - Math.pow(X[startX], 2.0D), 2.0D);
/*      */   }
/*      */ 
/*      */   private void DSWAP(int N, double[] DX, int STARTX, int INCX, double[] DY, int STARTY, int INCY)
/*      */   {
/* 1072 */     if (N <= 0) return;
/* 1073 */     if ((INCX == 1) && (INCY == 1)) {
/* 1074 */       int M = N % 3;
/* 1075 */       if (M != 0) {
/* 1076 */         for (int I = 1; I <= M; I++) {
/* 1077 */           double DTEMP = DX[(I - 1 + STARTX)];
/* 1078 */           DX[(I - 1 + STARTX)] = DY[(I - 1 + STARTX)];
/* 1079 */           DY[(I - 1 + STARTX)] = DTEMP;
/*      */         }
/* 1081 */         if (N < 3) return;
/*      */       }
/* 1083 */       int MP1 = M + 1;
/* 1084 */       for (int I = MP1; I <= N; I += 3) {
/* 1085 */         double DTEMP = DX[(I - 1 + STARTX)];
/* 1086 */         DX[(I - 1 + STARTX)] = DY[(I - 1 + STARTX)];
/* 1087 */         DY[(I - 1 + STARTX)] = DTEMP;
/* 1088 */         DTEMP = DX[(I + STARTX)];
/* 1089 */         DX[(I + STARTX)] = DY[(I + STARTX)];
/* 1090 */         DY[(I + STARTX)] = DTEMP;
/* 1091 */         DTEMP = DX[(I + 1 + STARTX)];
/* 1092 */         DX[(I + 1 + STARTX)] = DY[(I + 1 + STARTX)];
/* 1093 */         DY[(I + 1 + STARTX)] = DTEMP;
/*      */       }
/*      */     } else {
/* 1096 */       int IX = 1;
/* 1097 */       int IY = 1;
/* 1098 */       if (INCX < 0) IX = (-N + 1) * INCX + 1;
/* 1099 */       if (INCY < 0) IY = (-N + 1) * INCY + 1;
/* 1100 */       for (int I = 1; I <= N; I++) {
/* 1101 */         double DTEMP = DX[(IX - 1 + STARTX)];
/* 1102 */         DX[(IX - 1 + STARTX)] = DY[(IY - 1 + STARTY)];
/* 1103 */         DY[(IY - 1 + STARTY)] = DTEMP;
/* 1104 */         IX += INCX;
/* 1105 */         IY += INCY;
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   private double UPDIST(int N, double[] XSET, int startXSET, double[] XBASE, double[] DIST, int startDist)
/*      */   {
/* 1156 */     double DMAX = 0.0D;
/* 1157 */     for (int i = 0; i < this.NP; i++) {
/* 1158 */       int IM1 = i;
/* 1159 */       double NRM2 = 0.0D;
/* 1160 */       for (int j = 0; j < N; j++) {
/* 1161 */         NRM2 += Math.pow(XSET[(startXSET + IM1 * N + j)] - XBASE[j], 2.0D);
/*      */       }
/* 1163 */       NRM2 = Math.sqrt(NRM2);
/* 1164 */       DIST[(startDist + i)] = NRM2;
/* 1165 */       DMAX = Math.max(DMAX, NRM2);
/*      */     }
/* 1167 */     return DMAX;
/*      */   }
/*      */ 
/*      */   private void GETNFP(int N, int LP, double[] XSET, int startXSET, double[] YSET, int startYSET, int ADDPTS, double[] XBASE, double FXBASE, double[] DIST, int startDIST, double RADIUS, double CUTOFF, double[] NFP, int startNFP, double[] XARCH, int startXARCH, double[] YARCH, int startYARCH, char HTYPE, int NH, int[] HESSTR, double[] W, int startW)
/*      */   {
/* 1345 */     int[] FIRST = new int[4];
/*      */ 
/* 1347 */     double BIGPIV = 1.0E+020D; double THRESH = 1.E-015D;
/* 1348 */     int NP1 = N + 1;
/* 1349 */     int NSQ = N * N;
/*      */ 
/* 1351 */     if (this.NP < 0)
/*      */     {
/* 1353 */       System.out.println("ERROR (GETNFP): NEGATIVE NUMBER OF INTERPOLATION POINTS!");
/* 1354 */       return;
/*      */     }
/*      */ 
/* 1357 */     if (LP < this.NP)
/*      */     {
/* 1359 */       System.out.println("ERROR (GETNFP): DECLARED LENGTH OF A POLYNOMIAL TOO SMALL!");
/* 1360 */       return;
/*      */     }
/*      */ 
/* 1363 */     FIRST[0] = 1;
/* 1364 */     if (ADDPTS > 0) {
/* 1365 */       FIRST[1] = NP1;
/* 1366 */       FIRST[2] = (LP + 1);
/*      */     } else {
/* 1368 */       FIRST[1] = (Math.min(this.NP, N) + 1);
/* 1369 */       FIRST[2] = (this.NP + 1);
/*      */     }
/*      */ 
/* 1376 */     int NXT = 1;
/* 1377 */     int NPOLD = this.NP;
/*      */ 
/* 1379 */     if (CUTOFF >= 0.0D) {
/* 1380 */       double TMP = CUTOFF * RADIUS;
/* 1381 */       this.NP = 0;
/* 1382 */       for (int IP = 1; IP <= NPOLD; IP++) {
/* 1383 */         int IX = 1 + (IP - 1) * N;
/*      */ 
/* 1385 */         if (DIST[(IP - 1 + startDIST)] > TMP) {
/* 1386 */           ARCHIV(N, XSET, IX - 1 + startXSET, YSET[(IP - 1 + startYSET)], XARCH, startXARCH, YARCH, startYARCH);
/*      */         }
/*      */         else {
/* 1389 */           this.NP += 1;
/* 1390 */           System.arraycopy(XSET, IX - 1 + startXSET, XSET, NXT - 1 + startXSET, N);
/* 1391 */           YSET[(this.NP - 1 + startYSET)] = YSET[(IP - 1 + startYSET)];
/* 1392 */           DIST[(this.NP - 1 + startDIST)] = DIST[(IP - 1 + startDIST)];
/* 1393 */           NXT += N;
/*      */         }
/*      */       }
/* 1396 */       NPOLD = this.NP;
/*      */     }
/*      */ 
/* 1399 */     NXT = LP * LP - N * (LP - N);
/* 1400 */     for (int IP = 1; IP <= NXT; IP++) {
/* 1401 */       NFP[(IP - 1 + startNFP)] = 0.0D;
/*      */     }
/* 1403 */     for (IP = 1; IP <= NSQ; IP += NP1) {
/* 1404 */       NFP[(IP - 1 + startNFP)] = 1.0D;
/*      */     }
/* 1406 */     for (IP = NSQ + N + 1; IP <= NXT; IP += LP + 1) {
/* 1407 */       NFP[(IP - 1 + startNFP)] = 1.0D;
/*      */     }
/*      */ 
/* 1419 */     int NEW = 0;
/* 1420 */     this.NP = 0;
/* 1421 */     NXT = 1;
/* 1422 */     int NXTX = 1;
/* 1423 */     this.SMPIV = 1.0E+020D;
/* 1424 */     for (int IB = 1; IB <= 2; IB++)
/* 1425 */       for (IP = FIRST[(IB - 1)]; IP <= FIRST[IB] - 1; IP++)
/*      */       {
/* 1435 */         Point FL = NFPDAT(N, IP, LP);
/* 1436 */         int JP = FL.x;
/* 1437 */         int LIP = FL.y;
/*      */ 
/* 1446 */         double TOLPIV = 0.0D;
/* 1447 */         int IPIV = NXT;
/* 1448 */         double VALPIV = 0.0D;
/* 1449 */         for (int IX = NXT; IX <= NPOLD; IX++)
/*      */         {
/* 1451 */           int JX = 1 + (IX - 1) * N;
/*      */ 
/* 1453 */           System.arraycopy(XSET, JX - 1 + startXSET, W, startW, N);
/* 1454 */           DAXPY(N, -1.0D, XBASE, 0, 1, W, startW, 1);
/*      */ 
/* 1456 */           double VAL = VALP(N, W, startW, NFP, startNFP + JP - 1, LIP, HTYPE, NH, HESSTR);
/* 1457 */           double ABSVAL = Math.abs(VAL);
/*      */ 
/* 1459 */           if (ABSVAL > TOLPIV) {
/* 1460 */             IPIV = IX;
/* 1461 */             TOLPIV = ABSVAL;
/* 1462 */             VALPIV = VAL;
/*      */           }
/*      */         }
/*      */ 
/* 1466 */         if (TOLPIV >= 1.E-015D)
/*      */         {
/* 1468 */           if (IPIV > NXT) {
/* 1469 */             DSWAP(N, XSET, startXSET + (IPIV - 1) * N, 1, XSET, NXTX - 1 + startXSET, 1);
/* 1470 */             double VAL = YSET[(NXT - 1 + startYSET)];
/* 1471 */             YSET[(NXT - 1 + startYSET)] = YSET[(IPIV - 1 + startYSET)];
/* 1472 */             YSET[(IPIV - 1 + startYSET)] = VAL;
/*      */           }
/*      */ 
/* 1475 */           System.arraycopy(XSET, NXTX - 1 + startXSET, W, startW, N);
/* 1476 */           DAXPY(N, -1.0D, XBASE, 0, 1, W, startW, 1);
/*      */         }
/*      */         else
/*      */         {
/* 1481 */           if (NEW == 0) {
/* 1482 */             for (IX = NXT; IX <= this.NP; IX++) {
/* 1483 */               ARCHIV(N, XSET, startXSET + (IX - 1) * N, YSET[(IX - 1 + startYSET)], XARCH, startXARCH, YARCH, startYARCH);
/*      */             }
/*      */           }
/*      */ 
/* 1487 */           if (NEW < ADDPTS)
/*      */           {
/* 1489 */             NEW++;
/*      */ 
/* 1496 */             VALPIV = MAXABS(N, RADIUS, NFP, JP - 1, LIP, XSET, NXTX - 1, HTYPE, NH, HESSTR, W, 0);
/* 1497 */             TOLPIV = Math.abs(VALPIV);
/* 1498 */             if (TOLPIV >= 1.E-015D)
/*      */             {
/* 1500 */               System.arraycopy(XSET, NXTX - 1 + startXSET, W, startW, N);
/* 1501 */               DAXPY(N, 1.0D, XBASE, 0, 1, XSET, NXTX - 1 + startXSET, 1);
/*      */ 
/* 1503 */               DIST[(NXT - 1 + startDIST)] = DNRM2(N, W, startW, 1);
/*      */ 
/* 1505 */               this.NF += 1;
/* 1506 */               if (this.NF > this.MAXNF) return;
/* 1507 */               YSET[(NXT - 1 + startYSET)] = UFN(N, XSET, NXTX - 1 + startXSET);
/*      */             }
/*      */           }
/*      */           else
/*      */           {
/* 1512 */             return;
/*      */           }
/*      */         }
/*      */ 
/* 1516 */         if (TOLPIV < this.SMPIV) {
/* 1517 */           this.SMPIV = TOLPIV;
/*      */         }
/*      */ 
/* 1520 */         DSCAL(LIP, 1.0D / VALPIV, NFP, JP - 1 + startNFP, 1);
/* 1521 */         for (int KP = FIRST[(IB - 1)]; KP <= LP; KP++) {
/* 1522 */           if (KP != IP) {
/* 1523 */             Point point = NFPDAT(N, KP, LP);
/* 1524 */             int KX = point.x;
/* 1525 */             int LKP = point.y;
/* 1526 */             double TMP = VALP(N, W, startW, NFP, KX - 1 + startNFP, LKP, HTYPE, NH, HESSTR);
/* 1527 */             DAXPY(LIP, -TMP, NFP, JP - 1 + startNFP, 1, NFP, KX - 1 + startNFP, 1);
/*      */           }
/*      */         }
/*      */ 
/* 1531 */         this.NP = NXT;
/* 1532 */         NXT++;
/* 1533 */         NXTX += N;
/*      */       }
/*      */   }
/*      */ 
/*      */   private void INTERP(int N, int LP, double[] XSET, int startXSET, double[] YSET, int startYSET, double[] XBASE, double FXBASE, double[] POL, int startPOL, double[] NFP, int startNFP, char HTYPE, int NH, int[] HESSTR, double[] W, int startW)
/*      */   {
/* 1648 */     int[] FIRST = new int[4];
/*      */ 
/* 1651 */     int NPP1 = this.NP + 1;
/* 1652 */     FIRST[0] = 1;
/* 1653 */     FIRST[1] = (N + 1);
/* 1654 */     FIRST[2] = NPP1;
/*      */     int NB;
/*      */     int NB;
/* 1655 */     if (this.NP >= FIRST[1])
/* 1656 */       NB = 2;
/*      */     else {
/* 1658 */       NB = 1;
/*      */     }
/*      */ 
/* 1665 */     for (int IP = 1; IP <= this.NP; IP++) {
/* 1666 */       YSET[(IP - 1 + startW)] -= FXBASE;
/*      */     }
/*      */ 
/* 1672 */     this.NFPMAX = 0.0D;
/* 1673 */     this.GFDMAX = 0.0D;
/* 1674 */     for (int IB = 2; IB <= NB; IB++) {
/* 1675 */       int IBP1 = IB + 1;
/* 1676 */       int ISTR = FIRST[IB];
/* 1677 */       int IEND = FIRST[(IB - 1)] - 1;
/* 1678 */       int NXTI = 1 + IEND * N;
/* 1679 */       for (IP = FIRST[(IB - 1)]; IP <= this.NP; IP++) {
/* 1680 */         System.arraycopy(XSET, NXTI - 1 + startXSET, W, NPP1 - 1 + startW, N);
/* 1681 */         DAXPY(N, -1.0D, XBASE, 0, 1, W, NPP1 - 1 + startW, 1);
/* 1682 */         NXTI += N;
/* 1683 */         for (int KP = ISTR; KP <= IEND; KP++) {
/* 1684 */           Point point = NFPDAT(N, KP, LP);
/* 1685 */           int NXTK = point.x;
/* 1686 */           int LKP = point.y;
/* 1687 */           double VAL = VALP(N, W, NPP1 - 1 + startW, NFP, NXTK - 1 + startNFP, LKP, HTYPE, NH, HESSTR);
/* 1688 */           W[(IP - 1 + startW)] -= W[(KP - 1 + startW)] * VAL;
/* 1689 */           if ((IP >= FIRST[(IB - 1)]) && (IP < FIRST[(IBP1 - 1)])) this.NFPMAX = Math.max(this.NFPMAX, Math.abs(VAL));
/*      */         }
/* 1691 */         this.GFDMAX = Math.max(Math.abs(W[(IP - 1 + startW)]), this.GFDMAX);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1699 */     for (IP = 1; IP <= LP; IP++) {
/* 1700 */       POL[(IP - 1 + startPOL)] = 0.0D;
/*      */     }
/* 1702 */     for (IP = 1; IP <= this.NP; IP++) {
/* 1703 */       Point p = NFPDAT(N, IP, LP);
/* 1704 */       int NXTI = p.x;
/* 1705 */       int LIP = p.y;
/* 1706 */       DAXPY(LIP, W[(IP - 1 + startW)], NFP, NXTI - 1 + startNFP, 1, POL, startPOL, 1);
/*      */     }
/*      */ 
/* 1711 */     this.ERRMAX = 0.0D;
/* 1712 */     for (IP = 1; IP <= this.NP; IP++) {
/* 1713 */       System.arraycopy(XSET, startXSET + (IP - 1) * N, W, NPP1 - 1 + startW, N);
/* 1714 */       DAXPY(N, -1.0D, XBASE, 0, 1, W, NPP1 - 1 + startW, 1);
/* 1715 */       this.ERRMAX = Math.max(this.ERRMAX, Math.abs(YSET[(IP - 1)] - FXBASE - VALP(N, W, NPP1 - 1 + startW, POL, startPOL, LP, HTYPE, NH, HESSTR)));
/*      */     }
/*      */   }
/*      */ 
/*      */   private double DNRM2(int N, double[] X, int startX, int INCX)
/*      */   {
/*      */     double NORM;
/*      */     double NORM;
/* 1733 */     if ((N < 1) || (INCX < 1)) {
/* 1734 */       NORM = 0.0D;
/*      */     }
/*      */     else
/*      */     {
/*      */       double NORM;
/* 1735 */       if (N == 1) {
/* 1736 */         NORM = Math.abs(X[startX]);
/*      */       } else {
/* 1738 */         double SCALE = 0.0D;
/* 1739 */         double SSQ = 1.0D;
/*      */ 
/* 1741 */         for (int IX = 1; IX <= 1 + (N - 1) * INCX; IX += INCX) {
/* 1742 */           if (X[(IX - 1 + startX)] != 0.0D) {
/* 1743 */             double ABSXI = Math.abs(X[(IX - 1 + startX)]);
/* 1744 */             if (SCALE < ABSXI) {
/* 1745 */               SSQ = 1.0D + SSQ * Math.pow(SCALE / ABSXI, 2.0D);
/* 1746 */               SCALE = ABSXI;
/*      */             } else {
/* 1748 */               SSQ += Math.pow(ABSXI / SCALE, 2.0D);
/*      */             }
/*      */           }
/*      */         }
/* 1752 */         NORM = SCALE * Math.sqrt(SSQ);
/*      */       }
/*      */     }
/* 1754 */     return NORM;
/*      */   }
/*      */ 
/*      */   private void MKHESS(int N, double[] MODEL, int startMODEL, int LENGTH, double[] HSSIAN, int startHSSIAN, char HTYPE, int NH, int[] HESSTR)
/*      */   {
/* 1811 */     if (LENGTH > N) {
/* 1812 */       int NP1 = N + 1;
/*      */ 
/* 1814 */       int NXTR = 1;
/* 1815 */       int NXTM = NP1;
/* 1816 */       for (int I = 1; I <= N; I++) {
/* 1817 */         HSSIAN[(NXTR - 1 + startHSSIAN)] = MODEL[(NXTM - 1 + startMODEL)];
/* 1818 */         NXTM++;
/* 1819 */         NXTR += NP1;
/*      */       }
/*      */ 
/* 1822 */       if (HTYPE == 'D') {
/* 1823 */         for (int I = 2; I <= N; I++) {
/* 1824 */           int NXTC = I;
/* 1825 */           NXTR = 1 + (I - 1) * N;
/* 1826 */           for (int J = I; J <= N; J++) {
/* 1827 */             double TMP = MODEL[(NXTM - 1 + startMODEL)];
/* 1828 */             HSSIAN[(NXTR - 1 + startHSSIAN)] = TMP;
/* 1829 */             HSSIAN[(NXTC - 1 + startHSSIAN)] = TMP;
/* 1830 */             NXTM++;
/* 1831 */             NXTC += NP1;
/* 1832 */             NXTR += NP1;
/*      */           }
/*      */         }
/*      */       }
/* 1836 */       else if (HTYPE == 'B') {
/* 1837 */         for (int I = 2; I <= NH; I++) {
/* 1838 */           int NXTC = I;
/* 1839 */           NXTR = 1 + (I - 1) * N;
/* 1840 */           for (int J = I; J <= N; J++) {
/* 1841 */             double TMP = MODEL[(NXTM - 1 + startMODEL)];
/* 1842 */             HSSIAN[(NXTR - 1 + startHSSIAN)] = TMP;
/* 1843 */             HSSIAN[(NXTC - 1 + startHSSIAN)] = TMP;
/* 1844 */             NXTM++;
/* 1845 */             NXTC += NP1;
/* 1846 */             NXTR += NP1;
/*      */           }
/*      */         }
/* 1849 */         for (int I = NH + 1; I <= N; I++) {
/* 1850 */           int NXTC = I;
/* 1851 */           NXTR = 1 + (I - 1) * N;
/* 1852 */           for (int J = I; J <= N; J++) {
/* 1853 */             HSSIAN[(NXTR - 1 + startHSSIAN)] = 0.0D;
/* 1854 */             HSSIAN[(NXTC - 1 + startHSSIAN)] = 0.0D;
/* 1855 */             NXTC += NP1;
/* 1856 */             NXTR += NP1;
/*      */           }
/*      */         }
/*      */       }
/*      */       else {
/* 1861 */         NXTR = 1;
/* 1862 */         for (int I = 1; I <= N; I++) {
/* 1863 */           for (int J = 1; J <= N; J++) {
/* 1864 */             if (I != J) HSSIAN[(NXTR - 1 + startHSSIAN)] = 0.0D;
/* 1865 */             NXTR++;
/*      */           }
/*      */         }
/* 1868 */         for (int I = 1; I <= NH + NH; I += 2) {
/* 1869 */           NXTR = HESSTR[(I - 1)];
/* 1870 */           int NXTC = HESSTR[I];
/* 1871 */           double TMP = MODEL[(NXTM - 1 + startMODEL)];
/* 1872 */           HSSIAN[(N * (NXTC - 1) + NXTR - 1 + startHSSIAN)] = TMP;
/* 1873 */           HSSIAN[(N * (NXTR - 1) + NXTC - 1 + startHSSIAN)] = TMP;
/* 1874 */           NXTM++;
/*      */         }
/*      */       }
/*      */     }
/*      */     else {
/* 1879 */       for (int I = 1; I <= N * N; I++)
/* 1880 */         HSSIAN[(I - 1 + startHSSIAN)] = 0.0D;
/*      */     }
/*      */   }
/*      */ 
/*      */   private void DCOPY(int N, double[] DX, int startDX, int INCX, double[] DY, int startDY, int INCY)
/*      */   {
/* 1892 */     if (N <= 0) return;
/* 1893 */     if ((INCX == 1) && (INCY == 1)) {
/* 1894 */       System.arraycopy(DX, startDX, DY, startDY, N);
/*      */     } else {
/* 1896 */       int IX = 1;
/* 1897 */       int IY = 1;
/* 1898 */       if (INCX < 0) IX = (-N + 1) * INCX + 1;
/* 1899 */       if (INCY < 0) IY = (-N + 1) * INCY + 1;
/* 1900 */       for (int I = 1; I <= N; I++) {
/* 1901 */         DY[(IY - 1 + startDY)] = DX[(IX - 1 + startDX)];
/* 1902 */         IX += INCX;
/* 1903 */         IY += INCY;
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   private DGQToutput DGQT(int N, double[] A, int startA, int LDA, double[] B, int startB, double DELTA, double RTOL, double ATOL, int ITMAX, double PAR, double[] X, int startX, int ITER, double[] Z, int startZ, double[] WA1, int startWA1, double[] WA2, int startWA2)
/*      */   {
/* 2040 */     double P001 = 0.001D; double P5 = 0.5D;
/*      */ 
/* 2043 */     double ALPHA = 0.0D; double RZNORM = 0.0D; double F = 0.0D;
/* 2044 */     double PARF = 0.0D;
/* 2045 */     double XNORM = 0.0D;
/* 2046 */     double RXNORM = 0.0D;
/* 2047 */     boolean REDNC = false;
/* 2048 */     DGQToutput output = new DGQToutput();
/* 2049 */     for (int J = 1; J <= N; J++) {
/* 2050 */       X[(J - 1 + startX)] = 0.0D;
/* 2051 */       Z[(J - 1 + startZ)] = 0.0D;
/*      */     }
/*      */ 
/* 2054 */     DCOPY(N, A, startA, LDA + 1, WA1, startWA1, 1);
/* 2055 */     for (J = 1; J <= N - 1; J++) {
/* 2056 */       DCOPY(N - J, A, J * LDA + J - 1 + startA, LDA, A, (J - 1) * LDA + J + startA, 1);
/*      */     }
/*      */ 
/* 2059 */     double ANORM = 0.0D;
/* 2060 */     for (J = 1; J <= N; J++) {
/* 2061 */       WA2[(J - 1 + startWA2)] = DASUM(N, A, (J - 1) * LDA + startA, 1);
/* 2062 */       ANORM = Math.max(ANORM, WA2[(J - 1 + startWA2)]);
/*      */     }
/* 2064 */     for (J = 1; J <= N; J++) {
/* 2065 */       WA2[(J - 1 + startWA2)] -= Math.abs(WA1[(J - 1 + startWA1)]);
/*      */     }
/* 2067 */     double BNORM = DNRM2(N, B, startB, 1);
/*      */ 
/* 2069 */     double PARS = -ANORM;
/* 2070 */     double PARL = -ANORM;
/* 2071 */     double PARU = -ANORM;
/* 2072 */     for (J = 1; J <= N; J++) {
/* 2073 */       PARS = Math.max(PARS, -WA1[(J - 1 + startWA1)]);
/* 2074 */       PARL = Math.max(PARL, WA1[(J - 1 + startWA1)] + WA2[(J - 1 + startWA2)]);
/* 2075 */       PARU = Math.max(PARU, -WA1[(J - 1 + startWA1)] + WA2[(J - 1 + startWA2)]);
/*      */     }
/* 2077 */     PARL = Math.max(Math.max(0.0D, BNORM / DELTA - PARL), PARS);
/* 2078 */     PARU = Math.max(0.0D, BNORM / DELTA + PARU);
/*      */ 
/* 2080 */     PAR = Math.max(PAR, PARL);
/* 2081 */     PAR = Math.min(PAR, PARU);
/*      */ 
/* 2083 */     PARU = Math.max(PARU, (1.0D + RTOL) * PARL);
/*      */ 
/* 2085 */     int INFO = 0;
/* 2086 */     for (ITER = 1; ITER <= ITMAX; ITER++)
/*      */     {
/* 2088 */       if ((PAR <= PARS) && (PARU > 0.0D)) PAR = Math.max(0.001D, Math.sqrt(PARL / PARU)) * PARU;
/*      */ 
/* 2090 */       for (J = 1; J <= N - 1; J++) {
/* 2091 */         DCOPY(N - J, A, (J - 1) * LDA + J + startA, 1, A, J * LDA + J - 1 + startA, LDA);
/*      */       }
/* 2093 */       for (J = 1; J <= N; J++) {
/* 2094 */         A[((J - 1) * LDA + J - 1 + startA)] = (WA1[(J - 1 + startWA1)] + PAR);
/*      */       }
/*      */ 
/* 2097 */       int INDEF = DPOTRF('U', N, A, startA, LDA);
/*      */       double PARC;
/* 2099 */       if (INDEF == 0)
/*      */       {
/* 2101 */         PARF = PAR;
/* 2102 */         DCOPY(N, B, startB, 1, WA2, startWA2, 1);
/* 2103 */         DTRSV('U', 'T', 'N', N, A, startA, LDA, WA2, startWA2, 1);
/* 2104 */         RXNORM = DNRM2(N, WA2, startWA2, 1);
/* 2105 */         DTRSV('U', 'N', 'N', N, A, startA, LDA, WA2, startWA2, 1);
/* 2106 */         DCOPY(N, WA2, startWA2, 1, X, startX, 1);
/* 2107 */         DSCAL(N, -1.0D, X, startX, 1);
/* 2108 */         XNORM = DNRM2(N, X, startX, 1);
/*      */ 
/* 2110 */         if ((Math.abs(XNORM - DELTA) <= RTOL * DELTA) || ((PAR == 0.0D) && (XNORM <= (1.0D + RTOL) * DELTA))) INFO = 1;
/*      */ 
/* 2112 */         RZNORM = DESTSV(N, A, startA, LDA, Z, startZ);
/* 2113 */         PARS = Math.max(PARS, PAR - Math.pow(RZNORM, 2.0D));
/*      */ 
/* 2115 */         REDNC = false;
/* 2116 */         if (XNORM < DELTA)
/*      */         {
/* 2118 */           double PROD = DDOT(N, Z, startZ, 1, X, startX, 1) / DELTA;
/* 2119 */           double TEMP = (DELTA - XNORM) * ((DELTA + XNORM) / DELTA);
/* 2120 */           ALPHA = TEMP / (Math.abs(PROD) + Math.sqrt(Math.pow(PROD, 2.0D) + TEMP / DELTA));
/* 2121 */           ALPHA = SIGN(ALPHA, PROD);
/*      */ 
/* 2123 */           RZNORM = Math.abs(ALPHA) * RZNORM;
/* 2124 */           if (Math.pow(RZNORM / DELTA, 2.0D) + PAR * Math.pow(XNORM / DELTA, 2.0D) <= PAR) REDNC = true;
/*      */ 
/* 2126 */           if (0.5D * Math.pow(RZNORM / DELTA, 2.0D) <= RTOL * (1.0D - 0.5D * RTOL) * (PAR + Math.pow(RXNORM / DELTA, 2.0D)))
/* 2127 */             INFO = 1;
/* 2128 */           else if ((0.5D * (PAR + Math.pow(RXNORM / DELTA, 2.0D)) <= ATOL / DELTA / DELTA) && (INFO == 0))
/* 2129 */             INFO = 2;
/* 2130 */           else if (XNORM == 0.0D)
/* 2131 */             INFO = 1;
/*      */         }
/*      */         double PARC;
/*      */         double PARC;
/* 2135 */         if (XNORM == 0.0D) {
/* 2136 */           PARC = -PAR;
/*      */         } else {
/* 2138 */           DCOPY(N, X, startX, 1, WA2, startWA2, 1);
/* 2139 */           double TEMP = 1.0D / XNORM;
/* 2140 */           DSCAL(N, TEMP, WA2, startWA2, 1);
/* 2141 */           DTRSV('U', 'T', 'N', N, A, startA, LDA, WA2, startWA2, 1);
/* 2142 */           TEMP = DNRM2(N, WA2, startWA2, 1);
/* 2143 */           PARC = (XNORM - DELTA) / DELTA / TEMP / TEMP;
/*      */         }
/*      */ 
/* 2146 */         if (XNORM > DELTA) PARL = Math.max(PARL, PAR);
/* 2147 */         if (XNORM < DELTA) PARU = Math.min(PARU, PAR);
/*      */       }
/*      */       else
/*      */       {
/* 2151 */         if (INDEF > 1)
/*      */         {
/* 2153 */           DCOPY(INDEF - 1, A, INDEF - 1 + startA, LDA, A, (INDEF - 1) * LDA + startA, 1);
/* 2154 */           A[((INDEF - 1) * LDA + INDEF - 1 + startA)] = (WA1[(INDEF - 1 + startWA1)] + PAR);
/*      */ 
/* 2156 */           DCOPY(INDEF - 1, A, INDEF - 1 + startA, 1, WA2, startWA2, 1);
/* 2157 */           DTRSV('U', 'T', 'N', INDEF - 1, A, startA, LDA, WA2, startWA2, 1);
/* 2158 */           A[((INDEF - 1) * LDA + INDEF - 1 + startA)] -= Math.pow(DNRM2(INDEF - 1, WA2, startWA2, 1), 2.0D);
/* 2159 */           DTRSV('U', 'N', 'N', INDEF - 1, A, startA, LDA, WA2, startWA2, 1);
/*      */         }
/* 2161 */         WA2[(INDEF - 1 + startWA2)] = -1.0D;
/* 2162 */         double TEMP = DNRM2(INDEF, WA2, startWA2, 1);
/* 2163 */         PARC = -(A[((INDEF - 1) * LDA + INDEF - 1 + startA)] / TEMP) / TEMP;
/* 2164 */         PARS = Math.max(PARS, Math.max(PAR, PAR + PARC));
/*      */ 
/* 2166 */         PARU = Math.max(PARU, (1.0D + RTOL) * PARS);
/*      */       }
/*      */ 
/* 2169 */       PARL = Math.max(PARL, PARS);
/*      */ 
/* 2171 */       if (INFO == 0) {
/* 2172 */         if (ITER == ITMAX) INFO = 4;
/* 2173 */         if (PARU <= (1.0D + 0.5D * RTOL) * PARS) INFO = 3;
/* 2174 */         if (PARU == 0.0D) INFO = 2;
/*      */       }
/*      */ 
/* 2177 */       if (INFO != 0)
/*      */       {
/* 2179 */         PAR = PARF;
/* 2180 */         F = -0.5D * (Math.pow(RXNORM, 2.0D) + PAR * Math.pow(XNORM, 2.0D));
/* 2181 */         if (REDNC) {
/* 2182 */           F = -0.5D * (Math.pow(RXNORM, 2.0D) + PAR * Math.pow(DELTA, 2.0D) - Math.pow(RZNORM, 2.0D));
/* 2183 */           DAXPY(N, ALPHA, Z, startZ, 1, X, startX, 1);
/*      */         }
/*      */ 
/* 2186 */         for (J = 1; J <= N - 1; J++) {
/* 2187 */           DCOPY(N - J, A, (J - 1) * LDA + J + startA, 1, A, J * LDA + J - 1 + startA, LDA);
/*      */         }
/* 2189 */         DCOPY(N, WA1, startWA1, 1, A, startA, LDA + 1);
/* 2190 */         output.PAR = PAR;
/* 2191 */         output.F = F;
/* 2192 */         output.INFO = INFO;
/* 2193 */         return output;
/*      */       }
/*      */ 
/* 2196 */       PAR = Math.max(PARL, PAR + PARC);
/*      */     }
/*      */ 
/* 2199 */     output.PAR = PAR;
/* 2200 */     output.F = F;
/* 2201 */     output.INFO = INFO;
/* 2202 */     return output;
/*      */   }
/*      */ 
/*      */   private void DTRSV(char UPLO, char TRANS, char DIAG, int N, double[] A, int startA, int LDA, double[] X, int startX, int INCX)
/*      */   {
/* 2309 */     int KX = 0;
/*      */ 
/* 2312 */     int INFO = 0;
/* 2313 */     if ((UPLO != 'U') && (UPLO != 'L'))
/* 2314 */       INFO = 1;
/* 2315 */     else if ((TRANS != 'N') && (TRANS != 'T') && (TRANS != 'C'))
/* 2316 */       INFO = 2;
/* 2317 */     else if ((DIAG != 'U') && (DIAG != 'N'))
/* 2318 */       INFO = 3;
/* 2319 */     else if (N < 0)
/* 2320 */       INFO = 4;
/* 2321 */     else if (LDA < Math.max(1, N))
/* 2322 */       INFO = 6;
/* 2323 */     else if (INCX == 0) {
/* 2324 */       INFO = 8;
/*      */     }
/* 2326 */     if (INFO != 0) {
/* 2327 */       return;
/*      */     }
/*      */ 
/* 2330 */     if (N == 0) return;
/* 2331 */     boolean NOUNIT = DIAG == 'N';
/*      */ 
/* 2333 */     if (INCX <= 0)
/* 2334 */       KX = 1 - (N - 1) * INCX;
/* 2335 */     else if (INCX != 1) {
/* 2336 */       KX = 1;
/*      */     }
/*      */ 
/* 2339 */     if (TRANS == 'N')
/*      */     {
/* 2341 */       if (UPLO == 'U') {
/* 2342 */         if (INCX == 1) {
/* 2343 */           for (int J = N; J <= 1; J--)
/* 2344 */             if (X[(J - 1 + startX)] != 0.0D) {
/* 2345 */               if (NOUNIT) X[(J - 1 + startX)] /= A[((J - 1) * LDA + J - 1 + startA)];
/* 2346 */               double TEMP = X[(J - 1 + startX)];
/* 2347 */               for (int I = J - 1; I <= 1; I--)
/* 2348 */                 X[(I - 1 + startX)] -= TEMP * A[((J - 1) * LDA + I - 1 + startA)];
/*      */             }
/*      */         }
/*      */         else
/*      */         {
/* 2353 */           int JX = KX + (N - 1) * INCX;
/* 2354 */           for (int J = N; J <= 1; J--) {
/* 2355 */             if (X[(JX - 1 + startX)] != 0.0D) {
/* 2356 */               if (NOUNIT) X[(JX - 1 + startX)] /= A[((J - 1) * LDA + J - 1 + startA)];
/* 2357 */               double TEMP = X[(JX - 1 + startX)];
/* 2358 */               int IX = JX;
/* 2359 */               for (int I = J - 1; I <= 1; I--) {
/* 2360 */                 IX -= INCX;
/* 2361 */                 X[(IX - 1 + startX)] -= TEMP * A[((J - 1) * LDA + I - 1 + startA)];
/*      */               }
/*      */             }
/* 2364 */             JX -= INCX;
/*      */           }
/*      */         }
/*      */       }
/* 2368 */       else if (INCX == 1) {
/* 2369 */         for (int J = 1; J <= N; J++)
/* 2370 */           if (X[(J - 1 + startX)] != 0.0D) {
/* 2371 */             if (NOUNIT) X[(J - 1 + startX)] /= A[((J - 1) * LDA + J - 1 + startA)];
/* 2372 */             double TEMP = X[(J - 1 + startX)];
/* 2373 */             for (int I = J + 1; I <= N; I++)
/* 2374 */               X[(I - 1 + startX)] -= TEMP * A[((J - 1) * LDA + I - 1 + startA)];
/*      */           }
/*      */       }
/*      */       else
/*      */       {
/* 2379 */         int JX = KX;
/* 2380 */         for (int J = 1; J <= N; J++) {
/* 2381 */           if (X[(JX - 1 + startX)] != 0.0D) {
/* 2382 */             if (NOUNIT) X[(JX - 1 + startX)] /= A[((J - 1) * LDA + J - 1 + startA)];
/* 2383 */             double TEMP = X[(JX - 1 + startX)];
/* 2384 */             int IX = JX;
/* 2385 */             for (int I = J + 1; I <= N; I++) {
/* 2386 */               IX += INCX;
/* 2387 */               X[(IX - 1 + startX)] -= TEMP * A[((J - 1) * LDA + I - 1 + startA)];
/*      */             }
/*      */           }
/* 2390 */           JX += INCX;
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*      */     }
/* 2396 */     else if (UPLO == 'U') {
/* 2397 */       if (INCX == 1) {
/* 2398 */         for (int J = 1; J <= N; J++) {
/* 2399 */           double TEMP = X[(J - 1 + startX)];
/* 2400 */           for (int I = 1; I <= J - 1; I++) {
/* 2401 */             TEMP -= A[((J - 1) * LDA + I - 1 + startA)] * X[(I - 1 + startX)];
/*      */           }
/* 2403 */           if (NOUNIT) TEMP /= A[((J - 1) * LDA + J - 1 + startA)];
/* 2404 */           X[(J - 1 + startX)] = TEMP;
/*      */         }
/*      */       } else {
/* 2407 */         int JX = KX;
/* 2408 */         for (int J = 1; J <= N; J++) {
/* 2409 */           double TEMP = X[(JX - 1 + startX)];
/* 2410 */           int IX = KX;
/* 2411 */           for (int I = 1; I <= J - 1; I++) {
/* 2412 */             TEMP -= A[((J - 1) * LDA + I - 1 + startA)] * X[(IX - 1 + startX)];
/* 2413 */             IX += INCX;
/*      */           }
/* 2415 */           if (NOUNIT) TEMP /= A[((J - 1) * LDA + J - 1 + startA)];
/* 2416 */           X[(JX - 1 + startX)] = TEMP;
/* 2417 */           JX += INCX;
/*      */         }
/*      */       }
/*      */     }
/* 2421 */     else if (INCX == 1) {
/* 2422 */       for (int J = N; J <= 1; J--) {
/* 2423 */         double TEMP = X[(J - 1 + startX)];
/* 2424 */         for (int I = N; I <= J + 1; I--) {
/* 2425 */           TEMP -= A[((J - 1) * LDA + I - 1 + startA)] * X[(I - 1 + startX)];
/*      */         }
/* 2427 */         if (NOUNIT) TEMP /= A[((J - 1) * LDA + J - 1 + startA)];
/* 2428 */         X[(J - 1 + startX)] = TEMP;
/*      */       }
/*      */     } else {
/* 2431 */       KX += (N - 1) * INCX;
/* 2432 */       int JX = KX;
/* 2433 */       for (int J = N; J <= 1; J--) {
/* 2434 */         double TEMP = X[(JX - 1 + startX)];
/* 2435 */         int IX = KX;
/* 2436 */         for (int I = N; I <= J + 1; I--) {
/* 2437 */           TEMP -= A[((J - 1) * LDA + I - 1 + startA)] * X[(IX - 1 + startX)];
/* 2438 */           IX -= INCX;
/*      */         }
/* 2440 */         if (NOUNIT) TEMP /= A[((J - 1) * LDA + J - 1 + startA)];
/* 2441 */         X[(JX - 1 + startX)] = TEMP;
/* 2442 */         JX -= INCX;
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   private int DPOTRF(char UPLO, int N, double[] A, int startA, int LDA)
/*      */   {
/* 2500 */     int INFO = 0;
/* 2501 */     boolean UPPER = UPLO == 'U';
/* 2502 */     if ((!UPPER) && (UPLO != 'L'))
/* 2503 */       INFO = -1;
/* 2504 */     else if (N < 0)
/* 2505 */       INFO = -2;
/* 2506 */     else if (LDA < Math.max(1, N)) {
/* 2507 */       INFO = -4;
/*      */     }
/* 2509 */     if (INFO != 0) {
/* 2510 */       return INFO;
/*      */     }
/*      */ 
/* 2513 */     if (N == 0) return INFO;
/* 2514 */     if (UPPER)
/*      */     {
/* 2516 */       for (int J = 1; J <= N; J++)
/*      */       {
/* 2518 */         double AJJ = A[((J - 1) * LDA + J - 1 + startA)] - DDOT(J - 1, A, (J - 1) * LDA + startA, 1, A, (J - 1) * LDA + startA, 1);
/* 2519 */         if (AJJ <= 0.0D) {
/* 2520 */           A[((J - 1) * LDA + J - 1 + startA)] = AJJ;
/* 2521 */           return J;
/*      */         }
/* 2523 */         AJJ = Math.sqrt(AJJ);
/* 2524 */         A[((J - 1) * LDA + J - 1 + startA)] = AJJ;
/*      */ 
/* 2526 */         if (J < N) {
/* 2527 */           DGEMV('T', J - 1, N - J, -1.0D, A, J * LDA + startA, LDA, A, (J - 1) * LDA + startA, 1, 1.0D, A, J * LDA + J - 1 + startA, LDA);
/* 2528 */           DSCAL(N - J, 1.0D / AJJ, A, J * LDA + J - 1 + startA, LDA);
/*      */         }
/*      */       }
/*      */     }
/*      */     else {
/* 2533 */       for (int J = 1; J <= N; J++)
/*      */       {
/* 2535 */         double AJJ = A[((J - 1) * LDA + J - 1 + startA)] - DDOT(J - 1, A, J - 1 + startA, LDA, A, J - 1 + startA, LDA);
/* 2536 */         if (AJJ <= 0.0D) {
/* 2537 */           A[((J - 1) * LDA + J - 1 + startA)] = AJJ;
/* 2538 */           return J;
/*      */         }
/* 2540 */         AJJ = Math.sqrt(AJJ);
/* 2541 */         A[((J - 1) * LDA + J - 1 + startA)] = AJJ;
/*      */ 
/* 2543 */         if (J < N) {
/* 2544 */           DGEMV('N', N - J, J - 1, -1.0D, A, J + startA, LDA, A, J - 1 + startA, LDA, 1.0D, A, (J - 1) * LDA + J + startA, 1);
/* 2545 */           DSCAL(N - J, 1.0D / AJJ, A, (J - 1) * LDA + J + startA, 1);
/*      */         }
/*      */       }
/*      */     }
/* 2549 */     return INFO;
/*      */   }
/*      */ 
/*      */   private void DGEMV(char TRANS, int M, int N, double ALPHA, double[] A, int startA, int LDA, double[] X, int startX, int INCX, double BETA, double[] Y, int startY, int INCY)
/*      */   {
/* 2649 */     int INFO = 0;
/* 2650 */     if ((TRANS != 'N') && (TRANS != 'T') && (TRANS != 'C'))
/* 2651 */       INFO = 1;
/* 2652 */     else if (M < 0)
/* 2653 */       INFO = 2;
/* 2654 */     else if (N < 0)
/* 2655 */       INFO = 3;
/* 2656 */     else if (LDA < Math.max(1, M))
/* 2657 */       INFO = 6;
/* 2658 */     else if (INCX == 0)
/* 2659 */       INFO = 8;
/* 2660 */     else if (INCY == 0) {
/* 2661 */       INFO = 11;
/*      */     }
/* 2663 */     if (INFO != 0) {
/* 2664 */       return;
/*      */     }
/*      */ 
/* 2667 */     if ((M == 0) || (N == 0) || ((ALPHA == 0.0D) && (BETA == 1.0D)))
/*      */       return;
/*      */     int LENY;
/*      */     int LENX;
/*      */     int LENY;
/* 2669 */     if (TRANS == 'N') {
/* 2670 */       int LENX = N;
/* 2671 */       LENY = M;
/*      */     } else {
/* 2673 */       LENX = M;
/* 2674 */       LENY = N;
/*      */     }
/*      */     int KX;
/*      */     int KX;
/* 2676 */     if (INCX > 0)
/* 2677 */       KX = 1;
/*      */     else
/* 2679 */       KX = 1 - (LENX - 1) * INCX;
/*      */     int KY;
/*      */     int KY;
/* 2681 */     if (INCY > 0)
/* 2682 */       KY = 1;
/*      */     else {
/* 2684 */       KY = 1 - (LENY - 1) * INCY;
/*      */     }
/*      */ 
/* 2689 */     if (BETA != 1.0D) {
/* 2690 */       if (INCY == 1) {
/* 2691 */         if (BETA == 0.0D) {
/* 2692 */           for (int I = 1; I <= LENY; I++)
/* 2693 */             Y[(I - 1 + startY)] = 0.0D;
/*      */         }
/*      */         else
/* 2696 */           for (int I = 1; I <= LENY; I++)
/* 2697 */             Y[(I - 1 + startY)] = (BETA * Y[(I - 1 + startY)]);
/*      */       }
/*      */       else
/*      */       {
/* 2701 */         int IY = KY;
/* 2702 */         if (BETA == 0.0D)
/* 2703 */           for (int I = 1; I <= LENY; I++) {
/* 2704 */             Y[(IY - 1 + startY)] = 0.0D;
/* 2705 */             IY += INCY;
/*      */           }
/*      */         else {
/* 2708 */           for (int I = 1; I <= LENY; I++) {
/* 2709 */             Y[(IY - 1 + startY)] = (BETA * Y[(IY - 1 + startY)]);
/* 2710 */             IY += INCY;
/*      */           }
/*      */         }
/*      */       }
/*      */     }
/* 2715 */     if (ALPHA == 0.0D) return;
/* 2716 */     if (TRANS == 'N')
/*      */     {
/* 2718 */       int JX = KX;
/* 2719 */       if (INCY == 1)
/* 2720 */         for (int J = 1; J <= N; J++) {
/* 2721 */           if (X[(JX - 1 + startX)] != 0.0D) {
/* 2722 */             double TEMP = ALPHA * X[(JX - 1 + startX)];
/* 2723 */             for (int I = 1; I <= M; I++) {
/* 2724 */               Y[(I - 1 + startY)] += TEMP * A[((J - 1) * LDA + I - 1 + startA)];
/*      */             }
/*      */           }
/* 2727 */           JX += INCX;
/*      */         }
/*      */       else
/* 2730 */         for (int J = 1; J <= N; J++) {
/* 2731 */           if (X[(JX - 1 + startX)] != 0.0D) {
/* 2732 */             double TEMP = ALPHA * X[(JX - 1 + startX)];
/* 2733 */             int IY = KY;
/* 2734 */             for (int I = 1; I <= M; I++) {
/* 2735 */               Y[(IY - 1 + startY)] += TEMP * A[((J - 1) * LDA + I - 1 + startA)];
/* 2736 */               IY += INCY;
/*      */             }
/*      */           }
/* 2739 */           JX += INCX;
/*      */         }
/*      */     }
/*      */     else
/*      */     {
/* 2744 */       int JY = KY;
/* 2745 */       if (INCX == 1)
/* 2746 */         for (int J = 1; J <= N; J++) {
/* 2747 */           double TEMP = 0.0D;
/* 2748 */           for (int I = 1; I <= M; I++) {
/* 2749 */             TEMP += A[((J - 1) * LDA + I - 1 + startA)] * X[(I - 1 + startX)];
/*      */           }
/* 2751 */           Y[(JY - 1 + startY)] += ALPHA * TEMP;
/* 2752 */           JY += INCY;
/*      */         }
/*      */       else
/* 2755 */         for (int J = 1; J <= N; J++) {
/* 2756 */           double TEMP = 0.0D;
/* 2757 */           int IX = KX;
/* 2758 */           for (int I = 1; I <= M; I++) {
/* 2759 */             TEMP += A[((J - 1) * LDA + I - 1 + startA)] * X[(IX - 1 + startX)];
/* 2760 */             IX += INCX;
/*      */           }
/* 2762 */           Y[(JY - 1 + startY)] += ALPHA * TEMP;
/* 2763 */           JY += INCY;
/*      */         }
/*      */     }
/*      */   }
/*      */ 
/*      */   private void DAXPY(int N, double DA, double[] DX, int startX, int INCX, double[] DY, int startY, int INCY)
/*      */   {
/* 2777 */     if (N <= 0) return;
/* 2778 */     if (DA == 0.0D) return;
/* 2779 */     if ((INCX == 1) && (INCY == 1)) {
/* 2780 */       int M = N % 4;
/* 2781 */       if (M != 0) {
/* 2782 */         for (int I = 1; I <= M; I++) {
/* 2783 */           DY[(I - 1 + startY)] += DA * DX[(I - 1 + startX)];
/*      */         }
/* 2785 */         if (N < 4) return;
/*      */       }
/* 2787 */       int MP1 = M + 1;
/* 2788 */       for (int I = MP1; I <= N; I += 4) {
/* 2789 */         DY[(I - 1 + startY)] += DA * DX[(I - 1 + startX)];
/* 2790 */         DY[(I + 1 - 1 + startY)] += DA * DX[(I + 1 - 1 + startX)];
/* 2791 */         DY[(I + 2 - 1 + startY)] += DA * DX[(I + 2 - 1 + startX)];
/* 2792 */         DY[(I + 3 - 1 + startY)] += DA * DX[(I + 3 - 1 + startX)];
/*      */       }
/*      */     } else {
/* 2795 */       int IX = 1;
/* 2796 */       int IY = 1;
/* 2797 */       if (INCX < 0) IX = (-N + 1) * INCX + 1;
/* 2798 */       if (INCY < 0) IY = (-N + 1) * INCY + 1;
/* 2799 */       for (int I = 1; I <= N; I++) {
/* 2800 */         DY[(IY - 1 + startY)] += DA * DX[(IX - 1 + startX)];
/* 2801 */         IX += INCX;
/* 2802 */         IY += INCY;
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   private char IMPRVE(int N, int LP, double[] XSET, int startXSET, double[] YSET, int startYSET, double[] XBASE, double FXBASE, double[] DIST, int startDIST, double DELTA, double[] NFP, int startNFP, double[] XARCH, int startXARCH, double[] YARCH, int startYARCH, char HTYPE, int NH, int[] HESSTR, double[] W, int startW)
/*      */   {
/* 2943 */     if (this.NP < LP) {
/* 2944 */       char ACTION = IMPRVC(N, LP, XSET, startXSET, YSET, startYSET, XBASE, FXBASE, DIST, startDIST, DELTA, NFP, startNFP, HTYPE, NH, HESSTR, W, startW);
/* 2945 */       if ((ACTION != '-') || (this.NF > this.MAXNF)) {
/* 2946 */         return ACTION;
/*      */       }
/*      */     }
/*      */ 
/* 2950 */     char ACTION = IMPRVF(N, LP, XSET, startXSET, YSET, startYSET, XBASE, FXBASE, DIST, startDIST, DELTA, NFP, startNFP, XARCH, startXARCH, YARCH, startYARCH, HTYPE, NH, HESSTR, W, startW);
/* 2951 */     if ((ACTION != '-') || (this.NF > this.MAXNF)) {
/* 2952 */       return ACTION;
/*      */     }
/*      */ 
/* 2955 */     ACTION = IMPRVL(N, LP, XSET, startXSET, YSET, startYSET, XBASE, FXBASE, DIST, startDIST, DELTA, NFP, startNFP, XARCH, startXARCH, YARCH, startYARCH, HTYPE, NH, HESSTR, W, startW);
/* 2956 */     return ACTION;
/*      */   }
/*      */ 
/*      */   private char IMPRVC(int N, int LP, double[] XSET, int startXSET, double[] YSET, int startYSET, double[] XBASE, double FXBASE, double[] DIST, int startDIST, double DELTA, double[] NFP, int startNFP, char HTYPE, int NH, int[] HESSTR, double[] W, int startW)
/*      */   {
/* 3062 */     int INEW = 1 + this.NP * N;
/* 3063 */     Point FL = NFPDAT(N, this.NP + 1, LP);
/* 3064 */     int IPOL = FL.x;
/* 3065 */     int LPOL = FL.y;
/* 3066 */     MAXABS(N, DELTA, NFP, IPOL - 1 + startNFP, LPOL, XSET, INEW - 1 + startXSET, HTYPE, NH, HESSTR, W, startW);
/*      */ 
/* 3068 */     this.NP += 1;
/* 3069 */     DIST[(this.NP - 1 + startDIST)] = DNRM2(N, XSET, INEW - 1 + startXSET, 1);
/* 3070 */     DAXPY(N, 1.0D, XBASE, 0, 1, XSET, INEW - 1 + startXSET, 1);
/*      */ 
/* 3072 */     this.NF += 1;
/* 3073 */     if (this.NF > this.MAXNF) return '-';
/* 3074 */     YSET[(this.NP - 1 + startYSET)] = UFN(N, XSET, INEW - 1 + startXSET);
/*      */ 
/* 3076 */     return 'A';
/*      */   }
/*      */ 
/*      */   private char IMPRVF(int N, int LP, double[] XSET, int startXSET, double[] YSET, int startYSET, double[] XBASE, double FXBASE, double[] DIST, int startDIST, double DELTA, double[] NFP, int startNFP, double[] XARCH, int startXARCH, double[] YARCH, int startYARCH, char HTYPE, int NH, int[] HESSTR, double[] W, int startW)
/*      */   {
/* 3195 */     int HIST = 10; int NP1 = N + 1;
/* 3196 */     double ONET = 1.2D;
/* 3197 */     char ACTION = '-';
/*      */ 
/* 3199 */     double MAXDST = 1.2D * DELTA;
/* 3200 */     double DMAX = 0.0D;
/* 3201 */     int IP = 0;
/* 3202 */     for (int J = 1; J <= this.NP; J++) {
/* 3203 */       if (DIST[(J - 1 + startDIST)] > DMAX) {
/* 3204 */         DMAX = DIST[(J - 1 + startDIST)];
/* 3205 */         IP = J;
/*      */       }
/*      */     }
/*      */ 
/* 3209 */     if (DMAX > MAXDST)
/*      */     {
/* 3211 */       Point FL = NFPDAT(N, IP, LP);
/* 3212 */       int ifP = FL.x;
/* 3213 */       int LFP = FL.y;
/*      */ 
/* 3220 */       double FPMAX = 0.0D;
/* 3221 */       int INEW = 0;
/* 3222 */       int JP = 1 + Math.max(this.NARCH - 10, 0) * N;
/* 3223 */       int IA = 0;
/* 3224 */       for (J = JP; J <= this.NARCH; J++) {
/* 3225 */         int JX = 1 + (J - 1) * N;
/* 3226 */         System.arraycopy(XARCH, JX - 1 + startXARCH, W, NP1 - 1 + startW, N);
/* 3227 */         DAXPY(N, -1.0D, XBASE, 0, 1, W, NP1 - 1 + startW, 1);
/* 3228 */         double DJ = DNRM2(N, W, NP1 - 1 + startW, 1);
/* 3229 */         if (DJ <= DELTA) {
/* 3230 */           double POLVAL = Math.abs(VALP(N, W, NP1 - 1 + startW, NFP, ifP - 1 + startNFP, LFP, HTYPE, NH, HESSTR));
/* 3231 */           if (POLVAL > FPMAX) {
/* 3232 */             IA = JX;
/* 3233 */             INEW = J;
/* 3234 */             FPMAX = POLVAL;
/*      */           }
/*      */ 
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/* 3243 */       if (FPMAX >= 1.2D)
/*      */       {
/* 3245 */         REPLCE(N, IP, XSET, startXSET, YSET, startYSET, XARCH, IA - 1 + startXARCH, YARCH[(INEW - 1 + startYARCH)], false, XBASE, FXBASE, DIST, startDIST, XARCH, startXARCH, YARCH, startYARCH);
/*      */ 
/* 3247 */         return 'O';
/*      */       }
/*      */ 
/* 3258 */       double VALUE = MAXABS(N, DELTA, NFP, ifP - 1 + startNFP, LFP, W, startW, HTYPE, NH, HESSTR, W, NP1 - 1 + startW);
/* 3259 */       DAXPY(N, 1.0D, XBASE, 0, 1, W, startW, 1);
/*      */ 
/* 3261 */       REPLCE(N, IP, XSET, startXSET, YSET, startYSET, W, startW, VALUE, true, XBASE, FXBASE, DIST, startDIST, XARCH, startXARCH, YARCH, startYARCH);
/*      */ 
/* 3263 */       return 'F';
/*      */     }
/*      */ 
/* 3266 */     return ACTION;
/*      */   }
/*      */ 
/*      */   private char IMPRVL(int N, int LP, double[] XSET, int startXSET, double[] YSET, int startYSET, double[] XBASE, double FXBASE, double[] DIST, int startDIST, double DELTA, double[] NFP, int startNFP, double[] XARCH, int startXARCH, double[] YARCH, int startYARCH, char HTYPE, int NH, int[] HESSTR, double[] W, int startW)
/*      */   {
/* 3384 */     double VALUE = 0.0D;
/* 3385 */     double ZERO = 0.0D; double ONE = 1.0D; double REPTHR = 2.0D;
/* 3386 */     int NP1 = N + 1;
/* 3387 */     int NNP1 = N + NP1;
/*      */ 
/* 3395 */     double FPMAX = 0.0D;
/* 3396 */     int INEW = 1;
/* 3397 */     for (int J = 1; J <= this.NP; J++) {
/* 3398 */       Point FL = NFPDAT(N, J, LP);
/* 3399 */       int IPOL = FL.x;
/* 3400 */       int LPOL = FL.y;
/* 3401 */       VALUE = MAXABS(N, DELTA, NFP, IPOL - 1 + startNFP, LPOL, W, NP1 - 1 + startW, HTYPE, NH, HESSTR, W, NNP1 - 1 + startW);
/* 3402 */       if (VALUE > FPMAX) {
/* 3403 */         FPMAX = VALUE;
/* 3404 */         INEW = J;
/* 3405 */         System.arraycopy(W, NP1 - 1 + startW, W, startW, N);
/*      */       }
/*      */     }
/* 3408 */     DAXPY(N, 1.0D, XBASE, 0, 1, W, startW, 1);
/*      */     char ACTION;
/*      */     char ACTION;
/* 3412 */     if (FPMAX > 2.0D) {
/* 3413 */       REPLCE(N, INEW, XSET, startXSET, YSET, startYSET, W, startW, VALUE, true, XBASE, FXBASE, DIST, startDIST, XARCH, startXARCH, YARCH, startYARCH);
/*      */ 
/* 3415 */       ACTION = 'C';
/*      */     }
/*      */     else {
/* 3418 */       ACTION = '-';
/*      */     }
/* 3420 */     return ACTION;
/*      */   }
/*      */ 
/*      */   private char TRYRPL(int N, int LP, double[] XSET, int startXSET, double[] YSET, int startYSET, double[] XNEW, int startXNEW, double FXNEW, double[] XBASE, double FXBASE, double[] DIST, int startDIST, double DELTA, double[] NFP, int startNFP, double[] XARCH, int startXARCH, double[] YARCH, int startYARCH, char HTYPE, int NH, int[] HESSTR, double[] W, int startW)
/*      */   {
/* 3568 */     double ONET = 1.2D; double THRNEW = 0.05D; double THRFAR = 2.0D; double THRCLO = 2.0D;
/*      */ 
/* 3570 */     char ACTION = '-';
/*      */ 
/* 3572 */     GETNFP(N, LP, XSET, startXSET, YSET, startYSET, 0, XBASE, FXBASE, DIST, startDIST, DELTA, -1.0D, NFP, startNFP, XARCH, startXARCH, YARCH, startYARCH, HTYPE, NH, HESSTR, W, startW);
/*      */ 
/* 3574 */     System.arraycopy(XNEW, startXNEW, W, startW, N);
/* 3575 */     DAXPY(N, -1.0D, XBASE, 0, 1, W, startW, 1);
/*      */ 
/* 3577 */     if (this.NP < LP)
/*      */     {
/* 3579 */       Point FL = NFPDAT(N, this.NP + 1, LP);
/* 3580 */       int IPOL = FL.x;
/* 3581 */       int LPOL = FL.y;
/* 3582 */       double PVAL = Math.abs(VALP(N, W, startW, NFP, IPOL - 1 + startNFP, LPOL, HTYPE, NH, HESSTR));
/*      */ 
/* 3584 */       if (PVAL >= 0.05D) {
/* 3585 */         ACTION = 'A';
/* 3586 */         int I = this.NP * N + 1;
/* 3587 */         this.NP += 1;
/* 3588 */         System.arraycopy(XNEW, startXNEW, XSET, I - 1 + startXSET, N);
/* 3589 */         YSET[(this.NP - 1 + startYSET)] = FXNEW;
/* 3590 */         DIST[(this.NP - 1 + startDIST)] = DNRM2(N, W, startW, 1);
/*      */       }
/*      */     }
/* 3594 */     else { double DUP = 1.0E+020D;
/*      */       boolean doitagain;
/*      */       do {
/* 3597 */         doitagain = false;
/* 3598 */         double DMAX = 1.2D * DELTA;
/* 3599 */         int IMAX = 0;
/* 3600 */         for (int I = 1; I <= this.NP; I++) {
/* 3601 */           double DI = DIST[(I - 1 + startDIST)];
/* 3602 */           if ((DI > DMAX) && (DI < DUP)) {
/* 3603 */             DMAX = DI;
/* 3604 */             IMAX = I;
/*      */           }
/*      */         }
/*      */ 
/* 3608 */         if (IMAX > 0)
/*      */         {
/* 3610 */           Point FL = NFPDAT(N, IMAX, LP);
/* 3611 */           int IPOL = FL.x;
/* 3612 */           int LPOL = FL.y;
/* 3613 */           double PVAL = Math.abs(VALP(N, W, startW, NFP, IPOL - 1 + startNFP, LPOL, HTYPE, NH, HESSTR));
/*      */ 
/* 3615 */           if (PVAL >= 2.0D * Math.pow(DELTA / DMAX, 2.0D)) {
/* 3616 */             REPLCE(N, IMAX, XSET, startXSET, YSET, startYSET, XNEW, startXNEW, FXNEW, false, XBASE, FXBASE, DIST, startDIST, XARCH, startXARCH, YARCH, startYARCH);
/* 3617 */             ACTION = 'F';
/*      */           }
/*      */           else {
/* 3620 */             DUP = DMAX;
/* 3621 */             doitagain = true;
/*      */           }
/*      */         }
/*      */         else {
/* 3625 */           double PMAX = 0.0D;
/* 3626 */           IMAX = 0;
/* 3627 */           for (I = 1; I <= this.NP; I++) {
/* 3628 */             Point FL = NFPDAT(N, I, LP);
/* 3629 */             int IPOL = FL.x;
/* 3630 */             int LPOL = FL.y;
/* 3631 */             double PVAL = Math.abs(VALP(N, W, startW, NFP, IPOL - 1 + startNFP, LPOL, HTYPE, NH, HESSTR));
/* 3632 */             if (PVAL > PMAX) {
/* 3633 */               IMAX = I;
/* 3634 */               PMAX = PVAL;
/*      */             }
/*      */           }
/*      */ 
/* 3638 */           if (PMAX >= 2.0D) {
/* 3639 */             REPLCE(N, IMAX, XSET, startXSET, YSET, startYSET, XNEW, startXNEW, FXNEW, false, XBASE, FXBASE, DIST, startDIST, XARCH, startXARCH, YARCH, startYARCH);
/* 3640 */             ACTION = 'C';
/*      */           }
/*      */         }
/*      */       }
/* 3596 */       while (
/* 3643 */         doitagain);
/*      */     }
/* 3645 */     return ACTION;
/*      */   }
/*      */ 
/*      */   private void ARCHIV(int N, double[] XX, int startXX, double FXX, double[] XARCH, int startXARCH, double[] YARCH, int startYARCH)
/*      */   {
/*      */     int JX;
/*      */     int JX;
/* 3690 */     if (this.NARCH < this.MXARCH)
/* 3691 */       JX = 1 + this.NARCH * N;
/*      */     else {
/* 3693 */       JX = 1;
/*      */     }
/* 3695 */     this.NARCH = Math.min(this.MXARCH, this.NARCH + 1);
/*      */ 
/* 3697 */     System.arraycopy(XX, startXX, XARCH, JX - 1 + startXARCH, N);
/* 3698 */     YARCH[(this.NARCH - 1 + startYARCH)] = FXX;
/*      */   }
/*      */ 
/*      */   private double VALP(int N, double[] X, int startX, double[] POL, int startPOL, int LPOL, char HTYPE, int NH, int[] HESSTR)
/*      */   {
/* 3757 */     double HALF = 0.5D;
/*      */ 
/* 3759 */     double VALP = DDOT(N, X, startX, 1, POL, startPOL, 1);
/*      */ 
/* 3762 */     if (LPOL > N) {
/* 3763 */       int NXT = N + 1;
/* 3764 */       for (int J = 1; J <= N; J++) {
/* 3765 */         double XJ = X[(J - 1 + startX)];
/* 3766 */         VALP += 0.5D * POL[(NXT - 1 + startPOL)] * XJ * XJ;
/* 3767 */         NXT++;
/*      */       }
/*      */ 
/* 3770 */       if (HTYPE == 'D') {
/* 3771 */         for (J = 2; J < N; J++) {
/* 3772 */           int II = J;
/* 3773 */           int JJ = 1;
/* 3774 */           for (int I = J; I < N; I++) {
/* 3775 */             VALP += X[(II - 1 + startX)] * X[(JJ - 1 + startX)] * POL[(NXT - 1 + startPOL)];
/* 3776 */             NXT++;
/* 3777 */             II++;
/* 3778 */             JJ++;
/*      */           }
/*      */         }
/*      */       }
/* 3782 */       else if (HTYPE == 'B') {
/* 3783 */         for (J = 2; J < NH; J++) {
/* 3784 */           int II = J;
/* 3785 */           int JJ = 1;
/* 3786 */           for (int I = J; I < N; I++) {
/* 3787 */             VALP += X[(II - 1 + startX)] * X[(JJ - 1 + startX)] * POL[(NXT - 1 + startPOL)];
/* 3788 */             NXT++;
/* 3789 */             II++;
/* 3790 */             JJ++;
/*      */           }
/*      */         }
/*      */       }
/*      */       else {
/* 3795 */         for (int I = 1; I <= NH + NH; I += 2) {
/* 3796 */           VALP += X[(HESSTR[(I - 1)] - 1 + startX)] * X[(HESSTR[I] - 1 + startX)] * POL[(NXT - 1 + startPOL)];
/* 3797 */           NXT++;
/*      */         }
/*      */       }
/*      */     }
/* 3801 */     return VALP;
/*      */   }
/*      */ 
/*      */   private double DDOT(int N, double[] DX, int startDX, int INCX, double[] DY, int startDY, int INCY)
/*      */   {
/* 3812 */     double DDOT = 0.0D;
/* 3813 */     if (N <= 0) return 0.0D;
/* 3814 */     if ((INCX == 1) && (INCY == 1))
/*      */     {
/* 3816 */       int M = N % 5;
/* 3817 */       if (M != 0) {
/* 3818 */         for (int I = 1; I <= M; I++) {
/* 3819 */           DDOT += DX[(I - 1 + startDX)] * DY[(I - 1 + startDY)];
/*      */         }
/* 3821 */         if (N < 5) return DDOT;
/*      */       }
/* 3823 */       int MP1 = M + 1;
/* 3824 */       for (int I = MP1; I <= N; I += 5)
/* 3825 */         DDOT = DDOT + DX[(I - 1 + startDX)] * DY[(I - 1 + startDY)] + DX[(I + startDX)] * DY[(I + startDY)] + DX[(I + 1 + startDX)] * DY[(I + 1 + startDY)] + DX[(I + 2 + startDX)] * DY[(I + 2 + startDY)] + DX[(I + 3 + startDX)] * DY[(I + 3 + startDY)];
/*      */     }
/*      */     else
/*      */     {
/* 3829 */       int IX = 1;
/* 3830 */       int IY = 1;
/* 3831 */       if (INCX < 0) IX = (-N + 1) * INCX + 1;
/* 3832 */       if (INCY < 0) IY = (-N + 1) * INCY + 1;
/* 3833 */       for (int I = 1; I <= N; I++) {
/* 3834 */         DDOT += DX[(IX - 1 + startDX)] * DY[(IY - 1 + startDY)];
/* 3835 */         IX += INCX;
/* 3836 */         IY += INCY;
/*      */       }
/*      */     }
/* 3839 */     return DDOT;
/*      */   }
/*      */ 
/*      */   private void REPLCE(int N, int IP, double[] XSET, int startXSET, double[] YSET, int startYSET, double[] XNEW, int startXNEW, double FXNEW, boolean CALCF, double[] XBASE, double FXBASE, double[] DIST, int startDIST, double[] XARCH, int startXARCH, double[] YARCH, int startYARCH)
/*      */   {
/* 3937 */     int IX = 1 + (IP - 1) * N;
/* 3938 */     ARCHIV(N, XSET, IX - 1 + startXSET, YSET[(IP - 1 + startYSET)], XARCH, startXARCH, YARCH, startYARCH);
/*      */ 
/* 3940 */     System.arraycopy(XNEW, startXNEW, XSET, IX - 1 + startXSET, N);
/*      */ 
/* 3942 */     double VALUE = 0.0D;
/* 3943 */     for (int I = 1; I <= N; I++) {
/* 3944 */       VALUE += Math.pow(XNEW[(I - 1 + startXNEW)] - XBASE[(I - 1)], 2.0D);
/*      */     }
/* 3946 */     DIST[(IP - 1 + startDIST)] = Math.sqrt(VALUE);
/*      */ 
/* 3948 */     if (CALCF) {
/* 3949 */       this.NF += 1;
/* 3950 */       if (this.NF > this.MAXNF) return;
/* 3951 */       YSET[(IP - 1 + startYSET)] = UFN(N, XSET, IX - 1 + startXSET);
/*      */     } else {
/* 3953 */       YSET[(IP - 1 + startYSET)] = FXNEW;
/*      */     }
/*      */   }
/*      */ 
/*      */   private Point NFPDAT(int N, int I, int LP)
/*      */   {
/*      */     int FIRST;
/*      */     int LENGTH;
/*      */     int FIRST;
/* 3971 */     if (I <= N) {
/* 3972 */       int LENGTH = N;
/* 3973 */       FIRST = 1 + (I - 1) * N;
/*      */     } else {
/* 3975 */       LENGTH = LP;
/* 3976 */       FIRST = 1 + N * N + (I - N - 1) * LENGTH;
/*      */     }
/* 3978 */     return new Point(FIRST, LENGTH);
/*      */   }
/*      */ 
/*      */   private double MAXABS(int N, double DELTA, double[] POL, int startPOL, int LPOL, double[] XSOL, int startXSOL, char HTYPE, int NH, int[] HESSTR, double[] W, int startW)
/*      */   {
/* 4051 */     int ITER = 100;
/*      */ 
/* 4053 */     double MONE = -1.0D;
/* 4054 */     int ITMAX = 10;
/*      */ 
/* 4056 */     int NSQ = N * N;
/* 4057 */     int N1 = 1 + NSQ;
/* 4058 */     int N2 = N1 + N;
/* 4059 */     int N3 = N2 + N;
/* 4060 */     double LAMBDA = 0.0D;
/*      */ 
/* 4062 */     MKHESS(N, POL, startPOL, LPOL, W, startW, HTYPE, NH, HESSTR);
/* 4063 */     DGQToutput dgqt = DGQT(N, W, startW, N, POL, startPOL, DELTA, 0.1D, 0.1D, 10, LAMBDA, XSOL, startXSOL, ITER, W, N1 - 1 + startW, W, N2 - 1 + startW, W, N3 - 1 + startW);
/* 4064 */     LAMBDA = dgqt.PAR;
/* 4065 */     double MINVAL = dgqt.F;
/*      */ 
/* 4067 */     DSCAL(N, -1.0D, POL, startPOL, 1);
/* 4068 */     DSCAL(NSQ, -1.0D, W, startW, 1);
/*      */ 
/* 4070 */     int N4 = N3 + N;
/* 4071 */     dgqt = DGQT(N, W, startW, N, POL, startPOL, DELTA, 0.1D, 0.1D, 10, LAMBDA, W, N4 - 1 + startW, ITER, W, N1 - 1 + startW, W, N2 - 1 + startW, W, N3 - 1 + startW);
/* 4072 */     LAMBDA = dgqt.PAR;
/* 4073 */     double MAXVAL = dgqt.F;
/* 4074 */     MAXVAL = -MAXVAL;
/*      */ 
/* 4076 */     DSCAL(N, -1.0D, POL, startPOL, 1);
/*      */     double VALUE;
/*      */     double VALUE;
/* 4078 */     if (MAXVAL > -MINVAL) {
/* 4079 */       System.arraycopy(W, N4 - 1 + startW, XSOL, startXSOL, N);
/* 4080 */       VALUE = MAXVAL;
/*      */     } else {
/* 4082 */       VALUE = MINVAL;
/*      */     }
/* 4084 */     return VALUE;
/*      */   }
/*      */ 
/*      */   private void DSCAL(int N, double DA, double[] DX, int startDX, int INCX)
/*      */   {
/* 4094 */     if ((N <= 0) || (INCX <= 0)) return;
/* 4095 */     if (INCX == 1) {
/* 4096 */       int M = N % 5;
/* 4097 */       if (M != 0) {
/* 4098 */         for (int I = 1; I <= M; I++) {
/* 4099 */           DX[(I - 1 + startDX)] = (DA * DX[(I - 1 + startDX)]);
/*      */         }
/* 4101 */         if (N < 5) return;
/*      */       }
/* 4103 */       int MP1 = M + 1;
/* 4104 */       for (int I = MP1; I <= N; I += 5) {
/* 4105 */         DX[(I - 1 + startDX)] = (DA * DX[(I - 1 + startDX)]);
/* 4106 */         DX[(I + 1 - 1 + startDX)] = (DA * DX[(I + 1 - 1 + startDX)]);
/* 4107 */         DX[(I + 2 - 1 + startDX)] = (DA * DX[(I + 2 - 1 + startDX)]);
/* 4108 */         DX[(I + 3 - 1 + startDX)] = (DA * DX[(I + 3 - 1 + startDX)]);
/* 4109 */         DX[(I + 4 - 1 + startDX)] = (DA * DX[(I + 4 - 1 + startDX)]);
/*      */       }
/*      */     } else {
/* 4112 */       int NINCX = N * INCX;
/* 4113 */       for (int I = 1; I <= NINCX; I += INCX)
/* 4114 */         DX[(I - 1 + startDX)] = (DA * DX[(I - 1 + startDX)]);
/*      */     }
/*      */   }
/*      */ 
/*      */   private double DASUM(int N, double[] DX, int startDX, int INCX)
/*      */   {
/* 4124 */     double sum = 0.0D;
/* 4125 */     for (int i = 0; i < N; i += INCX) {
/* 4126 */       sum += Math.abs(DX[(i + startDX)]);
/*      */     }
/* 4128 */     return sum;
/*      */   }
/*      */ 
/*      */   private double DESTSV(int N, double[] R, int startR, int LDR, double[] Z, int startZ)
/*      */   {
/* 4187 */     double P01 = 0.01D;
/*      */ 
/* 4191 */     for (int I = 1; I <= N; I++) {
/* 4192 */       Z[(I - 1 + startZ)] = 0.0D;
/*      */     }
/*      */ 
/* 4197 */     double E = Math.abs(R[startR]);
/* 4198 */     if (E == 0.0D) {
/* 4199 */       double SVMIN = 0.0D;
/* 4200 */       Z[startZ] = 1.0D;
/* 4201 */       return SVMIN;
/*      */     }
/*      */ 
/* 4204 */     for (I = 1; I <= N; I++) {
/* 4205 */       E = SIGN(E, -Z[(I - 1 + startZ)]);
/*      */ 
/* 4207 */       if (Math.abs(E - Z[(I - 1 + startZ)]) > Math.abs(R[((I - 1) * LDR + I - 1 + startR)])) {
/* 4208 */         double TEMP = Math.min(0.01D, Math.abs(R[((I - 1) * LDR + I - 1 + startR)]) / Math.abs(E - Z[(I - 1 + startZ)]));
/* 4209 */         DSCAL(N, TEMP, Z, startZ, 1);
/* 4210 */         E = TEMP * E;
/*      */       }
/*      */       double WM;
/*      */       double W;
/*      */       double WM;
/* 4213 */       if (R[((I - 1) * LDR + I - 1 + startR)] == 0.0D) {
/* 4214 */         double W = 1.0D;
/* 4215 */         WM = 1.0D;
/*      */       } else {
/* 4217 */         W = (E - Z[(I - 1 + startZ)]) / R[((I - 1) * LDR + I - 1 + startR)];
/* 4218 */         WM = -(E + Z[(I - 1 + startZ)]) / R[((I - 1) * LDR + I - 1 + startR)];
/*      */       }
/*      */ 
/* 4221 */       double S = Math.abs(E - Z[(I - 1 + startZ)]);
/* 4222 */       double SM = Math.abs(E + Z[(I - 1 + startZ)]);
/* 4223 */       for (int J = I + 1; J <= N; J++) {
/* 4224 */         SM += Math.abs(Z[(J - 1 + startZ)] + WM * R[((J - 1) * LDR + I - 1 + startR)]);
/*      */       }
/* 4226 */       if (I < N) {
/* 4227 */         DAXPY(N - I, W, R, I * LDR + I - 1 + startR, LDR, Z, I + startZ, 1);
/* 4228 */         S += DASUM(N - I, Z, I + startZ, 1);
/*      */       }
/* 4230 */       if (S < SM) {
/* 4231 */         double TEMP = WM - W;
/* 4232 */         W = WM;
/* 4233 */         if (I < N) DAXPY(N - I, TEMP, R, I * LDR + I - 1 + startR, LDR, Z, I + startZ, 1);
/*      */       }
/* 4235 */       Z[(I - 1 + startZ)] = W;
/*      */     }
/* 4237 */     double YNORM = DNRM2(N, Z, startZ, 1);
/*      */ 
/* 4239 */     for (int J = N; J <= 1; J--)
/*      */     {
/* 4241 */       if (Math.abs(Z[(J - 1 + startZ)]) > Math.abs(R[((J - 1) * LDR + J - 1 + startR)])) {
/* 4242 */         double TEMP = Math.min(0.01D, Math.abs(R[((J - 1) * LDR + J - 1 + startR)]) / Math.abs(Z[(J - 1 + startZ)]));
/* 4243 */         DSCAL(N, TEMP, Z, startZ, 1);
/* 4244 */         YNORM = TEMP * YNORM;
/*      */       }
/* 4246 */       if (R[((J - 1) * LDR + J - 1 + startR)] == 0.0D)
/* 4247 */         Z[(J - 1 + startZ)] = 1.0D;
/*      */       else {
/* 4249 */         Z[(J - 1 + startZ)] /= R[((J - 1) * LDR + J - 1 + startR)];
/*      */       }
/* 4251 */       double TEMP = -Z[(J - 1 + startZ)];
/* 4252 */       DAXPY(J - 1, TEMP, R, (J - 1) * LDR + startR, 1, Z, startZ, 1);
/*      */     }
/*      */ 
/* 4255 */     double ZNORM = 1.0D / DNRM2(N, Z, startZ, 1);
/* 4256 */     double SVMIN = YNORM * ZNORM;
/* 4257 */     DSCAL(N, ZNORM, Z, startZ, 1);
/* 4258 */     return SVMIN;
/*      */   }
/*      */ 
/*      */   private double SIGN(double x, double y) {
/* 4262 */     if (y >= 0.0D) {
/* 4263 */       return Math.abs(x);
/*      */     }
/* 4265 */     return -Math.abs(x);
/*      */   }
/*      */ 
/*      */   private class DGQToutput
/*      */   {
/*      */     double PAR;
/*      */     double F;
/*      */     int INFO;
/*      */ 
/*      */     public DGQToutput()
/*      */     {
/*      */     }
/*      */   }
/*      */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.DFO
 * JD-Core Version:    0.6.2
 */