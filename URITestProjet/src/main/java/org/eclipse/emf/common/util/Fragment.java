package org.eclipse.emf.common.util;

import java.util.List;

/**
   * A subclass for representing a URI with a fragment.
   * Most methods simply delegate to the {@link #trimFragment() base} URI.
   */
  final class Fragment extends URI
  {
    /**
     * The {@link #trimFragment() base} URI.
     */
    protected final URI uri;

    /**
     * The representation of the fragment.
     * The fragment is {@link #splitInternFragment(String) split interned}.
     */
    protected CharSequence fragment;

    /**
     * Creates an instance from the components.
     * Assertions are used to validate the integrity of the result.
     * I.e., the fragment must be non-null and {@link #splitInternFragment(String) split interned} and the hash code must be equal to the hash code of the {@link #toString()}.
     */
    protected Fragment(int hashCode, URI uri, CharSequence fragment)
    {
      super(hashCode);

      this.uri = uri;
      this.fragment = fragment;

      // There must be a fragment.
      //
      assert fragment != null;

      // The hash code must be the same as that of the string representation,
      // unless it's deferred.
      //
      assert hashCode == 0 || hashCode == toString().hashCode();
    }

    @Override
    public boolean isRelative()
    {
      return uri.isRelative();
    }

    @Override
    protected boolean isBase()
    {
      return uri.isBase();
    }

    @Override
    public boolean isHierarchical()
    {
      return uri.isHierarchical();
    }

    @Override
    public boolean hasAuthority()
    {
      return uri.hasAuthority();
    }

    @Override
    public boolean hasOpaquePart()
    {
      return uri.hasOpaquePart();
    }

    @Override
    public boolean hasDevice()
    {
      return uri.hasDevice();
    }

    @Override
    public boolean hasPath()
    {
      return uri.hasPath();
    }

    @Override
    protected boolean hasDeviceOrPath()
    {
      return uri.hasDeviceOrPath();
    }

    @Override
    public boolean hasAbsolutePath()
    {
      return uri.hasAbsolutePath();
    }

    @Override
    public boolean hasRelativePath()
    {
      return uri.hasRelativePath();
    }

    @Override
    public boolean hasEmptyPath()
    {
      return uri.hasEmptyPath();
    }

    @Override
    public boolean hasQuery()
    {
      return uri.hasQuery();
    }

    @Override
    public boolean hasFragment()
    {
      return true;
    }

    @Override
    public boolean isCurrentDocumentReference()
    {
      return uri.isCurrentDocumentReference();
    }

    @Override
    public boolean isEmpty()
    {
      return false;
    }

    @Override
    public boolean isFile()
    {
      return uri.isFile();
    }

    @Override
    public boolean isPlatform()
    {
      return uri.isPlatform();
    }

    @Override
    public boolean isPlatformResource()
    {
      return uri.isPlatformResource();
    }

    @Override
    public boolean isPlatformPlugin()
    {
      return uri.isPlatformPlugin();
    }

    @Override
    public boolean isArchive()
    {
      return uri.isArchive();
    }

    @Override
    protected boolean segmentsEqual(URI uri)
    {
      return uri.segmentsEqual(uri);
    }

    @Override
    public String scheme()
    {
      return uri.scheme();
    }

    @Override
    public String opaquePart()
    {
      return uri.opaquePart();
    }

    @Override
    public String authority()
    {
      return uri.authority();
    }

    @Override
    public String userInfo()
    {
      return uri.userInfo();
    }

    @Override
    public String host()
    {
      return uri.host();
    }

    @Override
    public String port()
    {
      return uri.port();
    }

    @Override
    public String device()
    {
      return uri.device();
    }

    @Override
    public String[] segments()
    {
      return uri.segments();
    }

    @Override
    protected String[] rawSegments()
    {
      return uri.rawSegments();
    }

    @Override
    public List<String> segmentsList()
    {
      return uri.segmentsList();
    }

    @Override
    public int segmentCount()
    {
      return uri.segmentCount();
    }

    @Override
    public String segment(int i)
    {
      return uri.segment(i);
    }

    @Override
    public String lastSegment()
    {
      return uri.lastSegment();
    }

    @Override
    public String path()
    {
      return uri.path();
    }

    @Override
    public String devicePath()
    {
      return uri.devicePath();
    }

    @Override
    public String query()
    {
      return uri.query();
    }

    private URI appendFragment(URI uri)
    {
      // If the hash code is 0 then it's highly likely we've deferred split interning the fragment, so don't use rawAppendFragment in that case.
      //
      return hashCode == 0 ? uri.appendFragment(fragment.toString()) : uri.rawAppendFragment(fragment);
    }

    @Override
    public URI appendQuery(String query)
    {
      return appendFragment(uri.appendQuery(query));
    }

    @Override
    public URI trimQuery()
    {
      URI result = uri.trimQuery();
      return result == uri ? this : appendFragment(result);
    }

    @Override
    public String fragment()
    {
      if (hashCode == 0)
      {
        hashCode();
      }
      return fragment.toString();
    }

    @Override
    public URI appendFragment(String fragment)
    {
      return uri.appendFragment(fragment);
    }

    @Override
    public URI trimFragment()
    {
      return uri;
    }

    @Override
    public URI resolve(URI base, boolean preserveRootParents)
    {
      URI result = uri.resolve(base, preserveRootParents);
      return result == uri ? this : appendFragment(result);
    }

    @Override
    public URI deresolve(URI base, boolean preserveRootParents, boolean anyRelPath, boolean shorterRelPath)
    {
      URI result = uri.deresolve(base, preserveRootParents, anyRelPath, shorterRelPath);
      return result == uri ? this : appendFragment(result);
    }

    @Override
    protected String[] collapseSegments(boolean preserveRootParents)
    {
      return uri.collapseSegments(preserveRootParents);
    }

    @Override
    public String toString()
    {
      CommonUtil.StringPool.StringsAccessUnit result = CommonUtil.STRING_POOL.getStringBuilder();
      result.append(uri.toString());
      result.append(FRAGMENT_SEPARATOR);
      result.append(fragment);
      return CommonUtil.STRING_POOL.intern(result);
    }

    /**
     * If the hash code is <code>0</code> then most likely we've got a pending lazy {@link LazyFragmentInitializer}.
     */
    @Override
    public int hashCode()
    {
      // Check if we have a deferred hash code initialization pending...
      // Note there is the very remote possibility that the hash code could really be 0...
      //
      if (hashCode == 0)
      {
        hashCode = (uri.hashCode * 31 + FRAGMENT_SEPARATOR )* CommonUtil.powerOf31(fragment.length()) + fragment.hashCode();

        // In that case, also split intern the fragment, but check if it's really a string, because otherwise it really must be split interned already.
        //
        if (fragment instanceof String)
        {
          fragment = splitInternFragment(fragment.toString());
        }
      }
      return hashCode;
    }

    @Override
    public boolean equals(Object object)
    {
      if (object == this)
      {
        return true;
      }

      if (!(object instanceof Fragment))
      {
        return false;
      }

      // Be careful to accommodate the case of a deferred split interned fragment.
      //
      Fragment that = (Fragment)object;
      return uri == that.uri && (fragment == that.fragment || fragment.toString().equals(that.fragment().toString()));
    }

    @Override
    public String toFileString()
    {
      return uri.toFileString();
    }

    @Override
    public String toPlatformString(boolean decode)
    {
      return uri.toPlatformString(decode);
    }

    @Override
    public URI appendSegment(String segment)
    {
      URI result = uri.appendSegment(segment);
      return result == uri ? this : appendFragment(result);
    }

    @Override
    public URI appendSegments(String[] segments)
    {
      URI result = uri.appendSegments(segments);
      return result == uri ? this : appendFragment(result);
    }

    @Override
    public URI trimSegments(int i)
    {
      URI result = uri.trimSegments(i);
      return result == uri ? this : appendFragment(result);
    }

    @Override
    public boolean hasTrailingPathSeparator()
    {
      return uri.hasTrailingPathSeparator();
    }

    @Override
    public String fileExtension()
    {
      return uri.fileExtension();
    }

    @Override
    public URI appendFileExtension(String fileExtension)
    {
      URI result = uri.appendFileExtension(fileExtension);
      return result == uri ? this : appendFragment(result);
    }

    @Override
    public URI trimFileExtension()
    {
      URI result = uri.trimFileExtension();
      return result == uri ? this : result.rawAppendFragment(fragment);
    }

    @Override
    public URI replacePrefix(URI oldPrefix, URI newPrefix)
    {
      URI result = uri.replacePrefix(oldPrefix, newPrefix);
      return result == uri ? this : result == null ? null : appendFragment(result);
    }
  }