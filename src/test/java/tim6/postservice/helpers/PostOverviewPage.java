package tim6.postservice.helpers;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import tim6.postservice.adapter.http.dto.PostOverviewDTO;

public class PostOverviewPage extends PageImpl<PostOverviewDTO> {
    private static final long serialVersionUID = 2569345110865929975L;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public PostOverviewPage(
            @JsonProperty("content") final List<PostOverviewDTO> content,
            @JsonProperty("number") final int number,
            @JsonProperty("size") final int size,
            @JsonProperty("totalElements") final Long totalElements,
            @JsonProperty("pageable") final JsonNode pageable,
            @JsonProperty("last") final boolean last,
            @JsonProperty("totalPages") final int totalPages,
            @JsonProperty("sort") final JsonNode sort,
            @JsonProperty("first") final boolean first,
            @JsonProperty("numberOfElements") final int numberOfElements) {

        super(content, PageRequest.of(number, size), totalElements);
    }

    public PostOverviewPage(
            final SearchHits<PostOverviewDTO> content, final Pageable pageable, final long total) {
        super(
                content.getSearchHits().stream()
                        .map(SearchHit::getContent)
                        .collect(Collectors.toList()),
                pageable,
                total);
    }

    public PostOverviewPage(final SearchHits<PostOverviewDTO> content) {
        super(
                content.getSearchHits().stream()
                        .map(SearchHit::getContent)
                        .collect(Collectors.toList()));
    }

    public PostOverviewPage() {
        super(new ArrayList<>());
    }
}
