안드로이드 카메라 사용법
===


안드로이드에서 ACTION_IMAGE_CAPTURE 인텐트를 사용한 카메라 액션의 상세한 사용법입니다  
[상세한 한글 사용법](http://arcanelux.tistory.com/entry/%EC%95%88%EB%93%9C%EB%A1%9C%EC%9D%B4%EB%93%9C-%EC%B9%B4%EB%A9%94%EB%9D%BC-%EA%B0%80%EC%9D%B4%EB%93%9CACTIONIMAGECAPTURE)
__________________
###촬영버튼 클릭(onClick) 후 작동
####인텐트 생성 및 카메라 설정
인텐트 생성 후, 미리 카메라의 설정을 해준다  
Camera.parameters에서 getSupportedPictureSizes()로 현재 앱을 실행중인 기기에서 지원하는 캡쳐 사이즈 리스트를 얻어온다  
원하는 해상도에 가장 최적화된 Camera.Size정보를 구해주는 getOptimalPictureSize함수를 이용해 적용할 Size값을 구한다  
카메라의 Parameter적용을 완료한 후, release시킨다  

```
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
```

####카메라 촬영 후 저장할 사진 경로 설정
촬영 후 저장할 파일을 설정한다  
아래예제에서는 폴더명과 파일명을 이용해 folderPath, filePath를 지정하고, folderPath의 폴더를 먼저 생성해 준 후 filePath의 파일을 생성한다  
생선한 파일의 Uri를 outputFileUri변수에 지정한다  
```
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
```

####카메라 인텐트에 Action및 Extra설정
Intent에 카메라를 작동시키기위해 setAction(MediaStore.ACTION_IMAGE_CAPTURE)를 지정하고,  
putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri) 를 이용해 촬영 후 저장될 파일 경로를 지정해준다  
액티비티 실행 후 Result를 받아 다음 작동을 진행한다  
```
// 카메라 작동시키는 Action으로 인텐트 설정, OutputFileURI 추가
intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
intent.putExtra( MediaStore.EXTRA_OUTPUT, outputFileUri );
// requestCode지정해서 인텐트 실행
startActivityForResult(intent, TAKE_CAMERA);
```
______________________
###onActivityResult에서의 작동

####resultCode와 requestCode확인, 파일에서 Bitmap변환해서 이미지뷰에 삽입
TAKE_CAMERA, 사진찍기 액션 후에 저장된 파일 경로에서 비트맵을 얻어서 이미지뷰에 삽입한다  
```
if(resultCode == RESULT_OK){
			if(requestCode == TAKE_CAMERA){
				// 카메라 찍기 액션 후, 지정된 파일을 비트맵으로 꺼내 이미지뷰에 삽입
				BitmapFactory.Options options = new BitmapFactory.Options();
			    options.inPreferredConfig = Config.RGB_565;
			    Bitmap bm = BitmapFactory.decodeFile(filePath, options);
			    ivResult.setImageBitmap(bm);
```

_____________________
###최적화 카메라 캡쳐 사이즈 구하기
```
//지정한 해상도에 가장 최적화 된 카메라 캡쳐 사이즈 구해주는 함수
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
```
