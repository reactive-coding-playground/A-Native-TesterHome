package com.testerhome.nativeandroid.views;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.ShareActionProvider;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.testerhome.nativeandroid.Config;
import com.testerhome.nativeandroid.R;
import com.testerhome.nativeandroid.auth.TesterHomeAccountService;
import com.testerhome.nativeandroid.fragments.MarkdownFragment;
import com.testerhome.nativeandroid.fragments.TopicReplyFragment;
import com.testerhome.nativeandroid.models.CollectTopicResonse;
import com.testerhome.nativeandroid.models.PraiseEntity;
import com.testerhome.nativeandroid.models.TesterUser;
import com.testerhome.nativeandroid.models.TopicDetailEntity;
import com.testerhome.nativeandroid.models.TopicDetailResponse;
import com.testerhome.nativeandroid.networks.TesterHomeApi;
import com.testerhome.nativeandroid.utils.StringUtils;
import com.testerhome.nativeandroid.views.base.BackBaseActivity;

import butterknife.Bind;
import butterknife.OnClick;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by vclub on 15/9/17.
 */
public class TopicDetailActivity extends BackBaseActivity {

    private String mTopicId;
    private ShareActionProvider mShareActionProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic_detail);

        setCustomTitle("帖子详情");

        if (getIntent().hasExtra("topic_id")) {
            mTopicId = getIntent().getStringExtra("topic_id");

            setupView();

            loadInfo();
        } else {
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_topic_detail, menu);

        MenuItem item = menu.findItem(R.id.action_share);

        mShareActionProvider = new ShareActionProvider(this);
        MenuItemCompat.setActionProvider(item, mShareActionProvider);

        if (!TextUtils.isEmpty(mTopicId)){
            updateTopicShareUrl(String.format("https://testerhome.com/topics/%s", mTopicId));
        }

        return super.onCreateOptionsMenu(menu);
    }

    public void updateTopicShareUrl(String topicUrl) {
        if (mShareActionProvider != null){
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, topicUrl);
            mShareActionProvider.setShareIntent(intent);
        }
    }

    @Bind(R.id.tab_layout)
    TabLayout tabLayoutTopicsTab;

    @Bind(R.id.view_pager)
    ViewPager viewPagerTopics;

    private TopicDetailPagerAdapter mAdapter;

    private void setupView(){
        mAdapter = new TopicDetailPagerAdapter(getSupportFragmentManager());
        viewPagerTopics.setAdapter(mAdapter);

        tabLayoutTopicsTab.setupWithViewPager(viewPagerTopics);
        tabLayoutTopicsTab.setTabsFromPagerAdapter(mAdapter);
    }

    private MarkdownFragment mMarkdownFragment;

    public class TopicDetailPagerAdapter extends FragmentPagerAdapter {

        private String[] typeName = {"帖子","评论"};
        private String[] typeValue = {Config.TOPICS_TYPE_RECENT,
                Config.TOPICS_TYPE_POPULAR};

        public TopicDetailPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0){
                if (mMarkdownFragment == null){
                    mMarkdownFragment = new MarkdownFragment();
                }
                return mMarkdownFragment;
            }else
                return TopicReplyFragment.newInstance(mTopicId);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return typeName[position];
        }

        @Override
        public int getCount() {
            return typeName.length;
        }
    }

    @Bind(R.id.tv_detail_title)
    TextView tvDetailTitle;
    @Bind(R.id.sdv_detail_user_avatar)
    SimpleDraweeView sdvDetailUserAvatar;
    @Bind(R.id.tv_detail_name)
    TextView tvDetailName;
    @Bind(R.id.tv_detail_username)
    TextView tvDetailUsername;
    @Bind(R.id.tv_detail_publish_date)
    TextView tvDetailPublishDate;
    @Bind(R.id.tv_detail_replies_count)
    TextView tvDetailRepliesCount;


    private void loadInfo() {
        TesterHomeApi.getInstance().getTopicsService().getTopicById(mTopicId,
                new Callback<TopicDetailResponse>() {
                    @Override
                    public void success(TopicDetailResponse topicDetailResponse, Response response) {
                        TopicDetailEntity topicEntity = topicDetailResponse.getTopic();
                        tvDetailTitle.setText(topicEntity.getTitle());
                        tvDetailName.setText(topicEntity.getNode_name().concat("."));
                        tvDetailUsername.setText(TextUtils.isEmpty(topicEntity.getUser().getLogin()) ?
                                "匿名用户" : topicEntity.getUser().getName());
                        tvDetailPublishDate.setText(StringUtils.formatPublishDateTime(
                                topicEntity.getCreated_at()).concat(".")
                                .concat(topicEntity.getHits()).concat("次阅读"));
                        sdvDetailUserAvatar.setImageURI(Uri.parse(Config.getImageUrl(topicEntity.getUser().getAvatar_url())));

                        // 用户回复数
                        tvDetailRepliesCount.setText(String.format("%s条回复", topicEntity.getReplies_count()));

                        if (mMarkdownFragment != null){
                            mMarkdownFragment.showWebContent(topicDetailResponse.getTopic().getBody_html());
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {

                    }
                });
    }

    private TesterUser mCurrentUser;
    @Bind(R.id.tv_detail_collect)
    TextView tvDetailCollect;
    @Bind(R.id.tv_detail_praise)
    TextView tvDetailPraise;

    @OnClick(R.id.tv_detail_collect)
    void onDetailCollectClick() {
        if (mCurrentUser == null) {
            mCurrentUser = TesterHomeAccountService.getInstance(this).getActiveAccountInfo();
        }
        TesterHomeApi.getInstance().getTopicsService().collectTopic(mTopicId, mCurrentUser.getAccess_token(), new Callback<CollectTopicResonse>() {
            @Override
            public void success(CollectTopicResonse collectTopicResonse, Response response) {
                if (collectTopicResonse.getOk() == 1) {
                    Toast.makeText(TopicDetailActivity.this, "收藏成功", Toast.LENGTH_SHORT).show();
                    tvDetailCollect.setCompoundDrawablesWithIntrinsicBounds(R.drawable.btn_bookmark_off, 0, 0, 0);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(TopicDetailActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }


    @OnClick(R.id.tv_detail_praise)
    void onDetailPraiseClick() {
        if (mCurrentUser == null) {
            mCurrentUser = TesterHomeAccountService.getInstance(this).getActiveAccountInfo();
        }
        TesterHomeApi.getInstance().getTopicsService().praiseTopic(Config.PRAISE_TOPIC, mTopicId, mCurrentUser.getAccess_token(), new Callback<PraiseEntity>() {
            @Override
            public void success(PraiseEntity praiseEntity, Response response) {
                Toast.makeText(TopicDetailActivity.this, "点赞成功", Toast.LENGTH_SHORT).show();
                tvDetailPraise.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_heart_red, 0, 0, 0);
            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(TopicDetailActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }
}
