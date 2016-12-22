package org.eclipse.emf.common.util;

import org.eclipse.emf.common.util.Pool.AccessUnit;

/**
 * An Access unit for component-based access.
 */
class URIComponentsAccessUnit extends URIPoolAccessUnitBase
{
  /**
   * A value for {@link #validate} that implies no checking or interning of components is required.
   */
  protected static final int VALIDATE_NONE = -1;

  /**
   * A value for {@link #validate} that implies all components need to be validated.
   */
  protected static final int VALIDATE_ALL = -2;

  /**
   * A value for {@link #validate} that implies only the query componet needs validating.
   */
  protected static final int VALIDATE_QUERY = -3;

  protected static class Queue extends AccessUnit.Queue<URI>
  {
    private static final long serialVersionUID = 1L;

    @Override
    public URIComponentsAccessUnit pop(boolean isExclusive)
    {
      return (URIComponentsAccessUnit)super.pop(isExclusive);
    }

    @Override
    protected AccessUnit<URI> newAccessUnit()
    {
      return new URIComponentsAccessUnit(this);
    }
  }

  /**
   * One of the special values {@link #VALIDATE_NONE}, {@link #VALIDATE_ALL}, or {@link #VALIDATE_QUERY}, or the index in the {@link #segments} that need validation.
   */
  int validate;

  /**
   * Whether the components being accesses are for a hierarchical URI
   */
  boolean hierarchical;

  /**
   * The scheme being accessed.
   */
  String scheme;

  /**
   * The authority (or opaque part) being accessed.
   */
  String authority;

  /**
   * The device being accessed.
   */
  String device;
  /**
   * Whether the components being accesses are for an absolute path.
   */
  boolean absolutePath;

  /**
   * The segments being accessed.
   */
  String[] segments;

  /**
   * The query being accessed.
   */
  String query;

  /**
   * An access unit for exclusive use in {@link #internArray(String[], int)}.
   */
  SegmentSequence.StringArrayPool.SegmentsAccessUnit stringArraySegmentsAccessUnit = new SegmentSequence.StringArrayPool.SegmentsAccessUnit(null);

  /**
   * Creates an instance managed by the given queue.
   * @param queue
   */
  protected URIComponentsAccessUnit(URIComponentsAccessUnit.Queue queue)
  {
    super(queue);
  }

  protected String[] internArray(String[] segments, int count)
  {
    if (segments == null)
    {
      return SegmentSequence.EMPTY_ARRAY;
    }
    else
    {
      stringArraySegmentsAccessUnit.setValue(true, true, segments, count);
      return SegmentSequence.STRING_ARRAY_POOL.doIntern(false, stringArraySegmentsAccessUnit);
    }
  }

  /**
   * Caches the parameters.
   */
  protected void setValue(boolean hierarchical, String scheme, String authority, String device, boolean absolutePath, String[] segments, String query, int hashCode)
  {
    this.validate = VALIDATE_NONE;
    this.hierarchical = hierarchical;
    this.scheme = scheme;
    this.authority = authority;
    this.device = device;
    this.absolutePath = absolutePath;
    this.segments = segments;
    this.query = query;
    this.hashCode = hashCode;
  }

  /**
   * Caches the parameters and computes the hash code.
   */
  protected void setValue(int validate, boolean hierarchical, String scheme, String authority, String device, boolean absolutePath, String[] segments, String query)
  {
    int hashCode = 0;
    if (scheme != null)
    {
      if (validate == VALIDATE_ALL)
      {
        scheme = intern(true, scheme);
      }
      hashCode = scheme.hashCode() * 31 + URI.SCHEME_SEPARATOR;
    }

    this.validate = validate;
    this.hierarchical = hierarchical;
    this.scheme = scheme;
    this.authority = authority;
    this.device = device;
    this.absolutePath = absolutePath;
    this.segments = segments;
    this.query = query;

    if (hierarchical)
    {
      if (segments == null)
      {
        segments = URI.NO_SEGMENTS;
      }

      this.segments = segments;

      if (authority != null)
      {
        if (!URI.isArchiveScheme(scheme)) hashCode = hashCode * 961 + URI.AUTHORITY_SEPARATOR_HASH_CODE;
        hashCode = hashCode * CommonUtil.powerOf31(authority.length()) + authority.hashCode();
      }

      if (device != null)
      {
        hashCode = hashCode * 31 + URI.SEGMENT_SEPARATOR;
        hashCode = hashCode * CommonUtil.powerOf31(device.length()) + device.hashCode();
      }

      if (absolutePath) hashCode = hashCode * 31 + URI.SEGMENT_SEPARATOR;

      for (int i = 0, len = segments.length; i < len; i++)
      {
        if (i != 0) hashCode = hashCode * 31 + URI.SEGMENT_SEPARATOR;
        String segment = segments[i];
        if (segment == null)
        {
          throw new IllegalArgumentException("invalid segment: null");
        }
        hashCode = hashCode * CommonUtil.powerOf31(segment.length()) + segment.hashCode();
      }

      if (query != null)
      {
        hashCode = hashCode * 31 + URI.QUERY_SEPARATOR;
        hashCode = hashCode * CommonUtil.powerOf31(query.length()) + query.hashCode();
      }
    }
    else
    {
      hashCode = hashCode * CommonUtil.powerOf31(authority.length()) + authority.hashCode();
    }

    this.hashCode = hashCode;
  }

  @Override
  protected boolean matches(URI value)
  {
    return value.matches(validate, hierarchical, scheme, authority, device, absolutePath, segments, query);
  }

  @Override
  public URI getInternalizedValue()
  {
    if (validate == VALIDATE_ALL)
    {
      // Validate all the components.
      //
      URI.validateURI(hierarchical, scheme, authority, device, absolutePath, segments, query, null);

      // Intern the components.
      //
      if (authority != null)
      {
        authority = intern(authority);
      }
      if (device != null)
      {
        device = intern(device);
      }
      segments = segments == null ? null : internArray(segments, segments.length);
      if (query != null)
      {
        query = intern(query);
      }
    }
    else if (validate == VALIDATE_QUERY)
    {
      // Validate just the query.
      //
      if (!URI.validQuery(query))
      {
        throw new IllegalArgumentException("invalid query portion: " + query);
      }

      // Intern the just the query.
      //
      if (query != null)
      {
        query = intern(query);
      }
    }
    else if (validate != VALIDATE_NONE)
    {
      // Validate the segments that need validation.
      //
      for (int i = validate, length = segments.length; i < length; ++i)
      {
        String segment = segments[i];
        if (!URI.validSegment(segment))
        {
          throw new IllegalArgumentException("invalid segment: " + segment);
        }
      }
    }

    // Create the appropriate type of URI.
    //
    if (hierarchical)
    {
      return new Hierarchical(hashCode, scheme, authority, device, absolutePath, segments, query);
    }
    else
    {
      return new Opaque(hashCode, scheme, authority);
    }
  }

  @Override
  public void reset(boolean isExclusive)
  {
    this.scheme = null;
    this.authority = null;
    this.device = null;
    this.segments = null;
    this.query = null;
    super.reset(isExclusive);
  }
}