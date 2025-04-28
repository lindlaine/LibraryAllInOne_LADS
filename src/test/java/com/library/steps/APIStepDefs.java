package com.library.steps;

import com.library.pages.BookPage;
import com.library.pages.LoginPage;
import com.library.utility.BrowserUtil;
import com.library.utility.DB_Util;
import com.library.utility.DatabaseHelper;
import com.library.utility.LibraryAPI_Util;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

public class APIStepDefs extends LibraryAPI_Util {

    RequestSpecification givenPart = given().log().all();
    Response response;
    ValidatableResponse thenPart;
    JsonPath jp;
    String paramValue;
    String token;


//    ==============================US01==============================
//    ===============Retrieve all users from the API endpoint===============

    @Given("I logged Library api as a {string}")
    public void i_logged_library_api_as_a(String role) {
        givenPart.header("x-library-token", LibraryAPI_Util.getToken(role));
    }

    @Given("Accept header is {string}")
    public void accept_header_is(String acceptHeader) {
        givenPart.accept(acceptHeader);
    }

    @When("I send GET request to {string} endpoint")
    public void i_send_get_request_to_endpoint(String endpoint) {
        response = givenPart.when().get(endpoint);
//        response.prettyPrint();
    }

    @Then("status code should be {int}")
    public void status_code_should_be(int expectedStatusCode) {
        thenPart = response.then();
        thenPart.statusCode(expectedStatusCode);
    }

    @Then("Response Content type is {string}")
    public void response_content_type_is(String expectedResponseContentType) {
        thenPart.contentType(expectedResponseContentType);
    }

    @Then("Each {string} field should not be null")
    public void each_field_should_not_be_null(String path) {
        thenPart.body(path, everyItem(notNullValue()));
    }

//    ==============================US02==============================
//    ======================Retrieve single user======================

    @Given("Path param {string} is {string}")
    public void path_param_is(String paramKey, String paramValue) {
        this.paramValue = paramValue;
        givenPart.pathParam(paramKey, paramValue);
    }

    @Then("{string} field should be same with path param")
    public void field_should_be_same_with_path_param(String path) {
        String actualResponse = response.path(path);
        String expectedResponse = paramValue;
        System.out.println("actualResponse = " + actualResponse);
        System.out.println("expectedResponse = " + expectedResponse);
        assertEquals(expectedResponse, actualResponse);
    }

    @Then("following fields should not be null")
    public void following_fields_should_not_be_null(List<String> fields) {
        for (String eachField : fields) {
//            System.out.println("response.path(eachField) = " + response.path(eachField));
            thenPart.body(eachField, notNullValue());
        }
    }

    //    ==============================US03==============================
//    ======================Create a new book API=====================
    @Given("Request Content Type header is {string}")
    public void request_content_type_header_is(String contentType) {
        givenPart.contentType(contentType);
    }

    public static Map<String, Object> randomDataMap;

    @Given("I create a random {string} as request body")
    public void i_create_a_random_as_request_body(String type) {

        switch (type.toLowerCase()) {
            case "book":
                randomDataMap = getRandomBookMap();
                break;
            case "user":
                randomDataMap = getRandomUserMap();
                break;
            default:
                throw new IllegalArgumentException("Unsupported type: " + type);

        }
//        givenPart.formParams(randomDataMap);

    }

    @When("I send POST request to {string} endpoint")
    public void i_send_post_request_to_endpoint(String postEndpoint) {
        response = givenPart.formParams(randomDataMap).when().post(postEndpoint);
//        response = givenPart.when().post(postEndpoint);
    }

    @Then("the field value for {string} path should be equal to {string}")
    public void the_field_value_for_path_should_be_equal_to(String path, String expectedValue) {
        thenPart = response.then();
        System.out.println("response.path(path) = " + response.path(path));
        assertEquals(expectedValue, response.path(path));
    }

    @Then("{string} field should not be null")
    public void field_should_not_be_null(String path) {
        thenPart.body(path, notNullValue());
    }

    //    ==============================US03==============================
//    ======================Verify All Layers Match=====================
    LoginPage loginPage = new LoginPage();
    BookPage bookPage = new BookPage();

    @Then("UI, Database and API created book information must match")
    public void ui_database_and_api_created_book_information_must_match() {
        jp = response.jsonPath();
        String bookIdAPI = jp.getString("book_id");


//        Retrieve API
        jp = givenPart.accept(ContentType.JSON).pathParam("id", bookIdAPI).when().get("/get_book_by_id/{id}")
                .then().statusCode(200).extract().jsonPath();

        String nameAPI = jp.getString("name");
        String isbnAPI = jp.getString("isbn");
        String yearAPI = jp.getString("year");
        String authorAPI = jp.getString("author");
        String categoryIdAPI = jp.getString("book_category_id");
        String descriptionAPI = jp.getString("description");

        List<String> expectedDataAPI = new ArrayList<>();
        expectedDataAPI.add(nameAPI);
        expectedDataAPI.add(isbnAPI);
        expectedDataAPI.add(yearAPI);
        expectedDataAPI.add(authorAPI);
        expectedDataAPI.add(categoryIdAPI);
        expectedDataAPI.add(descriptionAPI);

//        Retrieve DB
        String query = DatabaseHelper.getBookByIdQuery(bookIdAPI);
        DB_Util.runQuery(query);


        Map<String,Object> dbDataMap = DB_Util.getRowMap(1);
        String nameDB = (String) dbDataMap.get("name");
        String isbnDB = (String) dbDataMap.get("isbn");
        String yearDB = (String) dbDataMap.get("year");
        String authorDB = (String) dbDataMap.get("author");
        String categoryDB = (String) dbDataMap.get("book_category_id");
        String descriptionDB = (String) dbDataMap.get("description");

        List<String> expectedDataDB = new ArrayList<>();
        expectedDataDB.add(nameDB);
        expectedDataDB.add(isbnDB);
        expectedDataDB.add(yearDB);
        expectedDataDB.add(authorDB);
        expectedDataDB.add(categoryDB);
        expectedDataDB.add(descriptionDB);

//        Retrieve UI
        String createdBookName = (String) randomDataMap.get("name");
        bookPage.search.sendKeys(createdBookName);
        bookPage.editBook(createdBookName).click();

        List<String> expectedDataUI = new ArrayList<>();

        BrowserUtil.waitFor(2);



        expectedDataUI.add(bookPage.bookName.getAttribute("value"));
        expectedDataUI.add(bookPage.isbn.getAttribute("value"));
        expectedDataUI.add(bookPage.year.getAttribute("value"));
        expectedDataUI.add(bookPage.author.getAttribute("value"));
        Select select = new Select(bookPage.categoryDropdown);
        expectedDataUI.add(select.getFirstSelectedOption().getAttribute("value"));
        expectedDataUI.add(bookPage.description.getAttribute("value"));

        System.out.println("expectedDataUI = " + expectedDataUI);
        System.out.println("expectedDataAPI = " + expectedDataAPI);
        System.out.println("expectedDataDB = " + expectedDataDB);

        assertTrue(expectedDataAPI.equals(expectedDataDB) && expectedDataDB.equals(expectedDataUI));
    }

    //    ====================================US04====================================
//    ===========================Create a new user ALL LAYERS============================
    @Then("created user information should match with Database")
    public void created_user_information_should_match_with_database() {

//        get userID
        jp = response.jsonPath();
        int id = jp.getInt("user_id");
//        System.out.println("id = " + id);

//        Run query
        String query = DatabaseHelper.getUserByIdQuery(id);
        DB_Util.runQuery(query);
        Map<String, Object> dbDataMap = DB_Util.getRowMap(1);

        System.out.println("dbDataMap = " + dbDataMap);
        System.out.println("randomDataMap = " + randomDataMap);

        // compare fields that exist in both
        assertEquals(randomDataMap.get("full_name"), dbDataMap.get("full_name"));
        assertEquals(randomDataMap.get("email"), dbDataMap.get("email"));
        assertEquals(randomDataMap.get("user_group_id").toString(), dbDataMap.get("user_group_id"));
    }


    //    ==============================US05==============================
//    ==========================Decode User===========================
    @Given("I logged Library api with credentials {string} and {string}")
    public void i_logged_library_api_with_credentials_and(String email, String password) {
        token = LibraryAPI_Util.getToken(email, password);
    }

    @Given("I send {string} information as request body")
    public void i_send_information_as_request_body(String request) {
        randomDataMap = Map.of(request, token);
//        givenPart.formParams(randomDataMap);
    }
}
