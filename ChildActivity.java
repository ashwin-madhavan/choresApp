package com.example.choresappv2;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class ChildActivity extends AppCompatActivity {
    private RequestQueue rQueue;
    ArrayList<String> itemList3;
    ArrayList<String> itemNameList;
    ArrayList<String> itemIdList;

    ArrayList<ChoreItem> choreItemList;

    ArrayAdapter<String> adapter3;

    Button completeButton;

    ListView lv3;
    TextView tvPoints;

    public static ArrayList<ChoreItem> sortArray(ArrayList<ChoreItem> toBeSortedList) {
        ArrayList<ChoreItem> sortedChoreList = new ArrayList<>();
        while (toBeSortedList.size() > 0) {
            ChoreItem holder = toBeSortedList.get(0);
            int holderPoints = toBeSortedList.get(0).getPoints();


            for (int i = 0; i < toBeSortedList.size(); i++) {
                if (toBeSortedList.get(i).getPoints() > holderPoints) {
                    holder = toBeSortedList.get(i);
                    holderPoints = toBeSortedList.get(i).getPoints();
                }
            }

            sortedChoreList.add(holder);
            toBeSortedList.remove(toBeSortedList.indexOf(holder));
        }


        return sortedChoreList;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child);

        rQueue = Volley.newRequestQueue(this);
        lv3 = (ListView) findViewById(R.id.listView3);
        completeButton = findViewById(R.id.completeButton);
        tvPoints = findViewById(R.id.textViewPoints);

        itemList3 = new ArrayList<>();
        itemNameList = new ArrayList<>();
        itemIdList = new ArrayList<>();

        choreItemList = new ArrayList<>();

        adapter3 = new ArrayAdapter<String>(ChildActivity.this,
                android.R.layout.simple_list_item_multiple_choice, itemList3);

        String URL = "http://10.0.2.2:5000/api/TodoItems";

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, URL, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        response.length();
                        try {


                            for (int i = 0; i < response.length(); i++) {
                                response.getJSONObject(i);
                                JSONObject todoItem = response.getJSONObject(i);

                                int id = todoItem.getInt("id");
                                String name = todoItem.getString("name");
                                int points = todoItem.getInt("points");
//                                Boolean isComplete = todoItem.getBoolean("isComplete");

                                choreItemList.add(new ChoreItem(id, name, points));
                            }

                            ArrayList<ChoreItem> sortedArrayList = sortArray(choreItemList);
                            for (int i = 0; i < sortedArrayList.size(); i++) {
                                itemList3.add(sortedArrayList.get(i).getName() + "[" +
                                        sortedArrayList.get(i).getPoints() + "]");
                                itemIdList.add(String.valueOf(sortedArrayList.get(i).getId()));
                                adapter3.notifyDataSetChanged();
                            }
                            choreItemList = sortedArrayList;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        rQueue.add(request);

        completeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SparseBooleanArray positionChecker = lv3.getCheckedItemPositions();
                int count = lv3.getCount();

                for (int item = count - 1; item >= 0; item--) {
                    if (positionChecker.get(item)) {
//                        int intID = Integer.parseInt(itemIdList.get(item));
                        int intID = choreItemList.get(item).getId();
                        String name = choreItemList.get(item).getName();
                        int points = choreItemList.get(item).getPoints();
                        itemIdList.remove(item);

                        points += Integer.parseInt(tvPoints.getText().toString());
                        tvPoints.setText(String.valueOf(points));


                        try {
                            RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
                            String URL = "http://10.0.2.2:5000/api/TodoItems/" + intID;
                            JSONObject jsonBody = new JSONObject();
                            jsonBody.put("id", intID);
                            jsonBody.put("name", name);
                            jsonBody.put("points", points);
                            jsonBody.put("isComplete", true);
                            final String requestBody = jsonBody.toString();

                            StringRequest stringRequest = new StringRequest(Request.Method.PUT,
                                    URL, new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    Log.i("VOLLEY", response);
                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Log.e("VOLLEY", error.toString());
                                }
                            }) {
                                @Override
                                public String getBodyContentType() {
                                    return "application/json; charset=utf-8";
                                }

                                @Override
                                public byte[] getBody() throws AuthFailureError {
                                    try {
                                        //if body is null then return null else return bytes of requestBody via 8-bit
                                        return requestBody == null ? null : requestBody.getBytes("utf-8");
                                    } catch (UnsupportedEncodingException uee) {
                                        VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBody, "utf-8");
                                        return null;
                                    }
                                }
                            };

                            requestQueue.add(stringRequest);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        adapter3.remove(itemList3.get(item));
                    }
                }

                positionChecker.clear();
                adapter3.notifyDataSetChanged();

            }
        });

//        completeButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                SparseBooleanArray positionChecker = lv3.getCheckedItemPositions();
//                int count = lv3.getCount();
//
//                for (int item = count - 1; item >= 0; item--) {
//                    if (positionChecker.get(item)) {
//                        int intID = Integer.parseInt(itemIdList.get(item));
//                        int bean = choreItemList.get(item).getPoints();
//                        itemIdList.remove(item);
//
//                        bean += Integer.parseInt(tvPoints.getText().toString());
//                        tvPoints.setText(String.valueOf(bean));
//
//
//                        try {
//                            RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
//                            String URL = "http://10.0.2.2:5000/api/TodoItems/" + intID;
//                            JSONObject jsonBody = new JSONObject();
//                            jsonBody.put("id", intID);
//                            final String requestBody = jsonBody.toString();
//
//                            StringRequest stringRequest = new StringRequest(Request.Method.DELETE,
//                                    URL, new Response.Listener<String>() {
//                                @Override
//                                public void onResponse(String response) {
//                                    Log.i("VOLLEY", response);
//                                }
//                            }, new Response.ErrorListener() {
//                                @Override
//                                public void onErrorResponse(VolleyError error) {
//                                    Log.e("VOLLEY", error.toString());
//                                }
//                            }) {
//                                @Override
//                                public String getBodyContentType() {
//                                    return "application/json; charset=utf-8";
//                                }
//
//                                @Override
//                                public byte[] getBody() throws AuthFailureError {
//                                    try {
//                                        //if body is null then return null else return bytes of requestBody via 8-bit
//                                        return requestBody == null ? null :
//                                                requestBody.getBytes("utf-8");
//                                    } catch (UnsupportedEncodingException uee) {
//                                        VolleyLog.wtf("Unsupported Encoding while trying to get" +
//                                                " the bytes of %s using %s", requestBody, "utf-8");
//                                        return null;
//                                    }
//                                }
//                            };
//
//                            requestQueue.add(stringRequest);
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//
//
//                        //////////////////////////////////////
//                        adapter3.remove(itemList3.get(item));
//                    }
//                }
//
//                positionChecker.clear();
//                adapter3.notifyDataSetChanged();
//
//                ///////////////
//            }
//        });

        lv3.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                SparseBooleanArray positionChecker = lv3.getCheckedItemPositions();

                int count = lv3.getCount();

                for (int item = count - 1; item >= 0; item--) {
                    if (positionChecker.get(item)) {
                        adapter3.remove(itemList3.get(item));

                        Toast.makeText(ChildActivity.this,
                                "Chore Deleted Successfully", Toast.LENGTH_SHORT).show();
                    }
                }

                positionChecker.clear();
                adapter3.notifyDataSetChanged();

                return false;
            }
        });

        lv3.setAdapter(adapter3);
    }
}