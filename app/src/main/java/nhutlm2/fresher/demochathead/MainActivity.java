package nhutlm2.fresher.demochathead;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import chathead.ChatHead;
import chathead.User;


import static android.view.View.GONE;
import static android.view.View.SYSTEM_UI_FLAG_FULLSCREEN;
import static android.view.View.VISIBLE;

/**
 * Created by luvikaser on 01/03/2017.
 */

public class MainActivity extends AppCompatActivity {
    public static int ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE= 5469;
    private ChatHead chatHead = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermission();
    }

    final Handler handler = new Handler();
    Runnable mLongPressed = new Runnable() {
        public void run() {
            BitmapFactory.Options options = new BitmapFactory.Options();


            Bitmap bitmap2 = BitmapFactory.decodeResource(getResources(), R.drawable.pic3, options);
            User user2 = new User(2, 2, bitmap2, false, "dfgdfsgdfs");
            chatHead.push(user2, false);
            handler.postDelayed(mLongPressed1, 3000);
        }
    };
    Runnable mLongPressed1 = new Runnable() {
        public void run() {
            BitmapFactory.Options options = new BitmapFactory.Options();


            Bitmap bitmap2 = BitmapFactory.decodeResource(getResources(), R.drawable.pic3, options);
            User user2 = new User(3, 3, bitmap2, false, "ffffffffffffff");
            chatHead.push(user2, false);
            handler.postDelayed(mLongPressed2, 3000);
        }
    };
    Runnable mLongPressed2 = new Runnable() {
        public void run() {
            BitmapFactory.Options options = new BitmapFactory.Options();


            Bitmap bitmap2 = BitmapFactory.decodeResource(getResources(), R.drawable.pic3, options);
            User user2 = new User(4, 4, bitmap2, false, "ffffffffffffff");
            chatHead.push(user2, false);
            handler.postDelayed(mLongPressed3, 3000);

        }
    };
    Runnable mLongPressed3 = new Runnable() {
        public void run() {
            BitmapFactory.Options options = new BitmapFactory.Options();


            Bitmap bitmap2 = BitmapFactory.decodeResource(getResources(), R.drawable.pic3, options);
            User user2 = new User(5, 5, bitmap2, false, "ffffffffffffff");
            chatHead.push(user2, false);
            handler.postDelayed(mLongPressed4, 3000);

        }
    };
        Runnable mLongPressed4 = new Runnable() {
            public void run() {
                BitmapFactory.Options options = new BitmapFactory.Options();


                Bitmap bitmap2 = BitmapFactory.decodeResource(getResources(), R.drawable.pic3, options);
                User user2 = new User(6, 6, bitmap2, false, "ffffffffffffff");
                chatHead.push(user2, false);
            }
        };
   public void test(){
       if (chatHead == null){
           chatHead = new ChatHead(getApplicationContext());
       }
       chatHead.start();

       BitmapFactory.Options options = new BitmapFactory.Options();


       Bitmap bitmap2 = BitmapFactory.decodeResource(getResources(), R.drawable.dismiss_big, options);
       User user2 = new User(0, 0, bitmap2, true, "");
       chatHead.push(user2, false);

       final Bitmap bitmap3 = BitmapFactory.decodeResource(getResources(), R.drawable.pic3, options);
       User user3 = new User(1, 1, bitmap3, false, "23423");
       chatHead.push(user3, false);
//       User user4 = new User(4, 2, bitmap3, false, "32432");
//       chatHead.push(user4, false);
//       User user5 = new User(5, 3, bitmap3, false, "32432423");
//       chatHead.push(user5, false);

       ((Button)findViewById(R.id.text)).setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               User user5 = new User(7, 6, bitmap3, false, "abcdfsadfdsafdsafasdf");
               chatHead.push(user5, false);

           }
       });
       handler.postDelayed(mLongPressed, 5000);


   }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE) {
            if (Settings.canDrawOverlays(this)) {
                test();
            }
        }



    }
    public void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE);
            } else{
                test();
            }
        } else{
            test();
        }
    }
}
