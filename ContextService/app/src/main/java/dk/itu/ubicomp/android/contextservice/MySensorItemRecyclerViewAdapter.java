package dk.itu.ubicomp.android.contextservice;

import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import dk.itu.ubicomp.android.contextservice.Data.SensorData;
import dk.itu.ubicomp.android.contextservice.SensorItemFragment.OnListFragmentInteractionListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link SensorData} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 */
public class MySensorItemRecyclerViewAdapter extends RecyclerView.Adapter<MySensorItemRecyclerViewAdapter.ViewHolder> {

    private final List<SensorData> mValues;
    private final OnListFragmentInteractionListener mListener;

    public MySensorItemRecyclerViewAdapter(List<SensorData> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;

        new RetrieveList().execute("http://" + ContextService.AUTHORITY + "/getdata.php?getdata=2");
    }

    public void UpdateList(List<SensorData> list)
    {
        if (list != null)
        {
            Log.e("ADDED FROM JSON", Integer.toString(list.size()));
            mValues.clear();
            mValues.addAll(list);
            this.notifyDataSetChanged();
            Log.e("NOTIFY CHANGE", Integer.toString(mValues.size()));
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_sensoritem, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mTypeView.setText(mValues.get(position).sensortype);
        holder.mValueView.setText(mValues.get(position).value);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mTypeView;
        public final TextView mTimestampView;
        public final TextView mValueView;
        public SensorData mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mTypeView = (TextView) view.findViewById(R.id.sensor_type);
            mTimestampView = (TextView) view.findViewById(R.id.timestamp);
            mValueView = (TextView) view.findViewById(R.id.value);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mValueView.getText() + "'";
        }
    }

    public class RetrieveList extends AsyncTask<String, String, List<SensorData>> {

        private boolean RequestOK = false;
        private List<SensorData> list = null;

        @Override
        protected List<SensorData> doInBackground(String... params) {
            try {
                // http client
                URL url = new URL(params[0]);
                HttpURLConnection httpClient = (HttpURLConnection) url.openConnection();
                httpClient.setRequestMethod("GET");
                httpClient.setUseCaches(false);
                httpClient.setDoInput(true);
                httpClient.setDoOutput(true);
                httpClient.setRequestProperty("Connection", "Keep-Alive");

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(httpClient.getInputStream()));
                StringBuffer response = new StringBuffer();

                if (httpClient.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    String line;
                    StringBuilder result = new StringBuilder();
                    while ((line = in.readLine()) != null) {
                        Log.e("HTTP REQUEST", line);
                        result.append(line);
                    }
                    try {
                        JSONObject jsnobject = new JSONObject(result.toString());
                        JSONArray jsonArray = jsnobject.getJSONArray("myarray");

                        List<SensorData> sensorDataList = new ArrayList<SensorData>();

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject explrObject = jsonArray.getJSONObject(i);
                            sensorDataList.add(new SensorData(
                                    explrObject.get("Sid").toString(),
                                    explrObject.get("sensortype").toString(),
                                    explrObject.get("value").toString(),
                                    explrObject.get("timestamp").toString()));

                            Log.e("PRINTING JSON", explrObject.toString());
                        }
                        Log.e("ADDED FROM JSON", Integer.toString(sensorDataList.size()));
                        RequestOK = true;
                        return sensorDataList;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(List<SensorData> result) {
            // execution of result of Long time consuming operation
            Log.e("POSTEXECUTE", Integer.toString(result.size()));
            if (RequestOK)
            {
                if (result != null)
                {
                    Log.e("POSTEXECUTE", Integer.toString(result.size()));
                    MySensorItemRecyclerViewAdapter.this.UpdateList(result);
                }
            }
        }
    }
}
