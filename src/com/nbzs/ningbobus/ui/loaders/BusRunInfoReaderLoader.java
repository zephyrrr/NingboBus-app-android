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

package com.nbzs.ningbobus.ui.loaders;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.nbzs.ningbobus.core.BusLineRunInfoItem;
import com.nbzs.ningbobus.services.FeedService;
import com.nbzs.ningbobus.utils.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import com.nbzs.ningbobus.R;

public class BusRunInfoReaderLoader extends AsyncLoader<List<BusLineRunInfoItem>> {

    private static final String TAG = "BusRunInfoReaderLoader";

    private Activity mActivity;

    private URI mFeedUri;

    private String mFeedTitle;

    public BusRunInfoReaderLoader(Activity context, URI feedUri, String feedTitle) {
        super(context);
        mActivity = context;
        mFeedUri = feedUri;
        mFeedTitle = feedTitle;
    }

    private BusLineRunInfoItem downloadOneFeedItem(Integer busLine) {
        try {
            URI newUri = URI.create(mFeedUri.toString()+ "/" + Integer.toString(busLine) + "/?format=json");
            HttpURLConnection conn = (HttpURLConnection) newUri.toURL().openConnection();
            InputStream in;
            int status;

            conn.setDoInput(true);
            conn.setInstanceFollowRedirects(true);
            conn.setRequestProperty("User-Agent", getContext().getString(R.string.user_agent_string));

            in = conn.getInputStream();
            status = conn.getResponseCode();

            if (status < 400) {
                String result = IOUtils.readStream(in);
                BusLineRunInfoItem item = new BusLineRunInfoItem();
                //result = "{\"RunInfo\" : " + result + "}";
                JSONObject jo = new JSONObject(result);
                //item.setBusLineName(jo.getString("Name"));
                item.setBusLineCurrentInfo(jo.getString("RunInfo"));
                //result = URLDecoder.decode(result, "UTF-8");
                //item.setBusLineCurrentInfo(result);
                item.setBusLineName(FeedService.getBusLineFromId(getContext(), busLine));
                return item;
            }
            conn.disconnect();
        }
        catch (UnknownHostException e) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mActivity, "No network connection.", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (IOException e) {
            Log.d(TAG, "error reading feed");
            Log.d(TAG, Log.getStackTraceString(e));
        } catch (IllegalStateException e) {
            Log.d(TAG, "this should not happen");
            Log.d(TAG, Log.getStackTraceString(e));
        } catch (JSONException e) {
            Log.d(TAG, "this should not happen");
            Log.d(TAG, Log.getStackTraceString(e));
        }
        return null;
    }

    public List<BusLineRunInfoItem> loadInBackground() {
        Log.d(TAG, "loading URI: " + mFeedUri);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getContext());
        String selectedBusLines = prefs.getString("SelectedBusLines", "");
        String[] selected = selectedBusLines.split(",");
        List<Integer> busLines = new ArrayList<Integer>();
        for(int i=0; i<selected.length; ++i)
        {
            try
            {
                busLines.add(Integer.parseInt(selected[i]));
            }catch (NumberFormatException e)
            {
            }
        }

        Calendar cal = Calendar.getInstance();
        cal.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        Log.i(TAG, "start read feed at" + sdf.format(cal.getTime()));

        final List<BusLineRunInfoItem> items = Collections.synchronizedList(new ArrayList<BusLineRunInfoItem>());

        Thread[] threads = new Thread[busLines.size()];
        for (int i = 0; i < busLines.size(); ++i) {
            final int idx = busLines.get(i);
            threads[i] = new Thread(new Runnable(){
                public void run(){
                    BusLineRunInfoItem item = downloadOneFeedItem(idx);
                    synchronized(items) {
                        if (item != null){
                            items.add(item);
                        }
                    }
                }
             });
            threads[i].start();
        }
        try {
            for (Thread thread : threads) {
                thread.join();
            }
        }
        catch (InterruptedException e){
            Log.d(TAG, "this should not happen");
            Log.d(TAG, Log.getStackTraceString(e));
        }
        cal.getTime();
        Log.i(TAG, "end read feed at" + sdf.format(cal.getTime()));
        return items;
    }
}
