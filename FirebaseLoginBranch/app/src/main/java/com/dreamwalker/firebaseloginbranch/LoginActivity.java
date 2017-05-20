package com.dreamwalker.firebaseloginbranch;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.dreamwalker.firebaseloginbranch.KakaoLogin.KakaoSDKAdapter;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.TwitterAuthProvider;
import com.kakao.auth.ErrorCode;
import com.kakao.auth.ISessionCallback;
import com.kakao.auth.KakaoSDK;
import com.kakao.auth.Session;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.LogoutResponseCallback;
import com.kakao.usermgmt.callback.MeResponseCallback;
import com.kakao.usermgmt.response.model.UserProfile;
import com.kakao.util.exception.KakaoException;
import com.kakao.util.helper.log.Logger;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.fabric.sdk.android.Fabric;

import static android.support.design.widget.BottomSheetBehavior.STATE_COLLAPSED;
import static android.support.design.widget.BottomSheetBehavior.STATE_EXPANDED;

/**
 * Created by 2E313JCP on 2017-05-19.
 */

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private static final int REQUEST_CODE_GOOGLE_SIGN_IN = 100;
    private static final int REQUEST_CODE_EMAIL_SIGN_IN = 101;
    private static final int REQUEST_CODE_PROFILE = 102;

    private static final int RC_SIGN_IN = 1;

    @BindView(R.id.email)
    EditText inputEmail;
    @BindView(R.id.password)
    EditText inputPassword;

    @BindView(R.id.btn_login)
    Button btnLogin;
    @BindView(R.id.btn_other_login)
    Button btnOtherLogin;

    @BindView(R.id.btn_sign_up)
    Button btnSignUp;
    @BindView(R.id.btn_reset_password)
    Button btnResetPassword;
    @BindView(R.id.btn_google_login)
    SignInButton btnGoogleLogin;
    @BindView(R.id.btn_twitter_login)
    TwitterLoginButton twitterLoginButton;
    @BindView(R.id.btn_line_login)
    Button btnLineLogin;

    @BindView(R.id.btn_facebook_login)
    LoginButton btnFacebookLogin;

    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    @BindView(R.id.rl_bottom_sheet)
    RelativeLayout rlBottomSheet;

    @BindView(R.id.fab)
    FloatingActionButton floatingActionButton;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private GoogleApiClient mGoogleApiClient;
    private BottomSheetBehavior bottomSheetBehavior;
    private CallbackManager callbackManager;
    private SessionCallback callback;

    private static volatile LoginActivity obj = null;
    private static volatile Activity currentActivity = null;

    boolean firstStart;

    public static LoginActivity getGlobalApplicationContext() {
        return obj;
    }

    public static Activity getCurrentActivity() {
        return currentActivity;
    }

    // Activity가 올라올때마다 Activity의 onCreate에서 호출해줘야한다.
    public static void setCurrentActivity(Activity currentActivity) {
        LoginActivity.currentActivity = currentActivity;
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FacebookSdk.sdkInitialize(getApplicationContext());

        /**
         * 트위터 key init
         */
        TwitterAuthConfig authConfig = new TwitterAuthConfig(getString(R.string.twitter_consumer_key), getString(R.string.twitter_consumer_secret));
        Fabric.with(this, new Twitter(authConfig));

        setContentView(R.layout.activity_login);
        Log.d(TAG, "onCreate " + TAG);
        ButterKnife.bind(this);

        obj = this;
        if (KakaoSDK.getAdapter() == null) {
            KakaoSDK.init(new KakaoSDKAdapter());
        }

        SharedPreferences sharedPreferences = getSharedPreferences("Pref", 0);
        firstStart = sharedPreferences.getBoolean("init", true);
        if (firstStart) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("init", false);
            editor.commit();
            //TODO: 실행시 한번만 초기화 되어야 하는 경우
        }



        /**
         * 카카오 어뎁터 초기화
         * 주의점 꼭 앱 실행시 한번만 초기화 해야한다.
         * 중복 초기화가 진행되면  com.kakao.auth.KakaoSDK$AlreadyInitializedException 발생함.
         *
         */


        floatingActionButton.setVisibility(View.GONE);

        bottomSheetBehavior = BottomSheetBehavior.from(rlBottomSheet);
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_EXPANDED:
                        //setTitle("State Expanded");
                        setTitle("Login");
                        break;
                    case BottomSheetBehavior.STATE_HIDDEN:
                        floatingActionButton.setVisibility(View.GONE);
                    default:
                        setTitle("Login");
                        break;
                }
                Log.d("TAG", "newState " + newState);
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                // Slide ...
                // 1이면 완전 펼쳐진 상태
                // 0이면 peekHeight인 상태
                // -1이면 숨김 상태
                Log.i("TAG", "slideOffset " + slideOffset);
            }
        });

        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        /*// Configure Twitter SDK
        TwitterAuthConfig authConfig =  new TwitterAuthConfig("KTOYEQPn7JmzUUlv3wslTUntz", "LrrEKg0tZxVQin4GZ5oXVG2JUVOZrqFwDj5f947oxI9giOOLdm");
        Fabric.with(this, new Twitter(authConfig));*/

        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);
        }


        /**
         * Firebase 초기화
         */
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    Bundle userInfoBundle = new Bundle();
                    userInfoBundle.putString(HomeActivity.KEY_USER_UID, user.getUid());
                    userInfoBundle.putString(HomeActivity.KEY_USER_EMAIL, user.getEmail());
                    if (user.getProviders() != null && !user.getProviders().isEmpty()) {
                        userInfoBundle.putString(HomeActivity.KEY_USER_PROVIDER, user.getProviders().get(0));
                    } else {
                        userInfoBundle.putString(HomeActivity.KEY_USER_PROVIDER, "unknown");
                    }
                    startActivityForResult(HomeActivity.createIntent(LoginActivity.this, userInfoBundle), REQUEST_CODE_PROFILE);
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };
        /**
         * Facebook 초기화
         */
        // initialize facebook login button
        callbackManager = CallbackManager.Factory.create();
        FacebookCallback<LoginResult> facebookLoginCallback = new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                firebaseAuthWithFacebook(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
                if (error != null) {
                    DialogManager.createDialog(LoginActivity.this, error).show();
                }
            }
        };

        btnFacebookLogin.setReadPermissions("email", "public_profile");
        btnFacebookLogin.registerCallback(callbackManager, facebookLoginCallback);


        /**
         * 카카오톡 로그아웃 요청
         *
         * **/
        //한번 로그인이 성공하면 세션 정보가 남아있어서 로그인창이 뜨지 않고 바로 onSuccess()메서드를 호출합니다.
        //테스트 하시기 편하라고 매번 로그아웃 요청을 수행하도록 코드를 넣었습니다 ^^
        UserManagement.requestLogout(new LogoutResponseCallback() {
            @Override
            public void onCompleteLogout() {
                //로그아웃 성공 후 하고싶은 내용 코딩 ~
            }
        });
        callback = new SessionCallback();
        Session.getCurrentSession().addCallback(callback);

        /**
         * Twitter INit
         *
         */
        // initialize twitter login button
        Callback<TwitterSession> twitterLoginCallback = new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                // The TwitterSession is also available through:
                // Twitter.getInstance().core.getSessionManager().getActiveSession()
                TwitterSession session = result.data;
                // with your app's user model
                firebaseAuthWithTwitter(session);
            }

            @Override
            public void failure(TwitterException exception) {
                if (exception != null) {
                    DialogManager.createDialog(LoginActivity.this, exception).show();
                }
            }
        };
        twitterLoginButton.setCallback(twitterLoginCallback);

    }

    @OnClick(R.id.fab)
    public void otherLoginFad() {
        bottomSheetBehavior.setState(STATE_EXPANDED);
    }

    @OnClick(R.id.btn_reset_password)
    public void resetPassword() {
        Toast.makeText(getApplicationContext(), "reset Pass", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(LoginActivity.this, ResetPasswordActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.btn_sign_up)
    public void signUp() {
        Toast.makeText(getApplicationContext(), "Sign Up", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.btn_login)
    public void login(View view) {
        String email = inputEmail.getText().toString().trim();
        final String password = inputPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            Snackbar.make(view, "Enter Email Address", Snackbar.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            Snackbar.make(view, "Enter Email Password", Snackbar.LENGTH_LONG).show();
            return;
        }
        progressBar.setVisibility(View.VISIBLE);

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                progressBar.setVisibility(View.GONE);
                Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());

                // If sign in fails, display a message to the user. If sign in succeeds
                // the auth state listener will be notified and logic to handle the
                // signed in user can be handled in the listener.
                if (!task.isSuccessful()) {
                    Log.w(TAG, "signInWithEmail:failed", task.getException());
                    Toast.makeText(LoginActivity.this, R.string.auth_failed,
                            Toast.LENGTH_SHORT).show();

                    if (password.length() < 6) {
                        inputPassword.setError(getString(R.string.minimum_password));
                    } else {
                        Toast.makeText(LoginActivity.this, getString(R.string.auth_failed), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }

    @OnClick(R.id.btn_other_login)
    public void expendOtherLoginView() {
        bottomSheetBehavior.setState(STATE_COLLAPSED);
        floatingActionButton.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.btn_google_login)
    public void googleLogin() {
        googleLoginInit();
        googleSignIn();
    }

    /*@OnClick(R.id.btn_twitter_login)
    public void twitterLogin() {
        Intent intent = new Intent(LoginActivity.this, TwitterLoginActivity.class);
        startActivity(intent);
    }*/

    private void googleLoginInit() {
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail().build();
        mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext()).enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
            @Override
            public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
            }
        }).addApi(Auth.GOOGLE_SIGN_IN_API, googleSignInOptions).build();
    }


    private void googleSignIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, REQUEST_CODE_GOOGLE_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

//        twitterLoginButton.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == REQUEST_CODE_GOOGLE_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            //btnTwitterLogin.onActivityResult(requestCode, resultCode, data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                // Google Sign In failed, update UI appropriately
                // ...
                //DialogManager.createDialog(LoginActivity.this, CommonStatusCodes.getStatusCodeString(result.getStatus().getStatusCode())).show();
            }
        }else if (requestCode == REQUEST_CODE_PROFILE && resultCode == RESULT_OK) {
            logoutFacebook();
        } else {
            callbackManager.onActivityResult(requestCode, resultCode, data);
            twitterLoginButton.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void logoutFacebook() {
        LoginManager.getInstance().logOut();
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                // If sign in fails, display a message to the user. If sign in succeeds
                // the auth state listener will be notified and logic to handle the
                // signed in user can be handled in the listener.
                if (!task.isSuccessful()) {
                    Log.w(TAG, "signInWithCredential", task.getException());
                    Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }

    private void firebaseAuthWithTwitter(TwitterSession session) {
        AuthCredential credential = TwitterAuthProvider.getCredential(session.getAuthToken().token, session.getAuthToken().secret);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "signInWithCredentialInTwitter:success");
                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                    startActivity(intent);
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.getException());
                    Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void firebaseAuthWithFacebook(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "signInWithCredentialIn Facebook:success");
                    FirebaseUser user = mAuth.getCurrentUser();
                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                    startActivity(intent);
                } else {
                    // If sign in fails, display a message to the user.
                    Log.e(TAG, "signInWithCredential:failure");
                    Log.w(TAG, "signInWithCredential:failure", task.getException());
                    Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }

    private class SessionCallback implements ISessionCallback {

        @Override
        public void onSessionOpened() {

            UserManagement.requestMe(new MeResponseCallback() {

                @Override
                public void onFailure(ErrorResult errorResult) {
                    String message = "failed to get user info. msg=" + errorResult;
                    Logger.d(message);

                    ErrorCode result = ErrorCode.valueOf(errorResult.getErrorCode());
                    if (result == ErrorCode.CLIENT_ERROR_CODE) {
                        finish();
                    } else {
                        //redirectMainActivity();
                    }
                }

                @Override
                public void onSessionClosed(ErrorResult errorResult) {

                }

                @Override
                public void onNotSignedUp() {
                }

                @Override
                public void onSuccess(UserProfile userProfile) {
                    //로그인에 성공하면 로그인한 사용자의 일련번호, 닉네임, 이미지url등을 리턴합니다.
                    //사용자 ID는 보안상의 문제로 제공하지 않고 일련번호는 제공합니다.
                    Log.d(TAG, "KAkao onSuccess");
                    Log.e("UserProfile", userProfile.toString());
                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                }
            });
        }

        @Override
        public void onSessionOpenFailed(KakaoException exception) {
            // 세션 연결이 실패했을때
            // 어쩔때 실패되는지는 테스트를 안해보았음 ㅜㅜ
        }
    }
}
