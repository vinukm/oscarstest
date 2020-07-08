package de.cyberport.core.model;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

/**
 * Sling Model class for the Film Entry component
 * @author Vinay K M
 */
@Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL, resourceType = "test/filmEntry")
public class FilmEntry {

    @ValueMapValue
    private String title;

    @ValueMapValue
    private Integer year;

    @ValueMapValue
    private Integer awards;

    @ValueMapValue
    private Integer nominations;

    @ValueMapValue
    private Integer numberOfReferences;

    @ValueMapValue
    private Boolean isBestPicture;

    public String getTitle() {
        return title;
    }

    public Integer getYear() {
        return year;
    }

    public Integer getAwards() {
        return awards;
    }

    public Integer getNominations() {
        return nominations;
    }

    public Integer getNumberOfReferences() {
        return numberOfReferences;
    }

    public Boolean getBestPicture() {
        return isBestPicture;
    }
}
