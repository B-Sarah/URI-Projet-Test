package org.eclipse.emf.common.util;

/**
 * An access unit for basic string access.
 */
class StringAccessUnit extends URIPoolAccessUnitBase
{
  /**
   * This unit's pool.
   */
  protected final URIPool pool;

  /**
   * The value being accessed.
   */
  protected String value;

  /**
   * The cached hash code computed by {@link #findMajorSeparator(int, String, int)} and {@link #findSegmentEnd(int, String, int)}.
   */
  protected int findHashCode;

  /**
   * The cached terminating character computed by {@link #findMajorSeparator(int, String, int)} and {@link #findSegmentEnd(int, String, int)}.
   */
  protected char findTerminatingCharacter;

  /**
   * An access unit for exclusive use in {@link #internArray(String, int, int, int)}.
   */
  protected SegmentSequence.StringArrayPool.SubstringAccessUnit stringArraySubstringAccessUnit = new SegmentSequence.StringArrayPool.SubstringAccessUnit(null);

  /**
   * An access unit for exclusive use in {@link #internArray(int, String[], int, String, int, int, int)}.
   */
  protected SegmentSequence.StringArrayPool.SegmentsAndSubsegmentAccessUnit stringArraySegmentsAndSubsegmentAccessUnit = new SegmentSequence.StringArrayPool.SegmentsAndSubsegmentAccessUnit(null);

  protected String[] internArray(String segment, int offset, int count, int hashCode)
  {
    stringArraySubstringAccessUnit.setValue(segment, offset, count, hashCode);
    return SegmentSequence.STRING_ARRAY_POOL.doIntern(false, stringArraySubstringAccessUnit);
  }

  protected String[] internArray(int hashCode, String[] segments, int segmentCount, String segment, int offset, int count, int segmentHashCode)
  {
    if (segmentCount == 0)
    {
      return internArray(segment, offset, count, segmentHashCode);
    }
    else
    {
      stringArraySegmentsAndSubsegmentAccessUnit.setValue(hashCode, segments, segmentCount, segment, offset, count, segmentHashCode);
      return SegmentSequence.STRING_ARRAY_POOL.doIntern(false, stringArraySegmentsAndSubsegmentAccessUnit);
    }
  }

  /**
   * Creates an instance managed by this queue and pool.
   */
  protected StringAccessUnit(Queue queue, URIPool pool)
  {
    super(queue);
    this.pool = pool;
  }

  /**
   * Caches the parameters.
   */
  protected void setValue(String value)
  {
    this.value = value;
    this.hashCode = value.hashCode();
  }

  /**
   * Caches the parameters.
   */
  protected void setValue(String value, int hashCode)
  {
    this.value = value;
    this.hashCode = hashCode;
  }

  @Override
  protected boolean matches(URI value)
  {
    return value.matches(this.value);
  }

  @Override
  public URI match()
  {
    // If we fail to match, use getInternalizedValue to parse and cache an instance.
    //
    URI result = super.match();
    return result == null ? getInternalizedValue() : result;
  }

  @Override
  public URI getInternalizedValue()
  {
    return parseIntoURI(value);
  }

  /**
   * A string-parsing implementation.
   * This method creates instances in the pool as a side-effect.
   * Note that we never pass in a string with a fragment separator to this method.
   */
  protected URI parseIntoURI(String uri)
  {
    // The initial values for what we'll compute.
    //
    boolean hasExpectedHashCode = true;
    boolean isSchemeNormal = true;
    String scheme = null;
    String authority = null;
    String device = null;
    boolean absolutePath = false;
    String[] segments = URI.NO_SEGMENTS;
    int segmentsHashCode = 1;
    String query = null;
    boolean isArchiveScheme = false;
    boolean isPlatformScheme = false;

    // Look for the major separator, i.e., one of ":/?"
    //
    int length = uri.length();
    int i = 0;
    int j = findMajorSeparator(length, uri, i);

    // If we've found the scheme separator...
    //
    if (findTerminatingCharacter == URI.SCHEME_SEPARATOR)
    {
      // Look if the scheme's hash code matches one of the most likely schemes we expect to find...
      //
      int findHashCode = this.findHashCode;
      if (findHashCode == URI.SCHEME_PLATFORM_HASH_CODE)
      {
        scheme = URI.SCHEME_PLATFORM;
        isPlatformScheme = true;
      }
      else if (findHashCode == URI.SCHEME_FILE_HASH_CODE)
      {
        scheme = URI.SCHEME_FILE;
      }
      else if (findHashCode == URI.SCHEME_HTTP_HASH_CODE)
      {
        scheme = URI.SCHEME_HTTP;
      }
      else if (findHashCode == URI.SCHEME_JAR_HASH_CODE)
      {
        scheme = URI.SCHEME_JAR;
        isArchiveScheme = true;
      }
      else if (findHashCode == URI.SCHEME_ARCHIVE_HASH_CODE)
      {
        scheme = URI.SCHEME_ARCHIVE;
        isArchiveScheme = true;
      }
      else if (findHashCode == URI.SCHEME_ZIP_HASH_CODE)
      {
        scheme = URI.SCHEME_ZIP;
        isArchiveScheme = true;
      }

      // If it isn't one of the expected schemes, or it is, then we need to make sure it's really equal to what's in the URI, not an accidential hash code collision...
      //
      if (scheme == null || !scheme.regionMatches(0, uri, 0, j))
      {
        // Intern the provided version of the scheme.
        //
        String unnormalizedScheme = intern(uri, 0, j, findHashCode);

        // Intern the lower case version of the scheme.
        //
        stringAccessUnit.setValue(true, unnormalizedScheme);
        stringAccessUnit.add(unnormalizedScheme, stringPoolEntry);
        scheme = stringAccessUnit.match();
        stringAccessUnit.reset(false);

        // Determine if the provided version is in normal form, i.e., already lower cased.
        //
        isSchemeNormal = unnormalizedScheme.equals(scheme);

        // Check whether it's a different hash code; we'll need to compute the right hash code if we've lower cased the scheme.
        //
        hasExpectedHashCode = scheme.hashCode() == findHashCode;

        // Check if it's an archive scheme...
        //
        for (String archiveScheme : URI.ARCHIVE_SCHEMES)
        {
          if (scheme.equals(archiveScheme))
          {
            isArchiveScheme = true;
            break;
          }
        }

        isPlatformScheme = scheme.equals(URI.SCHEME_PLATFORM);
      }

      // Look for the end of the following segment.
      //
      i = j + 1;
      j = findSegmentEnd(length, uri, i);
    }

    if (isArchiveScheme)
    {
      // Look for the archive separator, which must be present.
      //
      j = uri.lastIndexOf(URI.ARCHIVE_SEPARATOR);
      if (j == -1)
      {
        throw new IllegalArgumentException("no archive separator");
      }

      // In that case it's an absolute path and the authority is everything up to and including the ! of the archive separator.
      //
      absolutePath = true;
      authority = intern(uri, i, ++j - i);

      // Look for the end of the following segment starting after the / in the archive separator.
      //
      i = j + 1;
      j = findSegmentEnd(length, uri, i);
    }
    else if (i == j && findTerminatingCharacter == URI.SEGMENT_SEPARATOR)
    {
      // If we're starting with a / so it's definitely hierarchical.
      // Look for the next segment end, and if we find a / as the next character...
      //
      j = findSegmentEnd(length, uri, ++i);
      if (j == i && findTerminatingCharacter == URI.SEGMENT_SEPARATOR)
      {
        // Look for the segment that follows; it's the authority, even if it's empty.
        //
        j = findSegmentEnd(length, uri, ++i);
        authority = intern(uri, i,  j - i, findHashCode);
        i = j;

        // If the authority is followed by a /...
        //
        if (findTerminatingCharacter == URI.SEGMENT_SEPARATOR)
        {
          // Then it's an absolute path so look for the end of the following segment.
          //
          absolutePath = true;
          j = findSegmentEnd(length, uri, ++i);
        }
      }
      else
      {
        // Because it started with a /, the current segment, which we'll capcture below, is the start of an absolute path.
        //
        absolutePath = true;
      }
    }
    else if (scheme != null)
    {
      // There's a scheme, but it's not followed immediately by a /, so it's an opaque URI.
      //
      authority = intern(uri, i, length - i);
      URI resultURI = pool.intern(false, URIComponentsAccessUnit.VALIDATE_NONE, false, scheme, authority, null, false, null, null);

      // If something tries to add an entry for this access unit, we'd better be sure that the hash code is that of the transformed URI.
      //
      this.hashCode = resultURI.hashCode();

      return resultURI;
    }

    // Start analyzing the first segment...
    //
    boolean segmentsRemain = false;
    int start = i;
    int len =  j - i;
    i = j;
    if (len == 0)
    {
      // If we found a /, then we have one single empty segment so far.
      //
      if (findTerminatingCharacter != URI.QUERY_SEPARATOR)
      {
        segments = URI.ONE_EMPTY_SEGMENT;
        segmentsHashCode = 31;

        // Look for the next segment. There is one even if it's empty.
        //
        j = findSegmentEnd(length, uri, ++i);
        segmentsRemain = true;
      }
    }
    // If this first segment ends with a : and we're not processing an archive URI, then treat it as the device...
    //
    else if (!isArchiveScheme && !isPlatformScheme && uri.charAt(j - 1) == URI.DEVICE_IDENTIFIER)
    {
      device = intern(uri, start, len, findHashCode);

      // If the device is at the end of the URI...
      //
      if (findTerminatingCharacter == URI.QUERY_SEPARATOR)
      {
        // Then there's no absolute path and no segments remain.
        //
        absolutePath = false;
      }
      else
      {
        // Look for the segment that follows.
        //
        j = findSegmentEnd(length, uri, ++i);

        // If it's empty, then we ignore it because the empty segment is implicit from this being an absolute path.
        // Or, if there is another /, then we have another segment to process.
        //
        segmentsRemain = i != j || findTerminatingCharacter == URI.SEGMENT_SEPARATOR;
      }
    }
    else
    {
      // Append the segment...
      //
      segments = internArray(uri, start, j - start, findHashCode);
      segmentsHashCode = 31 * segmentsHashCode + findHashCode;

      // If we're not already at the end...
      //
      if (findTerminatingCharacter != URI.QUERY_SEPARATOR)
      {
        // Find the end of the following segment, and indicate that we should process it.
        //
        j = findSegmentEnd(length, uri, ++i);
        segmentsRemain = true;
      }
    }

    // If we have more segments to process...
    //
    if (segmentsRemain)
    {
      for (;;)
      {
        // Append that segment...
        //
        segments = internArray(segmentsHashCode, segments, segments.length, uri, i, j - i, findHashCode);
        segmentsHashCode = 31 * segmentsHashCode + findHashCode;
        i = j;

        // If the current segment is terminated by a /...
        //
        if (findTerminatingCharacter == URI.SEGMENT_SEPARATOR)
        {
          // Find the end of the following segment.
          //
          j = findSegmentEnd(length, uri, ++i);
        }
        else
        {
          // Otherwise, we're done.
          //
          break;
        }
      }
    }

    // If we're not yet past the end of the string, what remains must be the query string.
    //
    if (i++ < length)  // implies uri.charAt(i) == QUERY_SEPARATOR
    {
      // Intern what's left to the end of the string.
      //
      query = intern(uri, i, length - i);
    }

    // If we're sure we have the right hash code (the scheme was not lower cased), we can use it, otherwise, we must compute a hash code.
    //
    URI resultURI;
    if (hasExpectedHashCode)
    {
      resultURI = pool.intern(true, true, scheme, authority, device, absolutePath, segments, query, hashCode);
    }
    else
    {
      resultURI = pool.intern(true, URIComponentsAccessUnit.VALIDATE_NONE, true, scheme, authority, device, absolutePath, segments, query);

      // If something tries to add an entry for this access unit, we'd better be sure that the hash code is that of the transformed URI.
      //
      this.hashCode = resultURI.hashCode();
    }

    if (isSchemeNormal)
    {
      resultURI.cacheString(uri);
    }

    return resultURI;
  }

  /**
   * Looks for a '/', ':', or '?', computing the {@link #findHashCode hash code} and {@link #findTerminatingCharacter setting the character} that terminated the scan.
   */
  protected int findMajorSeparator(int length, String s, int i)
  {
    findTerminatingCharacter = URI.QUERY_SEPARATOR;
    int hashCode = 0;
    for (; i < length; i++)
    {
      char c = s.charAt(i);
      if (c == URI.SEGMENT_SEPARATOR || c == URI.SCHEME_SEPARATOR || c == URI.QUERY_SEPARATOR)
      {
        findTerminatingCharacter = c;
        break;
      }
      hashCode = 31 * hashCode + c;
    }
    findHashCode = hashCode;
    return i;
  }

  /**
   * Looks for a '/', or '?', computing the {@link #findHashCode hash code} and {@link #findTerminatingCharacter setting the character} that terminated the scan.
   */
  protected int findSegmentEnd(int length, String s, int i)
  {
    findTerminatingCharacter = URI.QUERY_SEPARATOR;
    int hashCode = 0;
    for (; i < length; i++)
    {
      char c = s.charAt(i);
      if (c == URI.SEGMENT_SEPARATOR || c == URI.QUERY_SEPARATOR)
      {
        findTerminatingCharacter = c;
        break;
      }
      hashCode = 31 * hashCode + c;
    }
    findHashCode = hashCode;
    return i;
  }

  @Override
  public void reset(boolean isExclusive)
  {
    value = null;
    super.reset(isExclusive);
  }
}