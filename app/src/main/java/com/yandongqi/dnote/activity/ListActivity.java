package com.yandongqi.dnote.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.FindCallback;
import com.yandongqi.dnote.R;
import com.yandongqi.dnote.adpter.NoteAdapter;
import com.yandongqi.dnote.global.NoteFactory;

import java.util.List;

/**
 * Class ListActivity
 *
 * @author XhinLiang
 */
public class ListActivity extends AppCompatActivity {
    public static final String KEY_EXTRA_NOTE = "note";
    public static final int REQUEST_FOR_EDIT_NOTE = 100;
    private static final int REQUEST_FOR_CREATE_NOTE = 101;

    private NoteAdapter adapter;
    private ListView listView;
    private FloatingActionButton fab;
    private SwipeRefreshLayout swipeRefreshLayout;

    private NoteFactory noteFactory = NoteFactory.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        initView();
        initData();
        initListView();
        initEvents();
    }

    private void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        listView = (ListView) findViewById(R.id.listview_content);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.srl_refresh);
        listView.setEmptyView(findViewById(R.id.empty_view));
        setSupportActionBar(toolbar);


    }

    private void initData() {
        adapter = new NoteAdapter(this, noteFactory.getNotes());
    }

    private void initListView() {
        // 启用 ListView 的嵌套滚动
        listView.setNestedScrollingEnabled(true);
        // 设置 Adapter
        listView.setAdapter(adapter);
    }

    private void initEvents() {
        // ListView 的 Item 点击的时候的逻辑
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(ListActivity.this, DetailActivity.class);
                intent.putExtra(KEY_EXTRA_NOTE, position);
                startActivityForResult(intent, REQUEST_FOR_EDIT_NOTE);
            }
        });

        // 浮动按钮执行的逻辑
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ListActivity.this, DetailActivity.class);
                intent.putExtra(KEY_EXTRA_NOTE, -1);
                startActivityForResult(intent, REQUEST_FOR_CREATE_NOTE);
            }
        });

        // 下拉刷新的时候执行的逻辑
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                AVQuery<AVObject> avQuery = new AVQuery<>("Note");
                avQuery.orderByDescending("createdAt");
                avQuery.whereEqualTo("owner", AVUser.getCurrentUser());
                avQuery.findInBackground(new FindCallback<AVObject>() {
                    @Override
                    public void done(List<AVObject> list, AVException e) {
                        swipeRefreshLayout.setRefreshing(false);
                        if (e != null) {
                            Toast.makeText(ListActivity.this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                            return;
                        }
                        noteFactory.refresh(list);
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        });

        // ListView 只有在最顶端的时候才可以触发 SwipeRefresh
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int topRowVerticalPosition = (listView == null || listView.getChildCount() == 0) ? 0 : listView.getChildAt(0).getTop();
                swipeRefreshLayout.setEnabled(firstVisibleItem == 0 && topRowVerticalPosition >= 0);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_FOR_EDIT_NOTE) {
            Snackbar.make(fab, R.string.edit_note_success, Snackbar.LENGTH_LONG).show();
            adapter.notifyDataSetChanged();
        }
        if (requestCode == REQUEST_FOR_CREATE_NOTE) {
            Snackbar.make(fab, R.string.create_note_success, Snackbar.LENGTH_LONG)
                    .setAction(R.string.revert, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            noteFactory.getNotes().remove(noteFactory.getNotes().size() - 1);
                            adapter.notifyDataSetChanged();
                        }
                    })
                    .show();
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_list, menu);
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                AVUser.logOut();
                noteFactory.getNotes().clear();
                Intent intent = new Intent();
                intent.setClass(ListActivity.this,LoginActivity.class);
                startActivity(intent);
                //finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
