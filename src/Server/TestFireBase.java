//package Server;
//
//import com.google.firebase.FirebaseApp;
//import com.google.firebase.FirebaseOptions;
//import com.google.firebase.auth.FirebaseCredentials;
//import com.google.firebase.database.DataSnapshot;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.database.ValueEventListener;
//
//import java.io.FileInputStream;
//
///**
// * Created by nicholas on 15-May-17.
// */
//public class TestFireBase {
//
//    public static void main(String[] args) {
//        // Fetch the service account key JSON file contents
//        FileInputStream serviceAccount = new FileInputStream("path/to/serviceAccountCredentials.json");
//
//// Initialize the app with a service account, granting admin privileges
//        FirebaseOptions options = new FirebaseOptions.Builder()
//                .setCredential(FirebaseCredentials.fromCertificate(serviceAccount))
//                .setDatabaseUrl("https://databaseName.firebaseio.com")
//                .build();
//        FirebaseApp.initializeApp(options);
//
//// As an admin, the app has access to read and write all data, regardless of Security Rules
//        DatabaseReference ref = FirebaseDatabase
//                .getInstance()
//                .getReference("restricted_access/secret_document");
//        ref.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                Object document = dataSnapshot.getValue();
//                System.out.println(document);
//            }
//        });
//    }
//}
