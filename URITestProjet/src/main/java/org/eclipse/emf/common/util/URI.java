/**
 * Copyright (c) 2002-2013 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   IBM - Initial API and implementation
 */
package org.eclipse.emf.common.util;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

/**
 * A representation of a Uniform Resource Identifier (URI), as specified by
 * <a href="http://www.ietf.org/rfc/rfc2396.txt">RFC 2396</a>, with certain
 * enhancements.  A <code>URI</code> instance can be created by specifying
 * values for its components, or by providing a single URI string, which is
 * parsed into its components.  Static factory methods whose names begin
 * with "create" are used for both forms of object creation.  No public or
 * protected constructors are provided; this class can not be subclassed.
 *
 * <p>Like <code>String</code>, <code>URI</code> is an immutable class;
 * a <code>URI</code> instance offers several by-value methods that return a
 * new <code>URI</code> object based on its current state.  Most useful,
 * a relative <code>URI</code> can be {@link #resolve(URI) resolve}d against
 * a base absolute <code>URI</code> -- the latter typically identifies the
 * document in which the former appears.  The inverse to this is {@link
 * #deresolve(URI) deresolve}, which answers the question, "what relative
 * URI will resolve, against the given base, to this absolute URI?"
 *
 * <p>In the <a href="http://www.ietf.org/rfc/rfc2396.txt">RFC</a>, much
 * attention is focused on a hierarchical naming system used widely to
 * locate resources via common protocols such as HTTP, FTP, and Gopher, and
 * to identify files on a local file system.  Accordingly, most of this
 * class's functionality is for handling such URIs, which can be identified
 * via {@link #isHierarchical isHierarchical}.
 *
 * <p><a name="device_explanation">
 * The primary enhancement beyond the RFC description is an optional
 * device component.  Instead of treating the device as just another segment
 * in the path, it can be stored as a separate component (almost a
 * sub-authority), with the root below it.  For example, resolving
 * <code>/bar</code> against <code>file:///c:/foo</code> would result in
 * <code>file:///c:/bar</code> being returned.  Also, you cannot take
 * the parent of a device, so resolving <code>..</code> against
 * <code>file:///c:/</code> would not yield <code>file:///</code>, as you
 * might expect.  This feature is useful when working with file-scheme
 * URIs, as devices do not typically occur in protocol-based ones.  A
 * device-enabled <code>URI</code> is created by parsing a string with
 * {@link #createURI(String) createURI}; if the first segment of the path
 * ends with the <code>:</code> character, it is stored (including the colon)
 * as the device, instead.  Alternately, either the {@link
 * #createHierarchicalURI(String, String, String, String, String) no-path}
 * or the {@link #createHierarchicalURI(String, String, String, String[],
 * String, String) absolute-path} form of <code>createHierarchicalURI()</code>
 * can be used, in which a non-null <code>device</code> parameter can be
 * specified.
 *
 * <p><a name="archive_explanation">
 * The other enhancement provides support for the almost-hierarchical
 * form used for files within archives, such as the JAR scheme, defined
 * for the Java Platform in the documentation for {@link
 * java.net.JarURLConnection}. By default, this support is enabled for
 * absolute URIs with scheme equal to "jar", "zip", or "archive" (ignoring case), and
 * is implemented by a hierarchical URI, whose authority includes the
 * entire URI of the archive, up to and including the <code>!</code>
 * character.  The URI of the archive must have no fragment.  The whole
 * archive URI must have no device and an absolute path.  Special handling
 * is supported for {@link #createURI(String) creating}, {@link
 * #validArchiveAuthority validating}, {@link #devicePath getting the path}
 * from, and {@link #toString() displaying} archive URIs. In all other
 * operations, including {@link #resolve(URI) resolving} and {@link
 * #deresolve(URI) deresolving}, they are handled like any ordinary URI.
 * The schemes that identify archive URIs can be changed from their default
 * by setting the <code>URI.archiveSchemes</code>
 * system property. Multiple schemes should be space separated, and the test
 * of whether a URI's scheme matches is always case-insensitive.
 *
 * <p>This implementation does not impose all of the restrictions on
 * character validity that are specified in the RFC.  Static methods whose
 * names begin with "valid" are used to test whether a given string is valid
 * value for the various URI components.  Presently, these tests place no
 * restrictions beyond what would have been required in order for {@link
 * #createURI(String) createURI} to have parsed them correctly from a single
 * URI string.  If necessary in the future, these tests may be made more
 * strict, to better conform to the RFC.
 *
 * <p>Another group of static methods, whose names begin with "encode", use
 * percent escaping to encode any characters that are not permitted in the
 * various URI components. Another static method is provided to {@link
 * #decode decode} encoded strings.  An escaped character is represented as
 * a percent symbol (<code>%</code>), followed by two hex digits that specify
 * the character code.  These encoding methods are more strict than the
 * validation methods described above.  They ensure validity according to the
 * RFC, with one exception: non-ASCII characters.
 *
 * <p>The RFC allows only characters that can be mapped to 7-bit US-ASCII
 * representations.  Non-ASCII, single-byte characters can be used only via
 * percent escaping, as described above.  This implementation uses Java's
 * Unicode <code>char</code> and <code>String</code> representations, and
 * makes no attempt to encode characters 0xA0 and above.  Characters in the
 * range 0x80-0x9F are still escaped.  In this respect, EMF's notion of a URI
 * is actually more like an IRI (Internationalized Resource Identifier), for
 * which an RFC is now in <href="http://www.w3.org/International/iri-edit/draft-duerst-iri-09.txt">draft
 * form</a>.
 *
 * <p>Finally, note the difference between a <code>null</code> parameter to
 * the static factory methods and an empty string.  The former signifies the
 * absence of a given URI component, while the latter simply makes the
 * component blank.  This can have a significant effect when resolving.  For
 * example, consider the following two URIs: <code>/bar</code> (with no
 * authority) and <code>///bar</code> (with a blank authority).  Imagine
 * resolving them against a base with an authority, such as
 * <code>http://www.eclipse.org/</code>.  The former case will yield
 * <code>http://www.eclipse.org/bar</code>, as the base authority will be
 * preserved.  In the latter case, the empty authority will override the
 * base authority, resulting in <code>http:///bar</code>!
 */
public abstract class URI
{
  protected static final boolean DEBUG = false;

  /**
   * The cached hash code of the URI.
   * This is always equal to the hash code of {@link #toString()}
   */
  protected int hashCode;

  /**
   * A pool for managing {@link URI} instances.
   */
  protected static final URIPool POOL = new URIPool(CommonUtil.REFERENCE_CLEARING_QUEUE);

  // The lower-cased schemes that will be used to identify archive URIs.
  protected static final String[] ARCHIVE_SCHEMES;

  // Identifies a file-type absolute URI.
  protected static final String SCHEME_FILE = "file";
  protected static final String SCHEME_JAR = "jar";
  protected static final String SCHEME_ZIP = "zip";
  protected static final String SCHEME_ARCHIVE = "archive";
  protected static final String SCHEME_PLATFORM = "platform";
  protected static final String SCHEME_HTTP = "http";

  protected static final int SCHEME_FILE_HASH_CODE = SCHEME_FILE.hashCode();
  protected static final int SCHEME_JAR_HASH_CODE = SCHEME_JAR.hashCode();
  protected static final int SCHEME_ZIP_HASH_CODE = SCHEME_ZIP.hashCode();
  protected static final int SCHEME_ARCHIVE_HASH_CODE = SCHEME_ARCHIVE.hashCode();
  protected static final int SCHEME_PLATFORM_HASH_CODE = SCHEME_PLATFORM.hashCode();
  protected static final int SCHEME_HTTP_HASH_CODE = SCHEME_HTTP.hashCode();

  // Special segment values interpreted at resolve and resolve time.
  protected static final String SEGMENT_EMPTY = "";
  protected static final String SEGMENT_SELF = ".";
  protected static final String SEGMENT_PARENT = "..";

  // Special segments used for platform URIs.
  protected static final String SEGMENT_PLUGIN = "plugin";
  protected static final String SEGMENT_RESOURCE = "resource";

  // Ensure that all the string constants used as components in URIs are directly in the string pool.
  //
  static
  {
    // Ensure that all the string constants are themselves Java interned in the string pool.
    //
    CommonUtil.STRING_POOL.javaIntern(SCHEME_FILE);
    CommonUtil.STRING_POOL.javaIntern(SCHEME_JAR);
    CommonUtil.STRING_POOL.javaIntern(SCHEME_ZIP);
    CommonUtil.STRING_POOL.javaIntern(SCHEME_ARCHIVE);
    CommonUtil.STRING_POOL.javaIntern(SCHEME_PLATFORM);
    CommonUtil.STRING_POOL.javaIntern(SCHEME_HTTP);

    CommonUtil.STRING_POOL.javaIntern(SEGMENT_EMPTY);
    CommonUtil.STRING_POOL.javaIntern(SEGMENT_SELF);
    CommonUtil.STRING_POOL.javaIntern(SEGMENT_PARENT);

    CommonUtil.STRING_POOL.javaIntern(SEGMENT_PLUGIN);
    CommonUtil.STRING_POOL.javaIntern(SEGMENT_RESOURCE);
  }

  // Special arrays uses for segments
  protected static final String[] NO_SEGMENTS = SegmentSequence.EMPTY_ARRAY;
  protected static final String[] ONE_EMPTY_SEGMENT = SegmentSequence.EMPTY_STRING_ARRAY;
  protected static final String[] ONE_SELF_SEGMENT = SegmentSequence.STRING_ARRAY_POOL.intern(SEGMENT_SELF, false);

  // Separators for parsing a URI string.
  protected static final char SCHEME_SEPARATOR = ':';
  protected static final String AUTHORITY_SEPARATOR = "//";
  protected static final int AUTHORITY_SEPARATOR_HASH_CODE = AUTHORITY_SEPARATOR.hashCode();
  protected static final char DEVICE_IDENTIFIER = ':';
  protected static final char SEGMENT_SEPARATOR = '/';
  protected static final char QUERY_SEPARATOR = '?';
  protected static final char FRAGMENT_SEPARATOR = '#';
  protected static final char USER_INFO_SEPARATOR = '@';
  protected static final char PORT_SEPARATOR = ':';
  protected static final char FILE_EXTENSION_SEPARATOR = '.';
  protected static final char ARCHIVE_IDENTIFIER = '!';
  protected static final String ARCHIVE_SEPARATOR = "!/";

  // Characters to use in escaping.
  protected static final char ESCAPE = '%';
  protected static final char[] HEX_DIGITS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

  // Some character classes, as defined in RFC 2396's BNF for URI.
  // These are 128-bit bitmasks, stored as two longs, where the Nth bit is set
  // iff the ASCII character with value N is included in the set.  These are
  // created with the highBitmask() and lowBitmask() methods defined below,
  // and a character is tested against them using matches().
  //
  protected static final long ALPHA_HI = highBitmask('a', 'z') | highBitmask('A', 'Z');
  protected static final long ALPHA_LO = lowBitmask('a', 'z')  | lowBitmask('A', 'Z');
  protected static final long DIGIT_HI = highBitmask('0', '9');
  protected static final long DIGIT_LO = lowBitmask('0', '9');
  protected static final long ALPHANUM_HI = ALPHA_HI | DIGIT_HI;
  protected static final long ALPHANUM_LO = ALPHA_LO | DIGIT_LO;
  protected static final long HEX_HI = DIGIT_HI | highBitmask('A', 'F') | highBitmask('a', 'f');
  protected static final long HEX_LO = DIGIT_LO | lowBitmask('A', 'F')  | lowBitmask('a', 'f');
  protected static final long UNRESERVED_HI = ALPHANUM_HI | highBitmask("-_.!~*'()");
  protected static final long UNRESERVED_LO = ALPHANUM_LO | lowBitmask("-_.!~*'()");
  protected static final long RESERVED_HI = highBitmask(";/?:@&=+$,");
  protected static final long RESERVED_LO = lowBitmask(";/?:@&=+$,");
  protected static final long URIC_HI = RESERVED_HI | UNRESERVED_HI;  // | ucschar | escaped
  protected static final long URIC_LO = RESERVED_LO | UNRESERVED_LO;

  // Additional useful character classes, including characters valid in certain
  // URI components and separators used in parsing them out of a string.
  //
  protected static final long SEGMENT_CHAR_HI = UNRESERVED_HI | highBitmask(";:@&=+$,");  // | ucschar | escaped
  protected static final long SEGMENT_CHAR_LO = UNRESERVED_LO | lowBitmask(";:@&=+$,");
  protected static final long PATH_CHAR_HI = SEGMENT_CHAR_HI | highBitmask('/');  // | ucschar | escaped
  protected static final long PATH_CHAR_LO = SEGMENT_CHAR_LO | lowBitmask('/');
  protected static final long MAJOR_SEPARATOR_HI = highBitmask(":/?#");
  protected static final long MAJOR_SEPARATOR_LO = lowBitmask(":/?#");
  protected static final long SEGMENT_END_HI = highBitmask("/?#");
  protected static final long SEGMENT_END_LO = lowBitmask("/?#");
  protected static final long PLATFORM_SEGMENT_RESERVED_HI = highBitmask("/?#\\");
  protected static final long PLATFORM_SEGMENT_RESERVED_LO = lowBitmask("/?#\\");

  // The intent of this was to switch over to encoding platform resource URIs
  // by default, but allow people to use a system property to avoid this.
  // However, that caused problems for people and we had to go back to not
  // encoding and introduce yet another factory method that explicitly enables
  // encoding.
  //
  protected static final boolean ENCODE_PLATFORM_RESOURCE_URIS =
    System.getProperty("URI.encodePlatformResourceURIs") != null &&
    !"false".equalsIgnoreCase(System.getProperty("URI.encodePlatformResourceURIs"));

  // Static initializer for archiveSchemes.
  static
  {
    // Initialize the archive schemes.
    //
    List<String> list = new UniqueEList<String>();

    String propertyValue = System.getProperty("URI.archiveSchemes");

    list.add(SCHEME_JAR);
    list.add(SCHEME_ZIP);
    list.add(SCHEME_ARCHIVE);

    if (propertyValue != null)
    {
      for (StringTokenizer t = new StringTokenizer(propertyValue); t.hasMoreTokens(); )
      {
        String token = t.nextToken().toLowerCase();
        if (validScheme(token))
        {
          String scheme = CommonUtil.javaIntern(token);
          list.add(scheme);
        }
      }
    }

    ARCHIVE_SCHEMES = list.toArray(new String[list.size()]);
  }

  // Returns the lower half bitmask for the given ASCII character.
  protected static long lowBitmask(char c)
  {
    return c < 64 ? 1L << c : 0L;
  }

  // Returns the upper half bitmask for the given ACSII character.
  protected static long highBitmask(char c)
  {
    return c >= 64 && c < 128 ? 1L << (c - 64) : 0L;
  }

  // Returns the lower half bitmask for all ASCII characters between the two
  // given characters, inclusive.
  protected static long lowBitmask(char from, char to)
  {
    long result = 0L;
    if (from < 64 && from <= to)
    {
      to = to < 64 ? to : 63;
      for (char c = from; c <= to; c++)
      {
        result |= 1L << c;
      }
    }
    return result;
  }

  // Returns the upper half bitmask for all AsCII characters between the two
  // given characters, inclusive.
  protected static long highBitmask(char from, char to)
  {
    return to < 64 ? 0 : lowBitmask((char)(from < 64 ? 0 : from - 64), (char)(to - 64));
  }

  // Returns the lower half bitmask for all the ASCII characters in the given
  // string.
  protected static long lowBitmask(String chars)
  {
    long result = 0L;
    for (int i = 0, len = chars.length(); i < len; i++)
    {
      char c = chars.charAt(i);
      if (c < 64) result |= 1L << c;
    }
    return result;
  }

  // Returns the upper half bitmask for all the ASCII characters in the given
  // string.
  protected static long highBitmask(String chars)
  {
    long result = 0L;
    for (int i = 0, len = chars.length(); i < len; i++)
    {
      char c = chars.charAt(i);
      if (c >= 64 && c < 128) result |= 1L << c - 64;
    }
    return result;
  }

  // Returns whether the given character is in the set specified by the given
  // bitmask.
  protected static boolean matches(char c, long highBitmask, long lowBitmask)
  {
    if (c >= 128) return false;
    return c < 64 ?
      ((1L << c) & lowBitmask) != 0 :
      ((1L << (c - 64)) & highBitmask) != 0;
  }

  // Debugging method: converts the given long to a string of binary digits.
/*
  protected static String toBits(long l)
  {
    StringBuffer result = new StringBuffer();
    for (int i = 0; i < 64; i++)
    {
      boolean b = (l & 1L) != 0;
      result.insert(0, b ? '1' : '0');
      l >>= 1;
    }
    return result.toString();
  }
*/

  /**
   * Static factory method for a generic, non-hierarchical URI.  There is no
   * concept of a relative non-hierarchical URI; such an object cannot be
   * created.
   *
   * @exception java.lang.IllegalArgumentException if <code>scheme</code> is
   * null, if <code>scheme</code> is an <a href="#archive_explanation">archive
   * URI</a> scheme, or if <code>scheme</code>, <code>opaquePart</code>, or
   * <code>fragment</code> is not valid according to {@link #validScheme
   * validScheme}, {@link #validOpaquePart validOpaquePart}, or {@link
   * #validFragment validFragment}, respectively.
   */
  public static URI createGenericURI(String scheme, String opaquePart, String fragment)
  {
    if (scheme == null)
    {
      throw new IllegalArgumentException("relative non-hierarchical URI");
    }

    if (isArchiveScheme(scheme))
    {
      throw new IllegalArgumentException("non-hierarchical archive URI");
    }

    return POOL.intern(false, URIComponentsAccessUnit.VALIDATE_ALL, false, scheme, opaquePart, null, false, null, null).appendFragment(fragment);
  }

  /**
   * Static factory method for a hierarchical URI with no path.  The
   * URI will be relative if <code>scheme</code> is non-null, and absolute
   * otherwise.  An absolute URI with no path requires a non-null
   * <code>authority</code> and/or <code>device</code>.
   *
   * @exception java.lang.IllegalArgumentException if <code>scheme</code> is
   * non-null while <code>authority</code> and <code>device</code> are null,
   * if <code>scheme</code> is an <a href="#archive_explanation">archive
   * URI</a> scheme, or if <code>scheme</code>, <code>authority</code>,
   * <code>device</code>, <code>query</code>, or <code>fragment</code> is not
   * valid according to {@link #validScheme validSheme}, {@link
   * #validAuthority validAuthority}, {@link #validDevice validDevice},
   * {@link #validQuery validQuery}, or {@link #validFragment validFragment},
   * respectively.
   */
  public static URI createHierarchicalURI(String scheme, String authority, String device, String query, String fragment)
  {
    if (scheme != null && authority == null && device == null)
    {
      throw new IllegalArgumentException("absolute hierarchical URI without authority, device, path");
    }

    if (isArchiveScheme(scheme))
    {
      throw new IllegalArgumentException("archive URI with no path");
    }

    return POOL.intern(false, URIComponentsAccessUnit.VALIDATE_ALL, true, scheme, authority, device, false, NO_SEGMENTS, query).appendFragment(fragment);
  }

  /**
   * Static factory method for a hierarchical URI with absolute path.
   * The URI will be relative if <code>scheme</code> is non-null, and
   * absolute otherwise.
   *
   * @param segments an array of non-null strings, each representing one
   * segment of the path.  As an absolute path, it is automatically
   * preceded by a <code>/</code> separator.  If desired, a trailing
   * separator should be represented by an empty-string segment as the last
   * element of the array.
   *
   * @exception java.lang.IllegalArgumentException if <code>scheme</code> is
   * an <a href="#archive_explanation">archive URI</a> scheme and
   * <code>device</code> is non-null, or if <code>scheme</code>,
   * <code>authority</code>, <code>device</code>, <code>segments</code>,
   * <code>query</code>, or <code>fragment</code> is not valid according to
   * {@link #validScheme validScheme}, {@link #validAuthority validAuthority}
   * or {@link #validArchiveAuthority validArchiveAuthority}, {@link
   * #validDevice validDevice}, {@link #validSegments validSegments}, {@link
   * #validQuery validQuery}, or {@link #validFragment validFragment}, as
   * appropriate.
   */
  public static URI createHierarchicalURI(String scheme, String authority, String device, String[] segments, String query, String fragment)
  {
    if (device != null)
    {
      if (isArchiveScheme(scheme))
      {
        throw new IllegalArgumentException("archive URI with device");
      }
      if (SCHEME_PLATFORM.equals(scheme))
      {
        throw new IllegalArgumentException("platform URI with device");
      }
    }

    return POOL.intern(false, URIComponentsAccessUnit.VALIDATE_ALL, true, scheme, authority, device, true, segments, query).appendFragment(fragment);
  }

  /**
   * Static factory method for a relative hierarchical URI with relative
   * path.
   *
   * @param segments an array of non-null strings, each representing one
   * segment of the path.  A trailing separator is represented by an
   * empty-string segment at the end of the array.
   *
   * @exception java.lang.IllegalArgumentException if <code>segments</code>,
   * <code>query</code>, or <code>fragment</code> is not valid according to
   * {@link #validSegments validSegments}, {@link #validQuery validQuery}, or
   * {@link #validFragment validFragment}, respectively.
   */
  public static URI createHierarchicalURI(String[] segments, String query, String fragment)
  {
    return POOL.intern(false, URIComponentsAccessUnit.VALIDATE_ALL, true, null, null, null, false, segments, query).appendFragment(fragment);
  }

  /**
   * Static factory method based on parsing a URI string, with
   * <a href="#device_explanation">explicit device support</a> and handling
   * for <a href="#archive_explanation">archive URIs</a> enabled. The
   * specified string is parsed as described in <a
   * href="http://www.ietf.org/rfc/rfc2396.txt">RFC 2396</a>, and an
   * appropriate <code>URI</code> is created and returned.  Note that
   * validity testing is not as strict as in the RFC; essentially, only
   * separator characters are considered.  This method also does not perform
   * encoding of invalid characters, so it should only be used when the URI
   * string is known to have already been encoded, so as to avoid double
   * encoding.
   *
   * @exception java.lang.IllegalArgumentException if any component parsed
   * from <code>uri</code> is not valid according to {@link #validScheme
   * validScheme}, {@link #validOpaquePart validOpaquePart}, {@link
   * #validAuthority validAuthority}, {@link #validArchiveAuthority
   * validArchiveAuthority}, {@link #validDevice validDevice}, {@link
   * #validSegments validSegments}, {@link #validQuery validQuery}, or {@link
   * #validFragment validFragment}, as appropriate.
   */
  public static URI createURI(String uri)
  {
    return createURIWithCache(uri);
  }

  /**
   * Static factory method that encodes and parses the given URI string.
   * Appropriate encoding is performed for each component of the URI.
   * If more than one <code>#</code> is in the string, the last one is
   * assumed to be the fragment's separator, and any others are encoded.
   * This method is the simplest way to safely parse an arbitrary URI string.
   *
   * @param ignoreEscaped <code>true</code> to leave <code>%</code> characters
   * unescaped if they already begin a valid three-character escape sequence;
   * <code>false</code> to encode all <code>%</code> characters.  This
   * capability is provided to allow partially encoded URIs to be "fixed",
   * while avoiding adding double encoding; however, it is usual just to
   * specify <code>false</code> to perform ordinary encoding.
   *
   * @exception java.lang.IllegalArgumentException if any component parsed
   * from <code>uri</code> is not valid according to {@link #validScheme
   * validScheme}, {@link #validOpaquePart validOpaquePart}, {@link
   * #validAuthority validAuthority}, {@link #validArchiveAuthority
   * validArchiveAuthority}, {@link #validDevice validDevice}, {@link
   * #validSegments validSegments}, {@link #validQuery validQuery}, or {@link
   * #validFragment validFragment}, as appropriate.
   */
  public static URI createURI(String uri, boolean ignoreEscaped)
  {
    return createURIWithCache(encodeURI(uri, ignoreEscaped, FRAGMENT_LAST_SEPARATOR));
  }

  /**
   * When specified as the last argument to {@link #createURI(String, boolean, int)
   * createURI}, indicates that there is no fragment, so any <code>#</code> characters
   * should be encoded.
   * @see #createURI(String, boolean, int)
   */
  public static final int FRAGMENT_NONE = 0;

  /**
   * When specified as the last argument to {@link #createURI(String, boolean, int)
   * createURI}, indicates that the first <code>#</code> character should be taken as
   * the fragment separator, and any others should be encoded.
   * @see #createURI(String, boolean, int)
   */
  public static final int FRAGMENT_FIRST_SEPARATOR = 1;

  /**
   * When specified as the last argument to {@link #createURI(String, boolean, int)
   * createURI}, indicates that the last <code>#</code> character should be taken as
   * the fragment separator, and any others should be encoded.
   * @see #createURI(String, boolean, int)
   */
  public static final int FRAGMENT_LAST_SEPARATOR = 2;

  /**
   * Static factory method that encodes and parses the given URI string.
   * Appropriate encoding is performed for each component of the URI.
   * Control is provided over which, if any, <code>#</code> should be
   * taken as the fragment separator and which should be encoded.
   * This method is the preferred way to safely parse an arbitrary URI string
   * that is known to contain <code>#</code> characters in the fragment or to
   * have no fragment at all.
   *
   * @param ignoreEscaped <code>true</code> to leave <code>%</code> characters
   * unescaped if they already begin a valid three-character escape sequence;
   * <code>false</code> to encode all <code>%</code> characters.  This
   * capability is provided to allow partially encoded URIs to be "fixed",
   * while avoiding adding double encoding; however, it is usual just to
   * specify <code>false</code> to perform ordinary encoding.
   *
   * @param fragmentLocationStyle one of {@link #FRAGMENT_NONE},
   * {@link #FRAGMENT_FIRST_SEPARATOR}, or {@link #FRAGMENT_LAST_SEPARATOR},
   * indicating which, if any, of the <code>#</code> characters should be
   * considered the fragment separator. Any others will be encoded.
   *
   * @exception java.lang.IllegalArgumentException if any component parsed
   * from <code>uri</code> is not valid according to {@link #validScheme
   * validScheme}, {@link #validOpaquePart validOpaquePart}, {@link
   * #validAuthority validAuthority}, {@link #validArchiveAuthority
   * validArchiveAuthority}, {@link #validDevice validDevice}, {@link
   * #validSegments validSegments}, {@link #validQuery validQuery}, or {@link
   * #validFragment validFragment}, as appropriate.
   */
  public static URI createURI(String uri, boolean ignoreEscaped, int fragmentLocationStyle)
  {
    return createURIWithCache(encodeURI(uri, ignoreEscaped, fragmentLocationStyle));
  }

  /**
   * Static factory method based on parsing a URI string, with
   * <a href="#device_explanation">explicit device support</a> enabled.
   * Note that validity testing is not a strict as in the RFC; essentially,
   * only separator characters are considered.  So, for example, non-Latin
   * alphabet characters appearing in the scheme would not be considered an
   * error.
   *
   * @exception java.lang.IllegalArgumentException if any component parsed
   * from <code>uri</code> is not valid according to {@link #validScheme
   * validScheme}, {@link #validOpaquePart validOpaquePart}, {@link
   * #validAuthority validAuthority}, {@link #validArchiveAuthority
   * validArchiveAuthority}, {@link #validDevice validDevice}, {@link
   * #validSegments validSegments}, {@link #validQuery validQuery}, or {@link
   * #validFragment validFragment}, as appropriate.
   *
   * @deprecated Use {@link #createURI(String) createURI}, which now has explicit
   * device support enabled. The two methods now operate identically.
   */
  @Deprecated
  public static URI createDeviceURI(String uri)
  {
    return createURIWithCache(uri);
  }

  // Uses the URI pool to speed up creation of a URI from a string.
  /**
   * This method was included in the public API by mistake.
   *
   * @deprecated Please use {@link #createURI(String) createURI} instead.
   */
  @Deprecated
  public static URI createURIWithCache(String uri)
  {
    int index = uri.indexOf(FRAGMENT_SEPARATOR);
    return
      index == -1 ?
        POOL.intern(uri) :
        POOL.intern(uri.substring(0, index)).appendFragment(uri.substring(index + 1));
  }

  /**
   * Static factory method based on parsing a {@link java.io.File} path
   * string.  The <code>pathName</code> is converted into an appropriate
   * form, as follows: platform specific path separators are converted to
   * <code>/</code>; the path is encoded; and a "file" scheme, if the path is {@link File#isAbsolute() absolute}, and, if missing,
   * a leading <code>/</code>, are added to an absolute path.  The result
   * is then parsed the same as if using {@link #createURI(String) createURI}.
   *
   * <p>The encoding step escapes all spaces, <code>#</code> characters, and
   * other characters disallowed in URIs, as well as <code>?</code>, which
   * would delimit a path from a query.  Decoding is automatically performed
   * by {@link #toFileString toFileString}, and can be applied to the values
   * returned by other accessors via the static {@link #decode(String)
   * decode} method.
   *
   * <p>A relative path with a specified device (something like
   * <code>C:myfile.txt</code>) cannot be expressed as a valid URI.
   * An absolute URI, i.e., one with <code>file:</code> will only be returned if the <code>pathName</code> itself is {@link File#isAbsolute() absolute}.
   * In other words, a relative path will yield a {@link #isRelative() relative} URI,
   * and in particular on Windows, a path is absolute only if the device is specified, 
   * e.g., <code>C:/myfile.text</code> is absolute but <code>/myfile.text</code> is relative on Windows though absolute on Unix-style file systems.
   * 
   *
   * @exception java.lang.IllegalArgumentException if <code>pathName</code>
   * specifies a device and a relative path, or if any component of the path
   * is not valid according to {@link #validAuthority validAuthority}, {@link
   * #validDevice validDevice}, or {@link #validSegments validSegments},
   * {@link #validQuery validQuery}, or {@link #validFragment validFragment}.
   */
  public static URI createFileURI(String pathName)
  {
    return POOL.internFile(pathName);
  }

  /**
   * Static factory method based on parsing a workspace-relative path string.
   *
   * <p>The <code>pathName</code> must be of the form:
   * <pre>
   *   /project-name/path</pre>
   *
   * <p>Platform-specific path separators will be converted to slashes.
   * If not included, the leading path separator will be added.  The
   * result will be of this form, which is parsed using {@link #createURI(String)
   * createURI}:
   * <pre>
   *   platform:/resource/project-name/path</pre>
   *
   * <p>This scheme supports relocatable projects in Eclipse and in
   * stand-alone EMF.
   *
   * <p>Path encoding is performed only if the
   * <code>URI.encodePlatformResourceURIs</code>
   * system property is set to "true". Decoding can be performed with the
   * static {@link #decode(String) decode} method.
   *
   * @exception java.lang.IllegalArgumentException if any component parsed
   * from the path is not valid according to {@link #validDevice validDevice},
   * {@link #validSegments validSegments}, {@link #validQuery validQuery}, or
   * {@link #validFragment validFragment}.
   *
   * @see org.eclipse.core.runtime.Platform#resolve
   * @see #createPlatformResourceURI(String, boolean)
   * @deprecated Use {@link #createPlatformResourceURI(String, boolean)} instead.
   */
  @Deprecated
  public static URI createPlatformResourceURI(String pathName)
  {
    return createPlatformResourceURI(pathName, ENCODE_PLATFORM_RESOURCE_URIS);
  }

  /**
   * Static factory method based on parsing a workspace-relative path string,
   * with an option to encode the created URI.
   *
   * <p>The <code>pathName</code> must be of the form:
   * <pre>
   *   /project-name/path</pre>
   *
   * <p>Platform-specific path separators will be converted to slashes.
   * If not included, the leading path separator will be added.  The
   * result will be of this form, which is parsed using {@link #createURI(String)
   * createURI}:
   * <pre>
   *   platform:/resource/project-name/path</pre>
   *
   * <p>This scheme supports relocatable projects in Eclipse and in
   * stand-alone EMF.
   *
   * <p>Depending on the <code>encode</code> argument, the path may be
   * automatically encoded to escape all spaces, <code>#</code> characters,
   * and other characters disallowed in URIs, as well as <code>?</code>,
   * which would delimit a path from a query.  Decoding can be performed with
   * the static {@link #decode(String) decode} method. It is strongly
   * recommended to specify <code>true</code> to enable encoding, unless the
   * path string has already been encoded.
   *
   * @exception java.lang.IllegalArgumentException if any component parsed
   * from the path is not valid according to {@link #validDevice validDevice},
   * {@link #validSegments validSegments}, {@link #validQuery validQuery}, or
   * {@link #validFragment validFragment}.
   *
   * @see org.eclipse.core.runtime.Platform#resolve
   */
  public static URI createPlatformResourceURI(String pathName, boolean encode)
  {
    return POOL.intern(SEGMENT_RESOURCE, pathName, encode);
  }

  /**
   * Static factory method based on parsing a plug-in-based path string,
   * with an option to encode the created URI.
   *
   * <p>The <code>pathName</code> must be of the form:
   * <pre>
   *   /plugin-id/path</pre>
   *
   * <p>Platform-specific path separators will be converted to slashes.
   * If not included, the leading path separator will be added.  The
   * result will be of this form, which is parsed using {@link #createURI(String)
   * createURI}:
   * <pre>
   *   platform:/plugin/plugin-id/path</pre>
   *
   * <p>This scheme supports relocatable plug-in content in Eclipse.
   *
   * <p>Depending on the <code>encode</code> argument, the path may be
   * automatically encoded to escape all spaces, <code>#</code> characters,
   * and other characters disallowed in URIs, as well as <code>?</code>,
   * which would delimit a path from a query.  Decoding can be performed with
   * the static {@link #decode(String) decode} method. It is strongly
   * recommended to specify <code>true</code> to enable encoding, unless the
   * path string has already been encoded.
   *
   * @exception java.lang.IllegalArgumentException if any component parsed
   * from the path is not valid according to {@link #validDevice validDevice},
   * {@link #validSegments validSegments}, {@link #validQuery validQuery}, or
   * {@link #validFragment validFragment}.
   *
   * @see org.eclipse.core.runtime.Platform#resolve
   * @since org.eclipse.emf.common 2.3
   */
  public static URI createPlatformPluginURI(String pathName, boolean encode)
  {
    return POOL.intern(SEGMENT_PLUGIN, pathName, encode);
  }

  // Splits the fragment into a segment sequence if it starts with a /, i.e., if it's used as a fragment path by EMF's resource implementation.
  //
  protected static CharSequence splitInternFragment(String fragment)
  {
    if (fragment.length() > 0 && fragment.charAt(0) == SEGMENT_SEPARATOR)
    {
      return SegmentSequence.create("/", fragment);
    }
    else
    {
      return CommonUtil.intern(fragment);
    }
  }

  // Protected constructor for use of pool.
  //
  protected URI(int hashCode)
  {
    this.hashCode = hashCode;
  }

  // Validates all of the URI components.  Factory methods should call this
  // before using the constructor, though they must ensure that the
  // inter-component requirements described in their own Javadocs are all
  // satisfied, themselves.  If a new URI is being constructed out of
  // an existing URI, this need not be called.  Instead, just the new
  // components may be validated individually.
  protected static boolean validateURI(boolean hierarchical, String scheme, String authority, String device, boolean absolutePath, String[] segments, String query, String fragment)
  {
    if (!validScheme(scheme))
    {
      throw new IllegalArgumentException("invalid scheme: " + scheme);
    }

    if (!hierarchical)
    {
      if (!validOpaquePart(authority))
      {
        throw new IllegalArgumentException("invalid opaquePart: " + authority);
      }
    }
    else
    {
      if (isArchiveScheme(scheme) ? !validArchiveAuthority(authority) : !validAuthority(authority))
      {
        throw new IllegalArgumentException("invalid authority: " + authority);
      }

      if (!validSegments(segments))
      {
        String s = segments == null ? "invalid segments: null" :
          "invalid segment: " + firstInvalidSegment(segments);
        throw new IllegalArgumentException(s);
      }
    }

    if (!validDevice(device))
    {
      throw new IllegalArgumentException("invalid device: " + device);
    }

    if (!validQuery(query))
    {
      throw new IllegalArgumentException("invalid query: " + query);
    }

    if (!validFragment(fragment))
    {
      throw new IllegalArgumentException("invalid fragment: " + fragment);
    }

    return true;
  }

  // Alternate, stricter implementations of the following validation methods
  // are provided, commented out, for possible future use...

  /**
   * Returns <code>true</code> if the specified <code>value</code> would be
   * valid as the scheme component of a URI; <code>false</code> otherwise.
   *
   * <p>A valid scheme may be null or contain any characters except for the
   * following: <code>: / ? #</code>
   */
  public static boolean validScheme(String value)
  {
    return value == null || !contains(value, MAJOR_SEPARATOR_HI, MAJOR_SEPARATOR_LO);

  // <p>A valid scheme may be null, or consist of a single letter followed
  // by any number of letters, numbers, and the following characters:
  // <code>+ - .</code>

    //if (value == null) return true;
    //return value.length() != 0 &&
    //  matches(value.charAt(0), ALPHA_HI, ALPHA_LO) &&
    //  validate(value, SCHEME_CHAR_HI, SCHEME_CHAR_LO, false, false);
  }

  /**
   * Returns <code>true</code> if the specified <code>value</code> would be
   * valid as the opaque part component of a URI; <code>false</code>
   * otherwise.
   *
   * <p>A valid opaque part must be non-null, non-empty, and not contain the
   * <code>#</code> character.  In addition, its first character must not be
   * <code>/</code>
   */
  public static boolean validOpaquePart(String value)
  {
    return value != null && value.indexOf(FRAGMENT_SEPARATOR) == -1 && value.length() > 0 && value.charAt(0) != SEGMENT_SEPARATOR;

  // <p>A valid opaque part must be non-null and non-empty. It may contain
  // any allowed URI characters, but its first character may not be
  // <code>/</code>

    //return value != null && value.length() != 0 &&
    //  value.charAt(0) != SEGMENT_SEPARATOR &&
    //  validate(value, URIC_HI, URIC_LO, true, true);
  }

  /**
   * Returns <code>true</code> if the specified <code>value</code> would be
   * valid as the authority component of a URI; <code>false</code> otherwise.
   *
   * <p>A valid authority may be null or contain any characters except for
   * the following: <code>/ ? #</code>
   */
  public static boolean validAuthority(String value)
  {
    return value == null || !contains(value, SEGMENT_END_HI, SEGMENT_END_LO);

  // A valid authority may be null or contain any allowed URI characters except
  // for the following: <code>/ ?</code>

    //return value == null || validate(value, SEGMENT_CHAR_HI, SEGMENT_CHAR_LO, true, true);
  }

  /**
   * Returns <code>true</code> if the specified <code>value</code> would be
   * valid as the authority component of an <a
   * href="#archive_explanation">archive URI</a>; <code>false</code>
   * otherwise.
   *
   * <p>To be valid, the authority, itself, must be a URI with no fragment or query,
   * followed by the character <code>!</code>.
   */
  public static boolean validArchiveAuthority(String value)
  {
    if (value != null && value.length() > 0 && value.charAt(value.length() - 1) == ARCHIVE_IDENTIFIER)
    {
      try
      {
        URI archiveURI = createURI(value.substring(0, value.length() - 1));
        return !archiveURI.hasFragment();
      }
      catch (IllegalArgumentException e)
      {
          e.getMessage();
      	}
     }
    return false;
  }

  /**
   * Tests whether the specified <code>value</code> would be valid as the
   * authority component of an <a href="#archive_explanation">archive
   * URI</a>. This method has been replaced by {@link #validArchiveAuthority
   * validArchiveAuthority} since the same form of URI is now supported
   * for schemes other than "jar". This now simply calls that method.
   *
   * @deprecated As of EMF 2.0, replaced by {@link #validArchiveAuthority
   * validArchiveAuthority}.
   */
  @Deprecated
  public static boolean validJarAuthority(String value)
  {
    return validArchiveAuthority(value);
  }

  /**
   * Returns <code>true</code> if the specified <code>value</code> would be
   * valid as the device component of a URI; <code>false</code> otherwise.
   *
   * <p>A valid device may be null or non-empty, containing any characters
   * except for the following: <code>/ ? #</code>  In addition, its last
   * character must be <code>:</code>
   */
  public static boolean validDevice(String value)
  {
    if (value == null) return true;
    int len = value.length();
    return len > 0 && value.charAt(len - 1) == DEVICE_IDENTIFIER && !contains(value, SEGMENT_END_HI, SEGMENT_END_LO);
  }

  /**
   * Returns <code>true</code> if the specified <code>value</code> would be
   * a valid path segment of a URI; <code>false</code> otherwise.
   *
   * <p>A valid path segment must be non-null and not contain any of the
   * following characters: <code>/ ? #</code>
   */
  public static boolean validSegment(String value)
  {
    return value != null && !contains(value, SEGMENT_END_HI, SEGMENT_END_LO);

  // <p>A valid path segment must be non-null and may contain any allowed URI
  // characters except for the following: <code>/ ?</code>

    //return value != null && validate(value, SEGMENT_CHAR_HI, SEGMENT_CHAR_LO, true, true);
  }

  /**
   * Returns <code>true</code> if the specified <code>value</code> would be
   * a valid path segment array of a URI; <code>false</code> otherwise.
   *
   * <p>A valid path segment array must be non-null and contain only path
   * segments that are valid according to {@link #validSegment validSegment}.
   */
  public static boolean validSegments(String[] value)
  {
    if (value == null) return false;
    for (int i = 0, len = value.length; i < len; i++)
    {
      if (!validSegment(value[i])) return false;
    }
    return true;
  }

  // Returns null if the specified value is null or would be a valid path
  // segment array of a URI; otherwise, the value of the first invalid
  // segment.
  protected static String firstInvalidSegment(String[] value)
  {
    if (value == null) return null;
    for (int i = 0, len = value.length; i < len; i++)
    {
      if (!validSegment(value[i])) return value[i];
    }
    return null;
  }

  /**
   * Returns <code>true</code> if the specified <code>value</code> would be
   * valid as the query component of a URI; <code>false</code> otherwise.
   *
   * <p>A valid query may be null or contain any characters except for
   * <code>#</code>
   */
  public static boolean validQuery(String value)
  {
    return value == null || value.indexOf(FRAGMENT_SEPARATOR) == -1;

  // <p>A valid query may be null or contain any allowed URI characters.

    //return value == null || validate(value, URIC_HI, URIC_LO, true, true);
}

  /**
   * Returns <code>true</code> if the specified <code>value</code> would be
   * valid as the fragment component of a URI; <code>false</code> otherwise.
   *
   * <p>A fragment is taken to be unconditionally valid.
   */
  public static boolean validFragment(String value)
  {
    return true;

  // <p>A valid fragment may be null or contain any allowed URI characters.

    //return value == null || validate(value, URIC_HI, URIC_LO, true, true);
  }

  // Searches the specified string for any characters in the set represented
  // by the 128-bit bitmask.  Returns true if any occur, or false otherwise.
  //
  protected static boolean contains(String s, long highBitmask, long lowBitmask)
  {
    for (int i = 0, len = s.length(); i < len; i++)
    {
      if (matches(s.charAt(i), highBitmask, lowBitmask)) return true;
    }
    return false;
  }

  protected void flushCachedString()
  {
    // Do nothing.
  }

  protected void cacheString(String string)
  {
    // Do nothing.
  }

  protected String getCachedString()
  {
    return null;
  }

  /**
   * Returns <code>true</code> if this is a relative URI, or
   * <code>false</code> if it is an absolute URI.
   */
  public boolean isRelative()
  {
    return false;
  }

  // Whether this this URI valid has a base URI against which to resolve.
  //
  protected boolean isBase()
  {
    return false;
  }

  /**
   * Returns <code>true</code> if this a a hierarchical URI, or
   * <code>false</code> if it is of the generic form.
   */
  public boolean isHierarchical()
  {
    return false;
  }

  /**
   * Returns <code>true</code> if this is a hierarchical URI with an authority
   * component; <code>false</code> otherwise.
   */
  public boolean hasAuthority()
  {
    return false;
  }

  /**
   * Returns <code>true</code> if this is a non-hierarchical URI with an
   * opaque part component; <code>false</code> otherwise.
   */
  public boolean hasOpaquePart()
  {
    // note: hierarchical -> authority != null
    return false;
  }

  /**
   * Returns <code>true</code> if this is a hierarchical URI with a device
   * component; <code>false</code> otherwise.
   */
  public boolean hasDevice()
  {
    // note: device != null -> hierarchical
    return false;
  }

  protected boolean hasDeviceOrPath()
  {
    return false;
  }

  /**
   * Returns <code>true</code> if this is a hierarchical URI with an
   * absolute or relative path; <code>false</code> otherwise.
   */
  public boolean hasPath()
  {
    // note: (absolutePath || authority == null) -> hierarchical
    // (authority == null && device == null && !absolutePath) -> scheme == null
    return false;
  }

  /**
   * Returns <code>true</code> if this is a hierarchical URI with an
   * absolute path, or <code>false</code> if it is non-hierarchical, has no
   * path, or has a relative path.
   */
  public boolean hasAbsolutePath()
  {
    // note: absolutePath -> hierarchical
    return false;
  }

  /**
   * Returns <code>true</code> if this is a hierarchical URI with a relative
   * path, or <code>false</code> if it is non-hierarchical, has no path, or
   * has an absolute path.
   */
  public boolean hasRelativePath()
  {
    // note: authority == null -> hierarchical
    // (authority == null && device == null && !absolutePath) -> scheme == null
    return false;
  }

  /**
   * Returns <code>true</code> if this is a hierarchical URI with an empty
   * relative path; <code>false</code> otherwise.
   *
   * <p>Note that <code>!hasEmpty()</code> does <em>not</em> imply that this
   * URI has any path segments; however, <code>hasRelativePath &&
   * !hasEmptyPath()</code> does.
   */
  public boolean hasEmptyPath()
  {
    // note: authority == null -> hierarchical
    // (authority == null && device == null && !absolutePath) -> scheme == null
    return false;
  }

  /**
   * Returns <code>true</code> if this is a hierarchical URI with a query
   * component; <code>false</code> otherwise.
   */
  public boolean hasQuery()
  {
    // note: query != null -> hierarchical
    return false;
  }

  /**
   * Returns <code>true</code> if this URI has a fragment component;
   * <code>false</code> otherwise.
   */
  public boolean hasFragment()
  {
    return false;
  }

  /**
   * Returns <code>true</code> if this is a current document reference; that
   * is, if it is a relative hierarchical URI with no authority, device or
   * query components, and no path segments; <code>false</code> is returned
   * otherwise.
   */
  public boolean isCurrentDocumentReference()
  {
    // note: authority == null -> hierarchical
    // (authority == null && device == null && !absolutePath) -> scheme == null
    return false;
  }

  /**
   * Returns <code>true</code> if this is a {@link
   * #isCurrentDocumentReference() current document reference} with no
   * fragment component; <code>false</code> otherwise.
   *
   * @see #isCurrentDocumentReference()
   */
  public boolean isEmpty()
  {
    // note: authority == null -> hierarchical
    // (authority == null && device == null && !absolutePath) -> scheme == null
    return false;
  }

  /**
   * Returns <code>true</code> if this is a hierarchical URI that may refer
   * directly to a locally accessible file.  This is considered to be the
   * case for a file-scheme absolute URI, or for a relative URI with no query;
   * <code>false</code> is returned otherwise.
   */
  public boolean isFile()
  {
    return false;
  }

  /**
   * Returns <code>true</code> if this is a platform URI, that is, an absolute,
   * hierarchical URI, with "platform" scheme, no authority, and at least two
   * segments; <code>false</code> is returned otherwise.
   * @since org.eclipse.emf.common 2.3
   */
  public boolean isPlatform()
  {
    return false;
  }

  /**
   * Returns <code>true</code> if this is a platform resource URI, that is,
   * a {@link #isPlatform platform URI} whose first segment is "resource";
   * <code>false</code> is returned otherwise.
   * @see #isPlatform
   * @since org.eclipse.emf.common 2.3
   */
  public boolean isPlatformResource()
  {
    return false;
  }

  /**
   * Returns <code>true</code> if this is a platform plug-in URI, that is,
   * a {@link #isPlatform platform URI} whose first segment is "plugin";
   * <code>false</code> is returned otherwise.
   * @see #isPlatform
   * @since org.eclipse.emf.common 2.3
   */
  public boolean isPlatformPlugin()
  {
    return false;
  }

  /**
   * Returns <code>true</code> if this is an archive URI.  If so, it is also
   * hierarchical, with an authority (consisting of an absolute URI followed
   * by "!"), no device, and an absolute path.
   */
  public boolean isArchive()
  {
    return false;
  }

  /**
   * Returns <code>true</code> if the specified <code>value</code> would be
   * valid as the scheme of an <a
   * href="#archive_explanation">archive URI</a>; <code>false</code>
   * otherwise.
   */
  public static boolean isArchiveScheme(String value)
  {
    // Returns true if the given value is an archive scheme, as defined by
    // the URI.archiveSchemes system property.
    // By default, "jar", "zip", and "archive" are considered archives.
    for (String scheme : ARCHIVE_SCHEMES)
    {
      if (scheme.equals(value))
      {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns the hash code.
   */
  @Override
  public int hashCode()
  {
    return hashCode;
  }
  
  @Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
	  if(obj!=null)
		return this.hashCode == obj.hashCode();	
	  return false;
  }

  // Tests whether this URI's path segment array is equal to that of the given uri.
  protected boolean segmentsEqual(URI uri)
  {
    return false;
  }

  // Tests two objects for equality, tolerating nulls; null is considered
  // to be a valid value that is only equal to itself.
  protected static boolean equals(Object o1, Object o2)
  {
    return o1 == o2 || o1 != null && o1.equals(o2);
  }

  /**
   * If this is an absolute URI, returns the scheme component;
   * <code>null</code> otherwise.
   */
  public String scheme()
  {
    return null;
  }

  /**
   * If this is a non-hierarchical URI, returns the opaque part component;
   * <code>null</code> otherwise.
   */
  public String opaquePart()
  {
    return null;
  }

  /**
   * If this is a hierarchical URI with an authority component, returns it;
   * <code>null</code> otherwise.
   */
  public String authority()
  {
    return null;
  }

  /**
   * If this is a hierarchical URI with an authority component that has a
   * user info portion, returns it; <code>null</code> otherwise.
   */
  public String userInfo()
  {
    return null;
  }

  /**
   * If this is a hierarchical URI with an authority component that has a
   * host portion, returns it; <code>null</code> otherwise.
   */
  public String host()
  {
    return null;
  }

  /**
   * If this is a hierarchical URI with an authority component that has a
   * port portion, returns it; <code>null</code> otherwise.
   */
  public String port()
  {
    return null;
  }

  /**
   * If this is a hierarchical URI with a device component, returns it;
   * <code>null</code> otherwise.
   */
  public String device()
  {
    return null;
  }

  /**
   * If this is a hierarchical URI with a path, returns an array containing
   * the segments of the path; an empty array otherwise.  The leading
   * separator in an absolute path is not represented in this array, but a
   * trailing separator is represented by an empty-string segment as the
   * final element.
   */
  public String[] segments()
  {
    return NO_SEGMENTS;
  }

  // Directly returns the underlying segments without cloning them.
  //
  protected String[] rawSegments()
  {
    return NO_SEGMENTS;
  }

  /**
   * Returns an unmodifiable list containing the same segments as the array
   * returned by {@link #segments segments}.
   */
  public List<String> segmentsList()
  {
    return Collections.emptyList();
  }

  /**
   * Returns the number of elements in the segment array that would be
   * returned by {@link #segments segments}.
   */
  public int segmentCount()
  {
    return 0;
  }

  /**
   * Provides fast, indexed access to individual segments in the path
   * segment array.
   *
   * @exception java.lang.IndexOutOfBoundsException if <code>i < 0</code> or
   * <code>i >= segmentCount()</code>.
   */
  public String segment(int i)
  {
    throw new IndexOutOfBoundsException("No such segment: " + i);
  }

  /**
   * Returns the last segment in the segment array, or <code>null</code>.
   */
  public String lastSegment()
  {
    return null;
  }

  /**
   * If this is a hierarchical URI with a path, returns a string
   * representation of the path; <code>null</code> otherwise.  The path
   * consists of a leading segment separator character (a slash), if the
   * path is absolute, followed by the slash-separated path segments.  If
   * this URI has a separate <a href="#device_explanation">device
   * component</a>, it is <em>not</em> included in the path.
   */
  public String path()
  {
    return null;
  }

  /**
   * If this is a hierarchical URI with a path, returns a string
   * representation of the path, including the authority and the
   * <a href="#device_explanation">device component</a>;
   * <code>null</code> otherwise.
   *
   * <p>If there is no authority, the format of this string is:
   * <pre>
   *   device/pathSegment1/pathSegment2...</pre>
   *
   * <p>If there is an authority, it is:
   * <pre>
   *   //authority/device/pathSegment1/pathSegment2...</pre>
   *
   * <p>For an <a href="#archive_explanation">archive URI</a>, it's just:
   * <pre>
   *   authority/pathSegment1/pathSegment2...</pre>
   */
  public String devicePath()
  {
    return null;
  }

  /**
   * If this is a hierarchical URI with a query component, returns it;
   * <code>null</code> otherwise.
   */
  public String query()
  {
    return null;
  }

  /**
   * Returns the URI formed from this URI and the given query.
   *
   * @exception java.lang.IllegalArgumentException if
   * <code>query</code> is not a valid query (portion) according
   * to {@link #validQuery validQuery}.
   */
  public URI appendQuery(String query)
  {
    if (!validQuery(query))
    {
      throw new IllegalArgumentException("invalid query portion: " + query);
    }
    return this;
  }

  /**
   * If this URI has a non-null {@link #query query}, returns the URI
   * formed by removing it; this URI unchanged, otherwise.
   */
  public URI trimQuery()
  {
    return this;
  }

  /**
   * If this URI has a fragment component, returns it; <code>null</code> otherwise.
   */
  public String fragment()
  {
    return null;
  }

  /**
   * Returns the URI formed from this URI and the given fragment.
   *
   * @exception java.lang.IllegalArgumentException if
   * <code>fragment</code> is not a valid fragment (portion) according
   * to {@link #validFragment validFragment}.
   */
  public URI appendFragment(String fragment)
  {
    if (fragment == null)
    {
      return this;
    }
    else
    {
      if (POOL.externalQueue != null)
      {
        final Fragment result = new Fragment(0, this, fragment);
        new LazyFragmentInitializer(result, POOL.externalQueue, fragment);
        return result;
      }
      else
      {
        int hashCode = (this.hashCode * 31+ FRAGMENT_SEPARATOR) * CommonUtil.powerOf31(fragment.length()) + fragment.hashCode();
        return new Fragment(hashCode, this, splitInternFragment(fragment));
      }
    }
  }

  // Returns the URI formed from this uri and the already properly interned fragment representation.
  //
  protected URI rawAppendFragment(CharSequence fragment)
  {
    if (fragment == null)
    {
      return this;
    }
    else
    {
      int hashCode = (this.hashCode * 31 + FRAGMENT_SEPARATOR) * CommonUtil.powerOf31(fragment.length()) + fragment.hashCode();
      return new Fragment(hashCode, this, fragment);
    }
  }

  /**
   * If this URI has a non-null {@link #fragment fragment}, returns the URI
   * formed by removing it; this URI unchanged, otherwise.
   */
  public URI trimFragment()
  {
    return this;
  }

  /**
   * Resolves this URI reference against a <code>base</code> absolute
   * hierarchical URI, returning the resulting absolute URI.  If already
   * absolute, the URI itself is returned.  URI resolution is described in
   * detail in section 5.2 of <a href="http://www.ietf.org/rfc/rfc2396.txt">RFC
   * 2396</a>, "Resolving Relative References to Absolute Form."
   *
   * <p>During resolution, empty segments, self references ("."), and parent
   * references ("..") are interpreted, so that they can be removed from the
   * path.  Step 6(g) gives a choice of how to handle the case where parent
   * references point to a path above the root: the offending segments can
   * be preserved or discarded.  This method preserves them.  To have them
   * discarded, please use the two-parameter form of {@link
   * #resolve(URI, boolean) resolve}.
   *
   * @exception java.lang.IllegalArgumentException if <code>base</code> is
   * non-hierarchical or is relative.
   */
  public URI resolve(URI base)
  {
    return resolve(base, true);
  }

  /**
   * Resolves this URI reference against a <code>base</code> absolute
   * hierarchical URI, returning the resulting absolute URI.  If already
   * absolute, the URI itself is returned.  URI resolution is described in
   * detail in section 5.2 of <a href="http://www.ietf.org/rfc/rfc2396.txt">RFC
   * 2396</a>, "Resolving Relative References to Absolute Form."
   *
   * <p>During resolution, empty segments, self references ("."), and parent
   * references ("..") are interpreted, so that they can be removed from the
   * path.  Step 6(g) gives a choice of how to handle the case where parent
   * references point to a path above the root: the offending segments can
   * be preserved or discarded.  This method can do either.
   *
   * @param preserveRootParents <code>true</code> if segments referring to the
   * parent of the root path are to be preserved; <code>false</code> if they
   * are to be discarded.
   *
   * @exception java.lang.IllegalArgumentException if <code>base</code> is
   * non-hierarchical or is relative.
   */
  public URI resolve(URI base, boolean preserveRootParents)
  {
    if (!base.isHierarchical() || base.isRelative())
    {
      throw new IllegalArgumentException(
        "resolve against non-hierarchical or relative base");
    }
    return this;
  }

  /**
   * Finds the shortest relative or, if necessary, the absolute URI that,
   * when resolved against the given <code>base</code> absolute hierarchical
   * URI using {@link #resolve(URI) resolve}, will yield this absolute URI.
   * If <code>base</code> is non-hierarchical or is relative,
   * or <code>this</code> is non-hierarchical or is relative,
   * <code>this</code> will be returned.
   */
  public URI deresolve(URI base)
  {
    return deresolve(base, true, false, true);
  }

  /**
   * Finds an absolute URI that, when resolved against the given
   * <code>base</code> absolute hierarchical URI using {@link
   * #resolve(URI, boolean) resolve}, will yield this absolute URI.
   * If <code>base</code> is non-hierarchical or is relative,
   * or <code>this</code> is non-hierarchical or is relative,
   * <code>this</code> will be returned.
   *
   * @param preserveRootParents the boolean argument to <code>resolve(URI,
   * boolean)</code> for which the returned URI should resolve to this URI.
   * @param anyRelPath if <code>true</code>, the returned URI's path (if
   * any) will be relative, if possible.  If <code>false</code>, the form of
   * the result's path will depend upon the next parameter.
   * @param shorterRelPath if <code>anyRelPath</code> is <code>false</code>
   * and this parameter is <code>true</code>, the returned URI's path (if
   * any) will be relative, if one can be found that is no longer (by number
   * of segments) than the absolute path.  If both <code>anyRelPath</code>
   * and this parameter are <code>false</code>, it will be absolute.
   */
  public URI deresolve(URI base, boolean preserveRootParents, boolean anyRelPath, boolean shorterRelPath)
  {
    return this;
  }

  protected String[] collapseSegments(boolean preserveRootParents)
  {
    return NO_SEGMENTS;
  }

  // Returns whether the string representation of the URI fully matches the given string.
  //
  protected boolean matches(String string)
  {
    return false;
  }

  // Used to match a URI against the specified components.
  //
  protected boolean matches(int validate, boolean hierarchical, String scheme, String authority, String device, boolean absolutePath, String[] segments, String query)
  {
    return false;
  }

  // Used to match a platform URI composed from these two components.
  //
  protected boolean matches(String base, String path)
  {
    return false;
  }

  /**
   * If this URI may refer directly to a locally accessible file, as
   * determined by {@link #isFile isFile}, {@link #decode decodes} and formats
   * the URI as a pathname to that file; returns null otherwise.
   *
   * <p>If there is no authority, the format of this string is:
   * <pre>
   *   device/pathSegment1/pathSegment2...</pre>
   *
   * <p>If there is an authority, it is:
   * <pre>
   *   //authority/device/pathSegment1/pathSegment2...</pre>
   *
   * <p>However, the character used as a separator is system-dependent and
   * obtained from {@link java.io.File#separatorChar}.
   */
  public String toFileString()
  {
    return null;
  }

  /**
   * If this is a platform URI, as determined by {@link #isPlatform}, returns
   * the workspace-relative or plug-in-based path to the resource, optionally
   * {@link #decode decoding} the segments in the process.
   * @see #createPlatformResourceURI(String, boolean)
   * @see #createPlatformPluginURI
   * @since org.eclipse.emf.common 2.3
   */
  public String toPlatformString(boolean decode)
  {
    return null;
  }

  /**
   * Returns the URI formed by appending the specified segment on to the end
   * of the path of this URI, if hierarchical; this URI unchanged,
   * otherwise.  If this URI has an authority and/or device, but no path,
   * the segment becomes the first under the root in an absolute path.
   *
   * @exception java.lang.IllegalArgumentException if <code>segment</code>
   * is not a valid segment according to {@link #validSegment}.
   */
  public URI appendSegment(String segment)
  {
    if (!validSegment(segment))
    {
      throw new IllegalArgumentException("invalid segment: " + segment);
    }

    return this;
  }

  /**
   * Returns the URI formed by appending the specified segments on to the
   * end of the path of this URI, if hierarchical; this URI unchanged,
   * otherwise.  If this URI has an authority and/or device, but no path,
   * the segments are made to form an absolute path.
   *
   * @param segments an array of non-null strings, each representing one
   * segment of the path.  If desired, a trailing separator should be
   * represented by an empty-string segment as the last element of the
   * array.
   *
   * @exception java.lang.IllegalArgumentException if <code>segments</code>
   * is not a valid segment array according to {@link #validSegments}.
   */
  public URI appendSegments(String[] segments)
  {
    if (!validSegments(segments))
    {
      String s = segments == null ? "invalid segments: null" : "invalid segment: " + firstInvalidSegment(segments);
      throw new IllegalArgumentException(s);
    }

    return this;
  }

  /**
   * Returns the URI formed by trimming the specified number of segments
   * (including empty segments, such as one representing a trailing
   * separator) from the end of the path of this URI, if hierarchical;
   * otherwise, this URI is returned unchanged.
   *
   * <p>Note that if all segments are trimmed from an absolute path, the
   * root absolute path remains.
   *
   * @param i the number of segments to be trimmed in the returned URI.  If
   * less than 1, this URI is returned unchanged; if equal to or greater
   * than the number of segments in this URI's path, all segments are
   * trimmed.
   */
  public URI trimSegments(int i)
  {
    return this;
  }

  /**
   * Returns <code>true</code> if this is a hierarchical URI that has a path
   * that ends with a trailing separator; <code>false</code> otherwise.
   *
   * <p>A trailing separator is represented as an empty segment as the
   * last segment in the path; note that this definition does <em>not</em>
   * include the lone separator in the root absolute path.
   */
  public boolean hasTrailingPathSeparator()
  {
    return false;
  }

  /**
   * If this is a hierarchical URI whose path includes a file extension,
   * that file extension is returned; null otherwise.  We define a file
   * extension as any string following the last period (".") in the final
   * path segment.  If there is no path, the path ends in a trailing
   * separator, or the final segment contains no period, then we consider
   * there to be no file extension.  If the final segment ends in a period,
   * then the file extension is an empty string.
   */
  public String fileExtension()
  {
    return null;
  }

  /**
   * Returns the URI formed by appending a period (".") followed by the
   * specified file extension to the last path segment of this URI, if it is
   * hierarchical with a non-empty path ending in a non-empty segment;
   * otherwise, this URI is returned unchanged.

   * <p>The extension is appended regardless of whether the segment already
   * contains an extension.
   *
   * @exception java.lang.IllegalArgumentException if
   * <code>fileExtension</code> is not a valid segment (portion) according
   * to {@link #validSegment}.
   */
  public URI appendFileExtension(String fileExtension)
  {
    if (!validSegment(fileExtension))
    {
      throw new IllegalArgumentException("invalid segment portion: " + fileExtension);
    }

    return this;
  }

  /**
   * If this URI has a non-null {@link #fileExtension fileExtension},
   * returns the URI formed by removing it; this URI unchanged, otherwise.
   */
  public URI trimFileExtension()
  {
    return this;
  }

  /**
   * Returns <code>true</code> if this is a hierarchical URI that ends in a
   * slash; that is, it has a trailing path separator or is the root
   * absolute path, and has no query and no fragment; <code>false</code>
   * is returned otherwise.
   */
  public boolean isPrefix()
  {
    return false;
  }

  /**
   * If this is a hierarchical URI reference and <code>oldPrefix</code> is a
   * prefix of it, this returns the URI formed by replacing it by
   * <code>newPrefix</code>; <code>null</code> otherwise.
   *
   * <p>In order to be a prefix, the <code>oldPrefix</code>'s
   * {@link #isPrefix isPrefix} must return <code>true</code>, and it must
   * match this URI's scheme, authority, and device.  Also, the paths must
   * match, up to prefix's end.
   *
   * @exception java.lang.IllegalArgumentException if either
   * <code>oldPrefix</code> or <code>newPrefix</code> is not a prefix URI
   * according to {@link #isPrefix}.
   */
  public URI replacePrefix(URI oldPrefix, URI newPrefix)
  {
    if (!oldPrefix.isPrefix() || !newPrefix.isPrefix())
    {
      String which = oldPrefix.isPrefix() ? "new" : "old";
      throw new IllegalArgumentException("non-prefix " + which + " value");
    }

    return null;
  }

  /**
   * Encodes a string so as to produce a valid opaque part value, as defined
   * by the RFC.  All excluded characters, such as space and <code>#</code>,
   * are escaped, as is <code>/</code> if it is the first character.
   *
   * @param ignoreEscaped <code>true</code> to leave <code>%</code> characters
   * unescaped if they already begin a valid three-character escape sequence;
   * <code>false</code> to encode all <code>%</code> characters.  Note that
   * if a <code>%</code> is not followed by 2 hex digits, it will always be
   * escaped.
   */
  public static String encodeOpaquePart(String value, boolean ignoreEscaped)
  {
    String result = encode(value, URIC_HI, URIC_LO, ignoreEscaped);
    return
      result != null && result.length() > 0 && result.charAt(0) == SEGMENT_SEPARATOR ? "%2F" + result.substring(1) : result;
  }

  /**
   * Encodes a string so as to produce a valid authority, as defined by the
   * RFC.  All excluded characters, such as space and <code>#</code>,
   * are escaped, as are <code>/</code> and <code>?</code>
   *
   * @param ignoreEscaped <code>true</code> to leave <code>%</code> characters
   * unescaped if they already begin a valid three-character escape sequence;
   * <code>false</code> to encode all <code>%</code> characters.  Note that
   * if a <code>%</code> is not followed by 2 hex digits, it will always be
   * escaped.
   */
  public static String encodeAuthority(String value, boolean ignoreEscaped)
  {
    return encode(value, SEGMENT_CHAR_HI, SEGMENT_CHAR_LO, ignoreEscaped);
  }

  /**
   * Encodes a string so as to produce a valid segment, as defined by the
   * RFC.  All excluded characters, such as space and <code>#</code>,
   * are escaped, as are <code>/</code> and <code>?</code>
   *
   * @param ignoreEscaped <code>true</code> to leave <code>%</code> characters
   * unescaped if they already begin a valid three-character escape sequence;
   * <code>false</code> to encode all <code>%</code> characters.  Note that
   * if a <code>%</code> is not followed by 2 hex digits, it will always be
   * escaped.
   */
  public static String encodeSegment(String value, boolean ignoreEscaped)
  {
    return encode(value, SEGMENT_CHAR_HI, SEGMENT_CHAR_LO, ignoreEscaped);
  }

  /**
   * Encodes a string so as to produce a valid query, as defined by the RFC.
   * Only excluded characters, such as space and <code>#</code>, are escaped.
   *
   * @param ignoreEscaped <code>true</code> to leave <code>%</code> characters
   * unescaped if they already begin a valid three-character escape sequence;
   * <code>false</code> to encode all <code>%</code> characters.  Note that
   * if a <code>%</code> is not followed by 2 hex digits, it will always be
   * escaped.
   */
  public static String encodeQuery(String value, boolean ignoreEscaped)
  {
    return encode(value, URIC_HI, URIC_LO, ignoreEscaped);
  }

  /**
   * Encodes a string so as to produce a valid fragment, as defined by the
   * RFC.  Only excluded characters, such as space and <code>#</code>, are
   * escaped.
   *
   * @param ignoreEscaped <code>true</code> to leave <code>%</code> characters
   * unescaped if they already begin a valid three-character escape sequence;
   * <code>false</code> to encode all <code>%</code> characters.  Note that
   * if a <code>%</code> is not followed by 2 hex digits, it will always be
   * escaped.
   */
  public static String encodeFragment(String value, boolean ignoreEscaped)
  {
    return encode(value, URIC_HI, URIC_LO, ignoreEscaped);
  }

  // Encodes a complete URI, optionally leaving % characters unescaped when
  // beginning a valid three-character escape sequence.  We can either treat
  // the first or # as a fragment separator, or encode them all.
  protected static String encodeURI(String uri, boolean ignoreEscaped, int fragmentLocationStyle)
  {
    if (uri == null) return null;

    StringBuffer result = new StringBuffer();

    int i = uri.indexOf(SCHEME_SEPARATOR);
    if (i != -1)
    {
      String scheme = uri.substring(0, i);
      result.append(scheme);
      result.append(SCHEME_SEPARATOR);
    }

    int j =
      fragmentLocationStyle == FRAGMENT_FIRST_SEPARATOR ?
        uri.indexOf(FRAGMENT_SEPARATOR) :
        fragmentLocationStyle == FRAGMENT_LAST_SEPARATOR ?
          uri.lastIndexOf(FRAGMENT_SEPARATOR) :
          -1;

    if (j != -1)
    {
      String sspart = uri.substring(++i, j);
      result.append(encode(sspart, URIC_HI, URIC_LO, ignoreEscaped));
      result.append(FRAGMENT_SEPARATOR);

      String fragment = uri.substring(++j);
      result.append(encode(fragment, URIC_HI, URIC_LO, ignoreEscaped));
    }
    else
    {
      String sspart = uri.substring(++i);
      result.append(encode(sspart, URIC_HI, URIC_LO, ignoreEscaped));
    }

    return result.toString();
  }

  // Encodes the given string, replacing each ASCII character that is not in
  // the set specified by the 128-bit bitmask and each non-ASCII character
  // below 0xA0 by an escape sequence of % followed by two hex digits.  If
  // % is not in the set but ignoreEscaped is true, then % will not be encoded
  // iff it already begins a valid escape sequence.
  protected static String encode(String value, long highBitmask, long lowBitmask, boolean ignoreEscaped)
  {
    if (value == null) return null;

    StringBuffer result = null;

    for (int i = 0, len = value.length(); i < len; i++)
    {
      char c = value.charAt(i);

      if (!matches(c, highBitmask, lowBitmask) && c < 160 && (!ignoreEscaped || !isEscaped(value, i)))
      {
        if (result == null)
        {
          result = new StringBuffer(value.substring(0, i));
        }
        appendEscaped(result, (byte)c);
      }
      else if (result != null)
      {
        result.append(c);
      }
    }
    return result == null ? value : result.toString();
  }

  // Tests whether an escape occurs in the given string, starting at index i.
  // An escape sequence is a % followed by two hex digits.
  protected static boolean isEscaped(String s, int i)
  {
    return s.charAt(i) == ESCAPE && s.length() > i + 2 && matches(s.charAt(i + 1), HEX_HI, HEX_LO) && matches(s.charAt(i + 2), HEX_HI, HEX_LO);
  }

  // Computes a three-character escape sequence for the byte, appending
  // it to the StringBuffer.  Only characters up to 0xFF should be escaped;
  // all but the least significant byte will be ignored.
  protected static void appendEscaped(StringBuffer result, byte b)
  {
    result.append(ESCAPE);

    // The byte is automatically widened into an int, with sign extension,
    // for shifting.  This can introduce 1's to the left of the byte, which
    // must be cleared by masking before looking up the hex digit.
    //
    result.append(HEX_DIGITS[(b >> 4) & 0x0F]);
    result.append(HEX_DIGITS[b & 0x0F]);
  }

  /**
   * Decodes the given string by interpreting three-digit escape sequences as the bytes of a UTF-8 encoded character
   * and replacing them with the characters they represent.
   * Incomplete escape sequences are ignored and invalid UTF-8 encoded bytes are treated as extended ASCII characters.
   */
  public static String decode(String value)
  {
    if (value == null) return null;

    int i = value.indexOf('%');
    if (i < 0)
    {
      return value;
    }
    else
    {
      StringBuilder result = new StringBuilder(value.substring(0, i));
      byte [] bytes = new byte[4];
      int receivedBytes = 0;
      int expectedBytes = 0;
      for (int len = value.length(); i < len; i++)
      {
        if (isEscaped(value, i))
        {
          char character = unescape(value.charAt(i + 1), value.charAt(i + 2));
          i += 2;

          if (expectedBytes > 0)
          {
            if ((character & 0xC0) == 0x80)
            {
              bytes[receivedBytes++] = (byte)character;
            }
            else
            {
              expectedBytes = 0;
            }
          }
          else if (character >= 0x80)
          {
            if ((character & 0xE0) == 0xC0)
            {
              bytes[receivedBytes++] = (byte)character;
              expectedBytes = 2;
            }
            else if ((character & 0xF0) == 0xE0)
            {
              bytes[receivedBytes++] = (byte)character;
              expectedBytes = 3;
            }
            else if ((character & 0xF8) == 0xF0)
            {
              bytes[receivedBytes++] = (byte)character;
              expectedBytes = 4;
            }
          }

          if (expectedBytes > 0)
          {
            if (receivedBytes == expectedBytes)
            {
              switch (receivedBytes)
              {
                case 2:
                {
                  result.append((char)((bytes[0] & 0x1F) << 6 | bytes[1] & 0x3F));
                  break;
                }
                case 3:
                {
                  result.append((char)((bytes[0] & 0xF) << 12 | (bytes[1] & 0X3F) << 6 | bytes[2] & 0x3F));
                  break;
                }
                case 4:
                {
                  result.appendCodePoint((bytes[0] & 0x7) << 18 | (bytes[1] & 0X3F) << 12 | (bytes[2] & 0X3F) << 6 | bytes[3] & 0x3F);
                  break;
                }
              }
              receivedBytes = 0;
              expectedBytes = 0;
            }
          }
          else
          {
            for (int j = 0; j < receivedBytes; ++j)
            {
              result.append((char)bytes[j]);
            }
            receivedBytes = 0;
            result.append(character);
          }
        }
        else
        {
          for (int j = 0; j < receivedBytes; ++j)
          {
            result.append((char)bytes[j]);
          }
          receivedBytes = 0;
          result.append(value.charAt(i));
        }
      }
      return result.toString();
    }
  }

  // Returns the character encoded by % followed by the two given hex digits,
  // which is always 0xFF or less, so can safely be casted to a byte.  If
  // either character is not a hex digit, a bogus result will be returned.
  protected static char unescape(char highHexDigit, char lowHexDigit)
  {
    return (char)((valueOf(highHexDigit) << 4) | valueOf(lowHexDigit));
  }

  // Returns the int value of the given hex digit.
  protected static int valueOf(char hexDigit)
  {
    if (hexDigit <= '9')
    {
      if (hexDigit >= '0')
      {
        return hexDigit - '0';
      }
    }
    else if (hexDigit <= 'F')
    {
      if (hexDigit >= 'A')
      {
        return hexDigit - 'A' + 10;
      }
    }
    else if (hexDigit >= 'a' && hexDigit <= 'f')
    {
      return hexDigit - 'a' + 10;
    }

    return 0;
  }
}
