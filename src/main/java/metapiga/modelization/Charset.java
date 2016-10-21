/*     */ package metapiga.modelization;
/*     */ 
/*     */ import java.awt.Color;
/*     */
/*     */ import java.awt.Dimension;
/*     */ import java.awt.Font;
/*     */ import java.awt.Frame;
/*     */ import java.awt.GridBagConstraints;
/*     */ import java.awt.GridBagLayout;
/*     */ import java.awt.Toolkit;
/*     */ import java.util.ArrayList;
/*     */ import java.util.BitSet;
/*     */
/*     */ import java.util.Hashtable;
/*     */ import java.util.Iterator;
/*     */ import java.util.LinkedList;
/*     */ import java.util.List;
/*     */ import java.util.Set;
/*     */ import java.util.TreeSet;
/*     */
/*     */ import javax.swing.JDialog;
/*     */ import javax.swing.JPanel;
/*     */ import javax.swing.JScrollPane;
/*     */ import javax.swing.JTextPane;
/*     */
/*     */ import javax.swing.text.AttributeSet;
/*     */ import javax.swing.text.BadLocationException;
/*     */ import javax.swing.text.DefaultStyledDocument;
/*     */ import javax.swing.text.SimpleAttributeSet;
/*     */ import javax.swing.text.StyleConstants;
/*     */ import metapiga.MainFrame;
/*     */ import metapiga.exceptions.UnknownDataException;
/*     */ import metapiga.modelization.data.DataType;
/*     */ import metapiga.parameters.Parameters;
/*     */
/*     */ import metapiga.utilities.Tools;
/*     */ import org.biojavax.bio.phylo.io.nexus.CharactersBlock;
/*     */ 
/*     */ public class Charset
/*     */   implements Comparable<Charset>
/*     */ {
/*     */   public static final String FULL_SET = "FULL SET";
/*     */   public static final String REMAINING = "REMAINING";
/*     */   private String label;
/*     */   private List<Range> characters;
/*  56 */   private boolean isGoingToNexus = true;
/*     */ 
/*     */   public Charset(String label)
/*     */   {
/*  78 */     this.label = label;
/*  79 */     this.characters = new LinkedList();
/*     */   }
/*     */ 
/*     */   public void setLabel(String label) {
/*  83 */     this.label = label;
/*     */   }
/*     */ 
/*     */   public void addRange(int start, int end)
/*     */   {
/*  94 */     Set current = new TreeSet();
/*     */     Range r;
/*     */     int i;
/*  95 */     for (Iterator localIterator1 = this.characters.iterator(); localIterator1.hasNext(); 
/*  96 */       i <= r.end)
/*     */     {
/*  95 */       r = (Range)localIterator1.next();
/*  96 */       i = r.start; continue;
/*  97 */       current.add(Integer.valueOf(i));
/*     */ 
/*  96 */       i++;
/*     */     }
/*     */ 
/* 100 */     for (int i = start; i <= end; i++) {
/* 101 */       current.add(Integer.valueOf(i));
/*     */     }
/* 103 */     this.characters.clear();
/* 104 */     int currentStart = -1;
/* 105 */     int currentEnd = -1;
/* 106 */     for (Iterator localIterator2 = current.iterator(); localIterator2.hasNext(); ) { int i = ((Integer)localIterator2.next()).intValue();
/* 107 */       if (i > 0) {
/* 108 */         if (currentStart == -1) {
/* 109 */           currentStart = i;
/* 110 */           currentEnd = i;
/*     */         }
/* 112 */         else if (i == currentEnd + 1) {
/* 113 */           currentEnd = i;
/*     */         } else {
/* 115 */           this.characters.add(new Range(currentStart, currentEnd));
/* 116 */           currentStart = i;
/* 117 */           currentEnd = i;
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 122 */     if (currentStart != -1)
/* 123 */       this.characters.add(new Range(currentStart, currentEnd));
/*     */   }
/*     */ 
/*     */   public void removeRange(int start, int end)
/*     */   {
/* 134 */     Set current = new TreeSet();
/*     */     Range r;
/*     */     int i;
/* 135 */     for (Iterator localIterator1 = this.characters.iterator(); localIterator1.hasNext(); 
/* 136 */       i <= r.end)
/*     */     {
/* 135 */       r = (Range)localIterator1.next();
/* 136 */       i = r.start; continue;
/* 137 */       current.add(Integer.valueOf(i));
/*     */ 
/* 136 */       i++;
/*     */     }
/*     */ 
/* 140 */     for (int i = start; i <= end; i++) {
/* 141 */       current.remove(Integer.valueOf(i));
/*     */     }
/* 143 */     this.characters.clear();
/* 144 */     int currentStart = -1;
/* 145 */     int currentEnd = -1;
/* 146 */     for (Iterator localIterator2 = current.iterator(); localIterator2.hasNext(); ) { int i = ((Integer)localIterator2.next()).intValue();
/* 147 */       if (i > 0) {
/* 148 */         if (currentStart == -1) {
/* 149 */           currentStart = i;
/* 150 */           currentEnd = i;
/*     */         }
/* 152 */         else if (i == currentEnd + 1) {
/* 153 */           currentEnd = i;
/*     */         } else {
/* 155 */           this.characters.add(new Range(currentStart, currentEnd));
/* 156 */           currentStart = i;
/* 157 */           currentEnd = i;
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 162 */     if (currentStart != -1)
/* 163 */       this.characters.add(new Range(currentStart, currentEnd));
/*     */   }
/*     */ 
/*     */   public void addRange(String range)
/*     */   {
/* 176 */     range = range.trim();
/* 177 */     int slash = range.indexOf("/");
/* 178 */     int tiret = range.indexOf("-");
/* 179 */     String r = range;
/* 180 */     int step = 1;
/* 181 */     if (slash > 0) {
/* 182 */       String[] s = range.split("/");
/* 183 */       r = s[0];
/* 184 */       step = Integer.parseInt(s[1]);
/*     */     }
/*     */     int end;
/*     */     int start;
/*     */     int end;
/* 187 */     if (tiret == r.length() - 1) {
/* 188 */       int start = Integer.parseInt(r.substring(0, tiret));
/* 189 */       end = Integer.parseInt(r.substring(0, tiret));
/*     */     }
/*     */     else
/*     */     {
/*     */       int end;
/* 190 */       if (tiret == 0) {
/* 191 */         int start = 1;
/* 192 */         end = Integer.parseInt(r.substring(tiret + 1));
/*     */       }
/*     */       else
/*     */       {
/*     */         int end;
/* 193 */         if (tiret > 0) {
/* 194 */           int start = Integer.parseInt(r.substring(0, tiret));
/* 195 */           end = Integer.parseInt(r.substring(tiret + 1));
/*     */         } else {
/* 197 */           start = Integer.parseInt(r);
/* 198 */           end = Integer.parseInt(r);
/*     */         }
/*     */       }
/*     */     }
/* 200 */     if (step == 1)
/* 201 */       addRange(start, end);
/*     */     else
/* 203 */       for (int i = start; i <= end; i += step)
/* 204 */         addRange(i, i);
/*     */   }
/*     */ 
/*     */   public void removeRange(String range)
/*     */   {
/* 219 */     range = range.trim();
/* 220 */     int slash = range.indexOf("/");
/* 221 */     int tiret = range.indexOf("-");
/* 222 */     String r = range;
/* 223 */     int step = 1;
/* 224 */     if (slash > 0) {
/* 225 */       String[] s = range.split("/");
/* 226 */       r = s[0];
/* 227 */       step = Integer.parseInt(s[1]);
/*     */     }
/*     */     int end;
/*     */     int start;
/*     */     int end;
/* 230 */     if (tiret == r.length() - 1) {
/* 231 */       int start = Integer.parseInt(r.substring(0, tiret));
/* 232 */       end = Integer.parseInt(r.substring(0, tiret));
/*     */     }
/*     */     else
/*     */     {
/*     */       int end;
/* 233 */       if (tiret == 0) {
/* 234 */         int start = 1;
/* 235 */         end = Integer.parseInt(r.substring(tiret + 1));
/*     */       }
/*     */       else
/*     */       {
/*     */         int end;
/* 236 */         if (tiret > 0) {
/* 237 */           int start = Integer.parseInt(r.substring(0, tiret));
/* 238 */           end = Integer.parseInt(r.substring(tiret + 1));
/*     */         } else {
/* 240 */           start = Integer.parseInt(r);
/* 241 */           end = Integer.parseInt(r);
/*     */         }
/*     */       }
/*     */     }
/* 243 */     if (step == 1)
/* 244 */       removeRange(start, end);
/*     */     else
/* 246 */       for (int i = start; i <= end; i += step)
/* 247 */         removeRange(i, i);
/*     */   }
/*     */ 
/*     */   public void merge(Charset charset)
/*     */   {
/* 253 */     this.characters.addAll(charset.characters);
/*     */   }
/*     */ 
/*     */   public boolean isEmpty() {
/* 257 */     return this.characters.isEmpty();
/*     */   }
/*     */ 
/*     */   public String getLabel() {
/* 261 */     return this.label;
/*     */   }
/*     */ 
/*     */   public int getSize() {
/* 265 */     int size = 0;
/* 266 */     for (Range p : this.characters) {
/* 267 */       size += p.end - p.start + 1;
/*     */     }
/* 269 */     return size;
/*     */   }
/*     */ 
/*     */   public boolean isInCharset(int character) {
/* 273 */     for (Range p : this.characters) {
/* 274 */       if ((character >= p.start) && (character <= p.end)) {
/* 275 */         return true;
/*     */       }
/*     */     }
/* 278 */     return false;
/*     */   }
/*     */ 
/*     */   public List<Integer> getCharacters() {
/* 282 */     List chars = new ArrayList();
/*     */     Range p;
/*     */     int c;
/* 283 */     for (Iterator localIterator = this.characters.iterator(); localIterator.hasNext(); 
/* 284 */       c <= p.end)
/*     */     {
/* 283 */       p = (Range)localIterator.next();
/* 284 */       c = p.start; continue;
/* 285 */       chars.add(Integer.valueOf(c));
/*     */ 
/* 284 */       c++;
/*     */     }
/*     */ 
/* 288 */     return chars;
/*     */   }
/*     */ 
/*     */   public boolean intersect(Charset charset)
/*     */   {
/*     */     Iterator localIterator2;
/* 292 */     for (Iterator localIterator1 = charset.characters.iterator(); localIterator1.hasNext(); 
/* 293 */       localIterator2.hasNext())
/*     */     {
/* 292 */       Range r1 = (Range)localIterator1.next();
/* 293 */       localIterator2 = this.characters.iterator(); continue; Range r2 = (Range)localIterator2.next();
/* 294 */       if ((r1.start >= r2.start) && (r1.start <= r2.end))
/* 295 */         return true;
/* 296 */       if ((r2.start >= r1.start) && (r2.start <= r1.end))
/* 297 */         return true;
/* 298 */       if ((r1.end >= r2.start) && (r1.end <= r2.end))
/* 299 */         return true;
/* 300 */       if ((r2.end >= r1.start) && (r2.end <= r1.end)) {
/* 301 */         return true;
/*     */       }
/*     */     }
/* 304 */     return false;
/*     */   }
/*     */ 
/*     */   public String getAllRanges() {
/* 308 */     String res = "";
/* 309 */     for (Range p : this.characters) {
/* 310 */       res = res + p.toString() + " ";
/*     */     }
/* 312 */     return res.trim();
/*     */   }
/*     */ 
/*     */   public String toString() {
/* 316 */     return this.label;
/*     */   }
/*     */ 
/*     */   public int compareTo(Charset c) {
/* 320 */     return this.label.compareTo(c.label);
/*     */   }
/*     */ 
/*     */   public boolean equals(Object obj) {
/* 324 */     if (this == obj)
/* 325 */       return true;
/* 326 */     if ((obj == null) || (obj.getClass() != getClass()))
/* 327 */       return false;
/* 328 */     return this.label.equals(obj.toString());
/*     */   }
/*     */ 
/*     */   public int hashCode() {
/* 332 */     int hash = 42;
/* 333 */     hash = 31 * hash + (this.label == null ? 0 : this.label.hashCode());
/* 334 */     return hash;
/*     */   }
/*     */ 
/*     */   public void setAsNonRecordable()
/*     */   {
/* 343 */     this.isGoingToNexus = false;
/*     */   }
/*     */ 
/*     */   public boolean isRecordable()
/*     */   {
/* 352 */     return this.isGoingToNexus;
/*     */   }
/*     */ 
/*     */   public void show(Parameters P)
/*     */     throws BadLocationException, UnknownDataException
/*     */   {
/* 365 */     Frame charFrame = new Frame();
/* 366 */     charFrame.setIconImage(Tools.getScaledIcon(MainFrame.imageMatrix, 32).getImage());
/*     */ 
/* 368 */     JDialog dialog = new JDialog(charFrame, "Charset : " + getLabel(), true);
/* 369 */     JScrollPane nexusScrollPane = new JScrollPane();
/* 370 */     JPanel nowordwrap = new JPanel();
/* 371 */     nowordwrap.setLayout(new GridBagLayout());
/* 372 */     GridBagConstraints cons = new GridBagConstraints();
/* 373 */     cons.fill = 1;
/* 374 */     cons.weighty = 1.0D;
/* 375 */     cons.weightx = 1.0D;
/* 376 */     JTextPane pane = new JTextPane();
/* 377 */     pane.setBackground(Color.black);
/* 378 */     pane.setFont(new Font("Courier New", 0, 12));
/* 379 */     pane.setForeground(Color.green);
/* 380 */     pane.setOpaque(true);
/* 381 */     pane.setRequestFocusEnabled(true);
/* 382 */     pane.setCaretColor(Color.black);
/* 383 */     nexusScrollPane
/* 384 */       .setHorizontalScrollBarPolicy(32);
/* 385 */     nexusScrollPane
/* 386 */       .setVerticalScrollBarPolicy(22);
/*     */ 
/* 388 */     String NORMAL = "Normal";
/* 389 */     String ITALIC = "Italic";
/* 390 */     String BOLD = "Bold";
/* 391 */     DefaultStyledDocument doc = new DefaultStyledDocument();
/* 392 */     Hashtable paraStyles = new Hashtable();
/* 393 */     SimpleAttributeSet attr = new SimpleAttributeSet();
/* 394 */     paraStyles.put("Normal", attr);
/* 395 */     attr = new SimpleAttributeSet();
/* 396 */     StyleConstants.setItalic(attr, true);
/* 397 */     paraStyles.put("Italic", attr);
/* 398 */     attr = new SimpleAttributeSet();
/* 399 */     StyleConstants.setBold(attr, true);
/* 400 */     paraStyles.put("Bold", attr);
/* 401 */     AttributeSet defaultStyle = (AttributeSet)paraStyles.get("Normal");
/*     */ 
/* 404 */     int longestTaxon = 0;
/* 405 */     for (Iterator localIterator1 = P.charactersBlock.getMatrixLabels().iterator(); localIterator1.hasNext(); ) { Object taxa = localIterator1.next();
/* 406 */       if (taxa.toString().length() > longestTaxon) {
/* 407 */         longestTaxon = taxa.toString().length();
/*     */       }
/*     */     }
/* 410 */     DataType dataType = P.dataset.getDataType();
/* 411 */     String matchSymbol = P.charactersBlock.getMatchChar() == null ? "." : P.charactersBlock.getMatchChar();
/* 412 */     String missingSymbol = P.charactersBlock.getMissing() == null ? "." : P.charactersBlock.getMissing();
/* 413 */     String gapSymbol = P.charactersBlock.getGap() == null ? "." : P.charactersBlock.getGap();
/* 414 */     pane.setStyledDocument(doc = new DefaultStyledDocument());
/* 415 */     doc.insertString(doc.getLength(), "\n", defaultStyle);
/* 416 */     for (Iterator localIterator2 = P.charactersBlock.getMatrixLabels().iterator(); localIterator2.hasNext(); ) { Object taxa = localIterator2.next();
/* 417 */       List data = new LinkedList();
/* 418 */       for (Iterator localIterator3 = P.charactersBlock.getMatrixData(taxa.toString()).iterator(); localIterator3.hasNext(); ) { Object obj = localIterator3.next();
/* 419 */         String nucl = obj.toString();
/* 420 */         if (nucl.length() > 0) {
/* 421 */           if (nucl.length() > 1) {
/* 422 */             BitSet bitSet = new BitSet(dataType.numOfStates());
/* 423 */             for (char c : nucl.toCharArray()) {
/* 424 */               bitSet.set(dataType.getStateOf(c));
/*     */             }
/* 426 */             data.add(dataType.getData(bitSet).toString());
/*     */           }
/* 428 */           else if ((nucl.equals(matchSymbol)) || (
/* 429 */             (P.charactersBlock.isRespectCase()) && 
/* 430 */             (nucl
/* 430 */             .equalsIgnoreCase(matchSymbol)))) {
/* 431 */             data.add(matchSymbol);
/* 432 */           } else if ((nucl.equals(missingSymbol)) || (
/* 433 */             (P.charactersBlock.isRespectCase()) && 
/* 434 */             (nucl
/* 434 */             .equalsIgnoreCase(missingSymbol)))) {
/* 435 */             data.add(missingSymbol);
/* 436 */           } else if ((nucl.equals(gapSymbol)) || (
/* 437 */             (P.charactersBlock.isRespectCase()) && 
/* 438 */             (nucl
/* 438 */             .equalsIgnoreCase(gapSymbol)))) {
/* 439 */             data.add(gapSymbol);
/*     */           }
/*     */           else
/*     */           {
/*     */             try {
/* 444 */               data.add(nucl.toUpperCase());
/*     */             } catch (Exception e) {
/* 446 */               e.printStackTrace();
/* 447 */               throw new UnknownDataException(nucl, 
/* 448 */                 taxa.toString(), e.getCause());
/*     */             }
/*     */           }
/*     */         }
/*     */       }
/*     */ 
/* 454 */       attr = new SimpleAttributeSet();
/* 455 */       StyleConstants.setForeground(attr, Color.GREEN);
/* 456 */       String t = taxa.toString();
/* 457 */       int spaces = longestTaxon - taxa.toString().length();
/* 458 */       for (int j = 0; j < spaces; j++) {
/* 459 */         t = t + " ";
/*     */       }
/* 461 */       t = t + "    ";
/* 462 */       doc.insertString(doc.getLength(), t, attr);
/* 463 */       for (int i = 0; i < data.size(); i++) {
/* 464 */         attr = new SimpleAttributeSet();
/* 465 */         String s = (String)data.get(i);
/* 466 */         int domainStartPosition = P.codonDomain != null ? P.codonDomain.getStartCodonDomainPosition() : 1;
/* 467 */         int domainEndPosition = P.codonDomain != null ? P.codonDomain.getEndCodonDomainPosition() : data.size() + 1;
/* 468 */         int dataTypeSize = dataType.getRenderingSize();
/* 469 */         int relativePosition = (i + 1 - domainStartPosition) / dataTypeSize + 1;
/* 470 */         int grayness = 20;
/* 471 */         StyleConstants.setForeground(attr, Color.GREEN);
/* 472 */         StyleConstants.setBackground(attr, Color.black);
/* 473 */         if ((i + 1 < domainStartPosition) || (i + 1 > domainEndPosition)) {
/* 474 */           StyleConstants.setForeground(attr, new Color(grayness, grayness, grayness));
/* 475 */         } else if (isInCharset(relativePosition)) {
/* 476 */           StyleConstants.setForeground(attr, Color.BLACK);
/*     */ 
/* 478 */           StyleConstants.setBackground(attr, new Color(127, 255, 212));
/*     */         }
/* 480 */         doc.insertString(doc.getLength(), s, attr);
/*     */       }
/* 482 */       doc.insertString(doc.getLength(), "\n", defaultStyle);
/*     */     }
/*     */ 
/* 485 */     nowordwrap.add(pane, cons);
/* 486 */     nexusScrollPane.getViewport().add(nowordwrap, null);
/* 487 */     dialog.getContentPane().add(nexusScrollPane);
/* 488 */     Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
/* 489 */     dialog.pack();
/* 490 */     Dimension frameSize = dialog.getSize();
/* 491 */     if (frameSize.width > screenSize.width - 100)
/* 492 */       screenSize.width -= 100;
/* 493 */     if (frameSize.height > screenSize.height - 100)
/* 494 */       screenSize.height -= 100;
/* 495 */     dialog.setSize(frameSize);
/* 496 */     if (frameSize.height > screenSize.height) {
/* 497 */       frameSize.height = screenSize.height;
/*     */     }
/* 499 */     if (frameSize.width > screenSize.width) {
/* 500 */       frameSize.width = screenSize.width;
/*     */     }
/* 502 */     dialog.setLocation((screenSize.width - frameSize.width) / 2, 
/* 503 */       (screenSize.height - frameSize.height) / 2);
/* 504 */     dialog.setVisible(true);
/*     */   }
/*     */ 
/*     */   private static class Range
/*     */   {
/*     */     public int start;
/*     */     public int end;
/*     */ 
/*     */     public Range(int start, int end)
/*     */     {
/*  63 */       this.start = start;
/*  64 */       this.end = end;
/*     */     }
/*     */ 
/*     */     public boolean isInRange(int character)
/*     */     {
/*  69 */       return (character >= this.start) && (character <= this.end);
/*     */     }
/*     */ 
/*     */     public String toString() {
/*  73 */       return this.start;
/*     */     }
/*     */   }
/*     */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.Charset
 * JD-Core Version:    0.6.2
 */