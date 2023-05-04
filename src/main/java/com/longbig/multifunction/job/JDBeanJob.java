package com.longbig.multifunction.job;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.longbig.multifunction.utils.FileUtils;
import com.longbig.multifunction.utils.OkHttpUtils;
import com.longbig.multifunction.utils.ResourceUtils;
import lombok.extern.slf4j.Slf4j;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author yuyunlong
 * @date 2022/2/27 12:54 下午
 * @description
 */
@Component
@Slf4j
public class JDBeanJob {

//    @Value("${jd.pt_key}")
//    private String pt_key;
//
//    @Value("${jd.pt_pin}")
//    private String pt_pin;
    @Autowired
    private ResourceUtils resourceUtils;
    @Value("${jd.filePath}")
    private String filePath;

    @Value("${start.docker}")
    private Integer fromDocker;

    private List<String> cookies;

    @PostConstruct
    public void init() {
        log.info("加载cookie到全局变量");
        cookies = Lists.newArrayList();
        List<String> ptAlls = Lists.newArrayList();
        Boolean isFromDocker = fromDocker == 1 ? true : false;
        if (isFromDocker) {
            log.info("start from docker，filePath:{}", filePath);
            ptAlls = FileUtils.readFileToStringList(filePath);
        } else {
            log.info("start from java, filePath:{}", filePath);
            ptAlls = resourceUtils.readFromClassPath(filePath);
        }
        for (String cookie : ptAlls) {
            cookies.add(cookie);
        }
        log.info("cookies size：{}", cookies.size());
    }

    /**
     * 京东每日签到
     *
     * @return
     */
    @Scheduled(cron = "0 0 6,18 1/1 * ?")
    public String getJdSign() throws Exception {
        String url = "https://api.m.jd.com/client.action?functionId=signBeanAct&body=%7B%22fp%22%3A%22-1%22%2C%22shshshfp%22%3A%22-1%22%2C%22shshshfpa%22%3A%22-1%22%2C%22referUrl%22%3A%22-1%22%2C%22userAgent%22%3A%22-1%22%2C%22jda%22%3A%22-1%22%2C%22rnVersion%22%3A%223.9%22%7D&appid=ld&client=apple&clientVersion=10.0.4&networkType=wifi&osVersion=14.8.1&uuid=3acd1f6361f86fc0a1bc23971b2e7bbe6197afb6&openudid=3acd1f6361f86fc0a1bc23971b2e7bbe6197afb6&jsonp=jsonp_1645885800574_58482";
        String body = "{\"eid\":\"eidAb47c8121a5s24aIy0D0WQXSKdROGt9BUSeGiNEbMeQodwSwkLi6x5/GTFC7BV7lPMjljpMxVNCcAW/qdrQvDSdhaI5715Sui3MB7nluMccMWqWFL\",\"fp\":\"-1\",\"jda\":\"-1\",\"referUrl\":\"-1\",\"rnVersion\":\"4.7\",\"shshshfp\":\"-1\",\"shshshfpa\":\"-1\",\"userAgent\":\"-1\"}";

        int n = 0;
        Map<String, String> header = Maps.newHashMap();
        RequestBody requestBody = new FormBody.Builder().add("body", body).build();

        for (String cookie : cookies) {
            String response = OkHttpUtils.post(url, cookie, requestBody, header);
            log.info("京东签到任务执行次数:{}, 结果:{}", ++n, response);
            Thread.sleep(1000L);
        }
        return "执行完成";
    }

    /**
     * 京东摇京豆签到
     *
     * @return
     */
    @Scheduled(cron = "0 0 7,19 1/1 * ?")
    public String getSharkBean() throws Exception {
        log.info("摇京豆签到开始");
        int n = 0;
        for (String cookie : cookies) {
            for (int i = 1; i < 8; i++) {
                String url = "https://api.m.jd.com/?appid=sharkBean&functionId=pg_interact_interface_invoke&body=%7B%22floorToken%22:%22f1d574ec-b1e9-43ba-aa84-b7a757f27f0e%22,%22dataSourceCode%22:%22signIn%22,%22argMap%22:%7B%22currSignCursor%22:" +
                        i +
                        "%7D,%22riskInformation%22:%7B%22platform%22:1,%22pageClickKey%22:%22%22,%22eid%22:%227IJ4SBWVAY6L5FOEQHCBZ57B3CYAYAA4LGJH2NGO6F6BE7PLEAJUY5WQOUI4BDGFRPH3RSGPLV5APHF4YV4DMJZ2UQ%22,%22fp%22:%22e0e4fadfadac7be71f89b78901f60fe4%22,%22shshshfp%22:%2298d7f7d062531be7af606b13b9c57a3e%22,%22shshshfpa%22:%222768c811-4a2f-1596-cf01-9d0cbd0319b9-1651280386%22,%22shshshfpb%22:%22iMZyawmZjTHrSJ72sZmuHog%22%7D%7D";
                Map<String, String> header = new HashMap<>();
                header.put("origin", "https://spa.jd.com");
                header.put("referer", "https://spa.jd.com/");
                RequestBody requestBody = new FormBody.Builder().build();

                String response = OkHttpUtils.post(url, cookie, requestBody, header);
                log.info("摇京豆执行{}次，response:{}", ++n, response);
                JSONObject object = JSON.parseObject(response);
                String success = object.getString("success");
                Thread.sleep(3000);
            }
        }
        return "success";
    }

    /**
     * 京豆抽奖任务，抽奖获取的京豆随机
     *
     * @return
     */
    @Scheduled(cron = "0 0 8,20 1/1 * ?")
    public String getLottery() throws Exception {
        String url = "https://api.m.jd.com/client.action?functionId=babelGetLottery";
        String body = "{\"enAwardK\":\"ltvTJ/WYFPZcuWIWHCAjRz/NdrezuUkm8ZIGKKD06/oaqi8FPY5ILISE5QLULmK6RUnNSgnFndqy\\ny4p8d6/bK/bwdZK6Aw80mPSE7ShF/0r28HWSugMPNPm5JQ8b9nflgkMfDwDJiaqThDW7a9IYpL8z\\n7mu4l56kMNsaMgLecghsgTYjv+RZ8bosQ6kKx+PNAP61OWarrOeJ2rhtFmhQncw6DQFeBryeMUM1\\nw9SpK5iag4uLvHGIZstZMKOALjB/r9TIJDYxHs/sFMU4vtb2jX9DEwleHSLTLeRpLM1w+RakAk8s\\nfC4gHoKM/1zPHJXq1xfwXKFh5wKt4jr5hEqddxiI8N28vWT05HuOdPqtP+0EbGMDdSPdisoPmlru\\n+CyHR5Kt0js9JUM=_babel\",\"awardSource\":\"1\",\"srv\":\"{\\\"bord\\\":\\\"0\\\",\\\"fno\\\":\\\"0-0-2\\\",\\\"mid\\\":\\\"70952802\\\",\\\"bi2\\\":\\\"2\\\",\\\"bid\\\":\\\"0\\\",\\\"aid\\\":\\\"01155413\\\"}\",\"encryptProjectId\":\"3u4fVy1c75fAdDN6XRYDzAbkXz1E\",\"encryptAssignmentId\":\"2x5WEhFsDhmf8JohWQJFYfURTh9w\",\"authType\":\"2\",\"riskParam\":{\"platform\":\"3\",\"orgType\":\"2\",\"openId\":\"-1\",\"pageClickKey\":\"Babel_WheelSurf\",\"eid\":\"eidI69b381246dseNGdrD6vtTrOauSQ/zRycuDRnbInWZmVfFbyoI59uVkzYYiQZrUGzGkpqNpHHJHv37CthY6ooTnYpqX2mBZ2riJHvc8c9kta1QpZh\",\"fp\":\"-1\",\"shshshfp\":\"98d7f7d062531be7af606b13b9c57a3e\",\"shshshfpa\":\"2768c811-4a2f-1596-cf01-9d0cbd0319b9-1651280386\",\"shshshfpb\":\"iMZyawmZjTHrSJ72sZmuHog\",\"childActivityUrl\":\"https%3A%2F%2Fpro.m.jd.com%2Fmall%2Factive%2F2xoBJwC5D1Q3okksMUFHcJQhFq8j%2Findex.html%3Ftttparams%3DjyJinIeyJnTG5nIjoiMTE2LjQwNjQ1IiwiZ0xhdCI6IjQwLjA2MjkxIn60%253D%26un_area%3D1_2901_55565_0%26lng%3D116.4065317104862%26lat%3D40.06278498159455\",\"userArea\":\"-1\",\"client\":\"\",\"clientVersion\":\"\",\"uuid\":\"\",\"osVersion\":\"\",\"brand\":\"\",\"model\":\"\",\"networkType\":\"\",\"jda\":\"-1\"},\"siteClient\":\"apple\",\"mitemAddrId\":\"\",\"geo\":{\"lng\":\"116.4065317104862\",\"lat\":\"40.06278498159455\"},\"addressId\":\"5777681655\",\"posLng\":\"\",\"posLat\":\"\",\"homeLng\":\"116.40645\",\"homeLat\":\"40.06291\",\"focus\":\"\",\"innerAnchor\":\"\",\"cv\":\"2.0\"}";

        log.info("抽京豆开始");
        Map<String, String> header = Maps.newHashMap();
        header.put("origin", "https://pro.m.jd.com");
        header.put("referer", "https://pro.m.jd.com/");

        RequestBody requestBody = new FormBody.Builder()
                .add("body", body)
                .add("client", "wh5")
                .add("clientVersion", "1.0.0")
                .build();
        int n = 0;
        for (String cookie : cookies) {
            String response = OkHttpUtils.post(url, cookie, requestBody, header);
            log.info("抽京豆执行{}次，response：{}", ++n, response);
            Thread.sleep(1000L);

        }
        return "success";

    }

    /**
     * 京东plus会员签到
     *
     * @return
     * @throws IOException
     */
    @Scheduled(cron = "0 0 9,21 1/1 * ?")
    public String plusSign() throws Exception {
        String url = "https://api.m.jd.com/client.action?functionId=doInteractiveAssignment";
        String body = "{\"sourceCode\":\"acetttsign\",\"encryptProjectId\":\"3FCTNcsr7BoQUw7dx1h3KJ9Hi9yJ\",\"encryptAssignmentId\":\"3o2cWjTjZoCjKJcQwQ2bFgLkTnZC\",\"completionFlag\":true,\"itemId\":\"1\",\"extParam\":{\"forceBot\":\"1\",\"businessData\":{\"random\":\"LLSuE5uy\"},\"signStr\":\"1651280954835~1OgLu20Tq0QMDFGRlluRzk5MQ==.d3BsX3V+dmBadHV2bxBxf3NgXyYAczZfOXdqb0J2ancnXDl3ODYjLgosPAp3dTMVMRUnFxMWKQwxKAkWOA==.6aac9775~1,1~18BD8887199573132F7270C7423274FE2B819200~1yxx4at~C~TRNMWhYJbWwUFUBdWxMCbBZXAxx6eRhydB0BBGYfBx8IBwQfQhMUFVAEG3N2G3VxGggOfhgCGAEIBxhHFGwUFVNBWBMCBhgRRUIaDRYCAAQJBQwDDwULBwMLDwMAAxYfFEZdUxYJFEVMQ0BHUEReFRgRQVRZFQ4RUFdMQ0FHQ1AaGxZDUl8aDW8ABB0JDgMfBwIUDhgCAx0JahgRXFsaDQUfFFJLFQ4RUwkOBVYHVwMMBABRUAIODlBRV1QMAgIBAwELBARRAVAaGxZdRhMCFXlSVXhWQ1FfFB0aQxYJBwcNBgYLBggNAwwAAx0aXV8RDBNZFRgRUEFaFQ4RWXxRe15WUgELQnhsZFBpfUxdfEZJUGURGhNWQRYJFHZXWFNfUxFxWVcdFB0aWVVFFAsaVBYfFEJbRRYJbQkKDhgGBgFlGxZBWRMCbBZSFB0aVhYfFFAaGxZSFB0aVhYfFFAaGxZSFGwUFV1cVxMCFVJVUFdeUUBHFB0aVl4RDBNNFRgRVVgaDRZEBR8NGQYRGhNbUWtFFAsaDg0RGhNaUxYJFENZWVBcWwx0e2dGcwRNThYfFFxSFQ5oBx0IGwRuGhNaW1tUFAsaVhYfFFxLUBYJFFAaSg==~1cs6hu7\",\"sceneid\":\"babel_3joSPpr7RgdHMbcuqoRQ8HbcPo9U\"},\"activity_id\":\"3joSPpr7RgdHMbcuqoRQ8HbcPo9U\",\"template_id\":\"00019605\",\"floor_id\":\"75471325\",\"enc\":\"A4737E261D02E91C30C566C1C671734D124B75F8759F591EFAFB127342C10708BAA7D80C309F2B17973BB15312D14004B865E9A1F04C7C3E3E312AA7309E7B31\"}";
        log.info("京东plus会员签到开始");
        Map<String, String> header = Maps.newHashMap();
        header.put("origin", "https://pro.m.jd.com");
        header.put("referer", "https://pro.m.jd.com/");

        RequestBody requestBody = new FormBody.Builder()
                .add("body", body)
                .add("appid", "babelh5")
                .add("sign", "11")
                .build();
        int n = 0;
        for (String cookie : cookies) {
            String response = OkHttpUtils.post(url, cookie, requestBody, header);
            log.info("京东plus会员签到执行{}次，response：{}", ++n, response);
            Thread.sleep(1000L);
        }

        return "success";
    }

}
