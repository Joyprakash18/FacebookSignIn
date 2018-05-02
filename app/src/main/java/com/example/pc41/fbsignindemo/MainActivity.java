package com.example.pc41.fbsignindemo;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.Login;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    LoginButton FbloginButton;
    TextView userInfo;
    CallbackManager callbackManager;
    private URL profile_url;
    private ImageView profile_picture;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        callbackManager = CallbackManager.Factory.create();
        setContentView(R.layout.activity_main);
        FbloginButton=(LoginButton)findViewById(R.id.login_button);
        FbloginButton.setReadPermissions(Arrays.asList("email", "user_birthday","user_posts"));
        userInfo=(TextView)findViewById(R.id.userInfo);
        profile_picture=(ImageView)findViewById(R.id.profile_image);

        FbloginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                String accessToken = loginResult.getAccessToken().getToken();
                /*userInfo.setText(
                        "User ID: "
                                + loginResult.getAccessToken().getUserId()
                                + "\n" +
                                "Auth Token: "
                                + loginResult.getAccessToken().getToken()+"\n"+
                                "User Name:"+loginResult.getAccessToken().getLastRefresh()
                );*/
                GraphRequest request = GraphRequest.newMeRequest(
                        AccessToken.getCurrentAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(
                                    JSONObject object,
                                    GraphResponse response) {
                                getFbInfo(object);
                            }
                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,first_name,last_name,email,gender,birthday"); // id,first_name,last_name,email,gender,birthday,cover,picture.type(large)
                request.setParameters(parameters);
                request.executeAsync();

                //getFbInfo();
            }

            @Override
            public void onCancel() {
                userInfo.setText("Login attempt canceled.");

            }

            @Override
            public void onError(FacebookException error) {
                userInfo.setText("Login attempt failed.");

            }
        });

        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    getPackageName(),
                    PackageManager.GET_SIGNATURES);
            for (android.content.pm.Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }
    }

    private void getFbInfo(JSONObject object) {
        try {
            //Log.d(LOG_TAG, "fb json object: " + object);
            //Log.d(LOG_TAG, "fb graph response: " + response);

            String id = object.getString("id");
            String first_name = object.getString("first_name");
            String last_name = object.getString("last_name");
            //String gender = object.getString("gender");
            String birthday=object.getString("birthday");
            profile_url=new URL("https://graph.facebook.com/" + id+ "/picture?width=250&height=250");
            Picasso.with(this).load(profile_url.toString()).into(profile_picture);
            //String image_url = "http://graph.facebook.com/" + id + "/picture?type=large";
            /*String email = null;
            if (object.has("email")) {
                email = object.getString("email");
            }*/


            userInfo.setText(
                    "User ID: "
                            + first_name +" "+last_name
                            + "\n" +
                            "Email: "
                            + object.getString("email")+"\n"+
                            "Birthday:"+birthday
            );
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LoginManager.getInstance().logOut();
        userInfo.setText("");
    }
}
