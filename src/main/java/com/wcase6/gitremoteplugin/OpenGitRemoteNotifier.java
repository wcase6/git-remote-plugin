package com.wcase6.gitremoteplugin;

import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;

public class OpenGitRemoteNotifier {

    public static void notifyError(Project project, String content) {
        NotificationGroupManager.getInstance()
                .getNotificationGroup("Git Remote Group")
                .createNotification(content, NotificationType.ERROR)
                .notify(project);
    }

}
