import org.hyperskill.hstest.dynamic.DynamicTest;
import org.hyperskill.hstest.dynamic.input.DynamicTesting;
import org.hyperskill.hstest.exception.outcomes.WrongAnswer;
import org.hyperskill.hstest.mocks.web.response.HttpResponse;
import org.hyperskill.hstest.stage.SpringTest;
import org.hyperskill.hstest.testcase.CheckResult;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

import static org.hyperskill.hstest.testing.expect.Expectation.expect;
import static org.hyperskill.hstest.testing.expect.json.JsonChecker.isObject;
import static org.hyperskill.hstest.testing.expect.json.JsonChecker.isString;

public class QRCodeApiTest extends SpringTest {
    private static final String BAD_SIZE_MSG = "Image size must be between 150 and 350 pixels";
    private static final String BAD_TYPE_MSG = "Only png, jpeg and gif image types are supported";

    CheckResult testGetHealth() {
        var url = "/api/health";
        HttpResponse response = get(url).send();

        checkStatusCode(response, 200);

        return CheckResult.correct();
    }
    @DynamicTest
    DynamicTesting[] tests = {
            this::testGetHealth,

            () -> testGetQrCode(150, "png", "b67a6f17fe353b997585e65e2903ab7b"),
            () -> testGetQrCode(350, "jpeg", "f614890233a60b13e8e40c7ff554a92c"),
            () -> testGetQrCode(250, "gif", "cc9d9b226e2fab856cb5d008c94c5475"),

            () -> testGetQrCodeInvalidParams(99, "gif", BAD_SIZE_MSG),
            () -> testGetQrCodeInvalidParams(351, "png", BAD_SIZE_MSG),
            () -> testGetQrCodeInvalidParams(451, "webp", BAD_SIZE_MSG),
            () -> testGetQrCodeInvalidParams(200, "tiff", BAD_TYPE_MSG)
    };

    CheckResult testGetQrCode(int size, String imgType, String expectedHash) {
        var url = "/api/qrcode?size=%d&type=%s".formatted(size, imgType);
        HttpResponse response = get(url).send();

        checkStatusCode(response, 200);
        checkContentType(response, imgType);

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

    CheckResult testGetQrCodeInvalidParams(int size, String imgType, String message) {
        var url = "/api/qrcode?size=%d&type=%s".formatted(size, imgType);
        HttpResponse response = get(url).send();

        checkStatusCode(response, 400);
        checkErrorMessage(response, message);

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

    private void checkErrorMessage(HttpResponse response, String message) {
        var endpoint = response.getRequest().getEndpoint();
        if (!response.getJson().isJsonObject()) {
            throw new WrongAnswer("""
                    Request: GET %s
                    
                    Response contains a wrong object:
                    Expected JSON but responded with %s
                    
                    """.formatted(endpoint, response.getContent().getClass())
            );
        }

        expect(response.getContent()).asJson().check(
                isObject().value("error", isString(message))
        );
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
