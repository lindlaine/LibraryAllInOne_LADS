package com.library.steps;


import com.library.pages.BasePage;
import com.library.pages.BookPage;
import com.library.pages.LoginPage;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import lombok.extern.slf4j.Slf4j;
import static com.library.steps.APIStepDefs.randomDataMap;
import static org.junit.Assert.*;


@Slf4j
public class UIStepDefs extends BasePage {
    LoginPage loginPage = new LoginPage();
    BookPage bookPage = new BookPage();


//    =========================US03 PT 2========================
//    ===============Create a new book all layers===============
    @Given("I logged in Library UI as {string}")
    public void i_logged_in_library_ui_as(String userType) {
        loginPage.login(userType);
    }

    @Given("I navigate to {string} page")
    public void i_navigate_to_page(String pageName) {
        bookPage.navigateModule(pageName);
    }




//    ============================US04================================
//    ==================Create a new user all layers==================
@Then("created user should be able to login Library UI")
public void created_user_should_be_able_to_login_library_ui() {
    String email = randomDataMap.get("email").toString();
    String password = randomDataMap.get("password").toString();

    LoginPage loginPage = new LoginPage();
    loginPage.login(email, password);

}

    @Then("created user name should appear in Dashboard Page")
    public void created_user_name_should_appear_in_dashboard_page() {
        String actualAccountName = accountHolderName.getText();
        String expectedAccountName = randomDataMap.get("full_name").toString();

        System.out.println("actualAccountName = " + actualAccountName);
        System.out.println("expectedAccountName = " + expectedAccountName);

        assertEquals(expectedAccountName, actualAccountName);
    }

}
