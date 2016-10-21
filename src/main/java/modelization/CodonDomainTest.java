/*    */ package modelization;
/*    */ 
/*    */ import metapiga.parameters.Parameters;
/*    */ import org.biojavax.bio.phylo.io.nexus.CharactersBlock;
/*    */ import org.junit.Test;
/*    */ import org.mockito.Mockito;
/*    */ import org.mockito.stubbing.OngoingStubbing;
/*    */ 
/*    */ public class CodonDomainTest
/*    */ {
/*    */   @Test
/*    */   public void negaiveValueTest()
/*    */   {
/* 16 */     Parameters testParam = new Parameters("TestParam");
/* 17 */     CharactersBlock mockCharBlock = (CharactersBlock)Mockito.mock(CharactersBlock.class);
/* 18 */     Mockito.when(Integer.valueOf(mockCharBlock.getDimensionsNChar())).thenReturn(Integer.valueOf(10));
/*    */ 
/* 20 */     testParam.charactersBlock = mockCharBlock;
/*    */   }
/*    */ }

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.modelization.CodonDomainTest
 * JD-Core Version:    0.6.2
 */