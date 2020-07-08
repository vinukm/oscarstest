package de.cyberport.core.servlets;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import de.cyberport.core.model.FilmEntry;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.request.RequestParameter;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.servlets.annotations.SlingServletResourceTypes;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.propertytypes.ServiceDescription;

import javax.servlet.Servlet;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Servlet that writes information about the Oscar films in json format into the response.
 * It is mounted for all resources of a specific Sling resource type.
 *
 * Based on the request parameters, a filtering and sorting should be applied. Default sort order is by title.
 *
 * For cases when there is no supported request parameter provided in the request,
 * the servlet should return all the films below the requested container.
 *
 * The Servlet must support following request parameters:
 * 1. title - String. The exact film title
 * 2. year - Integer. The exact year when the film was nominated
 * 3. minYear - Integer. The minimum value of the year for the nominated film
 * 4. maxYear - Integer. The maximum value of the year for the nominated film
 * 5. minAwards - Integer. The minimum value for number of awards
 * 6. maxAwards - Integer. The maximum value for number of awards
 * 7. nominations - Integer. The exact number of nominations
 * 8. isBestPicture - Boolean. True to return only the winners of the best picture nomination.
 * 9. sortBy - Enumeration. Sorting in ascending order, supported values are: title, year, awards, nominations. Default value should be title.
 * 10. limit - Integer. Maximum amount of result entries in the response.
 *
 * Please note:
 * More then 1 filter must be supported.
 * The resulting JSON must not contain "jcr:primaryType" and "sling:resourceType" properties
 * When there will be no results based on the provided filter an empty array should be returned. Please refer to the 3rd example.
 *
 * Examples based on the data stored in oscars.json in resources directory.
 *
 * 1. Request parameters: year=2019&minAwards=4
 *
 * Sample response:
 * {
 *   "result": [
 *     {
 *       "title": "Parasite",
 *       "year": "2019",
 *       "awards": 4,
 *       "nominations": 6,
 *       "isBestPicture": true,
 *       "numberOfReferences": 8855
 *     }
 *   ]
 * }
 *
 * 2. Request parameters: minYear=2018&minAwards=3&sortBy=nominations&limit=4
 *
 * Sample response:
 * {
 *   "result": [
 *     {
 *       "title": "Bohemian Rhapsody",
 *       "year": "2018",
 *       "awards": 4,
 *       "nominations": 5,
 *       "isBestPicture": false,
 *       "numberOfReferences": 387
 *     },
 *     {
 *       "title": "Green Book",
 *       "year": "2018",
 *       "awards": 3,
 *       "nominations": 5,
 *       "isBestPicture": true,
 *       "numberOfReferences": 2945
 *     },
 *     {
 *       "title": "Parasite",
 *       "year": "2019",
 *       "awards": 4,
 *       "nominations": 6,
 *       "isBestPicture": true,
 *       "numberOfReferences": 8855
 *     },
 *     {
 *       "title": "Black Panther",
 *       "year": "2018",
 *       "awards": 3,
 *       "nominations": 7,
 *       "isBestPicture": false,
 *       "numberOfReferences": 770
 *     }
 *   ]
 * }
 *
 * 3. Request parameters: title=nonExisting
 *
 * Sample response:
 * {
 *   "result": []
 * }
 * @author Vitalii Afonin
 */
@Component(service = { Servlet.class }, immediate = true)
@SlingServletResourceTypes(
        resourceTypes="test/filmEntryContainer",
        methods=HttpConstants.METHOD_GET,
        extensions="json")
@ServiceDescription("Oscar Film Container Servlet")
public class OscarFilmContainerServlet extends SlingSafeMethodsServlet {

    private static final long serialVersionUID = 1L;

    enum SortBy {
        TITLE,
        YEAR,
        AWARDS,
        NOMINATIONS;
    }

    @Override
    protected void doGet(final SlingHttpServletRequest request, final SlingHttpServletResponse response) throws IOException {
        searchFilms(request, response);
    }

    private void searchFilms(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {

        List<RequestParameter> requestParameters = request.getRequestParameterList();
        String sortByProperty = request.getRequestParameter("sortBy")!=null ? request.getRequestParameter("sortBy").getString() : null;
        Integer limit = StringUtils.isNotBlank(request.getParameter("limit")) ? Integer.parseInt(request.getParameter("limit")) : -1;
        final Resource resource = request.getResource();
        List<FilmEntry> resultEntries = new ArrayList<>();

        filterResults(request, requestParameters, limit, resource, resultEntries);
        resultEntries = sortResults(sortByProperty, resultEntries);
        prepareResponse(response, resultEntries);
    }

    /**
     * Filter the results based on parameters
     * @param request
     * @param requestParameters
     * @param limit
     * @param resource
     * @param resultEntries
     */
    private void filterResults(SlingHttpServletRequest request, List<RequestParameter> requestParameters, Integer limit, Resource resource, List<FilmEntry> resultEntries) {
        for (Resource child : resource.getChildren()) {
            FilmEntry filmEntry = child.adaptTo(FilmEntry.class);
            ValueMap valueMap = child.getValueMap();

            Boolean continueParams = false;
            for(RequestParameter parameter : requestParameters) {
                String param = parameter.getName();
                switch (param) {
                    case "title":
                        continueParams = StringUtils.equals(valueMap.get(param).toString(), request.getParameter(param)) ? true : false;
                        break;
                    case "year":
                        continueParams = filmEntry.getYear() == Integer.parseInt(request.getParameter(param)) ? true : false;
                        break;
                    case "minYear" :
                        continueParams = filmEntry.getYear() >= Integer.parseInt(request.getParameter(param)) ? true : false;
                        break;
                    case "maxYear" :
                        continueParams = filmEntry.getYear() <= Integer.parseInt(request.getParameter(param)) ? true : false;
                        break;
                    case "minAwards" :
                        continueParams = filmEntry.getAwards() >= Integer.parseInt(request.getParameter(param)) ? true : false;
                        break;
                    case "maxAwards" :
                        continueParams = filmEntry.getAwards() <= Integer.parseInt(request.getParameter(param)) ? true : false;
                        break;
                    case "nominations" :
                        continueParams = filmEntry.getNominations() == Integer.parseInt(request.getParameter(param)) ? true : false;
                        break;
                    case "isBestPicture" :
                        continueParams = filmEntry.getBestPicture() == Boolean.valueOf(request.getParameter(param)) ? true : false;
                        break;
                    default:
                        continueParams = true; //Ignore other parameters
                        break;
                }
                if(!continueParams) {
                    break;
                }
            }

            if (continueParams && (limit == -1 || (limit >=0 && resultEntries.size() < limit))) {
                resultEntries.add(filmEntry);
            } else if ((limit >= 0 && resultEntries.size() == limit)) {
                break;
            }
        }
    }

    /**
     * Sort the filtered results
     * @param sortByProperty
     * @param resultEntries
     * @return
     */
    private List<FilmEntry> sortResults(String sortByProperty, List<FilmEntry> resultEntries) {
        if (StringUtils.isNotBlank(sortByProperty)) {
            resultEntries = sortFilmsBy(resultEntries, sortByProperty);
        } else if(resultEntries.size() > 1){
            resultEntries = defaultSort(resultEntries);
        }
        return resultEntries;
    }

    /**
     * Sorting is supported only for title, year, awards and nominations. Any other property based sorting is ignored.
     * @param films
     * @param sortBy
     * @return
     */
    private List<FilmEntry> sortFilmsBy(List<FilmEntry> films, String sortBy) {
        SortBy sort = SortBy.valueOf(sortBy.toUpperCase());
        switch (sort) {
            case TITLE:
                films = defaultSort(films);
                break;
            case YEAR:
                films = sortByYear(films);
                break;
            case AWARDS:
                films = sortByAwards(films);
                break;
            case NOMINATIONS:
                films = sortByNominations(films);
                break;
            default:
                films = defaultSort(films);
                break;
        }
        return films;
    }

    /**
     * Prepare the output response
     * @param response
     * @param resultEntries
     * @throws IOException
     */
    private void prepareResponse(SlingHttpServletResponse response, List<FilmEntry> resultEntries) throws IOException {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("result", gson.toJsonTree(resultEntries));

        //System.out.println(resultEntries.size());
        System.out.println(jsonObject.toString());
        response.setContentType("application/json");
        response.getWriter().write(jsonObject.toString());
    }

    private List<FilmEntry> defaultSort(List<FilmEntry> resultEntries) {
        resultEntries.sort(new Comparator<FilmEntry>() {
            @Override
            public int compare(FilmEntry f1, FilmEntry f2) {
                return f1.getTitle().compareTo(f2.getTitle());
            }
        });
        return resultEntries;
    }

    private List<FilmEntry> sortByYear(List<FilmEntry> resultEntries) {
        resultEntries.sort(new Comparator<FilmEntry>() {
            @Override
            public int compare(FilmEntry f1, FilmEntry f2) {
                return f1.getYear() - f2.getYear();
            }
        });
        return resultEntries;
    }

    private List<FilmEntry> sortByAwards(List<FilmEntry> resultEntries) {
        resultEntries.sort(new Comparator<FilmEntry>() {
            @Override
            public int compare(FilmEntry f1, FilmEntry f2) {
                return f1.getAwards() - f2.getAwards();
            }
        });
        return resultEntries;
    }

    private List<FilmEntry> sortByNominations(List<FilmEntry> resultEntries) {
        resultEntries.sort(new Comparator<FilmEntry>() {
            @Override
            public int compare(FilmEntry f1, FilmEntry f2) {
                return f1.getNominations() - f2.getNominations();
            }
        });
        return resultEntries;
    }

}
