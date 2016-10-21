/*     */ package grid;
/*     */ 
/*     */ import java.io.PrintStream;
/*     */ import java.util.Set;
/*     */ import metapiga.WaitingLogo.Status;
/*     */ import metapiga.io.NexusWriter;
/*     */ import metapiga.monitors.SearchBatchGraphical;
/*     */ import metapiga.monitors.SearchConsole;
/*     */ import metapiga.monitors.SearchOnceGraphical;
/*     */ import metapiga.monitors.SearchSilent;
/*     */ import metapiga.parameters.Parameters;
/*     */ import metapiga.parameters.Parameters.ReplicatesStopCondition;
/*     */ import metapiga.utilities.Tools;
/*     */ import xwchclientapi.XWCHClient;
/*     */ import xwchclientapi.XWCHClient.fileref;
/*     */ 
/*     */ public class GridConnection
/*     */ {
/*     */   private GridMonitor.GridMonitorType parentType;
/*     */   private Object parent;
/*     */   private String commandLine;
/*     */   private XWCHClient client;
/*     */   private GridClient gridClient;
/*     */   private String appid;
/*     */   private XWCHClient.fileref input_ref;
/*     */   private int xms;
/*     */   private int xmx;
/*  33 */   private final String output = "results.nex";
/*     */ 
/*     */   public GridConnection(Parameters parameters, String directory) throws Exception {
/*  36 */     initializeGrid(parameters, directory);
/*     */   }
/*     */ 
/*     */   public GridConnection(SearchOnceGraphical parent, Parameters parameters, String directory) throws Exception {
/*  40 */     this.parentType = GridMonitor.GridMonitorType.SINGLE_SEARCH_GRAPHICAL;
/*  41 */     this.parent = parent;
/*  42 */     initializeGrid(parameters, directory);
/*     */   }
/*     */ 
/*     */   public GridConnection(SearchBatchGraphical parent, Parameters parameters, String directory) throws Exception {
/*  46 */     this.parentType = GridMonitor.GridMonitorType.BATCH_SEARCH_GRAPHICAL;
/*  47 */     this.parent = parent;
/*  48 */     initializeGrid(parameters, directory);
/*     */   }
/*     */ 
/*     */   public GridConnection(SearchConsole parent, Parameters parameters, String directory) throws Exception {
/*  52 */     this.parentType = GridMonitor.GridMonitorType.CONSOLE;
/*  53 */     this.parent = parent;
/*  54 */     initializeGrid(parameters, directory);
/*     */   }
/*     */ 
/*     */   public GridConnection(SearchSilent parent, Parameters parameters, String directory) throws Exception {
/*  58 */     this.parentType = GridMonitor.GridMonitorType.SILENT;
/*  59 */     this.parent = parent;
/*  60 */     initializeGrid(parameters, directory);
/*     */   }
/*     */ 
/*     */   private void initializeGrid(Parameters p, String directory) throws Exception {
/*  64 */     showText("Connecting with the GRID coordinator ...");
/*  65 */     this.client = new XWCHClient(p.gridServer, directory, p.gridClient);
/*  66 */     if (!this.client.Init()) {
/*  67 */       throw new Exception("Cannot initialize XtremWeb-CH client");
/*     */     }
/*  69 */     this.gridClient = new GridClient(this.client);
/*  70 */     showText("GRID warehouse is reachable: " + this.client.PingWarehouse());
/*     */ 
/*  72 */     this.appid = this.client.AddApplication("MetaPIGA");
/*  73 */     if (this.appid == null) {
/*  74 */       throw new Exception("Cannot add an application 'MetaPIGA', provided client id (" + p.gridClient + ") may not be available.");
/*     */     }
/*  76 */     showText("Application id retreived for MetaPIGA : " + this.appid);
/*  77 */     String input = prepareInputData(p, directory);
/*     */ 
/*  79 */     showText("Sending input data on the GRID ...");
/*  80 */     this.input_ref = this.client.AddData(input);
/*  81 */     if (this.input_ref == null) {
/*  82 */       throw new Exception("Error when sending input file on the GRID !");
/*     */     }
/*  84 */     showText("GRID is ready !");
/*     */   }
/*     */ 
/*     */   private String prepareInputData(Parameters p, String directory) throws Exception {
/*  88 */     p.cloudOutput = "results.nex";
/*  89 */     Parameters gridParam = p.duplicateButShareDataset();
/*  90 */     gridParam.logFiles.clear();
/*  91 */     gridParam.replicatesNumber = 1;
/*  92 */     gridParam.replicatesParallel = 1;
/*  93 */     gridParam.cpCoreNum = 1;
/*  94 */     gridParam.replicatesStopCondition = Parameters.ReplicatesStopCondition.NONE;
/*  95 */     gridParam.outputDir = "/MetaPIGA GRID results";
/*  96 */     gridParam.useGrid = false;
/*  97 */     gridParam.gridReplicate = true;
/*  98 */     this.xms = 128;
/*  99 */     int estMem = (int)Tools.estimateNecessaryMemory(gridParam) + 100;
/* 100 */     this.xmx = (64 * (estMem / 64 + 1));
/* 101 */     NexusWriter nw = new NexusWriter(directory + "/gridinput.nex", gridParam);
/* 102 */     nw.execute();
/* 103 */     WaitingLogo.Status status = (WaitingLogo.Status)nw.get();
/* 104 */     if (status == WaitingLogo.Status.NEXUS_FILE_SAVED) {
/* 105 */       Tools.compressSinglefile(directory + "/gridinput.nex", directory + "/input.zip");
/* 106 */       String input = "input.zip";
/* 107 */       this.commandLine = ("metapiga.bat " + this.xms + " " + this.xmx + " gridinput.nex");
/* 108 */       return input;
/*     */     }
/* 110 */     throw new Exception("Cannot create an input file (" + directory + "/gridinput.nex" + ") and zip it (" + directory + "/input.zip" + ") to be sent on the GRID !");
/*     */   }
/*     */ 
/*     */   public boolean endApplication()
/*     */   {
/* 115 */     this.gridClient.close();
/* 116 */     return this.client.EndApplication(this.appid);
/*     */   }
/*     */ 
/*     */   private void showText(String text) {
/* 120 */     switch ($SWITCH_TABLE$metapiga$grid$GridMonitor$GridMonitorType()[this.parentType.ordinal()]) {
/*     */     case 1:
/* 122 */       ((SearchOnceGraphical)this.parent).showText(text);
/* 123 */       break;
/*     */     case 2:
/* 125 */       ((SearchBatchGraphical)this.parent).showText(text);
/* 126 */       break;
/*     */     case 3:
/* 128 */       ((SearchConsole)this.parent).showText(text);
/* 129 */       break;
/*     */     case 4:
/* 131 */       break;
/*     */     default:
/* 133 */       System.out.println(text);
/*     */     }
/*     */   }
/*     */ 
/*     */   public GridClient getClient()
/*     */   {
/* 139 */     return this.gridClient;
/*     */   }
/*     */ 
/*     */   public String getApplicationId() {
/* 143 */     return this.appid;
/*     */   }
/*     */ 
/*     */   public String getCommandLine() {
/* 147 */     return this.commandLine;
/*     */   }
/*     */ 
/*     */   public XWCHClient.fileref getInputReference() {
/* 151 */     return this.input_ref;
/*     */   }
/*     */ 
/*     */   public int getXmsValue() {
/* 155 */     return this.xms;
/*     */   }
/*     */ 
/*     */   public int getXmxValue() {
/* 159 */     return this.xmx;
/*     */   }
/*     */ 
/*     */   public String getOutputName() {
/* 163 */     return "results.nex";
/*     */   }
/*     */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.grid.GridConnection
 * JD-Core Version:    0.6.2
 */