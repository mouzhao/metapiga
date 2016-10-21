package metapiga;/*     */
/*     */ 
/*     */

/*     */ import java.util.List;
/*     */ import javax.swing.JProgressBar;
/*     */ import javax.swing.SwingUtilities;
/*     */ import metapiga.utilities.Tools;
/*     */ 
/*     */ public class ProgressHandling
/*     */ {
/*  18 */   private static final char[] indeterminateChars = { '|', '/', '-', '\\' };
/*  19 */   public static int consoleWidth = 79;
/*     */   private JProgressBar[] progressBar;
/*     */   private int nbrProgress;
/*  23 */   private MetaPIGA.UI ui = MetaPIGA.UI.GRAPHICAL;
/*     */   private String[] currentText;
/*     */   private int[] currentValue;
/*     */   private long[] currentTime;
/*     */   private String indeterminateText;
/*     */   private boolean[] showPercent;
/*     */   private boolean[] showTime;
/*     */   private boolean waitForEndMessage;
/*     */   private boolean finished;
/*     */   private int lastLineLength;
/*     */   private int[] currentIndChar;
/*     */ 
/*     */   public ProgressHandling(JProgressBar progressBar)
/*     */   {
/*  36 */     this.nbrProgress = 1;
/*  37 */     this.progressBar = new JProgressBar[this.nbrProgress];
/*  38 */     this.progressBar[0] = progressBar;
/*  39 */     this.currentText = new String[this.nbrProgress];
/*  40 */     this.currentTime = new long[this.nbrProgress];
/*  41 */     this.currentValue = new int[this.nbrProgress];
/*  42 */     this.showPercent = new boolean[this.nbrProgress];
/*  43 */     this.showTime = new boolean[this.nbrProgress];
/*  44 */     this.currentIndChar = new int[this.nbrProgress];
/*     */   }
/*     */ 
/*     */   public ProgressHandling(int progressNumber) {
/*  48 */     this.nbrProgress = progressNumber;
/*  49 */     this.progressBar = new JProgressBar[progressNumber];
/*  50 */     for (int i = 0; i < progressNumber; i++)
/*  51 */       this.progressBar[i] = new JProgressBar();
/*  52 */     this.currentText = new String[this.nbrProgress];
/*  53 */     this.currentTime = new long[this.nbrProgress];
/*  54 */     this.currentValue = new int[this.nbrProgress];
/*  55 */     this.showPercent = new boolean[this.nbrProgress];
/*  56 */     this.showTime = new boolean[this.nbrProgress];
/*  57 */     this.currentIndChar = new int[this.nbrProgress];
/*     */   }
/*     */   public void setUI(MetaPIGA.UI ui) {
/*  60 */     this.ui = ui;
/*     */   }
/*     */ 
/*     */   public MetaPIGA.UI getUI() {
/*  64 */     return this.ui;
/*     */   }
/*     */ 
/*     */   public void newIndeterminateProgress(String text) {
/*  68 */     this.waitForEndMessage = false;
/*  69 */     this.finished = false;
/*  70 */     this.showPercent[0] = false;
/*  71 */     this.showTime[0] = false;
/*  72 */     this.progressBar[0].setIndeterminate(true);
/*  73 */     this.indeterminateText = text;
/*  74 */     this.progressBar[0].setString(text);
/*  75 */     this.progressBar[0].setStringPainted(true);
/*  76 */     this.lastLineLength = 0;
/*  77 */     this.currentIndChar[0] = 0;
/*  78 */     if (this.ui == MetaPIGA.UI.CONSOLE)
/*  79 */       System.out.println(text);
/*     */   }
/*     */ 
/*     */   public void newSingleProgress(int minimum, int maximum, String text)
/*     */   {
/*  84 */     this.waitForEndMessage = false;
/*  85 */     this.finished = false;
/*  86 */     this.showPercent[0] = true;
/*  87 */     this.showTime[0] = false;
/*  88 */     this.progressBar[0].setIndeterminate(false);
/*  89 */     this.progressBar[0].setMinimum(minimum);
/*  90 */     this.progressBar[0].setMaximum(maximum);
/*  91 */     this.lastLineLength = 0;
/*  92 */     this.currentIndChar[0] = 0;
/*  93 */     this.currentText[0] = text;
/*  94 */     setValue(0);
/*     */   }
/*     */ 
/*     */   public void newMultiProgress(int replicate, int minimum, int maximum, String text) {
/*  98 */     this.waitForEndMessage = true;
/*  99 */     this.finished = false;
/* 100 */     this.showPercent[replicate] = true;
/* 101 */     this.showTime[replicate] = false;
/* 102 */     this.progressBar[replicate].setIndeterminate(false);
/* 103 */     this.progressBar[replicate].setMinimum(minimum);
/* 104 */     this.progressBar[replicate].setMaximum(maximum);
/* 105 */     this.lastLineLength = 0;
/* 106 */     this.currentIndChar[replicate] = 0;
/* 107 */     this.currentText[replicate] = text;
/* 108 */     setValue(replicate, 0);
/*     */   }
/*     */ 
/*     */   public synchronized void newSearchProgress(int replicate, int maxSteps, long maxTime, String startLikelihood) {
/* 112 */     this.waitForEndMessage = true;
/* 113 */     this.showPercent[replicate] = (maxSteps > 0 ? 1 : false);
/* 114 */     this.showTime[replicate] = (maxTime > 0L ? 1 : false);
/* 115 */     this.progressBar[replicate].setIndeterminate(false);
/* 116 */     this.progressBar[replicate].setMinimum(0);
/* 117 */     this.progressBar[replicate].setMaximum(maxSteps);
/* 118 */     this.currentTime[replicate] = maxTime;
/* 119 */     this.currentText[replicate] = startLikelihood;
/* 120 */     this.lastLineLength = 0;
/* 121 */     this.currentIndChar[replicate] = 0;
/* 122 */     this.finished = false;
/* 123 */     setValue(replicate, 0);
/*     */   }
/*     */ 
/*     */   public void setValue(int replicate, int value) {
/* 127 */     this.currentValue[replicate] = (value + 1);
/* 128 */     this.progressBar[replicate].setValue(this.currentValue[replicate]);
/* 129 */     showProgress();
/*     */   }
/*     */ 
/*     */   public void setTime(int replicate, long time) {
/* 133 */     this.currentTime[replicate] = time;
/* 134 */     showProgress();
/*     */   }
/*     */ 
/*     */   public void setText(int replicate, String text) {
/* 138 */     this.currentText[replicate] = text;
/* 139 */     showProgress();
/*     */   }
/*     */ 
/*     */   public void setValue(int value) {
/* 143 */     setValue(0, value);
/*     */   }
/*     */ 
/*     */   public void setTime(long time) {
/* 147 */     setTime(0, time);
/*     */   }
/*     */ 
/*     */   public void setText(String text) {
/* 151 */     setText(0, text);
/*     */   }
/*     */ 
/*     */   private void showProgress() {
/* 155 */     if (this.ui != MetaPIGA.UI.SILENT)
/* 156 */       SwingUtilities.invokeLater(new Runnable() {
/*     */         public void run() {
/* 158 */           if (!ProgressHandling.this.finished) {
/* 159 */             StringBuilder text = new StringBuilder(ProgressHandling.consoleWidth);
/* 160 */             for (int p = 0; p < ProgressHandling.this.nbrProgress; p++) {
/* 161 */               if (p > 0) text.append(" | ");
/* 162 */               if (ProgressHandling.this.currentText[p] == null)
/* 163 */                 text.append("-");
/*     */               else
/* 165 */                 text.append(ProgressHandling.this.currentText[p]);
/* 166 */               if (ProgressHandling.this.showPercent[p] != 0) {
/* 167 */                 double completed = ProgressHandling.this.progressBar[p].getPercentComplete();
/* 168 */                 text.append(" -- " + Tools.doubleToPercent(completed, 0));
/* 169 */                 if ((ProgressHandling.this.ui == MetaPIGA.UI.CONSOLE) && (ProgressHandling.this.nbrProgress == 1)) {
/* 170 */                   text.append(" [");
/* 171 */                   for (int i = 0; i < 25; i++) {
/* 172 */                     if (completed * 100.0D < (i + 1) * 4) text.append("."); else
/* 173 */                       text.append("=");
/*     */                   }
/* 175 */                   text.append("]");
/*     */                 }
/*     */               }
/* 178 */               if (ProgressHandling.this.showTime[p] != 0) {
/* 179 */                 long sec = ProgressHandling.this.currentTime[p] / 1000L;
/* 180 */                 long h = sec / 3600L;
/* 181 */                 sec -= h * 3600L;
/* 182 */                 long min = sec / 60L;
/* 183 */                 sec -= min * 60L;
/* 184 */                 text.append(" -- " + (int)h + "h " + (int)min + "m " + (int)sec + "s" + " left");
/*     */               }
/* 186 */               if ((ProgressHandling.this.showPercent[p] == 0) && (ProgressHandling.this.showTime[p] == 0) && (ProgressHandling.this.ui == MetaPIGA.UI.CONSOLE)) {
/* 187 */                 text.append(" -- " + ProgressHandling.this.nextIndeterminateChar(p));
/*     */               }
/* 189 */               ProgressHandling.this.progressBar[p].setString(text.toString());
/*     */             }
/* 191 */             if (ProgressHandling.this.ui == MetaPIGA.UI.GRAPHICAL) {
/* 192 */               for (int p = 0; p < ProgressHandling.this.nbrProgress; p++)
/* 193 */                 ProgressHandling.this.progressBar[p].setStringPainted(true);
/*     */             }
/*     */             else {
/* 196 */               for (int i = text.length(); i < ProgressHandling.this.lastLineLength; i++) {
/* 197 */                 text.append(" ");
/*     */               }
/* 199 */               if (text.length() > ProgressHandling.consoleWidth) {
/* 200 */                 text.delete(0, text.length());
/* 201 */                 for (int p = 0; p < ProgressHandling.this.nbrProgress; p++) {
/* 202 */                   if (p > 0) text.append(" | ");
/* 203 */                   if (ProgressHandling.this.currentText[p] == null)
/* 204 */                     text.append("-");
/* 205 */                   else if (ProgressHandling.this.currentText[p].startsWith("Best ML"))
/* 206 */                     text.append(ProgressHandling.this.currentText[p].split("\\.")[0].replace("Best ML : ", "ML:").replace(' ', ','));
/* 207 */                   else if (ProgressHandling.this.currentText[p].startsWith("Creating metapopulation"))
/* 208 */                     text.append("Pop");
/* 209 */                   else if (ProgressHandling.this.currentText[p].startsWith("Building distance matrix"))
/* 210 */                     text.append("DM");
/* 211 */                   else if (ProgressHandling.this.currentText[p].startsWith("Creating population"))
/* 212 */                     text.append("Pop");
/* 213 */                   else if (ProgressHandling.this.currentText[p].startsWith("Optimization"))
/* 214 */                     text.append("Opti");
/* 215 */                   else if (ProgressHandling.this.currentText[p].startsWith("Setting temperature"))
/* 216 */                     text.append("Temp");
/* 217 */                   else if (ProgressHandling.this.currentText[p].startsWith("Building starting tree")) {
/* 218 */                     text.append("StartTree");
/*     */                   }
/* 220 */                   if (ProgressHandling.this.showPercent[p] != 0) {
/* 221 */                     double completed = ProgressHandling.this.progressBar[p].getPercentComplete();
/* 222 */                     text.append(" " + Tools.doubleToPercent(completed, 0));
/*     */                   }
/* 224 */                   if (ProgressHandling.this.showTime[p] != 0) {
/* 225 */                     long sec = ProgressHandling.this.currentTime[p] / 1000L;
/* 226 */                     long h = sec / 3600L;
/* 227 */                     sec -= h * 3600L;
/* 228 */                     long min = sec / 60L;
/* 229 */                     sec -= min * 60L;
/* 230 */                     text.append(" " + (int)h + ":" + (int)min + ":" + (int)sec);
/*     */                   }
/* 232 */                   if ((ProgressHandling.this.showPercent[p] == 0) && (ProgressHandling.this.showTime[p] == 0) && (ProgressHandling.this.ui == MetaPIGA.UI.CONSOLE)) {
/* 233 */                     text.append(" " + ProgressHandling.this.nextIndeterminateChar(p));
/*     */                   }
/* 235 */                   ProgressHandling.this.progressBar[p].setString(text.toString());
/*     */                 }
/* 237 */                 text.setLength(ProgressHandling.consoleWidth);
/*     */               }
/* 239 */               System.out.print("\r" + text.toString());
/* 240 */               ProgressHandling.this.lastLineLength = text.length();
/*     */             }
/* 242 */             ProgressHandling.this.finished = true;
/* 243 */             for (int p = 0; p < ProgressHandling.this.nbrProgress; p++) {
/* 244 */               if (((ProgressHandling.this.showPercent[p] != 0) && (ProgressHandling.this.currentValue[p] >= ProgressHandling.this.progressBar[p].getMaximum())) || (
/* 245 */                 (ProgressHandling.this.showTime[p] != 0) && (ProgressHandling.this.currentTime[p] == 0L))) {
/* 246 */                 ProgressHandling.this.progressBar[p].setIndeterminate(true);
/* 247 */                 ProgressHandling.this.progressBar[p].setString(ProgressHandling.this.indeterminateText);
/*     */               } else {
/* 249 */                 ProgressHandling.this.finished = false;
/*     */               }
/*     */             }
/* 252 */             if ((ProgressHandling.this.finished) && (ProgressHandling.this.ui == MetaPIGA.UI.CONSOLE) && (!ProgressHandling.this.waitForEndMessage))
/* 253 */               System.out.println();
/*     */           }
/*     */         }
/*     */       });
/*     */   }
/*     */ 
/*     */   public synchronized void displayEndMessage(List<String> textLines)
/*     */   {
/* 262 */     if (this.ui != MetaPIGA.UI.SILENT) {
/* 263 */       String message = (String)textLines.remove(0);
/* 264 */       StringBuilder s = new StringBuilder("\r" + message);
/* 265 */       for (int i = message.length(); i < this.lastLineLength; i++) {
/* 266 */         s.append(" ");
/*     */       }
/* 268 */       for (String st : textLines) {
/* 269 */         s.append("\n" + st);
/*     */       }
/* 271 */       System.out.println(s.toString());
/*     */     }
/*     */   }
/*     */ 
/*     */   private char nextIndeterminateChar(int replicate) {
/* 276 */     if (this.currentIndChar[replicate] >= indeterminateChars.length) this.currentIndChar[replicate] = 0;
/*     */     int tmp28_27 = replicate;
/*     */     int[] tmp28_24 = this.currentIndChar;
/*     */     int tmp30_29 = tmp28_24[tmp28_27]; tmp28_24[tmp28_27] = (tmp30_29 + 1); return indeterminateChars[tmp30_29];
/*     */   }
/*     */ 
/*     */   public void setVisible(boolean isVisible) {
/* 281 */     if (this.ui == MetaPIGA.UI.GRAPHICAL)
/* 282 */       this.progressBar[0].setVisible(isVisible);
/*     */   }
/*     */ 
/*     */   public void printText(String text)
/*     */   {
/* 287 */     if (this.ui == MetaPIGA.UI.CONSOLE)
/* 288 */       System.out.println(text);
/*     */   }
/*     */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.ProgressHandling
 * JD-Core Version:    0.6.2
 */