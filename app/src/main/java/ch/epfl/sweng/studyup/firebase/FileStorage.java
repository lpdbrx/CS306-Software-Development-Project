package ch.epfl.sweng.studyup.firebase;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import ch.epfl.sweng.studyup.utils.Utils;

import java.io.File;

public class FileStorage {

    public static StorageReference storageRef = FirebaseStorage.getInstance().getReference();

    private static void uploadFile(String dirName, File file) {

        Uri fileURI = Uri.fromFile(file);
        String destFilePath = dirName + "/" + fileURI.getLastPathSegment();

        StorageReference fileRef = storageRef.child(destFilePath);
        UploadTask uploadTask = fileRef.putFile(fileURI);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                System.out.println("Unable to upload problem image");
                exception.printStackTrace();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                System.out.println("Successfully uploaded problem image");
            }
        });
    }


    public static void uploadProblemImage(File file) {
        uploadFile(Utils.question_images_directory_name, file);
    }

    public static void uploadProfilePicture(File file) {
        uploadFile(Utils.profile_pictures_directory_name, file);
    }

    public static StorageReference getProblemImageRef(Uri fileURI) {

        StorageReference fileRef = storageRef.child(Utils.question_images_directory_name + "/" + fileURI.getLastPathSegment());
        return fileRef;
    }

    public static StorageReference getProfilePictureRef(String sciper) {

        StorageReference fileRef = storageRef.child(Utils.profile_pictures_directory_name + "/" + sciper + ".png");
        return fileRef;
    }

    public static void downloadProfilePicture(String sciper, final ImageView image_view) {

        StorageReference ref = getProfilePictureRef(sciper);

        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                //Pass the URL to Picasso to download and show in ImageView
                Picasso.get().load(uri.toString()).into(image_view);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                exception.printStackTrace();
            }
        });
    }
}