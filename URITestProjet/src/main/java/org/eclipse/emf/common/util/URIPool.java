package org.eclipse.emf.common.util;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

/**
   * A pool for caching URIs.
   */
  class URIPool extends Pool<URI>
  {
    protected static final long serialVersionUID = 1L;

    /**
     * A reference queue for managing the {@link URI#toString} values.
     */
    protected final ReferenceQueue<String> cachedToStrings;

    public URIPool(ReferenceQueue<Object> queue)
    {
      super(1031, null, queue);

      // The string cache will be managed by either an internal or external cache as appropriate.
      //
      cachedToStrings = externalQueue == null ? new ReferenceQueue<String>() : null;
    }

    /**
     * Access units for basic string access.
     */
    protected final StringAccessUnit.Queue stringAccessUnits = new StringAccessUnit.Queue(this);

    /**
     * Access units for platform URI string-based access.
     */
    protected final PlatformAccessUnit.Queue platformAccessUnits = new PlatformAccessUnit.Queue();

    /**
     * Access units for file URI string-based access.
     */
    protected final FileAccessUnit.Queue fileAccessUnits = new FileAccessUnit.Queue();

    /**
     * Access units for component-based access.
     */
    protected final URIComponentsAccessUnit.Queue uriComponentsAccessUnits = new URIComponentsAccessUnit.Queue();

    /**
     * Intern a URI from its string representation, parsing if necessary.
     * The string must not contain the fragment separator.
     */
    protected URI intern(String string)
    {
      if (string == null)
      {
        return null;
      }
      else
      {
        // Iterate over the entries with the matching hash code.
        //
        int hashCode = string.hashCode();
        for (Entry<URI> entry = getEntry(hashCode); entry != null; entry = entry.getNextEntry())
        {
          // Check that the referent isn't garbage collected and then compare it.
          //
          URI uri = entry.get();
          if (uri != null && uri.matches(string))
          {
            // Return that already present value.
            //
            return uri;
          }
        }

        writeLock.lock();
        try
        {
          StringAccessUnit accessUnit = stringAccessUnits.pop(true);
          accessUnit.setValue(string, hashCode);

          // The implementation returns an internalized value that's already pooled as a side effect.
          //
          URI result = accessUnit.getInternalizedValue();

          accessUnit.reset(true);
          return result;
        }
        finally
        {
          writeLock.unlock();
        }
      }
    }

    /**
     * Intern a platform URI from its path representation, parsing if necessary.
     */
    protected URI intern(String base, String pathName, boolean encode)
    {
      PlatformAccessUnit accessUnit = platformAccessUnits.pop(false);
      accessUnit.setValue(base, pathName, encode);
      return doIntern(false, accessUnit);
    }

    /**
     * Intern a file URI from its path representation, parsing if necessary.
     */
    protected URI internFile(String pathName)
    {
      FileAccessUnit accessUnit = fileAccessUnits.pop(false);
      accessUnit.setValue(pathName);
      return doIntern(false, accessUnit);
    }

    /**
     * Intern a URI from its component parts.
     * If <code>isExclusive</code> is true, acquire the {@link #writeLock} first.
     * Use {@link #intern(boolean, boolean, String, String, String, boolean, String[], String, int)} if the hash code is known and no validation is required.
     */
    protected URI intern(boolean isExclusive, int validate, boolean hierarchical, String scheme, String authority, String device, boolean absolutePath, String[] segments, String query)
    {
      if (isExclusive)
      {
        writeLock.lock();
      }
      URI uri;
      try
      {
        URIComponentsAccessUnit accessUnit = uriComponentsAccessUnits.pop(isExclusive);
        accessUnit.setValue(validate, hierarchical, scheme, authority, device, absolutePath, segments, query);
        uri = doIntern(isExclusive, accessUnit);
      }
      finally
      {
        if (isExclusive)
        {
          writeLock.unlock();
        }
      }
      return uri;
    }

    /**
     * Intern a URI from its component parts.
     * If <code>isExclusive</code> is true, acquire the {@link #writeLock} first.
     */
    protected URI intern(boolean isExclusive, boolean hierarchical, String scheme, String authority, String device, boolean absolutePath, String[] segments, String query, int hashCode)
    {
      if (isExclusive)
      {
        writeLock.lock();
      }
      URI uri;
      try
      {
        URIComponentsAccessUnit accessUnit = uriComponentsAccessUnits.pop(isExclusive);
        accessUnit.setValue(hierarchical, scheme, authority, device, absolutePath, segments, query, hashCode);
        uri = doIntern(isExclusive, accessUnit);
      }
      finally
      {
        if (isExclusive)
        {
          writeLock.unlock();
        }
      }
      return uri;
    }

    /**
     * Specialized to manage the {@link #cachedToStrings}.
     */
    @Override
    protected void doCleanup()
    {
      super.doCleanup();
      for (;;)
      {
        Reference<? extends String> cachedToString = cachedToStrings.poll();
        if (cachedToString == null)
        {
          return;
        }
        else
        {
          cachedToString.clear();
        }
      }
    }

    protected WeakReference<String> newCachedToString(URI uri, String string)
    {
      return
        cachedToStrings == null ?
          new CachedToString(uri, string, externalQueue) :
          new CachedToString(uri, string, cachedToStrings);
    }
  }