package uk.gov.digital.ho.hocs.document;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static uk.gov.digital.ho.hocs.testutil.TestFileReader.getResourceFileAsString;

public class JSONToSimpleTextConverterTest {

    @Test
    public void shouldBuildFormattedDocument() throws IOException {
        String json = getResourceFileAsString("staffBehaviour.json");
        String expectedText = getResourceFileAsString("staffBehaviour.txt");
        JSONToSimpleTextConverter complaintData = new JSONToSimpleTextConverter(json);
        assertEquals(expectedText, complaintData.getConvertedOutput());
    }
}