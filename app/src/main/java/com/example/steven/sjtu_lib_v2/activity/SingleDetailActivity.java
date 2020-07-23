package com.example.steven.sjtu_lib_v2.activity;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.TypeReference;
import com.aliyun.alink.dm.model.RequestModel;
import com.aliyun.alink.linkkit.api.LinkKit;
import com.aliyun.alink.linksdk.cmp.connect.channel.MqttPublishRequest;
import com.aliyun.alink.linksdk.cmp.core.base.AMessage;
import com.aliyun.alink.linksdk.cmp.core.base.ARequest;
import com.aliyun.alink.linksdk.cmp.core.base.AResponse;
import com.aliyun.alink.linksdk.cmp.core.base.ConnectState;
import com.aliyun.alink.linksdk.cmp.core.listener.IConnectNotifyListener;
import com.aliyun.alink.linksdk.cmp.core.listener.IConnectSendListener;
import com.aliyun.alink.linksdk.tmp.device.payload.ValueWrapper;
import com.aliyun.alink.linksdk.tmp.listener.IPublishResourceListener;
import com.aliyun.alink.linksdk.tools.AError;
import com.aliyun.alink.linksdk.tools.ALog;
import com.example.steven.sjtu_lib_v2.R;
import com.example.steven.sjtu_lib_v2.adapter.TableAdapter;
import com.example.steven.sjtu_lib_v2.devicesdk.demo.LightExampleActivity;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.BitmapCallback;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.next.tagview.TagCloudView;
import okhttp3.Call;

/**
 * Created by steven on 2016/2/11.
 */
public class SingleDetailActivity extends AppCompatActivity {
    private static Context c;
    private static String taskstr;
    public static void startRbWatcher(String str)
    {
        Intent intent=new Intent(c,RoboWatcherActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("task",str);
        intent.putExtras(bundle);
        c.startActivity(intent);
    }

    @Bind(R.id.listview_table)
    ListView lv_table;
    @Bind(R.id.tv_book_author)
    TextView tvBookAuthor;
    //@Bind(R.id.tv_book_time)
    static TextView tvBookTime;
    @Bind(R.id.tv_book_page)
    TextView tvBookPage;
    //@Bind(R.id.tv_book_publicer)
    static TextView tvBookPublicer;
    @Bind(R.id.tv_book_isbn)
    TextView tvBookIsbn;
    @Bind(R.id.tv_book_price)
    TextView tvBookPrice;
    @Bind(R.id.tv_book_score)
    TextView tvBookScore;
    @Bind(R.id.iv_book_icon)
    ImageView ivBookIcon;
    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.tag_cloud_view)
    TagCloudView tagCloudView;
    @Bind(R.id.bookinfo)
    TextView tvBookInfo;

    static TextView titleTextView = null;
    private static String REGEX_CHINESE = "[\u4e00-\u9fa5]";// 中文正则

    TableAdapter adapter;
    List<Element> table_data = new ArrayList<Element>();
    String bookInfo;
    String authorInfo;
    String url = null;
    public static String base_url = "http://ourex.lib.sjtu.edu.cn/primo_library/libweb/action/";

    public final static int REPORT_MSG = 0x100;
    public static InternalHandler mHandler = new InternalHandler();
    static boolean isdoubanok = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        c=this;
        setContentView(R.layout.single_drawer);
        ButterKnife.bind(this);
        tvBookTime = (TextView)findViewById(R.id.tv_book_time) ;
        tvBookPublicer = (TextView)findViewById(R.id.tv_book_publicer) ;


        adapter = new TableAdapter(getApplicationContext(), 0, table_data);
        lv_table.setAdapter(adapter);
        setDownStreamListener();

        final String detail_html = get_html_from_intent();
        url = get_url_from_intent();
        get_table_data(url);

        try {
            Field f = toolbar.getClass().getDeclaredField("mTitleTextView");
            f.setAccessible(true);
            titleTextView = (TextView) f.get(toolbar);

            titleTextView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            titleTextView.setFocusable(true);
            titleTextView.setFocusableInTouchMode(true);
            titleTextView.requestFocus();
            titleTextView.setSingleLine(true);
            titleTextView.setSelected(true);
            titleTextView.setMarqueeRepeatLimit(-1);
            titleTextView.setTextColor(Color.WHITE);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        toolbar.inflateMenu(R.menu.menu_main);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                SQLiteDatabase db = openOrCreateDatabase("collection.db", Context.MODE_PRIVATE, null);
                db.execSQL("create table if not exists favourite (_id INTEGER PRIMARY KEY AUTOINCREMENT, book_name VARCHAR, url VARCHAR unique)");
                switch (item.getItemId()) {
                    case R.id.add_to_collection:
                        ContentValues cv = new ContentValues();
                        cv.put("book_name", detail_html);
                        cv.put("url", url);
                        db.insert("favourite", null, cv);

                        Toast.makeText(getApplicationContext(), "收藏成功", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.remove_from_collection:
                        if (db.delete("favourite", "url=?", new String[]{url}) > 0) {
                            Toast.makeText(getApplicationContext(), "取消收藏成功", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "你尚未收藏此书", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case R.id.share:
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_SEND);
                        intent.putExtra(Intent.EXTRA_TEXT, url);
                        intent.setType("text/plain");
                        startActivity(intent);
                        break;
                }
                return true;
            }
        });

    }

    private String get_html_from_intent() {
        String detail_html = getIntent().getExtras().getString("detail");
        Document docuement = Jsoup.parse(detail_html);
        tvBookAuthor.setText(docuement.getElementsByClass("EXLResultAuthor").text());
        tvBookPublicer.setText(docuement.getElementsByClass("EXLResultFourthLine").text());
        toolbar.setTitle(docuement.getElementsByClass("EXLResultTitle").text());
        return detail_html;
    }

    public String get_url_from_intent() {
        String url = getIntent().getExtras().getString("url");
        return url;
    }

    public void get_table_data(String url) {
        OkHttpUtils.get()
                .url(url)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {

                    }

                    @Override
                    public void onResponse(String response) {
                        Pattern pattern = Pattern.compile("(?<=rft\\.isbn=)\\d+");
                        Matcher matcher = pattern.matcher(response);
                        if (matcher.find()) {
                            tvBookIsbn.setText(matcher.group(0));
                            getDoubanInfo(matcher.group(0));
                        } else {
                            isdoubanok=false;
                            Toast.makeText(getApplicationContext(), "未能找到isbn，无法加载豆瓣数据", Toast.LENGTH_SHORT).show();
                        }

                        Document doc = Jsoup.parse(response);
                        Elements EXLLocationTableColumn1_eles = doc.getElementsByClass("EXLLocationTableColumn1");
                        if (!EXLLocationTableColumn1_eles.isEmpty()) {
                            for (Element i : EXLLocationTableColumn1_eles) {
                                table_data.add(i.parent());
                            }
                            adapter.notifyDataSetChanged();
                        } else {
                            List<String> link_list = new ArrayList<String>();
                            Elements link_elm = doc.getElementsByClass("EXLLocationsIcon");
                            for (Element i : link_elm) {
                                String temp_link = i.attr("href");
                                temp_link = base_url + temp_link;
                                link_list.add(temp_link);
                            }
                            get_location_from_linklist(link_list);

                        }
                    }

                    private void get_location_from_linklist(List<String> link_list) {
                        for (String link : link_list) {
                            get_location_from_link(link);
                        }
                    }

                    private void get_location_from_link(String link) {
                        OkHttpUtils.get()
                                .url(link)
                                .build()
                                .execute(new StringCallback() {
                                    @Override
                                    public void onError(Call call, Exception e) {

                                    }

                                    @Override
                                    public void onResponse(String response) {
                                        Document doc = Jsoup.parse(response, "", Parser.xmlParser());
                                        String first_modification = doc.getElementsByTag("modification").first().text();
                                        Document modi_html = Jsoup.parse(first_modification, "", Parser.htmlParser());
                                        Elements fin_eles = modi_html.getElementsByClass("EXLLocationTableColumn3");

                                        for (Element i : fin_eles) {
                                            table_data.add(i.parent());
                                        }
                                        adapter.notifyDataSetChanged();
                                    }
                                });
                    }

                    private void getDoubanInfo(String isbn) {
                        OkHttpUtils.get()
                                //.url("https://api.douban.com/v2/book/isbn/" + isbn) //豆瓣凉了
                                //.url("http://api.xiaomafeixiang.com/api/bookinfo?isbn="+isbn) //书库太少
                                //.url("http://book.feelyou.top/isbn/"+isbn) //zbq凉了
                                .url("https://api.douban.com/v2/book/isbn/" + isbn+"?apikey=0df993c66c0c636e29ecbb5344252a4a") //随时可能凉
                                .build()
                                .execute(new StringCallback() {
                                    @Override
                                    public void onError(Call call, Exception e) {
                                        isdoubanok=false;
                                        Toast.makeText(getApplicationContext(), "加载豆瓣数据失败", Toast.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void onResponse(String response) {
                                        isdoubanok=true;
                                        Toast.makeText(getApplicationContext(), "加载豆瓣数据成功0", Toast.LENGTH_SHORT).show();
                                        try {
                                            JSONObject jsonobect = new JSONObject(response);
                                            tvBookPage.setText(jsonobect.getString("pages"));
                                            tvBookPrice.setText(jsonobect.getString("price"));
                                            tvBookAuthor.setText(jsonobect.getString("author"));
                                            tvBookPublicer.setText(jsonobect.getString("publisher"));
                                            tvBookTime.setText(jsonobect.getString("pubdate"));
                                            tvBookScore.setText(jsonobect.getJSONObject("rating").getString("average"));
                                            tvBookPage.setText(jsonobect.getString("pages"));
                                            tvBookInfo.setText(jsonobect.getString("summary"));
                                            authorInfo = jsonobect.getString("author_intro");
                                            final String doubanlink=jsonobect.getString("alt");
                                            makeTextViewResizable(tvBookInfo, 3, "View More", true);

                                            ivBookIcon.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(doubanlink));
                                                    startActivity(browserIntent);
                                                }
                                            });

                                            String imageUrl = jsonobect.getString("image");
                                            OkHttpUtils.get()
                                                    .url(imageUrl)
                                                    .build()
                                                    .execute(new BitmapCallback() {
                                                        @Override
                                                        public void onError(Call call, Exception e) {
                                                            Toast.makeText(getApplicationContext(), "加载图片失败", Toast.LENGTH_SHORT).show();
                                                        }

                                                        @Override
                                                        public void onResponse(Bitmap response) {
                                                            ivBookIcon.setImageBitmap(response);
                                                        }
                                                    });

                                            JSONArray jsonarray = jsonobect.getJSONArray("tags");
                                            List<String > tag=new ArrayList<String>();
                                            for (int i = 0; i < jsonarray.length(); i++) {
                                                JSONObject object = jsonarray.getJSONObject(i);
                                                tag.add(object.getString("title"));

//                                                tag.add(jsonarray.getString(i));
                                            }
                                            tagCloudView.setTags(tag);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                    }
                });
    }

    @OnClick(R.id.tv_book_author)
    public void showAuthorInfo() {
        if (authorInfo == null) {
            new AlertDialog.Builder(SingleDetailActivity.this)
                    .setTitle("作者信息")
                    .setMessage("请稍等。。。")
                    .setPositiveButton("确认", null)
                    .create()
                    .show();
        } else {
            new AlertDialog.Builder(SingleDetailActivity.this)
                    .setMessage(authorInfo)
                    .setTitle("作者信息")
                    .setPositiveButton("确认", null)
                    .create()
                    .show();
        }
    }

    public static void makeTextViewResizable(final TextView tv, final int maxLine, final String expandText, final boolean viewMore) {

        if (tv.getTag() == null) {
            tv.setTag(tv.getText());
        }
        ViewTreeObserver vto = tv.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @SuppressWarnings("deprecation")
            @Override
            public void onGlobalLayout() {

                ViewTreeObserver obs = tv.getViewTreeObserver();
                obs.removeGlobalOnLayoutListener(this);
                if (maxLine == 0) {
                    int lineEndIndex = tv.getLayout().getLineEnd(0);
                    String text = tv.getText().subSequence(0, lineEndIndex - expandText.length() + 1) + " " + expandText;
                    tv.setText(text);
                    tv.setMovementMethod(LinkMovementMethod.getInstance());
                    tv.setText(
                            addClickablePartTextViewResizable(Html.fromHtml(tv.getText().toString()), tv, maxLine, expandText,
                                    viewMore), TextView.BufferType.SPANNABLE);
                } else if (maxLine > 0 && tv.getLineCount() >= maxLine) {
                    int lineEndIndex = tv.getLayout().getLineEnd(maxLine - 1);
                    String text = tv.getText().subSequence(0, lineEndIndex - expandText.length() + 1) + " " + expandText;
                    tv.setText(text);
                    tv.setMovementMethod(LinkMovementMethod.getInstance());
                    tv.setText(
                            addClickablePartTextViewResizable(Html.fromHtml(tv.getText().toString()), tv, maxLine, expandText,
                                    viewMore), TextView.BufferType.SPANNABLE);
                } else {
                    int lineEndIndex = tv.getLayout().getLineEnd(tv.getLayout().getLineCount() - 1);
                    String text = tv.getText().subSequence(0, lineEndIndex) + " " + expandText;
                    tv.setText(text);
                    tv.setMovementMethod(LinkMovementMethod.getInstance());
                    tv.setText(
                            addClickablePartTextViewResizable(Html.fromHtml(tv.getText().toString()), tv, lineEndIndex, expandText,
                                    viewMore), TextView.BufferType.SPANNABLE);
                }
            }
        });

    }

    private static SpannableStringBuilder addClickablePartTextViewResizable(final Spanned strSpanned, final TextView tv,
                                                                            final int maxLine, final String spanableText, final boolean viewMore) {
        String str = strSpanned.toString();
        SpannableStringBuilder ssb = new SpannableStringBuilder(strSpanned);

        if (str.contains(spanableText)) {
            ssb.setSpan(new ClickableSpan() {

                @Override
                public void onClick(View widget) {

                    if (viewMore) {
                        tv.setLayoutParams(tv.getLayoutParams());
                        tv.setText(tv.getTag().toString(), TextView.BufferType.SPANNABLE);
                        tv.invalidate();
                        makeTextViewResizable(tv, -1, "View Less", false);
                    } else {
                        tv.setLayoutParams(tv.getLayoutParams());
                        tv.setText(tv.getTag().toString(), TextView.BufferType.SPANNABLE);
                        tv.invalidate();
                        makeTextViewResizable(tv, 3, "View More", true);
                    }

                }
            }, str.indexOf(spanableText), str.indexOf(spanableText) + spanableText.length(), 0);

        }
        return ssb;

    }

    public static class InternalHandler extends Handler {
        public InternalHandler() {
            super(Looper.getMainLooper());
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg == null) {
                return;
            }
            int what = msg.what;
            switch (what) {
                case REPORT_MSG:
                    reportBookInfo(msg.getData());
                    //mHandler.sendEmptyMessageDelayed(REPORT_MSG, 5*1000);
                    break;
            }

        }
    }
    /**
     * 数据上行
     * 上报该本图书信息
     */
    public static void reportBookInfo(Bundle bundle) {
        String path = bundle.getString("location");
        String code = bundle.getString("bookid");
        char[] srChar=path.toCharArray();
        char locHead='Z';
        char numHead='9';
        char idHead=code.charAt(0);
        int year;
        if(isdoubanok&&!tvBookTime.getText().toString().isEmpty()){
            year = Integer.parseInt(tvBookTime.getText().toString().substring(0,4));
        }else{
            year = Integer.parseInt(tvBookPublicer.getText().toString().substring(tvBookPublicer.getText().toString().length()-4));
        }
        //year=Integer.parseInt(code.substring(code.length() - 2));//对于时间的获取，需要通过解析出版社最后四位或出版时间前4位
        if(path.contains("主馆临时")){
            if(year>=2019){
                path="C300";
            }else {
                path=path.substring(path.indexOf(idHead)+3,path.indexOf(idHead)+7);
            }
        }else if(path.contains("主馆图书")){
            for (char c : srChar) {
                if ((char)c>='A'&&(char)c<='Z') {
                    locHead=c;
                    break;
                }
            }
            for (char c : srChar) {
                if ((char)c>='1' && (char)c<='9') {
                    numHead=c;
                    break;
                }
            }
            Pattern pat = Pattern.compile(REGEX_CHINESE);
            Matcher mat = pat.matcher(path);
            String nocnwords = mat.replaceAll("").replace(" ", "");
            path=nocnwords.substring(0,2)+"00";
        }else path="warning!"+path;
        Log.v("msg","上报 Hello, World！");
        try {
            Map<String, ValueWrapper> reportData = new HashMap<>();
            //reportData.put("Status", new ValueWrapper.BooleanValueWrapper(1)); // 1开 0 关
            //reportData.put("Data", new ValueWrapper.StringValueWrapper("Hello, World!")); //
            String tasklist="";
            String name = titleTextView.getText().toString();
//            String path = "C300";
//            String code = "I313.45/24-3 2019";
            tasklist+="{\"bookname\":\""+name+"\"," +
                        "\"path\":\""+path+"\"," +
                        "\"code\":\""+code+"\"}\n";
            taskstr=tasklist;
            reportData.put("data",new ValueWrapper.StringValueWrapper(tasklist));
            reportData.put("Uab",new ValueWrapper.IntValueWrapper(1234));
            LinkKit.getInstance().getDeviceThing().thingPropertyPost(reportData, new IPublishResourceListener() {
                @Override
                public void onSuccess(String s, Object o) {
                    Log.v("msg","onSuccess() called with: s = [" + s + "], o = [" + o + "]");
                    //Toast.makeText(getClass(), "设备上报状态成功", Toast.LENGTH_SHORT).show();
                    //showToast("设备上报状态成功");
                    Log.v("msg","上报成功。");

                    startRbWatcher(taskstr);
                }

                @Override
                public void onError(String s, AError aError) {
                    Log.v("msg","onError() called with: s = [" + s + "], aError = [" + aError + "]");
                    //showToast("设备上报状态失败");
                    Log.v("msg","上报失败。");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void setDownStreamListener(){
        LinkKit.getInstance().registerOnPushListener(notifyListener);
    }

    private IConnectNotifyListener notifyListener = new IConnectNotifyListener() {
        @Override
        public void onNotify(String s, String s1, AMessage aMessage) {
            try {
//                if (s1 != null && s1.contains("service/property/set")) {
                if (s1 != null && s1.contains("service/property/set")) {
                    String result = new String((byte[]) aMessage.data, "UTF-8");
                    RequestModel<String> receiveObj = com.alibaba.fastjson.JSONObject.parseObject(result, new TypeReference<RequestModel<String>>() {
                    }.getType());
                    Log.v("msg","Received raw: "+result);
                    Log.v("msg","Received a message: " + (receiveObj==null?"":receiveObj.params));
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (Exception e){
                e.printStackTrace();
            }
        }

        @Override
        public boolean shouldHandle(String s, String s1) {
            Log.v("msg", "shouldHandle() called with: s = [" + s + "], s1 = [" + s1 + "]");
            return true;
        }

        @Override
        public void onConnectStateChange(String s, ConnectState connectState) {
            Log.v("msg","onConnectStateChange() called with: s = [" + s + "], connectState = [" + connectState + "]");
        }
    };
    public void showToast(final String message){
        ALog.d("msg", "showToast() called with: message = [" + message + "]");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
