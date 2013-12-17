package org.wikimedia.wikipedia.savedpages;

import android.content.Context;
import org.wikimedia.wikipedia.Page;
import org.wikimedia.wikipedia.WikipediaApp;
import org.wikimedia.wikipedia.concurrency.ExecutorService;
import org.wikimedia.wikipedia.concurrency.SaneAsyncTask;

import java.io.FileOutputStream;
import java.util.concurrent.Executor;

public class SavePageTask extends SaneAsyncTask<Void> {
    private final WikipediaApp app;
    private final Page page;

    public SavePageTask(Context context, Page page) {
        super(ExecutorService.getSingleton().getExecutor(SavePageTask.class, 1));
        app = (WikipediaApp) context.getApplicationContext();
        this.page = page;
    }

    @Override
    public Void performTask() throws Throwable {
        SavedPagePerister persister = (SavedPagePerister) app.getPersister(SavedPage.class);

        FileOutputStream out = app.openFileOutput("savedpage-" + page.getTitle().getHashedItentifier(), Context.MODE_PRIVATE);
        out.write(page.toJSON().toString().getBytes("utf-8"));
        out.close();

        SavedPage savedPage = new SavedPage(page.getTitle());

        persister.upsert(savedPage);
        return null;
    }
}