import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SdkSuppress;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.BySelector;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject2;
import android.util.Log;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;

import java.io.File;

/**
 * Created by guarionex on 9/13/16.
 */
@RunWith(AndroidJUnit4.class)
@SdkSuppress(minSdkVersion = 18)
public class Test {
    UiDevice uiDevice;

    @Before
    public void setUp() {
        // Initialize UiDevice instance
        Log.d("JUnit", "Setup running");
        uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        assert uiDevice != null;

        // Start from the home screen
        uiDevice.pressHome();

    }

    @org.junit.Test
    public void test() throws InterruptedException {
        openApp("com.emobilepos.app");
        UiObject2 editText = waitForObject(By.res("com.emobilepos.app:id/initAccountNumber"));
        takeScreenshot("screenshot-1.png");
        editText.setText("150023120409");
        UiObject2 protectObject = waitForObject(By.text("Log In"));
        protectObject.click();
        takeScreenshot("screenshot-2.png");

        Thread.sleep(10000);
    }

    private void openApp(String packageName) {
        Context context = InstrumentationRegistry.getInstrumentation().getContext();
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }

    private void takeScreenshot(String name) {
        Log.d("TEST", "takeScreenshot");
        String dir = String.format("%s/%s", Environment.getExternalStorageDirectory().getPath(), "test-screenshots");
        File theDir = new File(dir);
        if (!theDir.exists()) {
            theDir.mkdir();
        }
        uiDevice.takeScreenshot(new File(String.format("%s/%s", dir, name)));
    }

    private UiObject2 waitForObject(BySelector selector) throws InterruptedException {
        UiObject2 object = null;
        int timeout = 30000;
        int delay = 1000;
        long time = System.currentTimeMillis();
        while (object == null) {
            object = uiDevice.findObject(selector);
            Thread.sleep(delay);
            if (System.currentTimeMillis() - timeout > time) {

            }
        }
        return object;
    }

}
