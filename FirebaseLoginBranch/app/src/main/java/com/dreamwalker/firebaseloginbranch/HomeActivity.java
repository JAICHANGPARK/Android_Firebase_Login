package com.dreamwalker.firebaseloginbranch;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.dreamwalker.firebaseloginbranch.R.id.changeEmail;
import static com.dreamwalker.firebaseloginbranch.R.id.remove;

/**
 * Created by 2E313JCP on 2017-05-19.
 */

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";
    private int btnCount = 0;


    @BindView(R.id.old_email)
    EditText oldEmail;
    @BindView(R.id.new_email)
    EditText newEmail;
    @BindView(R.id.password)
    EditText password;
    @BindView(R.id.new_Password)
    EditText newPassword;

    @BindView(changeEmail)
    Button btnChangeEmail;
    @BindView(R.id.changePass)
    Button btnChangePass;
    @BindView(R.id.send)
    Button btnEmailSend;
    @BindView(remove)
    Button btnRemove;

    @BindView(R.id.sign_out)
    Button btnSignOut;

    @BindView(R.id.change_email_button)
    Button BtnClickChangeEmail;
    @BindView(R.id.change_password_button)
    Button BtnClickChangePwd;
    @BindView(R.id.sending_pass_reset_button)
    Button BtnClickResetPwd;

    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    @BindView(R.id.tool_bar_home)
    Toolbar toolbar;

    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener mAuthListener;

    public FirebaseUser users;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);
        Log.d(TAG, "onCreate " + TAG);



        //setSupportActionBar(toolbar);

        oldEmail.setVisibility(View.GONE);
        newEmail.setVisibility(View.GONE);
        password.setVisibility(View.GONE);
        newPassword.setVisibility(View.GONE);
        btnChangeEmail.setVisibility(View.GONE);
        btnChangePass.setVisibility(View.GONE);
        btnEmailSend.setVisibility(View.GONE);
        btnRemove.setVisibility(View.GONE);

        mAuth = FirebaseAuth.getInstance();

        //get Current FireBase Useer
        //FirebaseUser users = FirebaseAuth.getInstance().getCurrentUser();

        mAuthListener = new FirebaseAuth.AuthStateListener() {

            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    toolbar.setTitle(user.getEmail());
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    //Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                    //startActivity(intent);
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    btnSignOut.setText("Sign In");
                }
            }
        };

        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
    }

    @OnClick(R.id.change_email_button)
    public void changeEmailButton() {
        if (btnCount % 2 == 0) {

            btnCount++;

            newEmail.setVisibility(View.VISIBLE);
            btnChangeEmail.setVisibility(View.VISIBLE);
            oldEmail.setVisibility(View.GONE);
            password.setVisibility(View.GONE);
            newPassword.setVisibility(View.GONE);
            btnChangePass.setVisibility(View.GONE);
            btnEmailSend.setVisibility(View.GONE);
            btnRemove.setVisibility(View.GONE);


        } else if (btnCount % 2 == 1) {

            newEmail.setVisibility(View.GONE);
            btnChangeEmail.setVisibility(View.GONE);
            oldEmail.setVisibility(View.GONE);
            password.setVisibility(View.GONE);
            newPassword.setVisibility(View.GONE);
            btnChangePass.setVisibility(View.GONE);
            btnEmailSend.setVisibility(View.GONE);
            btnRemove.setVisibility(View.GONE);
            btnCount++;

        }
    }

    @OnClick(R.id.change_password_button)
    public void changePasswordBtn() {
        if (btnCount % 2 == 0) {

            btnCount++;

            newPassword.setVisibility(View.VISIBLE);
            btnChangePass.setVisibility(View.VISIBLE);
            newEmail.setVisibility(View.GONE);
            btnChangeEmail.setVisibility(View.GONE);
            oldEmail.setVisibility(View.GONE);
            password.setVisibility(View.GONE);
            btnEmailSend.setVisibility(View.GONE);
            btnRemove.setVisibility(View.GONE);

        } else if (btnCount % 2 == 1) {

            newEmail.setVisibility(View.GONE);
            btnChangeEmail.setVisibility(View.GONE);
            oldEmail.setVisibility(View.GONE);
            password.setVisibility(View.GONE);
            newPassword.setVisibility(View.GONE);
            btnChangePass.setVisibility(View.GONE);
            btnEmailSend.setVisibility(View.GONE);
            btnRemove.setVisibility(View.GONE);
            btnCount++;

        }
    }

    @OnClick(R.id.sending_pass_reset_button)
    public void changePwdResetBtn() {

        if (btnCount % 2 == 0) {

            btnCount++;
            oldEmail.setVisibility(View.VISIBLE);
            btnEmailSend.setVisibility(View.VISIBLE);

            newPassword.setVisibility(View.GONE);
            btnChangePass.setVisibility(View.GONE);
            newEmail.setVisibility(View.GONE);
            btnChangeEmail.setVisibility(View.GONE);
            password.setVisibility(View.GONE);
            btnRemove.setVisibility(View.GONE);

        } else if (btnCount % 2 == 1) {

            newEmail.setVisibility(View.GONE);
            btnChangeEmail.setVisibility(View.GONE);
            oldEmail.setVisibility(View.GONE);
            password.setVisibility(View.GONE);
            newPassword.setVisibility(View.GONE);
            btnChangePass.setVisibility(View.GONE);
            btnEmailSend.setVisibility(View.GONE);
            btnRemove.setVisibility(View.GONE);
            btnCount++;

        }
    }

    @OnClick(R.id.changeEmail)
    public void changeEmail(final View v) {

        progressBar.setVisibility(View.VISIBLE);
        users = FirebaseAuth.getInstance().getCurrentUser();
        if (users != null && !newEmail.getText().toString().trim().equals("")) {
            users.updateEmail(newEmail.getText().toString().trim()).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        mAuth.signOut();
                        progressBar.setVisibility(View.GONE);
                    } else {
                        Snackbar.make(v, "Failed to update email!", Snackbar.LENGTH_LONG).show();
                        progressBar.setVisibility(View.GONE);
                    }
                }
            });
        } else if (newEmail.getText().toString().trim().equals("")) {
            newEmail.setText("Enter Email");
            progressBar.setVisibility(View.GONE);
        }
    }

    @OnClick(R.id.changePass)
    public void changPassword(View view) {
        progressBar.setVisibility(View.VISIBLE);
        if (users != null && !newPassword.getText().toString().trim().equals("")) {
            if (newPassword.getText().toString().trim().length() < 6) {
                newPassword.setError("Password To0 Short,enter minimum 6 characters");
                progressBar.setVisibility(View.GONE);
            } else {
                users.updatePassword(newPassword.getText().toString().trim()).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(HomeActivity.this, "Password is updated, sign in with new password!", Toast.LENGTH_SHORT).show();
                            mAuth.signOut();
                            progressBar.setVisibility(View.GONE);
                        } else {
                            Toast.makeText(HomeActivity.this, "Failed to update password!", Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });
            }
        } else if (newPassword.getText().toString().trim().equals("")) {
            newPassword.setError("Enter password");
            progressBar.setVisibility(View.GONE);
        }
    }

    @OnClick(R.id.send)
    public void resetPassword(final View view) {
        progressBar.setVisibility(View.VISIBLE);
        //trim() 문자의 공백을 지운다.
        if (!oldEmail.getText().toString().trim().equals("")) {
            mAuth.sendPasswordResetEmail(oldEmail.getText().toString().trim()).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Snackbar.make(view, "Reset password email is sent", Snackbar.LENGTH_LONG).show();
                        progressBar.setVisibility(View.GONE);
                    } else {
                        Snackbar.make(view, "Failed to send reset email", Snackbar.LENGTH_LONG).show();
                        progressBar.setVisibility(View.GONE);
                    }
                }
            });

        } else {
            oldEmail.setError("Enter email");
            progressBar.setVisibility(View.GONE);
        }
    }

    @OnClick(R.id.remove_user_button)
    public void removeUser(View view) {
        progressBar.setVisibility(View.VISIBLE);
        if (users != null) {
            users.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(HomeActivity.this, "Your profile is deleted:( Create a account now!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(HomeActivity.this, SignUpActivity.class));
                        finish();
                    } else {
                        Toast.makeText(HomeActivity.this, "Failed to delete your account!", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    }
                }
            });
        }
    }

    @OnClick(R.id.sign_out)
    public void signOutUser() {

        mAuth.signOut();
        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();

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
        progressBar.setVisibility(View.GONE);
    }

    private void showRemoveConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
    }


}
