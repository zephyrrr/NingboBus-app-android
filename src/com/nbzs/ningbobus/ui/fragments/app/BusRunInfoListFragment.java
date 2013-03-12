/*
 * This project constitutes a work of the United States Government and is
 * not subject to domestic copyright protection under 17 USC § 105.
 * 
 * However, because the project utilizes code licensed from contributors
 * and other third parties, it therefore is licensed under the MIT
 * License.  http://opensource.org/licenses/mit-license.php.  Under that
 * license, permission is granted free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the conditions that any appropriate copyright notices and this
 * permission notice are included in all copies or substantial portions
 * of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.nbzs.ningbobus.ui.fragments.app;

import com.google.gson.reflect.TypeToken;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import com.nbzs.ningbobus.R;
import com.nbzs.ningbobus.core.BusLineRunInfoItem;
import com.nbzs.ningbobus.services.FeedService;

import com.nbzs.ningbobus.ui.adapters.BusRunInfoItemsListAdapter;
import com.nbzs.ningbobus.ui.fragments.BaseListFragment;
import com.nbzs.ningbobus.ui.loaders.FeedReaderLoader;

import static com.nbzs.ningbobus.services.FeedService.EXTRA_CACHED;
import static com.nbzs.ningbobus.services.FeedService.EXTRA_SERVER_ERROR;

/**
 * A Fragment to display a list of text-based articles from a feed.
 */
public class BusRunInfoListFragment extends BaseListFragment
        implements LoaderManager.LoaderCallbacks<List<BusLineRunInfoItem>> {

    public static final String ARG_FEED_TITLE = "feed_title";

    public static final String ARG_FEED_TYPE = "feed_type";

    public static final String ARG_FEED_URL = "feed_url";

    public static final String ARG_ITEM_JSON = "item_json";

    public static final int ARTICLE_TYPE_FEED = 0;

    private static String TAG = "BusRunInfoListFragment";

    private ArticleFeedReceiver mArticleFeedReceiver;

    private BaseAdapter mAdapter;

    private List<BusLineRunInfoItem> mFeedItems;

    private String mFeedURL;

    private String mFeedTitle;

    private int mFeedType;

    private class ArticleFeedReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            /* Don't do anything if we can't get to our parent activity */
            if (isDetached() || isRemoving()) {
                return;
            }

            List<BusLineRunInfoItem> fetched = FeedService.loadFeedFromCache(getBaseActivity(), mFeedTitle);
            if (fetched != null) {
                mAdapter = new BusRunInfoItemsListAdapter(getBaseActivity());
                TypeToken<List<BusLineRunInfoItem>> typeToken = new TypeToken<List<BusLineRunInfoItem>>() {
                };

                mFeedItems = new ArrayList<BusLineRunInfoItem>();
                mFeedItems.addAll(fetched);
                ((BusRunInfoItemsListAdapter) mAdapter).fillWithItems(mFeedItems);
                mAdapter.notifyDataSetChanged();

                setListAdapter(mAdapter);
            }

            final boolean isCachedData = intent.getBooleanExtra(EXTRA_CACHED, false);
            if (!isCachedData) {
            }

            if (intent.getBooleanExtra(EXTRA_SERVER_ERROR, false)) {
                Toast.makeText(getBaseActivity(), "No network connection.", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);

        setRetainInstance(true);

        final Bundle args = getSherlockActivity().getIntent().getExtras();
        if (args != null) {
            mFeedType = args.getInt(ARG_FEED_TYPE, ARTICLE_TYPE_FEED);
            mFeedURL = args.getString(ARG_FEED_URL);
            mFeedTitle = args.getString(ARG_FEED_TITLE);
        } else {
            mFeedType = ARTICLE_TYPE_FEED;
        }

        if (mFeedTitle == null) {
            mFeedTitle = getString(R.string.favorites);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (mFeedTitle == null) {
            getSherlockActivity().setTitle("");
        } else {
            getSherlockActivity().setTitle(mFeedTitle.toUpperCase());
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        if (mFeedType == ARTICLE_TYPE_FEED) {
            getLoaderManager().restartLoader(mFeedType, null, this);
        } else {
            /*
             * Register the broadcast receiver with the parent activity
             */
            /*if (mArticleFeedReceiver == null) {
                mArticleFeedReceiver = new ArticleFeedReceiver();
            }

            IntentFilter refreshFilter = new IntentFilter(FeedService.REFRESH_FEED_UI_INTENT);
            getSherlockActivity().registerReceiver(mArticleFeedReceiver, refreshFilter);

            final Intent startService = new Intent(getSherlockActivity(), FeedService.class);
            startService.putExtra(FeedService.ARG_FEED_TITLE, mFeedTitle);
            startService.putExtra(FeedService.ARG_FEED_URL, mFeedURL);
            startService.setAction(FeedService.GET_FEED_DATA_INTENT);
            getSherlockActivity().startService(startService);*/
        }

        /*final int padding = (int) (applyDimension(COMPLEX_UNIT_DIP, 6.0f,
                getResources().getDisplayMetrics()) + 0.5f);
        getListView().setPadding(padding, padding, padding, padding);
        getListView().setDividerHeight(0);
        getListView().setSelector(android.R.color.transparent);

        // show the scroll indicator on the outside...
        getListView().setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);*/
    }

    @Override
    public void onPause() {
        super.onPause();

        try {
            if (mArticleFeedReceiver != null) {
                /* Unregister the receiver when we pause the fragment */
                getSherlockActivity().unregisterReceiver(mArticleFeedReceiver);
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
    }

    @Override
    public Loader<List<BusLineRunInfoItem>> onCreateLoader(int i, Bundle bundle) {
        {
            return new FeedReaderLoader(getSherlockActivity(), URI.create(mFeedURL), mFeedTitle);
        }
    }

    @Override
    public void onLoadFinished(Loader<List<BusLineRunInfoItem>> listLoader, List<BusLineRunInfoItem> feedItems) {
        mFeedItems = new ArrayList<BusLineRunInfoItem>();
        mFeedItems.addAll(feedItems);

        {
            mAdapter = new BusRunInfoItemsListAdapter(getBaseActivity());
            ((BusRunInfoItemsListAdapter) mAdapter).fillWithItems(mFeedItems);
        }
        mAdapter.notifyDataSetChanged();

        setListAdapter(mAdapter);

        setListShown(true);
    }

    @Override
    public void onLoaderReset(Loader<List<BusLineRunInfoItem>> listLoader) {
        mFeedItems.clear();
    }

}
