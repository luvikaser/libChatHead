package chathead;

import android.graphics.Bitmap;

import java.io.Serializable;

/**
 * Created by luvikaser on 13/03/2017.
 */

public class User implements Serializable {
    public int id;
    public int countMessage;
    public Bitmap avatar;

    public User(int id, int countMessage, Bitmap avatar) {
        this.id = id;
        this.countMessage = countMessage;
        this.avatar = avatar;
    }

    public boolean equals(User user){
        return (this.id == user.id);
    }
}
