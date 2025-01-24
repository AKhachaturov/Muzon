package com.example.muzon;





import static com.example.muzon.MainActivity.mService;

import android.content.ContentUris;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.Fragment;

import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;

import androidx.recyclerview.widget.LinearLayoutManager;

import androidx.recyclerview.widget.RecyclerView;
import android.database.Cursor;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.util.Log;

import java.io.File;

import java.util.ArrayList;
import java.util.List;



/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SongsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SongsFragment extends Fragment implements NotificationCenter.Delegate{

    private ArrayList<Song> songs = new ArrayList<>();
    private SongsAdapter adapter;
    private RecyclerView recyclerView;
    private Song lastSong = null;
    private LinearLayoutManager linearLayoutManager;


    private SongsFragment() {
        super(R.layout.songs_fragment);
    }

    public static SongsFragment newInstance() {
        return new SongsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.songs_fragment, container, false);

        SongsAdapter.songClickListener clickListener = new SongsAdapter.songClickListener(){
            @Override
            public void onSongClick(Song mSong, int position){
                if(mSong != lastSong){
                    lastSong = mSong;
                    Uri uri = Uri.parse(mSong.getPath());
                    MediaItem mediaItem = MediaItem.fromUri(uri);
                    mService.setCurrentSong(mediaItem);
                    PlayerController.getInstance().setPlaying(true);
                    NotificationCenter.getInstance().postNotification(NotificationCenter.clickedSong, mSong, position);

                }
                else {
                    NotificationCenter.getInstance().postNotification(NotificationCenter.repeatClick);
                    PlayerController.getInstance().changePlaying();
                }
            }
        };
        recyclerView = view.findViewById(R.id.list);
        // recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setHasFixedSize(true);
        linearLayoutManager.setSmoothScrollbarEnabled(true);
        recyclerView.setItemViewCacheSize(10);
        recyclerView.setDrawingCacheEnabled(true);
        adapter = new SongsAdapter(requireContext(), songs, clickListener);
        recyclerView.setAdapter(adapter);
        DividerItemDecoration divider = new DividerItemDecoration(recyclerView.getContext(),
                R.drawable.divider);
        recyclerView.addItemDecoration(divider);

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NotificationCenter.getInstance().addObserver(NotificationCenter.songChanged, this);
        linearLayoutManager = new LinearLayoutManager(requireContext());

    }

    @Override
    public void onStart() {
        super.onStart();
        ArrayList<Song> loadedSongs = loadSongs(requireContext());
        NotificationCenter.getInstance().postNotification(NotificationCenter.songsLoaded, loadedSongs);
        songs.clear();
        songs.addAll(loadedSongs);
        adapter.notifyDataSetChanged();
        }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }


    @Override
    public void onDestroy(){
        super.onDestroy();
        NotificationCenter.getInstance().removeObserver(NotificationCenter.songChanged, this);
    }

    @Override
    public void didReceivedNotification(int id, Object... args){
        if(id == NotificationCenter.songChanged){
            lastSong = (Song)args[0];
        }else if(id == NotificationCenter.songsLoaded){
            songs.addAll((ArrayList<Song>)args[0]);
            adapter.notifyDataSetChanged();
        }
    }

    public ArrayList<Song> loadSongs(Context context) {
        ArrayList<Song> update = new ArrayList<>();
        try (Cursor data = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null,
                null)) {
            if (data == null) {
                return new ArrayList<>();
            }

            while (data.moveToNext()) {
                int isMusic = data.getInt(data
                        .getColumnIndexOrThrow(MediaStore.Audio.Media.IS_MUSIC));
                if (isMusic > 0) {
                    Song song = new Song();

                    song.setPath(data.getString(data
                            .getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)));
                    if (!new File(song.getPath()).exists()) {
                        continue;
                    }

                    song.setId(data.getLong(data
                            .getColumnIndexOrThrow(MediaStore.Audio.Media._ID)));
                    song.setName(data.getString(data
                            .getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)));
                    song.setAuthor(data.getString(data
                            .getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)));

                    long albumId = data.getLong(data
                            .getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));

                    song.setImage(getAlbumArt(albumId));
                    song.setDuration(data.getLong(data.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)));


                    update.add(song);

                }
            }
            return update;
        }
    }

    private Uri getAlbumArt(long album_id) {
        Context context = requireContext();
        Uri mUriAlbums = Uri.parse("content://media/external/audio/albumart");
        DocumentFile file = DocumentFile.fromSingleUri(context, ContentUris.withAppendedId(mUriAlbums, album_id));
        if(file == null){

            return Uri.parse("android.resource://" + context.getPackageName() + "/" + R.drawable.photo);
        }
        return ContentUris.withAppendedId(mUriAlbums, album_id);
    }
}