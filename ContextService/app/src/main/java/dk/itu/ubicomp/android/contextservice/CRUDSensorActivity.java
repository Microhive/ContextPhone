package dk.itu.ubicomp.android.contextservice;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import dk.itu.ubicomp.android.contextservice.Data.DummyContent.DummyItem;

public class CRUDSensorActivity extends AppCompatActivity {

    private DummyItem mItem;
    private TextView mTextViewValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_crud);

        // set the toolbar title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.nav_title_sensor_list_edit);
        }

        // To retrieve object in second Activity
        mItem = (DummyItem)getIntent().getSerializableExtra("ITEM");
        mTextViewValue = (TextView) findViewById(R.id.editTextValue);
        mTextViewValue.setText(mItem.id);
    }
}
