/*
 * Copyright (c) 2016, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.hisp.dhis.android.dashboard.adapters;

import android.content.Context;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.squareup.picasso.Picasso;

import org.hisp.dhis.android.dashboard.R;
import org.hisp.dhis.android.dashboard.utils.PicassoProvider;
import org.hisp.dhis.client.sdk.core.common.preferences.PreferencesModule;
import org.hisp.dhis.client.sdk.models.common.Access;
import org.hisp.dhis.client.sdk.models.dashboard.DashboardContent;
import org.hisp.dhis.client.sdk.models.dashboard.DashboardElement;
import org.hisp.dhis.client.sdk.models.dashboard.DashboardItem;
import org.hisp.dhis.client.sdk.ui.adapters.AbsAdapter;

import java.util.ArrayList;
import java.util.List;

import okhttp3.HttpUrl;

// TODO Replacing ButterKnife(if need be) and buildImage URL
public class DashboardItemAdapter extends AbsAdapter<DashboardItem, DashboardItemAdapter.ItemViewHolder> {
    private static final String DATE_FORMAT = "MMMM dd, YYYY";
    private static final String EMPTY_FIELD = "";

    /**
     * Basically we have three types of dashboard items:
     * 1) Item which can be represented as image: charts, event charts, maps.
     * 2) Item which contain tables: report table, event report.
     * 3) Item which contains links to content: resource, reports, users.
     */
    private static final int ITEM_WITH_IMAGE_TYPE = 0;
    private static final int ITEM_WITH_TABLE_TYPE = 1;
    private static final int ITEM_WITH_LIST_TYPE = 2;

    /**
     * As dashboard access rules apply to items, we need to have it here in order to
     * restrict user actions in correspondence to user permissions.
     */
    private final Access mDashboardAccess;

    /**
     * Callback which reacts to user actions on each dashboard item.
     */
    private final OnItemClickListener mClickListener;

    /**
     * This is the maximum number of columns which can fit in screen for given device.
     */
    private final int mMaxSpanCount;

    /**
     * Some resources cached in field variables.
     */
    private final String mUsersName;
    private final String mReportsName;
    private final String mResourcesName;

    /**
     * Image loading utility.
     */
    private final Picasso mImageLoader;

    /**
     * Server Url fetched from PreferenceModule
     */
    private final PreferencesModule mPreferencesModule;

    public DashboardItemAdapter(Context context, Access dashboardAccess,
                                int maxSpanCount, PreferencesModule preferencesModule, OnItemClickListener clickListener) {
        super(context, LayoutInflater.from(context));

        mDashboardAccess = dashboardAccess;
        mMaxSpanCount = maxSpanCount;
        mClickListener = clickListener;
        mPreferencesModule = preferencesModule;

        mUsersName = context.getString(R.string.users);
        mReportsName = context.getString(R.string.reports);
        mResourcesName = context.getString(R.string.resources);

        mImageLoader = new PicassoProvider().getInstance(context , mPreferencesModule);
    }

    private String buildImageUrl(String resource, String id) {
        HttpUrl url = HttpUrl.parse(mPreferencesModule.getConfigurationPreferences().get().getServerUrl());
        return url.newBuilder()
                .addPathSegment("api").addPathSegment(resource).addPathSegment(id).addPathSegment("data.png")
                .addQueryParameter("width", "480").addQueryParameter("height", "320").build()
                .toString();
    }

    /* returns type of row depending on item content type. */
    @Override
    public int getItemViewType(int position) {

        switch (getItem(position).getType()) {
            case DashboardContent.TYPE_CHART:
            case DashboardContent.TYPE_EVENT_CHART:
            case DashboardContent.TYPE_MAP:
                return ITEM_WITH_IMAGE_TYPE;
            case DashboardContent.TYPE_REPORT_TABLE:
            case DashboardContent.TYPE_EVENT_REPORT:
                return ITEM_WITH_TABLE_TYPE;
            case DashboardContent.TYPE_USERS:
            case DashboardContent.TYPE_REPORTS:
            case DashboardContent.TYPE_RESOURCES:
                return ITEM_WITH_LIST_TYPE;
        }

        throw new IllegalArgumentException();
    }

    public final int getSpanSize(int position) {
        if (getItemCount() > position) {
            DashboardItem dashboardItem = getItem(position);

            String itemShape = dashboardItem.getShape();
            if (itemShape == null) {
                itemShape = DashboardItem.SHAPE_NORMAL;
            }

            switch (itemShape) {
                case DashboardItem.SHAPE_NORMAL: {
                    return getSpanSizeNormal();
                }
                case DashboardItem.SHAPE_FULL_WIDTH: {
                    return getSpanSizeFull();
                }
                case DashboardItem.SHAPE_DOUBLE_WIDTH: {
                    return getSpanSizeDouble();
                }
            }
        }
        return 1;
    }

    private int getSpanSizeNormal() {
        return 1;
    }

    private int getSpanSizeDouble() {
        switch (mMaxSpanCount) {
            case 1:
                return 1;
            case 2:
                return 1;
            case 3:
                return 2;
            case 4:
                return 2;
            default:
                return 1;
        }
    }

    /////////////////////////////////////////////////////////////////////////
    // Generic item view handling logic.
    /////////////////////////////////////////////////////////////////////////

    private int getSpanSizeFull() {
        return mMaxSpanCount;
    }

    /**
     * Inflating all necessary views for particular viewType.
     * <p/>
     * {@inheritDoc}
     */
    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rootView = getLayoutInflater().inflate(
                R.layout.recycler_view_dashboard_item, parent, false);

        LinearLayout itemBody = (LinearLayout) rootView
                .findViewById(R.id.dashboard_item_body_container);
        TextView itemName = (TextView) rootView
                .findViewById(R.id.dashboard_item_name);
        TextView lastUpdated = (TextView) rootView
                .findViewById(R.id.dashboard_item_last_updated);
        ImageView itemMenuButton = (ImageView) rootView
                .findViewById(R.id.dashboard_item_menu);

        /* Here we are creating view holder depending on view type*/
        IElementContentViewHolder elementContentViewHolder
                = onCreateElementContentViewHolder(itemBody, viewType);
        /* attaching child view */
        itemBody.addView(elementContentViewHolder.getView());

        // Overflow menu button click listener
        MenuButtonHandler menuButtonHandler = new MenuButtonHandler(
                rootView.getContext(), mDashboardAccess, mClickListener);
        itemMenuButton.setOnClickListener(menuButtonHandler);

        return new ItemViewHolder(
                rootView, itemBody, itemName, lastUpdated,
                itemMenuButton, menuButtonHandler, elementContentViewHolder
        );
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        DashboardItem item = getItem(holder.getAdapterPosition());

        holder.menuButtonHandler.setDashboardItem(item);
        holder.lastUpdated.setText(item.getLastUpdated().toString(DATE_FORMAT));

        /* setting name extracted from content to TextView at top of item layout. */
        if (DashboardContent.TYPE_CHART.equals(item.getType()) && item.getChart() != null) {
            holder.itemName.setText(item.getChart().getDisplayName());
        } else if (DashboardContent.TYPE_MAP.equals(item.getType()) && item.getMap() != null) {
            holder.itemName.setText(item.getMap().getDisplayName());
        } else if (DashboardContent.TYPE_EVENT_CHART.equals(item.getType()) && item.getEventChart() != null) {
            holder.itemName.setText(item.getEventChart().getDisplayName());
        } else if (DashboardContent.TYPE_REPORT_TABLE.equals(item.getType()) && item.getReportTable() != null) {
            holder.itemName.setText(item.getReportTable().getDisplayName());
        } else if (DashboardContent.TYPE_EVENT_REPORT.equals(item.getType()) && item.getEventReport() != null) {
            holder.itemName.setText(item.getEventReport().getDisplayName());
        } else if (DashboardContent.TYPE_USERS.equals(item.getType())) {
            holder.itemName.setText(mUsersName);
        } else if (DashboardContent.TYPE_REPORTS.equals(item.getType())) {
            holder.itemName.setText(mReportsName);
        } else if (DashboardContent.TYPE_RESOURCES.equals(item.getType())) {
            holder.itemName.setText(mResourcesName);
        }

        /* handling visibility of 3-dot menu button */
        holder.itemMenuButton.setVisibility(
                holder.menuButtonHandler.isMenuVisible() ? View.VISIBLE : View.GONE);
        onBindElementContentViewHolder(holder.contentViewHolder,
                holder.getItemViewType(), holder.getAdapterPosition());
    }

    /**
     * Depending on viewType, this method will return correct IElementContentViewHolder.
     *
     * @param parent   Parent ViewGroup.
     * @param viewType Type of view we want to get IElementContentViewHolder for.
     * @return view holder.
     */
    private IElementContentViewHolder onCreateElementContentViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case ITEM_WITH_IMAGE_TYPE: {
                ImageView imageView = (ImageView) getLayoutInflater()
                        .inflate(R.layout.recycler_view_dashboard_item_imageview, parent, false);
                return new ImageItemViewHolder(imageView, mClickListener);
            }
            case ITEM_WITH_TABLE_TYPE: {
                TextView textView = (TextView) getLayoutInflater()
                        .inflate(R.layout.recycler_view_dashboard_item_textview, parent, false);
                return new TextItemViewHolder(textView, mClickListener);
            }
            case ITEM_WITH_LIST_TYPE: {
                LinearLayout textViewContainer = (LinearLayout) getLayoutInflater()
                        .inflate(R.layout.recycler_view_dashboard_item_list, parent, false);
                return new ListItemViewHolder(textViewContainer, mClickListener, mDashboardAccess);
            }
        }
        return null;
    }

    /* handling data */
    private void onBindElementContentViewHolder(IElementContentViewHolder holder, int viewType, int position) {
        switch (viewType) {
            case ITEM_WITH_IMAGE_TYPE: {
                handleItemsWithImages((ImageItemViewHolder) holder, getItem(position));
                break;
            }
            case ITEM_WITH_TABLE_TYPE: {
                handleItemsWithTables((TextItemViewHolder) holder, getItem(position));
                break;
            }
            case ITEM_WITH_LIST_TYPE: {
                handleItemsWithLists((ListItemViewHolder) holder, getItem(position));
                break;
            }
        }
    }

    /* builds the URL to image data and loads it by means of Picasso. */
    private void handleItemsWithImages(ImageItemViewHolder holder, DashboardItem item) {
        DashboardElement element = null;
        String request = null;
        if (DashboardContent.TYPE_CHART.equals(item.getType()) && item.getChart() != null) {
            element = item.getChart();
            request = buildImageUrl("charts", element.getUId());
        } else if (DashboardContent.TYPE_MAP.equals(item.getType()) && item.getMap() != null) {
            element = item.getMap();
            request = buildImageUrl("maps", element.getUId());
        } else if (DashboardContent.TYPE_EVENT_CHART.equals(item.getType()) && item.getEventChart() != null) {
            element = item.getEventChart();
            request = buildImageUrl("eventCharts", element.getUId());
        }

        holder.listener.setDashboardElement(element);
        mImageLoader.load(request)
                .placeholder(R.mipmap.ic_placeholder_image)
                .into(holder.imageView);
    }

    /////////////////////////////////////////////////////////////////////////
    // ITEM_WITH_IMAGE_TYPE view handling logic.
    /////////////////////////////////////////////////////////////////////////

    private void handleItemsWithTables(TextItemViewHolder holder, DashboardItem item) {
        DashboardElement element = null;
        if (DashboardContent.TYPE_REPORT_TABLE.equals(item.getType()) && item.getReportTable() != null) {
            element = item.getReportTable();
        } else if (DashboardContent.TYPE_EVENT_REPORT.equals(item.getType()) && item.getEventReport() != null) {
            element = item.getEventReport();
        }

        if (element != null) {
            holder.listener.setDashboardElement(element);
            holder.textView.setText(element.getDisplayName());
        }
    }

    private void handleItemsWithLists(ListItemViewHolder holder, DashboardItem item) {
        List<DashboardElement> elementList = null;
        if (DashboardContent.TYPE_USERS.equals(item.getType())) {
            elementList = item.getUsers();
        } else if (DashboardContent.TYPE_REPORTS.equals(item.getType())) {
            elementList = item.getReports();
        } else if (DashboardContent.TYPE_RESOURCES.equals(item.getType())) {
            elementList = item.getResources();
        }

        /*
        * each time RecyclerView binds data to a row, we need to handle recycling properly.
        * This also applies to view listeners, which will contain reference to data from previous row
        * (if not handled properly).That's why we need to set element list each time
        * handleItemsWithLists() is called.
        */
        holder.onListElementInternalClickListener.setElements(elementList);

        /* Handling embedded list items. */

        for(int i=0 ; i<holder.elementItems.size(); i++){
            DashboardElement element = getElement(elementList, i);
            holder.elementItems.get(i).setVisibility(element == null ? View.INVISIBLE : View.VISIBLE);
            holder.elementItems.get(i).setText(element == null ? EMPTY_FIELD : element.getDisplayName());
        }

        for(int i=0 ; i<holder.elementItemDeleteButtons.size(); i++){
            DashboardElement element = getElement(elementList, i);
            if (element == null || !mDashboardAccess.isUpdate()) {
                holder.elementItemDeleteButtons.get(i).setVisibility(View.INVISIBLE);
            } else {
                holder.elementItemDeleteButtons.get(i).setVisibility(View.VISIBLE);
            }
        }

        // TODO Remove below butterknife commented out code if above implementation is correct
        /**
        ButterKnife.apply(holder.elementItems,
                holder.ELEMENT_ITEMS_SETTER, elementList);
        ButterKnife.apply(holder.elementItemDeleteButtons,
                holder.ELEMENT_ITEM_BUTTONS_SETTER, elementList);
         **/
    }

    /* convenience method for removing dashboard items with animations */
    public void removeItem(DashboardItem item) {
        if (getData() != null) {
            int truePosition = getData().indexOf(item);
            if (!(truePosition < 0)) {
                getData().remove(truePosition);
                notifyItemRemoved(truePosition);
            }
        }
    }

    /////////////////////////////////////////////////////////////////////////
    // ITEM_WITH_TABLE_TYPE view handling logic.
    /////////////////////////////////////////////////////////////////////////

    interface IElementContentViewHolder {
        View getView();
    }

    public interface OnItemClickListener {
        void onContentClick(DashboardElement element);

        void onContentDeleteClick(DashboardElement element);

        void onItemDeleteClick(DashboardItem item);

        void onItemShareClick(DashboardItem item);
    }

    /////////////////////////////////////////////////////////////////////////
    // ITEM_WITH_LIST_TYPE view handling logic.
    /////////////////////////////////////////////////////////////////////////

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        final View itemBody;
        final TextView itemName;
        final TextView lastUpdated;
        final ImageView itemMenuButton;
        final MenuButtonHandler menuButtonHandler;
        final IElementContentViewHolder contentViewHolder;

        public ItemViewHolder(View itemView, View itemBody,
                              TextView itemName, TextView lastUpdated,
                              ImageView itemMenuButton, MenuButtonHandler menuButtonHandler,
                              IElementContentViewHolder elementContentViewHolder) {
            super(itemView);
            this.itemBody = itemBody;
            this.itemName = itemName;
            this.lastUpdated = lastUpdated;
            this.itemMenuButton = itemMenuButton;
            this.menuButtonHandler = menuButtonHandler;
            this.contentViewHolder = elementContentViewHolder;
        }
    }

    static class OnElementInternalClickListener implements View.OnClickListener {
        final OnItemClickListener mListener;
        DashboardElement mElement;

        OnElementInternalClickListener(OnItemClickListener listener) {
            this.mListener = listener;
        }

        public void setDashboardElement(DashboardElement element) {
            mElement = element;
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.dashboard_item_image: {
                    mListener.onContentClick(mElement);
                    break;
                }
                case R.id.dashboard_item_text: {
                    mListener.onContentClick(mElement);
                    break;
                }
            }
        }
    }

    /* View holder for ImageView */
    static class ImageItemViewHolder implements IElementContentViewHolder {
        final OnElementInternalClickListener listener;
        final ImageView imageView;

        public ImageItemViewHolder(ImageView view, OnItemClickListener outerListener) {
            imageView = view;

            listener = new OnElementInternalClickListener(outerListener);
            imageView.setOnClickListener(listener);
        }

        @Override
        public View getView() {
            return imageView;
        }
    }

    static class TextItemViewHolder implements IElementContentViewHolder {
        final OnElementInternalClickListener listener;
        final TextView textView;

        public TextItemViewHolder(TextView view, OnItemClickListener outerListener) {
            textView = view;

            listener = new OnElementInternalClickListener(outerListener);
            textView.setOnClickListener(this.listener);
        }

        @Override
        public View getView() {
            return textView;
        }
    }

    /* on content click listener (handles clicks both for items and delete buttons) */
    static class OnListElementInternalClickListener implements View.OnClickListener {
        private List<DashboardElement> mElements;
        private OnItemClickListener mListener;

        public OnListElementInternalClickListener(OnItemClickListener listener) {
            mListener = listener;
        }

        public void setElements(List<DashboardElement> elements) {
            mElements = elements;
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.element_item_0: {
                    onContentClick(0);
                    break;
                }
                case R.id.element_item_1: {
                    onContentClick(1);
                    break;
                }
                case R.id.element_item_2: {
                    onContentClick(2);
                    break;
                }
                case R.id.element_item_3: {
                    onContentClick(3);
                    break;
                }
                case R.id.element_item_4: {
                    onContentClick(4);
                    break;
                }
                case R.id.element_item_5: {
                    onContentClick(5);
                    break;
                }
                case R.id.element_item_6: {
                    onContentClick(6);
                    break;
                }
                case R.id.element_item_7: {
                    onContentClick(7);
                    break;
                }
                case R.id.element_item_0_delete_button: {
                    onContentDeleteClick(0);
                    break;
                }
                case R.id.element_item_1_delete_button: {
                    onContentDeleteClick(1);
                    break;
                }
                case R.id.element_item_2_delete_button: {
                    onContentDeleteClick(2);
                    break;
                }
                case R.id.element_item_3_delete_button: {
                    onContentDeleteClick(3);
                    break;
                }
                case R.id.element_item_4_delete_button: {
                    onContentDeleteClick(4);
                    break;
                }
                case R.id.element_item_5_delete_button: {
                    onContentDeleteClick(5);
                    break;
                }
                case R.id.element_item_6_delete_button: {
                    onContentDeleteClick(6);
                    break;
                }
                case R.id.element_item_7_delete_button: {
                    onContentDeleteClick(7);
                    break;
                }
            }
        }

        private void onContentDeleteClick(int position) {
            if (mElements != null && mElements.size() > position) {
                mListener.onContentDeleteClick(mElements.get(position));
            }
        }

        private void onContentClick(int position) {
            if (mElements != null && mElements.size() > position) {
                mListener.onContentClick(mElements.get(position));
            }
        }
    }

    static class ListItemViewHolder implements IElementContentViewHolder {

        final OnListElementInternalClickListener onListElementInternalClickListener;
        final View itemElementsContainer;

        List<TextView> elementItems = new ArrayList<>();
        List<View> elementItemDeleteButtons = new ArrayList<>();

        public ListItemViewHolder(View view, OnItemClickListener listener, Access dashboardAccess) {

            itemElementsContainer = view;
            onListElementInternalClickListener = new OnListElementInternalClickListener(listener);

            int[] elementItemsIds = {R.id.element_item_0, R.id.element_item_1, R.id.element_item_2,
                    R.id.element_item_3, R.id.element_item_4, R.id.element_item_5,
                    R.id.element_item_6, R.id.element_item_7};

            int[] elementItemDeleteButtonIds = {R.id.element_item_0_delete_button,
                    R.id.element_item_1_delete_button, R.id.element_item_2_delete_button,
                    R.id.element_item_3_delete_button, R.id.element_item_4_delete_button,
                    R.id.element_item_5_delete_button, R.id.element_item_6_delete_button,
                    R.id.element_item_7_delete_button};

            for (int i = 0; i < elementItemsIds.length; i++) {
                TextView item = (TextView)view.findViewById(elementItemsIds[i]);
                View deleteButton = view.findViewById(elementItemDeleteButtonIds[i]);
                elementItems.add(item);
                elementItemDeleteButtons.add(deleteButton);
                item.setOnClickListener(onListElementInternalClickListener);
                deleteButton.setOnClickListener(onListElementInternalClickListener);
            }

        }

        @Override
        public View getView() {
            return itemElementsContainer;
        }
    }

    static DashboardElement getElement(List<DashboardElement> elements, int position) {
        if (elements != null && elements.size() > position) {
            return elements.get(position);
        }

        return null;
    }

    private static class MenuButtonHandler implements View.OnClickListener {
        /* menu item ids */
        static final int MENU_GROUP_ID = 9382352;
        static final int MENU_SHARE_ITEM_ID = 8936352;
        static final int MENU_DELETE_ITEM_ID = 149232;
        static final int MENU_SHARE_ITEM_ORDER = 100;
        static final int MENU_DELETE_ITEM_ORDER = 110;

        final Context mContext;

        /* access which we will use in order to determine
        if user has access to particular actions */
        final Access mDashboardAccess;
        final OnItemClickListener mListener;

        /* dashboard item will change on each call to onBindViewHolder() in recycler view */
        DashboardItem mDashboardItem;

        public MenuButtonHandler(Context context, Access dashboardAccess,
                                 OnItemClickListener listener) {
            mContext = context;
            mDashboardAccess = dashboardAccess;
            mListener = listener;
        }

        public void setDashboardItem(DashboardItem dashboardItem) {
            mDashboardItem = dashboardItem;
        }

        /* helper method for client code, which allows to
        determine if we need to show 3-dot button*/
        public boolean isMenuVisible() {
            return isItemShareable() || isDashboardUpdatable();
        }

        /* helper method which returns true if we can show share menu item */
        private boolean isItemShareable() {
            return mDashboardItem != null && (
                    DashboardContent.TYPE_CHART.equals(mDashboardItem.getType()) ||
                            DashboardContent.TYPE_MAP.equals(mDashboardItem.getType()) ||
                            DashboardContent.TYPE_REPORT_TABLE.equals(mDashboardItem.getType())
            );
        }

        private boolean isDashboardUpdatable() {
            return mDashboardAccess.isUpdate();
        }

        /* here we will build popup menu and show it. */
        @Override
        public void onClick(View view) {
            PopupMenu popupMenu = new PopupMenu(mContext, view);

            if (isItemShareable()) {
                popupMenu.getMenu().add(MENU_GROUP_ID,
                        MENU_SHARE_ITEM_ID, MENU_SHARE_ITEM_ORDER,
                        R.string.share_interpretation);
            }

            if (isDashboardUpdatable()) {
                popupMenu.getMenu().add(MENU_GROUP_ID,
                        MENU_DELETE_ITEM_ID, MENU_DELETE_ITEM_ORDER,
                        R.string.delete);
            }

            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    if (mListener == null) {
                        return false;
                    }

                    if (menuItem.getItemId() == MENU_SHARE_ITEM_ID) {
                        mListener.onItemShareClick(mDashboardItem);
                    } else if (menuItem.getItemId() == MENU_DELETE_ITEM_ID) {
                        mListener.onItemDeleteClick(mDashboardItem);
                    }

                    return true;
                }
            });

            popupMenu.show();
        }
    }
}