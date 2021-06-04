package com.example.choresappv2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
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

public class ParentActivity extends AppCompatActivity {
    RequestQueue rq;
    private RequestQueue requestQueue;

    ArrayList<String> itemList;
    ArrayList<String> itemNameList;
    ArrayList<String> itemIdList;

    ArrayList<ChoreItem> choreItemList;

    ArrayAdapter<String> adapter;

    EditText itemText;
    EditText pointsText;

    Button addButton;
    Button deleteButton;

    ListView lv;

    public static ArrayList<ChoreItem> sortArray(ArrayList<ChoreItem> choreItemList) {
        ArrayList<ChoreItem> sortedChoreList = new ArrayList<>();
        while (choreItemList.size() > 0) {
            ChoreItem holder = choreItemList.get(0);
            int holderPoints = choreItemList.get(0).getPoints();


            for (int i = 0; i < choreItemList.size(); i++) {
                if (choreItemList.get(i).getPoints() > holderPoints) {
                    holder = choreItemList.get(i);
                    holderPoints = choreItemList.get(i).getPoints();
                }
            }

            sortedChoreList.add(holder);
            choreItemList.remove(choreItemList.indexOf(holder));
        }


        return sortedChoreList;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent);
        rq = Volley.newRequestQueue(this);

        lv = (ListView) findViewById(R.id.listView2);
        itemText = findViewById(R.id.addChore);
        pointsText = findViewById(R.id.addPoints);
        addButton = findViewById(R.id.addButton);
        deleteButton = findViewById(R.id.deleteButton);

        itemList = new ArrayList<>();
        itemNameList = new ArrayList<>();
        itemIdList = new ArrayList<>();

        choreItemList = new ArrayList<>();

        adapter = new ArrayAdapter<String>(ParentActivity.this,
                android.R.layout.simple_list_item_multiple_choice, itemList);

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
                                Boolean isComplete = todoItem.getBoolean("isComplete");

                                if (!isComplete) {
                                    choreItemList.add(new ChoreItem(id, name, points));
                                }

                            }
                            ArrayList<ChoreItem> sortedArrayList = sortArray(choreItemList);
                            for (int i = 0; i < sortedArrayList.size(); i++) {
                                itemList.add(sortedArrayList.get(i).getName() + "[" +
                                        sortedArrayList.get(i).getPoints() + "]");
                                itemIdList.add(String.valueOf(sortedArrayList.get(i).getId()));
                                adapter.notifyDataSetChanged();
                            }
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
        rq.add(request);


        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemList.add(itemText.getText().toString() + "[" + pointsText.getText().toString() + "]");
                adapter.notifyDataSetChanged();

                try {
                    RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
//                    String URL = "https://familychoreswebapp.azurewebsites.net/api/TodoItems";
                    String URL = "http://10.0.2.2:5000/api/TodoItems";

                    JSONObject jsonBody = new JSONObject();
//                    jsonBody.put("id", Integer.parseInt("3"));
                    jsonBody.put("name", itemText.getText().toString());
                    jsonBody.put("points", Integer.parseInt(pointsText.getText().toString()));
                    jsonBody.put("isComplete", false);
                    final String requestBody = jsonBody.toString();

                    itemText.setText("");
                    pointsText.setText("");

                    StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
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

//                        @Override
//                        protected Response<String> parseNetworkResponse(NetworkResponse response) {
//                            String responseString = "";
//                            if (response != null) {
//                                responseString = String.valueOf(response.statusCode);
//                                // can get more details such as response.headers
//                            }
//                            return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
//                        }
                    };

                    requestQueue.add(stringRequest);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                /////////////////////

                Intent i = new Intent(ParentActivity.this, ParentActivity.class);
                startActivity(i);

            }

        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SparseBooleanArray positionChecker = lv.getCheckedItemPositions();
                int count = lv.getCount();

                for (int item = count - 1; item >= 0; item--) {
                    if (positionChecker.get(item)) {
                        int intID = Integer.parseInt(itemIdList.get(item));
                        itemIdList.remove(item);
                        try {
                            RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
                            String URL = "http://10.0.2.2:5000/api/TodoItems/" + intID;
                            JSONObject jsonBody = new JSONObject();
                            jsonBody.put("id", intID);
                            final String requestBody = jsonBody.toString();

                            StringRequest stringRequest = new StringRequest(Request.Method.DELETE,
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
                                        return requestBody == null ? null :
                                                requestBody.getBytes("utf-8");
                                    } catch (UnsupportedEncodingException uee) {
                                        VolleyLog.wtf("Unsupported Encoding while trying to get" +
                                                " the bytes of %s using %s", requestBody, "utf-8");
                                        return null;
                                    }
                                }
                            };

                            requestQueue.add(stringRequest);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        adapter.remove(itemList.get(item));
                    }
                }

                positionChecker.clear();
                adapter.notifyDataSetChanged();

            }
        });

        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                SparseBooleanArray positionChecker = lv.getCheckedItemPositions();

                int count = lv.getCount();

                for (int item = count - 1; item >= 0; item--) {
                    if (positionChecker.get(item)) {
                        adapter.remove(itemList.get(item));

                        Toast.makeText(ParentActivity.this,
                                "Chore Deleted Successfully", Toast.LENGTH_SHORT).show();
                    }
                }

                positionChecker.clear();
                adapter.notifyDataSetChanged();

                return false;
            }
        });

        lv.setAdapter(adapter);

    }
}