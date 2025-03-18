import org.hyperskill.hstest.dynamic.DynamicTest;
import org.hyperskill.hstest.exception.outcomes.WrongAnswer;
import org.hyperskill.hstest.mocks.web.response.HttpResponse;
import org.hyperskill.hstest.stage.SpringTest;
import org.hyperskill.hstest.testcase.CheckResult;

public class QRCodeApiTest extends SpringTest {

    @DynamicTest
    CheckResult testGetHealth() {
        var url = "/api/health";
        HttpResponse response = get(url).send();

        checkStatusCode(
            response.getRequest().getEndpoint(),
            response.getStatusCode(),
            200);

        return CheckResult.correct();
    }

    @DynamicTest
    CheckResult testGetQrCode() {
        var url = "/api/qrcode";
        HttpResponse response = get(url).send();

        checkStatusCode(
            response.getRequest().getEndpoint(),
            response.getStatusCode(),
            501);

        return CheckResult.correct();
    }

    private void checkStatusCode(String endpoint, int actual, int expected) {
        if (actual != expected) {
            throw new WrongAnswer("""
                    Request: GET %s
                                        
                    Response has incorrect status code:
                    Expected %d, but responded with %d
                                        
                    """.formatted(endpoint, expected, actual)
                    );
        }
    }
}
