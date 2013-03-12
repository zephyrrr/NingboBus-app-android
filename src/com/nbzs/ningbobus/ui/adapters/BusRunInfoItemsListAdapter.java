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

package com.nbzs.ningbobus.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;

import com.nbzs.ningbobus.R;
import com.nbzs.ningbobus.core.BusLineRunInfoItem;
import com.nbzs.ningbobus.ui.activities.BaseActivity;

public class BusRunInfoItemsListAdapter extends BaseAdapter {

    private static class ViewHolder {

        TextView title;

        TextView content;
    }

    private BaseActivity mContext;

    private ArrayList<BusLineRunInfoItem> mItems;

    public BusRunInfoItemsListAdapter(final BaseActivity context) {
        super();

        mContext = context;
        mItems = new ArrayList<BusLineRunInfoItem>();
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public BusLineRunInfoItem getItem(int i) {
        return mItems.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final BusLineRunInfoItem item = getItem(position);
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.feedrow, null);
            holder = new ViewHolder();
            holder.title = (TextView) convertView.findViewById(R.id.feed_title);
            holder.content = (TextView) convertView.findViewById(R.id.feed_content);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.title.setText(item.getBusLineName());
        holder.content.setText(item.getBusLineCurrentInfo());

        return convertView;
    }

    /**
     * Replaces the existing items in the adapter with a given collection
     */
    public void fillWithItems(Collection<BusLineRunInfoItem> items) {
        mItems.clear();
        mItems.addAll(items);
    }

    /**
     * Appends a given list of FeedItems to the adapter
     */
    public void appendWithItems(Collection<BusLineRunInfoItem> items) {
        mItems.addAll(items);
    }

    public void clear() {
        mItems.clear();
    }
}