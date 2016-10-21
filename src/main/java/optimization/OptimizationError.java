/*    */ package optimization;
/*    */ 
/*    */ public class OptimizationError extends Exception
/*    */ {
/*    */   public OptimizationError()
/*    */   {
/*    */   }
/*    */ 
/*    */   public OptimizationError(String message)
/*    */   {
/* 15 */     super(message);
/*    */   }
/*    */ 
/*    */   public OptimizationError(Throwable cause) {
/* 19 */     super(cause);
/*    */   }
/*    */ 
/*    */   public OptimizationError(String message, Throwable cause) {
/* 23 */     super(message, cause);
/*    */   }
/*    */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.optimization.OptimizationError
 * JD-Core Version:    0.6.2
 */