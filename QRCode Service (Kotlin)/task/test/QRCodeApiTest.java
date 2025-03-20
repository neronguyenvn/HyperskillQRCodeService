import org.hyperskill.hstest.dynamic.DynamicTest;
import org.hyperskill.hstest.dynamic.input.DynamicTesting;
import org.hyperskill.hstest.exception.outcomes.WrongAnswer;
import org.hyperskill.hstest.mocks.web.response.HttpResponse;
import org.hyperskill.hstest.stage.SpringTest;
import org.hyperskill.hstest.testcase.CheckResult;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

public class QRCodeApiTest extends SpringTest {

    @DynamicTest
    DynamicTesting[] tests = {
            this::testGetHealth,
            this::testGetQrCode
    };

    CheckResult testGetHealth() {
        var url = "/api/health";
        HttpResponse response = get(url).send();

        checkStatusCode(response, 200);

        return CheckResult.correct();
    }

    CheckResult testGetQrCode() {
        var url = "/api/qrcode";
        HttpResponse response = get(url).send();

        checkStatusCode(response, 200);
        checkContentType(response, "png");

        var expectedHash = "a370a8d3e1ee0f0184132a3c3b5d2952";
        var contentHash = getMD5Hash(response.getRawContent());
        if (!contentHash.equals(expectedHash)) {
            return CheckResult.wrong("""
                    Response: GET %s
                    
                    Response body does not contain a correct image:
                    Expected image hash %s, but was %s
                    Make sure the size, the contents and the format of the image are correct.
                    
                    """.formatted(url, expectedHash, contentHash)
            );
        }

        return CheckResult.correct();
    }

    private void checkStatusCode(HttpResponse response, int expected) {
        var endpoint = response.getRequest().getEndpoint();
        var actual = response.getStatusCode();
        if (actual != expected) {
            throw new WrongAnswer("""
                    Request: GET %s
                    
                    Response has incorrect status code:
                    Expected %d, but responded with %d
                    
                    """.formatted(endpoint, expected, actual)
            );
        }
    }

    private void checkContentType(HttpResponse response, String imgType) {
        var endpoint = response.getRequest().getEndpoint();
        var expected = "image/" + imgType;
        var actual = response.getHeaders().get("Content-Type");
        if (!Objects.equals(expected, actual)) {
            throw new WrongAnswer("""
                    Request: GET %s
                    
                    Response has incorrect 'Content-Type' header:
                    Expected "%s" but responded with "%s"
                    
                    """.formatted(endpoint, expected, actual)
            );
        }
    }

    private String getMD5Hash(byte[] rawContent) {
        try {
            var md = MessageDigest.getInstance("MD5");
            var hash = md.digest(rawContent);
            var hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append("%02x".formatted(b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
