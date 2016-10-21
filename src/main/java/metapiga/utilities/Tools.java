/*     */ package metapiga.utilities;
/*     */ 
/*     */ import Jama.Matrix;
/*     */ import cern.jet.random.Exponential;
/*     */ import cern.jet.random.Normal;
/*     */ import cern.jet.stat.Gamma;
/*     */ import cern.jet.stat.Probability;
/*     */ import com.install4j.api.SystemInfo;
/*     */ import java.awt.BorderLayout;
/*     */ import java.awt.Color;
/*     */ import java.awt.Component;
/*     */ import java.awt.Dimension;
/*     */ import java.awt.Image;
/*     */ import java.io.BufferedInputStream;
/*     */ import java.io.BufferedOutputStream;
/*     */ import java.io.BufferedReader;
/*     */ import java.io.File;
/*     */ import java.io.FileInputStream;
/*     */ import java.io.FileOutputStream;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.io.InputStreamReader;
/*     */ import java.io.OutputStream;
/*     */ import java.io.PrintStream;
/*     */ import java.lang.reflect.Method;
/*     */ import java.text.DecimalFormat;
/*     */ import java.text.DecimalFormatSymbols;
/*     */ import java.text.NumberFormat;
/*     */ import java.util.Locale;
/*     */ import java.util.zip.ZipEntry;
/*     */ import java.util.zip.ZipInputStream;
/*     */ import java.util.zip.ZipOutputStream;
/*     */ import javax.swing.ImageIcon;
/*     */ import javax.swing.JFileChooser;
/*     */ import javax.swing.JFrame;
/*     */ import javax.swing.JOptionPane;
/*     */ import javax.swing.JPanel;
/*     */ import javax.swing.JScrollPane;
/*     */ import javax.swing.JTextArea;
/*     */ import javax.swing.filechooser.FileSystemView;
/*     */ import metapiga.MetaPIGA;
/*     */ import metapiga.MetaPIGA.UI;
/*     */ import metapiga.modelization.Dataset;
/*     */ import metapiga.modelization.data.DataType;
/*     */ import metapiga.parameters.Parameters;
/*     */ import metapiga.parameters.Parameters.EvaluationDistribution;
/*     */ 
/*     */ public class Tools
/*     */ {
/*     */   private static final double normalMean = 1.0D;
/*     */   private static final double normalSD = 0.5D;
/*     */   private static final String ZIP_EXTENSION = ".zip";
/*     */   private static final int DEFAULT_LEVEL_COMPRESSION = 9;
/*     */ 
/*     */   public static String getErrorMessage(Exception e)
/*     */   {
/*  67 */     String message = e.getMessage();
/*  68 */     message = message + "\n Java exception : " + e.getCause() + " (" + e.getMessage() + ")";
/*  69 */     for (StackTraceElement el : e.getStackTrace()) {
/*  70 */       message = message + "\n\tat " + el.toString();
/*     */     }
/*  72 */     return message;
/*     */   }
/*     */ 
/*     */   public static JPanel getErrorPanel(String message, Exception e) {
/*  76 */     JPanel panel = new JPanel();
/*  77 */     panel.setLayout(new BorderLayout());
/*  78 */     JTextArea label = new JTextArea(message);
/*  79 */     Color bg = panel.getBackground();
/*  80 */     label.setBackground(new Color(bg.getRed(), bg.getGreen(), bg.getBlue()));
/*  81 */     label.setBorder(null);
/*  82 */     label.setEditable(false);
/*  83 */     panel.add(label, "North");
/*  84 */     JTextArea textArea = new JTextArea();
/*  85 */     textArea.setText(e.getMessage() + "\n");
/*  86 */     textArea.append("Java exception : " + e.getCause());
/*  87 */     for (StackTraceElement el : e.getStackTrace()) {
/*  88 */       textArea.append("\n  " + el.toString());
/*     */     }
/*  90 */     textArea.setCaretPosition(0);
/*  91 */     textArea.setEditable(false);
/*  92 */     JScrollPane scrollPane = new JScrollPane(textArea);
/*  93 */     panel.add(scrollPane, "Center");
/*  94 */     panel.setPreferredSize(new Dimension(500, 300));
/*  95 */     return panel;
/*     */   }
/*     */ 
/*     */   public static void ShowErrorMessage(Component parent, String message, String title) {
/*  99 */     JPanel panel = new JPanel();
/* 100 */     panel.setLayout(new BorderLayout());
/* 101 */     JTextArea textArea = new JTextArea();
/* 102 */     textArea.setText(message);
/* 103 */     textArea.setCaretPosition(0);
/* 104 */     textArea.setEditable(false);
/* 105 */     JScrollPane scrollPane = new JScrollPane(textArea);
/* 106 */     panel.add(scrollPane, "Center");
/* 107 */     panel.setPreferredSize(new Dimension(500, 300));
/* 108 */     JOptionPane.showMessageDialog(parent, panel, title, 0);
/*     */   }
/*     */ 
/*     */   public static void showWarningMessage(Component parent, String message, String title) {
/* 112 */     MetaPIGA.UI ui = MetaPIGA.ui;
/* 113 */     if (ui == MetaPIGA.UI.GRAPHICAL) {
/* 114 */       if (parent == null) {
/* 115 */         parent = new JFrame("WARNING");
/*     */       }
/* 117 */       JOptionPane.showMessageDialog(parent, message, title, 2);
/* 118 */     } else if (ui == MetaPIGA.UI.CONSOLE) {
/* 119 */       System.out.println(title + ":");
/* 120 */       System.out.println(message);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static ImageIcon getScaledIcon(ImageIcon icon, int size)
/*     */   {
/* 127 */     return new ImageIcon(icon.getImage().getScaledInstance(size, size, 4));
/*     */   }
/*     */ 
/*     */   public static String doubletoString(double x, int d) {
/* 131 */     if ((x != 0.0D) && (Math.abs(x) < Math.pow(10.0D, -d))) {
/* 132 */       if ((Double.isNaN(x)) || (Double.isInfinite(x))) {
/* 133 */         return x;
/*     */       }
/* 135 */       DecimalFormatSymbols dfs = new DecimalFormatSymbols();
/* 136 */       dfs.setDecimalSeparator('.');
/* 137 */       dfs.setGroupingSeparator(',');
/* 138 */       String card = "";
/* 139 */       for (int i = 0; i < d; i++) card = card + "#";
/* 140 */       NumberFormat formatter = new DecimalFormat("0." + card + "E0", dfs);
/* 141 */       return formatter.format(x);
/*     */     }
/* 143 */     NumberFormat fmt = NumberFormat.getInstance(Locale.US);
/* 144 */     if ((fmt instanceof DecimalFormat)) {
/* 145 */       DecimalFormatSymbols symb = new DecimalFormatSymbols(Locale.US);
/* 146 */       symb.setGroupingSeparator(' ');
/* 147 */       ((DecimalFormat)fmt).setDecimalFormatSymbols(symb);
/* 148 */       ((DecimalFormat)fmt).setMaximumFractionDigits(d);
/*     */ 
/* 150 */       ((DecimalFormat)fmt).setGroupingUsed(true);
/*     */     }
/* 152 */     String s = fmt.format(x);
/* 153 */     return s;
/*     */   }
/*     */ 
/*     */   public static double parseDouble(String s)
/*     */   {
/* 158 */     NumberFormat fmt = NumberFormat.getInstance(Locale.US);
/* 159 */     if ((fmt instanceof DecimalFormat)) {
/* 160 */       DecimalFormatSymbols symb = new DecimalFormatSymbols(Locale.US);
/* 161 */       symb.setGroupingSeparator(' ');
/* 162 */       ((DecimalFormat)fmt).setDecimalFormatSymbols(symb);
/*     */ 
/* 164 */       ((DecimalFormat)fmt).setGroupingUsed(true);
/*     */     }
/*     */     try {
/* 167 */       Number n = fmt.parse(s);
/* 168 */       return n.doubleValue();
/*     */     } catch (Exception e) {
/* 170 */       System.err.println("Cannot parse double '" + s + "'");
/* 171 */     }return 0.0D;
/*     */   }
/*     */ 
/*     */   public static String doubleToPercent(double x, int d)
/*     */   {
/* 176 */     x *= 100.0D;
/* 177 */     NumberFormat fmt = NumberFormat.getInstance(Locale.US);
/* 178 */     if ((fmt instanceof DecimalFormat)) {
/* 179 */       DecimalFormatSymbols symb = new DecimalFormatSymbols(Locale.US);
/* 180 */       symb.setGroupingSeparator(' ');
/* 181 */       ((DecimalFormat)fmt).setDecimalFormatSymbols(symb);
/* 182 */       ((DecimalFormat)fmt).setMaximumFractionDigits(d);
/*     */ 
/* 184 */       ((DecimalFormat)fmt).setGroupingUsed(true);
/*     */     }
/* 186 */     String s = fmt.format(x) + "%";
/* 187 */     return s;
/*     */   }
/*     */ 
/*     */   public static boolean isIdentity(Matrix M) {
/* 191 */     for (int i = 0; i < M.getRowDimension(); i++) {
/* 192 */       for (int j = 0; j < M.getColumnDimension(); j++) {
/* 193 */         if (i == j) {
/* 194 */           if (M.get(i, j) != 1.0D) return false;
/*     */         }
/* 196 */         else if (M.get(i, j) != 0.0D) return false;
/*     */       }
/*     */     }
/*     */ 
/* 200 */     return true;
/*     */   }
/*     */ 
/*     */   public static int randInt(int max)
/*     */   {
/* 209 */     return (int)Math.floor(Math.random() * max);
/*     */   }
/*     */ 
/*     */   public static double positiveNormalRand() {
/* 213 */     double rand = 1.0D;
/*     */     do
/* 215 */       rand = Normal.staticNextDouble(1.0D, 0.5D);
/* 216 */     while (rand <= 0.4D);
/* 217 */     return rand;
/*     */   }
/*     */ 
/*     */   public static double exponentialMultiplierRand()
/*     */   {
/* 225 */     return Exponential.staticNextDouble(2.0D) + 0.5D;
/*     */   }
/*     */ 
/*     */   public static double percentagePointChi2(double prob, double v)
/*     */   {
/* 238 */     double e = 5.0E-007D; double aa = 0.6931471805D; double p = prob; double small = 1.0E-006D;
/* 239 */     double a = 0.0D; double q = 0.0D; double p1 = 0.0D; double p2 = 0.0D; double t = 0.0D; double x = 0.0D; double b = 0.0D;
/*     */ 
/* 241 */     if (p < small) return 0.0D;
/* 242 */     if (p > 1.0D - small) return 9999.0D;
/* 243 */     if (v <= 0.0D) return -1.0D;
/*     */ 
/* 245 */     double g = Gamma.logGamma(v / 2.0D);
/* 246 */     double xx = v / 2.0D; double c = xx - 1.0D;
/*     */     double ch;
/* 247 */     if (v < -1.24D * Math.log(p)) {
/* 248 */       double ch = Math.pow(p * xx * Math.exp(g + xx * aa), 1.0D / xx);
/* 249 */       if (ch - e < 0.0D) return ch;
/*     */     }
/* 251 */     else if (v <= 0.32D) {
/* 252 */       double ch = 0.4D; a = Math.log(1.0D - p);
/*     */       do {
/* 254 */         q = ch; p1 = 1.0D + ch * (4.67D + ch); p2 = ch * (6.73D + ch * (6.66D + ch));
/* 255 */         t = -0.5D + (4.67D + 2.0D * ch) / p1 - (6.73D + ch * (13.32D + 3.0D * ch)) / p2;
/* 256 */         ch -= (1.0D - Math.exp(a + g + 0.5D * ch + c * aa) * p2 / p1) / t;
/* 257 */       }while (Math.abs(q / ch - 1.0D) - 0.01D > 0.0D);
/*     */     } else {
/* 259 */       x = Probability.normalInverse(p);
/* 260 */       p1 = 0.222222D / v; ch = v * Math.pow(x * Math.sqrt(p1) + 1.0D - p1, 3.0D);
/* 261 */       if (ch > 2.2D * v + 6.0D) ch = -2.0D * (Math.log(1.0D - p) - c * Math.log(0.5D * ch) + g);
/*     */     }
/*     */     do
/*     */     {
/* 265 */       q = ch; p1 = 0.5D * ch;
/* 266 */       if ((t = Gamma.incompleteGamma(xx, p1)) < 0.0D) return -1.0D;
/* 267 */       p2 = p - t;
/* 268 */       t = p2 * Math.exp(xx * aa + g + p1 - c * Math.log(ch));
/* 269 */       b = t / ch; a = 0.5D * t - b * c;
/*     */ 
/* 271 */       double s1 = (210.0D + a * (140.0D + a * (105.0D + a * (84.0D + a * (70.0D + 60.0D * a))))) / 420.0D;
/* 272 */       double s2 = (420.0D + a * (735.0D + a * (966.0D + a * (1141.0D + 1278.0D * a)))) / 2520.0D;
/* 273 */       double s3 = (210.0D + a * (462.0D + a * (707.0D + 932.0D * a))) / 2520.0D;
/* 274 */       double s4 = (252.0D + a * (672.0D + 1182.0D * a) + c * (294.0D + a * (889.0D + 1740.0D * a))) / 5040.0D;
/* 275 */       double s5 = (84.0D + 264.0D * a + c * (175.0D + 606.0D * a)) / 2520.0D;
/* 276 */       double s6 = (120.0D + c * (346.0D + 127.0D * c)) / 5040.0D;
/* 277 */       ch += t * (1.0D + 0.5D * t * s1 - b * c * (s1 - b * (s2 - b * (s3 - b * (s4 - b * (s5 - b * s6))))));
/* 278 */     }while (Math.abs(q / ch - 1.0D) > e);
/* 279 */     return ch;
/*     */   }
/*     */ 
/*     */   public static long getMaxPhysicalMemory()
/*     */   {
/* 287 */     long max = SystemInfo.getPhysicalMemory() / 1024L / 1024L;
/* 288 */     if ((Integer.parseInt(System.getProperty("sun.arch.data.model")) == 32) && (max > 2048L)) max = 2048L;
/* 289 */     if ((System.getProperty("os.name").equals("Mac OS X")) && (Integer.parseInt(System.getProperty("sun.arch.data.model")) == 64)) {
/*     */       try
/*     */       {
/* 292 */         Process p = Runtime.getRuntime().exec("sysctl hw.memsize");
/* 293 */         BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
/* 294 */         String line = br.readLine();
/* 295 */         line = line.substring(12).trim();
/* 296 */         max = Long.valueOf(line).longValue() / 1024L / 1024L;
/*     */       } catch (Exception e) {
/* 298 */         e.printStackTrace();
/*     */       }
/*     */     }
/* 301 */     return max;
/*     */   }
/*     */ 
/*     */   public static long estimateNecessaryMemory(Parameters p)
/*     */   {
/* 312 */     double mem = 0.0D;
/*     */ 
/* 314 */     double T = 1.0D;
/* 315 */     switch ($SWITCH_TABLE$metapiga$parameters$Parameters$Heuristic()[p.heuristic.ordinal()]) {
/*     */     case 4:
/* 317 */       switch ($SWITCH_TABLE$metapiga$parameters$Parameters$CPSelection()[p.cpSelection.ordinal()]) {
/*     */       case 3:
/*     */       case 4:
/*     */       case 5:
/* 321 */         T = p.cpPopNum * p.cpIndNum + 1;
/* 322 */         break;
/*     */       case 1:
/*     */       case 2:
/* 325 */         T = (p.cpPopNum + (p.cpCoreNum > 1 ? p.cpPopNum : 1)) * p.cpIndNum + 1;
/*     */       }
/*     */ 
/* 328 */       break;
/*     */     case 3:
/* 330 */       switch ($SWITCH_TABLE$metapiga$parameters$Parameters$GASelection()[p.gaSelection.ordinal()]) {
/*     */       case 3:
/*     */       case 4:
/*     */       case 5:
/* 334 */         T = p.gaIndNum + 1;
/* 335 */         break;
/*     */       case 1:
/*     */       case 2:
/* 338 */         T = p.gaIndNum * 2 + 1;
/*     */       }
/*     */ 
/* 341 */       break;
/*     */     case 2:
/* 343 */       T = 3.0D;
/* 344 */       break;
/*     */     case 1:
/* 346 */       T = 3.0D;
/* 347 */       break;
/*     */     case 5:
/* 349 */       T = 1.0D;
/*     */     }
/*     */ 
/* 353 */     double N = 2 * p.dataset.getNTax() - 1;
/*     */ 
/* 355 */     double D = p.dataset.getCompressedNChar();
/*     */ 
/* 357 */     double C = p.evaluationDistribution != Parameters.EvaluationDistribution.NONE ? p.evaluationDistributionSubsets : 1;
/*     */ 
/* 359 */     double S = p.dataset.getDataType().numOfStates();
/*     */ 
/* 361 */     double Prr = p.replicatesParallel;
/*     */ 
/* 363 */     double precision = 4.0D;
/*     */ 
/* 365 */     double state = S * 2.0D / 8.0D;
/*     */ 
/* 368 */     mem += T;
/* 369 */     mem /= 1024.0D;
/* 370 */     mem /= 1024.0D;
/* 371 */     mem *= N;
/* 372 */     mem *= D;
/* 373 */     mem *= C;
/* 374 */     mem *= S;
/* 375 */     mem *= precision;
/* 376 */     mem *= Prr;
/*     */ 
/* 378 */     mem += p.dataset.getNTax() * (D + 1.0D) * state / 1024.0D / 1024.0D;
/*     */ 
/* 380 */     mem *= 1.3D;
/* 381 */     return ()mem;
/*     */   }
/*     */ 
/*     */   public static void openURL(String url) {
/* 385 */     String osName = System.getProperty("os.name");
/*     */     try {
/* 387 */       if (osName.startsWith("Mac OS")) {
/* 388 */         Class fileMgr = Class.forName("com.apple.eio.FileManager");
/* 389 */         Method openURL = fileMgr.getDeclaredMethod("openURL", 
/* 390 */           new Class[] { String.class });
/* 391 */         openURL.invoke(null, new Object[] { url });
/*     */       }
/* 393 */       else if (osName.startsWith("Windows")) {
/* 394 */         Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
/*     */       } else {
/* 396 */         String[] browsers = { 
/* 397 */           "firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape" };
/* 398 */         String browser = null;
/* 399 */         for (int count = 0; (count < browsers.length) && (browser == null); count++)
/* 400 */           if (Runtime.getRuntime().exec(
/* 401 */             new String[] { "which", browsers[count] }).waitFor() == 0)
/* 402 */             browser = browsers[count];
/* 403 */         if (browser == null) {
/* 404 */           throw new Exception("Could not find supported web browser");
/*     */         }
/* 406 */         Runtime.getRuntime().exec(new String[] { browser, url });
/*     */       }
/*     */     }
/*     */     catch (Exception e) {
/* 410 */       ShowErrorMessage(null, "Cannot open web browser:\n" + e.getLocalizedMessage(), "Opening web browser");
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void compressSinglefile(String file, String target)
/*     */     throws IOException
/*     */   {
/* 422 */     compressSinglefile(new File(file), new File(target), 9);
/*     */   }
/*     */ 
/*     */   public static void decompress(File file, File folder, boolean deleteZipAfter)
/*     */     throws IOException
/*     */   {
/* 433 */     ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(file.getCanonicalFile())));
/*     */     ZipEntry ze;
/*     */     try
/*     */     {
/*     */       ZipEntry ze;
/* 436 */       while ((ze = zis.getNextEntry()) != null)
/*     */       {
/*     */         ZipEntry ze;
/* 437 */         File f = new File(folder.getCanonicalPath(), ze.getName());
/* 438 */         if (f.exists()) {
/* 439 */           f.delete();
/*     */         }
/* 441 */         if (ze.isDirectory()) {
/* 442 */           f.mkdirs();
/*     */         }
/*     */         else {
/* 445 */           f.getParentFile().mkdirs();
/* 446 */           OutputStream fos = new BufferedOutputStream(new FileOutputStream(f));
/*     */           try {
/*     */             try {
/* 449 */               byte[] buf = new byte[8192];
/*     */               int bytesRead;
/* 451 */               while (-1 != (bytesRead = zis.read(buf)))
/*     */               {
/*     */                 int bytesRead;
/* 452 */                 fos.write(buf, 0, bytesRead);
/*     */               }
/*     */             } finally {
/* 455 */               fos.close();
/*     */             }
/*     */           } catch (IOException ioe) {
/* 458 */             f.delete();
/* 459 */             throw ioe;
/*     */           }
/*     */         }
/*     */       }
/*     */     } finally { zis.close(); }
/*     */ 
/* 465 */     if (deleteZipAfter)
/* 466 */       file.delete();
/*     */   }
/*     */ 
/*     */   private static void compressSinglefile(File file, File target, int compressionLevel) throws IOException
/*     */   {
/* 471 */     File source = file.getCanonicalFile();
/* 472 */     ZipOutputStream out = new ZipOutputStream(new FileOutputStream(getZipTypeFile(source, target.getCanonicalFile())));
/* 473 */     out.setMethod(8);
/* 474 */     out.setLevel(compressionLevel);
/* 475 */     compressFile(out, "", file);
/* 476 */     out.close();
/*     */   }
/*     */ 
/*     */   private static final void compressFile(ZipOutputStream out, String parentFolder, File file) throws IOException {
/* 480 */     String zipName = parentFolder + file.getName() + (file.isDirectory() ? Character.valueOf('/') : "");
/* 481 */     ZipEntry entry = new ZipEntry(zipName);
/* 482 */     entry.setSize(file.length());
/* 483 */     entry.setTime(file.lastModified());
/* 484 */     out.putNextEntry(entry);
/* 485 */     if (file.isDirectory()) {
/* 486 */       for (File f : file.listFiles()) {
/* 487 */         compressFile(out, zipName.toString(), f);
/*     */       }
/* 489 */       return;
/*     */     }
/* 491 */     InputStream in = new BufferedInputStream(new FileInputStream(file));
/*     */     try {
/* 493 */       byte[] buf = new byte[8192];
/*     */       int bytesRead;
/* 495 */       while (-1 != (bytesRead = in.read(buf)))
/*     */       {
/*     */         int bytesRead;
/* 496 */         out.write(buf, 0, bytesRead);
/*     */       }
/*     */     } finally {
/* 499 */       in.close();
/*     */     }
/*     */   }
/*     */ 
/*     */   private static File getZipTypeFile(File source, File target) throws IOException {
/* 504 */     if (target.getName().toLowerCase().endsWith(".zip")) {
/* 505 */       return target;
/*     */     }
/* 507 */     String tName = target.isDirectory() ? source.getName() : target.getName();
/* 508 */     int index = tName.lastIndexOf('.');
/* 509 */     return new File((target.isDirectory() ? target.getCanonicalPath() : target.getParentFile().getCanonicalPath()) + File.separatorChar + (index < 0 ? tName : tName.substring(0, index)) + ".zip");
/*     */   }
/*     */ 
/*     */   public static File getHomeDirectory() {
/*     */     try {
/* 514 */       return new JFileChooser().getFileSystemView().getDefaultDirectory();
/*     */     } catch (Error err) {
/* 516 */       return new File(System.getProperty("user.home")); } catch (Exception e) {
/*     */     }
/* 518 */     return new File(System.getProperty("user.home"));
/*     */   }
/*     */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.utilities.Tools
 * JD-Core Version:    0.6.2
 */