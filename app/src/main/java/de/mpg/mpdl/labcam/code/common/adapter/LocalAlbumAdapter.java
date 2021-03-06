package de.mpg.mpdl.labcam.code.common.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.mpg.mpdl.labcam.R;

/**
 * Created by yingli on 4/7/16.
 */
public class LocalAlbumAdapter extends RecyclerView.Adapter<LocalAlbumAdapter.LocalAlbumViewHolder> {


    private Activity activity;
    private List<String> galleryItems;
    public Set<Integer> positionSet = new HashSet<>();

    public LocalAlbumAdapter(Activity activity, List<String> galleryItems) {
        this.activity = activity;
        this.galleryItems = galleryItems;
    }

    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }


    // get screen size
    private Point getPoint(){
        WindowManager windowManager = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        Log.v(size.x / 2 + " ", size.y / 2 + "");
        return size;
    }

    public static class LocalAlbumViewHolder extends RecyclerView.ViewHolder {
        public final ImageView imageView;
        public final ImageView checkMark;

        public LocalAlbumViewHolder(View view) {
            super(view);
            imageView = (ImageView) view.findViewById(R.id.image_view);
            checkMark = (ImageView) view.findViewById(R.id.gallery_grid_check_mark);
        }
    }

    @Override
    public LocalAlbumViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(activity).inflate(R.layout.gallery_grid_cell, parent, false);
        return new LocalAlbumViewHolder(view);    }

    @Override
    public void onBindViewHolder(LocalAlbumViewHolder holder, final int position) {

        //prepare data
        Point size = getPoint();
        String filePath = galleryItems.get(position);

        // show mark when selected
        if (positionSet.contains(position)) {
            holder.checkMark.setVisibility(View.VISIBLE);
        }else {
            holder.checkMark.setVisibility(View.GONE);
        }

        //show image
        Uri uri = Uri.fromFile(new File(filePath));
        Picasso.with(activity)
                .load(uri)
                .resize(size.x/2,size.y/3)
                .centerCrop()
                .into(holder.imageView);

        if (onItemClickListener!=null){
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListener.onItemClick(v,position);
                }
            });
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    onItemClickListener.onItemLongClick(v,position);
                    return false;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return galleryItems.size();
    }

    public void setPositionSet(Set<Integer> positionSet){
        this.positionSet = positionSet;
        notifyDataSetChanged();
    }

    public interface OnItemClickListener{
        void onItemClick(View view, int position);
        void onItemLongClick(View view, int position);
    }
}
