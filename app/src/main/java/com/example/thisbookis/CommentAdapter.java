package com.example.thisbookis;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.thisbookis.data.Comment;
import com.example.thisbookis.data.User;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.Map;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.Viewholder> {

    Context mContext;
    ArrayList<Comment> comments;
    Map<String, User> users;

    public CommentAdapter(Context mContext, ArrayList<Comment> comments, Map<String, User> users) {
        this.mContext = mContext;
        this.comments = comments;
        this.users = users;
    }

    @NonNull
    @Override
    public Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.comment_item, parent, false);
        return new Viewholder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull Viewholder holder, int position) {
        Comment comment = comments.get(position);
        User user = users.get(comment.getWriter());
        holder.setItem(comment, user);
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    public class Viewholder extends RecyclerView.ViewHolder{

        ImageView profileImageView;
        TextView nicknameTextView, contentsTextView, addTimeTextView;
        User user;

        public Viewholder(@NonNull View itemView) {
            super(itemView);

            profileImageView = itemView.findViewById(R.id.comment_item_profile_image_iv);
            nicknameTextView = itemView.findViewById(R.id.comment_item_nickname_tv);
            contentsTextView = itemView.findViewById(R.id.comment_item_comment_tv);
            addTimeTextView = itemView.findViewById(R.id.comment_item_add_time_tv);


        }

        public void setItem(Comment comment, User user){
            Glide.with(mContext).load(user.getProfileURL()).apply(BaseApplication.profileImageOptions)
                    .into(profileImageView);

            nicknameTextView.setText(user.getNickname());
            contentsTextView.setText(comment.getContent());
            addTimeTextView.setText(comment.getTimeAddComments());
        }
    }
}
