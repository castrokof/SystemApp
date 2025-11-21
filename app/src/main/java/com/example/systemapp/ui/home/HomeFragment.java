package com.example.systemapp.ui.home;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.systemapp.R;
import com.example.systemapp.databinding.FragmentHomeBinding;

import java.util.Arrays;
import java.util.List;


public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private  TextView input_search;
    private ListView listItems;

    private String nombres [] = {"Carlos", "Samuel", "Jhonnathan", "Andres"};
    private String edades [] = {"18", "20", "36", "37"};


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root   = binding.getRoot();
        input_search =(TextView) root.findViewById(R.id.input_search);
        listItems = (ListView) root.findViewById(R.id.listItems);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.list_item_ordenes,nombres);


        listItems.setAdapter(adapter);


        listItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                input_search.setText("La Edad de " +listItems.getItemAtPosition(i) + " Es " + edades[i] + " años");
            }
        });

        return root;


    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}