/*     */ package metapiga.parameters;
/*     */ 
/*     */

/*     */ import java.util.ArrayList;
/*     */ import java.util.Collection;
/*     */ import java.util.HashMap;
/*     */ import java.util.Iterator;
/*     */ import java.util.Map;
/*     */ import java.util.Set;
/*     */ import java.util.TreeSet;
/*     */ import metapiga.modelization.Charset;
/*     */ import metapiga.utilities.Tools;
/*     */ 
/*     */ public class CharsetsContainer
/*     */ {
/*  21 */   private Map<String, Charset> charsets = new HashMap();
/*  22 */   private Map<String, Charset> binnedCharsets = new HashMap();
/*  23 */   private Set<Charset> excludedCharsets = new TreeSet();
/*  24 */   private Set<Charset> partitions = new TreeSet();
/*     */ 
/*  26 */   private Charset fullDataset = null;
/*  27 */   private Charset remainingCharset = null;
/*  28 */   boolean isCodonMode = false;
/*     */   private final Parameters P;
/*     */ 
/*     */   public CharsetsContainer(Parameters inP)
/*     */   {
/*  33 */     this.P = inP;
/*     */   }
/*     */   public void addCharset(String key, Charset charset) {
/*  36 */     if (key.contentEquals("REMAINING"))
/*  37 */       this.remainingCharset = charset;
/*  38 */     else if (key.contentEquals("FULL SET"))
/*  39 */       this.fullDataset = charset;
/*     */     else
/*  41 */       this.charsets.put(key, charset);
/*     */   }
/*     */ 
/*     */   public void addCharsets(Map<String, Charset> charsetsToAdd)
/*     */   {
/*  50 */     if (charsetsToAdd.containsKey("REMAINING")) {
/*  51 */       this.remainingCharset = ((Charset)charsetsToAdd.get("REMAINING"));
/*  52 */       charsetsToAdd.remove("REMAINING");
/*  53 */     } else if (charsetsToAdd.containsKey("FULL SET")) {
/*  54 */       this.fullDataset = ((Charset)charsetsToAdd.get("FULL SET"));
/*  55 */       charsetsToAdd.remove("FULL SET");
/*     */     }
/*  57 */     this.charsets.putAll(charsetsToAdd);
/*     */   }
/*     */ 
/*     */   public void binCharset(String key, Charset ch)
/*     */   {
/*  66 */     this.binnedCharsets.put(key, ch);
/*     */   }
/*     */ 
/*     */   public Map<String, Charset> getCharsets()
/*     */   {
/*  74 */     return new HashMap(this.charsets);
/*     */   }
/*     */ 
/*     */   public Map<String, Charset> getBinned()
/*     */   {
/*  82 */     return new HashMap(this.charsets);
/*     */   }
/*     */ 
/*     */   public Charset getCharset(String key)
/*     */   {
/*  92 */     Charset retCharset = null;
/*  93 */     if (key.contentEquals("REMAINING"))
/*  94 */       retCharset = this.remainingCharset;
/*  95 */     else if (key.contentEquals("FULL SET"))
/*  96 */       retCharset = this.fullDataset;
/*     */     else {
/*  98 */       retCharset = (Charset)this.charsets.get(key);
/*     */     }
/* 100 */     return retCharset;
/*     */   }
/*     */ 
/*     */   public Set<Charset> getPartitions()
/*     */   {
/* 108 */     return new TreeSet(this.partitions);
/*     */   }
/*     */ 
/*     */   public Set<Charset> getExcludedCharsets()
/*     */   {
/* 116 */     return new TreeSet(this.excludedCharsets);
/*     */   }
/*     */ 
/*     */   public Iterator<Charset> getCharsetIterator()
/*     */   {
/* 123 */     return this.charsets.values().iterator();
/*     */   }
/*     */ 
/*     */   public Iterator<Charset> getPartitionIterator()
/*     */   {
/* 130 */     return this.partitions.iterator();
/*     */   }
/*     */ 
/*     */   public Iterator<Charset> getExcludedCharsetIterator()
/*     */   {
/* 137 */     return this.excludedCharsets.iterator();
/*     */   }
/*     */ 
/*     */   public Iterator<Charset> getBinnedCharsetsIterator()
/*     */   {
/* 144 */     return this.binnedCharsets.values().iterator();
/*     */   }
/*     */ 
/*     */   public void removePartition(String removeLabel)
/*     */   {
/* 156 */     if (removeLabel.contentEquals("REMAINING")) {
/* 157 */       Charset removeCharset = this.remainingCharset;
/* 158 */       this.remainingCharset = null;
/* 159 */       if (removeCharset != null) this.partitions.remove(removeCharset); 
/*     */     }
/* 160 */     else if (removeLabel.contentEquals("FULL SET")) {
/* 161 */       Charset removeCharset = this.fullDataset;
/* 162 */       this.fullDataset = null;
/* 163 */       if (removeCharset != null) this.partitions.remove(removeCharset); 
/*     */     }
/* 165 */     else { Charset removeCharset = getCharset(removeLabel);
/* 166 */       this.partitions.remove(removeCharset);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void removeExcluded(String removeLabel)
/*     */   {
/* 177 */     Charset remCharset = getCharset(removeLabel);
/* 178 */     this.excludedCharsets.remove(remCharset);
/*     */   }
/*     */ 
/*     */   public void excludeCharset(Charset exCharset)
/*     */   {
/* 187 */     if (this.charsets.containsValue(exCharset))
/* 188 */       this.excludedCharsets.add(exCharset);
/*     */     else
/* 190 */       unknownCharset();
/*     */   }
/*     */ 
/*     */   public boolean contains(String label)
/*     */   {
/* 201 */     return this.charsets.containsKey(label);
/*     */   }
/*     */ 
/*     */   public boolean containsPartition(String label)
/*     */   {
/* 210 */     Charset queryCharset = getCharset(label);
/* 211 */     if (queryCharset == null) {
/* 212 */       if (label.contentEquals("REMAINING")) {
/* 213 */         if ((this.remainingCharset != null) && (this.partitions.contains(this.remainingCharset)))
/* 214 */           return true;
/* 215 */         if ((this.fullDataset != null) && (this.partitions.contains(this.fullDataset))) {
/* 216 */           return true;
/*     */         }
/*     */       }
/* 219 */       return false;
/*     */     }
/* 221 */     return this.partitions.contains(queryCharset);
/*     */   }
/*     */ 
/*     */   public boolean containsPartition(Charset ch)
/*     */   {
/* 229 */     return this.partitions.contains(ch);
/*     */   }
/*     */ 
/*     */   public boolean containsExcluded(String label)
/*     */   {
/* 238 */     Charset queryCharset = (Charset)this.charsets.get(label);
/* 239 */     if (queryCharset == null) {
/* 240 */       return false;
/*     */     }
/* 242 */     return this.excludedCharsets.contains(queryCharset);
/*     */   }
/*     */ 
/*     */   public boolean containsExcluded(Charset ch)
/*     */   {
/* 251 */     return this.excludedCharsets.contains(ch);
/*     */   }
/*     */ 
/*     */   public void excludeCharset(String charsetLabel)
/*     */   {
/* 259 */     Charset exCharset = getCharset(charsetLabel);
/* 260 */     if (exCharset == null) {
/* 261 */       unknownCharset();
/*     */     }
/* 263 */     this.excludedCharsets.add(exCharset);
/*     */   }
/*     */ 
/*     */   public void replaceExcludedCharsets(Collection<Charset> exCharsets)
/*     */   {
/* 271 */     this.excludedCharsets.clear();
/* 272 */     addExcludedCharsets(exCharsets);
/*     */   }
/*     */ 
/*     */   public void addExcludedCharsets(Collection<Charset> exCharsets)
/*     */   {
/* 280 */     for (Charset charset : exCharsets)
/* 281 */       if (this.charsets.containsValue(charset))
/* 282 */         excludeCharset(charset);
/*     */       else
/* 284 */         unknownCharset();
/*     */   }
/*     */ 
/*     */   public void partitionCharset(Charset partCharset)
/*     */   {
/* 295 */     if (partCharset.getLabel().contentEquals("FULL SET")) {
/* 296 */       this.fullDataset = partCharset;
/* 297 */       this.partitions.add(this.fullDataset);
/* 298 */       return;
/*     */     }
/*     */ 
/* 301 */     if (partCharset.getLabel().contentEquals("REMAINING")) {
/* 302 */       this.remainingCharset = partCharset;
/* 303 */       this.partitions.add(this.remainingCharset);
/* 304 */       return;
/*     */     }
/*     */ 
/* 307 */     if (this.charsets.containsValue(partCharset))
/* 308 */       this.partitions.add(partCharset);
/*     */     else
/* 310 */       unknownCharset();
/*     */   }
/*     */ 
/*     */   public void partitionCharset(String label)
/*     */   {
/* 319 */     Charset partCharset = getCharset(label);
/* 320 */     this.partitions.add(partCharset);
/*     */   }
/*     */ 
/*     */   public void replacePartitionCharsets(Collection<Charset> partCharsets)
/*     */   {
/* 328 */     this.partitions.clear();
/* 329 */     addPartitionCharsets(partCharsets);
/*     */   }
/*     */ 
/*     */   public void addPartitionCharsets(Collection<Charset> partCharsets)
/*     */   {
/* 338 */     for (Charset charset : partCharsets)
/* 339 */       if ((this.charsets.containsValue(charset)) || (charset.getLabel().contentEquals("FULL SET")) || 
/* 340 */         (charset.getLabel().contentEquals("REMAINING")))
/* 341 */         partitionCharset(charset);
/*     */       else
/* 343 */         unknownCharset();
/*     */   }
/*     */ 
/*     */   private void unknownCharset()
/*     */   {
/* 349 */     System.err.println("Unknown charset");
/*     */   }
/*     */ 
/*     */   public void removeCharset(String key)
/*     */   {
/* 358 */     Charset charsetToRemove = (Charset)this.charsets.get(key);
/* 359 */     if (this.excludedCharsets.contains(charsetToRemove)) {
/* 360 */       this.excludedCharsets.remove(charsetToRemove);
/*     */     }
/* 362 */     if (this.partitions.contains(charsetToRemove)) {
/* 363 */       this.partitions.remove(charsetToRemove);
/*     */     }
/*     */ 
/* 366 */     if (this.binnedCharsets.containsKey(charsetToRemove)) {
/* 367 */       this.binnedCharsets.remove(charsetToRemove);
/*     */     }
/*     */ 
/* 370 */     if (key.contentEquals("REMAINING"))
/* 371 */       this.remainingCharset = null;
/* 372 */     else if (key.contentEquals("FULL SET"))
/* 373 */       this.fullDataset = null;
/*     */     else
/* 375 */       this.charsets.remove(key);
/*     */   }
/*     */ 
/*     */   public void clearAll()
/*     */   {
/* 383 */     this.charsets.clear();
/* 384 */     this.partitions.clear();
/* 385 */     this.excludedCharsets.clear();
/* 386 */     this.binnedCharsets.clear();
/* 387 */     this.remainingCharset = null;
/* 388 */     this.fullDataset = null;
/*     */   }
/*     */ 
/*     */   public void clearPartitions()
/*     */   {
/* 395 */     this.partitions.clear();
/* 396 */     this.fullDataset = null;
/* 397 */     this.remainingCharset = null;
/*     */   }
/*     */ 
/*     */   public void clearExcludedCharsets()
/*     */   {
/* 404 */     this.excludedCharsets.clear();
/*     */   }
/*     */ 
/*     */   public int numPartitions()
/*     */   {
/* 411 */     return this.partitions.size();
/*     */   }
/*     */ 
/*     */   public int numCharsets()
/*     */   {
/* 419 */     int numCharsets = this.charsets.size();
/* 420 */     return numCharsets;
/*     */   }
/*     */ 
/*     */   public int numExcludedCharsets() {
/* 424 */     return this.excludedCharsets.size();
/*     */   }
/*     */ 
/*     */   public boolean isPartitionsEmpty() {
/* 428 */     return this.partitions.isEmpty();
/*     */   }
/*     */ 
/*     */   public boolean isExcludedCharsetsEmpty() {
/* 432 */     return this.excludedCharsets.isEmpty();
/*     */   }
/*     */ 
/*     */   public boolean isCharsetsEmpty() {
/* 436 */     return this.charsets.isEmpty();
/*     */   }
/*     */ 
/*     */   void translateCodonToNucleotideCharsets()
/*     */   {
/*     */     int startOfCodonDomain;
/*     */     int startOfCodonDomain;
/* 446 */     if (this.P.codonDomain != null)
/* 447 */       startOfCodonDomain = this.P.codonDomain.getStartCodonDomainPosition();
/*     */     else
/* 449 */       startOfCodonDomain = -1;
/*     */     Charset oldCharset;
/* 452 */     for (String charsetKey : this.charsets.keySet()) {
/* 453 */       oldCharset = (Charset)this.charsets.get(charsetKey);
/* 454 */       Charset newCharset = translateBackTheCharsets(startOfCodonDomain, oldCharset);
/*     */ 
/* 456 */       if (this.partitions.contains(oldCharset)) {
/* 457 */         this.partitions.remove(oldCharset);
/* 458 */         this.partitions.add(newCharset);
/*     */       }
/* 460 */       if (this.excludedCharsets.contains(oldCharset)) {
/* 461 */         this.excludedCharsets.remove(oldCharset);
/* 462 */         this.excludedCharsets.add(newCharset);
/*     */       }
/* 464 */       this.charsets.put(charsetKey, newCharset);
/*     */     }
/* 466 */     Collection keySet = new ArrayList(this.binnedCharsets.keySet());
/* 467 */     for (String key : keySet) {
/* 468 */       Charset c = (Charset)this.binnedCharsets.get(key);
/* 469 */       this.binnedCharsets.remove(key);
/* 470 */       this.charsets.put(key, c);
/*     */     }
/*     */   }
/*     */ 
/*     */   void translateToCodons(int newFirstPosition, int newLastPosition)
/*     */   {
/*     */     boolean areAlreadyCodons;
/*     */     int currentStartPosition;
/*     */     boolean areAlreadyCodons;
/* 483 */     if (this.P.codonDomain == null) {
/* 484 */       int currentStartPosition = -1;
/* 485 */       areAlreadyCodons = false;
/*     */     } else {
/* 487 */       currentStartPosition = this.P.codonDomain
/* 488 */         .getStartCodonDomainPosition();
/* 489 */       areAlreadyCodons = true;
/*     */     }
/* 491 */     translateToCodonCharsets(newFirstPosition, newLastPosition, currentStartPosition, areAlreadyCodons);
/* 492 */     reEvaluateBinnedCharsets(newFirstPosition, newLastPosition, currentStartPosition);
/*     */   }
/*     */ 
/*     */   private void binInconsistentCharsets(int firstPosition, int lastPosition, int currentFirstPosition, boolean areAllreadyCodons)
/*     */   {
/* 507 */     boolean anyNewBinned = false;
/* 508 */     if (this.charsets.isEmpty()) {
/* 509 */       return;
/*     */     }
/* 511 */     Collection keySet = new ArrayList(this.charsets.keySet());
/* 512 */     for (String key : keySet) {
/* 513 */       Charset charset = (Charset)this.charsets.get(key);
/* 514 */       String s = charset.getAllRanges();
/* 515 */       String[] ranges = s.split(" ");
/* 516 */       for (String range : ranges) {
/* 517 */         String[] startAndEnd = range.split("-");
/* 518 */         assert (startAndEnd.length <= 2) : "invalid length";
/* 519 */         int start = Integer.parseInt(startAndEnd[0]);
/* 520 */         int end = start;
/* 521 */         if (startAndEnd.length == 2) {
/* 522 */           end = Integer.parseInt(startAndEnd[1]);
/*     */         }
/* 524 */         assert (end >= start) : "Error in start end positions";
/*     */ 
/* 528 */         if (areAllreadyCodons) {
/* 529 */           start = translateStartPositionCodToNuc(
/* 530 */             currentFirstPosition, start);
/* 531 */           end = translateEndPositionCodToNuc(
/* 532 */             currentFirstPosition, end);
/*     */         }
/* 534 */         if ((start < firstPosition) || (end > lastPosition) || 
/* 535 */           (start > lastPosition) || (end < firstPosition) || 
/* 536 */           ((start - firstPosition) % 3 != 0) || ((end - firstPosition + 1) % 3 != 0)) {
/* 537 */           this.charsets.remove(key);
/* 538 */           if (this.partitions.contains(charset)) this.partitions.remove(charset);
/* 539 */           if (this.excludedCharsets.contains(charset)) this.excludedCharsets.remove(charset);
/* 540 */           if (areAllreadyCodons) {
/* 541 */             charset = translateBackTheCharsets(this.P.codonDomain.getStartCodonDomainPosition(), charset);
/*     */           }
/* 543 */           this.binnedCharsets.put(key, charset);
/* 544 */           anyNewBinned = true;
/* 545 */           break;
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 550 */     if (anyNewBinned) {
/* 551 */       String binnedMessage = "Charset limits are in collision with codons, i.e. some of charset limits are splitting a codon in half. These charsets will be unavailable in the codon mode of the analysis";
/*     */ 
/* 554 */       Tools.showWarningMessage(null, binnedMessage, "Warning");
/*     */     }
/*     */   }
/*     */ 
/*     */   private void reEvaluateBinnedCharsets(int newFirstPosition, int newLastPosition, int currentFirstPosition)
/*     */   {
/* 565 */     if (this.binnedCharsets.isEmpty()) return;
/*     */ 
/* 567 */     Collection keySet = new ArrayList(this.binnedCharsets.keySet());
/*     */     int j;
/*     */     int i;
/* 568 */     for (Iterator localIterator = keySet.iterator(); localIterator.hasNext(); 
/* 572 */       i < j)
/*     */     {
/* 568 */       String key = (String)localIterator.next();
/* 569 */       Charset binned = (Charset)this.binnedCharsets.get(key);
/* 570 */       String rangesString = binned.getAllRanges();
/* 571 */       String[] ranges = rangesString.split(" ");
/*     */       String[] arrayOfString1;
/* 572 */       j = (arrayOfString1 = ranges).length; i = 0; continue; String range = arrayOfString1[i];
/* 573 */       String[] startAndEnd = range.split("-");
/* 574 */       assert (startAndEnd.length <= 2) : "invalid length";
/* 575 */       int start = Integer.parseInt(startAndEnd[0]);
/* 576 */       int end = start;
/* 577 */       if (startAndEnd.length == 2) {
/* 578 */         end = Integer.parseInt(startAndEnd[1]);
/*     */       }
/* 580 */       assert (end >= start) : "Error in start end positions";
/*     */ 
/* 582 */       if ((start >= newFirstPosition) && (end <= newLastPosition) && 
/* 583 */         (start <= newLastPosition) && (end >= newFirstPosition) && 
/* 584 */         ((start - newFirstPosition) % 3 == 0) && ((end - newFirstPosition + 1) % 3 == 0)) {
/* 585 */         this.binnedCharsets.remove(key);
/* 586 */         Charset translatedBinned = translateCharsetToCodonCharset(newFirstPosition, currentFirstPosition, false, binned);
/* 587 */         this.charsets.put(key, translatedBinned);
/*     */       }
/* 572 */       i++;
/*     */     }
/*     */   }
/*     */ 
/*     */   private void translateToCodonCharsets(int firstPosition, int lastPosition, int currentFirstPosition, boolean areAllreadyCodons)
/*     */   {
/* 595 */     binInconsistentCharsets(firstPosition, lastPosition, currentFirstPosition, areAllreadyCodons);
/* 596 */     for (String key : this.charsets.keySet()) {
/* 597 */       Charset charset = (Charset)this.charsets.get(key);
/* 598 */       Charset translatedCharset = translateCharsetToCodonCharset(
/* 599 */         firstPosition, currentFirstPosition, areAllreadyCodons, 
/* 600 */         charset);
/* 601 */       this.charsets.put(key, translatedCharset);
/* 602 */       if (this.partitions.contains(charset)) {
/* 603 */         this.partitions.remove(charset);
/* 604 */         this.partitions.add(translatedCharset);
/*     */       }
/* 606 */       if (this.excludedCharsets.contains(charset)) {
/* 607 */         this.excludedCharsets.remove(charset);
/* 608 */         this.excludedCharsets.add(translatedCharset);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   private Charset translateCharsetToCodonCharset(int firstPosition, int currentFirstPosition, boolean areAllreadyCodons, Charset charset)
/*     */     throws NumberFormatException
/*     */   {
/* 623 */     Charset translatedCharset = new Charset(charset.getLabel());
/* 624 */     String s = charset.getAllRanges();
/* 625 */     String[] allRanges = s.split(" ");
/* 626 */     for (String range : allRanges) {
/* 627 */       String[] startAndEnd = range.split("-");
/* 628 */       assert (startAndEnd.length <= 2) : "invalid length";
/*     */ 
/* 630 */       int start = Integer.parseInt(startAndEnd[0]);
/* 631 */       int end = start;
/* 632 */       if (startAndEnd.length == 2) {
/* 633 */         end = Integer.parseInt(startAndEnd[1]);
/*     */       }
/*     */ 
/* 636 */       if (areAllreadyCodons) {
/* 637 */         start = translateStartPositionCodToNuc(currentFirstPosition, start);
/* 638 */         end = translateEndPositionCodToNuc(currentFirstPosition, end);
/*     */       }
/* 640 */       assert (start <= end) : "invalid start & end position";
/*     */ 
/* 642 */       start = (start - firstPosition) / 3 + 1;
/* 643 */       end = (end - firstPosition + 1) / 3;
/*     */ 
/* 645 */       String translatedRange = start + "-" + end;
/* 646 */       translatedCharset.addRange(translatedRange);
/*     */     }
/* 648 */     return translatedCharset;
/*     */   }
/*     */   private Charset translateBackTheCharsets(int startPositionReference, Charset charset) {
/* 651 */     Charset translatedCharset = new Charset(charset.getLabel());
/* 652 */     String rangesStringSet = charset.getAllRanges();
/* 653 */     String[] ranges = rangesStringSet.split(" ");
/* 654 */     for (String rangeString : ranges) {
/* 655 */       String[] startAndEnd = rangeString.split("-");
/* 656 */       assert (startAndEnd.length <= 2) : "invalid length";
/* 657 */       int start = Integer.parseInt(startAndEnd[0]);
/* 658 */       int end = start;
/* 659 */       if (startAndEnd.length == 2) {
/* 660 */         end = Integer.parseInt(startAndEnd[1]);
/*     */       }
/* 662 */       start = translateStartPositionCodToNuc(startPositionReference, start);
/* 663 */       end = translateEndPositionCodToNuc(startPositionReference, end);
/* 664 */       String newRange = start + "-" + end;
/* 665 */       translatedCharset.addRange(newRange);
/*     */     }
/*     */ 
/* 668 */     return translatedCharset;
/*     */   }
/*     */ 
/*     */   private int translateStartPositionCodToNuc(int startPos, int start)
/*     */   {
/* 673 */     return startPos + (start - 1) * 3;
/*     */   }
/*     */ 
/*     */   private int translateEndPositionCodToNuc(int startPos, int end) {
/* 677 */     return startPos + (end - 1) * 3 + 2;
/*     */   }
/*     */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.CharsetsContainer
 * JD-Core Version:    0.6.2
 */