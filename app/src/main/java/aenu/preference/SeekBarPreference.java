package aenu.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import aenu.reverse.ui.R;

public class SeekBarPreference extends DialogPreference implements OnSeekBarChangeListener {
    private static final String NAMESPACE = "http://schemas.android.com/apk/aenu.preference";
    private int maxValue;
    private int minValue;
    private int newValue;
    private int oldValue;
    private android.widget.SeekBar seekBar;
    private TextView valueView;

    @Override
    public void onStartTrackingTouch(android.widget.SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(android.widget.SeekBar seekBar) {
    }

    public SeekBarPreference(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.minValue = attributeSet.getAttributeIntValue(NAMESPACE, "minValue", 0);
        this.maxValue = attributeSet.getAttributeIntValue(NAMESPACE, "maxValue", 100);
        setDialogLayoutResource(R.layout.preference_seekbar);
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);
    }

    @Override
    public void onBindDialogView(View view) {
        super.onBindDialogView(view);
        if (this.newValue < this.minValue) {
            this.newValue = this.minValue;
        }
        if (this.newValue > this.maxValue) {
            this.newValue = this.maxValue;
        }
        this.seekBar = (android.widget.SeekBar) view.findViewById(R.id.seekbar);
        this.seekBar.setMax(this.maxValue - this.minValue);
        this.seekBar.setProgress(this.newValue - this.minValue);
        this.seekBar.setSecondaryProgress(this.newValue - this.minValue);
        this.seekBar.setOnSeekBarChangeListener(this);
        this.valueView = (TextView) view.findViewById(R.id.value);
        this.valueView.setText(Integer.toString(this.newValue));
    }

    public void onProgressChanged(android.widget.SeekBar seekBar, int i, boolean z) {
        this.newValue = this.minValue + i;
        this.valueView.setText(Integer.toString(this.newValue));
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (!positiveResult)
            newValue = oldValue;
        else {
            oldValue = newValue;
            persistInt(newValue);
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInteger(index, 0);
    }

    @Override
    protected void onSetInitialValue(
        boolean restoreValue, Object defaultValue) {
        oldValue = (restoreValue ?
            getPersistedInt(0) : ((Integer) defaultValue).intValue());
        newValue = oldValue;
    }
}

