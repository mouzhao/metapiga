/*     */ package modelization;
/*     */ 
/*     */ import java.util.BitSet;
/*     */ import java.util.Collection;
/*     */ import java.util.Collections;
/*     */ import java.util.Iterator;
/*     */ import java.util.LinkedHashMap;
/*     */ import java.util.LinkedList;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Map.Entry;
/*     */ import java.util.Set;
/*     */ import metapiga.exceptions.IncompatibleDataException;
/*     */ import metapiga.exceptions.NexusInconsistencyException;
/*     */ import metapiga.exceptions.UnknownDataException;
/*     */ import metapiga.modelization.data.Data;
/*     */ import metapiga.modelization.data.DataType;
/*     */ import metapiga.parameters.Parameters.CodonDomainDefinition;
/*     */ import org.biojavax.bio.phylo.io.nexus.CharactersBlock;
/*     */ 
/*     */ public class CodonCharactersBlock
/*     */ {
/*  25 */   final Map<String, List<Data>> matrix = new LinkedHashMap();
/*     */   private final BitSet gaps;
/*     */   private final BitSet ngaps;
/*     */   private final int fullNChar;
/*  29 */   private final int nucleotidesInCodon = 3;
/*     */ 
/*     */   public CodonCharactersBlock(CharactersBlock block, Parameters.CodonDomainDefinition definitionInterval)
/*     */     throws UnknownDataException, NexusInconsistencyException, IncompatibleDataException
/*     */   {
/*  34 */     DataType dataType = null;
/*  35 */     if (block.getDataType().toUpperCase().equals("NUCLEOTIDES")) {
/*  36 */       dataType = DataType.CODON;
/*  37 */     } else if (block.getDataType().toUpperCase().equals("CODONS")) {
/*  38 */       dataType = DataType.CODON;
/*  39 */     } else if (block.getDataType().toUpperCase().equals("DNA")) {
/*  40 */       dataType = DataType.CODON; } else {
/*  41 */       if (block.getDataType().toUpperCase().equals("RNA")) {
/*  42 */         throw new UnknownDataException("RNA");
/*     */       }
/*  44 */       dataType = DataType.valueOf(block.getDataType().toUpperCase());
/*     */     }
/*  46 */     if (dataType != DataType.CODON) {
/*  47 */       throw new IncompatibleDataException(DataType.DNA, dataType);
/*     */     }
/*     */ 
/*  50 */     this.fullNChar = block.getDimensionsNChar();
/*  51 */     String matchSymbol = block.getMatchChar() == null ? "." : block.getMatchChar();
/*  52 */     String missingSymbol = block.getMissing() == null ? "_" : block.getMissing();
/*  53 */     String gapSymbol = block.getGap() == null ? "-" : block.getGap();
/*  54 */     this.gaps = new BitSet();
/*  55 */     this.ngaps = new BitSet();
/*     */ 
/*  57 */     String firstTaxon = null;
/*  58 */     for (Iterator localIterator1 = block.getMatrixLabels().iterator(); localIterator1.hasNext(); ) { Object taxon = localIterator1.next();
/*  59 */       List seq = new LinkedList();
/*  60 */       if (firstTaxon == null) firstTaxon = taxon.toString();
/*  61 */       int positionCounter = 0;
/*  62 */       String codon = "";
/*  63 */       for (Iterator localIterator2 = block.getMatrixData(taxon.toString()).iterator(); localIterator2.hasNext(); ) { Object obj = localIterator2.next();
/*  64 */         String nucl = obj.toString();
/*  65 */         if (nucl.length() > 0)
/*  66 */           if (nucl.length() > 1) {
/*  67 */             for (char n : nucl.toCharArray())
/*     */             {
/*  69 */               positionCounter++;
/*  70 */               if (positionCounter >= definitionInterval.getStartCodonDomainPosition())
/*     */               {
/*  72 */                 if (positionCounter > definitionInterval.getEndCodonDomainPosition()) {
/*     */                   break;
/*     */                 }
/*  75 */                 codon = codon + n;
/*  76 */                 codon = isCodonComplete(dataType, seq, codon);
/*     */               }
/*     */             }
/*     */           } else { positionCounter++;
/*  80 */             if (positionCounter >= definitionInterval.getStartCodonDomainPosition())
/*     */             {
/*  82 */               if (positionCounter > definitionInterval.getEndCodonDomainPosition())
/*     */               {
/*     */                 break;
/*     */               }
/*  86 */               if ((nucl.equals(matchSymbol)) || ((block.isRespectCase()) && (nucl.equalsIgnoreCase(matchSymbol)))) {
/*  87 */                 if (firstTaxon == null) throw new NexusInconsistencyException("You cannot use MATCHCHAR symbol on first line !");
/*  88 */                 Data fd = (Data)((List)this.matrix.get(firstTaxon)).get(seq.size());
/*  89 */                 String firstTaxonCodon = fd.toString();
/*  90 */                 codon = codon + firstTaxonCodon.toCharArray()[codon.length()];
/*     */                 try
/*     */                 {
/*  93 */                   codon = isCodonComplete(dataType, seq, codon);
/*     */                 } catch (Exception e) {
/*  95 */                   e.printStackTrace();
/*  96 */                   throw new UnknownDataException(nucl, taxon.toString());
/*     */                 }
/*  98 */               } else if ((nucl.equals(missingSymbol)) || ((block.isRespectCase()) && (nucl.equalsIgnoreCase(missingSymbol)))) {
/*  99 */                 codon = codon + "-";
/* 100 */               } else if ((nucl.equals(gapSymbol)) || ((block.isRespectCase()) && (nucl.equalsIgnoreCase(gapSymbol)))) {
/* 101 */                 codon = codon + "-";
/*     */               } else {
/* 103 */                 codon = codon + nucl;
/*     */               }
/*     */               try {
/* 106 */                 codon = isCodonComplete(dataType, seq, codon);
/*     */               } catch (UnknownDataException e) {
/* 108 */                 e.printStackTrace();
/* 109 */                 throw new UnknownDataException(nucl, taxon.toString());
/*     */               }
/*     */             }
/*     */           }
/*     */       }
/* 114 */       this.matrix.put(taxon.toString(), seq);
/* 115 */       if (((List)this.matrix.get(firstTaxon)).size() != seq.size())
/* 116 */         throw new NexusInconsistencyException("Line " + taxon + " has a size of " + seq.size() + ", and should have " + ((List)this.matrix.get(firstTaxon)).size() + " as first line");
/*     */     }
/*     */   }
/*     */ 
/*     */   private String isCodonComplete(DataType dataType, List<Data> seq, String codon)
/*     */     throws UnknownDataException
/*     */   {
/* 129 */     if (codon.length() == 3) {
/* 130 */       Data d = completeCodon(codon, dataType);
/* 131 */       if (d.isUndeterminate()) {
/* 132 */         this.ngaps.set(seq.size());
/* 133 */         this.gaps.set(seq.size());
/*     */       }
/* 135 */       seq.add(d);
/* 136 */       return "";
/*     */     }
/* 138 */     return codon;
/*     */   }
/*     */ 
/*     */   private Data completeCodon(String codon, DataType dataType) throws UnknownDataException {
/* 142 */     if (isCodonUndeterminate(codon)) {
/* 143 */       return dataType.getUndeterminateData();
/*     */     }
/* 145 */     return dataType.getData(codon.toUpperCase());
/*     */   }
/*     */ 
/*     */   private boolean isCodonUndeterminate(String codon) {
/* 149 */     if (codon.lastIndexOf("-") != -1) return true;
/* 150 */     if (codon.lastIndexOf("R") != -1) return true;
/* 151 */     if (codon.lastIndexOf("Y") != -1) return true;
/* 152 */     if (codon.lastIndexOf("W") != -1) return true;
/* 153 */     if (codon.lastIndexOf("S") != -1) return true;
/* 154 */     if (codon.lastIndexOf("M") != -1) return true;
/* 155 */     if (codon.lastIndexOf("K") != -1) return true;
/* 156 */     if (codon.lastIndexOf("B") != -1) return true;
/* 157 */     if (codon.lastIndexOf("D") != -1) return true;
/* 158 */     if (codon.lastIndexOf("H") != -1) return true;
/* 159 */     if (codon.lastIndexOf("V") != -1) return true;
/* 160 */     if (codon.lastIndexOf("N") != -1) return true;
/* 161 */     return false;
/*     */   }
/*     */ 
/*     */   public int getDimensionsNChar() {
/* 165 */     Iterator localIterator = this.matrix.entrySet().iterator(); if (localIterator.hasNext()) { Entry entry = (Entry)localIterator.next();
/* 166 */       int numChars = ((List)this.matrix.get(entry.getKey())).size();
/* 167 */       return numChars;
/*     */     }
/* 169 */     return 0;
/*     */   }
/*     */ 
/*     */   public Map<String, List<Data>> getDataMatrix() {
/* 173 */     return this.matrix;
/*     */   }
/*     */ 
/*     */   public Collection<String> getMatrixLabels() {
/* 177 */     return Collections.unmodifiableSet(this.matrix.keySet());
/*     */   }
/*     */ 
/*     */   public List<Data> getMatrixData(String taxa) {
/* 181 */     return (List)this.matrix.get(taxa);
/*     */   }
/*     */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.modelization.CodonCharactersBlock
 * JD-Core Version:    0.6.2
 */