package com.gtsr.gtsr.testModule;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gtsr.gtsr.AlertShowingDialog;
import com.gtsr.gtsr.HomeActivity;
import com.gtsr.gtsr.LanguagesKeys;
import com.gtsr.gtsr.R;
import com.gtsr.gtsr.RefreshShowingDialog;
import com.gtsr.gtsr.dataController.LanguageTextController;
import com.gtsr.gtsr.dataController.UrineTestDataCreatorController;
import com.gtsr.gtsr.database.TestFactorDataController;
import com.gtsr.gtsr.database.TestFactors;
import com.gtsr.gtsr.database.UrineResultsDataController;
import com.spectrochips.spectrumsdk.FRAMEWORK.SCConnectionHelper;
import com.spectrochips.spectrumsdk.FRAMEWORK.SCFileHelper;
import com.spectrochips.spectrumsdk.FRAMEWORK.SCTestAnalysis;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.OnClick;
//import de.hdodenhof.circleimageview.CircleImageView;

import static com.gtsr.gtsr.LanguagesKeys.BILLIBURIN_DESCRIPTION_RESULT_KEY;
import static com.gtsr.gtsr.LanguagesKeys.GLOUCOSE_DESCRIPTION_KEY;
import static com.gtsr.gtsr.LanguagesKeys.KETONS_DESCRIPTION_KEY;
import static com.gtsr.gtsr.LanguagesKeys.LECHOCYTE_DISCRIPTION_KEY;
import static com.gtsr.gtsr.LanguagesKeys.NITRATE_DISCRIPTION_KEY;
import static com.gtsr.gtsr.LanguagesKeys.OCCULT_BLOOD_DESCRIPTION_RESULT_KEY;
import static com.gtsr.gtsr.LanguagesKeys.PH_DISCRIPTION_KEY;
import static com.gtsr.gtsr.LanguagesKeys.PROTINE_DESCRIPTION_KEY;
import static com.gtsr.gtsr.LanguagesKeys.SG_DESCRIPTION_KEY;
import static com.gtsr.gtsr.LanguagesKeys.UROBILLINGEN_DESCRIPTION_RESULT_KEY;

/**
 * Created by WAVE on 9/29/2017.
 */
public class ResultPageViewController extends AppCompatActivity {
    Toolbar toolbar;
    ImageView back, img_share, deleteButton, img_doctor;
    TextView tool_text, txt_currenrDate, txt_month, txt_name, clientid, txt_nodata, healthdata, userage, usergender;
    public int selectedPosition = -1;
    RecyclerView resultRecyclerView;
    ResultsTableViewCell resultsTableViewCell;
    public static Calendar resultCalendar;
    SimpleDateFormat simpleDateFormat, simpleMonthFormat, dateFormat/*, currentdateFormat*/;
    RelativeLayout rl_home, rl_nodata, rl_recycler, relativeshare;
    RefreshShowingDialog alertDilogue;
    ArrayList<String> unitTypeArray;
    ArrayList<TestFactors> testFactorsArrayList;
    private static final int REQUEST_WRITE_PERMISSION = 56;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resultpage);
        ButterKnife.bind(this);
        resultCalendar = Calendar.getInstance();
        simpleDateFormat = new SimpleDateFormat("EEEE,dd", Locale.ENGLISH); //for get Monday ,sunday and dd for date
        simpleMonthFormat = new SimpleDateFormat("MMMM yyyy", Locale.ENGLISH);//for get,month year
        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
       // currentdateFormat = new SimpleDateFormat("dd MMM yyyy hh:mm a", Locale.ENGLISH);
        alertDilogue = new RefreshShowingDialog(ResultPageViewController.this);
        //set current member testNameArray and reelation to textviws
        deleteButton = (ImageView) findViewById(R.id.btn_delete);
        txt_name = (TextView) findViewById(R.id.name);
        userage = (TextView) findViewById(R.id.userage);
        usergender = (TextView) findViewById(R.id.usergender);
        clientid = (TextView) findViewById(R.id.clientid);
        rl_nodata = (RelativeLayout) findViewById(R.id.rl_nodata);
        txt_nodata = (TextView) findViewById(R.id.txt_nodata);
        rl_recycler = (RelativeLayout) findViewById(R.id.rl_recycler);

        setToolbar();
        setResultRecyclerViewData();
        unitTypeArray = new ArrayList<String>();
        unitTypeArray.add("(mg/dl)");
        //  TabsGraphActivity.fillTestTypeArray();
        txt_currenrDate = (TextView) findViewById(R.id.txt_currentDate);
        loadCurrentDate();
        if (UrineResultsDataController.getInstance().currenturineresultsModel != null) {
            testFactorsArrayList = TestFactorDataController.getInstance().fetchTestFactorresults(UrineResultsDataController.getInstance().currenturineresultsModel);
        }
    }

    private void loadCurrentDate() {
       // Calendar calobj = Calendar.getInstance();
        if (UrineResultsDataController.getInstance().currenturineresultsModel != null) {
            try {
                txt_currenrDate.setText(UrineResultsDataController.getInstance().convertTestTimeTodate(UrineResultsDataController.getInstance().currenturineresultsModel.getTestedTime()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    public void setResultRecyclerViewData() {

        resultRecyclerView = (RecyclerView) findViewById(R.id.result_recycler);
        resultRecyclerView.setNestedScrollingEnabled(false);
        resultsTableViewCell = new ResultsTableViewCell();
        LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager(getApplicationContext(),
                LinearLayoutManager.VERTICAL, false);
        resultRecyclerView.setLayoutManager(horizontalLayoutManager);
        resultRecyclerView.setAdapter(resultsTableViewCell);
        resultRecyclerView.setMotionEventSplittingEnabled(false);
        resultsTableViewCell.notifyDataSetChanged();
        resultsTableViewCell.notifyItemChanged(selectedPosition);
        //  txt_month = (TextView) findViewById(R.id.txt_month);
    }

    public void setToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        back = (ImageView) findViewById(R.id.toolbar_icon);//Spectrum
        back.setBackgroundResource(R.drawable.back);
        tool_text = (TextView) toolbar.findViewById(R.id.toolbar_text);
        tool_text.setText(LanguageTextController.getInstance().currentLanguageDictionary.get(LanguagesKeys.TEST_RESULTS_TILE_KEY));
        img_share = (ImageView) toolbar.findViewById(R.id.img_share);
        relativeshare = (RelativeLayout) toolbar.findViewById(R.id.relativeshare);
        // relativeshare.setOnClickListener(mShareListener);
        img_share.setBackgroundResource(R.drawable.share);
        img_share.setOnClickListener(mShareListener);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SCConnectionHelper.getInstance().disconnectWithPeripheral();
                startActivity(new Intent(getApplicationContext(), HomeActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            }
        });
        rl_home = (RelativeLayout) findViewById(R.id.rootView);
    }

    public String getAge(int year, int month, int day) {
        Calendar dob = Calendar.getInstance();
        Calendar today = Calendar.getInstance();
        dob.set(year, month, day);
        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }
        Integer ageInt = new Integer(age);
        String ageS = ageInt.toString();
        return ageS;
    }



    View.OnClickListener mShareListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            startActivity(new Intent(getApplicationContext(),HtmltoPdfConversionActivity.class));
          /*  img_share.setVisibility(View.GONE);
            back.setVisibility(View.GONE);
            View viewScreen = img_share.getRootView();
            viewScreen.buildDrawingCache();
            viewScreen.setDrawingCacheEnabled(true);
            viewScreen.destroyDrawingCache();
            Bitmap screenshot1 = Bitmap.createBitmap(viewScreen.getWidth(), viewScreen.getHeight(), Bitmap.Config.RGB_565);
            viewScreen.draw(new Canvas(screenshot1));
            File mfile2 = savebitmap2(screenshot1);
            final Uri screenshotUri = Uri.fromFile(mfile2);

            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "GTSR");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "" + "GTSr Results");
            shareIntent.putExtra(Intent.EXTRA_STREAM, screenshotUri);
            shareIntent.setType("image/*");
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(shareIntent, "Sharing Status.."));

            img_share.setVisibility(View.VISIBLE);
            back.setVisibility(View.VISIBLE);*/
        }
    };



    private File savebitmap2(Bitmap bmp) {
        String temp = "UrineResultHistory";

        OutputStream outStream = null;
        String path = Environment.getExternalStorageDirectory()
                .toString();
        new File(path + "/SplashItTemp2").mkdirs();
        File file = new File(path + "/SplashItTemp2", temp + ".png");
        if (file.exists()) {
            file.delete();
            file = new File(path + "/SplashItTemp2", temp + ".png");
        }

        try {
            outStream = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, outStream);
            outStream.flush();
            outStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return file;
    }

    private void requestPermissions() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_PERMISSION);
        } else {

        }
    }


    public boolean isConn() {
        ConnectivityManager connectivity = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity.getActiveNetworkInfo() != null) {
            if (connectivity.getActiveNetworkInfo().isConnected())
                return true;
        }
        return false;
    }


    public class ResultsTableViewCell extends RecyclerView.Adapter<ResultsTableViewCell.ViewHolder> {

        public ResultsTableViewCell() {
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_result_items, parent, false);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            TestFactors objTestFactors = testFactorsArrayList.get(position);
            holder.testName.setText(objTestFactors.getTestName());

            holder.testValue.setText(objTestFactors.getResult());
            holder.unitsName.setText(objTestFactors.getUnit());

            holder.testrsult.setText(objTestFactors.getValue());

            Log.e("testresults","all"+holder.testrsult.getText().toString());
            holder.txt_bg.setBackgroundColor(changeLableColor(holder.testrsult.getText().toString()));
            holder.txt_bg1.setBackgroundColor(changeLableColor(holder.testrsult.getText().toString()));
            if (objTestFactors.isFlag()) {
                holder.testCondition.setText(LanguageTextController.getInstance().currentLanguageDictionary.get(LanguagesKeys.NORMAL_VALUE_KEY));
                holder.testCondition.setTextColor(Color.parseColor("#090A0B"));
                holder.imageView.setBackgroundResource(R.drawable.happiness);
            } else {
                holder.imageView.setBackgroundResource(R.drawable.sad);
                holder.testCondition.setText(LanguageTextController.getInstance().currentLanguageDictionary.get(LanguagesKeys.ABNORMAL_VALUE_KEY));
                holder.testCondition.setTextColor(Color.parseColor("#FF0000"));

            }

            if (selectedPosition == position) {
                holder.rlScrollMeg.setVisibility(View.VISIBLE);
                holder.down.setBackgroundResource(R.drawable.down_blue);
                holder.txt_discription = (TextView) holder.itemView.findViewById(R.id.test_discription);
                holder.txt_discription.setText(getTheDescriptionForTestName(holder.testName.getText().toString()));
            } else {
                holder.rlScrollMeg.setVisibility(View.GONE);
                holder.down.setBackgroundResource(R.drawable.down_blue);
            }
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (selectedPosition != position) {
                        selectedPosition = position;
                    } else {
                        selectedPosition = -1;
                    }
                    notifyDataSetChanged();

                }

            });
        }

        @Override
        public int getItemCount() {
            if (testFactorsArrayList.size() > 0) {
                return testFactorsArrayList.size();
            } else {
                return 0;
            }
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            TextView testName, testValue, testCondition, txt_discription, unitsName, testrsult;
            Context ctx;
            ImageView imageView;
            Button down;
            TextView txt_bg, txt_bg1;
            RelativeLayout rlScrollMeg;

            public ViewHolder(View itemView) {
                super(itemView);
                testName = (TextView) itemView.findViewById(R.id.testName);
                unitsName = (TextView) itemView.findViewById(R.id.unitsName);
                testValue = (TextView) itemView.findViewById(R.id.testVal);
                testrsult = (TextView) itemView.findViewById(R.id.testrsult);
                testCondition = (TextView) itemView.findViewById(R.id.testCondition);
                imageView = (ImageView) itemView.findViewById(R.id.img_icon);
                down = (Button) itemView.findViewById(R.id.btn_down);
                txt_bg = (TextView) itemView.findViewById(R.id.txt1);
                txt_bg1 = (TextView) itemView.findViewById(R.id.txt2);
                rlScrollMeg = (RelativeLayout) itemView.findViewById(R.id.rl_msg);
                txt_discription = (TextView) itemView.findViewById(R.id.test_discription);
            }
        }
    }

    public Bitmap convertByteArrayTOBitmap(byte[] profilePic) {
        ByteArrayInputStream arrayInputStream = new ByteArrayInputStream(profilePic);
        Bitmap bitmap = BitmapFactory.decodeStream(arrayInputStream);
        return bitmap;
    }

/*
    View.OnClickListener mShareListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            Intent i = new Intent(getApplicationContext(), HtmltoPdfConversionActivity.class);
            i.putExtra("key", String.valueOf(selectedRecordPosition));
            startActivity(i);

            //pdfAlert();
        }
    };
*/


/*
    public void pdfAlert() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.pdfalert);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.layout_cornerbg);

        Button cancle = (Button) dialog.findViewById(R.id.btn_no);
        Button ok = (Button) dialog.findViewById(R.id.btn_yes);
        TextView txt_msg = (TextView) dialog.findViewById(R.id.txt_msg);
        txt_msg.setText(LanguageTextController.getInstance().currentLanguageDictionary.get(LanguagesKeys.ARE_YOU_TAKEN_TEST_WITH_FASTING));
        TextView txt_alert = (TextView) dialog.findViewById(R.id.text_reminder);
        txt_alert.setText(LanguageTextController.getInstance().currentLanguageDictionary.get(LanguagesKeys.ALERT_KEY));
        ok.setText(LanguageTextController.getInstance().currentLanguageDictionary.get(LanguagesKeys.YES_KEY));
        cancle.setText(LanguageTextController.getInstance().currentLanguageDictionary.get(LanguagesKeys.NO_KEY));

        cancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
                String value = "No";
                Intent i = new Intent(getApplicationContext(), HtmltoPdfConversionActivity.class);
                i.putExtra("key", String.valueOf(selectedRecordPosition));
                startActivity(i);
            }
        });
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                String value = "Yes";
                Intent i = new Intent(getApplicationContext(), HtmltoPdfConversionActivity.class);
                i.putExtra("key", String.valueOf(selectedRecordPosition));
                startActivity(i);
            }
        });

    }
*/

    @Override
    public void onBackPressed() {
        super.onBackPressed();

      //  SCConnectionHelper.getInstance().disconnectWithPeripheral();
        startActivity(new Intent(getApplicationContext(), HomeActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                String strEditText = data.getStringExtra("editTextValue");
                Date selectedDateOject = null;
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
                try {
                    selectedDateOject = df.parse(strEditText);
                    resultCalendar.setTime(selectedDateOject);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    public void respondToSwipeGesture() {
        new SwipeDetector(resultRecyclerView).setOnSwipeListener(new SwipeDetector.onSwipeEvent() {
            @Override
            public void SwipeEventDetected(View v, SwipeDetector.SwipeTypeEnum swipeType) {
                if (swipeType == SwipeDetector.SwipeTypeEnum.LEFT_TO_RIGHT) {
                    //  previousButtonAction();
                }
                if (swipeType == SwipeDetector.SwipeTypeEnum.RIGHT_TO_LEFT) {
                    //  nextButtonAction();
                }
            }
        });

    }

    public static class SwipeDetector implements View.OnTouchListener {

        private int min_distance = 100;
        private float downX, downY, upX, upY;
        private View v;

        private onSwipeEvent swipeEventListener;


        public SwipeDetector(View v) {
            this.v = v;
            v.setOnTouchListener(this);
        }

        public void setOnSwipeListener(onSwipeEvent listener) {
            try {
                swipeEventListener = listener;
            } catch (ClassCastException e) {
                Log.e("ClassCastException", "please pass SwipeDetector.onSwipeEvent Interface instance", e);
            }
        }


        public void onRightToLeftSwipe() {
            if (swipeEventListener != null)
                swipeEventListener.SwipeEventDetected(v, SwipeTypeEnum.RIGHT_TO_LEFT);
            else
                Log.e("SwipeDetector error", "please pass SwipeDetector.onSwipeEvent Interface instance");
        }

        public void onLeftToRightSwipe() {
            if (swipeEventListener != null)
                swipeEventListener.SwipeEventDetected(v, SwipeTypeEnum.LEFT_TO_RIGHT);
            else
                Log.e("SwipeDetector error", "please pass SwipeDetector.onSwipeEvent Interface instance");
        }

        public void onTopToBottomSwipe() {
            if (swipeEventListener != null)
                swipeEventListener.SwipeEventDetected(v, SwipeTypeEnum.TOP_TO_BOTTOM);
            else
                Log.e("SwipeDetector error", "please pass SwipeDetector.onSwipeEvent Interface instance");
        }

        public void onBottomToTopSwipe() {
            if (swipeEventListener != null)
                swipeEventListener.SwipeEventDetected(v, SwipeTypeEnum.BOTTOM_TO_TOP);
            else
                Log.e("SwipeDetector error", "please pass SwipeDetector.onSwipeEvent Interface instance");
        }

        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    downX = event.getX();
                    downY = event.getY();
                    return true;
                }
                case MotionEvent.ACTION_UP: {
                    upX = event.getX();
                    upY = event.getY();

                    float deltaX = downX - upX;
                    float deltaY = downY - upY;

                    //HORIZONTAL SCROLL
                    if (Math.abs(deltaX) > Math.abs(deltaY)) {
                        if (Math.abs(deltaX) > min_distance) {
                            // left or right
                            if (deltaY > 5) {
                                this.onLeftToRightSwipe();
                                return true;
                            }
                            if (deltaX > 5) {
                                this.onRightToLeftSwipe();
                                return true;
                            }
                        } else {
                            //not long enough swipe...
                            return false;
                        }
                    }
                    //VERTICAL SCROLL
                    else {
                        if (Math.abs(deltaY) > min_distance) {
                            // top or down
                            if (deltaY < 0) {
                                this.onTopToBottomSwipe();
                                return true;
                            }
                            if (deltaY > 0) {
                                this.onBottomToTopSwipe();
                                return true;
                            }
                        } else {
                            //not long enough swipe...
                            return false;
                        }
                    }

                    return true;
                }
            }
            return false;
        }

        public interface onSwipeEvent {
            public void SwipeEventDetected(View v, SwipeTypeEnum SwipeType);
        }

        public SwipeDetector setMinDistanceInPixels(int min_distance) {
            this.min_distance = min_distance;
            return this;
        }

        public enum SwipeTypeEnum {
            RIGHT_TO_LEFT, LEFT_TO_RIGHT, TOP_TO_BOTTOM, BOTTOM_TO_TOP
        }

    }

/*
    public void loadAnimatonToRecyclerView() {
        // for adding animatio to recyclerview
        int resId = R.anim.layout_animation_fall_down;
        LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(getApplicationContext(), resId);
        resultRecyclerView.setLayoutAnimation(animation);

    }
*/

    public int changeLableColor(String condition) {
        int color = 0;
        color = UrineTestDataCreatorController.getInstance().getTestColorForLable(condition);
        return color;
    }

    private String getTheDescriptionForTestName(String testName) {
        switch (testName) {
            case "LEUKOCYTES":
                return LanguageTextController.getInstance().currentLanguageDictionary.get(LECHOCYTE_DISCRIPTION_KEY);
            case "Nitrite":
                return LanguageTextController.getInstance().currentLanguageDictionary.get(NITRATE_DISCRIPTION_KEY);
            case "Urobilinogen":
                return LanguageTextController.getInstance().currentLanguageDictionary.get(UROBILLINGEN_DESCRIPTION_RESULT_KEY);
            case "Protein":
                return LanguageTextController.getInstance().currentLanguageDictionary.get(PROTINE_DESCRIPTION_KEY);
            case "PH":
                return LanguageTextController.getInstance().currentLanguageDictionary.get(PH_DISCRIPTION_KEY);
            case "Occult Blood":
                return LanguageTextController.getInstance().currentLanguageDictionary.get(OCCULT_BLOOD_DESCRIPTION_RESULT_KEY);
            case "Specific Gravity":
                return LanguageTextController.getInstance().currentLanguageDictionary.get(SG_DESCRIPTION_KEY);
            case "Ketone":
                return LanguageTextController.getInstance().currentLanguageDictionary.get(KETONS_DESCRIPTION_KEY);
            case "Bilirubin":
                return LanguageTextController.getInstance().currentLanguageDictionary.get(BILLIBURIN_DESCRIPTION_RESULT_KEY);
            default:  // GLUCOSE
                return LanguageTextController.getInstance().currentLanguageDictionary.get(GLOUCOSE_DESCRIPTION_KEY);
        }

    }

}

