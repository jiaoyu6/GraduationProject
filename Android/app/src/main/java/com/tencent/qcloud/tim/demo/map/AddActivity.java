package com.tencent.qcloud.tim.demo.map;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.tencent.qcloud.tim.demo.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

public class AddActivity extends AppCompatActivity {
    private Button take_photo,select_photo;
    public static final int TAKE_PHOTO = 1;
    public static final int SELECT_PHOTO = 2;
    private ImageView imageview;
    private Uri imageUri;

    private EditText etDate = null,etTime=null,etTitle=null,etContent=null;
    private Button btnSave = null;

    static final int DATE_DIALOG_ID = 0;
    static final int TIME_DIALOG_ID = 1;

    private DbHelper dbhelper;

    Bundle bundle;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);
        take_photo = (Button) findViewById(R.id.take_photo);
        select_photo = (Button) findViewById(R.id.select_photo);
        imageview = (ImageView) findViewById(R.id.imageview);

        take_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //拍照获取图片
                take_photo();
            }
        });

        select_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //从相册中选取图片
                select_photo();
            }
        });


        dbhelper = new DbHelper(this, "db_bwl", null, 1);

        etTitle = (EditText)findViewById(R.id.etTitle);
        etContent = (EditText)findViewById(R.id.etContent);

        etDate = (EditText)findViewById(R.id.etDate);
        etDate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                //并会调用 onCreateDialog(int)回调函数来请求一个Dialog
                showDialog(DATE_DIALOG_ID);

            }
        });

        etTime = (EditText)findViewById(R.id.etTime);
        etTime.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                //并会调用 onCreateDialog(int)回调函数来请求一个Dialog
                showDialog(TIME_DIALOG_ID);

            }
        });


        btnSave = (Button)findViewById(R.id.btnSave);
        btnSave.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                ContentValues value = new ContentValues();

                String title = etTitle.getText().toString();
                String content = etContent.getText().toString();
                String noticeDate = etDate.getText().toString();
                String noticeTime = etTime.getText().toString();

                value.put("title", title);
                value.put("content", content);
                value.put("noticeDate", noticeDate);
                value.put("noticeTime", noticeTime);


                SQLiteDatabase db = dbhelper.getWritableDatabase();

                long id = 0;

                long status = 0;
                if(bundle!=null){
                    id = bundle.getLong("id");
                    status = db.update("tb_bwl", value, "id=?", new String[]{bundle.getLong("id")+""});
                }else{
                    status = db.insert("tb_bwl", null, value);
                    id = status;
                }

                if(status!=-1){
                    setAlarm(id);
                    Toast.makeText(AddActivity.this, "保存成功", Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(AddActivity.this, "保存失败", Toast.LENGTH_LONG).show();
                }
            }
        });

        //获取上一个activity的传值
        bundle = this.getIntent().getExtras();
        if(bundle!=null){
            etDate.setText(bundle.getString("noticeDate"));
            etTime.setText(bundle.getString("noticeTime"));
            etTitle.setText(bundle.getString("title"));
            etContent.setText(bundle.getString("content"));
        }
    }

    public void take_photo() {
        String status= Environment.getExternalStorageState();
        if(status.equals(Environment.MEDIA_MOUNTED)) {
            //创建File对象，用于存储拍照后的图片
            File outputImage = new File(getExternalCacheDir(), "output_image.jpg");
            try {
                if (outputImage.exists()) {
                    outputImage.delete();
                }
                outputImage.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (Build.VERSION.SDK_INT >= 24) {
                imageUri = FileProvider.getUriForFile(this, "com.llk.study.activity.PhotoActivity", outputImage);
            } else {
                imageUri = Uri.fromFile(outputImage);
            }
            //启动相机程序
            Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(intent, TAKE_PHOTO);
        }else
        {

            Toast.makeText(AddActivity.this, "没有储存卡", Toast.LENGTH_LONG).show();
        }
    }
    /**
     * 从相册中获取图片
     * */
    public void select_photo() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        }else {
            openAlbum();
        }
    }
    /**
     * 打开相册的方法
     * */
    private void openAlbum() {
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent,SELECT_PHOTO);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case TAKE_PHOTO :
                if (resultCode == RESULT_OK) {
                    try {
                        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                        imageview.setImageBitmap(bitmap);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case SELECT_PHOTO :
                if (resultCode == RESULT_OK) {
                    //判断手机系统版本号
                    if (Build.VERSION.SDK_INT > 19) {
                        //4.4及以上系统使用这个方法处理图片
                        handleImgeOnKitKat(data);
                    }else {
                        handleImageBeforeKitKat(data);
                    }
                }
                break;
            default:
                break;
        }
    }
    /**
     *4.4以下系统处理图片的方法
     * */
    private void handleImageBeforeKitKat(Intent data) {
        Uri uri = data.getData();
        String imagePath = getImagePath(uri,null);
        displayImage(imagePath);
    }
    /**
     * 4.4及以上系统处理图片的方法
     * */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void handleImgeOnKitKat(Intent data) {
        String imagePath = null;
        Uri uri = data.getData();
        if (DocumentsContract.isDocumentUri(this,uri)) {
            //如果是document类型的uri，则通过document id处理
            String docId = DocumentsContract.getDocumentId(uri);
            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                //解析出数字格式的id
                String id = docId.split(":")[1];
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,selection);
            }else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                imagePath = getImagePath(contentUri,null);
            }else if ("content".equalsIgnoreCase(uri.getScheme())) {
                //如果是content类型的uri，则使用普通方式处理
                imagePath = getImagePath(uri,null);
            }else if ("file".equalsIgnoreCase(uri.getScheme())) {
                //如果是file类型的uri，直接获取图片路径即可
                imagePath = uri.getPath();
            }
            //根据图片路径显示图片
            displayImage(imagePath);
        }
    }
    /**
     * 根据图片路径显示图片的方法
     * */
    private void displayImage(String imagePath) {
        if (imagePath != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            imageview.setImageBitmap(bitmap);
        }else {
            Toast.makeText(AddActivity.this,"failed to get image", Toast.LENGTH_SHORT).show();
        }
    }
    /**
     * 通过uri和selection来获取真实的图片路径
     * */
    private String getImagePath(Uri uri, String selection) {
        String path = null;
        Cursor cursor = getContentResolver().query(uri,null,selection,null,null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1 :
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openAlbum();
                }else {
                    Toast.makeText(AddActivity.this,"failed to get image", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker datePicker, int year, int month, int day) {

            StringBuilder dateStr = new StringBuilder();
            dateStr.append(year).append("-")
                    .append(month+1).append("-")
                    .append(day);

            etDate.setText(dateStr.toString());
        }
    };


    private TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {

        @Override
        public void onTimeSet(TimePicker timePicker, int hour, int minute) {


            StringBuilder timeStr = new StringBuilder();
            timeStr.append(hour).append(":")
                    .append(minute);

            etTime.setText(timeStr.toString());
        }
    };

    /**
     * 当Activity调用showDialog函数时会触发该函数的调用
     */
    protected Dialog onCreateDialog(int id){
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        switch(id){
            case DATE_DIALOG_ID:
                DatePickerDialog dpd = new DatePickerDialog(this,dateSetListener, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
                dpd.setCancelable(true);
                dpd.setTitle("选择日期");
                dpd.show();
                break;
            case TIME_DIALOG_ID:
                TimePickerDialog tpd = new TimePickerDialog(this, timeSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true);
                tpd.setCancelable(true);
                tpd.setTitle("选择时间");
                tpd.show();
                break;
            default:
                break;
        }
        return null;
    }

    private AlarmManager alarmManager=null;


    public void setAlarm(long id){

        Log.e("AndroidBWL", "setAlarm start...");


        String noticeDate = etDate.getText().toString();
        String noticeTime = etTime.getText().toString();

        Calendar calendar = Calendar.getInstance();

        calendar.set(Integer.parseInt(noticeDate.split("-")[0]),
                Integer.parseInt(noticeDate.split("-")[1])-1,
                Integer.parseInt(noticeDate.split("-")[2]),
                Integer.parseInt(noticeTime.split(":")[0]),
                Integer.parseInt(noticeTime.split(":")[1]));

        Log.e("AndroidBWL", ""+(calendar.getTimeInMillis()- System.currentTimeMillis()));



        alarmManager=(AlarmManager)getSystemService(Context.ALARM_SERVICE);


        Intent intent = new Intent(AddActivity.this, AlarmReceiver.class); //创建Intent对象

        Bundle bundle = new Bundle();
        bundle.putLong("id", id);
        bundle.putString("title", etTitle.getText().toString());
        bundle.putString("content", etContent.getText().toString());
        bundle.putString("noticeDate", etDate.getText().toString());
        bundle.putString("noticeTime", etTime.getText().toString());

        intent.putExtras(bundle);

        //PendingIntent.getBroadcast intent 数据不更新。
        //传不同的 action 来解决这个问题
        intent.setAction("ALARM_ACTION"+calendar.getTimeInMillis());

        PendingIntent pi = PendingIntent.getBroadcast(AddActivity.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT); //创建PendingIntent

        //参数说明：http://www.eoeandroid.com/blog-119358-2995.html
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis()+5000, pi); //设置闹钟，当前时间就唤醒

        Log.e("AndroidBWL", "setAlarm end...");

    }


}
