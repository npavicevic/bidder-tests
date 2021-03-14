package bidder.tests;

import bidder.enpoints.Endpoints;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class getBidTests {

    private final String sample_bid = "db131b41-a0d6-42ee-8e2e-514a7459530d";
    private final String sample_ssp = "1";

    /**
     * Sends a request to server with specified query params and checks the specified response status code.
     * If the response code is as expected, a JSONObject is returned for further checks of the response.
     * @param ssp SSP (Supply Side Platform), which sends a server a request with information about the specific ad.
     * @param bid The ID of the bid.
     * @param adsize The size of the ad in widthxheight in pixels
     * @param statusCode The expected status code of the response.
     * @return JSONObject
     */
    public static JSONObject getBid(String ssp, String bid, String adsize, int statusCode)
    {
        RequestSpecification reqSpec = Endpoints.bidder();
        Response response = RestAssured
                .given()
                    .log().all()
                    .spec(reqSpec)
                    .queryParam("ssp", ssp)
                    .queryParam("bid", bid)
                    .queryParam("adsize", adsize)
                .when()
                    .get()
                .then()
                    .statusCode(statusCode)
                    .extract().response();

        // TODO: This should be removed when the response is returning application/json Content Type instead of text/plain, and some minor changes to tests need to be done.
        if(response.getBody().asString().equals(""))
            return new JSONObject("{}");
        else
            return new JSONObject(response.getBody().asString());
    }

    @Test
    public void getBidHappyPath()
    {
        JSONObject response = getBid(sample_ssp, sample_bid, "90x728", 200);

        Assert.assertEquals(sample_bid, response.getString("bidId"));
        Assert.assertEquals(1, response.getInt("bannerId"));
        Assert.assertEquals(10, response.getInt("price"));
    }

    @Test
    public void getBidInactive()
    {
        JSONObject response = getBid(sample_ssp, sample_bid, "250x300", 200);

        Assert.assertTrue(response.isEmpty());
    }

    @Test
    public void getBidNoBanner()
    {
        JSONObject response = getBid(sample_ssp, sample_bid, "1x1", 200);

        Assert.assertTrue(response.isEmpty());
    }

    @Test
    public void getBidNoSSP()
    {
        RequestSpecification reqSpec = Endpoints.bidder();
        RestAssured
                .given()
                    .log().all()
                    .spec(reqSpec)
                        .queryParam("bid", sample_bid)
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
                        .queryParam("ssp", sample_ssp)
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
                        .queryParam("ssp", sample_ssp)
                        .queryParam("bid", sample_bid)
                .when()
                    .get()
                .then()
                    .statusCode(400)
                    .body("error", equalTo("No Bid ID Provided."));
    }

    @Test
    public void getBidOverBudget()
    {
        JSONObject response = getBid(sample_ssp, sample_bid, "200x500", 200);

        Assert.assertTrue(response.has("error"));
        Assert.assertEquals("Not enough budget.", response.getString("error"));
    }

    @Test
    public void getBidNegativeBudget()
    {
        JSONObject response = getBid(sample_ssp, sample_bid, "350x350", 200);

        Assert.assertTrue(response.has("error"));
        Assert.assertEquals("Not enough budget.", response.getString("error"));
    }

    @Test
    public void getBidHigherPrice()
    {
        JSONObject response = getBid(sample_ssp, sample_bid, "600x160", 200);

        Assert.assertEquals(sample_bid, response.getString("bidId"));
        Assert.assertEquals(5, response.getInt("bannerId"));
        Assert.assertEquals(20, response.getInt("price"));
    }

    @Test
    public void getBidSamePrice()
    {
        JSONObject response = getBid(sample_ssp, sample_bid, "100x100", 200);

        Assert.assertEquals(sample_bid, response.getString("bidId"));
        assertThat(response.getInt("bannerId"), either(is(6)).or(is(7)));
        Assert.assertEquals(5, response.getInt("price"));

    }

    @Test
    public void getBidReflectedXSS()
    {
        String maliciousString = "<script>alert(1)</script>";
        JSONObject response = getBid(sample_ssp, maliciousString, "90x728", 200);

        Assert.assertFalse(response.getString("bidId").contains(sample_ssp));
    }
}
