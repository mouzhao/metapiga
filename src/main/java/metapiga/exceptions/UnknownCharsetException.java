/*    */ package metapiga.exceptions;
/*    */ 
/*    */ import metapiga.modelization.Charset;
/*    */ 
/*    */ public class UnknownCharsetException extends Exception
/*    */ {
/*    */   private static final long serialVersionUID = -7700608519028240174L;
/*    */ 
/*    */   public UnknownCharsetException(Charset charset)
/*    */   {
/* 15 */     super("Unknown charset:" + charset.getLabel());
/*    */   }
/*    */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.UnknownCharsetException
 * JD-Core Version:    0.6.2
 */