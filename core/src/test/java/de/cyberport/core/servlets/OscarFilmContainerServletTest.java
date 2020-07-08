package de.cyberport.core.servlets;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletRequest;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletResponse;
import org.junit.Before;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * @author Vitalii Afonin
 */
@RunWith(PowerMockRunner.class)
@ExtendWith({ AemContextExtension.class})
class OscarFilmContainerServletTest {

    private OscarFilmContainerServlet underTest = new OscarFilmContainerServlet();

    @Before
    public void setUp() {
//        underTest = new OscarFilmContainerServlet();
//        request = context.request();
//        response = context.response();
//        context.load().json("/oscars.json", "/content/oscars");
//        context.currentResource("/content/oscars");
//        context.addModelsForPackage("de.cyberport.core.model");
//        request.setResource(context.currentResource());
    }

    @Test
    void limitTest(AemContext context) throws IOException {

        MockSlingHttpServletRequest request = context.request();
        MockSlingHttpServletResponse response = context.response();

        context.addModelsForPackage("de.cyberport.core.model");
        context.load().json("/oscars.json", "/content/oscars");
        context.currentResource("/content/oscars");

        request.setResource(context.currentResource());

        final Map<String, Object> params = new HashMap<>();
        params.put("minAwards", "4");
        params.put("minYear", "2000");
        params.put("sortBy", "year");
        params.put("limit", 12);
        request.setParameterMap(params);

        underTest.doGet(request, response);

        assertThat(response.getOutputAsString(), containsString("\"title\""));
        StringUtils.countMatches(response.getOutputAsString(), "\"title\"");
        assertThat(12, is(StringUtils.countMatches(response.getOutputAsString(), "\"title\"")));
    }

    @Test
    void noexistingTitleTest(AemContext context) throws IOException {

        MockSlingHttpServletRequest request = context.request();
        MockSlingHttpServletResponse response = context.response();

        context.addModelsForPackage("de.cyberport.core.model");
        context.load().json("/oscars.json", "/content/oscars");
        context.currentResource("/content/oscars");

        request.setResource(context.currentResource());

        final Map<String, Object> params = new HashMap<>();
        params.put("title", "nonExisting");

        request.setParameterMap(params);

        underTest.doGet(request, response);
        assertThat(response.getContentType(), containsString("application/json"));
        assertThat(response.getOutputAsString(), containsString("{\"result\":[]}"));

    }

    @Test
    void singleParameterTest(AemContext context) throws IOException {

        MockSlingHttpServletRequest request = context.request();
        MockSlingHttpServletResponse response = context.response();

        context.addModelsForPackage("de.cyberport.core.model");
        context.load().json("/oscars.json", "/content/oscars");
        context.currentResource("/content/oscars");
        request.setResource(context.currentResource());

        final Map<String, Object> params = new HashMap<>();
        params.put("year", "2019");
        request.setParameterMap(params);
        underTest.doGet(request, response);
        assertThat(response.getOutputAsString(), containsString("\"title\""));

    }

    @Test
    void maxTest(AemContext context) throws IOException {

        MockSlingHttpServletRequest request = context.request();
        MockSlingHttpServletResponse response = context.response();

        context.addModelsForPackage("de.cyberport.core.model");
        context.load().json("/oscars.json", "/content/oscars");
        context.currentResource("/content/oscars");

        request.setResource(context.currentResource());

        final Map<String, Object> params = new HashMap<>();
        params.put("maxAwards", "1");
        params.put("maxYear", "2000");
        params.put("sortBy", "awards");

        request.setParameterMap(params);

        underTest.doGet(request, response);

        assertThat(response.getOutputAsString(), containsString("\"title\""));

    }

    @Test
    void bestPictureTest(AemContext context) throws IOException {

        MockSlingHttpServletRequest request = context.request();
        MockSlingHttpServletResponse response = context.response();

        context.addModelsForPackage("de.cyberport.core.model");
        context.load().json("/oscars.json", "/content/oscars");
        context.currentResource("/content/oscars");

        request.setResource(context.currentResource());

        final Map<String, Object> params = new HashMap<>();
        params.put("isBestPicture", false);
        params.put("title", "Zorba the Greek");

        request.setParameterMap(params);

        underTest.doGet(request, response);

        assertThat(1, is(StringUtils.countMatches(response.getOutputAsString(), "\"title\"")));

    }

    @Test
    void nominationsTest(AemContext context) throws IOException {

        MockSlingHttpServletRequest request = context.request();
        MockSlingHttpServletResponse response = context.response();

        context.addModelsForPackage("de.cyberport.core.model");
        context.load().json("/oscars.json", "/content/oscars");
        context.currentResource("/content/oscars");

        request.setResource(context.currentResource());

        final Map<String, Object> params = new HashMap<>();
        params.put("nominations", "7");
        params.put("sortBy", "nominations");

        request.setParameterMap(params);

        underTest.doGet(request, response);

    }

}
