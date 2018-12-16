package com.ti3h.maingambar;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.net.URI;



public class MainGambar extends AppCompatActivity {

    //untuk Folder path firebase storage
    String Storage_Path = "All_Image_Uploads/";

    //root database name for firebase database
    public static final String Database_Path = "All_Image_Uploads_Database";

    Button ChooseButton, UploadButton, DisplayImageButton;

    EditText ImageName;

    ImageView SelectImage;

    Uri FilePathUri;

    StorageReference storageReference;
    DatabaseReference databaseReference;

    int Image_Request_Code = 7;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_gambar);

        storageReference = FirebaseStorage.getInstance().getReference();

        databaseReference = FirebaseDatabase.getInstance().getReference(Database_Path);

        ChooseButton = (Button)findViewById(R.id.ButtonChooseImage);
        UploadButton = (Button)findViewById(R.id.ButtonUploadImage);

        DisplayImageButton = (Button)findViewById(R.id.DisplayImagesButton);


        ImageName = (EditText)findViewById(R.id.ImageNameEditText);

        SelectImage = (ImageView)findViewById(R.id.ShowImageView);

        progressDialog = new ProgressDialog(MainGambar.this);

        ChooseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Silahkan Pilih Gambar"), Image_Request_Code);
            }
        });

        UploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UploadImageFileToFirebaseStorage();
            }
        });

        DisplayImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainGambar.this, DisplayImageActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Image_Request_Code && resultCode == RESULT_OK && data != null && data.getData() != null){

            FilePathUri = data.getData();

            try{
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), FilePathUri);
                SelectImage.setImageBitmap(bitmap);

                ChooseButton.setText("Gambar Terpilih");
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    public String GetFileExtension(Uri uri){
        ContentResolver contentResolver = getContentResolver();

        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();

        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));

    }

    public void UploadImageFileToFirebaseStorage(){
        if (FilePathUri != null){
            progressDialog.setTitle("Mengirim Gambar. . . .");

            progressDialog.show();

            StorageReference storageReference2nd = storageReference.child(Storage_Path + System.currentTimeMillis()+"."+GetFileExtension(FilePathUri));

            storageReference2nd.putFile(FilePathUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            String TempImageName = ImageName.getText().toString().trim();

                            progressDialog.dismiss();

                            Toast.makeText(getApplicationContext(), "Gambar Berhasil Upload", Toast.LENGTH_LONG ).show();

                            @SuppressWarnings("VisibleForTests")
                            ImageUploadInfo imageUploadInfo = new ImageUploadInfo(TempImageName, taskSnapshot.getDownloadUrl().toString());

                            String ImageUploadId = databaseReference.push().getKey();

                            databaseReference.child(ImageUploadId).setValue(imageUploadInfo);
                        }
                    })

                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            progressDialog.dismiss();

                            Toast.makeText(MainGambar.this, exception.getMessage(),Toast.LENGTH_LONG ).show();
                        }
                    })

                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.setTitle("Gambar Upload. . . ");
                        }
                    });

        }
        else {
            Toast.makeText(MainGambar.this, "Silahkan Pilih Gambar dan Isikan Kritik Saran", Toast.LENGTH_LONG).show();
        }
    }
}

