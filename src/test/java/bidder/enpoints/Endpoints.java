package bidder.enpoints;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;

/**
 * This is the class where the endpoints for tests are defined.
 */
public class Endpoints {
    private static final String baseUri = "http://localhost";
    /**
     * This method returns a Request specification of the bidder endpoint.
     * @return RequestSpecification
     */
    public static RequestSpecification bidder()
    {
        return new RequestSpecBuilder()
                .setBaseUri(baseUri)
                .setPort(12345)
                .setBasePath("/bidder/")
                .build();
    }
}
