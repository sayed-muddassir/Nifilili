// FILE: com.nifilili.search.dto.response.AutoSuggestResponse
package com.nifilili.search.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

/**
 * Response wrapper for the auto-suggest endpoint.
 * <p>
 * Contains an ordered list of suggestion items, with business suggestions
 * appearing first, followed by category and location suggestions.
 * </p>
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Auto-suggest response containing ranked suggestions")
public class AutoSuggestResponse {

    /** Ordered list of suggestion items. Business suggestions appear first. */
    @Schema(description = "Ordered list of suggestions")
    private List<SuggestionItem> suggestions;
}
