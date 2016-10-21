/*    */ package metapiga.exceptions;
/*    */ 
/*    */ import java.util.BitSet;
/*    */ 
/*    */ public class UnknownDataException extends Exception
/*    */ {
/*    */   public UnknownDataException(String dataType)
/*    */   {
/* 14 */     super("Unknown DataType: " + dataType);
/*    */   }
/*    */ 
/*    */   public UnknownDataException(BitSet bitset) {
/* 18 */     super("Unknown Data token from BitSet: " + bitset);
/*    */   }
/*    */ 
/*    */   public UnknownDataException(BitSet bitset, Throwable cause) {
/* 22 */     super("Unknown Data token from BitSet: " + bitset, cause);
/*    */   }
/*    */ 
/*    */   public UnknownDataException(int state) {
/* 26 */     super("Unknown Data token from state: " + state);
/*    */   }
/*    */ 
/*    */   public UnknownDataException(int state, Throwable cause) {
/* 30 */     super("Unknown Data token from state: " + state, cause);
/*    */   }
/*    */ 
/*    */   public UnknownDataException(String data, String taxa) {
/* 34 */     super("Unknown Data token: " + data + " from taxa " + taxa);
/*    */   }
/*    */ 
/*    */   public UnknownDataException(String data, String taxa, Throwable cause) {
/* 38 */     super("Unknown Data token: " + data + " from taxa " + taxa, cause);
/*    */   }
/*    */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.UnknownDataException
 * JD-Core Version:    0.6.2
 */