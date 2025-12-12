package com.orhanobut.benchmark;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.orhanobut.hawk.Hawk;

import java.util.Locale;

public class MainActivity extends Activity {

  private static final int RUNS = 200;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    benchmark("Hawk.init", () -> {
      Hawk.init(this)
          .setLogInterceptor(message -> Log.d("HAWK", message))
          .build();
    });

    benchmark("Hawk.put", () -> Hawk.put("key", "value".repeat(5000)));

    benchmark("Hawk.get", () -> Hawk.get("key"));

    benchmark("Hawk.contains", () -> Hawk.contains("key"));

    benchmark("Hawk.count", Hawk::count);

    benchmark("Hawk.delete", () -> Hawk.delete("key"));
  }

  // ======================
  // Benchmark Helper
  // ======================
  private void benchmark(String label, Runnable action) {
    long min = Long.MAX_VALUE;
    long max = Long.MIN_VALUE;
    long sum = 0;

    // Warm up
    for (int i = 0; i < 10; i++) action.run();

    // Actual measurements
    for (int i = 0; i < RUNS; i++) {
      long start = System.nanoTime();
      action.run();
      long end = System.nanoTime();

      long time = end - start;
      sum += time;
      min = Math.min(min, time);
      max = Math.max(max, time);
    }

    long avg = sum / RUNS;

    System.out.println(
        label + " → " +
            "min=" + nanosToMs(min) + "ms, " +
            "avg=" + nanosToMs(avg) + "ms, " +
            "max=" + nanosToMs(max) + "ms"
    );
  }

  private String nanosToMs(long nanos) {
    return String.format(Locale.US, "%.3f", nanos / 1_000_000.0);
  }
}
