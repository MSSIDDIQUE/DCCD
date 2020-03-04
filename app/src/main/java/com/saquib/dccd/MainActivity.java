package com.saquib.dccd;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.DrawableUtils;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_CODE = 1;
    private static int REQUEST_IMAGE_CAPTURE = 1;
    private static int REQUEST_IMAGE_CROP = 2;
    private Bitmap imageBitmap;
    private TextView textView;
    @BindView(R.id.input_card_no) EditText cardNo;
    @BindView(R.id.input_expiry_date) EditText cardExpiry;
    @BindView(R.id.input_brand) EditText cardBrand;
    @BindView(R.id.imageView) ImageView imageView;
    private Uri uri;
    private CoordinatorLayout coordinatorLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });
        ButterKnife.bind(this);
    }

    public void detectTxt()
    {
        FirebaseVisionImage firebaseVisionImage = FirebaseVisionImage.fromBitmap(imageBitmap);
        FirebaseVisionTextRecognizer textRecognizer = FirebaseVision.getInstance()
                .getOnDeviceTextRecognizer();
        Task<FirebaseVisionText> results = textRecognizer.processImage(firebaseVisionImage).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
            @Override
            public void onSuccess(FirebaseVisionText firebaseVisionText) {
                ProcessText(firebaseVisionText);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Snackbar.make(coordinatorLayout,"Unable to Recognize the Text",Snackbar.LENGTH_LONG);
            }
        });
    }

    private void CropImage()
    {
        Intent cropIntent = new Intent("com.android.camera.action.CROP");
        //indicate image type and Uri
        cropIntent.setDataAndType(uri, "image/*");
        //set crop properties
        cropIntent.putExtra("crop", "true");
        //indicate aspect of desired crop
        cropIntent.putExtra("aspectX", 4);
        cropIntent.putExtra("aspectY", 2.5);
        //indicate output X and Y
        cropIntent.putExtra("outputX", 450);
        cropIntent.putExtra("outputY", 275);
        //retrieve data on return
        cropIntent.putExtra("return-data", true);
        cropIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        cropIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        cropIntent.putExtra(MediaStore.EXTRA_OUTPUT,uri);
        //start the activity - we handle returning in onActivityResult
        startActivityForResult(cropIntent, REQUEST_IMAGE_CROP);
    }

    private void ProcessText(FirebaseVisionText text)
    {
        String s ="",all="";
        String CardNo="";
        String Expiry = "";
        String Bank = "";
        String Brand ="";
        TreeSet<String>ListOfBanks=new TreeSet<String>(Arrays.asList(new String[]{
                "Allahabad Bank",
                "State Bank",
                "Indian Overseas Bank",
                "Andhra Bank",
                "Oriental Bank of Commerce",
                "Bank of Baroda",
                "Punjab National Bank",
                "Bank of India",
                "Syndicate Bank",
                "Bank of Maharashtra",
                "Union Bank of India",
                "Canara Bank",
                "United Bank of India",
                "Central Bank of India",
                "Punjab & Sind Bank",
                "Corporation Bank",
                "UCO Bank",
                "Dena Bank",
                "Vijaya Bank",
                "Indian Bank",
                "Axis Bank",
                "Bandhan Bank",
                "Catholic Syrian Bank",
                "City Union Bank",
                "DCB Bank",
                "Dhanlaxmi Bank",
                "Federal Bank",
                "HDFC Bank",
                "ICICI Bank",
                "IndusInd Bank",
                "IDFC Bank",
                "Jammu & Kashmir Bank",
                "Karnataka Bank",
                "Karur Vysya Bank",
                "Kotak Mahindra Bank",
                "Lakshmi Vilas Bank",
                "Nainital Bank",
                "RBL Bank",
                "South Indian Bank",
                "Tamilnad Mercantile Bank",
                "YES Bank",
                "BOI"
        }));
        TreeSet<String>ListOfBrads=new TreeSet<String>(Arrays.asList(new String []{
            "Visa",
            "MasterCard",
                "Maestro",
                "Rupay"
        }));
        List<FirebaseVisionText.TextBlock> blocks = text.getTextBlocks();
        if(blocks.size()==0)
        {
            Toast.makeText(this,"No text is detected", Toast.LENGTH_LONG);
        }
        else
        {
            for(int i=0;i<blocks.size();i++)
            {
                List<FirebaseVisionText.Line> lines = blocks.get(i).getLines();
                for(int j=0;j<lines.size();j++)
                {
                    s="";
                    List<FirebaseVisionText.Element> elements = lines.get(j).getElements();
                    for(int k=0;k<elements.size();k++)
                    {
                        s+=(elements.get(k).getText()+" ");
                        all+=(elements.get(k).getText()+" ");
                    }
                    all+="\n";
                    if(CardNo.length()<s.replaceAll("[^0-9]","").length())
                    {
                        CardNo = s.replaceAll("[^0-9]","");
                    }
                    if(s.replaceAll("[^0-9,/,^0-9]","").length()==5)
                    {
                        Expiry = s.replaceAll("[^0-9,/,^0-9]","");
                    }
                    for(String b :ListOfBanks)
                    {
                        if(s.toLowerCase().trim().contains(b.toLowerCase().trim()))
                        {
                            Bank = b;
                        }
                    }
                    for(String br:ListOfBrads)
                    {
                        if(s.toLowerCase().contains(br.toLowerCase()))
                        {
                            Brand = br;
                        }
                    }
                }
            }
            //textView.setText(CardNo+"\n"+Expiry+"\n"+Bank+"\n"+Brand+"\n");
            cardNo.setText(CardNo,TextView.BufferType.EDITABLE);
            cardBrand.setText(Brand,TextView.BufferType.EDITABLE);
            cardExpiry.setText(Expiry,TextView.BufferType.EDITABLE);
        }

    }

    public void dispatchTakePictureIntent() {
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M)
        {
            if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_DENIED&&checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)==PackageManager.PERMISSION_DENIED)
            {
                String [] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
                requestPermissions(permissions,PERMISSION_CODE);
            }
            else
            {
                takePicture();
            }
        }
    }

    public void takePicture()
    {
        Intent takePictureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        File file = new File(Environment.getExternalStorageDirectory(),"Picture.jpg");
        uri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".providers", file);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,uri);
        takePictureIntent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        startActivityForResult(takePictureIntent,REQUEST_IMAGE_CAPTURE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CROP && resultCode == RESULT_OK) {
            try {
                imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                Drawable d = new BitmapDrawable(getResources(), imageBitmap);
                imageView.setImageDrawable(d);
                detectTxt();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK)
        {
            CropImage();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case PERMISSION_CODE:
                if(grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED)
                {
                    takePicture();
                }
                else
                {
                    Snackbar.make(coordinatorLayout,"Permission Not Granted",Snackbar.LENGTH_LONG);
                }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
