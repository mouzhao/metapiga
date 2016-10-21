/*    */ package metapiga.exceptions;
/*    */ 
/*    */ public class NoGPUException extends Exception
/*    */ {
/*    */   public NoGPUException()
/*    */   {
/*  8 */     super("You've picked the graphical card calculation option, but MetaPIGA didn't detect one." + System.getProperty("line.separator") + 
/*  8 */       "Calculations will be performed on the CPU.");
/*    */   }
/*    */ 
/*    */   public NoGPUException(Throwable cause)
/*    */   {
/* 13 */     super("You've picked the graphical card calculation option, but MetaPIGA didn't detect one." + System.getProperty("line.separator") + 
/* 13 */       "Calculations will be performed on the CPU.", cause);
/*    */   }
/*    */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.NoGPUException
 * JD-Core Version:    0.6.2
 */