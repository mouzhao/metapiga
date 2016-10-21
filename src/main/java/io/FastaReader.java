/*     */ package io;
/*     */ 
/*     */ import java.io.BufferedReader;
/*     */ import java.io.File;
/*     */ import java.io.FileReader;
/*     */ import java.io.InputStream;
/*     */ import java.io.InputStreamReader;
/*     */ import java.util.ArrayList;
/*     */ import java.util.HashMap;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Map.Entry;
/*     */ import java.util.concurrent.ExecutionException;
/*     */ import javax.swing.DefaultListModel;
/*     */ import javax.swing.SwingWorker;
/*     */ import metapiga.MainFrame;
/*     */ import metapiga.MetaPIGA;
/*     */ import metapiga.MetaPIGA.UI;
/*     */ import metapiga.WaitingLogo.Status;
/*     */ import metapiga.modelization.data.DataType;
/*     */ import metapiga.parameters.Parameters;
/*     */ import metapiga.utilities.Tools;
/*     */ import org.biojavax.bio.phylo.io.nexus.DataBlock;
/*     */ 
/*     */ public class FastaReader extends SwingWorker<WaitingLogo.Status, Object>
/*     */ {
/*     */   private MetaPIGA metapiga;
/*  38 */   File fasta = null;
/*  39 */   private InputStream is = null;
/*     */   private Parameters parameters;
/*     */   private DefaultListModel parametersList;
/*     */ 
/*     */   public FastaReader(File fastaFile, MetaPIGA metapiga)
/*     */   {
/*  44 */     this.metapiga = metapiga;
/*  45 */     this.parametersList = metapiga.parameters;
/*  46 */     this.fasta = fastaFile;
/*     */   }
/*     */ 
/*     */   public FastaReader(InputStream fastaInputStream, MetaPIGA metapiga) {
/*  50 */     this.metapiga = metapiga;
/*  51 */     this.parametersList = metapiga.parameters;
/*  52 */     this.is = fastaInputStream;
/*     */   }
/*     */ 
/*     */   public WaitingLogo.Status doInBackground() {
/*     */     try {
/*  57 */       BufferedReader br = null;
/*  58 */       FileReader fr = null;
/*  59 */       InputStreamReader isr = null;
/*  60 */       if (this.fasta != null) {
/*  61 */         String name = "";
/*  62 */         String[] split = this.fasta.getName().split("\\.");
/*  63 */         for (int i = 0; i < split.length - 1; i++) {
/*  64 */           name = name + split[i];
/*  65 */           if (i + 1 < split.length - 1) name = name + ".";
/*     */         }
/*  67 */         this.parameters = new Parameters(name);
/*  68 */         fr = new FileReader(this.fasta);
/*  69 */         br = new BufferedReader(fr);
/*     */       } else {
/*  71 */         this.parameters = new Parameters("default");
/*  72 */         isr = new InputStreamReader(this.is);
/*  73 */         br = new BufferedReader(isr);
/*     */       }
/*     */ 
/*  76 */       String taxon = "";
/*  77 */       List taxa = new ArrayList();
/*  78 */       Map sequences = new HashMap();
/*     */       String line;
/*  79 */       while ((line = br.readLine()) != null)
/*     */       {
/*  80 */         if (line.startsWith(">")) {
/*  81 */           taxon = line.substring(1);
/*  82 */           taxa.add(taxon);
/*  83 */           sequences.put(taxon, "");
/*     */         } else {
/*  85 */           sequences.put(taxon, ((String)sequences.get(taxon)).concat(line.toUpperCase()));
/*     */         }
/*     */       }
/*  88 */       if (br != null) br.close();
/*  89 */       if (fr != null) fr.close();
/*  90 */       if (isr != null) isr.close();
/*     */ 
/*  92 */       DataBlock dataBlock = new DataBlock();
/*  93 */       dataBlock.setGap("-");
/*  94 */       dataBlock.setMatchChar(".");
/*  95 */       dataBlock.setMissing("?");
/*  96 */       int nTot = 0;
/*  97 */       int nACGT = 0;
/*  98 */       int n01 = 0;
/*     */       int j;
/*     */       int i;
/*  99 */       for (Iterator localIterator1 = taxa.iterator(); localIterator1.hasNext(); 
/* 101 */         i < j)
/*     */       {
/*  99 */         String tax = (String)localIterator1.next();
/* 100 */         dataBlock.addMatrixEntry(formatForNexus(tax));
/*     */         char[] arrayOfChar1;
/* 101 */         j = (arrayOfChar1 = ((String)sequences.get(tax)).toCharArray()).length; i = 0; continue; char c = arrayOfChar1[i];
/* 102 */         if ((c == 'A') || (c == 'C') || (c == 'G') || (c == 'T') || (c == 'N')) {
/* 103 */           nTot++;
/* 104 */           nACGT++;
/* 105 */         } else if ((c == '0') || (c == '1')) {
/* 106 */           n01++;
/* 107 */           nTot++;
/* 108 */         } else if ((c == 'R') || (c == 'D') || (c == 'Q') || (c == 'E') || 
/* 109 */           (c == 'H') || (c == 'I') || (c == 'L') || (c == 'K') || 
/* 110 */           (c == 'M') || (c == 'F') || (c == 'P') || (c == 'S') || 
/* 111 */           (c == 'W') || (c == 'Y') || (c == 'V') || (c == 'B') || 
/* 112 */           (c == 'Z') || (c == 'J') || (c == 'X')) {
/* 113 */           nTot++;
/*     */         }
/* 101 */         i++;
/*     */       }
/*     */       DataType dataType;
/*     */       DataType dataType;
/* 118 */       if (nACGT / nTot > 0.8D) {
/* 119 */         dataType = DataType.DNA;
/*     */       }
/*     */       else
/*     */       {
/*     */         DataType dataType;
/* 120 */         if (n01 / nTot > 0.8D)
/* 121 */           dataType = DataType.STANDARD;
/*     */         else
/* 123 */           dataType = DataType.PROTEIN;
/*     */       }
/* 125 */       dataBlock.setDataType(dataType.name());
/* 126 */       int nchar = 0;
/* 127 */       for (Entry e : sequences.entrySet()) {
/* 128 */         int nchart = 0;
/* 129 */         for (char c : ((String)e.getValue()).toCharArray()) {
/* 130 */           switch (dataType) {
/*     */           case CODON:
/* 132 */             switch (c) {
/*     */             case '-':
/*     */             case 'A':
/*     */             case 'B':
/*     */             case 'C':
/*     */             case 'D':
/*     */             case 'G':
/*     */             case 'H':
/*     */             case 'K':
/*     */             case 'M':
/*     */             case 'N':
/*     */             case 'R':
/*     */             case 'S':
/*     */             case 'T':
/*     */             case 'V':
/*     */             case 'W':
/*     */             case 'Y':
/* 149 */               dataBlock.appendMatrixData(formatForNexus((String)e.getKey()), c);
/* 150 */               nchart++;
/*     */             }
/*     */ 
/* 155 */             break;
/*     */           case DNA:
/* 157 */             switch (c) {
/*     */             case '-':
/*     */             case 'A':
/*     */             case 'B':
/*     */             case 'C':
/*     */             case 'D':
/*     */             case 'E':
/*     */             case 'F':
/*     */             case 'G':
/*     */             case 'H':
/*     */             case 'I':
/*     */             case 'J':
/*     */             case 'K':
/*     */             case 'L':
/*     */             case 'M':
/*     */             case 'N':
/*     */             case 'P':
/*     */             case 'Q':
/*     */             case 'R':
/*     */             case 'S':
/*     */             case 'T':
/*     */             case 'V':
/*     */             case 'W':
/*     */             case 'X':
/*     */             case 'Y':
/*     */             case 'Z':
/* 183 */               dataBlock.appendMatrixData(formatForNexus((String)e.getKey()), c);
/* 184 */               nchart++;
/*     */             case '.':
/*     */             case '/':
/*     */             case '0':
/*     */             case '1':
/*     */             case '2':
/*     */             case '3':
/*     */             case '4':
/*     */             case '5':
/*     */             case '6':
/*     */             case '7':
/*     */             case '8':
/*     */             case '9':
/*     */             case ':':
/*     */             case ';':
/*     */             case '<':
/*     */             case '=':
/*     */             case '>':
/*     */             case '?':
/*     */             case '@':
/*     */             case 'O':
/* 189 */             case 'U': } break;
/*     */           case PROTEIN:
/* 191 */             switch (c) {
/*     */             case '-':
/*     */             case '0':
/*     */             case '1':
/*     */             case 'X':
/* 196 */               dataBlock.appendMatrixData(formatForNexus((String)e.getKey()), c);
/* 197 */               nchart++;
/*     */             }
/*     */ 
/*     */             break;
/*     */           }
/*     */ 
/*     */         }
/*     */ 
/* 205 */         if (nchar == 0) {
/* 206 */           nchar = nchart;
/*     */         }
/* 208 */         else if (nchart != nchar) {
/* 209 */           WaitingLogo.Status error = WaitingLogo.Status.DATA_FILE_NOT_LOADED;
/*     */           WaitingLogo.Status tmp1352_1350 = error; tmp1352_1350.text = (tmp1352_1350.text + "\nCannot load fasta file: all sequences don't have the same length (" + (String)taxa.get(0) + " has a size of " + nchar + " and " + (String)e.getKey() + " has a size of " + nchart + ").");
/* 211 */           return error;
/*     */         }
/*     */       }
/*     */ 
/* 215 */       dataBlock.setDimensionsNTax(taxa.size());
/* 216 */       dataBlock.setDimensionsNChar(nchar);
/* 217 */       this.parameters.setParameters(dataBlock);
/* 218 */       this.parameters.buildDataset();
/* 219 */       this.parameters.checkParameters();
/* 220 */       this.parametersList.addElement(this.parameters);
/* 221 */       if (MetaPIGA.ui == MetaPIGA.UI.GRAPHICAL) this.metapiga.mainFrame.updateMatrixTextPanes();
/* 222 */       return WaitingLogo.Status.DATA_FILE_LOADED;
/*     */     } catch (OutOfMemoryError e) {
/* 224 */       WaitingLogo.Status error = WaitingLogo.Status.DATA_FILE_NOT_LOADED;
/* 225 */       error.text += "\nOut of memory: please, assign more RAM to MetaPIGA. You can easily do so by using the menu 'Tools --> Memory settings'.";
/* 226 */       return error;
/*     */     } catch (Exception e) {
/* 228 */       e.printStackTrace();
/* 229 */       WaitingLogo.Status error = WaitingLogo.Status.DATA_FILE_NOT_LOADED;
/*     */       WaitingLogo.Status tmp1571_1570 = error; tmp1571_1570.text = (tmp1571_1570.text + "\n" + Tools.getErrorMessage(e));
/* 231 */       return error;
/*     */     }
/*     */   }
/*     */ 
/*     */   private String formatForNexus(String taxon) {
/* 236 */     return taxon.replace('\'', '!').replace('(', '!').replace(')', '!').replace('[', '!').replace(']', '!').replace(',', '!').replace(':', '!').replace('§', '!').replace(';', '!').replace('_', ' ');
/*     */   }
/*     */ 
/*     */   protected void done() {
/*     */     try {
/* 241 */       if (MetaPIGA.ui == MetaPIGA.UI.GRAPHICAL) this.metapiga.mainFrame.setAllEnabled(this.metapiga.mainFrame, (WaitingLogo.Status)get()); else
/* 242 */         this.metapiga.busy = false;
/*     */     } catch (ExecutionException e) {
/* 244 */       e.getCause().printStackTrace();
/*     */     } catch (InterruptedException e) {
/* 246 */       e.printStackTrace();
/*     */     }
/*     */   }
/*     */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.io.FastaReader
 * JD-Core Version:    0.6.2
 */