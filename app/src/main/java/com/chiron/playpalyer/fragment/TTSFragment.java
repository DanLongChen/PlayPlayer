package com.chiron.playpalyer.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.chiron.playpalyer.R;

import org.jetbrains.annotations.NotNull;

public class TTSFragment extends Fragment {
    private TTSFragment(){}

    public static TTSFragment newInstance(){
        TTSFragment ttsFragment = new TTSFragment();
        return ttsFragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tts,container,false);
        return view;
    }
}
