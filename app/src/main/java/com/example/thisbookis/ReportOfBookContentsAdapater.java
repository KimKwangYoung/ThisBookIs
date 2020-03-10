package com.example.thisbookis;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.example.thisbookis.data.Report;
import com.example.thisbookis.data.User;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class ReportOfBookContentsAdapater extends RecyclerView.Adapter<ReportOfBookContentsAdapater.Viewholder> {

    Context mContext;
    private ArrayList<Report> reports;
    private LinkedHashMap<String, User> users;

    public ReportOfBookContentsAdapater(Context mContext, ArrayList<Report> reports, LinkedHashMap<String, User> users) {
        this.mContext = mContext;
        this.reports = reports;
        this.users = users;
    }

    @NonNull
    @Override
    public Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.book_contents_reports_item, parent, false);
        return new Viewholder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull Viewholder holder, int position) {
        Report report = reports.get(position);
        User user = users.get(report.getWriter());
        holder.setItem(report, user);
    }

    @Override
    public int getItemCount() {
        return reports.size();
    }

    public class Viewholder extends RecyclerView.ViewHolder implements View.OnClickListener{

        LinearLayout reportItemLinearLayout;
        ImageView profileImageView;
        TextView reportTitleTextView, nicknameTextView;
        View boundaryView;

        private Viewholder(@NonNull View itemView) {
            super(itemView);

            profileImageView = itemView.findViewById(R.id.book_contents_reports_item_profile_image_iv);
            reportTitleTextView = itemView.findViewById(R.id.book_contents_reports_item_report_title_tv);
            nicknameTextView = itemView.findViewById(R.id.book_contents_reports_item_nickname_tv);
            boundaryView = itemView.findViewById(R.id.book_contents_reports_item_line_boundary);
            reportItemLinearLayout = itemView.findViewById(R.id.book_contents_reports_item_ll);

            reportItemLinearLayout.setOnClickListener(this);
        }

        private void setItem(Report report, User user) {

            RequestOptions options = RequestOptions.bitmapTransform(new CircleCrop()).skipMemoryCache(true)
                    .error(R.drawable.ic_user_profile);

            Glide.with(mContext).load(user.getProfileURL()).apply(options).into(profileImageView);
            String nickname = user.getNickname() + " 님";
            nicknameTextView.setText(nickname);
            reportTitleTextView.setText(report.getTitle());

            if(getAdapterPosition() == reports.size()-1){
                boundaryView.setVisibility(View.GONE);
            }
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.book_contents_reports_item_ll:
                    Intent intent = new Intent(mContext, ReportActivity.class);
                    intent.putExtra("report", reports.get(getAdapterPosition()));
                    mContext.startActivity(intent);
                    break;
            }
        }
    }
}
