/*    */ package metapiga.modelization.data;
/*    */ 
/*    */ import java.awt.Color;
/*    */ import java.util.BitSet;
/*    */ import metapiga.exceptions.UnknownDataException;
/*    */ 
/*    */ public enum DNA
/*    */   implements Data
/*    */ {
/* 15 */   A(true, false, false, false, 0, Color.GREEN), 
/* 16 */   C(false, true, false, false, 1, Color.BLUE), 
/* 17 */   G(false, false, true, false, 2, Color.YELLOW), 
/* 18 */   T(false, false, false, true, 3, Color.RED), 
/* 19 */   R(true, false, true, false, -1, new Color(255, 20, 147)), 
/* 20 */   Y(false, true, false, true, -1, new Color(255, 20, 147)), 
/* 21 */   W(true, false, false, true, -1, new Color(255, 20, 147)), 
/* 22 */   S(false, true, true, false, -1, new Color(255, 20, 147)), 
/* 23 */   M(true, true, false, false, -1, new Color(255, 20, 147)), 
/* 24 */   K(false, false, true, true, -1, new Color(255, 20, 147)), 
/* 25 */   B(false, true, true, true, -1, new Color(255, 20, 147)), 
/* 26 */   D(true, false, true, true, -1, new Color(255, 20, 147)), 
/* 27 */   H(true, true, false, true, -1, new Color(255, 20, 147)), 
/* 28 */   V(true, true, true, false, -1, new Color(255, 20, 147)), 
/* 29 */   N(true, true, true, true, -1, new Color(255, 20, 147));
/*    */ 
/*    */   private final BitSet bits;
/*    */   private final Color color;
/*    */   public final int state;
/*    */ 
/* 36 */   private DNA(boolean A, boolean C, boolean G, boolean T, int state, Color color) { this.state = state;
/* 37 */     this.color = color;
/* 38 */     this.bits = new BitSet(4);
/* 39 */     this.bits.set(0, A);
/* 40 */     this.bits.set(1, C);
/* 41 */     this.bits.set(2, G);
/* 42 */     this.bits.set(3, T); }
/*    */ 
/*    */   public String toString() {
/* 45 */     return name(); } 
/* 46 */   public char toChar() { return name().charAt(0); } 
/*    */   public final BitSet toBits() {
/* 48 */     return this.bits; } 
/* 49 */   public final int numOfStates() { return this.bits.cardinality(); } 
/* 50 */   public final int getMaxStates() { return 4; } 
/* 51 */   public final int getState() { return this.state; } 
/* 52 */   public final Color getColor() { return this.color; } 
/* 53 */   public final boolean isState(int state) { return this.bits.get(state); } 
/* 54 */   public final boolean isUndeterminate() { return this == N; }
/*    */ 
/*    */ 
/*    */   public static DNA getDNAWithState(int state)
/*    */     throws UnknownDataException
/*    */   {
/* 63 */     for (DNA dna : values()) {
/* 64 */       if (dna.state == state) {
/* 65 */         return dna;
/*    */       }
/*    */     }
/* 68 */     throw new UnknownDataException(state);
/*    */   }
/*    */ 
/*    */   public static DNA getDNA(BitSet bitSet)
/*    */     throws UnknownDataException
/*    */   {
/* 78 */     for (DNA dna : values()) {
/* 79 */       if (dna.bits.equals(bitSet)) {
/* 80 */         return dna;
/*    */       }
/*    */     }
/* 83 */     throw new UnknownDataException(bitSet);
/*    */   }
/*    */ 
/*    */   public static int getStateOf(String dna)
/*    */   {
/* 92 */     return valueOf(dna).state;
/*    */   }
/*    */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.DNA
 * JD-Core Version:    0.6.2
 */