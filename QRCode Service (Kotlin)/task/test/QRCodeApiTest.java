import org.hyperskill.hstest.dynamic.DynamicTest;
import org.hyperskill.hstest.dynamic.input.DynamicTesting;
import org.hyperskill.hstest.exception.outcomes.WrongAnswer;
import org.hyperskill.hstest.mocks.web.response.HttpResponse;
import org.hyperskill.hstest.stage.SpringTest;
import org.hyperskill.hstest.testcase.CheckResult;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

import static org.hyperskill.hstest.testing.expect.Expectation.expect;
import static org.hyperskill.hstest.testing.expect.json.JsonChecker.isObject;
import static org.hyperskill.hstest.testing.expect.json.JsonChecker.isString;

public class QRCodeApiTest extends SpringTest {
    private static final String BAD_SIZE_MSG = "Image size must be between 150 and 350 pixels";
    private static final String BAD_TYPE_MSG = "Only png, jpeg and gif image types are supported";
    private static final String BAD_CONTENTS_MSG = "Contents cannot be null or blank";

    CheckResult testGetHealth() {
        var url = "/api/health";
        HttpResponse response = get(url).send();

        checkStatusCode(response, 200);

        return CheckResult.correct();
    }
    String[] contents = {
            "text content",
            "mailto:name@company.com",
            "geo:-27.07,109.21",
            "tel:1234567890",
            "smsto:1234567890:texting!",
            "Here is some text",
            "https://hyperskill.org",
            """
            BEGIN:VCARD
            VERSION:3.0
            N:John Doe
            ORG:FAANG
            TITLE:CEO
            TEL:1234567890
            EMAIL:business@example.com
            END:VCARD"""
    };
    @DynamicTest
    DynamicTesting[] tests = {
            this::testGetHealth,

            () -> testGetQrCode(contents[1], 200, "jpeg", "a9e1e394f5766304127ba88bd9f0bd5a"),
            () -> testGetQrCode(contents[2], 200, "gif", "3d6cc8d84284c0d10af3370c1fa883a8"),
            () -> testGetQrCode(contents[3], 300, "png", "e2e18076d34f09a01eb283c7b140b268"),
            () -> testGetQrCode(contents[4], 300, "jpeg", "3f00dbd2593bdf4b229d6addf09464a4"),
            () -> testGetQrCode(contents[5], 200, "gif", "db6ef9d4a2d81285c9f5ed85f870d092"),
            () -> testGetQrCode(contents[6], 200, "jpeg", "401a4a780f22cd752b8162512d1eb3f8"),
            () -> testGetQrCode(contents[7], 300, "gif", "d167d42b222297df6c754aea3273681f"),

            () -> testGetQrCodeInvalidParams(contents[0], 99, "gif", BAD_SIZE_MSG),
            () -> testGetQrCodeInvalidParams(contents[0], 351, "png", BAD_SIZE_MSG),
            () -> testGetQrCodeInvalidParams(contents[0], 451, "webp", BAD_SIZE_MSG),
            () -> testGetQrCodeInvalidParams(contents[0], 200, "webp", BAD_TYPE_MSG),
            () -> testGetQrCodeInvalidParams("", 200, "webp", BAD_CONTENTS_MSG),
            () -> testGetQrCodeInvalidParams("   ", 500, "webp", BAD_CONTENTS_MSG)
    };

    CheckResult testGetQrCode(String contents, int size, String imgType, String expectedHash) {
        var url = "/api/qrcode?contents=%s&size=%d&type=%s"
                .formatted(encodeUrl(contents), size, imgType);
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

    CheckResult testGetQrCodeInvalidParams(String contents, int size, String imgType, String message) {
        var url = "/api/qrcode?contents=%s&size=%d&type=%s"
                .formatted(encodeUrl(contents), size, imgType);

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

    private String encodeUrl(String param) {
        return URLEncoder.encode(param, StandardCharsets.UTF_8);
    }
}
