package com.nifilili.business.e2e;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * End-to-end happy path for the business module.
 *
 * This test intentionally uses real HTTP calls against a running deployment
 * (local/QA/staging) to validate the onboarding sequence with realistic auth
 * and endpoint wiring.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BusinessHappyPathE2eTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static HttpClient httpClient;
    private static String baseUrl;

    private static String adminJwtToken;
    private static String userJwtToken;

    private static Long verticalId;
    private static Long categoryId;
    private static Long sectionId;
    private static Long sectionFieldId;
    private static Long attributeId;
    private static Long documentDefinitionId;
    private static Long businessId;

    @BeforeAll
    static void setUpClient() {
        httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(20))
                .build();
        baseUrl = BusinessE2eRuntimeConfiguration.baseUrl();
    }

    @Test
    @Order(1)
    void loginAdmin_ShouldReturnJwtToken() throws Exception {
        JsonNode response = postJson(
                "/api/auth/login",
                "{\"usernameOrEmail\":\"" + BusinessE2eRuntimeConfiguration.adminUsername() +
                        "\",\"password\":\"" + BusinessE2eRuntimeConfiguration.adminPassword() + "\"}",
                null,
                200
        );

        adminJwtToken = response.path("accessToken").asText();
        assertTrue(adminJwtToken != null && !adminJwtToken.isBlank());
    }

    @Test
    @Order(2)
    void adminConfigurationFlow_ShouldCreateAllMasterDefinitions() throws Exception {
        Instant uniqueMarker = Instant.now();
        String suffix = String.valueOf(uniqueMarker.toEpochMilli());

        JsonNode verticalResponse = postJson(
                "/api/v1/admin/config/verticals",
                """
                {
                  "name": "Food Services %s",
                  "slug": "food-services-%s",
                  "description": "Vertical for E2E onboarding",
                  "iconUrl": "https://example.com/icons/food.png",
                  "active": true
                }
                """.formatted(suffix, suffix),
                adminJwtToken,
                200
        );
        verticalId = verticalResponse.path("id").asLong();
        assertTrue(verticalId > 0);

        JsonNode categoryResponse = postJson(
                "/api/v1/admin/config/categories",
                """
                {
                  "businessVerticalId": %d,
                  "parentCategoryId": null,
                  "name": "Cafe %s",
                  "slug": "cafe-%s",
                  "description": "Cafe category for E2E",
                  "iconUrl": "https://example.com/icons/cafe.png",
                  "activeStatus": true
                }
                """.formatted(verticalId, suffix, suffix),
                adminJwtToken,
                200
        );
        categoryId = categoryResponse.path("id").asLong();
        assertTrue(categoryId > 0);

        JsonNode sectionResponse = postJson(
                "/api/v1/admin/config/sections",
                """
                {
                  "verticalId": %d,
                  "categoryId": %d,
                  "name": "menu_details_%s",
                  "label": "Menu Details",
                  "promptText": "Please provide menu details",
                  "required": true,
                  "allowMultiple": false,
                  "groupable": false
                }
                """.formatted(verticalId, categoryId, suffix),
                adminJwtToken,
                200
        );
        sectionId = sectionResponse.path("id").asLong();
        assertTrue(sectionId > 0);

        JsonNode fieldResponse = postJson(
                "/api/v1/admin/config/sections/%d/fields".formatted(sectionId),
                """
                {
                  "name": "primary_dish",
                  "label": "Primary Dish",
                  "type": "TEXT",
                  "options": [],
                  "required": true,
                  "allowMultiple": false
                }
                """,
                adminJwtToken,
                200
        );
        sectionFieldId = fieldResponse.path("id").asLong();
        assertTrue(sectionFieldId > 0);

        JsonNode attributeResponse = postJson(
                "/api/v1/admin/config/attributes",
                """
                {
                  "verticalId": %d,
                  "name": "service_mode_%s",
                  "label": "Service Mode",
                  "type": "DROPDOWN",
                  "options": ["DINE_IN", "TAKEAWAY"],
                  "required": true,
                  "allowMultiple": false,
                  "prompt": "Choose one mode"
                }
                """.formatted(verticalId, suffix),
                adminJwtToken,
                200
        );
        attributeId = attributeResponse.path("id").asLong();
        assertTrue(attributeId > 0);

        JsonNode documentResponse = postJson(
                "/api/v1/admin/config/documents",
                """
                {
                  "verticalId": %d,
                  "name": "business_registration_%s",
                  "label": "Business Registration",
                  "allowedExtensions": ["pdf", "jpg"],
                  "maxFileSize": 5242880,
                  "required": true
                }
                """.formatted(verticalId, suffix),
                adminJwtToken,
                200
        );
        documentDefinitionId = documentResponse.path("id").asLong();
        assertTrue(documentDefinitionId > 0);
    }

    @Test
    @Order(3)
    void loginUser_ShouldReturnJwtToken() throws Exception {
        JsonNode response = postJson(
                "/api/auth/login",
                "{\"usernameOrEmail\":\"" + BusinessE2eRuntimeConfiguration.userUsername() +
                        "\",\"password\":\"" + BusinessE2eRuntimeConfiguration.userPassword() + "\"}",
                null,
                200
        );

        userJwtToken = response.path("accessToken").asText();
        assertTrue(userJwtToken != null && !userJwtToken.isBlank());
    }

    @Test
    @Order(4)
    void onboardingHappyPath_ShouldCreateAndSubmitBusiness() throws Exception {
        JsonNode createBusinessResponse = postJson(
                "/api/v1/business",
                """
                {
                  "verticalId": %d,
                  "name": "Happy Path Cafe",
                  "legalName": "Happy Path Cafe Pvt Ltd",
                  "municipalityId": 1,
                  "wardNumber": 1,
                  "toleName": "Downtown",
                  "addressField1": "Main Road",
                  "addressField2": "Near Landmark",
                  "postalCode": "44600",
                  "website": "https://happypath.example.com"
                }
                """.formatted(verticalId),
                userJwtToken,
                201
        );
        businessId = createBusinessResponse.path("businessId").asLong();
        assertTrue(businessId > 0);

        putNoContent(
                "/api/v1/business/%d/profile".formatted(businessId),
                """
                {
                  "businessSummary": "A complete E2E onboarding run",
                  "legalName": "Happy Path Cafe Pvt Ltd",
                  "addressField2": "Updated Landmark",
                  "contacts": {"phone": "+977-9800000000", "email": "owner@example.com"},
                  "businessHours": {"mon": "09:00-18:00"},
                  "latitude": 27.7172,
                  "longitude": 85.3240
                }
                """,
                userJwtToken
        );

        putNoContent(
                "/api/v1/business/%d/categories".formatted(businessId),
                "{\"categoryIds\":[%d]}".formatted(categoryId),
                userJwtToken
        );

        postNoBody(
                "/api/v1/business/%d/sections/%d".formatted(businessId, sectionId),
                """
                {
                  "sectionGroupId": null,
                  "fieldValues": {"primary_dish": "Mocha"}
                }
                """,
                userJwtToken,
                201
        );

        putNoContent(
                "/api/v1/business/%d/attributes".formatted(businessId),
                """
                {
                  "attributeId": %d,
                  "attributeValue": "DINE_IN"
                }
                """.formatted(attributeId),
                userJwtToken
        );

        postNoBody(
                "/api/v1/business/%d/documents".formatted(businessId),
                """
                {
                  "documentDefinitionId": %d,
                  "fileUrl": "https://example.com/docs/registration.pdf",
                  "fileName": "registration.pdf"
                }
                """.formatted(documentDefinitionId),
                userJwtToken,
                202
        );

        postNoBody(
                "/api/v1/business/%d/submit".formatted(businessId),
                "{\"message\":\"Please review my onboarding request\"}",
                userJwtToken,
                202
        );

        assertNotNull(sectionFieldId);
        assertNotNull(documentDefinitionId);
    }

    private static JsonNode postJson(String path, String body, String jwtToken, int expectedStatusCode)
            throws IOException, InterruptedException {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body));

        if (jwtToken != null && !jwtToken.isBlank()) {
            requestBuilder.header("Authorization", "Bearer " + jwtToken);
        }

        HttpResponse<String> response = httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
        assertEquals(expectedStatusCode, response.statusCode(), "Unexpected status for path " + path + ": " + response.body());

        return OBJECT_MAPPER.readTree(response.body());
    }

    private static void putNoContent(String path, String body, String jwtToken)
            throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + jwtToken)
                .PUT(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(204, response.statusCode(), "Unexpected status for path " + path + ": " + response.body());
    }

    private static void postNoBody(String path, String body, String jwtToken, int expectedStatusCode)
            throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + jwtToken)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(expectedStatusCode, response.statusCode(), "Unexpected status for path " + path + ": " + response.body());
    }
}
