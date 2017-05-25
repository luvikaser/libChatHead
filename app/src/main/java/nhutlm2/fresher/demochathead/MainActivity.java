package nhutlm2.fresher.demochathead;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
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

   public void test(){
       if (chatHead == null){
           chatHead = new ChatHead(getApplicationContext());
       }
       chatHead.start();

       BitmapFactory.Options options = new BitmapFactory.Options();


       Bitmap bitmap2 = BitmapFactory.decodeResource(getResources(), R.drawable.pic3, options);
       User user2 = new User(2, 10, bitmap2);
       chatHead.push(user2);
       User user3 = new User(3, 17, bitmap2);
       chatHead.push(user3);
       ((Button)findViewById(R.id.text)).setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               if (chatHead == null){
                   chatHead = new ChatHead(getApplicationContext());
               }
               chatHead.start();

               BitmapFactory.Options options = new BitmapFactory.Options();
               options.inSampleSize = 4;
               Bitmap bitmap3 = BitmapFactory.decodeResource(getResources(), R.drawable.dismiss_big, options);
               User user3 = new User(2, 14, bitmap3);
               chatHead.push(user3);
           }
       });
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
