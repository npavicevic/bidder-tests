package bidder.tests;

import bidder.enpoints.Endpoints;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class getBidTests {

    public static ValidatableResponse getBid(String ssp, String bid, String adsize)
    {
        RequestSpecification reqSpec = Endpoints.bidder();
        return RestAssured
                .given()
                    .log().all()
                    .spec(reqSpec)
                    .queryParam("ssp", ssp)
                    .queryParam("bid", bid)
                    .queryParam("adsize", adsize)
                .when()
                    .get()
                .then();
    }

    @Test
    public void getBidHappyPath()
    {
        Response response =
                getBid("1", "db131b41-a0d6-42ee-8e2e-514a7459530d", "90x728")
                    .statusCode(200)
                    .extract().response();
        JSONObject jsonObject = new JSONObject(response.getBody().asString());
        Assertions.assertEquals("db131b41-a0d6-42ee-8e2e-514a7459530d", jsonObject.getString("bidId"));
        Assertions.assertEquals(1, jsonObject.getInt("bannerId"));
        Assertions.assertEquals(10, jsonObject.getInt("price"));
    }

    @Test
    public void getBidInactive()
    {
        Response response =
                getBid("1", "db131b41-a0d6-42ee-8e2e-514a7459530d", "250x300")
                    .statusCode(200)
                    .extract().response();
        Assertions.assertEquals("", response.getBody().asString());
    }

    @Test
    public void getBidNoBanner()
    {
        Response response =
                getBid("1", "db131b41-a0d6-42ee-8e2e-514a7459530d", "1x1")
                    .statusCode(200)
                    .extract().response();
        Assertions.assertEquals("", response.getBody().asString());
    }

    @Test
    public void getBidNoSSP()
    {
        RequestSpecification reqSpec = Endpoints.bidder();
        RestAssured
                .given()
                    .log().all()
                    .spec(reqSpec)
                        .queryParam("bid", "db131b41-a0d6-42ee-8e2e-514a7459530d")
                        .queryParam("adsize", "600x160")
                .when()
                    .get()
                .then()
                    .statusCode(400)
                    .body("error", equalTo("No SSP Provided."));
    }

    @Test
    public void getBidNoBidId()
    {
        RequestSpecification reqSpec = Endpoints.bidder();
        RestAssured
                .given()
                    .log().all()
                    .spec(reqSpec)
                        .queryParam("ssp", "1")
                        .queryParam("adsize", "600x160")
                .when()
                    .get()
                .then()
                    .statusCode(400)
                    .body("error", equalTo("No Bid ID Provided."));
    }

    @Test
    public void getBidNoAdSize()
    {
        RequestSpecification reqSpec = Endpoints.bidder();
        RestAssured
                .given()
                    .log().all()
                    .spec(reqSpec)
                        .queryParam("ssp", "1")
                        .queryParam("bid", "db131b41-a0d6-42ee-8e2e-514a7459530d")
                .when()
                    .get()
                .then()
                    .statusCode(400)
                    .body("error", equalTo("No Bid ID Provided."));
    }

    @Test
    public void getBidOverBudget()
    {
        Response response =
                getBid("1", "db131b41-a0d6-42ee-8e2e-514a7459530d", "200x500")
                        .statusCode(200).extract().response();
        JSONObject jsonObject = new JSONObject(response.getBody().asString());
        Assertions.assertEquals("Not enough budget.", jsonObject.getString("error"));
    }

    @Test
    public void getBidNegativeBudget()
    {
        Response response =
                getBid("1", "db131b41-a0d6-42ee-8e2e-514a7459530d", "350x350")
                        .statusCode(200).extract().response();
        JSONObject jsonObject = new JSONObject(response.getBody().asString());
        Assertions.assertEquals("Not enough budget.", jsonObject.getString("error"));
    }

    @Test
    public void getBidHigherPrice()
    {
        Response response =
                getBid("1", "db131b41-a0d6-42ee-8e2e-514a7459530d", "600x160")
                        .statusCode(200)
                        .extract().response();
        JSONObject jsonObject = new JSONObject(response.getBody().asString());
        Assertions.assertEquals("db131b41-a0d6-42ee-8e2e-514a7459530d", jsonObject.getString("bidId"));
        Assertions.assertEquals(5, jsonObject.getInt("bannerId"));
        Assertions.assertEquals(20, jsonObject.getInt("price"));
    }

    @Test
    public void getBidSamePrice()
    {
        Response response =
                getBid("1", "db131b41-a0d6-42ee-8e2e-514a7459530d", "100x100")
                        .statusCode(200)
                        .extract().response();
        JSONObject jsonObject = new JSONObject(response.getBody().asString());
        Assertions.assertEquals("db131b41-a0d6-42ee-8e2e-514a7459530d", jsonObject.getString("bidId"));
        assertThat(jsonObject.getInt("bannerId"), either(is(6)).or(is(7)));
        Assertions.assertEquals(10, jsonObject.getInt("price"));

    }

    @Test
    public void getBidReflectedXSS()
    {
        Response response =
                getBid("1", "<script>alert(1)</script>", "90x728")
                        .statusCode(200)
                        .extract().response();
        JSONObject jsonObject = new JSONObject(response.getBody().asString());
        Assertions.assertFalse(jsonObject.getString("bidId").contains("<script>alert(1)</script>"));
    }
}
