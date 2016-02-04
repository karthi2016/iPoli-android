package io.ipoli.android.app.services;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Locale;

import javax.inject.Inject;

import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.navigation.ActivityIntentFactory;
import io.ipoli.android.chat.ChatActivity;
import io.ipoli.android.quest.Quest;
import io.ipoli.android.quest.activities.PlanDayActivity;
import io.ipoli.android.quest.activities.QuestActivity;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.quest.receivers.SnoozeQuestReceiver;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 9/21/15.
 */
public class ReminderIntentService extends IntentService {

    public static final String ACTION_REMIND_PLAN_DAY = "io.ipoli.android.action.REMIND_PLAN_DAY";
    public static final String ACTION_REMIND_REVIEW_DAY = "io.ipoli.android.action.REMIND_REVIEW_DAY";
    public static final String ACTION_REMIND_START_QUEST = "io.ipoli.android.action.REMIND_START_QUEST";

    @Inject
    QuestPersistenceService questPersistenceService;

    public ReminderIntentService() {
        super("iPoli-ReminderIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null || TextUtils.isEmpty(intent.getAction())) {
            Log.d("iPoli", getClass().getSimpleName() + " was started without intent or action");
            return;
        }

        App.getAppComponent(this).inject(this);
        String action = intent.getAction();
        if (action.equals(ACTION_REMIND_PLAN_DAY)) {

            Intent planDayIntent = new Intent(this, PlanDayActivity.class);
            planDayIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            planDayIntent.setAction(ACTION_REMIND_PLAN_DAY);

            PendingIntent pendingNotificationIntent = PendingIntent.getActivity(this, 0, planDayIntent, PendingIntent.FLAG_ONE_SHOT);

            Bitmap largeIcon = BitmapFactory.decodeResource(this.getResources(), R.mipmap.ic_launcher);

            NotificationCompat.Builder builder = (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                    .setContentTitle("New day ahead")
                    .setContentText("Ready to plan your great day?")
                    .setSmallIcon(R.drawable.ic_notification_small)
                    .setLargeIcon(largeIcon)
                    .setOnlyAlertOnce(true)
                    .setAutoCancel(true)
                    .setContentIntent(pendingNotificationIntent)
                    .setDefaults(NotificationCompat.DEFAULT_VIBRATE)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
            notificationManagerCompat.notify(Constants.REMIND_PLAN_DAY_NOTIFICATION_ID, builder.build());
        } else if (action.equals(ACTION_REMIND_REVIEW_DAY)) {

            Intent reviewDayIntent = new Intent(this, ChatActivity.class);
            reviewDayIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            reviewDayIntent.setAction(ACTION_REMIND_REVIEW_DAY);

            PendingIntent pendingNotificationIntent = PendingIntent.getActivity(this, 0, reviewDayIntent, PendingIntent.FLAG_ONE_SHOT);

            Bitmap largeIcon = BitmapFactory.decodeResource(this.getResources(), R.mipmap.ic_launcher);

            NotificationCompat.Builder builder = (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                    .setContentTitle("Day in review")
                    .setContentText("Ready to review your day?")
                    .setSmallIcon(R.drawable.ic_notification_small)
                    .setLargeIcon(largeIcon)
                    .setOnlyAlertOnce(true)
                    .setAutoCancel(true)
                    .setContentIntent(pendingNotificationIntent)
                    .setDefaults(NotificationCompat.DEFAULT_VIBRATE)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
            notificationManagerCompat.notify(Constants.REMIND_REVIEW_DAY_NOTIFICATION_ID, builder.build());
        } else if (action.equals(ACTION_REMIND_START_QUEST)) {
            String questId = intent.getStringExtra(Constants.QUEST_ID_EXTRA_KEY);
            Quest q = questPersistenceService.findById(questId);
            if (q == null) {
                return;
            }
            Intent remindStartQuestIntent = new Intent(this, QuestActivity.class);
            remindStartQuestIntent.setAction(ACTION_REMIND_START_QUEST);
            remindStartQuestIntent.putExtra(Constants.QUEST_ID_EXTRA_KEY, questId);
            String name = q.getName();

            PendingIntent pendingNotificationIntent = ActivityIntentFactory.createWithParentStack(QuestActivity.class, remindStartQuestIntent, this);

            Bitmap largeIcon = BitmapFactory.decodeResource(this.getResources(), R.mipmap.ic_launcher);

            PendingIntent startQuestPI = getStartPendingIntent(q.getId());
            PendingIntent snoozeQuestPI = getSnoozePendingIntent(q.getId());

            NotificationCompat.Builder builder = (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                    .setContentTitle(name)
                    .setContentText("Ready to start?")
                    .setContentIntent(pendingNotificationIntent)
                    .setShowWhen(false)
                    .setContentInfo(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(q.getStartTime()))
                    .setSmallIcon(R.drawable.ic_notification_small)
                    .setLargeIcon(largeIcon)
                    .setOnlyAlertOnce(true)
                    .setAutoCancel(true)
                    .addAction(R.drawable.ic_snooze_black_24dp, "SNOOZE", snoozeQuestPI)
                    .addAction(R.drawable.ic_play_arrow_black_24dp, "START", startQuestPI)
                    .setDefaults(NotificationCompat.DEFAULT_VIBRATE | NotificationCompat.DEFAULT_SOUND | NotificationCompat.DEFAULT_LIGHTS)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
            notificationManagerCompat.notify(Constants.REMIND_START_QUEST_NOTIFICATION_ID, builder.build());
        }
    }

    private PendingIntent getStartPendingIntent(String questId) {
        Intent intent = new Intent(this, QuestActivity.class);
        intent.putExtra(Constants.QUEST_ID_EXTRA_KEY, questId);
        intent.setAction(QuestActivity.ACTION_START_QUEST);

        return ActivityIntentFactory.createWithParentStack(QuestActivity.class, intent, this);
    }

    private PendingIntent getSnoozePendingIntent(String questId) {
        Intent intent = new Intent(SnoozeQuestReceiver.ACTION_SNOOZE_QUEST);
        intent.putExtra(Constants.QUEST_ID_EXTRA_KEY, questId);
        return PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
    }
}