package vikas.gettingwatchdata.Constants;

/**
 * Created by vikasaggarwal on 24/03/18.
 */

public class Constant {
   public static boolean IS_DEBUGGER_ON = true;

   // watch data constant
   public static int SLEEP  = 1;
   public static int HEART_RATE  = 2;
   public static int REAL_TIME_HEART_RATE  = -85;
   public static int REAL_TIME_MM_HG = 65;// this is wrong
   public static int STEPS_COUNT = -84;
   public static int PEDOMETER  = 3;
   public static int PEDOMETER_ALL  = -93;
   public static int HEART_RATE_ALL  = -92;
   public static int HEART_RATE_REAL  = -121;
   public static int SPORTS_MODE  = 4;
   public static int BLOOD_PRESSURE_OXYGEN  = 5;
   public static int OXYGEN  = 9;
   public static int DMEO = 0x13;


   public interface ACTION {
      public static String MAIN_ACTION = "com.truiton.foregroundservice.action.main";
      public static String PREV_ACTION = "com.truiton.foregroundservice.action.prev";
      public static String PLAY_ACTION = "com.truiton.foregroundservice.action.play";
      public static String NEXT_ACTION = "com.truiton.foregroundservice.action.next";
      public static String STARTFOREGROUND_ACTION = "com.truiton.foregroundservice.action.startforeground";
      public static String STOPFOREGROUND_ACTION = "com.truiton.foregroundservice.action.stopforeground";
   }
   public interface NOTIFICATION_ID {
      public static int FOREGROUND_SERVICE = 101;
   }
   public interface SHARED_PREFERENCE_ID {
      public static String SERVICE_COUNT = "SERVICE_COUNT";
      public static String USER_NUMBER = "USER_NUMBER";
      public static String DEVICE_ID = "DEVICE_ID";
      public static String SERVICE_RUNNING = "SERVICE_RUNNING";
      public static String DISTANCE = "DISTANCE";
      public static String CALORIE = "CALORIE";
      public static String STEP = "STEP";
      public static String SYNC_TIME = "SYNC_TIME";
      public static String CONNECT_STATUS = "CONNECT_STATUS";
   }
   public interface URL {
      //public static String SYNC_URL = "http://ec2-13-126-206-32.ap-south-1.compute.amazonaws.com:8080/hc/api/v1/device/sync";
      public static String SYNC_URL = "http://13.232.128.143:8080/hc/api/v1/device/sync";

   }
}
