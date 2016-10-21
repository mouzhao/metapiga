/*    */ package metapiga.exceptions;
/*    */ 
/*    */ import metapiga.modelization.Charset;
/*    */ 
/*    */ public class CharsetIntersectionException extends Exception
/*    */ {
/*    */   public CharsetIntersectionException(Charset c1, Charset c2)
/*    */   {
/* 14 */     super("Charset " + c1 + " intersection with charset " + c2 + " is not empty.");
/*    */   }
/*    */ 
/*    */   public CharsetIntersectionException(Charset c1, Charset c2, String message) {
/* 18 */     super("Charset " + c1 + " intersection with charset " + c2 + " is not empty. " + message);
/*    */   }
/*    */ 
/*    */   public CharsetIntersectionException(Charset c1, Charset c2, Throwable cause) {
/* 22 */     super("Charset " + c1 + " intersection with charset " + c2 + " is not empty.", cause);
/*    */   }
/*    */ 
/*    */   public CharsetIntersectionException(Charset c1, Charset c2, String message, Throwable cause) {
/* 26 */     super("Charset " + c1 + " intersection with charset " + c2 + " is not empty. " + message, cause);
/*    */   }
/*    */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.CharsetIntersectionException
 * JD-Core Version:    0.6.2
 */