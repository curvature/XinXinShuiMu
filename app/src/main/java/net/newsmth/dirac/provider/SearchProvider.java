package net.newsmth.dirac.provider;

import android.content.SearchRecentSuggestionsProvider;

/**
 * Created by cameoh on 4/24/16.
 */
public class SearchProvider extends SearchRecentSuggestionsProvider {

    public final static String AUTHORITY = SearchProvider.class.getCanonicalName();
    public final static int MODE = DATABASE_MODE_QUERIES;

    public SearchProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }


}
