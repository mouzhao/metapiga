/*    */ package modelization.data;
/*    */ 
/*    */ import java.awt.Color;
/*    */ import java.util.BitSet;
/*    */ import metapiga.exceptions.UnknownDataException;
/*    */ 
/*    */ public enum Standard
/*    */   implements Data
/*    */ {
/* 15 */   _0(true, false, 0, Color.RED), 
/* 16 */   _1(false, true, 1, Color.BLUE), 
/* 17 */   X(true, true, -1, new Color(255, 20, 147));
/*    */ 
/*    */   private final BitSet bits;
/*    */   private final Color color;
/*    */   public final int state;
/*    */ 
/* 24 */   private Standard(boolean _0, boolean _1, int state, Color color) { this.state = state;
/* 25 */     this.color = color;
/* 26 */     this.bits = new BitSet(2);
/* 27 */     this.bits.set(0, _0);
/* 28 */     this.bits.set(1, _1); }
/*    */ 
/*    */   public String toString()
/*    */   {
/* 32 */     return this.state;
/*    */   }
/*    */   public char toChar() {
/* 35 */     switch (this.state) {
/*    */     case 0:
/* 37 */       return '0';
/*    */     case 1:
/* 39 */       return '1';
/*    */     case -1:
/*    */     }
/* 42 */     return 'X';
/*    */   }
/*    */ 
/*    */   public final BitSet toBits() {
/* 46 */     return this.bits; } 
/* 47 */   public final int numOfStates() { return this.bits.cardinality(); } 
/* 48 */   public final int getMaxStates() { return 2; } 
/* 49 */   public final int getState() { return this.state; } 
/* 50 */   public final Color getColor() { return this.color; } 
/* 51 */   public final boolean isState(int state) { return this.bits.get(state); } 
/* 52 */   public final boolean isUndeterminate() { return false; }
/*    */ 
/*    */ 
/*    */   public static Standard getStandardWithState(int state)
/*    */     throws UnknownDataException
/*    */   {
/* 61 */     for (Standard standard : values()) {
/* 62 */       if (standard.state == state) {
/* 63 */         return standard;
/*    */       }
/*    */     }
/* 66 */     throw new UnknownDataException(state);
/*    */   }
/*    */ 
/*    */   public static Standard getStandard(BitSet bitSet)
/*    */     throws UnknownDataException
/*    */   {
/* 76 */     for (Standard standard : values()) {
/* 77 */       if (standard.bits.equals(bitSet)) {
/* 78 */         return standard;
/*    */       }
/*    */     }
/* 81 */     throw new UnknownDataException(bitSet);
/*    */   }
/*    */ 
/*    */   public static int getStateOf(String standard)
/*    */   {
/*    */     try
/*    */     {
/* 91 */       return Integer.parseInt(standard); } catch (NumberFormatException e) {
/*    */     }
/* 93 */     return -1;
/*    */   }
/*    */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.modelization.data.Standard
 * JD-Core Version:    0.6.2
 */