package com.ky.thisbookis.fragment;


import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ky.thisbookis.KakaoApiClient;
import com.ky.thisbookis.BaseApplication;
import com.ky.thisbookis.R;
import com.ky.thisbookis.RecentSearchesAdapter;
import com.ky.thisbookis.SearchAdapter;
import com.ky.thisbookis.data.SearchResult;
import com.ky.thisbookis.data.User;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class SearchFragment extends Fragment implements TextView.OnEditorActionListener {

    static final String TAG = "SearchFragment";

    private EditText searchEditText;
    private RecyclerView searchRecyclerView, recentSearchesRecyclerView;
    private LinearLayout recentSearchLinearLayout;
    private ProgressBar progressBar;

    private InputMethodManager imm;

    private int index;
    private int percent = 0;

    private User user;

    private List<SearchResult.Document> searchResults = new ArrayList<>();

    private ViewGroup rootView;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
         rootView = (ViewGroup) inflater.inflate(R.layout.fragment_search, container, false);

        initData();
        initView(rootView);


        return rootView;

    }

    private void initView(ViewGroup rootView){
        searchEditText = rootView.findViewById(R.id.search_keyword_et);
        searchRecyclerView = rootView.findViewById(R.id.search_rv);
        progressBar = rootView.findViewById(R.id.search_pb);
        recentSearchesRecyclerView = rootView.findViewById(R.id.search_recent_searches_rv);
        recentSearchLinearLayout = rootView.findViewById(R.id.search_recent_searches_ll);

        searchEditText.setOnEditorActionListener(this);

        connectRecentSearchesAdapter();
    }

    private void initData(){
        user = BaseApplication.userData;
        if(getActivity() != null) {
            imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        }

    }

    /**
     * 검색시 뒤에 페이지가 존재 시 계속해서 document를 받아와 List에 추가
     * 마지막 페이지까지 불러온 뒤 받아온 List를 adpater에 전달하여 View보여주기
     * progressbar로 진행 상황보여주기
     * **/
    private void search(){
        String keyword = searchEditText.getText().toString();
        if(keyword.equals("")){
            BaseApplication.showDialog(getActivity(), "검색어가 없습니다.", null);
            return;
        }
        String apiKey = getString(R.string.kakao_api_key);
        Call<SearchResult> call = KakaoApiClient.getInstance().searchService.getBookList(apiKey,keyword,50, index+=1);

        Callback<SearchResult> callback = new Callback<SearchResult>() {
            @Override
            public void onResponse(Call<SearchResult> call, Response<SearchResult> response) {
                if(response.isSuccessful()){
                    SearchResult searchResult = response.body();
                    if(searchResult.getDocuments().size() == 0){
                        Toast.makeText(getActivity(), "검색된 결과가 없습니다.", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        Log.d("SearchFragment", call.request().url().toString());
                        return;
                    }
                    if(searchResult.getMeta().getPageable_count() > 50){
                        int totalCount = searchResult.getMeta().getPageable_count(); // 284
                        int size = 50;
                        double count = totalCount/size; //6
                        double celiCount = Math.ceil(count);
                        double count2 = 100/celiCount;
                        int count3 = (int)Math.ceil(count2);
                        percent += count3;
                        progressBar.setProgress(percent);
                    }
                    Log.d("SearchFragment", call.request().url().toString());
                    searchResults.addAll(searchResult.getDocuments());
                    if(!searchResult.getMeta().getIs_end()){
                        search();
                    }else{
                        progressBar.setProgress(100);
                        progressBar.setVisibility(View.GONE);

                        connectSearchResultAdapter(searchResults);
                    }



                }else{
                    Log.e("SearchFragment", response.toString() + " " + call.request().url().toString());
                }
            }

            @Override
            public void onFailure(Call<SearchResult> call, Throwable t) {
                Log.e(TAG, "search() : 검색 실페" + t.getMessage());
                BaseApplication.showErrorToast(getActivity(), "오류가 발생했습니다. 다시 시도하여 주세요");
            }
        };

        call.enqueue(callback);
    }

    private void connectSearchResultAdapter(List<SearchResult.Document> documents){
        recentSearchLinearLayout.setVisibility(View.GONE);
        searchRecyclerView.setVisibility(View.VISIBLE);
        LinearLayoutManager manager = new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false);
        searchRecyclerView.setLayoutManager(manager);
        SearchAdapter searchAdapter = new SearchAdapter(getActivity(), documents);
        searchRecyclerView.setAdapter(searchAdapter);
    }



    private void connectRecentSearchesAdapter(){
        if(user.getRecentSearches() == null || searchRecyclerView.getVisibility() == View.VISIBLE){
            recentSearchLinearLayout.setVisibility(View.GONE);
            Log.e(TAG, "null");
            return;
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), RecyclerView.HORIZONTAL, false);
        recentSearchesRecyclerView.setLayoutManager(layoutManager);
        RecentSearchesAdapter adapter = new RecentSearchesAdapter(getActivity(),user.getRecentSearches());
        recentSearchesRecyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
        if(i == EditorInfo.IME_ACTION_SEARCH){
            searchResults.clear();
            percent = 0;
            progressBar.setVisibility(View.VISIBLE);
            Log.d("SearchFragment", searchResults.size() + " ");
            index = 0;
            search();
            imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
            return true;
        }

        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        initData();
        initView(rootView);
    }
}
