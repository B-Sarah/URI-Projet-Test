package org.eclipse.emf.common.util;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
   * A subclass for representing a hierarchical URI.
   */
  final class Hierarchical extends URI
  {
    /**
     * The {@link #flags} bit for representing {@link URI#hasAbsolutePath()}.
     */
    protected static final int HAS_ABSOLUTE_PATH               = 1 << 0;

    /**
     * The {@link #flags} bit for representing {@link URI#hasRelativePath()}.
     */
    protected static final int HAS_RELATIVE_PATH               = 1 << 1;

    /**
     * The {@link #flags} bit for representing {@link URI#hasEmptyPath()}.
     */
    protected static final int HAS_EMPTY_PATH                  = 1 << 2;

    /**
     * The {@link #flags} bit for representing {@link URI#isCurrentDocumentReference()}.
     */
    protected static final int IS_CURRENT_DOCUMENT_REFERENCE   = 1 << 3;

    /**
     * The {@link #flags} bit for representing {@link URI#isFile()}.
     */
    protected static final int IS_FILE                         = 1 << 4;

    /**
     * The {@link #flags} bit for representing {@link URI#isPlatform()}.
     */
    protected static final int IS_PLATFORM                     = 1 << 5;

    /**
     * The {@link #flags} bit for representing {@link URI#isPlatformResource()}.
     */
    protected static final int IS_PLATFORM_RESOURCE            = 1 << 6;

    /**
     * The {@link #flags} bit for representing {@link URI#isPlatformPlugin()}.
     */
    protected static final int IS_PLATFORM_PLUGIN              = 1 << 7;

    /**
     * The {@link #flags} bit for representing {@link URI#isArchive()}.
     */
    protected static final int IS_ARCHIVE                      = 1 << 8;

    /**
     * The {@link #flags} bit for representing {@link URI#hasTrailingPathSeparator()}.
     */
    protected static final int HAS_TRAILING_PATH_SEPARATOR     = 1 << 9;

    /**
     * The {@link #flags} bit for representing {@link URI#isPrefix()}.
     */
    protected static final int IS_PREFIX                       = 1 << 10;

    /**
     * The {@link #flags} bits for representing {@link URI#hasPath()}.
     */
    protected static final int HAS_PATH = HAS_ABSOLUTE_PATH | HAS_RELATIVE_PATH;

    /**
     * Bit flags for the results of all the boolean no-argument methods.
     */
    protected final int flags;

    /**
     * The scheme of the hierarchical URIs.
     */
    protected final String scheme;  // null -> relative URI reference

    /**
     * The authority of the hierarchical URI.
     */
    protected final String authority;

    /**
     * The device of the hierarchical URI.
     */
    protected final String device;

    /**
     * The segments of the hierarchical URI.
     */
    protected final String[] segments; // empty last segment -> trailing separator

    /**
     * The query of the hierarchical URI.
     */
    protected final String query;

    /**
     * A weakly cached reference to the string representation.
     */
    protected WeakReference<String> toString;

    /**
     * Creates an instance from the components, computing the {@link #flags} bits.
     * Assertions are used to validate the integrity of the result.
     * I.e., all components must be interned and the hash code must be equal to the hash code of the {@link #toString()}.
     */
    protected Hierarchical(int hashCode, String scheme, String authority, String device, boolean absolutePath, String[] segments, String query)
    {
      super(hashCode);

      int flags = 0;
      if (absolutePath)
      {
        flags |= HAS_ABSOLUTE_PATH;
      }
      else if (device == null && authority == null)
      {
        flags |= HAS_RELATIVE_PATH;
        if (segments == NO_SEGMENTS)
        {
          flags |= HAS_EMPTY_PATH;
          if (query == null)
          {
            flags |= IS_CURRENT_DOCUMENT_REFERENCE;
          }
        }

        if (query == null)
        {
          flags |= IS_FILE;
        }
      }

      if (scheme != null)
      {
        if (scheme.equals(SCHEME_FILE))
        {
          flags |= IS_FILE;
        }
        else if (scheme.equals(SCHEME_PLATFORM))
        {
          if (authority == null  && device == null && segments.length >= 2)
          {
            flags |= IS_PLATFORM;
            String firstSegment = segments[0];
            if (firstSegment.equals(SEGMENT_RESOURCE))
            {
              flags |= IS_PLATFORM_RESOURCE;
            }
            else if (firstSegment.equals(SEGMENT_PLUGIN))
            {
              flags |= IS_PLATFORM_PLUGIN;
            }
          }
        }
        else
        {
          for (String archiveScheme : ARCHIVE_SCHEMES)
          {
            if (scheme.equals(archiveScheme))
            {
              flags |= IS_ARCHIVE;
              break;
            }
          }
        }
      }

      if (segments == NO_SEGMENTS)
      {
        if (absolutePath && query == null)
        {
          flags |= IS_PREFIX;
        }
      }
      else if (segments[segments.length - 1].equals(SEGMENT_EMPTY))
      {
        flags |= HAS_TRAILING_PATH_SEPARATOR;
        if (query == null)
        {
          flags |= IS_PREFIX;
        }
      }

      this.flags = flags;
      this.scheme = scheme;
      this.authority = authority;
      this.device = device;
      this.segments = segments;
      this.query = query;

      // The segments array must be interned.
      //
      assert segments == SegmentSequence.STRING_ARRAY_POOL.intern(true, true, segments, segments.length);

      // The scheme must be interned and must be lower cased.
      //
      assert scheme.equals(CommonUtil.internToLowerCase(scheme));

      // The authority must be interned.
      //
      assert authority.equals(CommonUtil.intern(authority));

      // The query must be interned.
      //
      assert query.equals(CommonUtil.intern(query));

      // The device must be interned.
      //
      assert device.equals(CommonUtil.intern(device));

      // The components must be valid.
      //
      assert validateURI(true, scheme, authority, device, hasAbsolutePath(), segments, query, null);

      // The hash code must be the same as that of the string representation
      //
      assert hashCode == toString().hashCode();
    }

    @Override
    public boolean isRelative()
    {
      return scheme == null;
    }

    @Override
    protected boolean isBase()
    {
      return scheme != null;
    }

    @Override
    public boolean isHierarchical()
    {
      return true;
    }

    @Override
    public boolean hasAuthority()
    {
      return authority != null;
    }

    @Override
    public boolean hasDevice()
    {
      return device != null;
    }

    @Override
    public boolean hasPath()
    {
      // note: (absolutePath || authority == null) -> hierarchical
      // (authority == null && device == null && !absolutePath) -> scheme == null
      return (flags & HAS_PATH) != 0;
    }

    @Override
    protected boolean hasDeviceOrPath()
    {
      return (flags & HAS_PATH) != 0 || device != null;
    }

    @Override
    public boolean hasAbsolutePath()
    {
      // note: absolutePath -> hierarchical
      return (flags & HAS_ABSOLUTE_PATH) != 0;
    }

    @Override
    public boolean hasRelativePath()
    {
      // note: authority == null -> hierarchical
      // (authority == null && device == null && !absolutePath) -> scheme == null
      return (flags & HAS_RELATIVE_PATH) != 0;
    }

    @Override
    public boolean hasEmptyPath()
    {
      // note: authority == null -> hierarchical
      // (authority == null && device == null && !absolutePath) -> scheme == null
      return (flags & HAS_EMPTY_PATH) != 0;
    }

    @Override
    public boolean hasQuery()
    {
      // note: query != null -> hierarchical
      return query != null;
    }

    @Override
    public boolean isCurrentDocumentReference()
    {
      // note: authority == null -> hierarchical
      // (authority == null && device == null && !absolutePath) -> scheme == null
      return (flags & IS_CURRENT_DOCUMENT_REFERENCE) != 0;
    }

    @Override
    public boolean isEmpty()
    {
      // note: authority == null -> hierarchical
      // (authority == null && device == null && !absolutePath) -> scheme == null
      return (flags & IS_CURRENT_DOCUMENT_REFERENCE) != 0;
    }

    @Override
    public boolean isFile()
    {
      return (flags & IS_FILE) != 0;
    }

    @Override
    public boolean isPlatform()
    {
      return (flags & IS_PLATFORM) != 0;
    }

    @Override
    public boolean isPlatformResource()
    {
      return (flags & IS_PLATFORM_RESOURCE) != 0;
    }

    @Override
    public boolean isPlatformPlugin()
    {
      return (flags & IS_PLATFORM_PLUGIN) != 0;
    }

    @Override
    public boolean isArchive()
    {
      return (flags & IS_ARCHIVE) != 0;
    }

    @Override
    protected boolean segmentsEqual(URI uri)
    {
      String[] segments = this.segments;
      int length = segments.length;
      if (length != uri.segmentCount()) return false;
      for (int i = 0; i < length; i++)
      {
        if (!(segments[i]).equals(uri.segment(i))) return false;
      }
      return true;
    }

    @Override
    public String scheme()
    {
      return scheme;
    }

    @Override
    public String authority()
    {
      return authority;
    }

    @Override
    public String userInfo()
    {
      if (!hasAuthority()) return null;

      int i = authority.indexOf(USER_INFO_SEPARATOR);
      return i < 0 ? null : authority.substring(0, i);
    }

    @Override
    public String host()
    {
      if (!hasAuthority()) return null;

      int i = authority.indexOf(USER_INFO_SEPARATOR);
      int j = authority.indexOf(PORT_SEPARATOR, i);
      return j < 0 ? authority.substring(i + 1) : authority.substring(i + 1, j);
    }

    @Override
    public String port()
    {
      if (!hasAuthority()) return null;

      int i = authority.indexOf(USER_INFO_SEPARATOR);
      int j = authority.indexOf(PORT_SEPARATOR, i);
      return j < 0 ? null : authority.substring(j + 1);
    }

    @Override
    public String device()
    {
      return device;
    }

    @Override
    public String[] segments()
    {
      return segments.clone();
    }

    @Override
    protected String[] rawSegments()
    {
      return segments;
    }

    @Override
    public List<String> segmentsList()
    {
      return Collections.unmodifiableList(Arrays.asList(segments));
    }

    @Override
    public int segmentCount()
    {
      return segments.length;
    }

    @Override
    public String segment(int i)
    {
      return segments[i];
    }

    @Override
    public String lastSegment()
    {
      int len = segments.length;
      if (len == 0) return null;
      return segments[len - 1];
    }

    @Override
    public String path()
    {
      if (!hasPath()) return null;

      CommonUtil.StringPool.StringsAccessUnit result = CommonUtil.STRING_POOL.getStringBuilder();
      if (hasAbsolutePath()) result.append(SEGMENT_SEPARATOR);

      String[] segments = this.segments;
      for (int i = 0, len = segments.length; i < len; i++)
      {
        if (i != 0) result.append(SEGMENT_SEPARATOR);
        result.append(segments[i]);
      }
      return CommonUtil.STRING_POOL.intern(result);
    }

    @Override
    public String devicePath()
    {
      if (!hasPath()) return null;

      CommonUtil.StringPool.StringsAccessUnit result = CommonUtil.STRING_POOL.getStringBuilder();

      if (hasAuthority())
      {
        if (!isArchive()) result.append(AUTHORITY_SEPARATOR);
        result.append(authority);

        if (hasDevice()) result.append(SEGMENT_SEPARATOR);
      }

      if (hasDevice()) result.append(device);
      if (hasAbsolutePath()) result.append(SEGMENT_SEPARATOR);

      String[] segments = this.segments;
      for (int i = 0, len = segments.length; i < len; i++)
      {
        if (i != 0) result.append(SEGMENT_SEPARATOR);
        result.append(segments[i]);
      }
      return CommonUtil.STRING_POOL.intern(result);
    }

    @Override
    public String query()
    {
      return query;
    }

    @Override
    public URI appendQuery(String query)
    {
      return POOL.intern(false, URIComponentsAccessUnit.VALIDATE_QUERY, true, scheme, authority, device, hasAbsolutePath(), segments, query);
    }

    @Override
    public URI trimQuery()
    {
      if (query == null)
      {
        return this;
      }
      else
      {
        return POOL.intern(false, URIComponentsAccessUnit.VALIDATE_NONE, true, scheme, authority, device, hasAbsolutePath(), segments, null);
      }
    }

    @Override
    public URI resolve(URI base, boolean preserveRootParents)
    {
      if (!base.isBase())
      {
        throw new IllegalArgumentException("resolve against non-hierarchical or relative base");
      }

      // an absolute URI needs no resolving
      if (!isRelative()) return this;

      String newAuthority = authority;
      String newDevice = device;
      boolean newAbsolutePath = hasAbsolutePath();
      String[] newSegments = segments;
      String newQuery = query;

      // note: it's okay for two URIs to share a segments array, since neither will ever modify it

      if (authority == null)
      {
        // no authority: use base's
        newAuthority = base.authority();

        if (device == null)
        {
          // no device: use base's
          newDevice = base.device();

          if (hasEmptyPath() && query == null)
          {
            // current document reference: use base path and query
            newAbsolutePath = base.hasAbsolutePath();
            newSegments = base.rawSegments();
            newQuery = base.query();
          }
          else if (hasRelativePath())
          {
            // relative path: merge with base and keep query.
            // note that if the base has no path and this a non-empty relative path, there is an implied root in the resulting path
            newAbsolutePath = base.hasAbsolutePath() || !hasEmptyPath();
            newSegments = newAbsolutePath ? mergePath(base, preserveRootParents) : NO_SEGMENTS;
          }
          // else absolute path: keep it and query
        }
        // else keep device, path, and query
      }
      // else keep authority, device, path, and query

      // Use scheme from base; no validation needed because all components are from existing URIs
      return POOL.intern(false, URIComponentsAccessUnit.VALIDATE_NONE, true, base.scheme(), newAuthority, newDevice, newAbsolutePath, newSegments, newQuery);
    }

    // Merges this URI's relative path with the base non-relative path.
    // If base has no path, treat it as the root absolute path, unless this has  no path either.
    //
    protected String[] mergePath(URI base, boolean preserveRootParents)
    {
      if (base.hasRelativePath())
      {
        throw new IllegalArgumentException("merge against relative path");
      }
      if (!hasRelativePath())
      {
        throw new IllegalStateException("merge non-relative path");
      }

      int baseSegmentCount = base.segmentCount();
      int segmentCount = segments.length;
      String[] stack = new String[baseSegmentCount + segmentCount];
      int sp = 0;

      // use a stack to accumulate segments of base, except for the last
      // (i.e. skip trailing separator and anything following it), and of relative path
      //
      for (int i = 0; i < baseSegmentCount - 1; i++)
      {
        sp = accumulate(stack, sp, base.segment(i), preserveRootParents);
      }

      for (int i = 0; i < segmentCount; i++)
      {
        sp = accumulate(stack, sp, segments[i], preserveRootParents);
      }

      // if the relative path is empty or ends in an empty segment, a parent
      // reference, or a self reference, add a trailing separator to a
      // non-empty path
      if (sp > 0)
      {
        if (segmentCount == 0)
        {
          stack[sp++] = SEGMENT_EMPTY;
        }
        else
        {
          String segment = segments[segmentCount - 1];
          if (segment.equals(SEGMENT_EMPTY)  || segment.equals(SEGMENT_PARENT) || segment.equals(SEGMENT_SELF))
          {
            stack[sp++] = SEGMENT_EMPTY;
          }
        }
      }

      // return a correctly sized result
      return SegmentSequence.STRING_ARRAY_POOL.intern(stack, 0, sp);
    }

    // Adds a segment to a stack, skipping empty segments and self references,
    // and interpreting parent references.
    protected static int accumulate(String[] stack, int sp, String segment, boolean preserveRootParents)
    {
      if (SEGMENT_PARENT == segment)
      {
        if (sp == 0)
        {
          // special care must be taken for a root's parent reference: it is
          // either ignored or the symbolic reference itself is pushed
          if (preserveRootParents) stack[sp++] = segment;
        }
        else
        {
          // unless we're already accumulating root parent references,
          // parent references simply pop the last segment descended
          if (SEGMENT_PARENT == stack[sp - 1]) stack[sp++] = segment;
          else sp--;
        }
      }
      else if (!SEGMENT_EMPTY.equals(segment) && !SEGMENT_SELF.equals(segment))
      {
        // skip empty segments and self references; push everything else
        stack[sp++] = segment;
      }
      return sp;
    }

    @Override
    public URI deresolve(URI base, boolean preserveRootParents, boolean anyRelPath, boolean shorterRelPath)
    {
      if (!base.isBase() || isRelative()) return this;

      // note: these assertions imply that neither this nor the base URI has a
      // relative path; thus, both have either an absolute path or no path

      // different scheme: need complete, absolute URI
      if (scheme != base.scheme()) return this;

      String newAuthority = authority;
      String newDevice = device;
      boolean newAbsolutePath = hasAbsolutePath();
      String[] newSegments = segments;
      String newQuery = query;

      if (authority == base.authority() && (hasDeviceOrPath() || !base.hasDeviceOrPath()))
      {
        // matching authorities and no device or path removal
        newAuthority = null;

        if (device == base.device())
        {
          boolean hasPath = hasPath();
          boolean baseHasPath = base.hasPath();
          if (hasPath || !baseHasPath)
          {
            // matching devices and no path removal
            newDevice = null;

            // exception if (!hasPath() && base.hasPath())
            /*
            if (!anyRelPath && !shorterRelPath)
            {
              // user rejects a relative path: keep absolute or no path
            	
            }
            else*/
            if (hasPath == baseHasPath && segmentsEqual(base) && query == base.query())
            {
              // current document reference: keep no path or query
              newAbsolutePath = false;
              newSegments = NO_SEGMENTS;
              newQuery = null;
            }
            else if (hasPath && !baseHasPath)
            {
              // no paths: keep query only
              newAbsolutePath = false;
              newSegments = NO_SEGMENTS;
            }
            // exception if (!hasAbsolutePath())
            
            /*
            else if (hasCollapsableSegments(preserveRootParents))
            {
              // path form demands an absolute path: keep it and query
            }
            */
            
            else
            {
              // keep query and select relative or absolute path based on length
              String[] rel = findRelativePath(base, preserveRootParents);
              if (anyRelPath || segments.length > rel.length)
              {
                // user demands a relative path or the absolute path is longer
                newAbsolutePath = false;
                newSegments = rel;
              }
              // else keep shorter absolute path
            }
          }
        }
        // else keep device, path, and query
      }
      // else keep authority, device, path, and query

      // always include fragment, even if null;
      // no validation needed since all components are from existing URIs
      return POOL.intern(false, URIComponentsAccessUnit.VALIDATE_NONE, true, null, newAuthority, newDevice, newAbsolutePath, newSegments, newQuery);
    }

    // Returns true if the non-relative path includes segments that would be
    // collapsed when resolving; false otherwise.  If preserveRootParents is
    // true, collapsible segments include any empty segments, except for the
    // last segment, as well as and parent and self references.  If
    // preserveRootsParents is false, parent references are not collapsible if
    // they are the first segment or preceded only by other parent
    // references.
    protected boolean hasCollapsableSegments(boolean preserveRootParents)
    {
      if (hasRelativePath())
      {
        throw new IllegalStateException("test collapsability of relative path");
      }

      String[] segments = this.segments;
      for (int i = 0, len = segments.length; i < len; i++)
      {
        String segment = segments[i];
        if (i < len - 1 && SEGMENT_EMPTY.equals(segment) ||
              SEGMENT_SELF.equals(segment) ||
              SEGMENT_PARENT.equals(segment) && (!preserveRootParents || i != 0 && !SEGMENT_PARENT.equals(segments[i - 1])))
        {
          return true;
        }
      }
      return false;
    }

    // Returns the shortest relative path between the the non-relative path of
    // the given base and this absolute path.  If the base has no path, it is
    // treated as the root absolute path.
    protected String[] findRelativePath(URI base, boolean preserveRootParents)
    {
      if (base.hasRelativePath())
      {
        throw new IllegalArgumentException(
          "find relative path against base with relative path");
      }
      if (!hasAbsolutePath())
      {
        throw new IllegalArgumentException(
          "find relative path of non-absolute path");
      }

      // treat an empty base path as the root absolute path
      String[] startPath = base.collapseSegments(preserveRootParents);
      String[] endPath = segments;

      // drop last segment from base, as in resolving
      int startCount = startPath.length > 0 ? startPath.length - 1 : 0;
      int endCount = endPath.length;

      // index of first segment that is different between endPath and startPath
      int diff = 0;

      // if endPath is shorter than startPath, the last segment of endPath may
      // not be compared: because startPath has been collapsed and had its
      // last segment removed, all preceding segments can be considered non-
      // empty and followed by a separator, while the last segment of endPath
      // will either be non-empty and not followed by a separator, or just empty
      for (int count = startCount < endCount ? startCount : endCount - 1; diff < count && startPath[diff].equals(endPath[diff]); diff++)
      {
        // Empty statement.
      }

      int upCount = startCount - diff;
      int downCount = endCount - diff;

      // a single separator, possibly preceded by some parent reference
      // segments, is redundant
      if (downCount == 1 && SEGMENT_EMPTY.equals(endPath[endCount - 1]))
      {
        downCount = 0;
      }

      // an empty path needs to be replaced by a single "." if there is no
      // query, to distinguish it from a current document reference
      int length = upCount + downCount;
      if (length == 0)
      {
        if (query == null) return ONE_SELF_SEGMENT;
        return NO_SEGMENTS;
      }

      // return a correctly sized result
      String[] result = new String[length];
      Arrays.fill(result, 0, upCount, SEGMENT_PARENT);
      System.arraycopy(endPath, diff, result, upCount, downCount);
      return SegmentSequence.STRING_ARRAY_POOL.intern(false, false, result, length);
    }

    @Override
    protected String[] collapseSegments(boolean preserveRootParents)
    {
      if (hasRelativePath())
      {
        throw new IllegalStateException("collapse relative path");
      }

      if (!hasCollapsableSegments(preserveRootParents)) return rawSegments();

      // use a stack to accumulate segments
      String[] segments = this.segments;
      int segmentCount = segments.length;
      String[] stack = new String[segmentCount];
      int sp = 0;

      for (int i = 0; i < segmentCount; i++)
      {
        sp = accumulate(stack, sp, segments[i], preserveRootParents);
      }

      // if the path is non-empty and originally ended in an empty segment, a
      // parent reference, or a self reference, add a trailing separator
      if (sp > 0)
      {
        String segment = segments[segmentCount - 1];
        if (segment.equals(SEGMENT_EMPTY) || segment.equals(SEGMENT_PARENT)|| segment.equals(SEGMENT_SELF))
        {
          stack[sp++] = SEGMENT_EMPTY;
        }
      }

      // return a correctly sized result
      return SegmentSequence.STRING_ARRAY_POOL.intern(stack, 0, sp);
    }

    @Override
    protected void cacheString(String string)
    {
      toString = POOL.newCachedToString(this, string);
    }

    @Override
    protected void flushCachedString()
    {
      toString = null;
    }

    @Override
    protected String getCachedString()
    {
      WeakReference<String> toString = this.toString;
      if (toString != null)
      {
        String result = toString.get();
        if (result == null)
        {
          toString.clear();
        }
        else
        {
          return result;
        }
      }
      return null;
    }

    @Override
    public String toString()
    {
      String cachedToString = getCachedString();
      if (cachedToString != null)
      {
        return cachedToString;
      }

      CommonUtil.StringPool.StringsAccessUnit result = CommonUtil.STRING_POOL.getStringBuilder();

      if (!isRelative())
      {
        result.append(scheme);
        result.append(SCHEME_SEPARATOR);
      }

      if (hasAuthority())
      {
        if (!isArchive()) result.append(AUTHORITY_SEPARATOR);
        result.append(authority);
      }

      if (hasDevice())
      {
        result.append(SEGMENT_SEPARATOR);
        result.append(device);
      }

      if (hasAbsolutePath()) result.append(SEGMENT_SEPARATOR);

      for (int i = 0, len = segments.length; i < len; i++)
      {
        if (i != 0) result.append(SEGMENT_SEPARATOR);
        result.append(segments[i]);
      }

      if (hasQuery())
      {
        result.append(QUERY_SEPARATOR);
        result.append(query);
      }

      String string = CommonUtil.STRING_POOL.intern(result);
      this.toString = POOL.newCachedToString(this, string);
      return string;
    }

    @Override
    protected boolean matches(String string)
    {
      String cachedString = getCachedString();
      if (cachedString != null)
      {
        return cachedString.equals(string);
      }

      int length = string.length();

      int index = 0;
      if (!isRelative())
      {
        if (!string.startsWith(scheme))
        {
          return false;
        }
        index += scheme.length();
        if (index >= length || string.charAt(index) != SCHEME_SEPARATOR)
        {
          return false;
        }
        ++index;
      }

      if (hasAuthority())
      {
        if (!isArchive())
        {
          if (!string.startsWith(AUTHORITY_SEPARATOR, index))
          {
            return false;
          }
          index += 2;
        }
        if (!string.startsWith(authority, index))
        {
          return false;
        }
        index += authority.length();
      }

      if (hasDevice())
      {
        if (index >= length || string.charAt(index) != SEGMENT_SEPARATOR)
        {
          return false;
        }
        ++index;
        if (!string.startsWith(device, index))
        {
          return false;
        }
        index += device.length();
      }

      if (hasAbsolutePath())
      {
        if (index >= length || string.charAt(index) != SEGMENT_SEPARATOR)
        {
          return false;
        }
        ++index;
      }

      String[] segments = this.segments;
      for (int i = 0, len = segments.length; i < len; i++)
      {
        if (i != 0)
        {
          if (index >= length || string.charAt(index) != SEGMENT_SEPARATOR)
          {
            return false;
          }
          ++index;
        }
        String segment = segments[i];
        if (!string.startsWith(segment, index))
        {
          return false;
        }
        index += segment.length();
      }

      if (hasQuery())
      {
        if (index >= length || string.charAt(index) != QUERY_SEPARATOR)
        {
          return false;
        }
        ++index;
        if (!string.startsWith(query, index))
        {
          return false;
        }
        index += query.length();
      }

      return index == length;
    }

    @Override
    protected boolean matches(int validate, boolean hierarchical, String scheme, String authority, String device, boolean absolutePath, String[] segments, String query)
    {
      return
        hierarchical &&
          hasAbsolutePath() == absolutePath &&
          validate >= URIComponentsAccessUnit.VALIDATE_NONE ?
             this.segments == segments && this.scheme.equals(scheme) && this.authority.equals(authority) && this.device.equals(device) && this.query.equals(query) :
             Arrays.equals(this.segments, segments) && equals(this.scheme, scheme) && equals(this.authority, authority) && equals(this.device, device) && equals(this.query, query);
    }

    @Override
    protected boolean matches(String base, String path)
    {
      if (!isPlatform() || !(segments[0]).equals(base))
      {
        return false;
      }

      String[] segments = this.segments;
      int length = path.length();
      for (int i = 1, len = segments.length, index = 1; i < len; i++)
      {
        if (i != 1)
        {
          if (index >= length || path.charAt(index) != SEGMENT_SEPARATOR)
          {
            return false;
          }
          ++index;
        }
        String segment = segments[i];
        if (!path.startsWith(segment, index))
        {
          return false;
        }
        index += segment.length();
      }
      return true;
    }

    @Override
    public String toFileString()
    {
      if (!isFile()) return null;

      CommonUtil.StringPool.StringsAccessUnit result = CommonUtil.STRING_POOL.getStringBuilder();
      char separator = File.separatorChar;
      boolean hasDevice = hasDevice();

      if (hasAuthority())
      {
        result.append("//");
        result.append(authority);

        if (hasDevice) result.append(separator);
      }

      if (hasDevice) result.append(device);
      if (hasAbsolutePath()) result.append(separator);

      String[] segments = this.segments;
      for (int i = 0, len = segments.length; i < len; i++)
      {
        if (i != 0) result.append(separator);
        result.append(segments[i]);
      }

      return decode(CommonUtil.STRING_POOL.intern(result));
    }

    @Override
    public String toPlatformString(boolean decode)
    {
      if (isPlatform())
      {
        CommonUtil.StringPool.StringsAccessUnit result = CommonUtil.STRING_POOL.getStringBuilder();
        String[] segments = this.segments;
        for (int i = 1, len = segments.length; i < len; i++)
        {
          result.append('/');
          result.append(decode ? URI.decode(segments[i]) : segments[i]);
        }
        return CommonUtil.STRING_POOL.intern(result);
      }
      return null;
    }

    @Override
    public URI appendSegment(String segment)
    {
      // Do preliminary checking now but full checking later.
      //
      if (segment == null)
      {
        throw new IllegalArgumentException("invalid segment: null");
      }
      
      boolean isEmptySegment = segment.length() == 0;
      if (isEmptySegment && scheme != null && authority == null && device == null && segments.length == 0)
      {
        // Ignore appending an empty segment that would turn scheme:/ into schema:// because that would look like an empty authority without segments.
        return this;
      }

      // absolute path or no path -> absolute path
      boolean newAbsolutePath = !hasRelativePath();
      
      String[] newSegments;
      String newDevice = device;
      
      if (isEmptySegment && segments.length == 0)
      {
        // Turn it into trailing separator.
        newAbsolutePath = true;
        newSegments = NO_SEGMENTS;
      }
      else if (device == null && segments.length == 0 && !isEmptySegment && segment.charAt(segment.length() - 1)== DEVICE_IDENTIFIER)
      {
        // Capture the first segment as the device.
        newDevice = CommonUtil.intern(segment);
        newSegments = NO_SEGMENTS;
        newAbsolutePath = false;
      }
      else
      {
        // Really append the segment.
        newSegments = SegmentSequence.STRING_ARRAY_POOL.intern(segments, segments.length, segment, true);
      }

      return POOL.intern(false, segments.length, true, scheme, authority, newDevice, newAbsolutePath, newSegments, query);
    }

    @Override
    public URI appendSegments(String[] segments)
    {
      // Do preliminary checking now but full checking later.
      //
      if (segments == null)
      {
        throw new IllegalArgumentException("invalid segments: null");
      }
      else if (segments.length == 1)
      {
        return appendSegment(segments[0]);
      }
      else
      {
        if (device == null && authority == null && this.segments.length == 0)
        {
          String[] newSegments = SegmentSequence.STRING_ARRAY_POOL.intern(segments, 1, segments.length - 1);
          return appendSegment(segments[0]).appendSegments(newSegments);
        }
        
        for (String segment : segments)
        {
          if (segment == null)
          {
            throw new IllegalArgumentException("invalid segment: null");
          }
        }
      }

      // absolute path or no path -> absolute path
      boolean newAbsolutePath = !hasRelativePath();

      String[] newSegments = SegmentSequence.STRING_ARRAY_POOL.intern(this.segments, segments, true);
      return POOL.intern(false, this.segments.length, true, scheme, authority, device, newAbsolutePath, newSegments, query);
    }

    @Override
    public URI trimSegments(int i)
    {
      if (i < 1) return this;

      String[] newSegments;
      int len = segments.length - i;
      if (len > 0)
      {
        newSegments = SegmentSequence.STRING_ARRAY_POOL.intern(segments, 0, len);
      }
      else
      {
        newSegments = NO_SEGMENTS;
      }

      return POOL.intern(false, URIComponentsAccessUnit.VALIDATE_NONE, true, scheme, authority, device, hasAbsolutePath(), newSegments, query);
    }

    @Override
    public boolean hasTrailingPathSeparator()
    {
      return (flags & HAS_TRAILING_PATH_SEPARATOR) != 0;
    }

    @Override
    public String fileExtension()
    {
      int len = segments.length;
      if (len == 0) return null;

      String lastSegment = segments[len - 1];
      int i = lastSegment.lastIndexOf(FILE_EXTENSION_SEPARATOR);
      return i < 0 ? null : lastSegment.substring(i + 1);
    }

    @Override
    public URI appendFileExtension(String fileExtension)
    {
      // Do preliminary checking now and full validation later.
      if (fileExtension == null)
      {
        throw new IllegalArgumentException("invalid segment portion: null");
      }

      int len = segments.length;
      if (len == 0)
      {
        if (!validSegment(fileExtension))
        {
          throw new IllegalArgumentException("invalid segment portion: " + fileExtension);
        }
        return this;
      }

      String lastSegment = segments[len - 1];
      if (SEGMENT_EMPTY.equals(lastSegment))
      {
        if (!validSegment(fileExtension))
        {
          throw new IllegalArgumentException("invalid segment portion: " + fileExtension);
        }
        return this;
      }

      CommonUtil.StringPool.StringsAccessUnit newLastSegment = CommonUtil.STRING_POOL.getStringBuilder();
      newLastSegment.append(lastSegment);
      newLastSegment.append(FILE_EXTENSION_SEPARATOR);
      newLastSegment.append(fileExtension);

      String[] newSegments = SegmentSequence.STRING_ARRAY_POOL.intern(segments, segments.length - 1, CommonUtil.STRING_POOL.intern(newLastSegment), false);

      // note: segments.length > 0 -> hierarchical
      return POOL.intern(false, len, true, scheme, authority, device, hasAbsolutePath(), newSegments, query);
    }

    @Override
    public URI trimFileExtension()
    {
      int len = segments.length;
      if (len == 0) return this;

      String lastSegment = segments[len - 1];
      int i = lastSegment.lastIndexOf(FILE_EXTENSION_SEPARATOR);
      if (i < 0) return this;

      String newLastSegment = lastSegment.substring(0, i);
      String[] newSegments = SegmentSequence.STRING_ARRAY_POOL.intern(segments, len - 1, newLastSegment, true);

      // note: segments.length > 0 -> hierarchical
      return POOL.intern(false, URIComponentsAccessUnit.VALIDATE_NONE, true, scheme, authority, device, hasAbsolutePath(), newSegments, query);
    }

    @Override
    public boolean isPrefix()
    {
      return (flags & IS_PREFIX) != 0;
    }

    @Override
    public URI replacePrefix(URI oldPrefix, URI newPrefix)
    {
      if (!oldPrefix.isPrefix() || !newPrefix.isPrefix())
      {
        String which = oldPrefix.isPrefix() ? "new" : "old";
        throw new IllegalArgumentException("non-prefix " + which + " value");
      }

      // Don't even consider it unless this is hierarchical and has scheme,
      // authority, device and path absoluteness equal to those of the prefix.
      if (scheme != oldPrefix.scheme() || authority != oldPrefix.authority() || device != oldPrefix.device() || hasAbsolutePath() != oldPrefix.hasAbsolutePath())
      {
        return null;
      }

      String[] segments = this.segments;
      int segmentsLength = segments.length;

      // If the prefix has no segments, then it is the root absolute path, and
      // we know this is an absolute path, too.
      // Get what's left of the segments after trimming the prefix.
      String [] oldPrefixSegments = oldPrefix.rawSegments();
      int oldPrefixSegmentCount = oldPrefixSegments.length;
      int tailSegmentCount;
      if (oldPrefixSegmentCount == 0)
      {
        tailSegmentCount = segmentsLength;
      }
      else
      {
        // This must have no fewer segments than the prefix.  Since the prefix
        // is not the root absolute path, its last segment is empty; all others
        // must match.
        int i = 0;
        int segmentsToCompare = oldPrefixSegmentCount - 1;
        if (segmentsLength <= segmentsToCompare) return null;

        for (; i < segmentsToCompare; i++)
        {
          if (segments[i] != oldPrefixSegments[i]) return null;
        }

        // The prefix really is a prefix of this.  If this has just one more,
        // empty segment, the paths are the same.
        if (i == segmentsLength - 1 && segments[i].equals(SEGMENT_EMPTY))
        {
          return newPrefix;
        }
        else
        {
          tailSegmentCount = segmentsLength - i;
        }
      }

      // If the new prefix has segments, it is not the root absolute path,
      // and we need to drop the trailing empty segment and append the tail
      // segments.
      String [] newPrefixSegments = newPrefix.rawSegments();
      int newPrefixSegmentCount = newPrefixSegments.length;
      String[] mergedSegments;
      if (newPrefixSegmentCount == 0)
      {
        mergedSegments = tailSegmentCount == segmentsLength ? segments : SegmentSequence.STRING_ARRAY_POOL.intern(segments, segmentsLength - tailSegmentCount, tailSegmentCount);
      }
      else
      {
        mergedSegments = SegmentSequence.STRING_ARRAY_POOL.intern(newPrefixSegments, 0, newPrefixSegmentCount - 1, segments, segmentsLength - tailSegmentCount, tailSegmentCount);
      }

      // no validation needed since all components are from existing URIs
      return POOL.intern(false, URIComponentsAccessUnit.VALIDATE_NONE, true, newPrefix.scheme(), newPrefix.authority(), newPrefix.device(), newPrefix.hasAbsolutePath(), mergedSegments, query);
    }
  }