package com.ky.thisbookis;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ky.thisbookis.data.Notice;

import java.util.ArrayList;

public class NoticeAdapter extends RecyclerView.Adapter<NoticeAdapter.Viewholder> {

    Context mContext;
    ArrayList<Notice> notices;

    public NoticeAdapter(Context mContext, ArrayList<Notice> notices) {
        this.mContext = mContext;
        this.notices = notices;
    }

    @NonNull
    @Override
    public Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.notice_item, parent, false);

        return new Viewholder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull Viewholder holder, int position) {
        Notice notice = notices.get(position);
        holder.setItem(notice);
    }

    @Override
    public int getItemCount() {
        return notices.size();
    }

    class Viewholder extends RecyclerView.ViewHolder{

        TextView titleTextView, timeTextView;
        LinearLayout noticeLinearLayout;

        Viewholder(@NonNull View itemView) {
            super(itemView);

            noticeLinearLayout = itemView.findViewById(R.id.notice_item_ll);
            titleTextView = itemView.findViewById(R.id.notice_item_title);
            timeTextView = itemView.findViewById(R.id.notice_item_time);

            noticeLinearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mContext, NoticeContentActivity.class);
                    intent.putExtra("notice", notices.get(getAdapterPosition()));
                    mContext.startActivity(intent);
                }
            });
        }

        private void setItem(Notice notice){
            titleTextView.setText(notice.getTitle());
            timeTextView.setText(notice.getTime());
        }
    }
}
