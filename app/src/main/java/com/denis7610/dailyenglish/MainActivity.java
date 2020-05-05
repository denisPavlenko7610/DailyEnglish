package com.denis7610.dailyenglish;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.ads.consent.ConsentForm;
import com.google.ads.consent.ConsentFormListener;
import com.google.ads.consent.ConsentInfoUpdateListener;
import com.google.ads.consent.ConsentInformation;
import com.google.ads.consent.ConsentStatus;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private ConsentForm form;
    private TextView countText;
    private ImageView questionImage;
    private TextView textTranslate;
    private TextView textTranscription;
    private AdView adView;
    private int count;

    String worldText;

    private int arrayIndex = 0;

    ArrayList<String> data = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        addBannerAdmob();

        ESPolicy();

        loadData();

        init();

        generateCard();

        showCount();
    }

    private void ESPolicy() {
        ConsentInformation consentInformation = ConsentInformation.getInstance(getApplicationContext());
        String[] publisherIds = {"pub-7173647303121367"};
        consentInformation.requestConsentInfoUpdate(publisherIds, new ConsentInfoUpdateListener() {
            @Override
            public void onConsentInfoUpdated(ConsentStatus consentStatus) {
                // User's consent status successfully updated.
                boolean inEEA = ConsentInformation.getInstance(getApplicationContext()).isRequestLocationInEeaOrUnknown();

                if (inEEA) {
                    if (consentStatus == consentStatus.PERSONALIZED) {
                        //no code
                    } else if (consentStatus == consentStatus.NON_PERSONALIZED) {
                        Bundle extras = new Bundle();
                        extras.putString("npa", "1");

                        AdRequest request = new AdRequest.Builder()
                                .addNetworkExtrasBundle(AdMobAdapter.class, extras)
                                .build();
                    } else { //start code form

                        URL privacyUrl = null;
                        try {
                            privacyUrl = new URL("https://wolfprogrammer.000webhostapp.com/");
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                            // Handle error.
                        }
                        form = new ConsentForm.Builder(MainActivity.this, privacyUrl)
                                .withListener(new ConsentFormListener() {
                                    @Override
                                    public void onConsentFormLoaded() {
                                        // Consent form loaded successfully.
                                        form.show();
                                    }

                                    @Override
                                    public void onConsentFormOpened() {
                                        // Consent form was displayed.
                                    }

                                    @Override
                                    public void onConsentFormClosed(
                                            ConsentStatus consentStatus, Boolean userPrefersAdFree) {
                                        // Consent form was closed.
                                        if (consentStatus == ConsentStatus.NON_PERSONALIZED) {
                                            Bundle extras = new Bundle();
                                            extras.putString("npa", "1");

                                            AdRequest request = new AdRequest.Builder()
                                                    .addNetworkExtrasBundle(AdMobAdapter.class, extras)
                                                    .build();
                                        }
                                    }

                                    @Override
                                    public void onConsentFormError(String errorDescription) {
                                        // Consent form error.
                                    }
                                })
                                .withPersonalizedAdsOption()
                                .withNonPersonalizedAdsOption()
                                .build();
                        form.load();
                    } //end code form
                }
            }

            @Override
            public void onFailedToUpdateConsentInfo(String errorDescription) {
                // User's consent status failed to update.
            }
        });
    }

    private void addBannerAdmob() {
        //AdMob
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        adView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

    }

    private void init() {
        countText = findViewById(R.id.count);
        questionImage = findViewById(R.id.questionImage);
        textTranslate = findViewById(R.id.textTranslate);
        textTranscription = findViewById(R.id.textTranscription);
        Button buttonNo = findViewById(R.id.buttonNo);
        Button buttonYes = findViewById(R.id.buttonYes);
    }

    @Override
    protected void onStop() {
        super.onStop();
        saveData();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        saveData();
    }

    private void saveData() {
        if (data.size() != 0) {
            SharedPreferences sharedPreferences = getSharedPreferences("shared preferences", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            Gson gson = new Gson();
            String json = gson.toJson(data);
            editor.putString("know list", json);
            editor.apply();
        }
    }

    private void loadData() {
        SharedPreferences sharedPreferences = getSharedPreferences("shared preferences", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("know list", null);
        Type type = new TypeToken<ArrayList<String>>() {
        }.getType();
        if (json != null) {
            data = gson.fromJson(json, type);
        } else {
            generateQuizes();
        }
    }

    private void showCount() {
        count = 0;
        if (data.size() != 0) {
            count = data.size() / 3;
        }
        String countString = "Pictures left: " + count;
        countText.setText(countString);
    }

    private void showNextQuiz() {
        if (arrayIndex >= data.size()) {
            arrayIndex = 0;
        }
        if (data.size() == 0) {
            Toast.makeText(this, "Congratulation! Try Again to improve yourself)", Toast.LENGTH_SHORT).show();
            arrayIndex = 0;
            generateQuizes();
        }
        generateCard();
    }

    private void generateCard() {

        questionImage.setImageResource(getResources().getIdentifier(String.valueOf(data.get(arrayIndex)), "drawable", getPackageName()));

        worldText = String.valueOf(data.get(arrayIndex + 1));
        String transcription = String.valueOf(data.get(arrayIndex + 2));

        textTranslate.setText(worldText);
        textTranscription.setText(transcription);
    }

    public void buttonYes(View view) {
        deleteWord();
        showCount();
        showNextQuiz();
    }

    private void deleteWord() {
        if (data.size() > 0) {
            for (int i = 0; i < 3; i++) {
                data.remove(arrayIndex);
            }
        }
    }

    public void buttonNo(View view) {
        showCount();
        arrayIndex += 3;
        showNextQuiz();
    }

    public void showLicense(View view) {
        Intent intent = new Intent(this, LicenseActivity.class);
        startActivity(intent);
    }

    public void sentToGoogleTranslate(View view) {
        try {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_TEXT, worldText);
            intent.putExtra("key_text_input", worldText);
            intent.putExtra("key_text_output", "");
            intent.putExtra("key_language_from", "en");
            intent.putExtra("key_language_to", "mal");
            intent.putExtra("key_suggest_translation", "");
            intent.putExtra("key_from_floating_window", false);
            intent.setComponent(new ComponentName(
                    "com.google.android.apps.translate",
                    //Change is here
                    //"com.google.android.apps.translate.HomeActivity"));
                    "com.google.android.apps.translate.TranslateActivity"));
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            // TODO Auto-generated catch block
            Toast.makeText(getApplication(), "Sorry, No Google Translation Installed",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void generateQuizes() {
        data.add("ic_001_apple");
        data.add("apple");
        data.add("[æpl]");

        data.add("ic_002_carrot");
        data.add("carrot");
        data.add("[kærət]");

        data.add("ic_003_salad");
        data.add("salad");
        data.add("[ˈsæləd]");

        data.add("ic_005_water");
        data.add("water");
        data.add("[ˈwɔːtər]");

        data.add("ic_006_cheese");
        data.add("cheese");
        data.add("[tʃiːz]");

        data.add("ic_007_orange");
        data.add("orange");
        data.add("[ˈɔːrɪndʒ]");

        data.add("ic_008_tomato");
        data.add("tomato");
        data.add("[təˈmeɪtoʊ]");

        data.add("ic_009_grapes");
        data.add("grapes");
        data.add("[ˈɡreɪps]");

        data.add("ic_010_broccoli");
        data.add("broccoli");
        data.add("[ˈbrɑːkəli]");

        data.add("ic_011_strawberry");
        data.add("strawberry");
        data.add("[ˈstrɔːberi]");

        data.add("ic_012_fish");
        data.add("fish");
        data.add("[fɪʃ]");

        data.add("ic_013_steak");
        data.add("steak");
        data.add("[steɪk]");

        data.add("ic_014_cherries");
        data.add("cherries");
        data.add("ˈtʃeriz");

        data.add("ic_015_tea");
        data.add("tea");
        data.add("[tiː]");

        data.add("ic_016_cupcake");
        data.add("cupcake");
        data.add("[ˈkʌpkeɪk]");

        data.add("ic_017_pineapple");
        data.add("pineapple");
        data.add("[ˈpaɪnæpl]");

        data.add("ic_018_milk");
        data.add("milk");
        data.add("[mɪlk]");

        data.add("ic_019_watermelon");
        data.add("watermelon");
        data.add("[ˈwɔːtərmelən]");

        data.add("ic_020_lemon");
        data.add("lemon");
        data.add("[ˈlemən]");

        data.add("ic_021_sandwich");
        data.add("sandwich");
        data.add("[ˈsænwɪtʃ]");

        data.add("ic_022_toffee");
        data.add("toffee");
        data.add("[ˈtɔːfi]");

        data.add("ic_023_turkey");
        data.add("turkey");
        data.add("[ˈtɜːrki]");

        data.add("ic_024_pizza");
        data.add("pizza");
        data.add("[ˈpiːtsə]");

        data.add("ic_025_chili");
        data.add("chili");
        data.add("[ˈtʃɪli]");

        data.add("ic_026_egg");
        data.add("egg");
        data.add("[eɡ]");

        data.add("ic_027_raspberry");
        data.add("raspberry");
        data.add("[ˈræzberi]");

        data.add("ic_028_fries");
        data.add("fries");
        data.add("[fraɪz]");

        data.add("ic_029_chocolate");
        data.add("chocolate");
        data.add("[ˈtʃɑːklət]");

        data.add("ic_030_potatoes");
        data.add("potatoes");
        data.add("[pəˈteɪtoʊz]");

        data.add("ic_031_glass");
        data.add("glass");
        data.add("[ɡlæs]");

        data.add("ic_032_food");
        data.add("food");
        data.add("[fuːd]");

        data.add("ic_033_baguette");
        data.add("baguette");
        data.add("[baguette]");

        data.add("ic_034_aubergine");
        data.add("aubergine");
        data.add("[ˈoʊbərʒiːn]");

        data.add("ic_035_knife");
        data.add("knife");
        data.add("[naɪf]");

        data.add("ic_036_garlic");
        data.add("garlic");
        data.add("[ˈɡɑːrlɪk]");

        data.add("ic_037_cake");
        data.add("cake");
        data.add("[keɪk]");

        data.add("ic_038_flour");
        data.add("flour");
        data.add("[ˈflaʊər]");

        data.add("ic_041_bread");
        data.add("bread");
        data.add("[bred]");

        data.add("ic_045_corn");
        data.add("corn");
        data.add("[kɔːrn]");

        data.add("ic_046_shrimp");
        data.add("shrimp");
        data.add("[ʃrɪmp]");

        data.add("ic_051_honey");
        data.add("honey");
        data.add("[ˈhʌni]");

        data.add("ic_052_sushi");
        data.add("sushi");
        data.add("['suːʃɪ]");

        data.add("ic_054_nut");
        data.add("nut");
        data.add("[nʌt]");

        data.add("ic_060_pumpkin");
        data.add("pumpkin");
        data.add("[ˈpʌmpkɪn]");

        data.add("ic_061_cup");
        data.add("cup");
        data.add("[kʌp]");

        data.add("ic_063_gingerbread");
        data.add("gingerbread");
        data.add("[ˈdʒɪndʒərbred]");

        data.add("ic_064_weight");
        data.add("weight");
        data.add("[weɪt]");

        data.add("ic_066_oil");
        data.add("oil");
        data.add("[ɔɪl]");

        data.add("ic_067_dessert");
        data.add("dessert");
        data.add("[dɪˈzɜːrt]");

        data.add("ic_071_snack");
        data.add("snack");
        data.add("[snæk]");

        data.add("ic_073_pepper");
        data.add("pepper");
        data.add("[ˈpepər]");

        data.add("ic_074_coconut");
        data.add("coconut");
        data.add("[ˈkoʊkənʌt]");

        data.add("ic_076_jar");
        data.add("jar");
        data.add("[dʒɑːr]");

        data.add("ic_077_biscuit");
        data.add("biscuit");
        data.add("[ˈbɪskɪt]");

        data.add("ic_082_radish");
        data.add("radish");
        data.add("[radish]");

        data.add("ic_087_toast");
        data.add("toast");
        data.add("[toʊst]");

        data.add("ic_088_mushroom");
        data.add("mushroom");
        data.add("[ˈmʌʃrʊm]");

        data.add("ic_089_spoon");
        data.add("spoon");
        data.add("[spuːn]");

        data.add("ic_090_spatula");
        data.add("spatula");
        data.add("[ˈspætʃələ]");

        data.add("ic_091_asparagus");
        data.add("asparagus");
        data.add("[əˈspærəɡəs]");

        data.add("ic_092_pot");
        data.add("pot");
        data.add("[pɑːt]");

        data.add("ic_094_jam");
        data.add("jam");
        data.add("[dʒæm]");

        data.add("ic_097_pretzel");
        data.add("pretzel");
        data.add("[ˈpretsl]");

        data.add("ic_101_salt");
        data.add("salt");
        data.add("[sɔːlt]");

        data.add("ic_102_grater");
        data.add("grater");
        data.add("[ˈɡreɪtər]");

        data.add("ic_103_corkscrew");
        data.add("corkscrew");
        data.add("[ˈkɔːkskruː]");

        data.add("ic_104_soup");
        data.add("soup");
        data.add("[suːp]");

        data.add("ic_palm");
        data.add("palm");
        data.add("[pɑːm]");

        data.add("ic_pharaoh");
        data.add("pharaoh");
        data.add("[ˈferoʊ]");

        data.add("ic_pyramid");
        data.add("pyramid");
        data.add("[ˈpɪrəmɪd]");

        data.add("ic_snake");
        data.add("snake");
        data.add("[sneɪk]");

        data.add("ic_amphora");
        data.add("amphora");
        data.add("[ˈæmfərə]");

        data.add("ic_flower");
        data.add("flower");
        data.add("[ˈflaʊər]");

        data.add("ic_camel");
        data.add("camel");
        data.add("[ˈkæml]");

        data.add("ic_cactus");
        data.add("cactus");
        data.add("[ˈkæktəs]");

        data.add("ic_cards");
        data.add("card");
        data.add("[kɑːrd]");

        data.add("ic_bag_with_money");
        data.add("bag with money");
        data.add("");

        data.add("ic_gun");
        data.add("gun");
        data.add("[ɡʌn]");

        data.add("ic_tepee");
        data.add("tepee");
        data.add("[ˈtiːpiː]");

        data.add("ic_horse");
        data.add("horse");
        data.add("[hɔːrs]");

        data.add("ic_axe");
        data.add("axe");
        data.add("[æks]");

        data.add("ic_horseshoe");
        data.add("horseshoe");
        data.add("[ˈhɔːrʃʃuː]");

        data.add("ic_guitar");
        data.add("guitar");
        data.add("[ɡɪˈtɑːr]");

        data.add("ic_lamp");
        data.add("lamp");
        data.add("[læmp]");

        data.add("ic_barrel");
        data.add("barrel");
        data.add("[ˈbærəl]");

        data.add("ic_hat");
        data.add("hat");
        data.add("[hæt]");

        data.add("ic_cartwheel");
        data.add("cartwheel");
        data.add("[ˈkɑːrtwiːl]");

        data.add("ic_dynamite");
        data.add("dynamite");
        data.add("[ˈdaɪnəmaɪt]");

        data.add("ic_bomb");
        data.add("bomb");
        data.add("[bɑːm]");

        data.add("ic_whale");
        data.add("whale");
        data.add("[weɪl]");

        data.add("ic_ship");
        data.add("ship");
        data.add("[ʃɪp]");

        data.add("ic_bottle");
        data.add("bottle");
        data.add("[ˈbɑːtl]");

        data.add("ic_cannon");
        data.add("cannon");
        data.add("[ˈkænən]");

        data.add("ic_parrot");
        data.add("parrot");
        data.add("[ˈpærət]");

        data.add("ic_lighthouse");
        data.add("lighthouse");
        data.add("[ˈlaɪthaʊs]");

        data.add("ic_turtle");
        data.add("turtle");
        data.add("[ˈtɜːrtl]");

        data.add("ic_swords");
        data.add("swords");
        data.add("[ˈsɔːrdz]");

        data.add("ic_octopus");
        data.add("octopus");
        data.add("[ˈɑːktəpəs]");

        data.add("ic_pirate");
        data.add("pirate");
        data.add("[ˈpaɪrət]");

        data.add("ic_craps");
        data.add("craps");
        data.add("[kræps]");

    }


}

