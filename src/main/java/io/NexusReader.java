/*     */ package io;
/*     */ 
/*     */ import java.io.BufferedReader;
/*     */ import java.io.File;
/*     */ import java.io.FileReader;
/*     */ import java.io.InputStream;
/*     */ import java.io.InputStreamReader;
/*     */ import java.io.PrintStream;
/*     */ import java.util.HashMap;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.concurrent.ExecutionException;
/*     */ import javax.swing.DefaultListModel;
/*     */ import javax.swing.SwingWorker;
/*     */ import metapiga.MainFrame;
/*     */ import metapiga.MetaPIGA;
/*     */ import metapiga.MetaPIGA.UI;
/*     */ import metapiga.WaitingLogo.Status;
/*     */ import metapiga.exceptions.CharsetIntersectionException;
/*     */ import metapiga.exceptions.IncompatibleDataException;
/*     */ import metapiga.exceptions.NexusInconsistencyException;
/*     */ import metapiga.exceptions.UnknownDataException;
/*     */ import metapiga.modelization.data.DataType;
/*     */ import metapiga.parameters.Parameters;
/*     */ import metapiga.parameters.Parameters.CodonTransitionTableType;
/*     */ import metapiga.parameters.Parameters.StartingTreeGeneration;
/*     */ import metapiga.trees.Tree;
/*     */ import metapiga.utilities.Tools;
/*     */ import org.biojavax.bio.phylo.io.nexus.BatchBlock;
/*     */ import org.biojavax.bio.phylo.io.nexus.CharactersBlock;
/*     */ import org.biojavax.bio.phylo.io.nexus.DataBlock;
/*     */ import org.biojavax.bio.phylo.io.nexus.MetapigaBlock;
/*     */ import org.biojavax.bio.phylo.io.nexus.MyNexusFileBuilder;
/*     */ import org.biojavax.bio.phylo.io.nexus.NexusBlock;
/*     */ import org.biojavax.bio.phylo.io.nexus.NexusComment;
/*     */ import org.biojavax.bio.phylo.io.nexus.NexusFile;
/*     */ import org.biojavax.bio.phylo.io.nexus.NexusFileFormat;
/*     */ import org.biojavax.bio.phylo.io.nexus.TreesBlock;
/*     */ 
/*     */ public class NexusReader extends SwingWorker<WaitingLogo.Status, Object>
/*     */ {
/*     */   private MetaPIGA metapiga;
/*  53 */   File nexus = null;
/*  54 */   private InputStream is = null;
/*     */   private boolean isBatch;
/*     */   private Parameters parameters;
/*     */   private DefaultListModel parametersList;
/*     */   private BatchBlock batchBlock;
/*     */   private Map<String, CharactersBlock> dataBlocks;
/*     */   private Map<String, MetapigaBlock> metapigaBlocks;
/*     */   private Map<String, TreesBlock> treesBlocks;
/*     */ 
/*     */   public NexusReader(File nexusFile, MetaPIGA metapiga)
/*     */   {
/*  64 */     this.metapiga = metapiga;
/*  65 */     this.parametersList = metapiga.parameters;
/*  66 */     this.nexus = nexusFile;
/*     */   }
/*     */ 
/*     */   public NexusReader(InputStream nexusInputStream, MetaPIGA metapiga) {
/*  70 */     this.metapiga = metapiga;
/*  71 */     this.parametersList = metapiga.parameters;
/*  72 */     this.is = nexusInputStream;
/*     */   }
/*     */ 
/*     */   public NexusReader(DefaultListModel listOfParams, File nexusFile)
/*     */   {
/*  77 */     this.metapiga = null;
/*  78 */     this.parametersList = listOfParams;
/*  79 */     this.nexus = nexusFile;
/*     */   }
/*     */ 
/*     */   public boolean isBatch() {
/*  83 */     return this.isBatch;
/*     */   }
/*     */ 
/*     */   public WaitingLogo.Status doInBackground()
/*     */   {
/*     */     try {
/*  89 */       checkNexusFile();
/*  90 */       MyNexusFileBuilder builder = new MyNexusFileBuilder();
/*  91 */       if (this.nexus != null) {
/*  92 */         NexusFileFormat.parseFile(builder, this.nexus);
/*  93 */         String name = "";
/*  94 */         String[] split = this.nexus.getName().split("\\.");
/*  95 */         for (int i = 0; i < split.length - 1; i++) {
/*  96 */           name = name + split[i];
/*  97 */           if (i + 1 < split.length - 1) name = name + ".";
/*     */         }
/*  99 */         this.parameters = new Parameters(name);
/*     */       } else {
/* 101 */         NexusFileFormat.parseInputStream(builder, this.is);
/* 102 */         this.parameters = new Parameters("default");
/*     */       }
/*     */ 
/* 105 */       this.isBatch = false;
/* 106 */       for (Iterator it = builder.getNexusFile().blockIterator(); it.hasNext(); ) {
/* 107 */         NexusBlock block = (NexusBlock)it.next();
/* 108 */         if (block.getBlockName().equals("BATCH")) {
/* 109 */           this.isBatch = true;
/* 110 */           this.batchBlock = ((BatchBlock)block);
/*     */         }
/*     */       }
/* 113 */       if (this.isBatch) return batchRunExtraction(builder.getNexusFile());
/* 114 */       return singleRunExtraction(builder.getNexusFile());
/*     */     } catch (OutOfMemoryError e) {
/* 116 */       WaitingLogo.Status error = WaitingLogo.Status.DATA_FILE_NOT_LOADED;
/* 117 */       error.text += "\nOut of memory: please, assign more RAM to MetaPIGA. You can easily do so by using the menu 'Tools --> Memory settings'.";
/* 118 */       return error;
/*     */     } catch (Exception e) {
/* 120 */       WaitingLogo.Status error = WaitingLogo.Status.DATA_FILE_NOT_LOADED;
/*     */       WaitingLogo.Status tmp277_276 = error; tmp277_276.text = (tmp277_276.text + "\n" + Tools.getErrorMessage(e));
/* 122 */       return error;
/*     */     }
/*     */   }
/*     */ 
/*     */   private void checkNexusFile() throws Exception {
/* 127 */     BufferedReader br = null;
/* 128 */     FileReader fr = null;
/* 129 */     InputStreamReader isr = null;
/* 130 */     if (this.nexus != null) {
/* 131 */       fr = new FileReader(this.nexus);
/* 132 */       br = new BufferedReader(fr);
/*     */     } else {
/* 134 */       isr = new InputStreamReader(this.is);
/* 135 */       br = new BufferedReader(isr);
/*     */     }
/*     */     String line;
/* 138 */     while ((line = br.readLine()) != null)
/*     */     {
/*     */       String line;
/* 139 */       if (line.toUpperCase().contains("ENDBLOCK")) {
/* 140 */         throw new Exception("'ENDBLOCK' is not a valid token.\nPlease replace it by 'END'.");
/*     */       }
/*     */     }
/* 143 */     if (br != null) br.close();
/* 144 */     if (fr != null) fr.close();
/* 145 */     if (isr != null) isr.close(); 
/*     */   }
/*     */ 
/*     */   private WaitingLogo.Status singleRunExtraction(NexusFile nexusFile)
/*     */     throws ParseNexusException, NexusInconsistencyException, UnknownDataException, CharsetIntersectionException, IncompatibleDataException
/*     */   {
/* 150 */     TreesBlock tb = null;
/* 151 */     boolean isConvertingToCodons = false;
/* 152 */     int startCodons = 0;
/* 153 */     int endCodons = 0;
/* 154 */     Parameters.CodonTransitionTableType codonTableType = null;
/*     */     NexusBlock block;
/* 155 */     for (Iterator it = nexusFile.blockIterator(); it.hasNext(); ) {
/* 156 */       block = (NexusBlock)it.next();
/* 157 */       if (block.getBlockName().equals("METAPIGA")) {
/* 158 */         MetapigaBlock mp = (MetapigaBlock)block;
/* 159 */         this.parameters.setParameters(mp);
/* 160 */         if (mp.getDataType() == DataType.CODON) {
/* 161 */           isConvertingToCodons = true;
/* 162 */           startCodons = mp.getCodonDomainStartPosition();
/* 163 */           endCodons = mp.getCodonDomainEndPosition();
/* 164 */           codonTableType = mp.getCodonTable();
/*     */         }
/* 166 */       } else if (block.getBlockName().equals("CHARACTERS")) {
/* 167 */         CharactersBlock cb = (CharactersBlock)block;
/* 168 */         if (cb.getGap() == null) cb.setGap("-");
/* 169 */         this.parameters.setParameters(cb);
/* 170 */       } else if (block.getBlockName().equals("DATA")) {
/* 171 */         DataBlock db = (DataBlock)block;
/* 172 */         if (db.getGap() == null) db.setGap("-");
/* 173 */         this.parameters.setParameters(db);
/* 174 */       } else if (block.getBlockName().equals("TREES")) {
/* 175 */         tb = (TreesBlock)block;
/* 176 */       } else if (block.getBlockName().equals("SETS")) {
/* 177 */         System.out.println("SETS block is not used in MetaPIGA. \nIf you want to define charsets, you can do it with the CHARSET command in the METAPIGA block.");
/* 178 */       } else if (block.getBlockName().equals("TAXA")) {
/* 179 */         System.out.println("TAXA block is not used in MetaPIGA. \nMetaPiga uses taxas found in DATA or CHARACTER block.");
/*     */       } else {
/* 181 */         System.out.println(block.getBlockName() + " block is not used in MetaPIGA.");
/*     */       }
/*     */     }
/* 184 */     this.parameters.nexusFile = this.nexus;
/* 185 */     if (isConvertingToCodons) {
/* 186 */       this.parameters.setCodonsInRange(startCodons, endCodons, codonTableType);
/*     */     }
/* 188 */     this.parameters.buildDataset();
/* 189 */     this.parameters.checkParameters();
/* 190 */     if (tb != null) {
/* 191 */       this.parameters.setParameters(tb);
/* 192 */       if (this.parameters.startingTreeGeneration == Parameters.StartingTreeGeneration.GIVEN) {
/* 193 */         for (Tree tree : this.parameters.startingTrees) {
/* 194 */           if (!tree.isCompatibleWithOutgroup(this.parameters.outgroup))
/* 195 */             throw new ParseNexusException("Topology of tree " + tree.getName() + " is NOT compatible with defined outgroup.");
/*     */         }
/*     */       }
/*     */     }
/* 199 */     this.parametersList.addElement(this.parameters);
/* 200 */     if (MetaPIGA.ui == MetaPIGA.UI.GRAPHICAL) this.metapiga.mainFrame.updateMatrixTextPanes();
/* 201 */     return WaitingLogo.Status.DATA_FILE_LOADED;
/*     */   }
/*     */ 
/*     */   private WaitingLogo.Status batchRunExtraction(NexusFile nexusFile) throws ParseNexusException, NexusInconsistencyException, UnknownDataException, CharsetIntersectionException, IncompatibleDataException
/*     */   {
/* 206 */     this.dataBlocks = new HashMap();
/* 207 */     this.metapigaBlocks = new HashMap();
/* 208 */     this.treesBlocks = new HashMap();
/*     */     NexusBlock block;
/* 209 */     for (Iterator it = nexusFile.blockIterator(); it.hasNext(); ) {
/* 210 */       block = (NexusBlock)it.next();
/* 211 */       if (block.getBlockName().equals("METAPIGA")) {
/* 212 */         MetapigaBlock mp = (MetapigaBlock)block;
/* 213 */         String label = extractLabelFromComment(mp.getComments());
/* 214 */         if (label != null) this.metapigaBlocks.put(label, mp); 
/*     */       }
/* 215 */       else if (block.getBlockName().equals("CHARACTERS")) {
/* 216 */         CharactersBlock cb = (CharactersBlock)block;
/* 217 */         String label = extractLabelFromComment(cb.getComments());
/* 218 */         if (label != null) this.dataBlocks.put(label, cb); 
/*     */       }
/* 219 */       else if (block.getBlockName().equals("DATA")) {
/* 220 */         DataBlock db = (DataBlock)block;
/* 221 */         String label = extractLabelFromComment(db.getComments());
/* 222 */         if (label != null) this.dataBlocks.put(label, db); 
/*     */       }
/* 223 */       else if (block.getBlockName().equals("TREES")) {
/* 224 */         TreesBlock tb = (TreesBlock)block;
/* 225 */         String label = extractLabelFromComment(tb.getComments());
/* 226 */         if (label != null) this.treesBlocks.put(label, tb);
/*     */       }
/*     */     }
/* 229 */     for (String label : this.batchBlock.getRunLabels()) {
/* 230 */       if (!this.batchBlock.getRunData().containsKey(label)) throw new ParseNexusException("RUN " + label + " has no DATA associated with it !");
/* 231 */       if (!this.batchBlock.getRunParam().containsKey(label)) throw new ParseNexusException("RUN " + label + " has no PARAM associated with it !");
/* 232 */       String data = (String)this.batchBlock.getRunData().get(label);
/* 233 */       String param = (String)this.batchBlock.getRunParam().get(label);
/* 234 */       String trees = this.batchBlock.getRunTrees().containsKey(label) ? (String)this.batchBlock.getRunTrees().get(label) : null;
/* 235 */       if (!this.dataBlocks.containsKey(data)) throw new ParseNexusException("No DATA or CHARACTER block labelled " + data + " was found in the Nexus file.");
/* 236 */       if (!this.metapigaBlocks.containsKey(param)) throw new ParseNexusException("No METAPIGA block labelled " + param + " was found in the Nexus file.");
/* 237 */       if ((trees != null) && (!this.treesBlocks.containsKey(trees))) throw new ParseNexusException("No TREES block labelled " + trees + " was found in the Nexus file.");
/* 238 */       Parameters P = new Parameters(label);
/* 239 */       P.setParameters((MetapigaBlock)this.metapigaBlocks.get(param));
/* 240 */       P.label = label;
/* 241 */       P.setParameters((CharactersBlock)this.dataBlocks.get(data));
/* 242 */       P.buildDataset();
/* 243 */       P.checkParameters();
/* 244 */       if (trees != null) {
/* 245 */         P.setParameters((TreesBlock)this.treesBlocks.get(trees));
/* 246 */         if (P.startingTreeGeneration == Parameters.StartingTreeGeneration.GIVEN) {
/* 247 */           for (Tree tree : P.startingTrees) {
/* 248 */             if (!tree.isCompatibleWithOutgroup(P.outgroup))
/* 249 */               throw new ParseNexusException("RUN " + label + " : Topology of tree " + tree.getName() + " is NOT compatible with defined outgroup.");
/*     */           }
/*     */         }
/*     */       }
/* 253 */       this.parametersList.addElement(P);
/*     */     }
/* 255 */     if (MetaPIGA.ui == MetaPIGA.UI.GRAPHICAL) this.metapiga.mainFrame.updateMatrixTextPanes();
/* 256 */     return WaitingLogo.Status.DATA_BATCH_LOADED;
/*     */   }
/*     */ 
/*     */   private String extractLabelFromComment(List comments)
/*     */   {
/*     */     Iterator it;
/* 261 */     for (Iterator localIterator1 = comments.iterator(); localIterator1.hasNext(); 
/* 262 */       it.hasNext())
/*     */     {
/* 261 */       Object comment = localIterator1.next();
/* 262 */       it = ((NexusComment)comment).commentIterator(); continue;
/* 263 */       String comString = it.next().toString().toUpperCase();
/* 264 */       if (comString.contains("BATCHLABEL")) {
/* 265 */         return comString.split("=")[1].replace('_', ' ').trim();
/*     */       }
/*     */     }
/*     */ 
/* 269 */     return null;
/*     */   }
/*     */ 
/*     */   protected void done() {
/*     */     try {
/* 274 */       if (MetaPIGA.ui == MetaPIGA.UI.GRAPHICAL) this.metapiga.mainFrame.setAllEnabled(this.metapiga.mainFrame, (WaitingLogo.Status)get()); else
/* 275 */         this.metapiga.busy = false;
/*     */     } catch (ExecutionException e) {
/* 277 */       e.getCause().printStackTrace();
/*     */     } catch (InterruptedException e) {
/* 279 */       e.printStackTrace();
/*     */     }
/*     */   }
/*     */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.io.NexusReader
 * JD-Core Version:    0.6.2
 */