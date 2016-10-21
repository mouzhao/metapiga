/*     */ package io;
/*     */ 
/*     */ import java.io.BufferedReader;
/*     */ import java.io.File;
/*     */ import java.io.FileReader;
/*     */ import java.io.FileWriter;
/*     */ import java.io.IOException;
/*     */ import java.io.PrintStream;
/*     */ import java.util.ArrayList;
/*     */ import java.util.HashMap;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Map.Entry;
/*     */ import java.util.Set;
/*     */ import java.util.concurrent.ExecutionException;
/*     */ import javax.swing.ListModel;
/*     */ import javax.swing.SwingWorker;
/*     */ import metapiga.MainFrame;
/*     */ import metapiga.WaitingLogo.Status;
/*     */ import metapiga.parameters.CharsetsContainer;
/*     */ import metapiga.parameters.Parameters;
/*     */ import metapiga.parameters.Parameters.StartingTreeGeneration;
/*     */ import metapiga.trees.Tree;
/*     */ import org.apache.commons.io.FileUtils;
/*     */ import org.biojavax.bio.phylo.io.nexus.BatchBlock;
/*     */ import org.biojavax.bio.phylo.io.nexus.CharactersBlock;
/*     */ import org.biojavax.bio.phylo.io.nexus.DataBlock;
/*     */ import org.biojavax.bio.phylo.io.nexus.MetapigaBlock;
/*     */ import org.biojavax.bio.phylo.io.nexus.NexusComment;
/*     */ import org.biojavax.bio.phylo.io.nexus.NexusFileFormat;
/*     */ 
/*     */ public class NexusWriter extends SwingWorker<WaitingLogo.Status, Object>
/*     */ {
/*  42 */   private String endl = NexusFileFormat.NEW_LINE;
/*     */   MainFrame frame;
/*     */   String filename;
/*     */   List<Parameters> P;
/*  46 */   boolean saveModified = false;
/*     */ 
/*     */   public NexusWriter(String filename, Parameters parameters) {
/*  49 */     this.filename = filename;
/*  50 */     this.frame = null;
/*  51 */     this.P = new ArrayList();
/*  52 */     this.P.add(parameters);
/*     */   }
/*     */ 
/*     */   public NexusWriter(String filename, Parameters parameters, boolean saveModified, MainFrame frame) {
/*  56 */     this.filename = filename;
/*  57 */     this.frame = frame;
/*  58 */     this.saveModified = saveModified;
/*  59 */     this.P = new ArrayList();
/*  60 */     this.P.add(parameters);
/*     */   }
/*     */ 
/*     */   public NexusWriter(String filename, ListModel parameters, MainFrame frame) {
/*  64 */     this.filename = filename;
/*  65 */     this.frame = frame;
/*  66 */     this.P = new ArrayList();
/*  67 */     for (int i = 0; i < parameters.getSize(); i++)
/*  68 */       this.P.add((Parameters)parameters.getElementAt(i));
/*     */   }
/*     */ 
/*     */   public WaitingLogo.Status doInBackground()
/*     */   {
/*  74 */     File tempOutput = null;
/*     */     try {
/*  76 */       tempOutput = File.createTempFile("nexus", ".temp");
/*  77 */       tempOutput.deleteOnExit();
/*     */     } catch (IOException ex1) {
/*  79 */       System.err.println("Cannot create temporary file");
/*  80 */       ex1.printStackTrace();
/*     */     }
/*  82 */     if (this.P.size() > 1)
/*     */     {
/*  84 */       BatchBlock batchBlock = new BatchBlock();
/*  85 */       Map metapigaBlocks = new HashMap();
/*  86 */       Map dataBlocks = new HashMap();
/*  87 */       Map treesBlocks = new HashMap();
/*  88 */       int mc = 1; int dc = 1; int tc = 1;
/*     */       MetapigaBlock mb;
/*  90 */       for (Parameters p : this.P) {
/*  91 */         batchBlock.addLabel(p.label);
/*     */ 
/*  93 */         mb = p.getMetapigaBlock();
/*  94 */         String key = "param_" + mc;
/*  95 */         mc++;
/*  96 */         metapigaBlocks.put(key, mb);
/*  97 */         batchBlock.addParam(p.label, key);
/*     */ 
/*  99 */         if (p.startingTreeGeneration == Parameters.StartingTreeGeneration.GIVEN) {
/* 100 */           key = "trees_" + tc;
/* 101 */           tc++;
/* 102 */           treesBlocks.put(key, p);
/* 103 */           batchBlock.addTree(p.label, key);
/*     */         }
/*     */ 
/* 106 */         CharactersBlock cb = p.charactersBlock;
/* 107 */         key = null;
/* 108 */         for (Entry E : dataBlocks.entrySet()) {
/* 109 */           if (cb == E.getValue()) {
/* 110 */             key = (String)E.getKey();
/*     */           }
/*     */         }
/* 113 */         if (key == null) {
/* 114 */           key = "data_" + dc;
/* 115 */           dc++;
/* 116 */           dataBlocks.put(key, cb);
/*     */         }
/* 118 */         batchBlock.addData(p.label, key);
/*     */       }
/*     */       try {
/* 121 */         FileWriter fw = new FileWriter(tempOutput);
/* 122 */         fw.write("#NEXUS" + this.endl);
/* 123 */         fw.write("[Metapiga 2 - LANE (Laboratory of Artificial and Natural Evolution, University of Geneva)]" + this.endl);
/* 124 */         fw.write(this.endl);
/* 125 */         batchBlock.writeObject(fw);
/* 126 */         fw.write(this.endl);
/* 127 */         for (Entry E : metapigaBlocks.entrySet()) {
/* 128 */           NexusComment comment = new NexusComment();
/* 129 */           comment.addCommentText("BATCHLABEL=" + ((String)E.getKey()).replace(' ', '_'));
/* 130 */           ((MetapigaBlock)E.getValue()).addComment(comment);
/* 131 */           ((MetapigaBlock)E.getValue()).writeObject(fw);
/* 132 */           fw.write(this.endl);
/*     */         }
/*     */         boolean remove;
/* 134 */         for (Entry E : dataBlocks.entrySet()) {
/* 135 */           for (Iterator com = ((CharactersBlock)E.getValue()).getComments().iterator(); com.hasNext(); ) {
/* 136 */             remove = false;
/* 137 */             for (Iterator subCom = ((NexusComment)com.next()).commentIterator(); subCom.hasNext(); ) {
/* 138 */               String s = (String)subCom.next();
/* 139 */               if (s.toUpperCase().contains("BATCHLABEL")) {
/* 140 */                 remove = true;
/* 141 */                 break;
/*     */               }
/*     */             }
/* 144 */             if (remove) com.remove();
/*     */           }
/* 146 */           NexusComment comment = new NexusComment();
/* 147 */           comment.addCommentText("BATCHLABEL=" + ((String)E.getKey()).replace(' ', '_'));
/* 148 */           ((CharactersBlock)E.getValue()).addComment(comment);
/* 149 */           ((CharactersBlock)E.getValue()).writeObject(fw);
/* 150 */           fw.write(this.endl);
/*     */         }
/* 152 */         for (Entry E : treesBlocks.entrySet()) {
/* 153 */           fw.write("BEGIN TREES;" + this.endl);
/* 154 */           fw.write("[BATCHLABEL=" + ((String)E.getKey()).replace(' ', '_') + "]" + this.endl);
/* 155 */           for (Tree tree : ((Parameters)E.getValue()).startingTrees) {
/* 156 */             fw.write(tree.toNewickLine(false, false) + this.endl);
/*     */           }
/* 158 */           fw.write("END;" + this.endl);
/*     */         }
/* 160 */         fw.close();
/*     */       } catch (Exception e) {
/* 162 */         System.err.println("Can't retrieve source nexus file information");
/* 163 */         e.printStackTrace();
/*     */       }
/*     */     }
/*     */     else {
/* 167 */       Parameters p = (Parameters)this.P.get(0);
/* 168 */       if ((p.nexusFile != null) && (!this.saveModified))
/*     */         try {
/* 170 */           FileReader fr = new FileReader(p.nexusFile);
/* 171 */           FileWriter fw = new FileWriter(tempOutput);
/* 172 */           BufferedReader br = new BufferedReader(fr);
/*     */ 
/* 174 */           fw.write("#NEXUS" + this.endl);
/* 175 */           fw.write("[Metapiga 2 - LANE (Laboratory of Artificial and Natural Evolution, University of Geneva)]" + this.endl);
/* 176 */           fw.write("[You can paste this METAPIGA block in any Nexus file to use it in metapiga with this parameters]" + this.endl + this.endl);
/* 177 */           p.getMetapigaBlock().writeObject(fw);
/* 178 */           fw.write(this.endl);
/* 179 */           if (p.startingTreeGeneration == Parameters.StartingTreeGeneration.GIVEN) {
/* 180 */             p.writeTreeBlock(fw);
/*     */           }
/* 182 */           fw.write(this.endl);
/*     */           String line;
/* 183 */           while ((line = br.readLine()) != null)
/*     */           {
/*     */             String line;
/* 184 */             while ((line != null) && (!line.toUpperCase().startsWith("BEGIN METAPIGA")) && (
/* 185 */               (!line.toUpperCase().startsWith("BEGIN TREES")) || (p.startingTreeGeneration != Parameters.StartingTreeGeneration.GIVEN))) {
/* 186 */               if ((!line.startsWith("[Metapiga 2 - LANE (Laboratory of Artificial and Natural Evolution, University of Geneva)]")) && 
/* 187 */                 (!line.startsWith("[You can paste this METAPIGA block in any Nexus file to use it in metapiga with this parameters]")) && 
/* 188 */                 (!line.toUpperCase().startsWith("#NEXUS")) && (line.length() != 0)) {
/* 189 */                 if (line.startsWith("END")) fw.write(line + this.endl + this.endl); else
/* 190 */                   fw.write(line);
/* 191 */                 fw.write(this.endl);
/*     */               }
/* 193 */               line = br.readLine();
/*     */             }
/* 195 */             if (line != null) {
/* 196 */               while (!line.toUpperCase().startsWith("END")) {
/* 197 */                 line = br.readLine();
/*     */               }
/*     */             }
/*     */           }
/* 201 */           br.close();
/* 202 */           fr.close();
/* 203 */           fw.close();
/*     */         } catch (Exception e) {
/* 205 */           System.err.println("Can't retrieve source nexus file information");
/* 206 */           e.printStackTrace();
/*     */         }
/*     */       else
/*     */         try {
/* 210 */           FileWriter fw = new FileWriter(tempOutput);
/* 211 */           fw.write("#NEXUS" + this.endl);
/* 212 */           fw.write("[Metapiga 2 - LANE (Laboratory of Artificial and Natural Evolution, University of Geneva)]" + this.endl);
/* 213 */           fw.write("[You can paste this METAPIGA block in any Nexus file to use it in metapiga with this parameters]" + this.endl + this.endl);
/* 214 */           if (this.saveModified) {
/* 215 */             Parameters newP = p.duplicate();
/* 216 */             newP.deletedTaxa.clear();
/* 217 */             newP.charsets.clearAll();
/* 218 */             newP.getMetapigaBlock().writeObject(fw);
/* 219 */             fw.write(this.endl);
/* 220 */             DataBlock dataBlock = p.getModifiedDataBlock();
/* 221 */             dataBlock.writeObject(fw);
/*     */           } else {
/* 223 */             p.getMetapigaBlock().writeObject(fw);
/* 224 */             fw.write(this.endl);
/* 225 */             p.charactersBlock.writeObject(fw);
/*     */           }
/* 227 */           fw.write(this.endl);
/* 228 */           if (p.startingTreeGeneration == Parameters.StartingTreeGeneration.GIVEN) {
/* 229 */             p.writeTreeBlock(fw);
/*     */           }
/* 231 */           fw.close();
/*     */         } catch (Exception e) {
/* 233 */           System.err.println("Can't retrieve source nexus file information");
/* 234 */           e.printStackTrace();
/*     */         }
/*     */     }
/*     */     File nexusOutput;
/*     */     File nexusOutput;
/* 239 */     if (this.filename == null)
/* 240 */       nexusOutput = new File("default.nex");
/*     */     else
/* 242 */       nexusOutput = new File(this.filename);
/*     */     try
/*     */     {
/* 245 */       nexusOutput.delete();
/*     */ 
/* 247 */       FileUtils.moveFile(tempOutput, nexusOutput);
/* 248 */       if ((this.P.size() == 1) && (((Parameters)this.P.get(0)).nexusFile != null)) {
/* 249 */         ((Parameters)this.P.get(0)).nexusFile = new File(nexusOutput.getPath());
/*     */       }
/* 251 */       return WaitingLogo.Status.NEXUS_FILE_SAVED;
/*     */     } catch (Exception ex) {
/* 253 */       WaitingLogo.Status error = WaitingLogo.Status.NEXUS_FILE_NOT_SAVED;
/*     */       WaitingLogo.Status tmp1788_1786 = error; tmp1788_1786.text = (tmp1788_1786.text + this.endl + ex.getMessage());
/* 255 */       return error;
/*     */     }
/*     */   }
/*     */ 
/*     */   public void done() {
/*     */     try {
/* 261 */       if (this.frame != null) this.frame.setAllEnabled(this.frame, (WaitingLogo.Status)get()); 
/*     */     }
/* 263 */     catch (ExecutionException e) { e.getCause().printStackTrace();
/*     */     } catch (InterruptedException e) {
/* 265 */       e.printStackTrace();
/*     */     }
/*     */   }
/*     */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.io.NexusWriter
 * JD-Core Version:    0.6.2
 */