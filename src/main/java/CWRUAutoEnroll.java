import java.util.ArrayList;
import java.time.Duration;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

public class CWRUAutoEnroll {

    //Twilio acc info
    public static final String accountID = System.getenv("TWILIO_ACCOUNT_ID");
    public static final String authToken = System.getenv("TWILIO_ACCOUNT_TOKEN");

    //user's phone number for notifications
    static String phoneNumber = "+1" + LoginGUI.getPhoneNumber();

    //user's caseID and password for login
    static String caseID = LoginGUI.getUser();
    static String passphrase = LoginGUI.getPassword();

    //HashMap to store the selected classes in the shopping cart
    //also possibly the classes that failed to enroll
    static HashMap<String, String> selectedClasses = new HashMap<>();

    //HashMap to store the classes that will be enrolled in during the next shopping cart scrolling
    static ArrayList<String> enrollList = new ArrayList<>();

    static HashMap<String, String> dontEnrollList = new HashMap<>();

    //method that returns true if current index is lab, false if not lab
    public static boolean isLab(WebDriver driver, int index){
        try {
            driver.findElement(By.cssSelector("span[id='DERIVED_SSR_FL_SSR_AVAIL_FL$" + (index+1) + "']"));
        } catch (NoSuchElementException e) { //this means we're at the end of the shopping cart
            return false;
        }

        WebElement lab = driver.findElement(By.cssSelector("span[id='DERIVED_SSR_FL_SSR_AVAIL_FL$" + (index+1) + "']"));
        String labText = lab.getText();

        if (labText.equals(" "))
            return true;
        return false;
    }

    //method that uses Twilio to send SMS text message
    public static void sendSMS(String usersPhoneNumber, String noti){
        //if your number is American, begin with "+1xxxxxxxxxx"
        //if otherwise, begin with "+(respective country area code)xxxxxxxxxx"

        Twilio.init(accountID, authToken);
        Message message = Message.creator(
                        new PhoneNumber(usersPhoneNumber),
                        new PhoneNumber("+14156219032"),
                        noti)
                .create();
    }

    //method that navigates to the shopping cart page
    public static void goToShoppingCart(WebDriver driver){
        driver.get("https://sis.case.edu/psc/P92SCWR_newwin/EMPLOYEE/SA/c/SSR_STUDENT_FL.SSR_SHOP_CART_FL.GBL?Page=SSR_TERM_STA3_FL&pslnkid=CS_S201608070058152725454770&ICAJAXTrf=true");
        if(cartIsEmpty(driver)){
            try {
                //try to find an element that only exists on the empty shopping cart page
                driver.findElement(By.cssSelector("span[id='DERIVED_SSR_FL_SSR_NOCLASSES_MSG$88$']"));
            } catch (NoSuchElementException e) {
                //The current page is the "select a term page", meaning user has classes in cart from multiple terms
                int i = 0;
                while(i < 13){
                    try {
                        driver.findElement(By.cssSelector("a[id='SSR_CART_TRM_FL_TERM_DESCR30$" + i + "']"));
                    } catch (NoSuchElementException x) {
                        WebElement currentSemester = driver.findElement(By.cssSelector("a[id='SSR_CART_TRM_FL_TERM_DESCR30$" + (i-1) + "']"));
                        currentSemester.click();
                        break;
                    }
                    i++;
                }

            }
            //if cart is empty and the no classes text field was successfully found, no more work needs to be done
        }
    }

    //helper method to select a class on the shopping cart page
    public static void selectClass(WebDriver driver, int index){
        WebElement checkBox = driver.findElement(By.cssSelector("input[id='DERIVED_REGFRM1_SSR_SELECT$" + index + "']"));
        checkBox.click();
    }

    //method that clicks the enroll button on the shopping cart page
    public static void clickEnroll(WebDriver driver){
        WebElement enroll = driver.findElement(By.cssSelector("a[id='DERIVED_SSR_FL_SSR_ENROLL_FL']"));
        enroll.click();
    }

    //method that clicks the validate button on the shopping cart page
    public static void clickValidate(WebDriver driver){
        WebElement validate = driver.findElement(By.cssSelector("a[id='DERIVED_SSR_FL_SSR_VALIDATE_FL']"));
        validate.click();
    }

    //method that delays the program by a specified number of seconds
    public static void delay(int seconds){
        int milliseconds = seconds*1000;
        try
        {
            System.out.println("Start of " + seconds + " second delay");
            Thread.sleep(milliseconds);
            System.out.println("End of " + seconds + " second delay");
        }
        catch(InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    //boolean method returns true if shopping cart is empty, false if not empty
    public static boolean cartIsEmpty(WebDriver driver){
        try {
            driver.findElement(By.cssSelector("span[id='DERIVED_SSR_FL_SSR_AVAIL_FL$" + 0 + "']"));
        } catch (NoSuchElementException e) {
            System.out.print("\nShopping Cart is Empty\n");
            return true;
        }
        return false;
    }

    //method parses lecture/lab ID text in the shopping cart
    public static String parseID(WebDriver driver, String lectureID){
        String[] arr = lectureID.split("- ");
        return arr[2];
    }

    /*
    method to parse the class name abbreviation and the following numbers
    Example: CSDS 314 Computer Architecture --> CSDS 314
    */
    public static String parseClassName(String className){
        String[] arr = className.split(" ", 3);
        return arr[0] + " " +  arr[1];
    }

    public static void waitForSelectClassScreen(WebDriver driver) throws InterruptedException {
        // explicit wait - to wait for the compose button to be click-able
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("span[id='DERIVED_SSR_FL_SSR_AVAIL_FL$" + 0 + "']")));
    }

    public static void waitForValidationScreen(WebDriver driver) throws InterruptedException {
        // explicit wait - to wait for the compose button to be click-able
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("span[id='DERIVED_REGFRM1_DESCRLONG$" + 0 + "']")));
    }

    public static String phedOrNormParse(String validPageClassName) {
        String[] fixer = validPageClassName.split(" ", 5);
        String fixedClassNameText;
        if (fixer[0].equals("PHED")){
            fixedClassNameText = fixer[0] + " " + fixer[3];
            String[] arr = fixedClassNameText.split(":", 2);
            fixedClassNameText = arr[0];

        } else {
            fixedClassNameText = fixer[0] + " " + fixer[2] + " " + fixer[4];
            fixedClassNameText = parseClassName(fixedClassNameText);
        }
        return fixedClassNameText;
    }


    public static void main(String[] args) {

        ChromeOptions options = new ChromeOptions();
        options.addArguments("start-maximized");
        options.addArguments("--remote-allow-origins=*");

        //Creating new WebDriver Instance
        WebDriver driver = new ChromeDriver(options);

        //Case SIS Login Link
        driver.get("https://sis.case.edu/psc/P92SCWR/EMPLOYEE/SA/c/NUI_FRAMEWORK.PT_LANDINGPAGE.GBL?");
        driver.findElement(By.name("SSO_Signin")).click();

        WebElement username = driver.findElement(By.id("username"));
        WebElement password = driver.findElement(By.id("password"));

        //Insert username and password
        username.sendKeys(caseID);
        password.sendKeys(passphrase);
        driver.findElement(By.id("login-submit")).click();
        driver.findElement(By.id("PTNUI_LAND_REC_GROUPLET_LBL$2")).click();

        // validation shopping cart scrolling, aka, 1st shopping cart scrolling
        goToShoppingCart(driver);
        try{waitForSelectClassScreen(driver);} //put this into "goToShoppingCart" method
        catch (InterruptedException e) {
            System.out.println("Slow internet probably");
        }

        while(!cartIsEmpty(driver)){

            while (selectedClasses.isEmpty()) {

                // 3 minute delay between reloads
                delay(3);
                goToShoppingCart(driver);
                try{waitForSelectClassScreen(driver);}
                catch (InterruptedException e) {
                    System.out.println("Slow internet probably");
                }

                int i = 0;
                while (i < Integer.MAX_VALUE) {

                    try {
                        driver.findElement(By.cssSelector("span[id='DERIVED_SSR_FL_SSR_AVAIL_FL$" + i + "']"));
                    } catch (NoSuchElementException e) {
                        System.out.print("Your Shopping Cart has been Checked!\n\n");
                        break;
                    }

                    //cart availability (open, closed, " ") elements
                    WebElement classStatus = driver.findElement(By.cssSelector("span[id='DERIVED_SSR_FL_SSR_AVAIL_FL$" + i + "']"));
                    String classStatusText = classStatus.getText();


                    if (classStatusText.equals("Open")) {

                        //enroll method WIP
                        WebElement className = driver.findElement(By.cssSelector("span[id='DERIVED_SSR_FL_SSR_DESCR80$" + i + "']"));
                        String classNameText = parseClassName(className.getText());

                        WebElement lectureID = driver.findElement(By.cssSelector("a[id='DERIVED_SSR_FL_SSR_CLASSNAME_LONG$" + i + "']"));
                        //parse last 4 digits
                        String lectureIDText = parseID(driver, lectureID.getText());

                        if (dontEnrollList.containsKey(classNameText)) {
                            if (isLab(driver, i)) {
                                WebElement labID = driver.findElement(By.cssSelector("a[id='DERIVED_SSR_FL_SSR_CLASSNAME_LONG$" + i+1 + "']"));
                                String labIDText = parseID(driver, labID.getText());
                                if (lectureIDText.equals(dontEnrollList.get(classNameText)) || labIDText.equals(dontEnrollList.get(classNameText))) {
                                    i += 2;
                                    continue;
                                } else {
                                    selectClass(driver, i);
                                    continue;
                                }
                            } else {
                                if (lectureIDText.equals(dontEnrollList.get(classNameText))) {
                                    i++;
                                    continue;
                                } else {
                                    selectClass(driver, i);
                                    continue;
                                }
                            }
                        }

                        selectedClasses.put(classNameText, lectureIDText);
                        selectClass(driver, i); //dontEnrollList doesn't contain class name? select

                        i++;
                    } else if (classStatusText.equals("Closed") || classStatusText.equals(" ")) {
                        i++;
                    }
                }
            }

            //this point can only be reached if while loop is broken
            //aka, some classes have opened up and been selected

            clickValidate(driver);
            delay(3); //this delay varies based on how quickly the validate screen loads
            try{waitForValidationScreen(driver);}
            catch (InterruptedException e) {
                System.out.println("Slow internet probably");
            }

            //validation page scrolling, aka 2nd scrolling
            int m = 0;
            while (m < Integer.MAX_VALUE) {

                try {
                    driver.findElement(By.cssSelector("span[id='DERIVED_REGFRM1_DESCRLONG$" + m + "']"));
                } catch (NoSuchElementException e) {
                    goToShoppingCart(driver);
                    System.out.print("Validation Complete!\n");
                    break;
                }

                WebElement className = driver.findElement(By.cssSelector("span[id='DERIVED_REGFRM1_DESCRLONG$" + m + "']"));
                String classNameText = className.getText();


                //win## changes every time validation page is loaded. Edit find element to search for just divDERIVED_REGFRM1_SS_MESSAGE_LONG$ + m instead
                WebElement message = driver.findElement(By.cssSelector("div[id$='divDERIVED_REGFRM1_SS_MESSAGE_LONG$" + m + "']"));
                String messageText = message.getText();

                //this is to remove the extra spaces in the class name on the validation page
                /* On the validation page, the class name displayed for PHED classes are unusual. They require a method that handles
                the unique name displayed */
                String fixedClassNameText = phedOrNormParse(classNameText);

                if (messageText.contains("There is a time conflict for")) {

                    //this is to parse the ID number of the class from the time conflict error message
                    Matcher matcher = Pattern.compile("\\d+").matcher(messageText);
                    ArrayList<String> arr = new ArrayList<>();
                    while (matcher.find()) {
                        arr.add(matcher.group());
                    }

                    dontEnrollList.put(fixedClassNameText, arr.get(1));
                    sendSMS(phoneNumber, "FooBot: There is currently a meeting time conflict for two of your shopping cart classes," +
                            " or a shopping cart class and one of your enrolled classes for this term.\n\nClass attempted to enroll: "
                            + fixedClassNameText + "\n\nConflicting Class ID: " + arr.get(0)  );
                }

                if (messageText.contains("Okay to Add to Class Schedule")) {
                    enrollList.add(fixedClassNameText);
                    sendSMS(phoneNumber, "FooBot: The course " + fixedClassNameText + " has been successfully enrolled in!");
                }

                if (messageText.contains("This course has been taken previously. You may add this class,")) {
                    String lectureID = selectedClasses.get(fixedClassNameText);
                    enrollList.add(fixedClassNameText);
                    sendSMS(phoneNumber, "FooBot: The course " + fixedClassNameText + " has been successfully enrolled in! "
                            + "However, this course has been taken previously. Once the class has been graded you may exceed the " +
                            "repeatable limit, depending on the grade you receive.");
                }

                if (messageText.contains("Term unit maximum would be exceeded")) {
                    String lectureID = selectedClasses.get(fixedClassNameText);
                    dontEnrollList.put(fixedClassNameText, lectureID);
                    sendSMS(phoneNumber, "FooBot: To enroll in the course " + fixedClassNameText + ", " + messageText);
                }

                if (messageText.contains("Multiple enrollments are not allowed for this class")) {
                    String lectureID = selectedClasses.get(fixedClassNameText);
                    dontEnrollList.put(fixedClassNameText, lectureID);
                    sendSMS(phoneNumber, "FooBot: The course " + fixedClassNameText + " cannot be enrolled in because " + messageText);
                }

                if (messageText.contains("Enrollment Requisites are not met")) {
                    String lectureID = selectedClasses.get(fixedClassNameText);
                    dontEnrollList.put(fixedClassNameText, lectureID);
                    sendSMS(phoneNumber, "FooBot: You cannot enroll in " + fixedClassNameText + " because " + messageText);
                }

                m++;
            }

            try{waitForSelectClassScreen(driver);}
            catch (InterruptedException e) {
                System.out.println("Slow internet probably");
            }
            //we are now in the shopping cart (due to previous try-catch block) and are ready for the 3rd scroll (enrolling scroll)
            int p = 0;
            while (p < Integer.MAX_VALUE) {

                try {
                    driver.findElement(By.cssSelector("span[id='DERIVED_SSR_FL_SSR_AVAIL_FL$" + p + "']"));
                } catch (NoSuchElementException e) {
                    System.out.print("\nYour Shopping Cart has been Checked!\n");
                    break;
                }

                //cart availability (open, closed, " ") elements
                WebElement classStatus = driver.findElement(By.cssSelector("span[id='DERIVED_SSR_FL_SSR_AVAIL_FL$" + p + "']"));
                String classStatusText = classStatus.getText();


                if (classStatusText.equals("Open")) {
                    WebElement className = driver.findElement(By.cssSelector("span[id='DERIVED_SSR_FL_SSR_DESCR80$" + p + "']"));

                    //edge case: class name on shopping cart page can be different from class name on validation page
                    String classNameText = parseClassName(className.getText());

                    WebElement lectureID = driver.findElement(By.cssSelector("a[id='DERIVED_SSR_FL_SSR_CLASSNAME_LONG$" + p + "']"));
                    String lectureIDText = parseID(driver, lectureID.getText());
                    //parse the ID right here (the last 4-5 digits is the ID


                    if (enrollList.contains(classNameText)) {
                        if (dontEnrollList.containsKey(classNameText)) {
                            if (isLab(driver, p)) {
                                WebElement labID = driver.findElement(By.cssSelector("a[id='DERIVED_SSR_FL_SSR_CLASSNAME_LONG$" + p+1 + "']"));
                                String labIDText = parseID(driver, labID.getText());
                                //parse the ID right here (the last 4-5 digits is the ID)

                                //potential problem: dontEnrollList HashMap doesn't have the class ID it needs for the respective class
                                if (lectureIDText.equals(dontEnrollList.get(classNameText)) || labIDText.equals(dontEnrollList.get(classNameText))) {
                                    p += 2;
                                    continue;
                                } else {
                                    selectClass(driver, p);
                                    continue;
                                }
                            } else { //if it's not a lab
                                if (lectureIDText.equals(dontEnrollList.get(classNameText))) {
                                    p++;
                                    continue;
                                } else {
                                    selectClass(driver, p);
                                    continue;
                                }
                            }
                        } //end of "if dontEnrollList contains className". Should be impossible to reach this point if doNotEnrollList contains class ID

                        selectClass(driver, p);//dontEnrollList doesn't contain class name? select
                    }
                    p++; //enroll list doesn't contain class name? skip
                } else if (classStatusText.equals("Closed") || classStatusText.equals(" ")) {
                    p++;
                }
            }
            clickEnroll(driver);
            selectedClasses.clear();

        }
        //at this point, shopping cart is empty
        sendSMS(phoneNumber, "FooBot: You're Shopping Cart is Empty!");
        System.out.println("\nprogram finished");
    }
}