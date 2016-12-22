package org.eclipse.emf.common.util;

import org.eclipse.emf.common.util.Pool.AccessUnit;

/**
 * A based access unit for this pool.
 */
class URIPoolAccessUnitBase extends AccessUnit<URI>
{
  /**
   * A local access unit for exclusive use in {@link #intern(char[], int, int)}.
   */
  protected CommonUtil.StringPool.CharactersAccessUnit charactersAccessUnit =  new CommonUtil.StringPool.CharactersAccessUnit(null);

  /**
   * A local access unit for exclusive for normalizing the scheme in {@link #intern(String)}, {@link #intern(boolean, String)}, and {@link StringAccessUnit#parseIntoURI(String)}.
   */
  protected CommonUtil.StringPool.StringAccessUnit stringAccessUnit =  new CommonUtil.StringPool.StringAccessUnit(CommonUtil.STRING_POOL, null);

  /**
   * The string pool entry found during the most recent use of {@link #substringAccessUnit}.
   */
  protected CommonUtil.StringPool.StringPoolEntry stringPoolEntry;

  /**
   * A local access unit for exclusive use in {@link #intern(String, int, int)} and {@link #intern(String, int, int, int)}.
   * It {@link #stringPoolEntry} the string pool entry that was matched when {@link CommonUtil.StringPool.SubstringAccessUnit#reset(boolean)} is called.
   */
  protected CommonUtil.StringPool.SubstringAccessUnit substringAccessUnit =
    new CommonUtil.StringPool.SubstringAccessUnit(null)
    {
      @Override
      public void reset(boolean isExclusive)
      {
        stringPoolEntry = (CommonUtil.StringPool.StringPoolEntry)getEntry();
        super.reset(isExclusive);
      }
    };

  /**
   * An access unit for exclusive use in {@link #internArray(String[], int, int, int)}.
   */
  protected SegmentSequence.StringArrayPool.SegmentsAndSegmentCountAccessUnit stringArraySegmentsAndSegmentCountAccessUnit = new SegmentSequence.StringArrayPool.SegmentsAndSegmentCountAccessUnit(null);

  protected URIPoolAccessUnitBase(Pool.AccessUnit.Queue<URI> queue)
  {
    super(queue);
  }

  @Override
  protected URI getValue()
  {
    throw new UnsupportedOperationException();
  }

  @Override
  protected void setValue(URI value)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  protected boolean setArbitraryValue(Object value)
  {
    throw new UnsupportedOperationException();
  }

  protected String intern(String string)
  {
    stringAccessUnit.setValue(string);
    return CommonUtil.STRING_POOL.doIntern(false, stringAccessUnit);
  }

  protected String intern(boolean toLowerCase, String string)
  {
    stringAccessUnit.setValue(toLowerCase, string);
    return CommonUtil.STRING_POOL.doIntern(false, stringAccessUnit);
  }

  protected String intern(String string, int offset, int count, int hashCode)
  {
    substringAccessUnit.setValue(string, offset, count, hashCode);
    return CommonUtil.STRING_POOL.doIntern(false, substringAccessUnit);
  }

  protected String intern(String string, int offset, int count)
  {
    substringAccessUnit.setValue(string, offset, count);
    return CommonUtil.STRING_POOL.doIntern(false, substringAccessUnit);
  }

  protected String intern(char[] characters, int offset, int count)
  {
    charactersAccessUnit.setValue(characters, offset, count);
    return CommonUtil.STRING_POOL.doIntern(false, charactersAccessUnit);
  }

  protected String intern(char[] characters, int offset, int count, int hashCode)
  {
    charactersAccessUnit.setValue(characters, offset, count, hashCode);
    return CommonUtil.STRING_POOL.doIntern(false, charactersAccessUnit);
  }

  protected String[] internArray(String[] segments, int offset, int segmentCount, int hashCode)
  {
    stringArraySegmentsAndSegmentCountAccessUnit.setValue(segments, offset, segmentCount, hashCode);
    return SegmentSequence.STRING_ARRAY_POOL.doIntern(false, stringArraySegmentsAndSegmentCountAccessUnit);
  }

  @Override
  public void reset(boolean isExclusive)
  {
    stringPoolEntry = null;
    super.reset(isExclusive);
  }
}