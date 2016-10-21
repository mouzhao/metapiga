/*    */ package metapiga.exceptions;
/*    */ 
/*    */ import java.io.PrintStream;
/*    */ import metapiga.modelization.data.DataType;
/*    */ 
/*    */ public class IncompatibleDataException extends Exception
/*    */ {
/*  8 */   String errorString = "";
/*    */ 
/*    */   public IncompatibleDataException(DataType expectedType, DataType actualType)
/*    */   {
/* 12 */     super("Expected data type mismatch. Expected type is " + expectedType.verbose() + 
/* 12 */       ", while actual type data type is " + actualType.verbose());
/*    */   }
/*    */ 
/*    */   public IncompatibleDataException(DataType[] expectedDataTypes, DataType actualDataType) {
/* 16 */     String expectedDataTypesString = "";
/* 17 */     for (DataType expectedDataType : expectedDataTypes) {
/* 18 */       expectedDataTypesString = expectedDataTypesString + expectedDataType.verbose() + ", ";
/*    */     }
/*    */ 
/* 21 */     this.errorString = 
/* 22 */       (this.errorString + "Expected data type mismatch. Expected types are " + expectedDataTypesString + 
/* 22 */       ", while actual type data type is " + actualDataType.verbose());
/*    */   }
/*    */ 
/*    */   public void printStackTrace()
/*    */   {
/* 28 */     System.err.println(this.errorString);
/* 29 */     super.printStackTrace();
/*    */   }
/*    */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.IncompatibleDataException
 * JD-Core Version:    0.6.2
 */