package com.example.muzon;

import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;


import java.util.List;

public class SongsAdapter extends RecyclerView.Adapter<SongsAdapter.ViewHolder> {
    private final LayoutInflater inflater;
    private List<Song> list;
    private final songClickListener onClickListener;
    public final int MAX_LENGTH = 26;

    SongsAdapter(Context context, List<Song> list, songClickListener listener){
        this.inflater = LayoutInflater.from(context);
        this.list = list;
        onClickListener = listener;
    }

    @NonNull
    @Override
    public SongsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view = inflater.inflate(R.layout.songs_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SongsAdapter.ViewHolder holder, int position){
        Song song = list.get(position);
        int pos = position;
        holder.author.setText(Utils.cutString(song.getAuthor(), MAX_LENGTH));
        holder.name.setText(Utils.cutString(song.getName(), MAX_LENGTH));
        Glide.with(holder.author.getContext()).load(song.getImage()).placeholder(R.drawable.photo).into(holder.image);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickListener.onSongClick(song, pos);
            }
        });
    }

    @Override
    public int getItemCount(){
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        final TextView name;
        final TextView author;
        final ImageView image;

        ViewHolder(View view){
            super(view);
            name = view.findViewById(R.id.namef);
            author = view.findViewById(R.id.authorf);
            image = view.findViewById(R.id.imagef);

        }
    }

    @Override
    public int getItemViewType(int position) {
        // Return a unique view type for the item at the given position
        return position % 2 == 0 ? 0 : 1;
    }

    public String getDuration(Long length){
        return length.toString();
    }

    public interface songClickListener{
        void onSongClick(Song mSong, int position);
    }
}
