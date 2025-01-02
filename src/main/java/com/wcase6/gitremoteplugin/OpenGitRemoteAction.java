package com.wcase6.gitremoteplugin;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import git4idea.GitLocalBranch;
import git4idea.repo.GitRemote;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryManager;

import java.util.Objects;
import java.util.Optional;

public class OpenGitRemoteAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        if (e.getProject() == null) {
            OpenGitRemoteNotifier.notifyError(null, "Unable to identify project for this file");
            return;
        }

        if (e.getProject().getBasePath() == null) {
            OpenGitRemoteNotifier.notifyError(e.getProject(), "Unable to get base path for this project");
            return;
        }

        PsiFile file = e.getData(CommonDataKeys.PSI_FILE);
        if (file == null || !file.isValid()) {
            OpenGitRemoteNotifier.notifyError(e.getProject(), "The requested file is null or invalid");
            return;
        }

        VirtualFile vFile = file.getVirtualFile();
        GitRepositoryManager repositoryManager = GitRepositoryManager.getInstance(e.getProject());
        Optional<GitRepository> repository = repositoryManager.getRepositories().stream().findFirst();

        if (repository.isEmpty()) {
            OpenGitRemoteNotifier.notifyError(e.getProject(), "Unable to determine git repository");
            return;
        }

        Optional<String> url = repository.get().getRemotes().stream()
                        .map(GitRemote::getFirstUrl)
                        .filter(Objects::nonNull)
                        .findFirst();

        if (url.isEmpty()) {
            OpenGitRemoteNotifier.notifyError(null, "Unable to determine remote git url");
            return;
        }

        String httpUrl = url.get().replaceAll("git@|git://|git@ssh\\.", "https://")
                .replaceAll("com:", "com/")
                .replaceAll("\\.git", "");

        Optional<String> hash = repository
                .map(GitRepository::getCurrentRevision);
        Optional<String> branch = repository
                .map(GitRepository::getCurrentBranch)
                .map(GitLocalBranch::getName);

        String blob = hash.orElse(branch.orElse("master"));

        String relativeFilePath = vFile.getPath().replace(e.getProject().getBasePath(), "");
        BrowserUtil.browse(httpUrl + "/blob/" + blob + relativeFilePath);
    }
}
