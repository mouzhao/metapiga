package metapiga.modelization.likelihood;

public abstract interface SequenceArrays
{
  public abstract void setElement(float paramFloat, int paramInt1, int paramInt2, int paramInt3, int paramInt4);

  public abstract float getElement(int paramInt1, int paramInt2, int paramInt3, int paramInt4);

  public abstract SequenceArrays clone();

  public abstract void clone(SequenceArrays paramSequenceArrays);

  public abstract int getNodeCount();

  public abstract int getCategoryCount();

  public abstract int getCharacterCountNoPadding();

  public abstract int getStateCount();
}

/* Location:           C:\Program Files\Metapiga\MetaPIGA.jar
 * Qualified Name:     metapiga.SequenceArrays
 * JD-Core Version:    0.6.2
 */