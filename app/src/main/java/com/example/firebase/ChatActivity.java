package com.example.firebase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.firebase.chat.AdapterMensajes;
import com.example.firebase.chat.Mensaje;
import com.example.firebase.hora.MensajeEnviar;
import com.example.firebase.hora.MensajeRecibir;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private CircleImageView fotoPerfil;
    private TextView nombre;
    private RecyclerView rvMensajes;
    private EditText edtMensaje;
    private Button btnEnviar;
    private ImageButton btnEnviarFoto;
    private static final int PHOTO_SEND = 1;
    private static final int PHOTO_PERFIL = 2;
    private String fotoPerfilCadena;
    private AdapterMensajes adapter;
    private FirebaseDatabase database;
    private DatabaseReference databaseReference;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        fotoPerfil =(CircleImageView) findViewById(R.id.fotoPerfil);
        nombre = (TextView) findViewById(R.id.nombreUsuario);
        rvMensajes =(RecyclerView) findViewById(R.id.rvMensajes);
        edtMensaje = (EditText) findViewById(R.id.edtMensaje);
        btnEnviar =(Button) findViewById(R.id.btnEnviar);
        btnEnviarFoto = (ImageButton) findViewById(R.id.btnEnviarFoto);
        adapter = new AdapterMensajes(this);
        fotoPerfilCadena = "";
        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("chat"); //Sala de chat
        storage = FirebaseStorage.getInstance();
        LinearLayoutManager llm = new LinearLayoutManager(this);
        rvMensajes.setLayoutManager(llm);
        rvMensajes.setAdapter(adapter);
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                setScrollBar();
            }
        });
        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                MensajeRecibir m = snapshot.getValue(MensajeRecibir.class);
                adapter.addMensaje(m);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        fotoPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY,true);
                startActivityForResult(intent.createChooser(intent,"Selecciona una foto"),PHOTO_PERFIL);
            }
        });
    }


    private void setScrollBar(){
        rvMensajes.scrollToPosition(adapter.getItemCount()-1);
    }
    public void enviarMensaje(View view) {
        String mensaje = String.valueOf(edtMensaje.getText());
        String nombreUsuario = String.valueOf(nombre.getText());
        databaseReference.push().setValue(new MensajeEnviar(mensaje,nombreUsuario,"","1", ServerValue.TIMESTAMP));
        edtMensaje.setText("");
    }

    public void enviarFoto(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/jpeg");
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY,true);
        startActivityForResult(intent.createChooser(intent,"Selecciona una foto"),PHOTO_SEND);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PHOTO_SEND && resultCode == RESULT_OK){
            Uri u = data.getData();
            storageReference = storage.getReference("imagenes_chat");
            final StorageReference fotoReferencia = storageReference.child(u.getLastPathSegment());
            fotoReferencia.putFile(u).addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    StorageMetadata storageMetadata = taskSnapshot.getMetadata();
                    StorageReference storageReference = storageMetadata.getReference();
                    Task<Uri> uriTask = storageReference.getDownloadUrl();
                    uriTask.addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()){
                                Uri u = uriTask.getResult();
                                MensajeEnviar m = new MensajeEnviar("Kevin te ha enviado una foto",nombre.getText().toString(),u.toString(),fotoPerfilCadena,"2",ServerValue.TIMESTAMP);
                                databaseReference.push().setValue(m);
                            }else{
                                Exception e = task.getException();
                            }
                        }
                    });


                }
            });
        }else if (requestCode == PHOTO_PERFIL && requestCode == RESULT_OK){
            Uri u = data.getData();
            storageReference = storage.getReference("fotos_perfil");
            final StorageReference fotoReferencia = storageReference.child(u.getLastPathSegment());
            fotoReferencia.putFile(u).addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    StorageMetadata storageMetadata = taskSnapshot.getMetadata();
                    StorageReference storageReference = storageMetadata.getReference();
                    Task<Uri> uriTask = storageReference.getDownloadUrl();
                    uriTask.addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()){
                                Uri u = uriTask.getResult();
                                fotoPerfilCadena = u.toString();
                                MensajeEnviar m = new MensajeEnviar("Kevin ha actualizado su foto de perfil",nombre.getText().toString(),u.toString(),fotoPerfilCadena,"2",ServerValue.TIMESTAMP);
                                databaseReference.push().setValue(m);
                                Glide.with(ChatActivity.this).load(u.toString()).into(fotoPerfil);
                            }else{
                                Exception e = task.getException();
                            }
                        }
                    });


                }
            });
        }
    }
}