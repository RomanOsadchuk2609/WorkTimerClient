package osadchuk.worktimer;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sun.javafx.application.HostServicesDelegate;
import javafx.embed.swing.SwingFXUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.security.core.Authentication;
import osadchuk.worktimer.controller.HomeController;
import osadchuk.worktimer.controller.SettingsController;
import osadchuk.worktimer.entity.PrimitiveUser;
import osadchuk.worktimer.entity.SimpleTask;
import osadchuk.worktimer.entity.Timer;
import osadchuk.worktimer.util.TimerConstants;
import osadchuk.worktimer.webRequest.HTTPRequest;
import sun.misc.BASE64Decoder;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.prefs.Preferences;

@Slf4j
public class Utils {

    public static List<SimpleTask> simpleTaskList;
    public static String JSESSIONID;
    public static Authentication authToken;
    public static javafx.scene.image.Image userIcon;
    public static String username;
    public static HostServicesDelegate hostServices;
    public static HomeController homeController;

    public static final String INTERNET_ERROR_IN_WORKING =
            "Could not connect to\n" +
                    "the server. Please, check\n" +
                    "your Internet connection.\n" +
                    "The data about your work\n" +
                    "will be sent the next time.";

    public static final String INTERNET_ERROR =
            "Could not connect to\n" +
                    "the server. Please, check\n" +
                    "your Internet connection.";

    public static final String CHEATING_ERROR = "Don't cheat!";

    public static final String IDENTICAL_SCREENSHOTS =
            "Identical screenshots were\n" +
                    "detected!";

    public static String serverIpAddress;

    static {
        Map<TimerConstants.PROPERTY, String> propertyMap = loadPropertymap();
        setServerInfo(propertyMap);
    }

    public static Map<TimerConstants.PROPERTY, String> loadPropertymap() {
        Map<TimerConstants.PROPERTY, String> propertyMap = new EnumMap<>(TimerConstants.PROPERTY.class);
        Preferences pref = Preferences.userNodeForPackage(SettingsController.class);
        String protocol = pref.get(TimerConstants.PROPERTY.PROTOCOL.getName(), TimerConstants.EMPTY_STRING);
        String ip = pref.get(TimerConstants.PROPERTY.IP_ADDRESS.getName(), TimerConstants.EMPTY_STRING);
        String port = pref.get(TimerConstants.PROPERTY.PORT.getName(), TimerConstants.EMPTY_STRING);
        boolean protocolIsNull = false;
        boolean ipIsNull = false;
        boolean portIsNull = false;

        if (ip == null || ip.isEmpty()) ipIsNull = true;
        if (port == null || port.isEmpty()) portIsNull = true;
        if (protocol == null || protocol.isEmpty()) protocolIsNull = true;

        if (ipIsNull || portIsNull || protocolIsNull) {
            try {
                Properties settings = new Properties();
                settings.load(Utils.class.getResourceAsStream("/public/config/settings.conf"));

                if (protocolIsNull) protocol = settings.getProperty(TimerConstants.PROPERTY.PROTOCOL.getName());
                if (ipIsNull) ip = settings.getProperty(TimerConstants.PROPERTY.IP_ADDRESS.getName());
                if (portIsNull) port = settings.getProperty(TimerConstants.PROPERTY.PORT.getName());

            } catch (IOException e) {
                log.error("Error: ", e);
            }
        }
        propertyMap.putIfAbsent(TimerConstants.PROPERTY.PROTOCOL, protocol);
        propertyMap.putIfAbsent(TimerConstants.PROPERTY.IP_ADDRESS, ip);
        propertyMap.putIfAbsent(TimerConstants.PROPERTY.PORT, port);
        return propertyMap;
    }

    public static void setServerInfo(Map<TimerConstants.PROPERTY, String> propertyMap) {
        StringBuilder serverAddressBuilder = new StringBuilder();
        serverAddressBuilder.append(propertyMap.get(TimerConstants.PROPERTY.PROTOCOL));
        serverAddressBuilder.append("://");
        serverAddressBuilder.append(propertyMap.get(TimerConstants.PROPERTY.IP_ADDRESS));
        String port = propertyMap.get(TimerConstants.PROPERTY.PORT);
        if (port != null && !port.equals("80")) {
            serverAddressBuilder.append(":");
            serverAddressBuilder.append(port);
        }
        serverAddressBuilder.append("/");
        serverIpAddress = serverAddressBuilder.toString();
    }

    public static Timer getTimerFromJson(String jsonString) {
        Gson gson = new Gson();
        return gson.fromJson(jsonString, Timer.class);
    }

    public static List<SimpleTask> getListOfSimpleTasksFromJson(String value) {
        Type listType = new TypeToken<List<SimpleTask>>() {
        }.getType();
        return new Gson().fromJson(value, listType);
    }

    public static List<PrimitiveUser> getListOfPrimitiveUsersFromJson(String value) {
        Type listType = new TypeToken<List<PrimitiveUser>>() {
        }.getType();
        return new Gson().fromJson(value, listType);
    }

    public static BufferedImage resizeImage(BufferedImage img, int height, int width) {
        Image tmp = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = resized.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();
        return resized;
    }

    public static String authenticationToJson(Authentication authentication) {
        Gson gson = new Gson();
        return gson.toJson(authentication);
    }

    public static Authentication authenticationFromJson(String authentication) {
        Gson gson = new Gson();
        return gson.fromJson(authentication, Authentication.class);
    }

    public static javafx.scene.image.Image getImageFromBase64(String base64Image, Integer height, Integer width) {
        int startIndex = base64Image.indexOf(',');
        base64Image = base64Image.substring(startIndex + 1);
        BufferedImage bufferedImage = null;
        byte[] imageByte;
        try {
            BASE64Decoder decoder = new BASE64Decoder();
            imageByte = decoder.decodeBuffer(base64Image);
            ByteArrayInputStream bis = new ByteArrayInputStream(imageByte);
            bufferedImage = ImageIO.read(bis);
            bis.close();
        } catch (Exception e) {
            log.error("Error: ", e);
        }
        javafx.scene.image.Image image = null;
        if (height != null && width != null && bufferedImage != null) {
            bufferedImage = Utils.resizeImage(bufferedImage, 30, 30);
            image = SwingFXUtils.toFXImage(bufferedImage, null);
        }
        return image;
    }

    public static void updateSimpleTaskList(boolean showUpdateDialog) throws IOException, URISyntaxException {
        List<NameValuePair> parameters = new ArrayList<>();
        parameters.add(new BasicNameValuePair("username", Utils.username));
        String response = HTTPRequest.getResponseFromPost(Utils.serverIpAddress + "api/simple_tasks/by_username", parameters, Utils.JSESSIONID);
        List<SimpleTask> list = Utils.getListOfSimpleTasksFromJson(response);

        if (list != null && !list.isEmpty() && !list.equals(Utils.simpleTaskList)) {
            Utils.simpleTaskList = list;
            homeController.showTaskTreeView();
        } else {
            Utils.simpleTaskList = null;
        }

        if (showUpdateDialog) {
            homeController.showTaskUpdateDialog();
        }
    }
}
