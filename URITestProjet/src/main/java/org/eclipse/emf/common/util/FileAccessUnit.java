package org.eclipse.emf.common.util;

import java.io.File;

/**
 * An Access unit for file URI string-based access.
 */
class FileAccessUnit extends URIPoolAccessUnitBase
{
  /**
   * The base URI for file scheme URIs.
   */
  protected static final String FILE_BASE = "file:/";

  /**
   * The length of the base URI for file scheme URIs.
   */
  protected static final int FILE_BASE_LENGTH = "file:/".length();

  /**
   * The hash code of the base URI for file scheme URIs.
   */
  protected static final int FILE_BASE_HASH_CODE = FILE_BASE.hashCode();

  /**
   * The file path being accessed.
   */
  protected String path;

  /**
   * The buffer for absolute file paths.
   */
  protected char[] absoluteCharacters = new char[100];

  /**
   * The buffer for relative file paths.
   */
  protected char[] relativeCharacters = new char[100];

  /**
   * The segments of the path.
   */
  protected String[] segments = new String[20];

  /**
   * The number of segments in the path.
   */
  protected int segmentCount;

  /**
   * The number of segments populated with strings during intern that need to be nulled during reset.
   */
  protected int usedSegmentCount;

  /**
   * The boundaries of the segments in the path.
   */
  protected int[] segmentBoundaries = new int[100];

  /**
   * The hash codes of the segments in the path.
   */
  protected int[] segmentHashCodes = new int[100];

  /**
   * The final encoded path.
   */
  protected String encodedPath;

  /**
   * Whether the file path represents an absolute file.
   */
  protected boolean isAbsoluteFile;

  /**
   * Whether the path itself is absolute.
   */
  protected boolean isAbsolutePath;

  /**
   * Creates an instance managed by the given queue.
   */
  public FileAccessUnit(Queue queue)
  {
    super(queue);

    // Caches the base absolute file path characters.
    //
    FILE_BASE.getChars(0, FILE_BASE_LENGTH, absoluteCharacters, 0);
  }

  /**
   * Caches the parameter and computes the hash code.
   */
  protected void setValue(String path)
  {
    this.path = path;

    int length = path.length();
    if (length == 0)
    {
      // It's just the empty string with the zero hash code.
      //
      encodedPath = "";
      this.hashCode =  0;
    }
    else
    {
      // Is this path considered an absolute file by the file system implementation?
      //
      isAbsoluteFile = new File(path).isAbsolute();

      // This will use either the absoluteCharacters or the relativeCharacters...
      //
      char[] characters;

      // Check the first character.
      //
      char character = path.charAt(0);

      // We're convert this character to a /.
      //
      char separatorchar = File.separatorChar;

      // Compose the hash code.
      //
      int hashCode;

      // Walk the path segments...
      //
      int i;

      // There can be at most as many boundaries as characters.
      //
      if (segmentBoundaries.length < length)
      {
        segmentBoundaries = new int[length];
        segmentHashCodes = new int[length];
      }

      if (isAbsoluteFile)
      {
        // If it's an absolute file then it must be an absolute path.
        //
        isAbsolutePath = true;

        // There can be at most as many encoded characters as three times the length, plus we need to account for the characters in the base.
        //
        int maxEncodedLength = 3 * length + FILE_BASE_LENGTH;
        if (absoluteCharacters.length <= maxEncodedLength)
        {
          // Allocate one slightly larger and copy in the base path.
          //
          absoluteCharacters = new char[maxEncodedLength + 1];
          FILE_BASE.getChars(0, FILE_BASE_LENGTH, absoluteCharacters, 0);
        }

        // Process the absolute characters.
        //
        characters = absoluteCharacters;

        if (character == URI.SEGMENT_SEPARATOR || character == separatorchar)
        {
          // If the path starts with a separator, copy over the characters after that / to the buffer after the base.
          //
          path.getChars(1, length, characters, FILE_BASE_LENGTH);

          // The length is larger by one less than the base.
          //
          length += FILE_BASE_LENGTH - 1;
        }
        else
        {
          // If the path doesn't start with a /, copy over all the characters into the buffer after the base.
          //
          path.getChars(0, length, characters, FILE_BASE_LENGTH);

          // The length is larger by the base.
          //
          length += FILE_BASE_LENGTH;
        }

        // The first boundary is after the base and that's where we start processing the characters.
        //
        segmentBoundaries[0] = i = FILE_BASE_LENGTH;

        // The hash code so far is the base's hash code.
        //
        hashCode = FILE_BASE_HASH_CODE;
      }
      else
      {
        // There can be at most as many encoded characters as three times the length.
        //
        int maxEncodedLength = 3 * length;
        if (relativeCharacters.length <= maxEncodedLength)
        {
          // Allocate one slightly larger.
          //
          relativeCharacters = new char[maxEncodedLength + 1];
        }

        // Process the relative characters.
        //
        characters = relativeCharacters;

        if (character == URI.SEGMENT_SEPARATOR || character == separatorchar)
        {
          // If the path starts with a separator, then it's an absolute path.
          //
          isAbsolutePath = true;

          // Set the leading / and   copy over the characters after the leading / or \ to the buffer after that.
          //
          characters[0] = URI.SEGMENT_SEPARATOR;
          path.getChars(1, length, characters, 1);

          // The first boundary is after the / and that's where we start processing the characters.
          //
          segmentBoundaries[0] = i = 1;

          // The hash code so far is the /'s hash code.
          //
          hashCode = URI.SEGMENT_SEPARATOR;
        }
        else
        {
          // No leading separator so it's a relative path.
          //
          isAbsolutePath = false;

          //  Copy over all the characters in the bufffer.
          //
          path.getChars(0, length, characters, 0);

          // The first boundary is at the start and that's where we start processing the characters.
          //
          segmentBoundaries[0] = i = 0;

          // The hash code so far is zero.
          //
          hashCode = 0;
        }
      }

      // Compose the segment hash code as we scan the characters.
      //
      int segmentHashCode = 0;
      for (; i < length; ++i)
      {
        // If the current character needs encoding (including the separator characters) or is the device identifier and we're processing the first segment of an absolute path...
        //
        character = characters[i];
        if (character < 160 && (!URI.matches(character, URI.SEGMENT_CHAR_HI, URI.SEGMENT_CHAR_LO) || character == URI.DEVICE_IDENTIFIER && !isAbsolutePath && segmentCount == 0))
        {
          if (character == URI.SEGMENT_SEPARATOR)
          {
            // If it's a /, cache the segment hash code and the segment boundary, reset the segment hash code, and compose the segments hash code.
            //
            segmentHashCodes[segmentCount] = segmentHashCode;
            segmentBoundaries[++segmentCount] = i;
            segmentHashCode = 0;
            hashCode = 31 * hashCode + URI.SEGMENT_SEPARATOR;
          }
          else if (character == separatorchar)
          {
            // If it's a \, change it to a /, cache the segment hash code and the segment boundary, reset the segment hash code, and compose the segments hash code.
            //
            characters[i] = URI.SEGMENT_SEPARATOR;
            segmentHashCodes[segmentCount] = segmentHashCode;
            segmentBoundaries[++segmentCount] = i;
            segmentHashCode = 0;
            hashCode = 31 * hashCode + URI.SEGMENT_SEPARATOR;
          }
          else
          {
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
      segmentBoundaries[++segmentCount] = length;

      // Compose the overall hash code to include the base's hash code.
      //
      this.hashCode = hashCode;

      // Cache the encoded path.
      //
      encodedPath = intern(characters, 0, length, hashCode);
    }
  }

  @Override
  protected boolean matches(URI value)
  {
    return value.matches(encodedPath);
  }

  @Override
  public URI getInternalizedValue()
  {
    // Ensure that we have enough room for all the segments.
    //
    int segmentCount = this.segmentCount;
    if (segments.length <= segmentCount)
    {
      segments = new String[segmentCount + 1];
    }

    // Process the appropriate characters.
    //
    char[] characters = isAbsoluteFile ? absoluteCharacters : relativeCharacters;

    // Parse out the device and the authority...
    //
    String device = null;
    String authority = null;

    // The initial hash code for the over all final segments.
    //
    int segmentsHashCode = 1;

    // Where we expect the special device segment to appear.
    //
    int deviceIndex = 0;

    // An empty segment at this index will be igored.
    //
    int ignoredEmptySegmentIndex = -1;

    // Whether we ignored an empty segment.
    //
    boolean ignoredEmptySegment = false;

    // Process all the segments...
    //
    for (int i = 0, segmentIndex = 0, offset = segmentBoundaries[0]; segmentIndex < segmentCount; ++i)
    {
      // The end of the current segment's boundary.
      //
      int end = segmentBoundaries[i + 1];

      // The number of characters of the current segment.
      //
      int count = end - offset;

      // If this is an empty segment we wish to ignore...
      //
      if (i == ignoredEmptySegmentIndex && count == 0)
      {
        // Ignore it and indicate that we ignored a leading empty segment.
        //
        --segmentCount;
        ignoredEmptySegment = true;
      }
      else
      {
        // Retrieve the segment's hash code.
        //
        int segmentHashCode = segmentHashCodes[i];

        // Intern the segment characters...
        //
        String segment = intern(characters, offset, count, segmentHashCode);

        // If we're at a device index while processing an absolute file, and we have an empty segment that's not the only segment or the last character of the segment is the device identifier...
        //
        if (i == deviceIndex && isAbsoluteFile && (count == 0 && segmentCount > 1 || characters[end - 1] == URI.DEVICE_IDENTIFIER))
        {
          // If the segment has zero length...
          //
          if (count == 0)
          {
            // Proceed to the next segment; there must be one because of the guard...
            //
            offset = end + 1;
            segmentHashCode = segmentHashCodes[++i];
            end = segmentBoundaries[i + 1];
            count = end - offset;

            // This segment is really the authority...
            //
            authority = intern(characters, offset, count, segmentHashCode);

            // There are now two fewer segments.
            //
            segmentCount -= 2;

            // We should still check for a device at index 2.
            //
            deviceIndex = 2;

            // We should still consider ignoring an empty segment if it's at index 2.
            //
            ignoredEmptySegmentIndex = 2;
          }
          else
          {
            // Otherwise the segment must end with a :, so it must be the device.
            //
            device = segment;

            // There's one fewer real segment.
            //
            --segmentCount;

            // We should ignore an empty segment if it comes next.
            //
            ignoredEmptySegmentIndex = deviceIndex + 1;
          }
        }
        else
        {
          // It's a normal segment so include it and compose the overall segments hash code.
          //
          segments[segmentIndex++] = segment;
          segmentsHashCode = 31 * segmentsHashCode + segmentHashCode;
        }
      }

      // Continue with the characters after the current segment's closing boundary.
      //
      offset = end + 1;
    }

    // Remember which segments need to be cleared in reset.
    //
    usedSegmentCount = segmentCount;

    // Intern the segments array itself.
    //
    String[] internedSegments = internArray(segments, 0, segmentCount, segmentsHashCode);
    if (isAbsoluteFile)
    {
      // If it's absolute, we include the file scheme, and it has an absolute path, if there is one or more segments, or if we ignored an empty segment.
      //
      return new Hierarchical(this.hashCode, URI.SCHEME_FILE, authority, device, segmentCount > 0 || ignoredEmptySegment, internedSegments, null);
    }
    else
    {
      // It's a relative URI...
      //
      return new Hierarchical(this.hashCode, null, null, null, isAbsolutePath, internedSegments, null);
    }
  }

  @Override
  public void reset(boolean isExclusive)
  {
    for (int i = 0; i < usedSegmentCount; ++i)
    {
      segments[i] = null;
    }
    usedSegmentCount = 0;
    segmentCount = 0;
    encodedPath = null;
    path = null;

    super.reset(isExclusive);
  }
}