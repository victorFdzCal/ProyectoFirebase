package com.example.firebase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    private EditText edtEmail;
    private EditText edtPass;
    private EditText edtConfirmPass;
    private EditText edtUserName;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String usertype;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        edtEmail = findViewById(R.id.edtSignUpEmail);
        edtPass = findViewById(R.id.edtSignUpPass);
        edtUserName = findViewById(R.id.edtSignUpUserName);
        edtConfirmPass = findViewById(R.id.edtSignUpConfirmPass);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
    }


    public void signup(String usertype) {
        String userName = String.valueOf(edtUserName.getText());
        String email = String.valueOf(edtEmail.getText());
        String pass = String.valueOf(edtPass.getText());
        String confirmPass = String.valueOf(edtConfirmPass.getText());
        if (userName.isEmpty() || userName.length() < 5){
            edtUserName.setError("Nombre de usuario no válido");
        }else if (email.isEmpty() || !email.contains("@")){
            edtEmail.setError("Email no válido");
        }else if (pass.isEmpty() || pass.length() < 6){
            edtPass.setError("Contraseña no válida, mínimo 6 caracteres");
        }else if (confirmPass.isEmpty() || !confirmPass.equals(pass)){
            edtConfirmPass.setError("Contraseña no válida, no coincide");
        }
        else{
            mAuth.createUserWithEmailAndPassword(email,pass).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()){
                        Log.d("signup","createUserWithEmail: success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        addToFirestore(usertype);
                        user.sendEmailVerification();
                        mAuth.signOut();
                        finish();
                    }
                    else {
                        Log.w("signup","createUserWithEmail: failure" + task.getException().toString(), task.getException());
                        Toast.makeText(SignUpActivity.this, "Fallo al crear", Toast.LENGTH_SHORT).show();
                        try {
                            throw task.getException();
                        }catch (FirebaseAuthUserCollisionException e){
                            edtEmail.setError("Email ya registrado");
                            Toast.makeText(SignUpActivity.this, "", Toast.LENGTH_SHORT).show();
                        } catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
    }

    public void addToFirestore(String usertype) {
        Map<String,Object> user = new HashMap<>();
        user.put("nombre", edtUserName.getText().toString());
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

    public void signupEmpresa(View view) {
        usertype = "business";
        signup(usertype);
    }

    public void signupUsuario(View view) {
        usertype = "users";
        signup(usertype);
    }
}