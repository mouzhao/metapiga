/*     */ package metapiga.utilities;
/*     */ 
/*     */ import Jama.Matrix;
/*     */ import java.io.BufferedReader;
/*     */ import java.io.FileInputStream;
/*     */ import java.io.FileNotFoundException;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.io.InputStreamReader;
/*     */ import java.io.PrintStream;
/*     */ import java.io.PrintWriter;
/*     */ import java.nio.charset.Charset;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import metapiga.modelization.likelihood.SequenceArrays;
/*     */ import metapiga.trees.Node;
/*     */ import metapiga.trees.Tree;
/*     */ 
/*     */ public class SequenceDebugging
/*     */ {
/*     */   public static void printSequence(SequenceArrays s, String toFileName, Tree tree, Map<Node, Integer> nodeIdxs)
/*     */   {
/*  22 */     PrintWriter outFile = null;
/*  23 */     System.err.println("Debugging function");
/*     */     try
/*     */     {
/*  26 */       outFile = new PrintWriter(toFileName);
/*     */     } catch (FileNotFoundException e) {
/*  28 */       e.printStackTrace();
/*     */     }
/*     */ 
/*  31 */     int numNodes = s.getNodeCount();
/*  32 */     int numCats = s.getCategoryCount();
/*  33 */     int numSites = s.getCharacterCountNoPadding();
/*  34 */     int numStates = s.getStateCount();
/*     */ 
/*  36 */     String text = "";
/*     */ 
/*  38 */     List nodes = tree.getInodes();
/*  39 */     for (Node nod : nodes) {
/*  40 */       int n = ((Integer)nodeIdxs.get(nod)).intValue();
/*  41 */       String catText = "";
/*  42 */       for (int cat = 0; cat < numCats; cat++) {
/*  43 */         String siteText = "";
/*  44 */         for (int site = 0; site < numSites; site++) {
/*  45 */           String stateText = "";
/*  46 */           for (int state = 0; state < numStates; state++) {
/*  47 */             float val = s.getElement(n, cat, site, state);
/*  48 */             if (!stateText.contentEquals(""))
/*  49 */               stateText = stateText + "," + val;
/*     */             else {
/*  51 */               stateText = stateText + val;
/*     */             }
/*     */           }
/*  54 */           if (!siteText.contentEquals(""))
/*  55 */             siteText = siteText + "I" + stateText;
/*     */           else {
/*  57 */             siteText = siteText + stateText;
/*     */           }
/*     */         }
/*  60 */         if (!catText.contentEquals(""))
/*  61 */           catText = catText + ":" + siteText;
/*     */         else {
/*  63 */           catText = catText + siteText;
/*     */         }
/*     */       }
/*  66 */       if (!text.contentEquals(""))
/*  67 */         text = text + "/" + catText;
/*     */       else {
/*  69 */         text = text + catText;
/*     */       }
/*     */     }
/*  72 */     outFile.println(text);
/*  73 */     outFile.close();
/*     */   }
/*     */ 
/*     */   public static void printJamaMatrixForMatlab(Matrix matrix, String toFileName, String matName) {
/*  77 */     PrintWriter outFile = null;
/*  78 */     System.err.println("Debugging function");
/*     */     try {
/*  80 */       outFile = new PrintWriter(toFileName);
/*     */     } catch (FileNotFoundException e) {
/*  82 */       e.printStackTrace();
/*     */     }
/*     */ 
/*  85 */     int r = matrix.getRowDimension();
/*  86 */     int c = matrix.getColumnDimension();
/*  87 */     outFile.println(matName + "=[");
/*  88 */     String rowString = "";
/*  89 */     for (int row = 0; row < r; row++) {
/*  90 */       rowString = "[";
/*  91 */       for (int column = 0; column < c; column++) {
/*  92 */         if (rowString.contentEquals("["))
/*  93 */           rowString = rowString + matrix.get(row, column);
/*     */         else {
/*  95 */           rowString = rowString + ", " + matrix.get(row, column);
/*     */         }
/*     */       }
/*  98 */       if (row == r - 1) rowString = rowString + "]"; else
/*  99 */         rowString = rowString + "],";
/* 100 */       outFile.println(rowString);
/*     */     }
/* 102 */     outFile.println("];");
/*     */ 
/* 104 */     outFile.flush();
/* 105 */     outFile.close();
/*     */   }
/*     */ 
/*     */   public static void printArrayMatrixForMatlab(double[][] matrix, String toFileName, String matrixName) {
/* 109 */     PrintWriter outFile = null;
/* 110 */     System.err.println("Debugging function");
/*     */     try {
/* 112 */       outFile = new PrintWriter(toFileName);
/*     */     } catch (FileNotFoundException e) {
/* 114 */       e.printStackTrace();
/*     */     }
/*     */ 
/* 117 */     int r = matrix.length;
/* 118 */     int c = matrix[0].length;
/* 119 */     outFile.println(matrixName + "=[");
/* 120 */     String rowString = "";
/* 121 */     for (int row = 0; row < r; row++) {
/* 122 */       rowString = "[";
/* 123 */       for (int column = 0; column < c; column++) {
/* 124 */         if (rowString.contentEquals("["))
/* 125 */           rowString = rowString + matrix[row][column];
/*     */         else {
/* 127 */           rowString = rowString + ", " + matrix[row][column];
/*     */         }
/*     */       }
/* 130 */       if (row == r - 1) rowString = rowString + "]"; else
/* 131 */         rowString = rowString + "],";
/* 132 */       outFile.println(rowString);
/*     */     }
/* 134 */     outFile.println("];");
/*     */ 
/* 136 */     outFile.flush();
/* 137 */     outFile.close();
/*     */   }
/*     */ 
/*     */   public static void printArrayMatrixForMatlab(float[][] matrix, String toFileName, String matrixName) {
/* 141 */     PrintWriter outFile = null;
/* 142 */     System.err.println("Debugging function");
/*     */     try {
/* 144 */       outFile = new PrintWriter(toFileName);
/*     */     } catch (FileNotFoundException e) {
/* 146 */       e.printStackTrace();
/*     */     }
/*     */ 
/* 149 */     int r = matrix.length;
/* 150 */     int c = matrix[0].length;
/* 151 */     outFile.println(matrixName + "=[");
/* 152 */     String rowString = "";
/* 153 */     for (int row = 0; row < r; row++) {
/* 154 */       rowString = "[";
/* 155 */       for (int column = 0; column < c; column++) {
/* 156 */         if (rowString.contentEquals("["))
/* 157 */           rowString = rowString + matrix[row][column];
/*     */         else {
/* 159 */           rowString = rowString + ", " + matrix[row][column];
/*     */         }
/*     */       }
/* 162 */       if (row == r - 1) rowString = rowString + "]"; else
/* 163 */         rowString = rowString + "],";
/* 164 */       outFile.println(rowString);
/*     */     }
/* 166 */     outFile.println("];");
/*     */ 
/* 168 */     outFile.flush();
/* 169 */     outFile.close();
/*     */   }
/*     */ 
/*     */   public static void printArrayVectorForMatlab(double[] matrix, String toFileName, String matrixName) {
/* 173 */     PrintWriter outFile = null;
/* 174 */     System.err.println("Debugging function");
/*     */     try {
/* 176 */       outFile = new PrintWriter(toFileName);
/*     */     } catch (FileNotFoundException e) {
/* 178 */       e.printStackTrace();
/*     */     }
/*     */ 
/* 181 */     int r = matrix.length;
/* 182 */     outFile.println(matrixName + "=[");
/* 183 */     String rowString = "";
/* 184 */     for (int row = 0; row < r; row++) {
/* 185 */       rowString = rowString + matrix[row];
/* 186 */       if (row != r - 1) {
/* 187 */         rowString = rowString + ",";
/*     */       }
/*     */     }
/* 190 */     outFile.println(rowString);
/* 191 */     outFile.println("];");
/*     */ 
/* 193 */     outFile.flush();
/* 194 */     outFile.close();
/*     */   }
/*     */ 
/*     */   public static void calculateSequenceDiff(String seq1, String seq2, String result, boolean equals, float delta)
/*     */   {
/* 201 */     String line = "";
/*     */ 
/* 205 */     String line2 = "";
/*     */ 
/* 207 */     PrintWriter outFile = null;
/* 208 */     System.err.println("Debugging function");
/*     */     try {
/* 210 */       outFile = new PrintWriter(result);
/*     */ 
/* 212 */       InputStream fis1 = new FileInputStream(seq1);
/* 213 */       InputStream fis2 = new FileInputStream(seq2);
/* 214 */       BufferedReader br1 = new BufferedReader(new InputStreamReader(fis1, 
/* 215 */         Charset.forName("UTF-8")));
/* 216 */       BufferedReader br2 = new BufferedReader(new InputStreamReader(fis2, 
/* 217 */         Charset.forName("UTF-8")));
/* 218 */       line = br1.readLine();
/* 219 */       line2 = br2.readLine();
/*     */     } catch (FileNotFoundException e) {
/* 221 */       e.printStackTrace();
/*     */     } catch (IOException e) {
/* 223 */       e.printStackTrace();
/*     */     }
/*     */ 
/* 226 */     String[] nodes1 = line.split("/");
/* 227 */     String[] nodes2 = line2.split("/");
/* 228 */     if (nodes1.length != nodes2.length) {
/* 229 */       System.out.println("Unequal number of nodes");
/* 230 */       return;
/*     */     }
/*     */ 
/* 233 */     for (int n = 0; n < nodes2.length; n++) {
/* 234 */       String nodeLine1 = nodes1[n];
/* 235 */       String nodeLine2 = nodes2[n];
/* 236 */       String[] cats1 = nodeLine1.split(":");
/* 237 */       String[] cats2 = nodeLine2.split(":");
/* 238 */       if (cats1.length != cats2.length) {
/* 239 */         System.out.println("Unequal number of cats in node " + n);
/* 240 */         return;
/*     */       }
/*     */ 
/* 243 */       for (int c = 0; c < cats2.length; c++) {
/* 244 */         String catLine1 = cats1[c];
/* 245 */         String catLine2 = cats2[c];
/* 246 */         String[] sites1 = catLine1.split("I");
/* 247 */         String[] sites2 = catLine2.split("I");
/* 248 */         if (sites1.length != sites2.length) {
/* 249 */           System.out.println("Unequal number of sites in node " + n + "and cat " + c);
/* 250 */           return;
/*     */         }
/*     */ 
/* 253 */         for (int site = 0; site < sites2.length; site++) {
/* 254 */           String siteLine1 = sites1[site];
/* 255 */           String siteLine2 = sites2[site];
/* 256 */           String[] states1 = siteLine1.split(",");
/* 257 */           String[] states2 = siteLine2.split(",");
/* 258 */           if (states1.length != states2.length) {
/* 259 */             System.out.println("Unequal number of sites in node " + n + "and cat " + c + " and site " + site);
/* 260 */             return;
/*     */           }
/*     */ 
/* 263 */           for (int state = 0; state < states2.length; state++) {
/* 264 */             float val1 = Float.parseFloat(states1[state]);
/* 265 */             float val2 = Float.parseFloat(states2[state]);
/* 266 */             if (!equals) {
/* 267 */               if (val1 != val2) {
/* 268 */                 outFile.println("Diff in [" + n + "," + c + "," + site + "," + state + "]");
/* 269 */                 outFile.println("val1 = " + val1);
/* 270 */                 outFile.println("val2 = " + val2);
/*     */               }
/*     */             }
/* 273 */             else if (val1 == val2) {
/* 274 */               outFile.println("Equal in [" + n + "," + c + "," + site + "," + state + "]");
/* 275 */               outFile.println("val1 = " + val1);
/* 276 */               outFile.println("val2 = " + val2);
/*     */             }
/*     */           }
/*     */         }
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 284 */     outFile.flush();
/* 285 */     outFile.close();
/*     */   }
/*     */ 
/*     */   public static void main(String[] args0) {
/* 289 */     calculateSequenceDiff("C:\\Users\\calavera\\Desktop\\debugPrints\\node2gpu.txt", 
/* 291 */       "C:\\Users\\calavera\\Desktop\\debugPrints\\node2classic.txt", 
/* 292 */       "C:\\Users\\calavera\\Desktop\\debugPrints\\diffSeq.txt", 
/* 293 */       false, 0.001F);
/*     */   }
/*     */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.utilities.SequenceDebugging
 * JD-Core Version:    0.6.2
 */