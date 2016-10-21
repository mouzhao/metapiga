/*     */ package metapiga.io;
/*     */ 
/*     */ import java.util.LinkedHashMap;
/*     */ import java.util.Map;
/*     */
/*     */ import metapiga.parameters.Parameters;
/*     */ import metapiga.trees.ConsensusNode;
/*     */ import metapiga.trees.Node;
/*     */ import metapiga.trees.Node.Neighbor;
/*     */ import metapiga.trees.Tree;
/*     */ import metapiga.trees.exceptions.TooManyNeighborsException;
/*     */ import metapiga.trees.exceptions.UnknownNeighborException;
/*     */ import metapiga.trees.exceptions.UnknownTaxonException;
/*     */ import metapiga.utilities.Tools;
/*     */ 
/*     */ public class NewickReader
/*     */ {
/*     */   Tree tree;
/*     */   NewickParser parser;
/*     */   boolean isRooted;
/*     */   boolean isConsensus;
/*     */   Map<String, String> translation;
/*     */ 
/*     */   public NewickReader(Parameters parameters, String treeName, String newickTree, Map<String, String> translation)
/*     */   {
/*  31 */     this.tree = new Tree(treeName.trim(), parameters);
/*  32 */     this.translation = translation;
/*  33 */     this.parser = new NewickParser(newickTree.trim());
/*     */   }
/*     */ 
/*     */   public NewickReader(Parameters parameters, String newickString) {
/*  37 */     int c = 0;
/*  38 */     boolean found = false;
/*  39 */     boolean comment = false;
/*  40 */     while ((c < newickString.length()) && (!found)) {
/*  41 */       if (newickString.charAt(c) == '[')
/*  42 */         comment = true;
/*  43 */       else if (newickString.charAt(c) == ']') {
/*  44 */         comment = false;
/*     */       }
/*  46 */       else if ((!comment) && (newickString.charAt(c) == '=')) found = true;
/*     */ 
/*  48 */       c++;
/*     */     }
/*     */     String newick;
/*     */     String newick;
/*  51 */     if (!found) {
/*  52 */       String name = "Unnamed tree";
/*  53 */       newick = newickString;
/*     */     } else {
/*  55 */       name = newickString.substring(0, c - 1).trim();
/*  56 */       newick = newickString.substring(c).trim();
/*     */     }
/*  58 */     if (name.toUpperCase().startsWith("TREE ")) {
/*  59 */       name = name.substring(4).trim();
/*     */     }
/*  61 */     String name = name.replace('_', ' ');
/*  62 */     this.tree = new Tree(name.trim(), parameters);
/*  63 */     this.translation = new LinkedHashMap();
/*  64 */     this.parser = new NewickParser(newick.trim());
/*     */   }
/*     */ 
/*     */   public Tree parseNewick()
/*     */     throws ParseTreeException
/*     */   {
/*     */     try
/*     */     {
/* 247 */       Node accessNode = parseNext();
/* 248 */       if ((this.parser.isRooted()) && (accessNode.getNeighborNodes().size() == 2))
/*     */       {
/* 250 */         Node A = accessNode.getNeighbor(Node.Neighbor.A);
/* 251 */         Node B = accessNode.getNeighbor(Node.Neighbor.B);
/* 252 */         Node.Neighbor keyA = A.replaceNeighbor(accessNode, B);
/* 253 */         Node.Neighbor keyB = B.replaceNeighbor(accessNode, A);
/* 254 */         A.setBranchLength(keyA, A.getBranchLength(keyA) + B.getBranchLength(keyB));
/* 255 */         accessNode = A;
/*     */       } else {
/* 257 */         this.tree.addNode(accessNode);
/*     */       }
/* 259 */       this.tree.setAccessNode(accessNode);
/* 260 */       this.tree.labelizeTree();
/* 261 */       return this.tree;
/*     */     } catch (UnknownTaxonException e) {
/* 263 */       throw new ParseTreeException("Cannot parse the tree " + this.tree.getName() + ": " + Tools.getErrorMessage(e), e.getCause());
/*     */     } catch (TooManyNeighborsException e) {
/* 265 */       throw new ParseTreeException("Cannot parse the tree " + this.tree.getName() + ": " + Tools.getErrorMessage(e), e.getCause());
/*     */     } catch (UnknownNeighborException e) {
/* 267 */       throw new ParseTreeException("Cannot parse the tree " + this.tree.getName() + ": " + Tools.getErrorMessage(e), e.getCause());
/*     */     }
/*     */   }
/*     */ 
/*     */   public Node parseNext() throws TooManyNeighborsException, UnknownTaxonException, ParseTreeException {
/* 272 */     Node currentNode = this.isConsensus ? new ConsensusNode() : new Node();
/* 273 */     while (this.parser.hasNext()) {
/* 274 */       Token token = this.parser.next();
/* 275 */       if (token.isOP()) {
/* 276 */         Node firstNeighbor = parseNext();
/* 277 */         Node.Neighbor key = currentNode.addNeighbor(firstNeighbor);
/* 278 */         this.tree.addNode(firstNeighbor);
/* 279 */         if (this.parser.seeNext().isConsensus()) {
/* 280 */           token = this.parser.next();
/* 281 */           token = this.parser.next();
/* 282 */           ((ConsensusNode)currentNode).setBranchStrength(key, token.toDouble());
/*     */         }
/* 284 */         if (this.parser.seeNext().isColon()) {
/* 285 */           token = this.parser.next();
/* 286 */           token = this.parser.next();
/* 287 */           currentNode.setBranchLength(key, token.toDouble());
/*     */         }
/* 289 */         if (this.parser.seeNext().isConsensus()) {
/* 290 */           token = this.parser.next();
/* 291 */           token = this.parser.next();
/* 292 */           ((ConsensusNode)currentNode).setBranchStrength(key, token.toDouble());
/*     */         }
/* 294 */       } else if (token.isComma()) {
/* 295 */         Node nextNeighbor = parseNext();
/* 296 */         Node.Neighbor key = currentNode.addNeighbor(nextNeighbor);
/* 297 */         this.tree.addNode(nextNeighbor);
/* 298 */         if (this.parser.seeNext().isConsensus()) {
/* 299 */           token = this.parser.next();
/* 300 */           token = this.parser.next();
/* 301 */           ((ConsensusNode)currentNode).setBranchStrength(key, token.toDouble());
/*     */         }
/* 303 */         if (this.parser.seeNext().isColon()) {
/* 304 */           token = this.parser.next();
/* 305 */           token = this.parser.next();
/* 306 */           currentNode.setBranchLength(key, token.toDouble());
/*     */         }
/* 308 */         if (this.parser.seeNext().isConsensus()) {
/* 309 */           token = this.parser.next();
/* 310 */           token = this.parser.next();
/* 311 */           ((ConsensusNode)currentNode).setBranchStrength(key, token.toDouble());
/*     */         }
/*     */       } else { if (token.isString()) {
/* 314 */           String label = token.toString();
/* 315 */           if (this.translation.containsKey(label)) label = (String)this.translation.get(label);
/* 316 */           currentNode.setLabel(label.replace('_', ' '));
/* 317 */           return currentNode;
/* 318 */         }if (token.isCP()) {
/* 319 */           if ((!this.isConsensus) && (this.parser.seeNext().isString())) {
/* 320 */             token = this.parser.next();
/* 321 */             String label = token.toString();
/* 322 */             if (this.translation.containsKey(label)) label = (String)this.translation.get(label);
/* 323 */             currentNode.setLabel(label.replace('_', ' '));
/*     */           }
/* 325 */           return currentNode;
/* 326 */         }if (token.isSemiColon()) {
/* 327 */           return currentNode;
/*     */         }
/* 329 */         throw new ParseTreeException(token.toString() + " found where it should not !");
/*     */       }
/*     */     }
/* 332 */     throw new ParseTreeException("terminal ';' was not found");
/*     */   }
/*     */ 
/*     */   private class NewickParser
/*     */   {
/*     */     String newickString;
/*  69 */     int currentPos = 0;
/*     */ 
/*     */     public NewickParser(String newickTree) {
/*  72 */       this.newickString = newickTree;
/*  73 */       if (!this.newickString.toUpperCase().contains("[&")) {
/*  74 */         NewickReader.this.isConsensus = ((this.newickString.toUpperCase().contains(")0.")) || (this.newickString.toUpperCase().indexOf(")1:") != this.newickString.toUpperCase().lastIndexOf(")1:")));
/*     */       } else {
/*  76 */         NewickReader.this.isRooted = this.newickString.toUpperCase().contains("[&R]");
/*  77 */         NewickReader.this.isConsensus = this.newickString.toUpperCase().contains("[&C]");
/*     */       }
/*  79 */       this.newickString = removeComments(this.newickString).trim();
/*  80 */       if (!this.newickString.endsWith(";")) this.newickString += ";"; 
/*     */     }
/*     */ 
/*     */     public String removeComments(String newick)
/*     */     {
/*  84 */       boolean commentConsensus = false;
/*  85 */       String res = "";
/*  86 */       int c = 0;
/*  87 */       while (c < newick.length()) {
/*  88 */         if (newick.charAt(c) == '[') {
/*  89 */           String comment = "";
/*  90 */           c++;
/*  91 */           while ((c < newick.length()) && (newick.charAt(c) != ']')) {
/*  92 */             comment = comment + newick.charAt(c);
/*  93 */             c++;
/*     */           }
/*  95 */           if (comment.startsWith("C=")) {
/*  96 */             commentConsensus = true;
/*  97 */             if (comment.endsWith("%")) {
/*  98 */               double d = Double.parseDouble(comment.substring(2, comment.length() - 1));
/*  99 */               res = res + "§" + d / 100.0D;
/*     */             } else {
/* 101 */               res = res + "§" + comment.substring(2);
/*     */             }
/*     */           }
/* 104 */           c++;
/*     */         }
/* 106 */         res = res + (newick.charAt(c) == '§' ? "|" : Character.valueOf(newick.charAt(c)));
/* 107 */         c++;
/*     */       }
/* 109 */       if ((NewickReader.this.isConsensus) && (!commentConsensus)) {
/* 110 */         res = res.replace(")", ")§");
/* 111 */         res = res.replace(")§,", "),");
/* 112 */         res = res.replace(")§)", "))");
/* 113 */         res = res.replace(")§;", ");");
/* 114 */         res = res.replace(")§:", "):");
/*     */       }
/*     */ 
/* 119 */       return res;
/*     */     }
/*     */ 
/*     */     public boolean hasNext() {
/* 123 */       return this.currentPos < this.newickString.length();
/*     */     }
/*     */ 
/*     */     public Token next() {
/* 127 */       String s = "";
/* 128 */       for (; s.trim().length() == 0; 
/* 133 */         !
/* 136 */         isToken(this.newickString.charAt(this.currentPos)))
/*     */       {
/* 129 */         if (isToken(this.newickString.charAt(this.currentPos))) {
/* 130 */           this.currentPos += 1;
/* 131 */           return new Token(this.newickString.charAt(this.currentPos - 1));
/*     */         }
/*     */ 
/* 134 */         s = s + this.newickString.charAt(this.currentPos);
/* 135 */         this.currentPos += 1;
/*     */       }
/*     */ 
/* 138 */       return new Token(s);
/*     */     }
/*     */ 
/*     */     public Token seeNext() {
/* 142 */       int pos = this.currentPos;
/* 143 */       String s = "";
/* 144 */       for (; s.trim().length() == 0; 
/* 149 */         !
/* 152 */         isToken(this.newickString.charAt(pos)))
/*     */       {
/* 145 */         if (isToken(this.newickString.charAt(pos))) {
/* 146 */           pos++;
/* 147 */           return new Token(this.newickString.charAt(pos - 1));
/*     */         }
/*     */ 
/* 150 */         s = s + this.newickString.charAt(pos);
/* 151 */         pos++;
/*     */       }
/*     */ 
/* 154 */       return new Token(s);
/*     */     }
/*     */ 
/*     */     private boolean isToken(char c) {
/* 158 */       if (c == '(') return true;
/* 159 */       if (c == ')') return true;
/* 160 */       if (c == ',') return true;
/* 161 */       if (c == ':') return true;
/* 162 */       if (c == '§') return true;
/* 163 */       if (c == ';') return true;
/* 164 */       return false;
/*     */     }
/*     */ 
/*     */     public boolean isRooted() {
/* 168 */       return NewickReader.this.isRooted;
/*     */     }
/*     */ 
/*     */     public boolean isConsensus()
/*     */     {
/* 173 */       return NewickReader.this.isConsensus;
/*     */     }
/*     */   }
/*     */ 
/*     */   private static class Token {
/*     */     String token;
/*     */ 
/*     */     public Token(String s) {
/* 181 */       this.token = s;
/*     */     }
/*     */ 
/*     */     public boolean isOP() {
/* 185 */       return this.token.equals("(");
/*     */     }
/*     */ 
/*     */     public boolean isCP() {
/* 189 */       return this.token.equals(")");
/*     */     }
/*     */ 
/*     */     public boolean isComma() {
/* 193 */       return this.token.equals(",");
/*     */     }
/*     */ 
/*     */     public boolean isColon() {
/* 197 */       return this.token.equals(":");
/*     */     }
/*     */ 
/*     */     public boolean isConsensus() {
/* 201 */       return this.token.equals("§");
/*     */     }
/*     */ 
/*     */     public boolean isSemiColon() {
/* 205 */       return this.token.equals(";");
/*     */     }
/*     */ 
/*     */     public boolean isNumber()
/*     */     {
/*     */       try {
/* 211 */         Double.parseDouble(this.token);
/* 212 */         return true; } catch (NumberFormatException e) {
/*     */       }
/* 214 */       return false;
/*     */     }
/*     */ 
/*     */     public boolean isString()
/*     */     {
/* 219 */       if (isOP()) return false;
/* 220 */       if (isCP()) return false;
/* 221 */       if (isComma()) return false;
/* 222 */       if (isColon()) return false;
/* 223 */       if (isConsensus()) return false;
/* 224 */       if (isSemiColon()) return false;
/* 225 */       return true;
/*     */     }
/*     */ 
/*     */     public String toString() {
/* 229 */       return this.token;
/*     */     }
/*     */ 
/*     */     public double toDouble() {
/* 233 */       return Double.parseDouble(this.token);
/*     */     }
/*     */   }
/*     */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.NewickReader
 * JD-Core Version:    0.6.2
 */