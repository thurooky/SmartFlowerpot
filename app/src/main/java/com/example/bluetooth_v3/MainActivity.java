package com.example.bluetooth_v3;

import com.baidu.aip.imageclassify.*;
import androidx.appcompat.app.AppCompatActivity;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.media.Image;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;
//输入数据流正则表达式匹配
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class MainActivity extends AppCompatActivity {

    private final static String MY_UUID = "00001101-0000-1000-8000-00805F9B34FB";   //SPP服务UUID号
    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); //获取蓝牙实例
    private String path = "/storage//Pictures/Browser/123.jpg";
    //系统时间变量
    final Calendar mCalendar=Calendar.getInstance();
    private int mHour;
    private int mMinutes;
    private LayoutInflater inflater;
    private View layoutMain;
    private View layoutSecond;
    private TextView tv_in1; //接收显示句柄1
    private TextView tv_in2; //接收显示句柄2
    private TextView tv_in3; //接收显示句柄

    private TextView tv_in4; //json文件展示
    private TextView tv_in5; //json文件展示
    private TextView tv_in6; //json文件展示
    private ImageView image1;

    private Pattern p = Pattern.compile("([T][^TCH]+)([C][^TCH]+)([H][^TCH]+)"); //根据编码模式解码
    private Pattern p1 = Pattern.compile("(score.*)");
    private Pattern p2 = Pattern.compile("(name.*)");
    private Pattern p3 = Pattern.compile("(description.*)");
    private Matcher m;
    private String smsg = ""; //显示用数据缓存
    private String smsg_1 = ""; //显示用json传输
    private String mac = "";
    private boolean isBlueToothConnected = false;
    private boolean isIlluminated = false;
    private boolean isWatered = false;
    private boolean isSpined = false;
    private int speed = 0;

    BluetoothDevice mBluetoothDevice = null; //蓝牙设备
    BluetoothSocket mBluetoothSocket = null; //蓝牙通信Socket

    private InputStream is;    //输入流，用来接收蓝牙数据
    private OutputStream os;   //输出流，用来发送蓝牙数据

    //获取按钮实例
    private Button mbutton2;
    private Button mbutton3;
    private Button mbutton4;
    private Button mbutton5;
    private Button mbutton6;
    private Button mbutton7;

    private AipImageClassify aipImageClassify;
    public static final String APP_ID = "22614650";
    public static final String API_KEY = "Xo5NLxNOAkvoiVxe3js7sMU3";
    public static final String SECRET_KEY = "Pm2cjfHL25umTToIGhGh48XDdjl1vpDm";

    private Bitmap bitmap;
    //以上是赋值声明


    public void doDiscovery() {
        if (mBluetoothAdapter .isDiscovering()) {
            //判断蓝牙是否正在扫描，如果是调用取消扫描方法；如果不是，则开始扫描
            mBluetoothAdapter .cancelDiscovery();
        } else
            mBluetoothAdapter .startDiscovery();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inflater = this.getLayoutInflater();
        layoutMain = inflater.inflate(R.layout.activity_main, null);
        layoutSecond = inflater.inflate(R.layout.data, null);
        setTitle("首页");
        setContentView(layoutMain);
        Typeface typeFace = Typeface.createFromAsset(getAssets(),"Fonts/font.TTF");
        TextView text1 = findViewById(R.id.textView1);
        text1.setTypeface(typeFace);

        //如果打不开蓝牙提示信息，结束程序
        if (mBluetoothAdapter == null){
            Toast.makeText(getApplicationContext(),"无法打开手机蓝牙，请确认手机是否有蓝牙功能！",Toast.LENGTH_SHORT).show();
            finish();
            return;
        }


        //连接按钮响应
        final Button connectButton = (Button)findViewById(R.id.button);
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //打开蓝牙
                if (mBluetoothAdapter.isEnabled() == false) {
                    Toast.makeText(getApplicationContext(), " 未打开蓝牙", Toast.LENGTH_LONG).show();
                    Intent enabler = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enabler, 1);
                }

                //使设备可被发现
                if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) //不在可被搜索的范围
                {
                    Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                    discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);//设置本机蓝牙在300秒内可见
                    startActivity(discoverableIntent);
                }

                doDiscovery();


                setTitle("操作页面");
                setContentView(layoutSecond);



                Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
                if(pairedDevices.size() > 0){
                    for(BluetoothDevice device:pairedDevices){
                        // 把名字和地址取出来添加到适配器中
                        if(device.getName().equals("PLANTBLUE")) {
                            mac = device.getAddress();
                            mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(mac);
                            break;
                        }
                    }
                }

                //调试用
                //TextView set = (TextView) findViewById(R.id.textView9);
                //set.setText(mac);

                try {
                    mBluetoothSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(UUID.fromString(MY_UUID));
                    mBluetoothSocket.connect();
                    if(mBluetoothSocket.isConnected()){
                        Toast.makeText(getApplicationContext(),"连接成功",Toast.LENGTH_SHORT).show();
                    }
                }catch (IOException e) {
                    Toast.makeText(getApplicationContext(), "连接失败！", Toast.LENGTH_SHORT).show();
                }

                //打开输出线程
                try {
                    os = mBluetoothSocket.getOutputStream();
                } catch (IOException e) {
                    try {
                        mBluetoothSocket.close();
                        Toast.makeText(getApplicationContext(), "输出数据失败！", Toast.LENGTH_SHORT).show();
                        mBluetoothSocket = null;
                    } catch (IOException ee) {
                        Toast.makeText(getApplicationContext(), "连接失败！", Toast.LENGTH_SHORT).show();
                    }
                }
                //打开接收线程
                try {
                    is = mBluetoothSocket.getInputStream();   //得到蓝牙数据输入流
                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(), "接收数据失败！", Toast.LENGTH_SHORT).show();
                }

                Initialization();

                ReadThread rt = new ReadThread();
                rt.start();


            }
        });

        //植物观察界面响应
        final Button button8 = (Button)findViewById(R.id.button8);
        button8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setContentView(R.layout.image);
                Initialization_2();
                setTitle("植物观察页面");
            }
        });
    }

    //输入数据流处理
    private class ReadThread extends Thread {
        private byte[] buffer;
        private int size;
        private String str;
        private TextView text;
        public void run(){
            while (true){
                buffer = new byte[1024];
                if (is != null) {
                    try {
                        size = is.read(buffer);
                        if (size > 0) {
                            str = new String(buffer, 0, size, "UTF-8");
                            Message msg = new Message();
                            smsg = str;
                            handler.sendMessage(msg);
                        }
                    } catch(IOException e) {
                        Toast.makeText(getApplicationContext(), "接收数据出现错误！", Toast.LENGTH_SHORT).show();
                    }
                }
                try {
                    Thread.sleep(500);
                } catch(InterruptedException e){

                }
            }
        }
    }

    //主界面初始化
    private void Initialization() {

        //传输系统时间
        mHour = mCalendar.get(Calendar.HOUR_OF_DAY);
        mMinutes = mCalendar.get(Calendar.MINUTE);
        try {
            os.write('T');
            os.write(mHour);
            os.write('M');
            os.write(mMinutes);
        } catch(IOException e) {
            Toast.makeText(getApplicationContext(), "发送时间失败", Toast.LENGTH_SHORT).show();
        }

        //按钮事件响应
        //返回按钮
        final Button backButton = (Button)findViewById(R.id.button6);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setTitle("首页");
                setContentView(R.layout.activity_main);
                final Button connectButton = (Button)findViewById(R.id.button);
                connectButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setTitle("操作页面");
                        setContentView(R.layout.data);
                        mbutton2 = findViewById(R.id.button2);
                        fillLight(mbutton2);
                        mbutton3 = findViewById(R.id.button3);
                        watering(mbutton3);
                    }
                });

            }
        });

        //补光按钮
        final Button button2 = (Button)findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if(isIlluminated == true) {
                        //停止补光
                        os.write(0x02);
                        isIlluminated = false;
                    }else {
                        //补光
                        os.write(0x01);
                        isIlluminated = true;
                    }
                    Toast.makeText(getApplicationContext(), "已发出！", Toast.LENGTH_SHORT).show();
                } catch(IOException e) {
                    Toast.makeText(getApplicationContext(), "未能发出！", Toast.LENGTH_SHORT).show();
                }
            }
        });
        //浇水按钮
        final Button button3 = (Button)findViewById(R.id.button3);
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if(isWatered == true) {
                        //停止浇水
                        os.write(0x04);
                        isWatered = false;
                    }else {
                        //浇水
                        os.write(0x03);
                        isWatered = true;
                    }
                    Toast.makeText(getApplicationContext(), "已发出！", Toast.LENGTH_SHORT).show();
                } catch(IOException e) {
                    Toast.makeText(getApplicationContext(), "未能发出！", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //旋转按钮
        final Button button4 = (Button)findViewById(R.id.button4);
        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if(isSpined == true) {
                        //停止旋转
                        os.write(0x06);
                        isSpined = false;
                    }else {
                        //旋转
                        os.write(0x05);
                        isSpined = true;
                    }
                    Toast.makeText(getApplicationContext(), "已发出！", Toast.LENGTH_SHORT).show();
                } catch(IOException e) {
                    Toast.makeText(getApplicationContext(), "未能发出！", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //加速按钮
        final Button button5 = (Button)findViewById(R.id.button5);
        button5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    os.write(0x07);
                    Toast.makeText(getApplicationContext(), "已发出！", Toast.LENGTH_SHORT).show();
                } catch(IOException e) {
                    Toast.makeText(getApplicationContext(), "未能发出！", Toast.LENGTH_SHORT).show();
                }
            }
        });
        //减速按钮
        final Button button7 = (Button)findViewById(R.id.button7);
        button7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    os.write(0x08);
                    Toast.makeText(getApplicationContext(), "已发出！", Toast.LENGTH_SHORT).show();
                } catch(IOException e) {
                    Toast.makeText(getApplicationContext(), "未能发出！", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //文本框初始化
        Typeface typeFace1 = Typeface.createFromAsset(getAssets(),"Fonts/font.TTF");
        TextView text2 = findViewById(R.id.textView8);
        text2.setTypeface(typeFace1);
        text2 = findViewById(R.id.textView9);
        tv_in1 = text2;
        text2.setText("数据载入中...");
        text2.setTypeface(typeFace1);
        text2 = findViewById(R.id.textView10);
        text2.setTypeface(typeFace1);
        text2 = findViewById(R.id.textView11);
        tv_in2 = text2;
        text2.setText("数据载入中...");
        text2.setTypeface(typeFace1);
        text2 = findViewById(R.id.textView12);
        text2.setTypeface(typeFace1);
        text2 = findViewById(R.id.textView13);
        tv_in3 = text2;
        text2.setText("数据载入中...");
        text2.setTypeface(typeFace1);
    }

    private void Initialization_2() {


        image1 = (ImageView)findViewById(R.id.image1);

        final EditText t3 = findViewById(R.id.editText);

        tv_in4 = (TextView)findViewById(R.id.textView14);
        tv_in5 = (TextView)findViewById(R.id.textView3);
        tv_in6 = (TextView)findViewById(R.id.textView4);

        Button button9 = (Button)findViewById(R.id.button9);
        button9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //处理图像
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        byte[] content = getBitmapByte(bitmap);

                        aipImageClassify = new AipImageClassify(APP_ID, API_KEY, SECRET_KEY);
                        aipImageClassify.setConnectionTimeoutInMillis(2000);
                        aipImageClassify.setSocketTimeoutInMillis(6000);
                        HashMap<String, String> options = new HashMap<String, String>();
                        options.put("baike_num", "1");
                        JSONObject res = aipImageClassify.plantDetect(content, options);
                        Message message = new Message();
                        /*tv_in4.setVisibility(View.VISIBLE);
                        tv_in5.setVisibility(View.VISIBLE);
                        tv_in6.setVisibility(View.VISIBLE);*/

                        try {

                           smsg_1 = res.toString(2);
                        }
                        catch (JSONException e) {
                            Toast.makeText(getApplicationContext(), "识别失败！",Toast.LENGTH_SHORT).show();
                        }
                        message.what = 1;
                        handler.sendMessage(message);
                    }
                }).start();
            }
        });
        Button button10 = (Button)findViewById(R.id.button10);
        button10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setContentView(R.layout.activity_main);//返回
            }
        });

        //加载图片按钮
        Button button11 = (Button)findViewById(R.id.button11);
        button11.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               String url1 = t3.getText().toString();
               try{

                    URL url = new URL(url1); //path为网址

                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                    conn.setConnectTimeout(5000);
                    conn.setReadTimeout(5000);
                    conn.setRequestMethod("GET");
                    conn.getResponseCode();

                   if (conn.getResponseCode() == 200) {
                       InputStream inputStream = conn.getInputStream();

                       bitmap = BitmapFactory.decodeStream(inputStream);
                       image1.setImageBitmap(bitmap);
                       t3.setVisibility(View.INVISIBLE);
                   }

                    }
               catch(MalformedURLException m) {
                        Toast.makeText(getApplicationContext(),"无效的路径！",Toast.LENGTH_SHORT).show();
                    }
               catch(IOException e ) {
                        Toast.makeText(getApplicationContext(),"网络连接失败！",Toast.LENGTH_SHORT).show();
                    }




           }
        });


    }


    //各按钮初始化函数
    //补光按钮
    private void fillLight(Button button) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if(isIlluminated == true) {
                        //停止补光
                        os.write(0x02);
                        isIlluminated = false;
                    }else {
                        //补光
                        os.write(0x01);
                        isIlluminated = true;
                    }
                    Toast.makeText(getApplicationContext(), "已发出！", Toast.LENGTH_SHORT).show();
                } catch(IOException e) {
                    Toast.makeText(getApplicationContext(), "未能发出！", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //浇水按钮
    private void watering(Button button) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if(isWatered == true) {
                        //停止浇水
                        os.write(0x04);
                        isWatered = false;
                    }else {
                        //浇水
                        os.write(0x03);
                        isWatered = true;
                    }
                    Toast.makeText(getApplicationContext(), "已发出！", Toast.LENGTH_SHORT).show();
                } catch(IOException e) {
                    Toast.makeText(getApplicationContext(), "未能发出！", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //旋转按钮
    private void spining(Button button) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if(isSpined == true) {
                        //停止旋转
                        os.write(0x06);
                        isSpined = false;
                    }else {
                        //旋转
                        os.write(0x05);
                        isSpined = true;
                    }
                    Toast.makeText(getApplicationContext(), "已发出！", Toast.LENGTH_SHORT).show();
                } catch(IOException e) {
                    Toast.makeText(getApplicationContext(), "未能发出！", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //加速按钮
    private void speedUp(Button button) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    os.write(0x07);
                    Toast.makeText(getApplicationContext(), "已发出！", Toast.LENGTH_SHORT).show();
                } catch(IOException e) {
                    Toast.makeText(getApplicationContext(), "未能发出！", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    //减速按钮
    private void slowDown(Button button) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    os.write(0x08);
                    Toast.makeText(getApplicationContext(), "已发出！", Toast.LENGTH_SHORT).show();
                } catch(IOException e) {
                    Toast.makeText(getApplicationContext(), "未能发出！", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //返回按钮
    private void goBack(Button button) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setTitle("首页");
                setContentView(R.layout.activity_main);
                final Button connectButton = (Button) findViewById(R.id.button);
                connectButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setTitle("操作页面");
                        setContentView(R.layout.data);
                    }
                });
            }
        });
    }


    //消息处理队列
    Handler handler= new Handler(){
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            if(msg.what == 1) {
                m = p1.matcher(smsg_1);
                image1.setVisibility(View.INVISIBLE);
                if(m.find()){
                    tv_in4.setText("得分:"+m.group(1).substring(7,13));
                }
                m = p2.matcher(smsg_1);
                if(m.find()){
                    tv_in5.setText(m.group(1).substring(6).replace('\"',' ').replace(',',' '));
                }
                m = p3.matcher(smsg_1);
                if(m.find()){
                    tv_in6.setText(m.group(1).substring(13).replace('\"',' ').replace(',',' '));
                }
            }
            else {
                //显示数据
                m = p.matcher(smsg);
                if (m.find()) {
                    tv_in1.setText(Float.parseFloat(m.group(1).substring(1)) / 10 + "℃");
                    float tmp = Float.parseFloat(m.group(3).substring(1)) / 100;
                    if (tmp > 0.02)
                        tv_in2.setText(Float.parseFloat(m.group(3).substring(1)) / 100 + "%");
                    tv_in3.setText(Float.parseFloat(m.group(2).substring(1)) + "Lux");
                } else {
                }
            }
        }
    };


    //将Bitmap转换为二进制数组
    public byte[] getBitmapByte(Bitmap bitmap) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        try {
            out.flush();
            out.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return out.toByteArray();
    }
    public static byte[] readInputStream(InputStream instream) throws Exception{
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[]  buffer = new byte[1204];
        int len = 0;
        while ((len = instream.read(buffer)) != -1){
            outStream.write(buffer,0,len);
        }
        instream.close();
        return outStream.toByteArray();
    }
}