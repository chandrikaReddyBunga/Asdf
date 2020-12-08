package com.gtsr.gtsr.testModule;

import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gtsr.gtsr.AlertShowingDialog;
import com.gtsr.gtsr.HomeActivity;
import com.gtsr.gtsr.LanguagesKeys;
import com.gtsr.gtsr.R;
import com.gtsr.gtsr.RefreshShowingDialog;
import com.gtsr.gtsr.dataController.ExportToExcelFileController;
import com.gtsr.gtsr.dataController.LanguageTextController;
import com.gtsr.gtsr.database.TestFactorDataController;
import com.gtsr.gtsr.database.UrineResultsDataController;
import com.gtsr.gtsr.database.UrineresultsModel;
import com.spectrochips.spectrumsdk.FRAMEWORK.SCConnectionHelper;
import com.spectrochips.spectrumsdk.FRAMEWORK.SCTestAnalysis;
import com.spectrochips.spectrumsdk.FRAMEWORK.TestFactors;
import com.spectrochips.spectrumsdk.MODELS.IntensityChart;

import org.json.JSONException;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by Abhilash on 2/1/2019.
 */

public class AnalizingPageViewController extends AppCompatActivity {
    ImageView imagerotateanalizine;
    RotateAnimation rotate;
    String longitude, latitude;
    long unixTime;
    TextView abort, toolbartext, waiting, txt_response, txt_results;
    Handler handler;
    HandlerThread handlerThread;
    RefreshShowingDialog testDialogue;
    private boolean isAbort = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analizingpage);
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                String filename = extras.getString("filename");
                String category = extras.getString("category");
                String date = extras.getString("addedDate");
                Log.e("filename", "call" + filename);
            }
        }
        init();
        unixTime = System.currentTimeMillis() / 1L;
        // TestDairyViewController.selectedPosition = 0;
        updatelanguage();
        testDialogue = new RefreshShowingDialog(AnalizingPageViewController.this, "Analyzing...");
        activateNotification();
    }

    public void activateNotification() {
        if (SCConnectionHelper.getInstance().isConnected) {
            abort.setVisibility(View.VISIBLE);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);// for stopping screen off while in testing
            // testDialogue.showAlert();
            SCTestAnalysis.getInstance().startTestAnalysis(new SCTestAnalysis.TeststaResultInterface() {
                @Override
                public void onSuccessForTestComplete(ArrayList<TestFactors> results, String msg, ArrayList<IntensityChart> intensityChartsArray) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.e("chatarray","call"+intensityChartsArray.size());
                            HtmltoPdfConversionActivity.testResults=results;
                            HtmltoPdfConversionActivity.intensityArray=intensityChartsArray;
                            loadResultsDataTODb(results);
                        }
                    });
                }
                @Override
                public void getRequestAndResponse(String data) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //addMessage(data);
                        }
                    });
                }
                @Override
                public void onFailureForTesting(String error) {
                    Log.e("onFailureForTesting", "call");
                    // testDialogue.hideRefreshDialog();
                    performTestFailedFunction();
                }
            });
            SCTestAnalysis.getInstance().loadInterface();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (SCConnectionHelper.getInstance().isConnected) {
                        SCTestAnalysis.getInstance().startTesting();
                    }
                }
            }, 1000 * 1);

        } else {
            Toast.makeText(getApplicationContext(), "Device not Connected", Toast.LENGTH_SHORT).show();
        }
        SCConnectionHelper.getInstance().activateScanNotification(new SCConnectionHelper.ScanDeviceInterface() {
            @Override
            public void onSuccessForConnection(String msg) {

            }

            @Override
            public void onSuccessForScanning(ArrayList<BluetoothDevice> deviceArray, boolean msg) {

            }

            @Override
            public void onFailureForConnection(String error) {
                Log.e("onFailureForConnection", "msssasnasn");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        rotate.cancel();
                        new AlertShowingDialog(AnalizingPageViewController.this, "Device Disconnected", LanguageTextController.getInstance().currentLanguageDictionary.get(LanguagesKeys.OK_KEY));
                    }
                });
            }

            @Override
            public void uartServiceClose(String error) {

            }
        });
    }

    private void addMessage(String msg) {
        // append the new string
        txt_response.append(msg + "\n");
        // find the amount we need to scroll.  This works by
        // asking the TextView's internal layout for the position
        // of the final line and then subtracting the TextView's height
        final int scrollAmount = txt_response.getLayout().getLineTop(txt_response.getLineCount()) - txt_response.getHeight();
        // if there is no need to scroll, scrollAmount will be <=0
        if (scrollAmount > 0)
            txt_response.scrollTo(0, scrollAmount);
        else
            txt_response.scrollTo(0, 0);
    }

    private void init() {
        imagerotateanalizine = (ImageView) findViewById(R.id.image);
        RelativeLayout back = findViewById(R.id.home);
        toolbartext = (TextView) findViewById(R.id.tool_txt);
        txt_results = findViewById(R.id.next);
        txt_results.setVisibility(View.GONE);
        txt_response = findViewById(R.id.response);
        txt_response.setMovementMethod(new ScrollingMovementMethod());
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //  performAbortFunction();
                // finish();
            }
        });

        rotate = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(2000);
        rotate.setRepeatCount(Animation.INFINITE);
        imagerotateanalizine.setAnimation(rotate);

        abort = (TextView) findViewById(R.id.abort);
        waiting = (TextView) findViewById(R.id.waiting);

        abort.setOnClickListener(manimate);

        txt_results.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), ResultPageViewController.class));
            }
        });
    }

    View.OnClickListener manimate = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            performAbortFunction();
        }
    };

    public void updatelanguage() {
        waiting.setText(LanguageTextController.getInstance().currentLanguageDictionary.get(LanguagesKeys.WAITING_FOR_RESULTS_KEY));
        abort.setText(LanguageTextController.getInstance().currentLanguageDictionary.get(LanguagesKeys.ABORT_KEY));
        toolbartext.setText(LanguageTextController.getInstance().currentLanguageDictionary.get(LanguagesKeys.ANALZING_KEY));
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void performAbortFunction() {
        final Dialog dialog = new Dialog(AnalizingPageViewController.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.stopanimate_alert);
        dialog.show();
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.layout_cornerbg);

        TextView text = (TextView) dialog.findViewById(R.id.text_reminder);
        text.setText(LanguageTextController.getInstance().currentLanguageDictionary.get(LanguagesKeys.ALERT_KEY));

        TextView text1 = (TextView) dialog.findViewById(R.id.text);
        text1.setText(LanguageTextController.getInstance().currentLanguageDictionary.get(LanguagesKeys.DO_YOU_WANT_TO_ABORT_TEST_KEY));

        Button no = (Button) dialog.findViewById(R.id.btn_no);
        no.setText(LanguageTextController.getInstance().currentLanguageDictionary.get(LanguagesKeys.CANCEL_KEY));

        Button yes = (Button) dialog.findViewById(R.id.btn_yes);
        yes.setText(LanguageTextController.getInstance().currentLanguageDictionary.get(LanguagesKeys.YES_KEY));


        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                waiting.setText(LanguageTextController.getInstance().currentLanguageDictionary.get(LanguagesKeys.ABORTING_KEY));
                //  rotate.cancel();
                isAbort = true;
              /*  SCTestAnalysis.getInstance().abortTesting(new SCTestAnalysis.AbortInterface() {
                    @Override
                    public void onAbortForTesting(boolean bool) {
                        SCConnectionHelper.getInstance().disconnectWithPeripheral();
                        startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                    }
                });*/

                // abortDialogue.showAlert();
                SCTestAnalysis.getInstance().ejectTesting(new SCTestAnalysis.EjectInterface() {
                    @Override
                    public void ejectStrip(boolean bool) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // abortDialogue.hideRefreshDialog();
                                SCConnectionHelper.getInstance().disconnectWithPeripheral();
                               /* SCTestAnalysis.getInstance().unRegisterReceiver();
                                SCTestAnalysis.getInstance().removereceiver();*/
                                startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                                Toast.makeText(getApplicationContext(), "Strip Ejected.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
                SCTestAnalysis.getInstance().abortTesting(new SCTestAnalysis.AbortInterface() {
                    @Override
                    public void onAbortForTesting(boolean isabort) {
                        if (isabort) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    waiting.setText("Strip Ejecting..");
                                    //  abortDialogue.hideRefreshDialog();
                                    Toast.makeText(getApplicationContext(), "Test Aborted.", Toast.LENGTH_SHORT).show();
                                    abort.setVisibility(View.GONE);
                                }
                            });
                        }
                    }
                });

            }
        });
    }

    private void performTestFailedFunction() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(AnalizingPageViewController.this);
        alertDialogBuilder.setMessage(LanguageTextController.getInstance().currentLanguageDictionary.get(LanguagesKeys.NO_STRIP_HOLDER_WAS_DETECTED_KEY))
                .setCancelable(false)
                .setPositiveButton(LanguageTextController.getInstance().currentLanguageDictionary.get(LanguagesKeys.TRY_AGAIN_KEY), new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {
                        SCTestAnalysis.getInstance().performTryAgainFunction();

                    }
                })
                .setNegativeButton(LanguageTextController.getInstance().currentLanguageDictionary.get(LanguagesKeys.CANCEL_KEY), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        SCTestAnalysis.getInstance().performTestCancelFunction();
                    }
                });


        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
        Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setTextColor(Color.parseColor("#FF0B8B42"));

        Button negativeButton = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        negativeButton.setTextColor(Color.parseColor("#FF0012"));

        positiveButton.setText(LanguageTextController.getInstance().currentLanguageDictionary.get(LanguagesKeys.OK_KEY));
        negativeButton.setText(LanguageTextController.getInstance().currentLanguageDictionary.get(LanguagesKeys.CANCEL_KEY));

    }

    String currentTime = String.valueOf(System.currentTimeMillis() / 1000L);
    String testID = "test" + String.valueOf(getRandomInteger(1000, 10));

    public int getRandomInteger(int maximum, int minimum) {
        return ((int) (Math.random() * (maximum - minimum))) + minimum;
    }

    public void loadResultsDataTODb(ArrayList<TestFactors> testResults) {
        UrineresultsModel objResult = new UrineresultsModel();
        objResult.setTestReportNumber("");
        objResult.setTestType("Urine");
        objResult.setLatitude("0.0");
        objResult.setLongitude("0.0");
        objResult.setTestedTime(currentTime);
        objResult.setIsFasting("false");
        objResult.setRelationtype("Patient");
        objResult.setTest_id(testID);

        if (UrineResultsDataController.getInstance().insertUrineResultsForMember(objResult)) {
            for (int index = 0; index < testResults.size(); index++) {
                TestFactors object = testResults.get(index);
                Log.e("asssssssss","call"+object.getValue());
                com.gtsr.gtsr.database.TestFactors objTest = new com.gtsr.gtsr.database.TestFactors();
                objTest.setFlag(object.isFlag());
                objTest.setUnit(object.getUnits());
                objTest.setHealthReferenceRanges(object.getReferenceRange());
                objTest.setTestName(object.getTestname());
                objTest.setResult(object.getResult());
                objTest.setValue(object.getValue());
                objTest.setUrineresultsModel(UrineResultsDataController.getInstance().currenturineresultsModel);
                if (TestFactorDataController.getInstance().insertTestFactorResults(objTest)) {

                }
            }
            //  testDialogue.hideRefreshDialog();
            txt_results.setVisibility(View.VISIBLE);
            // SCConnectionHelper.getInstance().disconnectWithPeripheral();
            startActivity(new Intent(getApplicationContext(), ResultPageViewController.class));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        // performAbortFunction();
    }
}
