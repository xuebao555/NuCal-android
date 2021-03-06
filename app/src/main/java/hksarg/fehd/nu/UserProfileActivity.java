package hksarg.fehd.nu;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.activeandroid.query.Select;

import java.io.IOException;

import hksarg.fehd.nu.model.User;

public class UserProfileActivity extends AppCompatActivity implements View.OnClickListener{

    public static final int OPEN_FOR_CUR_USER = 0;
    public static final int OPEN_FOR_NEW_USER = 1;
    public static final int OPEN_FOR_ADD_USER = 2;
    public static final int OPEN_FOR_CHG_USER = 3;
    private static final int PICK_IMAGE_REQUEST = 1000;

    ImageView   ivAvatar;
    EditText    edtName;
    View        optMale, optFemale;

    EditText    edtWeight;
    View        optLBS, optKG;

    EditText    edtHeightFeet, edtHeightInch, edtHeightMeter;
    View        optFeet, optMeter;

    SeekBar     seekbar;
    TextView    tvAge;

    ImageView   activity_low,activity_medium,activity_high;

    EditText    edtEnergyRequired;
    TextView    tvBMI;

    View asteriskName, asteriskWeight, asteriskHeight;

    View bmiMeter;
    ImageView ivBMIBoard, ivBMIPointer;
    View llEnergyRequired;
    int bdWidth, bdHeight, ptWidth, ptHeight;

    float m_previousRotation = 0;

    User m_user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        TextView tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvTitle.setText(R.string.menu4b_t40_edit);
        ImageView btnChangeUser = (ImageView) findViewById(R.id.btnLeft);
        btnChangeUser.setImageResource(R.drawable.btn_changeuser_draw);
        btnChangeUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( validateInputValues() && m_user.setAsDefaultUser() ) {
                    startActivity(new Intent(UserProfileActivity.this, UserListActivity.class));
                    finish();
                }
            }
        });

        final int openMode = getIntent().getIntExtra("open_mode", OPEN_FOR_CUR_USER);
        if(openMode == OPEN_FOR_NEW_USER) {
            btnChangeUser.setVisibility(View.GONE);
        }
        else if ( openMode == OPEN_FOR_ADD_USER ){

        }
        else {
            if ( openMode == OPEN_FOR_CUR_USER ) {
                m_user = User.getDefaultUser();
            }
            else {
                long id = getIntent().getLongExtra("user_id", 0);
                m_user = User.load(User.class, id);
            }
        }

        if ( m_user == null )
            m_user = new User();

        View btnHome = findViewById(R.id.btnRight);
        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ( validateInputValues() && m_user.setAsDefaultUser() ) {
                    AppConfig.gotoHome(UserProfileActivity.this);
                }
            }
        });

        asteriskName = findViewById(R.id.asteriskName);
        asteriskWeight = findViewById(R.id.asteriskWeight);
        asteriskHeight = findViewById(R.id.asteriskHeight);

        edtName = (EditText) findViewById(R.id.edtName);
        ivAvatar = (ImageView) findViewById(R.id.ivAvatar);
        optMale = findViewById(R.id.optMale);
        optFemale = findViewById(R.id.optFemale);

        edtWeight = (EditText) findViewById(R.id.edtWeight);
        optLBS = findViewById(R.id.optLBS);
        optKG = findViewById(R.id.optKG);

        edtHeightFeet = (EditText) findViewById(R.id.edtHeightFeet);
        edtHeightInch = (EditText) findViewById(R.id.edtHeightInch);
        edtHeightMeter = (EditText) findViewById(R.id.edtHeightMeter);
        optFeet = findViewById(R.id.optFeet);
        optMeter = findViewById(R.id.optMeter);

        seekbar= (SeekBar) findViewById(R.id.age_seek_bar);
        tvAge = (TextView) findViewById(R.id.tvAge);

        activity_low= (ImageView) findViewById(R.id.activity_low);
        activity_medium= (ImageView) findViewById(R.id.activity_medium);
        activity_high= (ImageView) findViewById(R.id.activity_high);

        edtEnergyRequired = (EditText) findViewById(R.id.edtEnergyRequired);
        tvBMI = (TextView) findViewById(R.id.tvBMI);

        bmiMeter = findViewById(R.id.bmiMeter);
        ivBMIBoard = (ImageView) findViewById(R.id.ivBMIBoard);
        ivBMIPointer = (ImageView) findViewById(R.id.ivBMIPointer);
        llEnergyRequired = findViewById(R.id.llEnergyRequired);

        Drawable d = getResources().getDrawable(R.drawable.pointer_normal);
        ptWidth = d.getIntrinsicWidth();
        ptHeight = d.getIntrinsicHeight();
        d = getResources().getDrawable(R.drawable.balance_asian);
        bdWidth = d.getIntrinsicWidth();
        bdHeight = d.getIntrinsicHeight();
        int ptTopMargin = bdHeight / 5;
        int hideSize = ptTopMargin + ptHeight - bdHeight;
        RelativeLayout.LayoutParams rlParam = (RelativeLayout.LayoutParams) bmiMeter.getLayoutParams();
        rlParam.bottomMargin = - hideSize;
        rlParam = (RelativeLayout.LayoutParams) ivBMIPointer.getLayoutParams();
        rlParam.topMargin = ptTopMargin;

        ivAvatar.setOnClickListener(this);
        optMale.setOnClickListener(this);
        optFemale.setOnClickListener(this);
        optLBS.setOnClickListener(this);
        optKG.setOnClickListener(this);
        optFeet.setOnClickListener(this);
        optMeter.setOnClickListener(this);
        activity_low.setOnClickListener(this);
        activity_medium.setOnClickListener(this);
        activity_high.setOnClickListener(this);
        findViewById(R.id.btnDecAge).setOnClickListener(this);
        findViewById(R.id.btnIncAge).setOnClickListener(this);
        findViewById(R.id.asian_bmi).setOnClickListener(this);
        findViewById(R.id.non_asian_bmi).setOnClickListener(this);

        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean fromUser) {
                //if ( fromUser )
                {
                    if ( i  >= 7 )
                        tvAge.setText(i + "");
                    else
                        seekBar.setProgress(7);

                    if ( i >= 80 ) {
                        activity_low.performClick();
                        activity_medium.setEnabled(false);
                        activity_high.setEnabled(false);
                    }
                    else if ( i >= 60 ) {
                        activity_medium.setEnabled(true);
                        activity_high.setEnabled(false);
                        if ( m_user.activityLevel == User.ACTIVITY_LEVEL_HIGH )
                            activity_medium.performClick();
                    }
                    else {
                        activity_medium.setEnabled(true);
                        activity_high.setEnabled(true);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });



        if (m_user.getId() == null) {
            optMale.performClick();
            optKG.performClick();
            optMeter.performClick();
            activity_low.performClick();

            bmiMeter.setVisibility(View.GONE);
            llEnergyRequired.setVisibility(View.GONE);

            m_user.gender = User.GENDER_MALE;
            m_user.weightUnit = User.WEIGHT_UNIT_KG;
            m_user.heightUnit = User.HEIGHT_UNIT_METER;
            m_user.activityLevel = User.ACTIVITY_LEVEL_LOW;
            m_user.isAsian = true;
        }
        else {
            Bitmap avatar = m_user.getAvatar();
            if ( avatar != null )
                ivAvatar.setImageBitmap(avatar);

            edtName.setText(m_user.name);
            setTwoOption(optMale, optFemale, m_user.gender == User.GENDER_MALE);
            edtWeight.setText(format(m_user.weightUnit == User.WEIGHT_UNIT_KG ? m_user.weight : m_user.weight * 2.20462f));
            setTwoOption(optLBS, optKG, m_user.weightUnit == User.WEIGHT_UNIT_LBS);

            if ( m_user.heightUnit == User.HEIGHT_UNIT_METER ) {
                edtHeightFeet.setVisibility(View.GONE);
                edtHeightInch.setVisibility(View.GONE);

                edtHeightMeter.setText(format(m_user.height));
            }
            else {
                edtHeightMeter.setVisibility(View.GONE);

                float inches = m_user.height * 39.3701f;
                int foot = (int) (inches / 12);
                int inch = (int) (inches % 12);
                edtHeightFeet.setText(foot + "");
                edtHeightInch.setText(inch + "");
            }
            setTwoOption(optFeet, optMeter, m_user.heightUnit == User.HEIGHT_UNIT_FEET);

            tvAge.setText(m_user.age + "");
            seekbar.setProgress(m_user.age);
            setActivityLevel(m_user.activityLevel);
            edtEnergyRequired.setText(m_user.energyRequired + "");

            updateBMI();
        }

        edtName.addTextChangedListener(new CustomTextWatcher(edtName, asteriskName));
        edtWeight.addTextChangedListener(new CustomTextWatcher(edtWeight, asteriskWeight));
        edtHeightMeter.addTextChangedListener(new CustomTextWatcher(edtHeightMeter, asteriskHeight));

        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if ( edtHeightFeet.getText().length() == 0 && edtHeightInch.getText().length() == 0) {
                    edtHeightFeet.setBackgroundResource(R.drawable.txtfield_error);
                    edtHeightInch.setBackgroundResource(R.drawable.txtfield_error);
                    asteriskHeight.setVisibility(View.VISIBLE);
                }
                else {
                    edtHeightFeet.setBackgroundResource(R.drawable.txtfield_normal);
                    edtHeightInch.setBackgroundResource(R.drawable.txtfield_normal);
                    asteriskHeight.setVisibility(View.INVISIBLE);
                }
            }
        };
        edtHeightFeet.addTextChangedListener(watcher);
        edtHeightInch.addTextChangedListener(watcher);
    }


    private boolean validateInputValues() {

        m_user.name = edtName.getText().toString();
        float weight = parseFloat(edtWeight.getText().toString());
        if ( m_user.weightUnit == User.WEIGHT_UNIT_KG )
            m_user.weight = weight;
        else
            m_user.weight = weight * 2.20462f;

        if ( m_user.heightUnit == User.HEIGHT_UNIT_METER )
            m_user.height = parseFloat(edtHeightMeter.getText().toString());
        else {
            float feet = 0, inch = 0;
            if (edtHeightFeet.getText().toString().length() > 0)
                feet = parseFloat(edtHeightFeet.getText().toString());
            if (edtHeightInch.getText().toString().length() > 0)
                inch = parseFloat(edtHeightInch.getText().toString());
            m_user.height = (feet * 12 + inch) * 0.0254f;
        }

        if ( TextUtils.isEmpty(m_user.name) ) {
            edtName.setBackgroundResource(R.drawable.txtfield_error);
            asteriskName.setVisibility(View.VISIBLE);
        }
        else
            edtName.setBackgroundResource(R.drawable.txtfield_normal);

        if ( weight == 0 ) {
            edtWeight.setBackgroundResource(R.drawable.txtfield_error);
            asteriskWeight.setVisibility(View.VISIBLE);
        }
        else
            edtWeight.setBackgroundResource(R.drawable.txtfield_normal);

        if ( m_user.height == 0 ) {
            if ( m_user.heightUnit == User.HEIGHT_UNIT_METER ) {
                edtHeightMeter.setBackgroundResource(R.drawable.txtfield_error);
                asteriskHeight.setVisibility(View.VISIBLE);
            }
            else {
                edtHeightFeet.setBackgroundResource(R.drawable.txtfield_error);
                edtHeightInch.setBackgroundResource(R.drawable.txtfield_error);
                asteriskHeight.setVisibility(View.VISIBLE);
            }
        }

        int nErrMsg = 0;
        //m_user.name = edtName.getText().toString();
        if ( TextUtils.isEmpty(m_user.name) ) {
            nErrMsg += 1;
        }

        //float weight = parseFloat(edtWeight.getText().toString());
        if ( weight == 0 ) {
            nErrMsg += 2;
        }

        if ( m_user.height == 0 ) {
            nErrMsg += 4;
        }

        if ( nErrMsg > 0 ) {
            int errMsgs[] = {R.string.menu4b_d02, R.string.menu4b_d03, R.string.menu4b_d05, R.string.menu4b_d04,
                    R.string.menu4b_d06, R.string.menu4b_d07, R.string.menu4b_d08};
            AppConfig.showMessageDialog(this, errMsgs[nErrMsg-1]);
            return false;
        }

        if ( m_user.getId() == null ) {
            User user = new Select().from(User.class).where("name='" + m_user.name + "'").executeSingle();
            if (user != null) {
                edtName.setBackgroundResource(R.drawable.txtfield_error);
                asteriskName.setVisibility(View.VISIBLE);
                AppConfig.showMessageDialog(this, R.string.menu4b_d01);
                return false;
            }
        }

        if ( m_user.weightUnit == User.WEIGHT_UNIT_KG ) {
            m_user.weight = weight;
            if ( weight > 200 ) {
                AppConfig.showMessageDialog(this, R.string.menu4b_d10);
                edtWeight.setBackgroundResource(R.drawable.txtfield_error);
                asteriskWeight.setVisibility(View.VISIBLE);
                return false;
            }
        }
        else {
            m_user.weight = weight * 2.20462f;
            if ( weight > 400 ) {
                AppConfig.showMessageDialog(this, R.string.menu4b_d09);
                edtWeight.setBackgroundResource(R.drawable.txtfield_error);
                asteriskWeight.setVisibility(View.VISIBLE);
                return false;
            }
        }

        if ( m_user.heightUnit == User.HEIGHT_UNIT_METER ) {
            m_user.height = parseFloat(edtHeightMeter.getText().toString());
            if ( m_user.height > 3 ) {
                AppConfig.showMessageDialog(this, R.string.menu4b_d11);
                edtHeightMeter.setBackgroundResource(R.drawable.txtfield_error);
                asteriskHeight.setVisibility(View.VISIBLE);
                return false;
            }
        }
        else {
            float feet = 0, inch = 0;
            if (edtHeightFeet.getText().toString().length() > 0)
                feet = parseFloat(edtHeightFeet.getText().toString());
            if (edtHeightInch.getText().toString().length() > 0)
                inch = parseFloat(edtHeightInch.getText().toString());
            m_user.height = (feet * 12 + inch) * 0.0254f;
            if ( feet >= 10 ) {
                AppConfig.showMessageDialog(this, R.string.menu4b_d12);
                edtHeightFeet.setBackgroundResource(R.drawable.txtfield_error);
                edtHeightInch.setBackgroundResource(R.drawable.txtfield_error);
                asteriskHeight.setVisibility(View.VISIBLE);
                return false;
            }
        }

        m_user.age = seekbar.getProgress();
        m_user.energyRequired = parseInt(edtEnergyRequired.getText().toString());

        return true;
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {

            case R.id.ivAvatar:
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, PICK_IMAGE_REQUEST);
                break;

            case R.id.optMale:
                m_user.gender = User.GENDER_MALE;
                setTwoOption(optMale, optFemale, true);
                break;

            case R.id.optFemale:
                m_user.gender = User.GENDER_FEMALE;
                setTwoOption(optMale, optFemale, false);
                break;

            case R.id.optLBS:
                setTwoOption(optLBS, optKG, true);
                if ( m_user.weightUnit == User.WEIGHT_UNIT_KG ) {
                    m_user.weightUnit = User.WEIGHT_UNIT_LBS;
                    float weight = parseFloat(edtWeight.getText().toString());
                    weight = weight * 2.20462f;
                    edtWeight.setText(format(weight));
                }
                break;

            case R.id.optKG:
                setTwoOption(optLBS, optKG, false);
                if ( m_user.weightUnit == User.WEIGHT_UNIT_LBS ) {
                    m_user.weightUnit = User.WEIGHT_UNIT_KG;
                    float weight = parseFloat(edtWeight.getText().toString());
                    weight = weight * 0.453592f;
                    edtWeight.setText(format(weight));
                }
                break;

            case R.id.optFeet:
                if ( m_user.heightUnit == User.HEIGHT_UNIT_METER ) {
                    m_user.heightUnit = User.HEIGHT_UNIT_FEET;
                    setTwoOption(optFeet, optMeter, true);
                    edtHeightFeet.setVisibility(View.VISIBLE);
                    edtHeightInch.setVisibility(View.VISIBLE);
                    edtHeightMeter.setVisibility(View.GONE);
                    if (edtHeightMeter.getText().toString().length() > 0) {
                        float height = parseFloat(edtHeightMeter.getText().toString());
                        float inches = height / 0.0254f;
                        int foot = (int) (inches / 12);
                        int inch = (int) Math.round(inches % 12);
                        edtHeightFeet.setText(foot + "");
                        edtHeightInch.setText(inch + "");
                    }
                }
                break;

            case R.id.optMeter:
                if ( m_user.heightUnit == User.HEIGHT_UNIT_FEET ) {
                    m_user.heightUnit = User.HEIGHT_UNIT_METER;
                    setTwoOption(optFeet, optMeter, false);
                    edtHeightFeet.setVisibility(View.GONE);
                    edtHeightInch.setVisibility(View.GONE);
                    edtHeightMeter.setVisibility(View.VISIBLE);
                    float feet = 0, inch = 0;
                    if (edtHeightFeet.getText().toString().length() > 0)
                        feet = parseFloat(edtHeightFeet.getText().toString());
                    if (edtHeightInch.getText().toString().length() > 0)
                        inch = parseFloat(edtHeightInch.getText().toString());
                    edtHeightMeter.setText(format((feet * 12 + inch) * 0.0254f));
                }
                break;

            case R.id.btnDecAge: {
                int i = parseInt(tvAge.getText().toString());
                if (i > 7) {
                    i--;
                    tvAge.setText(i + "");
                    seekbar.setProgress(i);
                }
            }
                break;

            case R.id.btnIncAge: {
                int i = parseInt(tvAge.getText().toString());
                if (i < 120) {
                    i++;
                    tvAge.setText(i + "");
                    seekbar.setProgress(i);
                }
            }
                break;

            case R.id.activity_low:
                setActivityLevel(User.ACTIVITY_LEVEL_LOW);
                break;

            case R.id.activity_medium:
                setActivityLevel(User.ACTIVITY_LEVEL_MEDIUM);
                break;

            case R.id.activity_high:
                setActivityLevel(User.ACTIVITY_LEVEL_HIGH);
                break;

            case R.id.asian_bmi:
                ivBMIBoard.setImageResource(R.drawable.balance_asian);
                m_user.isAsian = true;
                if ( !validateInputValues() )
                    return;

                if ( bmiMeter.getVisibility() == View.VISIBLE )
                    updateBMI();
                else
                    showBMIMeter();
                break;

            case R.id.non_asian_bmi:
                ivBMIBoard.setImageResource(R.drawable.balance_nonasian);
                m_user.isAsian = false;
                if ( !validateInputValues() )
                    return;

                if ( bmiMeter.getVisibility() == View.VISIBLE )
                    updateBMI();
                else
                    showBMIMeter();
                break;
        }
    }

    private void showBMIMeter() {
        int nTop = bmiMeter.getTop();
        TranslateAnimation anim = new TranslateAnimation(bmiMeter.getLeft(), bmiMeter.getLeft(),
                nTop + bdHeight + llEnergyRequired.getHeight(), nTop);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                bmiMeter.setVisibility(View.VISIBLE);
                llEnergyRequired.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                updateBMI();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        anim.setDuration(800);
        bmiMeter.startAnimation(anim);
    }

    private void updateBMI() {
        float weight = m_user.weight;
        float height = m_user.height;
        float bmi = weight / (height * height);

        // -19.5 == 18.5 BMI
        //  13.5 == 23   BMI
        //  53 (max) 14.64 ~ 26.86
        float unit = (19.5f + 13.5f) / ( 23 - 18.5f );
        float newRotation = unit * (bmi - 20.25f);
        newRotation = newRotation > 53 ? 53 : newRotation;
        newRotation = newRotation < -53 ? - 53 : newRotation;

        float bmidegree = 10 + (bmi - 22)*(110.f/13);
        newRotation = bmidegree <= - 55 ? -55 : bmidegree >= 55 ? 55 : bmidegree;

        RotateAnimation rotAnim = new RotateAnimation(m_previousRotation, newRotation, ptWidth / 2, ptHeight / 2);
        rotAnim.setDuration(1000);
        rotAnim.setStartOffset(200);
        rotAnim.setFillAfter(true);
        ivBMIPointer.startAnimation(rotAnim);

        tvBMI.setText(String.format("BMI: %.1f", bmi));

        if ( bmi < 18.5 ) {
            ivBMIPointer.setImageResource(R.drawable.pointer_underweight);
            AppConfig.showMessageDialog(this, R.string.menu4b_d13);
        }
        else if ( (m_user.isAsian && bmi < 23)||(!m_user.isAsian && bmi < 25) ) {
            ivBMIPointer.setImageResource(R.drawable.pointer_normal);
        }
        else {
            ivBMIPointer.setImageResource(R.drawable.pointer_overweight);
            AppConfig.showMessageDialog(this, R.string.menu4b_d13);
        }

        m_user.weight = weight;
        m_user.age = seekbar.getProgress();
        m_user.energyRequired = m_user.dailyEnergyRequired();
        edtEnergyRequired.setText(m_user.energyRequired + "");

        m_previousRotation = newRotation;
    }

    private int parseInt(String str) {
        int ret = 0;
        try {
            ret = Integer.parseInt(str);
        }
        catch(Exception e) {}
        return ret;
    }
    private float parseFloat(String str) {
        float ret = 0;
        try {
            ret = Float.parseFloat(str);
        }
        catch (Exception e){}

        return ret;
    }

    private String format(float value) {
        return value < 0.01 ? "" : String.format("%.02f", value);
    }

    public void setTwoOption(View optLeft, View optRight, boolean onLeft){
        if ( onLeft ) {
            optLeft.setBackgroundResource(R.drawable.btn_left_on);
            optRight.setBackgroundResource(R.drawable.btn_right_off);
        }
        else {
            optLeft.setBackgroundResource(R.drawable.btn_left_off);
            optRight.setBackgroundResource(R.drawable.btn_right_on);
        }
    }

    public void setActivityLevel(int activityLevel){
        m_user.activityLevel = activityLevel;
        switch (activityLevel){
            case User.ACTIVITY_LEVEL_LOW:
                activity_low.setImageResource(R.drawable.btn_pal_low_on);
                activity_medium.setImageResource(R.drawable.btn_pal_medium_off);
                activity_high.setImageResource(R.drawable.btn_pal_high_off);
                break;
            case User.ACTIVITY_LEVEL_MEDIUM:
                activity_low.setImageResource(R.drawable.btn_pal_low_off);
                activity_medium.setImageResource(R.drawable.btn_pal_medium_on);
                activity_high.setImageResource(R.drawable.btn_pal_high_off);;
                break;
            case User.ACTIVITY_LEVEL_HIGH:
                activity_low.setImageResource(R.drawable.btn_pal_low_off);
                activity_medium.setImageResource(R.drawable.btn_pal_medium_off);
                activity_high.setImageResource(R.drawable.btn_pal_high_on);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                float ratio = (float)bitmap.getWidth() / bitmap.getHeight();
                Log.e("###", "bitmap size = " + bitmap.getWidth() + " x " + bitmap.getHeight());
                int avatarSize = getResources().getDimensionPixelSize(R.dimen.profile_photo_size);
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, (int)(avatarSize * ratio), avatarSize, false);
                ivAvatar.setImageBitmap(scaledBitmap);
                m_user.setAvatar(scaledBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
