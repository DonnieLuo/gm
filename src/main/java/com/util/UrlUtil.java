package com.util;

import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

/**
 * Created by Donnie on 2017/2/20.
 */
@Slf4j
public class UrlUtil {
    private static final String APPID = "wx3a42b774b7b91ccf";
    private static final String APPSECRET = "tpfikag8WOgdhafho3-cEgqJVQwTN3daf-u9182mUbVT4H-uHsTqYUye7uk6Acnr";

    private static final String ACCESS_TOKEN_URL = "https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid=wx3a42b774b7b91ccf&corpsecret=tpfikag8WOgdhafho3-cEgqJVQwTN3daf-u9182mUbVT4H-uHsTqYUye7uk6Acnr";

    private static final String UPLOAD_URL = "https://qyapi.weixin.qq.com/cgi-bin/media/upload?access_token=ACCESS_TOKEN&type=TYPE";

    public static String urlGet (String urlStr) {
        StringBuilder json = new StringBuilder();
        BufferedReader in = null;
        try {
            URL url = new URL(urlStr);
            URLConnection conn = url.openConnection();
            in = new BufferedReader(new InputStreamReader(
                    conn.getInputStream()));
            String inputLine = null;
            while ( (inputLine = in.readLine()) != null) {
                json.append(inputLine);
            }
        } catch (MalformedURLException e) {
            log.error(e.toString());
        } catch (IOException e) {
            log.error(e.toString());
        }finally {
            try {
                in.close();
            } catch (IOException e) {
                log.error(e.toString());
            }
        }
        log.debug("------url get result:{}", json);
        return json.toString();
    }
    public static String urlPost(String urlStr, String param) {
        StringBuilder response = new StringBuilder();
        PrintWriter out = null;
        BufferedReader in = null;
        try {
            URL url = new URL(urlStr);
            URLConnection conn = url.openConnection();
            //post method
            conn.setDoOutput(true);
            conn.setDoInput(true);

            out = new PrintWriter(conn.getOutputStream());
            out.print(param);
            out.flush();

            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine = null;
            while ( (inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            out.close();
        } catch (Exception e) {
            log.error(e.toString());
        }
        log.debug("------json post result:{}", response);
        return response.toString();
    }

    public static String upload(String filePath, String accessToken,String type) throws IOException, NoSuchAlgorithmException, NoSuchProviderException, KeyManagementException {
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            throw new IOException("file not exist ");
        }

        String url = UPLOAD_URL.replace("ACCESS_TOKEN", accessToken).replace("TYPE",type);

        URL urlObj = new URL(url);
        //����
        HttpURLConnection con = (HttpURLConnection) urlObj.openConnection();

        con.setRequestMethod("POST");
        con.setDoInput(true);
        con.setDoOutput(true);
        con.setUseCaches(false);

        //��������ͷ��Ϣ
        con.setRequestProperty("Connection", "Keep-Alive");
        con.setRequestProperty("Charset", "UTF-8");

        //���ñ߽�
        String BOUNDARY = "----------" + System.currentTimeMillis();
        con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);

        StringBuilder sb = new StringBuilder();
        sb.append("--");
        sb.append(BOUNDARY);
        sb.append("\r\n");
        sb.append("Content-Disposition: form-data;name=\"file\";filename=\"" + file.getName() + "\"\r\n");
        sb.append("Content-Type:application/octet-stream\r\n\r\n");

        byte[] head = sb.toString().getBytes("utf-8");

        //��������
        OutputStream out = new DataOutputStream(con.getOutputStream());
        //�����ͷ
        out.write(head);

        //�ļ����Ĳ���
        //���ļ������ļ��ķ�ʽ ���뵽url��
        DataInputStream in = new DataInputStream(new FileInputStream(file));
        int bytes = 0;
        byte[] bufferOut = new byte[1024];
        while ((bytes = in.read(bufferOut)) != -1) {
            out.write(bufferOut, 0, bytes);
        }
        in.close();

        //��β����
        byte[] foot = ("\r\n--" + BOUNDARY + "--\r\n").getBytes("utf-8");//����������ݷָ���

        out.write(foot);

        out.flush();
        out.close();

        StringBuffer buffer = new StringBuffer();
        BufferedReader reader = null;
        String result = null;
        try {
            //����BufferedReader����������ȡURL����Ӧ
            reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String line = null;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
            if (result == null) {
                result = buffer.toString();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                reader.close();
            }
        }

        JsonObject jsonObject = GsonUtil.getInstance().fromJson(result, JsonObject.class);
        System.out.println(jsonObject);
        String typeName = "media_id";
        if(!"image".equals(type)){
            typeName = type + "_media_id";
        }
        String mediaId = jsonObject.get(typeName).getAsString();
        return mediaId;
    }

    public static String getAccessToken() {
        String url = ACCESS_TOKEN_URL.replace("APPID", APPID).replace("APPSECRET", APPSECRET);
        String tokenJsonStr = urlGet(url);
        JsonObject jsonObject = GsonUtil.getInstance().fromJson(tokenJsonStr, JsonObject.class);
        if(jsonObject!=null){
            return jsonObject.get("access_token").getAsString();
        }
        log.error("------get access token failed");
        return "";
    }
}
