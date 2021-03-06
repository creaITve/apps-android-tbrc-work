package org.wikipedia.settings;

import android.content.Context;
import android.preference.DialogPreference;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import org.wikipedia.R;
import org.wikipedia.WikipediaApp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public class LanguagePreference extends DialogPreference {
    private ListView languagesList;
    private EditText languagesFilter;

    private final String[] languages;
    private final WikipediaApp app;

    public LanguagePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setPersistent(false);
        setDialogLayoutResource(R.layout.dialog_preference_languages);
        languages = context.getResources().getStringArray(R.array.preference_language_keys);
        app = (WikipediaApp) context.getApplicationContext();
        int langIndex = app.findWikiIndex(app.getPrimaryLanguage());
        setSummary(app.localNameFor(langIndex));
        setPositiveButtonText(null);
        setNegativeButtonText(null);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        languagesFilter = (EditText) view.findViewById(R.id.preference_languages_filter);
        languagesList = (ListView) view.findViewById(R.id.preference_languages_list);

        languagesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String lang = (String) languagesList.getAdapter().getItem(i);
                app.setPrimaryLanguage(lang);
                int langIndex = app.findWikiIndex(app.getPrimaryLanguage());
                setSummary(app.localNameFor(langIndex));
                LanguagePreference.this.getDialog().dismiss();
            }
        });

        languagesList.setAdapter(new LanguagesAdapter(languages, app));

        int selectedLangIndex = Arrays.asList(languages).indexOf(app.getPrimaryLanguage());
        languagesList.setItemChecked(selectedLangIndex, true);
        languagesList.setSelection(selectedLangIndex - 1);

        languagesFilter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                ((LanguagesAdapter) languagesList.getAdapter()).setFilterText(s.toString());
            }
        });

    }

    public String getCurrentLanguageDisplayString() {
        Locale locale = new Locale(app.getPrimaryLanguage());
        return locale.getDisplayLanguage(locale);
    }

    private static final class LanguagesAdapter extends BaseAdapter {
        private final String[] originalLanguages;
        private final ArrayList<String> languages;
        private final WikipediaApp app;

        private LanguagesAdapter(String[] languages, WikipediaApp app) {
            this.originalLanguages = languages;
            this.languages = new ArrayList(Arrays.asList(languages));
            this.app = app;
        }

        public void setFilterText(String filter) {
            this.languages.clear();
            filter = filter.toLowerCase();
            for (String s: originalLanguages) {
                int langIndex = app.findWikiIndex(s);
                String canonicalLang = app.canonicalNameFor(langIndex);
                String localLang = app.localNameFor(langIndex);
                if (s.contains(filter)
                        || canonicalLang.toLowerCase().contains(filter)
                        || localLang.toLowerCase().contains(filter)) {
                    this.languages.add(s);
                }
            }
            notifyDataSetInvalidated();
        }

        @Override
        public int getCount() {
            return languages.size();
        }

        @Override
        public Object getItem(int position) {
            return languages.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.simple_list_item_activated_2, parent, false);
            }

            TextView nameText = (TextView) convertView.findViewById(android.R.id.text1);
            TextView localNameText = (TextView) convertView.findViewById(android.R.id.text2);

            String wikiCode = (String) getItem(position);

            int langIndex = app.findWikiIndex(wikiCode);

            nameText.setText(app.canonicalNameFor(langIndex));
            localNameText.setText(app.localNameFor(langIndex));
            return convertView;
        }
    }
}
