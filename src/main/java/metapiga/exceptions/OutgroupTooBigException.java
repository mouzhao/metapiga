/*    */ package metapiga.exceptions;
/*    */ 
/*    */ public class OutgroupTooBigException extends Exception
/*    */ {
/*    */   public OutgroupTooBigException()
/*    */   {
/* 12 */     super("Outgroup is too big, ingroup must contain at least 2 taxas");
/*    */   }
/*    */ 
/*    */   public OutgroupTooBigException(Throwable cause) {
/* 16 */     super("Outgroup is too big, ingroup must contain at least 2 taxas", cause);
/*    */   }
/*    */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.OutgroupTooBigException
 * JD-Core Version:    0.6.2
 */