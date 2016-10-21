/*     */ package metapiga.modelization;
/*     */ 
/*     */ import java.util.ArrayList;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */
/*     */ import metapiga.modelization.data.Codon;
/*     */ import metapiga.modelization.data.Data;
/*     */
/*     */ import org.biojavax.bio.phylo.io.nexus.CharactersBlock;
/*     */ import org.junit.Assert;
/*     */ import org.junit.Test;
/*     */ import org.mockito.Matchers;
/*     */ import org.mockito.Mockito;
/*     */ import org.mockito.stubbing.OngoingStubbing;
/*     */ 
/*     */ public class CodonCharacterBlockTest
/*     */ {
/*     */   @Test
/*     */   public void codonCharacterBlockTestNormal()
/*     */   {
/*  23 */     CharactersBlock mockBlock = (CharactersBlock)Mockito.mock(CharactersBlock.class);
/*  24 */     Mockito.when(mockBlock.getDataType()).thenReturn("DNA");
/*  25 */     Mockito.when(Integer.valueOf(mockBlock.getDimensionsNChar())).thenReturn(Integer.valueOf(10));
/*  26 */     ArrayList taxList = new ArrayList();
/*  27 */     taxList.add("tax1");
/*  28 */     taxList.add("tax2");
/*  29 */     Mockito.when(mockBlock.getMatrixLabels()).thenReturn(taxList);
/*  30 */     ArrayList seqList = new ArrayList();
/*  31 */     seqList.add("A"); seqList.add("C"); seqList.add("A");
/*  32 */     seqList.add("T"); seqList.add("A"); seqList.add("G");
/*  33 */     seqList.add("T"); seqList.add("A"); seqList.add("C");
/*  34 */     seqList.add("C");
/*  35 */     Mockito.when(mockBlock.getMatrixData(Matchers.anyString())).thenReturn(seqList);
/*  36 */     Mockito.when(Boolean.valueOf(mockBlock.isRespectCase())).thenReturn(Boolean.valueOf(false));
/*     */ 
/*  38 */     Parameters.CodonDomainDefinition domain = (Parameters.CodonDomainDefinition)Mockito.mock(Parameters.CodonDomainDefinition.class);
/*  39 */     Mockito.when(Integer.valueOf(domain.getStartCodonDomainPosition())).thenReturn(Integer.valueOf(0));
/*  40 */     Mockito.when(Integer.valueOf(domain.getEndCodonDomainPosition())).thenReturn(Integer.valueOf(10));
/*     */ 
/*  42 */     Data[] expecteds = { Codon.ACA, Codon.TAG, Codon.TAC };
/*  43 */     Data[] actuals = new Data[3];
/*     */     try {
/*  45 */       CodonCharactersBlock cb = new CodonCharactersBlock(mockBlock, domain);
/*  46 */       Map m = cb.getDataMatrix();
/*  47 */       Iterator localIterator = m.keySet().iterator(); if (localIterator.hasNext()) { String s = (String)localIterator.next();
/*  48 */         ((List)m.get(s)).toArray(actuals);
/*     */       }
/*     */ 
/*  51 */       Assert.assertEquals(expecteds, actuals);
/*     */     } catch (Exception e) {
/*  53 */       e.printStackTrace();
/*  54 */       Assert.fail("exception thrown @ CodonCharacterBlock invocation" + e.getMessage());
/*     */     }
/*     */   }
/*     */ 
/*     */   @Test
/*     */   public void codonCharacterBlockTestSmallerDatasetThatDomain()
/*     */   {
/*  64 */     CharactersBlock mockBlock = (CharactersBlock)Mockito.mock(CharactersBlock.class);
/*  65 */     Mockito.when(mockBlock.getDataType()).thenReturn("DNA");
/*  66 */     Mockito.when(Integer.valueOf(mockBlock.getDimensionsNChar())).thenReturn(Integer.valueOf(15));
/*  67 */     ArrayList taxList = new ArrayList();
/*  68 */     taxList.add("tax1");
/*  69 */     taxList.add("tax2");
/*  70 */     Mockito.when(mockBlock.getMatrixLabels()).thenReturn(taxList);
/*  71 */     ArrayList seqList = new ArrayList();
/*  72 */     seqList.add("A"); seqList.add("C"); seqList.add("A");
/*  73 */     seqList.add("T"); seqList.add("A"); seqList.add("G");
/*  74 */     seqList.add("T"); seqList.add("A"); seqList.add("C");
/*  75 */     seqList.add("C");
/*  76 */     Mockito.when(mockBlock.getMatrixData(Matchers.anyString())).thenReturn(seqList);
/*  77 */     Mockito.when(Boolean.valueOf(mockBlock.isRespectCase())).thenReturn(Boolean.valueOf(false));
/*     */ 
/*  79 */     Parameters.CodonDomainDefinition domain = (Parameters.CodonDomainDefinition)Mockito.mock(Parameters.CodonDomainDefinition.class);
/*  80 */     Mockito.when(Integer.valueOf(domain.getStartCodonDomainPosition())).thenReturn(Integer.valueOf(0));
/*  81 */     Mockito.when(Integer.valueOf(domain.getEndCodonDomainPosition())).thenReturn(Integer.valueOf(10));
/*     */ 
/*  83 */     Data[] expecteds = { Codon.ACA, Codon.TAG, Codon.TAC };
/*  84 */     Data[] actuals = new Data[3];
/*     */     try {
/*  86 */       CodonCharactersBlock cb = new CodonCharactersBlock(mockBlock, domain);
/*  87 */       Map m = cb.getDataMatrix();
/*  88 */       Iterator localIterator = m.keySet().iterator(); if (localIterator.hasNext()) { String s = (String)localIterator.next();
/*  89 */         ((List)m.get(s)).toArray(actuals);
/*     */       }
/*     */ 
/*  92 */       Assert.assertEquals(expecteds, actuals);
/*     */     } catch (Exception e) {
/*  94 */       e.printStackTrace();
/*  95 */       Assert.fail("exception thrown @ CodonCharacterBlock invocation" + e.getMessage());
/*     */     }
/*     */   }
/*     */ 
/*     */   @Test
/*     */   public void codonCharacterBlockTestDataClump()
/*     */   {
/* 106 */     CharactersBlock mockBlock = (CharactersBlock)Mockito.mock(CharactersBlock.class);
/* 107 */     Mockito.when(mockBlock.getDataType()).thenReturn("DNA");
/* 108 */     Mockito.when(Integer.valueOf(mockBlock.getDimensionsNChar())).thenReturn(Integer.valueOf(10));
/* 109 */     ArrayList taxList = new ArrayList();
/* 110 */     taxList.add("tax1");
/* 111 */     taxList.add("tax2");
/* 112 */     Mockito.when(mockBlock.getMatrixLabels()).thenReturn(taxList);
/* 113 */     ArrayList seqList = new ArrayList();
/* 114 */     seqList.add("AC"); seqList.add("A");
/* 115 */     seqList.add("T"); seqList.add("AG");
/* 116 */     seqList.add("T"); seqList.add("ACT");
/* 117 */     Mockito.when(mockBlock.getMatrixData(Matchers.anyString())).thenReturn(seqList);
/* 118 */     Mockito.when(Boolean.valueOf(mockBlock.isRespectCase())).thenReturn(Boolean.valueOf(false));
/*     */ 
/* 120 */     Parameters.CodonDomainDefinition domain = (Parameters.CodonDomainDefinition)Mockito.mock(Parameters.CodonDomainDefinition.class);
/* 121 */     Mockito.when(Integer.valueOf(domain.getStartCodonDomainPosition())).thenReturn(Integer.valueOf(0));
/* 122 */     Mockito.when(Integer.valueOf(domain.getEndCodonDomainPosition())).thenReturn(Integer.valueOf(10));
/*     */ 
/* 124 */     Data[] expecteds = { Codon.ACA, Codon.TAG, Codon.TAC };
/* 125 */     Data[] actuals = new Data[3];
/*     */     try {
/* 127 */       CodonCharactersBlock cb = new CodonCharactersBlock(mockBlock, domain);
/* 128 */       Map m = cb.getDataMatrix();
/* 129 */       Iterator localIterator = m.keySet().iterator(); if (localIterator.hasNext()) { String s = (String)localIterator.next();
/* 130 */         ((List)m.get(s)).toArray(actuals);
/*     */       }
/*     */ 
/* 133 */       Assert.assertEquals(expecteds, actuals);
/*     */     } catch (Exception e) {
/* 135 */       e.printStackTrace();
/* 136 */       Assert.fail("exception thrown @ CodonCharacterBlock invocation" + e.getMessage());
/*     */     }
/*     */   }
/*     */ 
/*     */   @Test
/*     */   public void codonCharacterBlockTestDash()
/*     */   {
/* 146 */     CharactersBlock mockBlock = (CharactersBlock)Mockito.mock(CharactersBlock.class);
/* 147 */     Mockito.when(mockBlock.getDataType()).thenReturn("DNA");
/* 148 */     Mockito.when(Integer.valueOf(mockBlock.getDimensionsNChar())).thenReturn(Integer.valueOf(10));
/* 149 */     ArrayList taxList = new ArrayList();
/* 150 */     taxList.add("tax1");
/* 151 */     taxList.add("tax2");
/*     */ 
/* 153 */     Mockito.when(mockBlock.getMatrixLabels()).thenReturn(taxList);
/* 154 */     ArrayList seqList = new ArrayList();
/* 155 */     seqList.add("A-"); seqList.add("A");
/* 156 */     seqList.add("-"); seqList.add("AG");
/* 157 */     seqList.add("T"); seqList.add("ACT");
/*     */ 
/* 159 */     Mockito.when(mockBlock.getMatrixData(Matchers.anyString())).thenReturn(seqList);
/* 160 */     Mockito.when(Boolean.valueOf(mockBlock.isRespectCase())).thenReturn(Boolean.valueOf(false));
/*     */ 
/* 162 */     Parameters.CodonDomainDefinition domain = (Parameters.CodonDomainDefinition)Mockito.mock(Parameters.CodonDomainDefinition.class);
/* 163 */     Mockito.when(Integer.valueOf(domain.getStartCodonDomainPosition())).thenReturn(Integer.valueOf(0));
/* 164 */     Mockito.when(Integer.valueOf(domain.getEndCodonDomainPosition())).thenReturn(Integer.valueOf(10));
/*     */ 
/* 166 */     Data[] expecteds = { Codon.__X, Codon.__X, Codon.TAC };
/* 167 */     Data[] actuals = new Data[3];
/*     */     try {
/* 169 */       CodonCharactersBlock cb = new CodonCharactersBlock(mockBlock, domain);
/* 170 */       Map m = cb.getDataMatrix();
/* 171 */       Iterator localIterator = m.keySet().iterator(); if (localIterator.hasNext()) { String s = (String)localIterator.next();
/* 172 */         ((List)m.get(s)).toArray(actuals);
/*     */       }
/*     */ 
/* 175 */       Assert.assertEquals(expecteds, actuals);
/*     */     } catch (Exception e) {
/* 177 */       e.printStackTrace();
/* 178 */       Assert.fail("exception thrown @ CodonCharacterBlock invocation" + e.getMessage());
/*     */     }
/*     */   }
/*     */ 
/*     */   @Test
/*     */   public void codonCharacterBlockTestScope()
/*     */   {
/* 188 */     CharactersBlock mockBlock = (CharactersBlock)Mockito.mock(CharactersBlock.class);
/* 189 */     Mockito.when(mockBlock.getDataType()).thenReturn("DNA");
/* 190 */     Mockito.when(Integer.valueOf(mockBlock.getDimensionsNChar())).thenReturn(Integer.valueOf(10));
/* 191 */     ArrayList taxList = new ArrayList();
/* 192 */     taxList.add("tax1");
/* 193 */     taxList.add("tax2");
/*     */ 
/* 195 */     Mockito.when(mockBlock.getMatrixLabels()).thenReturn(taxList);
/* 196 */     ArrayList seqList = new ArrayList();
/* 197 */     seqList.add("AG"); seqList.add("A");
/* 198 */     seqList.add("TGC"); seqList.add("AG");
/* 199 */     seqList.add("T"); seqList.add("ATACC");
/*     */ 
/* 201 */     Mockito.when(mockBlock.getMatrixData(Matchers.anyString())).thenReturn(seqList);
/* 202 */     Mockito.when(Boolean.valueOf(mockBlock.isRespectCase())).thenReturn(Boolean.valueOf(false));
/*     */ 
/* 204 */     Parameters.CodonDomainDefinition domain = (Parameters.CodonDomainDefinition)Mockito.mock(Parameters.CodonDomainDefinition.class);
/* 205 */     Mockito.when(Integer.valueOf(domain.getStartCodonDomainPosition())).thenReturn(Integer.valueOf(2));
/* 206 */     Mockito.when(Integer.valueOf(domain.getEndCodonDomainPosition())).thenReturn(Integer.valueOf(17));
/*     */ 
/* 208 */     Data[] expecteds = { Codon.GAT, Codon.GCA, Codon.GTA, Codon.TAC };
/* 209 */     Data[] actuals = new Data[4];
/*     */     try {
/* 211 */       CodonCharactersBlock cb = new CodonCharactersBlock(mockBlock, domain);
/* 212 */       Map m = cb.getDataMatrix();
/* 213 */       Iterator localIterator = m.keySet().iterator(); if (localIterator.hasNext()) { String s = (String)localIterator.next();
/* 214 */         ((List)m.get(s)).toArray(actuals);
/*     */       }
/*     */ 
/* 217 */       Assert.assertEquals(expecteds, actuals);
/*     */     } catch (Exception e) {
/* 219 */       e.printStackTrace();
/* 220 */       Assert.fail("exception thrown @ CodonCharacterBlock invocation" + e.getMessage());
/*     */     }
/*     */   }
/*     */ 
/*     */   @Test
/*     */   public void codonCharacterBlockTestMatchSign()
/*     */   {
/* 230 */     CharactersBlock mockBlock = (CharactersBlock)Mockito.mock(CharactersBlock.class);
/* 231 */     Mockito.when(mockBlock.getDataType()).thenReturn("DNA");
/* 232 */     Mockito.when(Integer.valueOf(mockBlock.getDimensionsNChar())).thenReturn(Integer.valueOf(10));
/* 233 */     ArrayList taxList = new ArrayList();
/* 234 */     taxList.add("tax1");
/* 235 */     taxList.add("tax2");
/*     */ 
/* 237 */     Mockito.when(mockBlock.getMatrixLabels()).thenReturn(taxList);
/* 238 */     ArrayList seqList = new ArrayList();
/* 239 */     seqList.add("AG"); seqList.add("A");
/* 240 */     seqList.add("TGC"); seqList.add("AG");
/* 241 */     seqList.add("T"); seqList.add("ATACC");
/*     */ 
/* 243 */     ArrayList seqList2 = new ArrayList();
/* 244 */     seqList2.add("A."); seqList2.add("A");
/* 245 */     seqList2.add(".GC"); seqList2.add("A.");
/* 246 */     seqList2.add("."); seqList2.add("ATACC");
/*     */ 
/* 248 */     Mockito.when(mockBlock.getMatrixData("tax1")).thenReturn(seqList);
/* 249 */     Mockito.when(mockBlock.getMatrixData("tax2")).thenReturn(seqList2);
/* 250 */     Mockito.when(Boolean.valueOf(mockBlock.isRespectCase())).thenReturn(Boolean.valueOf(false));
/*     */ 
/* 252 */     Parameters.CodonDomainDefinition domain = (Parameters.CodonDomainDefinition)Mockito.mock(Parameters.CodonDomainDefinition.class);
/* 253 */     Mockito.when(Integer.valueOf(domain.getStartCodonDomainPosition())).thenReturn(Integer.valueOf(2));
/* 254 */     Mockito.when(Integer.valueOf(domain.getEndCodonDomainPosition())).thenReturn(Integer.valueOf(17));
/*     */ 
/* 256 */     Data[] expecteds = { Codon.GAT, Codon.GCA, Codon.GTA, Codon.TAC };
/* 257 */     Data[] actuals = new Data[4];
/*     */     try {
/* 259 */       CodonCharactersBlock cb = new CodonCharactersBlock(mockBlock, domain);
/* 260 */       Map m = cb.getDataMatrix();
/* 261 */       Iterator localIterator = m.keySet().iterator(); if (localIterator.hasNext()) { String s = (String)localIterator.next();
/* 262 */         ((List)m.get(s)).toArray(actuals);
/*     */       }
/*     */ 
/* 265 */       Assert.assertEquals(expecteds, actuals);
/*     */     } catch (Exception e) {
/* 267 */       e.printStackTrace();
/* 268 */       Assert.fail("exception thrown @ CodonCharacterBlock invocation" + e.getMessage());
/*     */     }
/*     */   }
/*     */ 
/*     */   @Test
/*     */   public void codonCharacterBlockTestStopCodons()
/*     */   {
/* 278 */     CharactersBlock mockBlock = (CharactersBlock)Mockito.mock(CharactersBlock.class);
/* 279 */     Mockito.when(mockBlock.getDataType()).thenReturn("DNA");
/* 280 */     Mockito.when(Integer.valueOf(mockBlock.getDimensionsNChar())).thenReturn(Integer.valueOf(10));
/* 281 */     ArrayList taxList = new ArrayList();
/* 282 */     taxList.add("tax1");
/* 283 */     taxList.add("tax2");
/* 284 */     taxList.add("tax3");
/*     */ 
/* 286 */     Mockito.when(mockBlock.getMatrixLabels()).thenReturn(taxList);
/* 287 */     ArrayList seqList = new ArrayList();
/* 288 */     seqList.add("TGA"); seqList.add("ACT"); seqList.add("AAC");
/* 289 */     seqList.add("A"); seqList.add("C"); seqList.add("C");
/* 290 */     seqList.add("C"); seqList.add("G"); seqList.add("A");
/*     */ 
/* 292 */     ArrayList seqList2 = new ArrayList();
/* 293 */     seqList2.add("C"); seqList2.add("A"); seqList2.add("A");
/* 294 */     seqList2.add("TCC"); seqList2.add("TAA"); seqList2.add("GCA");
/* 295 */     seqList2.add("C"); seqList2.add("A"); seqList2.add("G");
/*     */ 
/* 297 */     ArrayList seqList3 = new ArrayList();
/* 298 */     seqList3.add("C"); seqList3.add("C"); seqList3.add("C");
/* 299 */     seqList3.add("A"); seqList3.add("A"); seqList3.add("A");
/* 300 */     seqList3.add("GCA"); seqList3.add("TAC"); seqList3.add("TAG");
/*     */ 
/* 302 */     Mockito.when(mockBlock.getMatrixData("tax1")).thenReturn(seqList);
/* 303 */     Mockito.when(mockBlock.getMatrixData("tax2")).thenReturn(seqList2);
/* 304 */     Mockito.when(mockBlock.getMatrixData("tax3")).thenReturn(seqList3);
/* 305 */     Mockito.when(Boolean.valueOf(mockBlock.isRespectCase())).thenReturn(Boolean.valueOf(false));
/*     */ 
/* 307 */     Parameters.CodonDomainDefinition domain = (Parameters.CodonDomainDefinition)Mockito.mock(Parameters.CodonDomainDefinition.class);
/* 308 */     Mockito.when(Integer.valueOf(domain.getStartCodonDomainPosition())).thenReturn(Integer.valueOf(2));
/* 309 */     Mockito.when(Integer.valueOf(domain.getEndCodonDomainPosition())).thenReturn(Integer.valueOf(17));
/*     */ 
/* 311 */     Data[] expecteds = { Codon.ACT, Codon.ACC };
/* 312 */     Data[] actuals = new Data[5];
/*     */     try {
/* 314 */       CodonCharactersBlock cb = new CodonCharactersBlock(mockBlock, domain);
/* 315 */       Map m = cb.getDataMatrix();
/* 316 */       for (String s : m.keySet()) {
/* 317 */         if (s.equalsIgnoreCase("tax1")) {
/* 318 */           ((List)m.get(s)).toArray(actuals);
/* 319 */           break;
/*     */         }
/*     */       }
/* 322 */       Assert.assertEquals(expecteds, actuals);
/*     */     } catch (Exception e) {
/* 324 */       e.printStackTrace();
/* 325 */       Assert.fail("exception thrown @ CodonCharacterBlock invocation" + e.getMessage());
/*     */     }
/*     */   }
/*     */ 
/*     */   @Test
/*     */   public void codonCharacterBlockTestAmbiguous()
/*     */   {
/* 336 */     CharactersBlock mockBlock = (CharactersBlock)Mockito.mock(CharactersBlock.class);
/* 337 */     Mockito.when(mockBlock.getDataType()).thenReturn("DNA");
/* 338 */     Mockito.when(Integer.valueOf(mockBlock.getDimensionsNChar())).thenReturn(Integer.valueOf(10));
/* 339 */     ArrayList taxList = new ArrayList();
/* 340 */     taxList.add("tax1");
/* 341 */     taxList.add("tax2");
/* 342 */     taxList.add("tax3");
/*     */ 
/* 344 */     Mockito.when(mockBlock.getMatrixLabels()).thenReturn(taxList);
/* 345 */     ArrayList seqList = new ArrayList();
/* 346 */     seqList.add("TNA"); seqList.add("ACT"); seqList.add("AAC");
/* 347 */     seqList.add("A"); seqList.add("C"); seqList.add("C");
/* 348 */     seqList.add("C"); seqList.add("G"); seqList.add("A");
/*     */ 
/* 350 */     ArrayList seqList2 = new ArrayList();
/* 351 */     seqList2.add("C"); seqList2.add("A"); seqList2.add("A");
/* 352 */     seqList2.add("TCC"); seqList2.add("MAA"); seqList2.add("GCA");
/* 353 */     seqList2.add("C"); seqList2.add("A"); seqList2.add("G");
/*     */ 
/* 355 */     ArrayList seqList3 = new ArrayList();
/* 356 */     seqList3.add("C"); seqList3.add("C"); seqList3.add("C");
/* 357 */     seqList3.add("A"); seqList3.add("A"); seqList3.add("A");
/* 358 */     seqList3.add("GCA"); seqList3.add("TAC"); seqList3.add("TA-");
/*     */ 
/* 360 */     Mockito.when(mockBlock.getMatrixData("tax1")).thenReturn(seqList);
/* 361 */     Mockito.when(mockBlock.getMatrixData("tax2")).thenReturn(seqList2);
/* 362 */     Mockito.when(mockBlock.getMatrixData("tax3")).thenReturn(seqList3);
/* 363 */     Mockito.when(Boolean.valueOf(mockBlock.isRespectCase())).thenReturn(Boolean.valueOf(false));
/*     */ 
/* 365 */     Parameters.CodonDomainDefinition domain = (Parameters.CodonDomainDefinition)Mockito.mock(Parameters.CodonDomainDefinition.class);
/* 366 */     Mockito.when(Integer.valueOf(domain.getStartCodonDomainPosition())).thenReturn(Integer.valueOf(2));
/* 367 */     Mockito.when(Integer.valueOf(domain.getEndCodonDomainPosition())).thenReturn(Integer.valueOf(17));
/*     */ 
/* 369 */     Data[] expecteds = { Codon.ACT, Codon.ACC };
/* 370 */     Data[] actuals = new Data[5];
/*     */     try {
/* 372 */       CodonCharactersBlock cb = new CodonCharactersBlock(mockBlock, domain);
/* 373 */       Map m = cb.getDataMatrix();
/* 374 */       for (String s : m.keySet()) {
/* 375 */         if (s.equalsIgnoreCase("tax1")) {
/* 376 */           ((List)m.get(s)).toArray(actuals);
/* 377 */           break;
/*     */         }
/*     */       }
/* 380 */       Assert.assertEquals(expecteds, actuals);
/*     */     } catch (Exception e) {
/* 382 */       e.printStackTrace();
/* 383 */       Assert.fail("exception thrown @ CodonCharacterBlock invocation" + e.getMessage());
/*     */     }
/*     */   }
/*     */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.CodonCharacterBlockTest
 * JD-Core Version:    0.6.2
 */