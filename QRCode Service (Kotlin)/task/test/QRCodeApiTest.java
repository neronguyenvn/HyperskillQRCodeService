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
    private static final String BAD_CORRECTION_MSG = "Permitted error correction levels are L, M, Q, H";
    private static final String BAD_CONTENTS_MSG = "Contents cannot be null or blank";

    CheckResult testGetHealth() {
        var url = "/api/health";
        HttpResponse response = get(url).send();

        checkStatusCode(response, 200);

        return CheckResult.correct();
    }
    @DynamicTest
    DynamicTesting[] tests = {
            this::testGetHealth,

            () -> testGetQrCode(contents[1], null, null, null, "f4d19902b0ae101de9b03b8aea5556dc"),
            () -> testGetQrCode(contents[1], 200, null, null, "357759acd42e878ce86bf7f00071df7d"),
            () -> testGetQrCode(contents[1], null, "H", null, "21d1f792360f6946a7583d79e8ae18ef"),
            () -> testGetQrCode(contents[2], null, null, "gif", "af3f3319944ad1271a3d2e3e5de12a30"),
            () -> testGetQrCode(contents[3], 200, "Q", null, "a524b79ddeff57aa74357f9b608b6dff"),
            () -> testGetQrCode(contents[4], 200, null, "jpeg", "2a700a58e2b593a998e428fae8f9f4e7"),
            () -> testGetQrCode(contents[5], null, "Q", "gif", "5208d69a5c3541c16e61fb846cd82f37"),
            () -> testGetQrCode(contents[6], 200, "H", "jpeg", "69879de9db73966792bacbbe69f06146"),

            () -> testGetQrCodeInvalidParams(contents[0], 99, "L", "gif", BAD_SIZE_MSG),
            () -> testGetQrCodeInvalidParams(contents[0], 351, "L", "png", BAD_SIZE_MSG),
            () -> testGetQrCodeInvalidParams(contents[0], 451, "L", "webp", BAD_SIZE_MSG),
            () -> testGetQrCodeInvalidParams(contents[0], 200, "L", "webp", BAD_TYPE_MSG),
            () -> testGetQrCodeInvalidParams("", 200, "L", "webp", BAD_CONTENTS_MSG),
            () -> testGetQrCodeInvalidParams("   ", 500, "S", "webp", BAD_CONTENTS_MSG),
            () -> testGetQrCodeInvalidParams(contents[0], 500, "S", "webp", BAD_SIZE_MSG),
            () -> testGetQrCodeInvalidParams(contents[0], 200, "S", "webp", BAD_CORRECTION_MSG)
    };

    CheckResult testGetQrCode(String contents,
                              Integer size,
                              String correction,
                              String imgType,
                              String expectedHash) {

        var url = "/api/qrcode?contents=%s".formatted(encodeUrl(contents));
        if (size != null) {
            url += "&size=%d".formatted(size);
        }
        if (correction != null) {
            url += "&correction=%s".formatted(correction);
        }
        if (imgType != null) {
            url += "&type=%s".formatted(imgType);
        }
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

    CheckResult testGetQrCodeInvalidParams(String contents,
                                           int size,
                                           String correction,
                                           String imgType,
                                           String message) {
        var url = "/api/qrcode?contents=%s&size=%d&correction=%s&type=%s"
                .formatted(encodeUrl(contents), size, correction, imgType);

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
        var expected = "image/" + (imgType == null ? "png" : imgType);
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
