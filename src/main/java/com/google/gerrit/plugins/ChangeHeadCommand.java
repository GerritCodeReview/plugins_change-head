// Copyright (C) 2012 The Android Open Source Project
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.gerrit.plugins;

import com.google.gerrit.extensions.annotations.Export;
import com.google.gerrit.server.git.GitRepositoryManager;
import com.google.gerrit.server.project.ProjectControl;
import com.google.gerrit.sshd.SshCommand;
import com.google.inject.Inject;

import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.RefUpdate;
import org.eclipse.jgit.lib.Repository;
import org.kohsuke.args4j.Argument;

@Export("change-head")
public final class ChangeHeadCommand extends SshCommand {

    @Argument(index = 0, required = true, metaVar = "REPO", usage = "Project to change HEAD for")
    private ProjectControl project;

    @Argument(index = 1, required = true, metaVar = "BRANCH", usage = "Target branch to point HEAD at")
    private String newHeadBranch;

    @Inject
    private GitRepositoryManager repoManager;

    @Override
    protected void run() throws UnloggedFailure, Failure, Exception {
      final Repository repo = repoManager.openRepository(project.getProject().getNameKey());

      if( !project.isOwner() ) {
        stdout.print( "Permission denied: you do not have ownership rights to " +
            project.getProject().getName() );
        return;
      }

      final Ref ref = repo.getRef(newHeadBranch);
      if (ref == null) {
        stdout.print("Destination branch " + newHeadBranch + " does not exist.\n");
        return;
      }

      final RefUpdate u = repo.updateRef(Constants.HEAD);
      u.disableRefLog();
      u.link(ref.getName());

      stdout.print("HEAD for project " + project.getProject().getName() +
          " now pointing at " + ref.getName() + ".\n");
    }
}
