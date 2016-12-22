package org.eclipse.emf.common.util;

import java.lang.ref.WeakReference;

/**
   * A subclass for representing an opaque URI.
   */
  final class Opaque extends URI
  {
    /**
     * The scheme of the opaque URI.
     */
    protected final String scheme;

    /**
     * The opaque part of the opaque URI.
     */
    protected final String opaquePart;

    /**
     * A weakly cached reference to the string representation.
     */
    protected WeakReference<String> toString;

    /**
     * Creates an instance from the components.
     * Assertions are used to validate the integrity of the result.
     * I.e., both components must be interned and the hash code must be equal to the hash code of the {@link #toString()}.
     */
    protected Opaque(int hashCode, String scheme, String opaquePart)
    {
      super(hashCode);

      this.scheme = scheme;
      this.opaquePart = opaquePart;

      // The scheme must be interned and must be lower cased.
      //
      assert scheme == CommonUtil.internToLowerCase(scheme);

      // The authority must be interned.
      //
      assert opaquePart.equals(CommonUtil.intern(opaquePart));

      // The components must be valid.
      //
      assert validateURI(false, scheme, opaquePart, null, false, NO_SEGMENTS, null, null);

      // The hash code must be the same as that of the string representation
      //
      assert hashCode == toString().hashCode();
    }

    @Override
    public boolean hasOpaquePart()
    {
      return true;
    }

    @Override
    public String scheme()
    {
      return scheme;
    }

    @Override
    public String opaquePart()
    {
      return opaquePart;
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
      String cachedString = getCachedString();
      if (cachedString != null)
      {
        return cachedString;
      }

      CommonUtil.StringPool.StringsAccessUnit result = CommonUtil.STRING_POOL.getStringBuilder();
      result.append(scheme);
      result.append(SCHEME_SEPARATOR);
      result.append(opaquePart);

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

      int index = 0;
      if (!string.startsWith(scheme))
      {
        return false;
      }

      int length = string.length();
      index += scheme.length();
      if (index >= length || string.charAt(index) != SCHEME_SEPARATOR)
      {
        return false;
      }
      ++index;

      if (!string.startsWith(opaquePart, index))
      {
        return false;
      }
      index += opaquePart.length();

      return index == length;
    }

    @Override
    protected boolean matches(int validate, boolean hierarchical, String scheme, String authority, String device, boolean absolutePath, String[] segments, String query)
    {
      return
        !hierarchical &&
          !absolutePath &&
          segments == null &&
          query == null &&
          validate >= URIComponentsAccessUnit.VALIDATE_NONE ?
             this.scheme.equals(scheme) && this.opaquePart.equals(authority) :
             equals(this.scheme, scheme) && equals(this.opaquePart, authority);
    }
  }