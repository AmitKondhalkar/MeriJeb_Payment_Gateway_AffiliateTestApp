package com.arindamn.shoppingcart;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.merijeb.paymentgateway.activity.MeriJebPGActivity;
import com.merijeb.paymentgateway.model.MeriJebConstant;
import com.merijeb.paymentgateway.model.MeriJebPGRequest;
import com.merijeb.paymentgateway.model.MeriJebPGResponse;
import com.merijeb.paymentgateway.utils.TokenGenerator;

import java.security.SecureRandom;

public class MainActivity extends AppCompatActivity{

    int merchantIndex = 0;

    int env = MeriJebConstant.STAGING_ENV;

    String merchantTestKeys[] = {"flipkartMerchantKey", "flipkartMerchantKey"};
    String merchantTestSalts[] = {"qwertyuiop", "qwertyuiop" };
    String salt = "";

    String merchantProductionKeys[] = {"", ""};
    String merchantProductionSalts[] = {"", "",};

    String merchantKey = env == MeriJebConstant.PRODUCTION_ENV ? merchantProductionKeys[merchantIndex]:merchantTestKeys[merchantIndex];

    String mandatoryKeys[] = {  MeriJebConstant.PRODUCT_INFO
            , MeriJebConstant.TXNID
            , MeriJebConstant.FIRST_NAME
            , MeriJebConstant.LASTNAME
            , MeriJebConstant.PHONE
            , MeriJebConstant.EMAIL
            , MeriJebConstant.AMOUNT
            , MeriJebConstant.OFFER_ID
            , MeriJebConstant.KEY
            , MeriJebConstant.SALT // temporary - remove it
            , MeriJebConstant.HASH
            , MeriJebConstant.ENV};
    String mandatoryValues[] = { "AFL-123"
            , ""+System.currentTimeMillis() +10
            , "firstname"
            , "lastname"
            , ""
            , "me@itsmeonly.com"
            , "1000.0"
            , ""
            , merchantKey
            , ""
            , ""
            , ""+env};

    private MeriJebPGRequest mPaymentParams;
    String inputData = "";
    private Intent intent;

    private Button btnSubmit;
    private ScrollView mainScrollView;
    private LinearLayout rowContainerLinearLayout;

    private SecureRandom random = new SecureRandom();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // lets initialize the views
        btnSubmit = (Button) findViewById(R.id.btnSubmit);
        rowContainerLinearLayout = (LinearLayout) findViewById(R.id.linear_layout_row_container);

        mainScrollView = (ScrollView) findViewById(R.id.scroll_view_main);

        // lets set the on click listener to the buttons
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()){
                    case R.id.btnSubmit:
                        navigateToMeriJebWalletActivity();
                        break;
                }
            }
        });


        // filling up the ui with the values.
        for(int i = 0 ; i < mandatoryKeys.length; i++){
            addView();
            LinearLayout currentLayout = (LinearLayout) rowContainerLinearLayout.getChildAt(i);
            ((EditText) currentLayout.getChildAt(0)).setText(mandatoryKeys[i]);
            if(null != mandatoryValues[i])
                ((EditText)currentLayout.getChildAt(1)).setText(mandatoryValues[i]);
        }
    }

    private void addView(){
        rowContainerLinearLayout.addView(getLayoutInflater().inflate(R.layout.row, null));
        findViewById(R.id.scroll_view_main).post(new Runnable() {
            @Override
            public void run() {
                mainScrollView.fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    private void navigateToMeriJebWalletActivity(){
        intent = new Intent(this, MeriJebPGActivity.class);
        LinearLayout rowContainerLayout = (LinearLayout) findViewById(R.id.linear_layout_row_container);
        mPaymentParams = new MeriJebPGRequest();

        int childNodeCount = rowContainerLayout.getChildCount();

        for(int i = 0; i < childNodeCount; i++){
            LinearLayout linearLayout = (LinearLayout) rowContainerLayout.getChildAt(i);
            inputData = ((EditText)linearLayout.getChildAt(1)).getText().toString();
            switch (((EditText)linearLayout.getChildAt(0)).getText().toString()){
                case MeriJebConstant.PRODUCT_INFO:
                    mPaymentParams.setProductinfo(inputData);
                    break;
                case MeriJebConstant.TXNID:
                    mPaymentParams.setTxnid(inputData);
                    break;
                case MeriJebConstant.FIRST_NAME:
                    mPaymentParams.setFirstname(inputData);
                    break;
                case MeriJebConstant.LASTNAME:
                    mPaymentParams.setLastname(inputData);
                    break;
                case MeriJebConstant.PHONE:
                    mPaymentParams.setPhone(inputData);
                    break;
                case MeriJebConstant.EMAIL:
                    mPaymentParams.setEmail(inputData);
                    break;
                case MeriJebConstant.AMOUNT:
                    mPaymentParams.setAmount(inputData);
                    break;
                case MeriJebConstant.OFFER_ID:
                    mPaymentParams.setOffer_id(inputData);
                    break;
                case MeriJebConstant.KEY:
                    //Below merchant key is for testing purpose. Please replace
                    mPaymentParams.setKey(inputData);
                    break;

                // other params- should be inside bundle, so that we can get them in next page.
                case MeriJebConstant.SALT:
                    salt = inputData;
                    break;

                // stetting up the environment
                case MeriJebConstant.ENV:
                    intent.putExtra(MeriJebConstant.ENV, inputData);
                    break;

            }

        }

        // generate hash from client;
        /**
         *  just for testing, dont use this in production.
         *  merchant should generate the hash from his server using the salt provided by MeriJeb
         *
         */
        if(TextUtils.isEmpty(salt)) {
            //this is test salt and has to be removed
            salt = merchantTestSalts[merchantIndex];
        }
        intent.putExtra(MeriJebConstant.SALT, salt);
        generateHashFromSDK(mPaymentParams, intent.getStringExtra(MeriJebConstant.SALT));
    }


    /****************************** Client hash generation ***********************************/
    // Do not use this, you may use this only for testing.
    // lets generate hashes.
    // This should be done from server side..
    // Do not keep salt anywhere in app.
    public void generateHashFromSDK(MeriJebPGRequest mPaymentParams, String Salt){
        mPaymentParams.setHash(TokenGenerator.getInHash(merchantKey,
                mPaymentParams.getTxnid(),
                mPaymentParams.getAmount(),
                mPaymentParams.getProductinfo(),
                mPaymentParams.getFirstname(),
                mPaymentParams.getEmail(),
                Salt));

        launchMeriJebWallet(env, mPaymentParams);
    }

    public void launchMeriJebWallet(final int mEnv, final MeriJebPGRequest mInputParams) {
        MeriJebPGRequest request = mInputParams;

        Intent intent = new Intent(MainActivity.this, MeriJebPGActivity.class);
        Bundle mBundle = new Bundle();
        mBundle.putParcelable(MeriJebConstant.MJ_REQUEST_OBJ, request);
        mBundle.putBoolean(MeriJebConstant.MJ_ENVIRONMENT, mEnv == MeriJebConstant.PRODUCTION_ENV? true:false);
        intent.putExtras(mBundle);
        startActivityForResult(intent, MeriJebConstant.MJ_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MeriJebConstant.MJ_REQUEST_CODE) {
            if(data != null ) {
                String result = data.getStringExtra(MeriJebConstant.MJ_RESPONSE_OBJ);
                MeriJebPGResponse responseObject = new MeriJebPGResponse(resultCode, result);

                new AlertDialog.Builder(this)
                        .setCancelable(false)
                        .setMessage(result)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {

                            }
                        }).show();
            }else{
                Toast.makeText(this, "Could not receive data", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        LinearLayout rowContainerLayout = (LinearLayout) findViewById(R.id.linear_layout_row_container);

        int childNodeCount = rowContainerLayout.getChildCount();
        // we need a unique txnid every time..
        for(int i = 0; i < childNodeCount; i++){
            LinearLayout linearLayout = (LinearLayout) rowContainerLayout.getChildAt(i);
            switch (((EditText)linearLayout.getChildAt(0)).getText().toString()){
                case MeriJebConstant.TXNID: // lets set up txnid.
                    ((EditText) linearLayout.getChildAt(1)).setText(""+System.currentTimeMillis());
                    break;
            }

        }
    }
}
