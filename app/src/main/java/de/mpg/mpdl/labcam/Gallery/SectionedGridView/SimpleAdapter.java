package de.mpg.mpdl.labcam.Gallery.SectionedGridView;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.mpg.mpdl.labcam.Model.LocalModel.Image;
import de.mpg.mpdl.labcam.R;
import de.mpg.mpdl.labcam.Utils.DBConnector;
import de.mpg.mpdl.labcam.Utils.ToastUtil;

/**
 * Created by yingli on 2/23/16.
 */
public class SimpleAdapter extends RecyclerView.Adapter<SimpleAdapter.SimpleViewHolder> {

    private final Context mContext;
    private final List<String> mItems;

    //remember selected positions
    public Set<Integer> positionSet = new HashSet<>();

    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public static class SimpleViewHolder extends RecyclerView.ViewHolder {
        public final ImageView imageView;
        public final ImageView checkMark;
        public final ImageView noteImageView;
        public final ImageView voiceImageView;
        public final RelativeLayout relativeLayout;

        public SimpleViewHolder(View view) {
            super(view);
            imageView = (ImageView) view.findViewById(R.id.header_grid_image);
            checkMark = (ImageView) view.findViewById(R.id.header_grid_check_mark);
            noteImageView = (ImageView) view.findViewById(R.id.header_grid_note);
            voiceImageView = (ImageView) view.findViewById(R.id.header_grid_voice);
            relativeLayout = (RelativeLayout) view.findViewById(R.id.layout_bottom);
        }
    }

    public SimpleAdapter(Context context,List<String> galleryItems) {
        mContext = context;
        mItems = galleryItems;
    }

    public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(mContext).inflate(R.layout.header_grid_item, parent, false);
        return new SimpleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SimpleViewHolder holder, final int position) {
        String filePath = mItems.get(position);

        Image image = DBConnector.getImageByPath(filePath);
        if(image!=null && (image.getNoteId()!=null || image.getVoiceId()!=null)){
            holder.relativeLayout.setVisibility(View.VISIBLE);
            if(image.getNoteId()!=null){      // show notes
                holder.noteImageView.setVisibility(View.VISIBLE);
            }else holder.noteImageView.setVisibility(View.INVISIBLE);

            if(image.getVoiceId()!=null){     // show voice
                holder.voiceImageView.setVisibility(View.VISIBLE);
            }else holder.voiceImageView.setVisibility(View.INVISIBLE);
        }else {
            holder.relativeLayout.setVisibility(View.INVISIBLE);
        }

        Uri uri = Uri.fromFile(new File(filePath));

        Picasso.with(mContext)
                .load(uri)
                .resize(235,235)
                .centerCrop()
                .into(holder.imageView);

        if(positionSet.contains(position)){
            holder.checkMark.setVisibility(View.VISIBLE);
        }else {
            holder.checkMark.setVisibility(View.GONE);
        }

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

    public void remove(String str){
        mItems.remove(str);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public void setPositionSet(Set<Integer> positionSet){
        this.positionSet = positionSet;
        Log.e("positionSet",positionSet.size()+"");
        notifyDataSetChanged();
    }

    public interface OnItemClickListener{
        void onItemClick(View view, int position);
        void onItemLongClick(View view, int position);
    }
}