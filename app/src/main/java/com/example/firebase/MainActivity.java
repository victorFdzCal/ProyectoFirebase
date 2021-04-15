package com.example.firebase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 123;
    private EditText edtEmail;
    private EditText edtPass;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private GoogleSignInClient mGoogleSignInClient;
    private GoogleSignInOptions gso;
    private Boolean nuevoUsuario;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        db = FirebaseFirestore.getInstance();
        edtEmail = (EditText) findViewById(R.id.edtUser);
        edtPass = (EditText) findViewById(R.id.edtPass);
        mAuth = FirebaseAuth.getInstance();
        mAuth.signOut();
        nuevoUsuario = false;
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    if (!nuevoUsuario) {
                        Toast.makeText(MainActivity.this, "El usuario ha iniciado sesión", Toast.LENGTH_SHORT).show();
                        goToProfileActivity();
                    }
                }
                else {
                    Toast.makeText(MainActivity.this, "El usuario salió de la sesión", Toast.LENGTH_SHORT).show();
                }
            }
        };

        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void goToProfileActivity() {
        Intent intent = new Intent(this,HomeActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (authStateListener != null){
            mAuth.removeAuthStateListener(authStateListener);
        }
    }

    public void login(View view) {
        String email = String.valueOf(edtEmail.getText());
        String pass = String.valueOf(edtPass.getText());
        if (email.isEmpty() || !email.contains("@")){
            edtEmail.setError("Email no válido");
        }else if (pass.isEmpty() || pass.length() < 3){
            edtPass.setError("Contraseña inválida");
        }else {
            mAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (!task.isSuccessful()) {
                        Log.w("login", "signInWithEmail: failure" + task.getException().toString());
                        Toast.makeText(MainActivity.this, "Fallo al iniciar sesión", Toast.LENGTH_SHORT).show();
                        try {
                            throw task.getException();
                        }catch (FirebaseAuthInvalidUserException e){
                            nuevoUsuario = true;
                            edtEmail.setError("No existe el usuario");
                        }catch (FirebaseAuthInvalidCredentialsException e){
                            edtPass.setError("Contraseña incorrecta");
                        } catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
    }

    private void signIn() {
        googleSignOut();
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void googleSignOut(){
        mGoogleSignInClient = GoogleSignIn.getClient(this,gso);
        mGoogleSignInClient.signOut().addOnCompleteListener(MainActivity.this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(MainActivity.this, "El usuario ha salido de la sesión de google", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                mAuth.signInWithEmailAndPassword(account.getEmail(), "1234567").addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w("login", "signInWithEmail: failure" + task.getException().toString());
                            Toast.makeText(MainActivity.this, "Fallo al iniciar sesión", Toast.LENGTH_SHORT).show();
                            try {
                                throw task.getException();
                            }catch (FirebaseAuthInvalidUserException e){
                                edtEmail.setError("No existe el usuario");
                            }catch (FirebaseAuthInvalidCredentialsException e){
                                edtPass.setError("Contraseña incorrecta");
                            } catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    }
                });
                Log.d("googleLogin", "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w("googleLogin", "Google sign in failed", e);
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("googleLogin", "signInWithCredential:success");
                            if (nuevoUsuario){
                                addToFirestore("users");
                            }
                            //FirebaseUser user = mAuth.getCurrentUser();
                            //updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("googleLogin", "signInWithCredential:failure", task.getException());
                            //updateUI(null);
                        }
                    }
                });
    }

    public void signup(View view) {
        Intent intent = new Intent(this, SignUpActivity.class);
        startActivity(intent);
    }

    public void rememberPass(View view) {
        Intent intent = new Intent(this,RememberPassActivity.class);
        startActivity(intent);
    }

    public void googleLogin(View view) {
        signIn();
    }

    public void addToFirestore(String usertype) {
        Map<String,Object> user = new HashMap<>();
        user.put("nombre", mAuth.getCurrentUser().getDisplayName());
        user.put("email", mAuth.getCurrentUser().getEmail());
        user.put("telefono", "");
        db.collection(usertype).document(mAuth.getCurrentUser().getUid()).set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.i("firestore","Nuevo usuario en firestore");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i("firestore","Fallo al insertar usuario en firestore");
            }
        });
    }
}