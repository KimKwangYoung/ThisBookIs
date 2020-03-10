package com.example.thisbookis;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.thisbookis.data.Draft;
import com.example.thisbookis.data.Report;
import com.example.thisbookis.data.SearchResult;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DraftListAdapter extends RecyclerView.Adapter<DraftListAdapter.Viewholder> {

    static final String TAG = "DraftListAdapter";
    Context mContext;
    ArrayList<Draft> drafts;

    public DraftListAdapter(Context mContext, ArrayList<Draft> drafts) {
        this.mContext = mContext;
        this.drafts = drafts;
    }

    @NonNull
    @Override
    public Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.draft_item, parent, false);
        return new Viewholder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull Viewholder holder, int position) {
        Draft draft = drafts.get(position);
        holder.setItem(draft);
    }

    @Override
    public int getItemCount() {
        return drafts.size();
    }

    class Viewholder extends RecyclerView.ViewHolder implements View.OnClickListener{

        LinearLayout draftLinearLayout;
        ImageView removeButton;
        TextView bookTitleTextView, draftTitleTextView, saveTimeTextView;

        private Viewholder(@NonNull View itemView) {
            super(itemView);

            draftLinearLayout = itemView.findViewById(R.id.draft_item_ll);
            removeButton = itemView.findViewById(R.id.draft_item_remove_btn);
            bookTitleTextView = itemView.findViewById(R.id.draft_item_book_title_tv);
            draftTitleTextView = itemView.findViewById(R.id.draft_item_title_tv);
            saveTimeTextView = itemView.findViewById(R.id.draft_item_time_tv);

            draftLinearLayout.setOnClickListener(this);
            removeButton.setOnClickListener(this);
        }

        private void setItem(Draft draft){
            bookTitleTextView.setText(draft.getBookTitle());
            draftTitleTextView.setText(draft.getTitle());
            saveTimeTextView.setText(draft.getSaveTime());
        }

        private void searchBookDocument(){
            Draft draft = drafts.get(getAdapterPosition());
            if(BaseApplication.userData.getReports() != null){
                for(Report r : BaseApplication.userData.getReports().values()){
                    if(r.getBookISBN().equals(draft.getBookISBN())){
                        BaseApplication.showDialog(mContext, "이미 해당책에 대한 독후감이 있습니다.", null);
                        return;
                    }
                }
            }

            String isbn = drafts.get(getAdapterPosition()).getBookISBN();
            String bookTitle = drafts.get(getAdapterPosition()).getBookTitle();
            String keyword = isbn.substring(isbn.lastIndexOf(" ") + 1);
            String authorizationKey = mContext.getString(R.string.kakao_api_key);
            Call<SearchResult> call = KakaoApiClient.getInstance().searchService.getBookList(authorizationKey, keyword, 50, 1);
            Callback<SearchResult> callback = new Callback<SearchResult>() {
                @Override
                public void onResponse(Call<SearchResult> call, Response<SearchResult> response) {
                    if(response.isSuccessful()) {
                        SearchResult searchResult = response.body();
                        List<SearchResult.Document> documentList = searchResult.getDocuments();
                        SearchResult.Document bookDocument = null;
                        for(SearchResult.Document d : documentList){
                            if(d.getTitle().equals(bookTitle)){
                                bookDocument = d;
                                break;
                            }
                        }

                        Intent intent = new Intent(mContext, WriteReportActivity.class);
                        intent.putExtra("isDraft", true);
                        intent.putExtra("draft", drafts.get(getAdapterPosition()));
                        intent.putExtra("book", bookDocument);
                        mContext.startActivity(intent);

                    }else{
                        BaseApplication.showErrorToast(mContext, "책 정보를 읽어오지 못했습니다. 다시 시도하여 주세요");
                        Log.e("MyBooksAdapter", "Retrofit Error :: " + response.errorBody());
                    }
                }

                @Override
                public void onFailure(Call<SearchResult> call, Throwable t) {

                }
            };

            call.enqueue(callback);
        }

        private void removeDraft(){
            LinkedHashMap<String, Draft> myDrafts = BaseApplication.userData.getTemporaryStorages();
            myDrafts.remove(drafts.get(getAdapterPosition()).getDraftKey());

            DatabaseReference draftRef = FirebaseDatabase.getInstance().getReference()
                    .child(mContext.getString(R.string.firebase_user_data_key))
                    .child(BaseApplication.userData.getUserId())
                    .child("temporaryStorages");

            draftRef.setValue(myDrafts).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    drafts.remove(getAdapterPosition());
                    notifyItemRemoved(getAdapterPosition());
                    BaseApplication.userData.setTemporaryStorages(myDrafts);
                    Log.d(TAG, "removeDraft() : 임시저장본 삭제 완료");

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "removeDraft() : 임시저장본 삭제 실패");
                }
            });
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.draft_item_ll:
                    searchBookDocument();
                    break;
                case R.id.draft_item_remove_btn:
                    BaseApplication.showDialog(mContext, "삭제하시겠습니까?", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            removeDraft();
                        }
                    });
                    break;
            }
        }
    }
}
