package com.example.firebase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class PerfilActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private TextView txtEstado;
    private TextView txtActivacion;
    private Button btnChat;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);
        txtEstado = findViewById(R.id.txtVerificacion);
        txtActivacion = findViewById(R.id.txtPulsaParaActivar);
        btnChat = findViewById(R.id.btnChat);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        if (!user.isEmailVerified()){
            btnChat.setEnabled(false);
            txtActivacion.setVisibility(View.VISIBLE);
            txtEstado.setVisibility(View.VISIBLE);
        }
    }

    public void chatAccess(View view) {
        Intent intent = new Intent(this,ChatActivity.class);
        startActivity(intent);
    }

    public void logout(View view) {
        mAuth.signOut();
        finish();
    }

    public void activateAccount(View view) {
        FirebaseUser user = mAuth.getCurrentUser();
        user.sendEmailVerification().addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(PerfilActivity.this, "Email enviado al correo: " + user.getEmail(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}