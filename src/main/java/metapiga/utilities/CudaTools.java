/*    */ package metapiga.utilities;
/*    */ 
/*    */ import java.io.ByteArrayOutputStream;
/*    */ import java.io.File;
/*    */ import java.io.IOException;
/*    */ import java.io.InputStream;
/*    */ import java.io.PrintStream;
/*    */ 
/*    */ public class CudaTools
/*    */ {
/*    */   public static String preparePtxFile(String cuFileName)
/*    */     throws IOException
/*    */   {
/* 21 */     int endIndex = cuFileName.lastIndexOf('.');
/* 22 */     if (endIndex == -1)
/*    */     {
/* 24 */       endIndex = cuFileName.length() - 1;
/*    */     }
/* 26 */     String ptxFileName = cuFileName.substring(0, endIndex + 1) + "ptx";
/* 27 */     File ptxFile = new File(ptxFileName);
/* 28 */     if (ptxFile.exists())
/*    */     {
/* 30 */       return ptxFileName;
/*    */     }
/*    */ 
/* 33 */     File cuFile = new File(cuFileName);
/* 34 */     if (!cuFile.exists())
/*    */     {
/* 36 */       throw new IOException("Input file not found: " + cuFileName);
/*    */     }
/* 38 */     String modelString = "-m" + System.getProperty("sun.arch.data.model");
/*    */ 
/* 42 */     String command = 
/* 43 */       "nvcc " + modelString + " -ptx " + 
/* 44 */       cuFile.getPath() + " -o " + ptxFileName;
/*    */ 
/* 46 */     System.out.println("Executing\n" + command);
/* 47 */     Process process = Runtime.getRuntime().exec(command);
/*    */ 
/* 49 */     String errorMessage = 
/* 50 */       new String(toByteArray(process.getErrorStream()));
/* 51 */     String outputMessage = 
/* 52 */       new String(toByteArray(process.getInputStream()));
/* 53 */     int exitValue = 0;
/*    */     try
/*    */     {
/* 56 */       exitValue = process.waitFor();
/*    */     }
/*    */     catch (InterruptedException e)
/*    */     {
/* 60 */       Thread.currentThread().interrupt();
/* 61 */       throw new IOException(
/* 62 */         "Interrupted while waiting for nvcc output", e);
/*    */     }
/*    */ 
/* 65 */     if (exitValue != 0)
/*    */     {
/* 67 */       System.out.println("nvcc process exitValue " + exitValue);
/* 68 */       System.out.println("errorMessage:\n" + errorMessage);
/* 69 */       System.out.println("outputMessage:\n" + outputMessage);
/* 70 */       throw new IOException(
/* 71 */         "Could not create .ptx file: " + errorMessage);
/*    */     }
/*    */ 
/* 74 */     System.out.println("Finished creating PTX file");
/* 75 */     return ptxFileName;
/*    */   }
/*    */ 
/*    */   private static byte[] toByteArray(InputStream inputStream)
/*    */     throws IOException
/*    */   {
/* 88 */     ByteArrayOutputStream baos = new ByteArrayOutputStream();
/* 89 */     byte[] buffer = new byte[8192];
/*    */     while (true)
/*    */     {
/* 92 */       int read = inputStream.read(buffer);
/* 93 */       if (read == -1)
/*    */       {
/*    */         break;
/*    */       }
/* 97 */       baos.write(buffer, 0, read);
/*    */     }
/* 99 */     return baos.toByteArray();
/*    */   }
/*    */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.utilities.CudaTools
 * JD-Core Version:    0.6.2
 */