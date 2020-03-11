package com.ky.thisbookis;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ky.thisbookis.data.Report;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class MyReportsAdapter extends RecyclerView.Adapter<MyReportsAdapter.Viewholder> {

    public static final String TAG = "MyReportsAdapter";

    Context mContext;
    private ArrayList<Report> reportsList;

    public MyReportsAdapter(Context mContext, ArrayList<Report> reportsList) {
        this.mContext = mContext;
        this.reportsList = reportsList;
    }

    @NonNull
    @Override
    public Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.my_report_item, parent, false);


        return new Viewholder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull Viewholder holder, int position) {
        Report report = reportsList.get(position);
        holder.setItem(report);
    }

    @Override
    public int getItemCount() {
        return reportsList.size();
    }

    class Viewholder extends RecyclerView.ViewHolder implements View.OnClickListener, PopupMenu.OnMenuItemClickListener {

        LinearLayout linearLayout;
        ImageView thumbnailImageView, menuButton;
        TextView bookDataTextView, titleTextView, contentsTextView, shareTextView;
        ImageView lockIconImageView;

        String bookInfo;

        private Viewholder(@NonNull View itemView) {
            super(itemView);

            linearLayout = itemView.findViewById(R.id.my_report_item_ll);
            thumbnailImageView = itemView.findViewById(R.id.my_reports_thumbnail_iv);
            bookDataTextView = itemView.findViewById(R.id.my_reports_book_title_authors_tv);
            titleTextView = itemView.findViewById(R.id.my_reports_title_tv);
            contentsTextView = itemView.findViewById(R.id.my_reports_contents_tv);
            shareTextView = itemView.findViewById(R.id.my_report_item_share_tv);
            lockIconImageView = itemView.findViewById(R.id.my_report_item_share_icon_iv);
            menuButton = itemView.findViewById(R.id.my_report_item_menu_btn);

            linearLayout.setOnClickListener(this);
            menuButton.setOnClickListener(this);

        }

        private void setItem(Report report){

            Glide.with(mContext).asBitmap().override(300, 400).error(R.drawable.ic_book)
                    .load(report.getBookThumbnail()).into(thumbnailImageView);

            String authors = BaseApplication.replaceString(report.getBookAuthors());
            bookInfo = report.getBookTitle() + " / " + authors;
            bookDataTextView.setText(bookInfo);

            titleTextView.setText(report.getTitle());
            contentsTextView.setText(report.getContents());

            if(report.isShouldShare()){
                shareTextView.setText("공개");
                shareTextView.setTextColor(mContext.getResources().getColor(R.color.green, null));

                lockIconImageView.setImageDrawable(mContext.getDrawable(R.drawable.ic_unlock));
            }else{
                shareTextView.setText("비공개");
                shareTextView.setTextColor(mContext.getResources().getColor(R.color.gray, null));

                lockIconImageView.setImageDrawable(mContext.getDrawable(R.drawable.ic_lock));
            }

        }

        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.my_report_item_ll:
                    Report report = reportsList.get(getAdapterPosition());
                    Intent intent = new Intent(mContext, ReportActivity.class);
                    intent.putExtra("report", report);
                    mContext.startActivity(intent);
                    break;
                case R.id.my_report_item_menu_btn:
                    PopupMenu popupMenu = new PopupMenu(mContext, view);
                    ((Activity)mContext).getMenuInflater().inflate(R.menu.my_report_menu, popupMenu.getMenu());
                    popupMenu.setOnMenuItemClickListener(this);
                    popupMenu.show();
                    break;
            }
        }

        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch (menuItem.getItemId()){
                case R.id.my_report_modify:
                    Intent intent = new Intent(mContext, WriteReportActivity.class);
                    intent.putExtra("report", reportsList.get(getAdapterPosition()));
                    intent.putExtra("modify", true);
                    mContext.startActivity(intent);
                    break;
                case R.id.my_report_remove:
                    BaseApplication.showDialog(mContext, "독후감을 삭제하시면 복구가 불가능 합니다.\n삭제하시겠습니까?", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            removeReportOfUser();
                        }
                    });
                    break;
            }
            return false;
        }

        /**
         * 유저 부분에 저장되어있는 독후감 삭제
         */
        private void removeReportOfUser(){
            Report r = reportsList.get(getAdapterPosition());
            LinkedHashMap<String, Report> myReports = BaseApplication.userData.getReports();
            myReports.remove(r.getReportKey());

            DatabaseReference reportRef = FirebaseDatabase.getInstance().getReference()
                    .child(mContext.getString(R.string.firebase_user_data_key)).child(BaseApplication.userData.getUserId())
                    .child("reports");

            reportRef.setValue(myReports).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    reportsList.remove(r);
                    if(r.isShouldShare()){
                        removeReportOfBook(r);
                        BaseApplication.userData.setReports(myReports);
                        return;
                    }
                    Log.d(TAG, "removeReportOfUser() : 유저 영역 독후감 지우기 성공");
                    BaseApplication.showCompleteToast(mContext, "독후감을 삭제하였습니다.");
                    notifyItemRemoved(getAdapterPosition());
                    BaseApplication.userData.setReports(myReports);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "removeReportOfUser() : 유저 영역 독후감 지우기 실패 " + e.getMessage());
                }
            });
        }

        /**
         * 책 부분에 저장되어 있는 독후감 삭제
         * @param r : 삭제할 독후감
         */
        private void removeReportOfBook(Report r) {
            DatabaseReference reportRef = FirebaseDatabase.getInstance().getReference()
                    .child(mContext.getString(R.string.firebase_book_data_key)).child(r.getBookISBN())
                    .child("reportsOfBook").child(r.getReportKey());

            reportRef.setValue(null).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d(TAG, "removeReportOfBook() : 책 영역 독후감 지우기 성공");
                    BaseApplication.showCompleteToast(mContext, "독후감을 삭제하였습니다.");
                    notifyItemRemoved(getAdapterPosition());
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "removeReportOfBook() : 책 영역 독후감 지우기 실패 " + e.getMessage());
                }
            });
        }
    }
}
