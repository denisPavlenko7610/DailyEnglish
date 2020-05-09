package com.denis7610.dailyenglish;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

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
    private ConstraintLayout mainBackground;
    private TextView textTranslate;
    private TextView textTranscription;
    private ImageButton changeThemeButton;
    private Button license;
    private AdView adView;
    private int count;

    boolean checked = true;

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
        mainBackground = findViewById(R.id.mainBackground);
        textTranscription = findViewById(R.id.textTranscription);
        license = findViewById(R.id.licence);
        changeThemeButton = findViewById(R.id.changeTheme);
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
        String countString = "Images: " + count;
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

    public void changeTheme(View view) {
        if (checked) {
            mainBackground.setBackgroundColor(Color.parseColor("#eeeeee"));
            countText.setTextColor(Color.parseColor("#333333"));
            license.setBackgroundColor(Color.parseColor("#eeeeee"));
            license.setTextColor(Color.parseColor("#333333"));
            changeThemeButton.setImageResource(R.drawable.theme_dark);
            license.setTextColor(Color.parseColor("#eeeeee"));
            changeThemeButton.setBackgroundColor(Color.parseColor("#eeeeee"));
            textTranscription.setTextColor(Color.parseColor("#333333"));
            textTranslate.setTextColor(Color.parseColor("#333333"));
            checked = false;
        } else {
            mainBackground.setBackgroundColor(Color.parseColor("#292525"));
            countText.setTextColor(Color.parseColor("#ffffff"));
            license.setBackgroundColor(Color.parseColor("#292525"));
            license.setTextColor(Color.parseColor("#999999"));
            changeThemeButton.setBackgroundColor(Color.parseColor("#292525"));
            changeThemeButton.setImageResource(R.drawable.theme);
            textTranscription.setTextColor(Color.parseColor("#ffffff"));
            textTranslate.setTextColor(Color.parseColor("#ffffff"));
            checked = true;
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
        data.add("[ˈtʃeriz]");

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

        data.add("ic_barn");
        data.add("barn");
        data.add("[bɑːrn]");

        data.add("ic_bee");
        data.add("bee");
        data.add("[biː]");

        data.add("ic_chainsaw");
        data.add("chainsaw");
        data.add("");

        data.add("ic_cow");
        data.add("cow");
        data.add("[kaʊ]");

        data.add("ic_duck");
        data.add("duck");
        data.add("[dʌk]");

        data.add("ic_farmer");
        data.add("farmer");
        data.add("[ˈfɑːrmər]");

        data.add("ic_hayfork");
        data.add("hayfork");
        data.add("[ˈheɪfɔːrk]");

        data.add("ic_hen");
        data.add("hen");
        data.add("[hen]");

        data.add("ic_hose");
        data.add("hose");
        data.add("[hoʊz]");

        data.add("ic_mill");
        data.add("mill");
        data.add("[mɪl]");

        data.add("ic_pig");
        data.add("pig");
        data.add("[pɪɡ]");

        data.add("ic_rabbit");
        data.add("rabbit");
        data.add("[ˈræbɪt]");

        data.add("ic_sheep");
        data.add("sheep");
        data.add("[ʃiːp]");

        data.add("ic_shovel");
        data.add("shovel");
        data.add("[ˈʃʌvl]");

        data.add("ic_tractor");
        data.add("tractor");
        data.add("[ˈtræktər]");

        data.add("ic_tree");
        data.add("tree");
        data.add("[triː]");

        data.add("ic_watering_pot");
        data.add("watering pot");
        data.add("");

        data.add("ic_well");
        data.add("well");
        data.add("[wel]");

        data.add("ic_wheat");
        data.add("wheat");
        data.add("[wiːt]");

        data.add("ic_backpack");
        data.add("backpack");
        data.add("[ˈbækpæk]");

        data.add("ic_binoculars");
        data.add("binoculars");
        data.add("[bɪˈnɑːkjələrz]");

        data.add("ic_boot");
        data.add("boot");
        data.add("[buːt]");

        data.add("ic_camera");
        data.add("camera");
        data.add("[ˈkæmərə]");

        data.add("ic_campfire");
        data.add("campfire");
        data.add("[ˈkæmpfaɪər]");

        data.add("ic_canned_products");
        data.add("canned products");
        data.add("");

        data.add("ic_compass");
        data.add("compass");
        data.add("[ˈkʌmpəs]");

        data.add("ic_flashlight");
        data.add("flashlight");
        data.add("[ˈflæʃlaɪt]");

        data.add("ic_forest");
        data.add("forest");
        data.add("[ˈfɔːrɪst]");

        data.add("ic_guitar");
        data.add("guitar");
        data.add("[ɡɪˈtɑːr]");

        data.add("ic_kettle");
        data.add("kettle");
        data.add("[ˈketl]");

        data.add("ic_log");
        data.add("log");
        data.add("[lɔːɡ]");

        data.add("ic_matches");
        data.add("matches");
        data.add("[ˈmætʃəz]");

        data.add("ic_medkit");
        data.add("medkit");
        data.add("");

        data.add("ic_mountains");
        data.add("mountains");
        data.add("[ˈmaʊntənz]");

        data.add("ic_radio");
        data.add("radio");
        data.add("[ˈreɪdioʊ]");

        data.add("ic_rifle");
        data.add("rifle");
        data.add("[ˈraɪfl]");

        data.add("ic_rubber_boat");
        data.add("rubber boat");
        data.add("");

        data.add("ic_sleeping_bag");
        data.add("sleeping bag");
        data.add("");

        data.add("ic_tent");
        data.add("tent");
        data.add("tent");

        data.add("ic_armchair");
        data.add("armchair");
        data.add("[ˈɑːrmtʃer]");

        data.add("ic_bed");
        data.add("bed");
        data.add("[ˌbiː ˈed]");

        data.add("ic_bookcase");
        data.add("bookcase");
        data.add("[ˈbʊkkeɪs]");

        data.add("ic_chest");
        data.add("chest");
        data.add("[tʃest]");

        data.add("ic_column");
        data.add("column");
        data.add("[ˈkɑːləm]");

        data.add("ic_couch");
        data.add("couch");
        data.add("[kaʊtʃ]");

        data.add("ic_curtain");
        data.add("curtain");
        data.add("[ˈkɜːrtn]");

        data.add("ic_mirror");
        data.add("mirror");
        data.add("[ˈmɪrər]");

        data.add("ic_picture");
        data.add("picture");
        data.add("[ˈpɪktʃər]");

        data.add("ic_refrigerator");
        data.add("refrigerator");
        data.add("[rɪˈfrɪdʒəreɪtər]");

        data.add("ic_vase");
        data.add("vase");
        data.add("[veɪs]");

        data.add("ic_accordion");
        data.add("accordion");
        data.add("[əˈkɔːrdiən]");

        data.add("ic_drum_set");
        data.add("drum set");
        data.add("");

        data.add("ic_drum");
        data.add("drum");
        data.add("[drʌm]");

        data.add("ic_earphones");
        data.add("earphones");
        data.add("[ˈɪərˌfoʊnz]");

        data.add("ic_electric_guitar");
        data.add("electric guitar");
        data.add("");

        data.add("ic_harp");
        data.add("harp");
        data.add("[hɑːrp]");

        data.add("ic_headphones");
        data.add("headphones");
        data.add("[ˈhedˌfoʊnz]");

        data.add("ic_maracas");
        data.add("maracas");
        data.add("");

        data.add("ic_metronome");
        data.add("metronome");
        data.add("[ˈmetrənoʊm]");

        data.add("ic_microphone");
        data.add("microphone");
        data.add("[ˈmaɪkrəfoʊn]");

        data.add("ic_note");
        data.add("note");
        data.add("[noʊt]");

        data.add("ic_piano");
        data.add("piano");
        data.add("[piˈænoʊ]");

        data.add("ic_records");
        data.add("records");
        data.add("[rəˈkɔːrdz]");

        data.add("ic_saxophone");
        data.add("saxophone");
        data.add("[ˈsæksəfoʊn]");

        data.add("ic_trumpet");
        data.add("trumpet");
        data.add("[ˈtrʌmpɪt]");

        data.add("ic_violin");
        data.add("violin");
        data.add("[ˌvaɪəˈlɪn]");

        data.add("ic_xylophone");
        data.add("xylophone");
        data.add("[ˈzaɪləfoʊn]");

    }
}