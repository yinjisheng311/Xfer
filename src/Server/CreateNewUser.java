//package Server;
//
//import com.google.firebase.database.DatabaseReference;
//
//import java.net.InetAddress;
//import java.net.UnknownHostException;
//import java.util.HashMap;
//import java.util.Map;
//
///**
// * Created by nicholas on 18-May-17.
// */
//public class CreateNewUser {
//
//    public static void main(String[] args) {
//        try {
//            ref.child("Nic").setValue(new User(true, InetAddress.getLocalHost().toString().split("/")[1]));
//        } catch (UnknownHostException e) {
//            e.printStackTrace();
//        }
//
//        Runnable test = new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    ref.child("Ji").setValue(new User(true , InetAddress.getLocalHost().toString().split("/")[1]));
//                } catch (UnknownHostException e) {
//                    e.printStackTrace();
//                }
//
//                try {
//                    Thread.sleep(3000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//
//                DatabaseReference userRef = ref.child("Ji");
//                Map<String, Object> userUpdates = new HashMap<String, Object>();
//                userUpdates.put("IPAddress", "123.456.32.8");
//                userUpdates.put("online",true);
//
//                userRef.updateChildren(userUpdates);
//            }
//        };
//
//        Thread tester = new Thread(test);
//        tester.start();
//
////        Runnable anotherTest = new Runnable() {
////            @Override
////            public void run() {
////                while(true){
////                    System.out.println(onlineUsers.toString());
////                    try {
////                        Thread.sleep(1000);
////                    } catch (InterruptedException e) {
////                        e.printStackTrace();
////                    }
////                }
////            }
////        };
////
////        new Thread(anotherTest).start();
//
////        while(true){ // This is to make sure this thread never terminates, destroying listener threads
////            Thread.sleep(Long.MAX_VALUE);
////        }
//    }
//}
