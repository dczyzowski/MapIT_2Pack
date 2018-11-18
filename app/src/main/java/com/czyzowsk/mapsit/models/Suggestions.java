
package com.czyzowsk.mapsit.models;

import java.util.ArrayList;
import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Suggestions {

    @SerializedName("suggestions")
    @Expose
    private List<Suggestion> suggestions = new ArrayList<Suggestion>();

    public List<Suggestion> getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(List<Suggestion> suggestions) {
        this.suggestions = suggestions;
    }

}
