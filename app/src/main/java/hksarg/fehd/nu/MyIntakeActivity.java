package hksarg.fehd.nu;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import hksarg.fehd.nu.model.Food;
import hksarg.fehd.nu.model.NuHist;
import hksarg.fehd.nu.model.User;

public class MyIntakeActivity extends AppCompatActivity {

    TextView tvDailyEnergy, tvRecordedTime;
    LinearLayout llFoods, llIndexContainer;
    View        llIndexHeader, btnCalculate, btnSave;

    User        m_user;
    List<Food>  m_foods;
    List<FoodViewHolder> m_foodItemViews;

    NuHist m_nuHist;

    String UNIT_GRAM;
    String UNIT_ML;
    String UNIT_GRAM_ML;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        UNIT_GRAM = getResources().getString(R.string.menu2a_text5c_g);
        UNIT_ML = getResources().getString(R.string.menu2a_text5c_ml);
        UNIT_GRAM_ML = getString(R.string.menu2a_text5c_g_ml);

        long nuHistId = getIntent().getLongExtra("nu_hist_id", 0);
        m_nuHist = NuHist.load(NuHist.class, nuHistId);
        if ( m_nuHist != null ) {
            setContentView(R.layout.activity_my_intake_show);
        }
        else {
            setContentView(R.layout.activity_my_intake_calc);
        }

        TextView tvTitle = (TextView) findViewById(R.id.tvTitle);
        findViewById(R.id.btnLeft).setVisibility(View.INVISIBLE);
        findViewById(R.id.btnRight).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppConfig.gotoHome(MyIntakeActivity.this);
            }
        });

        tvDailyEnergy = (TextView) findViewById(R.id.tvDailyEnergy);
        tvRecordedTime = (TextView) findViewById(R.id.tvRecordedTime);
        llFoods = (LinearLayout) findViewById(R.id.llFoods);
        llIndexContainer = (LinearLayout) findViewById(R.id.llIndexContainer);
        llIndexHeader = findViewById(R.id.llIndexHeader);
        btnCalculate = findViewById(R.id.btnCalculate);
        btnSave = findViewById(R.id.btnSave);

        ImageView   ivAvatar = (ImageView) findViewById(R.id.ivAvatar);
        TextView    tvUserName = (TextView) findViewById(R.id.tvUserName);
        m_user = User.getDefaultUser();
        if ( m_user == null ) {
            finish();
            return;
        }
        tvUserName.setText(m_user.name);
        Bitmap bmp = m_user.getAvatar();
        if ( bmp != null )
            ivAvatar.setImageBitmap(m_user.getAvatar());

        tvDailyEnergy.setText(m_user.energyRequired + getString(R.string.menu2a_text6c));

        m_foodItemViews = new ArrayList<FoodViewHolder>();

        if ( m_nuHist == null ) {

            m_foods = Food.getSelectedFoods();
            if ( m_foods == null || m_foods.size() == 0 ) {
                finish();
                return;
            }

            m_nuHist = new NuHist();

            btnCalculate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    calculateNutritions();
                }
            });
            btnSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    m_nuHist.userId = m_user.getId();
                    m_nuHist.recordTime = new Date();
                    m_nuHist.save();
                    finish();
                    startActivity(new Intent(MyIntakeActivity.this, MyIntakeHistoryActivity.class));
                }
            });

            tvTitle.setText(R.string.menu3a_t30_add);
            llIndexHeader.setVisibility(View.INVISIBLE);
            btnSave.setVisibility(View.GONE);

            Iterator<Food> itr = m_foods.iterator();
            while (itr.hasNext()) {
                Food food = itr.next();
                View item = LayoutInflater.from(this).inflate(R.layout.item_intake_food_edit, null);
                m_foodItemViews.add(new FoodViewHolder(item, food, false));
                llFoods.addView(item);
            }
        }
        else {
            tvTitle.setText(R.string.menu3a_t30_read);
            tvRecordedTime.setText(m_nuHist.getRecordTimeString(tvRecordedTime.getContext()));

            int nCount = m_nuHist.foodList.size();
            for ( int i = 0; i < nCount; i++) {
                Food food = Food.load(Food.class, m_nuHist.foodList.keyAt(i));
                if ( food != null ) {
                    int amount = m_nuHist.foodList.valueAt(i);
                    View item = LayoutInflater.from(this).inflate(R.layout.item_intake_food_view, null);
                    FoodViewHolder holder = new FoodViewHolder(item, food, true);
                    holder.setAmount(amount);
                    m_foodItemViews.add(holder);
                    llFoods.addView(item);
                }
            }

            generateIndexViews();
        }
    }

    private void calculateNutritions() {

        boolean invalidInput = false;
        Iterator<FoodViewHolder> itr = m_foodItemViews.iterator();
        while( itr.hasNext() ) {
            FoodViewHolder holder = itr.next();
            int amount = holder.getAmount();
            if (holder.edtAmount != null && holder.edtAmount.length() == 0) {
                holder.edtAmount.setBackgroundResource(R.drawable.txtfield_error);
                holder.asteriskView.setVisibility(View.VISIBLE);
                invalidInput = true;
            }
            m_nuHist.putFood(holder.foodData.getId().intValue(), amount);
        }

        if ( invalidInput )
            return;

        btnCalculate.setVisibility(View.GONE);
        btnSave.setVisibility(View.VISIBLE);

        generateIndexViews();
    }

    private String format(float value) {
        return String.format("%.02f", value);
    }

    private void generateIndexViews() {
        llIndexHeader.setVisibility(View.VISIBLE);
        llIndexContainer.removeAllViews();

        float totalEnergy = 0, totalProtein = 0, totalFat = 0,
                totalSatFat = 0, totalTransFat = 0, totalCholesterol = 0,
                totalCarbohydrate = 0, totalFibre = 0, totalSugar = 0, totalSodium = 0;

        Iterator<FoodViewHolder> itr = m_foodItemViews.iterator();
        while( itr.hasNext() ) {
            FoodViewHolder holder = itr.next();
            Food food = holder.foodData;
            float energySize = Math.max(0, food.energySize);
            float protein = Math.max(0, food.protein);
            float foodFat = Math.max(0, food.totalFat);
            float saturatedFat = Math.max(0, food.saturatedFat);
            float transFat = Math.max(0, food.transFat);
            float cholesterol = Math.max(0, food.cholesterol);
            float carbohydrate = Math.max(0, food.carbohydrate);
            float dietaryFibre = Math.max(0, food.dietaryFibre);
            float sugar = Math.max(0, food.sugar);
            float sodium = Math.max(0, food.sodium);

            float coef = holder.getAmount() / (float)food.packageSize;
            totalEnergy = totalEnergy + coef * (food.energyUnit == Food.ENERGY_UNIT_KCAL ? energySize : energySize / 4.184f );
            totalProtein = totalProtein + coef * protein;
            totalFat = totalFat + coef * foodFat;
            totalSatFat = totalSatFat + coef * saturatedFat;
            totalTransFat = totalTransFat + coef * transFat;
            totalCholesterol = totalCholesterol + coef * cholesterol;
            totalFibre = totalFibre + coef * dietaryFibre;
            totalSugar = totalSugar + coef * sugar;
            totalSodium = totalSodium + coef * sodium;
            if ( food.carbohydrateType == Food.CARBO_TYPE_TOTAL )
                totalCarbohydrate = totalCarbohydrate + coef * Math.max(0, carbohydrate - dietaryFibre);
            else
                totalCarbohydrate = totalCarbohydrate + coef * carbohydrate;
        }

        String UNIT_KCAL = getString(R.string.menu2a_text6c);
        String UNIT_G = getString(R.string.menu2a_text5c_g);
        String UNIT_MG = getString(R.string.menu2a_text11c);

        IndexViewHolder holder;
        holder = new IndexViewHolder(this, R.string.menu3a_t30_t2_row0_col1,
                format(totalEnergy) + UNIT_KCAL, totalEnergy / m_user.energyIntake(), true);
        llIndexContainer.addView(holder.container);
        m_nuHist.isOver = holder.isOver();

        holder = new IndexViewHolder(this, R.string.menu3a_t30_t2_row1_col1,
                format(totalProtein) + UNIT_G, totalProtein / m_user.proteinIntake(), false);
        llIndexContainer.addView(holder.container);
        m_nuHist.isOver = m_nuHist.isOver || holder.isOver();

        holder = new IndexViewHolder(this, R.string.menu3a_t30_t2_row2_col1,
                format(totalFat) + UNIT_G, totalFat / m_user.totalFatIntake(), true);
        llIndexContainer.addView(holder.container);
        m_nuHist.isOver = m_nuHist.isOver || holder.isOver();

        holder = new IndexViewHolder(this, R.string.menu3a_t30_t2_row3_col1,
                format(totalSatFat) + UNIT_G, totalSatFat / m_user.saturatedFatIntake(), false);
        llIndexContainer.addView(holder.container);
        m_nuHist.isOver = m_nuHist.isOver || holder.isOver();

        holder = new IndexViewHolder(this, R.string.menu3a_t30_t2_row4_col1,
                format(totalTransFat) + UNIT_G, totalTransFat / m_user.transFatIntake(), true);
        llIndexContainer.addView(holder.container);
        m_nuHist.isOver = m_nuHist.isOver || holder.isOver();

        holder = new IndexViewHolder(this, R.string.menu3a_t30_t2_row5_col1,
                format(totalCholesterol) + UNIT_MG, totalCholesterol / m_user.cholesterolIntake(), false);
        llIndexContainer.addView(holder.container);
        m_nuHist.isOver = m_nuHist.isOver || holder.isOver();

        holder = new IndexViewHolder(this, R.string.menu3a_t30_t2_row6_col1,
                format(totalCarbohydrate) + UNIT_MG, totalCarbohydrate / m_user.carbohydrateIntake(), true);
        llIndexContainer.addView(holder.container);
        m_nuHist.isOver = m_nuHist.isOver || holder.isOver();

        holder = new IndexViewHolder(this, R.string.menu3a_t30_t2_row7_col1,
                format(totalFibre) + UNIT_MG, totalFibre / m_user.fibreIntake(), false);
        llIndexContainer.addView(holder.container);
        m_nuHist.isOver = m_nuHist.isOver || holder.isOver();

        holder = new IndexViewHolder(this, R.string.menu3a_t30_t2_row8_col1,
                format(totalSugar) + UNIT_G, totalSugar / m_user.sugarIntake(), true);
        llIndexContainer.addView(holder.container);
        m_nuHist.isOver = m_nuHist.isOver || holder.isOver();

        holder = new IndexViewHolder(this, R.string.menu3a_t30_t2_row9_col1,
                format(totalSodium) + UNIT_MG, totalSodium / m_user.sodiumIntake(), false);
        llIndexContainer.addView(holder.container);
        m_nuHist.isOver = m_nuHist.isOver || holder.isOver();
    }

    class FoodViewHolder {
        TextView tvFoodName;
        EditText edtAmount;
        View asteriskView;
        TextView tvAmount;
        TextView tvAmountUnit;
        TextView tvPackageUnit;
        Food foodData;
        public FoodViewHolder(View view, Food food, boolean readOnly) {

            foodData = food;
            tvFoodName = (TextView) view.findViewById(R.id.tvFoodName);
            tvAmountUnit = (TextView) view.findViewById(R.id.tvAmountUnit);
            tvPackageUnit = (TextView) view.findViewById(R.id.tvPackageUnit);

            //String szUnit = foodData.packageUnit == Food.PACKAGE_UNIT_G ? UNIT_GRAM : UNIT_ML;
            if ( readOnly ) {
                tvAmount = (TextView) view.findViewById(R.id.tvAmount);
            }
            else {
                edtAmount = (EditText) view.findViewById(R.id.edtAmount);
                asteriskView = view.findViewById(R.id.asteriskView);
                edtAmount.addTextChangedListener(new CustomTextWatcher(edtAmount, asteriskView));
            }
            tvFoodName.setText(foodData.name);
            tvAmountUnit.setText(UNIT_GRAM_ML);
            tvPackageUnit.setText((int)foodData.packageSize + UNIT_GRAM_ML);
        }

        public void setAmount(int amount) {
            tvAmount.setText(amount + "");
        }

        public int getAmount() {
            String szAmount = edtAmount == null ? tvAmount.getText().toString() : edtAmount.getText().toString();
            int ret = 0;
            try {
                ret = Integer.parseInt(szAmount);
            }
            catch (Exception e) {}

            return ret;
        }
    }

    class IndexViewHolder {
        View container;
        TextView tvIndexName;
        TextView tvAmount;
        TextView tvPercent;
        ProgressBar pgPercent;
        View ll100Mark;
        int  progress;
        public IndexViewHolder(Context context, int nResIndexName, String szAmount, float percent, boolean bgWhite) {
            container = LayoutInflater.from(context).inflate(R.layout.item_intake_index, null);
            tvIndexName = (TextView) container.findViewById(R.id.tvIndexName);
            tvAmount = (TextView) container.findViewById(R.id.tvAmount);
            tvPercent = (TextView) container.findViewById(R.id.tvPercent);
            ll100Mark = container.findViewById(R.id.ll100Mark);
            pgPercent = (ProgressBar) container.findViewById(R.id.progress);

            if ( !bgWhite )
                container.setBackgroundColor(getResources().getColor(R.color.color_sky));

            tvIndexName.setText(nResIndexName);
            tvAmount.setText(szAmount);
            progress = (int)Math.round(percent * 100);
            tvPercent.setText(progress + "%");
            if ( percent > 1 ) {
                progress = 100 + (progress - 100) / 4;
                ll100Mark.setVisibility(View.INVISIBLE);
                tvAmount.setTextColor(getResources().getColor(R.color.color_over));
                tvPercent.setTextColor(getResources().getColor(R.color.color_over));

                pgPercent.setProgress(100);
                pgPercent.setSecondaryProgress(progress);
            }
            else {
                tvAmount.setTextColor(getResources().getColor(R.color.color_normal));
                tvPercent.setTextColor(getResources().getColor(R.color.color_normal));

                pgPercent.setProgress(progress);
                pgPercent.setSecondaryProgress(0);
            }
        }

        public boolean isOver() {
            return progress > 100;
        }
    }
}
