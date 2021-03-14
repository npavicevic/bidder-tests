package bidder.enpoints;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;

public class Endpoints {
    public static RequestSpecification bidder()
    {
        return new RequestSpecBuilder()
                .setBaseUri("http://localhost")
                .setPort(12345)
                .setBasePath("/bidder/")
                .build();
    }
}
