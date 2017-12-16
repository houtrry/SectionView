package com.het.sectionview;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private int[] mSectionColors = {Color.parseColor("#f66f09"), Color.parseColor("#516829"), Color.parseColor("#14b8e8"), Color.parseColor("#0099ee")};

    private float[] mCriticalValues = {0, 50, 70, 90, 100};
    private float[] mSectionProportions = {0.4f, 0.2f, 0.2f, 0.2f};

    private String[] mSectionDesc = {"较差", "一般", "良好", "优秀"};
    private SectionView mSectionView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSectionView = (SectionView) findViewById(R.id.sectionView);

        mSectionView.setCriticalValues(mCriticalValues);
        mSectionView.setSectionColors(mSectionColors);
        mSectionView.setSectionProportions(mSectionProportions);
        mSectionView.setSectionDesc(mSectionDesc);
        mSectionView.setCurrentPointValue(55);
        mSectionView.show();


        findViewById(R.id.changeValue).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Random random = new Random();
                mSectionView.setCurrentPointValue(random.nextInt(100));
                mSectionView.show();
            }
        });
    }
}
