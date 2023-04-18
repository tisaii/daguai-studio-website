package com.tisai.daguai.utils;

import com.aliyun.tea.TeaException;
import com.tisai.daguai.config.SMSConfig;

/**
 * 短信发送工具类
 */
public class SMSUtils {


	public static com.aliyun.dysmsapi20170525.Client createClient(String accessKeyId, String accessKeySecret) throws Exception {
		com.aliyun.teaopenapi.models.Config config = new com.aliyun.teaopenapi.models.Config()
				// 必填，您的 AccessKey ID
				.setAccessKeyId(accessKeyId)
				// 必填，您的 AccessKey Secret
				.setAccessKeySecret(accessKeySecret);
		// 访问的域名
		config.endpoint = "dysmsapi.aliyuncs.com";
		return new com.aliyun.dysmsapi20170525.Client(config);
	}



	/**
	 * 发送短信
	 * @param phoneNumbers 手机号
	 * @param param 参数
	 */
	public static void sendMessage(String phoneNumbers,String param) throws Exception {
		com.aliyun.dysmsapi20170525.Client client = SMSUtils.createClient(SMSConfig.ACCESSKEY_ID, SMSConfig.ACCESSKEY_SECRET);
		com.aliyun.dysmsapi20170525.models.SendSmsRequest sendSmsRequest = new com.aliyun.dysmsapi20170525.models.SendSmsRequest()
				.setSignName(SMSConfig.SIGN_NAME)
				.setTemplateCode(SMSConfig.TEMPLATE_CODE)
				.setPhoneNumbers(phoneNumbers)
				.setTemplateParam("{\"code\":\""+param+"\"}");
		com.aliyun.teautil.models.RuntimeOptions runtime = new com.aliyun.teautil.models.RuntimeOptions();
		try {
			// 复制代码运行请自行打印 API 的返回值
			client.sendSmsWithOptions(sendSmsRequest, runtime);
		} catch (TeaException error) {
			// 如有需要，请打印 error
			com.aliyun.teautil.Common.assertAsString(error.message);
		} catch (Exception _error) {
			TeaException error = new TeaException(_error.getMessage(), _error);
			// 如有需要，请打印 error
			com.aliyun.teautil.Common.assertAsString(error.message);
		}
	}

}
