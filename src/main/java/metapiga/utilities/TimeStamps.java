/*    */ package metapiga.utilities;
/*    */ 
/*    */ import java.io.BufferedWriter;
/*    */ import java.io.File;
/*    */ import java.io.FileWriter;
/*    */ import java.io.PrintStream;
/*    */ 
/*    */ public class TimeStamps
/*    */ {
/*    */   private long startTime;
/*    */   private BufferedWriter fileOut;
/* 13 */   private boolean alreadyReset = false;
/*    */ 
/*    */   public TimeStamps(String filename) {
/* 16 */     this.startTime = 0L;
/* 17 */     initStampFile(filename);
/*    */   }
/*    */ 
/*    */   public void resetTime()
/*    */   {
/* 22 */     if (!this.alreadyReset) {
/* 23 */       this.startTime = System.nanoTime();
/* 24 */       this.alreadyReset = true;
/*    */     }
/*    */   }
/*    */ 
/*    */   private void initStampFile(String filename) {
/*    */     try {
/* 30 */       this.fileOut = new BufferedWriter(new FileWriter(new File(
/* 31 */         filename), true));
/*    */     } catch (Exception e) {
/* 33 */       System.out.println("TimeStamps cannot open stamp file");
/*    */     }
/*    */   }
/*    */ 
/*    */   public void timeStamp()
/*    */   {
/* 40 */     long endTime = System.nanoTime();
/* 41 */     this.alreadyReset = false;
/* 42 */     double timePassed = endTime - this.startTime;
/* 43 */     timePassed /= 1000000000.0D;
/*    */     try
/*    */     {
/* 46 */       this.fileOut.write(timePassed);
/* 47 */       this.fileOut.newLine();
/* 48 */       this.fileOut.flush();
/*    */     } catch (Exception e) {
/* 50 */       System.out.println("TimeStamps cannot write to stamp file");
/*    */     }
/*    */   }
/*    */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.utilities.TimeStamps
 * JD-Core Version:    0.6.2
 */