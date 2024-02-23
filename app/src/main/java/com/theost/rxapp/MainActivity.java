package com.theost.rxapp;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.theost.rxapp.databinding.ActivityMainBinding;
import java.util.Comparator;
import java.util.List;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private Disposable disposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.button.setOnClickListener(v -> {
            String searchText = binding.editText.getText().toString().trim();
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.button.setVisibility(View.GONE);

            disposable = Api.getData()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .flatMapIterable(items -> items)
                    .map(ApiObject::getValue)
                    .filter(value -> value.contains(searchText))
                    .take(100)
                    .toSortedList()
                    .subscribe(this::handleResults, this::handleError);
        });
    }

    private void handleResults(List<String> sortedValues) {
        String resultString = String.join(", ", sortedValues);
        binding.textView.setText(resultString);
        binding.progressBar.setVisibility(View.GONE);
        binding.button.setVisibility(View.VISIBLE);
    }

    private void handleError(Throwable e) {
        binding.textView.setText("Произошла ошибка: " + e.getMessage());
        binding.progressBar.setVisibility(View.GONE);
        binding.button.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }
}
