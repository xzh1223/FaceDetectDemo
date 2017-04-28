package com.gst.gstfacedemo.controller;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.gst.gstfacedemo.model.DetectBean;
import com.gst.gstfacedemo.model.FindSimilarBean;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by alanzhou on 17-4-14.
 */

public class CloudManager {

    public static final String REQUEST_URL = "https://api.cognitive.azure.cn/face/v1.0/";
    public static final String AUTH_KEY = "b927c891a1964c4d87040736244e7824";
    public static final MediaType JSON_TYPE = MediaType.parse("application/json");
    public static final String CLOUD_FACELIST_ID = "gst_test_0";
    public static final String CLOUD_FACELIST_ID_2 = "gst_test_group";
    private static StringBuffer mFaceListId = new StringBuffer();

    public interface CallBack {
        void onFailure(String resultString);

        void onResponse(String resultString, Object resultObject);
    }


    /**
     * Detect
     */
    public static void Detect(byte dataByte[], final CallBack callback) {
        Detect(null, dataByte, callback);
    }

    public static void Detect(String faceAttr, byte dataByte[], final CallBack callback) {
        boolean returnFaceId = true;
        boolean returnFaceLandmarks = false;

//        age: an age number in years.
//        gender: male or female.
//        smile: smile intensity, a number between [0,1]
//        facialHair: consists of lengths of three facial hair areas: moustache, beard and sideburns.
//        headPose: 3-D roll/yew/pitch angles for face direction. Pitch value is a reserved field and will always return 0.
//        glasses: glasses type. Possible values are 'noGlasses', 'readingGlasses', 'sunglasses', 'swimmingGoggles'.
//        emotion: emotions intensity expressed by the face, incluing anger, contempt, disgust, fear, happiness, neutral, sadness and surprise.

//        faceAttr = "age,gender,smile,facialHair,headPose,glasses,emotion";
        StringBuffer requestBuffer = new StringBuffer(REQUEST_URL);
        requestBuffer.append("detect?");
        requestBuffer.append("returnFaceId=" + returnFaceId);
        requestBuffer.append("&returnFaceLandmarks=" + returnFaceLandmarks);

        if (faceAttr != null && faceAttr.length() > 0) {
            requestBuffer.append("&returnFaceAttributes=" + faceAttr);
        }

        String requestURL = requestBuffer.toString();

        OkHttpClient client = new OkHttpClient();

        MediaType TYPE_STREAM = MediaType.parse("application/octet-stream");
        RequestBody imgBody = RequestBody.create(TYPE_STREAM, dataByte);

        Request request = new Request.Builder()
                .header("Ocp-Apim-Subscription-Key", AUTH_KEY)
                .url(requestURL)
                .post(imgBody)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                Log.e("onFailure", e.toString());
                callback.onFailure(e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String resultMessage = response.message().toString();
                String resultBody = response.body().string();

                if (resultMessage.equals("OK")) {

                    JsonParser parser = new JsonParser();
                    JsonArray jsonArray = parser.parse(resultBody).getAsJsonArray();

                    Gson gson = new Gson();
                    ArrayList<DetectBean> userBeanList = new ArrayList<>();

                    //加强for循环遍历JsonArray
                    for (JsonElement user : jsonArray) {
                        //使用GSON，直接转成Bean对象
                        DetectBean userBean = gson.fromJson(user, DetectBean.class);
                        userBeanList.add(userBean);
                    }

                    if (userBeanList.size() <= 0) {
                        callback.onFailure("Can not detect someone");
                    } else if (userBeanList.size() > 1) {
                        callback.onFailure("More than one person");
                    } else {
                        Log.e("onResponse", resultBody);
                        callback.onResponse(resultBody, userBeanList.get(0).faceId);
                    }
                } else {
                    Log.e("onFailure", resultBody);
                    callback.onFailure(resultBody);
                }
            }
        });
    }

    /**
     * Create a Face List
     */
    /*public static void CreateFaceList(String faceListName, final CallBack callback) {
        StringBuffer requestBuffer = new StringBuffer(REQUEST_URL);
        requestBuffer.append("facelists/");
        requestBuffer.append(faceListName);
        String requestURL = requestBuffer.toString();

        OkHttpClient client = new OkHttpClient();
        JSONObject json = new JSONObject();
        try {
            json.put("name", faceListName);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(JSON_TYPE, json.toString());

        Log.e("alan.zhou", "json = " + json.toString());

        Request request = new Request.Builder()
                .header("Ocp-Apim-Subscription-Key", AUTH_KEY)
                .url(requestURL)
                .put(body)
                .build();

        Log.e("alan.zhou", "requestURL = " + request.toString());
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                Log.e("onFailure", e.toString());
                callback.onFailure(e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String resultMessage = response.message().toString();
                String resultBody = response.body().string();

                if (resultMessage.equals("OK")) {
                    Log.e("onResponse", "resultMessage = " + resultMessage + ", resultBody = " + resultBody);
                    callback.onResponse(resultBody, resultBody);
                } else {
                    callback.onFailure(resultBody);
                }
            }
        });
    }*/

    /**
     * Add a Face to a Face List
     */
    /*public static void AddFaceToList(String faceListId, byte dataByte[], final CallBack callback) {
        StringBuffer requestBuffer = new StringBuffer(REQUEST_URL);
        requestBuffer.append("facelists/" + faceListId + "/persistedFaces");
        String requestURL = requestBuffer.toString();
        OkHttpClient client = new OkHttpClient();

        MediaType JSON = MediaType.parse("application/octet-stream");
        RequestBody imgBody = RequestBody.create(JSON, dataByte);

        Request request = new Request.Builder()
                .header("Ocp-Apim-Subscription-Key", AUTH_KEY)
                .url(requestURL)
                .post(imgBody)
                .build();
        Log.e("alan.zhou", "requestURL = " + request.toString());
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                Log.e("onFailure", e.toString());
                callback.onFailure(e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String resultMessage = response.message().toString();
                String resultBody = response.body().string();

                if (resultMessage.equals("OK")) {
                    Gson gson = new Gson();
                    AddFaceToListBean bean = gson.fromJson(resultBody, AddFaceToListBean.class);
                    if(bean != null) {
                        callback.onResponse(resultBody, bean);
                    }
                    else {
                        callback.onFailure(resultBody);
                    }

                } else {
                    callback.onFailure(resultBody);
                }
            }
        });
    }*/

    /**
     * Find Similar
     */
    public static void FindSimilar(String faceId, String faceListId, final CallBack callback) {
        StringBuffer requestBuffer = new StringBuffer(REQUEST_URL);
        requestBuffer.append("findsimilars");
        String requestURL = requestBuffer.toString();

        OkHttpClient client = new OkHttpClient();
        JSONObject json = new JSONObject();
        try {
            json.put("faceId", faceId);
            json.put("faceListId", faceListId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(JSON_TYPE, json.toString());

        Request request = new Request.Builder()
                .header("Ocp-Apim-Subscription-Key", AUTH_KEY)
                .url(requestURL)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                Log.e("onFailure", e.toString());
                callback.onFailure(e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String resultMessage = response.message().toString();
                String resultBody = response.body().string();

                if(resultMessage.equals("OK")) {
                    JsonParser parser = new JsonParser();
                    JsonArray jsonArray = parser.parse(resultBody).getAsJsonArray();

                    Gson gson = new Gson();
                    ArrayList<FindSimilarBean> findBeanList = new ArrayList<>();

                    for (JsonElement tempJson : jsonArray) {
                        FindSimilarBean bean = gson.fromJson(tempJson, FindSimilarBean.class);
                        findBeanList.add(bean);
                    }
                    callback.onResponse(resultBody, findBeanList);
                }
                else {
                    callback.onFailure(resultBody);
                }

            }
        });
    }

    /**
     *  Get a face list
     */
    public static void getFaceList(String faceListId, final CallBack callback) {

        StringBuffer requestBuffer = new StringBuffer(REQUEST_URL);
        requestBuffer.append("facelists/");
        requestBuffer.append(faceListId);
        String requestURL = requestBuffer.toString();

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .header("Ocp-Apim-Subscription-Key", AUTH_KEY)
                .url(requestURL)
                .build();

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {

                Log.e("onFailure", e.toString());
                callback.onFailure(e.toString());

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String resultMessage = response.message().toString();
                String resultBody = response.body().string();

                if(resultMessage.equals("OK")) {

                    Log.e("resultBody",resultBody);

                    try {
                        callback.onResponse(resultBody,resultBody);
                    } catch (Exception e) {
                        callback.onFailure(e.toString());
                    }
                } else {
                    callback.onFailure(resultBody);
                }

            }
        });

    }

    /*
        Get face lists
     */
    public static void getFaceLists (String faceListId, final CallBack callback) {

        StringBuffer requestBuffer = new StringBuffer(REQUEST_URL);
        requestBuffer.append("facelists");
        String requestURL = requestBuffer.toString();

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .header("Ocp-Apim-Subscription-Key", AUTH_KEY)
                .url(requestURL)
                .build();

        Log.e("请求URL测试-------------->", "requestURL = " + request.toString());

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {

                Log.e("onFailure", e.toString());
                callback.onFailure(e.toString());

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String resultMessage = response.message().toString();
                String resultBody = response.body().string();

                if(resultMessage.equals("OK")) {

                    Log.e("resultBody", resultBody);

                    List<String> mList = new ArrayList<String>();
                    mList.clear();

                    try {
                        JSONArray jsonArray = new JSONArray(resultBody);
                        for (int i=0;i<jsonArray.length();i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            mList.add(jsonObject.getString("faceListId") + "\n\n" + jsonObject.getString("name"));
                        }

                        callback.onResponse(resultBody,mList);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

            }
        });

    }


    /**
     *  delete a face from a list
     */
    public static void deleteFace (String faceListId, String persistedFaceId, final CallBack callback) {

        StringBuffer requestBuffer = new StringBuffer(REQUEST_URL);
        requestBuffer.append("facelists/");
        requestBuffer.append(faceListId);
        requestBuffer.append("/persistedFaces/");
        requestBuffer.append(persistedFaceId);
        String requestURL = requestBuffer.toString();

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .header("Ocp-Apim-Subscription-Key", AUTH_KEY)
                .url(requestURL)
                .delete()
                .build();

        Log.e("请求URL测试-------------->", "requestURL = " + request.toString());

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("onFailure", e.toString());
                callback.onFailure(e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String resultMessage = response.message().toString();
                String resultBody = response.body().string();

                Log.e("resultBody", resultBody);

                if(resultMessage.equals("OK")) {

                    Log.e("resultBody", resultBody);
                    callback.onResponse(resultBody,resultBody);

                }

            }
        });

    }


    /**
     *  delete a facelist
     */
    public static void deleteFaceList (String faceListId, final CallBack callback) {

        StringBuffer requestBuffer = new StringBuffer(REQUEST_URL);
        requestBuffer.append("facelists/");
        requestBuffer.append(faceListId);
        String requestURL = requestBuffer.toString();

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .header("Ocp-Apim-Subscription-Key", AUTH_KEY)
                .url(requestURL)
                .delete()
                .build();

        Log.e("请求URL测试-------------->", "requestURL = " + request.toString());

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("onFailure", e.toString());
                callback.onFailure(e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String resultMessage = response.message().toString();
                String resultBody = response.body().string();

                Log.e("resultMessage", resultMessage);

                if(resultMessage.equals("OK")) {

                    Log.e("resultBody", resultBody);
                    callback.onResponse("删除成功",resultBody);

                }

            }
        });

    }

    /**
     *  update a face list
     */
    public static void updateFaceList(String faceListId,String name, String userData, final CallBack callback) {

        StringBuffer requestBuffer = new StringBuffer(REQUEST_URL);
        requestBuffer.append("facelists/");
        requestBuffer.append(faceListId);
        String requestURL = requestBuffer.toString();

        OkHttpClient client = new OkHttpClient();

        final JSONObject json = new JSONObject();
        try {
            json.put("name", name);
            json.put("userData", userData);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(JSON_TYPE, json.toString());

        Log.e("json数据测试-------------->", "json = " + json.toString());

        Request request = new Request.Builder()
                .header("Ocp-Apim-Subscription-Key", AUTH_KEY)
                .url(requestURL)
                .patch(body)
                .build();

        Log.e("请求URL测试-------------->", "requestURL = " + request.toString());

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {

                Log.e("onFailure", e.toString());
                callback.onFailure(e.toString());

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String resultMessage = response.message().toString();
                String resultBody = response.body().string();

                Log.e("resultMessage", resultMessage);

                if(resultMessage.equals("OK")) {

                    Log.e("resultBody", resultBody);
                    callback.onResponse("update success",resultBody);

                } else {

                    Log.e("resultBody", resultBody);
                    callback.onResponse(resultBody,resultBody);

                }

            }
        });

    }

    /**
     * get group
     */
    public static void getGroup(List<String> faceIds, final CallBack callback) {

        StringBuffer requestBuffer = new StringBuffer(REQUEST_URL);
        requestBuffer.append("group");
        String requestURL = requestBuffer.toString();

        OkHttpClient client = new OkHttpClient();

        final JSONObject json = new JSONObject();

        JSONArray array = new JSONArray(faceIds);

        try {
            json.put("faceIds", array);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(JSON_TYPE, json.toString());
        Log.e("json数据测试-------------->", "json = " + json.toString());
        try {
            String string = json.getString("faceIds");
            Log.e("json数据测试-------------->", "json = " + string);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Request request = new Request.Builder()
                .header("Ocp-Apim-Subscription-Key", AUTH_KEY)
                .url(requestURL)
                .post(body)
                .build();

        Log.e("请求URL测试-------------->", "requestURL = " + request.toString());

        client.newCall(request).enqueue(new Callback() {


            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("onFailure", e.toString());
                callback.onFailure(e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String resultMessage = response.message().toString();
                String resultBody = response.body().string();

                Log.e("resultMessage----->", resultMessage);

                if(resultMessage.equals("OK")) {

                    Log.e("groupReturnOk", resultBody);
                    callback.onResponse(resultBody,resultBody);

                } else {

                    Log.e("groupReturn", resultBody);
                    callback.onResponse(resultBody,resultBody);

                }
            }
        });

    }

    /**
     * create a person group
     */
    public static void createPersonGroup(String personGroupId, String name, String userData, final CallBack callback) {

        StringBuffer requestBuffer = new StringBuffer(REQUEST_URL);
        requestBuffer.append("persongroups/");
        requestBuffer.append(personGroupId);
        String requestURL = requestBuffer.toString();

        OkHttpClient client = new OkHttpClient();

        final JSONObject json = new JSONObject();
        try {
            json.put("name", name);
            json.put("userData",userData);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(JSON_TYPE, json.toString());

        Log.e("json数据测试-------------->", "json = " + json.toString());

        Request request = new Request.Builder()
                .header("Ocp-Apim-Subscription-Key", AUTH_KEY)
                .url(requestURL)
                .put(body)
                .build();

        Log.e("请求URL测试-------------->", "requestURL = " + request.toString());

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("onFailure", e.toString());
                callback.onFailure(e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String resultMessage = response.message().toString();
                String resultBody = response.body().string();

                if(resultMessage.equals("OK")) {

                    Log.e("ok", resultBody);
                    callback.onResponse("创建成功",resultBody);

                } else {

                    Log.e("error", resultBody);
                    callback.onFailure(resultBody);

                }

            }
        });

    }

    /**
     * delete a person group
     */
    public static void deletePersonGroup(String personGroupId, final CallBack callback) {

        StringBuffer requestBuffer = new StringBuffer(REQUEST_URL);
        requestBuffer.append("persongroups/");
        requestBuffer.append(personGroupId);
        String requestURL = requestBuffer.toString();

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .header("Ocp-Apim-Subscription-Key", AUTH_KEY)
                .url(requestURL)
                .delete()
                .build();

        Log.e("请求URL测试-------------->", "requestURL = " + request.toString());

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("onFailure", e.toString());
                callback.onFailure(e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String resultMessage = response.message().toString();
                String resultBody = response.body().string();

                if(resultMessage.equals("OK")) {

                    Log.e("ok", resultBody);
                    callback.onResponse("删除成功",resultBody);

                } else {

                    Log.e("error", resultBody);
                    callback.onFailure(resultBody);

                }
            }
        });

    }

    /**
     * get a person group
     */
    public static void getPersonGroup (String personGroupId, final CallBack callback) {

        StringBuffer requestBuffer = new StringBuffer(REQUEST_URL);
        requestBuffer.append("persongroups/");
        requestBuffer.append(personGroupId);
        String requestURL = requestBuffer.toString();

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .header("Ocp-Apim-Subscription-Key", AUTH_KEY)
                .url(requestURL)
                .build();

        Log.e("请求URL测试-------------->", "requestURL = " + request.toString());

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("onFailure", e.toString());
                callback.onFailure(e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String resultMessage = response.message().toString();
                String resultBody = response.body().string();

                if(resultMessage.equals("OK")) {

                    Log.e("ok", resultBody);
                    callback.onResponse(resultBody,resultBody);

                } else {

                    Log.e("error", resultBody);
                    callback.onFailure(resultBody);

                }
            }
        });

    }

    /**
     * create a person
     */
    public static void createPerson (String personGroupId, String name, String userData, final CallBack callback) {

        StringBuffer requestBuffer = new StringBuffer(REQUEST_URL);
        requestBuffer.append("persongroups/");
        requestBuffer.append(personGroupId);
        requestBuffer.append("/persons");
        String requestURL = requestBuffer.toString();

        OkHttpClient client = new OkHttpClient();

        final JSONObject json = new JSONObject();
        try {
            json.put("name", name);
            json.put("userData",userData);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(JSON_TYPE, json.toString());

        Log.e("json数据测试-------------->", "json = " + json.toString());

        Request request = new Request.Builder()
                .header("Ocp-Apim-Subscription-Key", AUTH_KEY)
                .url(requestURL)
                .post(body)
                .build();

        Log.e("请求URL测试-------------->", "requestURL = " + request.toString());

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("onFailure", e.toString());
                callback.onFailure(e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String resultMessage = response.message().toString();
                String resultBody = response.body().string();

                if(resultMessage.equals("OK")) {

                    Log.e("ok", resultBody);
                    callback.onResponse(resultBody,resultBody);

                } else {

                    Log.e("error", resultBody);
                    callback.onFailure(resultBody);

                }
            }
        });

    }


    /**
     * get person list
     */
    public static void getPersonList(String personGroupId, final CallBack callback) {

        StringBuffer requestBuffer = new StringBuffer(REQUEST_URL);
        requestBuffer.append("persongroups/");
        requestBuffer.append(personGroupId);
        requestBuffer.append("/persons");
        String requestURL = requestBuffer.toString();

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .header("Ocp-Apim-Subscription-Key", AUTH_KEY)
                .url(requestURL)
                .build();

        Log.e("请求URL测试-------------->", "requestURL = " + request.toString());

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("onFailure", e.toString());
                callback.onFailure(e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String resultMessage = response.message().toString();
                String resultBody = response.body().string();

                if(resultMessage.equals("OK")) {

                    Log.e("ok", resultBody);
                    callback.onResponse(resultBody,resultBody);

                } else {

                    Log.e("error", resultBody);
                    callback.onFailure(resultBody);

                }
            }
        });

    }

    /**
     * get a person
     */
    public static void getPerson(String personGroupId, String personId, final CallBack callback) {

        StringBuffer requestBuffer = new StringBuffer(REQUEST_URL);
        requestBuffer.append("persongroups/");
        requestBuffer.append(personGroupId);
        requestBuffer.append("/persons/");
        requestBuffer.append(personId);
        String requestURL = requestBuffer.toString();

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .header("Ocp-Apim-Subscription-Key", AUTH_KEY)
                .url(requestURL)
                .build();

        Log.e("请求URL测试-------------->", "requestURL = " + request.toString());

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("onFailure", e.toString());
                callback.onFailure(e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String resultMessage = response.message().toString();
                String resultBody = response.body().string();

                if(resultMessage.equals("OK")) {

                    Log.e("ok", resultBody);
                    callback.onResponse(resultBody,resultBody);

                } else {

                    Log.e("error", resultBody);
                    callback.onFailure(resultBody);

                }
            }
        });

    }

    /**
     * delete a person
     */
    public static void deletePerson (String personGroupId, String personId, final CallBack callback) {

        StringBuffer requestBuffer = new StringBuffer(REQUEST_URL);
        requestBuffer.append("persongroups/");
        requestBuffer.append(personGroupId);
        requestBuffer.append("/persons/");
        requestBuffer.append(personId);
        String requestURL = requestBuffer.toString();

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .header("Ocp-Apim-Subscription-Key", AUTH_KEY)
                .url(requestURL)
                .delete()
                .build();

        Log.e("请求URL测试-------------->", "requestURL = " + request.toString());

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("onFailure", e.toString());
                callback.onFailure(e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String resultMessage = response.message().toString();
                String resultBody = response.body().string();

                if(resultMessage.equals("OK")) {

                    Log.e("ok", resultBody);
                    callback.onResponse("删除成功",resultBody);

                } else {

                    Log.e("error", resultBody);
                    callback.onFailure(resultBody);

                }
            }
        });

    }

    /**
     * add a person face
     */
    public static void addPersonFace (String personGroupId, String personId, byte dataByte[], String userData, final CallBack callback) {
        StringBuffer requestBuffer = new StringBuffer(REQUEST_URL);
        requestBuffer.append("persongroups/");
        requestBuffer.append(personGroupId);
        requestBuffer.append("/persons/");
        requestBuffer.append(personId);
        requestBuffer.append("/persistedFaces?userData=");
        requestBuffer.append(userData);

        String requestURL = requestBuffer.toString();
        OkHttpClient client = new OkHttpClient();

        MediaType JSON = MediaType.parse("application/octet-stream");
        RequestBody imgBody = RequestBody.create(JSON, dataByte);

        Request request = new Request.Builder()
                .header("Ocp-Apim-Subscription-Key", AUTH_KEY)
                .url(requestURL)
                .post(imgBody)
                .build();
        Log.e("alan.zhou", "requestURL = " + request.toString());
        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("onFailure", e.toString());
                callback.onFailure(e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String resultMessage = response.message().toString();
                String resultBody = response.body().string();

                if(resultMessage.equals("OK")) {

                    Log.e("ok", resultBody);
                    try {
                        JSONObject jsonObject = new JSONObject(resultBody);
                        String persistedFaceId = jsonObject.getString("persistedFaceId");
                        callback.onResponse(persistedFaceId,resultBody);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else {

                    Log.e("error", resultBody);
                    callback.onFailure(resultBody);

                }
            }
        });

    }

    /**
     * get a person face
     */
    public static void getPersonFace(String personGroupId, String personId, String persistedFaceId,final CallBack callback) {

        StringBuffer requestBuffer = new StringBuffer(REQUEST_URL);
        requestBuffer.append("persongroups/");
        requestBuffer.append(personGroupId);
        requestBuffer.append("/persons/");
        requestBuffer.append(personId);
        requestBuffer.append("/persistedFaces/");
        requestBuffer.append(persistedFaceId);

        String requestURL = requestBuffer.toString();

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .header("Ocp-Apim-Subscription-Key", AUTH_KEY)
                .url(requestURL)
                .build();

        Log.e("请求URL测试-------------->", "requestURL = " + request.toString());

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("onFailure", e.toString());
                callback.onFailure(e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String resultMessage = response.message().toString();
                String resultBody = response.body().string();

                if(resultMessage.equals("OK")) {

                    Log.e("ok", resultBody);
                    callback.onResponse(resultBody,resultBody);

                } else {

                    Log.e("error", resultBody);
                    callback.onFailure(resultBody);

                }
            }
        });

    }

    /**
     * delete a person face
     */
    public static void deletePersonFace(String personGroupId, String personId, String persistedFaceId,final CallBack callback) {

        StringBuffer requestBuffer = new StringBuffer(REQUEST_URL);
        requestBuffer.append("persongroups/");
        requestBuffer.append(personGroupId);
        requestBuffer.append("/persons/");
        requestBuffer.append(personId);
        requestBuffer.append("/persistedFaces/");
        requestBuffer.append(persistedFaceId);

        String requestURL = requestBuffer.toString();

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .header("Ocp-Apim-Subscription-Key", AUTH_KEY)
                .url(requestURL)
                .delete()
                .build();

        Log.e("请求URL测试-------------->", "requestURL = " + request.toString());

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("onFailure", e.toString());
                callback.onFailure(e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String resultMessage = response.message().toString();
                String resultBody = response.body().string();

                if(resultMessage.equals("OK")) {

                    Log.e("ok", resultBody);
                    callback.onResponse("删除成功",resultBody);

                } else {

                    Log.e("error", resultBody);
                    callback.onFailure(resultBody);

                }
            }
        });

    }

    /**
     * update a person face
     */
    public static void updatePersonFace(String personGroupId, String personId,
                                        String persistedFaceId,String userData, final CallBack callback) {

        StringBuffer requestBuffer = new StringBuffer(REQUEST_URL);
        requestBuffer.append("persongroups/");
        requestBuffer.append(personGroupId);
        requestBuffer.append("/persons/");
        requestBuffer.append(personId);
        requestBuffer.append("/persistedFaces/");
        requestBuffer.append(persistedFaceId);

        String requestURL = requestBuffer.toString();

        OkHttpClient client = new OkHttpClient();

        final JSONObject json = new JSONObject();
        try {
            json.put("userData",userData);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(JSON_TYPE, json.toString());

        Log.e("json数据测试-------------->", "json = " + json.toString());

        Request request = new Request.Builder()
                .header("Ocp-Apim-Subscription-Key", AUTH_KEY)
                .url(requestURL)
                .patch(body)
                .build();

        Log.e("请求URL测试-------------->", "requestURL = " + request.toString());

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("onFailure", e.toString());
                callback.onFailure(e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String resultMessage = response.message().toString();
                String resultBody = response.body().string();

                if(resultMessage.equals("OK")) {

                    Log.e("ok", resultBody);
                    callback.onResponse("修改成功",resultBody);

                } else {

                    Log.e("error", resultBody);
                    callback.onFailure(resultBody);

                }
            }
        });

    }

    /**
     *  get person group list
     */
    public static void getPersonGroupList (final CallBack callback) {

        StringBuffer requestBuffer = new StringBuffer(REQUEST_URL);
        requestBuffer.append("persongroups");

        String requestURL = requestBuffer.toString();

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .header("Ocp-Apim-Subscription-Key", AUTH_KEY)
                .url(requestURL)
                .build();

        Log.e("请求URL测试----->", "requestURL = " + request.toString());

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("onFailure", e.toString());
                callback.onFailure(e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String resultMessage = response.message().toString();
                String resultBody = response.body().string();

                if(resultMessage.equals("OK")) {

                    Log.e("ok", resultBody);
                    callback.onResponse(resultBody,resultBody);

                } else {

                    Log.e("error", resultBody);
                    callback.onFailure(resultBody);

                }
            }
        });

    }

    /**
     *  create a face list
     */
    public static void createFaceList(String faceListId, String name, String userData, final CallBack callback) {

        StringBuffer requestBuffer = new StringBuffer(REQUEST_URL);
        requestBuffer.append("facelists/");
        requestBuffer.append(faceListId);
        String requestURL = requestBuffer.toString();

        OkHttpClient client = new OkHttpClient();
        JSONObject json = new JSONObject();
        try {
            json.put("name", name);
            json.put("userData",userData);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(JSON_TYPE, json.toString());

        Log.e("alan.zhou", "json = " + json.toString());

        Request request = new Request.Builder()
                .header("Ocp-Apim-Subscription-Key", AUTH_KEY)
                .url(requestURL)
                .put(body)
                .build();

        Log.e("alan.zhou", "requestURL = " + request.toString());
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                Log.e("onFailure", e.toString());
                callback.onFailure(e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String resultMessage = response.message().toString();
                String resultBody = response.body().string();

                if (resultMessage.equals("OK")) {
                    Log.e("onResponse", "resultMessage = " + resultMessage + ", resultBody = " + resultBody);
                    callback.onResponse("创建成功", resultBody);
                } else {
                    callback.onFailure(resultBody);
                }
            }
        });

    }

    /**
     * add a face into a face list
     */
    public static void addFace(String faceListId, byte dataByte[], String userData, final CallBack callback) {

        StringBuffer requestBuffer = new StringBuffer(REQUEST_URL);
        requestBuffer.append("facelists/");
        requestBuffer.append(faceListId);
        requestBuffer.append("/persistedFaces?userData=");
        requestBuffer.append(userData);

        String requestURL = requestBuffer.toString();
        OkHttpClient client = new OkHttpClient();

        MediaType JSON = MediaType.parse("application/octet-stream");
        RequestBody imgBody = RequestBody.create(JSON, dataByte);

        Request request = new Request.Builder()
                .header("Ocp-Apim-Subscription-Key", AUTH_KEY)
                .url(requestURL)
                .post(imgBody)
                .build();
        Log.e("alan.zhou", "requestURL = " + request.toString());
        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("onFailure", e.toString());
                callback.onFailure(e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String resultMessage = response.message().toString();
                String resultBody = response.body().string();

                if(resultMessage.equals("OK")) {

                    Log.e("ok", resultBody);
                    try {
                        JSONObject jsonObject = new JSONObject(resultBody);
                        String persistedFaceId = jsonObject.getString("persistedFaceId");
                        callback.onResponse(persistedFaceId,resultBody);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else {

                    Log.e("error", resultBody);
                    callback.onFailure(resultBody);

                }
            }
        });

    }

    /**
     *  identify
     */
    public static void identify (String personGroupId, List<String> mList, final CallBack callback) {

        StringBuffer requestBuffer = new StringBuffer(REQUEST_URL);
        requestBuffer.append("identify");
        String requestURL = requestBuffer.toString();

        OkHttpClient client = new OkHttpClient();

        JSONObject json = new JSONObject();
        JSONArray faceIds = new JSONArray(mList);
        try {
            json.put("personGroupId", personGroupId);
            json.put("faceIds",faceIds);
            json.put("maxNumOfCandidatesReturned",3);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(JSON_TYPE, json.toString());

        Log.e("alan.zhou", "json = " + json.toString());

        Request request = new Request.Builder()
                .header("Ocp-Apim-Subscription-Key", AUTH_KEY)
                .url(requestURL)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("onFailure", e.toString());
                callback.onFailure(e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String resultMessage = response.message().toString();
                String resultBody = response.body().string();

                if(resultMessage.equals("OK")) {

                    Log.e("ok", resultBody);
                    callback.onResponse(resultBody,resultBody);

                } else {

                    Log.e("error", resultBody);
                    callback.onFailure(resultBody);

                }

            }
        });

    }

    /**
     * train person Group
     */
    public static void train(String personGroupId, final CallBack callback) {

        StringBuffer requestBuffer = new StringBuffer(REQUEST_URL);
        requestBuffer.append("persongroups/");
        requestBuffer.append(personGroupId);
        requestBuffer.append("/train");

        String requestURL = requestBuffer.toString();

        OkHttpClient client = new OkHttpClient();
        JSONObject json = new JSONObject();
        try {
            json.put("personGroupId", personGroupId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(JSON_TYPE, json.toString());

        Request request = new Request.Builder()
                .header("Ocp-Apim-Subscription-Key", AUTH_KEY)
                .url(requestURL)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("onFailure", e.toString());
                callback.onFailure(e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String resultMessage = response.message().toString();
                String resultBody = response.body().string();

                Log.e("resultMessage", resultMessage);

                if(resultMessage.equals("Accepted")) {
                    Log.e("ok","train success");
                    callback.onResponse("train success",resultBody);
                } else {

                    Log.e("error", resultBody);
                    callback.onFailure(resultBody);

                }

            }
        });

    }

    /**
     * Verify
     */
    public static void verify(String faceId,String faceId2, String personGroupId, String personId, final CallBack callback) {

        StringBuffer requestBuffer = new StringBuffer(REQUEST_URL);
        requestBuffer.append("verify");

        String requestURL = requestBuffer.toString();

        OkHttpClient client = new OkHttpClient();
        JSONObject json = new JSONObject();
        try {
            if (faceId.length()<=0 || faceId2.length()<=0)   {
                json.put("personGroupId",personGroupId);
                json.put("personId",personId);
                if (faceId.length()<=0) {
                    json.put("faceId2",faceId2);
                }else if (faceId2.length()<=0) {
                    json.put("faceId",faceId);
                }
                Log.e("------>","one");
            } else if (faceId.length()>0 && faceId2.length()>0) {
                json.put("faceId1",faceId);
                json.put("faceId2",faceId2);
                Log.e("------>","two");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(JSON_TYPE, json.toString());

        Request request = new Request.Builder()
                .header("Ocp-Apim-Subscription-Key", AUTH_KEY)
                .url(requestURL)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("onFailure", e.toString());
                callback.onFailure(e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String resultMessage = response.message().toString();
                String resultBody = response.body().string();

                Log.e("resultMessage", resultMessage);

                if(resultMessage.equals("OK")) {
                    Log.e("ok",resultBody);
                    callback.onResponse(resultBody,resultBody);
                } else {

                    Log.e("error", resultBody);
                    callback.onFailure(resultBody);

                }

            }
        });
    }

    /**
     *  update a person group
     */
    public static void updatePersonGroup (String personGroupId, String name, String userData, final CallBack callback) {

        StringBuffer requestBuffer = new StringBuffer(REQUEST_URL);
        requestBuffer.append("persongroups/");
        requestBuffer.append(personGroupId);

        String requestURL = requestBuffer.toString();

        OkHttpClient client = new OkHttpClient();
        JSONObject json = new JSONObject();
        try {
            json.put("name",name);
            json.put("userData",userData);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(JSON_TYPE, json.toString());

        Request request = new Request.Builder()
                .header("Ocp-Apim-Subscription-Key", AUTH_KEY)
                .url(requestURL)
                .patch(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("onFailure", e.toString());
                callback.onFailure(e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String resultMessage = response.message().toString();
                String resultBody = response.body().string();

                Log.e("resultMessage", resultMessage);

                if(resultMessage.equals("OK")) {
                    Log.e("ok",resultBody);
                    callback.onResponse(resultBody,resultBody);
                } else {

                    Log.e("error", resultBody);
                    callback.onFailure(resultBody);

                }

            }
        });

    }

    /**
     * update a person
     */
    public static void updatePerson(String personGroupId, String personId, String name, String userData, final CallBack callback){

        StringBuffer requestBuffer = new StringBuffer(REQUEST_URL);
        requestBuffer.append("persongroups/");
        requestBuffer.append(personGroupId);
        requestBuffer.append("/persons/");
        requestBuffer.append(personId);

        String requestURL = requestBuffer.toString();

        OkHttpClient client = new OkHttpClient();
        JSONObject json = new JSONObject();
        try {
            json.put("name",name);
            json.put("userData",userData);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(JSON_TYPE, json.toString());

        Request request = new Request.Builder()
                .header("Ocp-Apim-Subscription-Key", AUTH_KEY)
                .url(requestURL)
                .patch(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("onFailure", e.toString());
                callback.onFailure(e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String resultMessage = response.message().toString();
                String resultBody = response.body().string();

                Log.e("resultMessage", resultMessage);

                if(resultMessage.equals("OK")) {
                    Log.e("ok",resultBody);
                    callback.onResponse(resultBody,resultBody);
                } else {

                    Log.e("error", resultBody);
                    callback.onFailure(resultBody);

                }

            }
        });

    }

    /**
     *  get person group train
     */
    public static void getTrainingStatus (String personGroupId, final CallBack callback) {

        StringBuffer requestBuffer = new StringBuffer(REQUEST_URL);
        requestBuffer.append("persongroups/");
        requestBuffer.append(personGroupId);
        requestBuffer.append("/training");

        String requestURL = requestBuffer.toString();

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .header("Ocp-Apim-Subscription-Key", AUTH_KEY)
                .url(requestURL)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("getTrainingStatus", e.toString());
                callback.onFailure(e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String resultMessage = response.message().toString();
                String resultBody = response.body().string();

                Log.e("getTrainingStatus", resultMessage);

                if(resultMessage.equals("OK")) {
                    try {
                        JSONObject jsonObject = new JSONObject(resultMessage);
                        String status = jsonObject.getString("status");
                        Log.e("ok",status);
                        callback.onResponse(status,resultBody);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else {

                    Log.e("error", resultBody);
                    callback.onFailure(resultBody);

                }

            }
        });

    }

}
