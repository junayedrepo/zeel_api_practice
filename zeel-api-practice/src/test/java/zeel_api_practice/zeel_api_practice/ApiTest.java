package zeel_api_practice.zeel_api_practice;

import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.time.Month;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.path.json.JsonPath.from;
import static io.restassured.RestAssured.given;

import org.json.simple.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

public class ApiTest {

    private static String host = "hs4hqu0udj.execute-api.us-east-1.amazonaws.com";
    private static String apiKey = "940893157622"; //got by calling 'host/test/getkey' url"
    private static String patientsListEndpoint = "patient";
    private static String apiKeyQueryParameter = "api_key";
    
    @BeforeClass
    public static void setupURL() {
        // here we setup the default URL and API base path to use throughout the tests
        RestAssured.baseURI = "https://" + host;
        RestAssured.basePath = "/test/";
    }
    
    //
    // #5
    //
    @Test
    public void test_has_appointment_date_in_june_2022() {

        //
        // The Response from the API call
        //
        Response response = given()
                .queryParam(apiKeyQueryParameter, apiKey)
                .when()
                .get(patientsListEndpoint)
                .then()
                .contentType(ContentType.JSON)
                .extract().response();
        //
        // Just want the appointment dates
        //
        List<String> appointments = response.path("appointment_date");
        //
        // Only need to check if we have just one
        //
        boolean containsAtLeastOne = false;
        //
        // Looping through list of appts
        //
        for (String aDateString : appointments) {
            //
            // Turning date string into Date
            //
            LocalDate apptDate = LocalDate.parse(aDateString);
            //
            // Extract the month
            //
            Month month = apptDate.getMonth();
            //
            // Extract year
            //
            int year = apptDate.getYear();
            //
            // Do our comparison
            //
            if (month.equals(Month.JUNE) && year == 2022) {
                containsAtLeastOne = true;
            }
        }
        //
        // Do our test assertion
        //
        assertTrue("Patient List has June 2022 appointments", containsAtLeastOne);
    }
    
    //
    // #6
    //
    @Test
    public void test_has_patient_id_83764S27R0876() {
        String requiredId = "83764S27R0876";
        //
        // The Response from the API call
        //
        Response response = given()
                .queryParam(apiKeyQueryParameter, apiKey)
                .when()
                .get(patientsListEndpoint)
                .then()
                .contentType(ContentType.JSON)
                .extract().response();

        //
         System.out.println(response.asString());
        //

        //
        // Verify patient with id
        //
        List<String> ids = response.path("id");
        //
        // Only need to check if we have just one
        //
        boolean patientIdExists = false;
        //
        // Looping through list of ids
        //
        for (String anId : ids) {
            if (anId.equals(requiredId)) {
            	patientIdExists = true;
            }
        }
        //
        // Assert that we have this id
        //
        assertTrue("Patient Id (83764S27R0876) does not exists!", patientIdExists);
    }
    
    //
    // #7
    //
    @Test
    public void test_has_valid_patient_id() {
        //
        // Id Made up by: first-init last-init birth-year birth-month birth-day
        // appt-year appt-month appt-day four-digits
        //

        Response response = given()
                .queryParam(apiKeyQueryParameter, apiKey)
                .when()
                .get(patientsListEndpoint)
                .then()
                .contentType(ContentType.JSON)
                .extract().response();

        //
        // System.out.println(response.asString());
        //

        String jsonAsString = response.asString();

        List<Map<String, ?>> jsonAsArrayList = from(jsonAsString).get("");

        for (Map<String, ?> aPatientMap : jsonAsArrayList) {
            String id = (String) aPatientMap.get("id");
            String[] birthday =  aPatientMap.get("birthdate").toString().split("-");
            String[] appointment_date = aPatientMap.get("appointment_date").toString().split("-");
            //
            // get First and Last Name Initials
            //
            Map<String, String> name = (Map<String, String>) aPatientMap.get("name");
            String fNameInitial = name.get("firstName").toString().substring(0, 1);
            String lNameInitial = name.get("lastName").toString().substring(0, 1);
            
            // 
            // Combine all values to make up id
            //
            String firstPartOfId = 
            		fNameInitial
            		+ lNameInitial
            		+ birthday[0] // birth year
            		+ birthday[1] // birth month
            		+ birthday[2] // birth date
            		+ appointment_date[0] // appointment year
            		+ appointment_date[1] // appointment month
            		+ appointment_date[2]; // appointment date
                    System.out.println(firstPartOfId);
                    
            assertTrue(id.startsWith(firstPartOfId));
        }
    }
        
    // 
    // use below method for #8, 9 10, and 12
    //
    private Map<String, ?> jsonForId(String id) {
        //
        // The Response from the API call
        //
        Response response = given()
                .queryParam(apiKeyQueryParameter, apiKey)
                .when()
                .get(patientsListEndpoint)
                .then()
                .contentType(ContentType.JSON)
                .extract().response();
        String jsonString = response.asString();
        List<Map<String, ?>> jsonAsArrayList = from(jsonString).get("");

        for (Map<String, ?> aPatientMap : jsonAsArrayList) {
            String anId = (String) aPatientMap.get("id");
            if (anId.equals(id)) {
                return aPatientMap;
            }
        }

        return null;

    }
    
    //
    // #8, 9, 10
    //
    @Test
    public void test_updating_specific_patient_info() {
        String objId = "SR19760827202206208364";

        String expectedFirstName = "Tester";
        String expectedLastName = "Awesome";
        String expectedStreet = "1234 I Can Count St";
        String expectedCity = "Urban Decay";
        String expectedState = "NJ";
        String expectedZip = "12345";
        String expectedBirthdate = "1976-08-27";
        String expectedPhone = "347-555-9876";
        String expectedApptDate = "2022-06-20";

        // https://hs4hqu0udj.execute-api.us-east-1.amazonaws.com/test/update?api_key=940893157622
        Map<String, Object> patientInfo = (Map<String, Object>) jsonForId(objId);

        Map<String, Object> newPayload = new HashMap<String, Object>();

        if (patientInfo != null) {

            newPayload.put("ID", objId);

            Map<String, Object> name = (Map<String, Object>) patientInfo.get("name");
            name.put("firstName", expectedFirstName);
            name.put("lastName", expectedLastName);

            // Name
            newPayload.put("name", name);

            Map<String, Object> address = (Map<String, Object>) patientInfo.get("address");
            address.put("street", expectedStreet);
            address.put("city", expectedCity);
            address.put("state", expectedState);
            address.put("zip", expectedZip);

            // Address
            newPayload.put("address", address);

            // GIVEN
            Response response = given()
                    .contentType(ContentType.JSON)

                    .queryParam(apiKeyQueryParameter, apiKey)
                    .body(newPayload)
                    .log()
                    .all()
                    .when()
                    .patch("update").andReturn();

            String jsonString = response.asString();

            String actualId = from(jsonString).get("id");
            String actualBirthdate = from(jsonString).get("birthdate");
            String actualPhone = from(jsonString).get("phone");
            String actualAppt = from(jsonString).get("appointment_date");
            String actualFirstName = from(jsonString).get("name.firstName");
            String actualLastName = from(jsonString).get("name.lastName");
            String actualStreet = from(jsonString).get("address.street");
            String actualCity = from(jsonString).get("address.city");
            String actualState = from(jsonString).get("address.state");
            String actualZip = from(jsonString).get("address.zip");

            // #10
            assertTrue(actualId.equals(objId));
            assertTrue(actualBirthdate.equals(expectedBirthdate));
            assertTrue(actualAppt.equals(expectedApptDate));
            assertTrue(actualFirstName.equals(expectedFirstName));
            assertTrue(actualLastName.equals(expectedLastName));
            assertTrue(actualPhone.equals(expectedPhone));
            assertTrue(actualStreet.equals(expectedStreet));
            assertTrue(actualCity.equals(expectedCity));
            assertTrue(actualState.equals(expectedState));
            assertTrue(actualZip.equals(expectedZip));

        }

    }
    
    //
    // #12
    //
    @Test
    public void test_post_JSON_payload() {
    	
      JSONObject request = new JSONObject();
    	
        request.put("firstName", "Junayed");
        request.put("lastName", "Ahmed");
        request.put("url", "https://github.com/jahmed84/zeel_api_practice");
        String payload = request.toJSONString();
        
        System.out.println(request);
                
        Response response = given()
                .queryParam(apiKeyQueryParameter, apiKey)
                .body(payload)
                .when()
                .post(patientsListEndpoint)
                .then()
                .contentType(ContentType.JSON)
                .statusCode(200)
                .extract().response();
    }

}
