package org.eclipse.emf.common.util;

import java.io.File;

import org.eclipse.emf.common.util.Pool.AccessUnit;

/**
 * An access units for platform URI string-based access.
 */
class PlatformAccessUnit extends URIPoolAccessUnitBase
{
  protected static class Queue extends AccessUnit.Queue<URI>
  {
    private static final long serialVersionUID = 1L;

    @Override
    public PlatformAccessUnit pop(boolean isExclusive)
    {
      return (PlatformAccessUnit)super.pop(isExclusive);
    }

    @Override
    protected AccessUnit<URI> newAccessUnit()
    {
      return new PlatformAccessUnit(this);
    }
  }

  /**
   * The hash code of <code>"platform:/resource/"</code>.
   */
  protected static final int PLATFORM_RESOURCE_BASE_FULL_HASH_CODE = "platform:/resource/".hashCode();

  /**
   * The hash code of <code>"platform:/plugin/"</code>.
   */
  protected static final int PLATFORM_PLUGIN_BASE_FULL_HASH_CODE = "platform:/plugin/".hashCode();

  /**
   * The hash code of <code>"platform:/resource"</code>.
   */
  protected static final int PLATFORM_RESOURCE_BASE_INITIAL_HASH_CODE = "platform:/resource".hashCode();

  /**
   * The hash code of <code>"platform:/plugin/"</code>.
   */
  protected static final int PLATFORM_PLUGIN_BASE_INITIAL_HASH_CODE = "platform:/plugin".hashCode();

  /**
   * The base that implicitly precedes the {@link #path}.
   */
  protected String base;

  /**
   * The path being accessed.
   */
  protected String path;

  /**
   * Whether the pathName needs encoding.
   */
  protected boolean encode;

  /**
   * A buffer uses for processing the path.
   */
  protected char[] characters = new char[100];

  /**
   * The accumulated segments pulled from the path.
   */
  protected String[] segments = new String[20];

  /**
   * The number of {@link #segments}.
   */
  protected int segmentCount;

  /**
   * The number of segments populated with strings during intern that need to be nulled during reset.
   */
  protected int usedSegmentCount;

  /**
   * The boundaries of the path segments.
   */
  protected int[] segmentBoundaries = new int[100];

  /**
   * The hash code of the path segments.
   */
  protected int[] segmentHashCodes = new int[100];

  /**
   * The path after it's been encoded.
   */
  protected String encodedPath;

  /**
   * Creates and instance managed by the given queue.
   */
  protected PlatformAccessUnit(PlatformAccessUnit.Queue queue)
  {
    super(queue);
  }

  /**
   * Caches the parameters and computes the hash code, which can involve encoding the path.
   */
  protected void setValue(String base, String path, boolean encode)
  {
    this.base = base;
    this.path = path;
    this.encode = encode;

    int length = path.length();
    if (length == 0)
    {
      encodedPath = "/";
      segmentBoundaries[segmentCount] = 0;
      segmentBoundaries[segmentCount++] = 1;
      this.hashCode =  base.equals(URI.SEGMENT_RESOURCE) ? PLATFORM_RESOURCE_BASE_FULL_HASH_CODE : PLATFORM_PLUGIN_BASE_FULL_HASH_CODE;
    }
    else
    {
      // At most each character could need encoding and that would triple the length.
      // Even when not encoding, we still check for the ? and # and encode those.
      //
      int maxEncodedLength = 3 * length;
      if (characters.length <= maxEncodedLength)
      {
        // Leave room for one more character, i.e., the leading / that may need to be added.
        //
        characters = new char[maxEncodedLength + 1];
      }

      // There can be at most as many segments as characters.
      //
      if (segmentBoundaries.length < length)
      {
        segmentBoundaries = new int[length];
        segmentHashCodes = new int[length];
      }

      // Keep track of whether any characters were encoded.
      //
      boolean isModified = false;

      // Treat this character the same as a /.  In fact, on non-Wwindows systems this will be a / anyway.
      //
      char separatorchar = File.separatorChar;

      char character = path.charAt(0);
      if (character == URI.SEGMENT_SEPARATOR)
      {
        // If the path starts with a /, copy over all the characters into the buffer.
        //
        path.getChars(0, length, characters, 0);
      }
      else if (character == separatorchar)
      {
        // If the path starts with a \, put a / at the start and copy over all the characters except the first into the buffer.
        //
        characters[0] = URI.SEGMENT_SEPARATOR;
        if (length != 1)
        {
          path.getChars(1, length, characters, 1);
        }
        // Indicate that we've modified the original path.
        //
        isModified = true;
      }
      else
      {
        // It doesn't start with a separator character so put a / at the start and copy all the characters into the buffer after that.
        //
        characters[0] = URI.SEGMENT_SEPARATOR;
        path.getChars(0, length, characters, 1);

        // The string is now one character longer and we've modified the path.
        //
        ++length;
        isModified = true;
      }

      // The first character in the buffer is a /, so that's the initial hash code.
      //
      int hashCode = URI.SEGMENT_SEPARATOR;
      int segmentHashCode = 0;

      // Iterate over all the characters...
      //
      for (int i = 1; i < length; ++i)
      {
        // If the character is one that needs encoding, including the path separators or special characters.
        //
        character = characters[i];
        if (encode ? character < 160 && !URI.matches(character, URI.SEGMENT_CHAR_HI, URI.SEGMENT_CHAR_LO) : URI.matches(character, URI.PLATFORM_SEGMENT_RESERVED_HI, URI.PLATFORM_SEGMENT_RESERVED_LO))
        {
          if (character == URI.SEGMENT_SEPARATOR)
          {
            // If it's a /, cache the segment hash code, and boundary, reset the segment hash code, and compose the complete hash code.
            //
            segmentHashCodes[segmentCount] = segmentHashCode;
            segmentBoundaries[segmentCount++] = i;
            segmentHashCode = 0;
            hashCode = 31 * hashCode + URI.SEGMENT_SEPARATOR;
          }
          else if (character == separatorchar)
          {
            // If it's a \, convert it to a /, cache the segment hash code, and boundary, reset the segment hash code, and compose the complete hash code, and indicate we've modified the original path.
            //
            characters[i] = URI.SEGMENT_SEPARATOR;
            segmentHashCodes[segmentCount] = segmentHashCode;
            segmentBoundaries[segmentCount++] = i;
            segmentHashCode = 0;
            hashCode = 31 * hashCode + URI.SEGMENT_SEPARATOR;
            isModified = true;
          }
          else
          {
            // Escape the character.
            //
            isModified = true;

            // Shift the buffer to the right 3 characters.
            //
            System.arraycopy(characters, i + 1, characters, i + 3, length - i - 1);

            // Add a % and compose the segment hashCode and the complete hash code.
            //
            characters[i] = URI.ESCAPE;
            hashCode = 31 * hashCode + URI.ESCAPE;
            segmentHashCode = 31 * segmentHashCode + URI.ESCAPE;

            // Add the first hex digit and compose the segment hashCode and the complete hash code.
            //
            char firstHexCharacter = characters[++i] = URI.HEX_DIGITS[(character >> 4) & 0x0F];
            hashCode = 31 * hashCode + firstHexCharacter;
            segmentHashCode = 31 * segmentHashCode + firstHexCharacter;

            // Add the second hex digit and compose the segment hashCode and the complete hash code.
            //
            char secondHexCharacter = characters[++i] = URI.HEX_DIGITS[character & 0x0F];
            hashCode = 31 * hashCode + secondHexCharacter;
            segmentHashCode = 31 * segmentHashCode + secondHexCharacter;

            // The length is two characters bigger than before.
            //
            length += 2;
          }
        }
        else
        {
          // No encoding required, so just compose the segment hash code and the complete hash code.
          //
          hashCode = 31 * hashCode + character;
          segmentHashCode = 31 * segmentHashCode + character;
        }
      }

      // Set cache the final segment's hash code and boundary.
      //
      segmentHashCodes[segmentCount] = segmentHashCode;
      segmentBoundaries[segmentCount++] = length;

      // Compose the overall hash code to include the base's hash code.
      //
      this.hashCode = (base.equals(URI.SEGMENT_RESOURCE) ? PLATFORM_RESOURCE_BASE_INITIAL_HASH_CODE : PLATFORM_PLUGIN_BASE_INITIAL_HASH_CODE) * CommonUtil.powerOf31(length) + hashCode;
      encodedPath = isModified ? intern(characters, 0, length, hashCode) : path;
    }
  }

  @Override
  protected boolean matches(URI value)
  {
    return value.matches(base, encodedPath);
  }

  @Override
  public URI getInternalizedValue()
  {
    // Ensure that there are enough segments to hold the results.
    //
    if (segments.length <= segmentCount)
    {
      segments = new String[segmentCount + 1];
    }

    // Start with the given base segment.
    //
    segments[0] = base;

    // Compute the hash code of the segments array.
    // The offset is the start of the segment within the character's buffer, which is initially at index 1, i.e., after the leading /.
    //
    int hashCode = 31 + base.hashCode();
    for (int i = 0, offset = 1, segmentCount = this.segmentCount; i < segmentCount; )
    {
      // Get the segment's hash code.
      //
      int segmentHashCode = segmentHashCodes[i];

      // Get the terminating boundary for this segment.
      //
      int end = segmentBoundaries[i++];

      // The number of characters in the segment.
      //
      int count = end - offset;

      // Intern that character range with the known segment hash code.
      //
      segments[i] = intern(characters, offset, count, segmentHashCode);

      // Compose the segment's hash code.
      //
      hashCode = 31 * hashCode + segmentHashCode;

      // Set the offset to be one character after the terminating /.
      offset = end + 1;
    }

    // The number of segments populated and needing to be reset to null.
    //
    usedSegmentCount = segmentCount + 1;

    // Create a hierarchical platform-scheme URI from the interned segments.
    //
    return new Hierarchical(this.hashCode, URI.SCHEME_PLATFORM, null, null, true, internArray(segments, 0, usedSegmentCount, hashCode), null);
  }

  @Override
  public void reset(boolean isExclusive)
  {
    for (int i = 0; i < usedSegmentCount; ++i)
    {
      segments[i] = null;
    }
    segmentCount = 0;
    usedSegmentCount = 0;
    encodedPath = null;
    base = null;
    path = null;

    super.reset(isExclusive);
  }
}