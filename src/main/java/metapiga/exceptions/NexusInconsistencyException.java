/*    */ package metapiga.exceptions;
/*    */ 
/*    */ public class NexusInconsistencyException extends Exception
/*    */ {
/*    */   public NexusInconsistencyException()
/*    */   {
/*    */   }
/*    */ 
/*    */   public NexusInconsistencyException(String message)
/*    */   {
/* 15 */     super(message);
/*    */   }
/*    */ 
/*    */   public NexusInconsistencyException(Throwable cause) {
/* 19 */     super(cause);
/*    */   }
/*    */ 
/*    */   public NexusInconsistencyException(String message, Throwable cause) {
/* 23 */     super(message, cause);
/*    */   }
/*    */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.NexusInconsistencyException
 * JD-Core Version:    0.6.2
 */