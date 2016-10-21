package metapiga;/*     */
/*     */ 
/*     */ import com.install4j.api.Util;
/*     */ import com.install4j.api.launcher.ApplicationLauncher;
/*     */ import com.install4j.api.launcher.StartupNotification;
/*     */ import com.install4j.api.launcher.StartupNotification.Listener;
/*     */ import com.install4j.api.windows.SpecialFolder;
/*     */ import com.install4j.api.windows.WinFileSystem;
/*     */ import java.awt.Dimension;
/*     */ import java.awt.Toolkit;
/*     */ import java.io.BufferedReader;
/*     */ import java.io.File;
/*     */ import java.io.FileReader;
/*     */
/*     */ import java.util.ArrayList;
/*     */ import java.util.List;
/*     */ import java.util.Locale;
/*     */ import javax.swing.DefaultListModel;
/*     */
/*     */ import javax.swing.JFrame;
/*     */ import javax.swing.JProgressBar;
/*     */ import javax.swing.SwingUtilities;
/*     */ import javax.swing.UIManager;
/*     */ import metapiga.io.FastaReader;
/*     */ import metapiga.io.NexusReader;
/*     */ import metapiga.monitors.SearchConsole;
/*     */ import metapiga.monitors.SearchSilent;
/*     */
/*     */ import metapiga.trees.TreeViewer;
/*     */ import metapiga.utilities.Tools;
/*     */ 
/*     */ public class MetaPIGA
/*     */ {
/*     */   public static final String version = "3.1";
/*     */   public static ProgressHandling progressHandling;
/*  47 */   public DefaultListModel parameters = new DefaultListModel();
/*     */   public static UI ui;
/*  49 */   public boolean busy = false;
/*     */   public MainFrame mainFrame;
/*  52 */   public static TreeViewer treeViewer = null;
/*  53 */   boolean packFrame = false;
/*     */ 
/*     */   public MetaPIGA(UI ui, List<File> dataFiles, int consoleWidth)
/*     */   {
/*  57 */     ui = ui;
/*  58 */     Locale.setDefault(new Locale("en", "US"));
/*  59 */     ProgressHandling.consoleWidth = consoleWidth - 1;
/*  60 */     if (ui == UI.GRAPHICAL) {
/*  61 */       this.mainFrame = new MainFrame(this);
/*  62 */       SwingUtilities.invokeLater(new Runnable()
/*     */       {
/*     */         public void run()
/*     */         {
/*  66 */           if (MetaPIGA.this.packFrame) {
/*  67 */             MetaPIGA.this.mainFrame.pack();
/*     */           }
/*     */           else {
/*  70 */             MetaPIGA.this.mainFrame.validate();
/*     */           }
/*     */ 
/*  73 */           Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
/*  74 */           Dimension frameSize = MetaPIGA.this.mainFrame.getSize();
/*  75 */           if (frameSize.height > screenSize.height) {
/*  76 */             frameSize.height = screenSize.height;
/*     */           }
/*  78 */           if (frameSize.width > screenSize.width) {
/*  79 */             frameSize.width = screenSize.width;
/*     */           }
/*  81 */           MetaPIGA.this.mainFrame.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
/*  82 */           MetaPIGA.this.mainFrame.setExtendedState(6);
/*  83 */           MetaPIGA.this.mainFrame.setVisible(true);
/*  84 */           StartupNotification.registerStartupListener(new MetaPIGA.FileAssociationListener(MetaPIGA.this));
/*  85 */           MetaPIGA.treeViewer = new TreeViewer();
/*  86 */           MetaPIGA.treeViewer.setSize(new Dimension((int)(screenSize.width * 0.8D), (int)(screenSize.height * 0.8D)));
/*  87 */           MetaPIGA.this.showUpdateDialog();
/*     */         } } );
/*     */     }
/*  90 */     else if (ui == UI.CONSOLE) {
/*  91 */       progressHandling = new ProgressHandling(new JProgressBar());
/*  92 */       progressHandling.setUI(UI.CONSOLE);
/*     */     } else {
/*  94 */       progressHandling = new ProgressHandling(new JProgressBar());
/*  95 */       progressHandling.setUI(UI.SILENT);
/*     */     }
/*  97 */     for (File dataFile : dataFiles) {
/*     */       try {
/*  99 */         if (ui == UI.CONSOLE) System.out.println("Found " + dataFile);
/* 100 */         loadDataFile(dataFile);
/*     */         do
/* 102 */           Thread.sleep(500L);
/* 101 */         while (
/* 103 */           this.busy);
/*     */       } catch (Exception e) {
/* 105 */         e.printStackTrace();
/*     */       }
/*     */     }
/* 108 */     if ((ui != UI.GRAPHICAL) && (!this.parameters.isEmpty()))
/*     */     {
/* 111 */       switch (ui) {
/*     */       case GRAPHICAL:
/* 113 */         Runnable search = new SearchConsole(this.parameters);
/* 114 */         Thread thread = new Thread(search, "ConsoleUI-Search");
/* 115 */         thread.start();
/*     */         try {
/* 117 */           thread.join();
/*     */         } catch (Exception e) {
/* 119 */           e.printStackTrace();
/*     */         }
/* 121 */         System.exit(0);
/* 122 */         break;
/*     */       case SILENT:
/* 124 */         Runnable search = new SearchSilent(this.parameters);
/* 125 */         Thread thread = new Thread(search, "Silent-Search");
/* 126 */         thread.start();
/*     */         try {
/* 128 */           thread.join();
/*     */         } catch (Exception e) {
/* 130 */           e.printStackTrace();
/*     */         }
/* 132 */         System.exit(0);
/* 133 */         break;
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public void loadDataFile(File dataFile)
/*     */   {
/* 141 */     this.busy = true;
/* 142 */     if (ui != UI.GRAPHICAL) {
/* 143 */       Parameters.FileFormat format = Parameters.FileFormat.NEXUS;
/*     */       try {
/* 145 */         FileReader fr = new FileReader(dataFile);
/* 146 */         BufferedReader br = new BufferedReader(fr);
/*     */         String line;
/* 148 */         while ((line = br.readLine()) != null)
/*     */         {
/*     */           String line;
/* 149 */           if ((line.length() > 0) && 
/* 150 */             (line.startsWith(">"))) {
/* 151 */             format = Parameters.FileFormat.FASTA;
/* 152 */             break;
/*     */           }
/*     */         }
/*     */ 
/* 156 */         br.close();
/* 157 */         fr.close();
/*     */       } catch (Exception e) {
/* 159 */         e.printStackTrace();
/*     */       }
/* 161 */       switch (format) {
/*     */       case NEXUS:
/* 163 */         FastaReader fastaReader = new FastaReader(dataFile, this);
/* 164 */         fastaReader.execute();
/* 165 */         break;
/*     */       case FASTA:
/*     */       default:
/* 168 */         NexusReader nexusReader = new NexusReader(dataFile, this);
/* 169 */         nexusReader.execute();
/* 170 */         break;
/*     */       }
/*     */     } else {
/* 173 */       System.out.println("Opening new file : " + dataFile);
/* 174 */       this.mainFrame.loadDataFile(dataFile);
/*     */     }
/*     */   }
/*     */ 
/*     */   private void showUpdateDialog()
/*     */   {
/* 188 */     String appData = System.getProperty("user.home");
/* 189 */     if (Util.isWindows())
/* 190 */       appData = WinFileSystem.getSpecialFolder(SpecialFolder.APPDATA, false) + "\\MetaPIGA\\";
/* 191 */     else if (Util.isMacOS())
/* 192 */       appData = "";
/*     */     else {
/* 194 */       appData = System.getProperty("user.home") + ".MetaPIGA/";
/*     */     }
/* 196 */     boolean showMessage = true;
/*     */     try {
/* 198 */       File file = new File(appData + "updateRead");
/* 199 */       if (file.exists()) {
/* 200 */         FileReader fr = new FileReader(file);
/* 201 */         BufferedReader br = new BufferedReader(fr);
/*     */         String line;
/* 203 */         while ((line = br.readLine()) != null)
/*     */         {
/* 204 */           String line;
/* 204 */           if (line.equals("3.1")) showMessage = false;
/*     */         }
/* 206 */         br.close();
/* 207 */         fr.close();
/*     */       }
/*     */     } catch (Exception ex) {
/* 210 */       ex.printStackTrace();
/*     */     }
/* 212 */     if (showMessage) {
/* 213 */       JFrame updateFrame = new JFrame();
/* 214 */       updateFrame.setIconImage(Tools.getScaledIcon(MainFrame.imageUpdater, 32).getImage());
/* 215 */       UpdateDialog dlg = new UpdateDialog(updateFrame);
/* 216 */       Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
/* 217 */       Dimension windowSize = dlg.getSize();
/* 218 */       dlg.setLocation(Math.max(0, (screenSize.width - windowSize.width) / 2), 
/* 219 */         Math.max(0, (screenSize.height - windowSize.height) / 2));
/* 220 */       dlg.setModal(false);
/* 221 */       dlg.setVisible(true);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void main(String[] args)
/*     */   {
/*     */     try
/*     */     {
/* 237 */       List fileArgs = new ArrayList();
/* 238 */       boolean updateCheck = true;
/* 239 */       UI ui = UI.GRAPHICAL;
/* 240 */       int consoleWidth = 80;
/* 241 */       String[] arrayOfString = args; int j = args.length; for (int i = 0; i < j; i++) { String arg = arrayOfString[i];
/* 242 */         if (arg.equals("noupdate")) updateCheck = false;
/* 243 */         else if ((arg.equals("nogui")) && (ui != UI.SILENT)) ui = UI.CONSOLE;
/* 244 */         else if (arg.equals("silent")) ui = UI.SILENT;
/* 245 */         else if (arg.startsWith("width=")) consoleWidth = Integer.parseInt(arg.substring(6)); else
/* 246 */           fileArgs.add(arg);
/*     */       }
/* 248 */       if (updateCheck) ApplicationLauncher.launchApplication("170", null, true, null);
/* 249 */       if (Util.isLinux())
/* 250 */         UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
/*     */       else {
/* 252 */         UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
/*     */       }
/* 254 */       List nexusFiles = new ArrayList();
/* 255 */       for (String filename : fileArgs) {
/* 256 */         nexusFiles.add(new File(filename));
/*     */       }
/* 258 */       new MetaPIGA(ui, nexusFiles, consoleWidth);
/*     */     }
/*     */     catch (Exception e) {
/* 261 */       e.printStackTrace();
/*     */     }
/*     */   }
/*     */ 
/*     */   public class FileAssociationListener
/*     */     implements StartupNotification.Listener
/*     */   {
/*     */     public FileAssociationListener()
/*     */     {
/*     */     }
/*     */ 
/*     */     public void startupPerformed(String args)
/*     */     {
/* 181 */       args = args.replace("\"", "");
/* 182 */       System.out.println("Opening new file : " + args);
/* 183 */       MetaPIGA.this.mainFrame.loadDataFile(new File(args));
/*     */     }
/*     */   }
/*     */ 
/*     */   public static enum UI
/*     */   {
/*  44 */     GRAPHICAL, CONSOLE, SILENT;
/*     */   }
/*     */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.MetaPIGA
 * JD-Core Version:    0.6.2
 */