package org.wikimedia.wikipedia;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import org.json.JSONException;
import org.json.JSONObject;
import org.mediawiki.api.json.Api;

public class PageViewFragment extends Fragment {
    public static final String KEY_TITLE = "title";
    public static final String KEY_PAGE = "page";

    private PageTitle title;
    private WebView view;
    private Page page;

    private CommunicationBridge bridge;
    private LinkHandler linkHandler;

    public PageViewFragment(PageTitle title) {
        this.title = title;
    }

    public PageViewFragment() {
    }

    private void displayPage(Page page) {
        JSONObject leadSectionPayload = new JSONObject();
        try {
            leadSectionPayload.put("title", page.getTitle().getPrefixedText());
            leadSectionPayload.put("leadSectionHTML", page.getSections().get(0).toHTML());
        } catch (JSONException e) {
            // This should never happen
            throw new RuntimeException(e);
        }

        bridge.sendMessage("displayLeadSection", leadSectionPayload);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(KEY_TITLE, title);
        outState.putParcelable(KEY_PAGE, page);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
        view = (WebView) inflater.inflate(R.layout.fragment_page, container, false);

        bridge = new CommunicationBridge(view, "file:///android_asset/index.html");
        linkHandler = new LinkHandler(getActivity(), bridge);

        if (savedInstanceState != null && savedInstanceState.containsKey(KEY_TITLE)) {
            title = savedInstanceState.getParcelable(KEY_TITLE);
            if (savedInstanceState.containsKey(KEY_PAGE)) {
                page = savedInstanceState.getParcelable(KEY_PAGE);
            }
        }
        if (title == null) {
            throw new RuntimeException("No PageTitle passed in to constructor or in instanceState");
        }

        if (page == null) {
            Api api = ((WikipediaApp)getActivity().getApplicationContext()).getPrimarySiteAPI();
            new PageFetchTask(api, title) {
                @Override
                public void onFinish(Page result) {
                    page = result;
                    displayPage(result);
                }
            }.execute();
        } else {
            displayPage(page);
        }
        return view;
    }
}