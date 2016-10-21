/*     */ package modelization.data;
/*     */ 
/*     */ import java.awt.Color;
/*     */ import java.util.BitSet;
/*     */ import metapiga.exceptions.UnknownDataException;
/*     */ 
/*     */ public enum Codon
/*     */   implements Data
/*     */ {
/*  16 */   TTT(0, Color.getHSBColor(0.0F, 1.0F, 1.0F)), 
/*  17 */   TTC(1, Color.getHSBColor(0.015625F, 1.0F, 1.0F)), 
/*  18 */   TTA(2, Color.getHSBColor(0.03125F, 1.0F, 1.0F)), 
/*  19 */   TTG(3, Color.getHSBColor(0.046875F, 1.0F, 1.0F)), 
/*  20 */   TCT(4, Color.getHSBColor(0.0625F, 1.0F, 1.0F)), 
/*  21 */   TCC(5, Color.getHSBColor(0.078125F, 1.0F, 1.0F)), 
/*  22 */   TCA(6, Color.getHSBColor(0.09375F, 1.0F, 1.0F)), 
/*  23 */   TCG(7, Color.getHSBColor(0.109375F, 1.0F, 1.0F)), 
/*  24 */   TAT(8, Color.getHSBColor(0.125F, 1.0F, 1.0F)), 
/*  25 */   TAC(9, Color.getHSBColor(0.140625F, 1.0F, 1.0F)), 
/*  26 */   TAA(10, Color.getHSBColor(0.15625F, 1.0F, 1.0F)), 
/*  27 */   TAG(11, Color.getHSBColor(0.171875F, 1.0F, 1.0F)), 
/*  28 */   TGT(12, Color.getHSBColor(0.1875F, 1.0F, 1.0F)), 
/*  29 */   TGC(13, Color.getHSBColor(0.203125F, 1.0F, 1.0F)), 
/*  30 */   TGA(14, Color.getHSBColor(0.21875F, 1.0F, 1.0F)), 
/*  31 */   TGG(15, Color.getHSBColor(0.234375F, 1.0F, 1.0F)), 
/*  32 */   CTT(16, Color.getHSBColor(0.25F, 1.0F, 1.0F)), 
/*  33 */   CTC(17, Color.getHSBColor(0.265625F, 1.0F, 1.0F)), 
/*  34 */   CTA(18, Color.getHSBColor(0.28125F, 1.0F, 1.0F)), 
/*  35 */   CTG(19, Color.getHSBColor(0.296875F, 1.0F, 1.0F)), 
/*  36 */   CCT(20, Color.getHSBColor(0.3125F, 1.0F, 1.0F)), 
/*  37 */   CCC(21, Color.getHSBColor(0.328125F, 1.0F, 1.0F)), 
/*  38 */   CCA(22, Color.getHSBColor(0.34375F, 1.0F, 1.0F)), 
/*  39 */   CCG(23, Color.getHSBColor(0.359375F, 1.0F, 1.0F)), 
/*  40 */   CAT(24, Color.getHSBColor(0.375F, 1.0F, 1.0F)), 
/*  41 */   CAC(25, Color.getHSBColor(0.390625F, 1.0F, 1.0F)), 
/*  42 */   CAA(26, Color.getHSBColor(0.40625F, 1.0F, 1.0F)), 
/*  43 */   CAG(27, Color.getHSBColor(0.421875F, 1.0F, 1.0F)), 
/*  44 */   CGT(28, Color.getHSBColor(0.4375F, 1.0F, 1.0F)), 
/*  45 */   CGC(29, Color.getHSBColor(0.453125F, 1.0F, 1.0F)), 
/*  46 */   CGA(30, Color.getHSBColor(0.46875F, 1.0F, 1.0F)), 
/*  47 */   CGG(31, Color.getHSBColor(0.484375F, 1.0F, 1.0F)), 
/*  48 */   ATT(32, Color.getHSBColor(0.5F, 1.0F, 1.0F)), 
/*  49 */   ATC(33, Color.getHSBColor(0.515625F, 1.0F, 1.0F)), 
/*  50 */   ATA(34, Color.getHSBColor(0.53125F, 1.0F, 1.0F)), 
/*  51 */   ATG(35, Color.getHSBColor(0.546875F, 1.0F, 1.0F)), 
/*  52 */   ACT(36, Color.getHSBColor(0.5625F, 1.0F, 1.0F)), 
/*  53 */   ACC(37, Color.getHSBColor(0.578125F, 1.0F, 1.0F)), 
/*  54 */   ACA(38, Color.getHSBColor(0.59375F, 1.0F, 1.0F)), 
/*  55 */   ACG(39, Color.getHSBColor(0.609375F, 1.0F, 1.0F)), 
/*  56 */   AAT(40, Color.getHSBColor(0.625F, 1.0F, 1.0F)), 
/*  57 */   AAC(41, Color.getHSBColor(0.640625F, 1.0F, 1.0F)), 
/*  58 */   AAA(42, Color.getHSBColor(0.65625F, 1.0F, 1.0F)), 
/*  59 */   AAG(43, Color.getHSBColor(0.671875F, 1.0F, 1.0F)), 
/*  60 */   AGT(44, Color.getHSBColor(0.6875F, 1.0F, 1.0F)), 
/*  61 */   AGC(45, Color.getHSBColor(0.703125F, 1.0F, 1.0F)), 
/*  62 */   AGA(46, Color.getHSBColor(0.71875F, 1.0F, 1.0F)), 
/*  63 */   AGG(47, Color.getHSBColor(0.734375F, 1.0F, 1.0F)), 
/*  64 */   GTT(48, Color.getHSBColor(0.75F, 1.0F, 1.0F)), 
/*  65 */   GTC(49, Color.getHSBColor(0.765625F, 1.0F, 1.0F)), 
/*  66 */   GTA(50, Color.getHSBColor(0.78125F, 1.0F, 1.0F)), 
/*  67 */   GTG(51, Color.getHSBColor(0.796875F, 1.0F, 1.0F)), 
/*  68 */   GCT(52, Color.getHSBColor(0.8125F, 1.0F, 1.0F)), 
/*  69 */   GCC(53, Color.getHSBColor(0.828125F, 1.0F, 1.0F)), 
/*  70 */   GCA(54, Color.getHSBColor(0.84375F, 1.0F, 1.0F)), 
/*  71 */   GCG(55, Color.getHSBColor(0.859375F, 1.0F, 1.0F)), 
/*  72 */   GAT(56, Color.getHSBColor(0.875F, 1.0F, 1.0F)), 
/*  73 */   GAC(57, Color.getHSBColor(0.890625F, 1.0F, 1.0F)), 
/*  74 */   GAA(58, Color.getHSBColor(0.90625F, 1.0F, 1.0F)), 
/*  75 */   GAG(59, Color.getHSBColor(0.921875F, 1.0F, 1.0F)), 
/*  76 */   GGT(60, Color.getHSBColor(0.9375F, 1.0F, 1.0F)), 
/*  77 */   GGC(61, Color.getHSBColor(0.953125F, 1.0F, 1.0F)), 
/*  78 */   GGA(62, Color.getHSBColor(0.96875F, 1.0F, 1.0F)), 
/*  79 */   GGG(63, Color.getHSBColor(0.984375F, 1.0F, 1.0F)), 
/*  80 */   __X(-1, new Color(255, 20, 147));
/*     */ 
/*     */   private final BitSet bits;
/*     */   private final Color color;
/*     */   public final int state;
/*     */ 
/*  88 */   private Codon(int state, Color color) { this.state = state;
/*  89 */     this.color = color;
/*  90 */     this.bits = new BitSet(64);
/*  91 */     if (state >= 0)
/*  92 */       this.bits.set(state);
/*  93 */     else if (name().equals("__X"))
/*  94 */       this.bits.set(0, 64); }
/*     */ 
/*     */   public String toString()
/*     */   {
/*  98 */     return name(); } 
/*  99 */   public char toChar() { return name().charAt(0); } 
/*     */   public final BitSet toBits() {
/* 101 */     return this.bits; } 
/* 102 */   public final int numOfStates() { return this.bits.cardinality(); } 
/* 103 */   public final int getMaxStates() { return 64; } 
/* 104 */   public final int getState() { return this.state; } 
/* 105 */   public final Color getColor() { return this.color; } 
/* 106 */   public final boolean isState(int state) { return this.bits.get(state); } 
/* 107 */   public final boolean isUndeterminate() { return this == __X; }
/*     */ 
/*     */   public final DNA[] getNucleotides() {
/* 110 */     DNA[] nucleotides = new DNA[3];
/* 111 */     if (equals(__X)) {
/* 112 */       for (int i = 0; i < nucleotides.length; i++) {
/* 113 */         nucleotides[i] = DNA.N;
/*     */       }
/* 115 */       return nucleotides;
/*     */     }
/* 117 */     String stringCodon = toString();
/* 118 */     String[] stringNucleotides = stringCodon.split("");
/* 119 */     int i = 0;
/* 120 */     for (String s : stringNucleotides) {
/* 121 */       if (!s.contentEquals("")) {
/* 122 */         nucleotides[i] = DNA.valueOf(s);
/* 123 */         i++;
/*     */       }
/*     */     }
/* 126 */     return nucleotides;
/*     */   }
/*     */ 
/*     */   public static Codon getCodonWithState(int state)
/*     */     throws UnknownDataException
/*     */   {
/* 136 */     for (Codon codon : values()) {
/* 137 */       if (codon.state == state) {
/* 138 */         return codon;
/*     */       }
/*     */     }
/* 141 */     throw new UnknownDataException(state);
/*     */   }
/*     */ 
/*     */   public static Codon getCodon(BitSet bitSet)
/*     */     throws UnknownDataException
/*     */   {
/* 151 */     for (Codon codon : values()) {
/* 152 */       if (codon.bits.equals(bitSet)) {
/* 153 */         return codon;
/*     */       }
/*     */     }
/* 156 */     throw new UnknownDataException(bitSet);
/*     */   }
/*     */ 
/*     */   public static int getStateOf(String codon)
/*     */   {
/* 165 */     return valueOf(codon).state;
/*     */   }
/*     */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.modelization.data.Codon
 * JD-Core Version:    0.6.2
 */