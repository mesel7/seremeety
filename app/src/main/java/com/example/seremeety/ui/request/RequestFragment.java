package com.example.seremeety.ui.request;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.seremeety.databinding.FragmentRequestBinding;

public class RequestFragment extends Fragment {

    private FragmentRequestBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        RequestViewModel requestViewModel =
                new ViewModelProvider(this).get(RequestViewModel.class);

        binding = FragmentRequestBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textRequest;
        requestViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
