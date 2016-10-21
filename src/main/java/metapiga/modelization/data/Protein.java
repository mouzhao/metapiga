/*     */ package metapiga.modelization.data;
/*     */ 
/*     */ import java.awt.Color;
/*     */ import java.util.BitSet;
/*     */ import metapiga.exceptions.UnknownDataException;
/*     */ 
/*     */ public enum Protein
/*     */   implements Data
/*     */ {
/*  15 */   A(0, Color.BLUE), 
/*  16 */   R(1, Color.GREEN), 
/*  17 */   N(2, new Color(46, 139, 87)), 
/*  18 */   D(3, new Color(127, 255, 0)), 
/*  19 */   C(4, Color.YELLOW), 
/*  20 */   Q(5, new Color(30, 250, 100)), 
/*  21 */   E(6, new Color(202, 255, 112)), 
/*  22 */   G(7, new Color(16, 78, 139)), 
/*  23 */   H(8, new Color(0, 250, 154)), 
/*  24 */   I(9, new Color(0, 191, 255)), 
/*  25 */   L(10, new Color(103, 148, 255)), 
/*  26 */   K(11, new Color(145, 214, 134)), 
/*  27 */   M(12, new Color(219, 219, 112)), 
/*  28 */   F(13, Color.RED), 
/*  29 */   P(14, Color.CYAN), 
/*  30 */   S(15, new Color(127, 255, 212)), 
/*  31 */   T(16, new Color(134, 206, 189)), 
/*  32 */   W(17, Color.ORANGE), 
/*  33 */   Y(18, new Color(255, 127, 0)), 
/*  34 */   V(19, new Color(138, 43, 226)), 
/*  35 */   B(-1, new Color(255, 20, 147)), 
/*  36 */   Z(-1, new Color(255, 20, 147)), 
/*  37 */   J(-1, new Color(255, 20, 147)), 
/*  38 */   X(-1, new Color(255, 20, 147));
/*     */ 
/*     */   private final BitSet bits;
/*     */   private final Color color;
/*     */   public final int state;
/*     */ 
/*  46 */   private Protein(int state, Color color) { this.state = state;
/*  47 */     this.color = color;
/*  48 */     this.bits = new BitSet(20);
/*  49 */     if (state >= 0) {
/*  50 */       this.bits.set(state);
/*  51 */     } else if (name().equals("X")) {
/*  52 */       this.bits.set(0, 20);
/*  53 */     } else if (name().equals("B")) {
/*  54 */       this.bits.set(2);
/*  55 */       this.bits.set(3);
/*  56 */     } else if (name().equals("Z")) {
/*  57 */       this.bits.set(5);
/*  58 */       this.bits.set(6);
/*  59 */     } else if (name().equals("J")) {
/*  60 */       this.bits.set(9);
/*  61 */       this.bits.set(10);
/*     */     } }
/*     */ 
/*     */   public String toString() {
/*  65 */     return name(); } 
/*  66 */   public char toChar() { return name().charAt(0); } 
/*     */   public final BitSet toBits() {
/*  68 */     return this.bits; } 
/*  69 */   public final int numOfStates() { return this.bits.cardinality(); } 
/*  70 */   public final int getMaxStates() { return 20; } 
/*  71 */   public final int getState() { return this.state; } 
/*  72 */   public final Color getColor() { return this.color; } 
/*  73 */   public final boolean isState(int state) { return this.bits.get(state); } 
/*  74 */   public final boolean isUndeterminate() { return this == X; }
/*     */ 
/*     */ 
/*     */   public static Protein getProteinWithState(int state)
/*     */     throws UnknownDataException
/*     */   {
/*  83 */     for (Protein protein : values()) {
/*  84 */       if (protein.state == state) {
/*  85 */         return protein;
/*     */       }
/*     */     }
/*  88 */     throw new UnknownDataException(state);
/*     */   }
/*     */ 
/*     */   public static Protein getProtein(BitSet bitSet)
/*     */     throws UnknownDataException
/*     */   {
/*  98 */     for (Protein protein : values()) {
/*  99 */       if (protein.bits.equals(bitSet)) {
/* 100 */         return protein;
/*     */       }
/*     */     }
/* 103 */     throw new UnknownDataException(bitSet);
/*     */   }
/*     */ 
/*     */   public static int getStateOf(String protein)
/*     */   {
/* 112 */     return valueOf(protein).state;
/*     */   }
/*     */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.Protein
 * JD-Core Version:    0.6.2
 */