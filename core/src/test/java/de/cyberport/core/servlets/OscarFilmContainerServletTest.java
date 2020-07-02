package de.cyberport.core.servlets;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import java.util.HashMap;
import java.util.Map;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletRequest;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * @author Vitalii Afonin
 */
@ExtendWith(AemContextExtension.class)
class OscarFilmContainerServletTest {

    private OscarFilmContainerServlet underTest = new OscarFilmContainerServlet();

    @Test
    void sampleTest(AemContext context) {
        MockSlingHttpServletRequest request = context.request();
        MockSlingHttpServletResponse response = context.response();

        context.load().json("/oscars.json", "/content/oscars");
        context.currentResource("/content/oscars");
        request.setResource(context.currentResource());

        final Map<String, Object> params = new HashMap<>();
        params.put("minAwards", "4");
        params.put("year", "2019");

        request.setParameterMap(params);

        underTest.doGet(request, response);

//        assertThat(response.getContentType(), containsString("application/json"));
//        assertThat(response.getOutputAsString(), containsString("Parasite"));
    }

    //TODO add tests to verify your implementation


}
