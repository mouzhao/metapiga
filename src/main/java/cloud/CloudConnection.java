/*     */ package cloud;
/*     */ 
/*     */ import com.microsoft.windowsazure.services.blob.client.CloudBlob;
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
/*     */ import org.hepia.venusc.AzureJavaAPI;
/*     */ import org.hepia.venusc.VMInstanceSize;
/*     */ import org.hepia.venusc.VMStatus;
/*     */ import org.hepia.venusc.VenusCloudClient;
/*     */ 
/*     */ public class CloudConnection
/*     */ {
/*     */   private CloudMonitor.CloudMonitorType parentType;
/*     */   private Object parent;
/*     */   private String commandLine;
/*     */   private VenusCloudClient client;
/*     */   private CloudClient cloudClient;
/*     */   private String appid;
/*     */   private int xms;
/*     */   private int xmx;
/*  30 */   private final String output = "results.nex";
/*     */   private VMStatus deploymentStatus;
/*     */   private final int VMnumber;
/*  33 */   private final int numberOfMaxVMs = 50;
/*     */ 
/*  36 */   private final String storageConnectionString = "DefaultEndpointsProtocol=http;AccountName=haithemstoragecloud;AccountKey=upXsDTYGbfDrrj10rRrrebQyHLIdMWi8H0uH0ZSy6WRUVqb0jZYWStuX8O7xKq87oynXWyLOTCGdhRLy22erVg==";
/*     */ 
/*  39 */   private String DeploymentName = "MetaPIGASubmission";
/*  40 */   private String HostedService = "NewVersion";
/*  41 */   private String DataContainerName = "testcontainer";
/*  42 */   private String AppContainerName = "applicationcontainer";
/*  43 */   private String ApplicationName = "MetapigaApplication";
/*  44 */   private String STSUserName = "Researcher";
/*  45 */   private String STSPasswd = "secret";
/*  46 */   private String STSCertCN = "HaithemCert";
/*  47 */   private String STSCertThumbprint = "08014A57CDFC8FC5861862361AA16F20E56CB405";
/*  48 */   private String IISserverURL = "http://129.194.184.224:8081/Service.asmx";
/*     */ 
/*     */   public CloudConnection(Parameters parameters, String directory) throws Exception {
/*  51 */     if (parameters.replicatesNumber > 50)
/*  52 */       this.VMnumber = 50;
/*     */     else {
/*  54 */       this.VMnumber = parameters.replicatesNumber;
/*     */     }
/*  56 */     initializeCloud(parameters, directory);
/*     */   }
/*     */ 
/*     */   public CloudConnection(SearchOnceGraphical parent, Parameters parameters, String directory) throws Exception {
/*  60 */     if (parameters.replicatesNumber > 50)
/*  61 */       this.VMnumber = 50;
/*     */     else {
/*  63 */       this.VMnumber = parameters.replicatesNumber;
/*     */     }
/*  65 */     this.parentType = CloudMonitor.CloudMonitorType.SINGLE_SEARCH_GRAPHICAL;
/*  66 */     this.parent = parent;
/*  67 */     initializeCloud(parameters, directory);
/*     */   }
/*     */ 
/*     */   public CloudConnection(SearchBatchGraphical parent, Parameters parameters, String directory) throws Exception {
/*  71 */     if (parameters.replicatesNumber > 50)
/*  72 */       this.VMnumber = 50;
/*     */     else {
/*  74 */       this.VMnumber = parameters.replicatesNumber;
/*     */     }
/*  76 */     this.parentType = CloudMonitor.CloudMonitorType.BATCH_SEARCH_GRAPHICAL;
/*  77 */     this.parent = parent;
/*  78 */     initializeCloud(parameters, directory);
/*     */   }
/*     */ 
/*     */   public CloudConnection(SearchConsole parent, Parameters parameters, String directory) throws Exception {
/*  82 */     if (parameters.replicatesNumber > 50)
/*  83 */       this.VMnumber = 50;
/*     */     else {
/*  85 */       this.VMnumber = parameters.replicatesNumber;
/*     */     }
/*  87 */     this.parentType = CloudMonitor.CloudMonitorType.CONSOLE;
/*  88 */     this.parent = parent;
/*  89 */     initializeCloud(parameters, directory);
/*     */   }
/*     */ 
/*     */   public CloudConnection(SearchSilent parent, Parameters parameters, String directory) throws Exception {
/*  93 */     if (parameters.replicatesNumber > 50)
/*  94 */       this.VMnumber = 50;
/*     */     else {
/*  96 */       this.VMnumber = parameters.replicatesNumber;
/*     */     }
/*  98 */     this.parentType = CloudMonitor.CloudMonitorType.SILENT;
/*  99 */     this.parent = parent;
/* 100 */     initializeCloud(parameters, directory);
/*     */   }
/*     */ 
/*     */   private void initializeCloud(Parameters parameters, String directory) throws Exception {
/* 104 */     showText("Preparing connection with the Venus Cloud");
/* 105 */     this.client = new VenusCloudClient(this.IISserverURL, 
/* 106 */       "DefaultEndpointsProtocol=http;AccountName=haithemstoragecloud;AccountKey=upXsDTYGbfDrrj10rRrrebQyHLIdMWi8H0uH0ZSy6WRUVqb0jZYWStuX8O7xKq87oynXWyLOTCGdhRLy22erVg==", this.DeploymentName, this.HostedService, 
/* 107 */       this.DataContainerName, this.AppContainerName, this.STSUserName, this.STSPasswd, 
/* 108 */       this.STSCertCN, this.STSCertThumbprint, true);
/*     */ 
/* 110 */     showText("Preparing input data");
/* 111 */     String fileName = "/cloudinput.nex";
/* 112 */     String inputDataFilePath = prepareInputData(parameters, directory, fileName);
/*     */ 
/* 114 */     VMInstanceSize VMsize = VMInstanceSize.SMALL;
/* 115 */     if (this.xmx > 1750)
/* 116 */       VMsize = VMInstanceSize.MEDIUM;
/* 117 */     else if (this.xmx > 3500)
/* 118 */       VMsize = VMInstanceSize.LARGE;
/* 119 */     else if (this.xmx > 7000) {
/* 120 */       VMsize = VMInstanceSize.XLARGE;
/*     */     }
/* 122 */     this.deploymentStatus = this.client.createVirtualMachines(this.VMnumber, VMsize);
/* 123 */     if (!this.deploymentStatus.equals(VMStatus.READY)) {
/* 124 */       showText("Cannot create cloud virtual machines!");
/*     */     }
/*     */ 
/* 129 */     if (!this.client.createMetaPigaApplication(this.ApplicationName)) {
/* 130 */       throw new Exception("Cannot create Metapiga application on the Venus Cloud");
/*     */     }
/*     */ 
/* 133 */     showText("Application ready on the cloud");
/*     */ 
/* 135 */     uploadInputFileToCloud(inputDataFilePath, fileName);
/*     */ 
/* 137 */     this.cloudClient = new CloudClient(this.client);
/* 138 */     showText("Cloud ready!");
/*     */   }
/*     */ 
/*     */   private void uploadInputFileToCloud(String inputFile, String inputFileName)
/*     */   {
/* 144 */     CloudBlob blob = AzureJavaAPI.UploadFile(inputFileName, inputFile, this.DataContainerName, "DefaultEndpointsProtocol=http;AccountName=haithemstoragecloud;AccountKey=upXsDTYGbfDrrj10rRrrebQyHLIdMWi8H0uH0ZSy6WRUVqb0jZYWStuX8O7xKq87oynXWyLOTCGdhRLy22erVg==");
/* 145 */     showText("Input data file successfully uploaded");
/*     */   }
/*     */ 
/*     */   private String prepareInputData(Parameters p, String directory, String fileName) throws Exception {
/* 149 */     p.cloudOutput = "results.nex";
/* 150 */     Parameters cloudParam = p.duplicateButShareDataset();
/* 151 */     cloudParam.logFiles.clear();
/* 152 */     cloudParam.replicatesNumber = 1;
/* 153 */     cloudParam.replicatesParallel = 1;
/* 154 */     cloudParam.cpCoreNum = 1;
/* 155 */     cloudParam.replicatesStopCondition = Parameters.ReplicatesStopCondition.NONE;
/* 156 */     cloudParam.outputDir = "/MetaPIGA CLOUD results";
/* 157 */     cloudParam.useCloud = false;
/* 158 */     this.xms = 128;
/* 159 */     int estMem = (int)Tools.estimateNecessaryMemory(cloudParam) + 100;
/* 160 */     this.xmx = (64 * (estMem / 64 + 1));
/* 161 */     String inputNex = directory + fileName;
/* 162 */     NexusWriter nw = new NexusWriter(inputNex, cloudParam);
/* 163 */     nw.execute();
/* 164 */     WaitingLogo.Status status = (WaitingLogo.Status)nw.get();
/* 165 */     if (status == WaitingLogo.Status.NEXUS_FILE_SAVED)
/*     */     {
/* 169 */       return inputNex;
/*     */     }
/* 171 */     throw new Exception("Cannot create an input file (" + directory + "/cloudinput.nex" + ") and zip it (" + directory + "/input.zip" + ") to be sent on the GRID !");
/*     */   }
/*     */ 
/*     */   private void showText(String text)
/*     */   {
/* 176 */     switch ($SWITCH_TABLE$metapiga$cloud$CloudMonitor$CloudMonitorType()[this.parentType.ordinal()]) {
/*     */     case 1:
/* 178 */       ((SearchOnceGraphical)this.parent).showText(text);
/* 179 */       break;
/*     */     case 2:
/* 181 */       ((SearchBatchGraphical)this.parent).showText(text);
/* 182 */       break;
/*     */     case 3:
/* 184 */       ((SearchConsole)this.parent).showText(text);
/* 185 */       break;
/*     */     case 4:
/* 187 */       break;
/*     */     default:
/* 189 */       System.out.println(text);
/*     */     }
/*     */   }
/*     */ 
/*     */   public boolean endApplication()
/*     */   {
/* 195 */     this.cloudClient.close();
/* 196 */     this.cloudClient.deleteAllTerminatedJobs();
/*     */ 
/* 198 */     return true;
/*     */   }
/*     */ 
/*     */   public CloudClient getClient() {
/* 202 */     return this.cloudClient;
/*     */   }
/*     */ 
/*     */   public String getApplicationId() {
/* 206 */     return this.appid;
/*     */   }
/*     */ 
/*     */   public String getCommandLine() {
/* 210 */     return this.commandLine;
/*     */   }
/*     */ 
/*     */   public int getXmsValue() {
/* 214 */     return this.xms;
/*     */   }
/*     */ 
/*     */   public int getXmxValue() {
/* 218 */     return this.xmx;
/*     */   }
/*     */ 
/*     */   public String getOutputName() {
/* 222 */     return "results.nex";
/*     */   }
/*     */ 
/*     */   public String getStorageConnectionString() {
/* 226 */     return "DefaultEndpointsProtocol=http;AccountName=haithemstoragecloud;AccountKey=upXsDTYGbfDrrj10rRrrebQyHLIdMWi8H0uH0ZSy6WRUVqb0jZYWStuX8O7xKq87oynXWyLOTCGdhRLy22erVg==";
/*     */   }
/*     */ 
/*     */   public String getDataContainerName() {
/* 230 */     return this.DataContainerName;
/*     */   }
/*     */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.cloud.CloudConnection
 * JD-Core Version:    0.6.2
 */