package arcanelux.cameraintent;

import java.io.File;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

import com.example.cameraintent.R;

public class CameraIntentActivity extends Activity implements OnClickListener {
	private final int TAKE_CAMERA = 100;		// 카메라 인텐트 사용 시 onActivityResult에서 사용할 requestCode
	private final String TAG = "Arcanelux_CameraIntent";

	private Button btnCameraIntent;
	private ImageView ivResult;
	
	// 카메라 찍은 후 저장될 파일 경로
	private String filePath;
	private String folderName = "Arcanelux";// 폴더명
	private String fileName = "CameraIntent"; // 파일명

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// 뷰 변수 할당
		btnCameraIntent = (Button) findViewById(R.id.btnCameraIntent);
		ivResult = (ImageView) findViewById(R.id.ivResult);

		// 버튼 클릭리스터 설정
		btnCameraIntent.setOnClickListener(this);
	}

	// onClick 이벤트
	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.btnCameraIntent:
			Intent intent = new Intent();
			Camera camera = Camera.open();
			Camera.Parameters parameters = camera.getParameters();
			List<Size> sizeList = parameters.getSupportedPictureSizes();
			// 카메라 SupportedPictureSize목록 출력 로그
			// for(int i=0; i<sizeList.size(); i++){
				// Size size = sizeList.get(i);
				//	Log.d(TAG, "Width : " + size.width + ", Height : " + size.height);
			// }
			// 원하는 최적화 사이즈를 1280x720 으로 설정
			Camera.Size size =  getOptimalPictureSize(parameters.getSupportedPictureSizes(), 1280, 720);
			Log.d(TAG, "Selected Optimal Size : (" + size.width + ", " + size.height + ")");
			parameters.setPreviewSize(size.width,  size.height);
			parameters.setPictureSize(size.width,  size.height);
			camera.setParameters(parameters);
			camera.release();
			
			// 저장할 파일 설정
			// 외부저장소 경로
			String path = Environment.getExternalStorageDirectory().getAbsolutePath();

			// 폴더명 및 파일명
			String folderPath = path + File.separator + folderName;
			filePath = path + File.separator + folderName + File.separator +  fileName + ".jpg";
			
			// 저장 폴더 지정 및 폴더 생성
			File fileFolderPath = new File(folderPath);
			fileFolderPath.mkdir();

			// 파일 이름 지정
			File file = new File(filePath);
			Uri outputFileUri = Uri.fromFile(file);
			
			
			// 카메라 작동시키는 Action으로 인텐트 설정, OutputFileURI 추가
			intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
			intent.putExtra( MediaStore.EXTRA_OUTPUT, outputFileUri );
			// requestCode지정해서 인텐트 실행
			startActivityForResult(intent, TAKE_CAMERA);
			break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode == RESULT_OK){
			if(requestCode == TAKE_CAMERA){
				// 카메라 찍기 액션 후, 지정된 파일을 비트맵으로 꺼내 이미지뷰에 삽입
				BitmapFactory.Options options = new BitmapFactory.Options();
			    options.inPreferredConfig = Config.RGB_565;
			    Bitmap bm = BitmapFactory.decodeFile(filePath, options);
			    ivResult.setImageBitmap(bm);
			    
				// Background에 Drawable로 사용할 경우엔 아래와 같이 사용
				//	Drawable drawable = new BitmapDrawable(bm);
				//	btnCameraIntent.setBackground(drawable);

			    
			    // Intent에 "data"로 넘어온 비트맵을 이미지뷰에 삽입 (저해상도 썸네일만 넘어와서 사용 불가)
//			    Bitmap bm = (Bitmap)data.getExtras().get("data");
//			    ivResult.setImageBitmap(bm);
			    
				// File 저장 예제 (사용하지 않음)
//				try {
//					// 외부저장소 경로
//					String path = Environment.getExternalStorageDirectory().getAbsolutePath();
//
//					// 폴더명 및 파일명
//					String folderName = "Arcanelux";// 폴더명
//					String fileName = "CameraIntent"; // 파일명
//
//					// 폴더 경로 및 파일 경로
//					String folderPath = path + "/" + folderName;
//					String filePath = folderPath + "/" + fileName + ".png";
//
//					// 저장 폴더 지정 및 폴더 생성
//					File fileFolderPath = new File(folderPath);
//					fileFolderPath.mkdir();
//
//					// 파일 이름 지정
//					File file = new File(filePath);
//					FileOutputStream fos = new FileOutputStream(file);
//
//					// 비트맵을 PNG방식으로 압축하여 저장
//					if (fos != null){ 
//						bm.compress(Bitmap.CompressFormat.PNG, 100, fos); 
//						fos.close(); 
//					}
//
//					// 로그 및 토스트
//					String logMessage = "File Save Success, File : " + filePath;
//					Toast.makeText(getApplicationContext(), logMessage, Toast.LENGTH_LONG).show();
//					Log.d(TAG, logMessage);
//				} catch (Exception e)	{
//					e.printStackTrace();
//					Log.d(TAG, "File Save Failed");
//				} 
			}
		}
	}

	
	// 지정한 해상도에 가장 최적화 된 카메라 캡쳐 사이즈 구해주는 함수
	private Size getOptimalPictureSize(List<Size> sizeList, int width, int height){
		Log.d(TAG, "getOptimalPictureSize, 기준 width,height : (" + width + ", " + height + ")");
		Size prevSize = sizeList.get(0);
		Size optSize = sizeList.get(1);
		for(Size size : sizeList){
			// 현재 사이즈와 원하는 사이즈의 차이
			int diffWidth = Math.abs((size.width - width));
			int diffHeight = Math.abs((size.height - height));

			// 이전 사이즈와 원하는 사이즈의 차이
			int diffWidthPrev = Math.abs((prevSize.width - width));
			int diffHeightPrev = Math.abs((prevSize.height - height));

			// 현재까지 최적화 사이즈와 원하는 사이즈의 차이
			int diffWidthOpt = Math.abs((optSize.width - width));
			int diffHeightOpt = Math.abs((optSize.height - height));

			// 이전 사이즈보다 현재 사이즈의 가로사이즈 차이가 적을 경우 && 현재까지 최적화 된 세로높이 차이보다 현재 세로높이 차이가 적거나 같을 경우에만 적용
			if(diffWidth < diffWidthPrev && diffHeight <= diffHeightOpt){
				optSize = size;
				Log.d(TAG, "가로사이즈 변경 / 기존 가로사이즈 : " + prevSize.width + ", 새 가로사이즈 : " + optSize.width);
			}
			// 이전 사이즈보다 현재 사이즈의 세로사이즈 차이가 적을 경우 && 현재까지 최적화 된 가로길이 차이보다 현재 가로길이 차이가 적거나 같을 경우에만 적용
			if(diffHeight < diffHeightPrev && diffWidth <= diffWidthOpt){
				optSize = size;
				Log.d(TAG, "세로사이즈 변경 / 기존 세로사이즈 : " + prevSize.height + ", 새 세로사이즈 : " + optSize.height);
			}

			// 현재까지 사용한 사이즈를 이전 사이즈로 지정
			prevSize = size;
		}
		Log.d(TAG, "결과 OptimalPictureSize : " + optSize.width + ", " + optSize.height);
		return optSize;
	}

}
