package org.mortar.client.services;

import java.util.Date;

import org.mortar.client.Logger;
import org.mortar.client.msg.ReceivedMessage;
import org.mortar.common.MessageSerializer;
import org.mortar.common.Utils;
import org.mortar.common.data.GsmMessage;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class SMSReceiver extends BroadcastReceiver {
	private MessageSerializer serializer = new MessageSerializer(ReceivedMessage.class.getPackage().getName());
	@Override
	public void onReceive(Context context, Intent intent) {
		Logger logger = new Logger(context);
		try {
			if (intent.hasExtra("com.parse.Data")) {
				onReceivePush(context, intent, logger);
			} else {
				onReceiveSMS(context, intent, logger);
			}
		} catch (Exception ex) {
			Utils.handle(ex, context);
		} finally {
			logger.close();
		}
	}

	private void onReceivePush(Context context, Intent intent, Logger logger) throws Exception {
		final String pkg = ReceivedMessage.class.getPackage().getName();
		String rawJson = intent.getExtras().getString("com.parse.Data");

		Gson gson = new Gson();
		JsonParser parser = new JsonParser();

		JsonElement json = parser.parse(rawJson);
		String type = json.getAsJsonObject().get("type").getAsString();
		Class<?> target = Class.forName(pkg + '.' + type);
		ReceivedMessage message = (ReceivedMessage) gson.fromJson(json, target);
		String msg = message.getClass().getSimpleName() + ":" + intent.getExtras();
		logger.log("push: " + msg);
		message.onReceive(context);
	}

	private void onReceiveSMS(Context context, Intent intent, Logger logger) throws Exception {
		Bundle extras = intent.getExtras();
		GsmMessage sms = GsmMessage.createFromBundle(extras);
		if (sms == null) {
			sms = (GsmMessage) extras.getSerializable("mortar");
		}
		ReceivedMessage message = (ReceivedMessage) serializer.deserialize(sms.contents);
		String msg = message.getClass().getSimpleName() + " from " + sms.from + " (" + new Date(sms.timestamp) + ")";
		logger.log("sms: " + msg);
		message.onReceive(context);
	}
}
