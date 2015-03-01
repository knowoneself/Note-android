package com.comtop.mynote;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.comtop.mynote.db.DBHelper;
import com.comtop.mynote.db.NoteVO;
import com.comtop.mynote.utils.AudioRecorder;
import com.comtop.mynote.utils.CommonUtil;
import com.comtop.mynote.utils.Constants;

public class NoteDetailActivity extends Activity {

	private static final String TAG = "NoteDetailActivity";
	
	//---文本相关start
	private EditText detailTitle; 
	private EditText detailContent;
	private Long noteId;
	private ImageView returnImage;
	private ImageView deleteImage;
	private ImageView sychnoteImage;
	//---文本相关 end
	
	//---录音相关start
	private Button record;
	private Dialog dialog;
	private AudioRecorder mr;
	private MediaPlayer mediaPlayer;
	private ImageView player_imgview;   
	//TextView luyin_txt,luyin_path;
	private Thread recordThread;
	
	private static int MAX_TIME = 100;    //最长录制时间，单位秒，0为无时间限制
	private static int MIX_TIME = 1;     //最短录制时间，单位秒，0为无时间限制，建议设为1
	
	private static int RECORD_NO = 0;  //不在录音
	private static int RECORD_ING = 1;   //正在录音
	private static int RECODE_ED = 2;   //完成录音
	
	private static int RECODE_STATE = 0;      //录音的状态
	
	private static float recodeTime=0.0f;    //录音的时间
	private static double voiceValue=0.0;    //麦克风获取的音量值
	
	private ImageView dialog_img;
	private static boolean playState = false;  //播放状态
	private String voiceUrl;
	//---录音相关start
	
	//---图片相关start
	private static final int CHOOSE_FROM_CAMERA = 1;
	private static final int CHOOSE_FROM_FILE = 2;
	private static final int COPY_PICTURE = 3;
	private ImageView camera_imgview;
	private Dialog camera_dialog;
	private String olderImageName;  //旧图片名称
	private String newImageName;  //新增的图片名称
	private Uri cameraImageUri;    //相机相册获取图片后的保存地址
	//---图片相关end
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.note_detail_activity);
		initControl();
	}
	
	/**
	 * 初始化控制 
	 */
	private void initControl() {
		
		//---文本相关start 
		detailTitle = (EditText) this.findViewById(R.id.detailTitleName);
		detailContent = (EditText) this.findViewById(R.id.detailContentName);
		Intent intent = this.getIntent();
		noteId = intent.getLongExtra("id", 0);
		
		//初始化控件
		NoteVO objNoteVO =   DBHelper.getInstance(this).readNoteVO(noteId);
	   if(null != objNoteVO){
			detailTitle.setText(objNoteVO.getTitle().toString());
			//detailContent.setText(objNoteVO.getContent().toString());
			
			String strContent = objNoteVO.getContent().toString();
		    SpannableString ss = new SpannableString(strContent);
		    //图片url的正则表达式
		    String strPat = CommonUtil.getSdCardPath() + Constants.IMG_PATH  +objNoteVO.getId()+"_"+"\\d*\\.jpg" ; 
		    Pattern p=Pattern.compile(strPat); 
		    Matcher m=p.matcher(strContent);
		    while(m.find()){
		        Bitmap bm = BitmapFactory.decodeFile(m.group());
		        ImageSpan span = new ImageSpan(this, bm);
		        ss.setSpan(span, m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		    }
		    detailContent.setText(ss);
			
		}else{
			detailTitle.setText(Constants.DEFAULT_NOTE_TITLE);
			detailContent.setText(Constants.DEFAULT_NOTE_CONTENT);
		}
		
		//修改后自动保存
	    detailTitle.addTextChangedListener(detailContentTextWatcher);
		detailContent.addTextChangedListener(detailContentTextWatcher);
		//---文本相关end
		 
		
	    //---录音相关start
		 player_imgview = (ImageView) findViewById(R.id.playerRecord);
		 //初始化录音存放目录
	     CommonUtil.createFileDir(CommonUtil.getSdCardPath() + Constants.VOICE_PATH);
	     voiceUrl = CommonUtil.getSdCardPath() + Constants.VOICE_PATH;
		 //播放
	     player_imgview.setOnClickListener(playerOnClickListener);
	     //录音
	     record = (Button) this.findViewById(R.id.record); 
	     record.setOnTouchListener( recordOnTouchListener);
	    //---录音相关end
	     
	     
	   //---图片相关start
	     CommonUtil.createFileDir(CommonUtil.getSdCardPath() + Constants.IMG_PATH);
	     camera_imgview  = (ImageView) findViewById(R.id.camera);	     
	     camera_imgview.setOnClickListener(cameraOnClickListener);
	   //---图片相关end
	     
	     //返回操作
	     returnImage  = (ImageView) this.findViewById(R.id.detailnoteImage);
			returnImage.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					NoteDetailActivity.this.finish();
				}
			});
			
		//删除操作	
	     deleteImage = (ImageView) this.findViewById(R.id.deletelnoteImage);
	     deleteImage.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if(noteId > 0){
				 DBHelper.getInstance(NoteDetailActivity.this).deleteNoteVO(noteId);
				}
				NoteDetailActivity.this.finish();
			}
		});
	     
	     sychnoteImage = (ImageView) this.findViewById(R.id.sychnoteImage);
	     sychnoteImage.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Toast.makeText(NoteDetailActivity.this, "调用同步接口！", Toast.LENGTH_SHORT).show();
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	private TextWatcher detailContentTextWatcher = new TextWatcher() {

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			// TODO Auto-generated method stub
		}
		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			// TODO Auto-generated method stub
		}
		@Override
		public void afterTextChanged(Editable s) {
			insertOrUpdateNote();
		}
	};

	
	/**
	 * 插入或更新数据
	 * @param objNoteVO
	 */
	private void insertOrUpdateNote() {
		//获取信息保存
		NoteVO objNoteVO = new NoteVO();
		objNoteVO.setContent(detailContent.getText().toString());
		objNoteVO.setTitle(detailTitle.getText().toString());
		if (noteId >0 ) { 
			objNoteVO.setId(noteId);
			DBHelper.getInstance(this).updateNoteVO(objNoteVO);
		} else {
			noteId = DBHelper.getInstance(this).insertNoteVO(objNoteVO);
		}
	}
	
	/**
	 * 新增数据
	 * @param objNoteVO
	 */
	private void createIfNoNote() {
		if (noteId >0 ) {
			return;
		}else{
			//获取信息保存
			NoteVO objNoteVO = new NoteVO();
			objNoteVO.setContent(detailContent.getText().toString());
			objNoteVO.setTitle(detailTitle.getText().toString());
			noteId = DBHelper.getInstance(this).insertNoteVO(objNoteVO);
		}
	}
	
	//---文本相关end
	
	//---录音相关start
	private OnClickListener playerOnClickListener = new OnClickListener(){

		
		@Override
		public void onClick(View v) {
			//先确认单据是否存在
			if( noteId < 1){
				Toast.makeText(NoteDetailActivity.this, "没有语音~亲！", Toast.LENGTH_SHORT).show();
				return;
			}
			// TODO Auto-generated method stub
			if (!playState) {
				mediaPlayer = new MediaPlayer();
				try
				{
					//模拟器里播放传url，真机播放传,文件路径如 /storage/sdcard/mynote/voice/xxx(id号).amr
					mediaPlayer.setDataSource(voiceUrl+noteId+".amr");
					mediaPlayer.prepare();
					mediaPlayer.start();
					player_imgview.setImageResource(R.drawable.stop_btn1);//.  setText("正在播放中");
					playState = true;
					//设置播放结束时监听
					mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
						
						@Override
						public void onCompletion(MediaPlayer mp) {
							if (playState) {
								player_imgview.setImageResource(R.drawable.play_btn1);
								playState = false;
							}
						}
					});
				}
				catch (IllegalArgumentException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				catch (IllegalStateException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				catch (IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
			}else {
				if (mediaPlayer.isPlaying()) {
					mediaPlayer.stop();
					playState = false;
				}else {
					playState = false;
				}
				player_imgview.setImageResource(R.drawable.play_btn1);
			}
		}
	};
	
	View.OnTouchListener recordOnTouchListener = new  View.OnTouchListener(){

		@Override
		public boolean onTouch(View v, MotionEvent event)
		{
			//先判断是否可以新增
			createIfNoNote();
			switch (event.getAction())
			{
			case MotionEvent.ACTION_DOWN:
				if (RECODE_STATE != RECORD_ING) {
					scanOldFile();		
					mr = new AudioRecorder(voiceUrl+noteId+".amr");
					RECODE_STATE=RECORD_ING;
					
					record.setText("松开    结束");
					showVoiceDialog();
					try {
						mr.start();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					mythread();
				}
				break;
			case MotionEvent.ACTION_UP:
				if (RECODE_STATE == RECORD_ING) {
					RECODE_STATE=RECODE_ED;
					if (dialog.isShowing()) {
						dialog.dismiss();
					}
					try {
							mr.stop();
							voiceValue = 0.0;
						} catch (IOException e) {
							e.printStackTrace();
						}
							if (recodeTime < MIX_TIME) {
								showWarnToast();
								record.setText("按住    说话");
								RECODE_STATE=RECORD_NO;
							}else{
							 record.setText("按住    说话"); //录音完成!按住重新录音
							 //luyin_txt.setText("录音时间："+((int)recodeTime));
							 //luyin_path.setText("文件路径："+getAmrPath());
							}
				}

				break;
			}
			return false;
		}
	};
	
	//删除老文件
		void scanOldFile(){
			File file = new File(Environment  
	                .getExternalStorageDirectory(), "my/voice.amr");
			if(file.exists()){
				file.delete();
			}
		}
		
		//录音时显示Dialog
		void showVoiceDialog(){
			dialog = new Dialog(NoteDetailActivity.this,R.style.DialogStyle);
			dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
					WindowManager.LayoutParams.FLAG_FULLSCREEN);
			dialog.setContentView(R.layout.my_dialog); 
			dialog_img=(ImageView)dialog.findViewById(R.id.dialog_img);
			dialog.show();
		}
		
		//录音时间太短时Toast显示
		void showWarnToast(){
			Toast toast = new Toast(NoteDetailActivity.this);
			 LinearLayout linearLayout = new LinearLayout(NoteDetailActivity.this);
			 linearLayout.setOrientation(LinearLayout.VERTICAL); 
			 linearLayout.setPadding(20, 20, 20, 20);
			
			// 定义一个ImageView
			 ImageView imageView = new ImageView(NoteDetailActivity.this);
			 imageView.setImageResource(R.drawable.voice_to_short); // 图标
			 
			 TextView mTv = new TextView(NoteDetailActivity.this);
			 mTv.setText("时间太短   录音失败");
			 mTv.setTextSize(14);
			 mTv.setTextColor(Color.WHITE);//字体颜色
			 //mTv.setPadding(0, 10, 0, 0);
			 
			// 将ImageView和ToastView合并到Layout中
			 linearLayout.addView(imageView);
			 linearLayout.addView(mTv);
			 linearLayout.setGravity(Gravity.CENTER);//内容居中
			 linearLayout.setBackgroundResource(R.drawable.record_bg);//设置自定义toast的背景
			 
			 toast.setView(linearLayout); 
			 toast.setGravity(Gravity.CENTER, 0,0);//起点位置为中间     100为向下移100dp
			 toast.show();				
		}
		
		
		//录音计时线程
		void mythread(){
			recordThread = new Thread(ImgThread);
			recordThread.start();
		}

		//录音Dialog图片随声音大小切换
		void setDialogImage(){
			if (voiceValue < 200.0) {
				dialog_img.setImageResource(R.drawable.voice001);
			}else if (voiceValue > 200.0 && voiceValue < 400) {
				dialog_img.setImageResource(R.drawable.voice002);
			}else if (voiceValue > 400.0 && voiceValue < 800) {
				dialog_img.setImageResource(R.drawable.voice003);
			}else if (voiceValue > 800.0 && voiceValue < 1600) {
				dialog_img.setImageResource(R.drawable.voice004);
			}else if (voiceValue > 1600.0 && voiceValue < 3200) {
				dialog_img.setImageResource(R.drawable.voice005);
			}else if (voiceValue > 3200.0 && voiceValue < 5000) {
				dialog_img.setImageResource(R.drawable.voice006);
			}else if (voiceValue > 5000.0 && voiceValue < 7000) {
				dialog_img.setImageResource(R.drawable.voice007);
			}else if (voiceValue > 7000.0 && voiceValue < 10000.0) {
				dialog_img.setImageResource(R.drawable.voice008);
			}else if (voiceValue > 10000.0 && voiceValue < 14000.0) {
				dialog_img.setImageResource(R.drawable.voice009);
			}else if (voiceValue > 14000.0 && voiceValue < 17000.0) {
				dialog_img.setImageResource(R.drawable.voice010);
			}else if (voiceValue > 17000.0 && voiceValue < 20000.0) {
				dialog_img.setImageResource(R.drawable.voice011);
			}else if (voiceValue > 20000.0) {
				dialog_img.setImageResource(R.drawable.voice012);
			}
		}
		
		//录音线程
		private Runnable ImgThread = new Runnable() {

			@Override
			public void run() {
				recodeTime = 0.0f;
				while (RECODE_STATE==RECORD_ING) {
					//录音超过100秒自动停止
					if (recodeTime >= MAX_TIME && MAX_TIME != 0) {
						imgHandle.sendEmptyMessage(0);
					}else{
					try {
						Thread.sleep(200);
						recodeTime += 0.2;
						if (RECODE_STATE == RECORD_ING) {
							voiceValue = mr.getAmplitude();
							imgHandle.sendEmptyMessage(1);
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				}
			}

		 Handler imgHandle = new Handler() {
				@Override
				public void handleMessage(Message msg) {
	            
					switch (msg.what) {
					case 0:
						//录音超过100秒自动停止
						if (RECODE_STATE == RECORD_ING) {
							RECODE_STATE=RECODE_ED ;
							if (dialog.isShowing()) {
								dialog.dismiss();
							}
							try {
									mr.stop();
									voiceValue = 0.0;
								} catch (IOException e) {
									e.printStackTrace();
								}
									
									if (recodeTime < 1.0) {
										showWarnToast();
										record.setText("按住    说话");
										RECODE_STATE=RECORD_NO;
									}else{
									 record.setText("按住    说话");
									 //luyin_txt.setText("录音时间："+((int)recodeTime));
									 //luyin_path.setText("文件路径："+getAmrPath());
									}
						}
						break;
					case 1:
						setDialogImage();
						break;
					default:
						break;
					}
					
				}
			};
		};
	
	//---录音相关end
		

		
     //---图片相关start
		private OnClickListener cameraOnClickListener = new OnClickListener(){
			@Override
			public void onClick(View v) {
				//当不存在记录时，先新增再做后续操作
				if(noteId < 1){
					insertOrUpdateNote();
				}
				newImageName = noteId+"_"+System.currentTimeMillis()+".jpg";
				cameraImageUri =Uri.parse("file://"+ CommonUtil.getSdCardPath() + Constants.IMG_PATH + newImageName);
			             if (camera_dialog == null) {  
			            	 camera_dialog = new AlertDialog.Builder(NoteDetailActivity.this).setItems(new String[] { "拍照", "选择本地图片" }, new DialogInterface.OnClickListener() {  
			                     @Override 
			                     public void onClick(DialogInterface dialog, int which) {  
			                    	 Intent intent =null;
			                         if (which == 0) {  
			                 			intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);//action is capture
			                			intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
			                			startActivityForResult(intent, CHOOSE_FROM_CAMERA);
			                         } else {  
			                 			intent = new Intent(Intent.ACTION_GET_CONTENT, null);
			                			intent.setType("image/*");
			                			intent.putExtra("crop", "true");
			                			intent.putExtra("aspectX", 2);
			                			intent.putExtra("aspectY", 3);
			                			intent.putExtra("outputX", 400);
			                			intent.putExtra("outputY", 600);
			                			intent.putExtra("scale", true);
			                			intent.putExtra("return-data", false);
			                			intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
			                			intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
			                			intent.putExtra("noFaceDetection", false); // no face detection
			                			startActivityForResult(intent, CHOOSE_FROM_FILE);
			                         }  

			                     }  

			                 }).create();  

			             }  
			             if (!camera_dialog.isShowing()) {  
			            	 camera_dialog.show();  
			             }  
			
			}
			
			
		};
		

		  @Override 
		  protected void onActivityResult(int requestCode, int resultCode, Intent intent) { 
		    	 super.onActivityResult(requestCode, resultCode, intent);
		    	 
		         if (resultCode != Activity.RESULT_OK) {   
		     		Log.e(TAG, "requestCode = " + requestCode);
					Log.e(TAG, "resultCode = " + resultCode);
					Log.e(TAG, "data = " + intent);
					return;
		         }else{
		        	 switch (requestCode) {
		        	 case CHOOSE_FROM_CAMERA:
						Intent objIntent = new Intent("com.android.camera.action.CROP");
		    			objIntent.setDataAndType(cameraImageUri, "image/*");
		    			objIntent.putExtra("crop", "true");
		    			objIntent.putExtra("aspectX", 2);
		    			objIntent.putExtra("aspectY", 3);
		    			objIntent.putExtra("outputX", 400);
		    			objIntent.putExtra("outputY", 600);
		    			objIntent.putExtra("scale", true);
		    			objIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
		    			objIntent.putExtra("return-data", false);
		    			objIntent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
		    			objIntent.putExtra("noFaceDetection", true); // no face detection
		    			startActivityForResult(objIntent, COPY_PICTURE);
		        		break;
		        	 case COPY_PICTURE:
		        		 Log.d(TAG, "COPY_PICTURE : intent="+intent);
		        		 showSelectPicture(cameraImageUri);
		        		 break;
		        	 case CHOOSE_FROM_FILE:
		        		 Log.d(TAG, "CHOOSE_PICTURE : intent="+intent);
		        		 showSelectPicture(cameraImageUri);
		        		 break;
		        	 }
		         }
		     }  
		     
		     /**
		      * 
		      * @param uri
		      */
		     private void  showSelectPicture(Uri uri){
		    	 Bitmap bmp = null;
					try {
						bmp = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
						insertIntoEditText(getBitmapMime(bmp,uri));
					} catch (FileNotFoundException e) {
						Log.e(TAG, "showSelectPicture(): " + e);
					}
					
		     }
		     
		     
		     private SpannableString getBitmapMime(Bitmap pic, Uri uri) {
		             String path = uri.getPath();
		             SpannableString ss = new SpannableString(path);
		             ImageSpan span = new ImageSpan(this, pic);
		             ss.setSpan(span, 0, path.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		             return ss;
		         } 
		     
		     
		     private void insertIntoEditText(SpannableString ss) {
		    	 System.out.println("ss="+ss.toString());
		         Editable et = detailContent.getText();// 先获取Edittext中的内容
		         int start = detailContent.getSelectionStart();
		         et.insert(start, ss);// 设置ss要添加的位置
		         detailContent.setText(et);// 把et添加到Edittext中
		         detailContent.setSelection(start + ss.length());// 设置Edittext中光标在最后面显示
		     } 

}
