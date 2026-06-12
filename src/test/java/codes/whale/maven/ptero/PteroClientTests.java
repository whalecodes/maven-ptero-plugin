package codes.whale.maven.ptero;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PteroClientTests {

    @Test
    public void normalizeDirectoryHandlesCommonShapes() {
        assertEquals("/plugins", PteroClient.normalizeDirectory("/home/container/plugins"));
        assertEquals("/plugins", PteroClient.normalizeDirectory("plugins"));
        assertEquals("/a/b", PteroClient.normalizeDirectory("\\a\\\\b"));
        assertEquals("/", PteroClient.normalizeDirectory("  "));
        assertEquals("/", PteroClient.normalizeDirectory("/home/container/"));
    }

    @Test
    public void encodePathEncodesSpacesButKeepsSlashes() {
        assertEquals("/my%20folder/sub", PteroClient.encodePath("/my folder/sub"));
        assertEquals("/a%26b", PteroClient.encodePath("/a&b"));
    }

    @Test
    public void parseUploadUrlExtractsAndUnescapesUrl() throws Exception {
        String body = "{\"object\":\"signed_url\",\"attributes\":{\"url\":\"https:\\/\\/node.example.com\\/upload?token=abc\"}}";
        assertEquals("https://node.example.com/upload?token=abc", PteroClient.parseUploadUrl(body));
    }

}
