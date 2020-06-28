package com.example.steven.sjtu_lib_v2.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;
import com.example.steven.sjtu_lib_v2.R;

import org.jsoup.nodes.Element;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by steven on 2016/2/9.
 */
public class BookItemAdapter extends ArrayAdapter<Element> {
    ImageLoader mImageloader;
    String book_cover_base_url="http://pds.cceu.org.cn/cgi-bin/isbn_cover.cgi?isbn=";

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Element complete_info=getItem(position);
        //System.out.println("test"+complete_info.html());//for test
        String cover_image_url=get_cover_image_url(complete_info);
        String book_name=get_book_name(complete_info);
        String book_detail = get_Author(complete_info)+"\n"+get_Press(complete_info)+"\n"+get_Available(complete_info);
        String avaliable = get_Available(complete_info);
        if(convertView==null){
            convertView= LayoutInflater.from(getContext()).inflate(R.layout.item,null);
        }
        TextView tv= (TextView) convertView.findViewById(R.id.textView2);
        TextView tv2= (TextView) convertView.findViewById(R.id.textView3);
        TextView tv3= (TextView) convertView.findViewById(R.id.textView4);
        NetworkImageView book_cover= (NetworkImageView) convertView.findViewById(R.id.book_cover);

        tv.setText(book_name);
        tv2.setText(book_detail);
        tv3.setText(avaliable);
        if(avaliable.contains("在架")){//本句好像没有用？

            tv3.setTextColor(Color.GREEN);
            tv3.append("(查看位置)");
        }
        book_cover.setImageUrl(cover_image_url,mImageloader);

        return convertView;
    }

    private String get_book_name(Element complete_info) {
        return complete_info.getElementsByClass("EXLResultTitle").text();
    }

    //Keenster changed
    private String get_Author(Element complete_info){
        return complete_info.getElementsByClass("EXLResultAuthor").text();
    }
    private String get_Press(Element complete_info){
        return complete_info.getElementsByClass("EXLResultFourthLine").text();
    }
    private String get_Available(Element complete_info){
        String data = complete_info.getElementsByClass("EXLResultStatusAvailable").text();
        if(data.contains("在架上")){
            return "在架上";
        }
        else return " ";
    }
    private String get_lib(Element complete_info){
        return complete_info.getElementsByClass("EXLAvailabilityLibraryName").text();
    }

    private String get_cover_image_url(Element complete_info) {
        Pattern pattern= Pattern.compile("(?<=isbn=)\\d*");
        Matcher matcher=pattern.matcher(complete_info.toString());
        if(matcher.find()){
           return(book_cover_base_url+matcher.group());
        }else{
            return null;
        }
    }

    public BookItemAdapter(Context context, int resource, List<Element> objects) {
        super(context, resource, objects);
        RequestQueue quene= Volley.newRequestQueue(context);
        mImageloader=new ImageLoader(quene,new BitmapCache());
    }

    private class BitmapCache implements ImageLoader.ImageCache {

        private LruCache<String,Bitmap> mCache;

        public BitmapCache(){
            int maxMemory= (int) Runtime.getRuntime().maxMemory();
            int cacheSize=maxMemory/8;
            mCache=new LruCache<String,Bitmap>(cacheSize){
                @Override
                protected int sizeOf(String key, Bitmap value) {
                    return value.getRowBytes()*value.getHeight();
                }
            };
        }
        @Override
        public Bitmap getBitmap(String url) {
            return mCache.get(url);
        }

        @Override
        public void putBitmap(String url, Bitmap bitmap) {
            mCache.put(url,bitmap);
        }
    }
}
