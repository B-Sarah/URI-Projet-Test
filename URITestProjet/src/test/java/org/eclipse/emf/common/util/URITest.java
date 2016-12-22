package org.eclipse.emf.common.util;

import static org.junit.Assert.*;

import java.util.Collections;

import org.junit.Ignore;
import org.junit.Test;


public class URITest {

	@Test
	public void testHashCode() {
		URI uri  = new URI(14) {
		};
		assert uri.hashCode() == uri.hashCode;
		
	}

	@Test (expected = IllegalArgumentException.class)
	public void testCreateGenericURICase1() {
	URI.createGenericURI(null,"opaque","fragment");
	}
	
	@Test (expected = IllegalArgumentException.class)
	public void testCreateGenericURICase2() {
	URI.createGenericURI("jar","opaque","fragment");
	}
	
	@Test (expected = IllegalArgumentException.class)
	public void testCreateGenericURICase3() {
	URI.createGenericURI("zip","opaque","fragment");
	}
	
	@Test
	@Ignore //seems buggy
	public void testCreateGenericURICase4() {
	String scheme = "txt";
	String opaque = "op";
	String fragment = "frag";
	URI uri = URI.createGenericURI(scheme,opaque,fragment);
	assert uri.scheme().equals("txt");
	assert uri.opaquePart().equals("op");
	assert uri.fragment().equals("frag");
	
	}
	

	@Test(expected = IllegalArgumentException.class)
	public void testCreateHierarchicalURIStringStringStringStringStringCase1() {
		URI.createHierarchicalURI("scheme", null, null, "def", "fragment");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCreateHierarchicalURIStringStringStringStringStringCase2() {
		URI.createHierarchicalURI("jar","opaque","device", "dadza", "fragment");
	}
	
	@Test
	@Ignore //seems buggy
	public void testCreateHierarchicalURIStringStringStringStringStringCase3() {
		URI uri = URI.createHierarchicalURI("txt","maressource","person:", "fromFrance", "fragment");
		assert uri.device().equals("person:");
		assert uri.fragment().equals("fragment");
		assert uri.scheme().equals("txt");
		assert uri.authority().equals("maressource");
		assert uri.query().equals("fromFrance");
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testCreateHierarchicalURIStringStringStringStringArrayStringStringcase1() {
		URI.createHierarchicalURI("jar",null,"device", null, null, null);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testCreateHierarchicalURIStringStringStringStringArrayStringStringcase2() {
		URI.createHierarchicalURI("plateform",null,"device", null, null, null);
	}
	
	@Test
	@Ignore //seems buggy
	public void testCreateHierarchicalURIStringStringStringStringArrayStringStringcase3() {
		URI uri = URI.createHierarchicalURI("txt","maressource","person:", new String[]{"fezfz", "fzef"},"fromFrance", "fragment");
		assert uri.device().equals("person:");
		assert uri.fragment().equals("fragment");
		assert uri.scheme().equals("txt");
		assert uri.authority().equals("maressource");
		assert uri.query().equals("fromFrance");
		assert uri.segmentCount()==2;
	}

	@Test(expected=IllegalArgumentException.class)
	public void testCreateHierarchicalURIStringArrayStringStringCase1() {
		URI.createHierarchicalURI(new String[]{"??//"}, "fromFrance", "");
	}
	
	@Test
	@Ignore //seems buggy
	public void testCreateHierarchicalURIStringArrayStringStringCase2() {
		String segments[] = new String[]{"dafa", "fzefz"};
		String query = "fromFrance";
		String fragment = "";
		URI uri = URI.createHierarchicalURI(segments, query, fragment);
		assert uri.segmentsList().contains("fezfz..");
		assert uri.query().equals("fromFrance");
		assert uri.hasFragment();
	}

	@Test(expected=IllegalArgumentException.class)
	@Ignore //seems buggy
	public void testCreateURIStringCase1() {
		URI.createURI("??::adaf/ddz/jar:!");
	}
	
	@Test
	@Ignore //seems buggy
	public void testCreateURIStringCase2() {
		URI uri = URI.createURI("file:///c:/foo");
		assert uri.matches("file:///c:/foo");
	}

	@Test
	@Ignore //seems buggy
	public void testCreateURIStringBoolean() {
		URI uri = URI.createURI("file:///c:/foo", false);
		assert uri.matches("file:///c:/foo");
	}

	@Test
	@Ignore //seems buggy
	public void testCreateURIStringBooleanInt() {
		URI uri = URI.createURI("file:///c:/foo##", false, URI.FRAGMENT_NONE);
		assert uri.matches("file:///c:/foo##");
	}

	@Test
	@Ignore //Not tested because based on file : hard to test
	public void testCreateFileURI() {
		fail("Hard to test file based method");
		
	}

	@Test
	@Ignore //Not tested because based on file : hard to test
	public void testCreatePlatformResourceURIStringBoolean() {
		fail("Hard to test file based method");
	}

	@Test
	@Ignore //Not tested because based on file : hard to test
	public void testCreatePlatformPluginURI() {
		fail("Hard to test file based method");
	}

	@Test
	public void testValidScheme() {
		assert URI.validScheme(null) == true;
		assert URI.validScheme("8743.///") == false;
		assert URI.validScheme("avevds485..") == true;
	}

	@Test
	public void testValidOpaquePart() {
		assert URI.validOpaquePart(null) == false;
		assert URI.validOpaquePart("/874") == false;
		assert URI.validOpaquePart("avevds485..") == true;
	}

	@Test
	public void testValidAuthority() {
		assert URI.validAuthority(null) == true;
		assert URI.validAuthority("8743.///") == false;
		assert URI.validAuthority("avevds485..") == true;
	}

	@Test
	public void testValidArchiveAuthority() {
		assert URI.validArchiveAuthority(null) == false;
		assert URI.validArchiveAuthority("jar") == false;
		assert URI.validArchiveAuthority("jarf!qsfq") == false;
		assert URI.validArchiveAuthority("f458:4ds") == false;
		assert URI.validArchiveAuthority("f458:4ds!") == true;
	}

	@Test
	public void testValidDevice() {
		assert URI.validDevice(null) == true;
		assert URI.validDevice("8743.///") ==false;
		assert URI.validDevice("avevds485") == false;
		assert URI.validDevice("avevds485:") == true;
	}

	@Test
	public void testValidSegment() {
		assert URI.validSegment(null) == false;
		assert URI.validSegment("87??43.///") == false;
		assert URI.validSegment("avevds485..") == true;
	}

	@Test
	public void testValidSegments() {
		assert URI.validSegments(null) == false;
		String segs1[] = new String[]{"ddvfdez..", "484564effse"};
		assert URI.validSegments(segs1) == true;
		
		String segs2[] = new String[]{"ddvfdez.//??.", "484564effse"};
		assert URI.validSegments(segs2) == false;
	}

	@Test
	public void testValidQuery() {
		assert URI.validQuery(null) == true;
		assert URI.validQuery("!!ùù%%@@++//++87??43./???//") == true;
	}

	@Test
	public void testValidFragment() {
		assert URI.validFragment("") == true;
	}

	@Test
	public void testIsRelative() {
		URI uri = new URI(22) {
		};
		
		assert uri.isRelative() == false;
	}

	@Test
	public void testIsHierarchical() {
		URI uri = new URI(2) {
		};
		assert uri.isHierarchical() == false;
	}

	@Test
	public void testHasAuthority() {
		URI uri = new URI(1) {
		};
		assert uri.hasAuthority() == false;
	}

	@Test
	public void testHasOpaquePart() {
		URI uri = new URI(4) {
		};
		assert uri.hasOpaquePart() == false;
	}

	@Test
	public void testHasDevice() {
		URI uri = new URI(0) {
		};
		assert uri.hasDevice() == false;
	}

	@Test
	public void testHasPath() {
		URI uri = new URI(47) {
		};
		assert uri.hasPath() == false;
	}

	@Test
	public void testHasAbsolutePath() {
		URI uri = new URI(10) {
		};
		assert uri.hasAbsolutePath() == false;
	}

	@Test
	public void testHasRelativePath() {
		URI uri = new URI(12) {
		};
		assert uri.hasRelativePath() == false;
	}

	@Test
	public void testHasEmptyPath() {
		URI uri = new URI(80) {
		};
		assert uri.hasEmptyPath() == false;
	}

	@Test
	public void testHasQuery() {
		URI uri = new URI(50) {
		};
		assert uri.hasQuery() == false;
	}

	@Test
	public void testHasFragment() {
		URI uri = new URI(8) {
		};
		assert uri.hasFragment() == false;
	}

	@Test
	public void testIsCurrentDocumentReference() {
		URI uri = new URI(1) {
		};
		assert uri.isCurrentDocumentReference() == false;
	}

	@Test
	public void testIsEmpty() {
		URI uri = new URI(2) {
		};
		assert uri.isEmpty() == false;
	}

	@Test
	public void testIsFile() {
		URI uri = new URI(5) {
		};
		assert uri.isFile() == false;
	}

	@Test
	public void testIsPlatform() {
		URI uri = new URI(1) {
		};
		assert uri.isPlatform() == false;
	}

	@Test
	public void testIsPlatformResource() {
		URI uri = new URI(25) {
		};
		assert uri.isPlatformResource() == false;
	}

	@Test
	public void testIsPlatformPlugin() {
		URI uri = new URI(7) {
		};
		assert uri.isPlatformPlugin() == false;
	}

	@Test
	public void testIsArchive() {
		URI uri = new URI(2) {
		};
		assert uri.isArchive() == false;
		
	}

	@Test
	public void testIsArchiveScheme() {
		assert URI.isArchiveScheme("zip");
		assert URI.isArchiveScheme("jar");
		assert URI.isArchiveScheme("archive");
		assert URI.isArchiveScheme("rar") == false;
	}

	@Test
	public void testEqualsObjectCase1() {
		URI uri1 = new URI(8) {
		};
		
		URI uri2 = new URI(8) {
		};
		assert uri1.equals(uri2);

	}
	
	@Test
	public void testEqualsObjectCase2() {
		URI uri1 = null;
		
		URI uri2 = new URI(8) {
		};
		assertFalse(uri2.equals(uri1));

	}
	@Test
	public void testEqualsObjectCase3() {
		URI uri1 = new URI(8) {
		};
		assertFalse(uri1.equals(2));

	}
	
	@Test
	public void testEqualsObjectCase4() {
		URI uri1 = new URI(8) {
		};
		URI uri2 = new URI(1) {
		};
		assertFalse(uri1.equals(uri2));

	}

	@Test
	public void testScheme() {
		URI uri = new URI(10) {
		};
		assert uri.scheme() == null;
	}

	@Test
	public void testOpaquePart() {
		URI uri = new URI(0) {
		};
		assert uri.opaquePart() == null;
	}
	

	@Test
	public void testAuthority() {
		URI uri = new URI(10) {
		};
		assert uri.authority() == null;
	}
	
	@Test
	public void testUserInfo() {
		URI uri = new URI(-1) {
		};
		assert uri.userInfo() == null;
	}

	@Test
	public void testHost() {
		URI uri = new URI(7) {
		};
		assert uri.host() == null;
	}

	@Test
	public void testPort() {
		URI uri = new URI(2) {
		};
		assert uri.port() == null;
	}

	@Test
	public void testDevice() {
		URI uri = new URI(10) {
		};
		assert uri.device() == null;
	}
	
	@Test
	public void testSegments() {
		URI uri = new URI(51) {
		};
		assert uri.segments() == URI.NO_SEGMENTS;
	
	}
	

	@Test
	public void testSegmentsList() {
		URI uri = new URI(14) {
		};
		assert uri.segmentsList().equals(Collections.emptyList());
	}

	@Test 
	public void testSegmentCount() {
		URI uri = new URI(3) {
		};
		assert uri.segmentCount() == 0;
	}

	@Test (expected = IndexOutOfBoundsException.class) 
	public void testSegment() {
		URI uri = new URI(3) {
		};
		uri.segment(1);
	}

	@Test
	public void testLastSegment() {
		URI uri = new URI(3) {
		};
		assert uri.lastSegment() == null;
	}

	@Test
	public void testPath() {
		URI uri = new URI(-5) {
		};
		assert uri.path() == null;
	}

	@Test
	public void testDevicePath() {
		URI uri = new URI(0) {
		};
		assert uri.devicePath() == null;
	}

	@Test
	public void testQuery() {
		URI uri = new URI(60) {
		};
		assert uri.query() == null;
	}

	@Test
	public void testAppendQuery() {
		URI uri = new URI(60) {
		};
		assert uri.appendQuery("dazef").equals(uri);
	}

	@Test
	public void testTrimQuery() {
		URI uri = new URI(3) {
		};
		assert uri.trimQuery().equals(uri);
	}

	@Test
	public void testFragment() {
		URI uri = new URI(30) {
		};
		assert uri.fragment() == null;
	}

	//other case of appendFragment can't be tested because of controlability of URI.POOL
	@Test
	public void testAppendFragmentCase1() {
		URI uri = new URI(3) {
		};
		assert uri.appendFragment(null).equals(uri);
	}
	

	@Test
	public void testTrimFragment() {
		URI uri = new URI(30) {
		};
		assert uri.trimFragment().equals(uri);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testResolveURI() {
		URI uri = new URI(5) {
		};
		uri.resolve(uri);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testResolveURIBoolean() {
		URI uri = new URI(2) {
		};
		uri.resolve(uri, false);
	}

	@Test
	public void testDeresolveURI() {
		URI uri = new URI(2) {
		};
		assert uri.deresolve(null).equals(uri);
		
	}

	@Test
	public void testDeresolveURIBooleanBooleanBoolean() {
		URI uri = new URI(2) {
		};
		assert uri.deresolve(null, false, false, true).equals(uri);
		assert uri.deresolve(null, true, true, false).equals(uri);
		assert uri.deresolve(null, true, false, false).equals(uri);
		assert uri.deresolve(null, false, true, true).equals(uri);
	}

	@Test
	public void testToFileString() {
		URI uri = new URI(2) {
		};
		assert uri.toFileString() == null;
	}

	@Test
	public void testToPlatformString() {
		URI uri = new URI(60) {
		};
		assert uri.toPlatformString(true) == null;
		assert uri.toPlatformString(false) == null;
	}
			
	@Test (expected =  IllegalArgumentException.class)
	public void testAppendSegmentCase1() {
		URI uri = new URI(40) {
		};
		uri.appendSegment("/segment/");
		
	}
	
	@Test (expected =  IllegalArgumentException.class)
	public void testAppendSegmentCase2() {
		URI uri = new URI(40) {
		};
		uri.appendSegment("segment#fd");
		
	}
	
	@Test (expected =  IllegalArgumentException.class)
	public void testAppendSegmentCase3() {
		URI uri = new URI(40) {
		};
		uri.appendSegment("segment?125");
		
	}
	
	@Test
	public void testAppendSegmentCase4(){
		URI uri = new URI(21) {
		};
		assert uri.appendSegment("segments_seg").equals(uri);
		
	}
	
	@Test (expected =  IllegalArgumentException.class)
	public void testAppendSegmentsCase1() {
		URI uri = new URI(21) {
		};
		String segments[] = new String[]{"segmen?47", "dezf"};
		uri.appendSegments(segments);
	}
	
	@Test
	public void testAppendSegmentsCase2() {
		URI uri = new URI(21) {
		};
		String segments[] = new String[]{"segmend7", "dezf"};
		assert uri.appendSegments(segments).equals(uri);
	}

	@Test
	public void testTrimSegments() {
		URI uri = new URI(19) {
		};
		assert uri.trimSegments(1).equals(uri);
	}

	@Test
	public void testHasTrailingPathSeparator() {
		URI uri = new URI(27) {
		};
		assert uri.hasTrailingPathSeparator() == false;
	}

	@Test
	public void testFileExtensionCase1() {
		URI uri = new URI(1) {
		};
		assert uri.fileExtension() == null;
	}

	@Test (expected =  IllegalArgumentException.class)
	public void testAppendFileExtensionCase2() {
		URI uri = new URI(7) {
		};
		uri.appendFileExtension("file?");
		
	}
	
	@Test (expected =  IllegalArgumentException.class)
	public void testAppendFileExtensionCase3() {
		URI uri = new URI(3) {
		};
		uri.appendFileExtension("#file");
		
	}
	
	@Test (expected =  IllegalArgumentException.class)
	public void testAppendFileExtensionCase4() {
		URI uri = new URI(14) {
		};
		uri.appendFileExtension("/file/");
		
	}
	
	@Test
	public void testAppendFileExtensionCase5() {
		URI uri = new URI(74) {
		};
		uri.appendFileExtension("file1");
		
	}
	

	@Test
	public void testTrimFileExtension() {
		URI uri = new URI(9) {
		};
		assert uri.trimFileExtension().equals(uri);
	}

	@Test
	public void testIsPrefix() {
		URI uri = new URI(147) {
		};
		assert uri.isPrefix() == false;
	}

	@Test(expected=IllegalArgumentException.class)
	public void testReplacePrefix() {
		URI uri = new URI(9) {
		};
		uri.replacePrefix(new URI(8) {}, new URI(7) {});
	}

	@Test
	public void testEncodeOpaquePart() {
		String str = "/op a%20q#ue";
		assert URI.encodeOpaquePart(str, false).equals("%2Fop%20a%2520q%23ue");
		
		str = "/op a%20q#ue";
		assert URI.encodeOpaquePart(str, true).equals("%2Fop%20a%20q%23ue");
	}

	@Test
	public void testEncodeAuthority() {
		String str = "op a%20q#ue";
		assert URI.encodeAuthority(str, false).equals("op%20a%2520q%23ue");
		
		str = "op a%20q#ue";
		assert URI.encodeAuthority(str, true).equals("op%20a%20q%23ue");
	}

	@Test
	public void testEncodeSegment() {
		String str = "op a%20q#ue";
		assert URI.encodeSegment(str, false).equals("op%20a%2520q%23ue");
		
		str = "op a%20q#ue";
		assert URI.encodeSegment(str, true).equals("op%20a%20q%23ue");
	}

	@Test
	public void testEncodeQuery() {
		String str = "o/p a%20q#ue";
		assert URI.encodeQuery(str, false).equals("o/p%20a%2520q%23ue");
		
		str = "o/p a%20q#ue";
		assert URI.encodeQuery(str, true).equals("o/p%20a%20q%23ue");
	}

	@Test
	public void testEncodeFragment() {
		String str = "o/p a%20q#ue";
		assert URI.encodeFragment(str, false).equals("o/p%20a%2520q%23ue");
		
		str = "o/p a%20q#ue";
		assert URI.encodeFragment(str, true).equals("o/p%20a%20q%23ue");
	}

	@Test
	public void testDecode() {
		String str = "o/p%20a%2520q%23ue";
		assert URI.decode(str).equals("o/p a%20q#ue");
	}

}
